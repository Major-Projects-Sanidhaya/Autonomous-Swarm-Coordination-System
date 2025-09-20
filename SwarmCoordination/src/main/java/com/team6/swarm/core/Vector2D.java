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
    
    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }
}