package com.jbialy.rce;

import com.jbialy.rce.callbacks.CallbackState;

import java.time.Duration;
import java.util.function.Predicate;

public class TriggerHelper {

    private TriggerHelper() {

    }

    public static Predicate<CallbackState> after(Duration deltaTime) {
        return jobState -> !jobState.durationUntilLastCall().minus(deltaTime).isNegative();
    }

    public static Predicate<CallbackState> afterItems(long itemsCount) {
        return jobState -> jobState.triesUntilLastCall() >= itemsCount;
    }
}
