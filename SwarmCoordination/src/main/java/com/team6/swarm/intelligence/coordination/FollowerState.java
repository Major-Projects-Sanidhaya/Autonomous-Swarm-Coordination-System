package com.team6.swarm.intelligence.coordination;

import com.team6.swarm.core.*;

/**
 * FOLLOWERSTATE CLASS - Track individual follower information
 *
 * PURPOSE:
 * - Store follower's offset from leader
 * - Track position correction history
 * - Monitor follower performance
 */
class FollowerState {
    public int agentId;
    public Vector2D offsetFromLeader;      // X, Y offset from leader
    public double followDistance;          // Desired distance from leader
    public long lastCorrectionTime;        // When position was last corrected
    public int correctionCount;            // Number of corrections made
    
    public FollowerState(int agentId) {
        this.agentId = agentId;
        this.offsetFromLeader = new Vector2D(0, 0);
        this.followDistance = 50.0;
        this.lastCorrectionTime = 0;
        this.correctionCount = 0;
    }
    
    /**
     * Get time since last correction
     */
    public long getTimeSinceCorrection() {
        if (lastCorrectionTime == 0) return -1;
        return System.currentTimeMillis() - lastCorrectionTime;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Follower[Agent %d | Offset: (%.1f, %.1f) | Distance: %.1f | Corrections: %d]",
            agentId, offsetFromLeader.x, offsetFromLeader.y, followDistance, correctionCount
        );
    }
}