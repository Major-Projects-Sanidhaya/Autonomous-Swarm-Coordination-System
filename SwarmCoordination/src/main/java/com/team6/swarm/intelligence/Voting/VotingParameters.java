/**
 * VOTINGPARAMETERS CLASS - Configuration for Voting Behavior
 *
 * PURPOSE:
 * - Stores tunable parameters for democratic decision making
 * - Allows different consensus requirements for different situations
 * - Enables adaptive voting strategies based on mission criticality
 *
 * PARAMETER CATEGORIES:
 *
 * CONSENSUS REQUIREMENTS:
 * - consensusThreshold: Percentage needed to pass (default: 0.6 = 60%)
 * - minimumQuorum: Minimum voters for valid decision (default: 3)
 * - requireUnanimous: If true, needs 100% agreement
 *
 * TIMING PARAMETERS:
 * - votingTimeout: How long to wait for votes (default: 8000ms = 8 sec)
 * - maxVotingRounds: Retry limit if first vote fails (default: 2)
 * - revoteDelay: Time between rounds (default: 3000ms)
 *
 * VOTING BEHAVIOR:
 * - allowAbstention: Can agents skip voting? (default: true)
 * - useWeightedVoting: Enable confidence/role weighting (default: false)
 * - allowRevoting: Can agents change their vote? (default: false)
 *
 * FALLBACK STRATEGIES:
 * - timeoutFallback: What to do if vote times out
 * - tiebreakerMode: How to resolve tied votes
 * - leaderOverride: Can leader decide if no consensus?
 *
 * CONFIGURATION PRESETS:
 *
 * STANDARD VOTING (default):
 * - 60% consensus threshold
 * - 8 second timeout
 * - Simple majority rules
 * - Good for routine decisions
 *
 * EMERGENCY VOTING:
 * - 100% unanimous required
 * - 10 second timeout
 * - Leader can override
 * - For critical safety decisions
 *
 * QUICK VOTING:
 * - 50% simple majority
 * - 5 second timeout
 * - Fast decision making
 * - For time-sensitive choices
 *
 * DELIBERATIVE VOTING:
 * - 67% supermajority
 * - 15 second timeout
 * - Multiple rounds allowed
 * - For important strategic decisions
 *
 * USAGE SCENARIOS:
 *
 * Navigation Obstacle:
 * - consensusThreshold: 0.6
 * - votingTimeout: 5000ms
 * - Quick decision needed
 *
 * Formation Change:
 * - consensusThreshold: 0.6
 * - votingTimeout: 8000ms
 * - Standard voting
 *
 * Mission Abort:
 * - requireUnanimous: true
 * - votingTimeout: 10000ms
 * - Critical decision
 *
 * Task Assignment:
 * - consensusThreshold: 0.5
 * - votingTimeout: 8000ms
 * - Simple majority
 *
 * INTEGRATION POINTS:
 * - Used by: VotingSystem for all vote processing
 * - Modified by: Anthony's UI for mission-specific tuning
 * - Read by: Consensus calculation algorithms
 */
package com.team6.swarm.intelligence.voting;

public class VotingParameters {
    // Consensus requirements
    public double consensusThreshold;    // 0.0 to 1.0 (percentage needed)
    public int minimumQuorum;            // Minimum voters required
    public boolean requireUnanimous;     // Must have 100% agreement
    
    // Timing parameters
    public long votingTimeout;           // Milliseconds to wait
    public int maxVotingRounds;          // Retry attempts
    public long revoteDelay;             // Time between rounds
    
    // Voting behavior
    public boolean allowAbstention;      // Can agents abstain?
    public boolean useWeightedVoting;    // Enable vote weighting?
    public boolean allowRevoting;        // Can change vote?
    
    // Fallback strategies
    public TimeoutFallback timeoutFallback;
    public TiebreakerMode tiebreakerMode;
    public boolean leaderOverride;
    
