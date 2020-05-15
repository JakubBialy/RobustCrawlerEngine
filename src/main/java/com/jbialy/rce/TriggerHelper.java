package com.jbialy.rce;

import java.time.Duration;
import java.util.function.Predicate;

public class TriggerHelper {
    public static Predicate<CallbackState> after(Duration deltaTime) {
        return jobState -> !jobState.durationUntilLastCall().minus(deltaTime).isNegative();
    }

    public static Predicate<CallbackState> afterItems(long itemsCount) {
        return jobState -> jobState.triesUntilLastCall() >= itemsCount;
    }
}
