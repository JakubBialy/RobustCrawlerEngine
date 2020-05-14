package com.jbialy.rce.downloader;

import java.io.Closeable;

public interface SafelyCloseable extends Closeable {
    @Override
    void close();
}
