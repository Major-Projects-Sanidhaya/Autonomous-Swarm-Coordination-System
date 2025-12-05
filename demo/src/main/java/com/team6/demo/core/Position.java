package com.team6.demo.core;

/**
 * Position - 3D coordinate representation for demo scenarios
 * Extends the existing Point2D from core package with altitude (z-coordinate)
 */
public class Position {
    public double x;
    public double y;
    public double z;  // altitude in meters

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(double x, double y) {
        this(x, y, 0.0);
    }

    /**
     * Calculate 3D Euclidean distance to another position
     */
    public double distanceTo(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculate 2D horizontal distance (ignoring altitude)
     */
    public double horizontalDistanceTo(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Move towards target at specified speed for given time
     * Returns new position
     */
    public Position moveTo(Position target, double speed, double deltaTime) {
        double distance = distanceTo(target);
        if (distance < 0.01) {
            return new Position(target.x, target.y, target.z);
        }

        double maxDistance = speed * deltaTime;
        if (maxDistance >= distance) {
            return new Position(target.x, target.y, target.z);
        }

        double ratio = maxDistance / distance;
        double newX = this.x + (target.x - this.x) * ratio;
        double newY = this.y + (target.y - this.y) * ratio;
        double newZ = this.z + (target.z - this.z) * ratio;

        return new Position(newX, newY, newZ);
    }

    /**
     * Convert to 2D Point2D for compatibility with existing core package
     */
    public com.team6.swarm.core.Point2D toPoint2D() {
        return new com.team6.swarm.core.Point2D(this.x, this.y);
    }

    /**
     * Create Position from Point2D with specified altitude
     */
    public static Position fromPoint2D(com.team6.swarm.core.Point2D point, double altitude) {
        return new Position(point.x, point.y, altitude);
    }

    /**
     * Create copy of this position
     */
    public Position copy() {
        return new Position(this.x, this.y, this.z);
    }

    /**
     * Check if within tolerance of target
     */
    public boolean isNear(Position target, double tolerance) {
        return distanceTo(target) <= tolerance;
    }

    /**
     * Add offsets to position, returning new Position
     */
    public Position add(double dx, double dy, double dz) {
        return new Position(this.x + dx, this.y + dy, this.z + dz);
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f, %.1f)", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return Math.abs(this.x - other.x) < 0.01 &&
               Math.abs(this.y - other.y) < 0.01 &&
               Math.abs(this.z - other.z) < 0.01;
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(x);
        bits ^= Double.doubleToLongBits(y) * 31;
        bits ^= Double.doubleToLongBits(z) * 31;
        return (int) (bits ^ (bits >> 32));
    }
}
