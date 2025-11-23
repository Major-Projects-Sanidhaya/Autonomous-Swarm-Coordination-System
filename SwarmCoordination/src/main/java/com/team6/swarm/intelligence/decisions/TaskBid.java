package com.team6.swarm.intelligence.decisions;

/**
 * TASKBID CLASS - Agent Bid for Task
 */
public class TaskBid {
    private final int agentId;
    private final String taskId;
    private final double cost;           // Agent's cost to complete task
    private final double quality;        // Expected quality of completion (0-100)
    private final long bidTime;

    public int getAgentId() { return agentId; }
    public String getTaskId() { return taskId; }
    public double getCost() { return cost; }
    public double getQuality() { return quality; }
    public long getBidTime() { return bidTime; }
    
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
