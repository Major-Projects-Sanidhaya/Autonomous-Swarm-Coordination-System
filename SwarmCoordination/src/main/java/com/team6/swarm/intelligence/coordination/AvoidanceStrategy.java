package com.team6.swarm.intelligence.coordination;

/**
 * AVOIDANCESTRATEGY ENUM - How swarm handles obstacles
 */
public enum AvoidanceStrategy {
    INDIVIDUAL,      // Each agent avoids independently
    COLLECTIVE,      // Swarm votes on navigation
    LEADER_BASED,    // Follow leader's decisions
    PATHFINDING      // Use pre-computed paths
}