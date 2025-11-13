package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Week 8 Implementation: MetricsCollector
 *
 * Provides comprehensive time-series metrics collection and analysis
 * for monitoring system performance and agent behavior.
 *
 * Key Features:
 * - Time-series data collection
 * - Real-time metric recording
 * - Statistical analysis (mean, min, max, percentiles)
 * - Metric aggregation and windowing
 * - Custom metric types
 * - Performance counters
 *
 * @author Team 6
 * @version Week 8
 */
public class MetricsCollector {

    private static final int DEFAULT_WINDOW_SIZE = 1000;
    private static final int MAX_DATA_POINTS = 10000;

    private final Map<String, MetricTimeSeries> metrics;
    private final Map<String, Counter> counters;
    private final CollectorMetrics collectorMetrics;

    private int windowSize;

    public MetricsCollector() {
        this(DEFAULT_WINDOW_SIZE);
    }

    public MetricsCollector(int windowSize) {
        this.metrics = new ConcurrentHashMap<>();
        this.counters = new ConcurrentHashMap<>();
        this.collectorMetrics = new CollectorMetrics();
        this.windowSize = windowSize;
    }

    /**
     * Records a numeric metric value
     */
    public void recordMetric(String metricName, double value) {
        MetricTimeSeries timeSeries = metrics.computeIfAbsent(
            metricName, k -> new MetricTimeSeries(metricName, windowSize));

        timeSeries.addDataPoint(value, System.currentTimeMillis());
        collectorMetrics.recordMetric();
    }

    /**
     * Records a metric with a custom timestamp
     */
    public void recordMetric(String metricName, double value, long timestamp) {
        MetricTimeSeries timeSeries = metrics.computeIfAbsent(
            metricName, k -> new MetricTimeSeries(metricName, windowSize));

        timeSeries.addDataPoint(value, timestamp);
        collectorMetrics.recordMetric();
    }

    /**
     * Increments a counter
     */
    public void incrementCounter(String counterName) {
        incrementCounter(counterName, 1);
    }

    /**
     * Increments a counter by a specific amount
     */
    public void incrementCounter(String counterName, long amount) {
        Counter counter = counters.computeIfAbsent(
            counterName, k -> new Counter(counterName));
        counter.increment(amount);
        collectorMetrics.recordCounter();
    }

    /**
     * Gets the current value of a counter
     */
    public long getCounterValue(String counterName) {
        Counter counter = counters.get(counterName);
        return counter != null ? counter.getValue() : 0;
    }

    /**
     * Resets a counter to zero
     */
    public void resetCounter(String counterName) {
        Counter counter = counters.get(counterName);
        if (counter != null) {
            counter.reset();
        }
    }

    /**
     * Gets statistics for a specific metric
     */
    public MetricStatistics getStatistics(String metricName) {
        MetricTimeSeries timeSeries = metrics.get(metricName);
        if (timeSeries == null) {
            return null;
        }

        return timeSeries.getStatistics();
    }

    /**
     * Gets the latest value of a metric
     */
    public Double getLatestValue(String metricName) {
        MetricTimeSeries timeSeries = metrics.get(metricName);
        if (timeSeries == null || timeSeries.dataPoints.isEmpty()) {
            return null;
        }

        synchronized (timeSeries.dataPoints) {
            return timeSeries.dataPoints.get(timeSeries.dataPoints.size() - 1).value;
        }
    }

    /**
     * Gets all data points for a metric within a time range
     */
    public List<DataPoint> getDataPoints(String metricName, long startTime, long endTime) {
        MetricTimeSeries timeSeries = metrics.get(metricName);
        if (timeSeries == null) {
            return Collections.emptyList();
        }

        synchronized (timeSeries.dataPoints) {
            return timeSeries.dataPoints.stream()
                .filter(dp -> dp.timestamp >= startTime && dp.timestamp <= endTime)
                .collect(Collectors.toList());
        }
    }

    /**
     * Gets recent data points for a metric
     */
    public List<DataPoint> getRecentDataPoints(String metricName, int count) {
        MetricTimeSeries timeSeries = metrics.get(metricName);
        if (timeSeries == null) {
            return Collections.emptyList();
        }

        synchronized (timeSeries.dataPoints) {
            int size = timeSeries.dataPoints.size();
            int start = Math.max(0, size - count);
            return new ArrayList<>(timeSeries.dataPoints.subList(start, size));
        }
    }

    /**
     * Gets all registered metric names
     */
    public Set<String> getMetricNames() {
        return new HashSet<>(metrics.keySet());
    }

    /**
     * Gets all registered counter names
     */
    public Set<String> getCounterNames() {
        return new HashSet<>(counters.keySet());
    }

    /**
     * Clears all data for a specific metric
     */
    public void clearMetric(String metricName) {
        metrics.remove(metricName);
    }

    /**
     * Clears all metrics and counters
     */
    public void clearAll() {
        metrics.clear();
        counters.clear();
    }

