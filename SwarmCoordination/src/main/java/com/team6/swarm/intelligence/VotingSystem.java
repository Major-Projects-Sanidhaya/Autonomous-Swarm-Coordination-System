/**
 * VOTINGSYSTEM CLASS - Week 2 Democratic Decision Making
 *
 * PURPOSE:
 * - Manages complete voting lifecycle from proposal to execution
 * - Implements consensus algorithms for distributed swarm coordination
 * - Handles vote collection, counting, and result determination
 *
 * VOTING LIFECYCLE:
 *
 * 1. INITIATE VOTE
 *    - Create VoteProposal with question and options
 *    - Set deadline and minimum votes
 *    - Broadcast to all agents (via John's system when ready)
 *    - Track proposal in active votes map
 *
 * 2. COLLECT VOTES
 *    - Agents receive proposal
 *    - Each agent creates VoteResponse
 *    - Responses sent back through communication
 *    - Store responses and validate them
 *
 * 3. CHECK CONSENSUS
 *    - Count votes for each option
 *    - Calculate percentages
 *    - Check if threshold met (e.g., 60% agreement)
 *    - Handle special cases (tie, timeout, unanimous)
 *
 * 4. DETERMINE RESULT
 *    - Create VoteResult with outcome
 *    - Include detailed breakdown
 *    - Set winning option if consensus reached
 *    - Explain failure if no consensus
 *
 * 5. EXECUTE DECISION
 *    - If consensus: implement winning option
 *    - If no consensus: apply fallback strategy
 *    - Update system state
 *    - Broadcast result to all agents
 *
 * CONSENSUS ALGORITHMS:
 *
 * SIMPLE MAJORITY (50%+):
 * - Most votes wins
 * - Quick decisions
 * - Routine operations
 *
 * SUPERMAJORITY (60-67%):
 * - Strong agreement required
 * - Important decisions
 * - Formation changes, path selection
 *
 * UNANIMOUS (100%):
 * - All agents must agree
 * - Critical safety decisions
 * - Mission aborts, emergency responses
 *
 * WEIGHTED VOTING:
 * - Expert opinions weighted higher
 * - Proximity to problem matters
 * - Battery level affects weight
 * - Confidence scores considered
 *
 * TIMEOUT HANDLING:
 *
 * If not all votes received by deadline:
 * - Calculate consensus with available votes
 * - If threshold met: proceed with decision
 * - If threshold not met: apply fallback
 * - Fallback options:
 *   * Leader decides
 *   * Status quo (no change)
 *   * Revote with modified options
 *   * Fail-safe default
 *
 * TIE BREAKING:
 *
 * If multiple options have equal votes:
 * - Leader decides (most common)
 * - First option wins (predetermined)
 * - Random selection
 * - Revote with only tied options
 * - Status quo (no change)
 *
 * EXAMPLE WORKFLOW:
 *
 * Obstacle Navigation:
 * 1. Obstacle detected at (300, 250)
 * 2. Create proposal: "Go left or right?"
 * 3. Broadcast to 7 agents
 * 4. Collect responses over 5 seconds
 * 5. Results: LEFT=5, RIGHT=2
 * 6. Consensus: 71% for LEFT (> 60% threshold)
 * 7. Execute: All agents navigate left
 *
 * INTEGRATION POINTS:
 * - Receives: VoteResponse from agents (via John)
 * - Sends: VoteProposal broadcasts (via John when ready)
 * - Sends: VoteResult notifications
 * - Sends: DecisionStatus to Anthony's UI
 * - Uses: VotingParameters for configuration
 */
