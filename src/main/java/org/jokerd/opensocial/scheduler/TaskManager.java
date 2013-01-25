/**
 * 
 */
package org.jokerd.opensocial.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author kotelnikov
 */
public class TaskManager<T> {

    /**
     * @author kotelnikov
     */
    public static class InMemoryTaskQueue<T> implements ITaskQueue<T> {

        private Comparator<T> COMPARATOR = new Comparator<T>() {
            public int compare(T o1, T o2) {
                Date first = fScheduler.getTime(o1);
                Date second = fScheduler.getTime(o2);
                return first.compareTo(second);
            }
        };

        protected Object fMutex = new Object();

        private Queue<T> fQueue;

        private ITaskScheduler<T> fScheduler;

        private Set<T> fStore;

        private long fTimestamp;

        public InMemoryTaskQueue(ITaskScheduler<T> scheduler) {
            fScheduler = scheduler;
        }

        public void delete(T task) {
            synchronized (fMutex) {
                Set<T> set = getTaskSet();
                set.remove(task);
                doSaveTasks(set);
            }
        }

        public T dequeue() {
            synchronized (fMutex) {
                return getQueue().poll();
            }
        }

        protected Set<T> doLoadTasks() {
            Set<T> set = loadAllTasks();
            fTimestamp = newTimestamp();
            return set;
        }

        protected void doSaveTasks(Set<T> set) {
            saveAllTasks(set);
            fTimestamp = newTimestamp();
        }

        public void enqueue(T task) {
            synchronized (fMutex) {
                getQueue().add(task);
                long now = newTimestamp();
                if (now - fTimestamp > getDelta()) {
                    Set<T> tasks = getTaskSet();
                    doSaveTasks(tasks);
                }
            }
        }

        protected long getDelta() {
            int sec = 1000;
            int min = 60 * sec;
            return 2 * min;
        }

        protected Queue<T> getQueue() {
            synchronized (fMutex) {
                if (fQueue == null) {
                    getTaskSet();
                }
                return fQueue;
            }
        }

        public List<T> getTasks() {
            synchronized (fMutex) {
                Set<T> set = getTaskSet();
                return new ArrayList<T>(set);
            }
        }

        protected Set<T> getTaskSet() {
            synchronized (fMutex) {
                if (fStore == null) {
                    fStore = doLoadTasks();
                    fQueue = new PriorityQueue<T>(fStore.size() + 1, COMPARATOR);
                    fQueue.addAll(fStore);
                }
                return fStore;
            }
        }

        protected Set<T> loadAllTasks() {
            return new HashSet<T>();
        }

        protected long newTimestamp() {
            return System.currentTimeMillis();
        }

        public void save(T task) {
            synchronized (fMutex) {
                Set<T> set = getTaskSet();
                set.add(task);
                doSaveTasks(set);
            }
        }

        protected void saveAllTasks(Set<T> set) {
        }

        public void setTasks(Collection<? extends T> list) {
            synchronized (fMutex) {
                Set<T> set = getTaskSet();
                set.clear();
                set.addAll(list);
                doSaveTasks(set);
            }
        }

    }

    /**
     * Executes the specified task.
     * 
     * @author kotelnikov
     */
    public interface ITaskExecutor<T> {

        public interface ICallback {
            void finish(boolean enqueue);
        }

        void execute(T task, ICallback callback);
    }

    /**
     * This interface is used to manage a set of tasks ordered by the task
     * execution time (see the {@link T#getTime()} method) in incremental order.
     * 
     * @author kotelnikov
     */
    public interface ITaskQueue<T> {

        void delete(T task);

        /**
         * @return the next task to execute
         */
        T dequeue();

        /**
         * Adds a new task to store.
         * 
         * @param task the task to add in the queue
         */
        void enqueue(T task);

        void save(T task);

    }

    public interface ITaskScheduler<T> {

        Date getTime(T task);

        void updateTime(T task);
    }

    private Object fMutex = new Object();

    private T fTask;

    private ITaskExecutor<T> fTaskExecutor;

    private ITaskQueue<T> fTaskQueue;

    private ITaskScheduler<T> fTaskScheduler;

    private Timer fTimer = new Timer();

    private TimerTask fTimerTask;

    public TaskManager(
        ITaskScheduler<T> scheduler,
        ITaskQueue<T> taskQueue,
        ITaskExecutor<T> taskExecutor) {
        fTaskQueue = taskQueue;
        fTaskExecutor = taskExecutor;
        fTaskScheduler = scheduler;
    }

    private T clear() {
        synchronized (fMutex) {
            T result = null;
            if (fTask != null) {
                result = fTask;
                fTask = null;
                fTimerTask.cancel();
                fTimerTask = null;
            }
            return result;
        }
    }

    public void close() {
        clear();
        fTimer.cancel();
    }

    public ITaskExecutor<T> getTaskExecutor() {
        return fTaskExecutor;
    }

    public ITaskQueue<T> getTaskQueue() {
        return fTaskQueue;
    }

    public void open() {
        schedule(null);
    }

    public void schedule(final T task) {
        schedule(task, true);
    }

    private void schedule(final T task, boolean save) {
        synchronized (fMutex) {
            T prevTask = clear();
            if (prevTask != null) {
                fTaskQueue.enqueue(prevTask);
            }
            if (task != null) {
                if (save) {
                    fTaskQueue.save(task);
                }
                fTaskScheduler.updateTime(task);
                fTaskQueue.enqueue(task);
            }
            T nextTask = fTaskQueue.dequeue();
            if (nextTask != null) {
                fTask = nextTask;
                Date time = fTaskScheduler.getTime(fTask);
                fTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        final T task = clear();
                        if (task != null) {
                            fTaskExecutor.execute(
                                task,
                                new ITaskExecutor.ICallback() {
                                    public void finish(boolean enqueue) {
                                        if (enqueue) {
                                            schedule(task, false);
                                        } else {
                                            fTaskQueue.delete(task);
                                        }
                                    }
                                });
                        }
                        // Go to the next element in the queue.
                        schedule(null, false);
                    }
                };
                fTimer.schedule(fTimerTask, time);
            }
        }
    }
}
