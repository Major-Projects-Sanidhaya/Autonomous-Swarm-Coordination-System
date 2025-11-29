/**
 * BEHAVIORMETRICS CLASS - Intelligence System Performance Tracking
 *
 * PURPOSE:
 * - Monitor quality and effectiveness of intelligence algorithms
 * - Integrates with Week 7-8 monitoring systems
 * - Provides unified metrics interface for intelligence systems
 * - Leverages SystemHealthMonitor and SwarmAnalytics
 *
 * INTEGRATION WITH EXISTING SYSTEMS:
 * - Uses SystemHealthMonitor from Week 7 for health tracking
 * - Uses SwarmAnalytics from Week 8 for behavior analysis
 * - Uses MetricsCollector from Week 8 for time-series data
 * - Extends existing monitoring with intelligence-specific metrics
 */
package com.team6.swarm.intelligence.optimization;

import com.team6.swarm.core.*;
import java.util.*;

public class BehaviorMetrics {
    // Use existing Week 7-8 systems
    private SystemHealthMonitor healthMonitor;
    private SwarmAnalytics swarmAnalytics;
    private MetricsCollector metricsCollector;
    
    // Custom metrics for intelligence-specific tracking
    private FlockingQualityMetrics flockingMetrics;
    private DecisionQualityMetrics decisionMetrics;
    private TaskEfficiencyMetrics taskMetrics;
    private FormationQualityMetrics formationMetrics;
    private CoordinationEfficiencyMetrics coordinationMetrics;
    
    // Tracking
    private long metricsStartTime;
    private int updateCount;
    
    /**
     * Constructor
     */
    public BehaviorMetrics() {
        this.healthMonitor = new SystemHealthMonitor();
        this.swarmAnalytics = new SwarmAnalytics();
        this.metricsCollector = new MetricsCollector();
        
        this.flockingMetrics = new FlockingQualityMetrics();
        this.decisionMetrics = new DecisionQualityMetrics();
        this.taskMetrics = new TaskEfficiencyMetrics();
        this.formationMetrics = new FormationQualityMetrics();
        this.coordinationMetrics = new CoordinationEfficiencyMetrics();
        
        this.metricsStartTime = System.currentTimeMillis();
        this.updateCount = 0;
        
        System.out.println("BehaviorMetrics initialized with Week 7-8 system integration");
    }
    
    // ==================== FLOCKING METRICS ====================
    
    /**
     * MEASURE FLOCKING COHESION
     * Calculate how well swarm stays together
     * Also uses SwarmAnalytics for additional analysis
     */
    public void measureFlockingCohesion(List<AgentState> agents) {
        if (agents.size() < 2) {
            flockingMetrics.recordCohesion(1.0);
            return;
        }
        
        // Use SwarmAnalytics for comprehensive behavior analysis
        SwarmAnalytics.SwarmBehaviorSnapshot snapshot = swarmAnalytics.analyzeSwarmBehavior(agents);
        
        if (snapshot != null) {
            // Use the cohesion score from SwarmAnalytics
            flockingMetrics.recordCohesion(snapshot.cohesionScore);
            metricsCollector.recordMetric("swarm_cohesion", snapshot.cohesionScore);
        } else {
            // Fallback calculation
            Point2D center = calculateSwarmCenter(agents);
            double totalDistance = 0;
            for (AgentState agent : agents) {
                totalDistance += agent.position.distanceTo(center);
            }
            double avgDistance = totalDistance / agents.size();
            double cohesion = Math.max(0, 1.0 - (avgDistance / 200.0));
            flockingMetrics.recordCohesion(cohesion);
            metricsCollector.recordMetric("swarm_cohesion", cohesion);
        }
    }
    
