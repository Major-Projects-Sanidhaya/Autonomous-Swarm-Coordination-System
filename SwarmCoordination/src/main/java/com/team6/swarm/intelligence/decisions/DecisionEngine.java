/**
 * DECISIONENGINE CLASS - Advanced Decision-Making Beyond Simple Voting
 *
 * PURPOSE:
 * - Implement sophisticated decision mechanisms beyond majority rule
 * - Support weighted voting based on agent expertise and context
 * - Enable multi-criteria decision analysis
 * - Provide auction-based task allocation
 *
 * DECISION TYPES:
 *
 * 1. WEIGHTED VOTING:
 *    - Expert agents get higher vote weight
 *    - Battery level affects vote influence
 *    - Distance to problem affects weight
 *    - Confidence scores integrated
 *
 * 2. MULTI-CRITERIA DECISIONS:
 *    - Evaluate multiple factors simultaneously
 *    - Calculate scores for each option
 *    - Weight criteria by importance
 *    - Select best overall option
 *
 * 3. AUCTION-BASED ALLOCATION:
 *    - Agents bid for tasks based on cost
 *    - Lowest cost wins assignment
 *    - Market-based coordination
 *    - Efficient resource allocation
 *
 * WEIGHTED VOTING ALGORITHM:
 * 
 * For each agent vote:
 *   baseWeight = 1.0
 *   
 *   // Expertise weighting
 *   if (agent.role == SCOUT && decision.type == NAVIGATION):
 *     baseWeight *= 1.5
 *   
 *   // Battery weighting
 *   baseWeight *= agent.batteryLevel
 *   
 *   // Proximity weighting
 *   if (distance < 50):
 *     baseWeight *= 1.2
 *   
 *   // Confidence weighting
 *   baseWeight *= vote.confidence
 *   
 *   totalWeight[option] += baseWeight
 *
 * MULTI-CRITERIA SCORING:
 * 
 * For each option:
 *   score = 0
 *   
 *   for each criterion:
 *     criterionScore = evaluateCriterion(option, criterion)
 *     weightedScore = criterionScore * criterion.weight
 *     score += weightedScore
 *   
 *   Select option with highest score
 *
 * AUCTION BIDDING:
 * 
 * For each agent:
 *   cost = calculateCost(agent, task)
 *   bid = Bid(agentId, cost, details)
 *   
 * Select agent with lowest cost bid
 *
 * INTEGRATION POINTS:
 * - Extends: VotingSystem for advanced voting
 * - Uses: AgentState for capability assessment
 * - Coordinates with: TaskAllocator for auction-based allocation
 * - Reports to: Anthony's UI for decision visualization
 */
package com.team6.swarm.intelligence.decisions;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.flocking.BehaviorType;
import com.team6.swarm.intelligence.voting.*;
import com.team6.swarm.intelligence.tasking.Task;

import java.util.*;

public class DecisionEngine {
    // Decision tracking
    private final Map<String, DecisionContext> activeDecisions;
    private final List<DecisionResult> decisionHistory;
    
    // Performance metrics
    private int totalDecisions;
    private int weightedVotingDecisions;
    private int multiCriteriaDecisions;
    private int auctionDecisions;
    
    /**
     * Constructor
     */
    public DecisionEngine() {
        this.activeDecisions = new HashMap<>();
        this.decisionHistory = new ArrayList<>();
        this.totalDecisions = 0;
        this.weightedVotingDecisions = 0;
        this.multiCriteriaDecisions = 0;
        this.auctionDecisions = 0;
    }
    
    // ==================== WEIGHTED VOTING ====================
    
