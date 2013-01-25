/**
 * 
 */
package org.jokerd.opensocial.scheduler;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.jokerd.opensocial.scheduler.TaskManager;
import org.jokerd.opensocial.scheduler.TaskManager.ITaskExecutor;
import org.jokerd.opensocial.scheduler.TaskManager.ITaskQueue;
import org.jokerd.opensocial.scheduler.TaskManager.ITaskScheduler;
import org.jokerd.opensocial.scheduler.TaskManager.InMemoryTaskQueue;

/**
 * @author kotelnikov
 */
public class TaskManagerTest extends TestCase {

    /**
     * @author kotelnikov
     */
    public static class TestTask {

        protected int fCounter;

        private Date fTime;

        private final int fTimeout;

        public TestTask(int timeout, int count) {
            this(-1, timeout, count);
        }

        public TestTask(long time, int timeout, int count) {
            fTimeout = timeout;
            fCounter = count;
            if (time > 0) {
                fTime = new Date(time);
            }
        }

        public synchronized boolean generateNextUpdateTime() {
            fTime = newTime();
            if (fCounter < 0) {
                return true;
            }
            if (fCounter > 0) {
                fCounter--;
                return true;
            }
            return false;
        }

        public synchronized int getCounter() {
            return fCounter;
        }

        public synchronized Date getTime() {
            return fTime;
        }

        public synchronized int getTimeout() {
            return fTimeout;
        }

        protected synchronized Date newTime() {
            return new Date(System.currentTimeMillis() + fTimeout);
        }

        @Override
        public String toString() {
            return "[" + fTimeout + "]=>" + fTime;
        }

    }

    // Maximal delay (in milliseconds) between two task calls
    private static final int MAX_TIMEOUT = 30;

    private int fClientCounter;

    private int fExecutionCounter;

    private CountDownLatch fLatch;

    private Random fRandom = new Random(System.currentTimeMillis());

    private TaskManager<TestTask> fTaskManager;

    private ITaskScheduler<TestTask> fTaskScheduler = new ITaskScheduler<TestTask>() {

        @Override
        public Date getTime(TestTask task) {
            return task.getTime();
        }

        @Override
        public void updateTime(TestTask task) {
            task.generateNextUpdateTime();
        }
    };

    /**
     * @param name
     */
    public TaskManagerTest(String name) {
        super(name);
    }

    private TestTask newTask(long startTime, int timeout, int cycles) {
        return new TestTask(startTime, timeout, cycles);
    }

    protected void reset(int clientNumber) {
        fLatch = new CountDownLatch(clientNumber);
        fClientCounter = 0;
        fExecutionCounter = 0;
    }

    protected void schedule(final int cycles) {
        int timeout = 0;
        while (timeout < 2) {
            timeout = fRandom.nextInt(MAX_TIMEOUT);
        }
        long startTime = System.currentTimeMillis();
        fTaskManager.schedule(newTask(startTime, timeout, cycles));
    }

    @Override
    protected void setUp() throws Exception {
        ITaskExecutor<TestTask> executor = new ITaskExecutor<TestTask>() {
            @Override
            public void execute(TestTask task, ICallback callback) {
                boolean reschedule = true;
                try {
                    TestTask t = task;
                    fExecutionCounter++;
                    if (t.getCounter() == 0) {
                        reschedule = false;
                        fClientCounter++;
                        fLatch.countDown();
                    }
                } finally {
                    callback.finish(reschedule);
                }
            }
        };
        ITaskQueue<TestTask> queue = new InMemoryTaskQueue<TestTask>(
            fTaskScheduler);
        fTaskManager = new TaskManager<TestTask>(
            fTaskScheduler,
            queue,
            executor);
    }

    public void testInMemoryTaskQueue() {
        testTaskQueue();
        testTaskQueue(newTask(68979, 10, 1));
        testTaskQueue(newTask(32400, 10, 1), newTask(68979, 10, 1));
        testTaskQueue(
            newTask(12300, 10, 1),
            newTask(19000, 10, 1),
            newTask(57800, 10, 1),
            newTask(10980, 10, 1),
            newTask(12310, 10, 1),
            newTask(32400, 10, 1),
            newTask(98324, 10, 1),
            newTask(29879, 10, 1),
            newTask(68979, 10, 1));
    }

    public void testMultiple() throws Exception {
        System.out.println("Test for multiple concurrent tasks.");
        testMultiple(2, 1);
        testMultiple(1, 1);
        testMultiple(1, 2);
        testMultiple(100, 1);
        testMultiple(1, 123);
        testMultiple(100, 123);
    }

    protected void testMultiple(final int cycles, final int clientNumber)
        throws InterruptedException {
        System.out.println("Client number: "
            + clientNumber
            + ". Number of cycles: "
            + cycles
            + ".");
        reset(clientNumber);
        for (int i = 0; i < clientNumber; i++) {
            schedule(cycles);
        }
        fLatch.await(MAX_TIMEOUT * cycles + 100, TimeUnit.MILLISECONDS);
        assertEquals(clientNumber * cycles, fExecutionCounter);
        assertEquals(clientNumber, fClientCounter);
    }

    public void testOne() throws Exception {
        final int cycles = 100;
        fLatch = new CountDownLatch(1);
        schedule(cycles);
        fLatch.await(30, TimeUnit.SECONDS);
        assertEquals(cycles, fExecutionCounter);
    }

    protected void testTaskQueue(
        ITaskQueue<TestTask> taskQueue,
        TestTask... tasks) {
        for (TestTask task : tasks) {
            taskQueue.enqueue(task);
        }
        int counter = 0;
        Date prevTime = null;
        while (true) {
            TestTask task = taskQueue.dequeue();
            if (task == null) {
                break;
            }
            Date time = fTaskScheduler.getTime(task);
            if (prevTime != null) {
                assertTrue(prevTime.compareTo(time) < 0);
            }
            prevTime = time;
            counter++;
        }
        assertEquals(tasks.length, counter);
    }

    protected void testTaskQueue(TestTask... tasks) {
        InMemoryTaskQueue<TestTask> manager = new InMemoryTaskQueue<TestTask>(
            fTaskScheduler);
        testTaskQueue(manager, tasks);
    }

    public void testUpdatableTask() {
        TestTask task = new TestTask(1, 1);
        assertEquals(1, task.getCounter());
        assertNull(task.getTime());
        assertEquals(1, task.getCounter());
        task.generateNextUpdateTime();
        assertEquals(0, task.getCounter());
        assertNotNull(task.getTime());

        int counter = 100;
        task = new TestTask(1, counter);
        for (int i = 0; i < counter; i++) {
            assertTrue(task.generateNextUpdateTime());
        }
        assertFalse(task.generateNextUpdateTime());
    }

}
