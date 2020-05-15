package com.jbialy.rce.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;

public class FileUtils {
    private FileUtils() {
    }

    public static File createTmpFile(File dir) throws IOException {
        return createTmpFile(dir, "", "");
    }

    public static File createTmpFile(File dir, String prefix, String suffix) throws IOException {
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.exists()) throw new IOException("File does not exists.");

        if (!dir.isDirectory()) throw new IOException("File must be a directory");

        File result = dir.toPath().resolve(prefix + StringUtils.randomString(4) + suffix).toFile();

        while (result.exists() || !result.createNewFile()) {
            result = dir.toPath().resolve(prefix + StringUtils.randomString(6) + suffix).toFile();
        }

        return result;
    }

    public static void stringToFile(String data, Path fileOutputPath) throws IOException {
        File dir = fileOutputPath.toFile().getParentFile();

        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Wrong filePath");
        }

        File tmpFile = FileUtils.createTmpFile(dir);

        Files.writeString(tmpFile.toPath(), data, CREATE);
        Files.deleteIfExists(fileOutputPath);
        Files.move(tmpFile.toPath(), fileOutputPath);
    }
}
