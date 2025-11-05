/**
 * SEARCHPATTERN CLASS - Systematic Area Coverage Strategies
 *
 * PURPOSE:
 * - Provides coordinated search algorithms for swarm agents
 * - Enables efficient area coverage for target detection
 * - Generates waypoint sequences for systematic searching
 * - Supports multiple search strategy patterns
 *
 * SEARCH PATTERNS:
 *
 * 1. GRID SEARCH:
 *    - Divide search area into grid cells
 *    - Each agent covers assigned cells systematically
 *    - Complete, thorough coverage guaranteed
 *    - Best for: Known bounded areas, methodical searches
 *    - Coverage: 100% with proper cell size
 *
 * 2. SPIRAL SEARCH:
 *    - Start at center point
 *    - Spiral outward in expanding pattern
 *    - Single or multiple simultaneous spirals
 *    - Best for: Target likely near center
 *    - Coverage: Radiates outward, dense near center
 *
 * 3. EXPANDING PERIMETER:
 *    - Start with small circle around center
 *    - Gradually expand radius outward
 *    - All agents maintain perimeter formation
 *    - Best for: Radiating search from known point
 *    - Coverage: Outward expansion, agents on perimeter
 *
 * 4. RANDOM WALK:
 *    - Semi-random movement with coverage tracking
 *    - Avoid previously visited areas
 *    - Flexible adaptation to obstacles
 *    - Best for: Unknown target location, complex terrain
 *    - Coverage: Less efficient but more adaptive
 *
 * COORDINATION:
 * - Assigns non-overlapping search zones to agents
 * - Tracks coverage to avoid redundant searching
 * - Adapts to agent count and capabilities
 * - Handles agent failure mid-search
 *
 * USAGE PATTERNS:
 *
 * Grid Search:
 * SearchPattern pattern = SearchPattern.createGridSearch(
 *     centerPoint, searchRadius, agentCount);
 * List<Point2D> waypoints = pattern.getWaypointsForAgent(agentId);
 *
 * Spiral Search:
 * SearchPattern pattern = SearchPattern.createSpiralSearch(
 *     centerPoint, maxRadius, spiralSpacing);
 *
 * Expanding Perimeter:
 * SearchPattern pattern = SearchPattern.createExpandingPerimeter(
 *     centerPoint, initialRadius, expansionRate, agentCount);
 *
 * Random Walk:
 * SearchPattern pattern = SearchPattern.createRandomWalk(
 *     searchArea, moveDistance, agentCount);
 *
 * INTEGRATION POINTS:
 * - Used by: TaskAllocator for search task creation
 * - Coordinates with: Formation controller for agent spacing
 * - Tracks coverage: Reports search progress to monitoring
 * - Adapts to: Obstacle data from agents
 */
package com.team6.swarm.intelligence.coordination;

import com.team6.swarm.core.Point2D;
import java.util.*;

public class SearchPattern {
    // Pattern identification
    public String patternId;
    public SearchPatternType patternType;
    
    // Search area definition
    public Point2D centerPoint;
    public double searchRadius;
    public List<Point2D> boundaryPoints;
    
    // Pattern parameters
    public double cellSize;           // Grid: size of each cell
    public double spiralSpacing;      // Spiral: distance between turns
    public double expansionRate;      // Perimeter: radius increase per step
    public double moveDistance;       // Random walk: step size
    
    // Agent coordination
    public int totalAgents;
    public Map<Integer, List<Point2D>> agentWaypoints;
    public Map<Integer, Integer> agentCellAssignments;
    
    // Coverage tracking
    public Set<String> visitedCells;
    public double coveragePercentage;
    public long searchStartTime;
    
