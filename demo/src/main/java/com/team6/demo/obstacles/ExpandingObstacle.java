package com.team6.demo.obstacles;

import com.team6.demo.core.Position;
import com.team6.swarm.core.Vector2D;

/**
 * ExpandingObstacle - Growing hazard (fire, smoke cloud, etc.)
 * Radius increases over time at specified rate
 */
public class ExpandingObstacle extends Obstacle {
    private double radius;
    private final double expansionRate; // meters per second
    private final double maxRadius;     // Maximum size

    /**
     * Create an expanding obstacle
     * @param center Center position
     * @param initialRadius Starting radius in meters
     * @param expansionRate Growth rate in m/s
     * @param maxRadius Maximum radius (growth stops)
     * @param name Obstacle name
     */
    public ExpandingObstacle(Position center, double initialRadius, double expansionRate,
                            double maxRadius, String name) {
        super(center, ObstacleType.DYNAMIC, RiskLevel.CRITICAL, name);
        this.radius = initialRadius;
        this.expansionRate = expansionRate;
        this.maxRadius = maxRadius;
    }

    @Override
    public void update(double deltaTime) {
        // Expand radius over time
        if (radius < maxRadius) {
            radius += expansionRate * deltaTime;
            if (radius > maxRadius) {
                radius = maxRadius;
            }
        }
    }

    @Override
    public boolean containsPoint(Position point) {
        // Spherical hazard zone
        return position.distanceTo(point) <= radius;
    }

    @Override
    public Position getClosestPointTo(Position dronePosition) {
        double dx = dronePosition.x - position.x;
        double dy = dronePosition.y - position.y;
        double dz = dronePosition.z - position.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.01) {
            // Drone at center, return point on surface
            return new Position(position.x + radius, position.y, position.z);
        }

        // Closest point on sphere surface
        double scale = radius / distance;
        return new Position(
            position.x + dx * scale,
            position.y + dy * scale,
            position.z + dz * scale
        );
    }

    @Override
    public Vector2D getAvoidanceVector(Position dronePosition) {
        double dx = dronePosition.x - position.x;
        double dy = dronePosition.y - position.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.01) {
            return new Vector2D(1, 0);
        }

        // Very strong avoidance for expanding hazards (CRITICAL risk)
        // Force increases as hazard grows
        double scale = 150.0 * (radius / maxRadius) / (distance - radius + 1.0);
        return new Vector2D(dx / distance * scale, dy / distance * scale);
    }

    // Getters
    public double getRadius() {
        return radius;
    }

    public double getExpansionRate() {
        return expansionRate;
    }

    public double getMaxRadius() {
        return maxRadius;
    }

    @Override
    public String toString() {
        return String.format("ExpandingObstacle[%s, center=%s, radius=%.1fm/%.1fm, rate=%.1fm/s]",
            name, position, radius, maxRadius, expansionRate);
    }
}
