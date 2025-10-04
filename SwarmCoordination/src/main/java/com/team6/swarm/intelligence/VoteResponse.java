/**
 * VOTERESPONSE CLASS - Individual Agent Vote Answer
 *
 * PURPOSE:
 * - Represents single agent's response to a VoteProposal
 * - Contains vote choice and optional metadata
 * - Enables tracking of who voted for what
 *
 * RESPONSE STRUCTURE:
 *
 * IDENTIFICATION:
 * - proposalId: Which vote this responds to (must match VoteProposal)
 * - voterId: Which agent is voting (agent ID)
 * - timestamp: When vote was cast
 *
 * VOTE CONTENT:
 * - choice: Selected option (must be from proposal's options list)
 * - confidence: How certain agent is (0.0 = uncertain, 1.0 = certain)
 * - reasoning: Optional explanation for vote
 *
 * AGENT CONTEXT:
 * - agentPosition: Voter's location (for proximity-weighted voting)
 * - agentBattery: Voter's power level (for resource-aware decisions)
 * - agentRole: Voter's current behavior (SCOUT, GUARD, etc.)
 *
 * VOTE VALIDITY:
 * - Must reference existing proposal
 * - Choice must be valid option from proposal
 * - Voter must be active agent
 * - Must arrive before deadline
 *
 * EXAMPLE RESPONSES:
 *
 * Simple Navigation Vote:
 * - ProposalId: "nav_obstacle_042"
 * - VoterId: 3
 * - Choice: "LEFT"
 * - Confidence: 0.8
 * - Reasoning: "Left path has more clearance"
 *
 * Formation Change Vote:
 * - ProposalId: "form_narrow_012"
 * - VoterId: 7
 * - Choice: "YES"
 * - Confidence: 1.0
 * - Reasoning: "Column formation optimal for passage"
 *
 * Emergency Decision Vote:
 * - ProposalId: "emerg_battery_008"
 * - VoterId: 2
 * - Choice: "RETURN_ALL"
 * - Confidence: 0.9
 * - Reasoning: "3 agents below 20%, mission safety at risk"
 * - AgentBattery: 0.18 (low battery agent voting)
 *
 * Weighted Vote (Expert Opinion):
 * - ProposalId: "nav_path_choice"
 * - VoterId: 1
 * - Choice: "RIGHT"
 * - Confidence: 1.0
 * - AgentRole: SCOUT (expert in navigation)
 * - Weight automatically increased by system
 *
 * CONFIDENCE LEVELS:
 * - 1.0: Certain, have clear information
 * - 0.8: Confident, reasonable information
 * - 0.6: Somewhat confident, limited info
 * - 0.4: Uncertain, guessing
 * - 0.2: Very uncertain, random choice
 * - 0.0: Abstaining (counted differently)
 *
 * USAGE PATTERNS:
 *
 * Basic Vote:
 * 1. Agent receives VoteProposal broadcast
 * 2. Agent evaluates options
 * 3. Agent creates VoteResponse with choice
 * 4. Agent sends response via communication system
 *
 * Weighted Vote (Advanced):
 * 1. Agent includes confidence level
 * 2. Agent includes reasoning
 * 3. System may weight vote based on:
 *    - Agent role (SCOUT votes weighted higher for navigation)
 *    - Agent proximity (closer agents weighted higher)
 *    - Agent battery (low battery votes weighted less)
 *    - Confidence level (higher confidence = more weight)
 *
 * INTEGRATION POINTS:
 * - Created by: Individual agents in response to proposals
 * - Sent via: John's CommunicationManager
 * - Received by: VotingSystem
 * - Stored in: Vote response collections
 * - Used in: Consensus calculations
 */
package com.team6.swarm.intelligence;

import com.team6.swarm.core.Point2D;

public class VoteResponse {
    // Identification
    public String proposalId;      // Which vote this responds to
    public int voterId;            // Which agent is voting
    public long timestamp;         // When vote was cast
    
    // Vote content
    public String choice;          // Selected option
    public double confidence;      // 0.0 to 1.0 (how certain)
    public String reasoning;       // Optional explanation
    
