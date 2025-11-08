/**
 * BEHAVIORPRIORITY CLASS - Behavior Conflict Resolution System
 *
 * PURPOSE:
 * - Resolve conflicts when multiple behaviors want to control agent
 * - Implement priority hierarchy for agent decision making
 * - Blend compatible behaviors with appropriate weights
 * - Ensure safety-critical behaviors always take precedence
 *
 * PRIORITY HIERARCHY (Highest to Lowest):
 *
 * 1. EMERGENCY (Priority 100):
 *    - Collision avoidance - immediate threat response
 *    - EVADING behavior
 *    - Overrides ALL other behaviors
 *    - No blending, pure emergency response
 *    - Example: Agent about to collide with obstacle
 *
 * 2. CRITICAL (Priority 90):
 *    - Battery return to base
 *    - RETURNING behavior when battery < 20%
 *    - Overrides mission objectives
 *    - Slight blending with obstacle avoidance
 *    - Example: Low battery agent heading to charging station
 *
 * 3. HIGH (Priority 70):
 *    - Mission-critical tasks
 *    - TASK_EXECUTION behavior
 *    - Important but can pause for emergencies
 *    - Blends with flocking at edges
 *    - Example: Scout reporting critical information
 *
 * 4. NORMAL (Priority 30-60):
 *    - Formation maintenance (60)
 *    - Leader navigation (55)
 *    - Scout/Guard duties (50)
 *    - Standard operational behaviors
 *    - Full blending allowed
 *    - Example: Maintaining V-formation while moving
 *
 * 5. LOW (Priority 10):
 *    - Idle wandering
 *    - Exploratory movement
 *    - Easily overridden
 *    - Example: Agent with no assigned task
 *
 * CONFLICT RESOLUTION STRATEGIES:
 *
 * 1. OVERRIDE:
 *    - Higher priority completely replaces lower
 *    - Used for: Emergency vs anything
 *    - Result: Only highest priority executes
 *
 * 2. WEIGHTED BLEND:
 *    - Combine compatible behaviors with weights
 *    - Used for: Flocking + Formation
 *    - Result: Smooth combined movement
 *    - Formula: result = (b1 * w1 + b2 * w2) / (w1 + w2)
 *
 * 3. SEQUENTIAL:
 *    - Execute high priority, queue lower for later
 *    - Used for: Task + Flocking
 *    - Result: Task completes, then resume flocking
 *
 * BEHAVIOR COMPATIBILITY:
 *
 * Compatible (Can Blend):
 * - Flocking + Formation: Maintain formation while flocking
 * - Flocking + Leader: Follow leader with flocking forces
 * - Guard + Formation: Guard position within formation
 *
 * Incompatible (Cannot Blend):
 * - Emergency + Any: Emergency must be pure
 * - Task Execution + Formation: Task requires precise movement
 * - Returning + Scout: Can't scout while returning to base
 *
 * USAGE PATTERNS:
 *
 * Initialize:
 * BehaviorPriority resolver = new BehaviorPriority();
 *
 * Register Behaviors:
 * resolver.registerBehavior(BehaviorType.FLOCKING, 30, flockingCommand);
 * resolver.registerBehavior(BehaviorType.EVADING, 100, emergencyCommand);
 *
 * Resolve Conflicts:
 * MovementCommand finalCommand = resolver.resolveConflicts(agentId);
 *
 * Check Priority:
 * boolean isEmergency = resolver.isEmergencyActive(agentId);
 *
 * INTEGRATION POINTS:
 * - Used by: All intelligence controllers
 * - Coordinates: Multiple behavior systems
 * - Reports to: SwarmCoordinator for system state
 * - Integrates with: Agent command processing
 */
package com.team6.swarm.intelligence.emergence;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.flocking.BehaviorType;
import java.util.*;

public class BehaviorPriority {
    // Active behaviors per agent
    private final Map<Integer, List<BehaviorRequest>> agentBehaviors;
    
    // Conflict resolution history
    private final Map<Integer, List<ConflictResolution>> resolutionHistory;
    
