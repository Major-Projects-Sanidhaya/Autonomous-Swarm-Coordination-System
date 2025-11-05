/**
 * FORMATION CLASS - Complete Formation Specification
 *
 * PURPOSE:
 * - Defines exact positions for all agents in a formation
 * - Stores formation parameters and configuration
 * - Enables formation creation, maintenance, and transitions
 *
 * FORMATION STRUCTURE:
 *
 * CORE PROPERTIES:
 * - formationType: Which shape (LINE, WEDGE, etc.)
 * - centerPoint: Formation center location
 * - heading: Direction formation faces (radians)
 * - spacing: Distance between agents
 *
 * AGENT ASSIGNMENT:
 * - participatingAgents: List of agent IDs in formation
 * - positions: Map of agentId → calculated position
 * - roles: Map of agentId → formation role (leader, wing, etc.)
 *
 * DYNAMIC PROPERTIES:
 * - isMoving: Whether formation is translating
 * - targetLocation: Where formation is heading
 * - rotationRate: How fast formation can rotate
 *
 * POSITION CALCULATION ALGORITHMS:
 *
 * LINE FORMATION:
 * For agent at index i:
 *   offset = (i - centerIndex) * spacing
 *   x = centerX + offset * cos(heading)
 *   y = centerY + offset * sin(heading)
 *
 * COLUMN FORMATION:
 * For agent at index i:
 *   offset = i * spacing
 *   x = centerX + offset * cos(heading)
 *   y = centerY + offset * sin(heading)
 *
 * WEDGE FORMATION:
 * Leader at point (index 0)
 * For other agents:
 *   layer = (index - 1) / 2 + 1
 *   side = (index - 1) % 2  (0=left, 1=right)
 *   lateralOffset = layer * spacing * (side == 0 ? -1 : 1)
 *   rearOffset = layer * spacing
 *
 * CIRCLE FORMATION:
 * For agent at index i:
 *   angle = (2π * i) / agentCount + heading
 *   radius = calculateRadius(agentCount, spacing)
 *   x = centerX + radius * cos(angle)
 *   y = centerY + radius * sin(angle)
 *
 * DIAMOND FORMATION:
 * 4 agents at cardinal points:
 *   North: (centerX, centerY - spacing)
 *   East:  (centerX + spacing, centerY)
 *   South: (centerX, centerY + spacing)
 *   West:  (centerX - spacing, centerY)
 * Additional agents fill in between
 *
 * GRID FORMATION:
 * Calculate rows and columns:
 *   cols = ceil(sqrt(agentCount))
 *   rows = ceil(agentCount / cols)
 * For agent at index i:
 *   row = i / cols
 *   col = i % cols
 *   x = centerX + (col - cols/2) * spacing
 *   y = centerY + (row - rows/2) * spacing
 *
 * FORMATION ROLES:
 *
 * LEADER:
 * - Front/center position
 * - Sets formation direction
 * - Navigates for group
 *
 * WING:
 * - Side positions in WEDGE
 * - Maintain offset from leader
 *
 * PERIMETER:
 * - Outer positions in CIRCLE
 * - Watch outward
 *
 * INTERIOR:
 * - Inner positions in GRID
 * - Protected position
 *
 * USAGE PATTERN:
 *
 * 1. Create Formation:
 * Formation formation = new Formation(
 *     FormationType.WEDGE,
 *     new Point2D(400, 300),  // center
 *     50.0,                   // spacing
 *     Math.PI / 2,           // heading (90°)
 *     agentIds
 * );
 *
 * 2. Calculate Positions:
 * formation.calculatePositions();
 *
 * 3. Get Agent Position:
 * Point2D pos = formation.getAgentPosition(agentId);
 *
 * 4. Move Formation:
 * formation.moveTo(new Point2D(500, 400));
 *
 * 5. Rotate Formation:
 * formation.setHeading(Math.PI);  // Face south
 *
 * INTEGRATION POINTS:
 * - Created by: FormationController
 * - Used by: Agents for target position
 * - Modified by: Mission commands
 * - Visualized by: Anthony's UI
 */
package com.team6.swarm.intelligence.formation;

import com.team6.swarm.core.Point2D;
import java.util.*;

public class Formation {
    // Core properties
    public FormationType formationType;
    public Point2D centerPoint;
    public double heading;           // Radians, 0 = East
    public double spacing;
    
    // Agent assignment
    public List<Integer> participatingAgents;
    public Map<Integer, Point2D> positions;      // agentId → position
    public Map<Integer, FormationRole> roles;    // agentId → role
    
    // Dynamic properties
    public boolean isMoving;
    public Point2D targetLocation;
    public double rotationRate;      // Radians per second
    
    // Formation metadata
    public String formationId;
    public long createdTime;
    public int leaderAgentId;        // -1 if no leader
    