    /**
     * CONDUCT WEIGHTED VOTE
     * Vote where agent expertise and context affect weight
     *
     * @param proposal Vote proposal
     * @param responses Agent vote responses
     * @param agents Current agent states for context
     * @return Weighted vote result
     */
    public WeightedVoteResult conductWeightedVote(VoteProposal proposal,
                                                   Map<Integer, VoteResponse> responses,
                                                   List<AgentState> agents) {
        System.out.println("Conducting weighted vote: " + proposal.proposalId);
        
        // Calculate weighted vote counts
        Map<String, Double> weightedVotes = new HashMap<>();
        Map<Integer, Double> agentWeights = new HashMap<>();
        
        // Initialize vote counts
        for (String option : proposal.options) {
            weightedVotes.put(option, 0.0);
        }
        
        // Calculate weight for each vote
        for (VoteResponse response : responses.values()) {
            // Find agent state for context
            AgentState agent = findAgent(response.voterId, agents);
            if (agent == null) continue;
            
            // Calculate vote weight based on multiple factors
            double weight = calculateVoteWeight(response, agent, proposal);
            agentWeights.put(response.voterId, weight);
            
            // Add weighted vote to total
            double currentVotes = weightedVotes.get(response.choice);
            weightedVotes.put(response.choice, currentVotes + weight);
            
            System.out.println(String.format("  Agent %d: %s (weight: %.2f)",
                response.voterId, response.choice, weight));
        }
        
        // Find winning option
        String winningOption = null;
        double maxWeight = 0;
        double totalWeight = 0;
        
        for (Map.Entry<String, Double> entry : weightedVotes.entrySet()) {
            totalWeight += entry.getValue();
            if (entry.getValue() > maxWeight) {
                maxWeight = entry.getValue();
                winningOption = entry.getKey();
            }
        }
        
        // Calculate consensus level
        double consensusLevel = totalWeight > 0 ? maxWeight / totalWeight : 0;
        
        // Create result
        WeightedVoteResult result = new WeightedVoteResult(
            proposal.proposalId,
            consensusLevel >= 0.6,  // Default threshold
            winningOption,
            consensusLevel,
            weightedVotes,
            agentWeights
        );
        
        weightedVotingDecisions++;
        totalDecisions++;
        
        System.out.println(String.format("Weighted vote result: %s wins with %.1f%% (%.2f/%.2f weighted votes)",
            winningOption, consensusLevel * 100, maxWeight, totalWeight));
        
        return result;
    }
    
    /**
     * CALCULATE VOTE WEIGHT
     * Determine how much influence an agent's vote should have
     */
    private double calculateVoteWeight(VoteResponse response, AgentState agent,
                                       VoteProposal proposal) {
        double weight = 1.0;  // Base weight
        
        // 1. EXPERTISE WEIGHTING (role-based)
        // Use role from VoteResponse if available, otherwise use generic weighting
        if (response.agentRole != null) {
            weight *= calculateExpertiseWeight(response.agentRole, proposal.proposalType);
        }
        
        // 2. BATTERY WEIGHTING
        // Lower battery = less weight (agent may not complete task)
        weight *= Math.max(0.5, agent.batteryLevel);  // Minimum 0.5x weight
        
        // 3. PROXIMITY WEIGHTING
        // Closer agents to problem have better information
        if (response.agentPosition != null && proposal.context != null) {
            weight *= calculateProximityWeight(response.agentPosition, proposal);
        }
        
        // 4. CONFIDENCE WEIGHTING
        weight *= response.confidence;
        
        // 5. EXPERIENCE WEIGHTING
        // Could track agent's historical decision quality
        // For now, use agent ID as proxy (lower ID = more experienced)
        double experienceBonus = 1.0 + (10.0 - agent.agentId) * 0.01;
        weight *= Math.max(1.0, Math.min(1.1, experienceBonus));
        
        return weight;
    }
    
    /**
     * Calculate expertise weight based on role and decision type
     */
    private double calculateExpertiseWeight(BehaviorType role, ProposalType proposalType) {
        if (role == null || proposalType == null) return 1.0;
        
        switch (proposalType) {
            case NAVIGATION:
                if (role == BehaviorType.SCOUT) return 1.5;
                if (role == BehaviorType.LEADER) return 1.3;
                break;
                
            case FORMATION:
                if (role == BehaviorType.LEADER) return 1.5;
                if (role == BehaviorType.FORMATION) return 1.4;
                break;
                
            case MISSION:
                if (role == BehaviorType.LEADER) return 1.4;
                if (role == BehaviorType.TASK_EXECUTION) return 1.2;
                break;
                
            case EMERGENCY:
                // All votes equal in emergencies
                return 1.0;
                
            default:
                break;
        }
        
        return 1.0;
    }
    
    /**
     * Calculate proximity weight (closer = better information)
     */
    private double calculateProximityWeight(Point2D agentPosition, VoteProposal proposal) {
        // Parse problem location from context if available
        // For now, use simple heuristic
        return 1.0;  // Placeholder - would need problem location in proposal
    }
    
    // ==================== MULTI-CRITERIA DECISION ====================
    
