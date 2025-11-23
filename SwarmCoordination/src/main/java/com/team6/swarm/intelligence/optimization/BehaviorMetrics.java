/**
 * BEHAVIORMETRICS CLASS - Intelligence System Performance Tracking
 *
 * PURPOSE:
 * - Monitor quality and effectiveness of intelligence algorithms
 * - Track flocking cohesion and coordination metrics
 * - Measure decision-making speed and accuracy
 * - Evaluate task allocation efficiency
 * - Assess formation accuracy and stability
 *
 * KEY METRICS:
 *
 * FLOCKING METRICS:
 * - Cohesion Quality: How tightly swarm stays together (0.0-1.0)
 * - Separation Safety: Collision avoidance effectiveness (0.0-1.0)
 * - Alignment Quality: Velocity matching across swarm (0.0-1.0)
 * - Flocking Stability: Movement smoothness (low jitter)
 *
 * DECISION METRICS:
 * - Consensus Speed: Time to reach voting agreement
 * - Decision Quality: Success rate of democratic choices
 * - Vote Participation: Percentage of agents voting
 * - Consensus Stability: Agreement over time
 *
 * TASK METRICS:
 * - Task Completion Rate: Successfully finished tasks
 * - Assignment Efficiency: Task-to-agent matching quality
 * - Workload Balance: Fair distribution across agents
 * - Failure Rate: Tasks that couldn't be completed
 *
 * FORMATION METRICS:
 * - Formation Accuracy: Position error from target
 * - Formation Stability: Consistency over time
 * - Transition Smoothness: Quality of formation changes
 * - Collision Avoidance: Near-miss count during formations
 *
 * OVERALL COORDINATION:
 * - System Responsiveness: Time to react to events
 * - Multi-Agent Efficiency: Coordination overhead
 * - Adaptability: Recovery from disruptions
 * - Scalability: Performance with agent count
 */
package com.team6.swarm.intelligence.optimization;

import com.team6.swarm.core.*;
import java.util.*;

public class BehaviorMetrics {
    // Flocking metrics
    private FlockingQualityMetrics flockingMetrics;
    
    // Decision metrics
    private DecisionQualityMetrics decisionMetrics;
    
    // Task metrics
    private TaskEfficiencyMetrics taskMetrics;
    
    // Formation metrics
    private FormationQualityMetrics formationMetrics;
    
    // Overall coordination
    private CoordinationEfficiencyMetrics coordinationMetrics;
    
    // Tracking
    private long metricsStartTime;
    private int updateCount;
    
    /**
     * Constructor
     */
    public BehaviorMetrics() {
        this.flockingMetrics = new FlockingQualityMetrics();
        this.decisionMetrics = new DecisionQualityMetrics();
        this.taskMetrics = new TaskEfficiencyMetrics();
        this.formationMetrics = new FormationQualityMetrics();
        this.coordinationMetrics = new CoordinationEfficiencyMetrics();
        this.metricsStartTime = System.currentTimeMillis();
        this.updateCount = 0;
    }
    
    // ==================== FLOCKING METRICS ====================
    
    /**
     * MEASURE FLOCKING COHESION
     * Calculate how well swarm stays together
     */
    public void measureFlockingCohesion(List<AgentState> agents) {
        if (agents.size() < 2) {
            flockingMetrics.cohesionQuality = 1.0;
            return;
        }
        
        // Calculate swarm center
        Point2D center = calculateSwarmCenter(agents);
        
        // Calculate average distance from center
        double totalDistance = 0;
        for (AgentState agent : agents) {
            totalDistance += agent.position.distanceTo(center);
        }
        double avgDistance = totalDistance / agents.size();
        
        // Calculate cohesion quality (lower distance = better cohesion)
        // Perfect cohesion (0 distance) = 1.0
        // Poor cohesion (200+ distance) = 0.0
        double cohesion = Math.max(0, 1.0 - (avgDistance / 200.0));
        
        flockingMetrics.recordCohesion(cohesion);
    }
    
