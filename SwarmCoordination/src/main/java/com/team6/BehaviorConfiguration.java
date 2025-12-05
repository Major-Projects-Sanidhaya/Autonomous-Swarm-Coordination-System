/**
 * BEHAVIORCONFIGURATION CLASS - Algorithm Parameter Settings
 *
 * PURPOSE:
 * - Encapsulates flocking and swarm behavior parameters
 * - Allows runtime tuning of algorithm weights and thresholds
 * - Provides validation for parameter ranges
 * - Enables saving/loading of configuration presets
 *
 * PARAMETER CATEGORIES:
 *
 * 1. FLOCKING WEIGHTS:
 *    - separationWeight: How strongly agents avoid each other (0.0-3.0)
 *    - alignmentWeight: How strongly agents match neighbor velocities (0.0-3.0)
 *    - cohesionWeight: How strongly agents move toward group center (0.0-3.0)
 *
 * 2. FLOCKING RADII:
 *    - separationRadius: Distance to maintain from neighbors (10-100)
 *    - alignmentRadius: Distance to consider for velocity matching (20-150)
 *    - cohesionRadius: Distance to consider for group center (30-200)
 *
 * 3. MOVEMENT PARAMETERS:
 *    - maxSpeed: Maximum agent velocity (10-100)
 *    - maxForce: Maximum steering force (0.1-5.0)
 *    - targetWeight: How strongly agents move toward targets (0.0-3.0)
 *
 * 4. DECISION PARAMETERS:
 *    - consensusThreshold: Required agreement for decisions (0.5-1.0)
 *    - votingTimeout: Time to wait for votes (1000-30000 ms)
 *    - leaderInfluence: Weight of leader's opinion (0.0-2.0)
 *
 * 5. FORMATION PARAMETERS:
 *    - formationSpacing: Distance between agents in formation (20-100)
 *    - formationStiffness: How rigidly formation is maintained (0.0-1.0)
 *    - formationDamping: Smoothness of formation adjustments (0.0-1.0)
 *
 * PRESET CONFIGURATIONS:
 *
 * TIGHT_FORMATION:
 * - High cohesion, high alignment, moderate separation
 * - Small radii, rigid formation
 * - Use for: Coordinated movement through obstacles
 *
 * LOOSE_EXPLORATION:
 * - Low cohesion, low alignment, high separation
 * - Large radii, flexible formation
 * - Use for: Area coverage, exploration
 *
 * BALANCED_DEFAULT:
 * - Equal weights, moderate radii
 * - Standard formation parameters
 * - Use for: General purpose coordination
 *
 * EMERGENCY_SCATTER:
 * - Very high separation, zero cohesion/alignment
 * - Large separation radius
 * - Use for: Avoiding collisions, dispersing
 *
 * USAGE EXAMPLE:
 * BehaviorConfiguration config = new BehaviorConfiguration();
 * config.setSeparationWeight(1.5);
 * config.setAlignmentWeight(1.0);
 * config.setCohesionWeight(1.0);
 * eventBus.publish(config);
 *
 * Or use preset:
 * BehaviorConfiguration config = BehaviorConfiguration.tightFormation();
 * systemController.applyBehaviorConfiguration(config);
 *
 * INTEGRATION POINTS:
 * - Created by: ControlPanel, ParameterPanel
 * - Consumed by: FlockingController (Lauren)
 * - Validated by: SystemController
 * - Stored by: UIConfiguration
 */
package com.team6;

import java.util.HashMap;
import java.util.Map;

public class BehaviorConfiguration {
    // ==================== FLOCKING WEIGHTS ====================
    private double separationWeight;
    private double alignmentWeight;
    private double cohesionWeight;
    
    // ==================== FLOCKING RADII ====================
    private double separationRadius;
    private double alignmentRadius;
    private double cohesionRadius;
    
    // ==================== MOVEMENT PARAMETERS ====================
    private double maxSpeed;
    private double maxForce;
    private double targetWeight;
    
    // ==================== DECISION PARAMETERS ====================
    private double consensusThreshold;
    private long votingTimeout;
    private double leaderInfluence;
    
    // ==================== FORMATION PARAMETERS ====================
    private double formationSpacing;
    private double formationStiffness;
    private double formationDamping;
    
    // ==================== METADATA ====================
    private String configurationName;
    private String description;
    private long timestamp;
    private Map<String, Object> customParameters;
    
    /**
     * Default constructor with balanced parameters
     */
    public BehaviorConfiguration() {
        // Default flocking weights (balanced)
        this.separationWeight = 1.5;
        this.alignmentWeight = 1.0;
        this.cohesionWeight = 1.0;
        
        // Default flocking radii
        this.separationRadius = 30.0;
        this.alignmentRadius = 50.0;
        this.cohesionRadius = 80.0;
        
        // Default movement parameters
        this.maxSpeed = 50.0;
        this.maxForce = 2.0;
        this.targetWeight = 1.5;
        
        // Default decision parameters
        this.consensusThreshold = 0.6;
        this.votingTimeout = 10000;  // 10 seconds
        this.leaderInfluence = 1.0;
        
        // Default formation parameters
        this.formationSpacing = 50.0;
        this.formationStiffness = 0.7;
        this.formationDamping = 0.5;
        
        // Metadata
        this.configurationName = "Default";
        this.description = "Balanced configuration for general use";
        this.timestamp = System.currentTimeMillis();
        this.customParameters = new HashMap<>();
    }
    
