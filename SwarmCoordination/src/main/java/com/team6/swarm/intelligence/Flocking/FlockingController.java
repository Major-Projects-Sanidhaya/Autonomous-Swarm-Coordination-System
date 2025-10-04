/**
 * FLOCKINGCONTROLLER CLASS - Reynolds Flocking Algorithm Implementation
 *
 * PURPOSE:
 * - Implements three fundamental flocking rules (separation, alignment, cohesion)
 * - Transforms individual agents into coordinated swarm through local rules
 * - Creates emergent group behavior from simple neighbor interactions
 *
 * THE THREE FLOCKING RULES:
 *
 * 1. SEPARATION - Don't get too close to neighbors
 *    Algorithm:
 *    - Find all neighbors within separationRadius
 *    - For each neighbor calculate vector pointing away
 *    - Weight by inverse distance (closer = stronger push)
 *    - Sum all repulsion vectors
 *    - Result: Force pushing away from crowded areas
 *
 * 2. ALIGNMENT - Move in same direction as neighbors  
 *    Algorithm:
 *    - Find all neighbors within alignmentRadius
 *    - Average their velocity vectors
 *    - Calculate difference from own velocity
 *    - Result: Force to match group direction
 *
 * 3. COHESION - Stay with the group
 *    Algorithm:
 *    - Find all neighbors within cohesionRadius
 *    - Calculate average position (group center of mass)
 *    - Calculate direction toward center
 *    - Result: Force pulling toward group
 *
 * FORCE COMBINATION:
 * totalForce = (separation * separationWeight) + 
 *              (alignment * alignmentWeight) +
 *              (cohesion * cohesionWeight)
 *
 * BEHAVIORAL ZONES:
 * - Zone 1 (0-30 units): Separation dominates - avoid collisions
 * - Zone 2 (30-50 units): Alignment active - match velocities
 * - Zone 3 (50-80 units): Cohesion active - stay with group
 * - Zone 4 (80+ units): Outside influence - no flocking forces
 *
 * WHAT MAKES GOOD FLOCKING:
 * - Smooth, natural-looking movement patterns
 * - No jittery or oscillating behavior
 * - Group maintains cohesion without collisions
 * - Adapts fluidly to obstacles and boundaries
 * - Emergent coordination without central control
 *
 * INTEGRATION POINTS:
 * - Receives: NeighborInfo from John's communication system
 * - Receives: FlockingParameters for tunable weights
 * - Sends: MovementCommand to Sanidhaya's agent system
 * - Sends: Performance metrics to Anthony's monitoring
 */
package com.team6.swarm.intelligence.Flocking;

import com.team6.swarm.core.*;

import java.util.List;

public class FlockingController {
    // Configuration parameters - use external FlockingParameters object
    private FlockingParameters parameters;
    
    // Performance tracking
    private long lastCalculationTime;
    private int calculationsPerformed;
    private double averageCalculationTime;
    
    /**
     * Constructor with default parameters
     */
    public FlockingController() {
        this.parameters = new FlockingParameters();
        this.lastCalculationTime = 0;
        this.calculationsPerformed = 0;
        this.averageCalculationTime = 0.0;
    }
    
    /**
     * Constructor with custom parameters
     */
    public FlockingController(FlockingParameters parameters) {
        this.parameters = parameters;
        this.lastCalculationTime = 0;
        this.calculationsPerformed = 0;
        this.averageCalculationTime = 0.0;
    }
    
    /**
     * MAIN FLOCKING CALCULATION
     * 
     * This is the core method called by the system for each agent each frame.
     * Combines three flocking rules into single movement command.
     *
     * @param agentId - ID of agent requesting flocking behavior
     * @param currentState - Agent's current position and velocity
     * @param neighbors - List of nearby agents within communication range
     * @return MovementCommand containing combined flocking forces
     */
    public MovementCommand calculateFlocking(int agentId, AgentState currentState, 
                                            List<NeighborInfo> neighbors) {
        long startTime = System.currentTimeMillis();
        
        // Calculate the three fundamental flocking forces
        Vector2D separationForce = calculateSeparation(currentState.position, neighbors);
        Vector2D alignmentForce = calculateAlignment(currentState.velocity, neighbors);
        Vector2D cohesionForce = calculateCohesion(currentState.position, neighbors);
        
        // Combine forces with configured weights
        Vector2D combinedForce = combineForces(separationForce, alignmentForce, cohesionForce);
        
        // Limit force magnitude to maxForce parameter
        if (combinedForce.magnitude() > parameters.maxForce) {
            combinedForce = normalize(combinedForce);
            combinedForce = multiply(combinedForce, parameters.maxForce);
        }
        
        // Create movement command for agent
        MovementCommand command = createFlockingCommand(agentId, combinedForce);
        
        // Update performance metrics
        updatePerformanceMetrics(startTime);
        
        // Debug logging (every 30th calculation to avoid spam)
        logFlockingCalculation(agentId, separationForce, alignmentForce, cohesionForce);
        
        return command;
    }
    
