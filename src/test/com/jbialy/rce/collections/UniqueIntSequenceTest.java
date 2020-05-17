package com.jbialy.rce.collections;

import com.jbialy.rce.collections.UniqueIntSequence;
import com.jbialy.tests.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jbialy.tests.utils.TestUtils.generateIntArray;

public class UniqueIntSequenceTest {
    @Test
    public void basicCreate() {
        UniqueIntSequence is = new UniqueIntSequence(0, 15);

        final List<Integer> expected = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        Assertions.assertTrue(expected.containsAll(is.toList()));
        Assertions.assertTrue(is.toList().containsAll(expected));
    }

    @Test
    public void createExceptTests() {
        UniqueIntSequence is = new UniqueIntSequence(0, 7, List.of(8, 9, 10));

        List<Integer> expected = List.of(0, 1, 2, 3, 4, 5, 6, 7);

        Assertions.assertTrue(expected.containsAll(is.toList()));
        Assertions.assertTrue(is.toList().containsAll(expected));

        is = new UniqueIntSequence(0, 7, List.of(3, 4, 6));

        expected = List.of(0, 1, 2, 5, 7);

        Assertions.assertTrue(expected.containsAll(is.toList()));
        Assertions.assertTrue(is.toList().containsAll(expected));
    }

    @Test
    public void simpleAddTest() {
        UniqueIntSequence is = new UniqueIntSequence(16);

        is.add(0);
        is.add(1);

        is.add(8);
        is.add(10);

        is.add(4);
        is.add(6);
    }

    @Test
    public void addTests() {
        UniqueIntSequence is = UniqueIntSequence.empty();
        is.add(0);
        Assertions.assertEquals(1, is.count());

        is = new UniqueIntSequence(0, 15, List.of(8, 9, 10, 14));

        is.add(14);
        int[] expected = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 11, 12, 13, 14, 15};
        Assertions.assertArrayEquals(expected, is.toArray());

        is.add(8);
        is.add(9);
        is.add(10);
        expected = generateIntArray(0, 15);
        Assertions.assertArrayEquals(expected, is.toArray());

        is = new UniqueIntSequence(0, 15);
        Assertions.assertArrayEquals(generateIntArray(0, 15), is.toArray());
        is.add(16);
        Assertions.assertArrayEquals(generateIntArray(0, 16), is.toArray());
        is.add(-1);

        for (int i = -10; i < 10; i++) {
            if (i != 0) {
                is.add(i * 1000);
                Assertions.assertTrue(is.contains(i * 1000));
                is.add(i * 1000 + 1);
                Assertions.assertTrue(is.contains(i * 1000 + 1));
            }
        }

        is.add(2002);
        is.add(-10001);
        is.add(-10000);
        is.add(-10001);

        is.add(512);
        is.add(511);

        Assertions.assertFalse(is.contains(Integer.MIN_VALUE));
        is.add(Integer.MIN_VALUE);
        Assertions.assertTrue(is.contains(Integer.MIN_VALUE));

        Assertions.assertFalse(is.contains(Integer.MAX_VALUE));
        is.add(Integer.MAX_VALUE);
        Assertions.assertTrue(is.contains(Integer.MAX_VALUE));

        int expectedSize = is.count();

