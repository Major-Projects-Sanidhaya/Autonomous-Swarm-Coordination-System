package com.team6.swarm.intelligence.decisions;

/**
 * DECISIONCRITERION CLASS - Individual Evaluation Criterion
 */
public class DecisionCriterion {
    public String name;
    public CriterionType type;
    public double weight;          // Importance weight (0.0 to 1.0)
    public boolean higherIsBetter;  // Scoring direction
    
    public DecisionCriterion(String name, CriterionType type, double weight) {
        this.name = name;
        this.type = type;
        this.weight = weight;
        this.higherIsBetter = (type != CriterionType.DISTANCE && 
                              type != CriterionType.BATTERY_COST && 
                              type != CriterionType.RISK);
    }
    
    @Override
    public String toString() {
        return String.format("%s (weight: %.2f)", name, weight);
    }
}