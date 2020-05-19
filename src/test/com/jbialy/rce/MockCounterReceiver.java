package com.jbialy.rce;

import com.jbialy.rce.downloader.core.OldReceiver;

import java.util.concurrent.atomic.AtomicLong;

public class MockCounterReceiver<T> extends OldReceiver<AtomicLong, T> {
    private final AtomicLong counter = new AtomicLong(0L);
    private final boolean printReceivedData;

    public MockCounterReceiver() {
        this(false);
    }

    public MockCounterReceiver(boolean printReceivedData) {
        super(null, null);
        this.printReceivedData = printReceivedData;
    }

    public long getCount() {
        return counter.get();
    }

    @Override
    public void close() {
        //NOP
    }

    @Override
    public void accept(T t) {
        this.counter.incrementAndGet();

        if (printReceivedData){
            System.out.println(t);
        }
    }
}
