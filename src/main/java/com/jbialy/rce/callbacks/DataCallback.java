package com.jbialy.rce;

@FunctionalInterface
public interface DataCallback<T> {
    void onCall(T t);
}
