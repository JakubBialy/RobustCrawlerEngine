package com.jbialy.rce.collections;

import com.jbialy.rce.utils.OutOfRangeException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DenseIntSet implements SortedSet<Integer>, Queue<Integer> {
    private final UniqueIntSequence uniqueIntSequence;

    public DenseIntSet() {
        this.uniqueIntSequence = UniqueIntSequence.empty();
    }

    public DenseIntSet(DenseIntSet denseIntSet) {
        this.uniqueIntSequence = new UniqueIntSequence(denseIntSet.uniqueIntSequence);
    }

    public DenseIntSet(UniqueIntSequence uniqueIntSequence) {
        this.uniqueIntSequence = uniqueIntSequence;
    }

    public DenseIntSet(int initialCapacity) {
        this.uniqueIntSequence = new UniqueIntSequence(initialCapacity);
    }

    public DenseIntSet(int firstValue, int lastValue) {
        this.uniqueIntSequence = new UniqueIntSequence(firstValue, lastValue);
    }

    @Override
    public Integer first() {
        return this.uniqueIntSequence.peekFirst();
    }

    @Override
    public Integer last() {
        return this.uniqueIntSequence.peekLast();
    }

    @Override
    public int size() {
        return (int) this.uniqueIntSequence.count();
    }

    @Override
    public boolean isEmpty() {
        return this.uniqueIntSequence.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            return this.uniqueIntSequence.contains((Integer) o);
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private final int size = (int) DenseIntSet.this.uniqueIntSequence.count();
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public Integer next() {
                try {
                    return DenseIntSet.this.uniqueIntSequence.getNthValue(currentIndex++);
                } catch (OutOfRangeException oor) {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return this.uniqueIntSequence.toList().toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return this.uniqueIntSequence.toList().toArray(a);
    }

    @Override
    public boolean add(Integer integer) {
        return this.uniqueIntSequence.add(integer);
    }

    @Override
    public boolean offer(Integer integer) {
        return add(integer);
    }

    @Override
    public Integer remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return poll();
        }
    }

    @Override
    public Integer poll() {
        return this.uniqueIntSequence.pollLast();
    }

    @Override
    public Integer element() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return peek();
        }
    }

    @Override
    public Integer peek() {
        return this.uniqueIntSequence.peekLast();
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Integer) {
            int i = (int) o;
            return this.uniqueIntSequence.removeValue(i);
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        if (this.isEmpty()) {
            return collection.isEmpty();
        } else {
            for (Object obj : collection) {
                if (!(obj instanceof Integer)) {
                    return false;
                } else {
                    if (this.uniqueIntSequence.contains((int) obj)) return true;
                }
            }
            return false;
        }

    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Integer> collection) {
        boolean result = false;
        for (int i : collection) {
            result = this.uniqueIntSequence.add(i) || result;
        }

        return result;

    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        if (this.uniqueIntSequence.isEmpty()) return false;
        else {
            boolean dataChanged = false;
            List<Integer> oldSequenceValues = this.uniqueIntSequence.toList();

            for (Integer oldValue : oldSequenceValues) {
                if (!c.contains(oldValue)) {
                    this.uniqueIntSequence.cutRange(oldValue, oldValue);
                    dataChanged = true;
                }
            }

            return dataChanged;
        }

    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        boolean dataChanged = false;

        for (Object obj : collection) {
            if (obj instanceof Integer) {
                if (dataChanged) { //data already changed
                    this.uniqueIntSequence.cutRange((int) obj, (int) obj);
                } else {
                    long sizeBefore = this.uniqueIntSequence.count();
                    this.uniqueIntSequence.cutRange((int) obj, (int) obj);
                    long sizeAfter = this.uniqueIntSequence.count();
                    dataChanged = sizeAfter != sizeBefore;
                }
            }
        }

        return dataChanged;
    }

    @Override
    public void clear() {
        this.uniqueIntSequence.clear();
    }

    @Override
    public Comparator<? super Integer> comparator() {
        return Integer::compareTo;
    }

    @NotNull
    @Override
    public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
        return new DenseIntSet(this.uniqueIntSequence.subSequence(fromElement, toElement));
    }

    @NotNull
    @Override
    public SortedSet<Integer> headSet(Integer toElement) {
        return new DenseIntSet(this.uniqueIntSequence.subSequenceToValue(toElement));
    }

    @NotNull
    @Override
    public SortedSet<Integer> tailSet(Integer fromElement) {
        return new DenseIntSet(this.uniqueIntSequence.subSequenceFrom(fromElement));
    }
}
