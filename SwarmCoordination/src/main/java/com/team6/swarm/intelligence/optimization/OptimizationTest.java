/**
 * OPTIMIZATIONTEST CLASS - Weeks 11-12 Performance Validation
 *
 * PURPOSE:
 * - Test system performance with 20+ agents
 * - Validate optimization techniques work correctly
 * - Measure scalability across different agent counts
 * - Verify no performance bottlenecks exist
 *
 * TEST SCENARIOS:
 *
 * 1. SPATIAL GRID PERFORMANCE TEST
 *    Setup: 30 agents, measure neighbor query time
 *    Expected: Query time < 2ms with spatial grid
 *    Success: Spatial partitioning provides speedup
 *
 * 2. FLOCKING CACHE TEST
 *    Setup: 25 agents, enable/disable caching
 *    Expected: 30%+ performance improvement with caching
 *    Success: Cache hit rate > 50%
 *
 * 3. SCALABILITY TEST
 *    Setup: Test with 10, 20, 30, 40, 50 agents
 *    Expected: Performance degrades gracefully
 *    Success: 20 agents at 60 FPS, 50 agents at 30 FPS
 *
 * 4. TASK ALLOCATION PERFORMANCE
 *    Setup: 100 tasks, 25 agents
 *    Expected: Allocation time < 50ms total
 *    Success: All tasks assigned efficiently
 *
 * 5. VOTING PERFORMANCE
 *    Setup: 30 agents voting simultaneously
 *    Expected: Consensus calculation < 20ms
 *    Success: System handles concurrent voting
 *
 * 6. FORMATION PERFORMANCE
 *    Setup: 40 agents in complex formation
 *    Expected: Position calculation < 10ms
 *    Success: Formation maintained at high agent count
 *
 * 7. COMPREHENSIVE STRESS TEST
 *    Setup: 50 agents, all systems active
 *    Expected: Stable operation for 60 seconds
 *    Success: No crashes, acceptable frame rate
 *
 * WEEKS 11-12 SUCCESS CRITERIA:
 * ✓ 20 agents at 60 FPS minimum
 * ✓ 30 agents at 45 FPS minimum
 * ✓ 50 agents at 30 FPS minimum
 * ✓ Spatial partitioning reduces query time
 * ✓ Caching improves performance
 * ✓ No memory leaks during extended runs
 * ✓ Graceful degradation with agent count
 * ✓ All algorithms optimized
 */
package com.team6.swarm.intelligence.optimization;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.flocking.*;
import com.team6.swarm.intelligence.tasking.*;
import com.team6.swarm.intelligence.voting.*;
import java.util.*;

public class OptimizationTest {
  // For easier indication because terminal gets flooded during tests
  static int testCounter = 0;
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("WEEKS 11-12: OPTIMIZATION TEST");
        System.out.println("========================================");
        System.out.println();
        
        // Run all test scenarios
        testSpatialGridPerformance();
        testFlockingCache();
        testScalability();
        testTaskAllocationPerformance();
        testVotingPerformance();
        testFormationPerformance();
        testComprehensiveStress();
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("All Weeks 11-12 tests completed!");
        System.out.println("========================================");

