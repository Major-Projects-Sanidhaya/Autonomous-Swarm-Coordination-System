package com.team6.swarm.intelligence.decisions;

/**
 * RESOLUTIONRESULT CLASS - Outcome of Conflict Resolution
 */
public class ResolutionResult {
    public String proposalId;
    public boolean resolved;
    public String chosenOption;
    public ResolutionStrategy strategyUsed;
    public String explanation;
    public long timestamp;
    
    public ResolutionResult(String proposalId, boolean resolved, String chosenOption,
                            ResolutionStrategy strategyUsed, String explanation) {
        this.proposalId = proposalId;
        this.resolved = resolved;
        this.chosenOption = chosenOption;
        this.strategyUsed = strategyUsed;
        this.explanation = explanation;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return String.format(
            "ResolutionResult[%s: %s | Strategy: %s | Resolved: %s]",
            proposalId, chosenOption, strategyUsed, resolved
        );
    }
}
