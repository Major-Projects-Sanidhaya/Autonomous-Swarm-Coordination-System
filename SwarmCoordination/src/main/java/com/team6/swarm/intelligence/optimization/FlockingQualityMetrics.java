/**
 * FLOCKINGQUALITYMETRICS CLASS - Flocking Performance Tracking
 *
 * PURPOSE:
 * - Track quality of flocking behavior
 * - Measure cohesion, separation, alignment, and stability
 * - Provide metrics for tuning flocking parameters
 */
package com.team6.swarm.intelligence.optimization;

public class FlockingQualityMetrics {
    private double cohesionQuality = 0.0;
    private double separationSafety = 0.0;
    private double alignmentQuality = 0.0;
    private double stabilityScore = 0.0;
    
    private int cohesionSamples = 0;
    private int separationSamples = 0;
    private int alignmentSamples = 0;
    private int stabilitySamples = 0;
    
    public void recordCohesion(double quality) {
        cohesionQuality = (cohesionQuality * cohesionSamples + quality) / (cohesionSamples + 1);
        cohesionSamples++;
    }
    
    public void recordSeparation(double safety) {
        separationSafety = (separationSafety * separationSamples + safety) / (separationSamples + 1);
        separationSamples++;
    }
    
    public void recordAlignment(double quality) {
        alignmentQuality = (alignmentQuality * alignmentSamples + quality) / (alignmentSamples + 1);
        alignmentSamples++;
    }
    
    public void recordStability(double stability) {
        stabilityScore = (stabilityScore * stabilitySamples + stability) / (stabilitySamples + 1);
        stabilitySamples++;
    }
    
    // Getters
    public double getCohesionQuality() {
        return cohesionQuality;
    }
    
    public double getSeparationSafety() {
        return separationSafety;
    }
    
    public double getAlignmentQuality() {
        return alignmentQuality;
    }
    
    public double getStabilityScore() {
        return stabilityScore;
    }
    
    public int getCohesionSamples() {
        return cohesionSamples;
    }
    
    public int getSeparationSamples() {
        return separationSamples;
    }
    
    public int getAlignmentSamples() {
        return alignmentSamples;
    }
    
    public int getStabilitySamples() {
        return stabilitySamples;
    }
}