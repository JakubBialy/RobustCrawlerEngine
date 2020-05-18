package com.jbialy.rce.collections;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class PackedCollection<T, R> implements Set<R>, Queue<R> {
    private final Function<T, R> toFrontType;
    private final Function<R, T> fromFrontType;
    private final ArrayDeque<T> internalSet;

    public PackedCollection(Function<R, T> pack, Function<T, R> unpack) {
        this.toFrontType = unpack;
        this.fromFrontType = pack;
        this.internalSet = new ArrayDeque<>();
    }

    @Override
    public int size() {
        return this.internalSet.size();
    }

    @Override
    public boolean isEmpty() {
        return this.internalSet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        final R r;
        try {
            r = (R) o;

            if (r == null) {
                return false;
            }
        } catch (ClassCastException cce) {
            return false;
        }

        return this.internalSet.contains(fromFrontType.apply(r));
    }

    @NotNull
    @Override
    public Iterator<R> iterator() {
        return null; //todo
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0]; //todo
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return null; //todo
    }

    @Override
    public boolean add(R r) {
        return this.internalSet.add(fromFrontType.apply(r));
    }

    @Override
    public boolean offer(R r) {
        return false; //todo
    }

    @Override
    public R remove() {
        return toFrontType.apply(this.internalSet.remove());
    }

    @Override
    public R poll() {
        return toFrontType.apply(this.internalSet.poll());
    }

    @Override
    public R element() {
        return toFrontType.apply(this.internalSet.element());
    }

    @Override
    public R peek() {
        return toFrontType.apply(this.internalSet.peek());
    }

    @Override
    public boolean remove(Object o) {
        final R r;
        try {
            r = (R) o;

            if (r == null) {
                return false;
            }
        } catch (ClassCastException cce) {
            return false;
        }

        return this.internalSet.remove(fromFrontType.apply(r));
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false; //todo
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends R> collection) {
        boolean result = false;
        for (R r : collection) {
            result |= this.internalSet.add(fromFrontType.apply(r));
        }

        return result;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false; //todo
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false; //todo
    }

    @Override
    public void clear() {
        this.internalSet.clear();
    }

    @Override
    public Spliterator<R> spliterator() {
        return null; //todo
    }
}
