/**
 * FORMATIONCONTROLLER CLASS - Formation Creation and Maintenance
 *
 * PURPOSE:
 * - Creates and manages swarm formations
 * - Maintains precise agent positioning in formations
 * - Handles formation transitions and adjustments
 * - Generates corrective movement commands
 *
 * MAIN RESPONSIBILITIES:
 *
 * 1. FORMATION CREATION:
 *    - Calculate positions for formation type
 *    - Assign agents to formation slots
 *    - Designate leader if needed
 *    - Initialize formation structure
 *
 * 2. FORMATION MAINTENANCE:
 *    - Monitor agent position errors
 *    - Calculate corrective forces
 *    - Generate movement commands
 *    - Keep formation intact during movement
 *
 * 3. FORMATION TRANSITIONS:
 *    - Smooth transition between formations
 *    - Interpolate between old and new positions
 *    - Avoid collisions during transition
 *    - Minimize disruption
 *
 * 4. ADAPTIVE CONTROL:
 *    - Adjust to agent failures
 *    - Rebalance after agent loss
 *    - Handle dynamic agent count
 *    - Maintain formation integrity
 *
 * POSITION ERROR TOLERANCE:
 *
 * ACCEPTABLE (0-5 units):
 * - Agent is in correct position
 * - No correction needed
 * - Continue current movement
 *
 * NEEDS CORRECTION (5-15 units):
 * - Minor position error
 * - Apply gentle corrective force
 * - Gradually return to position
 *
 * MAJOR CORRECTION (15+ units):
 * - Significant deviation
 * - Apply strong corrective force
 * - Priority return to formation
 *
 * CORRECTION ALGORITHM:
 *
 * For each agent:
 *   currentPos = agent.position
 *   targetPos = formation.getAgentPosition(agent.id)
 *   error = distance(currentPos, targetPos)
 *   
 *   if error < 5:
 *     // Within tolerance, no correction
 *     continue
 *   
 *   else if error < 15:
 *     // Minor correction
 *     correctionForce = (targetPos - currentPos) * 0.5
 *   
 *   else:
 *     // Major correction
 *     correctionForce = (targetPos - currentPos) * 1.0
 *   
 *   // Create movement command
 *   command = new MovementCommand(agent.id, correctionForce)
 *
 * FORMATION TRANSITIONS:
 *
 * Smooth Interpolation:
 *   t = transitionProgress (0.0 to 1.0)
 *   currentPos = lerp(oldPosition, newPosition, t)
 *   
 *   // Linear interpolation
 *   lerp(a, b, t) = a + (b - a) * t
 *
 * Transition Phases:
 * 1. PREPARE (0-20%): Agents slow down, prepare for transition
 * 2. MOVE (20-80%): Agents move to new positions
 * 3. SETTLE (80-100%): Fine-tune final positions
 *
 * INTEGRATION POINTS:
 * - Receives: AgentState list from Sanidhaya
 * - Sends: MovementCommand to agents
 * - Uses: Formation objects for position targets
 * - Reports: Formation status to Anthony
 */
package com.team6.swarm.intelligence.formation;

import com.team6.swarm.core.*;
import java.util.*;

public class FormationController {
    // Active formations
    private final Map<String, Formation> activeFormations;
    
    // Configuration
    private static final double ACCEPTABLE_ERROR = 5.0;      // Units
    private static final double MINOR_ERROR = 15.0;          // Units
    private static final double MINOR_CORRECTION = 0.5;      // Force multiplier
    private static final double MAJOR_CORRECTION = 1.0;      // Force multiplier
    
    // Transition management
    private Formation transitioningFrom;
    private Formation transitioningTo;
    private double transitionProgress;
    private long transitionStartTime;
    private long transitionDuration;
    
    // Performance tracking
    private int formationsCreated;
    private int transitionsCompleted;
    private double averagePositionError;
    
    /**
     * Constructor
     */
    public FormationController() {
        this.activeFormations = new HashMap<>();
        this.transitioningFrom = null;
        this.transitioningTo = null;
        this.transitionProgress = 0.0;
        this.formationsCreated = 0;
        this.transitionsCompleted = 0;
        this.averagePositionError = 0.0;
    }
    
