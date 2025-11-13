package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Week 7 Implementation: ThreadPoolManager
 *
 * Advanced thread pool management with dynamic sizing, priority-based task execution,
 * and comprehensive monitoring for optimal resource utilization.
 *
 * Key Features:
 * - Dynamic thread pool sizing based on workload
 * - Priority-based task queue
 * - Task timeout and cancellation
 * - Async execution with CompletableFuture support
 * - Thread pool statistics and monitoring
 * - Graceful shutdown handling
 *
 * @author Team 6
 * @version Week 7
 */
public class ThreadPoolManager {

    private static final int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_MAX_POOL_SIZE = DEFAULT_CORE_POOL_SIZE * 2;
    private static final long DEFAULT_KEEP_ALIVE_TIME = 60L;
    private static final int DEFAULT_QUEUE_CAPACITY = 1000;

    private final ThreadPoolExecutor executor;
    private final PriorityBlockingQueue<Runnable> taskQueue;
    private final Map<String, Future<?>> activeTasks;
    private final ThreadPoolStatistics statistics;
    private final ScheduledExecutorService monitoringExecutor;

    private volatile boolean autoTuning;
    private int corePoolSize;
    private int maxPoolSize;

    public ThreadPoolManager() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, DEFAULT_QUEUE_CAPACITY);
    }

    public ThreadPoolManager(int corePoolSize, int maxPoolSize, int queueCapacity) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.taskQueue = new PriorityBlockingQueue<>(queueCapacity, new TaskComparator());
        this.activeTasks = new ConcurrentHashMap<>();
        this.statistics = new ThreadPoolStatistics();
        this.autoTuning = false;

        this.executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            DEFAULT_KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            taskQueue,
            new NamedThreadFactory("SwarmWorker"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.monitoringExecutor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("PoolMonitor")
        );

        startMonitoring();
    }

    public <T> Future<T> submit(Callable<T> task, TaskPriority priority) {
        PrioritizedTask<T> prioritizedTask = new PrioritizedTask<>(task, priority);
        Future<T> future = executor.submit(prioritizedTask);
        statistics.recordTaskSubmitted();
        return future;
    }

    public Future<?> submit(Runnable task, TaskPriority priority) {
        return submit(Executors.callable(task), priority);
    }

    public <T> CompletableFuture<T> submitAsync(Callable<T> task, TaskPriority priority) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public String scheduleTask(Runnable task, TaskPriority priority, String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            taskId = UUID.randomUUID().toString();
        }

        Future<?> future = submit(task, priority);
        activeTasks.put(taskId, future);
        return taskId;
    }

    public boolean cancelTask(String taskId) {
        Future<?> future = activeTasks.remove(taskId);
        if (future != null && !future.isDone()) {
            return future.cancel(true);
        }
        return false;
    }

    public <T> T executeWithTimeout(Callable<T> task, long timeout, TimeUnit unit, TaskPriority priority)
            throws TimeoutException, ExecutionException, InterruptedException {
        Future<T> future = submit(task, priority);
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            statistics.recordTaskTimeout();
            throw e;
        }
    }

    public void executeAsync(Runnable task, TaskPriority priority, AsyncCallback callback) {
        CompletableFuture.runAsync(task, executor)
            .whenComplete((result, error) -> {
                if (error != null) {
                    callback.onFailure(error);
                    statistics.recordTaskFailed();
                } else {
                    callback.onSuccess();
                    statistics.recordTaskCompleted();
                }
            });
    }

    public void setAutoTuning(boolean enabled) {
        this.autoTuning = enabled;
    }

    public void adjustPoolSize(int corePoolSize, int maxPoolSize) {
        if (corePoolSize > 0 && maxPoolSize >= corePoolSize) {
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            executor.setCorePoolSize(corePoolSize);
            executor.setMaximumPoolSize(maxPoolSize);
            statistics.recordPoolSizeAdjustment();
        }
    }

    public ThreadPoolStatistics getStatistics() {
        updateStatistics();
        return statistics.copy();
    }

    public ThreadPoolStatus getStatus() {
        return new ThreadPoolStatus(
            executor.getActiveCount(),
            executor.getPoolSize(),
            executor.getCorePoolSize(),
            executor.getMaximumPoolSize(),
            executor.getQueue().size(),
            executor.getCompletedTaskCount(),
            executor.getTaskCount(),
            activeTasks.size()
        );
    }

    public void shutdown() {
        monitoringExecutor.shutdown();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!monitoringExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                monitoringExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            monitoringExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void shutdownNow() {
        monitoringExecutor.shutdownNow();
        executor.shutdownNow();
        activeTasks.clear();
    }

    private void startMonitoring() {
        monitoringExecutor.scheduleAtFixedRate(() -> {
            try {
                updateStatistics();
                if (autoTuning) {
                    performAutoTuning();
                }
                cleanupCompletedTasks();
            } catch (Exception e) {
                System.err.println("Error in pool monitoring: " + e.getMessage());
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    private void updateStatistics() {
        statistics.activeThreads = executor.getActiveCount();
        statistics.poolSize = executor.getPoolSize();
        statistics.queueSize = executor.getQueue().size();
        statistics.completedTasks = executor.getCompletedTaskCount();
        statistics.totalTasks = executor.getTaskCount();
    }

    private void performAutoTuning() {
        int activeCount = executor.getActiveCount();
        int poolSize = executor.getPoolSize();
        int queueSize = executor.getQueue().size();

        // Increase pool size if queue is backing up
        if (queueSize > DEFAULT_QUEUE_CAPACITY * 0.7 && poolSize < maxPoolSize) {
            int newSize = Math.min(poolSize + 2, maxPoolSize);
            executor.setMaximumPoolSize(newSize);
            statistics.recordAutoTune();
        }
        // Decrease if mostly idle
        else if (activeCount < corePoolSize * 0.3 && poolSize > corePoolSize) {
            int newSize = Math.max(corePoolSize, poolSize - 1);
            executor.setCorePoolSize(newSize);
            statistics.recordAutoTune();
        }
    }

    private void cleanupCompletedTasks() {
        activeTasks.entrySet().removeIf(entry -> entry.getValue().isDone());
    }

    public enum TaskPriority {
        LOW(3),
        NORMAL(2),
        HIGH(1),
        CRITICAL(0);

        public final int value;

        TaskPriority(int value) {
            this.value = value;
        }
    }

    private static class PrioritizedTask<T> implements Callable<T>, Comparable<PrioritizedTask<?>> {
        private final Callable<T> task;
        private final TaskPriority priority;
        private final long submissionTime;

        PrioritizedTask(Callable<T> task, TaskPriority priority) {
            this.task = task;
            this.priority = priority;
            this.submissionTime = System.nanoTime();
        }

        @Override
        public T call() throws Exception {
            return task.call();
        }

        @Override
        public int compareTo(PrioritizedTask<?> other) {
            int priorityCompare = Integer.compare(this.priority.value, other.priority.value);
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            return Long.compare(this.submissionTime, other.submissionTime);
        }
    }

    private static class TaskComparator implements Comparator<Runnable> {
        @Override
        public int compare(Runnable r1, Runnable r2) {
            if (r1 instanceof PrioritizedTask && r2 instanceof PrioritizedTask) {
                return ((PrioritizedTask<?>) r1).compareTo((PrioritizedTask<?>) r2);
            }
            return 0;
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
            t.setDaemon(false);
            return t;
        }
    }

    public interface AsyncCallback {
        void onSuccess();
        void onFailure(Throwable error);
    }

    public static class ThreadPoolStatus {
        public final int activeThreads;
        public final int poolSize;
        public final int corePoolSize;
        public final int maxPoolSize;
        public final int queueSize;
        public final long completedTasks;
        public final long totalTasks;
        public final int activeManagedTasks;

        public ThreadPoolStatus(int activeThreads, int poolSize, int corePoolSize, int maxPoolSize,
                               int queueSize, long completedTasks, long totalTasks, int activeManagedTasks) {
            this.activeThreads = activeThreads;
            this.poolSize = poolSize;
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.queueSize = queueSize;
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
            this.activeManagedTasks = activeManagedTasks;
        }

        public double getUtilization() {
            return poolSize > 0 ? (double) activeThreads / poolSize : 0.0;
        }

        @Override
        public String toString() {
            return String.format("ThreadPoolStatus[Active: %d/%d, Queue: %d, Completed: %d/%d, Utilization: %.1f%%]",
                activeThreads, poolSize, queueSize, completedTasks, totalTasks, getUtilization() * 100);
        }
    }

    public static class ThreadPoolStatistics {
        private final AtomicLong tasksSubmitted = new AtomicLong(0);
        private final AtomicLong tasksCompleted = new AtomicLong(0);
        private final AtomicLong tasksFailed = new AtomicLong(0);
        private final AtomicLong tasksTimedOut = new AtomicLong(0);
        private final AtomicLong poolSizeAdjustments = new AtomicLong(0);
        private final AtomicLong autoTunes = new AtomicLong(0);

        private volatile int activeThreads = 0;
        private volatile int poolSize = 0;
        private volatile int queueSize = 0;
        private volatile long completedTasks = 0;
        private volatile long totalTasks = 0;

        void recordTaskSubmitted() { tasksSubmitted.incrementAndGet(); }
        void recordTaskCompleted() { tasksCompleted.incrementAndGet(); }
        void recordTaskFailed() { tasksFailed.incrementAndGet(); }
        void recordTaskTimeout() { tasksTimedOut.incrementAndGet(); }
        void recordPoolSizeAdjustment() { poolSizeAdjustments.incrementAndGet(); }
        void recordAutoTune() { autoTunes.incrementAndGet(); }

        public long getTasksSubmitted() { return tasksSubmitted.get(); }
        public long getTasksCompleted() { return tasksCompleted.get(); }
        public long getTasksFailed() { return tasksFailed.get(); }
        public long getTasksTimedOut() { return tasksTimedOut.get(); }
        public int getActiveThreads() { return activeThreads; }
        public int getPoolSize() { return poolSize; }
        public int getQueueSize() { return queueSize; }

        public double getSuccessRate() {
            long total = tasksCompleted.get() + tasksFailed.get();
            return total > 0 ? (double) tasksCompleted.get() / total : 0.0;
        }

        public ThreadPoolStatistics copy() {
            ThreadPoolStatistics copy = new ThreadPoolStatistics();
            copy.tasksSubmitted.set(this.tasksSubmitted.get());
            copy.tasksCompleted.set(this.tasksCompleted.get());
            copy.tasksFailed.set(this.tasksFailed.get());
            copy.tasksTimedOut.set(this.tasksTimedOut.get());
            copy.poolSizeAdjustments.set(this.poolSizeAdjustments.get());
            copy.autoTunes.set(this.autoTunes.get());
            copy.activeThreads = this.activeThreads;
            copy.poolSize = this.poolSize;
            copy.queueSize = this.queueSize;
            copy.completedTasks = this.completedTasks;
            copy.totalTasks = this.totalTasks;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("ThreadPoolStatistics[Submitted: %d, Completed: %d, Failed: %d, Success Rate: %.1f%%]",
                tasksSubmitted.get(), tasksCompleted.get(), tasksFailed.get(), getSuccessRate() * 100);
        }
    }
}
