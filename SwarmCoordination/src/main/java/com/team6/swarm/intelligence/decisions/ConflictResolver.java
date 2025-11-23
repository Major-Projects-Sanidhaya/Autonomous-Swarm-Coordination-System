/**
 * CONFLICTRESOLVER CLASS - Handle Disagreements and Deadlocks
 *
 * PURPOSE:
 * - Resolve voting deadlocks when consensus cannot be reached
 * - Provide fallback strategies for split decisions
 * - Enable compromise solutions when agents disagree
 * - Prevent system paralysis from indecision
 *
 * RESOLUTION STRATEGIES:
 *
 * 1. FALLBACK LEADER:
 *    - If vote fails, designated leader decides
 *    - Emergency decision-making authority
 *    - Prevents deadlock in critical situations
 *    - Leader selection based on expertise/position
 *
 * 2. COMPROMISE SOLUTION:
 *    - If split vote, find middle ground
 *    - Partial implementation of both options
 *    - Satisfies both sides partially
 *    - Example: "Go left 60%, right 40%" -> slight left
 *
 * 3. REVOTE WITH MODIFIED OPTIONS:
 *    - If no consensus, present new choices
 *    - Learn from first vote distribution
 *    - Iterate toward agreement
 *    - Maximum 3 rounds before fallback
 *
 * 4. MULTI-STAGE DECISION:
 *    - Break decision into smaller parts
 *    - Vote on each part separately
 *    - Combine partial decisions
 *    - Easier to reach consensus on smaller questions
 *
 * 5. HYBRID APPROACH:
 *    - Combine multiple strategies
 *    - Use different strategies for subgroups
 *    - Example: Leaders vote first, others follow if close
 *
 * DEADLOCK SCENARIOS:
 *
 * TIE VOTE:
 * - Equal votes for multiple options
 * - No clear majority
 * - Resolution: Leader breaks tie
 *
 * INSUFFICIENT VOTES:
 * - Not enough responses by deadline
 * - Below minimum quorum
 * - Resolution: Use available votes or defer
 *
 * BELOW THRESHOLD:
 * - Highest option doesn't meet consensus threshold
 * - Example: 55% when need 60%
 * - Resolution: Revote or compromise
 *
 * INTEGRATION POINTS:
 * - Works with: VotingSystem for deadlock detection
 * - Uses: LeaderFollower for leader decisions
 * - Coordinates with: DecisionEngine for alternative approaches
 */
package com.team6.swarm.intelligence.decisions;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.voting.*;
import com.team6.swarm.intelligence.coordination.LeaderFollower;

import java.util.*;

public class ConflictResolver {
    // Resolution strategy
    private ResolutionStrategy defaultStrategy;
    
    // Revote tracking
    private Map<String, Integer> revoteAttempts;
    private static final int MAX_REVOTE_ATTEMPTS = 3;
    
    // Performance tracking
    private int totalConflicts;
    private int leaderResolutions;
    private int compromiseResolutions;
    private int revoteResolutions;
    private int failedResolutions;
    
    /**
     * Constructor
     */
    public ConflictResolver() {
        this.defaultStrategy = ResolutionStrategy.FALLBACK_LEADER;
        this.revoteAttempts = new HashMap<>();
        this.totalConflicts = 0;
        this.leaderResolutions = 0;
        this.compromiseResolutions = 0;
        this.revoteResolutions = 0;
        this.failedResolutions = 0;
    }
    
    /**
     * Constructor with custom strategy
     */
    public ConflictResolver(ResolutionStrategy strategy) {
        this();
        this.defaultStrategy = strategy;
    }
    
    // ==================== MAIN RESOLUTION METHOD ====================
    
