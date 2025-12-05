package com.team6.demo.obstacles;

import com.team6.demo.core.Position;
import com.team6.swarm.core.Vector2D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ObstacleManager - Manages all obstacles in the environment
 * Provides spatial queries and collision detection
 */
public class ObstacleManager {
    private final Map<Integer, Obstacle> obstacles;
    private final List<Obstacle> dynamicObstacles;

    public ObstacleManager() {
        this.obstacles = new ConcurrentHashMap<>();
        this.dynamicObstacles = new ArrayList<>();
    }

    /**
     * Add an obstacle to the environment
     */
    public void addObstacle(Obstacle obstacle) {
        obstacles.put(obstacle.getObstacleId(), obstacle);
        if (obstacle.getType() == ObstacleType.DYNAMIC) {
            dynamicObstacles.add(obstacle);
        }
    }

    /**
     * Remove an obstacle
     */
    public void removeObstacle(int obstacleId) {
        Obstacle removed = obstacles.remove(obstacleId);
        if (removed != null && removed.getType() == ObstacleType.DYNAMIC) {
            dynamicObstacles.remove(removed);
        }
    }

    /**
     * Get all obstacles within a radius of a position
     */
    public List<Obstacle> getObstaclesInRadius(Position position, double radius) {
        List<Obstacle> result = new ArrayList<>();
        for (Obstacle obstacle : obstacles.values()) {
            if (position.distanceTo(obstacle.getPosition()) <= radius) {
                result.add(obstacle);
            }
        }
        return result;
    }

    /**
     * Check if a position collides with any obstacle
     */
    public boolean checkCollision(Position position) {
        for (Obstacle obstacle : obstacles.values()) {
            if (obstacle.containsPoint(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if path between two positions is clear
     * Simple implementation: samples points along the path
     */
    public boolean getPathClear(Position start, Position end) {
        int samples = 10;
        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            double x = start.x + (end.x - start.x) * t;
            double y = start.y + (end.y - start.y) * t;
            double z = start.z + (end.z - start.z) * t;
            Position sample = new Position(x, y, z);

            if (checkCollision(sample)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Update all dynamic obstacles
     */
    public void updateDynamicObstacles(double deltaTime) {
        for (Obstacle obstacle : dynamicObstacles) {
            obstacle.update(deltaTime);
        }
    }

    /**
     * Get avoidance vector for a position considering all nearby obstacles
     */
    public Vector2D getAvoidanceVector(Position dronePosition, double detectionRadius) {
        List<Obstacle> nearbyObstacles = getObstaclesInRadius(dronePosition, detectionRadius);

        if (nearbyObstacles.isEmpty()) {
            return new Vector2D(0, 0);
        }

        // Combine avoidance vectors from all nearby obstacles
        Vector2D totalAvoidance = new Vector2D(0, 0);
        for (Obstacle obstacle : nearbyObstacles) {
            Vector2D avoidanceVec = obstacle.getAvoidanceVector(dronePosition);
            totalAvoidance = totalAvoidance.add(avoidanceVec);
        }

        return totalAvoidance;
    }

    /**
     * Get obstacles near a path (within detection radius of the path)
     */
    public List<Obstacle> getObstaclesNearPath(Position start, Position end, double detectionRadius) {
        List<Obstacle> nearbyObstacles = new ArrayList<>();

        for (Obstacle obstacle : obstacles.values()) {
            // Check if obstacle is close to any point along the path
            int samples = 10;
            for (int i = 0; i <= samples; i++) {
                double t = i / (double) samples;
                double x = start.x + (end.x - start.x) * t;
                double y = start.y + (end.y - start.y) * t;
                double z = start.z + (end.z - start.z) * t;
                Position pathPoint = new Position(x, y, z);

                if (pathPoint.distanceTo(obstacle.getPosition()) <= detectionRadius) {
                    if (!nearbyObstacles.contains(obstacle)) {
                        nearbyObstacles.add(obstacle);
                    }
                    break;
                }
            }
        }

        return nearbyObstacles;
    }

    // Getters
    public Collection<Obstacle> getAllObstacles() {
        return obstacles.values();
    }

    public int getObstacleCount() {
        return obstacles.size();
    }

    public Obstacle getObstacle(int obstacleId) {
        return obstacles.get(obstacleId);
    }
}
