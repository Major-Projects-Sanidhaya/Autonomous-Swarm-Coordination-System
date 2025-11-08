/**
 * ADAPTIVEBEHAVIOR CLASS - Self-Tuning Swarm Parameters
 *
 * PURPOSE:
 * - Automatically adjust behavior parameters based on performance
 * - Learn from success and failure patterns
 * - Optimize swarm efficiency over time
 * - Adapt to changing environmental conditions
 *
 * ADAPTATION TARGETS:
 *
 * 1. FLOCKING PARAMETERS:
 *    - Adjust separation weight based on collision rate
 *    - Tune cohesion based on swarm dispersion
 *    - Modify alignment for smoother movement
 *    - Adapt speed limits for efficiency
 *
 * 2. VOTING THRESHOLDS:
 *    - Lower consensus requirement if votes timeout
 *    - Raise threshold if decisions are poor quality
 *    - Adjust voting duration based on response time
 *
 * 3. TASK ALLOCATION:
 *    - Rebalance workload based on completion rates
 *    - Adjust battery thresholds based on mission length
 *    - Optimize distance scoring for efficiency
 *
 * 4. FORMATION SPACING:
 *    - Tighten spacing if collisions low
 *    - Loosen spacing if agents struggle to maintain
 *    - Adapt to different formation types
 *
 * LEARNING APPROACH:
 *
 * Simple Feedback Loop:
 * 1. Measure current performance metric
 * 2. Compare to desired target
 * 3. Adjust parameter in direction of improvement
 * 4. Apply small incremental changes
 * 5. Repeat continuously
 *
 * PERFORMANCE METRICS:
 *
 * Flocking Quality:
 * - Collision rate (lower is better)
 * - Swarm cohesion (0.7-0.9 is optimal)
 * - Movement smoothness (less jitter is better)
 *
 * Decision Quality:
 * - Vote completion rate (higher is better)
 * - Consensus time (faster is better)
 * - Decision reversal rate (lower is better)
 *
 * Task Efficiency:
 * - Task completion rate (higher is better)
 * - Workload balance (even distribution is better)
 * - Battery efficiency (less power per task is better)
 *
 * Formation Accuracy:
 * - Position error (lower is better)
 * - Correction frequency (lower is better)
 * - Formation cohesion (0.8+ is good)
 *
 * USAGE PATTERNS:
 *
 * Initialize:
 * AdaptiveBehavior adaptive = new AdaptiveBehavior(coordinator);
 *
 * Enable Adaptation:
 * adaptive.enableAdaptation();
 *
 * Update (called periodically):
 * adaptive.update(currentMetrics);
 *
 * Get Recommendations:
 * ParameterAdjustment adjustment = adaptive.getNextAdjustment();
 *
 * INTEGRATION POINTS:
 * - Monitors: All sub-system performance metrics
 * - Adjusts: FlockingParameters, VotingParameters, TaskAllocator settings
 * - Reports to: Anthony's UI for visualization
 * - Controlled by: User can enable/disable adaptation
 */
package com.team6.swarm.intelligence.emergence;

import com.team6.swarm.intelligence.flocking.FlockingParameters;
import com.team6.swarm.intelligence.voting.VotingParameters;
import java.util.*;

public class AdaptiveBehavior {
    // Reference to coordinator
    private SwarmCoordinator coordinator;
    
    // Adaptation state
    private boolean adaptationEnabled;
    private long lastAdaptationTime;
    private static final long ADAPTATION_INTERVAL = 5000;  // 5 seconds
    
    // Performance history
    private List<PerformanceSnapshot> performanceHistory;
    private static final int MAX_HISTORY = 20;
    
    // Adaptation parameters
    private static final double ADJUSTMENT_RATE = 0.05;  // 5% change per adjustment
    private static final double TARGET_COHESION = 0.8;
    private static final double TARGET_COLLISION_RATE = 0.02;  // 2%
    private static final double TARGET_VOTE_COMPLETION = 0.9;  // 90%
    
    // Adjustment tracking
    private int flockingAdjustments;
    private int votingAdjustments;
    private int taskAdjustments;
    private int formationAdjustments;
    
    /**
     * Constructor
     */
    public AdaptiveBehavior(SwarmCoordinator coordinator) {
        this.coordinator = coordinator;
        this.adaptationEnabled = false;
        this.lastAdaptationTime = System.currentTimeMillis();
        this.performanceHistory = new ArrayList<>();
        this.flockingAdjustments = 0;
        this.votingAdjustments = 0;
        this.taskAdjustments = 0;
        this.formationAdjustments = 0;
        
        System.out.println("AdaptiveBehavior initialized (disabled by default)");
    }
    
    // ==================== ADAPTATION CONTROL ====================
    
    /**
     * ENABLE ADAPTATION
     * Start automatic parameter tuning
     */
    public void enableAdaptation() {
        adaptationEnabled = true;
        lastAdaptationTime = System.currentTimeMillis();
        System.out.println("Adaptive behavior ENABLED - system will self-tune");
    }
    
