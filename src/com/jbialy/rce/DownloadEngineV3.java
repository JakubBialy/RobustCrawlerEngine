package com.jbialy.rce;

import com.jbialy.rce.collections.JobWorkspace;
import com.jbialy.rce.downloader.Receiver;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.function.Predicate;

public class DownloadEngineV3 {
    private static final int PARALLEL_JOBS = 2;
    private final List<JobDataV3<?, ?>> internalJobs;
    private final EngineState engineState;
    private final CountDownLatch finishedJobsLatch;

    public DownloadEngineV3(Collection<JobDataV3<?, ?>> jobs) {
        this.internalJobs = List.copyOf(jobs);
        this.engineState = new EngineState();
        this.finishedJobsLatch = new CountDownLatch(this.internalJobs.size());
    }

    public DownloadEngineV3(JobDataV3<?, ?>... jobs) {
        this(jobsTableToArray(jobs));
    }

    private static List<JobDataV3<?, ?>> jobsTableToArray(JobDataV3<?, ?>... jobs) {
        List<JobDataV3<?, ?>> result = new ArrayList<>(jobs.length);
        result.addAll(Arrays.asList(jobs));
        return result;
    }

    @NonNull
    private static <E> Flowable<E> workspaceToFlowable(long desiredRatePerSecond, SafetySwitch<?> requestSafetySwitch, EngineState engineState, JobWorkspace<E> workSpace) {
        return Flowable.generate(FlowableGeneratorState::new, (generatorState, emitter) -> {
            if (requestSafetySwitch.isActivated() || !engineState.isRunning() || workSpace.isToDoEmpty()) {
                emitter.onComplete();
                return generatorState;
            } else {
                final long current = System.currentTimeMillis();
                final double avgRate = generatorState.avgRate();

                if (avgRate >= desiredRatePerSecond) {
                    long ordinaryDelay = (long) (1000.0 / desiredRatePerSecond);
                    long correction = (current - generatorState.lastTimestamp);
                    long delay = ordinaryDelay - correction;

                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }

                long before = System.currentTimeMillis();
                emitter.onNext(workSpace.getAndMarkAsInProgress());

                generatorState.setLastTimestamp(before);
                generatorState.incrementCounter();
                return generatorState;
            }
        });
    }

    private static <U, D> ThrowingRunnable<Throwable> makeRunnable(EngineState engineState, JobDataV3<D, U> job) {
        return new ThrowingRunnable<>() {
            private Throwable throwableXXX = null;

            @Override
            public void run() throws Throwable {
                //Job enviroment
                final EngineConfigV3 config = job.getConfig();
                final JobWorkspace<U> jobWorkspace = job.workspaceSupplier().get();
                final SafetySwitch<DownloadResult<D, U>> requestSafetySwitch = new SafetySwitch<>(job.getSafetySwitchPredicate());
                final Downloader<D, U> downloader = job.getDownloadModule();

                //Compute
//                final ReTryPredicate<D, U> reTryPredicate = job.getReTryPredicate();
                final Predicate<DownloadResult<D, U>> passToReceiverPredicate = job.getPassToReceiverPredicate();
                final Function<DownloadResult<D, U>, Collection<U>> urisExtractor = job.urisExtractor();

                //Listeners
                final Runnable onCompleteListener = job.getOnCompleteListener();
                final CallbackTrigger<JobWorkspace<U>> checkpointCallbackTrigger = job.getCheckpointCallbackTrigger();
                final CallbackTrigger<JobStatistics> progressCallbackTrigger = job.getProgressUpdatesCallbackTrigger();

                try (final Receiver<?, DownloadResult<D, U>> responseReceiver = job.responsesHandler()) {
                    workspaceToFlowable(config.getMaxRate(), requestSafetySwitch, engineState, jobWorkspace)
                            //DOWNLOAD
                            .subscribeOn(Schedulers.io())
                            .parallel(config.getMaxDownloadingThreads(), 1)
                            .runOn(Schedulers.io(), 1)
                            .map(uri -> downloader.download(uri, requestSafetySwitch))
                            .sequential()
                            //PROCESSING
                            .parallel(config.getMaxResponseProcessingThreads(), 1)
                            .runOn(Schedulers.io())
                            .doOnNext(dr -> jobWorkspace.addAllToDo(urisExtractor.apply(dr)))
                            .doOnNext(dr -> {
                                if (passToReceiverPredicate.test(dr)) {
                                    jobWorkspace.getJobStatistics().incrementTasksSentToReceiver();
                                    responseReceiver.accept(dr);
                                }
                            })
                            .sequential()
                            //POST-PROCESSING
                            .doOnNext(dr -> jobWorkspace.markAsDone(dr.getRequest().getUri(), dr.getResponse().uri()))
                            .doOnNext(dr -> progressCallbackTrigger.tryTrigger(() -> jobWorkspace.getJobStatistics().copy()))
                            .doOnNext(dr -> checkpointCallbackTrigger.tryTrigger(jobWorkspace::copy))
                            .doOnComplete(onCompleteListener::run)
                            .blockingSubscribe(next -> {

                            }, throwable -> {
                                throwable.printStackTrace();
                                throwableXXX = throwable;

                            }, () -> {
                            });
                }

                if (throwableXXX != null) {
                    throw throwableXXX;
                }
            }
        };
    }

//    @NotNull
//    private static <T, U, E> DownloadResultImpl<T, U, E> dispatchRequest(
//            GenericHttpClient<T, U, E> httpClient,
//            GenericHttpRequest<U> request,
//            ReTryPredicate<T, U, E> reTryPredicate,
//            SafetySwitch<DownloadResultImpl<T, U, E>> requestSafetySwitch,
//            BiFunction<GenericHttpRequest<U>, GenericHttpResponse<T, U>, E> extraFunction
//    ) {
//
//        DownloadResultImpl<T, U, E> httpResponse;
//        long requestAttempt = 1;
//        do {
//            httpResponse = httpClient.sendRequest(request, extraFunction);
//        } while (requestSafetySwitch.safeTestPass(httpResponse) && reTryPredicate.shouldTry(httpResponse, requestAttempt++));
//
//        return httpResponse;
//    }

