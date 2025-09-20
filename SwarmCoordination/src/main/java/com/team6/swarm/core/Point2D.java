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