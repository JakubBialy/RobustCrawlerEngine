package com.jbialy.rce.benchmark;

import com.jbialy.rce.collections.DenseIntSet;
import com.jbialy.rce.collections.workspace.CollectionWorkspace;
import com.jbialy.rce.collections.workspace.JobWorkspace2Impl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
public class WorkspacesComparision {
//    @Param({"250000"})
    @Param({"10000"})
    public int OPERATIONS_COUNT;

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
                .build();
        new Runner(opt).run();

//        JobWorkspace2Impl_add_and_get(null);
//        CollectionWorkspace_add_and_get(null);
    }

    @Benchmark
    public void JobWorkspace2Impl_add_and_get(Blackhole blackhole) {
        JobWorkspace2Impl<Integer> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(i * 2);
        }

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.moveToProcessingAndReturn();
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void JobWorkspace2Impl_add(Blackhole blackhole) {
        JobWorkspace2Impl<Integer> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(i * 2);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void JobWorkspace2Impl_add_twice(Blackhole blackhole) {
        JobWorkspace2Impl<Integer> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(i * 2);
            workspace.add(i * 2);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void CollectionWorkspace_add(Blackhole blackhole) {
        CollectionWorkspace<Integer> workspace = new CollectionWorkspace<>(List.of());

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addToDo(i * 2);
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void CollectionWorkspace_add_and_get(Blackhole blackhole) {
        CollectionWorkspace<Integer> workspace = new CollectionWorkspace<>(List.of());

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addToDo(i * 2);
        }

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.getAndMarkAsInProgress();
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void CollectionWorkspace_add_twice(Blackhole blackhole) {
        CollectionWorkspace<Integer> workspace = new CollectionWorkspace<>(List.of());

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.addToDo(i * 2);
            workspace.addToDo(i * 2);
        }

        blackhole.consume(workspace);
    }

    @State(Scope.Thread)
    public static class Mock {
        private final int intsRandomLength = 250_000;
        private final int[] randomInts = new int[intsRandomLength];

        public int[] getRandomInts() {
            return randomInts;
        }

        public DenseIntSet generateTestSet() {
            DenseIntSet set = new DenseIntSet();

            for (int i = 0; i < 100; i++) {
                set.add(i);
            }

            for (int i = 0; i < 100; i += 10) {
                set.remove(i);
            }

            for (int i = 50; i < 60; i++) {
                set.remove(i);
            }
            return set;
        }

        public DenseIntSet createEmptySet() {
            return new DenseIntSet();
        }

        @Setup(Level.Iteration)
        public void setup() {

            HashSet<Integer> tmp = new HashSet<>(intsRandomLength, 1.0f);

            Random r = new Random(1337);
            while (tmp.size() < intsRandomLength) {
                tmp.add(Math.abs(r.nextInt()));
//                tmp.add(r.nextInt());
            }

            List<Integer> listTmp = tmp.stream()/*.sorted()*/.collect(Collectors.toList());

            for (int i = 0; i < intsRandomLength; i++) {
                this.randomInts[i] = listTmp.get(i);
            }
        }
    }

}
