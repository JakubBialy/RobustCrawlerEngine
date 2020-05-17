package com.jbialy.rce.collections.workspace;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jbialy.rce.downloader.JobStatistics;
import com.jbialy.rce.utils.NotImplementedError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CollectionWorkspace<T> implements JobWorkspace<T> {
    private final HashMap<T, Long> map;
    private final transient Lock lock;
    private final transient Condition changeCondition;
    private final JobStatistics jobStatistics;

    public CollectionWorkspace(Collection<T> collection) {
        this.lock = new ReentrantLock();
        this.changeCondition = lock.newCondition();

        this.map = new HashMap<>(collection.size());

        for (T t : collection) {
            this.map.putIfAbsent(t, 0L);
        }

        long toDoCount = this.map.values().stream().filter(val -> val.equals(0L)).count();
//        long inProgressCount = this.map.values().stream().filter(val -> val.equals(1L)).count();
        long doneCount = this.map.values().stream().filter(val -> val > 1L).count();
//        jobStatistics = new JobStatistics(0L, doneCount, inProgressCount, toDoCount);
        jobStatistics = new JobStatistics(0L, doneCount, toDoCount);
    }

    private CollectionWorkspace(HashMap<T, Long> map) {
        this(map, new JobStatistics());
    }

    private CollectionWorkspace(HashMap<T, Long> map, JobStatistics jobStatisticsCopy) {
        this.lock = new ReentrantLock();
        this.changeCondition = lock.newCondition();
        this.map = map;
        this.jobStatistics = jobStatisticsCopy;
    }

    public static <T> CollectionWorkspace<T> fromJobWorkspace(JobWorkspace<T> otherWS, boolean interpretInProgressAsToDo) {
        if (interpretInProgressAsToDo) {
            if (otherWS instanceof CollectionWorkspace<T> collectionWS) {
                final HashMap<T, Long> otherCollectionWSMapCopy = new HashMap<>((collectionWS).map);

                otherCollectionWSMapCopy
                        .entrySet()
                        .forEach((entry -> {
                            if (entry.getValue() < 2) {
                                entry.setValue(0L);
                            }
                        }));

                long toDoCount = otherCollectionWSMapCopy.values().stream().filter(val -> val.equals(0L)).count();
                long inProgressCount = otherCollectionWSMapCopy.values().stream().filter(val -> val.equals(1L)).count();
                long doneCount = otherCollectionWSMapCopy.values().stream().filter(val -> val > 1L).count();
//                final JobStatistics statistics = new JobStatistics(otherWS.getJobStatistics().getTasksSentToReceiver(), doneCount, inProgressCount, toDoCount);
                final JobStatistics statistics = new JobStatistics(otherWS.getJobStatistics().getTasksSentToReceiver(), doneCount, toDoCount);

                return new CollectionWorkspace<T>(otherCollectionWSMapCopy, statistics);
            } else {
                throw new NotImplementedError();
            }
        } else {
            if (otherWS instanceof CollectionWorkspace<T> collectionWS) {
                return new CollectionWorkspace<T>(new HashMap<>((collectionWS).map));
            } else {
                throw new NotImplementedError();
            }
        }
    }

    public static <T> String toJSON(JobWorkspace<T> otherWS, boolean interpretInProgressAsToDo) {
        return fromJobWorkspace(otherWS, interpretInProgressAsToDo).serializeToJSON();
    }

    public static <T> CollectionWorkspace<T> fromToDoCollection(Collection<T> todoCollection) {
        return new CollectionWorkspace<>(todoCollection);
    }

    public static <T> CollectionWorkspace<T> deserializeFromJSON(String json, Class<T> type) {
        return new GsonBuilder().create().fromJson(json, TypeToken.getParameterized(CollectionWorkspace.class, type).getType());
    }

    public static <T> CollectionWorkspace<T> fromFileOrElseGet(Class<T> type, Path path, Supplier<CollectionWorkspace<T>> supplier) {
        try {
            return CollectionWorkspace.deserializeFromJSON(Files.readString(path, StandardCharsets.UTF_8), type);
        } catch (IOException e) {
            return supplier.get();
        }
    }

    public static <T> CollectionWorkspace<T> fromFileOrElseCreateFromSeed(Class<T> type, Path path, T seed) {
        try {
            final CollectionWorkspace<T> workspace = CollectionWorkspace.deserializeFromJSON(Files.readString(path, StandardCharsets.UTF_8), type);

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
        lock.lock();
        try {
            return new GsonBuilder().create().toJson(this);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public JobWorkspace<T> copy() {
        HashMap<T, Long> mapCopy;
        JobStatistics jobStatisticsCopy;
        lock.lock();
        try {
            mapCopy = new HashMap<>(this.map);
            jobStatisticsCopy = this.jobStatistics.copy();
        } finally {
            lock.unlock();
        }

        return new CollectionWorkspace<>(mapCopy, jobStatisticsCopy);
    }

    @Override
    public JobStatistics getJobStatistics() {
        return this.jobStatistics;
    }

    @Override
    public T getAndMarkAsInProgress() { //todo make it faster
        lock.lock();
        try {

//            while (!this.map.containsValue(0L) && this.map.containsValue(1L)) {
//            while (this.jobStatistics.getTasksTodo() == 0L && this.jobStatistics.getTasksInProgress() != 0L) {
            while (this.jobStatistics.getTasksTodo() == 0L) {
                changeCondition.await();
            }

            return this.map.entrySet()
                    .parallelStream()
                    .filter(entry -> entry.getValue().equals(0L))
                    .findAny()
//                    .map(Map.Entry::getKey)
                    .map(entry -> {
                        final T foundKey = entry.getKey();
                        this.map.put(foundKey, 1L);
//                        this.jobStatistics.moveOneTaskFromToDoToInProgress();
                        return foundKey;

                    }).orElseGet(null);

//            if (result.isPresent()) {
//                T foundKey = result.get();
//                this.map.put(foundKey, 1L);
//                this.jobStatistics.moveOneTaskFromToDoToInProgress();
//                return foundKey;
//            } else {
//                return null;
//            }

//            return getAndMarkAsInProgress_ParallelStream();


//            final Iterator<Map.Entry<T, Long>> entryIterator = this.map.entrySet().iterator();
//            while (entryIterator.hasNext()) {
//                final Map.Entry<T, Long> entry = entryIterator.next();

//                if (entry.getValue().equals(0L)) {
//                    this.map.put(entry.getKey(), entry.getValue() + 1L);
//                    this.jobStatistics.moveOneTaskFromToDoToInProgress();
//                    return entry.getKey();
//                }
//            }

//            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return null;
        } finally {
            changeCondition.signalAll();
            checkInternalState();
            lock.unlock();

        }
    }


    private T getAndMarkAsInProgress_ParallelStream() {
        Optional<T> result = this.map.entrySet()
                .parallelStream()
                .filter(entry -> entry.getValue().equals(0L))
                .findAny()
                .map(Map.Entry::getKey);

        if (result.isPresent()) {
            T foundKey = result.get();
            this.map.put(foundKey, 1L);
//            this.jobStatistics.moveOneTaskFromToDoToInProgress();
            return foundKey;
        } else {
            return null;
        }
    }

    @Override
    public boolean isToDoEmpty() {
        lock.lock();
        try {
            checkInternalState();

            if (this.jobStatistics.getTasksTodo() != 0) {
                return false;
            }

//            while (this.jobStatistics.getTasksTodo() == 0L && this.jobStatistics.getTasksInProgress() != 0) {
            while (this.jobStatistics.getTasksTodo() == 0L) {
                changeCondition.await();
            }

            return !this.map.containsValue(0L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        } finally {
            checkInternalState();
            lock.unlock();
        }
    }

    @Override
    public int todoCount() {
        lock.lock();
        try {
            checkInternalState();
            return (int) this.jobStatistics.getTasksTodo();
        } finally {
            checkInternalState();
            lock.unlock();
        }
    }

    @Override
    public boolean isInProgressEmpty() {
        lock.lock();
        try {
//            return this.jobStatistics.getTasksInProgress() != 0;
            return true;
        } finally {
            lock.unlock();
            checkInternalState();
        }
    }

    @Override
    public boolean addToDo(T t) {
        lock.lock();
        try {
            if (this.map.putIfAbsent(t, 0L) != null) {
                this.jobStatistics.incrementTasksToDo();
                return false;
            } else {
                this.jobStatistics.incrementTasksToDo();
                return true;
            }
        } finally {
            changeCondition.signalAll();
            lock.unlock();
            checkInternalState();
        }
    }

    @Override
    public boolean addAllToDo(Collection<T> collection) {
        lock.lock();
        try {
            checkInternalState();
            int addedCounter = 0;

            for (T t : collection) {
                if (this.map.putIfAbsent(t, 0L) == null) {
                    addedCounter++;
                }
            }

            this.jobStatistics.incrementTasksToDo(addedCounter);

            return addedCounter != 0;
        } finally {
            changeCondition.signalAll();
            checkInternalState();
            lock.unlock();
        }
    }

    @Override
    public boolean markAsDone(T t) {
        lock.lock();
        try {
            return this.map.computeIfPresent(t, (key, oldValue) -> {
                if (oldValue == 1) {
//                    this.jobStatistics.moveOneTaskFromInProgressToDone();
                    this.jobStatistics.moveOneTaskFromToDoToDone();
                }

                return oldValue + 1;
            }) != null;
        } finally {
            changeCondition.signalAll();
            checkInternalState();
            lock.unlock();
        }
    }

    @Override
    public boolean markAsDone(T t1, T t2) {
        if (t1.equals(t2)) {
            return markAsDone(t1);
        } else {
            lock.lock();
            try {
                boolean t1Result = this.map.computeIfPresent(t1, (key, oldValue) -> {
                    if (oldValue == 1) {
//                        this.jobStatistics.moveOneTaskFromInProgressToDone();
                        this.jobStatistics.moveOneTaskFromToDoToDone();
                    }

                    return oldValue + 1;
                }) != null;

                boolean t2Result = this.map.computeIfPresent(t2, (key, oldValue) -> {
                    if (oldValue == 1) {
//                        this.jobStatistics.moveOneTaskFromInProgressToDone();
                        this.jobStatistics.moveOneTaskFromToDoToDone();
                    }

                    return oldValue + 1;
                }) != null;

                return t1Result || t2Result;
            } finally {
                changeCondition.signalAll();
                checkInternalState();
                lock.unlock();
            }
        }
    }

    @Override
    public int doneCount() {
        lock.lock();
        try {
            return (int) this.jobStatistics.getDoneTasks();
        } finally {
            lock.unlock();
            checkInternalState();
        }
    }

    private synchronized void checkInternalState() {
//        long expectedWIPCount = this.map.values().stream().filter(count -> count == 1L).count();
//        long statisticsWIPCount = this.jobStatistics.getTasksInProgress();
//        checkVariables(expectedWIPCount, statisticsWIPCount, "tasksInProgress");
//
//        long expectedDoneCount = this.map.values().stream().filter(count -> count > 1L).count();
//        long statisticsDoneCount = this.jobStatistics.getDoneTasks();
//        checkVariables(expectedDoneCount, statisticsDoneCount, "doneCount");
//
//        long expectedToDoCount = this.map.values().stream().filter(val -> val.equals(0L)).count();
//        long statisticsToDoCount = this.jobStatistics.getTasksTodo();
//        checkVariables(expectedToDoCount, statisticsToDoCount, "toDo");
//
//        final long expectedAllTasksCount = this.map.size();
//        final long currentAllTasksStatisticsCount = this.jobStatistics.getAllTasksCount();
//        checkVariables(expectedAllTasksCount, currentAllTasksStatisticsCount, "allTasksCount");
    }

    private void checkVariables(long expectedValue, long currentValue, String varName) {
        if (expectedValue != currentValue) {
            System.out.println("Wrong " + varName + " value!!!");
            System.out.println("Expected value: " + expectedValue);
            System.out.println("Current value: " + currentValue);
        }
    }

    @Override
    public int toDoAndInProgressAndDoneCount() {
        lock.lock();
        try {
            checkInternalState();
            return (int) this.jobStatistics.getAllTasksCount();
        } finally {
            checkInternalState();
            lock.unlock();
        }
    }

    @Override
    public Collection<T> getAllItems() {
        return this.map.keySet();
    }

    @Override
    public Collection<T> getToDoItems() {
        return this.map.entrySet().stream()
                .filter(entry -> entry.getValue().equals(0L))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<T> getDoneItems() {
        lock.lock();
        try {
            return this.map.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1L)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<T> getExceptDone(Collection<T> col) {
        return null;
    }
}
