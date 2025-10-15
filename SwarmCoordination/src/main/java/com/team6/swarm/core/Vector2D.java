/**
 * VECTOR2D CLASS - 2D Vector Mathematics
 *
 * PURPOSE:
 * - Represents direction and magnitude in 2D space
 * - Handles velocity, force, and directional calculations
 * - Essential for agent movement and physics simulation
 *
 * CORE FUNCTIONS:
 * 1. Constructor(x, y) - Creates vector with x,y components
 * 2. magnitude() - Returns length/magnitude of vector
 * 3. normalize() - Returns unit vector (length = 1) in same direction
 * 4. add(other) - Vector addition for combining forces/velocities
 * 5. multiply(scalar) - Scales vector by scalar value
 *
 * LOGIC:
 * - Magnitude: sqrt(x² + y²)
 * - Normalization: (x/mag, y/mag) where mag > 0, else (0,0)
 * - Addition: (x1+x2, y1+y2)
 * - Scalar multiplication: (x*scalar, y*scalar)
 *
 * EXPECTED OUTPUT EXAMPLES:
 * - Vector2D(3, 4).magnitude() returns: 5.0
 * - Vector2D(10, 0).normalize() returns: Vector2D(1.0, 0.0)
 * - Vector2D(1, 2).add(Vector2D(3, 4)) returns: Vector2D(4.0, 6.0)
 * - Vector2D(2, 3).multiply(2.5) returns: Vector2D(5.0, 7.5)
 *
 * USAGE IN SWARM:
 * - Agent velocity vectors
 * - Force calculations for flocking behavior
 * - Direction vectors for pathfinding
 * - Acceleration and physics calculations
 */
// src/main/java/com/team6/swarm/core/Vector2D.java
package com.team6.swarm.core;

public class Vector2D {
    public double x;
    public double y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    public Vector2D normalize() {
        double mag = magnitude();
        if (mag > 0) {
            return new Vector2D(x / mag, y / mag);
        }
        return new Vector2D(0, 0);
    }
    
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    @Override
    public String toString() {
        return String.format("Vector2D(%.2f, %.2f)", x, y);
    }
}