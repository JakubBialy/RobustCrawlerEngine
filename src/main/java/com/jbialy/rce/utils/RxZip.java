package com.jbialy.rce.utils;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableUsing;
import io.reactivex.rxjava3.subscribers.ResourceSubscriber;
import org.reactivestreams.Publisher;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.*;

public class RxZip {
    private RxZip() {

    }

    public static ResourceSubscriber<ZipEntryData> toZip(Path zipOutput) {
        return toZip(zipOutput, Deflater.DEFAULT_COMPRESSION);
    }

    public static ResourceSubscriber<ZipEntryData> toZip(Path zipOutput, int compressionLevel) {
        return new ResourceSubscriber<ZipEntryData>() {
            private ZipOutputStream zos;

            @Override
            protected void onStart() {
                try {
                    zos = new ZipOutputStream(new FileOutputStream(zipOutput.toFile()));
                    zos.setLevel(compressionLevel);
                } catch (FileNotFoundException e) {
                    onError(e);
                }
                request(1);
            }

            @Override
            public void onNext(ZipEntryData zipEntry) {
                try {
                    zos.putNextEntry(zipEntry.zipEntry);
                    zos.write(zipEntry.data, 0, zipEntry.data.length);
                    zos.closeEntry();
                } catch (IOException exception) {
                    if (exception instanceof ZipException && exception.getMessage().startsWith("duplicate entry:")) {
                        //ignore
                    } else {
                        onError(exception);
                    }
                }

                request(1);
            }

            @Override
            public void onError(Throwable t) {
                try {
                    zos.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

                dispose();
            }

            @Override
            public void onComplete() {
                try {
                    zos.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

                dispose();
            }
        };
    }

    public static FlowableUsing<ZipEntryData, ZipFile> openZip(Path zipInput) {
        return new FlowableUsing<>(
                () -> new ZipFile(zipInput.toFile()),
                (Function<ZipFile, Publisher<ZipEntryData>>) zipFile -> Flowable.generate(() -> (Iterator<ZipEntry>) zipFile.entries().asIterator(),
                        (entriesIterator, emitter) -> {
                            if (entriesIterator.hasNext()) {
                                try {
                                    final ZipEntry entry = entriesIterator.next();
                                    final byte[] data = zipFile.getInputStream(entry).readAllBytes();

                                    emitter.onNext(new ZipEntryData(entry, data));
                                } catch (Throwable throwable) {
                                    emitter.onError(throwable);
                                }
                            } else {
                                emitter.onComplete();
                            }
                        }),
                ZipFile::close, false);
    }

    public static class ZipEntryData {
        private final ZipEntry zipEntry;
        private final byte[] data;

        public ZipEntryData(ZipEntry zipEntry, byte[] data) {
            this.zipEntry = zipEntry;
            this.data = data;
        }

        public ZipEntryData(String name, byte[] data) {
            this.zipEntry = new ZipEntry(name);
            this.data = data;
        }

        public ZipEntry getZipEntry() {
            return zipEntry;
        }

        public byte[] getData() {
            return data;
        }
    }
}