        Assertions.assertEquals(expectedSize, is.count());
        is.add(Integer.MIN_VALUE);
        Assertions.assertEquals(expectedSize, is.count());
        is.add(Integer.MAX_VALUE);
        Assertions.assertEquals(expectedSize, is.count());
    }

    @Test
    public void randomAdd() {
        int intsRandomLength = 50_000;
        int[] randomInts = TestUtils.randomIntsArrayWithoutDuplicates(intsRandomLength);

        UniqueIntSequence emptySequence = UniqueIntSequence.empty();

        for (int i = 0; i < intsRandomLength; i++) {
            Assertions.assertFalse(emptySequence.contains(randomInts[i]));
            Assertions.assertTrue(emptySequence.add(randomInts[i]));
            Assertions.assertTrue(emptySequence.contains(randomInts[i]));
        }
    }

    @Test
    public void containsTest() {
        UniqueIntSequence is = UniqueIntSequence.empty();

        for (int i = 0; i < 4096; i++) {
            Assertions.assertFalse(is.contains(i));
            Assertions.assertTrue(is.add(i));
            Assertions.assertTrue(is.contains(i));
        }
    }

    @Test
    public void pollFirstTest() {
        UniqueIntSequence is = new UniqueIntSequence(0, 15);

        for (int i = 0; i <= 15; i++) {
            Assertions.assertEquals(i, is.pollFirst());
        }

        Assertions.assertThrows(IndexOutOfBoundsException.class, is::pollFirst);
    }

    @Test
    public void pollLastTest() {
        UniqueIntSequence is = new UniqueIntSequence(0, 15);

        for (int i = 15; i >= 0; i--) {
            Assertions.assertEquals(i, is.pollLast());
        }

        Assertions.assertThrows(IndexOutOfBoundsException.class, is::pollLast);
    }

    @Test
    public void cutRangeTest() {
        final int originalLastValue = 15;
        UniqueIntSequence is = new UniqueIntSequence(0, originalLastValue);

        for (int i = originalLastValue; i >= 0; i--) {
            is.cutRange(i, i);
            Assertions.assertEquals(i, is.count());
        }

        is = UniqueIntSequence.empty();

        for (int i = 0; i < 10; i++) {
            is.add(10 * i + 1);
            is.add(10 * i + 2);
            is.add(10 * i + 3);
            is.add(10 * i + 4);
            is.add(10 * i + 5);
            is.add(10 * i + 6);
            is.add(10 * i + 7);
            is.add(10 * i + 8);
            is.add(10 * i + 9);
        }

        is.contains(99);
        is.cutRange(25, 45);
        is.contains(99);
        Assertions.assertEquals(9, is.getSequencesCount());

        Assertions.assertEquals(1, is.peekFirst());
        is.cutRange(1, 9);
        Assertions.assertEquals(8, is.getSequencesCount());
        Assertions.assertEquals(11, is.peekFirst());

        is.cutRange(0, 15);
        Assertions.assertEquals(8, is.getSequencesCount());
        Assertions.assertEquals(16, is.peekFirst());

        is.cutRange(16, 18);
        Assertions.assertEquals(8, is.getSequencesCount());
        Assertions.assertEquals(19, is.peekFirst());

        is.cutRange(95, 98);
        Assertions.assertEquals(9, is.getSequencesCount());
        Assertions.assertEquals(99, is.peekLast());
        Assertions.assertFalse(is.contains(95));
        Assertions.assertFalse(is.contains(96));
        Assertions.assertFalse(is.contains(97));
        Assertions.assertFalse(is.contains(98));
    }

    @Test
    public void basicWrapMixedRight() {
        UniqueIntSequence is = new UniqueIntSequence(0, 1023, List.of(32, 33, 34, 35, 36));

        Assertions.assertEquals(2, is.getSequencesCount());

        for (int i = 32; i <= 35; i++) {
            Assertions.assertTrue(is.add(i));
            Assertions.assertEquals(2, is.getSequencesCount());
        }

        Assertions.assertTrue(is.add(36));

        Assertions.assertEquals(1, is.getSequencesCount());
    }

    @Test
    public void basicWrapMixedLeft() {
        UniqueIntSequence is = new UniqueIntSequence(0, 1023, List.of(32, 33, 34, 35, 36));

        Assertions.assertEquals(2, is.getSequencesCount());

        for (int i = 36; i > 32; i--) {
            Assertions.assertTrue(is.add(i));
            Assertions.assertEquals(2, is.getSequencesCount());
        }

        Assertions.assertTrue(is.add(32));

        Assertions.assertEquals(1, is.getSequencesCount());
    }
}
