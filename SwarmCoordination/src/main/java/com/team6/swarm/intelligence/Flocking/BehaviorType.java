/**
 * BEHAVIORTYPE ENUM - Agent Role and Behavior Classification
 *
 * PURPOSE:
 * - Defines all possible behaviors an agent can exhibit
 * - Enables role-based task assignment and visualization
 * - Supports dynamic behavior switching during missions
 *
 * BEHAVIOR CATEGORIES:
 *
 * COORDINATION BEHAVIORS:
 * - FLOCKING: Standard swarm movement (separation, alignment, cohesion)
 * - FORMATION: Maintaining specific geometric position
 * - LEADER: Leading the swarm, making navigation decisions
 * - FOLLOWER: Following leader or formation
 *
 * MISSION BEHAVIORS:
 * - SCOUT: Exploring ahead of main swarm
 * - GUARD: Protecting formation perimeter or specific location
 * - TASK_EXECUTION: Performing assigned work (patrol, search, etc.)
 * - IDLE: Available for assignment
 *
 * EMERGENCY BEHAVIORS:
 * - RETURNING: Low battery, returning to base
 * - EVADING: Emergency obstacle or threat avoidance
 * - FAILED: Agent malfunction, needs assistance
 *
 * BEHAVIOR CHARACTERISTICS:
 *
 * FLOCKING:
 * - Most common default behavior
 * - Enables beautiful coordinated movement
 * - Blends well with other behaviors
 * - Low computational cost
 *
 * LEADER:
 * - One agent per swarm typically
 * - Makes path decisions for group
 * - Others follow leader's position
 * - Rotation possible if leader fails
 *
 * SCOUT:
 * - Moves ahead of main swarm
 * - Explores unknown areas
 * - Reports back information
 * - Higher speed, less cohesion
 *
 * GUARD:
 * - Maintains perimeter position
 * - Watches for threats
 * - May have defensive maneuvers
 * - Lower speed, high awareness
 *
 * TASK_EXECUTION:
 * - Generic work execution mode
 * - Behavior depends on specific task
 * - May temporarily disable flocking
 * - Returns to FLOCKING when complete
 *
 * FORMATION:
 * - Precise position maintenance
 * - Overrides flocking forces
 * - Critical for coordinated maneuvers
 * - Requires active correction
 *
 * RETURNING:
 * - Emergency battery conservation
 * - Direct path to charging station
 * - Ignores most other behaviors
 * - High priority override
 *
 * EVADING:
 * - Highest priority behavior
 * - Immediate threat response
 * - Overrides all other behaviors
 * - Maximum speed and maneuverability
 *
 * USAGE PATTERNS:
 * 
 * Normal Operation:
 * Most agents in FLOCKING
 * 1-2 agents in SCOUT
 * 2-3 agents in GUARD
 *
 * Formation Flying:
 * All agents in FORMATION
 * Leader still designated
 * Precise coordination required
 *
 * Emergency:
 * EVADING overrides everything
 * RETURNING takes priority
 * Others maintain mission
 *
 * INTEGRATION POINTS:
 * - Used by: TaskAllocator for role assignment
 * - Used by: Anthony's UI for color-coding agents
 * - Used by: BehaviorPriority for conflict resolution
 * - Read by: All intelligence controllers
 *
 * EXPECTED USAGE:
 * - AgentState stores current behavior
 * - Behaviors can change dynamically
 * - Multiple behaviors can blend (FLOCKING + SCOUT)
 * - Priority system resolves conflicts
 */
package com.team6.swarm.intelligence.flocking;

public enum BehaviorType {
    /**
     * Standard coordinated swarm movement
     * Default behavior for most agents
     */
    FLOCKING,
    
    /**
     * Leading the swarm
     * Makes navigation decisions for group
     */
    LEADER,
    
    /**
     * Exploring ahead of main swarm
     * Higher speed, reports back information
     */
    SCOUT,
    
    /**
     * Protecting formation or location
     * Perimeter watch, lower mobility
     */
    GUARD,
    
    /**
     * Performing assigned task
     * Generic work execution mode
     */
    TASK_EXECUTION,
    
    /**
     * Maintaining specific position in formation
     * Precise geometric coordination
     */
    FORMATION,
    
    /**
     * Following leader or another agent
     * Position relative to target
     */
    FOLLOWER,
    
    /**
     * Available for task assignment
     * Minimal movement, low energy use
     */
    IDLE,
    
    /**
     * Low battery, returning to base
     * High priority emergency behavior
     */
    RETURNING,
    
    /**
     * Emergency threat avoidance
     * Highest priority, maximum speed
     */
    EVADING,
    
    /**
     * Agent malfunction or failure
     * Needs assistance or removal
     */
    FAILED;
    
    /**
     * Check if this is a coordination behavior
     */
    public boolean isCoordinationBehavior() {
        return this == FLOCKING || this == FORMATION || 
              this == LEADER || this == FOLLOWER;
    }
    
    /**
     * Check if this is a mission behavior
     */
    public boolean isMissionBehavior() {
        return this == SCOUT || this == GUARD || 
              this == TASK_EXECUTION || this == IDLE;
    }
    
    /**
     * Check if this is an emergency behavior
     */
    public boolean isEmergencyBehavior() {
        return this == RETURNING || this == EVADING || this == FAILED;
    }
    
    /**
     * Get priority level for behavior conflict resolution
     * Higher number = higher priority
     */
    public int getPriority() {
        switch (this) {
            case EVADING:
                return 100;  // Highest - immediate collision/threat avoidance
            case RETURNING:
                return 90;   // Critical - battery emergency
            case FAILED:
                return 85;   // Critical - malfunction
            case TASK_EXECUTION:
                return 70;   // High - mission critical
            case FORMATION:
                return 60;   // High - coordinated maneuver
            case LEADER:
                return 55;   // High - navigation responsibility
            case SCOUT:
                return 50;   // Medium - exploration
            case GUARD:
                return 50;   // Medium - protection
            case FOLLOWER:
                return 40;   // Medium - following
            case FLOCKING:
                return 30;   // Normal - default behavior
            case IDLE:
                return 10;   // Low - waiting for assignment
            default:
                return 0;
        }
    }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        switch (this) {
            case FLOCKING: return "Flocking";
            case LEADER: return "Leader";
            case SCOUT: return "Scout";
            case GUARD: return "Guard";
            case TASK_EXECUTION: return "Task Execution";
            case FORMATION: return "Formation";
            case FOLLOWER: return "Follower";
            case IDLE: return "Idle";
            case RETURNING: return "Returning to Base";
            case EVADING: return "Evading";
            case FAILED: return "Failed";
            default: return "Unknown";
        }
    }
    
    /**
     * Get suggested color for visualization
     * Returns hex color code
     */
    public String getVisualizationColor() {
        switch (this) {
            case FLOCKING: return "#4A90E2";    // Blue - calm movement
            case LEADER: return "#F5A623";      // Orange - leader
            case SCOUT: return "#7ED321";       // Green - exploration
            case GUARD: return "#BD10E0";       // Purple - protection
            case TASK_EXECUTION: return "#50E3C2"; // Cyan - working
            case FORMATION: return "#4A4A4A";   // Gray - formation
            case FOLLOWER: return "#9013FE";    // Violet - following
            case IDLE: return "#D8D8D8";        // Light gray - idle
            case RETURNING: return "#F8E71C";   // Yellow - low battery
            case EVADING: return "#D0021B";     // Red - danger
            case FAILED: return "#8B572A";      // Brown - failure
            default: return "#000000";          // Black - unknown
        }
    }
}