package com.jbialy.rce.utils.sneakytry;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowableBiFunction<T, U, R> {
    R apply(T t, U u) throws Throwable;

    default <V> ThrowableBiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) throws Throwable{
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}
