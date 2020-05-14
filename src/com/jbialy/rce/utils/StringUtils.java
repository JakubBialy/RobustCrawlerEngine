package com.jbialy.rce.utils;

import java.util.Random;

public class StringUtils {
    private static final Random random = new Random();
    private static final char[] CHARS = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h',
            'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A',
            'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '_'};

    private StringUtils() {
    }

    public static String randomString(int length) {
        if (length < 0) return "";

        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            result[i] = CHARS[random.nextInt(CHARS.length)];
        }

        return new String(result);
    }

    public static int countOccurrences(String source, String target) {
        if (source == null || source.length() < 1 || target == null) return 0;

        int result = 0;
        int lastIndex = -1;
        while ((lastIndex = source.indexOf(target, lastIndex + 1)) != -1) {
            result++;
        }

        return result;
    }
}
