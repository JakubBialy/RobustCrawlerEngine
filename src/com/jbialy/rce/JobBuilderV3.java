package com.jbialy.rce;


import com.jbialy.rce.collections.CollectionWorkspace;
import com.jbialy.rce.collections.JobWorkspace;
import com.jbialy.rce.collections.WorkSpaceHelper;
import com.jbialy.rce.downloader.Receiver;
import com.jbialy.rce.sneakytry.ThrowableBiConsumer;

import java.io.Closeable;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("jol")
public class JobBuilderV3<D, U extends Comparable<U>> {
    private final Supplier<JobWorkspace<U>> workspaceSupplier;
    private EngineConfigV3 configV3 = EngineConfigV3.DEFAULT_CONFIG;
    private Function<DownloadResult<D, U>, Collection<U>> uriExtractor = downloadResult -> List.of();
    private CallbackTrigger<JobWorkspace<U>> checkpointCallbackTrigger = CallbackTrigger.createEmpty();
    private Receiver<?, DownloadResult<D, U>> responsesHandler = new Receiver<>(() -> null, (closeable, euDownloadResult) -> {
    });
    private Runnable onCompleteListener = () -> {
    };
    private Supplier<GenericHttpClient_V3_2<D, U>> httpClientSupplier_V3_2 = null;

    private Predicate<DownloadResult<D, U>> safetySwitchPredicate = (x) -> false;
    private Predicate<DownloadResult<D, U>> passToReceiverPredicate;

    private Function<GenericHttpRequest<U>, DownloadResponse<D, U>> extraFunction = null;
    private CallbackTrigger<JobStatistics> progressUpdatesCallbackTrigger;

    public JobBuilderV3<D, U> setDownloader(Downloader<D, U> downloader) {
        this.downloader = downloader;

        return this;
    }

    private Downloader<D, U> downloader = null;

    public JobBuilderV3(Supplier<JobWorkspace<U>> workspaceSupplier) {
        this.workspaceSupplier = workspaceSupplier;
    }

    public JobBuilderV3(U... uris) {
        this.workspaceSupplier = () -> WorkSpaceHelper.fromCollectionAsSet(Arrays.asList(uris));
    }

