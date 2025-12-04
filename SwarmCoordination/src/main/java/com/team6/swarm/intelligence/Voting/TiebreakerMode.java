package com.team6.swarm.intelligence.voting;

/**
 * TIEBREAKERMODE ENUM - How to resolve tied votes
 */
public enum TiebreakerMode {
    LEADER_DECIDES,      // Leader breaks tie
    FIRST_OPTION,        // First option in list wins
    RANDOM_CHOICE,       // Random selection
    REVOTE,             // Vote again
    STATUS_QUO          // No change
}