package com.team6.swarm.intelligence.emergence;

/**
 * RESOLUTIONTYPE - How conflict was resolved
 */
public enum ResolutionType {
    EMERGENCY_OVERRIDE,    // Emergency took complete control
    PRIORITY_OVERRIDE,     // Highest priority won
    WEIGHTED_BLEND,        // Behaviors blended together
    SEQUENTIAL            // Queued for later
}