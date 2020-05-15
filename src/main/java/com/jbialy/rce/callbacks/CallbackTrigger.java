package com.jbialy.rce;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class CallbackTrigger<T> {
    private final Predicate<CallbackState> callbackPredicate;
    private final DataCallback<T> callback;
    private final CallbackState callbackState;

    public CallbackTrigger(Predicate<CallbackState> callbackPredicate, DataCallback<T> callback) {
        this.callbackState = new CallbackState(0L, 0L);
        this.callbackPredicate = callbackPredicate;
        this.callback = callback;
    }

    public static <T> CallbackTrigger createEmpty() {
        return new CallbackTrigger(x -> false, todo -> {
        });
    }

    public void tryTrigger(Supplier<T> data) {
        callbackState.incrementTriesCounterAndReturn();

        if (callbackPredicate != null && callback != null && callbackPredicate.test(callbackState)) {
            callbackState.triggeredNotify();
            callback.onCall(data.get());
        }
    }

    void forceTrigger(T callbackData) {
        this.callback.onCall(callbackData);
    }
}
