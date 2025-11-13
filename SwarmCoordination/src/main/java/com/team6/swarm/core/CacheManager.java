package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Week 7 Implementation: CacheManager
 *
 * Intelligent caching system for agent states and spatial data with TTL support,
 * spatial indexing for fast neighbor queries, and sophisticated cache invalidation.
 *
 * Key Features:
 * - Agent state caching with configurable TTL
 * - Spatial indexing using grid-based partitioning
 * - Fast neighbor queries using spatial index
 * - Query result caching
 * - LRU eviction policy
 * - Cache statistics and monitoring
 *
 * @author Team 6
 * @version Week 7
 */
public class CacheManager {

    private static final long DEFAULT_TTL_MS = 5000; // 5 seconds
    private static final int DEFAULT_MAX_CACHE_SIZE = 10000;
    private static final double DEFAULT_GRID_CELL_SIZE = 100.0;

    private final Map<String, CachedAgentState> agentStateCache;
    private final Map<String, CachedQueryResult> queryResultCache;
    private final SpatialIndex spatialIndex;
    private final ReadWriteLock cacheLock;
    private final CacheStatistics statistics;

    private long defaultTTL;
    private int maxCacheSize;
    private double gridCellSize;

    public CacheManager() {
        this(DEFAULT_TTL_MS, DEFAULT_MAX_CACHE_SIZE, DEFAULT_GRID_CELL_SIZE);
    }

    public CacheManager(long defaultTTL, int maxCacheSize, double gridCellSize) {
        this.agentStateCache = new ConcurrentHashMap<>();
        this.queryResultCache = new ConcurrentHashMap<>();
        this.spatialIndex = new SpatialIndex(gridCellSize);
        this.cacheLock = new ReentrantReadWriteLock();
        this.statistics = new CacheStatistics();
        this.defaultTTL = defaultTTL;
        this.maxCacheSize = maxCacheSize;
        this.gridCellSize = gridCellSize;
    }

    public void cacheAgentState(String agentId, AgentState state) {
        cacheAgentState(agentId, state, defaultTTL);
    }

