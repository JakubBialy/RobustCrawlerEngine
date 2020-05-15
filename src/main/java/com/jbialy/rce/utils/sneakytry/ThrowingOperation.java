package com.jbialy.rce.utils.sneakytry;

@FunctionalInterface
public interface ThrowingOperation<E extends Throwable> {
    void make() throws E;
}
