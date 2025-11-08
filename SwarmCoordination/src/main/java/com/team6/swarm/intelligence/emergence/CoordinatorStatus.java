package com.team6.swarm.intelligence.emergence;

/**
 * COORDINATORSTATUS - Status snapshot
 */
public class CoordinatorStatus {
    public CoordinationMode mode;
    public boolean active;
    public int updateCount;
    public double averageUpdateTime;
    public int emergencyCount;
    public int totalTasksAssigned;
    public int totalVotesProcessed;
    public int flockingCalculations;
    public int behaviorConflicts;
    
    @Override
    public String toString() {
        return String.format(
            "Status[Mode: %s | Tasks: %d | Votes: %d | Flocking: %d | Conflicts: %d]",
            mode, totalTasksAssigned, totalVotesProcessed, flockingCalculations, behaviorConflicts
        );
    }
}
