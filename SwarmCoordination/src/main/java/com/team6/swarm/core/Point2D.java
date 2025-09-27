/**
 * POINT2D CLASS - 2D Coordinate System
 *
 * PURPOSE:
 * - Represents a point in 2D space with x,y coordinates
 * - Provides basic geometric operations for agent positioning
 * - Foundation for all spatial calculations in the swarm system
 *
 * CORE FUNCTIONS:
 * 1. Constructor(x, y) - Creates point at specified coordinates
 * 2. distanceTo(other) - Calculates Euclidean distance between two points
 * 3. toString() - Returns formatted string representation
 *
 * LOGIC:
 * - Uses standard Cartesian coordinate system
 * - Distance calculation: sqrt((x2-x1)² + (y2-y1)²)
 * - Immutable coordinates (can be modified directly via public fields)
 *
 * EXPECTED OUTPUT EXAMPLES:
 * - Point2D(100, 200) creates point at (100.0, 200.0)
 * - distanceTo() returns: 141.42 (for points (0,0) to (100,100))
 * - toString() returns: "(100.0, 200.0)"
 *
 * USAGE IN SWARM:
 * - Agent positions in world space
 * - Target locations for movement commands
 * - Waypoints for path planning
 * - Collision detection reference points
 */
// src/main/java/com/team6/swarm/core/Point2D.java
package com.team6.swarm.core;

public class Point2D {
    public double x;
    public double y;
    
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double distanceTo(Point2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", x, y);
    }
}