    /**
     * Basic constructor
     */
    public SearchPattern(String patternId, SearchPatternType patternType) {
        this.patternId = patternId;
        this.patternType = patternType;
        this.agentWaypoints = new HashMap<>();
        this.agentCellAssignments = new HashMap<>();
        this.visitedCells = new HashSet<>();
        this.coveragePercentage = 0.0;
        this.searchStartTime = System.currentTimeMillis();
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * CREATE GRID SEARCH PATTERN
     * Divide area into grid cells, assign to agents
     */
    public static SearchPattern createGridSearch(Point2D center, double radius, int agentCount, double cellSize) {
        SearchPattern pattern = new SearchPattern("grid_" + System.currentTimeMillis(), 
                                                  SearchPatternType.GRID);
        pattern.centerPoint = center;
        pattern.searchRadius = radius;
        pattern.cellSize = cellSize;
        pattern.totalAgents = agentCount;
        
        // Generate grid cells
        List<Point2D> gridCells = generateGridCells(center, radius, cellSize);
        
        // Distribute cells among agents
        pattern.distributeGridCells(gridCells, agentCount);
        
        System.out.println(String.format(
            "Grid Search: %d cells divided among %d agents (%.1f cells per agent)",
            gridCells.size(), agentCount, (double) gridCells.size() / agentCount
        ));
        
        return pattern;
    }
    
    /**
     * CREATE SPIRAL SEARCH PATTERN
     * Expanding spiral from center point
     */
    public static SearchPattern createSpiralSearch(Point2D center, double maxRadius, double spiralSpacing, int agentCount) {
        SearchPattern pattern = new SearchPattern("spiral_" + System.currentTimeMillis(),
                                                  SearchPatternType.SPIRAL);
        pattern.centerPoint = center;
        pattern.searchRadius = maxRadius;
        pattern.spiralSpacing = spiralSpacing;
        pattern.totalAgents = agentCount;
        
        // Generate spiral waypoints for each agent
        double angleOffset = (2 * Math.PI) / agentCount;  // Space spirals evenly
        
        for (int i = 0; i < agentCount; i++) {
            List<Point2D> waypoints = generateSpiralWaypoints(
                center, maxRadius, spiralSpacing, i * angleOffset);
            pattern.agentWaypoints.put(i, waypoints);
        }
        
        System.out.println(String.format(
            "Spiral Search: %d simultaneous spirals, %.1f unit spacing",
            agentCount, spiralSpacing
        ));
        
        return pattern;
    }
    
    /**
     * CREATE EXPANDING PERIMETER PATTERN
     * Circular perimeter expanding outward
     */
    public static SearchPattern createExpandingPerimeter(Point2D center, 
                                                        double initialRadius,
                                                        double expansionRate,
                                                        int agentCount) {
        SearchPattern pattern = new SearchPattern("perimeter_" + System.currentTimeMillis(),
                                                  SearchPatternType.EXPANDING_PERIMETER);
        pattern.centerPoint = center;
        pattern.searchRadius = initialRadius;
        pattern.expansionRate = expansionRate;
        pattern.totalAgents = agentCount;
        
        // Generate initial perimeter positions
        List<Point2D> perimeterPoints = generatePerimeterPositions(
            center, initialRadius, agentCount);
        
        // Assign positions to agents
        for (int i = 0; i < agentCount; i++) {
            List<Point2D> waypoints = new ArrayList<>();
            waypoints.add(perimeterPoints.get(i));
            pattern.agentWaypoints.put(i, waypoints);
        }
        
        System.out.println(String.format(
            "Expanding Perimeter: %d agents, initial radius %.1f, expansion rate %.1f",
            agentCount, initialRadius, expansionRate
        ));
        
        return pattern;
    }
    
    /**
     * CREATE RANDOM WALK PATTERN
     * Semi-random exploration with coverage tracking
     */
    public static SearchPattern createRandomWalk(Point2D center, double radius,
                                                double moveDistance, int agentCount) {
        SearchPattern pattern = new SearchPattern("random_" + System.currentTimeMillis(),
                                                  SearchPatternType.RANDOM_WALK);
        pattern.centerPoint = center;
        pattern.searchRadius = radius;
        pattern.moveDistance = moveDistance;
        pattern.totalAgents = agentCount;
        
        // Generate initial random positions for agents
        Random rand = new Random();
        for (int i = 0; i < agentCount; i++) {
            List<Point2D> waypoints = new ArrayList<>();
            
            // Start at random position within search area
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = rand.nextDouble() * radius * 0.5;  // Start in inner half
            double x = center.x + distance * Math.cos(angle);
            double y = center.y + distance * Math.sin(angle);
            
            waypoints.add(new Point2D(x, y));
            pattern.agentWaypoints.put(i, waypoints);
        }
        
        System.out.println(String.format(
            "Random Walk: %d agents, step size %.1f, search radius %.1f",
            agentCount, moveDistance, radius
        ));
        
        return pattern;
    }
    
    // ==================== WAYPOINT GENERATION ====================
    
    /**
     * Generate grid cells covering search area
     */
    private static List<Point2D> generateGridCells(Point2D center, double radius, double cellSize) {
        List<Point2D> cells = new ArrayList<>();
        
        // Calculate grid bounds
        double minX = center.x - radius;
        double maxX = center.x + radius;
        double minY = center.y - radius;
        double maxY = center.y + radius;
        
        // Generate cell centers
        for (double y = minY + cellSize / 2; y < maxY; y += cellSize) {
            for (double x = minX + cellSize / 2; x < maxX; x += cellSize) {
                Point2D cellCenter = new Point2D(x, y);
                
                // Only include cells within search radius
                if (center.distanceTo(cellCenter) <= radius) {
                    cells.add(cellCenter);
                }
            }
        }
        
        return cells;
    }
    
    /**
     * Generate spiral waypoints from center
     */
    private static List<Point2D> generateSpiralWaypoints(Point2D center, double maxRadius, double spacing, 
    double startAngle) {
        List<Point2D> waypoints = new ArrayList<>();
        
        double angle = startAngle;
        double radius = spacing;
        
        // Generate points along spiral until max radius
        while (radius < maxRadius) {
            double x = center.x + radius * Math.cos(angle);
            double y = center.y + radius * Math.sin(angle);
            waypoints.add(new Point2D(x, y));
            
            // Increment angle and radius for spiral
            angle += 0.5;  // Radians per step
            radius += spacing / (2 * Math.PI);  // Gradual radius increase
        }
        
        return waypoints;
    }
    
    /**
     * Generate perimeter positions around center
     */
    private static List<Point2D> generatePerimeterPositions(Point2D center, double radius, int agentCount) {
        List<Point2D> positions = new ArrayList<>();
        
        double angleStep = (2 * Math.PI) / agentCount;
        
        for (int i = 0; i < agentCount; i++) {
            double angle = i * angleStep;
            double x = center.x + radius * Math.cos(angle);
            double y = center.y + radius * Math.sin(angle);
            positions.add(new Point2D(x, y));
        }
        
        return positions;
    }
    
    /**
     * Distribute grid cells among agents
     */
    private void distributeGridCells(List<Point2D> gridCells, int agentCount) {
        int cellsPerAgent = (int) Math.ceil((double) gridCells.size() / agentCount);
        
        for (int i = 0; i < agentCount; i++) {
            List<Point2D> agentCells = new ArrayList<>();
            
            // Assign cells to this agent
            int startIdx = i * cellsPerAgent;
            int endIdx = Math.min(startIdx + cellsPerAgent, gridCells.size());
            
            for (int j = startIdx; j < endIdx; j++) {
                agentCells.add(gridCells.get(j));
            }
            
            agentWaypoints.put(i, agentCells);
            agentCellAssignments.put(i, agentCells.size());
        }
    }
    
    // ==================== PATTERN MANAGEMENT ====================
    
    /**
     * Get waypoints for specific agent
     */
    public List<Point2D> getWaypointsForAgent(int agentId) {
        return agentWaypoints.getOrDefault(agentId, new ArrayList<>());
    }
    
    /**
     * Get next waypoint for agent
     */
    public Point2D getNextWaypoint(int agentId, Point2D currentPosition) {
        List<Point2D> waypoints = agentWaypoints.get(agentId);
        if (waypoints == null || waypoints.isEmpty()) {
            return null;
        }
        
        // For random walk, generate new random waypoint
        if (patternType == SearchPatternType.RANDOM_WALK) {
            return generateRandomWalkWaypoint(agentId, currentPosition);
        }
        
        // For expanding perimeter, generate next perimeter position
        if (patternType == SearchPatternType.EXPANDING_PERIMETER) {
            return generateNextPerimeterPosition(agentId, currentPosition);
        }
        
        // For grid and spiral, return next in sequence
        return waypoints.isEmpty() ? null : waypoints.get(0);
    }
    
    /**
     * Mark waypoint as visited
     */
    public void markVisited(Point2D waypoint, int agentId) {
        String cellKey = getCellKey(waypoint);
        visitedCells.add(cellKey);
        
        // Remove from agent's waypoint list
        List<Point2D> waypoints = agentWaypoints.get(agentId);
        if (waypoints != null) {
            waypoints.remove(waypoint);
        }
        
        // Update coverage percentage
        updateCoveragePercentage();
    }
    
    /**
     * Generate random walk waypoint avoiding visited areas
     */
    private Point2D generateRandomWalkWaypoint(int agentId, Point2D currentPosition) {
        Random rand = new Random();
        Point2D bestWaypoint = null;
        double bestScore = -1;
        
        // Try multiple random directions, pick best (least visited nearby)
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = moveDistance * (0.5 + rand.nextDouble() * 0.5);
            
            double x = currentPosition.x + distance * Math.cos(angle);
            double y = currentPosition.y + distance * Math.sin(angle);
            Point2D candidate = new Point2D(x, y);
            
            // Check if within search area
            if (centerPoint.distanceTo(candidate) > searchRadius) {
                continue;
            }
            
            // Score based on nearby visited cells (fewer = better)
            double score = scoreWaypoint(candidate);
            if (score > bestScore) {
                bestScore = score;
                bestWaypoint = candidate;
            }
        }
        
        return bestWaypoint != null ? bestWaypoint : currentPosition;
    }
    
