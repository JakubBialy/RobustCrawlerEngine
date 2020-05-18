package com.jbialy.rce.collections.workspace;

import com.jbialy.rce.collections.RangeIntUriSet;
import com.jbialy.rce.collections.workspace.implementation.CollectionWorkspaceOld;
import com.jbialy.rce.collections.workspace.implementation.GeneralPurposeJobWorkspace;
import com.jbialy.rce.downloader.JobStatistics;
import com.jbialy.rce.utils.NotImplementedError;

import java.net.URI;
import java.util.Collection;
import java.util.function.Supplier;

public class WorkspaceHelper {
    private WorkspaceHelper() {
    }

    public static <T> JobWorkspaceOld<T> synchronize(JobWorkspaceOld<T> ws) {
        return new JobWorkspaceOld<T>() {
            private final JobWorkspaceOld<T> internalJobWorkspaceOld = ws;

            @Override
            public synchronized JobWorkspaceOld<T> copy() {
                return ws.copy();
            }

            @Override
            public synchronized JobStatistics getJobStatistics() {
                return ws.getJobStatistics();
            }

            @Override
            public synchronized T getAndMarkAsInProgress() {
                return internalJobWorkspaceOld.getAndMarkAsInProgress();
            }

            @Override
            public synchronized boolean isToDoEmpty() {
                return internalJobWorkspaceOld.isToDoEmpty();
            }

            @Override
            public synchronized int todoCount() {
                return ws.todoCount();
            }

            @Override
            public synchronized boolean isInProgressEmpty() {
                return internalJobWorkspaceOld.isInProgressEmpty();
            }

            @Override
            public synchronized boolean addToDo(T t) {
                return internalJobWorkspaceOld.addToDo(t);
            }

            @Override
            public synchronized boolean addAllToDo(Collection<T> collection) {
                return internalJobWorkspaceOld.addAllToDo(collection);
            }

            @Override
            public synchronized boolean markAsDone(T t) {
                return internalJobWorkspaceOld.markAsDone(t);
            }

            @Override
            public boolean markAsDone(T t1, T t2) {
                return ws.markAsDone(t1, t2);
            }

            @Override
            public synchronized int doneCount() {
                return ws.doneCount();
            }

            @Override
            public synchronized int toDoAndInProgressAndDoneCount() {
                return ws.toDoAndInProgressAndDoneCount();
            }

            @Override
            public synchronized Collection<T> getAllItems() {
                return internalJobWorkspaceOld.getAllItems();
            }

            @Override
            public synchronized Collection<T> getToDoItems() {
                return ws.getToDoItems();
            }

            @Override
            public synchronized Collection<T> getDoneItems() {
                return ws.getDoneItems();
            }

            @Override
            public Collection<T> getExceptDone(Collection<T> col) {
                return null;
            }
        };
    }

    public static <T extends Comparable<T>> JobWorkspaceOld<T> fromCollection(Collection<T> collection) {
        return new CollectionWorkspaceOld<T>(collection);
    }

    public static <U extends Comparable<U>> JobWorkspaceOld<U> fromCollectionAsSet(Collection<U> collection) {
        return CollectionWorkspaceOld.fromToDoCollection(collection);
    }

    public static <U extends Comparable<U>> JobWorkspace<U> fromCollectionAsSet_2(Collection<U> collection) {
        return GeneralPurposeJobWorkspace.fromToDoCollection(collection);
    }

    public static class MemoryEfficient {
        private MemoryEfficient() {
        }

        public static Supplier<JobWorkspaceOld<URI>> fromRangeIntUriSet(RangeIntUriSet rangeIntUriSet) {
            final String leftPart = rangeIntUriSet.getLeftPart();
            final String rightPart = rangeIntUriSet.getRightPart();

//            return rangeIntUriSet(leftPart, rightPart);
            return rangeIntUriSet(rangeIntUriSet);
        }

        public static Supplier<JobWorkspaceOld<URI>> rangeIntUriSet(/*String leftPart, String rightPart,*/ RangeIntUriSet uriSet) {
            return new Supplier<>() {
                @Override
                public JobWorkspaceOld<URI> get() {
                    return new JobWorkspaceOld<>() {
                        private final RangeIntUriSet todo = new RangeIntUriSet(uriSet);
                        private final RangeIntUriSet inProgress = RangeIntUriSet.empty(todo.getLeftPart(), todo.getRightPart());
                        private int doneCounter = 0;
//                        private AtomicInteger doneCounter = new AtomicInteger(0);

                        @Override
                        public JobWorkspaceOld<URI> copy() {
                            throw new NotImplementedError();
                        }

                        @Override
                        public JobStatistics getJobStatistics() {
                            throw new NotImplementedError();
                        }

                        @Override
                        public synchronized URI getAndMarkAsInProgress() {
                            if (!todo.isEmpty()) {
                                URI item = todo.first();

                                if (item != null) {
                                    inProgress.add(item);
                                    todo.remove(item);
                                    inProgress.add(item);

                                    return item;
                                }
                            }
                            return null;
                        }

                        @Override
                        public synchronized boolean isToDoEmpty() {
                            return this.todo.isEmpty();
                        }

                        @Override
                        public synchronized int todoCount() {
                            return this.todo.size();
                        }

                        @Override
                        public synchronized boolean isInProgressEmpty() {
                            return this.inProgress.isEmpty();
                        }

                        @Override
                        public synchronized boolean addToDo(URI uri) {
                            if (this.todo.contains(uri)) {
                                return false;
                            }

                            return this.todo.add(uri);
                        }

                        @Override
                        public synchronized boolean addAllToDo(Collection<URI> collection) {
                            return todo.addAll(collection);
                        }

                        @Override
                        public synchronized boolean markAsDone(URI uri) {
                            boolean removeResult = this.inProgress.remove(uri);

                            if (removeResult) {
                                doneCounter++;
                            }

                            return removeResult;
                        }

                        @Override
                        public boolean markAsDone(URI t1, URI t2) {
                            throw new NotImplementedError();
                        }

                        @Override
                        public synchronized int doneCount() {
                            return this.doneCounter;
                        }

                        @Override
                        public synchronized int toDoAndInProgressAndDoneCount() {
                            return this.todo.size() + this.inProgress.size() + doneCounter;
                        }

                        @Override
                        public synchronized Collection<URI> getAllItems() {
                            Collection<URI> result = new RangeIntUriSet(this.todo);
                            result.addAll(this.inProgress);

                            return result;
                        }

                        @Override
                        public synchronized Collection<URI> getToDoItems() {
                            return new RangeIntUriSet(this.todo);
                        }

                        @Override
                        public Collection<URI> getDoneItems() {
                            throw new NotImplementedError();
                        }

                        @Override
                        public Collection<URI> getExceptDone(Collection<URI> col) {
                            return null;
                        }
                    };
                }
            };
        }
    }
}
