/**
 * FLOCKING CONTROLLER CLASS - Swarm Intelligence Core
 *
 * PURPOSE:
 * - Implements collective swarm behaviors using Reynolds flocking algorithm
 * - Processes neighbor information to generate coordinated movement commands
 * - Core intelligence system that creates emergent group behavior from simple rules
 *
 * MAIN COMPONENTS:
 * 1. Three Flocking Rules - Separation, Alignment, Cohesion
 * 2. Force Calculation - Weighted combination of behavioral forces
 * 3. Neighbor Analysis - Distance-based behavioral zone detection
 * 4. Movement Command Generation - Converts forces to actionable commands
 *
 * CORE FUNCTIONS:
 * 1. calculateFlocking() - Main entry point for flocking behavior
 * 2. calculateSeparation() - Collision avoidance with nearby agents
 * 3. calculateAlignment() - Velocity matching with local neighbors
 * 4. calculateCohesion() - Attraction toward group center
 * 5. combineForces() - Weighted force integration
 *
 * FLOCKING ALGORITHM LOGIC:
 * 1. Analyze all neighbors within communication range
 * 2. Apply separation force (avoid collisions) - highest priority
 * 3. Apply alignment force (match neighbor velocities) - medium priority  
 * 4. Apply cohesion force (move toward group center) - lowest priority
 * 5. Combine forces with configurable weights
 * 6. Generate MovementCommand with resulting force vector
 *
 * BEHAVIORAL ZONES:
 * - Separation Zone (0-30 units): Repulsion force, avoid collisions
 * - Alignment Zone (30-50 units): Velocity matching with neighbors
 * - Cohesion Zone (50-80 units): Attraction toward group center
 * - Communication Range (0-100 units): Maximum neighbor detection
 *
 * FORCE CALCULATION:
 * - Forces are normalized Vector2D objects (magnitude 0-1)
 * - Distance weighting: closer neighbors have stronger influence
 * - Default weights: Separation=1.5, Alignment=1.0, Cohesion=1.0
 * - Final force clamped to prevent extreme velocities
 *
 * EXPECTED OUTPUTS:
 * - MovementCommand with FLOCKING_BEHAVIOR type
 * - Force vectors pointing away from crowded areas
 * - Smooth group movement without oscillation
 * - Console: "Flocking: Sep(0.3,0.1) Align(-0.2,0.4) Cohes(0.1,-0.2)"
 *
 * INTEGRATION POINTS:
 * - Receives: NeighborInfo from John's communication system
 * - Receives: AgentCapabilities from Sanidhaya's agent system  
 * - Sends: MovementCommand to Sanidhaya for execution
 * - Sends: Debug info to Anthony's visualization system
 */
// src/main/java/com/team6/swarm/intelligence/FlockingController.java
package com.team6.swarm.intelligence;

