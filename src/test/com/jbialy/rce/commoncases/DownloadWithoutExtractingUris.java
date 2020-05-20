package com.jbialy.rce.commoncases;

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

public class DownloadWithoutExtractingUris {
    public static MirroringServer mirroringServer;

    @BeforeAll
    public static void init() throws IOException {
        mirroringServer = new MirroringServer(8888);
    }

    @AfterAll
    public static void cleanUp() {
        mirroringServer.stop();
    }

    @Test
    public void testCase() throws InterruptedException {
        URI[] targets = new URI[]{ // Targets to download
                URI.create("http://localhost:8888/test?id=0"),
                URI.create("http://localhost:8888/test?id=1"),
                URI.create("http://localhost:8888/test?id=2"),
                URI.create("http://localhost:8888/test?id=3"),
                URI.create("http://localhost:8888/test?id=4")
        };

        Job<String, URI> job = JobBuilder.htmlBuilder(targets)
                .setReceiver(new WriteToDirectoryReceiver<>(
                        downloadResult -> downloadResult.getResponse().body().getBytes(StandardCharsets.UTF_8), // extract the bytes to be saved to the file
                        downloadResult -> downloadResult.getRequest().getUri().getQuery().replaceAll("\\D", "") + ".html", //compute filename
                        new File("./download/") // indicate output directory
                ))
                .build();

        CrawlerEngine engineV3 = new CrawlerEngine(job); // Create CrawlerEngine instance with job
        engineV3.start(); // start CrawlerEngine
        engineV3.awaitForFinish(); // wait for all jobs to finish
    }
}
