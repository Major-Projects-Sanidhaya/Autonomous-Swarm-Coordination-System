package com.team6.swarm.intelligence.emergence;

/**
 * PERFORMANCESNAPSHOT - Historical performance record
 */
public class PerformanceSnapshot {
    public PerformanceMetrics metrics;
    public long timestamp;
    
    public PerformanceSnapshot(PerformanceMetrics metrics, long timestamp) {
        this.metrics = metrics;
        this.timestamp = timestamp;
    }
}