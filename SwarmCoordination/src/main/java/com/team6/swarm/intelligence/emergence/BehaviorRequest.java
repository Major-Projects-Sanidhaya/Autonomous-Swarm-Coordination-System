package com.team6.swarm.intelligence.emergence;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.flocking.BehaviorType;

/**
 * BEHAVIORREQUEST - Pending behavior for agent
 */
public class BehaviorRequest {
    public BehaviorType behaviorType;
    public int priority;
    public MovementCommand command;
    public long timestamp;
    
    public BehaviorRequest(BehaviorType behaviorType, int priority,
                          MovementCommand command, long timestamp) {
        this.behaviorType = behaviorType;
        this.priority = priority;
        this.command = command;
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("%s (Priority: %d)", behaviorType, priority);
    }
}
