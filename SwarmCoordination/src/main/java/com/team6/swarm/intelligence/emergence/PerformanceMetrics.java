package com.team6.swarm.intelligence.emergence;

/**
 * PERFORMANCEMETRICS - Current system performance
 */
public class PerformanceMetrics {
    private double collisionRate;        // 0.0 to 1.0
    private double swarmCohesion;        // 0.0 to 1.0
    private double movementJitter;       // 0.0 to 1.0
    private double voteCompletionRate;   // 0.0 to 1.0
    private long averageVoteTime;        // milliseconds
    private double taskCompletionRate;   // 0.0 to 1.0
    private boolean workloadBalanced;    // true/false

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

    // Copy constructor
    public PerformanceMetrics(PerformanceMetrics other) {
        this.collisionRate = other.collisionRate;
        this.swarmCohesion = other.swarmCohesion;
        this.movementJitter = other.movementJitter;
        this.voteCompletionRate = other.voteCompletionRate;
        this.averageVoteTime = other.averageVoteTime;
        this.taskCompletionRate = other.taskCompletionRate;
        this.workloadBalanced = other.workloadBalanced;
    }

    public double getCollisionRate() { return collisionRate; }
    public void setCollisionRate(double collisionRate) { this.collisionRate = collisionRate; }

    public double getSwarmCohesion() { return swarmCohesion; }
    public void setSwarmCohesion(double swarmCohesion) { this.swarmCohesion = swarmCohesion; }

    public double getMovementJitter() { return movementJitter; }
    public void setMovementJitter(double movementJitter) { this.movementJitter = movementJitter; }

    public double getVoteCompletionRate() { return voteCompletionRate; }
    public void setVoteCompletionRate(double voteCompletionRate) { this.voteCompletionRate = voteCompletionRate; }

    public long getAverageVoteTime() { return averageVoteTime; }
    public void setAverageVoteTime(long averageVoteTime) { this.averageVoteTime = averageVoteTime; }

    public double getTaskCompletionRate() { return taskCompletionRate; }
    public void setTaskCompletionRate(double taskCompletionRate) { this.taskCompletionRate = taskCompletionRate; }

    public boolean isWorkloadBalanced() { return workloadBalanced; }
    public void setWorkloadBalanced(boolean workloadBalanced) { this.workloadBalanced = workloadBalanced; }

    public PerformanceMetrics copy() { return new PerformanceMetrics(this); }
}
