/**
 * FLOCKINGPARAMETERS CLASS - Configuration for Flocking Behavior
 *
 * PURPOSE:
 * - Stores tunable parameters for Reynolds flocking algorithm
 * - Allows runtime adjustment of behavioral weights and radii
 * - Enables different flocking behaviors for different mission types
 *
 * MAIN COMPONENTS:
 * 1. Behavioral Radii - Distance thresholds for each flocking rule
 * 2. Force Weights - Relative strength of each behavioral component
 * 3. Physical Limits - Speed and acceleration constraints
 *
 * PARAMETER CATEGORIES:
 * 
 * SEPARATION PARAMETERS:
 * - separationRadius: Distance within which agents repel (default: 30 units)
 * - separationWeight: Strength of collision avoidance (default: 1.5)
 * - Critical for preventing agent collisions
 *
 * ALIGNMENT PARAMETERS:
 * - alignmentRadius: Distance for velocity matching (default: 50 units)
 * - alignmentWeight: Strength of direction matching (default: 1.0)
 * - Creates coordinated group movement
 *
 * COHESION PARAMETERS:
 * - cohesionRadius: Distance for group attraction (default: 80 units)
 * - cohesionWeight: Strength of group cohesion (default: 1.0)
 * - Keeps swarm together as unit
 *
 * PHYSICAL LIMITS:
 * - maxSpeed: Maximum agent velocity (default: 50 units/second)
 * - maxForce: Maximum steering force (default: 2.0 units/second²)
 *
 * USAGE SCENARIOS:
 * 
 * Tight Formation (narrow passage):
 * - separationWeight: 2.0 (stronger collision avoidance)
 * - alignmentWeight: 1.5 (match velocities closely)
 * - cohesionWeight: 0.8 (less pull to center)
 *
 * Loose Exploration:
 * - separationWeight: 1.0 (allow more spread)
 * - alignmentWeight: 0.5 (less velocity matching)
 * - cohesionWeight: 1.5 (keep general group)
 *
 * Emergency Evasion:
 * - separationWeight: 2.5 (maximum collision avoidance)
 * - maxSpeed: 75.0 (allow faster movement)
 *
 * EXPECTED VALUES:
 * - All radii in world units (0-200 typical range)
 * - All weights 0.0-3.0 (higher = stronger influence)
 * - maxSpeed in units per second (20-100 typical)
 *
 * INTEGRATION POINTS:
 * - Used by: FlockingController for force calculations
 * - Modified by: Anthony's UI sliders for runtime tuning
 * - Read by: All flocking behavior methods
 */
package com.team6.swarm.intelligence.Flocking;

public class FlockingParameters {
    // Separation parameters - collision avoidance
    public double separationRadius;
    public double separationWeight;
    
    // Alignment parameters - velocity matching
    public double alignmentRadius;
    public double alignmentWeight;
    
    // Cohesion parameters - group attraction
    public double cohesionRadius;
    public double cohesionWeight;
    
    // Physical limits
    public double maxSpeed;
    public double maxForce;
    
    /**
     * Constructor with default parameters
     * These values provide balanced, natural-looking flocking
     */
    public FlockingParameters() {
        // Defaults tuned for typical swarm scenarios
        this.separationRadius = 30.0;  // Don't get closer than 30 units
        this.separationWeight = 1.5;   // Collision avoidance is priority
        
        this.alignmentRadius = 50.0;   // Match velocities within 50 units
        this.alignmentWeight = 1.0;    // Standard velocity matching
        
        this.cohesionRadius = 80.0;    // Stay within 80 units of group
        this.cohesionWeight = 1.0;     // Standard group cohesion
        
        this.maxSpeed = 50.0;          // Maximum velocity magnitude
        this.maxForce = 2.0;           // Maximum steering force
    }
    
    /**
     * Constructor with custom parameters
     * Allows creation of specialized flocking profiles
     */
    public FlockingParameters(double separationRadius, double separationWeight,
                              double alignmentRadius, double alignmentWeight,
                              double cohesionRadius, double cohesionWeight,
                              double maxSpeed, double maxForce) {
        this.separationRadius = separationRadius;
        this.separationWeight = separationWeight;
        this.alignmentRadius = alignmentRadius;
        this.alignmentWeight = alignmentWeight;
        this.cohesionRadius = cohesionRadius;
        this.cohesionWeight = cohesionWeight;
        this.maxSpeed = maxSpeed;
        this.maxForce = maxForce;
    }
    
    /**
     * Validate parameters are within reasonable ranges
     * Prevents invalid configurations that could break flocking
     */
    public boolean validate() {
        if (separationRadius <= 0 || separationRadius > 200) return false;
        if (alignmentRadius <= 0 || alignmentRadius > 200) return false;
        if (cohesionRadius <= 0 || cohesionRadius > 200) return false;
        
        if (separationWeight < 0 || separationWeight > 5.0) return false;
        if (alignmentWeight < 0 || alignmentWeight > 5.0) return false;
        if (cohesionWeight < 0 || cohesionWeight > 5.0) return false;
        
        if (maxSpeed <= 0 || maxSpeed > 200) return false;
        if (maxForce <= 0 || maxForce > 10) return false;
        
        // Separation radius should be smallest
        if (separationRadius >= alignmentRadius) return false;
        if (alignmentRadius >= cohesionRadius) return false;
        
        return true;
    }
    
    /**
     * Create preset for tight formation flying
     */
    public static FlockingParameters createTightFormation() {
        return new FlockingParameters(
            25.0, 2.0,   // Stronger separation, smaller radius
            45.0, 1.5,   // Strong alignment
            70.0, 0.8,   // Weaker cohesion
            50.0, 2.5    // Standard limits
        );
    }
    
    /**
     * Create preset for loose exploration
     */
    public static FlockingParameters createLooseExploration() {
        return new FlockingParameters(
            35.0, 1.0,   // Weaker separation, larger radius
            60.0, 0.5,   // Weak alignment
            100.0, 1.5,  // Strong cohesion
            50.0, 2.0    // Standard limits
        );
    }
    
    /**
     * Create preset for emergency evasion
     */
    public static FlockingParameters createEmergencyEvasion() {
        return new FlockingParameters(
            40.0, 2.5,   // Maximum separation
            50.0, 0.8,   // Reduced alignment
            80.0, 0.5,   // Minimal cohesion
            75.0, 3.0    // Increased speed and force
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "FlockingParameters[sep=%.1f(%.1f) align=%.1f(%.1f) coh=%.1f(%.1f) speed=%.1f]",
            separationRadius, separationWeight,
            alignmentRadius, alignmentWeight,
            cohesionRadius, cohesionWeight,
            maxSpeed
        );
    }
}