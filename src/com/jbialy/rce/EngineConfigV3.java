package com.jbialy.rce;

public class EngineConfigV3 {
    public static final EngineConfigV3 DEFAULT_CONFIG = defaultConfig();

    private final int maxDownloadingThreads;
    private final int maxResponseHandlingThreads;
    private final long maxRate;

    public EngineConfigV3(int maxDownloadingThreads, int maxResponseHandlingThreads, long maxRate) {
        this.maxDownloadingThreads = maxDownloadingThreads;
        this.maxResponseHandlingThreads = maxResponseHandlingThreads;
        this.maxRate = maxRate;
    }

    private static EngineConfigV3 defaultConfig() {
        return new EngineConfigV3(1, 1, 8L);
    }

    public long getMaxRate() {
        return maxRate;
    }

    public int getMaxDownloadingThreads() {
        return maxDownloadingThreads;
    }

    public int getMaxResponseProcessingThreads() {
        return maxResponseHandlingThreads;
    }
}
