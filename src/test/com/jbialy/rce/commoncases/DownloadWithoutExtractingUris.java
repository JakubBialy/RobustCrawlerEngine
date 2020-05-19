package com.jbialy.rce.commoncases;

import com.jbialy.rce.downloader.JobBuilder;
import com.jbialy.rce.downloader.core.CrawlerEngine;
import com.jbialy.rce.downloader.core.JobData;
import com.jbialy.rce.downloader.core.WriteToDirectoryReceiver;
import com.jbialy.rce.utils.server.MirroringServer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class DownloadWithoutExtractingUris {

    @Test
    public void testCase() throws InterruptedException, IOException {
        final MirroringServer mirroringServer = new MirroringServer(8888);

        URI[] targets = new URI[]{
                URI.create("http://localhost:8888/test?id=0"),
                URI.create("http://localhost:8888/test?id=1"),
                URI.create("http://localhost:8888/test?id=2"),
                URI.create("http://localhost:8888/test?id=3"),
                URI.create("http://localhost:8888/test?id=4")
        };

        JobData<String, URI> job = JobBuilder.htmlBuilder(targets)
                .setReceiver(new WriteToDirectoryReceiver<>(
                        downloadResult -> downloadResult.getResponse().body().getBytes(StandardCharsets.UTF_8),
                        downloadResult -> downloadResult.getRequest().getUri().getQuery().replaceAll("\\D", "") + ".html",
                        new File("./download/")
                ))
                .build();

        CrawlerEngine engineV3 = new CrawlerEngine(job);
        engineV3.start();
        engineV3.awaitForFinish();
        mirroringServer.stop();
    }
}