    // ==================== FORMATION CREATION ====================
    
    /**
     * CREATE FORMATION
     * Generate new formation for given agents
     *
     * @param type Formation shape
     * @param center Formation center point
     * @param agentIds List of agents to include
     * @return Created formation
     */
    public Formation createFormation(FormationType type, Point2D center, 
                                    List<Integer> agentIds) {
        // Validate agent count
        if (agentIds.size() < type.getMinimumAgents()) {
            System.err.println(String.format(
                "Not enough agents for %s formation (need %d, have %d)",
                type.getDisplayName(), type.getMinimumAgents(), agentIds.size()
            ));
            return null;
        }
        
        // Get recommended spacing
        double spacing = type.getRecommendedSpacing(agentIds.size());
        
        // Create formation (heading East by default)
        Formation formation = new Formation(
            type, center, spacing, 0.0, agentIds
        );
        
        // Track formation
        activeFormations.put(formation.formationId, formation);
        formationsCreated++;
        
        System.out.println(String.format(
            "Created %s with %d agents at (%.1f, %.1f)",
            type.getDisplayName(), agentIds.size(), center.x, center.y
        ));
        
        return formation;
    }
    
    /**
     * CREATE FORMATION WITH CUSTOM SPACING
     */
    public Formation createFormation(FormationType type, Point2D center,
                                    double spacing, double heading,
                                    List<Integer> agentIds) {
        if (agentIds.size() < type.getMinimumAgents()) {
            System.err.println("Not enough agents for formation");
            return null;
        }
        
        Formation formation = new Formation(type, center, spacing, heading, agentIds);
        activeFormations.put(formation.formationId, formation);
        formationsCreated++;
        
        return formation;
    }
    
    // ==================== FORMATION MAINTENANCE ====================
    
    /**
     * MAINTAIN FORMATION
     * Generate corrective commands to keep agents in formation
     *
     * @param formation Formation to maintain
     * @param agents Current agent states
     * @return List of movement commands for corrections
     */
    public List<MovementCommand> maintainFormation(Formation formation, 
                                                    List<AgentState> agents) {
        List<MovementCommand> commands = new ArrayList<>();
        double totalError = 0.0;
        int agentCount = 0;
        
        // Check each agent's position error
        for (AgentState agent : agents) {
            if (!formation.hasAgent(agent.agentId)) {
                continue;  // Agent not in this formation
            }
            
            // Get target position for this agent
            Point2D targetPos = formation.getAgentPosition(agent.agentId);
            if (targetPos == null) {
                continue;
            }
            
            // Calculate position error
            double error = agent.position.distanceTo(targetPos);
            totalError += error;
            agentCount++;
            
            // Determine if correction needed
            if (error < ACCEPTABLE_ERROR) {
                // Within tolerance, no correction needed
                continue;
            }
            
            // Calculate corrective force
            Vector2D correctionForce = calculateCorrectionForce(
                agent.position, targetPos, error
            );
            
            // Create movement command
            MovementCommand cmd = new MovementCommand();
            cmd.agentId = agent.agentId;
            cmd.type = MovementType.FORMATION_POSITION;
            cmd.parameters.put("targetPosition", targetPos);
            cmd.parameters.put("correctionForce", correctionForce);
            cmd.parameters.put("positionError", error);
            
            commands.add(cmd);
        }
        
        // Update average error metric
        if (agentCount > 0) {
            averagePositionError = totalError / agentCount;
        }
        
        return commands;
    }
    
    /**
     * Calculate correction force based on position error
     */
    private Vector2D calculateCorrectionForce(Point2D currentPos, 
                                             Point2D targetPos, 
                                             double error) {
        // Direction toward target
        Vector2D direction = new Vector2D(
            targetPos.x - currentPos.x,
            targetPos.y - currentPos.y
        );
        
        // Normalize
        double magnitude = Math.sqrt(direction.x * direction.x + 
                                    direction.y * direction.y);
        if (magnitude > 0) {
            direction = new Vector2D(
                direction.x / magnitude,
                direction.y / magnitude
            );
        }
        
        // Apply correction strength based on error magnitude
        double strength;
        if (error < MINOR_ERROR) {
            strength = MINOR_CORRECTION;
        } else {
            strength = MAJOR_CORRECTION;
        }
        
        // Scale force by error (stronger correction for larger errors)
        strength *= Math.min(error / MINOR_ERROR, 2.0);
        
        return new Vector2D(direction.x * strength, direction.y * strength);
    }
    
