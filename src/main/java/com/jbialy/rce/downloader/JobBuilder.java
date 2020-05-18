package com.jbialy.rce.downloader;


import com.jbialy.rce.callbacks.CallbackState;
import com.jbialy.rce.callbacks.CallbackTrigger;
import com.jbialy.rce.callbacks.DataCallback;
import com.jbialy.rce.collections.workspace.*;
import com.jbialy.rce.collections.workspace.implementation.GeneralPurposeWorkspace;
import com.jbialy.rce.downloader.core.*;
import com.jbialy.rce.utils.sneakytry.ThrowableBiConsumer;

import java.io.Closeable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("jol")
public class JobBuilder<D, U extends Comparable<U>> {
    private final Supplier<Workspace<U>> workspaceSupplier;
    private EngineConfig configV3 = EngineConfig.DEFAULT_CONFIG;
    private Function<DownloadResult<D, U>, Collection<U>> uriExtractor = downloadResult -> List.of();
    private CallbackTrigger<Workspace<U>> checkpointCallbackTrigger = CallbackTrigger.createEmpty();
    private Receiver<?, DownloadResult<D, U>> responsesHandler = new Receiver<>(() -> null, (closeable, euDownloadResult) -> {
    });
    private Runnable onCompleteListener = () -> {
    };
    private Supplier<GenericHttpClient<D, U>> httpClientSupplier_V3_2 = null;

    private Predicate<DownloadResult<D, U>> safetySwitchPredicate = (x) -> false;
    private Predicate<DownloadResult<D, U>> passToReceiverPredicate;

    private Function<GenericHttpRequest<U>, DownloadResponse<D, U>> extraFunction = null;
    private CallbackTrigger<JobStatistics> progressUpdatesCallbackTrigger;

    public JobBuilder<D, U> setDownloader(Downloader<D, U> downloader) {
        this.downloader = downloader;

        return this;
    }

    private Downloader<D, U> downloader = null;

    public JobBuilder(Supplier<Workspace<U>> workspaceSupplier) {
        this.workspaceSupplier = workspaceSupplier;
    }

    public JobBuilder(U... uris) {
        this.workspaceSupplier = () -> WorkspaceHelper.fromCollectionAsSet_2(Arrays.asList(uris));
    }

    public static JobBuilder<byte[], URI> simpleUriBuilder(GeneralPurposeWorkspace<URI> workspace) {
        final Supplier<GenericHttpClient<byte[], URI>> httpClient = (Supplier<GenericHttpClient<byte[], URI>>) () -> HttpClientHelper.createDefaultHttpClient();
        return new JobBuilder<byte[], URI>(() -> workspace)
//                .setRequestMaker(GenericRequestHelper::uriGenericGetRequest)
//                .setHttpClientSupplier(HttpClientHelper::createDefaultHttpClient)
//                .setHttpClientSupplier_V3_2(HttpClientHelper::createDefaultHttpClient_V3_2)
//                .setHttpClientSupplier_V3_2(httpClient)
                .setPassToReceiverPredicate(downloadResult -> true)
                .setConfig(new EngineConfig(1, 1, 8L))
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

//    public static JobBuilder<byte[], URI> simpleUriBuilder(Path workspaceFilePath, URI seed) {
//        return simpleUriBuilder(CollectionWorkspace.fromFileOrElseCreateFromSeed(URI.class, workspaceFilePath, seed));
//    }


//    public static <D extends DownloadResultInterface<byte[], URI>> JobBuilderV3<D, byte[], URI> simpleUriBuilder(URI... uris) {
//        return simpleUriBuilder(CollectionWorkspace.fromToDoCollection(Arrays.asList(uris)));
//    }

    public JobBuilder<D, U> checkpointCallback(Predicate<CallbackState> callbackPredicate, DataCallback<Workspace<U>> callback) {
        this.checkpointCallbackTrigger = new CallbackTrigger<>(callbackPredicate, callback);
        return this;
    }

    public JobBuilder<D, U> setSafetySwitch(Predicate<DownloadResult<D, U>> safetySwitchPredicate) {
        this.safetySwitchPredicate = safetySwitchPredicate;
        return this;
    }

    public JobBuilder<D, U> setPassToReceiverPredicate(Predicate<DownloadResult<D, U>> passToReceiverPredicate) {
        this.passToReceiverPredicate = passToReceiverPredicate;
        return this;
    }

    public JobBuilder<D, U> setConfig(EngineConfig config) {
        this.configV3 = config;
        return this;
    }


    public JobBuilder<D, U> progressUpdate(Predicate<CallbackState> callbackPredicate, DataCallback<JobStatistics> callback) {
        this.progressUpdatesCallbackTrigger = new CallbackTrigger<>(callbackPredicate, callback);
        return this;
    }

    public JobBuilder<D, U> progressUpdate(CallbackTrigger<JobStatistics> progressUpdatesCallbackTrigger) {
        this.progressUpdatesCallbackTrigger = progressUpdatesCallbackTrigger;
        return this;
    }

    public JobBuilder<D, U> setonCompleteListener(Runnable onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
        return this;
    }

//    public JobBuilderV3<D, U> setHttpClientSupplier(Supplier<GenericHttpClient<T, U>> httpClientSupplier) {
//        this.httpClientSupplier = httpClientSupplier;
//        return this;
//    }

    public JobBuilder<D, U> setUriExtractor(Function<DownloadResult<D, U>, Collection<U>> uriExtractor) {
        this.uriExtractor = uriExtractor;
        return this;
    }

    public JobBuilder<D, U> setReceiver(Receiver<?, DownloadResult<D, U>> receiver) {
        this.responsesHandler = receiver;
        return this;
    }

    public <I extends Closeable> JobBuilder<D, U> setReceiver(Supplier<I> consumerSupplier, ThrowableBiConsumer<I, DownloadResult<D, U>> consumerAction) {
        this.responsesHandler = new Receiver<>(consumerSupplier, consumerAction);
        return this;
    }

    public JobData<D, U> build() {
        return new JobData<>() {
            @Override
            public EngineConfig getConfig() {
                return configV3;
            }

            @Override
            public Supplier<Workspace<U>> workspaceSupplier() {
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
            public CallbackTrigger<Workspace<U>> getCheckpointCallbackTrigger() {
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
