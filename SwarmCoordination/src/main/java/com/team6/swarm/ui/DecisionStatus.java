/**
 * DECISIONSTATUS CLASS - Voting and Decision Progress Information
 *
 * PURPOSE:
 * - Provides real-time status of voting and decision-making processes
 * - Enables UI visualization of democratic consensus
 * - Tracks voting progress and participation
 * - Supports decision history and analytics
 *
 * STATUS INFORMATION:
 *
 * 1. PROPOSAL DETAILS:
 *    - proposalId: Unique identifier for this vote
 *    - question: What is being decided
 *    - options: Available choices
 *    - proposalType: Category (navigation, formation, mission, etc.)
 *
 * 2. VOTING PROGRESS:
 *    - totalAgents: How many agents can vote
 *    - votesReceived: How many have voted so far
 *    - votesRequired: Minimum votes needed for quorum
 *    - progress: Percentage of votes received (0.0-1.0)
 *
 * 3. VOTE BREAKDOWN:
 *    - voteCounts: Map of option -> vote count
 *    - votePercentages: Map of option -> percentage
 *    - leadingOption: Current winner
 *    - leadingPercentage: Percentage for leading option
 *
 * 4. CONSENSUS STATUS:
 *    - consensusReached: Has decision been made?
 *    - consensusThreshold: Required agreement level
 *    - currentConsensus: Actual agreement level
 *    - winningOption: Final decision (if consensus reached)
 *
 * 5. TIMING INFORMATION:
 *    - startTime: When voting began
 *    - deadline: When voting ends
 *    - timeRemaining: Milliseconds until deadline
 *    - isExpired: Has deadline passed?
 *
 * 6. PARTICIPATION:
 *    - voters: List of agent IDs that voted
 *    - abstentions: Agents that abstained
 *    - nonResponders: Agents that haven't voted
 *
 * VISUALIZATION STATES:
 *
 * PENDING:
 * - Voting in progress
 * - Show progress bar
 * - Display current vote counts
 * - Highlight leading option
 *
 * CONSENSUS_REACHED:
 * - Decision made successfully
 * - Show winning option prominently
 * - Display final vote breakdown
 * - Indicate execution status
 *
 * TIMEOUT:
 * - Deadline passed without consensus
 * - Show timeout indicator
 * - Display fallback action
 * - Explain why consensus failed
 *
 * CANCELLED:
 * - Vote was aborted
 * - Show cancellation reason
 * - Clear voting UI
 *
 * USAGE EXAMPLE:
 * DecisionStatus status = new DecisionStatus();
 * status.proposalId = "vote_001";
 * status.question = "Navigate left or right?";
 * status.options = Arrays.asList("LEFT", "RIGHT");
 * status.totalAgents = 7;
 * status.votesReceived = 5;
 * status.voteCounts.put("LEFT", 3);
 * status.voteCounts.put("RIGHT", 2);
 * status.updateProgress();
 * visualizer.showDecisionProcess(status);
 *
 * INTEGRATION POINTS:
 * - Created by: VotingSystem (Lauren)
 * - Consumed by: DecisionVisualization, ControlPanel, StatusPanel
 * - Published via: EventBus
 * - Displayed by: Visualizer
 */
package com.team6.swarm.ui;

import java.util.*;

public class DecisionStatus {
    // ==================== PROPOSAL DETAILS ====================
    public String proposalId;
    public String question;
    public List<String> options;
    public String proposalType;  // "navigation", "formation", "mission", etc.
    
    // ==================== VOTING PROGRESS ====================
    public int totalAgents;
    public int votesReceived;
    public int votesRequired;  // Minimum for quorum
    public double progress;  // 0.0 to 1.0
    
    // ==================== VOTE BREAKDOWN ====================
    public Map<String, Integer> voteCounts;
    public Map<String, Double> votePercentages;
    public String leadingOption;
    public double leadingPercentage;
    
    // ==================== CONSENSUS STATUS ====================
    public boolean consensusReached;
    public double consensusThreshold;  // Required agreement (e.g., 0.6 for 60%)
    public double currentConsensus;  // Actual agreement level
    public String winningOption;
    public String consensusReason;  // Why consensus reached/failed
    
    // ==================== TIMING INFORMATION ====================
    public long startTime;
    public long deadline;
    public long timeRemaining;
    public boolean isExpired;
    
    // ==================== PARTICIPATION ====================
    public List<Integer> voters;  // Agent IDs that voted
    public List<Integer> abstentions;  // Agents that abstained
    public List<Integer> nonResponders;  // Agents that haven't voted
    
    // ==================== STATUS FLAGS ====================
    public boolean isPending;
    public boolean isTimeout;
    public boolean isCancelled;
    public boolean isExecuted;
    
    // ==================== METADATA ====================
    public long timestamp;
    public String statusMessage;
    public int priority;  // 0=low, 1=normal, 2=high, 3=critical
    