    /**
     * Basic constructor
     */
    public Formation(FormationType formationType, Point2D centerPoint,
                    double spacing, double heading, List<Integer> agentIds) {
        this.formationType = formationType;
        this.centerPoint = centerPoint;
        this.spacing = spacing;
        this.heading = heading;
        this.participatingAgents = new ArrayList<>(agentIds);
        this.positions = new HashMap<>();
        this.roles = new HashMap<>();
        this.isMoving = false;
        this.rotationRate = Math.PI / 4;  // 45° per second
        this.formationId = "formation_" + System.currentTimeMillis();
        this.createdTime = System.currentTimeMillis();
        this.leaderAgentId = -1;
        
        // Assign leader if formation requires one
        if (formationType.requiresLeader() && !agentIds.isEmpty()) {
            this.leaderAgentId = agentIds.get(0);
        }
        
        // Calculate initial positions
        calculatePositions();
    }
    
    // ==================== POSITION CALCULATION ====================
    
    /**
     * Calculate positions for all agents based on formation type
     */
    public void calculatePositions() {
        positions.clear();
        roles.clear();
        
        switch (formationType) {
            case LINE:
                calculateLineFormation();
                break;
            case COLUMN:
                calculateColumnFormation();
                break;
            case WEDGE:
                calculateWedgeFormation();
                break;
            case CIRCLE:
                calculateCircleFormation();
                break;
            case DIAMOND:
                calculateDiamondFormation();
                break;
            case GRID:
                calculateGridFormation();
                break;
        }
    }
    
    /**
     * LINE FORMATION
     * Agents in horizontal line
     */
    private void calculateLineFormation() {
        int agentCount = participatingAgents.size();
        int centerIndex = agentCount / 2;
        
        for (int i = 0; i < agentCount; i++) {
            int agentId = participatingAgents.get(i);
            
            // Calculate offset from center
            double offset = (i - centerIndex) * spacing;
            
            // Calculate position perpendicular to heading
            double perpHeading = heading + Math.PI / 2;  // 90° rotation
            double x = centerPoint.x + offset * Math.cos(perpHeading);
            double y = centerPoint.y + offset * Math.sin(perpHeading);
            
            positions.put(agentId, new Point2D(x, y));
            
            // Assign roles
            if (i == centerIndex) {
                roles.put(agentId, FormationRole.LEADER);
            } else {
                roles.put(agentId, FormationRole.WING);
            }
        }
    }
    
    /**
     * COLUMN FORMATION
     * Agents in vertical column (single file)
     */
    private void calculateColumnFormation() {
        for (int i = 0; i < participatingAgents.size(); i++) {
            int agentId = participatingAgents.get(i);
            
            // Calculate position along heading direction
            double offset = i * spacing;
            double x = centerPoint.x + offset * Math.cos(heading);
            double y = centerPoint.y + offset * Math.sin(heading);
            
            positions.put(agentId, new Point2D(x, y));
            
            // First agent is leader
            if (i == 0) {
                roles.put(agentId, FormationRole.LEADER);
            } else {
                roles.put(agentId, FormationRole.FOLLOWER);
            }
        }
    }
    
    /**
     * WEDGE FORMATION
     * V-shape with leader at point
     */
    private void calculateWedgeFormation() {
        int agentCount = participatingAgents.size();
        
        for (int i = 0; i < agentCount; i++) {
            int agentId = participatingAgents.get(i);
            
            if (i == 0) {
                // Leader at the point
                positions.put(agentId, new Point2D(centerPoint.x, centerPoint.y));
                roles.put(agentId, FormationRole.LEADER);
            } else {
                // Calculate layer and side
                int layer = (i - 1) / 2 + 1;
                int side = (i - 1) % 2;  // 0=left, 1=right
                
                // Lateral offset (perpendicular to heading)
                double lateralOffset = layer * spacing * (side == 0 ? -1 : 1);
                double perpHeading = heading + Math.PI / 2;
                
                // Rear offset (behind leader)
                double rearOffset = layer * spacing * 0.8;  // 80% of spacing
                
                double x = centerPoint.x 
                    + lateralOffset * Math.cos(perpHeading)
                    - rearOffset * Math.cos(heading);
                double y = centerPoint.y 
                    + lateralOffset * Math.sin(perpHeading)
                    - rearOffset * Math.sin(heading);
                
                positions.put(agentId, new Point2D(x, y));
                roles.put(agentId, FormationRole.WING);
            }
        }
    }
    
    /**
     * CIRCLE FORMATION
     * Agents arranged in circular ring
     */
    private void calculateCircleFormation() {
        int agentCount = participatingAgents.size();
        double radius = (agentCount * spacing) / (2 * Math.PI);
        
        for (int i = 0; i < agentCount; i++) {
            int agentId = participatingAgents.get(i);
            
            // Calculate angle for this agent
            double angle = (2 * Math.PI * i) / agentCount + heading;
            
            double x = centerPoint.x + radius * Math.cos(angle);
            double y = centerPoint.y + radius * Math.sin(angle);
            
            positions.put(agentId, new Point2D(x, y));
            roles.put(agentId, FormationRole.PERIMETER);
        }
    }
    
