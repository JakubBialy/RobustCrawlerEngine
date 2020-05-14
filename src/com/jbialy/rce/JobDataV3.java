package com.jbialy.rce;

import com.jbialy.rce.collections.JobWorkspace;
import com.jbialy.rce.downloader.Receiver;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

//public interface JobDataV3<T, U, > { //E - Extra | U - URI
public interface JobDataV3<D, U> { //E - Extra | U - URI
    EngineConfigV3 getConfig();

    Supplier<JobWorkspace<U>> workspaceSupplier();

    Receiver<?, DownloadResult<D, U>> responsesHandler();

    Runnable getOnCompleteListener();

    Function<DownloadResult<D, U>, Collection<U>> urisExtractor();

    Predicate<DownloadResult<D, U>> getSafetySwitchPredicate();

    Predicate<DownloadResult<D, U>> getPassToReceiverPredicate();

    CallbackTrigger<JobWorkspace<U>> getCheckpointCallbackTrigger();

    CallbackTrigger<JobStatistics> getProgressUpdatesCallbackTrigger();

    Downloader<D, U> getDownloadModule();
}