    /**
     * RESOLVE CONFLICT
     * Main entry point for conflict resolution
     *
     * @param voteResult Failed vote result
     * @param proposal Original vote proposal
     * @param agents Current agent states
     * @param leaderSystem Leader-follower system for leader decisions
     * @return Resolution result with chosen option
     */
    public ResolutionResult resolveConflict(VoteResult voteResult,
                                            VoteProposal proposal,
                                            List<AgentState> agents,
                                            LeaderFollower leaderSystem) {
        totalConflicts++;
        
        System.out.println("=== CONFLICT RESOLUTION ===");
        System.out.println("Vote failed: " + voteResult.reason);
        System.out.println("Applying strategy: " + defaultStrategy);
        
        ResolutionResult result = null;
        
        switch (defaultStrategy) {
            case FALLBACK_LEADER:
                result = resolveByLeader(voteResult, proposal, agents, leaderSystem);
                leaderResolutions++;
                break;
                
            case COMPROMISE:
                result = resolveByCompromise(voteResult, proposal, agents);
                compromiseResolutions++;
                break;
                
            case REVOTE:
                result = resolveByRevote(voteResult, proposal, agents);
                break;
                
            case MULTI_STAGE:
                result = resolveByMultiStage(voteResult, proposal, agents);
                break;
                
            case HYBRID:
                result = resolveByHybrid(voteResult, proposal, agents, leaderSystem);
                break;
                
            default:
                result = resolveByLeader(voteResult, proposal, agents, leaderSystem);
                break;
        }
        
        if (result != null && result.resolved) {
            System.out.println("✓ Conflict resolved: " + result.chosenOption);
        } else {
            System.out.println("✗ Conflict resolution failed");
            failedResolutions++;
        }
        
        return result;
    }
    
    // ==================== FALLBACK LEADER STRATEGY ====================
    
    /**
     * RESOLVE BY LEADER
     * Leader makes final decision
     */
    private ResolutionResult resolveByLeader(VoteResult voteResult,
                                            VoteProposal proposal,
                                            List<AgentState> agents,
                                            LeaderFollower leaderSystem) {
        System.out.println("Leader decision strategy:");
        
        // Get current leader
        int leaderId = leaderSystem.getCurrentLeader();
        if (leaderId == -1) {
            System.err.println("  No leader available!");
            return new ResolutionResult(proposal.proposalId, false, null, 
                                        ResolutionStrategy.FALLBACK_LEADER,
                                        "No leader available");
        }
        
        AgentState leader = findAgent(leaderId, agents);
        if (leader == null) {
            System.err.println("  Leader not found!");
            return new ResolutionResult(proposal.proposalId, false, null,
                                        ResolutionStrategy.FALLBACK_LEADER,
                                        "Leader not found");
        }
        
        // Leader chooses best option based on available data
        String leaderChoice = selectLeaderChoice(voteResult, proposal, leader);
        
        System.out.println(String.format("  Leader (Agent %d) decides: %s",
            leaderId, leaderChoice));
        
        return new ResolutionResult(
            proposal.proposalId,
            true,
            leaderChoice,
            ResolutionStrategy.FALLBACK_LEADER,
            String.format("Leader (Agent %d) decision", leaderId)
        );
    }
    
    /**
     * SELECT LEADER CHOICE
     * Leader's decision logic
     */
    private String selectLeaderChoice(VoteResult voteResult, VoteProposal proposal,
                                      AgentState leader) {
        // Leader picks option with most votes (even if below threshold)
        if (!voteResult.voteBreakdown.isEmpty()) {
            String topOption = null;
            int maxVotes = 0;
            
            for (Map.Entry<String, Integer> entry : voteResult.voteBreakdown.entrySet()) {
                if (entry.getValue() > maxVotes) {
                    maxVotes = entry.getValue();
                    topOption = entry.getKey();
                }
            }
            
            if (topOption != null) {
                return topOption;
            }
        }
        
        // Fallback: first option
        return proposal.options.isEmpty() ? null : proposal.options.get(0);
    }
    
    // ==================== COMPROMISE STRATEGY ====================
    
    /**
     * RESOLVE BY COMPROMISE
     * Find middle ground between options
     */
    private ResolutionResult resolveByCompromise(VoteResult voteResult,
                                                VoteProposal proposal,
                                                List<AgentState> agents) {
        System.out.println("Compromise strategy:");
        
        // Check if compromise is possible for this proposal type
        if (!isCompromisePossible(proposal)) {
            System.out.println("  Compromise not possible for this decision type");
            return new ResolutionResult(proposal.proposalId, false, null,
                                        ResolutionStrategy.COMPROMISE,
                                        "Compromise not applicable");
        }
        
        // Find top two options
        List<Map.Entry<String, Integer>> sortedVotes = new ArrayList<>(
            voteResult.voteBreakdown.entrySet());
        sortedVotes.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        if (sortedVotes.size() < 2) {
            // Only one option, just use it
            String option = sortedVotes.isEmpty() ? proposal.options.get(0) : 
                            sortedVotes.get(0).getKey();
            return new ResolutionResult(proposal.proposalId, true, option,
                                        ResolutionStrategy.COMPROMISE,
                                        "Only one option available");
        }
        
        String option1 = sortedVotes.get(0).getKey();
        String option2 = sortedVotes.get(1).getKey();
        int votes1 = sortedVotes.get(0).getValue();
        int votes2 = sortedVotes.get(1).getValue();
        
        // Create compromise option
        String compromise = createCompromise(option1, option2, votes1, votes2, proposal);
        
        System.out.println(String.format("  Compromise between %s (%d votes) and %s (%d votes)",
            option1, votes1, option2, votes2));
        System.out.println("  Compromise: " + compromise);
        
        return new ResolutionResult(
            proposal.proposalId,
            true,
            compromise,
            ResolutionStrategy.COMPROMISE,
            String.format("Compromise: %d%% %s, %d%% %s",
                (votes1 * 100) / (votes1 + votes2), option1,
                (votes2 * 100) / (votes1 + votes2), option2)
        );
    }
    
