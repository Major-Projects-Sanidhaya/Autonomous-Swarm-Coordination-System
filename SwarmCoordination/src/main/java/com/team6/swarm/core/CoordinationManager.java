/**
 * COORDINATIONMANAGER CLASS - Multi-Agent Coordination System (Week 7-8)
 *
 * PURPOSE:
 * - Manages multi-agent coordination for complex group behaviors
 * - Synchronizes agent actions for formation changes and group movements
 * - Coordinates transitions between different formations
 * - Handles timing and sequencing of coordinated maneuvers
 *
 * WEEK 7-8 REQUIREMENTS:
 * - Complex maneuvers require coordination between agents
 * - Coordinates formation changes and group movements
 * - Synchronize agent actions for group behaviors
 *
 * CORE RESPONSIBILITIES:
 * 1. Formation Management:
 *    - Create and maintain formations
 *    - Coordinate formation transitions
 *    - Synchronize agent movements within formations
 *
 * 2. Group Movement:
 *    - Move entire formations as cohesive units
 *    - Coordinate velocity and heading changes
 *    - Maintain formation integrity during movement
 *
 * 3. Transition Coordination:
 *    - Smooth transitions between different formation types
 *    - Timing synchronization for simultaneous actions
 *    - Collision avoidance during transitions
 *
 * 4. Maneuver Execution:
 *    - Split and merge operations
 *    - Rotation and scaling of formations
 *    - Complex multi-phase maneuvers
 *
 * KEY FEATURES:
 * - Formation lifecycle management (create, update, dissolve)
 * - Coordinated movement commands to all agents in formation
 * - Transition planning and execution
 * - Real-time position adjustment for formation maintenance
 * - Integration with Lauren's intelligence system
 * - Event publishing for status updates
 *
 * INTEGRATION POINTS:
 * - Formation.java: Uses formation definitions and position calculations
 * - Agent.java: Sends coordinated movement commands
 * - AgentManager.java: Accesses agent registry
 * - EventBus.java: Publishes coordination events
 * - Lauren's Intelligence: Receives formation requests
 * - Anthony's UI: Provides visualization data
 *
 * USAGE PATTERN:
 * 1. Create formation:
 *    int formationId = coordManager.createFormation(FormationType.WEDGE, agentIds, center, spacing);
 *
 * 2. Move formation:
 *    coordManager.moveFormation(formationId, targetPosition);
 *
 * 3. Change formation type:
 *    coordManager.transitionFormation(formationId, FormationType.CIRCLE);
 *
 * 4. Dissolve formation:
 *    coordManager.dissolveFormation(formationId);
 *
 * COORDINATION ALGORITHMS:
 * - Position Assignment: Calculates target positions for all agents
 * - Movement Synchronization: Ensures agents reach positions simultaneously
 * - Collision Prevention: Avoids inter-agent collisions during transitions
 * - Speed Adjustment: Matches velocities for cohesive movement
 */
package com.team6.swarm.core;

