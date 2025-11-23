/**
 * PERFORMANCEOPTIMIZER CLASS - System Performance Enhancement
 *
 * PURPOSE:
 * - Optimize intelligence algorithms for 20+ agent scalability
 * - Implement spatial partitioning for efficient neighbor searches
 * - Cache expensive calculations to reduce redundant computation
 * - Monitor and tune system performance dynamically
 *
 * OPTIMIZATION TECHNIQUES:
 *
 * 1. SPATIAL PARTITIONING (Grid-Based):
 *    - Divide world into grid cells
 *    - Only check agents in nearby cells for neighbors
 *    - Reduces neighbor search from O(n²) to O(n)
 *    - Critical for flocking with many agents
 *
 * 2. CALCULATION CACHING:
 *    - Cache flocking force calculations
 *    - Reuse distance calculations
 *    - Store formation positions between frames
 *    - Cache task allocation scores
 *
 * 3. INCREMENTAL UPDATES:
 *    - Update only changed data
 *    - Partial consensus recalculation
 *    - Delta-based state updates
 *    - Lazy evaluation where possible
 *
 * 4. BATCH PROCESSING:
 *    - Process multiple agents together
 *    - Batch movement commands
 *    - Grouped state updates
 *    - Parallel processing where safe
 *
 * PERFORMANCE TARGETS:
 * - 20 agents: 60 FPS minimum
 * - 30 agents: 45 FPS minimum
 * - 50 agents: 30 FPS minimum
 * - Flocking calculation: < 5ms per agent
 * - Task allocation: < 10ms total
 * - Voting consensus: < 20ms
 *
 * USAGE PATTERNS:
 *
 * Initialize:
 * PerformanceOptimizer optimizer = new PerformanceOptimizer(worldWidth, worldHeight);
 *
 * Spatial Partitioning:
 * optimizer.updateSpatialGrid(agents);
 * List<AgentState> neighbors = optimizer.getNeighborsInRadius(agent, radius);
 *
 * Caching:
 * Vector2D force = optimizer.getCachedFlockingForce(agentId);
 * if (force == null) {
 *     force = calculateFlocking(...);
 *     optimizer.cacheFlockingForce(agentId, force);
 * }
 *
 * Performance Monitoring:
 * optimizer.recordOperationTime("flocking", duration);
 * PerformanceReport report = optimizer.generateReport();
 */
package com.team6.swarm.intelligence.optimization;

