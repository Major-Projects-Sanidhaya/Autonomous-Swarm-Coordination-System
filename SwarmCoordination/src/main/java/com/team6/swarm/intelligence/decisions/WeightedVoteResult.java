package com.team6.swarm.intelligence.decisions;
import java.util.HashMap;
import java.util.Map;

/**
 * WEIGHTEDVOTERESULT CLASS - Outcome of Weighted Voting
 */
public class WeightedVoteResult {
    public String proposalId;
    public boolean consensusReached;
    public String winningOption;
    public double consensusLevel;
    public Map<String, Double> weightedVotes;  // option -> total weight
    public Map<Integer, Double> agentWeights;  // agentId -> weight
    public long timestamp;
    
    public WeightedVoteResult(String proposalId, boolean consensusReached,
                              String winningOption, double consensusLevel,
                              Map<String, Double> weightedVotes,
                              Map<Integer, Double> agentWeights) {
        this.proposalId = proposalId;
        this.consensusReached = consensusReached;
        this.winningOption = winningOption;
        this.consensusLevel = consensusLevel;
        this.weightedVotes = new HashMap<>(weightedVotes);
        this.agentWeights = new HashMap<>(agentWeights);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get total weight cast
     */
    public double getTotalWeight() {
        return weightedVotes.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Get weight for specific option
     */
    public double getWeightForOption(String option) {
        return weightedVotes.getOrDefault(option, 0.0);
    }
    
    @Override
    public String toString() {
        return String.format(
            "WeightedVoteResult[%s: %s wins | Consensus: %.1f%% | Total weight: %.2f]",
            proposalId, winningOption, consensusLevel * 100, getTotalWeight()
        );
    }
}
