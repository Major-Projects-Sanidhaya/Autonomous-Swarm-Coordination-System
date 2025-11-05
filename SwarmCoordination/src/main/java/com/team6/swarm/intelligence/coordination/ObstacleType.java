package com.team6.swarm.intelligence.coordination;

/**
 * OBSTACLETYPE ENUM - Obstacle categories
 */
public enum ObstacleType {
    STATIC,      // Fixed barriers (walls, buildings)
    DYNAMIC,     // Moving obstacles (other vehicles)
    TEMPORARY,   // Temporary barriers (can be removed)
    HAZARD       // Dangerous areas (must avoid)
}