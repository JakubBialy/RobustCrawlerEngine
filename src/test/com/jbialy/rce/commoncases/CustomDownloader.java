package com.jbialy.rce.commoncases;

import com.jbialy.rce.collections.workspace.Workspace;
import com.jbialy.rce.collections.workspace.implementation.GeneralPurposeWorkspace;
import com.jbialy.rce.downloader.DownloaderMapper;
import com.jbialy.rce.downloader.JobBuilder;
import com.jbialy.rce.downloader.core.CrawlerEngine;
import com.jbialy.rce.downloader.core.Job;
import com.jbialy.rce.downloader.core.WriteToDirectoryReceiver;
import com.jbialy.rce.utils.server.MirroringServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class CustomDownloader {
    public static MirroringServer mirroringServer;

    @BeforeAll
    public static void init() throws IOException {
        mirroringServer = new MirroringServer(8888);
        new File("./download/").delete();
    }

    @AfterAll
    public static void cleanUp() {
        mirroringServer.stop();
        new File("./download/").delete();
    }

    @Test
    public void testCase() throws InterruptedException {
        Workspace<Integer> workspace = GeneralPurposeWorkspace.fromToDoCollection(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        final Job<String, Integer> job = new JobBuilder<String, Integer>(() -> workspace)
                .setDownloader(new DownloaderMapper<>(
                        data -> new String(data, StandardCharsets.UTF_8),
                        integer -> URI.create("http://localhost:8888/test?id=" + integer)))
                .setReceiver(new WriteToDirectoryReceiver<>(
                        downloadResult -> downloadResult.getResponse().body().getBytes(StandardCharsets.UTF_8), // extract the bytes to be saved to the file
                        downloadResult -> downloadResult.getRequest().getUri() + ".html", //compute filename
                        new File("./download/") // indicate output directory
                ))
                .build();

        CrawlerEngine engineV3 = new CrawlerEngine(job); // Create CrawlerEngine instance with job
        engineV3.start(); // start CrawlerEngine
        engineV3.awaitForFinish(); // wait for all jobs to finish
    }
}
