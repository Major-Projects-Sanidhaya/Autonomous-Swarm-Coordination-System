package com.team6.swarm.intelligence.decisions;
import java.util.ArrayList;
import java.util.List;

/**
 * AUCTIONRESULT CLASS - Auction-Based Allocation Outcome
 */
public class AuctionResult {
    private final String taskId;
    private final TaskBid winningBid;
    private final List<TaskBid> allBids;
    private final long timestamp;

    public String getTaskId() {
      return taskId;
    }

    public TaskBid getWinningBid() {
      return winningBid;
    }

    public List<TaskBid> getAllBids() {
      return java.util.Collections.unmodifiableList(allBids);
    }

    public long getTimestamp() {
      return timestamp;
    }
    
    public AuctionResult(String taskId, TaskBid winningBid, List<TaskBid> allBids) {
        this.taskId = taskId;
        this.winningBid = winningBid;
        this.allBids = new ArrayList<>(allBids);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get number of bids received
     */
    public int getBidCount() {
        return allBids.size();
    }
    
    /**
     * Get average bid cost
     */
    public double getAverageBidCost() {
        if (allBids.isEmpty()) return 0;
        return allBids.stream()
            .mapToDouble(b -> b.getCost())
            .average()
            .orElse(0);
    }
    
    /**
     * Get cost savings (winning bid vs average)
     */
    public double getCostSavings() {
        double avgCost = getAverageBidCost();
        return avgCost - winningBid.getCost();
    }
    
    @Override
    public String toString() {
        return String.format(
            "AuctionResult[Task: %s | Winner: Agent %d | Cost: %.2f | Bids: %d]",
            taskId, winningBid.getAgentId(), winningBid.getCost(), allBids.size()
        );
    }
}