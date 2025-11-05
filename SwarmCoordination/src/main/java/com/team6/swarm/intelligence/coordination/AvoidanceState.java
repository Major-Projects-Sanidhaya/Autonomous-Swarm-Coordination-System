package com.team6.swarm.intelligence.coordination;

/**
 * AVOIDANCESTATE - Track agent's avoidance status
 */
public class AvoidanceState {
    public int agentId;
    public boolean isAvoiding;
    public Obstacle currentObstacle;
    public long avoidanceStartTime;
    
    public AvoidanceState(int agentId) {
        this.agentId = agentId;
        this.isAvoiding = false;
    }
}