    // Performance tracking
    private int totalConflicts;
    private int emergencyOverrides;
    private int successfulBlends;
    
    /**
     * Constructor
     */
    public BehaviorPriority() {
        this.agentBehaviors = new HashMap<>();
        this.resolutionHistory = new HashMap<>();
        this.totalConflicts = 0;
        this.emergencyOverrides = 0;
        this.successfulBlends = 0;
    }
    
    // ==================== BEHAVIOR REGISTRATION ====================
    
    /**
     * REGISTER BEHAVIOR
     * Agent wants to perform specific behavior
     */
    public void registerBehavior(int agentId, BehaviorType behaviorType, 
                                 int priority, MovementCommand command) {
        BehaviorRequest request = new BehaviorRequest(
            behaviorType, priority, command, System.currentTimeMillis());
        
        agentBehaviors.computeIfAbsent(agentId, k -> new ArrayList<>()).add(request);
    }
    
    /**
     * CLEAR BEHAVIORS
     * Remove all pending behaviors for agent
     */
    public void clearBehaviors(int agentId) {
        agentBehaviors.remove(agentId);
    }
    
    // ==================== CONFLICT RESOLUTION ====================
    
    /**
     * RESOLVE CONFLICTS
     * Determine which behavior(s) should execute
     *
     * @param agentId Agent with conflicting behaviors
     * @return Final movement command after resolution
     */
    public MovementCommand resolveConflicts(int agentId) {
        List<BehaviorRequest> behaviors = agentBehaviors.get(agentId);
        
        if (behaviors == null || behaviors.isEmpty()) {
            return null;  // No behaviors to resolve
        }
        
        // Single behavior - no conflict
        if (behaviors.size() == 1) {
            MovementCommand cmd = behaviors.get(0).command;
            clearBehaviors(agentId);
            return cmd;
        }
        
        // Multiple behaviors - resolve conflict
        totalConflicts++;
        
        // Sort by priority (highest first)
        behaviors.sort((b1, b2) -> Integer.compare(b2.priority, b1.priority));
        
        BehaviorRequest highest = behaviors.get(0);
        
        // Check if emergency override needed
        if (highest.priority >= 100) {
            emergencyOverrides++;
            logResolution(agentId, ResolutionType.EMERGENCY_OVERRIDE, 
                         highest.behaviorType, null);
            clearBehaviors(agentId);
            return highest.command;
        }
        
        // Check if blending possible
        if (canBlendBehaviors(behaviors)) {
            successfulBlends++;
            MovementCommand blended = blendBehaviors(agentId, behaviors);
            logResolution(agentId, ResolutionType.WEIGHTED_BLEND,
                         highest.behaviorType, getBehaviorTypes(behaviors));
            clearBehaviors(agentId);
            return blended;
        }
        
        // Default: highest priority wins
        logResolution(agentId, ResolutionType.PRIORITY_OVERRIDE,
                     highest.behaviorType, null);
        clearBehaviors(agentId);
        return highest.command;
    }
    