    /**
     * MEASURE SEPARATION SAFETY
     * Check how well agents avoid collisions
     */
    public void measureSeparationSafety(List<AgentState> agents) {
        if (agents.size() < 2) {
            flockingMetrics.recordSeparation(1.0);
            return;
        }
        
        int nearCollisions = 0;
        int totalPairs = 0;
        double safetyThreshold = 25.0; // Minimum safe distance
        
        // Check all agent pairs
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                double distance = agents.get(i).position.distanceTo(
                    agents.get(j).position);
                
                if (distance < safetyThreshold) {
                    nearCollisions++;
                }
                totalPairs++;
            }
        }
        
        // Safety = 1.0 - (near collisions / total pairs)
        double safety = totalPairs > 0 ? 
            1.0 - ((double) nearCollisions / totalPairs) : 1.0;
        
        flockingMetrics.recordSeparation(safety);
    }
    
    /**
     * MEASURE ALIGNMENT QUALITY
     * Check velocity matching across swarm
     */
    public void measureAlignmentQuality(List<AgentState> agents) {
        if (agents.size() < 2) {
            flockingMetrics.recordAlignment(1.0);
            return;
        }
        
        // Calculate average velocity
        double avgVx = 0;
        double avgVy = 0;
        for (AgentState agent : agents) {
            avgVx += agent.velocity.x;
            avgVy += agent.velocity.y;
        }
        avgVx /= agents.size();
        avgVy /= agents.size();
        
        // Calculate velocity variance
        double variance = 0;
        for (AgentState agent : agents) {
            double dx = agent.velocity.x - avgVx;
            double dy = agent.velocity.y - avgVy;
            variance += Math.sqrt(dx * dx + dy * dy);
        }
        variance /= agents.size();
        
        // Alignment quality (lower variance = better alignment)
        // Perfect alignment (0 variance) = 1.0
        // Poor alignment (50+ variance) = 0.0
        double alignment = Math.max(0, 1.0 - (variance / 50.0));
        
        flockingMetrics.recordAlignment(alignment);
    }
    
    /**
     * MEASURE FLOCKING STABILITY
     * Track smoothness of movement (jitter detection)
     */
    public void measureFlockingStability(List<AgentState> agents, 
                                        Map<Integer, Point2D> previousPositions) {
        if (previousPositions == null || previousPositions.isEmpty()) {
            return;
        }
        
        double totalJitter = 0;
        int count = 0;
        
        for (AgentState agent : agents) {
            Point2D prevPos = previousPositions.get(agent.agentId);
            if (prevPos == null) continue;
            
            // Calculate movement distance
            double movement = agent.position.distanceTo(prevPos);
            
            // Expected movement based on velocity
            double expectedMovement = agent.velocity.magnitude() * 0.033; // ~30 FPS
            
            // Jitter = unexpected movement
            double jitter = Math.abs(movement - expectedMovement);
            totalJitter += jitter;
            count++;
        }
        
        if (count > 0) {
            double avgJitter = totalJitter / count;
            // Stability = 1.0 - (jitter / threshold)
            double stability = Math.max(0, 1.0 - (avgJitter / 10.0));
            flockingMetrics.recordStability(stability);
        }
    }
    
    // ==================== DECISION METRICS ====================
    
    /**
     * RECORD VOTE COMPLETION
     */
    public void recordVoteCompletion(long durationMs, boolean consensusReached,
                                    int participantCount, int totalAgents) {
        decisionMetrics.recordVote(durationMs, consensusReached, 
                                    participantCount, totalAgents);
    }
    
    /**
     * RECORD DECISION OUTCOME
     */
    public void recordDecisionOutcome(boolean successful) {
        decisionMetrics.recordOutcome(successful);
    }
    
    // ==================== TASK METRICS ====================
    
    /**
     * RECORD TASK ASSIGNMENT
     */
    public void recordTaskAssignment(String taskId, int agentId, double assignmentScore) {
        taskMetrics.recordAssignment(assignmentScore);
    }
    
    /**
     * RECORD TASK COMPLETION
     */
    public void recordTaskCompletion(String taskId, boolean successful, 
                                    long duration, double efficiency) {
        taskMetrics.recordCompletion(successful, duration, efficiency);
    }
    
    /**
     * MEASURE WORKLOAD BALANCE
     */
    public void measureWorkloadBalance(Map<Integer, Integer> agentTaskCounts) {
        if (agentTaskCounts.isEmpty()) {
            taskMetrics.recordWorkloadBalance(1.0);
            return;
        }
        
        // Calculate variance in task counts
        double avgTasks = agentTaskCounts.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        
        double variance = 0;
        for (int taskCount : agentTaskCounts.values()) {
            variance += Math.pow(taskCount - avgTasks, 2);
        }
        variance /= agentTaskCounts.size();
        double stdDev = Math.sqrt(variance);
        
        // Balance = 1.0 - (stdDev / avgTasks)
        // Perfect balance (stdDev = 0) = 1.0
        double balance = avgTasks > 0 ? 
            Math.max(0, 1.0 - (stdDev / avgTasks)) : 1.0;
        
        taskMetrics.recordWorkloadBalance(balance);
    }
    
    // ==================== FORMATION METRICS ====================
    
    /**
     * MEASURE FORMATION ACCURACY
     */
    public void measureFormationAccuracy(List<AgentState> agents,
                                        Map<Integer, Point2D> targetPositions) {
        if (targetPositions.isEmpty()) {
            formationMetrics.recordAccuracy(1.0);
            return;
        }
        
        double totalError = 0;
        int count = 0;
        
        for (AgentState agent : agents) {
            Point2D target = targetPositions.get(agent.agentId);
            if (target == null) continue;
            
            double error = agent.position.distanceTo(target);
            totalError += error;
            count++;
        }
        
        if (count > 0) {
            double avgError = totalError / count;
            // Accuracy = 1.0 - (error / threshold)
            // Perfect accuracy (0 error) = 1.0
            // Poor accuracy (100+ error) = 0.0
            double accuracy = Math.max(0, 1.0 - (avgError / 100.0));
            formationMetrics.recordAccuracy(accuracy);
        }
    }
    
    /**
     * MEASURE FORMATION STABILITY
     */
    public void measureFormationStability(double currentAccuracy, 
                                          double previousAccuracy) {
        // Stability = consistency of accuracy over time
        double change = Math.abs(currentAccuracy - previousAccuracy);
        double stability = Math.max(0, 1.0 - (change * 10));
        formationMetrics.recordStability(stability);
    }
    
    // ==================== COORDINATION METRICS ====================
    
    /**
     * RECORD SYSTEM RESPONSE TIME
     */
    public void recordResponseTime(String eventType, long responseTimeMs) {
        coordinationMetrics.recordResponse(eventType, responseTimeMs);
    }
    
    /**
     * MEASURE COORDINATION EFFICIENCY
     */
    public void measureCoordinationEfficiency(int agentCount, 
                                              double computationTimeMs) {
        // Efficiency = expected time / actual time
        // Expected time increases with agent count
        double expectedTime = agentCount * 0.5; // 0.5ms per agent baseline
        double efficiency = expectedTime / Math.max(computationTimeMs, 0.1);
        efficiency = Math.min(1.0, efficiency); // Cap at 1.0
        
        coordinationMetrics.recordEfficiency(efficiency);
    }
    
    // ==================== REPORTING ====================
    
    /**
     * GENERATE COMPREHENSIVE REPORT
     */
    public MetricsReport generateReport() {
        MetricsReport report = new MetricsReport();
        report.setTimestamp(System.currentTimeMillis());
        report.setDurationSeconds((report.getTimestamp() - metricsStartTime) / 1000);
        report.setUpdateCount(updateCount);
        
        // Populate metrics
        report.setFlockingMetrics(flockingMetrics);
        report.setDecisionMetrics(decisionMetrics);
        report.setTaskMetrics(taskMetrics);
        report.setFormationMetrics(formationMetrics);
        report.setCoordinationMetrics(coordinationMetrics);
        // Include system health report from core monitor
        report.setSystemHealthReport(healthMonitor.getHealthReport());
        
        return report;
    }
    
    /**
     * PRINT SUMMARY
     */
    public void printSummary() {
        System.out.println("========================================");
        System.out.println("BEHAVIOR METRICS SUMMARY");
        System.out.println("========================================");
        System.out.println();
        
        System.out.println("Flocking Quality:");
        System.out.println("  Cohesion: " + formatPercent(flockingMetrics.getCohesionQuality()));
        System.out.println("  Separation Safety: " + formatPercent(flockingMetrics.getSeparationSafety()));
        System.out.println("  Alignment: " + formatPercent(flockingMetrics.getAlignmentQuality()));
        System.out.println("  Stability: " + formatPercent(flockingMetrics.getStabilityScore()));
        
        System.out.println();
        System.out.println("Decision Making:");
        System.out.println("  Avg Consensus Time: " + 
            String.format("%.1fms", decisionMetrics.getAvgConsensusTime()));
        System.out.println("  Success Rate: " + formatPercent(decisionMetrics.getSuccessRate()));
        System.out.println("  Participation Rate: " + 
            formatPercent(decisionMetrics.getAvgParticipationRate()));
        
        System.out.println();
        System.out.println("Task Management:");
        System.out.println("  Completion Rate: " + formatPercent(taskMetrics.getCompletionRate()));
        System.out.println("  Avg Assignment Score: " + 
            String.format("%.2f", taskMetrics.getAvgAssignmentScore()));
        System.out.println("  Workload Balance: " + formatPercent(taskMetrics.getWorkloadBalance()));
        
        System.out.println();
        System.out.println("Formation Quality:");
        System.out.println("  Accuracy: " + formatPercent(formationMetrics.getFormationAccuracy()));
        System.out.println("  Stability: " + formatPercent(formationMetrics.getStabilityScore()));
        
        System.out.println();
        System.out.println("Overall Coordination:");
        System.out.println("  Avg Response Time: " + 
            String.format("%.1fms", coordinationMetrics.getAvgResponseTime()));
        System.out.println("  Efficiency: " + formatPercent(coordinationMetrics.getEfficiency()));
        
        System.out.println();
        System.out.println("========================================");
    }
    
    private String formatPercent(double value) {
        return String.format("%.1f%%", value * 100);
    }
    
    /**
     * RESET ALL METRICS
     */
    public void reset() {
        flockingMetrics = new FlockingQualityMetrics();
        decisionMetrics = new DecisionQualityMetrics();
        taskMetrics = new TaskEfficiencyMetrics();
        formationMetrics = new FormationQualityMetrics();
        coordinationMetrics = new CoordinationEfficiencyMetrics();
        metricsStartTime = System.currentTimeMillis();
        updateCount = 0;
    }
    
    /**
     * UPDATE
     * Call each frame to increment counter
     */
    public void update() {
        updateCount++;
    }
    
    // Helper method
    private Point2D calculateSwarmCenter(List<AgentState> agents) {
        double sumX = 0;
        double sumY = 0;
        for (AgentState agent : agents) {
            sumX += agent.position.x;
            sumY += agent.position.y;
        }
        return new Point2D(sumX / agents.size(), sumY / agents.size());
    }
    
    // Getters
    public FlockingQualityMetrics getFlockingMetrics() {
        return flockingMetrics;
    }
    
    public DecisionQualityMetrics getDecisionMetrics() {
        return decisionMetrics;
    }
    
    public TaskEfficiencyMetrics getTaskMetrics() {
        return taskMetrics;
    }
    
    public FormationQualityMetrics getFormationMetrics() {
        return formationMetrics;
    }
    
    public CoordinationEfficiencyMetrics getCoordinationMetrics() {
        return coordinationMetrics;
    }
}