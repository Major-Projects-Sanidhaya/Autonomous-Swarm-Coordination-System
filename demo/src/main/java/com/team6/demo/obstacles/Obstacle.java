package com.team6.demo.obstacles;

import com.team6.demo.core.Position;

/**
 * Obstacle - Base class for all obstacles in the environment
 * Obstacles can be static or dynamic and have different risk levels
 */
public abstract class Obstacle {
    protected int obstacleId;
    protected Position position;
    protected ObstacleType type;
    protected RiskLevel riskLevel;
    protected String name;

    private static int nextId = 1;

    public Obstacle(Position position, ObstacleType type, RiskLevel riskLevel, String name) {
        this.obstacleId = nextId++;
        this.position = position;
        this.type = type;
        this.riskLevel = riskLevel;
        this.name = name;
    }

    /**
     * Check if a point is inside this obstacle
     */
    public abstract boolean containsPoint(Position point);

    /**
     * Get the closest point on the obstacle surface to the given position
     */
    public abstract Position getClosestPointTo(Position dronePosition);

    /**
     * Get a vector pointing away from the obstacle for avoidance
     */
    public abstract com.team6.swarm.core.Vector2D getAvoidanceVector(Position dronePosition);

    /**
     * Update dynamic obstacles (default: no-op for static obstacles)
     */
    public void update(double deltaTime) {
        // Override in dynamic obstacles
    }

    // Getters
    public int getObstacleId() { return obstacleId; }
    public Position getPosition() { return position; }
    public ObstacleType getType() { return type; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return String.format("%s[id=%d, type=%s, pos=%s]",
            name, obstacleId, type, position);
    }
}
