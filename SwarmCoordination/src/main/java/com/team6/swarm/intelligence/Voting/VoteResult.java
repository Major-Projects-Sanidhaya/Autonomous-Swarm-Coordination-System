/**
 * VOTERESULT CLASS - Outcome of Completed Vote
 *
 * PURPOSE:
 * - Represents final result after vote consensus reached (or failed)
 * - Contains winning option and detailed vote breakdown
 * - Enables execution of democratic decision
 *
 * RESULT STRUCTURE:
 *
 * IDENTIFICATION:
 * - proposalId: Which vote this result is for
 * - timestamp: When result was determined
 *
 * OUTCOME:
 * - consensusReached: Did we achieve required agreement?
 * - winningOption: What choice won (null if no consensus)
 * - consensusLevel: Percentage agreement (0.0 to 1.0)
 * - reason: Human-readable explanation of result
 *
 * VOTE BREAKDOWN:
 * - voteBreakdown: Map of option → vote count
 * - weightedVoteBreakdown: Map of option → weighted vote sum
 * - participatingAgents: List of agent IDs that voted
 * - abstentions: List of agent IDs that abstained
 * - nonResponders: List of agent IDs that didn't vote
 *
 * RESULT TYPES:
 *
 * CONSENSUS REACHED:
 * - consensusReached = true
 * - winningOption != null
 * - consensusLevel >= threshold
 * - reason explains winning margin
 * - Ready for execution
 *
 * NO CONSENSUS:
 * - consensusReached = false
 * - winningOption = null
 * - consensusLevel < threshold
 * - reason explains why failed
 * - Needs fallback decision
 *
 * UNANIMOUS:
 * - consensusReached = true
 * - consensusLevel = 1.0
 * - All agents voted same option
 * - Strong mandate for action
 *
 * EXPIRED:
 * - consensusReached = false
 * - Deadline passed
 * - Insufficient votes received
 * - Timeout fallback needed
 *
 * TIE:
 * - consensusReached = false
 * - Multiple options equal votes
 * - Needs tiebreaker mechanism
 * - Leader can decide
 *
 * EXAMPLE RESULTS:
 *
 * Successful Navigation Vote:
 * - ProposalId: "nav_obstacle_042"
 * - ConsensusReached: true
 * - WinningOption: "LEFT"
 * - ConsensusLevel: 0.71 (71%)
 * - VoteBreakdown: {LEFT: 5, RIGHT: 2}
 * - Reason: "LEFT wins with 71% (5/7 votes)"
 * - Action: Execute left navigation
 *
 * Failed Formation Vote:
 * - ProposalId: "form_narrow_012"
 * - ConsensusReached: false
 * - WinningOption: null
 * - ConsensusLevel: 0.43 (43%)
 * - VoteBreakdown: {YES: 3, NO: 4}
 * - Reason: "No consensus: need 60% agreement, have 43%"
 * - Action: Maintain current formation
 *
 * Unanimous Emergency Decision:
 * - ProposalId: "emerg_battery_008"
 * - ConsensusReached: true
 * - WinningOption: "RETURN_ALL"
 * - ConsensusLevel: 1.0 (100%)
 * - VoteBreakdown: {RETURN_ALL: 7}
 * - Reason: "Unanimous decision: RETURN_ALL (7/7 votes)"
 * - Action: Immediate return to base
 *
 * Tied Vote:
 * - ProposalId: "nav_path_choice"
 * - ConsensusReached: false
 * - WinningOption: null
 * - ConsensusLevel: 0.5 (50%)
 * - VoteBreakdown: {LEFT: 3, RIGHT: 3}
 * - Reason: "Tie: LEFT and RIGHT both have 3 votes"
 * - Action: Leader decides tiebreaker
 *
 * CONSENSUS THRESHOLDS:
 * - Simple Majority: 50%+ (most decisions)
 * - Supermajority: 60-67% (important decisions)
 * - Unanimous: 100% (critical safety decisions)
 *
 * USAGE PATTERN:
 * 1. VotingSystem collects all responses
 * 2. Calculate vote counts and percentages
 * 3. Check against threshold
 * 4. Create VoteResult with outcome
 * 5. If consensus: execute winning option
 * 6. If no consensus: apply fallback logic
 * 7. Broadcast result to all agents
 * 8. Update mission status
 *
 * INTEGRATION POINTS:
 * - Created by: VotingSystem after vote completion
 * - Used by: Decision execution systems
 * - Broadcast by: John's CommunicationManager
 * - Logged by: Anthony's monitoring system
 * - Stored in: Recent decisions history
 */
