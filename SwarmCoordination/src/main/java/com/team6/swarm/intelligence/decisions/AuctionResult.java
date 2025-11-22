package com.team6.swarm.intelligence.decisions;
import java.util.ArrayList;
import java.util.List;

/**
 * AUCTIONRESULT CLASS - Auction-Based Allocation Outcome
 */
public class AuctionResult {
    public String taskId;
    public TaskBid winningBid;
    public List<TaskBid> allBids;
    public long timestamp;
    
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
            .mapToDouble(b -> b.cost)
            .average()
            .orElse(0);
    }
    
    /**
     * Get cost savings (winning bid vs average)
     */
    public double getCostSavings() {
        double avgCost = getAverageBidCost();
        return avgCost - winningBid.cost;
    }
    
    @Override
    public String toString() {
        return String.format(
            "AuctionResult[Task: %s | Winner: Agent %d | Cost: %.2f | Bids: %d]",
            taskId, winningBid.agentId, winningBid.cost, allBids.size()
        );
    }
}