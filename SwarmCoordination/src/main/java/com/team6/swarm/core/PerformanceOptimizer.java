package com.team6.swarm.core;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Week 7 Implementation: PerformanceOptimizer
 *
 * Advanced performance optimization system that monitors resource usage,
 * identifies bottlenecks, and provides intelligent optimization recommendations.
 *
 * Key Features:
 * - Real-time CPU and memory usage tracking
 * - Performance bottleneck detection
 * - Automatic system parameter tuning
 * - Optimization recommendations
 * - Historical performance analysis
 *
 * @author Team 6
 * @version Week 7
 */
public class PerformanceOptimizer {

    private static final double CPU_THRESHOLD_HIGH = 0.80;
    private static final double MEMORY_THRESHOLD_HIGH = 0.75;
    private static final double CPU_THRESHOLD_LOW = 0.30;
    private static final int OPTIMIZATION_WINDOW_SIZE = 100;

    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;
    private final Map<String, PerformanceMetric> metrics;
    private final List<OptimizationRecommendation> recommendations;
    private final Map<String, Object> systemParameters;
    private final LinkedList<PerformanceSnapshot> performanceHistory;

    private long lastCpuTime;
    private long lastSystemTime;
    private boolean autoTuningEnabled;
    private AtomicLong optimizationCount;