    /**
     * MEASURE SEPARATION SAFETY
     * Check how well agents avoid collisions
     */
    public void measureSeparationSafety(List<AgentState> agents) {
        if (agents.size() < 2) {
            flockingMetrics.separationSafety = 1.0;
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
            flockingMetrics.alignmentQuality = 1.0;
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
            taskMetrics.workloadBalance = 1.0;
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
            formationMetrics.formationAccuracy = 1.0;
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
        report.timestamp = System.currentTimeMillis();
        report.durationSeconds = (report.timestamp - metricsStartTime) / 1000;
        report.updateCount = updateCount;
        
        // Populate metrics
        report.flockingMetrics = flockingMetrics;
        report.decisionMetrics = decisionMetrics;
        report.taskMetrics = taskMetrics;
        report.formationMetrics = formationMetrics;
        report.coordinationMetrics = coordinationMetrics;
        
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
        System.out.println("  Cohesion: " + formatPercent(flockingMetrics.cohesionQuality));
        System.out.println("  Separation Safety: " + formatPercent(flockingMetrics.separationSafety));
        System.out.println("  Alignment: " + formatPercent(flockingMetrics.alignmentQuality));
        System.out.println("  Stability: " + formatPercent(flockingMetrics.stabilityScore));
        
        System.out.println();
        System.out.println("Decision Making:");
        System.out.println("  Avg Consensus Time: " + 
            String.format("%.1fms", decisionMetrics.avgConsensusTime));
        System.out.println("  Success Rate: " + formatPercent(decisionMetrics.successRate));
        System.out.println("  Participation Rate: " + 
            formatPercent(decisionMetrics.avgParticipationRate));
        
        System.out.println();
        System.out.println("Task Management:");
        System.out.println("  Completion Rate: " + formatPercent(taskMetrics.completionRate));
        System.out.println("  Avg Assignment Score: " + 
            String.format("%.2f", taskMetrics.avgAssignmentScore));
        System.out.println("  Workload Balance: " + formatPercent(taskMetrics.workloadBalance));
        
        System.out.println();
        System.out.println("Formation Quality:");
        System.out.println("  Accuracy: " + formatPercent(formationMetrics.formationAccuracy));
        System.out.println("  Stability: " + formatPercent(formationMetrics.stabilityScore));
        
        System.out.println();
        System.out.println("Overall Coordination:");
        System.out.println("  Avg Response Time: " + 
            String.format("%.1fms", coordinationMetrics.avgResponseTime));
        System.out.println("  Efficiency: " + formatPercent(coordinationMetrics.efficiency));
        
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
}

// ==================== METRIC DATA CLASSES ====================

class FlockingQualityMetrics {
    double cohesionQuality = 0.0;
    double separationSafety = 0.0;
    double alignmentQuality = 0.0;
    double stabilityScore = 0.0;
    
    int cohesionSamples = 0;
    int separationSamples = 0;
    int alignmentSamples = 0;
    int stabilitySamples = 0;
    
    void recordCohesion(double quality) {
        cohesionQuality = (cohesionQuality * cohesionSamples + quality) / (cohesionSamples + 1);
        cohesionSamples++;
    }
    
    void recordSeparation(double safety) {
        separationSafety = (separationSafety * separationSamples + safety) / (separationSamples + 1);
        separationSamples++;
    }
    
    void recordAlignment(double quality) {
        alignmentQuality = (alignmentQuality * alignmentSamples + quality) / (alignmentSamples + 1);
        alignmentSamples++;
    }
    
    void recordStability(double stability) {
        stabilityScore = (stabilityScore * stabilitySamples + stability) / (stabilitySamples + 1);
        stabilitySamples++;
    }
}

class DecisionQualityMetrics {
    int totalVotes = 0;
    int successfulVotes = 0;
    double avgConsensusTime = 0.0;
    double avgParticipationRate = 0.0;
    double successRate = 0.0;
    
    void recordVote(long durationMs, boolean consensusReached, int participants, int total) {
        totalVotes++;
        if (consensusReached) successfulVotes++;
        
        avgConsensusTime = (avgConsensusTime * (totalVotes - 1) + durationMs) / totalVotes;
        
        double participationRate = total > 0 ? (double) participants / total : 0.0;
        avgParticipationRate = (avgParticipationRate * (totalVotes - 1) + participationRate) / totalVotes;
        
        successRate = (double) successfulVotes / totalVotes;
    }
    
    int successfulOutcomes = 0;
    int totalOutcomes = 0;
    
    void recordOutcome(boolean successful) {
        totalOutcomes++;
        if (successful) successfulOutcomes++;
    }
}

class TaskEfficiencyMetrics {
    int totalAssignments = 0;
    int successfulCompletions = 0;
    int failedCompletions = 0;
    double avgAssignmentScore = 0.0;
    double completionRate = 0.0;
    double avgEfficiency = 0.0;
    double workloadBalance = 1.0;
    
    void recordAssignment(double score) {
        avgAssignmentScore = (avgAssignmentScore * totalAssignments + score) / (totalAssignments + 1);
        totalAssignments++;
    }
    
    void recordCompletion(boolean successful, long duration, double efficiency) {
        if (successful) {
            successfulCompletions++;
            avgEfficiency = (avgEfficiency * (successfulCompletions - 1) + efficiency) / successfulCompletions;
        } else {
            failedCompletions++;
        }
        
        int total = successfulCompletions + failedCompletions;
        completionRate = total > 0 ? (double) successfulCompletions / total : 0.0;
    }
    
    void recordWorkloadBalance(double balance) {
        workloadBalance = balance;
    }
}

class FormationQualityMetrics {
    double formationAccuracy = 0.0;
    double stabilityScore = 0.0;
    int accuracySamples = 0;
    int stabilitySamples = 0;
    
    void recordAccuracy(double accuracy) {
        formationAccuracy = (formationAccuracy * accuracySamples + accuracy) / (accuracySamples + 1);
        accuracySamples++;
    }
    
    void recordStability(double stability) {
        stabilityScore = (stabilityScore * stabilitySamples + stability) / (stabilitySamples + 1);
        stabilitySamples++;
    }
}

class CoordinationEfficiencyMetrics {
    double avgResponseTime = 0.0;
    double efficiency = 1.0;
    int responseSamples = 0;
    int efficiencySamples = 0;
    
    void recordResponse(String eventType, long responseTimeMs) {
        avgResponseTime = (avgResponseTime * responseSamples + responseTimeMs) / (responseSamples + 1);
        responseSamples++;
    }
    
    void recordEfficiency(double eff) {
        efficiency = (efficiency * efficiencySamples + eff) / (efficiencySamples + 1);
        efficiencySamples++;
    }
}

class MetricsReport {
    long timestamp;
    long durationSeconds;
    int updateCount;
    
    FlockingQualityMetrics flockingMetrics;
    DecisionQualityMetrics decisionMetrics;
    TaskEfficiencyMetrics taskMetrics;
    FormationQualityMetrics formationMetrics;
    CoordinationEfficiencyMetrics coordinationMetrics;
}
