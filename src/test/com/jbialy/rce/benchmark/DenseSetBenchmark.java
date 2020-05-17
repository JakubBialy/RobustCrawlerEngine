package com.jbialy.rce.benchmark;

import com.jbialy.rce.collections.DenseIntSet;
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
import java.util.TreeSet;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
public class DenseSetBenchmark {
    @Param({"250000"})
    public int OPERATIONS_COUNT;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DenseSetBenchmark.class.getSimpleName())
                .forks(1)
                .mode(Mode.Throughput)
//                .measurementIterations(10)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(2)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void TreeSet_Add_ValueIncrementedBy_Two(Blackhole blackhole, Mock mock) {
        TreeSet<Integer> treeSet = new TreeSet<>();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            treeSet.add(i * 2);
        }

        blackhole.consume(treeSet);
    }

    @Benchmark
    public void TreeSet_Add_ValueIncrementedBy_One(Blackhole blackhole, Mock mock) {
        TreeSet<Integer> treeSet = new TreeSet<>();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            treeSet.add(i);
        }

        blackhole.consume(treeSet);
    }


    @Benchmark
    public void DenseIntSet_AddValueIncrementedBy_One(Blackhole blackhole, Mock mock) {
        DenseIntSet denseIntSet = mock.createEmptySet();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            denseIntSet.add(i);
        }

        blackhole.consume(denseIntSet);
    }

    @Benchmark
    public void DenseIntSet_Add_ValueIncrementedBy_Two(Blackhole blackhole, Mock mock) {
        DenseIntSet denseIntSet = mock.createEmptySet();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            denseIntSet.add(i * 2);
        }

        blackhole.consume(denseIntSet);
    }

    @Benchmark
    public void DenseIntSet_Add_Random(Blackhole blackhole, Mock data) {
        DenseIntSet denseIntSet = data.createEmptySet();

        int intsToAddLength = OPERATIONS_COUNT;
        int[] intsToAdd = data.getRandomInts();

        for (int i = 0; i < intsToAddLength; i++) {
            denseIntSet.add(intsToAdd[i]);
        }

        blackhole.consume(denseIntSet);
    }

    @Benchmark
    public void TreeSet_Add_Random(Blackhole blackhole, Mock data) {
        TreeSet<Integer> treeSet = new TreeSet<>();

        int intsToAddLength = OPERATIONS_COUNT;
        int[] intsToAdd = data.getRandomInts();

        for (int i = 0; i < intsToAddLength; i++) {
            treeSet.add(intsToAdd[i]);
        }

        blackhole.consume(treeSet);
    }

    @Benchmark
    public void TreeSet_AddAndRemove(Blackhole blackhole, Mock data) {
        TreeSet<Integer> treeSet = new TreeSet<>();

        int intsToAddLength = OPERATIONS_COUNT;
        int[] intsToAdd = data.getRandomInts();

        for (int i = 0; i < intsToAddLength; i++) {
            treeSet.add(intsToAdd[i]);
        }

        for (int i = 0; i < intsToAddLength; i++) {
            treeSet.remove(intsToAdd[i]);
        }

        blackhole.consume(treeSet);
    }

    @Benchmark
    public void DenseIntSet_AddAndRemove(Blackhole blackhole, Mock data) {
        DenseIntSet set = data.createEmptySet();

        int intsToAddLength = OPERATIONS_COUNT;
        int[] intsToAdd = data.getRandomInts();

        for (int i = 0; i < intsToAddLength; i++) {
            set.add(intsToAdd[i]);
        }

        for (int i = 0; i < intsToAddLength; i++) {
            set.remove(intsToAdd[i]);
        }

        blackhole.consume(set);
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