    /**
     * Check if compromise is possible
     */
    private boolean isCompromisePossible(VoteProposal proposal) {
        // Compromise works for directional decisions
        return proposal.proposalType == ProposalType.NAVIGATION ||
                proposal.proposalType == ProposalType.COORDINATION;
    }
    
    /**
     * CREATE COMPROMISE
     * Generate compromise option between two choices
     */
    private String createCompromise(String option1, String option2, 
                                    int votes1, int votes2,
                                    VoteProposal proposal) {
        // For navigation: blend directions
        if (proposal.proposalType == ProposalType.NAVIGATION) {
            // Avoid division by zero
            int total = votes1 + votes2;
            if (total == 0) {
                return proposal.options.isEmpty() ? null : proposal.options.get(0);
            }

            // Calculate weighted direction
            double weight1 = (double) votes1 / total;
            double weight2 = (double) votes2 / total;

            final double THRESHOLD = 0.6;

            if (weight1 > THRESHOLD) {
                return option1 + "_SLIGHT";  // Mostly option1
            } else if (weight2 > THRESHOLD) {
                return option2 + "_SLIGHT";  // Mostly option2
            } else {
                return "CENTER";  // Middle ground
            }
        }
        
        // For yes/no: partial implementation
        if (option1.equals("YES") && option2.equals("NO")) {
            return "PARTIAL";  // Partial implementation
        }
        
        // Default: pick higher vote option
        return votes1 >= votes2 ? option1 : option2;
    }
    
    // ==================== REVOTE STRATEGY ====================
    
    /**
     * RESOLVE BY REVOTE
     * Conduct another vote with modified options
     */
    private ResolutionResult resolveByRevote(VoteResult voteResult,
                                            VoteProposal proposal,
                                            List<AgentState> agents) {
        System.out.println("Revote strategy:");
        
        // Check revote attempts
        int attempts = revoteAttempts.getOrDefault(proposal.proposalId, 0);
        if (attempts >= MAX_REVOTE_ATTEMPTS) {
            System.out.println("  Maximum revote attempts reached");
            return new ResolutionResult(proposal.proposalId, false, null,
                                        ResolutionStrategy.REVOTE,
                                        "Max revote attempts exceeded");
        }
        
        // Increment attempt counter
        revoteAttempts.put(proposal.proposalId, attempts + 1);
        
        // Modify options based on previous vote
        List<String> modifiedOptions = modifyOptions(voteResult, proposal);
        
        System.out.println("  Modified options: " + modifiedOptions);
        System.out.println("  Initiating revote (attempt " + (attempts + 1) + ")");
        
        revoteResolutions++;
        
        // Return result indicating revote needed
        return new ResolutionResult(
            proposal.proposalId,
            true,
            "REVOTE_NEEDED",
            ResolutionStrategy.REVOTE,
            "Revote with modified options: " + modifiedOptions
        );
    }
    
    /**
     * MODIFY OPTIONS
     * Adjust options based on previous vote results
     */
    private List<String> modifyOptions(VoteResult voteResult, VoteProposal proposal) {
        List<String> modified = new ArrayList<>();
        
        // Keep top voted options
        List<Map.Entry<String, Integer>> sortedVotes = new ArrayList<>(
            voteResult.voteBreakdown.entrySet());
        sortedVotes.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // Take top 2-3 options
        int optionsToKeep = Math.min(3, sortedVotes.size());
        for (int i = 0; i < optionsToKeep; i++) {
            modified.add(sortedVotes.get(i).getKey());
        }
        
        // Add compromise option if applicable
        if (sortedVotes.size() >= 2 && isCompromisePossible(proposal)) {
            String compromise = createCompromise(
                sortedVotes.get(0).getKey(),
                sortedVotes.get(1).getKey(),
                sortedVotes.get(0).getValue(),
                sortedVotes.get(1).getValue(),
                proposal
            );
            if (!modified.contains(compromise)) {
                modified.add(compromise);
            }
        }
        
        return modified;
    }
    
