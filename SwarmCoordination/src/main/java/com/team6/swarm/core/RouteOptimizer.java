package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Week 7 Implementation: RouteOptimizer
 *
 * Advanced route optimization with A* pathfinding, dynamic obstacle avoidance,
 * and multi-agent coordination for efficient navigation.
 *
 * Key Features:
 * - A* pathfinding algorithm
 * - Dynamic route recalculation
 * - Obstacle detection and avoidance
 * - Multi-agent load balancing
 * - Route caching and optimization
 * - Collision detection
 *
 * @author Team 6
 * @version Week 7
 */
public class RouteOptimizer {

    private static final double DEFAULT_GRID_RESOLUTION = 10.0;
    private static final double DEFAULT_HEURISTIC_WEIGHT = 1.0;
    private static final int DEFAULT_MAX_PATH_LENGTH = 1000;

    private final Map<String, Route> routeCache;
    private final Map<Point2D, Double> obstacleMap;
    private final RouteStatistics statistics;

    private double gridResolution;
    private double heuristicWeight;
    private int maxPathLength;

    public RouteOptimizer() {
        this(DEFAULT_GRID_RESOLUTION, DEFAULT_HEURISTIC_WEIGHT, DEFAULT_MAX_PATH_LENGTH);
    }

    public RouteOptimizer(double gridResolution, double heuristicWeight, int maxPathLength) {
        this.routeCache = new ConcurrentHashMap<>();
        this.obstacleMap = new ConcurrentHashMap<>();
        this.statistics = new RouteStatistics();
        this.gridResolution = gridResolution;
        this.heuristicWeight = heuristicWeight;
        this.maxPathLength = maxPathLength;
    }

    public Route calculateOptimalRoute(Point2D start, Point2D goal) {
        return calculateOptimalRoute(start, goal, Collections.emptyList());
    }

    public Route calculateOptimalRoute(Point2D start, Point2D goal, List<Point2D> obstacles) {
        long startTime = System.nanoTime();

        String cacheKey = generateCacheKey(start, goal);
        Route cachedRoute = routeCache.get(cacheKey);

        if (cachedRoute != null && isRouteValid(cachedRoute, obstacles)) {
            statistics.recordCacheHit();
            return cachedRoute;
        }

        statistics.recordCacheMiss();

        // Update obstacle map
        updateObstacleMap(obstacles);

        // Run A* algorithm
        List<Point2D> path = aStar(start, goal);

        if (path == null || path.isEmpty()) {
            statistics.recordRouteFailed();
            return null;
        }

        double distance = calculatePathDistance(path);
        long duration = (System.nanoTime() - startTime) / 1000000;

        Route route = new Route(path, distance, duration);
        routeCache.put(cacheKey, route);

        statistics.recordRouteCalculated(duration);
        return route;
    }

    public Route optimizeExistingRoute(Route route, List<Point2D> obstacles) {
        if (route == null || route.path.isEmpty()) {
            return null;
        }

        Point2D start = route.path.get(0);
        Point2D goal = route.path.get(route.path.size() - 1);

        return calculateOptimalRoute(start, goal, obstacles);
    }

    public List<Route> calculateMultiAgentRoutes(List<AgentRouteRequest> requests) {
        List<Route> routes = new ArrayList<>();
        List<Point2D> allObstacles = new ArrayList<>();

        // Collect all obstacles
        for (AgentRouteRequest request : requests) {
            allObstacles.addAll(request.obstacles);
        }

        // Calculate routes with priority ordering
        requests.sort(Comparator.comparingInt(r -> r.priority.value));

        for (AgentRouteRequest request : requests) {
            Route route = calculateOptimalRoute(request.start, request.goal, allObstacles);
            if (route != null) {
                routes.add(route);
                // Add route waypoints as temporary obstacles for other agents
                allObstacles.addAll(route.path);
            }
        }

        return routes;
    }

    public boolean detectCollision(Route route1, Route route2) {
        if (route1 == null || route2 == null) {
            return false;
        }

        int maxSteps = Math.max(route1.path.size(), route2.path.size());

        for (int i = 0; i < maxSteps; i++) {
            Point2D p1 = getPathPoint(route1.path, i);
            Point2D p2 = getPathPoint(route2.path, i);

            if (p1 != null && p2 != null && p1.distanceTo(p2) < gridResolution) {
                return true;
            }
        }

        return false;
    }

