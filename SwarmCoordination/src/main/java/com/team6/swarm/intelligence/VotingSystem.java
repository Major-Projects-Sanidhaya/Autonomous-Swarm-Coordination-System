/**
 * VOTING SYSTEM CLASS - Distributed Democratic Decision Making
 *
 * PURPOSE:
 * - Enables agents to vote collectively on mission-critical decisions
 * - Implements consensus algorithms for distributed swarm coordination
 * - Manages proposal lifecycle from initiation to execution
 *
 * MAIN COMPONENTS:
 * 1. Proposal Management - Create, track, and close voting proposals
 * 2. Vote Collection - Gather and validate agent votes
 * 3. Consensus Detection - Determine when sufficient agreement reached
 * 4. Result Execution - Apply winning decisions to swarm behavior
 *
 * CORE FUNCTIONS:
 * 1. initiateVote() - Create new proposal and broadcast to agents
 * 2. processVote() - Record individual agent vote responses
 * 3. checkConsensus() - Evaluate if voting threshold met
 * 4. executeResult() - Apply winning decision to system
 * 5. expireProposals() - Clean up old/stale votes
 *
 * VOTING LIFECYCLE:
 * 1. Agent or system creates proposal (e.g., "Go left or right?")
 * 2. Proposal broadcast to all agents via communication system
 * 3. Agents respond with their preferred choice
 * 4. System tracks responses and calculates percentages
 * 5. When consensus threshold reached (60%), execute winning choice
 * 6. Proposal expires after timeout or completion
 *
 * CONSENSUS ALGORITHM:
 * - Simple Majority: Requires 60% agreement for decision
 * - Timeout Protection: Proposals expire after 10 seconds
 * - Quorum Requirements: Minimum 3 agents must participate
 * - Tie Handling: Default to first option or status quo
 *
 * PROPOSAL TYPES:
 * - NAVIGATION: Path selection around obstacles
 * - FORMATION: Change swarm formation pattern  
 * - MISSION: Accept/reject new mission assignments
 * - EMERGENCY: Immediate danger response protocols
 *
 * EXPECTED OUTPUTS:
 * - Console: "Vote initiated: 'Navigate around obstacle' - LEFT vs RIGHT"
 * - Console: "Agent 3 voted LEFT (4/7 total votes)"
 * - Console: "CONSENSUS REACHED: LEFT wins with 71% (5/7 votes)"
 * - OutgoingMessage broadcasts for proposals and results
 *
 * INTEGRATION POINTS:
 * - Receives: VoteResponse messages from John's communication system
 * - Receives: ProposalRequest from system events or user interface
 * - Sends: VoteProposal broadcasts via OutgoingMessage to John
 * - Sends: DecisionResult to Anthony's visualization system
 */
// src/main/java/com/team6/swarm/intelligence/VotingSystem.java
package com.team6.swarm.intelligence;