    /**
     * CHECK IF BEHAVIORS CAN BLEND
     * Determine if multiple behaviors are compatible
     */
    private boolean canBlendBehaviors(List<BehaviorRequest> behaviors) {
        if (behaviors.size() < 2) return false;
        
        // Get highest priority behavior
        BehaviorRequest highest = behaviors.get(0);
        
        // Emergency behaviors cannot blend
        if (highest.priority >= 100) return false;
        
        // Critical behaviors have limited blending
        if (highest.priority >= 90) {
            // RETURNING can blend slightly with obstacle avoidance
            return highest.behaviorType == BehaviorType.RETURNING &&
                   behaviors.stream().anyMatch(b -> b.behaviorType == BehaviorType.EVADING);
        }
        
        // Check compatibility matrix
        for (int i = 1; i < behaviors.size(); i++) {
            if (!areCompatible(highest.behaviorType, behaviors.get(i).behaviorType)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * CHECK BEHAVIOR COMPATIBILITY
     * Determine if two behaviors can work together
     */
    private boolean areCompatible(BehaviorType type1, BehaviorType type2) {
        // Same behavior type is always compatible
        if (type1 == type2) return true;
        
        // Define compatibility rules
        switch (type1) {
            case FLOCKING:
                // Flocking blends well with most behaviors
                return type2 == BehaviorType.FORMATION ||
                       type2 == BehaviorType.LEADER ||
                       type2 == BehaviorType.FOLLOWER ||
                       type2 == BehaviorType.SCOUT ||
                       type2 == BehaviorType.GUARD;
                
            case FORMATION:
                // Formation can blend with coordination behaviors
                return type2 == BehaviorType.FLOCKING ||
                       type2 == BehaviorType.LEADER ||
                       type2 == BehaviorType.FOLLOWER ||
                       type2 == BehaviorType.GUARD;
                
            case LEADER:
                // Leader can blend with flocking and formation
                return type2 == BehaviorType.FLOCKING ||
                       type2 == BehaviorType.FORMATION;
                
            case SCOUT:
                // Scout can maintain flocking
                return type2 == BehaviorType.FLOCKING;
                
            case GUARD:
                // Guard can maintain formation and flock
                return type2 == BehaviorType.FLOCKING ||
                       type2 == BehaviorType.FORMATION;
                
            case RETURNING:
                // Returning only blends with emergency avoidance
                return type2 == BehaviorType.EVADING;
                
            case TASK_EXECUTION:
            case EVADING:
            case FAILED:
            case IDLE:
                // These don't blend
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * BLEND BEHAVIORS
     * Combine compatible behaviors with weighted averaging
     */
    private MovementCommand blendBehaviors(int agentId, List<BehaviorRequest> behaviors) {
        MovementCommand blended = new MovementCommand();
        blended.agentId = agentId;
        blended.type = MovementType.FLOCKING_BEHAVIOR;  // Default type
        blended.timestamp = System.currentTimeMillis();
        
        // Calculate total priority weight
        double totalWeight = behaviors.stream()
            .mapToDouble(b -> b.priority)
            .sum();
        
        // Blend force vectors if available
        Vector2D blendedForce = new Vector2D(0, 0);
        Point2D blendedTarget = null;
        double blendedSpeed = 0;
        int forceBehaviors = 0;
        int targetBehaviors = 0;
        
        for (BehaviorRequest behavior : behaviors) {
            double weight = behavior.priority / totalWeight;
            
            // Blend force-based commands
            if (behavior.command.parameters.containsKey("combinedForce")) {
                Vector2D force = (Vector2D) behavior.command.parameters.get("combinedForce");
                blendedForce = new Vector2D(
                    blendedForce.x + force.x * weight,
                    blendedForce.y + force.y * weight
                );
                forceBehaviors++;
            }
            
            // Blend target-based commands
            if (behavior.command.parameters.containsKey("target")) {
                Point2D target = (Point2D) behavior.command.parameters.get("target");
                if (blendedTarget == null) {
                    blendedTarget = new Point2D(
                        target.x * weight,
                        target.y * weight
                    );
                } else {
                    blendedTarget = new Point2D(
                        blendedTarget.x + target.x * weight,
                        blendedTarget.y + target.y * weight
                    );
                }
                targetBehaviors++;
            }
            
            // Average speed (safely handle missing or non-Double values)
            Object speedObj = behavior.command.parameters.get("speed");
            if (speedObj instanceof Double) {
                blendedSpeed += (Double) speedObj * weight;
            } else if (speedObj instanceof Integer) {
                blendedSpeed += ((Integer) speedObj).doubleValue() * weight;
            }
        }
        
        // Set blended parameters
        if (forceBehaviors > 0) {
            blended.parameters.put("combinedForce", blendedForce);
        }
        if (targetBehaviors > 0) {
            blended.parameters.put("target", blendedTarget);
        }
        if (blendedSpeed > 0) {
            blended.parameters.put("speed", blendedSpeed);
        }
        
        // Set highest priority behavior type
        blended.type = getMovementTypeForBehavior(behaviors.get(0).behaviorType);
        
        return blended;
    }
    
    /**
     * Convert BehaviorType to MovementType
     */
    private MovementType getMovementTypeForBehavior(BehaviorType behaviorType) {
        switch (behaviorType) {
            case FLOCKING:
                return MovementType.FLOCKING_BEHAVIOR;
            case FORMATION:
            case FOLLOWER:
                return MovementType.FORMATION_POSITION;
            case EVADING:
                return MovementType.AVOID_OBSTACLE;
            default:
                return MovementType.MOVE_TO_TARGET;
        }
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * CHECK IF EMERGENCY ACTIVE
     * Determine if agent is in emergency mode
     */
    public boolean isEmergencyActive(int agentId) {
        List<BehaviorRequest> behaviors = agentBehaviors.get(agentId);
        if (behaviors == null) return false;
        
        return behaviors.stream()
            .anyMatch(b -> b.priority >= 100);
    }
    
    /**
     * GET HIGHEST PRIORITY BEHAVIOR
     */
    public BehaviorType getHighestPriorityBehavior(int agentId) {
        List<BehaviorRequest> behaviors = agentBehaviors.get(agentId);
        if (behaviors == null || behaviors.isEmpty()) {
            return BehaviorType.IDLE;
        }
        
        return behaviors.stream()
            .max(Comparator.comparingInt(b -> b.priority))
            .map(b -> b.behaviorType)
            .orElse(BehaviorType.IDLE);
    }
    
    /**
     * GET ACTIVE BEHAVIOR COUNT
     */
    public int getActiveBehaviorCount(int agentId) {
        List<BehaviorRequest> behaviors = agentBehaviors.get(agentId);
        return behaviors != null ? behaviors.size() : 0;
    }
    
    /**
     * GET ALL ACTIVE BEHAVIORS
     */
    public List<BehaviorType> getActiveBehaviors(int agentId) {
        List<BehaviorRequest> behaviors = agentBehaviors.get(agentId);
        if (behaviors == null) return new ArrayList<>();
        
        return behaviors.stream()
            .map(b -> b.behaviorType)
            .collect(java.util.stream.Collectors.toList());
    }
    
    // ==================== HISTORY & LOGGING ====================
    
    /**
     * LOG RESOLUTION
     * Record conflict resolution for analysis
     */
    private void logResolution(int agentId, ResolutionType type, 
                              BehaviorType winner, List<BehaviorType> others) {
        ConflictResolution resolution = new ConflictResolution(
            agentId, type, winner, others, System.currentTimeMillis());
        
        resolutionHistory.computeIfAbsent(agentId, k -> new ArrayList<>()).add(resolution);
        
        // Keep only last 50 resolutions per agent
        List<ConflictResolution> history = resolutionHistory.get(agentId);
        if (history.size() > 50) {
            history.remove(0);
        }
    }
    
    /**
     * GET RESOLUTION HISTORY
     */
    public List<ConflictResolution> getResolutionHistory(int agentId) {
        return new ArrayList<>(resolutionHistory.getOrDefault(agentId, new ArrayList<>()));
    }
    
    /**
     * Get list of behavior types from requests
     */
    private List<BehaviorType> getBehaviorTypes(List<BehaviorRequest> behaviors) {
        return behaviors.stream()
            .map(b -> b.behaviorType)
            .collect(java.util.stream.Collectors.toList());
    }
    
    // ==================== PERFORMANCE METRICS ====================
    
    public int getTotalConflicts() {
        return totalConflicts;
    }
    
    public int getEmergencyOverrides() {
        return emergencyOverrides;
    }
    
    public int getSuccessfulBlends() {
        return successfulBlends;
    }
    
    public double getBlendSuccessRate() {
        return totalConflicts > 0 ? (double) successfulBlends / totalConflicts : 0.0;
    }
    
    public void resetMetrics() {
        totalConflicts = 0;
        emergencyOverrides = 0;
        successfulBlends = 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "BehaviorPriority[Conflicts: %d | Emergencies: %d | Blends: %d | Blend Rate: %.1f%%]",
            totalConflicts, emergencyOverrides, successfulBlends,
            getBlendSuccessRate() * 100
        );
    }
}