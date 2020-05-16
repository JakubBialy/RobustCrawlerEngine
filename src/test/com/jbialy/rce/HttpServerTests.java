package com.jbialy.rce;

import com.jbialy.rce.collections.workspace.JobWorkspace2Impl;
import com.jbialy.rce.downloader.core.CrawlerEngine;
import com.jbialy.rce.callbacks.BasicConsoleProgressCallback;
import com.jbialy.rce.collections.workspace.CollectionWorkspace;
import com.jbialy.rce.downloader.core.DownloadResult;
import com.jbialy.rce.downloader.core.EngineConfig;
import com.jbialy.rce.downloader.JobBuilder;
import com.jbialy.rce.downloader.core.JobData;
import com.jbialy.rce.utils.FileUtils;
import com.jbialy.rce.utils.HtmlUtils;
import com.jbialy.tests.mock.MockCounterReceiver;
import com.jbialy.tests.server.MockDownloaderModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static com.jbialy.rce.TriggerHelper.*;
import static com.jbialy.rce.TriggerHelper.after;
import static com.jbialy.rce.utils.sneakytry.TryUtils.sneakyTry;

public class HttpServerTests {
    private static final Path TMP_DIR = Paths.get("./test_dir");
    private static File APD_FILE;
    private static File CHECKPOINT_FILE;

    @BeforeAll
    public static void init() throws IOException {
        APD_FILE = FileUtils.createTmpFile(TMP_DIR.toFile(), "", ".apd");
        CHECKPOINT_FILE = FileUtils.createTmpFile(TMP_DIR.toFile(), "", ".checkpoint");
    }

    @AfterAll
    public static void cleanUp() {
        APD_FILE.delete();
        CHECKPOINT_FILE.delete();
    }

    @Test
    public void simpleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void mockServerEngineTest() throws InterruptedException {
        final int PAGES_COUNT = 10_000;

        final File apdOutput = APD_FILE;
        final Path checkpointFile = CHECKPOINT_FILE.toPath();
        Assertions.assertTrue(apdOutput.exists());
        Assertions.assertTrue(checkpointFile.toFile().exists());
        Assertions.assertEquals(0L, apdOutput.length());
        Assertions.assertEquals(0L, checkpointFile.toFile().length());

        final MockCounterReceiver<DownloadResult<String, URI>> mockReceiver = new MockCounterReceiver<>();
        final MockDownloaderModule mockDownloader = MockDownloaderModule.ofPagesCount(PAGES_COUNT);
        final URI seed = mockDownloader.getFirstPage();

        JobData<String, URI> job = new JobBuilder<String, URI>
//                (() -> CollectionWorkspace.fromFileOrElseCreateFromSeed(URI.class, checkpointFile, seed))
                (() -> JobWorkspace2Impl.fromFileOrElseCreateFromSeed(URI.class, checkpointFile, seed))
                .setConfig(new EngineConfig(2, 2, Long.MAX_VALUE)) //Optional
                .setDownloader(mockDownloader) //Optional
                .setUriExtractor(HtmlUtils::simpleExtractUris) // Optional
                .setPassToReceiverPredicate(downloadResult -> true) //Optional
//                .checkpointCallback( //optional
//                        after(Duration.ofMinutes(15)).or(afterItems(100_000)),
//                        ws -> sneakyTry(() -> FileUtils.stringToFile(JobWorkspace2Impl.toJSON(ws, true), checkpointFile)))
                .setReceiver(mockReceiver)
                .progressUpdate(
                        after(Duration.ofSeconds(1)).and(afterItems(1)),
                        new BasicConsoleProgressCallback()
                )
                .build();

        CrawlerEngine engineV3 = new CrawlerEngine(job);
        engineV3.start();
        engineV3.awaitForFinish();

        Assertions.assertEquals(PAGES_COUNT, mockReceiver.getCount());
    }
}