    public Route smoothRoute(Route route) {
        if (route == null || route.path.size() < 3) {
            return route;
        }

        List<Point2D> smoothed = new ArrayList<>();
        smoothed.add(route.path.get(0));

        for (int i = 1; i < route.path.size() - 1; i++) {
            Point2D prev = route.path.get(i - 1);
            Point2D curr = route.path.get(i);
            Point2D next = route.path.get(i + 1);

            // Apply simple averaging smoothing
            double smoothX = (prev.x + curr.x + next.x) / 3.0;
            double smoothY = (prev.y + curr.y + next.y) / 3.0;

            smoothed.add(new Point2D(smoothX, smoothY));
        }

        smoothed.add(route.path.get(route.path.size() - 1));

        double distance = calculatePathDistance(smoothed);
        return new Route(smoothed, distance, route.calculationTimeMs);
    }

    public void addObstacle(Point2D location, double severity) {
        obstacleMap.put(location, severity);
        invalidateAffectedRoutes(location);
    }

    public void removeObstacle(Point2D location) {
        obstacleMap.remove(location);
    }

    public void clearObstacles() {
        obstacleMap.clear();
        routeCache.clear();
    }

    public void clearRouteCache() {
        routeCache.clear();
    }

    public RouteStatistics getStatistics() {
        return statistics.copy();
    }

    private List<Point2D> aStar(Point2D start, Point2D goal) {
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        Map<Point2D, AStarNode> allNodes = new HashMap<>();
        Set<Point2D> closedSet = new HashSet<>();

        AStarNode startNode = new AStarNode(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        int iterations = 0;

        while (!openSet.isEmpty() && iterations < maxPathLength) {
            iterations++;

            AStarNode current = openSet.poll();

            if (current.position.distanceTo(goal) < gridResolution) {
                return reconstructPath(current);
            }

            closedSet.add(current.position);

            for (Point2D neighbor : getNeighbors(current.position)) {
                if (closedSet.contains(neighbor) || isObstacle(neighbor)) {
                    continue;
                }

                double tentativeG = current.g + current.position.distanceTo(neighbor);
                AStarNode neighborNode = allNodes.get(neighbor);

                if (neighborNode == null) {
                    neighborNode = new AStarNode(neighbor, current, tentativeG, heuristic(neighbor, goal));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeG < neighborNode.g) {
                    openSet.remove(neighborNode);
                    neighborNode.parent = current;
                    neighborNode.g = tentativeG;
                    neighborNode.f = tentativeG + neighborNode.h;
                    openSet.add(neighborNode);
                }
            }
        }

        return null; // No path found
    }

    private List<Point2D> reconstructPath(AStarNode node) {
        List<Point2D> path = new ArrayList<>();
        AStarNode current = node;

        while (current != null) {
            path.add(0, current.position);
            current = current.parent;
        }

        return path;
    }

    private List<Point2D> getNeighbors(Point2D point) {
        List<Point2D> neighbors = new ArrayList<>();

        // 8-directional movement
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                double newX = point.x + dx * gridResolution;
                double newY = point.y + dy * gridResolution;
                neighbors.add(new Point2D(newX, newY));
            }
        }

