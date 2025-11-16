package com.team6.swarm.intelligence.emergence;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.flocking.BehaviorType;

/**
 * BEHAVIORREQUEST - Pending behavior for agent
 */
public class BehaviorRequest {
    private final BehaviorType behaviorType;
    private final int priority;
    private final MovementCommand command;
    private final long timestamp;

    public BehaviorType getBehaviorType() { return behaviorType; }
    public int getPriority() { return priority; }
    public MovementCommand getCommand() { return command; }
    public long getTimestamp() { return timestamp; }
    
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
