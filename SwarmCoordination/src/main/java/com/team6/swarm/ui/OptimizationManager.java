package com.team6.swarm.ui;

/**
 * Week 11-12: Automatic performance optimization
 * Purpose: Maintain smooth operation
 * Author: Anthony (UI Team)
 */
public class OptimizationManager {
    
    public enum QualityLevel {
        LOW,
        MEDIUM,
        HIGH,
        ULTRA
    }
    
    private QualityLevel currentQuality = QualityLevel.HIGH;
    private boolean autoOptimizeEnabled = true;
    
    // Performance thresholds
    private static final double MIN_FPS_THRESHOLD = 25.0;
    private static final double TARGET_FPS = 30.0;
    private static final double HIGH_FPS_THRESHOLD = 40.0;
    
    // Optimization settings
    private boolean enableTrails = true;
    private boolean enableEffects = true;
    private boolean enableAntialiasing = true;
    private int maxVisibleAgents = 100;
    private double lodDistance = 200.0;
    
    /**
     * Update optimization based on current FPS
     */
    public void optimize(double currentFPS) {
        if (!autoOptimizeEnabled) {
            return;
        }
        
        if (currentFPS < MIN_FPS_THRESHOLD) {
            // Performance too low - reduce quality
            reduceQuality();
        } else if (currentFPS > HIGH_FPS_THRESHOLD) {
            // Performance good - increase quality
            increaseQuality();
        }
    }
    
    /**
     * Reduce visual quality
     */
    private void reduceQuality() {
        switch (currentQuality) {
            case ULTRA -> {
                currentQuality = QualityLevel.HIGH;
                enableAntialiasing = false;
            }
            case HIGH -> {
                currentQuality = QualityLevel.MEDIUM;
                enableEffects = false;
            }
            case MEDIUM -> {
                currentQuality = QualityLevel.LOW;
                enableTrails = false;
                maxVisibleAgents = 50;
            }
        }
        System.out.println("OptimizationManager: Quality reduced to " + currentQuality);
    }
    
    /**
     * Increase visual quality
     */
    private void increaseQuality() {
        switch (currentQuality) {
            case LOW -> {
                currentQuality = QualityLevel.MEDIUM;
                enableTrails = true;
                maxVisibleAgents = 75;
            }
            case MEDIUM -> {
                currentQuality = QualityLevel.HIGH;
                enableEffects = true;
            }
            case HIGH -> {
                currentQuality = QualityLevel.ULTRA;
                enableAntialiasing = true;
                maxVisibleAgents = 100;
            }
        }
        System.out.println("OptimizationManager: Quality increased to " + currentQuality);
    }
    
    /**
     * Check if agent should be rendered based on distance
     */
    public boolean shouldRenderAgent(double distance) {
        return distance <= lodDistance;
    }
    
    /**
     * Get level of detail for agent at distance
     */
    public int getLOD(double distance) {
        if (distance < lodDistance * 0.5) {
            return 2; // High detail
        } else if (distance < lodDistance) {
            return 1; // Medium detail
        } else {
            return 0; // Low detail or culled
        }
    }
    
    // Getters and setters
    public QualityLevel getCurrentQuality() { return currentQuality; }
    public void setCurrentQuality(QualityLevel quality) { this.currentQuality = quality; }
    
    public boolean isAutoOptimizeEnabled() { return autoOptimizeEnabled; }
    public void setAutoOptimizeEnabled(boolean enabled) { this.autoOptimizeEnabled = enabled; }
    
    public boolean isEnableTrails() { return enableTrails; }
    public boolean isEnableEffects() { return enableEffects; }
    public boolean isEnableAntialiasing() { return enableAntialiasing; }
    public int getMaxVisibleAgents() { return maxVisibleAgents; }
}
