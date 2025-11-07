package com.team6.swarm.intelligence.tasking;

/**
 * TASKTYPE ENUM - Categories of work
 */
public enum TaskType {
    MOVE_TO_WAYPOINT,    // Simple navigation
    PATROL_AREA,         // Area monitoring
    SEARCH_PATTERN,      // Systematic search
    MAINTAIN_FORMATION,  // Formation flying
    FOLLOW_LEADER,       // Follow another agent
    GUARD_POSITION,      // Static guard duty
    SCOUT_AHEAD          // Forward exploration
}