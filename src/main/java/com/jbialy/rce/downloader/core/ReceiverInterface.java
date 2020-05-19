package com.jbialy.rce.downloader.core;

import com.jbialy.rce.utils.sneakytry.ThrowableConsumer;

public interface ReceiverInterface<T> extends SafelyCloseable, ThrowableConsumer<T, Throwable> {
}
