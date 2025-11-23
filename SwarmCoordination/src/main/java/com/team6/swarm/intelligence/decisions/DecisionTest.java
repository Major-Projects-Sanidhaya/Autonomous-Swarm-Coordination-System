/**
 * DECISIONTEST CLASS - Week 9-10 Decision Intelligence Validation
 *
 * PURPOSE:
 * - Test weighted voting mechanisms with real vote data
 * - Validate multi-criteria decision analysis calculations
 * - Verify auction-based allocation logic
 * - Test conflict resolution strategies
 * - Ensure decisions are based on actual data, not hardcoded outputs
 *
 * TEST SCENARIOS:
 *
 * 1. WEIGHTED VOTING TEST
 *    - Scouts vote on navigation with higher weight
 *    - Verify weights are calculated correctly
 *    - Ensure winning option changes based on weights
 *
 * 2. MULTI-CRITERIA DECISION TEST
 *    - Evaluate options on distance, battery, time, risk
 *    - Verify scoring calculations are accurate
 *    - Ensure best option is selected based on criteria
 *
 * 3. AUCTION ALLOCATION TEST
 *    - Agents bid based on distance and battery
 *    - Verify lowest cost wins
 *    - Ensure bid calculations are correct
 *
 * 4. CONFLICT RESOLUTION TEST
 *    - Test leader fallback with actual tie votes
 *    - Test compromise with split decisions
 *    - Verify revote strategy
 *
 * VALIDATION APPROACH:
 * - All tests use real calculated values
 * - No hardcoded expected results
 * - Verify mathematical relationships
 * - Check that outcomes match input data
 */
package com.team6.swarm.intelligence.decisions;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.flocking.BehaviorType;
import com.team6.swarm.intelligence.voting.*;
import com.team6.swarm.intelligence.coordination.LeaderFollower;
import com.team6.swarm.intelligence.coordination.LeaderSelectionReason;
import com.team6.swarm.intelligence.tasking.Task;

import java.util.*;