    /**
     * DISABLE ADAPTATION
     * Stop automatic tuning
     */
    public void disableAdaptation() {
        adaptationEnabled = false;
        System.out.println("Adaptive behavior DISABLED");
    }
    
    /**
     * MAIN UPDATE
     * Called periodically to check if adaptation needed
     */
    public void update(PerformanceMetrics currentMetrics) {
        if (!adaptationEnabled) return;
        
        long currentTime = System.currentTimeMillis();
        
        // Record current performance (always record for trend analysis)
        recordPerformance(currentMetrics);
        
        // Check if time to adapt
        if (currentTime - lastAdaptationTime < ADAPTATION_INTERVAL) {
            return;  // Not time to adapt yet
        }
        
        // Analyze and adapt parameters
        adaptFlockingParameters(currentMetrics);
        adaptVotingParameters(currentMetrics);
        adaptTaskParameters(currentMetrics);
        
        lastAdaptationTime = currentTime;
    }
    
    // ==================== FLOCKING ADAPTATION ====================
    
    /**
     * ADAPT FLOCKING PARAMETERS
     * Adjust based on collision rate and cohesion
     */
    private void adaptFlockingParameters(PerformanceMetrics metrics) {
        FlockingParameters params = coordinator.getFlockingController().getParameters();
        boolean adjusted = false;
        
        // Adapt separation weight based on collision rate
        if (metrics.getCollisionRate() > TARGET_COLLISION_RATE) {
            // Too many collisions - increase separation
            params.separationWeight *= (1 + ADJUSTMENT_RATE);
            System.out.println(String.format(
                "Adaptation: Increasing separation weight to %.2f (collisions: %.1f%%)",
                params.separationWeight, metrics.getCollisionRate() * 100
            ));
            adjusted = true;
        } else if (metrics.getCollisionRate() < TARGET_COLLISION_RATE * 0.5) {
            // Very few collisions - can reduce separation slightly
            params.separationWeight *= (1 - ADJUSTMENT_RATE * 0.5);
            adjusted = true;
        }
        
        // Adapt cohesion weight based on swarm dispersion
        if (metrics.getSwarmCohesion() < TARGET_COHESION) {
            // Swarm too spread out - increase cohesion
            params.cohesionWeight *= (1 + ADJUSTMENT_RATE);
            System.out.println(String.format(
                "Adaptation: Increasing cohesion weight to %.2f (cohesion: %.2f)",
                params.cohesionWeight, metrics.getSwarmCohesion()
            ));
            adjusted = true;
        } else if (metrics.getSwarmCohesion() > 0.95) {
            // Swarm too tight - reduce cohesion
            params.cohesionWeight *= (1 - ADJUSTMENT_RATE * 0.5);
            adjusted = true;
        }
        
        // Adapt alignment for smooth movement
        if (metrics.getMovementJitter() > 0.3) {
            // Jittery movement - increase alignment
            params.alignmentWeight *= (1 + ADJUSTMENT_RATE);
            System.out.println(String.format(
                "Adaptation: Increasing alignment weight to %.2f (jitter: %.2f)",
                params.alignmentWeight, metrics.getMovementJitter()
            ));
            adjusted = true;
        }
        
        // Ensure parameters stay in valid range
    params.separationWeight = Math.max(0.5, Math.min(3.0, params.separationWeight));
    params.cohesionWeight = Math.max(0.5, Math.min(2.5, params.cohesionWeight));
    params.alignmentWeight = Math.max(0.5, Math.min(2.0, params.alignmentWeight));
        
        // Apply changes
        if (adjusted) {
            coordinator.getFlockingController().updateParameters(params);
            flockingAdjustments++;
        }
    }
    
    // ==================== VOTING ADAPTATION ====================
    
    /**
     * ADAPT VOTING PARAMETERS
     * Adjust based on vote completion and quality
     */
    private void adaptVotingParameters(PerformanceMetrics metrics) {
        VotingParameters params = coordinator.getVotingSystem().getParameters();
        boolean adjusted = false;
        
        // Adapt consensus threshold based on completion rate
        if (metrics.getVoteCompletionRate() < TARGET_VOTE_COMPLETION) {
            // Too many failed votes - lower threshold
            params.consensusThreshold *= (1 - ADJUSTMENT_RATE);
            System.out.println(String.format(
                "Adaptation: Lowering consensus threshold to %.0f%% (completion: %.0f%%)",
                params.consensusThreshold * 100, metrics.getVoteCompletionRate() * 100
            ));
            adjusted = true;
        } else if (metrics.getVoteCompletionRate() > 0.98 && params.consensusThreshold < 0.7) {
            // Very high completion - can raise threshold
            params.consensusThreshold *= (1 + ADJUSTMENT_RATE * 0.5);
            adjusted = true;
        }
        
        // Adapt voting timeout based on response time
        if (metrics.getAverageVoteTime() > params.votingTimeout * 0.9) {
            // Votes taking too long - increase timeout (use explicit cast to avoid lossy compound assignment)
            params.votingTimeout = (long) (params.votingTimeout * 1.2);
            System.out.println(String.format(
                "Adaptation: Increasing vote timeout to %dms",
                params.votingTimeout
            ));
            adjusted = true;
        } else if (metrics.getAverageVoteTime() < params.votingTimeout * 0.5) {
            // Votes complete quickly - can reduce timeout (explicit cast)
            params.votingTimeout = (long) (params.votingTimeout * 0.9);
            adjusted = true;
        }
        
        // Ensure parameters stay in valid range
        params.consensusThreshold = Math.max(0.5, Math.min(0.9, params.consensusThreshold));
        params.votingTimeout = Math.max(3000, Math.min(15000, params.votingTimeout));
        
        // Apply changes
        if (adjusted) {
            coordinator.getVotingSystem().updateParameters(params);
            votingAdjustments++;
        }
    }
    
