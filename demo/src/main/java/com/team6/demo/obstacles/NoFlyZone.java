package com.team6.demo.obstacles;

import com.team6.demo.core.Position;
import com.team6.swarm.core.Vector2D;

/**
 * NoFlyZone - Circular restricted airspace
 * Drones cannot enter this area at any altitude
 */
public class NoFlyZone extends Obstacle {
    private final double radius;

    /**
     * Create a circular no-fly zone
     * @param center Center position of the zone
     * @param radius Radius in meters
     * @param name Zone name
     */
    public NoFlyZone(Position center, double radius, String name) {
        super(center, ObstacleType.STATIC, RiskLevel.CRITICAL, name);
        this.radius = radius;
    }

    @Override
    public boolean containsPoint(Position point) {
        // Check horizontal distance only (zone extends through all altitudes)
        double dx = point.x - position.x;
        double dy = point.y - position.y;
        double horizontalDistance = Math.sqrt(dx * dx + dy * dy);
        return horizontalDistance <= radius;
    }

    @Override
    public Position getClosestPointTo(Position dronePosition) {
        // Calculate direction from zone center to drone
        double dx = dronePosition.x - position.x;
        double dy = dronePosition.y - position.y;
        double horizontalDistance = Math.sqrt(dx * dx + dy * dy);

        if (horizontalDistance < 0.01) {
            // Drone is at zone center, return point on edge
            return new Position(position.x + radius, position.y, dronePosition.z);
        }

        // Closest point is on the circle edge
        double scale = radius / horizontalDistance;
        return new Position(
            position.x + dx * scale,
            position.y + dy * scale,
            dronePosition.z  // Keep drone's altitude
        );
    }

    @Override
    public Vector2D getAvoidanceVector(Position dronePosition) {
        double dx = dronePosition.x - position.x;
        double dy = dronePosition.y - position.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.01) {
            return new Vector2D(1, 0); // Default avoidance
        }

        // Very strong avoidance for no-fly zones (CRITICAL risk)
        // Scale increases dramatically as drone gets closer
        double scale = 100.0 / (distance - radius + 1.0);
        return new Vector2D(dx / distance * scale, dy / distance * scale);
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        return String.format("NoFlyZone[%s, center=%s, radius=%.1fm]",
            name, position, radius);
    }
}