    /**
     * Constructor with default standard voting parameters
     */
    public VotingParameters() {
        // Standard voting defaults
        this.consensusThreshold = 0.6;      // 60% agreement
        this.minimumQuorum = 3;             // At least 3 voters
        this.requireUnanimous = false;
        
        this.votingTimeout = 8000;          // 8 seconds
        this.maxVotingRounds = 2;           // Try twice
        this.revoteDelay = 3000;            // 3 second delay
        
        this.allowAbstention = true;
        this.useWeightedVoting = false;
        this.allowRevoting = false;
        
        this.timeoutFallback = TimeoutFallback.LEADER_DECIDES;
        this.tiebreakerMode = TiebreakerMode.LEADER_DECIDES;
        this.leaderOverride = true;
    }
    
    /**
     * Full constructor with all parameters
     */
    public VotingParameters(double consensusThreshold, int minimumQuorum,
                            boolean requireUnanimous, long votingTimeout,
                            int maxVotingRounds, boolean allowAbstention,
                            boolean useWeightedVoting) {
        this.consensusThreshold = consensusThreshold;
        this.minimumQuorum = minimumQuorum;
        this.requireUnanimous = requireUnanimous;
        this.votingTimeout = votingTimeout;
        this.maxVotingRounds = maxVotingRounds;
        this.revoteDelay = 3000;
        this.allowAbstention = allowAbstention;
        this.useWeightedVoting = useWeightedVoting;
        this.allowRevoting = false;
        this.timeoutFallback = TimeoutFallback.LEADER_DECIDES;
        this.tiebreakerMode = TiebreakerMode.LEADER_DECIDES;
        this.leaderOverride = true;
    }
    
    /**
     * Validate parameters are reasonable
     */
    public boolean validate() {
        if (consensusThreshold < 0.0 || consensusThreshold > 1.0) return false;
        if (minimumQuorum < 1) return false;
        if (votingTimeout < 1000 || votingTimeout > 60000) return false;
        if (maxVotingRounds < 1 || maxVotingRounds > 5) return false;
        if (revoteDelay < 0 || revoteDelay > 10000) return false;
        
        // Unanimous requires 100% threshold
        if (requireUnanimous && consensusThreshold != 1.0) {
            consensusThreshold = 1.0;
        }
        
        return true;
    }
    
    /**
     * Create preset for emergency voting
     */
    public static VotingParameters createEmergencyVoting() {
        VotingParameters params = new VotingParameters();
        params.requireUnanimous = true;
        params.consensusThreshold = 1.0;
        params.votingTimeout = 10000;
        // Ensure minimumQuorum does not exceed the number of available agents, or consensus may never be reached.
        params.minimumQuorum = 3;
        params.allowAbstention = false;
        params.leaderOverride = true;
        params.timeoutFallback = TimeoutFallback.LEADER_DECIDES;
        return params;
    }
    
    /**
     * Create preset for quick voting
     */
    public static VotingParameters createQuickVoting() {
        VotingParameters params = new VotingParameters();
        params.consensusThreshold = 0.5;   // Simple majority
        params.votingTimeout = 5000;       // 5 seconds
        params.minimumQuorum = 3;
        params.maxVotingRounds = 1;        // No retries
        return params;
    }
    
    /**
     * Create preset for deliberative voting
     */
    public static VotingParameters createDeliberativeVoting() {
        VotingParameters params = new VotingParameters();
        params.consensusThreshold = 0.67;  // Supermajority
        params.votingTimeout = 15000;      // 15 seconds
        params.minimumQuorum = 5;
        params.maxVotingRounds = 3;        // Multiple rounds
        params.useWeightedVoting = true;   // Consider expertise
        return params;
    }
    
    /**
     * Create preset for standard voting
     */
    public static VotingParameters createStandardVoting() {
        return new VotingParameters();  // Uses defaults
    }
    
    @Override
    public String toString() {
        return String.format(
            "VotingParameters[threshold=%.0f%% quorum=%d timeout=%dms rounds=%d unanimous=%s]",
            consensusThreshold * 100, minimumQuorum, votingTimeout, 
            maxVotingRounds, requireUnanimous
        );
    }
}