    /**
     * MULTI-CRITERIA DECISION ANALYSIS
     * Evaluate options based on multiple weighted criteria
     *
     * @param decision Decision context with options and criteria
     * @param agents Agent states for evaluation
     * @return Best option with scores
     */
    public MultiCriteriaResult evaluateMultiCriteria(DecisionContext decision,
                                                     List<AgentState> agents) {
        System.out.println("Multi-criteria evaluation: " + decision.decisionId);
        
        Map<String, Double> optionScores = new HashMap<>();
        Map<String, Map<String, Double>> detailedScores = new HashMap<>();
        
        // Evaluate each option
        for (String option : decision.options) {
            double totalScore = 0;
            Map<String, Double> criteriaScores = new HashMap<>();
            
            // Evaluate each criterion
            for (DecisionCriterion criterion : decision.criteria) {
                double score = evaluateCriterion(option, criterion, agents);
                double weightedScore = score * criterion.weight;
                
                criteriaScores.put(criterion.name, score);
                totalScore += weightedScore;
                
                System.out.println(String.format("  %s - %s: %.2f (weighted: %.2f)",
                    option, criterion.name, score, weightedScore));
            }
            
            optionScores.put(option, totalScore);
            detailedScores.put(option, criteriaScores);
        }
        
        // Find best option
        String bestOption = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Map.Entry<String, Double> entry : optionScores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestOption = entry.getKey();
            }
        }
        
        MultiCriteriaResult result = new MultiCriteriaResult(
            decision.decisionId,
            bestOption,
            bestScore,
            optionScores,
            detailedScores
        );
        
        multiCriteriaDecisions++;
        totalDecisions++;
        
        System.out.println(String.format("Best option: %s (score: %.2f)", bestOption, bestScore));
        
        return result;
    }
    
    /**
     * EVALUATE CRITERION
     * Score option on specific criterion
     */
    private double evaluateCriterion(String option, DecisionCriterion criterion,
                                     List<AgentState> agents) {
        switch (criterion.type) {
            case DISTANCE:
                return evaluateDistance(option, criterion, agents);
                
            case BATTERY_COST:
                return evaluateBatteryCost(option, criterion, agents);
                
            case TIME:
                return evaluateTime(option, criterion, agents);
                
            case RISK:
                return evaluateRisk(option, criterion, agents);
                
            case COVERAGE:
                return evaluateCoverage(option, criterion, agents);
                
            default:
                return 50.0;  // Neutral score
        }
    }
    
    /**
     * Evaluate distance criterion (shorter = better)
     */
    private double evaluateDistance(String option, DecisionCriterion criterion,
                                    List<AgentState> agents) {
        // Parse target location from option
        // Calculate average distance for all agents
        // Convert to score: closer = higher score
        double avgDistance = 100.0;  // Placeholder
        double maxDistance = 500.0;
        
        return Math.max(0, (maxDistance - avgDistance) / maxDistance * 100);
    }
    
    /**
     * Evaluate battery cost (lower cost = better)
     */
    private double evaluateBatteryCost(String option, DecisionCriterion criterion,
                                       List<AgentState> agents) {
        // Estimate battery consumption for this option
        double estimatedCost = 0.15;  // Placeholder: 15% battery
        
        // Convert to score: lower cost = higher score
        return Math.max(0, (1.0 - estimatedCost) * 100);
    }
    
    /**
     * Evaluate time criterion (faster = better)
     */
    private double evaluateTime(String option, DecisionCriterion criterion,
                               List<AgentState> agents) {
        // Estimate time to complete option
        double estimatedTime = 30.0;  // Placeholder: 30 seconds
        double maxTime = 120.0;
        
        return Math.max(0, (maxTime - estimatedTime) / maxTime * 100);
    }
    
    /**
     * Evaluate risk criterion (lower risk = better)
     */
    private double evaluateRisk(String option, DecisionCriterion criterion,
                               List<AgentState> agents) {
        // Assess risk level of option
        // Consider: collision probability, battery risk, failure risk
        double riskLevel = 0.2;  // Placeholder: 20% risk
        
        return Math.max(0, (1.0 - riskLevel) * 100);
    }
    
    /**
     * Evaluate coverage criterion (better coverage = better)
     */
    private double evaluateCoverage(String option, DecisionCriterion criterion,
                                   List<AgentState> agents) {
        // Estimate area coverage for option
        double coveragePercent = 0.75;  // Placeholder: 75% coverage
        
        return coveragePercent * 100;
    }
    
    // ==================== AUCTION-BASED ALLOCATION ====================
    
    /**
     * CONDUCT AUCTION
     * Agents bid for task, lowest cost wins
     *
     * @param task Task to auction
     * @param agents Potential bidders
     * @return Winning bid and agent
     */
    public AuctionResult conductAuction(Task task, List<AgentState> agents) {
        System.out.println("Auction for task: " + task.taskId);
        
        List<TaskBid> bids = new ArrayList<>();
        
        // Collect bids from all eligible agents
        for (AgentState agent : agents) {
            if (agent.status != AgentStatus.ACTIVE) continue;
            
            // Calculate agent's cost for this task
            double cost = calculateTaskCost(agent, task);
            
            // Agent only bids if cost is acceptable
            if (cost < 1000) {  // Maximum acceptable cost
                TaskBid bid = new TaskBid(
                    agent.agentId,
                    task.taskId,
                    cost,
                    calculateBidQuality(agent, task)
                );
                
                bids.add(bid);
                System.out.println(String.format("  Agent %d bids: cost=%.2f, quality=%.2f",
                    agent.agentId, cost, bid.quality));
            }
        }
        
        if (bids.isEmpty()) {
            System.out.println("No bids received for task");
            return null;
        }
        
        // Find winning bid (lowest cost, or best quality if costs similar)
        TaskBid winningBid = selectWinningBid(bids);
        
        AuctionResult result = new AuctionResult(
            task.taskId,
            winningBid,
            bids
        );
        
        auctionDecisions++;
        totalDecisions++;
        
        System.out.println(String.format("Winning bid: Agent %d (cost: %.2f)",
            winningBid.agentId, winningBid.cost));
        
        return result;
    }
    
    /**
     * CALCULATE TASK COST
     * Agent's cost to complete task
     */
    private double calculateTaskCost(AgentState agent, Task task) {
        double cost = 0;
        
        // 1. DISTANCE COST
        if (task.targetLocation != null) {
            double distance = agent.position.distanceTo(task.targetLocation);
            cost += distance * 0.5;  // Distance factor
        }
        
        // 2. BATTERY COST
        // Lower battery = higher cost (need to conserve)
        double batteryFactor = 2.0 - agent.batteryLevel;  // 1.0 to 2.0
        cost *= batteryFactor;
        
        // 3. WORKLOAD COST
        // Already busy agents have higher cost
        // Would integrate with TaskAllocator to get current load
        
        // 4. CAPABILITY MISMATCH COST
        // If task requires specific role, check if agent role matches
        // For now, use generic penalty for complexity
        if (task.requiredRole != null) {
            // Without agent.behaviorType, apply generic capability check
            // Could enhance this by storing current behavior in agent state
            cost *= 1.3;  // Generic capability uncertainty penalty
        }
        
        return cost;
    }
    
    /**
     * Calculate bid quality (higher = better)
     */
    private double calculateBidQuality(AgentState agent, Task task) {
        double quality = 50.0;  // Base quality
        
        // Higher battery = higher quality
        quality += agent.batteryLevel * 30;
        
        // Role match would increase quality
        // For now, use agent position and status as quality factors
        if (agent.status == AgentStatus.ACTIVE) {
            quality += 10;  // Active agents get bonus
        }
        
        // Closer agents typically have better task completion potential
        if (task.targetLocation != null) {
            double distance = agent.position.distanceTo(task.targetLocation);
            double distanceQuality = Math.max(0, (500 - distance) / 500 * 10);
            quality += distanceQuality;
        }
        
        return Math.min(100, quality);
    }
    
    /**
     * SELECT WINNING BID
     * Choose best bid from all submissions
     */
    private TaskBid selectWinningBid(List<TaskBid> bids) {
        // Sort by cost (lowest first)
        bids.sort(Comparator.comparingDouble(b -> b.cost));
        
        // If top bids are close in cost, choose by quality
        TaskBid bestBid = bids.get(0);
        
        if (bids.size() > 1) {
            TaskBid secondBid = bids.get(1);
            
            // If costs within 10%, choose by quality
            if (Math.abs(bestBid.cost - secondBid.cost) / bestBid.cost < 0.1) {
                if (secondBid.quality > bestBid.quality) {
                    bestBid = secondBid;
                }
            }
        }
        
        return bestBid;
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
     * CREATE DECISION CONTEXT
     * Set up multi-criteria decision
     */
    public DecisionContext createDecision(String decisionId, String description,
                                          List<String> options) {
        DecisionContext context = new DecisionContext(decisionId, description, options);
        activeDecisions.put(decisionId, context);
        return context;
    }
    
    /**
     * GET DECISION HISTORY
     */
    public List<DecisionResult> getDecisionHistory() {
        return new ArrayList<>(decisionHistory);
    }
    
    // ==================== PERFORMANCE METRICS ====================
    
    public int getTotalDecisions() {
        return totalDecisions;
    }
    
    public int getWeightedVotingDecisions() {
        return weightedVotingDecisions;
    }
    
    public int getMultiCriteriaDecisions() {
        return multiCriteriaDecisions;
    }
    
    public int getAuctionDecisions() {
        return auctionDecisions;
    }
    
    public void resetMetrics() {
        totalDecisions = 0;
        weightedVotingDecisions = 0;
        multiCriteriaDecisions = 0;
        auctionDecisions = 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "DecisionEngine[Total: %d | Weighted: %d | MultiCriteria: %d | Auction: %d]",
            totalDecisions, weightedVotingDecisions, multiCriteriaDecisions, auctionDecisions
        );
    }
}