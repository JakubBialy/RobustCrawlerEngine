package com.jbialy.rce.downloader;

import com.jbialy.rce.callbacks.CallbackState;
import com.jbialy.rce.callbacks.CallbackTrigger;
import com.jbialy.rce.callbacks.DataCallback;
import com.jbialy.rce.collections.workspace.Workspace;
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

public class JobBuilder<D, U extends Comparable<U>> {
    private final Supplier<Workspace<U>> workspaceSupplier;
    private EngineConfig configV3 = EngineConfig.DEFAULT_CONFIG;
    private Function<DownloadResult<D, U>, Collection<U>> uriExtractor = downloadResult -> List.of();
    private CallbackTrigger<Workspace<U>> checkpointCallbackTrigger = CallbackTrigger.createEmpty();
    private ReceiverInterface<DownloadResult<D, U>> responsesHandler = new OldReceiver<>(() -> null, (closeable, euDownloadResult) -> {
    });
    private Runnable onCompleteListener = () -> {
    };
    private Predicate<DownloadResult<D, U>> safetySwitchPredicate = x -> false;
    private Predicate<DownloadResult<D, U>> passToReceiverPredicate = x -> true;
    private Function<GenericHttpRequest<U>, DownloadResponse<D, U>> extraFunction = null;
    private CallbackTrigger<JobStatistics> progressUpdatesCallbackTrigger = new CallbackTrigger<>(x -> false, jobStatistics -> {
    });
    private Downloader<D, U> downloader = null;

    public JobBuilder(Supplier<Workspace<U>> workspaceSupplier) {
        this.workspaceSupplier = workspaceSupplier;
    }

    public JobBuilder(U... uris) {
        this.workspaceSupplier = () -> GeneralPurposeWorkspace.fromToDoCollection(Arrays.asList(uris));
    }

    public static JobBuilder<String, URI> htmlBuilder(URI... uriElements) {
        return htmlBuilder(GeneralPurposeWorkspace.fromToDoCollection(uriElements));
    }

    public static JobBuilder<String, URI> htmlBuilder(Collection<URI> uris) {
        return htmlBuilder(GeneralPurposeWorkspace.fromToDoCollection(uris));
    }
    public static JobBuilder<String, URI> htmlBuilder(Workspace<URI> workspace) {
        return new JobBuilder<String, URI>(() -> workspace)
                .setDownloader(new HtmlDownloader());
    }

    public JobBuilder<D, U> setDownloader(Downloader<D, U> downloader) {
        this.downloader = downloader;

        return this;
    }

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

    public JobBuilder<D, U> setUriExtractor(Function<DownloadResult<D, U>, Collection<U>> uriExtractor) {
        this.uriExtractor = uriExtractor;
        return this;
    }

    public JobBuilder<D, U> setReceiver(ReceiverInterface<DownloadResult<D, U>> receiver) {
        this.responsesHandler = receiver;
        return this;
    }

    public <I extends Closeable> JobBuilder<D, U> setReceiver(Supplier<I> consumerSupplier, ThrowableBiConsumer<I, DownloadResult<D, U>> consumerAction) {
        this.responsesHandler = new OldReceiver<>(consumerSupplier, consumerAction);
        return this;
    }

    public Job<D, U> build() {
        return new Job<>() {
            @Override
            public EngineConfig getConfig() {
                return configV3;
            }

            @Override
            public Supplier<Workspace<U>> workspaceSupplier() {
                return workspaceSupplier;
            }

            @Override
            public ReceiverInterface<DownloadResult<D, U>> responsesHandler() {
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
                return downloader;
            }
        };
    }
}
