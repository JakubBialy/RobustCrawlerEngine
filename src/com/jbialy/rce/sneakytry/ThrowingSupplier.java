package com.jbialy.rce.sneakytry;

public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
}
