package com.team6.swarm.intelligence.emergence;

/**
 * COORDINATORSTATUS - Status snapshot
 */
public class CoordinatorStatus {
    // Encapsulated fields to prevent external mutation; provide read-only accessors below.
    private CoordinationMode mode;
    private boolean active;
    private int updateCount;
    private double averageUpdateTime;
    private int emergencyCount;
    private int totalTasksAssigned;
    private int totalVotesProcessed;
    private int flockingCalculations;
    private int behaviorConflicts;
    
    @Override
    public String toString() {
        return String.format(
            "Status[Mode: %s | Tasks: %d | Votes: %d | Flocking: %d | Conflicts: %d]",
            mode, totalTasksAssigned, totalVotesProcessed, flockingCalculations, behaviorConflicts
        );
    }

    // ==================== READ-ONLY ACCESSORS ====================

    public CoordinationMode getMode() {
        return mode;
    }

    public boolean isActive() {
        return active;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public double getAverageUpdateTime() {
        return averageUpdateTime;
    }

    public int getEmergencyCount() {
        return emergencyCount;
    }

    public int getTotalTasksAssigned() {
        return totalTasksAssigned;
    }

    public int getTotalVotesProcessed() {
        return totalVotesProcessed;
    }

    public int getFlockingCalculations() {
        return flockingCalculations;
    }

    public int getBehaviorConflicts() {
        return behaviorConflicts;
    }

    // ==================== PACKAGE-PRIVATE MUTATORS ====================
    // Allow classes in the same package (e.g., SwarmCoordinator) to update status
    // while preventing external packages from mutating fields directly.

    void setMode(CoordinationMode mode) {
        this.mode = mode;
    }

    void setActive(boolean active) {
        this.active = active;
    }

    void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    void setAverageUpdateTime(double averageUpdateTime) {
        this.averageUpdateTime = averageUpdateTime;
    }

    void setEmergencyCount(int emergencyCount) {
        this.emergencyCount = emergencyCount;
    }

    void setTotalTasksAssigned(int totalTasksAssigned) {
        this.totalTasksAssigned = totalTasksAssigned;
    }

    void setTotalVotesProcessed(int totalVotesProcessed) {
        this.totalVotesProcessed = totalVotesProcessed;
    }

    void setFlockingCalculations(int flockingCalculations) {
        this.flockingCalculations = flockingCalculations;
    }

    void setBehaviorConflicts(int behaviorConflicts) {
        this.behaviorConflicts = behaviorConflicts;
    }
}
