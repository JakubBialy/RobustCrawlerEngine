package com.jbialy.rce.jvm.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State(Scope.Benchmark)
public class LoopAutoboxing {
    @Param({"1000000"})
    public static long COUNT;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LoopAutoboxing.class.getSimpleName())
                .forks(1)
                .mode(Mode.Throughput)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(5)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void longAutoboxing_long_to_long_compare(Blackhole blackhole) {
        Long sum = 0L;
        for (long i = 0; i <= COUNT; i++) {
            sum += i;
        }

        blackhole.consume(sum);
    }

    @Benchmark
    public void longAutoboxing_int_to_long_compare(Blackhole blackhole) {
        Long sum = 0L;
        for (int i = 0; i <= COUNT; i++) {
            sum += i;
        }

        blackhole.consume(sum);
    }

    @Benchmark
    public void longAutoboxing_long_to_long_compare_withNullCheck(Blackhole blackhole) {
        Long sum = null;
        for (long i = 0; i <= COUNT; i++) {
            if (sum == null) {
                sum = 0L;
            }

            sum += i;
        }

        blackhole.consume(sum);
    }

    @Benchmark
    public void primitiveLong(Blackhole blackhole) {
        long sum = 0;
        for (long i = 0; i <= COUNT; i++) {
            sum += i;
        }

        blackhole.consume(sum);
    }
}

/*
# JMH version: 1.23
# VM version: JDK 14, OpenJDK 64-Bit Server VM, 14+36-1461
# Warmup: 5 iterations, 1 s each
# Measurement: 50 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations

Benchmark                                                         (COUNT)   Mode  Cnt     Score    Error  Units
LoopAutoboxing.longAutoboxing_int_to_long_compare                 1000000  thrpt   50   362,571 ±  1,435  ops/s
LoopAutoboxing.longAutoboxing_long_to_long_compare                1000000  thrpt   50   366,173 ±  1,108  ops/s
LoopAutoboxing.longAutoboxing_long_to_long_compare_withNullCheck  1000000  thrpt   50   356,593 ±  1,168  ops/s
LoopAutoboxing.primitiveLong                                      1000000  thrpt   50  2721,035 ± 17,633  ops/s
 */