    // ==================== FORMATION TRANSITIONS ====================
    
    /**
     * TRANSITION TO NEW FORMATION
     * Smoothly change from one formation to another
     *
     * @param currentFormation Current formation
     * @param newType New formation type
     * @param duration Transition time (milliseconds)
     */
    public void transitionFormation(Formation currentFormation, 
                                    FormationType newType, 
                                    long duration) {
        // Create new formation with same agents
        Formation newFormation = createFormation(
            newType,
            currentFormation.centerPoint,
            currentFormation.participatingAgents
        );
        
        // Setup transition
        this.transitioningFrom = currentFormation;
        this.transitioningTo = newFormation;
        this.transitionProgress = 0.0;
        this.transitionStartTime = System.currentTimeMillis();
        this.transitionDuration = duration;
        
        System.out.println(String.format(
            "Starting transition: %s → %s (%.1f seconds)",
            currentFormation.formationType.getDisplayName(),
            newType.getDisplayName(),
            duration / 1000.0
        ));
    }
    
    /**
     * UPDATE TRANSITION
     * Calculate interpolated positions during transition
     *
     * @param agents Current agent states
     * @return Movement commands for transition
     */
    public List<MovementCommand> updateTransition(List<AgentState> agents) {
        if (transitioningFrom == null || transitioningTo == null) {
            return new ArrayList<>();  // No active transition
        }
        
        // Calculate transition progress
        long elapsed = System.currentTimeMillis() - transitionStartTime;
        transitionProgress = Math.min(1.0, (double) elapsed / transitionDuration);
        
        List<MovementCommand> commands = new ArrayList<>();
        
        // Generate interpolated positions for each agent
        for (AgentState agent : agents) {
            if (!transitioningTo.hasAgent(agent.agentId)) {
                continue;
            }
            
            Point2D oldPos = transitioningFrom.getAgentPosition(agent.agentId);
            Point2D newPos = transitioningTo.getAgentPosition(agent.agentId);
            
            if (oldPos == null || newPos == null) {
                continue;
            }
            
            // Linear interpolation
            Point2D targetPos = lerp(oldPos, newPos, transitionProgress);
            
            // Calculate force toward interpolated position
            double error = agent.position.distanceTo(targetPos);
            Vector2D force = calculateCorrectionForce(agent.position, targetPos, error);
            
            // Create command
            MovementCommand cmd = new MovementCommand();
            cmd.agentId = agent.agentId;
            cmd.type = MovementType.FORMATION_POSITION;
            cmd.parameters.put("targetPosition", targetPos);
            cmd.parameters.put("correctionForce", force);
            cmd.parameters.put("transitionProgress", transitionProgress);
            
            commands.add(cmd);
        }
        
        // Check if transition complete
        if (transitionProgress >= 1.0) {
            completeTransition();
        }
        
        return commands;
    }
    
    /**
     * Linear interpolation between two points
     */
    private Point2D lerp(Point2D a, Point2D b, double t) {
        double x = a.x + (b.x - a.x) * t;
        double y = a.y + (b.y - a.y) * t;
        return new Point2D(x, y);
    }
    
    /**
     * Complete transition and switch to new formation
     */
    private void completeTransition() {
        System.out.println(String.format(
            "Transition complete: Now in %s formation",
            transitioningTo.formationType.getDisplayName()
        ));
        
        // Replace old formation with new
        activeFormations.remove(transitioningFrom.formationId);
        
        // Clear transition state
        transitioningFrom = null;
        transitioningTo = null;
        transitionProgress = 0.0;
        transitionsCompleted++;
    }
    
    // ==================== FORMATION MANIPULATION ====================
    
