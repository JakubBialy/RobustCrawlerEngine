package com.jbialy.rce.downloader.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class WriteToDirectoryReceiver<T> implements ReceiverInterface<T> {
    private final Function<T, byte[]> toBytesFunction;
    private final Function<T, String> toFilenameFunction;
    private final File outDir;
    private volatile boolean closed = false;

    public WriteToDirectoryReceiver(Function<T, byte[]> toBytesFunction, Function<T, String> toFilenameFunction, File outDir) {
        this.toBytesFunction = toBytesFunction;
        this.toFilenameFunction = toFilenameFunction;
        this.outDir = outDir;
    }

    @Override
    public void close() {
        if (this.closed) {
            throw new IllegalStateException("Already closed");
        } else {
            this.closed = true;
        }
    }

    @Override
    public void accept(T t) throws Throwable {
        if (this.closed) {
            throw new IllegalStateException("Already closed");
        } else {
            final byte[] data = this.toBytesFunction.apply(t);
            final String filename = this.toFilenameFunction.apply(t);

            if (!this.outDir.exists()){
                outDir.mkdir();
            }

            Files.write(Paths.get(outDir.getAbsolutePath(), filename), data, CREATE_NEW);
        }
    }
}
