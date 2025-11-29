/**
 * SPATIALGRID CLASS - Efficient Spatial Partitioning
 *
 * PURPOSE:
 * - Divide world space into grid cells for fast spatial queries
 * - Reduce neighbor search complexity from O(n²) to O(1) + O(k)
 * - Enable efficient collision detection and proximity queries
 * - Critical optimization for systems with 20+ agents
 *
 * ALGORITHM:
 * - Divide world into uniform grid cells
 * - Each agent stored in cell containing its position
 * - Neighbor queries only check adjacent cells
 * - 9 cell checks maximum (3x3 grid) instead of all agents
 *
 * COMPLEXITY:
 * - Insert: O(1)
 * - Query radius: O(1) cell lookup + O(k) neighbor check
 * - Query rectangle: O(c) where c = cells in rectangle
 * - Memory: O(n) where n = agent count
 *
 * USAGE:
 * SpatialGrid grid = new SpatialGrid(800, 600, 100);
 * grid.insert(agent);
 * List<AgentState> neighbors = grid.queryRadius(position, radius);
 */
package com.team6.swarm.intelligence.optimization;

import com.team6.swarm.core.*;
import java.util.*;

public class SpatialGrid {
    // Grid configuration
    private double cellSize;
    private int gridWidth;
    private int gridHeight;
    
    // Grid storage
    private Map<String, List<AgentState>> cells;
    
    // Statistics
    private int totalInserts;
    private int totalQueries;
    
    /**
     * Constructor
     * @param worldWidth World width in units
     * @param worldHeight World height in units
     * @param cellSize Size of each grid cell
     */
    public SpatialGrid(double worldWidth, double worldHeight, double cellSize) {
        this.cellSize = cellSize;
        this.gridWidth = (int) Math.ceil(worldWidth / cellSize);
        this.gridHeight = (int) Math.ceil(worldHeight / cellSize);
        this.cells = new HashMap<>();
        this.totalInserts = 0;
        this.totalQueries = 0;
    }
    
    /**
     * INSERT AGENT
     * Add agent to appropriate grid cell
     */
    public void insert(AgentState agent) {
        String cellKey = getCellKey(agent.position);
        cells.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(agent);
        totalInserts++;
    }
    
    /**
     * QUERY RADIUS
     * Get all agents within radius of position
     */
    public List<AgentState> queryRadius(Point2D position, double radius) {
        totalQueries++;
        List<AgentState> results = new ArrayList<>();
        
        // Calculate cells to check
        int minCellX = getCellX(position.x - radius);
        int maxCellX = getCellX(position.x + radius);
        int minCellY = getCellY(position.y - radius);
        int maxCellY = getCellY(position.y + radius);
        
        // Check all cells in range
        for (int x = minCellX; x <= maxCellX; x++) {
            for (int y = minCellY; y <= maxCellY; y++) {
                String cellKey = getCellKey(x, y);
                List<AgentState> cellAgents = cells.get(cellKey);
                
                if (cellAgents != null) {
                    for (AgentState agent : cellAgents) {
                        double distance = position.distanceTo(agent.position);
                        if (distance <= radius) {
                            results.add(agent);
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * QUERY RECTANGLE
     * Get all agents in rectangular area
     */
    public List<AgentState> queryRectangle(Point2D topLeft, Point2D bottomRight) {
        totalQueries++;
        List<AgentState> results = new ArrayList<>();
        
        int minCellX = getCellX(topLeft.x);
        int maxCellX = getCellX(bottomRight.x);
        int minCellY = getCellY(topLeft.y);
        int maxCellY = getCellY(bottomRight.y);
        
        for (int x = minCellX; x <= maxCellX; x++) {
            for (int y = minCellY; y <= maxCellY; y++) {
                String cellKey = getCellKey(x, y);
                List<AgentState> cellAgents = cells.get(cellKey);
                
                if (cellAgents != null) {
                    for (AgentState agent : cellAgents) {
                        if (agent.position.x >= topLeft.x && 
                            agent.position.x <= bottomRight.x &&
                            agent.position.y >= topLeft.y && 
                            agent.position.y <= bottomRight.y) {
                            results.add(agent);
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * CLEAR GRID
     * Remove all agents
     */
    public void clear() {
        cells.clear();
    }
    
    /**
     * GET CELL KEY
     * Convert position to cell identifier
     */
    private String getCellKey(Point2D position) {
        int cellX = getCellX(position.x);
        int cellY = getCellY(position.y);
        return getCellKey(cellX, cellY);
    }
    
    private String getCellKey(int cellX, int cellY) {
        return cellX + "," + cellY;
    }
    
    /**
     * GET CELL COORDINATES
     */
    private int getCellX(double x) {
        int cellX = (int) (x / cellSize);
        return Math.max(0, Math.min(gridWidth - 1, cellX));
    }
    
    private int getCellY(double y) {
        int cellY = (int) (y / cellSize);
        return Math.max(0, Math.min(gridHeight - 1, cellY));
    }
    
    /**
     * GET STATISTICS
     */
    public int getCellCount() {
        return gridWidth * gridHeight;
    }
    
    public int getAgentCount() {
        return cells.values().stream().mapToInt(List::size).sum();
    }
    
    public int getTotalInserts() {
        return totalInserts;
    }
    
    public int getTotalQueries() {
        return totalQueries;
    }
    
    @Override
    public String toString() {
        return String.format(
            "SpatialGrid[%dx%d cells | %.0f unit cells | %d agents | %d queries]",
            gridWidth, gridHeight, cellSize, getAgentCount(), totalQueries
        );
    }
}