package com.team6.swarm.intelligence.Voting;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class VoteResult {
    // Identification
    public String proposalId;
    public long timestamp;
    
    // Outcome
    public boolean consensusReached;
    public String winningOption;
    public double consensusLevel;    // 0.0 to 1.0
    public String reason;            // Human-readable explanation
    
    // Vote breakdown
    public Map<String, Integer> voteBreakdown;           // option -> count
    public Map<String, Double> weightedVoteBreakdown;    // option -> weighted sum
    public List<Integer> participatingAgents;
    public List<Integer> abstentions;
    public List<Integer> nonResponders;
    
    // Metadata
    public int totalVotes;
    public int requiredVotes;
    public double requiredThreshold;
    public boolean wasUnanimous;
    public boolean wasTimeout;
    
    /**
     * Constructor for successful consensus
     */
    public VoteResult(String proposalId, boolean consensusReached, 
                      String winningOption, String reason) {
        this.proposalId = proposalId;
        this.consensusReached = consensusReached;
        this.winningOption = winningOption;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.voteBreakdown = new HashMap<>();
        this.weightedVoteBreakdown = new HashMap<>();
        this.participatingAgents = new ArrayList<>();
        this.abstentions = new ArrayList<>();
        this.nonResponders = new ArrayList<>();
    }
    
    /**
     * Full constructor with all parameters
     */
    public VoteResult(String proposalId, boolean consensusReached,
                      String winningOption, double consensusLevel,
                      String reason, Map<String, Integer> voteBreakdown,
                      List<Integer> participatingAgents, int totalVotes,
                      int requiredVotes, double requiredThreshold) {
        this.proposalId = proposalId;
        this.consensusReached = consensusReached;
        this.winningOption = winningOption;
        this.consensusLevel = consensusLevel;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.voteBreakdown = new HashMap<>(voteBreakdown);
        this.weightedVoteBreakdown = new HashMap<>();
        this.participatingAgents = new ArrayList<>(participatingAgents);
        this.abstentions = new ArrayList<>();
        this.nonResponders = new ArrayList<>();
        this.totalVotes = totalVotes;
        this.requiredVotes = requiredVotes;
        this.requiredThreshold = requiredThreshold;
        this.wasUnanimous = false;
        this.wasTimeout = false;
    }
    
    /**
     * Check if vote was unanimous
     */
    public boolean isUnanimous() {
        if (!consensusReached) return false;
        if (voteBreakdown.size() != 1) return false;
        return voteBreakdown.get(winningOption) == totalVotes;
    }
    
    /**
     * Check if vote was a tie
     */
    public boolean isTie() {
        if (voteBreakdown.size() < 2) return false;
        
        int maxVotes = 0;
        int tieCount = 0;
        
        for (int votes : voteBreakdown.values()) {
            if (votes > maxVotes) {
                maxVotes = votes;
                tieCount = 1;
            } else if (votes == maxVotes) {
                tieCount++;
            }
        }
        
        return tieCount > 1;
    }
    
    /**
     * Get runner-up option (second place)
     */
    public String getRunnerUp() {
        if (voteBreakdown.size() < 2) return null;
        
        String winner = winningOption;
        String runnerUp = null;
        int runnerUpVotes = 0;
        
        for (Map.Entry<String, Integer> entry : voteBreakdown.entrySet()) {
            if (!entry.getKey().equals(winner)) {
                if (entry.getValue() > runnerUpVotes) {
                    runnerUp = entry.getKey();
                    runnerUpVotes = entry.getValue();
                }
            }
        }
        
        return runnerUp;
    }
    
    /**
     * Get margin of victory (percentage points)
     */
    public double getMarginOfVictory() {
        if (!consensusReached || totalVotes == 0) return 0.0;
        
        int winnerVotes = voteBreakdown.getOrDefault(winningOption, 0);
        double winnerPercent = (double) winnerVotes / totalVotes;
        
        String runnerUp = getRunnerUp();
        if (runnerUp == null) return winnerPercent;  // Unanimous
        
        int runnerUpVotes = voteBreakdown.getOrDefault(runnerUp, 0);
        double runnerUpPercent = (double) runnerUpVotes / totalVotes;
        
        return winnerPercent - runnerUpPercent;
    }
    
    /**
     * Get detailed breakdown string for logging
     */
    public String getDetailedBreakdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vote Breakdown:\n");
        
        for (Map.Entry<String, Integer> entry : voteBreakdown.entrySet()) {
            double percent = (double) entry.getValue() / totalVotes * 100;
            sb.append(String.format("  %s: %d votes (%.1f%%)\n", 
                entry.getKey(), entry.getValue(), percent));
        }
        
        if (!abstentions.isEmpty()) {
            sb.append(String.format("  Abstentions: %d\n", abstentions.size()));
        }
        
        if (!nonResponders.isEmpty()) {
            sb.append(String.format("  Non-responders: %d\n", nonResponders.size()));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate success/failure summary message
     */
    public String getSummaryMessage() {
        if (consensusReached) {
            if (wasUnanimous) {
                return String.format("UNANIMOUS: %s selected by all %d agents", 
                    winningOption, totalVotes);
            } else {
                return String.format("CONSENSUS: %s wins with %.0f%% (%d/%d votes)",
                    winningOption, consensusLevel * 100, 
                    voteBreakdown.get(winningOption), totalVotes);
            }
        } else {
            if (wasTimeout) {
                return String.format("TIMEOUT: Only %d/%d votes received before deadline",
                    totalVotes, requiredVotes);
            } else if (isTie()) {
                return String.format("TIE: No clear winner, needs tiebreaker");
            } else {
                return String.format("NO CONSENSUS: Best option only has %.0f%%, need %.0f%%",
                    consensusLevel * 100, requiredThreshold * 100);
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "VoteResult[%s: %s | Consensus: %s | Winner: %s | Level: %.0f%%]",
            proposalId, 
            consensusReached ? "SUCCESS" : "FAILED",
            consensusReached ? "YES" : "NO",
            winningOption != null ? winningOption : "NONE",
            consensusLevel * 100
        );
    }
}