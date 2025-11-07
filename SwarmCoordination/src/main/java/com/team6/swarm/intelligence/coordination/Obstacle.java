package com.team6.swarm.intelligence.coordination;

import com.team6.swarm.core.*;

/**
 * OBSTACLE CLASS - Environmental barrier
 */
public class Obstacle {
    public Point2D position;
    public double radius;
    public ObstacleType type;
    public String id;
    
    public Obstacle(Point2D position, double radius, ObstacleType type) {
        this.position = position;
        this.radius = radius;
        this.type = type;
        this.id = "obstacle_" + System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return String.format("Obstacle[%s at (%.1f, %.1f) radius %.1f]",
            type, position.x, position.y, radius);
    }
}