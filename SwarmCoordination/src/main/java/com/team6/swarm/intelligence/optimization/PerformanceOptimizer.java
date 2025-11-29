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
 * INTEGRATION WITH EXISTING CLASSES:
 * - Uses CacheManager from Week 7 for caching
 * - Uses PerformanceMonitor from Week 5 for metrics
 * - Uses RouteOptimizer from Week 7 for pathfinding
 * - Uses MetricsCollector from Week 8 for data collection
 */
package com.team6.swarm.intelligence.optimization;

import com.team6.swarm.core.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceOptimizer {
    // Spatial partitioning grid
    private SpatialGrid spatialGrid;
    
    // Use existing CacheManager from Week 7
    private CacheManager cacheManager;
    
    // Use existing PerformanceMonitor from Week 5
    private PerformanceMonitor performanceMonitor;
    
    // Use existing MetricsCollector from Week 8
    private MetricsCollector metricsCollector;
    
    // Custom caches for flocking-specific data
    private Map<Integer, CachedFlockingData> flockingCache;
    // Task score cache and formation cache (local optimization caches)
    private Map<String, Double> taskScoreCache;
    private Map<Integer, Point2D> formationCache;
    // Operation timing metrics
    private Map<String, PerformanceMetric> operationMetrics;
    // Delegate to core PerformanceOptimizer for system-level optimization
    private com.team6.swarm.core.PerformanceOptimizer coreOptimizer;
    
    // Configuration
    private boolean enableCaching;
    private boolean enableSpatialPartitioning;
    private int maxCacheSize;
    private long cacheExpirationTime;
    private long lastOptimizationCheck;
    private static final long OPTIMIZATION_CHECK_INTERVAL = 5000; // 5 seconds
    
    /**
     * Constructor
     */
    public PerformanceOptimizer(double worldWidth, double worldHeight) {
        this.spatialGrid = new SpatialGrid(worldWidth, worldHeight, 100.0); // 100 unit cells
        this.cacheManager = new CacheManager();
        this.performanceMonitor = PerformanceMonitor.getInstance();
        this.metricsCollector = new MetricsCollector();
        this.flockingCache = new ConcurrentHashMap<>();
        this.taskScoreCache = new ConcurrentHashMap<>();
        this.formationCache = new ConcurrentHashMap<>();
        this.operationMetrics = new ConcurrentHashMap<>();
        this.coreOptimizer = new com.team6.swarm.core.PerformanceOptimizer();
        
        // Default configuration
        this.enableCaching = true;
        this.enableSpatialPartitioning = true;
        this.maxCacheSize = 1000;
        this.cacheExpirationTime = 100; // 100ms cache lifetime
        this.lastOptimizationCheck = System.currentTimeMillis();
        
        System.out.println("PerformanceOptimizer initialized:");
        System.out.println("  Spatial Grid: " + spatialGrid.getCellCount() + " cells");
        System.out.println("  Caching: " + (enableCaching ? "Enabled" : "Disabled"));
        System.out.println("  Integrated with existing Week 5-8 systems");
    }
    
    // ==================== SPATIAL PARTITIONING ====================
    
    /**
     * UPDATE SPATIAL GRID
     * Rebuild spatial grid with current agent positions
     * Call this once per frame before neighbor queries
     */
    public void updateSpatialGrid(List<AgentState> agents) {
        if (!enableSpatialPartitioning) return;
        
        performanceMonitor.startFrame(); // Use existing PerformanceMonitor
        
        spatialGrid.clear();
        for (AgentState agent : agents) {
            spatialGrid.insert(agent);
            // Also cache in CacheManager for consistency
            cacheManager.cacheAgentState(String.valueOf(agent.agentId), agent);
        }
        
        performanceMonitor.endFrame();
        metricsCollector.recordMetric("spatial_grid_updates", spatialGrid.getAgentCount());
    }
    
    /**
     * GET NEIGHBORS IN RADIUS
     * Fast neighbor lookup using spatial partitioning
     * Also leverages CacheManager for additional speedup
     */
    public List<AgentState> getNeighborsInRadius(AgentState agent, double radius) {
        if (!enableSpatialPartitioning) {
            // Fallback to CacheManager spatial query
            return new ArrayList<>(cacheManager.getNearbyAgents(agent.position, radius)
                .stream()
                .map(id -> cacheManager.getAgentState(id))
                .filter(a -> a != null)
                .toList());
        }
        
        List<AgentState> neighbors = spatialGrid.queryRadius(agent.position, radius);
        neighbors.removeIf(a -> a.agentId == agent.agentId);
        
        metricsCollector.recordMetric("neighbor_queries", neighbors.size());
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
        data.setForce(force);
        data.setNeighborIds(new HashSet<>(neighborIds));
        data.setTimestamp(System.currentTimeMillis());
        
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
        long age = System.currentTimeMillis() - data.getTimestamp();
        if (age > cacheExpirationTime) {
            flockingCache.remove(agentId);
            return null;
        }
        
        // Check if neighbors changed significantly
        Set<Integer> currentSet = new HashSet<>(currentNeighborIds);
        if (!data.getNeighborIds().equals(currentSet)) {
            // Neighbors changed, cache invalid
            flockingCache.remove(agentId);
            return null;
        }
        
        return data.getForce();
    }
    
    /**
     * CLEANUP FLOCKING CACHE
     * Remove expired entries
     */
    private void cleanupFlockingCache() {
        long currentTime = System.currentTimeMillis();
        flockingCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getTimestamp() > cacheExpirationTime
        );
    }
    
    // ==================== TASK ALLOCATION CACHE ====================
    
    /**
     * CACHE TASK SCORE - Uses existing CacheManager
     */
    public void cacheTaskScore(int agentId, String taskId, double score) {
        if (!enableCaching) return;
        
        // Store in metrics collector for tracking
        metricsCollector.recordMetric("task_score_" + agentId + "_" + taskId, score);
        // Also store locally for quick access/reporting
        taskScoreCache.put(agentId + ":" + taskId, score);
    }
    
    /**
     * GET CACHED TASK SCORE
     */
    public Double getCachedTaskScore(int agentId, String taskId) {
        if (!enableCaching) return null;
        
        // Retrieve from metrics collector
        MetricsCollector.DataPoint latestScore = metricsCollector.getRecentDataPoints(
            "task_score_" + agentId + "_" + taskId, 1
        ).stream().findFirst().orElse(null);
        
        if (latestScore == null) return null;
        
        // Check if too old
        long age = System.currentTimeMillis() - latestScore.timestamp;
        if (age > cacheExpirationTime * 5) {
            return null;
        }
        
        return latestScore.value;
    }
    
    // ==================== FORMATION CACHE ====================
    
    /**
     * CACHE FORMATION POSITION - Uses RouteOptimizer for pathfinding cache
     */
    public void cacheFormationPosition(int agentId, Point2D targetPosition, 
                                      String formationId) {
        if (!enableCaching) return;
        
        // Store as metric for tracking
        metricsCollector.recordMetric("formation_x_" + agentId, targetPosition.x);
        metricsCollector.recordMetric("formation_y_" + agentId, targetPosition.y);
        // Also cache the target position locally
        formationCache.put(agentId, targetPosition);
    }
    
    /**
     * GET CACHED FORMATION POSITION
     */
    public Point2D getCachedFormationPosition(int agentId, String currentFormationId) {
        if (!enableCaching) return null;
        
        List<MetricsCollector.DataPoint> xData = metricsCollector.getRecentDataPoints(
            "formation_x_" + agentId, 1);
        List<MetricsCollector.DataPoint> yData = metricsCollector.getRecentDataPoints(
            "formation_y_" + agentId, 1);
        
        if (xData.isEmpty() || yData.isEmpty()) return null;
        
        long age = System.currentTimeMillis() - xData.get(0).timestamp;
        if (age > cacheExpirationTime * 2) {
            return null;
        }
        
        return new Point2D(xData.get(0).value, yData.get(0).value);
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
        // Build a local performance report that supplements core optimizer's report
        PerformanceReport report = new PerformanceReport();
        report.timestamp = System.currentTimeMillis();

        // Collect local timing metrics
        for (PerformanceMetric metric : operationMetrics.values()) {
            report.addMetric(metric);
        }

        // Cache statistics
        report.flockingCacheSize = flockingCache.size();
        report.taskScoreCacheSize = taskScoreCache.size();
        report.formationCacheSize = formationCache.size();
        report.spatialGridCellCount = spatialGrid.getCellCount();
        report.spatialGridAgentCount = spatialGrid.getAgentCount();

        // Calculate neighbor query average time if available
        PerformanceMetric neighborMetric = operationMetrics.get("neighbor_query");
        if (neighborMetric != null) {
            report.spatialQueryAvgTime = neighborMetric.getAverageTime();
        }

        // Attach core optimizer report for system-level visibility (optional)
        report.coreReport = coreOptimizer.getOptimizationReport();

        return report;
    }
    
    /**
     * AUTO-TUNE PERFORMANCE
     * Adjust settings based on performance metrics from existing systems
     */
    public void autoTunePerformance() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastOptimizationCheck < OPTIMIZATION_CHECK_INTERVAL) {
            return;
        }
        
        lastOptimizationCheck = currentTime;
        
        // Use PerformanceMonitor for FPS-based tuning
        PerformanceMonitor.PerformanceMetrics metrics = performanceMonitor.getCurrentMetrics();
        
        if (metrics.currentFPS < 30.0 && !enableSpatialPartitioning) {
            enableSpatialPartitioning = true;
            System.out.println("Auto-tune: Enabled spatial partitioning (FPS too low)");
        }
        
        if (metrics.currentFPS < 20.0 && !enableCaching) {
            enableCaching = true;
            System.out.println("Auto-tune: Enabled caching (FPS critical)");
        }
        
        // Check cache performance
        CacheManager.CacheStatistics cacheStats = cacheManager.getCacheStatistics();
        if (cacheStats.getHitRate() < 0.3) {
            // Low hit rate, increase cache expiration time
            cacheExpirationTime = Math.min(500, cacheExpirationTime * 2);
            System.out.println("Auto-tune: Increased cache expiration to " + cacheExpirationTime + "ms");
        }
    }
    
    /**
     * CLEAR ALL CACHES
     */
    public void clearCaches() {
        flockingCache.clear();
        cacheManager.invalidateAll();
        System.out.println("All caches cleared (local + CacheManager invalidated)");
    }
    
    /**
     * RESET METRICS
     */
    public void resetMetrics() {
        performanceMonitor.reset();
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
    public CacheManager getCacheManager() { return cacheManager; }
    public PerformanceMonitor getPerformanceMonitor() { return performanceMonitor; }
    public MetricsCollector getMetricsCollector() { return metricsCollector; }

    // Getters for cache sizes
    public int getFlockingCacheSize() { return flockingCache.size(); }
    public int getTaskScoreCacheSize() { return taskScoreCache.size(); }
    public int getFormationCacheSize() { return formationCache.size(); }

    // Expose core optimizer capabilities
    public com.team6.swarm.core.PerformanceOptimizer.OptimizationResult optimize() {
        return coreOptimizer.optimize();
    }

    public com.team6.swarm.core.PerformanceOptimizer.OptimizationReport getCoreOptimizationReport() {
        return coreOptimizer.getOptimizationReport();
    }

    public void setCoreAutoTuningEnabled(boolean enabled) {
        coreOptimizer.setAutoTuningEnabled(enabled);
    }

    public Object getCoreParameter(String key) { return coreOptimizer.getParameter(key); }
    public void setCoreParameter(String key, Object value) { coreOptimizer.setParameter(key, value); }

    // Simple local metric class for recording operation timings
    private static class PerformanceMetric {
        private final String name;
        private final List<Double> values = new ArrayList<>();
        private final int maxSize = 200;

        public PerformanceMetric(String name) {
            this.name = name;
        }

        public void recordExecution(double durationMs) {
            values.add(durationMs);
            while (values.size() > maxSize) values.remove(0);
        }

        public double getAverageTime() {
            return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        @Override
        public String toString() {
            return String.format("%s: avg=%.3fms, samples=%d", name, getAverageTime(), values.size());
        }
    }

    // Lightweight performance report that combines local optimizer stats with core report
    public static class PerformanceReport {
        public long timestamp;
        public final List<PerformanceMetric> metrics = new ArrayList<>();
        public int flockingCacheSize;
        public int taskScoreCacheSize;
        public int formationCacheSize;
        public int spatialGridCellCount;
        public int spatialGridAgentCount;
        public double spatialQueryAvgTime;
        public com.team6.swarm.core.PerformanceOptimizer.OptimizationReport coreReport;

        public void addMetric(PerformanceMetric metric) { metrics.add(metric); }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("PerformanceReport @ ").append(timestamp).append("\n");
            sb.append("FlockingCache=" + flockingCacheSize + ", TaskScoreCache=" + taskScoreCacheSize + ", FormationCache=" + formationCacheSize + "\n");
            sb.append("SpatialGrid: cells=" + spatialGridCellCount + ", agents=" + spatialGridAgentCount + ", neighborAvgMs=" + String.format("%.3f", spatialQueryAvgTime) + "\n");
            for (PerformanceMetric m : metrics) sb.append(m.toString()).append("\n");
            if (coreReport != null) sb.append("CORE:").append(coreReport.toString()).append("\n");
            return sb.toString();
        }
    }
}