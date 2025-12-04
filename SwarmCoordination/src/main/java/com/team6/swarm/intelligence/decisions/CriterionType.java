package com.team6.swarm.intelligence.decisions;

/**
 * CRITERIONTYPE ENUM - Types of Evaluation Criteria
 */
public enum CriterionType {
    DISTANCE,        // Travel distance (lower is better)
    BATTERY_COST,    // Energy consumption (lower is better)
    TIME,            // Time to complete (lower is better)
    RISK,            // Risk level (lower is better)
    COVERAGE,        // Area coverage (higher is better)
    EFFICIENCY,      // Resource efficiency (higher is better)
    COORDINATION     // Team coordination quality (higher is better)
}