    /**
     * Default constructor
     */
    public DecisionStatus() {
        this.voteCounts = new HashMap<>();
        this.votePercentages = new HashMap<>();
        this.voters = new ArrayList<>();
        this.abstentions = new ArrayList<>();
        this.nonResponders = new ArrayList<>();
        this.options = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
        this.isPending = true;
        this.consensusReached = false;
        this.isExpired = false;
        this.isCancelled = false;
        this.isExecuted = false;
        this.progress = 0.0;
        this.priority = 1;
    }
    
    /**
     * Constructor with proposal details
     */
    public DecisionStatus(String proposalId, String question, List<String> options) {
        this();
        this.proposalId = proposalId;
        this.question = question;
        this.options = new ArrayList<>(options);
        
        // Initialize vote counts
        for (String option : options) {
            voteCounts.put(option, 0);
            votePercentages.put(option, 0.0);
        }
    }
    
    // ==================== UPDATE METHODS ====================
    
    /**
     * Update progress and calculate current status
     */
    public void updateProgress() {
        // Calculate progress
        if (totalAgents > 0) {
            progress = (double) votesReceived / totalAgents;
        }
        
        // Calculate percentages
        if (votesReceived > 0) {
            for (String option : options) {
                int count = voteCounts.getOrDefault(option, 0);
                double percentage = (double) count / votesReceived;
                votePercentages.put(option, percentage);
            }
        }
        
        // Find leading option
        int maxVotes = 0;
        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                leadingOption = entry.getKey();
                leadingPercentage = votePercentages.get(entry.getKey());
            }
        }
        
        // Calculate current consensus level
        if (votesReceived > 0 && leadingOption != null) {
            currentConsensus = (double) voteCounts.get(leadingOption) / votesReceived;
        }
        
        // Update time remaining
        if (deadline > 0) {
            timeRemaining = deadline - System.currentTimeMillis();
            isExpired = timeRemaining <= 0;
        }
        
        // Update timestamp
        timestamp = System.currentTimeMillis();
    }
    
    /**
     * Record a vote
     */
    public void recordVote(int agentId, String choice) {
        if (!voters.contains(agentId)) {
            voters.add(agentId);
            votesReceived++;
            
            if (choice != null && !choice.equals("ABSTAIN")) {
                voteCounts.put(choice, voteCounts.getOrDefault(choice, 0) + 1);
            } else {
                abstentions.add(agentId);
            }
            
            nonResponders.remove(Integer.valueOf(agentId));
            updateProgress();
        }
    }
    
    /**
     * Mark consensus as reached
     */
    public void markConsensusReached(String winningOption, String reason) {
        this.consensusReached = true;
        this.winningOption = winningOption;
        this.consensusReason = reason;
        this.isPending = false;
        this.statusMessage = "Consensus reached: " + winningOption;
        updateProgress();
    }
    
    /**
     * Mark as timeout
     */
    public void markTimeout(String reason) {
        this.isTimeout = true;
        this.isPending = false;
        this.consensusReason = reason;
        this.statusMessage = "Voting timeout: " + reason;
        updateProgress();
    }
    
    /**
     * Mark as cancelled
     */
    public void markCancelled(String reason) {
        this.isCancelled = true;
        this.isPending = false;
        this.consensusReason = reason;
        this.statusMessage = "Vote cancelled: " + reason;
    }
    
    /**
     * Mark as executed
     */
    public void markExecuted() {
        this.isExecuted = true;
        this.statusMessage = "Decision executed: " + winningOption;
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Check if quorum is met
     */
    public boolean hasQuorum() {
        return votesReceived >= votesRequired;
    }
    
    /**
     * Check if consensus threshold is met
     */
    public boolean meetsThreshold() {
        return currentConsensus >= consensusThreshold;
    }
    
    /**
     * Get participation rate
     */
    public double getParticipationRate() {
        return totalAgents > 0 ? (double) votesReceived / totalAgents : 0.0;
    }
    
    /**
     * Get time remaining as formatted string
     */
    public String getTimeRemainingFormatted() {
        if (timeRemaining <= 0) {
            return "Expired";
        }
        
        long seconds = timeRemaining / 1000;
        if (seconds < 60) {
            return seconds + "s";
        } else {
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return minutes + "m " + seconds + "s";
        }
    }
    
    /**
     * Get progress as percentage string
     */
    public String getProgressPercentage() {
        return String.format("%.0f%%", progress * 100);
    }
    
    /**
     * Get current status description
     */
    public String getStatusDescription() {
        if (isCancelled) {
            return "CANCELLED";
        } else if (isExecuted) {
            return "EXECUTED";
        } else if (consensusReached) {
            return "CONSENSUS REACHED";
        } else if (isTimeout) {
            return "TIMEOUT";
        } else if (isExpired) {
            return "EXPIRED";
        } else if (isPending) {
            return "VOTING IN PROGRESS";
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * Get vote summary
     */
    public String getVoteSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(question).append("\n");
        sb.append("Votes: ").append(votesReceived).append("/").append(totalAgents);
        sb.append(" (").append(getProgressPercentage()).append(")\n");
        
        if (leadingOption != null) {
            sb.append("Leading: ").append(leadingOption);
            sb.append(" (").append(String.format("%.0f%%", leadingPercentage * 100)).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Get detailed breakdown
     */
    public String getDetailedBreakdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== VOTING DETAILS ===\n");
        sb.append("Proposal: ").append(proposalId).append("\n");
        sb.append("Question: ").append(question).append("\n");
        sb.append("Status: ").append(getStatusDescription()).append("\n\n");
        
        sb.append("Progress:\n");
        sb.append("  Votes Received: ").append(votesReceived).append("/").append(totalAgents);
        sb.append(" (").append(getProgressPercentage()).append(")\n");
        sb.append("  Quorum Required: ").append(votesRequired).append("\n");
        sb.append("  Has Quorum: ").append(hasQuorum() ? "Yes" : "No").append("\n\n");
        
        sb.append("Vote Breakdown:\n");
        for (String option : options) {
            int count = voteCounts.getOrDefault(option, 0);
            double percentage = votePercentages.getOrDefault(option, 0.0);
            String indicator = option.equals(leadingOption) ? " ◄ LEADING" : "";
            sb.append(String.format("  %s: %d votes (%.0f%%)%s\n", 
                                   option, count, percentage * 100, indicator));
        }
        
        if (!abstentions.isEmpty()) {
            sb.append("  Abstentions: ").append(abstentions.size()).append("\n");
        }
        
        sb.append("\nConsensus:\n");
        sb.append("  Threshold Required: ").append(String.format("%.0f%%", consensusThreshold * 100)).append("\n");
        sb.append("  Current Consensus: ").append(String.format("%.0f%%", currentConsensus * 100)).append("\n");
        sb.append("  Consensus Reached: ").append(consensusReached ? "Yes" : "No").append("\n");
        
        if (consensusReached) {
            sb.append("  Winning Option: ").append(winningOption).append("\n");
        }
        
        if (consensusReason != null) {
            sb.append("  Reason: ").append(consensusReason).append("\n");
        }
        
        sb.append("\nTiming:\n");
        sb.append("  Time Remaining: ").append(getTimeRemainingFormatted()).append("\n");
        sb.append("  Is Expired: ").append(isExpired ? "Yes" : "No").append("\n");
        
        return sb.toString();
    }
    
    /**
     * Get visualization data for UI
     */
    public Map<String, Object> getVisualizationData() {
        Map<String, Object> data = new HashMap<>();
        data.put("proposalId", proposalId);
        data.put("question", question);
        data.put("options", options);
        data.put("voteCounts", voteCounts);
        data.put("votePercentages", votePercentages);
        data.put("progress", progress);
        data.put("leadingOption", leadingOption);
        data.put("consensusReached", consensusReached);
        data.put("winningOption", winningOption);
        data.put("timeRemaining", timeRemaining);
        data.put("status", getStatusDescription());
        return data;
    }
    
    /**
     * Clone status
     */
    public DecisionStatus clone() {
        DecisionStatus clone = new DecisionStatus();
        clone.proposalId = this.proposalId;
        clone.question = this.question;
        clone.options = new ArrayList<>(this.options);
        clone.proposalType = this.proposalType;
        clone.totalAgents = this.totalAgents;
        clone.votesReceived = this.votesReceived;
        clone.votesRequired = this.votesRequired;
        clone.progress = this.progress;
        clone.voteCounts = new HashMap<>(this.voteCounts);
        clone.votePercentages = new HashMap<>(this.votePercentages);
        clone.leadingOption = this.leadingOption;
        clone.leadingPercentage = this.leadingPercentage;
        clone.consensusReached = this.consensusReached;
        clone.consensusThreshold = this.consensusThreshold;
        clone.currentConsensus = this.currentConsensus;
        clone.winningOption = this.winningOption;
        clone.consensusReason = this.consensusReason;
        clone.startTime = this.startTime;
        clone.deadline = this.deadline;
        clone.timeRemaining = this.timeRemaining;
        clone.isExpired = this.isExpired;
        clone.voters = new ArrayList<>(this.voters);
        clone.abstentions = new ArrayList<>(this.abstentions);
        clone.nonResponders = new ArrayList<>(this.nonResponders);
        clone.isPending = this.isPending;
        clone.isTimeout = this.isTimeout;
        clone.isCancelled = this.isCancelled;
        clone.isExecuted = this.isExecuted;
        clone.statusMessage = this.statusMessage;
        clone.priority = this.priority;
        return clone;
    }
    
    @Override
    public String toString() {
        return String.format("DecisionStatus{id=%s, status=%s, votes=%d/%d, consensus=%s}", 
                           proposalId, getStatusDescription(), votesReceived, totalAgents, 
                           consensusReached ? winningOption : "pending");
    }
}
