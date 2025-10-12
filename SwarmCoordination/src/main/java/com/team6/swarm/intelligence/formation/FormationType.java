/**
 * FORMATIONTYPE ENUM - Standard Formation Shapes
 *
 * PURPOSE:
 * - Defines geometric patterns for coordinated swarm movement
 * - Enables mission-specific formation selection
 * - Provides standardized coordination structures
 *
 * FORMATION DEFINITIONS:
 *
 * LINE:
 * - Agents arranged in horizontal line
 * - Best for: Passing through narrow areas, search lines
 * - Spacing: Equal distance between agents
 * - Pattern: O---O---O---O---O
 * - Use case: Crossing bridges, narrow passages
 *
 * COLUMN:
 * - Agents arranged in vertical column (single file)
 * - Best for: Following paths, sequential movement
 * - Spacing: Equal distance front-to-back
 * - Pattern: O
 *           O
 *           O
 *           O
 * - Use case: Following trails, queue movement
 *
 * WEDGE (V-Formation):
 * - V-shaped formation with leader at point
 * - Best for: Efficient forward movement, reduces drag
 * - Spacing: Angular separation from leader
 * - Pattern:     O (leader)
 *              O   O
 *            O       O
 * - Use case: Long-distance travel, coordinated advance
 *
 * CIRCLE:
 * - Agents arranged in circular ring
 * - Best for: Surrounding points of interest, 360° coverage
 * - Spacing: Equal angular separation
 * - Pattern:     O
 *            O       O
 *          O     •     O  (• = center)
 *            O       O
 *                O
 * - Use case: Perimeter security, surrounding targets
 *
 * DIAMOND:
 * - Diamond/rhombus shape formation
 * - Best for: Balanced coverage, symmetric approach
 * - Spacing: Equal distances from center
 * - Pattern:       O
 *              O   •   O
 *                  O
 * - Use case: Balanced patrol, symmetric coverage
 *
 * GRID:
 * - Square grid pattern
 * - Best for: Area coverage, systematic search
 * - Spacing: Rows and columns evenly spaced
 * - Pattern: O   O   O
 *           O   O   O
 *           O   O   O
 * - Use case: Search patterns, area monitoring
 *
 * FORMATION CHARACTERISTICS:
 *
 * LINE:
 * - Width: spacing * (agentCount - 1)
 * - Depth: 0 (all agents at same Y)
 * - Good visibility, poor defense
 *
 * COLUMN:
 * - Width: 0 (all agents at same X)
 * - Depth: spacing * (agentCount - 1)
 * - Efficient single-file, vulnerable flanks
 *
 * WEDGE:
 * - Width: increases with distance from leader
 * - Depth: layered behind leader
 * - Aerodynamic, strong forward presence
 *
 * CIRCLE:
 * - Radius: calculated based on agent count
 * - Circumference: 2 * π * radius
 * - Equal coverage all directions
 *
 * DIAMOND:
 * - 4 points at cardinal directions
 * - Balanced, no strong directional bias
 * - Good for approach from any direction
 *
 * GRID:
 * - Rows × Columns arrangement
 * - Maximizes search coverage
 * - Systematic, organized
 *
 * WHEN TO USE EACH FORMATION:
 *
 * Navigation Scenarios:
 * - Narrow passage → LINE or COLUMN
 * - Open area travel → WEDGE
 * - Exploring unknown → GRID
 *
 * Mission Scenarios:
 * - Perimeter defense → CIRCLE
 * - Forward assault → WEDGE
 * - Area search → GRID
 * - Balanced patrol → DIAMOND
 *
 * Tactical Scenarios:
 * - Need speed → WEDGE
 * - Need coverage → GRID or CIRCLE
 * - Need flexibility → DIAMOND
 * - Constrained space → LINE or COLUMN
 *
 * INTEGRATION POINTS:
 * - Used by: FormationController for position calculations
 * - Selected by: Mission planner or voting system
 * - Displayed by: Anthony's UI with formation visualization
 * - Switched by: Formation change commands
 */
package com.team6.swarm.intelligence.formation;

public enum FormationType {
    /**
     * Horizontal line formation
     * Best for narrow passages
     */
    LINE,
    
    /**
     * Vertical column (single file)
     * Best for following paths
     */
    COLUMN,
    
    /**
     * V-shaped formation with leader at point
     * Best for efficient forward movement
     */
    WEDGE,
    
