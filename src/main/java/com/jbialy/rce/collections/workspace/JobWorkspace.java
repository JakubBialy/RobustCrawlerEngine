package com.jbialy.rce.collections.workspace;

import com.jbialy.rce.downloader.JobStatistics;

import java.util.Collection;

public interface JobWorkspace<T> {

    JobWorkspace<T> copy();

    JobStatistics getJobStatistics();

    // add new element (to do)
    boolean add(T t);

//    boolean add(T... t);

    boolean addAllToDo(Collection<T> collection);

    // move to processing
    T moveToProcessingAndReturn();

    T moveToProcessingAndReturnWaitIfInProgressIsNotEmpty();

    // move to done
    boolean moveToDone(T t);

    boolean moveToDone(T t1, T t2);

//    boolean moveToDone(T... t);

    // move to damaged
    boolean moveToDamaged(T t);

//    boolean moveToDamaged(T t1, T t2);

//    boolean moveToDamaged(T... t);

    ////////////////////////////
    int allItemsCount();

    int todoCount();

    int inProgressCount();

    int doneCount();

    int damagedCount();

    boolean isToDoEmpty();

    boolean isToDoAndInProgressEmpty();

    ///////////////////////////////
    Collection<T> getToDoItems();

    Collection<T> getInProgressItems();

    Collection<T> getDoneItems();

    Collection<T> getDamagedItems();
}
