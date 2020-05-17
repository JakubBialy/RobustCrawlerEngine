package com.jbialy.rce.benchmark;

import com.jbialy.rce.collections.workspace.CollectionWorkspace;
import com.jbialy.rce.collections.workspace.JobWorkspace2Impl;
import com.jbialy.rce.utils.server.MockHtmlUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.net.URI;
import java.util.List;

@State(Scope.Benchmark)
public class WorkspacesComparision {
    //    @Param({"250000"})
    @Param({"100000"})
    public static int OPERATIONS_COUNT;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WorkspacesComparision.class.getSimpleName())
                .forks(1)
                .mode(Mode.Throughput)
//                .measurementIterations(10)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .jvmArgsAppend("-Xmx8g")
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void JobWorkspace2Impl_add_crawlerEngineExtractedUris(Blackhole blackhole, Mock mockData) {
        JobWorkspace2Impl<URI> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void JobWorkspace2Impl_simulate_crawlerEngine(Blackhole blackhole, Mock mockData) {
        JobWorkspace2Impl<URI> workspace = JobWorkspace2Impl.createEmpty();
        workspace.add(mockData.getCrawlerExtractedUrisMock()[0].get(0)); //add seed

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            final URI currentUri = workspace.moveToProcessingAndReturnWaitIfInProgressIsNotEmpty();
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
            workspace.moveToDone(currentUri);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void JobWorkspace2Impl_add_and_get(Blackhole blackhole, Mock mockData) {
        JobWorkspace2Impl<URI> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
        }

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.moveToProcessingAndReturn();
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void JobWorkspace2Impl_add(Blackhole blackhole, Mock mockData) {
        JobWorkspace2Impl<URI> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void JobWorkspace2Impl_add_twice(Blackhole blackhole, Mock mockData) {
        JobWorkspace2Impl<URI> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
            workspace.add(mockData.mockUris[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void CollectionWorkspace_simulate_crawlerEngine(Blackhole blackhole, Mock mockData) {
        CollectionWorkspace<URI> workspace = new CollectionWorkspace<>(List.of());
        workspace.addToDo(mockData.getCrawlerExtractedUrisMock()[0].get(0)); //add seed

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            final URI currentUri = workspace.getAndMarkAsInProgress();
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
            workspace.markAsDone(currentUri);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void CollectionWorkspace_add_crawlerEngineExtractedUris(Blackhole blackhole, Mock mockData) {
        CollectionWorkspace<URI> workspace = new CollectionWorkspace<>(List.of());

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void CollectionWorkspace_add(Blackhole blackhole, Mock mockData) {
        CollectionWorkspace<URI> workspace = new CollectionWorkspace<>(List.of());

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addToDo(mockData.mockUris[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void CollectionWorkspace_add_and_get(Blackhole blackhole, Mock mockData) {
        CollectionWorkspace<URI> workspace = new CollectionWorkspace<>(List.of());

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addToDo(mockData.mockUris[i]);
        }

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.getAndMarkAsInProgress();
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void CollectionWorkspace_add_twice(Blackhole blackhole, Mock mockData) {
        CollectionWorkspace<URI> workspace = new CollectionWorkspace<>(List.of());

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addToDo(mockData.mockUris[i]);
            workspace.addToDo(mockData.mockUris[i]);
        }

        blackhole.consume(workspace);
    }

    @State(Scope.Thread)
    public static class Mock {
        private final int HREFS_COUNT = OPERATIONS_COUNT;
        private final URI[] mockUris = new URI[HREFS_COUNT];
        private final List<URI>[] crawlerExtractedUrisMock = new List[HREFS_COUNT];

        public List<URI>[] getCrawlerExtractedUrisMock() {
            return crawlerExtractedUrisMock;
        }

        @Setup(Level.Iteration)
        public void setup() {
            for (int i = 0; i < HREFS_COUNT; i++) {
                mockUris[i] = URI.create("https://localhost/" + i);
            }

            final URI baseUri = URI.create("https://localhost/");
            for (int i = 0; i < HREFS_COUNT; i++) {
                crawlerExtractedUrisMock[i] = MockHtmlUtils.createURIs(baseUri, i, 128, 0, 128, HREFS_COUNT);
            }
        }
    }
}
