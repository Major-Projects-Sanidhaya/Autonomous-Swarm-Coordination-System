package com.team6.swarm.intelligence.optimization;

import java.util.HashMap;
import java.util.Map;

/**
 * PERFORMANCE REPORT
 */
public class PerformanceReport {
    long timestamp;
    Map<String, PerformanceMetric> metrics;
    
    int flockingCacheSize;
    int taskScoreCacheSize;
    int formationCacheSize;
    int spatialGridCellCount;
    int spatialGridAgentCount;
    double spatialQueryAvgTime;
    
    public PerformanceReport() {
        this.metrics = new HashMap<>();
    }
    
    public void addMetric(PerformanceMetric metric) {
        metrics.put(metric.operationName, metric);
    }
    
    public void printReport() {
        System.out.println("========================================");
        System.out.println("PERFORMANCE REPORT");
        System.out.println("========================================");
        System.out.println();
        
        System.out.println("Operation Performance:");
        for (PerformanceMetric metric : metrics.values()) {
            System.out.println("  " + metric);
        }
        
        System.out.println();
        System.out.println("Cache Statistics:");
        System.out.println("  Flocking cache: " + flockingCacheSize + " entries");
        System.out.println("  Task score cache: " + taskScoreCacheSize + " entries");
        System.out.println("  Formation cache: " + formationCacheSize + " entries");
        
        System.out.println();
        System.out.println("Spatial Grid:");
        System.out.println("  Cells: " + spatialGridCellCount);
        System.out.println("  Agents: " + spatialGridAgentCount);
        System.out.println("  Query time: " + String.format("%.2fms", spatialQueryAvgTime));
        
        System.out.println();
        System.out.println("========================================");
    }
}