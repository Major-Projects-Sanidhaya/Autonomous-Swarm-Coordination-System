/**
 * DECISIONQUALITYMETRICS CLASS - Voting Performance Tracking
 *
 * PURPOSE:
 * - Track quality and speed of democratic decision making
 * - Measure consensus time, participation, and success rates
 * - Provide metrics for tuning voting parameters
 */
package com.team6.swarm.intelligence.optimization;

public class DecisionQualityMetrics {
    private int totalVotes = 0;
    private int successfulVotes = 0;
    private double avgConsensusTime = 0.0;
    private double avgParticipationRate = 0.0;
    private double successRate = 0.0;
    
    private int successfulOutcomes = 0;
    private int totalOutcomes = 0;
    
    public void recordVote(long durationMs, boolean consensusReached, int participants, int total) {
        totalVotes++;
        if (consensusReached) successfulVotes++;
        
        avgConsensusTime = (avgConsensusTime * (totalVotes - 1) + durationMs) / totalVotes;
        
        double participationRate = total > 0 ? (double) participants / total : 0.0;
        avgParticipationRate = (avgParticipationRate * (totalVotes - 1) + participationRate) / totalVotes;
        
        successRate = (double) successfulVotes / totalVotes;
    }
    
    public void recordOutcome(boolean successful) {
        totalOutcomes++;
        if (successful) successfulOutcomes++;
    }
    
    // Getters
    public int getTotalVotes() {
        return totalVotes;
    }
    
    public int getSuccessfulVotes() {
        return successfulVotes;
    }
    
    public double getAvgConsensusTime() {
        return avgConsensusTime;
    }
    
    public double getAvgParticipationRate() {
        return avgParticipationRate;
    }
    
    public double getSuccessRate() {
        return successRate;
    }
    
    public int getSuccessfulOutcomes() {
        return successfulOutcomes;
    }
    
    public int getTotalOutcomes() {
        return totalOutcomes;
    }
}