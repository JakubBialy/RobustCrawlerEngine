package com.jbialy.rce.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DataConverter {
    public static byte[] fromNamedData(String name, byte[] rawData) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        int nameBytesLength = nameBytes.length;
        byte[] result = new byte[4 + nameBytesLength + rawData.length];

        System.arraycopy(intToBytes(nameBytesLength), 0, result, 0, 4);
        System.arraycopy(nameBytes, 0, result, 4, nameBytesLength);
        System.arraycopy(rawData, 0, result, nameBytesLength + 4, rawData.length);

        return result;
    }

    public static NamedData toNamedData(byte[] packedData) {
        int nameBytesCount = ByteBuffer.wrap(packedData, 0, 4).getInt();
        String name = new String(Arrays.copyOfRange(packedData, 4, 4 + nameBytesCount), StandardCharsets.UTF_8);
        byte[] data = new byte[packedData.length - (4 + nameBytesCount)];
        System.arraycopy(packedData, 4 + nameBytesCount, data, 0, data.length);

        return new NamedData() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public byte[] getData() {
                return data;
            }
        };
    }

    private static byte[] intToBytes(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
    }

    public interface NamedData {
        String getName();

        byte[] getData();
    }
}


