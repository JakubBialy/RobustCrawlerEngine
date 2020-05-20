package com.jbialy.rce.benchmark;

import com.jbialy.rce.collections.UniqueIntSequence;
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
public class UniqueIntSequenceBenchmark {
    @Param({"250000"})
    public int OPERATIONS_COUNT;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UniqueIntSequenceBenchmark.class.getSimpleName())
                .forks(1)
                .mode(Mode.Throughput)
//                .measurementIterations(10)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void UniqueIntSequence_Add_ValueIncrementedBy_One(Blackhole blackhole, Mock mock) {
        UniqueIntSequence emptySequence = mock.getEmptySequenceV3();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            emptySequence.add(i);
        }

        blackhole.consume(emptySequence);
    }

    @Benchmark
    public void UniqueIntSequence_Add_ValueIncrementedBy_Two(Blackhole blackhole, Mock mock) {
        UniqueIntSequence emptySequence = mock.getEmptySequenceV3();

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            emptySequence.add(i * 2);
        }

        blackhole.consume(emptySequence);
    }

    @Benchmark
    public void UniqueIntSequence_Add_Random(Blackhole blackhole, Mock mock) {
        UniqueIntSequence emptySequence = mock.getEmptySequenceV3();

        int intsToAddLength = OPERATIONS_COUNT;
        int[] intsToAdd = mock.getRandomInts();
        for (int i = 0; i < intsToAddLength; i++) {
            emptySequence.add(intsToAdd[i]);
        }

        blackhole.consume(emptySequence);
    }

    @State(Scope.Thread)
    public static class Mock {
        private final int intsRandomLength = 250_000;
        private final int[] randomInts = new int[intsRandomLength];

        public int[] getRandomInts() {
            return randomInts;
        }

        public UniqueIntSequence getEmptySequenceV3() {
            return UniqueIntSequence.empty();
        }

        @Setup(Level.Iteration)
        public void setup() {
            HashSet<Integer> tmp = new HashSet<>(intsRandomLength, 1.0f);

            Random r = new Random(1337);
            while (tmp.size() < intsRandomLength) {
                tmp.add(r.nextInt());
            }

            List<Integer> listTmp = tmp.stream()/*.sorted()*/.collect(Collectors.toList());

            for (int i = 0; i < intsRandomLength; i++) {
                this.randomInts[i] = listTmp.get(i);
            }
        }
    }
}
