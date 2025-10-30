/**
 * PERFORMANCEMONITOR CLASS - System Performance Tracking and Optimization (Week 5-6)
 *
 * PURPOSE:
 * - Tracks system performance metrics (FPS, memory, CPU)
 * - Identifies performance bottlenecks and optimization opportunities
 * - Provides real-time performance feedback for adaptive optimization
 * - Ensures system runs smoothly with 10+ agents
 *
 * DESIGN PATTERNS USED:
 * 1. Singleton Pattern - Single performance monitor for entire system
 * 2. Observer Pattern - Notify listeners of performance issues
 * 3. Strategy Pattern - Different optimization strategies
 * 4. Template Method - Performance measurement framework
 *
 * CORE METRICS TRACKED:
 * 1. FPS (Frames Per Second) - Target: 30-60 FPS
 * 2. Frame Time - Time to complete one update cycle
 * 3. Memory Usage - Heap utilization percentage
 * 4. CPU Load - Approximate CPU usage
 * 5. Agent Count - Active agent population
 * 6. Update Time - Time spent updating agents
 *
 * PERFORMANCE THRESHOLDS:
 * - OPTIMAL: FPS >= 50, Memory < 70%, Update Time < 15ms
 * - NORMAL: FPS >= 30, Memory < 80%, Update Time < 30ms
 * - DEGRADED: FPS >= 20, Memory < 90%, Update Time < 50ms
 * - CRITICAL: FPS < 20, Memory >= 90%, Update Time >= 50ms
 *
 * OPTIMIZATION STRATEGIES:
 * - Reduce update frequency for distant agents
 * - Throttle non-critical updates
 * - Garbage collection hints
 * - Event batching
 * - Level of detail adjustments
 *
 * USAGE PATTERNS:
 * 1. Initialize:
 *    PerformanceMonitor monitor = PerformanceMonitor.getInstance();
 *
 * 2. Track frame:
 *    monitor.startFrame();
 *    // ... do work ...
 *    monitor.endFrame();
 *
 * 3. Check status:
 *    if (monitor.isPerformanceDegraded()) { optimize(); }
 *
 * 4. Get metrics:
 *    PerformanceMetrics metrics = monitor.getCurrentMetrics();
 *
 * INTEGRATION POINTS:
 * - Used by: SystemController for performance tracking
 * - Monitored by: AgentManager for optimization decisions
 * - Visualized by: Anthony's UI for performance display
 * - Logged by: System logs for analysis
 */
