package com.jbialy.rce.downloader.core;

import java.io.Closeable;

public interface SafelyCloseable extends Closeable {
    @Override
    void close();
}