    /**
     * Move formation to new location
     */
    public void moveFormation(String formationId, Point2D newCenter) {
        Formation formation = activeFormations.get(formationId);
        if (formation != null) {
            formation.moveTo(newCenter);
        }
    }
    
    /**
     * Rotate formation to new heading
     */
    public void rotateFormation(String formationId, double newHeading) {
        Formation formation = activeFormations.get(formationId);
        if (formation != null) {
            formation.setHeading(newHeading);
        }
    }
    
    /**
     * Adjust formation spacing
     */
    public void setFormationSpacing(String formationId, double newSpacing) {
        Formation formation = activeFormations.get(formationId);
        if (formation != null) {
            formation.setSpacing(newSpacing);
        }
    }
    
    /**
     * Handle agent failure - rebalance formation
     */
    public void handleAgentFailure(String formationId, int failedAgentId) {
        Formation formation = activeFormations.get(formationId);
        if (formation != null) {
            System.out.println(String.format(
                "Agent %d failed - removing from %s",
                failedAgentId, formation.formationType.getDisplayName()
            ));
            
            formation.removeAgent(failedAgentId);
            
            // Check if formation still viable
            if (formation.getAgentCount() < formation.formationType.getMinimumAgents()) {
                System.out.println("Formation no longer viable - disbanding");
                activeFormations.remove(formationId);
            }
        }
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Get formation by ID
     */
    public Formation getFormation(String formationId) {
        return activeFormations.get(formationId);
    }
    
    /**
     * Get all active formations
     */
    public Collection<Formation> getAllFormations() {
        return activeFormations.values();
    }
    
    /**
     * Check if currently transitioning
     */
    public boolean isTransitioning() {
        return transitioningFrom != null && transitioningTo != null;
    }
    
    /**
     * Get current transition progress
     */
    public double getTransitionProgress() {
        return transitionProgress;
    }
    
    /**
     * Get average position error across all formations
     */
    public double getAveragePositionError() {
        return averagePositionError;
    }

    /**
     * Compute a cohesion metric for a specific formation based on current agent states.
     * Returns a value in range [0.0, 1.0] where 1.0 means perfect cohesion (agents at
     * their target positions) and 0.0 means very poor cohesion.
     *
     * This method is tolerant of missing agent states; agents not found in the provided
     * list are ignored for the purpose of the calculation.
     */
    public double getFormationCohesion(Formation formation, List<AgentState> agents) {
        if (formation == null || agents == null || agents.isEmpty()) return 0.0;

        double totalDistance = 0.0;
        int counted = 0;

        // Build a quick lookup map of agentId -> AgentState for efficiency
        Map<Integer, AgentState> stateById = new HashMap<>();
        for (AgentState s : agents) {
            stateById.put(s.agentId, s);
        }

        for (Integer agentId : formation.participatingAgents) {
            AgentState s = stateById.get(agentId);
            if (s == null) continue;

            Point2D target = formation.getAgentPosition(agentId);
            if (target == null) continue;

            double dist = s.position.distanceTo(target);
            totalDistance += dist;
            counted++;
        }

        if (counted == 0) return 0.0;

        double avgDistance = totalDistance / counted;

        // Normalize cohesion: closer distances -> higher cohesion. We use formation.spacing
        // as the reference scale. The formula maps 0 -> 1.0 and (2*spacing or more) -> 0.0
        double scale = Math.max(1.0, formation.spacing * 2.0);
        double cohesion = 1.0 - Math.min(1.0, avgDistance / scale);
        return Math.max(0.0, Math.min(1.0, cohesion));
    }
    
    /**
     * Check if formation is well-maintained
     * (average error below acceptable threshold)
     */
    public boolean isFormationWellMaintained() {
        return averagePositionError < ACCEPTABLE_ERROR * 2;
    }
    
    // ==================== PERFORMANCE METRICS ====================
    
    public int getFormationsCreated() {
        return formationsCreated;
    }
    
    public int getTransitionsCompleted() {
        return transitionsCompleted;
    }
    
    public int getActiveFormationCount() {
        return activeFormations.size();
    }
    
    public void resetMetrics() {
        formationsCreated = 0;
        transitionsCompleted = 0;
        averagePositionError = 0.0;
    }
}