package com.jbialy.rce.collections.workspace;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jbialy.rce.downloader.JobStatistics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JobWorkspace2Impl<T extends Comparable<T>> implements JobWorkspace_2<T> {
    //  to do
    //  in progress
    //  done
    //  damaged
    private final SortedSet<T> allItems;
    private final NavigableSet<T> toDoItems;
    private final Set<T> inProgressItems;
    private final Set<T> doneItems;
    private final Set<T> damagedItems;
    private final JobStatistics jobStatistics;
    //    private final Lock lock = new ReentrantLock();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Condition inProgressEmptyCondition = readWriteLock.writeLock().newCondition();

    private JobWorkspace2Impl() {
        this.allItems = new TreeSet<>();
        this.toDoItems = new TreeSet<>();
        this.inProgressItems = new HashSet<>();
        this.doneItems = new HashSet<>();
        this.damagedItems = new HashSet<>();
        this.jobStatistics = new JobStatistics();
    }

    private JobWorkspace2Impl(Collection<T> todo) {
        this();
        this.toDoItems.addAll(todo);

        allItems.addAll(this.toDoItems);

        this.jobStatistics.incrementTasksToDo(todo.size());
    }

    private JobWorkspace2Impl(Collection<T> todo, Collection<T> inProgress, Collection<T> done, Collection<T> damaged) {
        this.allItems = new TreeSet<>();
        this.toDoItems = new TreeSet<>(todo);
        this.inProgressItems = new HashSet<>(inProgress);
        this.doneItems = new HashSet<>(done);
        this.damagedItems = new HashSet<>(damaged);

        allItems.addAll(this.toDoItems);
        allItems.addAll(this.inProgressItems);
        allItems.addAll(this.doneItems);
        allItems.addAll(this.damagedItems);

        this.jobStatistics = new JobStatistics();
    }

    private JobWorkspace2Impl(NavigableSet<T> todo, Set<T> inProgress, Set<T> done, Set<T> damaged, JobStatistics jobStatistics) {
        this.allItems = new TreeSet<>();
        this.toDoItems = todo;
        this.inProgressItems = inProgress;
        this.doneItems = done;
        this.damagedItems = damaged;

        allItems.addAll(this.toDoItems);
        allItems.addAll(this.inProgressItems);
        allItems.addAll(this.doneItems);
        allItems.addAll(this.damagedItems);

        this.jobStatistics = jobStatistics;
    }

    public static <T extends Comparable<T>> JobWorkspace2Impl<T> createEmpty() {
        return new JobWorkspace2Impl<>();
    }

    public static <T extends Comparable<T>> JobWorkspace2Impl<T> fromToDoCollection(Collection<T> todo) {
        return new JobWorkspace2Impl<>(todo);
    }

    public static <T extends Comparable<T>> JobWorkspace2Impl<T> deserializeFromJSON(String json, Class<T> type) {
        return new GsonBuilder().create().fromJson(json, TypeToken.getParameterized(JobWorkspace2Impl.class, type).getType());
    }

    public static <T extends Comparable<T>> JobWorkspace2Impl<T> fromJobWorkspace(JobWorkspace_2<T> otherWS, boolean interpretInProgressAsToDo) {
        final JobWorkspace_2<T> otherWorkspaceCopy = otherWS.copy();

        final Collection<T> todo = otherWorkspaceCopy.getToDoItems();
        final Collection<T> inProgress = otherWorkspaceCopy.getInProgressItems();
        final Collection<T> done = otherWorkspaceCopy.getDoneItems();
        final Collection<T> damaged = otherWorkspaceCopy.getDamagedItems();

        if (interpretInProgressAsToDo) {
            todo.addAll(inProgress);
            inProgress.clear();
        }

        return new JobWorkspace2Impl<>(todo, inProgress, done, damaged);
    }

    public static <T extends Comparable<T>> String toJSON(JobWorkspace_2<T> otherWS, boolean interpretInProgressAsToDo) {
        if (otherWS instanceof JobWorkspace2Impl<T> jobWorkspace) {
            return jobWorkspace.serializeToJSON();
        } else {
            return fromJobWorkspace(otherWS, interpretInProgressAsToDo).serializeToJSON();
        }
    }

    public static <T extends Comparable<T>> JobWorkspace2Impl<T> fromFileOrElseCreateFromSeed(Class<T> type, Path path, T seed) {
        try {
            final JobWorkspace2Impl<T> workspace = deserializeFromJSON(Files.readString(path, StandardCharsets.UTF_8), type);

            if (workspace == null) {
                return fromToDoCollection(Collections.singletonList(seed));
            } else {
                return workspace;
            }
        } catch (IOException e) {
            return fromToDoCollection(Collections.singletonList(seed));
        }
    }

    public String serializeToJSON() {
        return new GsonBuilder().create().toJson(copy());
    }

    @Override
    public JobWorkspace_2<T> copy() {
        NavigableSet<T> toDoItems;
        Set<T> inProgressItems;
        Set<T> doneItems;
        Set<T> damagedItems;
        JobStatistics jobStatistics;

        readWriteLock.readLock().lock();
        try {
            toDoItems = new TreeSet<>(this.toDoItems);
            inProgressItems = new HashSet<>(this.inProgressItems);
            doneItems = new HashSet<>(this.doneItems);
            damagedItems = new HashSet<>(this.damagedItems);
            jobStatistics = new JobStatistics(this.jobStatistics);
        } finally {
            readWriteLock.readLock().unlock();
        }

        return new JobWorkspace2Impl(toDoItems, inProgressItems, doneItems, damagedItems, jobStatistics);
    }

    @Override
    public JobStatistics getJobStatistics() {
        return this.jobStatistics;
    }

    @Override
    public boolean add(T t) {
        readWriteLock.writeLock().lock();
        try {
            final boolean addToAllResult = allItems.add(t);

            if (addToAllResult) {
                final boolean addToToDoResult = toDoItems.add(t);
                this.jobStatistics.incrementTasksToDo();

                if (!addToToDoResult) {
                    throw new IllegalStateException("Item in allItems:" + true + " in toDoItems:" + addToToDoResult + " expected: true, true");
                }

                return true;
            }

            return false;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean addAllToDo(Collection<T> collection) {
        boolean result = false;

        readWriteLock.writeLock().lock();
        try {
            for (T t : collection) {
                result |= add(t);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }


        return result;
    }

    @Override
    public T moveToProcessingAndReturn() {
        readWriteLock.writeLock().lock();
        try {
            final T poll = toDoItems.pollFirst();
            if (poll != null) {
                inProgressItems.add(poll);
                return poll;
            } else {
                return null;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public T moveToProcessingAndReturnWaitIfInProgressIsNotEmpty() {
        readWriteLock.writeLock().lock();
        try {
            while (true) {
                final T poll = toDoItems.pollFirst();
                if (poll != null) {
                    inProgressItems.add(poll);
                    return poll;
                } else if (this.toDoItems.isEmpty() && this.inProgressItems.isEmpty()) {
                    return null;
                } else {
                    inProgressEmptyCondition.await();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return null;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean moveToDone(T t) {
        readWriteLock.writeLock().lock();
        try {
            final boolean removedFromProcessing = inProgressItems.remove(t);

            if (inProgressItems.isEmpty()) {
                inProgressEmptyCondition.signalAll();
            }

            if (removedFromProcessing) {
                if (!this.doneItems.add(t)) {
                    throw new IllegalStateException("Item already present in doneItems");
                }

                this.jobStatistics.moveOneTaskFromToDoToDone();

                final long statisticsDoneTasks = this.jobStatistics.getDoneTasks();
                final int doneItems = this.doneItems.size();
                if (statisticsDoneTasks != doneItems) {
                    throw new IllegalStateException("Incorrect done tasks count");
                }
            }

            return removedFromProcessing;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean moveToDone(T t1, T t2) {
        readWriteLock.writeLock().lock();
        try {
            return moveToDone(t1) | moveToDone(t2);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean moveToDamaged(T t) {
        readWriteLock.writeLock().lock();
        try {
            final boolean removedFromProcessing = inProgressItems.remove(t);

            if (inProgressItems.isEmpty()) {
                inProgressEmptyCondition.signalAll();
            }

            if (removedFromProcessing) {
                if (!damagedItems.add(t)) {
                    throw new IllegalStateException("Item already present in damagedItems");
                }
            }

            return removedFromProcessing;
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public int allItemsCount() {
        readWriteLock.readLock().lock();
        try {
            return this.allItems.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public int todoCount() {
        readWriteLock.readLock().lock();
        try {
            return this.toDoItems.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public int inProgressCount() {
        readWriteLock.readLock().lock();
        try {
            return this.inProgressItems.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public int doneCount() {
        readWriteLock.readLock().lock();
        try {
            return this.doneItems.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public int damagedCount() {
        readWriteLock.readLock().lock();
        try {
            return this.damagedItems.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public boolean isToDoEmpty() {
        readWriteLock.readLock().lock();
        try {
            return this.toDoItems.isEmpty();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public boolean isToDoAndInProgressEmpty() {
        readWriteLock.readLock().lock();
        try {
            return this.toDoItems.isEmpty() && this.inProgressItems.isEmpty();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Collection<T> getToDoItems() {
        readWriteLock.readLock().lock();
        try {
            return new TreeSet<>(this.toDoItems);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Collection<T> getInProgressItems() {
        readWriteLock.readLock().lock();
        try {
            return new HashSet<>(this.inProgressItems);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Collection<T> getDoneItems() {
        readWriteLock.readLock().lock();
        try {
            return new HashSet<>(this.doneItems);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Collection<T> getDamagedItems() {
        readWriteLock.readLock().lock();
        try {
            return new HashSet<>(this.damagedItems);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