        return neighbors;
    }

    private double heuristic(Point2D from, Point2D to) {
        return from.distanceTo(to) * heuristicWeight;
    }

    private boolean isObstacle(Point2D point) {
        // Check if point is too close to any obstacle
        for (Map.Entry<Point2D, Double> entry : obstacleMap.entrySet()) {
            double distance = point.distanceTo(entry.getKey());
            if (distance < gridResolution * entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    private void updateObstacleMap(List<Point2D> obstacles) {
        for (Point2D obstacle : obstacles) {
            obstacleMap.putIfAbsent(obstacle, 1.0);
        }
    }

    private boolean isRouteValid(Route route, List<Point2D> obstacles) {
        for (Point2D waypoint : route.path) {
            for (Point2D obstacle : obstacles) {
                if (waypoint.distanceTo(obstacle) < gridResolution) {
                    return false;
                }
            }
        }
        return true;
    }

    private double calculatePathDistance(List<Point2D> path) {
        if (path == null || path.size() < 2) {
            return 0.0;
        }

        double distance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += path.get(i).distanceTo(path.get(i + 1));
        }
        return distance;
    }

    private Point2D getPathPoint(List<Point2D> path, int index) {
        if (index < 0 || index >= path.size()) {
            return path.get(path.size() - 1);
        }
        return path.get(index);
    }

    private String generateCacheKey(Point2D start, Point2D goal) {
        return String.format("%.2f_%.2f_%.2f_%.2f", start.x, start.y, goal.x, goal.y);
    }

    private void invalidateAffectedRoutes(Point2D obstacle) {
        routeCache.entrySet().removeIf(entry -> {
            for (Point2D waypoint : entry.getValue().path) {
                if (waypoint.distanceTo(obstacle) < gridResolution * 2) {
                    return true;
                }
            }
            return false;
        });
    }

    public void setGridResolution(double resolution) {
        this.gridResolution = resolution;
    }

    public void setHeuristicWeight(double weight) {
        this.heuristicWeight = weight;
    }

    public double getGridResolution() {
        return gridResolution;
    }

    private static class AStarNode implements Comparable<AStarNode> {
        Point2D position;
        AStarNode parent;
        double g; // Cost from start
        double h; // Heuristic to goal
        double f; // Total cost

        AStarNode(Point2D position, AStarNode parent, double g, double h) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.f, other.f);
        }
    }

    public static class Route {
        public final List<Point2D> path;
        public final double totalDistance;
        public final long calculationTimeMs;

        public Route(List<Point2D> path, double totalDistance, long calculationTimeMs) {
            this.path = new ArrayList<>(path);
            this.totalDistance = totalDistance;
            this.calculationTimeMs = calculationTimeMs;
        }

        @Override
        public String toString() {
            return String.format("Route[Waypoints: %d, Distance: %.2f, Time: %dms]",
                path.size(), totalDistance, calculationTimeMs);
        }
    }

    public static class AgentRouteRequest {
        public final String agentId;
        public final Point2D start;
        public final Point2D goal;
        public final List<Point2D> obstacles;
        public final RoutePriority priority;

        public AgentRouteRequest(String agentId, Point2D start, Point2D goal,
                                List<Point2D> obstacles, RoutePriority priority) {
            this.agentId = agentId;
            this.start = start;
            this.goal = goal;
            this.obstacles = new ArrayList<>(obstacles);
            this.priority = priority;
        }
    }

    public enum RoutePriority {
        LOW(3),
        NORMAL(2),
        HIGH(1),
        CRITICAL(0);

        public final int value;

        RoutePriority(int value) {
            this.value = value;
        }
    }

    public static class RouteStatistics {
        private long routesCalculated = 0;
        private long routesFailed = 0;
        private long cacheHits = 0;
        private long cacheMisses = 0;
        private long totalCalculationTimeMs = 0;

        void recordRouteCalculated(long calculationTimeMs) {
            routesCalculated++;
            totalCalculationTimeMs += calculationTimeMs;
        }

        void recordRouteFailed() {
            routesFailed++;
        }

        void recordCacheHit() {
            cacheHits++;
        }

        void recordCacheMiss() {
            cacheMisses++;
        }

        public long getRoutesCalculated() { return routesCalculated; }
        public long getRoutesFailed() { return routesFailed; }
        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }

        public double getAverageCalculationTimeMs() {
            return routesCalculated > 0 ? (double) totalCalculationTimeMs / routesCalculated : 0.0;
        }

        public double getCacheHitRate() {
            long total = cacheHits + cacheMisses;
            return total > 0 ? (double) cacheHits / total : 0.0;
        }

        public RouteStatistics copy() {
            RouteStatistics copy = new RouteStatistics();
            copy.routesCalculated = this.routesCalculated;
            copy.routesFailed = this.routesFailed;
            copy.cacheHits = this.cacheHits;
            copy.cacheMisses = this.cacheMisses;
            copy.totalCalculationTimeMs = this.totalCalculationTimeMs;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("RouteStatistics[Calculated: %d, Failed: %d, Cache Hit Rate: %.1f%%, Avg Time: %.2fms]",
                routesCalculated, routesFailed, getCacheHitRate() * 100, getAverageCalculationTimeMs());
        }
    }
}
