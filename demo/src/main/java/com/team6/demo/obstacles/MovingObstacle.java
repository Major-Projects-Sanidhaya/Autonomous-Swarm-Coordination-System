package com.team6.demo.obstacles;

import com.team6.demo.core.Position;
import com.team6.swarm.core.Vector2D;

/**
 * MovingObstacle - Dynamic obstacle that moves over time
 * Represents vehicles, birds, or other moving threats
 */
public class MovingObstacle extends Obstacle {
    private double radius;  // Collision radius
    private double velocityX;  // Speed in X direction (m/s)
    private double velocityY;  // Speed in Y direction (m/s)
    private double velocityZ;  // Speed in Z direction (m/s)

    /**
     * Create a moving obstacle
     * @param initialPosition Starting position
     * @param radius Collision radius in meters
     * @param velocityX X-axis velocity (m/s)
     * @param velocityY Y-axis velocity (m/s)
     * @param velocityZ Z-axis velocity (m/s, typically 0 for ground vehicles)
     * @param name Obstacle name
     */
    public MovingObstacle(Position initialPosition, double radius,
                         double velocityX, double velocityY, double velocityZ,
                         String name) {
        super(initialPosition, ObstacleType.DYNAMIC, RiskLevel.MEDIUM, name);
        this.radius = radius;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }

    @Override
    public boolean containsPoint(Position point) {
        // Spherical collision detection
        return position.distanceTo(point) <= radius;
    }

    @Override
    public Position getClosestPointTo(Position dronePosition) {
        // Calculate direction from obstacle center to drone
        double dx = dronePosition.x - position.x;
        double dy = dronePosition.y - position.y;
        double dz = dronePosition.z - position.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.01) {
            // Drone is at obstacle center, return point on surface
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
            return new Vector2D(1, 0); // Default avoidance
        }

        // Stronger avoidance for moving obstacles (they're less predictable)
        double scale = 80.0 / (distance + 1.0);
        return new Vector2D(dx / distance * scale, dy / distance * scale);
    }

    @Override
    public void update(double deltaTime) {
        // Move obstacle based on velocity
        position = position.add(velocityX * deltaTime, velocityY * deltaTime, velocityZ * deltaTime);
    }

    // Getters
    public double getRadius() { return radius; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }

    // Setters for velocity changes
    public void setVelocity(double vx, double vy, double vz) {
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
    }
}
