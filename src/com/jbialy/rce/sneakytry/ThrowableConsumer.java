package com.jbialy.rce.sneakytry;

@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> {

    void accept(T t) throws E;
}
