package com.jbialy.rce;


import com.jbialy.rce.utils.Utils;

public class JobStatistics {
    private long tasksSentToReceiver = 0L;
    private long doneTasks = 0L;
    private long tasksInProgress = 0L;
    private long tasksTodo = 0L;

    public JobStatistics() {
    }

    public JobStatistics(long tasksSentToReceiver, long doneTasks, long tasksInProgress, long tasksTodo) {
        this.tasksSentToReceiver = tasksSentToReceiver;
        this.doneTasks = doneTasks;
        this.tasksInProgress = tasksInProgress;
        this.tasksTodo = tasksTodo;
    }

    public synchronized long getTasksSentToReceiver() {
        return tasksSentToReceiver;
    }

    public synchronized long getDoneTasks() {
        return doneTasks;
    }

    public synchronized long getTasksInProgress() {
        return tasksInProgress;
    }

    public synchronized long getTasksTodo() {
        return tasksTodo;
    }

    @Override
    public String toString() {
        return "JobStatistics{" +
                "tasksSentToReceiver=" + Utils.toHumanReadableMetricSize(tasksSentToReceiver, " ", 2) +
                ", doneTasks=" + Utils.toHumanReadableMetricSize(doneTasks, " ", 2) +
//                ", tasksInProgress=" + Utils.toHumanReadableMetricSize(tasksInProgress, " ", 2) +
                ", tasksTodo=" + Utils.toHumanReadableMetricSize(tasksTodo, " ", 2) +
//                ", allTasks=" + Utils.toHumanReadableMetricSize(tasksTodo + tasksInProgress + doneTasks, " ", 2) +
                ", allTasks=" + Utils.toHumanReadableMetricSize(tasksTodo  + doneTasks, " ", 2) +
                '}';
    }

    public synchronized JobStatistics copy() {
        return new JobStatistics(tasksSentToReceiver, doneTasks, tasksInProgress, tasksTodo);
    }

    public synchronized void incrementTasksInProgress() {
        this.tasksInProgress++;
    }

    public synchronized void moveOneTaskFromToDoToInProgress() {
        this.tasksTodo--;
        this.tasksInProgress++;
    }

    public synchronized void decrementTasksInProgress() {
        this.tasksInProgress--;
    }

    public synchronized void moveOneTaskFromInProgressToDone() {
        this.tasksInProgress--;
        this.doneTasks++;
    }

    public synchronized void incrementTasksSentToReceiver() {
        this.tasksSentToReceiver++;
    }

    public synchronized void incrementDoneTasks() {
        this.doneTasks++;
    }

    public synchronized void setTasksToDo(long toDoAndInProgressAndDoneCount) {
        this.tasksTodo = toDoAndInProgressAndDoneCount;
    }

    public synchronized void incrementTasksToDo(long incrementBy) {
        this.tasksTodo = this.tasksTodo + incrementBy;
    }

    public synchronized void incrementTasksToDo() {
        this.tasksTodo++;
    }

    public synchronized long getAllTasksCount() {
        return this.doneTasks + this.tasksInProgress + this.tasksTodo;
    }
}