    // ==================== TASK ADAPTATION ====================
    
    /**
     * ADAPT TASK PARAMETERS
     * Adjust based on completion rates and balance
     */
    private void adaptTaskParameters(PerformanceMetrics metrics) {
        boolean adjusted = false;
        
        // Check workload balance
        if (!metrics.isWorkloadBalanced()) {
            System.out.println("Adaptation: Workload imbalanced - triggering redistribution");
            // Trigger task redistribution
            // coordinator.getTaskAllocator().redistributeTasks(agents);
            adjusted = true;
        }
        
        // Adapt based on task completion rate
        if (metrics.getTaskCompletionRate() < 0.7) {
            System.out.println(String.format(
                "Adaptation: Low task completion rate (%.0f%%) - reviewing assignments",
                metrics.getTaskCompletionRate() * 100
            ));
            // Could adjust battery thresholds or distance scoring
            adjusted = true;
        }
        
        if (adjusted) {
            taskAdjustments++;
        }
    }
    
    // ==================== PERFORMANCE TRACKING ====================
    
    /**
     * RECORD PERFORMANCE
     * Store snapshot for trend analysis
     */
    private void recordPerformance(PerformanceMetrics metrics) {
        PerformanceSnapshot snapshot = new PerformanceSnapshot(
            metrics, System.currentTimeMillis());
        
        performanceHistory.add(snapshot);
        
        // Keep only recent history
        if (performanceHistory.size() > MAX_HISTORY) {
            performanceHistory.remove(0);
        }
    }
    
    /**
     * GET PERFORMANCE TREND
     * Analyze if performance is improving or degrading
     */
    public PerformanceTrend getPerformanceTrend() {
        if (performanceHistory.size() < 5) {
            return PerformanceTrend.INSUFFICIENT_DATA;
        }
        
        // Compare recent performance to earlier performance
    double recentCohesion = getRecentAverage(s -> s.getMetrics().getSwarmCohesion(), 5);
    double earlierCohesion = getRecentAverage(s -> s.getMetrics().getSwarmCohesion(), 10);
        
    double recentCompletion = getRecentAverage(s -> s.getMetrics().getTaskCompletionRate(), 5);
    double earlierCompletion = getRecentAverage(s -> s.getMetrics().getTaskCompletionRate(), 10);
        
        // Calculate trend
        double cohesionChange = recentCohesion - earlierCohesion;
        double completionChange = recentCompletion - earlierCompletion;
        
        if (cohesionChange > 0.05 || completionChange > 0.05) {
            return PerformanceTrend.IMPROVING;
        } else if (cohesionChange < -0.05 || completionChange < -0.05) {
            return PerformanceTrend.DEGRADING;
        } else {
            return PerformanceTrend.STABLE;
        }
    }
    
    /**
     * Get recent average of metric
     */
    private double getRecentAverage(
            java.util.function.Function<PerformanceSnapshot, Double> extractor, 
            int count) {
        int start = Math.max(0, performanceHistory.size() - count);
        double sum = 0;
        int samples = 0;
        
        for (int i = start; i < performanceHistory.size(); i++) {
            sum += extractor.apply(performanceHistory.get(i));
            samples++;
        }
        
        return samples > 0 ? sum / samples : 0.0;
    }
    
    // ==================== QUERY METHODS ====================
    
    public boolean isAdaptationEnabled() {
        return adaptationEnabled;
    }
    
    public int getFlockingAdjustments() {
        return flockingAdjustments;
    }
    
    public int getVotingAdjustments() {
        return votingAdjustments;
    }
    
    public int getTaskAdjustments() {
        return taskAdjustments;
    }
    
    public int getTotalAdjustments() {
        return flockingAdjustments + votingAdjustments + taskAdjustments + formationAdjustments;
    }
    
    public List<PerformanceSnapshot> getPerformanceHistory() {
        return new ArrayList<>(performanceHistory);
    }
    
    /**
     * RESET ADAPTATION
     */
    public void reset() {
        performanceHistory.clear();
        flockingAdjustments = 0;
        votingAdjustments = 0;
        taskAdjustments = 0;
        formationAdjustments = 0;
        System.out.println("Adaptive behavior reset");
    }
    
    @Override
    public String toString() {
        return String.format(
            "AdaptiveBehavior[Enabled: %s | Adjustments: %d | Trend: %s]",
            adaptationEnabled, getTotalAdjustments(), getPerformanceTrend()
        );
    }
}