    // ==================== PRESET CONFIGURATIONS ====================
    
    /**
     * Tight formation preset - high coordination
     */
    public static BehaviorConfiguration tightFormation() {
        BehaviorConfiguration config = new BehaviorConfiguration();
        config.separationWeight = 1.2;
        config.alignmentWeight = 1.8;
        config.cohesionWeight = 2.0;
        config.separationRadius = 25.0;
        config.alignmentRadius = 40.0;
        config.cohesionRadius = 60.0;
        config.formationStiffness = 0.9;
        config.configurationName = "Tight Formation";
        config.description = "High coordination for precise movements";
        return config;
    }
    
    /**
     * Loose exploration preset - independent movement
     */
    public static BehaviorConfiguration looseExploration() {
        BehaviorConfiguration config = new BehaviorConfiguration();
        config.separationWeight = 2.0;
        config.alignmentWeight = 0.5;
        config.cohesionWeight = 0.3;
        config.separationRadius = 50.0;
        config.alignmentRadius = 80.0;
        config.cohesionRadius = 120.0;
        config.formationStiffness = 0.3;
        config.configurationName = "Loose Exploration";
        config.description = "Independent movement for area coverage";
        return config;
    }
    
    /**
     * Emergency scatter preset - maximum dispersion
     */
    public static BehaviorConfiguration emergencyScatter() {
        BehaviorConfiguration config = new BehaviorConfiguration();
        config.separationWeight = 3.0;
        config.alignmentWeight = 0.0;
        config.cohesionWeight = 0.0;
        config.separationRadius = 100.0;
        config.maxSpeed = 80.0;
        config.configurationName = "Emergency Scatter";
        config.description = "Maximum dispersion for collision avoidance";
        return config;
    }
    
    /**
     * Fast pursuit preset - high speed coordination
     */
    public static BehaviorConfiguration fastPursuit() {
        BehaviorConfiguration config = new BehaviorConfiguration();
        config.separationWeight = 1.0;
        config.alignmentWeight = 1.5;
        config.cohesionWeight = 1.2;
        config.maxSpeed = 80.0;
        config.maxForce = 3.0;
        config.targetWeight = 2.5;
        config.configurationName = "Fast Pursuit";
        config.description = "High speed target tracking";
        return config;
    }
    
    // ==================== GETTERS ====================
    
