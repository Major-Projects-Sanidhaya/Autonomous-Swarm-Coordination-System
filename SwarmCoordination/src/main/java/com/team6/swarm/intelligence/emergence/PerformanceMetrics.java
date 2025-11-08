package com.team6.swarm.intelligence.emergence;

/**
 * PERFORMANCEMETRICS - Current system performance
 */
public class PerformanceMetrics {
    public double collisionRate;        // 0.0 to 1.0
    public double swarmCohesion;        // 0.0 to 1.0
    public double movementJitter;       // 0.0 to 1.0
    public double voteCompletionRate;   // 0.0 to 1.0
    public long averageVoteTime;        // milliseconds
    public double taskCompletionRate;   // 0.0 to 1.0
    public boolean workloadBalanced;    // true/false
    
    public PerformanceMetrics() {
        // Default values
        this.collisionRate = 0.0;
        this.swarmCohesion = 0.8;
        this.movementJitter = 0.1;
        this.voteCompletionRate = 1.0;
        this.averageVoteTime = 5000;
        this.taskCompletionRate = 0.9;
        this.workloadBalanced = true;
    }
}