    public static JobBuilderV3<byte[], URI> simpleUriBuilder(CollectionWorkspace<URI> workspace) {
        final Supplier<GenericHttpClient_V3_2<byte[], URI>> httpClient = (Supplier<GenericHttpClient_V3_2<byte[], URI>>) () -> HttpClientHelper.createDefaultHttpClient_V3_2();
        return new JobBuilderV3<byte[], URI>(() -> workspace)
//                .setRequestMaker(GenericRequestHelper::uriGenericGetRequest)
//                .setHttpClientSupplier(HttpClientHelper::createDefaultHttpClient)
//                .setHttpClientSupplier_V3_2(HttpClientHelper::createDefaultHttpClient_V3_2)
//                .setHttpClientSupplier_V3_2(httpClient)
                .setPassToReceiverPredicate(downloadResult -> true)
                .setConfig(new EngineConfigV3(1, 1, 8L))
                .setSafetySwitch(downloadResult -> {
                    try {
                        if (downloadResult != null) {
                            if (downloadResult.getException() != null && downloadResult.getException().getMessage().contains("GOAWAY")) {
                                Thread.sleep(1_000);

                                return false;
                            } else if (downloadResult.getResponse() != null) {
                                final int statusCode = downloadResult.getResponse().statusCode();
                                return 400 <= statusCode && statusCode < 500;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();

                        return true;
                    }
                })
                .setonCompleteListener(() -> System.out.println("Done!"));
    }

    public static JobBuilderV3<byte[], URI> simpleUriBuilder(Path workspaceFilePath, URI seed) {
        return simpleUriBuilder(CollectionWorkspace.fromFileOrElseCreateFromSeed(URI.class, workspaceFilePath, seed));
    }


//    public static <D extends DownloadResultInterface<byte[], URI>> JobBuilderV3<D, byte[], URI> simpleUriBuilder(URI... uris) {
//        return simpleUriBuilder(CollectionWorkspace.fromToDoCollection(Arrays.asList(uris)));
//    }

    public JobBuilderV3<D, U> checkpointCallback(Predicate<CallbackState> callbackPredicate, DataCallback<JobWorkspace<U>> callback) {
        this.checkpointCallbackTrigger = new CallbackTrigger<>(callbackPredicate, callback);
        return this;
    }

    public JobBuilderV3<D, U> setSafetySwitch(Predicate<DownloadResult<D, U>> safetySwitchPredicate) {
        this.safetySwitchPredicate = safetySwitchPredicate;
        return this;
    }

    public JobBuilderV3<D, U> setPassToReceiverPredicate(Predicate<DownloadResult<D, U>> passToReceiverPredicate) {
        this.passToReceiverPredicate = passToReceiverPredicate;
        return this;
    }

    public JobBuilderV3<D, U> setConfig(EngineConfigV3 config) {
        this.configV3 = config;
        return this;
    }


    public JobBuilderV3<D, U> progressUpdate(Predicate<CallbackState> callbackPredicate, DataCallback<JobStatistics> callback) {
        this.progressUpdatesCallbackTrigger = new CallbackTrigger<>(callbackPredicate, callback);
        return this;
    }

    public JobBuilderV3<D, U> progressUpdate(CallbackTrigger<JobStatistics> progressUpdatesCallbackTrigger) {
        this.progressUpdatesCallbackTrigger = progressUpdatesCallbackTrigger;
        return this;
    }

    public JobBuilderV3<D, U> setonCompleteListener(Runnable onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
        return this;
    }

//    public JobBuilderV3<D, U> setHttpClientSupplier(Supplier<GenericHttpClient<T, U>> httpClientSupplier) {
//        this.httpClientSupplier = httpClientSupplier;
//        return this;
//    }

    public JobBuilderV3<D, U> setUriExtractor(Function<DownloadResult<D, U>, Collection<U>> uriExtractor) {
        this.uriExtractor = uriExtractor;
        return this;
    }

    public JobBuilderV3<D, U> setReceiver(Receiver<?, DownloadResult<D, U>> receiver) {
        this.responsesHandler = receiver;
        return this;
    }

    public <I extends Closeable> JobBuilderV3<D, U> setReceiver(Supplier<I> consumerSupplier, ThrowableBiConsumer<I, DownloadResult<D, U>> consumerAction) {
        this.responsesHandler = new Receiver<>(consumerSupplier, consumerAction);
        return this;
    }

    public JobDataV3<D, U> build() {
        return new JobDataV3<>() {
            @Override
            public EngineConfigV3 getConfig() {
                return configV3;
            }

            @Override
            public Supplier<JobWorkspace<U>> workspaceSupplier() {
                return workspaceSupplier;
            }

            @Override
            public Receiver<?, DownloadResult<D, U>> responsesHandler() {
                return responsesHandler;
            }

            @Override
            public Runnable getOnCompleteListener() {
                return onCompleteListener;
            }

            @Override
            public Function<DownloadResult<D, U>, Collection<U>> urisExtractor() {
                return uriExtractor;
            }


            @Override
            public Predicate<DownloadResult<D, U>> getSafetySwitchPredicate() {
                return safetySwitchPredicate;
            }

            @Override
            public Predicate<DownloadResult<D, U>> getPassToReceiverPredicate() {
                return passToReceiverPredicate;
            }

            @Override
            public CallbackTrigger<JobWorkspace<U>> getCheckpointCallbackTrigger() {
                return checkpointCallbackTrigger;
            }

            @Override
            public CallbackTrigger<JobStatistics> getProgressUpdatesCallbackTrigger() {
                return progressUpdatesCallbackTrigger;
            }

            @Override
            public Downloader<D, U> getDownloadModule() {
//                return SimpleDownloadModuleImpl()
                return downloader;
            }
        };
    }
}
