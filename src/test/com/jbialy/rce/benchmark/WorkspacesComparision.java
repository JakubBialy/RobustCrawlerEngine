package com.jbialy.rce.benchmark;

import com.jbialy.rce.collections.workspace.Workspace;
import com.jbialy.rce.collections.workspace.implementation.GeneralPurposeWorkspace;
import com.jbialy.rce.collections.workspace.implementation.PackedUriIntRangeWorkspace;
import com.jbialy.rce.utils.server.MockHtmlUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.jbialy.rce.utils.Utils.shuffleArray;

@State(Scope.Benchmark)
public class WorkspacesComparision {
    @Param({
            "10000", "50000", "250000", "1000000"
    })
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
                .warmupIterations(2)
                .jvmArgsAppend("-Xmx8g")
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public static void PackedUriIntRangeWorkspace_add(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = new PackedUriIntRangeWorkspace("https://localhost/test?id=", "");

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void GeneralPurposeWorkspace_add_crawlerEngineExtractedUris(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = GeneralPurposeWorkspace.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void GeneralPurposeWorkspace_simulate_crawlerEngine(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = GeneralPurposeWorkspace.createEmpty();
        workspace.add(mockData.getCrawlerExtractedUrisMock()[0].get(0)); //add seed

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            final URI currentUri = workspace.moveToProcessingAndReturnWaitIfInProgressIsNotEmpty();
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
            workspace.moveToDone(currentUri);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void GeneralPurposeWorkspace_add_and_get(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = GeneralPurposeWorkspace.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
        }

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.moveToProcessingAndReturn();
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void GeneralPurposeWorkspace_add(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = GeneralPurposeWorkspace.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void GeneralPurposeWorkspace_add_twice(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = GeneralPurposeWorkspace.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
            workspace.add(mockData.mockUris[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void PackedUriIntRangeWorkspace_add_crawlerEngineExtractedUris(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = new PackedUriIntRangeWorkspace("https://localhost/test?id=", "");

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void PackedUriIntRangeWorkspace_simulate_crawlerEngine(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = new PackedUriIntRangeWorkspace("https://localhost/test?id=", "");
        workspace.add(mockData.getCrawlerExtractedUrisMock()[0].get(0)); //add seed

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            final URI currentUri = workspace.moveToProcessingAndReturnWaitIfInProgressIsNotEmpty();
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
            workspace.moveToDone(currentUri);
        }

        blackhole.consume(workspace);
    }


    @Benchmark
    public void PackedUriIntRangeWorkspace_add_and_get(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = new PackedUriIntRangeWorkspace("https://localhost/test?id=", "");

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
        }

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.moveToProcessingAndReturn();
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void PackedUriIntRangeWorkspace_add_twice(Blackhole blackhole, Mock mockData) {
        Workspace<URI> workspace = new PackedUriIntRangeWorkspace("https://localhost/test?id=", "");

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(mockData.mockUris[i]);
            workspace.add(mockData.mockUris[i]);
        }

        blackhole.consume(workspace);
    }

    /*
        @Benchmark
        public void CollectionWorkspaceOld_simulate_crawlerEngine(Blackhole blackhole, Mock mockData) {
            CollectionWorkspaceOld<URI> workspace = new CollectionWorkspaceOld<>(List.of());
            workspace.addToDo(mockData.getCrawlerExtractedUrisMock()[0].get(0)); //add seed

            for (int i = 0; i < OPERATIONS_COUNT; i++) {
                final URI currentUri = workspace.getAndMarkAsInProgress();
                workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
                workspace.markAsDone(currentUri);
            }

            blackhole.consume(workspace);
        }

        @Benchmark
        public void CollectionWorkspaceOld_add_crawlerEngineExtractedUris(Blackhole blackhole, Mock mockData) {
            CollectionWorkspaceOld<URI> workspace = new CollectionWorkspaceOld<>(List.of());

            for (int i = 0; i < OPERATIONS_COUNT; i++) {
                workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
            }

            blackhole.consume(workspace);
        }

        @Benchmark
        public void CollectionWorkspaceOld_add(Blackhole blackhole, Mock mockData) {
            CollectionWorkspaceOld<URI> workspace = new CollectionWorkspaceOld<>(List.of());

            for (int i = 0; i < OPERATIONS_COUNT; i++) {
                workspace.addToDo(mockData.mockUris[i]);
            }

            blackhole.consume(workspace);
        }

        @Benchmark
        public void CollectionWorkspaceOld_add_and_get(Blackhole blackhole, Mock mockData) {
            CollectionWorkspaceOld<URI> workspace = new CollectionWorkspaceOld<>(List.of());

            for (int i = 0; i < OPERATIONS_COUNT; i++) {
                workspace.addToDo(mockData.mockUris[i]);
            }

            for (int i = 0; i < OPERATIONS_COUNT; i++) {
                workspace.getAndMarkAsInProgress();
            }

            blackhole.consume(workspace);
        }

        @Benchmark
        public void CollectionWorkspaceOld_add_twice(Blackhole blackhole, Mock mockData) {
            CollectionWorkspaceOld<URI> workspace = new CollectionWorkspaceOld<>(List.of());

            for (int i = 0; i < OPERATIONS_COUNT; i++) {
                workspace.addToDo(mockData.mockUris[i]);
                workspace.addToDo(mockData.mockUris[i]);
            }

            blackhole.consume(workspace);
        }
    */
    @State(Scope.Thread)
    public static class Mock {
        private final int HREFS_COUNT = OPERATIONS_COUNT;
        private final URI[] mockUris = new URI[HREFS_COUNT];
        private final List<URI>[] crawlerExtractedUrisMock = new List[HREFS_COUNT];

        public List<URI>[] getCrawlerExtractedUrisMock() {
            return crawlerExtractedUrisMock;
        }

        @Setup(Level.Trial)
        public void setup() {
            for (int i = 0; i < HREFS_COUNT; i++) {
                mockUris[i] = URI.create("https://localhost/test?id=" + i);
            }

            final URI baseUri = URI.create("https://localhost/");
            final List<URI> uriRange = MockHtmlUtils.createURIsRange(baseUri, 0, HREFS_COUNT);

            for (int currentPageId = 0; currentPageId < HREFS_COUNT; currentPageId++) {
                List<URI> currentPageUris = new ArrayList<>();
                for (long lastCurrentId = Math.min(128, currentPageId - 1); lastCurrentId > 0L; lastCurrentId--) {
                    final int nextId = (int) (currentPageId - lastCurrentId - 1);
                    currentPageUris.add(uriRange.get(nextId));
                }

                currentPageUris.add(uriRange.get(currentPageId));

                final long nextHrefsCount = Math.min(128, HREFS_COUNT - currentPageId - 1);
                for (long i = 0L; i < nextHrefsCount; i++) {
                    final int nextId = (int) (currentPageId + 1L + i);
                    currentPageUris.add(uriRange.get(nextId));
                }

                crawlerExtractedUrisMock[currentPageId] = currentPageUris;
            }

            shuffleArray(crawlerExtractedUrisMock);

//            System.out.print("(SETUP END) ");
        }
    }
}
