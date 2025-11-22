package com.team6.swarm.intelligence.decisions;

/**
 * DECISIONSTATUS ENUM - Decision Lifecycle State
 */
public enum DecisionStatus {
    PENDING,       // Decision created, awaiting evaluation
    EVALUATING,    // Currently being evaluated
    COMPLETED,     // Decision made
    CANCELLED      // Decision cancelled
}