    /**
     * Generate next expanding perimeter position
     * Uses agentId to preserve each agent's assigned angular sector (based on initial assignment)
     */
    private Point2D generateNextPerimeterPosition(int agentId, Point2D currentPosition) {
      // Defensive checks
      int agents = Math.max(1, this.totalAgents);
      double currentRadius = centerPoint.distanceTo(currentPosition);
      double newRadius = currentRadius + expansionRate;

      // Try to obtain the agent's initially assigned angle (from its first waypoint if present)
      double angle;
      List<Point2D> assigned = agentWaypoints.get(agentId);
      if (assigned != null && !assigned.isEmpty()) {
        Point2D initial = assigned.get(0);
        angle = Math.atan2(initial.y - centerPoint.y, initial.x - centerPoint.x);
      } else {
        // Fallback: evenly space by agentId around circle
        double angleStep = (2 * Math.PI) / agents;
        angle = (agentId % agents) * angleStep;
      }

      double x = centerPoint.x + newRadius * Math.cos(angle);
      double y = centerPoint.y + newRadius * Math.sin(angle);

      return new Point2D(x, y);
    }
    
    /**
     * Score waypoint based on coverage (higher = less visited nearby)
     */
    private double scoreWaypoint(Point2D waypoint) {
        double score = 100.0;
        
        // Reduce score for each nearby visited cell
        for (String cellKey : visitedCells) {
            Point2D visitedCell = parseCellKey(cellKey);
            double distance = waypoint.distanceTo(visitedCell);
            
            if (distance < moveDistance * 2) {
                score -= (moveDistance * 2 - distance) / (moveDistance * 2) * 20;
            }
        }
        
        return Math.max(0, score);
    }
    