    /**
     * SEPARATION RULE IMPLEMENTATION
     * "Don't get too close to neighbors"
     *
     * Algorithm Details:
     * 1. Examine each neighbor within separationRadius
     * 2. Calculate repulsion vector (away from neighbor)
     * 3. Weight by inverse distance:
     *    - Closer neighbors = stronger repulsion
     *    - Formula: repulsionStrength = 1.0 / distance
     * 4. Sum all weighted repulsion vectors
     * 5. Normalize to unit vector (direction only)
     *
     * @param myPosition - Current agent position
     * @param neighbors - List of nearby agents
     * @return Normalized separation force vector
     */
    private Vector2D calculateSeparation(Point2D myPosition, List<NeighborInfo> neighbors) {
        Vector2D steerForce = new Vector2D(0, 0);
        int neighborCount = 0;
        
        // Examine each neighbor for separation requirements
        for (NeighborInfo neighbor : neighbors) {
            double distance = neighbor.distance;
            
            // Only process neighbors within separation zone
            if (distance < parameters.separationRadius && distance > 0) {
                // Calculate vector pointing away from neighbor
                Vector2D awayVector = new Vector2D(
                    myPosition.x - neighbor.position.x,
                    myPosition.y - neighbor.position.y
                );
                
                // Normalize to unit vector (direction only)
                awayVector = normalize(awayVector);
                
                // Weight by inverse distance - closer neighbors push harder
                double weight = 1.0 / distance;
                awayVector = multiply(awayVector, weight);
                
                // Accumulate repulsion forces
                steerForce = add(steerForce, awayVector);
                neighborCount++;
            }
        }
        
        // Average the separation force if neighbors found
        if (neighborCount > 0) {
            steerForce = divide(steerForce, neighborCount);
            steerForce = normalize(steerForce);
        }
        
        return steerForce;
    }
    
    /**
     * ALIGNMENT RULE IMPLEMENTATION
     * "Move in same direction as neighbors"
     *
     * Algorithm Details:
     * 1. Find all neighbors within alignmentRadius
     * 2. Average their velocity vectors
     * 3. Calculate steering force toward average velocity
     * 4. Normalize to unit vector
     *
     * This creates coordinated group movement where all agents
     * gradually match their velocities.
     *
     * @param myVelocity - Current agent velocity
     * @param neighbors - List of nearby agents
     * @return Normalized alignment force vector
     */
    private Vector2D calculateAlignment(Vector2D myVelocity, List<NeighborInfo> neighbors) {
        Vector2D averageVelocity = new Vector2D(0, 0);
        int neighborCount = 0;
        
        // Calculate average velocity of neighbors in alignment zone
        // Note: Alignment zone is BETWEEN separation and cohesion radii
        for (NeighborInfo neighbor : neighbors) {
            double distance = neighbor.distance;
            
            if (distance < parameters.alignmentRadius && 
                distance >= parameters.separationRadius) {
                averageVelocity = add(averageVelocity, neighbor.velocity);
                neighborCount++;
            }
        }
        
        // Calculate steering force toward average velocity
        if (neighborCount > 0) {
            averageVelocity = divide(averageVelocity, neighborCount);
            
            // Desired velocity is the average
            // Steering force = desired - current
            Vector2D steerForce = subtract(averageVelocity, myVelocity);
            return normalize(steerForce);
        }
        
        return new Vector2D(0, 0);
    }
    
    /**
     * COHESION RULE IMPLEMENTATION
     * "Stay with the group"
     *
     * Algorithm Details:
     * 1. Find all neighbors within cohesionRadius
     * 2. Calculate their center of mass (average position)
     * 3. Calculate direction vector toward center
     * 4. Normalize to unit vector
     *
     * This creates attraction toward group center, keeping
     * the swarm together as a cohesive unit.
     *
     * @param myPosition - Current agent position
     * @param neighbors - List of nearby agents
     * @return Normalized cohesion force vector
     */
    private Vector2D calculateCohesion(Point2D myPosition, List<NeighborInfo> neighbors) {
        Vector2D centerOfMass = new Vector2D(0, 0);
        int neighborCount = 0;
        
        // Calculate center of mass for neighbors in cohesion zone
        // Note: Cohesion zone is BEYOND alignment radius
        for (NeighborInfo neighbor : neighbors) {
            double distance = neighbor.distance;
            
            if (distance < parameters.cohesionRadius && 
                distance >= parameters.alignmentRadius) {
                centerOfMass = add(centerOfMass, 
                    new Vector2D(neighbor.position.x, neighbor.position.y));
                neighborCount++;
            }
        }
        
        // Calculate steering force toward group center
        if (neighborCount > 0) {
            // Average position = center of mass
            centerOfMass = divide(centerOfMass, neighborCount);
            
            // Direction toward center
            Vector2D steerForce = new Vector2D(
                centerOfMass.x - myPosition.x,
                centerOfMass.y - myPosition.y
            );
            
            return normalize(steerForce);
        }
        
        return new Vector2D(0, 0);
    }
    
