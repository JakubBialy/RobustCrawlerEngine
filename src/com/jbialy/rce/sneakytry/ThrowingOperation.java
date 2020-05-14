package com.jbialy.rce.sneakytry;

@FunctionalInterface
public interface ThrowingOperation<E extends Throwable> {
    void make() throws E;
}
