/**
 * MOVEMENTCOMMANDTYPE ENUM - Types of Movement Commands
 *
 * PURPOSE:
 * - Defines different types of movement instructions for agents
 * - Used to categorize and process movement commands
 * - Enables type-safe command handling
 * 
 * NEED TO REMOVE?
 */
package com.team6.swarm.intelligence.optimization;

public enum MovementCommandType {
    MOVE_TO_POSITION,    // Move to specific coordinate
    FOLLOW_PATH,         // Follow waypoint path
    FLOCKING_BEHAVIOR,   // Apply flocking forces
    FORMATION_HOLD,      // Maintain formation position
    AVOID_OBSTACLE,      // Emergency avoidance
    RETURN_TO_BASE      // Low battery return
}