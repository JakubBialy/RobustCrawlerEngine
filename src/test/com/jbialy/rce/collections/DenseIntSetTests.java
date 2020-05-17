package com.jbialy.rce.collections;

import com.jbialy.rce.collections.DenseIntSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class DenseIntSetTests {

    @Test
    public void instanceCreateTest() {
        Set<Integer> set = new DenseIntSet();
    }

    @Test
    public void sizeTest() {
        Set<Integer> set = new DenseIntSet();

        for (int i = 0; i < 1024; i++) {
            Assertions.assertEquals(i, set.size());
            Assertions.assertTrue(set.add(i));
        }

        for (int i = 1024; i > 0; i--) {
            Assertions.assertEquals(i, set.size());
            Assertions.assertTrue(set.remove(i - 1));
        }

        Assertions.assertEquals(0, set.size());
    }

    @Test
    public void subSetTest() {
        SortedSet<Integer> set = new DenseIntSet();

        for (int i = 0; i < 1024; i++) {
            Assertions.assertFalse(set.contains(i));
            set.add(i);
            Assertions.assertTrue(set.contains(i));
        }

        Assertions.assertArrayEquals(toIntArray(set), toIntArray(set.subSet(0, 1024)));

        for (int i = 0; i <= 1000; i += 100) {
            set.remove(i);
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> toIntArray(set.subSet(0, -1)));
        Assertions.assertEquals(23, set.subSet(1000, 1024).size());
        Assertions.assertArrayEquals(toIntArray(set.subSet(1000, 1024)), toIntArray(set.subSet(1000, 1025)));
        Assertions.assertArrayEquals(new int[]{1023}, toIntArray(set.subSet(1023, 1024)));
        Assertions.assertArrayEquals(new int[]{}, toIntArray(set.subSet(100, 100)));
        Assertions.assertArrayEquals(new int[]{}, toIntArray(set.subSet(100, 101)));
        Assertions.assertArrayEquals(new int[]{101}, toIntArray(set.subSet(100, 102)));
        Assertions.assertArrayEquals(new int[]{}, toIntArray(set.subSet(0, 0)));
        Assertions.assertArrayEquals(new int[]{}, toIntArray(set.subSet(0, 1)));
        Assertions.assertArrayEquals(new int[]{1, 2}, toIntArray(set.subSet(0, 3)));
        Assertions.assertArrayEquals(new int[]{1}, toIntArray(set.subSet(1, 2)));
        Assertions.assertArrayEquals(new int[]{99, 101}, toIntArray(set.subSet(99, 102)));
        Assertions.assertArrayEquals(new int[]{99}, toIntArray(set.subSet(99, 100)));
        Assertions.assertArrayEquals(new int[]{1}, toIntArray(set.subSet(-1, 2)));
        Assertions.assertArrayEquals(new int[]{99}, toIntArray(set.subSet(99, 101)));
        Assertions.assertArrayEquals(new int[]{98, 99, 101, 102}, toIntArray(set.subSet(98, 103)));
        Assertions.assertArrayEquals(toIntArray(set), toIntArray(set.subSet(0, 1024)));
        Assertions.assertArrayEquals(toIntArray(new TreeSet<>(set).subSet(800, 1024)), toIntArray(set.subSet(800, 1024)));
    }

    @Test
    public void subSetTreeSetTest() {
        SortedSet<Integer> set = generateTestSet();

        for (int leftValue = -10; leftValue < 100; leftValue++) {
            for (int rightValue = leftValue; rightValue < 110; rightValue++) {
                final int[] expected = toIntArray(new TreeSet<>(set).subSet(leftValue, rightValue));
                final int[] actual = toIntArray(set.subSet(leftValue, rightValue));

                Assertions.assertArrayEquals(expected, actual);
            }
        }
    }

    @Test
    public void tailSetTreeSetTest() {
        SortedSet<Integer> set = generateTestSet();

        for (int leftValue = -10; leftValue < 110; leftValue++) {
            final int[] expected = toIntArray(new TreeSet<>(set).tailSet(leftValue));
            final int[] actual = toIntArray(set.tailSet(leftValue));

            Assertions.assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void headSetTreeSetTest() {
        SortedSet<Integer> set = generateTestSet();

        for (int rightValue = -10; rightValue < 110; rightValue++) {
            final int[] expected = toIntArray(new TreeSet<>(set).headSet(rightValue));
            final int[] actual = toIntArray(set.headSet(rightValue));

            Assertions.assertArrayEquals(expected, actual);
        }
    }

    @NotNull
    private SortedSet<Integer> generateTestSet() {
        SortedSet<Integer> set = new DenseIntSet();

        for (int i = 0; i < 100; i++) {
            set.add(i);
        }

        for (int i = 0; i < 100; i += 10) {
            set.remove(i);
        }

        for (int i = 50; i < 60; i++) {
            int t = 2;
            set.remove(i);
        }
        return set;
    }


    private int[] toIntArray(Collection<? extends Integer> collection) {
        int[] result = new int[collection.size()];

        int i = 0;
        for (int elem : collection) {
            result[i++] = elem;
        }

        return result;
    }
}
