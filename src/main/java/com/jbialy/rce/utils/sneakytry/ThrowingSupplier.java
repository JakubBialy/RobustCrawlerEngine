package com.jbialy.rce.utils.sneakytry;

public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
}
