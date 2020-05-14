package com.jbialy.mock;

import com.jbialy.rce.downloader.Receiver;
import java.util.concurrent.atomic.AtomicLong;

public class MockCounterReceiver<T> extends Receiver<AtomicLong, T> {
    private final AtomicLong counter = new AtomicLong(0L);

    public MockCounterReceiver() {
        super(null, null);
    }

    public long getCount() {
        return counter.get();
    }

    @Override
    public void close() {
        //NOP
    }

    @Override
    public void accept(T t) throws Throwable {
        this.counter.incrementAndGet();
    }
}
