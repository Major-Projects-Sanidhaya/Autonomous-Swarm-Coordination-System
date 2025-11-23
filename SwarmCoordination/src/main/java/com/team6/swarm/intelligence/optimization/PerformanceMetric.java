package com.team6.swarm.intelligence.optimization;

/**
 * PERFORMANCE METRIC
 */
public class PerformanceMetric {
    String operationName;
    long executionCount;
    double totalTime;
    double minTime;
    double maxTime;
    double averageTime;
    
    public PerformanceMetric(String name) {
        this.operationName = name;
        this.executionCount = 0;
        this.totalTime = 0.0;
        this.minTime = Double.MAX_VALUE;
        this.maxTime = 0.0;
        this.averageTime = 0.0;
    }
    
    public void recordExecution(double durationMs) {
        executionCount++;
        totalTime += durationMs;
        minTime = Math.min(minTime, durationMs);
        maxTime = Math.max(maxTime, durationMs);
        averageTime = totalTime / executionCount;
    }
    
    public double getAverageTime() {
        return averageTime;
    }
    
    @Override
    public String toString() {
        return String.format("%s: avg=%.2fms min=%.2fms max=%.2fms count=%d",
            operationName, averageTime, minTime, maxTime, executionCount);
    }
}