    public void cacheAgentState(String agentId, AgentState state, long ttlMs) {
        cacheLock.writeLock().lock();
        try {
            if (agentStateCache.size() >= maxCacheSize) {
                evictLRUEntry();
            }

            long expiryTime = System.currentTimeMillis() + ttlMs;
            CachedAgentState cached = new CachedAgentState(state, expiryTime);
            agentStateCache.put(agentId, cached);

            if (state.position != null) {
                spatialIndex.insert(agentId, state.position);
            }

            statistics.recordCacheWrite();
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public AgentState getAgentState(String agentId) {
        cacheLock.readLock().lock();
        try {
            CachedAgentState cached = agentStateCache.get(agentId);

            if (cached == null) {
                statistics.recordCacheMiss();
                return null;
            }

            if (cached.isExpired()) {
                statistics.recordCacheMiss();
                return null;
            }

            cached.updateAccessTime();
            statistics.recordCacheHit();
            return cached.state;
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    public List<String> getNearbyAgents(Point2D center, double radius) {
        String queryKey = generateQueryKey(center, radius);
        CachedQueryResult cachedResult = queryResultCache.get(queryKey);

        if (cachedResult != null && !cachedResult.isExpired()) {
            statistics.recordCacheHit();
            return new ArrayList<>(cachedResult.result);
        }

        statistics.recordCacheMiss();

        cacheLock.readLock().lock();
        try {
            List<String> nearbyAgents = spatialIndex.findNearby(center, radius);

            nearbyAgents = nearbyAgents.stream()
                .filter(agentId -> {
                    CachedAgentState cached = agentStateCache.get(agentId);
                    return cached != null && !cached.isExpired();
                })
                .collect(Collectors.toList());

            cacheQueryResult(queryKey, nearbyAgents);

            return nearbyAgents;
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    public void invalidateCache(String agentId) {
        cacheLock.writeLock().lock();
        try {
            CachedAgentState removed = agentStateCache.remove(agentId);
            if (removed != null && removed.state.position != null) {
                spatialIndex.remove(agentId, removed.state.position);
            }

            invalidateQueryCache();
            statistics.recordCacheInvalidation();
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public void invalidateAll() {
        cacheLock.writeLock().lock();
        try {
            agentStateCache.clear();
            queryResultCache.clear();
            spatialIndex.clear();
            statistics.recordCacheInvalidation();
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public void cleanupExpired() {
        cacheLock.writeLock().lock();
        try {
            long now = System.currentTimeMillis();
            List<String> expiredKeys = agentStateCache.entrySet().stream()
                .filter(entry -> entry.getValue().expiryTime < now)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            for (String key : expiredKeys) {
                CachedAgentState removed = agentStateCache.remove(key);
                if (removed != null && removed.state.position != null) {
                    spatialIndex.remove(key, removed.state.position);
                }
            }

            queryResultCache.entrySet().removeIf(entry -> entry.getValue().isExpired());

            statistics.recordCleanup(expiredKeys.size());
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public CacheStatistics getCacheStatistics() {
        cacheLock.readLock().lock();
        try {
            statistics.currentSize = agentStateCache.size();
            statistics.queryResultCacheSize = queryResultCache.size();
            return statistics.copy();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    private void evictLRUEntry() {
        String lruKey = null;
        long oldestAccessTime = Long.MAX_VALUE;

        for (Map.Entry<String, CachedAgentState> entry : agentStateCache.entrySet()) {
            if (entry.getValue().lastAccessTime < oldestAccessTime) {
                oldestAccessTime = entry.getValue().lastAccessTime;
                lruKey = entry.getKey();
            }
        }

        if (lruKey != null) {
            CachedAgentState removed = agentStateCache.remove(lruKey);
            if (removed != null && removed.state.position != null) {
                spatialIndex.remove(lruKey, removed.state.position);
            }
            statistics.recordEviction();
        }
    }

    private void cacheQueryResult(String queryKey, List<String> result) {
        if (queryResultCache.size() >= maxCacheSize / 10) {
            queryResultCache.clear();
        }
        long expiryTime = System.currentTimeMillis() + (defaultTTL / 2);
        queryResultCache.put(queryKey, new CachedQueryResult(result, expiryTime));
    }

    private void invalidateQueryCache() {
        queryResultCache.clear();
    }

    private String generateQueryKey(Point2D center, double radius) {
        return String.format("nearby_%.2f_%.2f_%.2f", center.x, center.y, radius);
    }

    private static class CachedAgentState {
        final AgentState state;
        final long expiryTime;
        long lastAccessTime;

        CachedAgentState(AgentState state, long expiryTime) {
            this.state = state;
            this.expiryTime = expiryTime;
            this.lastAccessTime = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    private static class CachedQueryResult {
        final List<String> result;
        final long expiryTime;

        CachedQueryResult(List<String> result, long expiryTime) {
            this.result = new ArrayList<>(result);
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private static class SpatialIndex {
        private final Map<GridCell, Set<String>> grid;
        private final Map<String, GridCell> agentCells;
        private final double cellSize;

        SpatialIndex(double cellSize) {
            this.grid = new ConcurrentHashMap<>();
            this.agentCells = new ConcurrentHashMap<>();
            this.cellSize = cellSize;
        }

        void insert(String agentId, Point2D position) {
            remove(agentId, position);
            GridCell cell = getCell(position);
            grid.computeIfAbsent(cell, k -> ConcurrentHashMap.newKeySet()).add(agentId);
            agentCells.put(agentId, cell);
        }

        void remove(String agentId, Point2D position) {
            GridCell cell = agentCells.remove(agentId);
            if (cell != null) {
                Set<String> agents = grid.get(cell);
                if (agents != null) {
                    agents.remove(agentId);
                    if (agents.isEmpty()) {
                        grid.remove(cell);
                    }
                }
            }
        }

        List<String> findNearby(Point2D center, double radius) {
            Set<String> result = new HashSet<>();
            int cellRadius = (int) Math.ceil(radius / cellSize);

            GridCell centerCell = getCell(center);

            for (int dx = -cellRadius; dx <= cellRadius; dx++) {
                for (int dy = -cellRadius; dy <= cellRadius; dy++) {
                    GridCell cell = new GridCell(centerCell.x + dx, centerCell.y + dy);
                    Set<String> agents = grid.get(cell);
                    if (agents != null) {
                        result.addAll(agents);
                    }
                }
            }

            return new ArrayList<>(result);
        }

        void clear() {
            grid.clear();
            agentCells.clear();
        }

        private GridCell getCell(Point2D position) {
            int x = (int) Math.floor(position.x / cellSize);
            int y = (int) Math.floor(position.y / cellSize);
            return new GridCell(x, y);
        }
    }

    private static class GridCell {
        final int x;
        final int y;

        GridCell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GridCell)) return false;
            GridCell gridCell = (GridCell) o;
            return x == gridCell.x && y == gridCell.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public static class CacheStatistics {
        private long hits = 0;
        private long misses = 0;
        private long writes = 0;
        private long invalidations = 0;
        private long evictions = 0;
        private long cleanups = 0;
        private int currentSize = 0;
        private int queryResultCacheSize = 0;

        void recordCacheHit() { hits++; }
        void recordCacheMiss() { misses++; }
        void recordCacheWrite() { writes++; }
        void recordCacheInvalidation() { invalidations++; }
        void recordEviction() { evictions++; }
        void recordCleanup(int count) { cleanups += count; }

        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public long getWrites() { return writes; }
        public long getInvalidations() { return invalidations; }
        public long getEvictions() { return evictions; }
        public long getCleanups() { return cleanups; }
        public int getCurrentSize() { return currentSize; }
        public int getQueryResultCacheSize() { return queryResultCacheSize; }

        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }

        public CacheStatistics copy() {
            CacheStatistics copy = new CacheStatistics();
            copy.hits = this.hits;
            copy.misses = this.misses;
            copy.writes = this.writes;
            copy.invalidations = this.invalidations;
            copy.evictions = this.evictions;
            copy.cleanups = this.cleanups;
            copy.currentSize = this.currentSize;
            copy.queryResultCacheSize = this.queryResultCacheSize;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("CacheStatistics[Size: %d, Hits: %d, Misses: %d, Hit Rate: %.2f%%]",
                currentSize, hits, misses, getHitRate() * 100);
        }
    }

    public void setDefaultTTL(long ttlMs) { this.defaultTTL = ttlMs; }
    public void setMaxCacheSize(int size) { this.maxCacheSize = size; }
    public long getDefaultTTL() { return defaultTTL; }
    public int getMaxCacheSize() { return maxCacheSize; }
}