import com.team6.swarm.intelligence.formation.Formation;
import com.team6.swarm.intelligence.formation.FormationType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoordinationManager {
    // Active formations registry
    private Map<Integer, Formation> activeFormations;
    private int nextFormationId;

    // Reference to agent manager for coordination
    private AgentManager agentManager;

    // Event bus for publishing coordination events
    private EventBus eventBus;

    // Transition tracking
    private Map<Integer, FormationTransition> activeTransitions;

    // Configuration
    private static final double POSITION_TOLERANCE = 5.0;

    /**
     * Constructor with dependencies
     */
    public CoordinationManager(AgentManager agentManager, EventBus eventBus) {
        this.agentManager = agentManager;
        this.eventBus = eventBus;
        this.activeFormations = new ConcurrentHashMap<>();
        this.activeTransitions = new ConcurrentHashMap<>();
        this.nextFormationId = 1;
    }

    /**
     * Default constructor
     */
    public CoordinationManager() {
        this(null, null);
    }

    // ==================== FORMATION MANAGEMENT ====================

    /**
     * Create new formation with specified agents
     *
     * @param formationType Type of formation (LINE, WEDGE, CIRCLE, etc.)
     * @param agentIds List of agent IDs to include in formation
     * @param centerPoint Center point of formation
     * @param spacing Distance between agents
     * @return Formation ID for future reference
     */
    public int createFormation(FormationType formationType, List<Integer> agentIds,
                              Point2D centerPoint, double spacing) {
        if (agentIds == null || agentIds.isEmpty()) {
            System.err.println("CoordinationManager: Cannot create formation with no agents");
            return -1;
        }

        // Validate minimum agents for formation type
        if (agentIds.size() < formationType.getMinimumAgents()) {
            System.err.println("CoordinationManager: Not enough agents for " +
                             formationType.getDisplayName() +
                             " (need " + formationType.getMinimumAgents() + ", got " + agentIds.size() + ")");
            return -1;
        }

        // Calculate initial heading based on agents' average velocity
        double heading = calculateAverageHeading(agentIds);

        // Create formation
        Formation formation = new Formation(formationType, centerPoint, spacing, heading, agentIds);
        int formationId = nextFormationId++;
        activeFormations.put(formationId, formation);

        // Send initial movement commands to agents
        assignFormationPositions(formation);

        // Publish formation creation event
        publishFormationEvent("FORMATION_CREATED", formationId, formation);

        System.out.println("CoordinationManager: Created " + formationType.getDisplayName() +
                         " (ID: " + formationId + ") with " + agentIds.size() + " agents");

        return formationId;
    }

    /**
     * Create formation with default spacing
     */
    public int createFormation(FormationType formationType, List<Integer> agentIds,
                              Point2D centerPoint) {
        double spacing = formationType.getRecommendedSpacing(agentIds.size());
        return createFormation(formationType, agentIds, centerPoint, spacing);
    }

    /**
     * Dissolve formation - agents become independent
     *
     * @param formationId Formation to dissolve
     */
    public void dissolveFormation(int formationId) {
        Formation formation = activeFormations.remove(formationId);

        if (formation != null) {
            // Cancel any active transitions
            activeTransitions.remove(formationId);

            // Publish dissolution event
            publishFormationEvent("FORMATION_DISSOLVED", formationId, formation);

            System.out.println("CoordinationManager: Dissolved formation " + formationId);
        }
    }

    /**
     * Get formation by ID
     */
    public Formation getFormation(int formationId) {
        return activeFormations.get(formationId);
    }

    /**
     * Get all active formations
     */
    public List<Formation> getAllFormations() {
        return new ArrayList<>(activeFormations.values());
    }

    // ==================== GROUP MOVEMENT ====================

    /**
     * Move entire formation to new center point
     * Maintains formation shape and relative positions
     *
     * @param formationId Formation to move
     * @param targetCenter New center point
     */
    public void moveFormation(int formationId, Point2D targetCenter) {
        Formation formation = activeFormations.get(formationId);

        if (formation == null) {
            System.err.println("CoordinationManager: Formation " + formationId + " not found");
            return;
        }

        // Update formation center
        formation.moveTo(targetCenter);
        formation.isMoving = true;
        formation.targetLocation = targetCenter;

        // Send movement commands to all agents
        assignFormationPositions(formation);

        // Publish movement event
        publishFormationEvent("FORMATION_MOVING", formationId, formation);

        System.out.println("CoordinationManager: Moving formation " + formationId +
                         " to " + targetCenter);
    }

    /**
     * Rotate formation to new heading
     *
     * @param formationId Formation to rotate
     * @param newHeading New heading in radians (0 = East)
     */
    public void rotateFormation(int formationId, double newHeading) {
        Formation formation = activeFormations.get(formationId);

        if (formation == null) {
            System.err.println("CoordinationManager: Formation " + formationId + " not found");
            return;
        }

        // Update formation heading
        formation.setHeading(newHeading);

        // Send updated position commands
        assignFormationPositions(formation);

        // Publish rotation event
        publishFormationEvent("FORMATION_ROTATED", formationId, formation);

        System.out.println("CoordinationManager: Rotated formation " + formationId +
                         " to " + Math.toDegrees(newHeading) + "°");
    }

    /**
     * Change formation spacing (expand/contract)
     *
     * @param formationId Formation to modify
     * @param newSpacing New spacing between agents
     */
    public void setFormationSpacing(int formationId, double newSpacing) {
        Formation formation = activeFormations.get(formationId);

        if (formation == null) {
            System.err.println("CoordinationManager: Formation " + formationId + " not found");
            return;
        }

        // Update spacing
        formation.setSpacing(newSpacing);

        // Send updated position commands
        assignFormationPositions(formation);

        System.out.println("CoordinationManager: Changed formation " + formationId +
                         " spacing to " + newSpacing);
    }

    // ==================== FORMATION TRANSITIONS ====================

    /**
     * Transition formation from one type to another
     * Coordinates smooth transformation with collision avoidance
     *
     * @param formationId Formation to transition
     * @param newFormationType Target formation type
     */
    public void transitionFormation(int formationId, FormationType newFormationType) {
        Formation formation = activeFormations.get(formationId);

        if (formation == null) {
            System.err.println("CoordinationManager: Formation " + formationId + " not found");
            return;
        }

        // Check if enough agents for new formation
        if (formation.participatingAgents.size() < newFormationType.getMinimumAgents()) {
            System.err.println("CoordinationManager: Not enough agents for " +
                             newFormationType.getDisplayName());
            return;
        }

        // Create transition plan
        FormationTransition transition = new FormationTransition();
        transition.formationId = formationId;
        transition.oldFormationType = formation.formationType;
        transition.newFormationType = newFormationType;
        transition.startTime = System.currentTimeMillis();
        transition.progress = 0.0;

        activeTransitions.put(formationId, transition);

        // Update formation type
        FormationType oldType = formation.formationType;
        formation.formationType = newFormationType;

        // Adjust spacing if needed
        double recommendedSpacing = newFormationType.getRecommendedSpacing(
            formation.participatingAgents.size());
        formation.setSpacing(recommendedSpacing);

        // Calculate new positions and send commands
        assignFormationPositions(formation);

        // Publish transition event
        publishFormationEvent("FORMATION_TRANSITION", formationId, formation);

        System.out.println("CoordinationManager: Transitioning formation " + formationId +
                         " from " + oldType.getDisplayName() +
                         " to " + newFormationType.getDisplayName());
    }

    /**
     * Add agent to existing formation
     * Recalculates positions to accommodate new agent
     *
     * @param formationId Formation to join
     * @param agentId Agent to add
     */
    public void addAgentToFormation(int formationId, int agentId) {
        Formation formation = activeFormations.get(formationId);

        if (formation == null) {
            System.err.println("CoordinationManager: Formation " + formationId + " not found");
            return;
        }

        if (formation.hasAgent(agentId)) {
            System.out.println("CoordinationManager: Agent " + agentId +
                             " already in formation " + formationId);
            return;
        }

        // Add agent and recalculate positions
        formation.addAgent(agentId);

        // Send updated position commands to all agents
        assignFormationPositions(formation);

        System.out.println("CoordinationManager: Added agent " + agentId +
                         " to formation " + formationId);
    }

    /**
     * Remove agent from formation
     * Recalculates positions for remaining agents
     *
     * @param formationId Formation to leave
     * @param agentId Agent to remove
     */
    public void removeAgentFromFormation(int formationId, int agentId) {
        Formation formation = activeFormations.get(formationId);

        if (formation == null) {
            System.err.println("CoordinationManager: Formation " + formationId + " not found");
            return;
        }

        if (!formation.hasAgent(agentId)) {
            System.out.println("CoordinationManager: Agent " + agentId +
                             " not in formation " + formationId);
            return;
        }

        // Remove agent and recalculate
        formation.removeAgent(agentId);

        // Check if formation still viable
        if (formation.getAgentCount() < formation.formationType.getMinimumAgents()) {
            System.out.println("CoordinationManager: Formation " + formationId +
                             " no longer viable, dissolving");
            dissolveFormation(formationId);
            return;
        }

        // Send updated position commands
        assignFormationPositions(formation);

        System.out.println("CoordinationManager: Removed agent " + agentId +
                         " from formation " + formationId);
    }

    // ==================== COORDINATION LOGIC ====================

    /**
     * Assign formation positions to all agents
     * Sends FORMATION_POSITION movement commands
     */
    private void assignFormationPositions(Formation formation) {
        if (agentManager == null) {
            System.out.println("CoordinationManager: AgentManager not set, cannot assign positions");
            return;
        }

        for (int agentId : formation.participatingAgents) {
            Agent agent = agentManager.getAgent(agentId);
            if (agent == null) {
                System.err.println("CoordinationManager: Agent " + agentId + " not found");
                continue;
            }

            // Get target position for this agent
            Point2D targetPosition = formation.getAgentPosition(agentId);
            if (targetPosition == null) {
                System.err.println("CoordinationManager: No position for agent " + agentId);
                continue;
            }

            // Create movement command
            MovementCommand cmd = new MovementCommand(agentId, MovementType.FORMATION_POSITION, CommandPriority.HIGH);
            cmd.parameters.put("formationPos", targetPosition);
            cmd.parameters.put("formationId", formation.formationId);
            cmd.taskId = "formation_" + formation.formationId + "_agent_" + agentId;

            // Send command to agent
            agent.addMovementCommand(cmd);
        }
    }

    /**
     * Calculate average heading of agents
     * Used for initial formation orientation
     */
    private double calculateAverageHeading(List<Integer> agentIds) {
        if (agentManager == null || agentIds.isEmpty()) {
            return 0.0;  // Default to East
        }

        double sumX = 0.0;
        double sumY = 0.0;
        int count = 0;

        for (int agentId : agentIds) {
            Agent agent = agentManager.getAgent(agentId);
            if (agent != null) {
                Vector2D velocity = agent.getState().velocity;
                if (velocity.magnitude() > 0.1) {  // Only consider moving agents
                    sumX += velocity.x;
                    sumY += velocity.y;
                    count++;
                }
            }
        }

        if (count > 0) {
            return Math.atan2(sumY, sumX);
        } else {
            return 0.0;  // Default heading
        }
    }

    /**
     * Check if formation has reached target position
     * Returns true if all agents are within tolerance
     */
    public boolean isFormationInPosition(int formationId) {
        Formation formation = activeFormations.get(formationId);

        if (formation == null || agentManager == null) {
            return false;
        }

        // Check each agent's distance to target
        for (int agentId : formation.participatingAgents) {
            Agent agent = agentManager.getAgent(agentId);
            if (agent == null) continue;

            Point2D targetPos = formation.getAgentPosition(agentId);
            if (targetPos == null) continue;

            Point2D currentPos = agent.getState().position;
            double distance = currentPos.distanceTo(targetPos);

            if (distance > POSITION_TOLERANCE) {
                return false;  // At least one agent not in position
            }
        }

        return true;  // All agents in position
    }

    /**
     * Update coordination state
     * Called periodically to check transition progress
     */
    public void update(double deltaTime) {
        // Check transition progress
        List<Integer> completedTransitions = new ArrayList<>();

        for (Map.Entry<Integer, FormationTransition> entry : activeTransitions.entrySet()) {
            int formationId = entry.getKey();
            FormationTransition transition = entry.getValue();

            if (isFormationInPosition(formationId)) {
                completedTransitions.add(formationId);
                System.out.println("CoordinationManager: Formation " + formationId +
                                 " completed transition to " +
                                 transition.newFormationType.getDisplayName());
            } else {
                // Update progress based on time
                long elapsed = System.currentTimeMillis() - transition.startTime;
                transition.progress = Math.min(1.0, elapsed / 5000.0);  // 5 second transition
            }
        }

        // Remove completed transitions
        for (int formationId : completedTransitions) {
            activeTransitions.remove(formationId);
        }
    }

    // ==================== EVENT PUBLISHING ====================

    /**
     * Publish formation event to EventBus
     */
    private void publishFormationEvent(String eventType, int formationId, Formation formation) {
        if (eventBus == null) return;

        String message = "Formation " + formationId + ": " + formation.formationType.getDisplayName() +
                        " with " + formation.getAgentCount() + " agents";

        SystemEvent event = SystemEvent.info(eventType, message);
        event.addMetadata("formationId", formationId);
        event.addMetadata("formationType", formation.formationType.toString());
        event.addMetadata("agentCount", formation.getAgentCount());
        event.addMetadata("sourceComponent", "CoordinationManager");

        eventBus.publish(event);
    }

    // ==================== SETTERS ====================

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get number of active formations
     */
    public int getActiveFormationCount() {
        return activeFormations.size();
    }

    /**
     * Get all formation IDs
     */
    public List<Integer> getFormationIds() {
        return new ArrayList<>(activeFormations.keySet());
    }

    /**
     * Check if agent is in any formation
     */
    public boolean isAgentInFormation(int agentId) {
        for (Formation formation : activeFormations.values()) {
            if (formation.hasAgent(agentId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get formation ID that contains agent
     * Returns -1 if agent not in any formation
     */
    public int getFormationForAgent(int agentId) {
        for (Map.Entry<Integer, Formation> entry : activeFormations.entrySet()) {
            if (entry.getValue().hasAgent(agentId)) {
                return entry.getKey();
            }
        }
        return -1;
    }
}

/**
 * FORMATIONTRANSITION - Tracks formation transition progress
 */
class FormationTransition {
    int formationId;
    FormationType oldFormationType;
    FormationType newFormationType;
    long startTime;
    double progress;  // 0.0 to 1.0
}