    public double getSeparationWeight() { return separationWeight; }
    public double getAlignmentWeight() { return alignmentWeight; }
    public double getCohesionWeight() { return cohesionWeight; }
    public double getSeparationRadius() { return separationRadius; }
    public double getAlignmentRadius() { return alignmentRadius; }
    public double getCohesionRadius() { return cohesionRadius; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getMaxForce() { return maxForce; }
    public double getTargetWeight() { return targetWeight; }
    public double getConsensusThreshold() { return consensusThreshold; }
    public long getVotingTimeout() { return votingTimeout; }
    public double getLeaderInfluence() { return leaderInfluence; }
    public double getFormationSpacing() { return formationSpacing; }
    public double getFormationStiffness() { return formationStiffness; }
    public double getFormationDamping() { return formationDamping; }
    public String getConfigurationName() { return configurationName; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
    
    // ==================== SETTERS WITH VALIDATION ====================
    
    public void setSeparationWeight(double weight) {
        this.separationWeight = clamp(weight, 0.0, 3.0);
    }
    
    public void setAlignmentWeight(double weight) {
        this.alignmentWeight = clamp(weight, 0.0, 3.0);
    }
    
    public void setCohesionWeight(double weight) {
        this.cohesionWeight = clamp(weight, 0.0, 3.0);
    }
    
    public void setSeparationRadius(double radius) {
        this.separationRadius = clamp(radius, 10.0, 100.0);
    }
    
    public void setAlignmentRadius(double radius) {
        this.alignmentRadius = clamp(radius, 20.0, 150.0);
    }
    
    public void setCohesionRadius(double radius) {
        this.cohesionRadius = clamp(radius, 30.0, 200.0);
    }
    
    public void setMaxSpeed(double speed) {
        this.maxSpeed = clamp(speed, 10.0, 100.0);
    }
    
    public void setMaxForce(double force) {
        this.maxForce = clamp(force, 0.1, 5.0);
    }
    
    public void setTargetWeight(double weight) {
        this.targetWeight = clamp(weight, 0.0, 3.0);
    }
    
    public void setConsensusThreshold(double threshold) {
        this.consensusThreshold = clamp(threshold, 0.5, 1.0);
    }
    
    public void setVotingTimeout(long timeout) {
        this.votingTimeout = Math.max(1000, Math.min(30000, timeout));
    }
    
    public void setLeaderInfluence(double influence) {
        this.leaderInfluence = clamp(influence, 0.0, 2.0);
    }
    
    public void setFormationSpacing(double spacing) {
        this.formationSpacing = clamp(spacing, 20.0, 100.0);
    }
    
    public void setFormationStiffness(double stiffness) {
        this.formationStiffness = clamp(stiffness, 0.0, 1.0);
    }
    
    public void setFormationDamping(double damping) {
        this.formationDamping = clamp(damping, 0.0, 1.0);
    }
    
    public void setConfigurationName(String name) {
        this.configurationName = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // ==================== CUSTOM PARAMETERS ====================
    
    /**
     * Add custom parameter
     */
    public void setCustomParameter(String key, Object value) {
        customParameters.put(key, value);
    }
    
    /**
     * Get custom parameter
     */
    public Object getCustomParameter(String key) {
        return customParameters.get(key);
    }
    
    /**
     * Get all custom parameters
     */
    public Map<String, Object> getCustomParameters() {
        return new HashMap<>(customParameters);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Clamp value to range
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Validate all parameters are within acceptable ranges
     */
    public boolean validate() {
        return separationWeight >= 0.0 && separationWeight <= 3.0 &&
               alignmentWeight >= 0.0 && alignmentWeight <= 3.0 &&
               cohesionWeight >= 0.0 && cohesionWeight <= 3.0 &&
               separationRadius >= 10.0 && separationRadius <= 100.0 &&
               alignmentRadius >= 20.0 && alignmentRadius <= 150.0 &&
               cohesionRadius >= 30.0 && cohesionRadius <= 200.0 &&
               maxSpeed >= 10.0 && maxSpeed <= 100.0 &&
               maxForce >= 0.1 && maxForce <= 5.0 &&
               consensusThreshold >= 0.5 && consensusThreshold <= 1.0 &&
               votingTimeout >= 1000 && votingTimeout <= 30000;
    }
    
    /**
     * Clone configuration
     */
    public BehaviorConfiguration clone() {
        BehaviorConfiguration clone = new BehaviorConfiguration();
        clone.separationWeight = this.separationWeight;
        clone.alignmentWeight = this.alignmentWeight;
        clone.cohesionWeight = this.cohesionWeight;
        clone.separationRadius = this.separationRadius;
        clone.alignmentRadius = this.alignmentRadius;
        clone.cohesionRadius = this.cohesionRadius;
        clone.maxSpeed = this.maxSpeed;
        clone.maxForce = this.maxForce;
        clone.targetWeight = this.targetWeight;
        clone.consensusThreshold = this.consensusThreshold;
        clone.votingTimeout = this.votingTimeout;
        clone.leaderInfluence = this.leaderInfluence;
        clone.formationSpacing = this.formationSpacing;
        clone.formationStiffness = this.formationStiffness;
        clone.formationDamping = this.formationDamping;
        clone.configurationName = this.configurationName + " (Copy)";
        clone.description = this.description;
        clone.customParameters = new HashMap<>(this.customParameters);
        return clone;
    }
    
    /**
     * Get configuration summary
     */
    public String getSummary() {
        return String.format("%s: Sep=%.1f, Align=%.1f, Coh=%.1f, Speed=%.0f", 
                           configurationName, separationWeight, alignmentWeight, 
                           cohesionWeight, maxSpeed);
    }
    
    /**
     * Get detailed description
     */
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration: ").append(configurationName).append("\n");
        sb.append("Description: ").append(description).append("\n\n");
        sb.append("Flocking Weights:\n");
        sb.append(String.format("  Separation: %.2f\n", separationWeight));
        sb.append(String.format("  Alignment: %.2f\n", alignmentWeight));
        sb.append(String.format("  Cohesion: %.2f\n", cohesionWeight));
        sb.append("\nFlocking Radii:\n");
        sb.append(String.format("  Separation: %.0f\n", separationRadius));
        sb.append(String.format("  Alignment: %.0f\n", alignmentRadius));
        sb.append(String.format("  Cohesion: %.0f\n", cohesionRadius));
        sb.append("\nMovement:\n");
        sb.append(String.format("  Max Speed: %.0f\n", maxSpeed));
        sb.append(String.format("  Max Force: %.2f\n", maxForce));
        sb.append("\nFormation:\n");
        sb.append(String.format("  Spacing: %.0f\n", formationSpacing));
        sb.append(String.format("  Stiffness: %.2f\n", formationStiffness));
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("BehaviorConfiguration{name='%s', sep=%.1f, align=%.1f, coh=%.1f}", 
                           configurationName, separationWeight, alignmentWeight, cohesionWeight);
    }
}
