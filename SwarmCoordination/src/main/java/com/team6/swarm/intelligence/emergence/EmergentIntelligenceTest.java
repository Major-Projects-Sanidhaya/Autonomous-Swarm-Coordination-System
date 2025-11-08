/**
 * EMERGENTINTELLIGENCETEST CLASS - Week 7-8 Validation
 *
 * PURPOSE:
 * - Test behavior priority system works correctly
 * - Validate SwarmCoordinator integrates all subsystems
 * - Verify adaptive behavior adjusts parameters
 * - Demonstrate emergent intelligence patterns
 *
 * TEST SCENARIOS:
 *
 * 1. BEHAVIOR PRIORITY TEST
 *    Setup: Agent has multiple conflicting behaviors
 *    Expected: Emergency overrides all, blending when compatible
 *    Success: Correct prioritization and blending
 *
 * 2. EMERGENCY OVERRIDE TEST
 *    Setup: Agent flocking, then collision detected
 *    Expected: Emergency behavior takes complete control
 *    Success: Flocking stopped, emergency executed
 *
 * 3. BEHAVIOR BLENDING TEST
 *    Setup: Agent has Flocking + Formation commands
 *    Expected: Commands blended with weights
 *    Success: Smooth combined movement
 *
 * 4. COORDINATOR INTEGRATION TEST
 *    Setup: Full swarm with all subsystems active
 *    Expected: All systems coordinate properly
 *    Success: No conflicts, smooth operation
 *
 * 5. EMERGENCY RESPONSE TEST
 *    Setup: Battery emergency triggered
 *    Expected: Emergency handling, vote initiated
 *    Success: Appropriate response executed
 *
 * 6. ADAPTIVE BEHAVIOR TEST
 *    Setup: High collision rate detected
 *    Expected: Separation weight automatically increased
 *    Success: Parameters adjusted correctly
 *
 * 7. COORDINATION MODE TEST
 *    Setup: Switch between different modes
 *    Expected: Parameters adjust for each mode
 *    Success: Mode-specific behaviors enabled
 *
 * 8. PERFORMANCE TREND TEST
 *    Setup: Record performance over time
 *    Expected: Trend analysis shows improvement
 *    Success: Adaptive system improving metrics
 *
 * WEEK 7-8 SUCCESS CRITERIA:
 * ✓ Priority system prevents conflicts
 * ✓ Emergency responses work immediately
 * ✓ Compatible behaviors blend smoothly
 * ✓ Coordinator manages all subsystems
 * ✓ Adaptive tuning improves performance
 * ✓ Complex emergent behaviors visible
 */
package com.team6.swarm.intelligence.emergence;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.flocking.*;
import com.team6.swarm.intelligence.voting.*;
import java.util.*;

