package com.team6.swarm.intelligence.tasking;

/**
 * AssignmentReason - Why a task was assigned to an agent.
 */
public enum AssignmentReason {
    NEAREST_AGENT,      // Closest to task location
    ROLE_MATCH,         // Has required capabilities
    LOAD_BALANCE,       // Least busy agent
    BATTERY_OPTIMAL,    // Best battery level
    MANUAL,             // User assigned
    REASSIGNMENT,       // Previous agent failed
    ONLY_AVAILABLE      // Only agent that can do it
}
