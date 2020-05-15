package com.jbialy.rce.utils.sneakytry;

import java.util.Objects;

@FunctionalInterface
public interface ThrowableBiConsumer<T, U> {

    void accept(T t, U u) throws Throwable;

    default ThrowableBiConsumer<T, U> andThen(ThrowableBiConsumer<? super T, ? super U> after) throws Throwable{
        Objects.requireNonNull(after);

        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}

