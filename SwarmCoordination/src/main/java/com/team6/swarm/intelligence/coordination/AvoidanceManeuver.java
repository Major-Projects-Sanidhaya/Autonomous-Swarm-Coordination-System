package com.team6.swarm.intelligence.coordination;

import com.team6.swarm.core.*;

/**
 * AVOIDANCEMANEUVER CLASS - Planned collective avoidance
 */
class AvoidanceManeuver {
    public String maneuverId;
    public Obstacle obstacle;
    public AvoidanceDirection direction;
    public Point2D maneuverWaypoint;
    public int participatingAgents;
    public long createdTime;
    
    public AvoidanceManeuver(String id, Obstacle obstacle, AvoidanceDirection direction,
                            Point2D waypoint, int agentCount) {
        this.maneuverId = id;
        this.obstacle = obstacle;
        this.direction = direction;
        this.maneuverWaypoint = waypoint;
        this.participatingAgents = agentCount;
        this.createdTime = System.currentTimeMillis();
    }
}