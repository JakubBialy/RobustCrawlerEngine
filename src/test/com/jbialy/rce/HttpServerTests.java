package com.jbialy.rce;

import com.jbialy.rce.callbacks.BasicConsoleProgressCallback;
import com.jbialy.rce.collections.workspace.implementation.GeneralPurposeWorkspace;
import com.jbialy.rce.collections.workspace.Workspace;
import com.jbialy.rce.downloader.HtmlDownloader;
import com.jbialy.rce.downloader.JobBuilder;
import com.jbialy.rce.downloader.core.CrawlerEngine;
import com.jbialy.rce.downloader.core.DownloadResult;
import com.jbialy.rce.downloader.core.EngineConfig;
import com.jbialy.rce.downloader.core.JobData;
import com.jbialy.rce.utils.server.MockDownloaderModule;
import com.jbialy.rce.utils.server.SimpleHttpServer;
import com.jbialy.rce.utils.FileUtils;
import com.jbialy.rce.utils.HtmlUtils;
import com.jbialy.rce.utils.WorkspaceCallback;
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

import static com.jbialy.rce.TriggerHelper.after;
import static com.jbialy.rce.TriggerHelper.afterItems;

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
    public void mockDownloaderTest() throws InterruptedException {
        final int PAGES_COUNT = 25_000;

        final File apdOutput = APD_FILE;
        final Path checkpointFile = CHECKPOINT_FILE.toPath();
        Assertions.assertTrue(apdOutput.exists());
        Assertions.assertTrue(checkpointFile.toFile().exists());
        Assertions.assertEquals(0L, apdOutput.length());
        Assertions.assertEquals(0L, checkpointFile.toFile().length());

        final MockDownloaderModule mockDownloader = MockDownloaderModule.ofPagesCount(PAGES_COUNT);
        final MockCounterReceiver<DownloadResult<String, URI>> mockReceiver = new MockCounterReceiver<>();
        final URI seed = mockDownloader.getFirstPage();
        final WorkspaceCallback workspaceCallback = new WorkspaceCallback();

        JobData<String, URI> job = new JobBuilder<String, URI>
                (() -> GeneralPurposeWorkspace.fromFileOrElseCreateFromSeed(URI.class, checkpointFile, seed))
                .setConfig(new EngineConfig(2, 2, Long.MAX_VALUE)) //Optional
                .setDownloader(mockDownloader) //Optional
                .setUriExtractor(HtmlUtils::simpleExtractUris) // Optional
                .setPassToReceiverPredicate(downloadResult -> true) //Optional
                .checkpointCallback( //Optional
                        after(Duration.ofMinutes(15)).or(afterItems(100_000)),
                        workspaceCallback)
                .setReceiver(mockReceiver)
                .progressUpdate(
                        after(Duration.ofSeconds(1)).and(afterItems(1)),
                        new BasicConsoleProgressCallback())
                .build();

        CrawlerEngine engineV3 = new CrawlerEngine(job);
        engineV3.start();
        engineV3.awaitForFinish();

        final Workspace<URI> workspace = workspaceCallback.getWorkspace();

        Assertions.assertEquals(PAGES_COUNT, mockReceiver.getCount());
        Assertions.assertEquals(PAGES_COUNT, workspace.allItemsCount());
        Assertions.assertEquals(PAGES_COUNT, workspace.doneCount());
        Assertions.assertEquals(0, workspace.todoCount());
        Assertions.assertEquals(0, workspace.damagedCount());
        Assertions.assertEquals(0, workspace.inProgressCount());
        Assertions.assertEquals(workspace.doneCount(), workspace.getJobStatistics().getDoneTasks());
        Assertions.assertEquals(workspace.allItemsCount(), workspace.getJobStatistics().getAllTasksCount());
        Assertions.assertEquals(workspace.todoCount(), workspace.getJobStatistics().getTasksTodo());
        Assertions.assertEquals(PAGES_COUNT, workspace.getJobStatistics().getTasksSentToReceiver());
    }

    @Test
    public void mockServerTest() throws InterruptedException, IOException {
        final int PAGES_COUNT = 10_000;

        final SimpleHttpServer simpleHttpServer = new SimpleHttpServer(PAGES_COUNT, 8888);

        final File apdOutput = APD_FILE;
        final Path checkpointFile = CHECKPOINT_FILE.toPath();
        Assertions.assertTrue(apdOutput.exists());
        Assertions.assertTrue(checkpointFile.toFile().exists());
        Assertions.assertEquals(0L, apdOutput.length());
        Assertions.assertEquals(0L, checkpointFile.toFile().length());

        final MockCounterReceiver<DownloadResult<String, URI>> mockReceiver = new MockCounterReceiver<>();
        final URI seed = URI.create("http://localhost:8888/test?id=0");
        final WorkspaceCallback workspaceCallback = new WorkspaceCallback();

        JobData<String, URI> job = new JobBuilder<String, URI>
                (() -> GeneralPurposeWorkspace.fromFileOrElseCreateFromSeed(URI.class, checkpointFile, seed))
                .setConfig(new EngineConfig(8, 2, Long.MAX_VALUE)) //Optional
                .setDownloader(new HtmlDownloader()) //Optional
                .setUriExtractor(HtmlUtils::simpleExtractUris) // Optional
                .setPassToReceiverPredicate(downloadResult -> true) //Optional
                .checkpointCallback( //Optional
                        after(Duration.ofMinutes(15)).or(afterItems(100_000)),
                        workspaceCallback)
                .setReceiver(mockReceiver)
                .progressUpdate(
                        after(Duration.ofSeconds(1)).and(afterItems(1)),
                        new BasicConsoleProgressCallback())
                .build();

        CrawlerEngine engineV3 = new CrawlerEngine(job);
        engineV3.start();
        engineV3.awaitForFinish();

        simpleHttpServer.stop();

        final Workspace<URI> workspace = workspaceCallback.getWorkspace();

        Assertions.assertEquals(PAGES_COUNT, mockReceiver.getCount());
        Assertions.assertEquals(PAGES_COUNT, workspace.allItemsCount());
        Assertions.assertEquals(PAGES_COUNT, workspace.doneCount());
        Assertions.assertEquals(0, workspace.todoCount());
        Assertions.assertEquals(0, workspace.damagedCount());
        Assertions.assertEquals(0, workspace.inProgressCount());
        Assertions.assertEquals(workspace.doneCount(), workspace.getJobStatistics().getDoneTasks());
        Assertions.assertEquals(workspace.allItemsCount(), workspace.getJobStatistics().getAllTasksCount());
        Assertions.assertEquals(workspace.todoCount(), workspace.getJobStatistics().getTasksTodo());
        Assertions.assertEquals(PAGES_COUNT, workspace.getJobStatistics().getTasksSentToReceiver());
    }
}
