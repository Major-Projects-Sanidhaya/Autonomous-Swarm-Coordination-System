package com.team6.swarm.intelligence.coordination;

/**
 * LEADERSELECTIONREASON ENUM - Why leader was chosen
 */
public enum LeaderSelectionReason {
    MANUAL,            // User designated
    HIGHEST_BATTERY,   // Most energy remaining
    CENTRAL_POSITION,  // Closest to swarm center
    BEST_SENSORS,      // Superior detection capabilities
    LOWEST_ID,         // Tie-breaker
    AUTO,              // Automatic selection (balanced criteria)
    SUCCESSION         // Previous leader failed
}