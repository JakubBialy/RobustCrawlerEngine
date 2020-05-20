package com.jbialy.rce.jvm.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static com.jbialy.rce.utils.Utils.shuffleIntArray;

@State(Scope.Benchmark)
public class BranchPrediction {
    @Param({"2000000"})
    public static int COUNT;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BranchPrediction.class.getSimpleName())
                .forks(1)
                .mode(Mode.Throughput)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void conditionalSum_Sorted(Blackhole blackhole, Mock data) {
        long sum = 0L;
        int[] array = data.sorted;

        for (int i = 0; i < array.length; i++) {
            if (array[i] > 0) {
                sum = sum + array[i];
            }
        }

        blackhole.consume(sum);
    }

    @Benchmark
    public void unconditionalSum_Sorted(Blackhole blackhole, Mock data) {
        long sum = 0L;
        int[] array = data.sorted;

        for (int i = 0; i < array.length; i++) {
            sum = sum + array[i];
        }

        blackhole.consume(sum);
    }

    @Benchmark
    public void conditionalSum_Unsorted(Blackhole blackhole, Mock data) {
        long sum = 0L;
        int[] array = data.unsorted;

        for (int i = 0; i < array.length; i++) {
            if (array[i] > 0) {
                sum = sum + array[i];
            }
        }

        blackhole.consume(sum);
    }

    @Benchmark
    public void unconditionalSum_Unsorted(Blackhole blackhole, Mock data) {
        long sum = 0L;
        int[] array = data.unsorted;

        for (int i = 0; i < array.length; i++) {
            sum = sum + array[i];
        }

        blackhole.consume(sum);
    }

    @Benchmark
    public void conditionalSort_Unsorted_and_sumSorted(Blackhole blackhole, Mock data) {
        long sum = 0L;
        int[] array = data.unsorted;

        Arrays.sort(array);

        for (int i = 0; i < array.length; i++) {
            if (array[i] > 0) {
                sum = sum + array[i];
            }
        }

        blackhole.consume(sum);
    }

    @Benchmark
    public void unconditionalSortUnsorted_and_sumSorted(Blackhole blackhole, Mock data) {
        long sum = 0L;
        int[] array = data.unsorted;

        Arrays.sort(array);

        for (int i = 0; i < array.length; i++) {
            sum = sum + array[i];
        }

        blackhole.consume(sum);
    }

    @State(Scope.Thread)
    public static class Mock {
        private final int[] sorted = new int[COUNT];
        private final int[] unsorted = new int[COUNT];

        @Setup(Level.Trial)
        public void setup() {
            Random random = new Random();
            HashSet<Integer> randomDistinctInts = new HashSet<>();

            for (int i = 0; i < COUNT; i++) {
                randomDistinctInts.add(random.nextInt());
            }

            while (randomDistinctInts.size() != COUNT) {
                randomDistinctInts.add(random.nextInt());
            }

            final ArrayList<Integer> list = new ArrayList<>(randomDistinctInts);
            list.sort(Integer::compareTo);

            for (int i = 0; i < list.size(); i++) {
                sorted[i] = list.get(i);
                unsorted[i] = list.get(i);
            }

            shuffleIntArray(unsorted);
        }
    }
}