import com.team6.swarm.core.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VotingSystem {
    // Consensus configuration
    private static final double CONSENSUS_THRESHOLD = 0.60; // 60% agreement required
    private static final int MINIMUM_QUORUM = 3; // Minimum voters for valid decision
    private static final long PROPOSAL_TIMEOUT_MS = 10000; // 10 second timeout
    
    // Active voting data
    private Map<String, VoteProposal> activeProposals;
    private Map<String, Map<Integer, String>> voteResponses; // proposalId -> (agentId -> choice)
    private Map<String, Long> proposalTimestamps;
    
    // System integration
    private Object communicationManager; // Will be CommunicationManager when John implements it
    private int nextProposalId;
    
    // Performance tracking
    private int totalVotesProcessed;
    private int consensusReached;
    private List<VoteResult> recentDecisions;
    
    public VotingSystem(Object communicationManager) {
        this.communicationManager = communicationManager;
        this.activeProposals = new ConcurrentHashMap<>();
        this.voteResponses = new ConcurrentHashMap<>();
        this.proposalTimestamps = new ConcurrentHashMap<>();
        this.nextProposalId = 1;
        this.recentDecisions = new ArrayList<>();
    }
    
    /**
     * Main voting initiation - creates proposal and broadcasts to swarm
     * @param question - Human readable question (e.g., "Navigate around obstacle?")
     * @param options - Available choices (e.g., ["LEFT", "RIGHT"])
     * @param proposalType - Category of decision being made
     * @return proposalId for tracking this vote
     */
    public String initiateVote(String question, List<String> options, ProposalType proposalType) {
        // Generate unique proposal ID
        String proposalId = "VOTE_" + nextProposalId++;
        
        // Create proposal object
        VoteProposal proposal = new VoteProposal();
        proposal.proposalId = proposalId;
        proposal.question = question;
        proposal.options = new ArrayList<>(options);
        proposal.proposalType = proposalType;
        proposal.initiatorId = -1; // System initiated
        proposal.timestamp = System.currentTimeMillis();
        
        // Store proposal data
        activeProposals.put(proposalId, proposal);
        voteResponses.put(proposalId, new ConcurrentHashMap<>());
        proposalTimestamps.put(proposalId, System.currentTimeMillis());
        
        // Broadcast to all agents
        broadcastProposal(proposal);
        
        // Log initiation
        System.out.println(String.format("Vote initiated: '%s' - %s", 
                          question, String.join(" vs ", options)));
        
        return proposalId;
    }
    
    /**
     * Process individual agent vote - called when vote message received
     * @param proposalId - Which vote this response is for
     * @param agentId - Which agent is voting
     * @param choice - Agent's selected option
     */
    public void processVote(String proposalId, int agentId, String choice) {
        // Validate proposal exists and is active
        if (!activeProposals.containsKey(proposalId)) {
            System.out.println("Warning: Vote received for unknown proposal " + proposalId);
            return;
        }
        
        VoteProposal proposal = activeProposals.get(proposalId);
        
        // Validate choice is valid option
        if (!proposal.options.contains(choice)) {
            System.out.println("Warning: Invalid vote choice '" + choice + "' for proposal " + proposalId);
            return;
        }
        
        // Record the vote
        voteResponses.get(proposalId).put(agentId, choice);
        totalVotesProcessed++;
        
        // Log vote received
        Map<Integer, String> currentVotes = voteResponses.get(proposalId);
        System.out.println(String.format("Agent %d voted %s (%d/%d total votes)", 
                          agentId, choice, currentVotes.size(), getCurrentSwarmSize()));
        
        // Check if consensus reached
        VoteResult result = checkConsensus(proposalId);
        if (result.consensusReached) {
            executeVoteResult(result);
        }
    }
    
    /**
     * Consensus detection - determines if enough votes received for decision
     * @param proposalId - Which proposal to check
     * @return VoteResult with consensus status and winning choice
     */
    public VoteResult checkConsensus(String proposalId) {
        VoteProposal proposal = activeProposals.get(proposalId);
        Map<Integer, String> votes = voteResponses.get(proposalId);
        
        if (proposal == null || votes == null) {
            return new VoteResult(proposalId, false, null, "Proposal not found");
        }
        
        // Count votes for each option
        Map<String, Integer> voteCounts = new HashMap<>();
        for (String option : proposal.options) {
            voteCounts.put(option, 0);
        }
        
        for (String vote : votes.values()) {
            voteCounts.put(vote, voteCounts.get(vote) + 1);
        }
        
        // Find option with most votes
        String winningOption = null;
        int maxVotes = 0;
        int totalVotes = votes.size();
        
        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                winningOption = entry.getKey();
            }
        }
        
        // Check consensus requirements
        boolean hasQuorum = totalVotes >= MINIMUM_QUORUM;
        boolean hasConsensus = maxVotes >= Math.ceil(totalVotes * CONSENSUS_THRESHOLD);
        boolean consensusReached = hasQuorum && hasConsensus;
        
        if (consensusReached) {
            double percentage = (double) maxVotes / totalVotes * 100;
            String reason = String.format("%s wins with %.0f%% (%d/%d votes)", winningOption, percentage, maxVotes, totalVotes);
            return new VoteResult(proposalId, true, winningOption, reason);
        }
        
        return new VoteResult(proposalId, false, null, 
                            String.format("No consensus: need %d%% agreement, have %d/%d votes", 
                                        (int)(CONSENSUS_THRESHOLD * 100), maxVotes, totalVotes));
    }
    
    /**
     * Execute winning vote decision - apply result to swarm behavior
     */
    private void executeVoteResult(VoteResult result) {
        System.out.println("CONSENSUS REACHED: " + result.reason);
        
        // Remove from active proposals
        VoteProposal proposal = activeProposals.remove(result.proposalId);
        voteResponses.remove(result.proposalId);
        proposalTimestamps.remove(result.proposalId);
        
        // Store in recent decisions
        recentDecisions.add(result);
        consensusReached++;
        
        // Execute the decision based on proposal type
        switch (proposal.proposalType) {
            case NAVIGATION:
                executeNavigationDecision(result.winningChoice);
                break;
            case FORMATION:
                executeFormationDecision(result.winningChoice);
                break;
            case MISSION:
                executeMissionDecision(result.winningChoice);
                break;
            case EMERGENCY:
                executeEmergencyDecision(result.winningChoice);
                break;
        }
        
        // Broadcast result to all agents
        broadcastVoteResult(result);
    }
    
    /**
     * Clean up expired proposals that haven't reached consensus
     */
    public void expireProposals() {
        long currentTime = System.currentTimeMillis();
        List<String> expiredIds = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : proposalTimestamps.entrySet()) {
            if (currentTime - entry.getValue() > PROPOSAL_TIMEOUT_MS) {
                expiredIds.add(entry.getKey());
            }
        }
        
        for (String proposalId : expiredIds) {
            System.out.println("Proposal " + proposalId + " expired without consensus");
            activeProposals.remove(proposalId);
            voteResponses.remove(proposalId);
            proposalTimestamps.remove(proposalId);
        }
    }
    
    // ==================== DECISION EXECUTION METHODS ====================
    
    private void executeNavigationDecision(String direction) {
        System.out.println("Executing navigation decision: " + direction);
        // This would integrate with your flocking controller
        // For example: FlockingController.setAvoidanceDirection(direction);
    }
    
    private void executeFormationDecision(String formation) {
        System.out.println("Executing formation change: " + formation);
        // This would trigger formation controller
    }
    
    private void executeMissionDecision(String decision) {
        System.out.println("Executing mission decision: " + decision);
        // This would update mission parameters
    }
    
    private void executeEmergencyDecision(String response) {
        System.out.println("Executing emergency response: " + response);
        // This would trigger emergency protocols
    }
    
    // ==================== COMMUNICATION INTEGRATION ====================
    
    private void broadcastProposal(VoteProposal proposal) {
        // TODO: This will use John's CommunicationManager when available
        // For now, just log the broadcast
        System.out.println("Broadcasting proposal: " + proposal.proposalId);
        
        /* Future implementation with John's system:
        OutgoingMessage message = new OutgoingMessage();
        message.senderId = -1; // System message
        message.receiverId = -1; // Broadcast to all
        message.priority = MessagePriority.HIGH;
        message.messageContent = new Message();
        message.messageContent.type = MessageType.VOTE_PROPOSAL;
        message.messageContent.payload = proposal;
        message.messageContent.timestamp = System.currentTimeMillis();
        
        communicationManager.sendMessage(message);
        */
    }
    
    private void broadcastVoteResult(VoteResult result) {
        // TODO: This will use John's CommunicationManager when available
        System.out.println("Broadcasting vote result: " + result.reason);
        
        /* Future implementation with John's system:
        OutgoingMessage message = new OutgoingMessage();
        message.senderId = -1; // System message
        message.receiverId = -1; // Broadcast to all
        message.priority = MessagePriority.HIGH;
        message.messageContent = new Message();
        message.messageContent.type = MessageType.VOTE_RESULT;
        message.messageContent.payload = result;
        message.messageContent.timestamp = System.currentTimeMillis();
        
        communicationManager.sendMessage(message);
        */
    }
    
    // ==================== UTILITY METHODS ====================
    
    private int getCurrentSwarmSize() {
        // This would integrate with Sanidhya's agent system
        // For now, estimate based on recent vote participation
        return 7; // Placeholder
    }
    
    // ==================== PERFORMANCE MONITORING ====================
    
    public int getTotalVotesProcessed() {
        return totalVotesProcessed;
    }
    
    public int getConsensusReached() {
        return consensusReached;
    }
    
    public List<VoteResult> getRecentDecisions() {
        return new ArrayList<>(recentDecisions);
    }
    
    public Map<String, VoteProposal> getActiveProposals() {
        return new HashMap<>(activeProposals);
    }
}

// ==================== SUPPORTING DATA STRUCTURES ====================

class VoteProposal {
    public String proposalId;
    public String question;
    public List<String> options;
    public ProposalType proposalType;
    public int initiatorId;
    public long timestamp;
}

class VoteResult {
    public String proposalId;
    public boolean consensusReached;
    public String winningChoice;
    public String reason;
    
    public VoteResult(String proposalId, boolean consensusReached, String winningChoice, String reason) {
        this.proposalId = proposalId;
        this.consensusReached = consensusReached;
        this.winningChoice = winningChoice;
        this.reason = reason;
    }
}

enum ProposalType {
    NAVIGATION, FORMATION, MISSION, EMERGENCY
}