    /**
     * Update coverage percentage based on visited cells
     */
    private void updateCoveragePercentage() {
        int totalCells = agentWaypoints.values().stream()
            .mapToInt(List::size)
            .sum() + visitedCells.size();
        
        if (totalCells > 0) {
            coveragePercentage = (double) visitedCells.size() / totalCells * 100;
        }
    }
    
    /**
     * Get cell key for position (for tracking)
     */
    private String getCellKey(Point2D point) {
        int gridX = (int) Math.floor(point.x / 10.0);  // 10-unit grid cells
        int gridY = (int) Math.floor(point.y / 10.0);
        return gridX + "," + gridY;
    }
    
    /**
     * Parse cell key back to approximate position
     */
    private Point2D parseCellKey(String cellKey) {
        // Defensive parsing to avoid NumberFormatException for malformed keys
        if (cellKey == null || cellKey.isEmpty()) {
            double cx = centerPoint != null ? centerPoint.x : 0.0;
            double cy = centerPoint != null ? centerPoint.y : 0.0;
            return new Point2D(cx, cy);
        }
        String[] parts = cellKey.split(",");
        if (parts.length < 2) {
            double cx = centerPoint != null ? centerPoint.x : 0.0;
            double cy = centerPoint != null ? centerPoint.y : 0.0;
            return new Point2D(cx, cy);
        }

        // Trim parts and try to parse safely; provide fallback to centerPoint
        String partX = parts[0] != null ? parts[0].trim() : "";
        String partY = parts[1] != null ? parts[1].trim() : "";
        double cx = centerPoint != null ? centerPoint.x : 0.0;
        double cy = centerPoint != null ? centerPoint.y : 0.0;

        try {
            double gx = Double.parseDouble(partX);
            double gy = Double.parseDouble(partY);
            return new Point2D(gx * 10 + 5, gy * 10 + 5);
        } catch (NumberFormatException e) {
            // Fallback: attempt to extract integer groups from the strings (e.g., "x:12")
            try {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("(-?\\d+)");
                java.util.regex.Matcher mx = p.matcher(partX);
                java.util.regex.Matcher my = p.matcher(partY);
                if (mx.find() && my.find()) {
                    double gx = Double.parseDouble(mx.group(1));
                    double gy = Double.parseDouble(my.group(1));
                    return new Point2D(gx * 10 + 5, gy * 10 + 5);
                }
            } catch (Exception ignored) {
                // ignore and fall through to return center
            }
            return new Point2D(cx, cy);
        }
    }
    
    /**
     * Check if search is complete
     */
    public boolean isSearchComplete() {
        // Check if all waypoints visited
        for (List<Point2D> waypoints : agentWaypoints.values()) {
            if (!waypoints.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get search progress
     */
    public double getProgress() {
        return coveragePercentage;
    }
    
    /**
     * Get search duration
     */
    public long getSearchDuration() {
        return System.currentTimeMillis() - searchStartTime;
    }
    
    @Override
    public String toString() {
        return String.format(
            "SearchPattern[%s: %s | Agents: %d | Coverage: %.1f%% | Duration: %ds]",
            patternId, patternType, totalAgents, coveragePercentage,
            getSearchDuration() / 1000
        );
    }
}