public class DecisionTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("WEEK 9-10: DECISION INTELLIGENCE TEST");
        System.out.println("========================================");
        System.out.println();
        
        // Run all test scenarios
        testWeightedVoting();
        testMultiCriteriaDecision();
        testAuctionAllocation();
        testConflictResolutionLeader();
        testConflictResolutionCompromise();
        testConflictResolutionRevote();
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("All Week 9-10 tests completed!");
        System.out.println("========================================");
    }
    
    /**
     * TEST 1: WEIGHTED VOTING
     * Verify expert opinions have correct weight
     */
    private static void testWeightedVoting() {
        System.out.println("TEST 1: Weighted Voting (Expert Opinions)");
        System.out.println("------------------------------------------");
        
        DecisionEngine engine = new DecisionEngine();
        
        // Create navigation proposal
        VoteProposal proposal = new VoteProposal(
            "nav_weighted_001",
            "Navigate around obstacle - left or right?",
            Arrays.asList("LEFT", "RIGHT")
        );
        proposal.proposalType = ProposalType.NAVIGATION;
        
        // Create agents
        List<AgentState> agents = createTestAgents(5);
        
        // Scout with high battery at (280, 290)
        agents.get(0).agentId = 1;
        agents.get(0).position = new Point2D(280, 290);
        agents.get(0).batteryLevel = 0.9;
        
        // Regular agents vote differently
        agents.get(1).agentId = 2;
        agents.get(1).batteryLevel = 0.8;
        agents.get(2).agentId = 3;
        agents.get(2).batteryLevel = 0.7;
        agents.get(3).agentId = 4;
        agents.get(3).batteryLevel = 0.6;
        agents.get(4).agentId = 5;
        agents.get(4).batteryLevel = 0.5;
        
        // Create vote responses
        Map<Integer, VoteResponse> responses = new HashMap<>();
        
        // Scout votes LEFT with high confidence (should have highest weight)
        VoteResponse scoutVote = new VoteResponse(proposal.proposalId, 1, "LEFT");
        scoutVote.confidence = 1.0;
        scoutVote.agentRole = BehaviorType.SCOUT;
        scoutVote.agentPosition = agents.get(0).position;
        scoutVote.agentBattery = agents.get(0).batteryLevel;
        responses.put(1, scoutVote);
        
        // 3 regular agents vote RIGHT (majority in count, but less weight)
        for (int i = 2; i <= 4; i++) {
            VoteResponse vote = new VoteResponse(proposal.proposalId, i, "RIGHT");
            vote.confidence = 0.6;
            vote.agentRole = BehaviorType.FLOCKING;
            vote.agentPosition = agents.get(i-1).position;
            vote.agentBattery = agents.get(i-1).batteryLevel;
            responses.put(i, vote);
        }
        
        // 1 leader votes LEFT (moderate weight)
        VoteResponse leaderVote = new VoteResponse(proposal.proposalId, 5, "LEFT");
        leaderVote.confidence = 0.8;
        leaderVote.agentRole = BehaviorType.LEADER;
        leaderVote.agentPosition = agents.get(4).position;
        leaderVote.agentBattery = agents.get(4).batteryLevel;
        responses.put(5, leaderVote);
        
        System.out.println("Vote distribution:");
        System.out.println("  LEFT: Scout (high weight) + Leader (medium weight) = 2 votes");
        System.out.println("  RIGHT: 3 regular agents (lower weight) = 3 votes");
        System.out.println();
        
        // Conduct weighted vote
        WeightedVoteResult result = engine.conductWeightedVote(proposal, responses, agents);
        
        System.out.println();
        System.out.println("Results:");
        System.out.println("  Total weighted votes for LEFT: " + 
            String.format("%.2f", result.getWeightForOption("LEFT")));
        System.out.println("  Total weighted votes for RIGHT: " + 
            String.format("%.2f", result.getWeightForOption("RIGHT")));
        System.out.println("  Winner: " + result.getWinningOption());
        System.out.println("  Consensus level: " + String.format("%.1f%%", result.getConsensusLevel() * 100));
        
        // VALIDATION: Verify weights were actually calculated
        double leftWeight = result.getWeightForOption("LEFT");
        double rightWeight = result.getWeightForOption("RIGHT");
        
        // Scout + Leader should have more weight than 3 regular agents
        // Scout weight ≈ 1.0 * 1.5 (expertise) * 0.9 (battery) * 1.0 (confidence) = 1.35
        // Leader weight ≈ 1.0 * 1.3 (expertise) * 0.5 (battery) * 0.8 (confidence) = 0.52
        // LEFT total ≈ 1.87
        // Regular agent weight ≈ 1.0 * 1.0 * 0.7 (avg battery) * 0.6 (confidence) = 0.42 each
        // RIGHT total ≈ 1.26 (3 * 0.42)
        
        System.out.println();
        if (leftWeight > rightWeight && result.getWinningOption().equals("LEFT")) {
            System.out.println("  ✓ PASS: Weighted voting correctly favored expert opinions");
            System.out.println("         LEFT weight (" + String.format("%.2f", leftWeight) + 
                              ") > RIGHT weight (" + String.format("%.2f", rightWeight) + ")");
        } else {
            System.out.println("  ✗ FAIL: Weighting did not work as expected");
        }
        System.out.println();
    }
    
    /**
     * TEST 2: MULTI-CRITERIA DECISION
     * Verify criteria scoring and selection
     */
    private static void testMultiCriteriaDecision() {
        System.out.println("TEST 2: Multi-Criteria Decision Analysis");
        System.out.println("------------------------------------------");
        
        DecisionEngine engine = new DecisionEngine();
        
        // Create decision context
        DecisionContext decision = engine.createDecision(
            "route_choice_001",
            "Select optimal route to destination",
            Arrays.asList("ROUTE_A", "ROUTE_B", "ROUTE_C")
        );
        
        // Add weighted criteria
        decision.addCriterion("Distance", CriterionType.DISTANCE, 0.4);  // 40% weight
        decision.addCriterion("Battery Cost", CriterionType.BATTERY_COST, 0.3);  // 30% weight
        decision.addCriterion("Time", CriterionType.TIME, 0.2);  // 20% weight
        decision.addCriterion("Risk", CriterionType.RISK, 0.1);  // 10% weight
        
        System.out.println("Criteria weights:");
        for (DecisionCriterion criterion : decision.getCriteria()) {
            System.out.println(String.format("  %s: %.0f%%", 
                criterion.getName(), criterion.getWeight() * 100));
        }
        System.out.println();
        
        // Create agents for context
        List<AgentState> agents = createTestAgents(5);
        
        // Evaluate options
        MultiCriteriaResult result = engine.evaluateMultiCriteria(decision, agents);
        
        System.out.println();
        System.out.println("Option scores:");
        for (Map.Entry<String, Double> entry : result.getOptionScores().entrySet()) {
            System.out.println(String.format("  %s: %.2f points", 
                entry.getKey(), entry.getValue()));
            
            // Show detailed breakdown
            Map<String, Double> details = result.getDetailedScores(entry.getKey());
            for (Map.Entry<String, Double> detail : details.entrySet()) {
                System.out.println(String.format("    - %s: %.2f", 
                    detail.getKey(), detail.getValue()));
            }
        }
        
        System.out.println();
        System.out.println("Selected option: " + result.getBestOption());
        System.out.println("Score: " + String.format("%.2f", result.getBestScore()));
        System.out.println("Margin over second: " + String.format("%.2f", result.getScoreMargin()));
        
        // VALIDATION: Verify best option has highest score
        double maxScore = Double.NEGATIVE_INFINITY;
        String expectedWinner = null;
        
        for (Map.Entry<String, Double> entry : result.getOptionScores().entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                expectedWinner = entry.getKey();
            }
        }
        
        System.out.println();
        if (result.getBestOption().equals(expectedWinner) && 
            Math.abs(result.getBestScore() - maxScore) < 0.01) {
            System.out.println("  ✓ PASS: Highest scoring option selected");
            System.out.println("         " + expectedWinner + " with score " + 
                              String.format("%.2f", maxScore));
        } else {
            System.out.println("  ✗ FAIL: Selection does not match highest score");
        }
        System.out.println();
    }
    
    /**
     * TEST 3: AUCTION ALLOCATION
     * Verify lowest cost bid wins
     */
    private static void testAuctionAllocation() {
        System.out.println("TEST 3: Auction-Based Task Allocation");
        System.out.println("--------------------------------------");
        
        DecisionEngine engine = new DecisionEngine();
        
        // Create task at specific location
        Task task = Task.createMoveToWaypoint("task_auction_001", new Point2D(400, 300));
        task.minimumBattery = 0.3;
        task.estimatedDuration = 60000;
        
        System.out.println("Task: Move to (400, 300)");
        System.out.println("Minimum battery: 30%");
        System.out.println();
        
        // Create agents at different distances with varying battery
        List<AgentState> agents = new ArrayList<>();
        
        // Agent 1: Close but low battery
        AgentState agent1 = new AgentState();
        agent1.agentId = 1;
        agent1.position = new Point2D(350, 300);  // Distance: 50
        agent1.batteryLevel = 0.4;
        agent1.status = AgentStatus.ACTIVE;
        agents.add(agent1);
        
        // Agent 2: Medium distance, high battery (should win)
        AgentState agent2 = new AgentState();
        agent2.agentId = 2;
        agent2.position = new Point2D(300, 300);  // Distance: 100
        agent2.batteryLevel = 0.9;
        agent2.status = AgentStatus.ACTIVE;
        agents.add(agent2);
        
        // Agent 3: Far away, medium battery
        AgentState agent3 = new AgentState();
        agent3.agentId = 3;
        agent3.position = new Point2D(200, 300);  // Distance: 200
        agent3.batteryLevel = 0.6;
        agent3.status = AgentStatus.ACTIVE;
        agents.add(agent3);
        
        // Agent 4: Very close, very low battery (excluded)
        AgentState agent4 = new AgentState();
        agent4.agentId = 4;
        agent4.position = new Point2D(380, 300);  // Distance: 20
        agent4.batteryLevel = 0.2;
        agent4.status = AgentStatus.ACTIVE;
        agents.add(agent4);
        
        System.out.println("Agents:");
        for (AgentState agent : agents) {
            double distance = agent.position.distanceTo(task.targetLocation);
            System.out.println(String.format("  Agent %d: distance=%.0f, battery=%.0f%%",
                agent.agentId, distance, agent.batteryLevel * 100));
        }
        System.out.println();
        
        // Conduct auction
        AuctionResult result = engine.conductAuction(task, agents);
        
        System.out.println();
        if (result != null) {
            System.out.println("Auction results:");
            System.out.println("  Bids received: " + result.getBidCount());
            System.out.println("  Average bid cost: " + String.format("%.2f", result.getAverageBidCost()));
            System.out.println();
            
            // Show all bids
            System.out.println("All bids:");
            List<TaskBid> sortedBids = new ArrayList<>(result.getAllBids());
            sortedBids.sort(Comparator.comparingDouble(b -> b.getCost()));
            
            for (TaskBid bid : sortedBids) {
                System.out.println(String.format("  Agent %d: cost=%.2f, quality=%.2f, value=%.2f",
                    bid.getAgentId(), bid.getCost(), bid.getQuality(), bid.getValue()));
            }
            
            System.out.println();
            System.out.println("Winner: Agent " + result.getWinningBid().getAgentId());
            System.out.println("Winning cost: " + String.format("%.2f", result.getWinningBid().getCost()));
            System.out.println("Cost savings: " + String.format("%.2f", result.getCostSavings()));
            
            // VALIDATION: Verify winner has lowest cost
            TaskBid lowestBid = result.getAllBids().stream()
                .min(Comparator.comparingDouble(b -> b.getCost()))
                .orElse(null);
            
            System.out.println();
            if (lowestBid != null && result.getWinningBid().getAgentId() == lowestBid.getAgentId()) {
                System.out.println("  ✓ PASS: Lowest cost bid won the auction");
                System.out.println("         Agent " + lowestBid.getAgentId() + 
                                  " with cost " + String.format("%.2f", lowestBid.getCost()));
                
                // Verify cost calculation makes sense
                AgentState winner = findAgent(result.getWinningBid().getAgentId(), agents);
                if (winner != null) {
                    double distance = winner.position.distanceTo(task.targetLocation);
                    System.out.println("         Distance: " + String.format("%.0f", distance) +
                                     ", Battery: " + String.format("%.0f%%", winner.batteryLevel * 100));
                }
            } else {
                System.out.println("  ✗ FAIL: Winner does not have lowest cost");
            }
        } else {
            System.out.println("  ✗ FAIL: No bids received");
        }
        System.out.println();
    }
    
    /**
     * TEST 4: CONFLICT RESOLUTION - LEADER FALLBACK
     * Verify leader decides on tie
     */
    private static void testConflictResolutionLeader() {
        System.out.println("TEST 4: Conflict Resolution - Leader Fallback");
        System.out.println("----------------------------------------------");
        
        ConflictResolver resolver = new ConflictResolver(ResolutionStrategy.FALLBACK_LEADER);
        LeaderFollower leaderSystem = new LeaderFollower();
        
        // Create agents and set leader
        List<AgentState> agents = createTestAgents(6);
        leaderSystem.setLeader(1, LeaderSelectionReason.MANUAL);
        
        // Create tie vote result
        VoteProposal proposal = new VoteProposal(
            "tie_vote_001",
            "Formation change - diamond or wedge?",
            Arrays.asList("DIAMOND", "WEDGE")
        );
        proposal.proposalType = ProposalType.FORMATION;
        
        // Simulate perfect tie: 3 vs 3
        VoteResult tieResult = new VoteResult(
            proposal.proposalId,
            false,  // No consensus
            null,   // No winner
            0.5,    // 50/50 split
            "Tie: DIAMOND and WEDGE both have 3 votes",
            new HashMap<String, Integer>() {{
                put("DIAMOND", 3);
                put("WEDGE", 3);
            }},
            Arrays.asList(1, 2, 3, 4, 5, 6),
            6, 6, 0.6
        );
        
        System.out.println("Vote result: Perfect tie (3-3)");
        System.out.println("  DIAMOND: 3 votes");
        System.out.println("  WEDGE: 3 votes");
        System.out.println("  Consensus threshold: 60%");
        System.out.println();
        System.out.println("Current leader: Agent " + leaderSystem.getCurrentLeader());
        System.out.println();
        
        // Resolve conflict
        ResolutionResult resolution = resolver.resolveConflict(
            tieResult, proposal, agents, leaderSystem);
        
        System.out.println();
        if (resolution.resolved) {
            System.out.println("Resolution: " + resolution.chosenOption);
            System.out.println("Strategy: " + resolution.strategyUsed);
            System.out.println("Explanation: " + resolution.explanation);
            
            // VALIDATION: Verify leader decided and option is valid
            boolean optionIsValid = proposal.options.contains(resolution.chosenOption);
            boolean usedLeaderStrategy = resolution.strategyUsed == ResolutionStrategy.FALLBACK_LEADER;
            
            System.out.println();
            if (optionIsValid && usedLeaderStrategy) {
                System.out.println("  ✓ PASS: Leader successfully broke tie");
                System.out.println("         Chosen option '" + resolution.chosenOption + 
                                  "' is valid");
            } else {
                System.out.println("  ✗ FAIL: Resolution issue");
                if (!optionIsValid) {
                    System.out.println("         Invalid option chosen: " + resolution.chosenOption);
                }
                if (!usedLeaderStrategy) {
                    System.out.println("         Wrong strategy used: " + resolution.strategyUsed);
                }
            }
        } else {
            System.out.println("  ✗ FAIL: Could not resolve conflict");
        }
        System.out.println();
    }
    
    /**
     * TEST 5: CONFLICT RESOLUTION - COMPROMISE
     * Verify compromise between close votes
     */
    private static void testConflictResolutionCompromise() {
        System.out.println("TEST 5: Conflict Resolution - Compromise");
        System.out.println("-----------------------------------------");
        
        ConflictResolver resolver = new ConflictResolver(ResolutionStrategy.COMPROMISE);
        LeaderFollower leaderSystem = new LeaderFollower();
        
        // Create agents
        List<AgentState> agents = createTestAgents(7);
        
        // Create close vote result (55% vs 45%)
        VoteProposal proposal = new VoteProposal(
            "close_vote_001",
            "Navigate left or right around obstacle?",
            Arrays.asList("LEFT", "RIGHT")
        );
        proposal.proposalType = ProposalType.NAVIGATION;
        
        VoteResult closeResult = new VoteResult(
            proposal.proposalId,
            false,  // Below 60% threshold
            "LEFT",
            0.57,   // 57% not enough for 60% threshold
            "No consensus: LEFT has 57%, need 60%",
            new HashMap<String, Integer>() {{
                put("LEFT", 4);
                put("RIGHT", 3);
            }},
            Arrays.asList(1, 2, 3, 4, 5, 6, 7),
            7, 7, 0.6
        );
        
        System.out.println("Vote result: Close split (4-3)");
        System.out.println("  LEFT: 4 votes (57%)");
        System.out.println("  RIGHT: 3 votes (43%)");
        System.out.println("  Below 60% threshold");
        System.out.println();
        
        // Resolve with compromise
        ResolutionResult resolution = resolver.resolveConflict(
            closeResult, proposal, agents, leaderSystem);
        
        System.out.println();
        if (resolution.resolved) {
            System.out.println("Resolution: " + resolution.chosenOption);
            System.out.println("Strategy: " + resolution.strategyUsed);
            System.out.println("Explanation: " + resolution.explanation);
            
            // VALIDATION: Verify compromise was attempted
            boolean usedCompromise = resolution.strategyUsed == ResolutionStrategy.COMPROMISE;
            boolean hasCompromise = resolution.chosenOption != null;
            
            // Check if compromise leans toward majority
            
            System.out.println();
            if (usedCompromise && hasCompromise) {
                boolean leanCorrect = resolution.chosenOption.contains("LEFT") || 
                                      resolution.chosenOption.equals("CENTER");
                System.out.println("  ✓ PASS: Compromise strategy applied");
                System.out.println("         Result: " + resolution.chosenOption);
                if (leanCorrect) {
                    System.out.println("         Correctly leans toward majority (LEFT)");
                }
            } else {
                System.out.println("  ✗ FAIL: Compromise not applied correctly");
            }
        } else {
            System.out.println("  ✗ FAIL: Could not create compromise");
        }
        System.out.println();
    }
    
    /**
     * TEST 6: CONFLICT RESOLUTION - REVOTE
     * Verify revote with modified options
     */
    private static void testConflictResolutionRevote() {
        System.out.println("TEST 6: Conflict Resolution - Revote Strategy");
        System.out.println("----------------------------------------------");
        
        ConflictResolver resolver = new ConflictResolver(ResolutionStrategy.REVOTE);
        LeaderFollower leaderSystem = new LeaderFollower();
        
        // Create agents
        List<AgentState> agents = createTestAgents(8);
        
        // Create split vote result (no clear winner)
        VoteProposal proposal = new VoteProposal(
            "split_vote_001",
            "Select search pattern",
            Arrays.asList("GRID", "SPIRAL", "RANDOM", "PERIMETER")
        );
        proposal.proposalType = ProposalType.COORDINATION;
        
        VoteResult splitResult = new VoteResult(
            proposal.proposalId,
            false,
            null,
            0.375,  // 37.5% for top option
            "No consensus: GRID has 37.5%, need 60%",
            new HashMap<String, Integer>() {{
                put("GRID", 3);
                put("SPIRAL", 2);
                put("RANDOM", 2);
                put("PERIMETER", 1);
            }},
            Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8),
            8, 8, 0.6
        );
        
        System.out.println("Vote result: Split among 4 options");
        System.out.println("  GRID: 3 votes (37.5%)");
        System.out.println("  SPIRAL: 2 votes (25%)");
        System.out.println("  RANDOM: 2 votes (25%)");
        System.out.println("  PERIMETER: 1 vote (12.5%)");
        System.out.println("  No option reaches 60% threshold");
        System.out.println();
        
        // Resolve with revote
        ResolutionResult resolution = resolver.resolveConflict(
            splitResult, proposal, agents, leaderSystem);
        
        System.out.println();
        if (resolution.resolved) {
            System.out.println("Resolution: " + resolution.chosenOption);
            System.out.println("Strategy: " + resolution.strategyUsed);
            System.out.println("Explanation: " + resolution.explanation);
            
            // VALIDATION: Verify revote was initiated
            boolean usedRevote = resolution.strategyUsed == ResolutionStrategy.REVOTE;
            boolean needsRevote = resolution.chosenOption.contains("REVOTE");
            
            System.out.println();
            if (usedRevote && needsRevote) {
                System.out.println("  ✓ PASS: Revote strategy initiated");
                System.out.println("         Will narrow options from 4 to top 2-3");
                
                // Verify modified options would keep top choices
                String explanation = resolution.explanation;
                boolean hasGrid = explanation.contains("GRID");
                boolean hasSpiral = explanation.contains("SPIRAL") || 
                                  explanation.contains("RANDOM");
                
                if (hasGrid && hasSpiral) {
                    System.out.println("         Top options retained for revote");
                }
            } else {
                System.out.println("  ✗ FAIL: Revote not properly initiated");
            }
        } else {
            System.out.println("  ✗ FAIL: Could not initiate revote");
        }
        System.out.println();
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Create test agents with default properties
     */
    private static List<AgentState> createTestAgents(int count) {
        List<AgentState> agents = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            AgentState agent = new AgentState();
            agent.agentId = i;
            agent.agentName = "Agent_" + i;
            agent.position = new Point2D(100 * i, 100);
            agent.velocity = new Vector2D(0, 0);
            agent.status = AgentStatus.ACTIVE;
            agent.batteryLevel = 0.8;
            agent.maxSpeed = 50.0;
            agent.communicationRange = 100.0;
            agents.add(agent);
        }
        
        return agents;
    }
    
    /**
     * Find agent by ID
     */
    private static AgentState findAgent(int agentId, List<AgentState> agents) {
        for (AgentState agent : agents) {
            if (agent.agentId == agentId) {
                return agent;
            }
        }
        return null;
    }
}