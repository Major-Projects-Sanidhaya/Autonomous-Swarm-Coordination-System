package com.team6.swarm.intelligence.emergence;

/**
 * PERFORMANCESNAPSHOT - Historical performance record
 */
public class PerformanceSnapshot {
    private final PerformanceMetrics metrics;
    private final long timestamp;

    public PerformanceSnapshot(PerformanceMetrics metrics, long timestamp) {
        this.metrics = metrics != null ? metrics.copy() : new PerformanceMetrics();
        this.timestamp = timestamp;
    }

    public PerformanceMetrics getMetrics() { return metrics.copy(); }
    public long getTimestamp() { return timestamp; }
}