    /**
     * Circular ring formation
     * Best for surrounding targets
     */
    CIRCLE,
    
    /**
     * Diamond/rhombus shape
     * Best for balanced coverage
     */
    DIAMOND,
    
    /**
     * Square grid pattern
     * Best for area coverage
     */
    GRID;
    
    /**
     * Get recommended spacing for this formation type
     * @param agentCount Number of agents in formation
     * @return Recommended distance between agents
     */
    public double getRecommendedSpacing(int agentCount) {
        switch (this) {
            case LINE:
            case COLUMN:
                return 40.0;  // Tight spacing for lines
                
            case WEDGE:
                return 50.0;  // Moderate spacing for V-shape
                
            case CIRCLE:
                // Spacing depends on circle size
                double radius = getCircleRadius(agentCount, 40.0);
                return 2 * Math.PI * radius / agentCount;
                
            case DIAMOND:
                return 60.0;  // Wider spacing for diamond
                
            case GRID:
                return 50.0;  // Standard grid spacing
                
            default:
                return 40.0;
        }
    }
    
    /**
     * Calculate circle radius based on desired spacing
     */
    private double getCircleRadius(int agentCount, double desiredSpacing) {
        // Circumference = agentCount * desiredSpacing
        // Radius = Circumference / (2π)
        return (agentCount * desiredSpacing) / (2 * Math.PI);
    }
    
    /**
     * Get minimum number of agents needed for this formation
     */
    public int getMinimumAgents() {
        switch (this) {
            case LINE:
            case COLUMN:
                return 2;  // At least 2 for a line
                
            case WEDGE:
                return 3;  // Need leader + 2 wings
                
            case CIRCLE:
                return 4;  // Need at least 4 for circle
                
            case DIAMOND:
                return 4;  // 4 cardinal points
                
            case GRID:
                return 4;  // 2×2 minimum grid
                
            default:
                return 2;
        }
    }
    
    /**
     * Check if formation needs a designated leader
     */
    public boolean requiresLeader() {
        return this == WEDGE || this == COLUMN;
    }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        switch (this) {
            case LINE: return "Line Formation";
            case COLUMN: return "Column Formation";
            case WEDGE: return "Wedge Formation";
            case CIRCLE: return "Circle Formation";
            case DIAMOND: return "Diamond Formation";
            case GRID: return "Grid Formation";
            default: return "Unknown Formation";
        }
    }
    
    /**
     * Get tactical description
     */
    public String getDescription() {
        switch (this) {
            case LINE:
                return "Horizontal line - best for narrow passages and search lines";
            case COLUMN:
                return "Single file - best for following paths and constrained movement";
            case WEDGE:
                return "V-formation - best for efficient travel and coordinated advance";
            case CIRCLE:
                return "Circular perimeter - best for surrounding and 360° coverage";
            case DIAMOND:
                return "Diamond shape - best for balanced coverage and flexibility";
            case GRID:
                return "Grid pattern - best for systematic search and area coverage";
            default:
                return "Unknown formation type";
        }
    }
    
    /**
     * Get relative difficulty to maintain (1=easy, 5=hard)
     */
    public int getDifficulty() {
        switch (this) {
            case LINE:
            case COLUMN:
                return 2;  // Simple to maintain
                
            case DIAMOND:
                return 3;  // Moderate complexity
                
            case WEDGE:
                return 3;  // Needs leader tracking
                
            case CIRCLE:
                return 4;  // Requires precise positioning
                
            case GRID:
                return 4;  // Complex multi-dimensional
                
            default:
                return 3;
        }
    }
    
    /**
     * Get recommended use cases
     */
    public String[] getUseCases() {
        switch (this) {
            case LINE:
                return new String[]{"Narrow passages", "Search lines", "Bridges"};
            case COLUMN:
                return new String[]{"Following paths", "Constrained areas", "Queue movement"};
            case WEDGE:
                return new String[]{"Long travel", "Coordinated advance", "Efficient movement"};
            case CIRCLE:
                return new String[]{"Perimeter defense", "Surrounding targets", "360° watch"};
            case DIAMOND:
                return new String[]{"Balanced patrol", "Flexible coverage", "Symmetric approach"};
            case GRID:
                return new String[]{"Area search", "Systematic coverage", "Grid patrol"};
            default:
                return new String[]{"General purpose"};
        }
    }
}