    /**
     * Gets collector metrics
     */
    public CollectorMetrics getCollectorMetrics() {
        collectorMetrics.activeMetrics = metrics.size();
        collectorMetrics.activeCounters = counters.size();

        long totalDataPoints = 0;
        for (MetricTimeSeries ts : metrics.values()) {
            totalDataPoints += ts.dataPoints.size();
        }
        collectorMetrics.totalDataPoints = totalDataPoints;

        return collectorMetrics.copy();
    }

    /**
     * Calculates the rate of change for a metric (per second)
     */
    public double getMetricRate(String metricName, int sampleCount) {
        List<DataPoint> dataPoints = getRecentDataPoints(metricName, sampleCount);
        if (dataPoints.size() < 2) {
            return 0.0;
        }

        DataPoint first = dataPoints.get(0);
        DataPoint last = dataPoints.get(dataPoints.size() - 1);

        double valueDelta = last.value - first.value;
        double timeDelta = (last.timestamp - first.timestamp) / 1000.0; // Convert to seconds

        return timeDelta > 0 ? valueDelta / timeDelta : 0.0;
    }

    /**
     * Calculates a moving average for a metric
     */
    public double getMovingAverage(String metricName, int windowSize) {
        List<DataPoint> dataPoints = getRecentDataPoints(metricName, windowSize);
        if (dataPoints.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (DataPoint dp : dataPoints) {
            sum += dp.value;
        }

        return sum / dataPoints.size();
    }

    private static class MetricTimeSeries {
        final String name;
        final List<DataPoint> dataPoints;
        final int maxSize;

        MetricTimeSeries(String name, int maxSize) {
            this.name = name;
            this.dataPoints = Collections.synchronizedList(new ArrayList<>());
            this.maxSize = maxSize;
        }

        void addDataPoint(double value, long timestamp) {
            synchronized (dataPoints) {
                dataPoints.add(new DataPoint(value, timestamp));

                // Remove oldest if exceeding limit
                while (dataPoints.size() > maxSize) {
                    dataPoints.remove(0);
                }
            }
        }

        MetricStatistics getStatistics() {
            synchronized (dataPoints) {
                if (dataPoints.isEmpty()) {
                    return new MetricStatistics(name, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                }

                List<Double> values = dataPoints.stream()
                    .map(dp -> dp.value)
                    .sorted()
                    .collect(Collectors.toList());

                int count = values.size();
                double sum = values.stream().mapToDouble(Double::doubleValue).sum();
                double mean = sum / count;
                double min = values.get(0);
                double max = values.get(count - 1);

                double p50 = values.get(count / 2);
                double p95 = values.get((int) (count * 0.95));
                double p99 = values.get((int) (count * 0.99));

                return new MetricStatistics(name, count, mean, min, max, p50, p95, p99);
            }
        }
    }

    private static class Counter {
        final String name;
        long value;
        final long createdTime;

        Counter(String name) {
            this.name = name;
            this.value = 0;
            this.createdTime = System.currentTimeMillis();
        }

        synchronized void increment(long amount) {
            value += amount;
        }

        synchronized long getValue() {
            return value;
        }

        synchronized void reset() {
            value = 0;
        }
    }

    public static class DataPoint {
        public final double value;
        public final long timestamp;

        public DataPoint(double value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("DataPoint[value=%.2f, time=%d]", value, timestamp);
        }
    }

    public static class MetricStatistics {
        public final String metricName;
        public final int count;
        public final double mean;
        public final double min;
        public final double max;
        public final double p50;
        public final double p95;
        public final double p99;

        public MetricStatistics(String metricName, int count, double mean,
                               double min, double max, double p50, double p95, double p99) {
            this.metricName = metricName;
            this.count = count;
            this.mean = mean;
            this.min = min;
            this.max = max;
            this.p50 = p50;
            this.p95 = p95;
            this.p99 = p99;
        }

        @Override
        public String toString() {
            return String.format("Statistics[%s: count=%d, mean=%.2f, min=%.2f, max=%.2f, p50=%.2f, p95=%.2f, p99=%.2f]",
                metricName, count, mean, min, max, p50, p95, p99);
        }
    }

    public static class CollectorMetrics {
        private long metricsRecorded = 0;
        private long countersIncremented = 0;
        private int activeMetrics = 0;
        private int activeCounters = 0;
        private long totalDataPoints = 0;

        void recordMetric() { metricsRecorded++; }
        void recordCounter() { countersIncremented++; }

        public long getMetricsRecorded() { return metricsRecorded; }
        public long getCountersIncremented() { return countersIncremented; }
        public int getActiveMetrics() { return activeMetrics; }
        public int getActiveCounters() { return activeCounters; }
        public long getTotalDataPoints() { return totalDataPoints; }

        public CollectorMetrics copy() {
            CollectorMetrics copy = new CollectorMetrics();
            copy.metricsRecorded = this.metricsRecorded;
            copy.countersIncremented = this.countersIncremented;
            copy.activeMetrics = this.activeMetrics;
            copy.activeCounters = this.activeCounters;
            copy.totalDataPoints = this.totalDataPoints;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("CollectorMetrics[Metrics: %d (%d recorded), Counters: %d (%d incremented), Data Points: %d]",
                activeMetrics, metricsRecorded, activeCounters, countersIncremented, totalDataPoints);
        }
    }
}
