package com.team6.swarm.intelligence.decisions;

/**
 * RESOLUTIONSTRATEGY ENUM - Conflict Resolution Approaches
 */
public enum ResolutionStrategy {
    FALLBACK_LEADER,    // Leader decides
    COMPROMISE,         // Find middle ground
    REVOTE,            // Vote again with modified options
    MULTI_STAGE,       // Break into smaller decisions
    HYBRID             // Combine strategies
}