    // Agent context (for weighted voting)
    public Point2D agentPosition;  // Voter location
    public double agentBattery;    // Voter power level
    public BehaviorType agentRole; // Voter current role
    
    // Vote processing
    public double calculatedWeight; // System-calculated vote weight
    public boolean isValid;         // Passed validation
    
    /**
     * Basic constructor for simple vote
     */
    public VoteResponse(String proposalId, int voterId, String choice) {
        this.proposalId = proposalId;
        this.voterId = voterId;
        this.choice = choice;
        this.timestamp = System.currentTimeMillis();
        this.confidence = 1.0;        // Default: fully confident
        this.reasoning = "";
        this.calculatedWeight = 1.0;  // Default: equal weight
        this.isValid = false;         // Must be validated
    }
    
    /**
     * Full constructor with all parameters
     */
    public VoteResponse(String proposalId, int voterId, String choice,
                        double confidence, String reasoning,
                        Point2D agentPosition, double agentBattery,
                        BehaviorType agentRole) {
        this.proposalId = proposalId;
        this.voterId = voterId;
        this.choice = choice;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.timestamp = System.currentTimeMillis();
        this.agentPosition = agentPosition;
        this.agentBattery = agentBattery;
        this.agentRole = agentRole;
        this.calculatedWeight = 1.0;
        this.isValid = false;
    }
    
    /**
     * Validate vote response
     * Checks if response is properly formed
     */
    public boolean validate(VoteProposal proposal) {
        // Check proposal ID matches
        if (!this.proposalId.equals(proposal.proposalId)) {
            return false;
        }
        
        // Check choice is valid option
        if (!proposal.options.contains(this.choice)) {
            return false;
        }
        
        // Check confidence in valid range
        if (this.confidence < 0.0 || this.confidence > 1.0) {
            return false;
        }
        
        // Check not arrived after deadline
        if (this.timestamp > proposal.deadline) {
            return false;
        }
        
        this.isValid = true;
        return true;
    }
    
    /**
     * Calculate vote weight based on agent context
     * Used for weighted voting systems
     */
    public void calculateWeight(VoteProposal proposal, Point2D problemLocation) {
        double weight = 1.0;  // Base weight
        
        // Weight by confidence
        weight *= confidence;
        
        // Weight by battery (low battery = less weight)
        if (agentBattery < 0.3) {
            weight *= 0.7;  // Low battery agents get reduced weight
        }
        
        // Weight by role relevance
        if (agentRole != null) {
            switch (proposal.proposalType) {
                case NAVIGATION:
                    // Scout opinions weighted higher for navigation
                    if (agentRole == BehaviorType.SCOUT) {
                        weight *= 1.5;
                    } else if (agentRole == BehaviorType.LEADER) {
                        weight *= 1.3;
                    }
                    break;
                    
                case FORMATION:
                    // Leader opinions weighted higher for formations
                    if (agentRole == BehaviorType.LEADER) {
                        weight *= 1.5;
                    }
                    break;
                    
                case EMERGENCY:
                    // All votes equal weight in emergencies
                    break;
                    
                default:
                    break;
            }
        }
        
        // Weight by proximity to problem
        if (agentPosition != null && problemLocation != null) {
            double distance = agentPosition.distanceTo(problemLocation);
            // Closer agents weighted higher (within reasonable range)
            if (distance < 100) {
                weight *= 1.2;
            } else if (distance > 300) {
                weight *= 0.8;
            }
        }
        
        this.calculatedWeight = weight;
    }
    
    /**
     * Check if this is an abstention vote
     * Confidence near 0 indicates abstention
     */
    public boolean isAbstention() {
        return confidence < 0.1;
    }
    
    /**
     * Get display string for logging
     */
    public String getVoteDescription() {
        if (reasoning != null && !reasoning.isEmpty()) {
            return String.format("Agent %d voted '%s' (%.0f%% confident): %s",
                voterId, choice, confidence * 100, reasoning);
        } else {
            return String.format("Agent %d voted '%s' (%.0f%% confident)",
                voterId, choice, confidence * 100);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "VoteResponse[Proposal: %s | Voter: %d | Choice: %s | Confidence: %.2f | Weight: %.2f]",
            proposalId, voterId, choice, confidence, calculatedWeight
        );
    }
}