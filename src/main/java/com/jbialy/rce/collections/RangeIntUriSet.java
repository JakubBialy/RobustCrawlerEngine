package com.jbialy.rce.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class RangeIntUriSet implements SortedSet<URI>, Queue<URI> {
    private final DenseIntSet idsSet;
    private final String leftPart;
    private final String rightPart;

    public RangeIntUriSet(String leftPart, int firstIdValue, int lastIdValue, String rightPart) {
        this.idsSet = new DenseIntSet(firstIdValue, lastIdValue);
        this.leftPart = leftPart;
        this.rightPart = rightPart;
    }

    public RangeIntUriSet(String leftPart, String rightPart) {
        this.idsSet = new DenseIntSet();
        this.leftPart = leftPart;
        this.rightPart = rightPart;
    }

    public RangeIntUriSet(RangeIntUriSet set) {
        this.idsSet = new DenseIntSet(set.idsSet);
        this.leftPart = set.leftPart;
        this.rightPart = set.rightPart;
    }

    public static RangeIntUriSet empty(String leftPart, String rightPart) {
        return new RangeIntUriSet(leftPart, rightPart);
    }

    public String getLeftPart() {
        return leftPart;
    }

    public String getRightPart() {
        return rightPart;
    }

    @Override
    public int size() {
        return this.idsSet.size();
    }

    @Override
    public boolean isEmpty() {
        return this.idsSet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof URI uri) {
            String asciiUri = uri.toASCIIString();

            if (asciiUri.contains(leftPart) && asciiUri.contains(rightPart)) {
                int id = Integer.parseInt(asciiUri.substring(leftPart.length(), asciiUri.lastIndexOf(rightPart)));
                return this.idsSet.contains(id);
            }
        }

        return false;
    }

    @NotNull
    @Override
    public Iterator<URI> iterator() {
        return new Iterator<>() {
            private final Iterator<Integer> idsIterator = idsSet.iterator();

            @Override
            public boolean hasNext() {
                return idsIterator.hasNext();
            }

            @Override
            public URI next() {
                return createUriFromId(idsIterator.next());
            }
        };
    }

    @NotNull
    private URI createUriFromId(int id) {
        return URI.create(leftPart + id + rightPart);
    }

    public int extractIdFromURI(URI uri) {
        String asciiUri = uri.toASCIIString();

        if (asciiUri.contains(leftPart) && asciiUri.contains(rightPart)) {
            return Integer.parseInt(asciiUri.substring(leftPart.length(), asciiUri.lastIndexOf(rightPart)));
        }

        throw new IllegalArgumentException();
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
    public boolean add(URI uri) {
        return this.idsSet.add(extractIdFromURI(uri));
    }

    @Override
    public boolean offer(URI uri) {
        return this.idsSet.offer(extractIdFromURI(uri));
    }

    @Override
    public URI remove() {
        return createUriFromId(this.idsSet.remove());
    }

    @Override
    public URI poll() {
        final Integer poll = this.idsSet.poll();

        if (poll != null) {
            return createUriFromId(poll);
        } else {
            return null;
        }
    }

    @Override
    public URI element() {
        return createUriFromId(this.idsSet.element());
    }

    @Override
    public URI peek() {
        final Integer peek = this.idsSet.peek();

        if (peek != null) {
            return createUriFromId(peek);
        } else {
            return null;
        }
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof URI uri) {
            return this.idsSet.remove(extractIdFromURI(uri));
        }

        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false; //todo
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends URI> c) {
        if (c instanceof RangeIntUriSet rangeIntUriSet) {
            return this.idsSet.addAll(rangeIntUriSet.idsSet);
        } else {
            return this.idsSet.addAll(c.stream().map(this::extractIdFromURI).collect(Collectors.toList()));
        }
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false; //todo
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false; //todo
    }

    @Override
    public void clear() {
        this.idsSet.clear();
    }

    @Nullable
    @Override
    public Comparator<? super URI> comparator() {
        return null; //todo
    }

    @NotNull
    @Override
    public SortedSet<URI> subSet(URI fromElement, URI toElement) {
        return null; //todo
    }

    @NotNull
    @Override
    public SortedSet<URI> headSet(URI toElement) {
        return null; //todo
    }

    @NotNull
    @Override
    public SortedSet<URI> tailSet(URI fromElement) {
        return null; //todo
    }

    @Override
    public URI first() {
        return createUriFromId(this.idsSet.first());
    }

    @Override
    public URI last() {
        return createUriFromId(this.idsSet.last());
    }
}