    // ==================== MULTI-STAGE STRATEGY ====================
    
    /**
     * RESOLVE BY MULTI-STAGE
     * Break decision into smaller parts
     */
    private ResolutionResult resolveByMultiStage(VoteResult voteResult,
                                                VoteProposal proposal,
                                                List<AgentState> agents) {
        System.out.println("Multi-stage strategy:");
        System.out.println("  Breaking decision into stages");
        
        // Stage 1: Eliminate least popular option
        if (proposal.options.size() > 2) {
            String leastPopular = findLeastPopularOption(voteResult);
            System.out.println("  Stage 1: Eliminate " + leastPopular);
            
            List<String> remainingOptions = new ArrayList<>(proposal.options);
            remainingOptions.remove(leastPopular);
            
            return new ResolutionResult(
                proposal.proposalId,
                true,
                "STAGE_1_COMPLETE",
                ResolutionStrategy.MULTI_STAGE,
                "Remaining options: " + remainingOptions
            );
        }
        
        // Stage 2: Final vote between top 2
        System.out.println("  Stage 2: Final vote needed");
        return new ResolutionResult(
            proposal.proposalId,
            true,
            "STAGE_2_NEEDED",
            ResolutionStrategy.MULTI_STAGE,
            "Final vote between top 2 options"
        );
    }
    
    /**
     * Find least popular option
     */
    private String findLeastPopularOption(VoteResult voteResult) {
        String leastPopular = null;
        int minVotes = Integer.MAX_VALUE;
        
        for (Map.Entry<String, Integer> entry : voteResult.voteBreakdown.entrySet()) {
            if (entry.getValue() < minVotes) {
                minVotes = entry.getValue();
                leastPopular = entry.getKey();
            }
        }
        
        return leastPopular;
    }
    
    // ==================== HYBRID STRATEGY ====================
    
    /**
     * RESOLVE BY HYBRID
     * Combine multiple strategies
     */
    private ResolutionResult resolveByHybrid(VoteResult voteResult,
                                            VoteProposal proposal,
                                            List<AgentState> agents,
                                            LeaderFollower leaderSystem) {
        System.out.println("Hybrid strategy:");
        
        // First try compromise if possible
        if (isCompromisePossible(proposal)) {
            System.out.println("  Attempting compromise...");
            ResolutionResult compromiseResult = resolveByCompromise(
                voteResult, proposal, agents);
            
            if (compromiseResult.resolved) {
                return compromiseResult;
            }
        }
        
        // If compromise fails, try leader decision
        System.out.println("  Falling back to leader decision...");
        return resolveByLeader(voteResult, proposal, agents, leaderSystem);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Find agent by ID
     */
    private AgentState findAgent(int agentId, List<AgentState> agents) {
        for (AgentState agent : agents) {
            if (agent.agentId == agentId) {
                return agent;
            }
        }
        return null;
    }
    
    /**
     * SET STRATEGY
     */
    public void setStrategy(ResolutionStrategy strategy) {
        this.defaultStrategy = strategy;
        System.out.println("Conflict resolution strategy changed to: " + strategy);
    }
    
    /**
     * RESET REVOTE ATTEMPTS
     */
    public void resetRevoteAttempts(String proposalId) {
        revoteAttempts.remove(proposalId);
    }
    
    // ==================== PERFORMANCE METRICS ====================
    
    public int getTotalConflicts() {
        return totalConflicts;
    }
    
    public int getLeaderResolutions() {
        return leaderResolutions;
    }
    
    public int getCompromiseResolutions() {
        return compromiseResolutions;
    }
    
    public int getRevoteResolutions() {
        return revoteResolutions;
    }
    
    public int getFailedResolutions() {
        return failedResolutions;
    }
    
    public double getResolutionSuccessRate() {
        return totalConflicts > 0 ? 
            (double) (totalConflicts - failedResolutions) / totalConflicts : 1.0;
    }
    
    public void resetMetrics() {
        totalConflicts = 0;
        leaderResolutions = 0;
        compromiseResolutions = 0;
        revoteResolutions = 0;
        failedResolutions = 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "ConflictResolver[Total: %d | Leader: %d | Compromise: %d | Revote: %d | Failed: %d]",
            totalConflicts, leaderResolutions, compromiseResolutions, 
            revoteResolutions, failedResolutions
        );
    }
}