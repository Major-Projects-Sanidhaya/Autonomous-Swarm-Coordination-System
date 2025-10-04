/**
 * VOTEPROPOSAL CLASS - Question for Swarm Democratic Decision
 *
 * PURPOSE:
 * - Standardized format for questions requiring swarm vote
 * - Contains all information needed for agents to make informed decision
 * - Tracks proposal lifecycle from creation to resolution
 *
 * PROPOSAL STRUCTURE:
 * 
 * IDENTIFICATION:
 * - proposalId: Unique identifier (e.g., "vote_001", "nav_obstacle_042")
 * - proposedBy: Agent ID that initiated vote (-1 for system)
 * - timestamp: When proposal created
 *
 * QUESTION DETAILS:
 * - question: Human-readable description of decision
 * - options: List of possible choices (minimum 2)
 * - context: Additional information for decision making
 *
 * VOTING PARAMETERS:
 * - deadline: When voting closes (timestamp)
 * - minimumVotes: How many responses required for valid result
 * - requiresUnanimous: If true, needs 100% agreement
 *
 * EXAMPLE PROPOSALS:
 *
 * Navigation Decision:
 * - Question: "Obstacle detected ahead - navigate left or right?"
 * - Options: ["LEFT", "RIGHT"]
 * - Context: "Obstacle at (300, 250), current heading: 90°"
 * - MinimumVotes: 5
 * - Deadline: currentTime + 5000ms
 *
 * Formation Change:
 * - Question: "Switch to column formation for narrow passage?"
 * - Options: ["YES", "NO"]
 * - Context: "Passage width: 50 units, current formation: WEDGE"
 * - MinimumVotes: 7
 * - Deadline: currentTime + 8000ms
 *
 * Mission Decision:
 * - Question: "Agent 3 low battery - abort mission or continue?"
 * - Options: ["ABORT", "CONTINUE", "SPLIT_SWARM"]
 * - Context: "Agent 3 battery: 15%, mission 60% complete"
 * - MinimumVotes: all active agents
 * - Deadline: currentTime + 10000ms
 *
 * Battery Emergency:
 * - Question: "Multiple agents low battery - return to base?"
 * - Options: ["RETURN_ALL", "RETURN_LOW_ONLY", "CONTINUE"]
 * - Context: "3 agents < 20% battery, base distance: 400 units"
 * - RequiresUnanimous: true (critical safety)
 *
 * PROPOSAL TYPES (for categorization):
 * - NAVIGATION: Path selection, obstacle avoidance
 * - FORMATION: Shape changes, spacing adjustments
 * - MISSION: Task acceptance, priority changes
 * - EMERGENCY: Safety responses, abort decisions
 * - COORDINATION: Timing, synchronization
 *
 * LIFECYCLE STATES:
 * - ACTIVE: Currently collecting votes
 * - COMPLETED: Consensus reached, result executed
 * - EXPIRED: Deadline passed without consensus
 * - CANCELLED: Manually cancelled by system
 *
 * USAGE PATTERN:
 * 1. Create VoteProposal with question and options
 * 2. Set deadline and minimum votes
 * 3. Broadcast via John's communication system
 * 4. Collect VoteResponse objects
 * 5. Check consensus via VotingSystem
 * 6. Execute VoteResult decision
 *
 * INTEGRATION POINTS:
 * - Created by: Any agent or system component
 * - Broadcast by: John's CommunicationManager
 * - Processed by: VotingSystem
 * - Stored in: Active proposals map
 * - Results in: VoteResult object
 */
package com.team6.swarm.intelligence.Voting;

import java.util.List;
import java.util.ArrayList;

public class VoteProposal {
    // Identification
    public String proposalId;
    public int proposedBy;        // Agent ID, -1 for system
    public long timestamp;
    
    // Question details
    public String question;
    public List<String> options;
    public String context;        // Additional decision information
    
    // Voting parameters
    public long deadline;         // When voting closes
    public int minimumVotes;      // Required responses
    public boolean requiresUnanimous;  // Needs 100% agreement
    
    // Categorization
    public ProposalType proposalType;
    
    // State tracking
    public ProposalState state;
    
