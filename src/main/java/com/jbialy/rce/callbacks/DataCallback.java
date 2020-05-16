package com.jbialy.rce.callbacks;

@FunctionalInterface
public interface DataCallback<T> {
    void onCall(T t);
}