    /**
     * FORCE COMBINATION
     * Applies weights to each behavioral force and combines them
     *
     * Formula:
     * total = (separation * separationWeight) +
     *         (alignment * alignmentWeight) +
     *         (cohesion * cohesionWeight)
     *
     * Higher weight = stronger influence of that behavior
     */
    private Vector2D combineForces(Vector2D separation, Vector2D alignment, Vector2D cohesion) {
        // Apply configured weights to each force component
        Vector2D weightedSeparation = multiply(separation, parameters.separationWeight);
        Vector2D weightedAlignment = multiply(alignment, parameters.alignmentWeight);
        Vector2D weightedCohesion = multiply(cohesion, parameters.cohesionWeight);
        
        // Sum all weighted forces
        Vector2D totalForce = add(add(weightedSeparation, weightedAlignment), weightedCohesion);
        
        return totalForce;
    }
    
    /**
     * Create MovementCommand from calculated flocking force
     * Converts force vector into format Sanidhaya's system understands
     */
    private MovementCommand createFlockingCommand(int agentId, Vector2D force) {
        MovementCommand command = new MovementCommand();
        command.agentId = agentId;
        command.type = MovementType.FLOCKING_BEHAVIOR;
        command.parameters.put("combinedForce", force);
        command.parameters.put("separationWeight", parameters.separationWeight);
        command.parameters.put("alignmentWeight", parameters.alignmentWeight);
        command.parameters.put("cohesionWeight", parameters.cohesionWeight);
        
        return command;
    }
    
    /**
     * Update performance tracking metrics
     * Tracks calculation time for optimization analysis
     */
    private void updatePerformanceMetrics(long startTime) {
        lastCalculationTime = System.currentTimeMillis() - startTime;
        calculationsPerformed++;
        
        // Calculate running average
        averageCalculationTime = (averageCalculationTime * (calculationsPerformed - 1) + 
                                  lastCalculationTime) / calculationsPerformed;
    }
    
    /**
     * Debug logging for flocking calculations
     * Only logs every 30th calculation to avoid console spam
     */
    private void logFlockingCalculation(int agentId, Vector2D sep, Vector2D align, Vector2D cohes) {
        if (calculationsPerformed % 30 == 0) {
            System.out.println(String.format(
                "Agent %d Flocking: Sep(%.2f,%.2f) Align(%.2f,%.2f) Cohes(%.2f,%.2f)",
                agentId, sep.x, sep.y, align.x, align.y, cohes.x, cohes.y
            ));
        }
    }
    
    // ==================== VECTOR MATH UTILITIES ====================
    
    private Vector2D normalize(Vector2D vector) {
        double mag = vector.magnitude();
        if (mag > 0) {
            return new Vector2D(vector.x / mag, vector.y / mag);
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
    
    // ==================== PARAMETER MANAGEMENT ====================
    
    /**
     * Update flocking parameters at runtime
     * Allows dynamic tuning from Anthony's UI
     */
    public void updateParameters(FlockingParameters newParameters) {
        if (newParameters.validate()) {
            this.parameters = newParameters;
            System.out.println("Flocking parameters updated: " + newParameters);
        } else {
            System.err.println("Invalid flocking parameters rejected");
        }
    }
    
    public FlockingParameters getParameters() {
        return this.parameters;
    }
    
    // ==================== PERFORMANCE MONITORING ====================
    
    public long getLastCalculationTime() {
        return lastCalculationTime;
    }
    
    public int getCalculationsPerformed() {
        return calculationsPerformed;
    }
    
    public double getAverageCalculationTime() {
        return averageCalculationTime;
    }
    
    public void resetPerformanceMetrics() {
        calculationsPerformed = 0;
        lastCalculationTime = 0;
        averageCalculationTime = 0.0;
    }
}

// ==================== SUPPORTING DATA STRUCTURES ====================

/**
 * NEIGHBORINFO CLASS - Information about nearby agents
 *
 * PURPOSE:
 * - Packages neighbor data for flocking calculations
 * - Provided by John's communication system
 * - Contains position, velocity, and distance information
 *
 * USAGE:
 * - FlockingController receives list of NeighborInfo
 * - Each represents one agent within communication range
 * - Distance pre-calculated by communication system for efficiency
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