package com.team6.swarm.intelligence.flocking;

import com.team6.swarm.core.*;

/**
 * NEIGHBORINFO CLASS - Information about nearby agents
 *
 * PURPOSE:
 * - Packages neighbor data for flocking calculations
 * - Provided by John's communication system
 * - Contains position, velocity, and distance information
 *
 * USAGE:
 * - FlockingController receives list of NeighborInfo
 * - Each represents one agent within communication range
 * - Distance pre-calculated by communication system for efficiency
 */
public class NeighborInfo {
    public Point2D position;
    public Vector2D velocity; 
    public double distance;
    public int agentId;
    
    public NeighborInfo(int agentId, Point2D position, Vector2D velocity, double distance) {
        this.agentId = agentId;
        this.position = position;
        this.velocity = velocity;
        this.distance = distance;
    }
}
