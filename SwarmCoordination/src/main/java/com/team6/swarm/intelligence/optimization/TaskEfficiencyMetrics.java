/**
 * TASKEFFICIENCYMETRICS CLASS - Task Allocation Performance Tracking
 *
 * PURPOSE:
 * - Track efficiency of task assignment and completion
 * - Measure completion rates, assignment quality, and workload balance
 * - Provide metrics for optimizing task allocation
 */
package com.team6.swarm.intelligence.optimization;

public class TaskEfficiencyMetrics {
    private int totalAssignments = 0;
    private int successfulCompletions = 0;
    private int failedCompletions = 0;
    private double avgAssignmentScore = 0.0;
    private double completionRate = 0.0;
    private double avgEfficiency = 0.0;
    private double workloadBalance = 1.0;
    
    public void recordAssignment(double score) {
        avgAssignmentScore = (avgAssignmentScore * totalAssignments + score) / (totalAssignments + 1);
        totalAssignments++;
    }
    
    public void recordCompletion(boolean successful, long duration, double efficiency) {
        if (successful) {
            successfulCompletions++;
            avgEfficiency = (avgEfficiency * (successfulCompletions - 1) + efficiency) / successfulCompletions;
        } else {
            failedCompletions++;
        }
        
        int total = successfulCompletions + failedCompletions;
        completionRate = total > 0 ? (double) successfulCompletions / total : 0.0;
    }
    
    public void recordWorkloadBalance(double balance) {
        workloadBalance = balance;
    }
    
    // Getters
    public int getTotalAssignments() {
        return totalAssignments;
    }
    
    public int getSuccessfulCompletions() {
        return successfulCompletions;
    }
    
    public int getFailedCompletions() {
        return failedCompletions;
    }
    
    public double getAvgAssignmentScore() {
        return avgAssignmentScore;
    }
    
    public double getCompletionRate() {
        return completionRate;
    }
    
    public double getAvgEfficiency() {
        return avgEfficiency;
    }
    
    public double getWorkloadBalance() {
        return workloadBalance;
    }
}