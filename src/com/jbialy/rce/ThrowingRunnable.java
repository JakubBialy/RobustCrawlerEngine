package com.jbialy.rce;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {
    void run() throws Throwable;
}