import com.team6.swarm.core.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class FlockingController {
    // Behavioral zone radii - tuned for realistic flocking
    private static final double SEPARATION_RADIUS = 30.0;
    private static final double ALIGNMENT_RADIUS = 50.0; 
    private static final double COHESION_RADIUS = 80.0;
    
    // Force weights - separation prioritized for collision avoidance
    private static final double SEPARATION_WEIGHT = 1.5;
    private static final double ALIGNMENT_WEIGHT = 1.0;
    private static final double COHESION_WEIGHT = 1.0;
    
    // Performance tracking
    private long lastCalculationTime;
    private int calculationsPerformed;
    
    /**
     * Main flocking calculation - called by system for each agent
     * @param agentId - ID of agent requesting flocking behavior
     * @param currentState - Agent's current position and velocity
     * @param neighbors - List of nearby agents within communication range
     * @return MovementCommand containing combined flocking forces
     */
    public MovementCommand calculateFlocking(int agentId, AgentState currentState, List<NeighborInfo> neighbors) {
        long startTime = System.currentTimeMillis();
        
        // Calculate individual behavioral forces
        Vector2D separationForce = calculateSeparation(currentState.position, neighbors);
        Vector2D alignmentForce = calculateAlignment(currentState.velocity, neighbors);
        Vector2D cohesionForce = calculateCohesion(currentState.position, neighbors);
        
        // Combine forces with weights
        Vector2D combinedForce = combineForces(separationForce, alignmentForce, cohesionForce);
        
        // Create movement command
        MovementCommand command = createFlockingCommand(agentId, combinedForce);
        
        // Update performance metrics
        updatePerformanceMetrics(startTime);
        
        // Debug output
        logFlockingCalculation(agentId, separationForce, alignmentForce, cohesionForce);
        
        return command;
    }
    
    /**
     * Separation behavior - avoid collisions with nearby agents
     * Creates repulsion force away from agents within separation radius
     */
    private Vector2D calculateSeparation(Point2D myPosition, List<NeighborInfo> neighbors) {
        Vector2D steerForce = new Vector2D(0, 0);
        int neighborCount = 0;
        
        // Examine each neighbor for separation requirements
        for (NeighborInfo neighbor : neighbors) {
            if (neighbor.distance < SEPARATION_RADIUS && neighbor.distance > 0) {
                // Calculate repulsion vector (away from neighbor)
                Vector2D repulsionVector = new Vector2D(
                    myPosition.x - neighbor.position.x,
                    myPosition.y - neighbor.position.y
                );
                
                // Weight by inverse distance - closer = stronger repulsion
                repulsionVector = normalize(repulsionVector);
                repulsionVector = multiply(repulsionVector, 1.0 / neighbor.distance);
                
                steerForce = add(steerForce, repulsionVector);
                neighborCount++;
            }
        }
        
        // Average and normalize the separation force
        if (neighborCount > 0) {
            steerForce = divide(steerForce, neighborCount);
            steerForce = normalize(steerForce);
        }
        
        return steerForce;
    }
    
    /**
     * Alignment behavior - match velocities with nearby agents
     * Creates steering force toward average neighbor velocity
     */
    private Vector2D calculateAlignment(Vector2D myVelocity, List<NeighborInfo> neighbors) {
        Vector2D averageVelocity = new Vector2D(0, 0);
        int neighborCount = 0;
        
        // Calculate average velocity of neighbors in alignment zone
        for (NeighborInfo neighbor : neighbors) {
            if (neighbor.distance < ALIGNMENT_RADIUS && neighbor.distance > SEPARATION_RADIUS) {
                averageVelocity = add(averageVelocity, neighbor.velocity);
                neighborCount++;
            }
        }
        
        // Generate steering force toward average velocity
        if (neighborCount > 0) {
            averageVelocity = divide(averageVelocity, neighborCount);
            Vector2D steerForce = subtract(averageVelocity, myVelocity);
            return normalize(steerForce);
        }
        
        return new Vector2D(0, 0);
    }
    
    /**
     * Cohesion behavior - move toward center of nearby group
     * Creates attraction force toward average neighbor position
     */
    private Vector2D calculateCohesion(Point2D myPosition, List<NeighborInfo> neighbors) {
        Vector2D centerOfMass = new Vector2D(0, 0);
        int neighborCount = 0;
        
        // Calculate center of mass for neighbors in cohesion zone
        for (NeighborInfo neighbor : neighbors) {
            if (neighbor.distance < COHESION_RADIUS && neighbor.distance > ALIGNMENT_RADIUS) {
                centerOfMass = add(centerOfMass, new Vector2D(neighbor.position.x, neighbor.position.y));
                neighborCount++;
            }
        }
        
        // Generate steering force toward group center
        if (neighborCount > 0) {
            centerOfMass = divide(centerOfMass, neighborCount);
            Vector2D steerForce = new Vector2D(
                centerOfMass.x - myPosition.x,
                centerOfMass.y - myPosition.y
            );
            return normalize(steerForce);
        }
        
        return new Vector2D(0, 0);
    }
    
    /**
     * Combine behavioral forces with configurable weights
     * Applies force priorities and limits final magnitude
     */
    private Vector2D combineForces(Vector2D separation, Vector2D alignment, Vector2D cohesion) {
        // Apply weights to individual forces
        Vector2D weightedSeparation = multiply(separation, SEPARATION_WEIGHT);
        Vector2D weightedAlignment = multiply(alignment, ALIGNMENT_WEIGHT);
        Vector2D weightedCohesion = multiply(cohesion, COHESION_WEIGHT);
        
        // Combine all forces
        Vector2D totalForce = add(add(weightedSeparation, weightedAlignment), weightedCohesion);
        
        // Normalize to prevent extreme forces
        if (totalForce.magnitude() > 1.0) {
            totalForce = normalize(totalForce);
        }
        
        return totalForce;
    }
    
    /**
     * Create MovementCommand from calculated flocking force
     */
    private MovementCommand createFlockingCommand(int agentId, Vector2D force) {
        MovementCommand command = new MovementCommand();
        command.agentId = agentId;
        command.type = MovementType.FLOCKING_BEHAVIOR;
        // Note: CommandPriority doesn't exist yet, will be added later
        command.parameters.put("combinedForce", force);
        command.parameters.put("separationWeight", SEPARATION_WEIGHT);
        command.parameters.put("alignmentWeight", ALIGNMENT_WEIGHT);
        command.parameters.put("cohesionWeight", COHESION_WEIGHT);
        
        return command;
    }
    
    /**
     * Update performance tracking metrics
     */
    private void updatePerformanceMetrics(long startTime) {
        lastCalculationTime = System.currentTimeMillis() - startTime;
        calculationsPerformed++;
    }
    
    /**
     * Debug logging for flocking calculations
     */
    private void logFlockingCalculation(int agentId, Vector2D sep, Vector2D align, Vector2D cohes) {
        if (calculationsPerformed % 30 == 0) { // Log every 30th calculation to avoid spam
            System.out.println(String.format(
                "Agent %d Flocking: Sep(%.2f,%.2f) Align(%.2f,%.2f) Cohes(%.2f,%.2f)",
                agentId, sep.x, sep.y, align.x, align.y, cohes.x, cohes.y
            ));
        }
    }
    
    // ==================== VECTOR MATH UTILITIES ====================
    // These are your mathematical foundation - all flocking depends on these
    
    private Vector2D normalize(Vector2D vector) {
        double magnitude = vector.magnitude();
        if (magnitude > 0) {
            return new Vector2D(vector.x / magnitude, vector.y / magnitude);
        }
        return new Vector2D(0, 0);
    }
    
    private Vector2D multiply(Vector2D vector, double scalar) {
        return new Vector2D(vector.x * scalar, vector.y * scalar);
    }
    
    private Vector2D add(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x + v2.x, v1.y + v2.y);
    }
    
    private Vector2D subtract(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x - v2.x, v1.y - v2.y);
    }
    
    private Vector2D divide(Vector2D vector, double scalar) {
        if (scalar != 0) {
            return new Vector2D(vector.x / scalar, vector.y / scalar);
        }
        return new Vector2D(0, 0);
    }
    
    // ==================== PERFORMANCE MONITORING ====================
    
    public long getLastCalculationTime() {
        return lastCalculationTime;
    }
    
    public int getCalculationsPerformed() {
        return calculationsPerformed;
    }
    
    public void resetPerformanceMetrics() {
        calculationsPerformed = 0;
        lastCalculationTime = 0;
    }
}

// ==================== SUPPORTING DATA STRUCTURES ====================

/**
 * Information about neighboring agents for flocking calculations
 */
class NeighborInfo {
    public Point2D position;
    public Vector2D velocity; 
    public double distance;
    public int agentId;
    
    public NeighborInfo(int agentId, Point2D position, Vector2D velocity, double distance) {
        this.agentId = agentId;
        this.position = position;
        this.velocity = velocity;
        this.distance = distance;
    }
}