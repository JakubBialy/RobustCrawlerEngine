package com.jbialy.rce.benchmark;

import com.jbialy.rce.collections.workspace.Workspace;
import com.jbialy.rce.collections.workspace.implementation.GeneralPurposeWorkspace;
import com.jbialy.rce.collections.workspace.implementation.PackedUriWorkspace;
import com.jbialy.rce.collections.workspace.implementation.PackedUriIntRangeWorkspace;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.net.URI;

@State(Scope.Benchmark)
public class WorkspaceOOMTests {
    @Param({
            "250000", "1000000", "2000000", // 250k, 1M, 2M
            "4000000", "8000000", "16000000", // 4M, 8M, 16M
//            "16000000", "32000000", "64000000", // 16M, 32M, 64M
//            "128000000", "256000000", "512000000" // 128M, 256M, 512M
    })
    public static int OPERATIONS_COUNT;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WorkspaceOOMTests.class.getSimpleName())
                .forks(1)
                .mode(Mode.Throughput)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(0)
                .jvmArgsAppend("-Xmx256m")
                .build();
        new Runner(opt).run();
    }

//    public static void main(String[] args) {
//        OPERATIONS_COUNT = 100;
//        JobWorkspaceOOMTests instance = new JobWorkspaceOOMTests();
//
////        instance.JobWorkspaceCompressedRangeUri_add(null);
////        instance.JobWorkspaceCompressedRangeUri_add(null);
//        instance.JobWorkspacePackedUri_commonLeftPart_add(null);
//    }

    @Benchmark
    public void WorkspaceImpl_add(Blackhole blackhole) {
        Workspace<URI> workspace = GeneralPurposeWorkspace.createEmpty();

        final URI baseUri = URI.create("https://localhost/");
        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(baseUri.resolve(URI.create("/test?id=" + i)));
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void WorkspacePackedUriIntRange_add(Blackhole blackhole) {
        Workspace<URI> workspace = new PackedUriIntRangeWorkspace("https://localhost/test?id=", "");

        final URI baseUri = URI.create("https://localhost/");
        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(baseUri.resolve(URI.create("/test?id=" + i)));
        }

        blackhole.consume(workspace);
    }

    @Benchmark
    public void WorkspacePackedUri_commonLeftPart_add(Blackhole blackhole) {
        Workspace<URI> workspace = PackedUriWorkspace.createWithCommonLeftPart("https://localhost/test?id=");

        final URI baseUri = URI.create("https://localhost/");
        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            workspace.add(baseUri.resolve(URI.create("/test?id=" + i)));
        }

        blackhole.consume(workspace);
    }
}