    public PerformanceOptimizer() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.metrics = new ConcurrentHashMap<>();
        this.recommendations = Collections.synchronizedList(new ArrayList<>());
        this.systemParameters = new ConcurrentHashMap<>();
        this.performanceHistory = new LinkedList<>();
        this.lastCpuTime = 0;
        this.lastSystemTime = System.nanoTime();
        this.autoTuningEnabled = true;
        this.optimizationCount = new AtomicLong(0);
        initializeDefaultParameters();
    }

    private void initializeDefaultParameters() {
        systemParameters.put("maxAgents", 1000);
        systemParameters.put("updateInterval", 100);
        systemParameters.put("threadPoolSize", Runtime.getRuntime().availableProcessors());
        systemParameters.put("cacheSize", 5000);
        systemParameters.put("communicationRange", 50.0);
    }

    public OptimizationResult optimize() {
        long startTime = System.currentTimeMillis();
        PerformanceSnapshot snapshot = capturePerformanceSnapshot();
        performanceHistory.add(snapshot);

        while (performanceHistory.size() > OPTIMIZATION_WINDOW_SIZE) {
            performanceHistory.removeFirst();
        }

        List<Bottleneck> bottlenecks = analyzeBottlenecks();
        List<OptimizationRecommendation> newRecommendations = generateRecommendations(snapshot, bottlenecks);
        recommendations.clear();
        recommendations.addAll(newRecommendations);

        Map<String, Object> appliedOptimizations = new HashMap<>();
        if (autoTuningEnabled) {
            appliedOptimizations = applyAutoTuning(snapshot, bottlenecks);
        }

        long duration = System.currentTimeMillis() - startTime;
        optimizationCount.incrementAndGet();

        return new OptimizationResult(snapshot, bottlenecks, newRecommendations, appliedOptimizations, duration);
    }

    public List<Bottleneck> analyzeBottlenecks() {
        List<Bottleneck> bottlenecks = new ArrayList<>();
        double cpuUsage = getCpuUsage();

        if (cpuUsage > CPU_THRESHOLD_HIGH) {
            bottlenecks.add(new Bottleneck(BottleneckType.CPU, Severity.HIGH,
                "CPU usage is at " + String.format("%.2f%%", cpuUsage * 100),
                "Consider reducing agent count or optimizing update frequency"));
        }

        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double memoryUsageRatio = (double) heapUsage.getUsed() / heapUsage.getMax();
        if (memoryUsageRatio > MEMORY_THRESHOLD_HIGH) {
            bottlenecks.add(new Bottleneck(BottleneckType.MEMORY, Severity.HIGH,
                "Memory usage is at " + String.format("%.2f%%", memoryUsageRatio * 100),
                "Enable garbage collection or reduce cache size"));
        }

        int threadCount = threadBean.getThreadCount();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (threadCount > availableProcessors * 3) {
            bottlenecks.add(new Bottleneck(BottleneckType.THREAD_CONTENTION, Severity.MEDIUM,
                "Thread count (" + threadCount + ") exceeds optimal range",
                "Reduce thread pool size or optimize task scheduling"));
        }

        if (performanceHistory.size() >= 10) {
            double avgRecentPerformance = calculateAveragePerformance(5);
            double avgHistoricalPerformance = calculateAveragePerformance(performanceHistory.size());
            if (avgRecentPerformance < avgHistoricalPerformance * 0.8) {
                bottlenecks.add(new Bottleneck(BottleneckType.DEGRADATION, Severity.MEDIUM,
                    "Performance has degraded by 20% or more",
                    "Review recent changes and consider system restart"));
            }
        }

        return bottlenecks;
    }

    public OptimizationReport getOptimizationReport() {
        PerformanceSnapshot currentSnapshot = capturePerformanceSnapshot();
        List<Bottleneck> currentBottlenecks = analyzeBottlenecks();
        Map<String, Double> trends = calculatePerformanceTrends();
        double healthScore = calculateHealthScore(currentSnapshot, currentBottlenecks);

        return new OptimizationReport(currentSnapshot, currentBottlenecks, recommendations,
            trends, healthScore, systemParameters, optimizationCount.get());
    }

    private PerformanceSnapshot capturePerformanceSnapshot() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return new PerformanceSnapshot(System.currentTimeMillis(), getCpuUsage(),
            (double) heapUsage.getUsed() / heapUsage.getMax(), heapUsage.getUsed(), heapUsage.getMax(),
            threadBean.getThreadCount(), threadBean.getDaemonThreadCount());
    }

    private double getCpuUsage() {
        long currentCpuTime = 0;
        for (long threadId : threadBean.getAllThreadIds()) {
            long cpuTime = threadBean.getThreadCpuTime(threadId);
            if (cpuTime > 0) currentCpuTime += cpuTime;
        }

        long currentSystemTime = System.nanoTime();
        double cpuUsage = 0.0;

        if (lastCpuTime > 0 && lastSystemTime > 0) {
            long cpuDelta = currentCpuTime - lastCpuTime;
            long systemDelta = currentSystemTime - lastSystemTime;
            if (systemDelta > 0) {
                int processors = Runtime.getRuntime().availableProcessors();
                cpuUsage = Math.min(1.0, (double) cpuDelta / (systemDelta * processors));
            }
        }

        lastCpuTime = currentCpuTime;
        lastSystemTime = currentSystemTime;
        return cpuUsage;
    }

    private List<OptimizationRecommendation> generateRecommendations(PerformanceSnapshot snapshot, List<Bottleneck> bottlenecks) {
        List<OptimizationRecommendation> recs = new ArrayList<>();

        if (snapshot.cpuUsage > CPU_THRESHOLD_HIGH) {
            recs.add(new OptimizationRecommendation("Reduce update frequency",
                "Increase update interval to reduce CPU load", Priority.HIGH,
                Map.of("updateInterval", (Integer) systemParameters.get("updateInterval") * 2)));
        }

        if (snapshot.cpuUsage < CPU_THRESHOLD_LOW && performanceHistory.size() > 10) {
            recs.add(new OptimizationRecommendation("Increase agent capacity",
                "System can handle more agents - consider increasing maxAgents", Priority.LOW,
                Map.of("maxAgents", (Integer) systemParameters.get("maxAgents") + 100)));
        }

        if (snapshot.memoryUsageRatio > MEMORY_THRESHOLD_HIGH) {
            recs.add(new OptimizationRecommendation("Reduce cache size",
                "Memory usage is high - reduce cache size", Priority.HIGH,
                Map.of("cacheSize", (Integer) systemParameters.get("cacheSize") / 2)));
        }

        if (snapshot.threadCount > Runtime.getRuntime().availableProcessors() * 2) {
            recs.add(new OptimizationRecommendation("Optimize thread pool",
                "Thread count is high - reduce thread pool size", Priority.MEDIUM,
                Map.of("threadPoolSize", Runtime.getRuntime().availableProcessors())));
        }

        return recs;
    }

    private Map<String, Object> applyAutoTuning(PerformanceSnapshot snapshot, List<Bottleneck> bottlenecks) {
        Map<String, Object> applied = new HashMap<>();
        int optimalThreads = Runtime.getRuntime().availableProcessors();

        if (snapshot.cpuUsage > CPU_THRESHOLD_HIGH) {
            optimalThreads = Math.max(2, optimalThreads - 1);
        } else if (snapshot.cpuUsage < CPU_THRESHOLD_LOW) {
            optimalThreads += 2;
        }

        if (!systemParameters.get("threadPoolSize").equals(optimalThreads)) {
            systemParameters.put("threadPoolSize", optimalThreads);
            applied.put("threadPoolSize", optimalThreads);
        }

        if (snapshot.memoryUsageRatio > MEMORY_THRESHOLD_HIGH) {
            int currentCache = (Integer) systemParameters.get("cacheSize");
            int newCache = (int) (currentCache * 0.8);
            systemParameters.put("cacheSize", newCache);
            applied.put("cacheSize", newCache);
        }

        return applied;
    }

    private double calculateAveragePerformance(int windowSize) {
        if (performanceHistory.isEmpty()) return 1.0;
        int count = Math.min(windowSize, performanceHistory.size());
        double sum = 0.0;

        for (int i = performanceHistory.size() - count; i < performanceHistory.size(); i++) {
            PerformanceSnapshot snapshot = performanceHistory.get(i);
            sum += (1.0 - snapshot.cpuUsage * 0.5 - snapshot.memoryUsageRatio * 0.5);
        }

        return sum / count;
    }

    private Map<String, Double> calculatePerformanceTrends() {
        Map<String, Double> trends = new HashMap<>();
        if (performanceHistory.size() < 2) return trends;

        PerformanceSnapshot first = performanceHistory.getFirst();
        PerformanceSnapshot last = performanceHistory.getLast();

        trends.put("cpuTrend", last.cpuUsage - first.cpuUsage);
        trends.put("memoryTrend", last.memoryUsageRatio - first.memoryUsageRatio);
        trends.put("threadTrend", (double) (last.threadCount - first.threadCount));

        return trends;
    }

    private double calculateHealthScore(PerformanceSnapshot snapshot, List<Bottleneck> bottlenecks) {
        double score = 100.0;

        if (snapshot.cpuUsage > CPU_THRESHOLD_HIGH) score -= 20;
        else if (snapshot.cpuUsage > 0.6) score -= 10;

        if (snapshot.memoryUsageRatio > MEMORY_THRESHOLD_HIGH) score -= 20;
        else if (snapshot.memoryUsageRatio > 0.6) score -= 10;

        for (Bottleneck bottleneck : bottlenecks) {
            switch (bottleneck.severity) {
                case HIGH: score -= 15; break;
                case MEDIUM: score -= 10; break;
                case LOW: score -= 5; break;
            }
        }

        return Math.max(0, score);
    }

    public void setAutoTuningEnabled(boolean enabled) { this.autoTuningEnabled = enabled; }
    public Object getParameter(String key) { return systemParameters.get(key); }
    public void setParameter(String key, Object value) { systemParameters.put(key, value); }

    // Inner classes
    public static class PerformanceSnapshot {
        public final long timestamp;
        public final double cpuUsage;
        public final double memoryUsageRatio;
        public final long memoryUsed;
        public final long memoryMax;
        public final int threadCount;
        public final int daemonThreadCount;

        public PerformanceSnapshot(long timestamp, double cpuUsage, double memoryUsageRatio,
                                 long memoryUsed, long memoryMax, int threadCount, int daemonThreadCount) {
            this.timestamp = timestamp;
            this.cpuUsage = cpuUsage;
            this.memoryUsageRatio = memoryUsageRatio;
            this.memoryUsed = memoryUsed;
            this.memoryMax = memoryMax;
            this.threadCount = threadCount;
            this.daemonThreadCount = daemonThreadCount;
        }

        @Override
        public String toString() {
            return String.format("PerformanceSnapshot[CPU: %.2f%%, Memory: %.2f%%, Threads: %d]",
                cpuUsage * 100, memoryUsageRatio * 100, threadCount);
        }
    }

    public static class Bottleneck {
        public final BottleneckType type;
        public final Severity severity;
        public final String description;
        public final String recommendation;

        public Bottleneck(BottleneckType type, Severity severity, String description, String recommendation) {
            this.type = type;
            this.severity = severity;
            this.description = description;
            this.recommendation = recommendation;
        }

        @Override
        public String toString() {
            return String.format("[%s - %s] %s", severity, type, description);
        }
    }

    public enum BottleneckType { CPU, MEMORY, THREAD_CONTENTION, DEGRADATION, NETWORK, DISK_IO }
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    public static class OptimizationRecommendation {
        public final String title;
        public final String description;
        public final Priority priority;
        public final Map<String, Object> suggestedParameters;

        public OptimizationRecommendation(String title, String description, Priority priority,
                                        Map<String, Object> suggestedParameters) {
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.suggestedParameters = suggestedParameters;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", priority, title, description);
        }
    }

    public static class OptimizationResult {
        public final PerformanceSnapshot snapshot;
        public final List<Bottleneck> bottlenecks;
        public final List<OptimizationRecommendation> recommendations;
        public final Map<String, Object> appliedOptimizations;
        public final long durationMs;

        public OptimizationResult(PerformanceSnapshot snapshot, List<Bottleneck> bottlenecks,
                                List<OptimizationRecommendation> recommendations,
                                Map<String, Object> appliedOptimizations, long durationMs) {
            this.snapshot = snapshot;
            this.bottlenecks = bottlenecks;
            this.recommendations = recommendations;
            this.appliedOptimizations = appliedOptimizations;
            this.durationMs = durationMs;
        }

        @Override
        public String toString() {
            return String.format("OptimizationResult[Bottlenecks: %d, Recommendations: %d, Applied: %d]",
                bottlenecks.size(), recommendations.size(), appliedOptimizations.size());
        }
    }

    public static class OptimizationReport {
        public final PerformanceSnapshot currentSnapshot;
        public final List<Bottleneck> bottlenecks;
        public final List<OptimizationRecommendation> recommendations;
        public final Map<String, Double> trends;
        public final double healthScore;
        public final Map<String, Object> systemParameters;
        public final long totalOptimizations;

        public OptimizationReport(PerformanceSnapshot currentSnapshot, List<Bottleneck> bottlenecks,
                                List<OptimizationRecommendation> recommendations, Map<String, Double> trends,
                                double healthScore, Map<String, Object> systemParameters, long totalOptimizations) {
            this.currentSnapshot = currentSnapshot;
            this.bottlenecks = bottlenecks;
            this.recommendations = recommendations;
            this.trends = trends;
            this.healthScore = healthScore;
            this.systemParameters = new HashMap<>(systemParameters);
            this.totalOptimizations = totalOptimizations;
        }

        @Override
        public String toString() {
            return String.format("OptimizationReport[Health: %.1f/100, Bottlenecks: %d, Recommendations: %d]",
                healthScore, bottlenecks.size(), recommendations.size());
        }
    }

    private static class PerformanceMetric {
        private final String name;
        private final List<Double> values;
        private final int maxSize;

        public PerformanceMetric(String name, int maxSize) {
            this.name = name;
            this.values = new ArrayList<>();
            this.maxSize = maxSize;
        }

        public void addValue(double value) {
            values.add(value);
            while (values.size() > maxSize) values.remove(0);
        }

        public double getAverage() {
            return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        public double getMax() {
            return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        }

        public double getMin() {
            return values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        }
    }
}