  /*  @NotNull
    private static <T, U, E> DownloadResultImpl<T, U, E> dispatchRequest(
            GenericHttpClient<T, U, E> httpClient,
            GenericHttpRequest<U> request,
            ReTryPredicate<T, U, E> reTryPredicate,
            SafetySwitch<DownloadResultImpl<T, U, E>> requestSafetySwitch,
            BiFunction<GenericHttpRequest<U>, GenericHttpResponse<T, U>, E> extraFunction
    ) {

        DownloadResultImpl<T, U, E> httpResponse;
        long requestAttempt = 1;
        do {
            httpResponse = httpClient.sendRequest(request, extraFunction);
        } while (requestSafetySwitch.safeTestPass(httpResponse) && reTryPredicate.shouldTry(httpResponse, requestAttempt++));

        return httpResponse;
    }*/

//    @NotNull
//    private static <T, U> DownloadResultInterface<T, U> dispatchRequest_V3_2(
//            GenericHttpClient_V3_2<DownloadResultInterface<T, U>, T, U> httpClient,
//            GenericHttpRequest<U> request,
//            ReTryPredicate<T, U> reTryPredicate,
//            SafetySwitch<DownloadResultInterface<T, U>> requestSafetySwitch
//    ) {
//
//        DownloadResultInterface<T, U> httpResponse;
//        long requestAttempt = 1;
//        do {
//            httpResponse = httpClient.sendRequest(request);
//        } while (requestSafetySwitch.safeTestPass(httpResponse) && reTryPredicate.shouldTry(httpResponse, requestAttempt++));
//
//        return httpResponse;
//    }

    @NotNull
    private static <D, U> DownloadResult<D, U> dispatchRequest_V3_2_1(
            GenericHttpClient_V3_2<D, U> httpClient,
            GenericHttpRequest<U> request,
            ReTryPredicate<D, U> reTryPredicate,
            SafetySwitch<DownloadResult<D, U>> requestSafetySwitch
    ) {

        DownloadResult<D, U> downloadResult;
        long requestAttempt = 1;
        do {
            try {
                final DownloadResponse<D, U> response = httpClient.sendRequest(request);

                downloadResult = new DownloadResult<>(null, response, request);
            } catch (Exception exception) {
                downloadResult = new DownloadResult<>(exception, null, request);
            }
        } while (requestSafetySwitch.safeTestPass(downloadResult) && reTryPredicate.shouldTry(downloadResult, requestAttempt++));

        return downloadResult;
    }

    @NotNull
    private static <T, U> DownloadResponse<T, U> createGenericResponse(@NotNull DownloadResponse<T, U> httpResponse) {
        return new DownloadResponse<>() {
            @Override
            public int statusCode() {
                return httpResponse.statusCode();
            }

            @Override
            public Map<String, List<String>> responseHeaders() {
                return httpResponse.responseHeaders();
            }

            @Override
            public T body() {
                return httpResponse.body();
            }

            @Override
            public U uri() {
                return httpResponse.uri();
            }
        };
    }

    public void start() {
        Iterator<JobDataV3<?, ?>> jobsIterator = internalJobs.iterator();

        Flowable.<ThrowingRunnable<Throwable>, Iterator<JobDataV3<?, ?>>>generate(() -> jobsIterator, (state, emitter) -> {
            if (jobsIterator.hasNext()) {
                final JobDataV3<?, ?> job = jobsIterator.next();
                emitter.onNext(makeRunnable(engineState, job));
            } else {
                emitter.onComplete();
            }
        })
                .parallel(PARALLEL_JOBS, 1)
                .runOn(Schedulers.computation(), 1)
                .flatMap(job -> Flowable.just(job)
                        .doOnNext(ThrowingRunnable::run)
                        .onErrorComplete()
                )
                .sequential()
                .subscribe(next -> {
                }, error -> { //Fatal error
                    error.printStackTrace();
                    stopEngine();
                    finishedJobsLatch.countDown();
                }, finishedJobsLatch::countDown);
    }

    public void stopEngine() {
        engineState.setRunning(false);
    }

    public boolean isRunning() {
        return engineState.isRunning();
    }

    public void awaitForFinish() throws InterruptedException {
        finishedJobsLatch.await();
    }

    static class FlowableGeneratorState {
        private final long startTimestamp;
        private long counter;
        private long lastTimestamp;

        public FlowableGeneratorState() {
            this.startTimestamp = System.currentTimeMillis();
            this.counter = 0L;
            this.lastTimestamp = 0L;
        }

        public long incrementCounter() {
            return counter++;
        }

        public void setLastTimestamp(long lastTimestamp) {
            this.lastTimestamp = lastTimestamp;
        }

        public double avgRate() {
            return (1000D * counter) / (System.currentTimeMillis() - startTimestamp);
        }
    }

    private static final class EngineState {
        private volatile boolean isRunning = true;

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }


    }
}
