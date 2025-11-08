package com.team6.swarm.intelligence.emergence;

import com.team6.swarm.intelligence.flocking.BehaviorType;
import java.util.*;

/**
 * CONFLICTRESOLUTION - Record of how conflict was resolved
 */
public class ConflictResolution {
    public int agentId;
    public ResolutionType resolutionType;
    public BehaviorType winningBehavior;
    public List<BehaviorType> otherBehaviors;
    public long timestamp;
    
    public ConflictResolution(int agentId, ResolutionType type,
                              BehaviorType winner, List<BehaviorType> others,
                              long timestamp) {
        this.agentId = agentId;
        this.resolutionType = type;
        this.winningBehavior = winner;
        this.otherBehaviors = others != null ? new ArrayList<>(others) : new ArrayList<>();
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("Resolution[Agent %d: %s won via %s]",
            agentId, winningBehavior, resolutionType);
    }
}
