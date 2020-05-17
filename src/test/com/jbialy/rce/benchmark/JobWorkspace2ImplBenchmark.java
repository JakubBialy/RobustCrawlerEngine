package com.jbialy.rce.benchmark;

import com.jbialy.rce.collections.workspace.JobWorkspace2Impl;
import com.jbialy.rce.server.MockHtmlUtils;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.net.URI;
import java.util.*;

import static com.jbialy.rce.utils.Utils.shuffleArray;

@State(Scope.Benchmark)
public class JobWorkspace2ImplBenchmark {
    @Param({"100000"})
    public static int OPERATIONS_COUNT;

    //    @Param({"HashSet", "LinkedHashSet", "TreeSet"})
    @Param({/*"HashSet",*/ "LinkedHashSet", "TreeSet"}) //TreeSet is definitely too slow (tested for OPERATIONS_COUNT=10000,100000)
    public static String ALL_ITEMS_COLLECTION;

    @Param({"LinkedList"/*, "ArrayDeque", "PriorityQueue"*/})
    public static String TODO_COLLECTION;

    @Param({/*"HashSet",*/ "LinkedHashSet"/*, "TreeSet"*/})
    public static String IN_PROGRESS_COLLECTION;

    @Param({/*"HashSet", */"LinkedHashSet"/*, "TreeSet"*/})
    public static String DONE_COLLECTION;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JobWorkspace2ImplBenchmark.class.getSimpleName())
                .forks(1)
                .mode(Mode.Throughput)
                .resultFormat(ResultFormatType.CSV)
//                .measurementIterations(10)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(2)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .jvmArgsAppend("-Xmx8g")
                .build();
        new Runner(opt).run();
    }

    private static <T> Set<T> createAllItemsCollection(String collName) {
        if (collName.equals("HashSet")) {
            return new HashSet<>();
        } else if (collName.equals("LinkedHashSet")) {
            return new LinkedHashSet<>();
        } else if (collName.equals("TreeSet")) {
            return new TreeSet<>();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static <T> Queue<T> createTodoCollection(String collName) {
        if (collName.equals("LinkedList")) {
            return new LinkedList<>();
        } else if (collName.equals("ArrayDeque")) {
            return new ArrayDeque<>();
        } else if (collName.equals("PriorityQueue")) {
            return new PriorityQueue<>();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static <T> Set<T> createInProgressCollection(String collName) {
        if (collName.equals("HashSet")) {
            return new HashSet<>();
        } else if (collName.equals("LinkedHashSet")) {
            return new LinkedHashSet<>();
        } else if (collName.equals("TreeSet")) {
            return new TreeSet<>();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static <T> Set<T> createDoneCollection(String collName) {
        if (collName.equals("HashSet")) {
            return new HashSet<>();
        } else if (collName.equals("LinkedHashSet")) {
            return new LinkedHashSet<>();
        } else if (collName.equals("TreeSet")) {
            return new TreeSet<>();
        } else {
            throw new IllegalArgumentException();
        }
    }
/*
    @Benchmark
    public void JobWorkspace2Impl_add_crawlerEngineExtractedUris(Blackhole blackhole, Mock mockData) {
        final Set<URI> allItems = createAllItemsCollection(ALL_ITEMS_COLLECTION);
        final Queue<URI> todo = createTodoCollection(TODO_COLLECTION);
        final Set<URI> inProgress = createInProgressCollection(IN_PROGRESS_COLLECTION);
        final Set<URI> done = createDoneCollection(DONE_COLLECTION);
        final Set<URI> damaged = createDamagedCollection(DAMAGED_COLLECTION);

        JobWorkspace2Impl<URI> workspace = JobWorkspace2Impl.createCustom(allItems, todo, inProgress, done, damaged);

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
        }

        blackhole.consume(workspace);
    }*/

    @Benchmark
    public void JobWorkspace2Impl_simulate_crawlerEngine(Blackhole blackhole, Mock mockData) {
        JobWorkspace2Impl<URI> workspace = createWorkspace();

        workspace.add(mockData.getCrawlerExtractedUrisMock()[0].get(0)); //add seed

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            final URI currentUri = workspace.moveToProcessingAndReturnWaitIfInProgressIsNotEmpty();
            workspace.addAllToDo(mockData.getCrawlerExtractedUrisMock()[i]);
            workspace.moveToDone(currentUri);
        }

        blackhole.consume(workspace);
    }

    @NotNull
    private static JobWorkspace2Impl<URI> createWorkspace() {
        final Set<URI> allItems = createAllItemsCollection(ALL_ITEMS_COLLECTION);
        final Queue<URI> todo = createTodoCollection(TODO_COLLECTION);
        final Set<URI> inProgress = createInProgressCollection(IN_PROGRESS_COLLECTION);
        final Set<URI> done = createDoneCollection(DONE_COLLECTION);
        final Set<URI> damaged = new HashSet<>();

        return JobWorkspace2Impl.createCustom(allItems, todo, inProgress, done, damaged);
    }

    @State(Scope.Thread)
    public static class Mock {
        private final int HREFS_COUNT = OPERATIONS_COUNT;
        private final List<URI>[] crawlerExtractedUrisMock = new List[HREFS_COUNT];

        public List<URI>[] getCrawlerExtractedUrisMock() {
            return crawlerExtractedUrisMock;
        }

        @Setup(Level.Iteration)
        public void setup() {
            final URI baseUri = URI.create("https://localhost/");

            for (int i = 0; i < HREFS_COUNT; i++) {
                crawlerExtractedUrisMock[i] = MockHtmlUtils.createURIs(baseUri, i, 128, 0, 128, HREFS_COUNT);
            }

            shuffleArray(crawlerExtractedUrisMock);
        }
    }
}