package com.team6.swarm.intelligence;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VotingSystem {
    // Configuration
    private VotingParameters parameters;
    
    // Active voting data
    private final Map<String, VoteProposal> activeProposals;
    private final Map<String, Map<Integer, VoteResponse>> voteResponses;
    private final Map<String, Long> proposalTimestamps;
    
    // Communication integration (placeholder until John implements)
    private final Object communicationManager;
    
    // Proposal ID generation
    private int nextProposalId;
    
    // Performance tracking
    private int totalVotesProcessed;
    private int consensusReachedCount;
    private int consensusFailedCount;
    private final List<VoteResult> recentDecisions;
    
    /**
     * Constructor with default voting parameters
     */
    public VotingSystem() {
        this.parameters = new VotingParameters();
        this.activeProposals = new ConcurrentHashMap<>();
        this.voteResponses = new ConcurrentHashMap<>();
        this.proposalTimestamps = new ConcurrentHashMap<>();
        this.communicationManager = null;  // Will be set when John's system ready
        this.nextProposalId = 1;
        this.totalVotesProcessed = 0;
        this.consensusReachedCount = 0;
        this.consensusFailedCount = 0;
        this.recentDecisions = new ArrayList<>();
    }
    
    /**
     * Constructor with custom parameters
     */
    public VotingSystem(VotingParameters parameters) {
        this.parameters = parameters;
        this.activeProposals = new ConcurrentHashMap<>();
        this.voteResponses = new ConcurrentHashMap<>();
        this.proposalTimestamps = new ConcurrentHashMap<>();
        this.communicationManager = null;
        this.nextProposalId = 1;
        this.totalVotesProcessed = 0;
        this.consensusReachedCount = 0;
        this.consensusFailedCount = 0;
        this.recentDecisions = new ArrayList<>();
    }
    
    /**
     * INITIATE VOTE
     * Creates proposal and broadcasts to swarm
     *
     * @param question Human-readable question
     * @param options List of possible choices
     * @param proposalType Category of decision
     * @return proposalId for tracking this vote
     */
    public String initiateVote(String question, List<String> options, 
                              ProposalType proposalType) {
        // Generate unique proposal ID
        String proposalId = "vote_" + String.format("%03d", nextProposalId++);
        
        // Create proposal object
        VoteProposal proposal = new VoteProposal(proposalId, question, options);
        proposal.proposalType = proposalType;
        proposal.deadline = System.currentTimeMillis() + parameters.votingTimeout;
        proposal.minimumVotes = parameters.minimumQuorum;
        proposal.requiresUnanimous = parameters.requireUnanimous;
        
        // Validate proposal
        if (!proposal.validate()) {
            System.err.println("Invalid proposal rejected: " + proposalId);
            return null;
        }
        
        // Store proposal data
        activeProposals.put(proposalId, proposal);
        voteResponses.put(proposalId, new ConcurrentHashMap<>());
        proposalTimestamps.put(proposalId, System.currentTimeMillis());
        
        // Broadcast to all agents (placeholder until John's system ready)
        broadcastProposal(proposal);
        
        // Log initiation
        System.out.println("Vote initiated: " + proposal);
        
        return proposalId;
    }
    
    /**
     * PROCESS VOTE
     * Records individual agent vote response
     * Note: Does NOT check consensus immediately - waits for timeout
     *
     * @param response Vote from an agent
     */
    public void processVote(VoteResponse response) {
        // Validate proposal exists and is active
        if (!activeProposals.containsKey(response.proposalId)) {
            System.out.println("Warning: Vote for unknown proposal " + response.proposalId);
            return;
        }
        
        VoteProposal proposal = activeProposals.get(response.proposalId);
        
        // Validate response against proposal
        if (!response.validate(proposal)) {
            System.out.println("Warning: Invalid vote rejected - " + response);
            return;
        }
        
        // Check if revoting allowed
        Map<Integer, VoteResponse> responses = voteResponses.get(response.proposalId);
        if (responses.containsKey(response.voterId) && !parameters.allowRevoting) {
            System.out.println("Warning: Revoting not allowed for agent " + response.voterId);
            return;
        }
        
        // Calculate vote weight if weighted voting enabled
        if (parameters.useWeightedVoting) {
            response.calculateWeight(proposal, null);  // problemLocation can be added later
        }
        
        // Record the vote
        responses.put(response.voterId, response);
        totalVotesProcessed++;
        
        // Log vote received
        System.out.println(String.format("  %s (%d/%d votes)",
            response.getVoteDescription(), responses.size(), getCurrentSwarmSize()));
        
        // DO NOT check consensus here - wait for timeout to collect all votes
        // Consensus will be checked when expireProposals() is called
    }
    
    /**
     * CHECK CONSENSUS
     * Determines if voting threshold met
     *
     * @param proposalId Which proposal to check
     * @return VoteResult with consensus status
     */
    public VoteResult checkConsensus(String proposalId) {
        VoteProposal proposal = activeProposals.get(proposalId);
        Map<Integer, VoteResponse> responses = voteResponses.get(proposalId);
        
        if (proposal == null || responses == null) {
            return new VoteResult(proposalId, false, null, 
                "Proposal not found");
        }
        
        // Count votes for each option
        Map<String, Integer> voteCounts = new HashMap<>();
        Map<String, Double> weightedVoteCounts = new HashMap<>();
        List<Integer> voters = new ArrayList<>();
        
        for (String option : proposal.options) {
            voteCounts.put(option, 0);
            weightedVoteCounts.put(option, 0.0);
        }
        
        // Tally votes
        for (VoteResponse response : responses.values()) {
            if (response.isAbstention() && !parameters.allowAbstention) {
                continue;  // Skip abstentions if not allowed
            }
            
            voteCounts.put(response.choice, voteCounts.get(response.choice) + 1);
            
            if (parameters.useWeightedVoting) {
                double current = weightedVoteCounts.get(response.choice);
                weightedVoteCounts.put(response.choice, current + response.calculatedWeight);
            }
            
            voters.add(response.voterId);
        }
        
        // Find option with most votes
        String winningOption = null;
        int maxVotes = 0;
        int totalVotes = responses.size();
        
        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                winningOption = entry.getKey();
            }
        }
        
        // Calculate consensus level
        double consensusLevel = totalVotes > 0 ? (double) maxVotes / totalVotes : 0.0;
        
        // Check consensus requirements
        boolean hasQuorum = totalVotes >= proposal.minimumVotes;
        boolean meetsThreshold = consensusLevel >= parameters.consensusThreshold;
        boolean isUnanimous = (consensusLevel == 1.0);
        
        // Determine if consensus reached
        boolean consensusReached = hasQuorum && meetsThreshold;
        
        if (proposal.requiresUnanimous) {
            consensusReached = hasQuorum && isUnanimous;
        }
        
        // Create result object
        VoteResult result = new VoteResult(
            proposalId, consensusReached, 
            consensusReached ? winningOption : null,
            consensusLevel,
            generateResultReason(consensusReached, winningOption, maxVotes, totalVotes, consensusLevel),
            voteCounts, voters, totalVotes, 
            proposal.minimumVotes, parameters.consensusThreshold
        );
        
        result.wasUnanimous = isUnanimous;
        result.wasTimeout = proposal.hasExpired();
        result.weightedVoteBreakdown = weightedVoteCounts;
        
        return result;
    }
    
    /**
     * Generate human-readable result reason
     */
    private String generateResultReason(boolean consensusReached, String winningOption,
                                        int maxVotes, int totalVotes, double consensusLevel) {
        if (consensusReached) {
            double percentage = consensusLevel * 100;
            return String.format("%s wins with %.0f%% (%d/%d votes)",
                winningOption, percentage, maxVotes, totalVotes);
        } else {
            double percentage = consensusLevel * 100;
            double required = parameters.consensusThreshold * 100;
            return String.format("No consensus: need %.0f%% agreement, best option has %.0f%% (%d/%d votes)",
                required, percentage, maxVotes, totalVotes);
        }
    }
    
    /**
     * EXECUTE VOTE RESULT
     * Implements the democratic decision
     */
    private void executeVoteResult(VoteResult result) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("CONSENSUS REACHED: " + result.getSummaryMessage());
        System.out.println("========================================");
        System.out.println(result.getDetailedBreakdown());
        
        // Remove from active proposals
        VoteProposal proposal = activeProposals.remove(result.proposalId);
        voteResponses.remove(result.proposalId);
        proposalTimestamps.remove(result.proposalId);
        
        // Store in recent decisions
        recentDecisions.add(result);
        if (recentDecisions.size() > 20) {
            recentDecisions.remove(0);  // Keep only last 20
        }
        
        consensusReachedCount++;
        
        // Execute the decision based on proposal type
        if (proposal != null) {
            executeDecision(proposal, result);
        }
        
        // Broadcast result to all agents (placeholder)
        broadcastVoteResult(result);
    }
    
    /**
     * Execute specific decision based on type
     */
    private void executeDecision(VoteProposal proposal, VoteResult result) {
        System.out.println("Executing decision: " + proposal.proposalType + 
                          " -> " + result.winningOption);
        
        switch (proposal.proposalType) {
            case NAVIGATION:
                executeNavigationDecision(result.winningOption);
                break;
            case FORMATION:
                executeFormationDecision(result.winningOption);
                break;
            case MISSION:
                executeMissionDecision(result.winningOption);
                break;
            case EMERGENCY:
                executeEmergencyDecision(result.winningOption);
                break;
            case COORDINATION:
                executeCoordinationDecision(result.winningOption);
                break;
        }
    }
    
    // Decision execution methods (to be implemented in integration)
    private void executeNavigationDecision(String direction) {
        System.out.println("  → Navigation: Swarm moving " + direction);
        // Will integrate with FlockingController and movement system
    }
    
    private void executeFormationDecision(String decision) {
        System.out.println("  → Formation: " + decision);
        // Will integrate with FormationController (Week 4)
    }
    
    private void executeMissionDecision(String decision) {
        System.out.println("  → Mission: " + decision);
        // Will integrate with TaskAllocator (Week 3)
    }
    
    private void executeEmergencyDecision(String response) {
        System.out.println("  → EMERGENCY: " + response);
        // Critical response execution
    }
    
    private void executeCoordinationDecision(String decision) {
        System.out.println("  → Coordination: " + decision);
        // Timing and synchronization
    }
    
    /**
     * EXPIRE PROPOSALS
     * Clean up votes that timed out without consensus
     */
    public void expireProposals() {
        long currentTime = System.currentTimeMillis();
        List<String> expiredIds = new ArrayList<>();
        
        for (Map.Entry<String, VoteProposal> entry : activeProposals.entrySet()) {
            if (entry.getValue().hasExpired()) {
                expiredIds.add(entry.getKey());
            }
        }
        
        for (String proposalId : expiredIds) {
            VoteResult result = checkConsensus(proposalId);
            result.wasTimeout = true;
            
            if (!result.consensusReached) {
                System.out.println("Proposal " + proposalId + " expired: " + result.reason);
                consensusFailedCount++;
                
                // Apply timeout fallback
                applyTimeoutFallback(activeProposals.get(proposalId), result);
            }
            
            // Cleanup
            activeProposals.remove(proposalId);
            voteResponses.remove(proposalId);
            proposalTimestamps.remove(proposalId);
            recentDecisions.add(result);
        }
    }
    
    /**
     * Apply fallback strategy when vote times out
     */
    private void applyTimeoutFallback(VoteProposal proposal, VoteResult result) {
        System.out.println("Applying timeout fallback: " + parameters.timeoutFallback);
        
        switch (parameters.timeoutFallback) {
            case LEADER_DECIDES:
                System.out.println("  → Leader will decide");
                break;
            case STATUS_QUO:
                System.out.println("  → Maintaining current state");
                break;
            case FAIL_SAFE:
                System.out.println("  → Choosing safest option");
                break;
            case RANDOM_CHOICE:
                String randomChoice = proposal.options.get(
                    (int)(Math.random() * proposal.options.size()));
                System.out.println("  → Random selection: " + randomChoice);
                break;
            case REVOTE:
                System.out.println("  → Initiating revote");
                // Could trigger new vote here
                break;
        }
    }
    
    // Communication integration (placeholders until John's system ready)
    private void broadcastProposal(VoteProposal proposal) {
        // TODO: Use John's CommunicationManager when available
        System.out.println("Broadcasting proposal: " + proposal.proposalId);
    }
    
    private void broadcastVoteResult(VoteResult result) {
        // TODO: Use John's CommunicationManager when available
        System.out.println("Broadcasting result: " + result.proposalId);
    }
    
    // Utility methods
    private int getCurrentSwarmSize() {
        // TODO: Get actual swarm size from Sanidhya's system
        return 7;  // Placeholder
    }
    
    // ==================== PARAMETER MANAGEMENT ====================
    
    /**
     * Update voting parameters at runtime
     */
    public void updateParameters(VotingParameters newParameters) {
        if (newParameters.validate()) {
            this.parameters = newParameters;
            System.out.println("Voting parameters updated: " + newParameters);
        } else {
            System.err.println("Invalid voting parameters rejected");
        }
    }
    
    public VotingParameters getParameters() {
        return this.parameters;
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Get all active proposals
     */
    public Map<String, VoteProposal> getActiveProposals() {
        return new HashMap<>(activeProposals);
    }
    
    /**
     * Get recent vote results (last 20)
     */
    public List<VoteResult> getRecentDecisions() {
        return new ArrayList<>(recentDecisions);
    }
    
    /**
     * Get specific proposal by ID
     */
    public VoteProposal getProposal(String proposalId) {
        return activeProposals.get(proposalId);
    }
    
    /**
     * Get current votes for specific proposal
     */
    public Map<Integer, VoteResponse> getVotesForProposal(String proposalId) {
        Map<Integer, VoteResponse> votes = voteResponses.get(proposalId);
        return votes != null ? new HashMap<>(votes) : new HashMap<>();
    }
    
    // ==================== PERFORMANCE MONITORING ====================
    
    public int getTotalVotesProcessed() {
        return totalVotesProcessed;
    }
    
    public int getConsensusReachedCount() {
        return consensusReachedCount;
    }
    
    public int getConsensusFailedCount() {
        return consensusFailedCount;
    }
    
    public double getConsensusSuccessRate() {
        int total = consensusReachedCount + consensusFailedCount;
        return total > 0 ? (double) consensusReachedCount / total : 0.0;
    }
    
    public void resetPerformanceMetrics() {
        totalVotesProcessed = 0;
        consensusReachedCount = 0;
        consensusFailedCount = 0;
    }
}