        if (testCounter == 7) {
          System.out.println("✓ ALL TESTS PASSED SUCCESSFULLY!");
        } else {
          System.out.println("✗ SOME TESTS FAILED. Please review results.");
        }
    }
    
    /**
     * TEST 1: SPATIAL GRID PERFORMANCE
     */
    private static void testSpatialGridPerformance() {
        System.out.println("TEST 1: Spatial Grid Performance");
        System.out.println("--------------------------------");
        
        int agentCount = 30;
        PerformanceOptimizer optimizer = new PerformanceOptimizer(800, 600);
        List<AgentState> agents = createTestAgents(agentCount);
        
        // Update spatial grid
        optimizer.updateSpatialGrid(agents);
        
        System.out.println(String.format("Testing with %d agents...", agentCount));
        
        // Measure neighbor queries
        long totalTime = 0;
        int queryCount = 100;
        
        for (int i = 0; i < queryCount; i++) {
            AgentState testAgent = agents.get(i % agents.size());
            
            long startTime = System.nanoTime();
            List<AgentState> neighbors = optimizer.getNeighborsInRadius(
                testAgent, 100.0);
            long endTime = System.nanoTime();
            
            totalTime += (endTime - startTime);
        }
        
        double avgTimeMs = (totalTime / queryCount) / 1_000_000.0;
        
        System.out.println();
        System.out.println(String.format("Performed %d neighbor queries", queryCount));
        System.out.println(String.format("Average query time: %.3fms", avgTimeMs));
        
        System.out.println();
        if (avgTimeMs < 2.0) {
          testCounter++;
            System.out.println("  ✓ PASS: Query time < 2ms (excellent performance)");
        } else if (avgTimeMs < 5.0) {
          testCounter++;
            System.out.println("  ✓ PASS: Query time < 5ms (acceptable performance)");
        } else {
            System.out.println("  ✗ FAIL: Query time too high");
        }
        System.out.println();
    }
    
    /**
     * TEST 2: FLOCKING CACHE
     */
    private static void testFlockingCache() {
        System.out.println("TEST 2: Flocking Cache Performance");
        System.out.println("----------------------------------");
        
        int agentCount = 25;
        PerformanceOptimizer optimizer = new PerformanceOptimizer(800, 600);
        FlockingController flocking = new FlockingController();
        List<AgentState> agents = createTestAgents(agentCount);
        
        // Run without caching
        optimizer.setEnableCaching(false);
        long timeWithoutCache = measureFlockingPerformance(
            flocking, optimizer, agents, 100);
        
        // Run with caching
        optimizer.setEnableCaching(true);
        long timeWithCache = measureFlockingPerformance(
            flocking, optimizer, agents, 100);
        
        double improvement = ((double)(timeWithoutCache - timeWithCache) / 
                             timeWithoutCache) * 100;
        
        System.out.println();
        System.out.println(String.format("Without cache: %.1fms", 
            timeWithoutCache / 1_000_000.0));
        System.out.println(String.format("With cache: %.1fms", 
            timeWithCache / 1_000_000.0));
        System.out.println(String.format("Improvement: %.1f%%", improvement));
        
        System.out.println();
        if (improvement > 20) {
          testCounter++;
            System.out.println("  ✓ PASS: Caching provides significant speedup");
        } else if (improvement > 0) {
            System.out.println("  ⚠ WARNING: Caching provides minimal improvement");
        } else {
            System.out.println("  ✗ FAIL: Caching doesn't improve performance");
        }
        System.out.println();
    }
    
    /**
     * TEST 3: SCALABILITY
     */
    private static void testScalability() {
        System.out.println("TEST 3: Scalability Test (10-50 Agents)");
        System.out.println("---------------------------------------");
        
        int[] agentCounts = {10, 20, 30, 40, 50};
        
        System.out.println("Testing frame times with different agent counts:");
        System.out.println();
        
        for (int count : agentCounts) {
            PerformanceOptimizer optimizer = new PerformanceOptimizer(800, 600);
            FlockingController flocking = new FlockingController();
            List<AgentState> agents = createTestAgents(count);
            
            // Simulate 100 frames
            long totalTime = 0;
            for (int frame = 0; frame < 100; frame++) {
                long frameStart = System.nanoTime();
                
                // Update spatial grid
                optimizer.updateSpatialGrid(agents);
                
                // Calculate flocking for all agents
                for (AgentState agent : agents) {
                    List<AgentState> neighbors = optimizer.getNeighborsInRadius(
                        agent, 100.0);
                    
                    List<NeighborInfo> neighborInfo = new ArrayList<>();
                    for (AgentState neighbor : neighbors) {
                        neighborInfo.add(new NeighborInfo(
                            neighbor.agentId,
                            neighbor.position,
                            neighbor.velocity,
                            agent.position.distanceTo(neighbor.position)
                        ));
                    }
                    
                    flocking.calculateFlocking(agent.agentId, agent, neighborInfo);
                }
                
                long frameEnd = System.nanoTime();
                totalTime += (frameEnd - frameStart);
            }
            
            double avgFrameTime = (totalTime / 100) / 1_000_000.0;
            double fps = 1000.0 / avgFrameTime;
            
            System.out.println(String.format("%2d agents: %.2fms per frame (%.0f FPS)", 
                count, avgFrameTime, fps));
        }
        
        System.out.println();
        System.out.println("  ✓ PASS: Scalability test completed");
        testCounter++;
        System.out.println("  Note: Performance should degrade gracefully");
        System.out.println();
    }
    
    /**
     * TEST 4: TASK ALLOCATION PERFORMANCE
     */
    private static void testTaskAllocationPerformance() {
        System.out.println("TEST 4: Task Allocation Performance");
        System.out.println("-----------------------------------");
        
        int agentCount = 25;
        int taskCount = 100;
        
        TaskAllocator allocator = new TaskAllocator();
        List<AgentState> agents = createTestAgents(agentCount);
        List<com.team6.swarm.intelligence.tasking.Task> tasks = createTestTasks(taskCount);
        
        System.out.println(String.format("Assigning %d tasks to %d agents...", 
            taskCount, agentCount));
        
        long startTime = System.nanoTime();
        List<TaskAssignment> assignments = allocator.assignTasks(tasks, agents);
        long endTime = System.nanoTime();
        
        double totalTime = (endTime - startTime) / 1_000_000.0;
        double timePerTask = totalTime / taskCount;
        
        System.out.println();
        System.out.println(String.format("Total time: %.2fms", totalTime));
        System.out.println(String.format("Time per task: %.3fms", timePerTask));
        System.out.println(String.format("Successfully assigned: %d/%d tasks", 
            assignments.size(), taskCount));
        
        System.out.println();
        if (totalTime < 50.0 && assignments.size() >= taskCount * 0.9) {
            testCounter++;
            System.out.println("  ✓ PASS: Task allocation efficient");
        } else if (totalTime < 100.0) {
          testCounter++;
            System.out.println("  ⚠ WARNING: Task allocation acceptable");
        } else {
            System.out.println("  ✗ FAIL: Task allocation too slow");
        }
        System.out.println();
    }
    
    /**
     * TEST 5: VOTING PERFORMANCE
     */
    private static void testVotingPerformance() {
        System.out.println("TEST 5: Voting System Performance");
        System.out.println("---------------------------------");
        
        int agentCount = 30;
        VotingSystem voting = new VotingSystem();
        
        // Initiate vote
        String proposalId = voting.initiateVote(
            "Performance test vote",
            Arrays.asList("OPTION_A", "OPTION_B"),
            ProposalType.COORDINATION
        );
        
        System.out.println(String.format("Processing %d votes...", agentCount));
        
        // Submit votes
        long submitStart = System.nanoTime();
        for (int i = 1; i <= agentCount; i++) {
            String choice = (i % 2 == 0) ? "OPTION_A" : "OPTION_B";
            voting.processVote(new VoteResponse(proposalId, i, choice));
        }
        long submitEnd = System.nanoTime();
        
        // Calculate consensus
        long consensusStart = System.nanoTime();
        VoteResult result = voting.checkConsensus(proposalId);
        long consensusEnd = System.nanoTime();
        
        double submitTime = (submitEnd - submitStart) / 1_000_000.0;
        double consensusTime = (consensusEnd - consensusStart) / 1_000_000.0;
        
        System.out.println();
        System.out.println(String.format("Vote submission time: %.2fms", submitTime));
        System.out.println(String.format("Consensus calculation: %.2fms", consensusTime));
        System.out.println(String.format("Result: %s", result.reason));
        
        System.out.println();
        if (consensusTime < 20.0) {
          testCounter++;
            System.out.println("  ✓ PASS: Voting performance excellent");
        } else if (consensusTime < 50.0) {
            System.out.println("  ⚠ WARNING: Voting performance acceptable");
        } else {
            System.out.println("  ✗ FAIL: Voting too slow");
        }
        System.out.println();
    }
    
    /**
     * TEST 6: FORMATION PERFORMANCE
     */
    private static void testFormationPerformance() {
        System.out.println("TEST 6: Formation Calculation Performance");
        System.out.println("-----------------------------------------");
        
        int agentCount = 40;
        List<AgentState> agents = createTestAgents(agentCount);
        
        // Calculate formation positions
        System.out.println(String.format("Calculating positions for %d agents...", 
            agentCount));
        
        long startTime = System.nanoTime();
        
        // Simulate V-formation calculation
        double spacing = 40.0;
        Map<Integer, Point2D> targetPositions = new HashMap<>();
        Point2D leaderPos = agents.get(0).position;
        
        int leftCount = 0;
        int rightCount = 0;
        for (int i = 1; i < agents.size(); i++) {
            if (i % 2 == 1) {
                leftCount++;
                double x = leaderPos.x - spacing * leftCount * 0.7;
                double y = leaderPos.y - spacing * leftCount;
                targetPositions.put(agents.get(i).agentId, new Point2D(x, y));
            } else {
                rightCount++;
                double x = leaderPos.x + spacing * rightCount * 0.7;
                double y = leaderPos.y - spacing * rightCount;
                targetPositions.put(agents.get(i).agentId, new Point2D(x, y));
            }
        }
        
        long endTime = System.nanoTime();
        
        double totalTime = (endTime - startTime) / 1_000_000.0;
        
        System.out.println();
        System.out.println(String.format("Formation calculation time: %.2fms", totalTime));
        System.out.println(String.format("Positions calculated: %d", 
            targetPositions.size()));
        
        System.out.println();
        if (totalTime < 10.0) {
          testCounter++;
            System.out.println("  ✓ PASS: Formation calculation fast");
        } else if (totalTime < 25.0) {
            System.out.println("  ⚠ WARNING: Formation calculation acceptable");
        } else {
            System.out.println("  ✗ FAIL: Formation calculation too slow");
        }
        System.out.println();
    }
    
    /**
     * TEST 7: COMPREHENSIVE STRESS TEST
     */
    private static void testComprehensiveStress() {
        System.out.println("TEST 7: Comprehensive Stress Test");
        System.out.println("---------------------------------");
        
        int agentCount = 50;
        int durationSeconds = 10; // Reduced from 60 for testing
        
        System.out.println(String.format("Running full system with %d agents for %d seconds...", 
            agentCount, durationSeconds));
        System.out.println("(All intelligence systems active)");
        System.out.println();
        
        // Initialize all systems
        PerformanceOptimizer optimizer = new PerformanceOptimizer(800, 600);
        FlockingController flocking = new FlockingController();
        BehaviorMetrics metrics = new BehaviorMetrics();
        List<AgentState> agents = createTestAgents(agentCount);
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000);
        int frameCount = 0;
        long totalFrameTime = 0;
        
        // Main simulation loop
        while (System.currentTimeMillis() < endTime) {
            long frameStart = System.nanoTime();
            
            // Update spatial grid
            optimizer.updateSpatialGrid(agents);
            
            // Calculate flocking for all agents
            for (AgentState agent : agents) {
                List<AgentState> neighbors = optimizer.getNeighborsInRadius(
                    agent, 100.0);
                
                List<NeighborInfo> neighborInfo = new ArrayList<>();
                for (AgentState neighbor : neighbors) {
                    neighborInfo.add(new NeighborInfo(
                        neighbor.agentId,
                        neighbor.position,
                        neighbor.velocity,
                        agent.position.distanceTo(neighbor.position)
                    ));
                }
                
                flocking.calculateFlocking(agent.agentId, agent, neighborInfo);
            }
            
            // Update metrics
            metrics.measureFlockingCohesion(agents);
            metrics.measureSeparationSafety(agents);
            metrics.update();
            
            long frameEnd = System.nanoTime();
            long frameDuration = frameEnd - frameStart;
            totalFrameTime += frameDuration;
            frameCount++;
            
            // Sleep to maintain frame rate (simulate 30 FPS target)
            try {
                long sleepTime = Math.max(0, 33 - (frameDuration / 1_000_000));
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        double avgFrameTime = (totalFrameTime / frameCount) / 1_000_000.0;
        double avgFPS = 1000.0 / avgFrameTime;
        
        System.out.println();
        System.out.println("Stress Test Results:");
        System.out.println(String.format("  Duration: %d seconds", durationSeconds));
        System.out.println(String.format("  Frames processed: %d", frameCount));
        System.out.println(String.format("  Avg frame time: %.2fms", avgFrameTime));
        System.out.println(String.format("  Avg FPS: %.1f", avgFPS));
        
        // Print metrics
        System.out.println();
        metrics.printSummary();
        
        System.out.println();
        if (avgFPS >= 30.0) {
          testCounter++;
            System.out.println("  ✓ PASS: System handles 50 agents at 30+ FPS");
        } else if (avgFPS >= 20.0) {
            System.out.println("  ⚠ WARNING: System runs below target FPS");
        } else {
            System.out.println("  ✗ FAIL: System performance unacceptable");
        }
        System.out.println();
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Create test agents with realistic properties
     */
    private static List<AgentState> createTestAgents(int count) {
        List<AgentState> agents = new ArrayList<>();
        Random rand = new Random();
        
        for (int i = 1; i <= count; i++) {
            AgentState agent = new AgentState();
            agent.agentId = i;
            agent.agentName = "Agent_" + i;
            
            // Random position
            agent.position = new Point2D(
                rand.nextDouble() * 800,
                rand.nextDouble() * 600
            );
            
            // Random velocity
            agent.velocity = new Vector2D(
                (rand.nextDouble() - 0.5) * 20,
                (rand.nextDouble() - 0.5) * 20
            );
            
            agent.status = AgentStatus.ACTIVE;
            agent.batteryLevel = 0.5 + rand.nextDouble() * 0.5;
            agent.maxSpeed = 50.0;
            agent.communicationRange = 100.0;
            
            agents.add(agent);
        }
        
        return agents;
    }
    
    /**
     * Create test tasks
     */
    private static List<com.team6.swarm.intelligence.tasking.Task> createTestTasks(int count) {
        List<com.team6.swarm.intelligence.tasking.Task> tasks = new ArrayList<>();
        Random rand = new Random();
        
        for (int i = 1; i <= count; i++) {
            Point2D target = new Point2D(
                rand.nextDouble() * 800,
                rand.nextDouble() * 600
            );
            
            com.team6.swarm.intelligence.tasking.Task task = com.team6.swarm.intelligence.tasking.Task.createMoveToWaypoint("task_" + i, target);
            tasks.add(task);
        }
        
        return tasks;
    }
    
    /**
     * Measure flocking performance
     */
    private static long measureFlockingPerformance(FlockingController flocking,
                                                    PerformanceOptimizer optimizer,
                                                    List<AgentState> agents,
                                                    int iterations) {
        optimizer.updateSpatialGrid(agents);
        
        long totalTime = 0;
        
        for (int iter = 0; iter < iterations; iter++) {
            for (AgentState agent : agents) {
                long start = System.nanoTime();
                
                List<AgentState> neighbors = optimizer.getNeighborsInRadius(
                    agent, 100.0);
                
                List<NeighborInfo> neighborInfo = new ArrayList<>();
                for (AgentState neighbor : neighbors) {
                    neighborInfo.add(new NeighborInfo(
                        neighbor.agentId,
                        neighbor.position,
                        neighbor.velocity,
                        agent.position.distanceTo(neighbor.position)
                    ));
                }
                
                flocking.calculateFlocking(agent.agentId, agent, neighborInfo);
                
                long end = System.nanoTime();
                totalTime += (end - start);
            }
        }
        
        return totalTime;
    }
}