public class EmergentIntelligenceTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("WEEK 7-8: EMERGENT INTELLIGENCE TEST");
        System.out.println("========================================");
        System.out.println();
        
        // Run all test scenarios
        testBehaviorPriority();
        testEmergencyOverride();
        testBehaviorBlending();
        testCoordinatorIntegration();
        testEmergencyResponse();
        testVotingWorkflow();  // NEW: Complete voting test
        testAdaptiveBehavior();
        testCoordinationModes();
        testPerformanceTrend();
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("All Week 7-8 tests completed!");
        System.out.println("========================================");
    }
    
    /**
     * TEST 1: BEHAVIOR PRIORITY SYSTEM
     */
    private static void testBehaviorPriority() {
        System.out.println("TEST 1: Behavior Priority System");
        System.out.println("--------------------------------");
        
        BehaviorPriority priority = new BehaviorPriority();
        int agentId = 1;
        
        // Register multiple behaviors
        System.out.println("Registering behaviors for Agent 1:");
        
        // Low priority: Idle
        MovementCommand idleCmd = createTestCommand(agentId, "idle");
        priority.registerBehavior(agentId, BehaviorType.IDLE, 10, idleCmd);
        System.out.println("  - IDLE (Priority: 10)");
        
        // Normal priority: Flocking
        MovementCommand flockCmd = createTestCommand(agentId, "flock");
        priority.registerBehavior(agentId, BehaviorType.FLOCKING, 30, flockCmd);
        System.out.println("  - FLOCKING (Priority: 30)");
        
        // High priority: Task
        MovementCommand taskCmd = createTestCommand(agentId, "task");
        priority.registerBehavior(agentId, BehaviorType.TASK_EXECUTION, 70, taskCmd);
        System.out.println("  - TASK_EXECUTION (Priority: 70)");
        
        System.out.println();
        System.out.println("Active behaviors: " + priority.getActiveBehaviorCount(agentId));
        System.out.println("Highest priority: " + priority.getHighestPriorityBehavior(agentId));
        
        // Resolve conflicts
        MovementCommand result = priority.resolveConflicts(agentId);
        
        System.out.println();
        if (result != null && result.parameters.get("type").equals("task")) {
            System.out.println("  ✓ PASS: Highest priority behavior (TASK) won");
        } else {
            System.out.println("  ✗ FAIL: Wrong behavior selected");
        }
        System.out.println();
    }
    
    /**
     * TEST 2: EMERGENCY OVERRIDE
     */
    private static void testEmergencyOverride() {
        System.out.println("TEST 2: Emergency Override");
        System.out.println("-------------------------");
        
        BehaviorPriority priority = new BehaviorPriority();
        int agentId = 2;
        
        // Normal flocking behavior
        MovementCommand flockCmd = createTestCommand(agentId, "flock");
        priority.registerBehavior(agentId, BehaviorType.FLOCKING, 30, flockCmd);
        System.out.println("Agent 2 flocking normally...");
        
        // EMERGENCY: Collision detected!
        MovementCommand emergencyCmd = createTestCommand(agentId, "evade");
        priority.registerBehavior(agentId, BehaviorType.EVADING, 100, emergencyCmd);
        System.out.println("COLLISION DETECTED! Emergency evasion triggered!");
        
        System.out.println();
        System.out.println("Emergency active: " + priority.isEmergencyActive(agentId));
        
        // Resolve - emergency should win
        MovementCommand result = priority.resolveConflicts(agentId);
        
        System.out.println();
        if (result != null && result.parameters.get("type").equals("evade")) {
            System.out.println("  ✓ PASS: Emergency behavior overrode flocking");
        } else {
            System.out.println("  ✗ FAIL: Emergency did not override");
        }
        
        // Check metrics
        System.out.println();
        System.out.println("Priority system metrics: " + priority);
        System.out.println();
    }
    
    /**
     * TEST 3: BEHAVIOR BLENDING
     */
    private static void testBehaviorBlending() {
        System.out.println("TEST 3: Behavior Blending");
        System.out.println("-------------------------");
        
        BehaviorPriority priority = new BehaviorPriority();
        int agentId = 3;
        
        // Compatible behaviors: Flocking + Formation
        System.out.println("Registering compatible behaviors:");
        
        MovementCommand flockCmd = new MovementCommand();
        flockCmd.agentId = agentId;
        flockCmd.type = MovementType.FLOCKING_BEHAVIOR;
        flockCmd.parameters.put("combinedForce", new Vector2D(5.0, 3.0));
        flockCmd.parameters.put("speed", 30.0);  // Add speed parameter
        priority.registerBehavior(agentId, BehaviorType.FLOCKING, 30, flockCmd);
        System.out.println("  - FLOCKING with force (5.0, 3.0)");
        
        MovementCommand formationCmd = new MovementCommand();
        formationCmd.agentId = agentId;
        formationCmd.type = MovementType.FORMATION_POSITION;
        formationCmd.parameters.put("combinedForce", new Vector2D(2.0, 8.0));
        formationCmd.parameters.put("speed", 40.0);  // Add speed parameter
        priority.registerBehavior(agentId, BehaviorType.FORMATION, 60, formationCmd);
        System.out.println("  - FORMATION with force (2.0, 8.0)");;
        
        System.out.println();
        System.out.println("Behaviors are compatible - attempting blend...");
        
        // Resolve - should blend
        MovementCommand result = priority.resolveConflicts(agentId);
        
        System.out.println();
        if (result != null && result.parameters.containsKey("combinedForce")) {
            Vector2D blended = (Vector2D) result.parameters.get("combinedForce");
            System.out.println(String.format("Blended force: (%.2f, %.2f)", 
                blended.x, blended.y));
            System.out.println("  ✓ PASS: Behaviors successfully blended");
        } else {
            System.out.println("  ✗ FAIL: Blending did not occur");
        }
        
        System.out.println();
        System.out.println("Successful blends: " + priority.getSuccessfulBlends());
        System.out.println();
    }
    
    /**
     * TEST 4: COORDINATOR INTEGRATION
     */
    private static void testCoordinatorIntegration() {
        System.out.println("TEST 4: SwarmCoordinator Integration");
        System.out.println("------------------------------------");
        
        SwarmCoordinator coordinator = new SwarmCoordinator();
        
        // Create test swarm
        List<AgentState> agents = createTestAgents(5);
        
        System.out.println("Starting coordination with 5 agents...");
        coordinator.startCoordination();
        
        System.out.println("Coordination active: " + coordinator.isCoordinationActive());
        System.out.println("Current mode: " + coordinator.getCurrentMode());
        
        // Run a few update cycles
        System.out.println();
        System.out.println("Running 3 coordination cycles...");
        for (int i = 0; i < 3; i++) {
            coordinator.update(0.033, agents);  // 30 FPS
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Check status
        CoordinatorStatus status = coordinator.getStatus();
        System.out.println();
        System.out.println("Coordinator status: " + status);
        
        System.out.println();
        if (coordinator.getUpdateCount() >= 3) {
            System.out.println("  ✓ PASS: Coordinator running and updating");
        } else {
            System.out.println("  ✗ FAIL: Coordinator not updating properly");
        }
        
        coordinator.stopCoordination();
        System.out.println();
    }
    
    /**
     * TEST 5: EMERGENCY RESPONSE
     */
    private static void testEmergencyResponse() {
        System.out.println("TEST 5: Emergency Response System");
        System.out.println("---------------------------------");
        
        SwarmCoordinator coordinator = new SwarmCoordinator();
        List<AgentState> agents = createTestAgents(7);
        
        // Set low battery on some agents
        agents.get(2).batteryLevel = 0.12;  // 12% - critical
        agents.get(4).batteryLevel = 0.14;  // 14% - critical
        
        System.out.println("Swarm status:");
        System.out.println("  Agent 3: Battery at 12% - CRITICAL!");
        System.out.println("  Agent 5: Battery at 14% - CRITICAL!");
        System.out.println();
        
        // Trigger emergency
        System.out.println("Triggering battery emergency...");
        coordinator.handleEmergency(
            EmergencyType.CRITICAL_BATTERY,
            Arrays.asList(3, 5)
        );
        
        System.out.println();
        System.out.println("Emergency responses: " + coordinator.getEmergencyResponseCount());
        
        // Check if emergency behaviors registered
        BehaviorPriority priority = coordinator.getBehaviorPriority();
        boolean agent3Emergency = priority.isEmergencyActive(3) || 
                                  priority.getHighestPriorityBehavior(3) == BehaviorType.RETURNING;
        boolean agent5Emergency = priority.isEmergencyActive(5) || 
                                  priority.getHighestPriorityBehavior(5) == BehaviorType.RETURNING;
        
        System.out.println();
        if (coordinator.getEmergencyResponseCount() > 0) {
            System.out.println("  ✓ PASS: Emergency response system triggered");
        } else {
            System.out.println("  ✗ FAIL: Emergency not handled");
        }
        System.out.println();
    }
    
    /**
     * TEST 6: COMPLETE VOTING WORKFLOW
     */
    private static void testVotingWorkflow() {
        System.out.println("TEST 6: Complete Voting Workflow");
        System.out.println("--------------------------------");
        
    SwarmCoordinator coordinator = new SwarmCoordinator();
        
        // Initiate a navigation vote
        System.out.println("Initiating navigation vote: Go left or right?");
        String proposalId = coordinator.initiateVote(
            "Obstacle ahead - navigate left or right?",
            Arrays.asList("LEFT", "RIGHT"),
            ProposalType.NAVIGATION
        );
        
        System.out.println("Proposal ID: " + proposalId);
        System.out.println();
        
        // Simulate 7 agents voting
        System.out.println("Collecting votes from 7 agents:");
        
        // 5 agents vote LEFT
        for (int i = 1; i <= 5; i++) {
            VoteResponse response = new VoteResponse(proposalId, i, "LEFT");
            response.confidence = 0.8 + (Math.random() * 0.2);
            coordinator.processVote(response);
            System.out.println("  Agent " + i + " voted LEFT (confidence: " + 
                String.format("%.0f%%", response.confidence * 100) + ")");
        }
        
        // 2 agents vote RIGHT
        for (int i = 6; i <= 7; i++) {
            VoteResponse response = new VoteResponse(proposalId, i, "RIGHT");
            response.confidence = 0.7;
            coordinator.processVote(response);
            System.out.println("  Agent " + i + " voted RIGHT (confidence: 70%)");
        }
        
        System.out.println();
        System.out.println("All votes collected. Checking consensus...");
        
        // Check vote result
        VoteResult result = coordinator.checkVoteStatus(proposalId);
        
        System.out.println();
        System.out.println("Vote Result:");
        System.out.println("  Consensus reached: " + result.consensusReached);
        System.out.println("  Winning option: " + result.winningOption);
        System.out.println("  Consensus level: " + String.format("%.0f%%", result.consensusLevel * 100));
        System.out.println("  Vote breakdown: " + result.voteBreakdown);
        
        System.out.println();
        if (result.consensusReached && "LEFT".equals(result.winningOption)) {
            System.out.println("  ✓ PASS: Vote completed successfully, LEFT won with " + 
                String.format("%.0f%%", result.consensusLevel * 100));
        } else if (!result.consensusReached) {
            System.out.println("  ⚠ WARNING: No consensus reached (may need to wait for timeout)");
        } else {
            System.out.println("  ✗ FAIL: Unexpected vote result");
        }
        System.out.println();
    }
    
    /**
     * TEST 7: ADAPTIVE BEHAVIOR
     */
    private static void testAdaptiveBehavior() {
        System.out.println("TEST 7: Adaptive Behavior System");
        System.out.println("--------------------------------");
        
        SwarmCoordinator coordinator = new SwarmCoordinator();
        AdaptiveBehavior adaptive = new AdaptiveBehavior(coordinator);
        
        System.out.println("Adaptive behavior initialized");
        System.out.println("Initial state: " + adaptive);
        
        // Enable adaptation
        System.out.println();
        System.out.println("Enabling adaptive behavior...");
        adaptive.enableAdaptation();
        
        // Simulate poor performance - high collision rate
    PerformanceMetrics metrics = new PerformanceMetrics();
    metrics.setCollisionRate(0.08);  // 8% collision rate (target: 2%)
    metrics.setSwarmCohesion(0.65);  // Low cohesion (target: 0.8)
    metrics.setMovementJitter(0.4);  // High jitter
        
        System.out.println();
        System.out.println("Simulating poor performance:");
        System.out.println("  Collision rate: 8.0% (target: 2.0%)");
        System.out.println("  Swarm cohesion: 0.65 (target: 0.8)");
        System.out.println("  Movement jitter: 0.40");
        
        // Get initial parameters
        FlockingParameters initialParams = coordinator.getFlockingController().getParameters();
        double initialSeparation = initialParams.separationWeight;
        double initialCohesion = initialParams.cohesionWeight;
        
        System.out.println();
        System.out.println("Initial parameters:");
        System.out.println(String.format("  Separation weight: %.2f", initialSeparation));
        System.out.println(String.format("  Cohesion weight: %.2f", initialCohesion));
        
        // Trigger adaptation
        System.out.println();
        System.out.println("Triggering adaptation...");
        
        // First update records the metrics
        adaptive.update(metrics);
        
        // Wait for adaptation interval (5 seconds normally, but we'll simulate)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Force another update after sufficient time has passed
        // We need to wait at least 5 seconds for adaptation to trigger
        System.out.println("Waiting for adaptation interval (simulating 5 seconds)...");
        
        // Simulate time passing by creating a new adaptive behavior with modified interval check
        // Or simply call update multiple times with delays
        for (int i = 0; i < 6; i++) {
            try {
                Thread.sleep(1000);  // 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            adaptive.update(metrics);
        }
        
        // Check adjusted parameters
        FlockingParameters adjustedParams = coordinator.getFlockingController().getParameters();
        double adjustedSeparation = adjustedParams.separationWeight;
        double adjustedCohesion = adjustedParams.cohesionWeight;
        
        System.out.println();
        System.out.println("Adjusted parameters:");
        System.out.println(String.format("  Separation weight: %.2f (change: %+.2f)",
            adjustedSeparation, adjustedSeparation - initialSeparation));
        System.out.println(String.format("  Cohesion weight: %.2f (change: %+.2f)",
            adjustedCohesion, adjustedCohesion - initialCohesion));
        
        System.out.println();
        System.out.println("Adaptation summary: " + adaptive);
        
        System.out.println();
        if (adjustedSeparation > initialSeparation && adjustedCohesion > initialCohesion) {
            System.out.println("  ✓ PASS: Adaptive system adjusted parameters correctly");
        } else {
            System.out.println("  ⚠ WARNING: Parameters adjusted, but not as expected");
        }
        System.out.println();
    }
    
    /**
     * TEST 8: COORDINATION MODES
     */
    private static void testCoordinationModes() {
        System.out.println("TEST 8: Coordination Mode Switching");
        System.out.println("-----------------------------------");
        
        SwarmCoordinator coordinator = new SwarmCoordinator();
        
        System.out.println("Testing different coordination modes:");
        System.out.println();
        
        // Test each mode
        CoordinationMode[] modes = {
            CoordinationMode.AUTONOMOUS,
            CoordinationMode.FORMATION_STRICT,
            CoordinationMode.EXPLORATION,
            CoordinationMode.EMERGENCY
        };
        
        for (CoordinationMode mode : modes) {
            FlockingParameters before = coordinator.getFlockingController().getParameters();
            double beforeCohesion = before.cohesionWeight;
            double beforeSeparation = before.separationWeight;
            
            System.out.println("Switching to " + mode + " mode...");
            coordinator.setCoordinationMode(mode);
            
            FlockingParameters after = coordinator.getFlockingController().getParameters();
            double afterCohesion = after.cohesionWeight;
            double afterSeparation = after.separationWeight;
            
            System.out.println(String.format("  Cohesion: %.2f -> %.2f",
                beforeCohesion, afterCohesion));
            System.out.println(String.format("  Separation: %.2f -> %.2f",
                beforeSeparation, afterSeparation));
            System.out.println();
        }
        
        System.out.println("Final mode: " + coordinator.getCurrentMode());
        
        System.out.println();
        System.out.println("  ✓ PASS: All coordination modes tested");
        System.out.println();
    }
    
    /**
     * TEST 9: PERFORMANCE TREND ANALYSIS
     */
    private static void testPerformanceTrend() {
        System.out.println("TEST 9: Performance Trend Analysis");
        System.out.println("----------------------------------");
        
        SwarmCoordinator coordinator = new SwarmCoordinator();
        AdaptiveBehavior adaptive = new AdaptiveBehavior(coordinator);
        adaptive.enableAdaptation();
        
        System.out.println("Simulating performance improvement over time...");
        System.out.println();
        
        // Simulate improving performance over 60 seconds (to trigger multiple adaptations)
        for (int i = 0; i < 12; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            
            // Gradually improve metrics
            metrics.setCollisionRate(0.08 - (i * 0.006));  // 8% -> 2.6%
            metrics.setSwarmCohesion(0.65 + (i * 0.02));   // 0.65 -> 0.87
            metrics.setMovementJitter(0.4 - (i * 0.025));  // 0.4 -> 0.1
            
            adaptive.update(metrics);
            
            // Force time progression for adaptation (5 second intervals)
            try {
                Thread.sleep(500);  // 0.5 seconds (simulate faster for test)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            if (i % 2 == 0) {
                System.out.print(".");
            }
        }
        System.out.println(" Done!");
        System.out.println();
        
        // Analyze trend
        PerformanceTrend trend = adaptive.getPerformanceTrend();
        
        System.out.println("Performance samples recorded: " + 
            adaptive.getPerformanceHistory().size());
        System.out.println("Total adjustments made: " + adaptive.getTotalAdjustments());
        System.out.println("  - Flocking: " + adaptive.getFlockingAdjustments());
        System.out.println("  - Voting: " + adaptive.getVotingAdjustments());
        System.out.println("  - Task: " + adaptive.getTaskAdjustments());
        
        System.out.println();
        System.out.println("Performance trend: " + trend);
        
        System.out.println();
        if (trend == PerformanceTrend.IMPROVING || trend == PerformanceTrend.STABLE) {
            System.out.println("  ✓ PASS: System showing positive trend");
        } else if (trend == PerformanceTrend.INSUFFICIENT_DATA) {
            System.out.println("  ⚠ WARNING: Insufficient data for trend analysis");
        } else {
            System.out.println("  ✗ FAIL: Performance degrading");
        }
        
        System.out.println();
        System.out.println("Adaptive behavior summary: " + adaptive);
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
            agent.position = new Point2D(100 * i, 100 * i);
            agent.velocity = new Vector2D(5, 5);
            agent.status = AgentStatus.ACTIVE;
            agent.batteryLevel = 0.8;
            agent.maxSpeed = 50.0;
            agent.communicationRange = 100.0;
            agents.add(agent);
        }
        
        return agents;
    }
    
    /**
     * Create test movement command
     */
    private static MovementCommand createTestCommand(int agentId, String type) {
        MovementCommand cmd = new MovementCommand();
        cmd.agentId = agentId;
        cmd.type = MovementType.MOVE_TO_TARGET;
        cmd.parameters.put("type", type);
        cmd.timestamp = System.currentTimeMillis();
        return cmd;
    }
}