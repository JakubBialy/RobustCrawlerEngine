package com.jbialy.rce;

import java.time.Duration;

public class CallbackState {
    private long lastCallMillisTimestamp;
    private long triesUntilLastCallCounter;

    public CallbackState(long lastCallMillisTimestamp, long triesUntilLastCallCounter) {
        this.lastCallMillisTimestamp = lastCallMillisTimestamp;
        this.triesUntilLastCallCounter = triesUntilLastCallCounter;
    }

    Duration durationUntilLastCall() {
        return Duration.ofMillis(System.currentTimeMillis() - lastCallMillisTimestamp);
    }

    long triesUntilLastCall() {
        return triesUntilLastCallCounter;
    }

    public void clearTriesCounter() {
        this.triesUntilLastCallCounter = 0L;
    }

    public void triggeredNotify() {
        this.triesUntilLastCallCounter = 0L;
        this.lastCallMillisTimestamp = System.currentTimeMillis();
    }

    public void updateLastCallTimestamp(long millisTimestamp) {
        this.lastCallMillisTimestamp = millisTimestamp;
    }

    public void incrementTriesCounter() {
        this.triesUntilLastCallCounter++;
    }

    public CallbackState incrementTriesCounterAndReturn() {
        this.triesUntilLastCallCounter++;

        return this;
    }
}
