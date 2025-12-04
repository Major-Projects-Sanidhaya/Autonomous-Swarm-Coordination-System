package com.team6.swarm.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Week 11-12: Track and display system performance
 * Purpose: Ensure smooth operation
 * Author: Anthony (UI Team)
 */
public class PerformanceMonitor {
    
    // Frame rate tracking
    private long lastFrameTime = 0;
    private double currentFPS = 0;
    private double averageFPS = 0;
    private double minFPS = Double.MAX_VALUE;
    private int frameCount = 0;
    
    // Timing breakdown
    private final Map<String, Long> timingBreakdown = new HashMap<>();
    private final Map<String, Long> startTimes = new HashMap<>();
    
    // System resources
    private double cpuUsage = 0;
    private long memoryUsed = 0;
    private long memoryTotal = 0;
    
    /**
     * Begin frame timing
     */
    public void beginFrame() {
        long currentTime = System.nanoTime();
        
        if (lastFrameTime > 0) {
            double deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0;
            currentFPS = 1.0 / deltaTime;
            
            // Update average
            averageFPS = (averageFPS * frameCount + currentFPS) / (frameCount + 1);
            
            // Update min
            if (currentFPS < minFPS) {
                minFPS = currentFPS;
            }
            
            frameCount++;
        }
        
        lastFrameTime = currentTime;
    }
    
    /**
     * Begin timing a component
     */
    public void beginTiming(String component) {
        startTimes.put(component, System.nanoTime());
    }
    
    /**
     * End timing a component
     */
    public void endTiming(String component) {
        Long startTime = startTimes.get(component);
        if (startTime != null) {
            long duration = System.nanoTime() - startTime;
            timingBreakdown.put(component, duration);
        }
    }
    
    /**
     * Update system resource usage
     */
    public void updateSystemResources() {
        Runtime runtime = Runtime.getRuntime();
        memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        memoryTotal = runtime.maxMemory();
        
        // CPU usage would require platform-specific code
        // This is a placeholder
        cpuUsage = 0.0;
    }
    
    /**
     * Get performance report
     */
    public String getPerformanceReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Performance Report\n");
        sb.append("==================\n");
        sb.append(String.format("FPS: %.1f (avg: %.1f, min: %.1f)\n", 
                  currentFPS, averageFPS, minFPS));
        sb.append(String.format("Memory: %d MB / %d MB\n", 
                  memoryUsed / 1_048_576, memoryTotal / 1_048_576));
        
        sb.append("\nTiming Breakdown:\n");
        for (Map.Entry<String, Long> entry : timingBreakdown.entrySet()) {
            sb.append(String.format("  %s: %.2f ms\n", 
                     entry.getKey(), entry.getValue() / 1_000_000.0));
        }
        
        return sb.toString();
    }
    
    /**
     * Reset statistics
     */
    public void reset() {
        frameCount = 0;
        averageFPS = 0;
        minFPS = Double.MAX_VALUE;
        timingBreakdown.clear();
    }
    
    // Getters
    public double getCurrentFPS() { return currentFPS; }
    public double getAverageFPS() { return averageFPS; }
    public double getMinFPS() { return minFPS; }
    public long getMemoryUsed() { return memoryUsed; }
    public long getMemoryTotal() { return memoryTotal; }
    public Map<String, Long> getTimingBreakdown() { return new HashMap<>(timingBreakdown); }
}
