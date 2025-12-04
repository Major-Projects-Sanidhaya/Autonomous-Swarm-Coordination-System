/**
 * FORMATIONQUALITYMETRICS CLASS - Formation Performance Tracking
 *
 * PURPOSE:
 * - Track quality of formation maintenance
 * - Measure accuracy and stability of formations
 * - Provide metrics for tuning formation controllers
 */
package com.team6.swarm.intelligence.optimization;

public class FormationQualityMetrics {
    private double formationAccuracy = 0.0;
    private double stabilityScore = 0.0;
    private int accuracySamples = 0;
    private int stabilitySamples = 0;
    
    public void recordAccuracy(double accuracy) {
        formationAccuracy = (formationAccuracy * accuracySamples + accuracy) / (accuracySamples + 1);
        accuracySamples++;
    }
    
    public void recordStability(double stability) {
        stabilityScore = (stabilityScore * stabilitySamples + stability) / (stabilitySamples + 1);
        stabilitySamples++;
    }
    
    // Getters
    public double getFormationAccuracy() {
        return formationAccuracy;
    }
    
    public double getStabilityScore() {
        return stabilityScore;
    }
    
    public int getAccuracySamples() {
        return accuracySamples;
    }
    
    public int getStabilitySamples() {
        return stabilitySamples;
    }
}