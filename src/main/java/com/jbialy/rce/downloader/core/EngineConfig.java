package com.jbialy.rce.downloader.core;

public class EngineConfig {
    public static final EngineConfig DEFAULT_CONFIG = defaultConfig();

    private final int maxDownloadingThreads;
    private final int maxResponseHandlingThreads;
    private final long maxRate;

    public EngineConfig(int maxDownloadingThreads, int maxResponseHandlingThreads, long maxRate) {
        this.maxDownloadingThreads = maxDownloadingThreads;
        this.maxResponseHandlingThreads = maxResponseHandlingThreads;
        this.maxRate = maxRate;
    }

    private static EngineConfig defaultConfig() {
        return new EngineConfig(1, 1, 8L);
    }

    public long getMaxRate() {
        return maxRate;
    }

    public int getMaxDownloadingThreadsNum() {
        return maxDownloadingThreads;
    }

    public int getMaxProcessingThreadsNum() {
        return maxResponseHandlingThreads;
    }
}
