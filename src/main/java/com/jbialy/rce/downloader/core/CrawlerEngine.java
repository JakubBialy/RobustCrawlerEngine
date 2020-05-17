package com.jbialy.rce.downloader.core;

import com.jbialy.rce.ThrowingRunnable;
import com.jbialy.rce.callbacks.CallbackTrigger;
import com.jbialy.rce.collections.workspace.JobWorkspace_2;
import com.jbialy.rce.downloader.JobStatistics;
import com.jbialy.rce.downloader.SafetySwitch;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.function.Predicate;

public class CrawlerEngine {
    private static final int PARALLEL_JOBS = 2;
    private final List<JobData<?, ?>> internalJobs;
    private final EngineState engineState;
    private final CountDownLatch finishedJobsLatch;

    public CrawlerEngine(Collection<JobData<?, ?>> jobs) {
        this.internalJobs = List.copyOf(jobs);
        this.engineState = new EngineState();
        this.finishedJobsLatch = new CountDownLatch(this.internalJobs.size());
    }

    public CrawlerEngine(JobData<?, ?>... jobs) {
        this(jobsTableToArray(jobs));
    }

    private static List<JobData<?, ?>> jobsTableToArray(JobData<?, ?>... jobs) {
        List<JobData<?, ?>> result = new ArrayList<>(jobs.length);
        result.addAll(Arrays.asList(jobs));
        return result;
    }

    @NonNull
    private static <E> Flowable<E> workspaceToFlowable(long desiredRatePerSecond, SafetySwitch<?> requestSafetySwitch, EngineState engineState, JobWorkspace_2<E> workspace) {
        return Flowable.generate(FlowableGeneratorState::new, (generatorState, emitter) -> {
            if (!requestSafetySwitch.isActivated() && engineState.isRunning()) {
                final E item = workspace.moveToProcessingAndReturnWaitIfInProgressIsNotEmpty();

                if (item != null) {
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

                    emitter.onNext(item);

                    generatorState.setLastTimestamp(before);
                    generatorState.incrementCounter();
                    return generatorState;
                }
            }

            emitter.onComplete();
            return generatorState;
        });
    }

    private static <U, D> ThrowingRunnable<Throwable> makeRunnable(EngineState engineState, JobData<D, U> job) {
        return () -> {
            //Job enviroment
            final EngineConfig config = job.getConfig();
            final JobWorkspace_2<U> jobWorkspace = job.workspaceSupplier().get();
            final SafetySwitch<DownloadResult<D, U>> requestSafetySwitch = new SafetySwitch<>(job.getSafetySwitchPredicate());
            final Downloader<D, U> downloader = job.getDownloadModule();

            //Compute
            final Predicate<DownloadResult<D, U>> passToReceiverPredicate = job.getPassToReceiverPredicate();
            final Function<DownloadResult<D, U>, Collection<U>> urisExtractor = job.urisExtractor();

            //Listeners
            final Runnable onCompleteListener = job.getOnCompleteListener();
            final CallbackTrigger<JobWorkspace_2<U>> checkpointCallbackTrigger = job.getCheckpointCallbackTrigger();
            final CallbackTrigger<JobStatistics> progressCallbackTrigger = job.getProgressUpdatesCallbackTrigger();

            try (final Receiver<?, DownloadResult<D, U>> responseReceiver = job.responsesHandler()) {
                workspaceToFlowable(config.getMaxRate(), requestSafetySwitch, engineState, jobWorkspace)
                        //DOWNLOAD
                        .subscribeOn(Schedulers.io())
                        .parallel(config.getMaxDownloadingThreadsNum(), 1)
                        .runOn(Schedulers.io(), 1)
                        .map(uri -> downloader.download(uri, requestSafetySwitch))
                        .sequential()
                        //PROCESSING
                        .parallel(config.getMaxProcessingThreadsNum(), 1)
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
                        .doOnNext(dr -> {
                            if (dr.getException() != null) {
                                jobWorkspace.moveToDamaged(dr.getRequest().getUri());
                                jobWorkspace.moveToDamaged(dr.getResponse().uri());
                            } else {
                                jobWorkspace.moveToDone(dr.getRequest().getUri(), dr.getResponse().uri());
                            }
                        })
                        .doOnNext(dr -> progressCallbackTrigger.tryTrigger(() -> jobWorkspace.getJobStatistics().copy()))
                        .doOnNext(dr -> checkpointCallbackTrigger.tryTrigger(jobWorkspace::copy))
                        .doOnComplete(onCompleteListener::run)
                        .blockingSubscribe(next -> {
                        }, Throwable::printStackTrace, () -> {
                            progressCallbackTrigger.forceTrigger(jobWorkspace.getJobStatistics().copy());
                            checkpointCallbackTrigger.forceTrigger(jobWorkspace.copy());
                        });
            }
        };
    }

    public void start() {
        Iterator<JobData<?, ?>> jobsIterator = internalJobs.iterator();

        Flowable.<ThrowingRunnable<Throwable>, Iterator<JobData<?, ?>>>generate(() -> jobsIterator, (state, emitter) -> {
            if (jobsIterator.hasNext()) {
                final JobData<?, ?> job = jobsIterator.next();
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
