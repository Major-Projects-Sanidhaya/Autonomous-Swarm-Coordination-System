package com.team6.swarm.intelligence.voting;

/**
 * TIMEOUTFALLBACK ENUM - What to do if vote times out
 */
public enum TimeoutFallback {
    LEADER_DECIDES,      // Leader makes decision
    STATUS_QUO,          // Keep current state
    FAIL_SAFE,          // Choose safest option
    RANDOM_CHOICE,       // Random selection
    REVOTE              // Try voting again
}