    /**
     * Constructor for basic proposal
     */
    public VoteProposal(String proposalId, String question, List<String> options) {
        this.proposalId = proposalId;
        this.question = question;
        this.options = new ArrayList<>(options);
        this.timestamp = System.currentTimeMillis();
        this.deadline = timestamp + 8000;  // Default 8 second timeout
        this.minimumVotes = 3;             // Default minimum quorum
        this.requiresUnanimous = false;
        this.proposedBy = -1;              // System proposal
        this.context = "";
        this.proposalType = ProposalType.COORDINATION;
        this.state = ProposalState.ACTIVE;
    }
    
    /**
     * Full constructor with all parameters
     */
    public VoteProposal(String proposalId, String question, List<String> options,
                        String context, long deadline, int minimumVotes,
                        boolean requiresUnanimous, int proposedBy, ProposalType type) {
        this.proposalId = proposalId;
        this.question = question;
        this.options = new ArrayList<>(options);
        this.context = context;
        this.timestamp = System.currentTimeMillis();
        this.deadline = deadline;
        this.minimumVotes = minimumVotes;
        this.requiresUnanimous = requiresUnanimous;
        this.proposedBy = proposedBy;
        this.proposalType = type;
        this.state = ProposalState.ACTIVE;
    }
    
    /**
     * Check if proposal has expired
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() > deadline;
    }
    
    /**
     * Get time remaining until deadline
     */
    public long getTimeRemaining() {
        long remaining = deadline - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * Validate proposal has valid structure
     */
    public boolean validate() {
        if (proposalId == null || proposalId.isEmpty()) return false;
        if (question == null || question.isEmpty()) return false;
        if (options == null || options.size() < 2) return false;
        if (minimumVotes < 1) return false;
        if (deadline <= timestamp) return false;
        return true;
    }
    
    /**
     * Factory method: Create navigation decision proposal
     */
    public static VoteProposal createNavigationProposal(String proposalId, 
                                                        String question,
                                                        String context) {
        List<String> options = new ArrayList<>();
        options.add("LEFT");
        options.add("RIGHT");
        
        VoteProposal proposal = new VoteProposal(proposalId, question, options);
        proposal.context = context;
        proposal.proposalType = ProposalType.NAVIGATION;
        proposal.deadline = System.currentTimeMillis() + 5000;  // Quick decision
        proposal.minimumVotes = 4;
        
        return proposal;
    }
    
    /**
     * Factory method: Create yes/no formation proposal
     */
    public static VoteProposal createFormationProposal(String proposalId,
                                                      String question,
                                                      String context) {
        List<String> options = new ArrayList<>();
        options.add("YES");
        options.add("NO");
        
        VoteProposal proposal = new VoteProposal(proposalId, question, options);
        proposal.context = context;
        proposal.proposalType = ProposalType.FORMATION;
        proposal.deadline = System.currentTimeMillis() + 8000;
        proposal.minimumVotes = 5;
        
        return proposal;
    }
    
    /**
     * Factory method: Create emergency decision proposal
     */
    public static VoteProposal createEmergencyProposal(String proposalId,
                                                      String question,
                                                      List<String> options,
                                                      String context) {
        VoteProposal proposal = new VoteProposal(proposalId, question, options);
        proposal.context = context;
        proposal.proposalType = ProposalType.EMERGENCY;
        proposal.deadline = System.currentTimeMillis() + 10000;
        proposal.requiresUnanimous = true;  // Critical decisions need full agreement
        proposal.minimumVotes = 3;        // TODO: Ideally, minimumVotes should equal the total number of agents to ensure unanimous participation. However, setting this to a fixed high value (e.g., 999) is not practical. Consider implementing a mechanism to dynamically set minimumVotes based on the current agent count.
        
        return proposal;
    }
    
    @Override
    public String toString() {
        return String.format(
            "VoteProposal[%s: '%s' | Options: %s | Deadline: %dms | Min: %d votes]",
            proposalId, question, options, getTimeRemaining(), minimumVotes
        );
    }
}

/**
 * PROPOSALTYPE ENUM - Vote Categorization
 */
enum ProposalType {
    NAVIGATION,      // Path and obstacle decisions
    FORMATION,       // Shape and spacing changes
    MISSION,         // Task and priority decisions
    EMERGENCY,       // Safety and abort decisions
    COORDINATION     // Timing and synchronization
}

/**
 * PROPOSALSTATE ENUM - Lifecycle Tracking
 */
enum ProposalState {
    ACTIVE,          // Currently collecting votes
    COMPLETED,       // Consensus reached
    EXPIRED,         // Deadline passed
    CANCELLED        // Manually cancelled
}