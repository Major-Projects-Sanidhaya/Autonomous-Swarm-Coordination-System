/**
 * MOVEMENTTYPE ENUM - Agent Movement Behaviors
 *
 * PURPOSE:
 * - Defines different types of movement commands for agents
 * - Enables behavior-specific movement logic and AI decisions
 * - Foundation for swarm coordination and individual agent actions
 *
 * MOVEMENT TYPES:
 * 1. MOVE_TO_TARGET - Direct movement toward specific coordinate
 * 2. FLOCKING_BEHAVIOR - Collective swarm movement with neighbors
 * 3. FORMATION_POSITION - Maintain specific position in formation
 * 4. AVOID_OBSTACLE - Reactive movement to avoid collisions
 *
 * LOGIC:
 * - Used in switch statements for movement command execution
 * - Determines which algorithm/behavior to apply
 * - Each type expects different parameters in MovementCommand
 *
 * EXPECTED PARAMETERS BY TYPE:
 * - MOVE_TO_TARGET: requires "target" (Point2D)
 * - FLOCKING_BEHAVIOR: requires "combinedForce" (Vector2D)
 * - FORMATION_POSITION: requires "formationPos" (Point2D), "leaderPos" (Point2D)
 * - AVOID_OBSTACLE: requires "obstacle" (Point2D), "avoidanceForce" (Vector2D)
 *
 * USAGE EXAMPLES:
 * - MovementCommand cmd = new MovementCommand();
 * - cmd.type = MovementType.MOVE_TO_TARGET;
 * - cmd.parameters.put("target", new Point2D(100, 200));
 *
 * BEHAVIOR PRIORITY:
 * 1. AVOID_OBSTACLE (highest - safety first)
 * 2. FORMATION_POSITION (maintain swarm structure)
 * 3. FLOCKING_BEHAVIOR (collective movement)
 * 4. MOVE_TO_TARGET (lowest - basic movement)
 */
package com.team6.swarm.core;

public enum MovementType {
    MOVE_TO_TARGET,
    FLOCKING_BEHAVIOR, 
    FORMATION_POSITION, 
    AVOID_OBSTACLE
}