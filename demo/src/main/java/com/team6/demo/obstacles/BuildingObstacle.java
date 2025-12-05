package com.team6.demo.obstacles;

import com.team6.demo.core.Position;
import com.team6.swarm.core.Vector2D;

/**
 * BuildingObstacle - Rectangular prism obstacle (buildings, structures)
 */
public class BuildingObstacle extends Obstacle {
    private final double minX, maxX, minY, maxY;
    private final double height;

    public BuildingObstacle(double x1, double y1, double x2, double y2, double height, String name) {
        super(new Position((x1 + x2) / 2, (y1 + y2) / 2, height / 2),
              ObstacleType.STATIC, RiskLevel.HIGH, name);
        this.minX = Math.min(x1, x2);
        this.maxX = Math.max(x1, x2);
        this.minY = Math.min(y1, y2);
        this.maxY = Math.max(y1, y2);
        this.height = height;
    }

    @Override
    public boolean containsPoint(Position point) {
        return point.x >= minX && point.x <= maxX &&
               point.y >= minY && point.y <= maxY &&
               point.z >= 0 && point.z <= height;
    }

    @Override
    public Position getClosestPointTo(Position dronePosition) {
        double closestX = Math.max(minX, Math.min(dronePosition.x, maxX));
        double closestY = Math.max(minY, Math.min(dronePosition.y, maxY));
        double closestZ = Math.max(0, Math.min(dronePosition.z, height));
        return new Position(closestX, closestY, closestZ);
    }

    @Override
    public Vector2D getAvoidanceVector(Position dronePosition) {
        Position closest = getClosestPointTo(dronePosition);
        double dx = dronePosition.x - closest.x;
        double dy = dronePosition.y - closest.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.01) {
            return new Vector2D(1, 0); // Default avoidance
        }

        // Normalize and scale by proximity
        double scale = 50.0 / (distance + 1.0);
        return new Vector2D(dx / distance * scale, dy / distance * scale);
    }

    public double getHeight() { return height; }

    // Getters for bounds (used by visualization)
    public double getXMin() { return minX; }
    public double getXMax() { return maxX; }
    public double getYMin() { return minY; }
    public double getYMax() { return maxY; }
}
