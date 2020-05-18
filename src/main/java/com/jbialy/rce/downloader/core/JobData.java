package com.jbialy.rce.downloader.core;

import com.jbialy.rce.callbacks.CallbackTrigger;
import com.jbialy.rce.collections.workspace.Workspace;
import com.jbialy.rce.downloader.JobStatistics;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface JobData<D, U> { //D - DownloadResult | U - URI
    EngineConfig getConfig();

    Supplier<Workspace<U>> workspaceSupplier();

    Receiver<?, DownloadResult<D, U>> responsesHandler();

    Runnable getOnCompleteListener();

    Function<DownloadResult<D, U>, Collection<U>> urisExtractor();

    Predicate<DownloadResult<D, U>> getSafetySwitchPredicate();

    Predicate<DownloadResult<D, U>> getPassToReceiverPredicate();

    CallbackTrigger<Workspace<U>> getCheckpointCallbackTrigger();

    CallbackTrigger<JobStatistics> getProgressUpdatesCallbackTrigger();

    Downloader<D, U> getDownloadModule();
}
