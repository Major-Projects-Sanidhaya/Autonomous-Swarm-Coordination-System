package com.team6.swarm.intelligence.emergence;

import com.team6.swarm.intelligence.flocking.BehaviorType;
import java.util.*;

/**
 * CONFLICTRESOLUTION - Record of how conflict was resolved
 */
public class ConflictResolution {
    private final int agentId;
    private final ResolutionType resolutionType;
    private final BehaviorType winningBehavior;
    private final List<BehaviorType> otherBehaviors;
    private final long timestamp;
    
    public int getAgentId() {
      return agentId;
    }

    public ResolutionType getResolutionType() {
      return resolutionType;
    }

    public BehaviorType getWinningBehavior() {
      return winningBehavior;
    }

    public List<BehaviorType> getOtherBehaviors() {
      return Collections.unmodifiableList(new ArrayList<>(otherBehaviors));
    }

    public long getTimestamp() {
      return timestamp;
    }
    
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
