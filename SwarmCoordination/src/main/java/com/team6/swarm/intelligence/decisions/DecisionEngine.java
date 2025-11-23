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
    private static final double DEFAULT_CONSENSUS_THRESHOLD = 0.6;
    private static final double MAX_ACCEPTABLE_BID_COST = 1000.0; // Maximum cost units before bid is rejected 
    
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
        // Determine threshold: prefer per-proposal value if present, otherwise use engine default
        double threshold = DEFAULT_CONSENSUS_THRESHOLD;
        if (proposal != null) {
          // Try a named field or getter if VoteProposal provides a per-proposal threshold
          try {
            // try getter first
            java.lang.reflect.Method m = proposal.getClass().getMethod("getConsensusThreshold");
            Object val = m.invoke(proposal);
            if (val instanceof Number && ((Number) val).doubleValue() > 0) {
              threshold = ((Number) val).doubleValue();
            }
          } catch (Exception ignoreGetter) {
            try {
              java.lang.reflect.Field f = proposal.getClass().getDeclaredField("consensusThreshold");
              f.setAccessible(true);
              Object val = f.get(proposal);
              if (val instanceof Number && ((Number) val).doubleValue() > 0) {
                threshold = ((Number) val).doubleValue();
              }
            } catch (Exception ignoreField) {
              // fallback to DEFAULT_CONSENSUS_THRESHOLD
            }
          }
        }

        WeightedVoteResult result = new WeightedVoteResult(
          proposal.proposalId,
          consensusLevel >= threshold,
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
     */
    private double calculateVoteWeight(VoteResponse response, AgentState agent, VoteProposal proposal) {
      // Base weight
      double weight = 1.0;

      // 1. EXPERTISE WEIGHTING (role-based)
      if (response.agentRole != null) {
        weight *= calculateExpertiseWeight(response.agentRole, proposal.proposalType);
      }

      // 2. BATTERY WEIGHTING
      weight *= Math.max(0.5, agent.batteryLevel);

      // 3. PROXIMITY WEIGHTING
      if (response.agentPosition != null && proposal.context != null) {
        weight *= calculateProximityWeight(response.agentPosition, proposal);
      }

      // 4. CONFIDENCE WEIGHTING
      weight *= response.confidence;

      // 5. EXPERIENCE WEIGHTING
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
        return 1.0;  //TODO Placeholder - would need problem location in proposal
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
        System.out.println("Multi-criteria evaluation: " + decision.getDecisionId());
        
        Map<String, Double> optionScores = new HashMap<>();
        Map<String, Map<String, Double>> detailedScores = new HashMap<>();
        
        // Evaluate each option
        for (String option : decision.getOptions()) {
            double totalScore = 0;
            Map<String, Double> criteriaScores = new HashMap<>();
            
            // Evaluate each criterion
            for (DecisionCriterion criterion : decision.getCriteria()) {
                double score = evaluateCriterion(option, criterion, agents);
                double weightedScore = score * criterion.getWeight();
                
                criteriaScores.put(criterion.getName(), score);
                totalScore += weightedScore;
                
                System.out.println(String.format("  %s - %s: %.2f (weighted: %.2f)",
                    option, criterion.getName(), score, weightedScore));
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
            decision.getDecisionId(),
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
      switch (criterion.getType()) {
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
     *
     * TODO: Replace the heuristic parsing below with structured option metadata
     *       (e.g., DecisionOption/Target objects) instead of parsing strings.
     *       Throwing UnsupportedOperationException makes it explicit this is a stub.
     */
    private double evaluateDistance(String option, DecisionCriterion criterion,
                    List<AgentState> agents) {
      // Try to parse a coordinate pair "(x,y)" or "x,y" from the option string.
      try {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\(?\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*,\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*\\)?");
        java.util.regex.Matcher m = p.matcher(option);
        if (m.find()) {
          double tx = Double.parseDouble(m.group(1));
          double ty = Double.parseDouble(m.group(2));

          // NOTE: Assumes Point2D has a (double,double) constructor and distanceTo method.
          // TODO: Use a proper target/location object instead of parsing strings.
          Point2D target = new Point2D(tx, ty);

          int count = 0;
          double sum = 0.0;
          for (AgentState a : agents) {
            if (a.position != null) {
              sum += a.position.distanceTo(target);
              count++;
            }
          }

          if (count == 0) {
            throw new UnsupportedOperationException("evaluateDistance: no agent positions available to compute distance for option: " + option);
          }

          double avgDistance = sum / count;
          double maxDistance = 500.0; // TODO: make configurable or derive from environment

          return Math.max(0, (maxDistance - avgDistance) / maxDistance * 100);
        }
      } catch (Exception e) {
        // fall through to explicit unsupported exception below
      }

      // If we cannot parse useful info from the option string, return a neutral score.
      // This keeps the multi-criteria evaluation robust when option metadata isn't structured.
      return 50.0; // neutral score (0-100)
    }

    /**
     * Evaluate battery cost (lower cost = better)
     *
     * TODO: Compute battery consumption based on distance, payload, maneuvers and agent models.
     */
    private double evaluateBatteryCost(String option, DecisionCriterion criterion,
                                      List<AgentState> agents) {
      // Try to parse explicit battery consumption like "batteryCost=0.15" or "consumption=15%"
      try {
        java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("battery[_-]?cost\\s*=\\s*([0-9]*\\.?[0-9]+)");
        java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("consumption\\s*=\\s*([0-9]*\\.?[0-9]+)\\s*%");

        java.util.regex.Matcher m1 = p1.matcher(option);
        java.util.regex.Matcher m2 = p2.matcher(option);

        if (m1.find()) {
          double estimatedCost = Double.parseDouble(m1.group(1)); // e.g., 0.15
          return Math.max(0, (1.0 - estimatedCost) * 100);
        } else if (m2.find()) {
          double estimatedPercent = Double.parseDouble(m2.group(1)) / 100.0;
          return Math.max(0, (1.0 - estimatedPercent) * 100);
        }

        // Fallback heuristic: if option contains a location, estimate battery from average agent distance
        java.util.regex.Pattern locP = java.util.regex.Pattern.compile("\\(?\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*,\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*\\)?");
        java.util.regex.Matcher lm = locP.matcher(option);
        if (lm.find()) {
          double tx = Double.parseDouble(lm.group(1));
          double ty = Double.parseDouble(lm.group(2));
          Point2D target = new Point2D(tx, ty);

          int count = 0;
          double sumDist = 0.0;
          for (AgentState a : agents) {
            if (a.position != null) {
              sumDist += a.position.distanceTo(target);
              count++;
            }
          }
          if (count == 0) {
            throw new UnsupportedOperationException("evaluateBatteryCost: no agent positions available to estimate battery cost for option: " + option);
          }
          double avgDistance = sumDist / count;

          // Simple linear battery model: cost = distance * factor / maxExpectedDistance
          double maxDistance = 500.0; // TODO: configure
          double estimatedCost = Math.min(1.0, avgDistance / maxDistance);
          return Math.max(0, (1.0 - estimatedCost) * 100);
        }
      } catch (Exception ignored) {
        // Fall through to neutral fallback
      }

      // Fallback: return neutral score when battery cost cannot be determined
      return 50.0;
    }

    // -------------------- TUNABLE CONSTANTS (replace magic numbers) --------------------
    /**
     * Distance cost factor: multiplier applied per distance unit when estimating task cost.
     * Rationale: 0.5 produces a moderate distance-driven cost so that distance contributes
     * meaningfully without dominating other factors (battery, capability).
     */
    private static final double DISTANCE_COST_FACTOR = 0.5;

    /**
     * Battery penalty maximum multiplier: used to increase cost for low-battery agents.
     * Rationale: 2.0 means an agent with minimal battery could see its nominal cost
     * doubled to reflect conservation needs and risk.
     */
    private static final double BATTERY_PENALTY_MAX = 2.0;

    /**
     * Role/Capability mismatch penalty multiplier.
     * Rationale: 1.3 applies a modest penalty when a task requires a specific role the
     * agent may not fully match; high enough to prefer matched agents but not so high
     * that borderline agents are completely excluded.
     */
    private static final double ROLE_MISMATCH_PENALTY = 1.3;

    /**
     * Bid quality base and component weights.
     * BID_BASE_QUALITY: baseline quality score for any bid.
     * QUALITY_BATTERY_WEIGHT: how much battery level contributes to quality (0..1 battery -> 0..30 points).
     * QUALITY_ACTIVE_BONUS: flat bonus for active agents.
     * QUALITY_DISTANCE_MAX_RANGE: distance used as normalization range for distance-based quality.
     * QUALITY_DISTANCE_MAX_BONUS: maximum additional quality points from proximity.
     *
     * Rationale: These values were chosen to yield quality scores roughly in the 0-100 range while
     * ensuring battery and proximity have significant but bounded influence.
     */
    private static final double BID_BASE_QUALITY = 50.0;
    private static final double QUALITY_BATTERY_WEIGHT = 30.0;
    private static final double QUALITY_ACTIVE_BONUS = 10.0;
    private static final double QUALITY_DISTANCE_MAX_RANGE = 500.0;
    private static final double QUALITY_DISTANCE_MAX_BONUS = 10.0;
    /**
     * Bid cost similarity threshold: if two top bids differ by less than this fraction,
     * prefer the higher-quality bid.
     */
    private static final double BID_COST_SIMILARITY_THRESHOLD = 0.1; // 10% cost difference threshold
    // -----------------------------------------------------------------------------------

    /**
     * Evaluate time criterion (faster = better)
     *
     * TODO: Replace heuristics with real ETA calculations using agent speeds, path planning estimates, and environment constraints.
     */
    private double evaluateTime(String option, DecisionCriterion criterion,
            List<AgentState> agents) {
      // Try explicit time parsing like "time=30" or "eta=30s"
      try {
      java.util.regex.Pattern pTime = java.util.regex.Pattern.compile("(?:time|eta)\\s*=\\s*([0-9]*\\.?[0-9]+)");
      java.util.regex.Matcher mTime = pTime.matcher(option);
      if (mTime.find()) {
        double estimatedTime = Double.parseDouble(mTime.group(1));
        double maxTime = 120.0; // TODO: derive from criterion or environment
        return Math.max(0, (maxTime - estimatedTime) / maxTime * 100);
      }

      // Fallback: estimate time from distance using a nominal speed
      java.util.regex.Pattern locP = java.util.regex.Pattern.compile("\\(?\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*,\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*\\)?");
      java.util.regex.Matcher lm = locP.matcher(option);
      if (lm.find()) {
        double tx = Double.parseDouble(lm.group(1));
        double ty = Double.parseDouble(lm.group(2));
        Point2D target = new Point2D(tx, ty);

        int count = 0;
        double sumDist = 0.0;
        for (AgentState a : agents) {
        if (a.position != null) {
          sumDist += a.position.distanceTo(target);
          count++;
        }
        }
        if (count == 0) {
        throw new UnsupportedOperationException("evaluateTime: no agent positions available to estimate time for option: " + option);
        }

        double avgDistance = sumDist / count;
        double nominalSpeed = 1.0; // units per second - TODO: replace with agent-specific speeds
        double estimatedTime = avgDistance / nominalSpeed;
        double maxTime = Math.max(estimatedTime, 120.0); // keep a sensible max cap

        return Math.max(0, (maxTime - estimatedTime) / maxTime * 100);
      }
      } catch (Exception ignored) {
        // fall through to neutral fallback
      }

      // Fallback: neutral score when ETA cannot be estimated
      return 50.0;
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
            if (cost < MAX_ACCEPTABLE_BID_COST) {  // Maximum acceptable cost
                TaskBid bid = new TaskBid(
                    agent.agentId,
                    task.taskId,
                    cost,
                    calculateBidQuality(agent, task)
                );
                
                bids.add(bid);
                System.out.println(String.format("  Agent %d bids: cost=%.2f, quality=%.2f",
                  agent.agentId, cost, bid.getQuality()));
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
          winningBid.getAgentId(), winningBid.getCost()));
        
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
        cost += distance * DISTANCE_COST_FACTOR;  // Distance factor (tunabled)
        }
        
        // 2. BATTERY COST
        // Lower battery = higher cost (need to conserve)
      double batteryFactor = BATTERY_PENALTY_MAX - agent.batteryLevel;  // e.g., 2.0 - batteryLevel -> 1.0..2.0
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
          cost *= ROLE_MISMATCH_PENALTY;  // Generic capability uncertainty penalty
        }
        
        return cost;
    }
    
    /**
     * Calculate bid quality (higher = better)
     */
    private double calculateBidQuality(AgentState agent, Task task) {
      double quality = BID_BASE_QUALITY;  // Base quality

      // Higher battery = higher quality (scaled)
      quality += agent.batteryLevel * QUALITY_BATTERY_WEIGHT;

      // Active agent bonus
      if (agent.status == AgentStatus.ACTIVE) {
        quality += QUALITY_ACTIVE_BONUS;
      }

      // Proximity contributes up to QUALITY_DISTANCE_MAX_BONUS points
      if (task.targetLocation != null) {
        double distance = agent.position.distanceTo(task.targetLocation);
        double distanceQuality = Math.max(0, (QUALITY_DISTANCE_MAX_RANGE - distance) / QUALITY_DISTANCE_MAX_RANGE * QUALITY_DISTANCE_MAX_BONUS);
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
        bids.sort(Comparator.comparingDouble(b -> b.getCost()));
        
        // If top bids are close in cost, choose by quality
        TaskBid bestBid = bids.get(0);
        
        if (bids.size() > 1) {
            TaskBid secondBid = bids.get(1);
            
            // If costs within similarity threshold, choose by quality
            if (Math.abs(bestBid.getCost() - secondBid.getCost()) / bestBid.getCost() < BID_COST_SIMILARITY_THRESHOLD) {
              if (secondBid.getQuality() > bestBid.getQuality()) {
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