package com.jbialy.rce.collections;


import com.jbialy.rce.JobStatistics;

import java.util.Collection;

public interface JobWorkspace<T> {

    JobWorkspace<T> copy();

    JobStatistics getJobStatistics();

    T getAndMarkAsInProgress();

    boolean isToDoEmpty();

    int todoCount();

    boolean isInProgressEmpty();

    boolean addToDo(T t);

    boolean addAllToDo(Collection<T> collection);

    boolean markAsDone(T t);

    boolean markAsDone(T t1, T t2);

    int doneCount();

    int toDoAndInProgressAndDoneCount();

    Collection<T> getAllItems();

    Collection<T> getToDoItems();

    Collection<T> getDoneItems();

    Collection<T> getExceptDone(Collection<T> col);
}
