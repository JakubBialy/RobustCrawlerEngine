package com.jbialy.rce.downloader.core;

import com.jbialy.rce.ThrowingRunnable;
import com.jbialy.rce.callbacks.CallbackTrigger;
import com.jbialy.rce.collections.workspace.Workspace;
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
    private final List<Job<?, ?>> internalJobs;
    private final EngineState engineState;
    private final CountDownLatch finishedJobsLatch;

    public CrawlerEngine(Collection<Job<?, ?>> jobs) {
        this.internalJobs = List.copyOf(jobs);
        this.engineState = new EngineState();
        this.finishedJobsLatch = new CountDownLatch(this.internalJobs.size());
    }

    public CrawlerEngine(Job<?, ?>... jobs) {
        this(jobsTableToArray(jobs));
    }

    private static List<Job<?, ?>> jobsTableToArray(Job<?, ?>... jobs) {
        List<Job<?, ?>> result = new ArrayList<>(jobs.length);
        result.addAll(Arrays.asList(jobs));
        return result;
    }

    @NonNull
    private static <E> Flowable<E> workspaceToFlowable(long desiredRatePerSecond, SafetySwitch<?> requestSafetySwitch, EngineState engineState, Workspace<E> workspace) {
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

    private static <U, D> ThrowingRunnable<Throwable> makeRunnable(EngineState engineState, Job<D, U> job) {
        return () -> {
            //Job environment
            final EngineConfig config = job.getConfig();
            final Workspace<U> workspace = job.workspaceSupplier().get();
            final SafetySwitch<DownloadResult<D, U>> requestSafetySwitch = new SafetySwitch<>(job.getSafetySwitchPredicate());
            final Downloader<D, U> downloader = job.getDownloadModule();

            //Compute
            final Predicate<DownloadResult<D, U>> passToReceiverPredicate = job.getPassToReceiverPredicate();
            final Function<DownloadResult<D, U>, Collection<U>> urisExtractor = job.urisExtractor();

            //Listeners
            final Runnable onCompleteListener = job.getOnCompleteListener();
            final CallbackTrigger<Workspace<U>> checkpointCallbackTrigger = job.getCheckpointCallbackTrigger();
            final CallbackTrigger<JobStatistics> progressCallbackTrigger = job.getProgressUpdatesCallbackTrigger();

            try (final ReceiverInterface<DownloadResult<D, U>> responseReceiver = job.responsesHandler()) {
                workspaceToFlowable(config.getMaxRate(), requestSafetySwitch, engineState, workspace)
                        //DOWNLOAD
                        .subscribeOn(Schedulers.io())
                        .parallel(config.getMaxDownloadingThreadsNum(), 1)
                        .runOn(Schedulers.io(), 1)
                        .map(uri -> downloader.download(uri, requestSafetySwitch))
                        .sequential()
                        //PROCESSING
                        .parallel(config.getMaxProcessingThreadsNum(), 1)
                        .runOn(Schedulers.io())
                        .doOnNext(dr -> workspace.addAllToDo(urisExtractor.apply(dr)))
                        .doOnNext(dr -> {
                            if (passToReceiverPredicate.test(dr)) {
                                workspace.getJobStatistics().incrementTasksSentToReceiver();
                                responseReceiver.accept(dr);
                            }
                        })
                        .sequential()
                        //POST-PROCESSING
                        .doOnNext(dr -> {
                            if (dr.getException() != null) {
                                if (dr.getRequest() != null)
                                    workspace.moveToDamaged(dr.getRequest().getUri());

                                if (dr.getResponse() != null)
                                    workspace.moveToDamaged(dr.getResponse().uri());
                            } else {
                                workspace.moveToDone(dr.getRequest().getUri(), dr.getResponse().uri());
                            }
                        })
                        .doOnNext(dr -> progressCallbackTrigger.tryTrigger(() -> workspace.getJobStatistics().copy()))
                        .doOnNext(dr -> checkpointCallbackTrigger.tryTrigger(workspace::copy))
                        .doOnComplete(onCompleteListener::run)
                        .blockingSubscribe(next -> {
                        }, Throwable::printStackTrace, () -> {
                            progressCallbackTrigger.forceTrigger(workspace.getJobStatistics().copy());
                            checkpointCallbackTrigger.forceTrigger(workspace.copy());
                        });
            }
        };
    }

    public void start() {
        Iterator<Job<?, ?>> jobsIterator = internalJobs.iterator();

        Flowable.<ThrowingRunnable<Throwable>, Iterator<Job<?, ?>>>generate(() -> jobsIterator, (state, emitter) -> {
            if (jobsIterator.hasNext()) {
                final Job<?, ?> job = jobsIterator.next();
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
