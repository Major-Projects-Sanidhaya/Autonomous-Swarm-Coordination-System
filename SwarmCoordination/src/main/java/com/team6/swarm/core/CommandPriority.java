/**
 * COMMAND PRIORITY - Movement Command Priority Levels
 *
 * PURPOSE:
 * - Defines priority levels for movement commands in agent command queues
 * - Enables critical commands to override normal behavior
 * - Supports emergency responses and collision avoidance
 *
 * PRIORITY LEVELS (from highest to lowest):
 * 1. EMERGENCY - Collision avoidance, safety-critical maneuvers
 * 2. HIGH - User overrides, formation changes, obstacle avoidance
 * 3. NORMAL - Regular flocking behavior, routine movements
 * 4. LOW - Exploration, idle wandering
 *
 * USAGE PATTERN:
 * - Attached to MovementCommand instances
 * - Command queue processes higher priority commands first
 * - Emergency commands can interrupt current action
 * - Used by decision-making algorithms to determine command importance
 *
 * EXAMPLE SCENARIOS:
 * - EMERGENCY: "Agent about to collide, execute evasive maneuver NOW"
 * - HIGH: "Formation leader issued new waypoint"
 * - NORMAL: "Continue flocking with neighbors"
 * - LOW: "No task assigned, wander randomly"
 *
 * INTEGRATION POINTS:
 * - Used by: MovementCommand
 * - Processed by: Agent.processCommands()
 * - Set by: Decision-making algorithms, collision avoidance, user commands
 *
 * IMPLEMENTATION NOTE:
 * - Can be implemented as enum with ordinal() for comparison
 * - Command queue should use PriorityQueue instead of ConcurrentLinkedQueue
 * - Higher priority values should be processed first
 */
package com.team6.swarm.core;

public enum CommandPriority {
    LOW,
    NORMAL,
    HIGH,
    EMERGENCY
}
