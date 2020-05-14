package com.jbialy.rce.downloader;

import com.jbialy.rce.sneakytry.ThrowableBiConsumer;
import com.jbialy.rce.sneakytry.ThrowableConsumer;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Supplier;

public class Receiver<I, T> implements SafelyCloseable, ThrowableConsumer<T, Throwable> {
    private final Supplier<I> instanceSupplier;
    private final ThrowableBiConsumer<I, T> consumerAction;
    private volatile I instance;

    public Receiver(Supplier<I> consumerSupplier, ThrowableBiConsumer<I, T> consumerAction) {
        this.instanceSupplier = consumerSupplier;
        this.consumerAction = consumerAction;
    }

    @Override
    public synchronized void close() {
        if (instance instanceof Closeable closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void accept(T t) throws Throwable {
        if (this.instance == null) {
            this.instance = instanceSupplier.get();
        }

        this.consumerAction.accept(this.instance, t);
    }
}
