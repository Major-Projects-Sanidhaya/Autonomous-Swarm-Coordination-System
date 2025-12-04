/**
 * FORMATIONROLE ENUM - Agent's Role Within Formation
 *
 * PURPOSE:
 * - Define specific positions/responsibilities within formation
 * - Enable role-based task assignment and behavior
 * - Support complex formation structures with hierarchy
 *
 * FORMATION ROLES:
 *
 * LEADER:
 * - Front/center position in formation
 * - Makes navigation decisions for formation
 * - First to encounter obstacles or threats
 * - Reference point for all other positions
 * - Typically highest capability or most experienced
 * - Responsibilities: Navigation, decision-making, threat detection
 *
 * WING:
 * - Side positions flanking the formation
 * - Provides wide sensor coverage
 * - Can break off for scouting or flanking
 * - Examples: Left wing, right wing in V-formation
 * - Responsibilities: Flank protection, wide-area sensing
 *
 * FOLLOWER:
 * - Following positions behind leader
 * - Maintains formation spacing and alignment
 * - Supports leader's decisions
 * - Examples: Second row in column formation
 * - Responsibilities: Formation maintenance, support
 *
 * PERIMETER:
 * - Outer edge positions
 * - First line of defense/detection
 * - Guards against external threats
 * - Examples: Outermost agents in circle formation
 * - Responsibilities: Perimeter security, early warning
 *
 * INTERIOR:
 * - Inner positions within formation
 * - Protected by perimeter agents
 * - Can focus on specialized tasks
 * - Examples: Center agents in grid formation
 * - Responsibilities: Protected tasks, internal coordination
 *
 * ROLE ASSIGNMENT PATTERNS:
 *
 * V-Formation:
 * - 1 LEADER at front point
 * - 2 WING agents on left/right sides
 * - Remaining as FOLLOWER behind
 *
 * Circle Formation:
 * - All agents are PERIMETER
 * - Or: PERIMETER on outside, INTERIOR in center
 *
 * Column Formation:
 * - 1 LEADER at front
 * - All others as FOLLOWER in line
 *
 * Grid Formation:
 * - Edge agents are PERIMETER
 * - Inner agents are INTERIOR
 * - Center agent could be LEADER
 *
 * USAGE PATTERNS:
 *
 * Assign Role to Agent:
 * agent.formationRole = FormationRole.LEADER;
 *
 * Role-Based Behavior:
 * if (agent.formationRole == FormationRole.LEADER) {
 *     makeNavigationDecision();
 * } else if (agent.formationRole == FormationRole.WING) {
 *     provideFlanking();
 * }
 *
 * Task Assignment by Role:
 * if (task.requiredRole == FormationRole.PERIMETER) {
 *     assignToPerimeterAgent(task);
 * }
 *
 * Formation Flexibility:
 * // Promote wing to leader if leader fails
 * if (leaderFailed && agent.formationRole == FormationRole.WING) {
 *     agent.formationRole = FormationRole.LEADER;
 * }
 *
 * INTEGRATION POINTS:
 * - Used by: FormationController for position assignment
 * - Used by: LeaderFollower for hierarchy management
 * - Used by: TaskAllocator for role-appropriate assignments
 * - Read by: Anthony's UI for role visualization
 */
package com.team6.swarm.intelligence.coordination;

public enum FormationRole {
    /**
     * Front/center position - navigation leader
     */
    LEADER,
    
    /**
     * Side positions - flanking and wide coverage
     */
    WING,
    
    /**
     * Following positions - support and maintenance
     */
    FOLLOWER,
    
    /**
     * Outer edge - perimeter defense
     */
    PERIMETER,
    
    /**
     * Inner positions - protected tasks
     */
    INTERIOR;
    
    /**
     * Check if role is a leadership position
     */
    public boolean isLeadershipRole() {
        return this == LEADER;
    }
    
    /**
     * Check if role is on formation edge
     */
    public boolean isEdgeRole() {
        return this == LEADER || this == WING || this == PERIMETER;
    }
    
    /**
     * Check if role is protected (interior)
     */
    public boolean isProtectedRole() {
        return this == INTERIOR;
    }
    
    /**
     * Check if role requires high awareness
     */
    public boolean requiresHighAwareness() {
        return this == LEADER || this == WING || this == PERIMETER;
    }
    
    /**
     * Get priority level for role (higher = more important)
     */
    public int getPriorityLevel() {
        switch (this) {
            case LEADER:
                return 10;      // Highest priority
            case WING:
                return 8;       // High priority
            case PERIMETER:
                return 7;       // High priority
            case FOLLOWER:
                return 5;       // Medium priority
            case INTERIOR:
                return 6;       // Medium-high priority
            default:
                return 5;
        }
    }
    
    /**
     * Get recommended sensor range multiplier for role
     */
    public double getSensorRangeMultiplier() {
        switch (this) {
            case LEADER:
                return 1.5;     // Leader needs extended range
            case WING:
                return 1.3;     // Wings need good coverage
            case PERIMETER:
                return 1.4;     // Perimeter needs early detection
            case FOLLOWER:
                return 1.0;     // Normal range
            case INTERIOR:
                return 0.8;     // Can use reduced range
            default:
                return 1.0;
        }
    }
    
    /**
     * Check if role can be promoted to leader
     */
    public boolean canPromoteToLeader() {
        return this == WING || this == FOLLOWER;
    }
    
    /**
     * Get next role in succession (for leader failure)
     */
    public FormationRole getSuccessionRole() {
        switch (this) {
            case WING:
                return LEADER;      // Wing promotes to leader
            case FOLLOWER:
                return WING;        // Follower promotes to wing
            case PERIMETER:
                return FOLLOWER;    // Perimeter moves to follower
            case INTERIOR:
                return FOLLOWER;    // Interior moves to follower
            default:
                return this;        // Leader stays leader
        }
    }
    
    /**
     * Get display color for UI
     */
    public String getDisplayColor() {
        switch (this) {
            case LEADER:
                return "#FFD700";    // Gold - leader
            case WING:
                return "#4A90E2";    // Blue - wing
            case FOLLOWER:
                return "#7ED321";    // Green - follower
            case PERIMETER:
                return "#F5A623";    // Orange - perimeter
            case INTERIOR:
                return "#9B9B9B";    // Gray - interior
            default:
                return "#000000";
        }
    }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        switch (this) {
            case LEADER:
                return "Leader";
            case WING:
                return "Wing";
            case FOLLOWER:
                return "Follower";
            case PERIMETER:
                return "Perimeter";
            case INTERIOR:
                return "Interior";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Get display icon for UI
     */
    public String getDisplayIcon() {
        switch (this) {
            case LEADER:
                return "★";         // Star for leader
            case WING:
                return "◄►";        // Arrows for wings
            case FOLLOWER:
                return "●";         // Dot for follower
            case PERIMETER:
                return "○";         // Circle for perimeter
            case INTERIOR:
                return "■";         // Square for interior
            default:
                return "?";
        }
    }
}