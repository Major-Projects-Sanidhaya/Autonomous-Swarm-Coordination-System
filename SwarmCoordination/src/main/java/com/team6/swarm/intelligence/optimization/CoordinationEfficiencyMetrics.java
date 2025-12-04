/**
 * COORDINATIONEFFICIENCYMETRICS CLASS - Overall Coordination Tracking
 *
 * PURPOSE:
 * - Track overall system coordination efficiency
 * - Measure response times and computational efficiency
 * - Provide high-level system performance metrics
 */
package com.team6.swarm.intelligence.optimization;

public class CoordinationEfficiencyMetrics {
    private double avgResponseTime = 0.0;
    private double efficiency = 1.0;
    private int responseSamples = 0;
    private int efficiencySamples = 0;
    
    public void recordResponse(String eventType, long responseTimeMs) {
        avgResponseTime = (avgResponseTime * responseSamples + responseTimeMs) / (responseSamples + 1);
        responseSamples++;
    }
    
    public void recordEfficiency(double eff) {
        efficiency = (efficiency * efficiencySamples + eff) / (efficiencySamples + 1);
        efficiencySamples++;
    }
    
    // Getters
    public double getAvgResponseTime() {
        return avgResponseTime;
    }
    
    public double getEfficiency() {
        return efficiency;
    }
    
    public int getResponseSamples() {
        return responseSamples;
    }
    
    public int getEfficiencySamples() {
        return efficiencySamples;
    }
}