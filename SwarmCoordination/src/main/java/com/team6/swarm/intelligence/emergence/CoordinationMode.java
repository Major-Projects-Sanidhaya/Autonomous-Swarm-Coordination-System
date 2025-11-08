package com.team6.swarm.intelligence.emergence;

/**
 * COORDINATIONMODE ENUM - Overall swarm behavior mode
 */
public enum CoordinationMode {
    AUTONOMOUS,         // Independent agent decisions
    FORMATION_STRICT,   // Strict formation maintenance
    EXPLORATION,        // Maximize area coverage
    EMERGENCY,          // Safety-first mode
    MISSION_FOCUSED     // Task completion priority
}
