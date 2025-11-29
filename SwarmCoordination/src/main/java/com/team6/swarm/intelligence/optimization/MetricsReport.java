/**
 * METRICSREPORT CLASS - Comprehensive Behavior Metrics Report
 *
 * PURPOSE:
 * - Aggregates all behavior metrics into single report
 * - Provides snapshot of intelligence system performance
 * - Enables comprehensive performance analysis
 */
package com.team6.swarm.intelligence.optimization;

public class MetricsReport {
    private long timestamp;
    private long durationSeconds;
    private int updateCount;
    
    private FlockingQualityMetrics flockingMetrics;
    private DecisionQualityMetrics decisionMetrics;
    private TaskEfficiencyMetrics taskMetrics;
    private FormationQualityMetrics formationMetrics;
    private CoordinationEfficiencyMetrics coordinationMetrics;
    // Optional system health report from core SystemHealthMonitor
    private com.team6.swarm.core.SystemHealthMonitor.HealthReport systemHealthReport;
    
    public MetricsReport() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getDurationSeconds() {
        return durationSeconds;
    }
    
    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    
    public int getUpdateCount() {
        return updateCount;
    }
    
    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }
    
    public FlockingQualityMetrics getFlockingMetrics() {
        return flockingMetrics;
    }
    
    public void setFlockingMetrics(FlockingQualityMetrics flockingMetrics) {
        this.flockingMetrics = flockingMetrics;
    }
    
    public DecisionQualityMetrics getDecisionMetrics() {
        return decisionMetrics;
    }
    
    public void setDecisionMetrics(DecisionQualityMetrics decisionMetrics) {
        this.decisionMetrics = decisionMetrics;
    }
    
    public TaskEfficiencyMetrics getTaskMetrics() {
        return taskMetrics;
    }
    
    public void setTaskMetrics(TaskEfficiencyMetrics taskMetrics) {
        this.taskMetrics = taskMetrics;
    }
    
    public FormationQualityMetrics getFormationMetrics() {
        return formationMetrics;
    }
    
    public void setFormationMetrics(FormationQualityMetrics formationMetrics) {
        this.formationMetrics = formationMetrics;
    }
    
    public CoordinationEfficiencyMetrics getCoordinationMetrics() {
        return coordinationMetrics;
    }
    
    public void setCoordinationMetrics(CoordinationEfficiencyMetrics coordinationMetrics) {
        this.coordinationMetrics = coordinationMetrics;
    }

    public com.team6.swarm.core.SystemHealthMonitor.HealthReport getSystemHealthReport() {
        return systemHealthReport;
    }

    public void setSystemHealthReport(com.team6.swarm.core.SystemHealthMonitor.HealthReport systemHealthReport) {
        this.systemHealthReport = systemHealthReport;
    }
}