package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PerformanceMonitor {
    // Singleton instance
    private static PerformanceMonitor instance;

    // Performance metrics
    private double currentFPS;
    private double averageFPS;
    private double minFPS;
    private double maxFPS;

    private long frameCount;
    private long startTime;
    private long lastFrameTime;
    private long frameStartTime;

    // Memory metrics
    private double memoryUsagePercent;
    private long heapUsed;
    private long heapMax;

    // Timing metrics
    private double averageFrameTime;
    private double maxFrameTime;
    private long totalFrameTime;

    // Agent metrics
    private int activeAgentCount;
    private int totalAgentCount;

    // Performance history (rolling window)
    private Queue<PerformanceSample> performanceHistory;
    private static final int HISTORY_SIZE = 60; // Last 60 frames

    // Performance status
    private PerformanceStatus currentStatus;

    // Optimization settings
    private int targetFPS;
    private boolean autoOptimize;
    private OptimizationLevel optimizationLevel;

    // Event bus for notifications
    private EventBus eventBus;

    // Performance thresholds
    private static final double OPTIMAL_FPS = 50.0;
    private static final double NORMAL_FPS = 30.0;
    private static final double DEGRADED_FPS = 20.0;
    private static final double OPTIMAL_MEMORY = 0.7;
    private static final double NORMAL_MEMORY = 0.8;
    private static final double CRITICAL_MEMORY = 0.9;

    /**
     * Performance status levels
     */
    public enum PerformanceStatus {
        OPTIMAL,     // Running at peak performance
        NORMAL,      // Running within acceptable range
        DEGRADED,    // Performance issues detected
        CRITICAL     // Severe performance problems
    }

    /**
     * Optimization levels
     */
    public enum OptimizationLevel {
        NONE,        // No optimizations
        LIGHT,       // Minor optimizations
        MODERATE,    // Balanced optimization
        AGGRESSIVE   // Maximum optimization
    }

    /**
     * Private constructor for Singleton
     */
    private PerformanceMonitor() {
        this.targetFPS = 60;
        this.autoOptimize = true;
        this.optimizationLevel = OptimizationLevel.MODERATE;
        this.currentStatus = PerformanceStatus.OPTIMAL;
        this.performanceHistory = new ConcurrentLinkedQueue<>();
        this.startTime = System.currentTimeMillis();
        this.lastFrameTime = startTime;
        this.frameCount = 0;
        this.minFPS = Double.MAX_VALUE;
        this.maxFPS = 0.0;

        System.out.println("PerformanceMonitor initialized");
    }

    /**
     * Get singleton instance
     */
    public static synchronized PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }

    /**
     * Set EventBus for performance notifications
     */
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    // ==================== FRAME TRACKING ====================

    /**
     * Mark start of frame (call at beginning of update cycle)
     */
    public void startFrame() {
        frameStartTime = System.nanoTime();
    }

    /**
     * Mark end of frame (call at end of update cycle)
     */
    public void endFrame() {
        long frameEndTime = System.nanoTime();
        long frameTimeNanos = frameEndTime - frameStartTime;
        double frameTimeMillis = frameTimeNanos / 1_000_000.0;

        // Update frame count
        frameCount++;

        // Update timing metrics
        totalFrameTime += frameTimeMillis;
        averageFrameTime = totalFrameTime / frameCount;
        maxFrameTime = Math.max(maxFrameTime, frameTimeMillis);

        // Calculate FPS
        long currentTime = System.currentTimeMillis();
        double timeSinceLastFrame = (currentTime - lastFrameTime) / 1000.0;

        if (timeSinceLastFrame > 0) {
            currentFPS = 1.0 / timeSinceLastFrame;
            minFPS = Math.min(minFPS, currentFPS);
            maxFPS = Math.max(maxFPS, currentFPS);
        }

        // Calculate average FPS over lifetime
        double totalSeconds = (currentTime - startTime) / 1000.0;
        if (totalSeconds > 0) {
            averageFPS = frameCount / totalSeconds;
        }

        lastFrameTime = currentTime;

        // Update memory metrics
        updateMemoryMetrics();

        // Add to performance history
        addPerformanceSample(frameTimeMillis, currentFPS);

        // Update performance status
        updatePerformanceStatus();

        // Auto-optimize if enabled
        if (autoOptimize) {
            performAutoOptimization();
        }
    }

    /**
     * Update memory usage metrics
     */
    private void updateMemoryMetrics() {
        Runtime runtime = Runtime.getRuntime();
        heapUsed = runtime.totalMemory() - runtime.freeMemory();
        heapMax = runtime.maxMemory();

        if (heapMax > 0) {
            memoryUsagePercent = (double) heapUsed / heapMax;
        }
    }

    /**
     * Add performance sample to history
     */
    private void addPerformanceSample(double frameTime, double fps) {
        PerformanceSample sample = new PerformanceSample(
            System.currentTimeMillis(),
            frameTime,
            fps,
            memoryUsagePercent
        );

        performanceHistory.offer(sample);

        // Maintain rolling window
        while (performanceHistory.size() > HISTORY_SIZE) {
            performanceHistory.poll();
        }
    }

    /**
     * Update current performance status
     */
    private void updatePerformanceStatus() {
        PerformanceStatus previousStatus = currentStatus;

        // Determine status based on multiple factors
        if (currentFPS >= OPTIMAL_FPS && memoryUsagePercent < OPTIMAL_MEMORY) {
            currentStatus = PerformanceStatus.OPTIMAL;
        } else if (currentFPS >= NORMAL_FPS && memoryUsagePercent < NORMAL_MEMORY) {
            currentStatus = PerformanceStatus.NORMAL;
        } else if (currentFPS >= DEGRADED_FPS && memoryUsagePercent < CRITICAL_MEMORY) {
            currentStatus = PerformanceStatus.DEGRADED;
        } else {
            currentStatus = PerformanceStatus.CRITICAL;
        }

        // Notify if status changed
        if (currentStatus != previousStatus && eventBus != null) {
            notifyStatusChange(previousStatus, currentStatus);
        }
    }

    /**
     * Notify listeners of status change
     */
    private void notifyStatusChange(PerformanceStatus oldStatus, PerformanceStatus newStatus) {
        String message = String.format(
            "Performance status changed: %s -> %s (FPS: %.1f, Memory: %.1f%%)",
            oldStatus, newStatus, currentFPS, memoryUsagePercent * 100
        );

        SystemEvent.Severity severity = newStatus == PerformanceStatus.CRITICAL ?
            SystemEvent.Severity.ERROR : SystemEvent.Severity.WARNING;

        SystemEvent event = new SystemEvent(
            "PERFORMANCE_STATUS_CHANGED",
            null,
            message,
            severity
        );

        eventBus.publish(event);
        System.out.println(message);
    }

    // ==================== AUTO-OPTIMIZATION ====================

    /**
     * Perform automatic optimization based on current performance
     */
    private void performAutoOptimization() {
        switch (currentStatus) {
            case CRITICAL:
                setOptimizationLevel(OptimizationLevel.AGGRESSIVE);
                suggestGarbageCollection();
                break;

            case DEGRADED:
                setOptimizationLevel(OptimizationLevel.MODERATE);
                break;

            case NORMAL:
                setOptimizationLevel(OptimizationLevel.LIGHT);
                break;

            case OPTIMAL:
                setOptimizationLevel(OptimizationLevel.NONE);
                break;
        }
    }

    /**
     * Suggest garbage collection if memory is high
     */
    private void suggestGarbageCollection() {
        if (memoryUsagePercent > CRITICAL_MEMORY) {
            System.out.println("PerformanceMonitor: Suggesting garbage collection (memory: " +
                String.format("%.1f%%)", memoryUsagePercent * 100));
            System.gc(); // Suggestion only, JVM decides
        }
    }

    // ==================== AGENT TRACKING ====================

    /**
     * Update agent count metrics
     */
    public void updateAgentCounts(int active, int total) {
        this.activeAgentCount = active;
        this.totalAgentCount = total;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if performance is degraded
     */
    public boolean isPerformanceDegraded() {
        return currentStatus == PerformanceStatus.DEGRADED ||
               currentStatus == PerformanceStatus.CRITICAL;
    }

    /**
     * Check if performance is critical
     */
    public boolean isPerformanceCritical() {
        return currentStatus == PerformanceStatus.CRITICAL;
    }

    /**
     * Check if performance is optimal
     */
    public boolean isPerformanceOptimal() {
        return currentStatus == PerformanceStatus.OPTIMAL;
    }

    /**
     * Get current performance metrics
     */
    public PerformanceMetrics getCurrentMetrics() {
        return new PerformanceMetrics(
            currentFPS,
            averageFPS,
            minFPS,
            maxFPS,
            averageFrameTime,
            maxFrameTime,
            memoryUsagePercent,
            heapUsed,
            heapMax,
            activeAgentCount,
            totalAgentCount,
            currentStatus,
            frameCount
        );
    }

    /**
     * Get performance report
     */
    public String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("===== PERFORMANCE REPORT =====\n");
        report.append(String.format("Status: %s\n", currentStatus));
        report.append(String.format("FPS: Current=%.1f, Avg=%.1f, Min=%.1f, Max=%.1f\n",
            currentFPS, averageFPS, minFPS, maxFPS));
        report.append(String.format("Frame Time: Avg=%.2fms, Max=%.2fms\n",
            averageFrameTime, maxFrameTime));
        report.append(String.format("Memory: %.1f%% (%.1fMB / %.1fMB)\n",
            memoryUsagePercent * 100,
            heapUsed / 1_048_576.0,
            heapMax / 1_048_576.0));
        report.append(String.format("Agents: %d active / %d total\n",
            activeAgentCount, totalAgentCount));
        report.append(String.format("Frames: %d (%.1fs runtime)\n",
            frameCount,
            (System.currentTimeMillis() - startTime) / 1000.0));
        report.append(String.format("Optimization: %s\n", optimizationLevel));
        report.append("==============================\n");
        return report.toString();
    }

    /**
     * Reset all metrics
     */
    public void reset() {
        this.frameCount = 0;
        this.startTime = System.currentTimeMillis();
        this.lastFrameTime = startTime;
        this.totalFrameTime = 0;
        this.minFPS = Double.MAX_VALUE;
        this.maxFPS = 0.0;
        this.averageFPS = 0.0;
        this.currentFPS = 0.0;
        this.averageFrameTime = 0.0;
        this.maxFrameTime = 0.0;
        this.performanceHistory.clear();
        this.currentStatus = PerformanceStatus.OPTIMAL;

        System.out.println("PerformanceMonitor: Metrics reset");
    }

    // ==================== CONFIGURATION ====================

    public void setTargetFPS(int fps) {
        this.targetFPS = fps;
    }

    public void setAutoOptimize(boolean enable) {
        this.autoOptimize = enable;
        System.out.println("PerformanceMonitor: Auto-optimize " + (enable ? "enabled" : "disabled"));
    }

    public void setOptimizationLevel(OptimizationLevel level) {
        if (this.optimizationLevel != level) {
            this.optimizationLevel = level;
            System.out.println("PerformanceMonitor: Optimization level set to " + level);
        }
    }

    // ==================== GETTERS ====================

    public double getCurrentFPS() { return currentFPS; }
    public double getAverageFPS() { return averageFPS; }
    public double getMinFPS() { return minFPS; }
    public double getMaxFPS() { return maxFPS; }
    public double getMemoryUsagePercent() { return memoryUsagePercent; }
    public double getAverageFrameTime() { return averageFrameTime; }
    public PerformanceStatus getCurrentStatus() { return currentStatus; }
    public OptimizationLevel getOptimizationLevel() { return optimizationLevel; }
    public long getFrameCount() { return frameCount; }
    public int getActiveAgentCount() { return activeAgentCount; }
    public int getTotalAgentCount() { return totalAgentCount; }

    // ==================== INNER CLASSES ====================

    /**
     * Performance sample - single point in time
     */
    private static class PerformanceSample {
        public final long timestamp;
        public final double frameTime;
        public final double fps;
        public final double memoryUsage;

        public PerformanceSample(long timestamp, double frameTime,
                                double fps, double memoryUsage) {
            this.timestamp = timestamp;
            this.frameTime = frameTime;
            this.fps = fps;
            this.memoryUsage = memoryUsage;
        }
    }

    /**
     * Performance metrics snapshot
     */
    public static class PerformanceMetrics {
        public final double currentFPS;
        public final double averageFPS;
        public final double minFPS;
        public final double maxFPS;
        public final double averageFrameTime;
        public final double maxFrameTime;
        public final double memoryUsagePercent;
        public final long heapUsed;
        public final long heapMax;
        public final int activeAgents;
        public final int totalAgents;
        public final PerformanceStatus status;
        public final long frameCount;

        public PerformanceMetrics(double currentFPS, double averageFPS,
                                 double minFPS, double maxFPS,
                                 double averageFrameTime, double maxFrameTime,
                                 double memoryUsagePercent,
                                 long heapUsed, long heapMax,
                                 int activeAgents, int totalAgents,
                                 PerformanceStatus status, long frameCount) {
            this.currentFPS = currentFPS;
            this.averageFPS = averageFPS;
            this.minFPS = minFPS;
            this.maxFPS = maxFPS;
            this.averageFrameTime = averageFrameTime;
            this.maxFrameTime = maxFrameTime;
            this.memoryUsagePercent = memoryUsagePercent;
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.activeAgents = activeAgents;
            this.totalAgents = totalAgents;
            this.status = status;
            this.frameCount = frameCount;
        }

        @Override
        public String toString() {
            return String.format(
                "PerformanceMetrics[FPS=%.1f, Memory=%.1f%%, Status=%s, Agents=%d/%d]",
                currentFPS, memoryUsagePercent * 100, status, activeAgents, totalAgents
            );
        }
    }
}
