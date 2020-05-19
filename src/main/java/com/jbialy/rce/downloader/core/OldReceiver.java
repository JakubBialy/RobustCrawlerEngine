package com.jbialy.rce.downloader.core;

import com.jbialy.rce.utils.sneakytry.ThrowableBiConsumer;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Supplier;

public class OldReceiver<I, T> implements ReceiverInterface<T> {
    private final Supplier<I> instanceSupplier;
    private final ThrowableBiConsumer<I, T> consumerAction;
    private volatile I instance;

    public OldReceiver(Supplier<I> consumerSupplier, ThrowableBiConsumer<I, T> consumerAction) {
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
