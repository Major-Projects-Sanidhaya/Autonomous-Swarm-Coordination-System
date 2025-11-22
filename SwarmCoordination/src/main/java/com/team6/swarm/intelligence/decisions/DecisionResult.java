package com.team6.swarm.intelligence.decisions;

/**
 * DECISIONRESULT CLASS - Generic Decision Outcome
 */
abstract class DecisionResult {
    public String decisionId;
    public String selectedOption;
    public long timestamp;
    public DecisionType decisionType;
    
    public DecisionResult(String decisionId, String selectedOption, DecisionType type) {
        this.decisionId = decisionId;
        this.selectedOption = selectedOption;
        this.timestamp = System.currentTimeMillis();
        this.decisionType = type;
    }
}