    /**
     * DIAMOND FORMATION
     * Diamond/rhombus shape
     */
    private void calculateDiamondFormation() {
        int agentCount = participatingAgents.size();
        
        if (agentCount >= 4) {
            // Place first 4 at cardinal points
            positions.put(participatingAgents.get(0), 
                new Point2D(centerPoint.x, centerPoint.y - spacing));  // North
            positions.put(participatingAgents.get(1), 
                new Point2D(centerPoint.x + spacing, centerPoint.y));  // East
            positions.put(participatingAgents.get(2), 
                new Point2D(centerPoint.x, centerPoint.y + spacing));  // South
            positions.put(participatingAgents.get(3), 
                new Point2D(centerPoint.x - spacing, centerPoint.y));  // West
            
            roles.put(participatingAgents.get(0), FormationRole.LEADER);
            for (int i = 1; i < 4; i++) {
                roles.put(participatingAgents.get(i), FormationRole.WING);
            }
            
            // Additional agents fill in between or inside
            for (int i = 4; i < agentCount; i++) {
                int agentId = participatingAgents.get(i);
                double angle = (2 * Math.PI * (i - 4)) / (agentCount - 4) + Math.PI / 4;
                double radius = spacing * 0.7;  // Slightly inside
                
                double x = centerPoint.x + radius * Math.cos(angle);
                double y = centerPoint.y + radius * Math.sin(angle);
                
                positions.put(agentId, new Point2D(x, y));
                roles.put(agentId, FormationRole.INTERIOR);
            }
        }
    }
    
    /**
     * GRID FORMATION
     * Square grid pattern
     */
    private void calculateGridFormation() {
        int agentCount = participatingAgents.size();
        
        // Calculate grid dimensions
        int cols = (int) Math.ceil(Math.sqrt(agentCount));
        int rows = (int) Math.ceil((double) agentCount / cols);
        
        for (int i = 0; i < agentCount; i++) {
            int agentId = participatingAgents.get(i);
            
            int row = i / cols;
            int col = i % cols;
            
            double x = centerPoint.x + (col - cols / 2.0) * spacing;
            double y = centerPoint.y + (row - rows / 2.0) * spacing;
            
            positions.put(agentId, new Point2D(x, y));
            
            // Corner agents are perimeter, others interior
            boolean isCorner = (row == 0 || row == rows - 1) && (col == 0 || col == cols - 1);
            roles.put(agentId, isCorner ? FormationRole.PERIMETER : FormationRole.INTERIOR);
        }
    }
    
    // ==================== FORMATION MANIPULATION ====================
    
    /**
     * Move formation to new center point
     */
    public void moveTo(Point2D newCenter) {
        this.centerPoint = newCenter;
        calculatePositions();
    }
    
    /**
     * Set new heading for formation
     */
    public void setHeading(double newHeading) {
        this.heading = newHeading;
        calculatePositions();
    }
    
    /**
     * Rotate formation by angle
     */
    public void rotate(double angleRadians) {
        this.heading += angleRadians;
        calculatePositions();
    }
    
    /**
     * Change spacing between agents
     */
    public void setSpacing(double newSpacing) {
        this.spacing = newSpacing;
        calculatePositions();
    }
    
    /**
     * Add agent to formation
     */
    public void addAgent(int agentId) {
        if (!participatingAgents.contains(agentId)) {
            participatingAgents.add(agentId);
            calculatePositions();
        }
    }
    
    /**
     * Remove agent from formation
     */
    public void removeAgent(int agentId) {
        participatingAgents.remove(Integer.valueOf(agentId));
        positions.remove(agentId);
        roles.remove(agentId);
        calculatePositions();
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Get position for specific agent
     */
    public Point2D getAgentPosition(int agentId) {
        return positions.get(agentId);
    }
    
    /**
     * Get role for specific agent
     */
    public FormationRole getAgentRole(int agentId) {
        return roles.get(agentId);
    }
    
    /**
     * Check if agent is in formation
     */
    public boolean hasAgent(int agentId) {
        return participatingAgents.contains(agentId);
    }
    
    /**
     * Get number of agents in formation
     */
    public int getAgentCount() {
        return participatingAgents.size();
    }
    
    @Override
    public String toString() {
        return String.format(
            "Formation[%s: %s | Agents: %d | Center: (%.1f,%.1f) | Heading: %.1f°]",
            formationId, formationType.getDisplayName(), 
            participatingAgents.size(),
            centerPoint.x, centerPoint.y,
            Math.toDegrees(heading)
        );
    }
}