import com.team6.swarm.core.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceOptimizer {
    // Spatial partitioning grid
    private SpatialGrid spatialGrid;
    
    // Calculation caches
    private Map<Integer, CachedFlockingData> flockingCache;
    private Map<String, CachedTaskScore> taskScoreCache;
    private Map<Integer, CachedFormationData> formationCache;
    
    // Performance tracking
    private Map<String, PerformanceMetric> operationMetrics;
    private long lastOptimizationCheck;
    private static final long OPTIMIZATION_CHECK_INTERVAL = 5000; // 5 seconds
    
    // Configuration
    private boolean enableCaching;
    private boolean enableSpatialPartitioning;
    private int maxCacheSize;
    private long cacheExpirationTime;
    
    /**
     * Constructor
     */
    public PerformanceOptimizer(double worldWidth, double worldHeight) {
        this.spatialGrid = new SpatialGrid(worldWidth, worldHeight, 100.0); // 100 unit cells
        this.flockingCache = new ConcurrentHashMap<>();
        this.taskScoreCache = new ConcurrentHashMap<>();
        this.formationCache = new ConcurrentHashMap<>();
        this.operationMetrics = new ConcurrentHashMap<>();
        
        // Default configuration
        this.enableCaching = true;
        this.enableSpatialPartitioning = true;
        this.maxCacheSize = 1000;
        this.cacheExpirationTime = 100; // 100ms cache lifetime
        this.lastOptimizationCheck = System.currentTimeMillis();
        
        System.out.println("PerformanceOptimizer initialized:");
        System.out.println("  Spatial Grid: " + spatialGrid.getCellCount() + " cells");
        System.out.println("  Caching: " + (enableCaching ? "Enabled" : "Disabled"));
    }
    
    // ==================== SPATIAL PARTITIONING ====================
    
    /**
     * UPDATE SPATIAL GRID
     * Rebuild spatial grid with current agent positions
     * Call this once per frame before neighbor queries
     */
    public void updateSpatialGrid(List<AgentState> agents) {
        if (!enableSpatialPartitioning) return;
        
        long startTime = System.nanoTime();
        
        spatialGrid.clear();
        for (AgentState agent : agents) {
            spatialGrid.insert(agent);
        }
        
        long duration = System.nanoTime() - startTime;
        recordOperationTime("spatial_grid_update", duration / 1_000_000.0); // Convert to ms
    }
    
    /**
     * GET NEIGHBORS IN RADIUS
     * Fast neighbor lookup using spatial partitioning
     * Replaces O(n²) search with O(1) cell lookup + O(k) neighbor check
     */
    public List<AgentState> getNeighborsInRadius(AgentState agent, double radius) {
        if (!enableSpatialPartitioning) {
            return new ArrayList<>(); // Fallback required
        }
        
        long startTime = System.nanoTime();
        
        List<AgentState> neighbors = spatialGrid.queryRadius(agent.position, radius);
        
        // Remove self from neighbors
        neighbors.removeIf(a -> a.agentId == agent.agentId);
        
        long duration = System.nanoTime() - startTime;
        recordOperationTime("neighbor_query", duration / 1_000_000.0);
        
        return neighbors;
    }
    
    /**
     * GET AGENTS IN AREA
     * Query all agents in rectangular area
     */
    public List<AgentState> getAgentsInArea(Point2D topLeft, Point2D bottomRight) {
        if (!enableSpatialPartitioning) {
            return new ArrayList<>();
        }
        
        return spatialGrid.queryRectangle(topLeft, bottomRight);
    }
    
    // ==================== FLOCKING CACHE ====================
    
    /**
     * CACHE FLOCKING FORCE
     * Store calculated flocking force for reuse
     */
    public void cacheFlockingForce(int agentId, Vector2D force, List<Integer> neighborIds) {
        if (!enableCaching) return;
        
        CachedFlockingData data = new CachedFlockingData();
        data.force = force;
        data.neighborIds = new HashSet<>(neighborIds);
        data.timestamp = System.currentTimeMillis();
        
        flockingCache.put(agentId, data);
        
        // Limit cache size
        if (flockingCache.size() > maxCacheSize) {
            cleanupFlockingCache();
        }
    }
    
    /**
     * GET CACHED FLOCKING FORCE
     * Retrieve cached force if still valid
     */
    public Vector2D getCachedFlockingForce(int agentId, List<Integer> currentNeighborIds) {
        if (!enableCaching) return null;
        
        CachedFlockingData data = flockingCache.get(agentId);
        if (data == null) return null;
        
        // Check if cache expired
        long age = System.currentTimeMillis() - data.timestamp;
        if (age > cacheExpirationTime) {
            flockingCache.remove(agentId);
            return null;
        }
        
        // Check if neighbors changed significantly
        Set<Integer> currentSet = new HashSet<>(currentNeighborIds);
        if (!data.neighborIds.equals(currentSet)) {
            // Neighbors changed, cache invalid
            flockingCache.remove(agentId);
            return null;
        }
        
        return data.force;
    }
    
    /**
     * CLEANUP FLOCKING CACHE
     * Remove expired entries
     */
    private void cleanupFlockingCache() {
        long currentTime = System.currentTimeMillis();
        flockingCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().timestamp > cacheExpirationTime
        );
    }
    
    // ==================== TASK ALLOCATION CACHE ====================
    
    /**
     * CACHE TASK SCORE
     * Store agent suitability score for task
     */
    public void cacheTaskScore(int agentId, String taskId, double score) {
        if (!enableCaching) return;
        
        String key = agentId + "_" + taskId;
        CachedTaskScore data = new CachedTaskScore();
        data.score = score;
        data.timestamp = System.currentTimeMillis();
        
        taskScoreCache.put(key, data);
        
        if (taskScoreCache.size() > maxCacheSize) {
            cleanupTaskScoreCache();
        }
    }
    
    /**
     * GET CACHED TASK SCORE
     */
    public Double getCachedTaskScore(int agentId, String taskId) {
        if (!enableCaching) return null;
        
        String key = agentId + "_" + taskId;
        CachedTaskScore data = taskScoreCache.get(key);
        
        if (data == null) return null;
        
        long age = System.currentTimeMillis() - data.timestamp;
        if (age > cacheExpirationTime * 5) { // Longer expiration for task scores
            taskScoreCache.remove(key);
            return null;
        }
        
        return data.score;
    }
    
    private void cleanupTaskScoreCache() {
        long currentTime = System.currentTimeMillis();
        taskScoreCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().timestamp > cacheExpirationTime * 5
        );
    }
    
    // ==================== FORMATION CACHE ====================
    
    /**
     * CACHE FORMATION POSITION
     * Store calculated formation target positions
     */
    public void cacheFormationPosition(int agentId, Point2D targetPosition, 
                                      String formationId) {
        if (!enableCaching) return;
        
        CachedFormationData data = new CachedFormationData();
        data.targetPosition = targetPosition;
        data.formationId = formationId;
        data.timestamp = System.currentTimeMillis();
        
        formationCache.put(agentId, data);
    }
    
    /**
     * GET CACHED FORMATION POSITION
     */
    public Point2D getCachedFormationPosition(int agentId, String currentFormationId) {
        if (!enableCaching) return null;
        
        CachedFormationData data = formationCache.get(agentId);
        if (data == null) return null;
        
        // Check formation ID matches
        if (!data.formationId.equals(currentFormationId)) {
            formationCache.remove(agentId);
            return null;
        }
        
        long age = System.currentTimeMillis() - data.timestamp;
        if (age > cacheExpirationTime * 2) {
            formationCache.remove(agentId);
            return null;
        }
        
        return data.targetPosition;
    }
    
    // ==================== PERFORMANCE MONITORING ====================
    
    /**
     * RECORD OPERATION TIME
     * Track execution time of operations
     */
    public void recordOperationTime(String operationName, double durationMs) {
        PerformanceMetric metric = operationMetrics.computeIfAbsent(
            operationName, k -> new PerformanceMetric(operationName)
        );
        
        metric.recordExecution(durationMs);
    }
    
    /**
     * GET OPERATION AVERAGE TIME
     */
    public double getAverageOperationTime(String operationName) {
        PerformanceMetric metric = operationMetrics.get(operationName);
        return metric != null ? metric.getAverageTime() : 0.0;
    }
    
    /**
     * GENERATE PERFORMANCE REPORT
     */
    public PerformanceReport generateReport() {
        PerformanceReport report = new PerformanceReport();
        report.timestamp = System.currentTimeMillis();
        
        // Collect metrics
        for (PerformanceMetric metric : operationMetrics.values()) {
            report.addMetric(metric);
        }
        
        // Cache statistics
        report.flockingCacheSize = flockingCache.size();
        report.taskScoreCacheSize = taskScoreCache.size();
        report.formationCacheSize = formationCache.size();
        report.spatialGridCellCount = spatialGrid.getCellCount();
        report.spatialGridAgentCount = spatialGrid.getAgentCount();
        
        // Calculate cache hit rates
        PerformanceMetric neighborMetric = operationMetrics.get("neighbor_query");
        if (neighborMetric != null) {
            report.spatialQueryAvgTime = neighborMetric.getAverageTime();
        }
        
        return report;
    }
    
    /**
     * AUTO-TUNE PERFORMANCE
     * Adjust settings based on performance metrics
     */
    public void autoTunePerformance() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastOptimizationCheck < OPTIMIZATION_CHECK_INTERVAL) {
            return; // Don't check too frequently
        }
        
        lastOptimizationCheck = currentTime;
        
        // Check neighbor query performance
        PerformanceMetric neighborMetric = operationMetrics.get("neighbor_query");
        if (neighborMetric != null) {
            double avgTime = neighborMetric.getAverageTime();
            
            if (avgTime > 5.0 && !enableSpatialPartitioning) {
                // Neighbor queries too slow, enable spatial partitioning
                enableSpatialPartitioning = true;
                System.out.println("Auto-tune: Enabled spatial partitioning (queries too slow)");
            }
        }
        
        // Check cache hit rate
        if (flockingCache.size() > maxCacheSize * 0.9) {
            // Cache almost full, increase size
            maxCacheSize = (int) (maxCacheSize * 1.5);
            System.out.println("Auto-tune: Increased cache size to " + maxCacheSize);
        }
        
        // Check flocking performance
        PerformanceMetric flockingMetric = operationMetrics.get("flocking_calculation");
        if (flockingMetric != null) {
            double avgTime = flockingMetric.getAverageTime();
            
            if (avgTime > 5.0 && !enableCaching) {
                // Flocking too slow, enable caching
                enableCaching = true;
                System.out.println("Auto-tune: Enabled caching (flocking too slow)");
            }
        }
    }
    
    /**
     * CLEAR ALL CACHES
     */
    public void clearCaches() {
        flockingCache.clear();
        taskScoreCache.clear();
        formationCache.clear();
        System.out.println("All caches cleared");
    }
    
    /**
     * RESET METRICS
     */
    public void resetMetrics() {
        operationMetrics.clear();
        System.out.println("Performance metrics reset");
    }
    
    // ==================== CONFIGURATION ====================
    
    public void setEnableCaching(boolean enable) {
        this.enableCaching = enable;
        if (!enable) clearCaches();
    }
    
    public void setEnableSpatialPartitioning(boolean enable) {
        this.enableSpatialPartitioning = enable;
    }
    
    public void setCacheExpirationTime(long milliseconds) {
        this.cacheExpirationTime = milliseconds;
    }
    
    public void setMaxCacheSize(int size) {
        this.maxCacheSize = size;
    }
    
    // Getters
    public boolean isCachingEnabled() { return enableCaching; }
    public boolean isSpatialPartitioningEnabled() { return enableSpatialPartitioning; }
    public int getFlockingCacheSize() { return flockingCache.size(); }
    public int getTaskScoreCacheSize() { return taskScoreCache.size(); }
    public int getFormationCacheSize() { return formationCache.size(); }
}