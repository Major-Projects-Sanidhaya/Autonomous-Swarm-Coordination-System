package com.team6.swarm.intelligence.decisions;

/**
 * TASKBID CLASS - Agent Bid for Task
 */
public class TaskBid {
    public int agentId;
    public String taskId;
    public double cost;           // Agent's cost to complete task
    public double quality;        // Expected quality of completion (0-100)
    public long bidTime;
    
    public TaskBid(int agentId, String taskId, double cost, double quality) {
        this.agentId = agentId;
        this.taskId = taskId;
        this.cost = cost;
        this.quality = quality;
        this.bidTime = System.currentTimeMillis();
    }
    
    /**
     * Calculate value (quality per unit cost)
     */
    public double getValue() {
        return cost > 0 ? quality / cost : 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TaskBid[Agent %d: cost=%.2f, quality=%.2f, value=%.2f]",
            agentId, cost, quality, getValue()
        );
    }
}
