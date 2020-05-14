package com.jbialy.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TestUtils {
    public static int[] generateIntArray(int firstValue, int lastValue) {
        int[] result = new int[lastValue - firstValue + 1];
        int resultIndex = 0;

        for (int i = firstValue; i <= lastValue; i++) {
            result[resultIndex++] = i;
        }

        return result;
    }

    public static int[] randomIntsArrayWithoutDuplicates(int length) {
        Random r = new Random(1337);
        int[] randomInts = new int[length];

        HashSet<Integer> tmp = new HashSet<>(length, 1.0f);

        for (int i = 0; i < length; i++) tmp.add(r.nextInt());
        while (tmp.size() < length) tmp.add(r.nextInt());

        List<Integer> listTmp = new ArrayList<>(tmp);

        for (int i = 0; i < length; i++) {
            randomInts[i] = listTmp.get(i);
        }

        return randomInts;
    }
}
