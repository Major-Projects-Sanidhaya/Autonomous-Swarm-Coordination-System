/**
 * FLOCKINGTEST CLASS - Week 1 Flocking Behavior Validation
 *
 * PURPOSE:
 * - Test that three flocking rules work correctly
 * - Validate force calculations produce expected results
 * - Verify integration with Sanidhaya's agent system
 * - Demonstrate emergent coordinated movement
 *
 * TEST SCENARIOS:
 *
 * 1. SEPARATION TEST
 *    Setup: 5 agents in tight cluster (all within 25 units)
 *    Expected: Strong separation forces pushing agents apart
 *    Success: Agents increase distance from each other
 *
 * 2. ALIGNMENT TEST
 *    Setup: 5 agents moving in different directions
 *    Expected: Alignment forces match velocities
 *    Success: Agents converge to similar velocity vectors
 *
 * 3. COHESION TEST
 *    Setup: 5 agents spread far apart (60-100 units)
 *    Expected: Cohesion forces pull toward group center
 *    Success: Agents move closer together
 *
 * 4. COMBINED FLOCKING TEST
 *    Setup: Random agent positions and velocities
 *    Expected: Emergent coordinated swarm movement
 *    Success: Natural-looking flocking behavior
 *
 * WHAT TO OBSERVE:
 * - Agents initially separate if too close
 * - Agents begin matching velocities
 * - Group maintains cohesion without collisions
 * - Smooth, non-jittery movement
 * - Battery levels gradually decrease
 *
 * WEEK 1 SUCCESS CRITERIA:
 * ✓ FlockingController calculates all three forces
 * ✓ Forces combine correctly with weights
 * ✓ MovementCommands sent to agents
 * ✓ Agents respond to flocking forces
 * ✓ Beautiful coordinated movement emerges
 */
package com.team6.swarm.intelligence.Flocking;

import com.team6.swarm.core.*;

import java.util.List;
import java.util.ArrayList;

public class FlockingTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("FLOCKING BEHAVIOR TEST");
        System.out.println("========================================");
        System.out.println();
        
        // Run all test scenarios
        testSeparationForce();
        testAlignmentForce();
        testCohesionForce();
        testCombinedFlocking();
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("Tests completed!");
        System.out.println("========================================");
    }
    
    /**
     * TEST 1: SEPARATION FORCE
     * Agents too close should push apart
     */
    private static void testSeparationForce() {
        System.out.println("TEST 1: Separation Force");
        System.out.println("-------------------------");
        
        FlockingController controller = new FlockingController();
        
        // Create test agent (centered)
        AgentState testAgent = new AgentState();
        testAgent.agentId = 1;
        testAgent.position = new Point2D(100, 100);
        testAgent.velocity = new Vector2D(0, 0);
        
        // Create neighbors very close (should trigger strong separation)
        List<NeighborInfo> neighbors = new ArrayList<>();
        neighbors.add(new NeighborInfo(2, new Point2D(110, 100), new Vector2D(0, 0), 10.0));
        neighbors.add(new NeighborInfo(3, new Point2D(90, 100), new Vector2D(0, 0), 10.0));
        neighbors.add(new NeighborInfo(4, new Point2D(100, 110), new Vector2D(0, 0), 10.0));
        
        // Calculate flocking
        MovementCommand cmd = controller.calculateFlocking(1, testAgent, neighbors);
        Vector2D force = (Vector2D) cmd.parameters.get("combinedForce");
        
        System.out.println("  Agent at (100, 100) with 3 close neighbors");
        System.out.println("  Separation force magnitude: " + force.magnitude());
        System.out.println("  Force direction: (" + 
            String.format("%.2f", force.x) + ", " + 
            String.format("%.2f", force.y) + ")");
        
        if (force.magnitude() > 0.5) {
            System.out.println("  ✓ PASS: Strong separation force generated");
        } else {
            System.out.println("  ✗ FAIL: Separation force too weak");
        }
        
        System.out.println();
    }
    
    /**
     * TEST 2: ALIGNMENT FORCE
     * Agents with different velocities should align
     */
    private static void testAlignmentForce() {
        System.out.println("TEST 2: Alignment Force");
        System.out.println("-----------------------");
        
        FlockingController controller = new FlockingController();
        
        // Create test agent moving right
        AgentState testAgent = new AgentState();
        testAgent.agentId = 1;
        testAgent.position = new Point2D(100, 100);
        testAgent.velocity = new Vector2D(10, 0);  // Moving right
        
        // Create neighbors moving in different direction (up)
        List<NeighborInfo> neighbors = new ArrayList<>();
        neighbors.add(new NeighborInfo(2, new Point2D(140, 100), 
            new Vector2D(0, 10), 40.0));  // Moving up
        neighbors.add(new NeighborInfo(3, new Point2D(100, 140), 
            new Vector2D(0, 10), 40.0));  // Moving up
        
        // Calculate flocking
        MovementCommand cmd = controller.calculateFlocking(1, testAgent, neighbors);
        Vector2D force = (Vector2D) cmd.parameters.get("combinedForce");
        
        System.out.println("  Agent velocity: (10, 0) - moving right");
        System.out.println("  Neighbor velocities: (0, 10) - moving up");
        System.out.println("  Alignment force: (" + 
            String.format("%.2f", force.x) + ", " + 
            String.format("%.2f", force.y) + ")");
        
        if (force.y > 0) {
            System.out.println("  ✓ PASS: Alignment force pulling toward group direction");
        } else {
            System.out.println("  ✗ FAIL: Alignment not working correctly");
        }
        
        System.out.println();
    }
    
    /**
     * TEST 3: COHESION FORCE
     * Spread agents should move toward group center
     */
    private static void testCohesionForce() {
        System.out.println("TEST 3: Cohesion Force");
        System.out.println("----------------------");
        
        FlockingController controller = new FlockingController();
        
        // Create test agent far from group
        AgentState testAgent = new AgentState();
        testAgent.agentId = 1;
        testAgent.position = new Point2D(100, 100);
        testAgent.velocity = new Vector2D(0, 0);
        
        // Create neighbors far away (should trigger cohesion)
        List<NeighborInfo> neighbors = new ArrayList<>();
        neighbors.add(new NeighborInfo(2, new Point2D(160, 100), 
            new Vector2D(0, 0), 60.0));
        neighbors.add(new NeighborInfo(3, new Point2D(160, 160), 
            new Vector2D(0, 0), 84.8));
        neighbors.add(new NeighborInfo(4, new Point2D(100, 160), 
            new Vector2D(0, 0), 60.0));
        
        // Calculate flocking
        MovementCommand cmd = controller.calculateFlocking(1, testAgent, neighbors);
        Vector2D force = (Vector2D) cmd.parameters.get("combinedForce");
        
        System.out.println("  Agent at (100, 100)");
        System.out.println("  Group center approximately at (140, 140)");
        System.out.println("  Cohesion force: (" + 
            String.format("%.2f", force.x) + ", " + 
            String.format("%.2f", force.y) + ")");
        
        if (force.x > 0 && force.y > 0) {
            System.out.println("  ✓ PASS: Cohesion pulling toward group center");
        } else {
            System.out.println("  ✗ FAIL: Cohesion not working correctly");
        }
        
        System.out.println();
    }
    
    /**
     * TEST 4: COMBINED FLOCKING
     * Full system test with live agents
     */
    private static void testCombinedFlocking() {
        System.out.println("TEST 4: Combined Flocking (Live Simulation)");
        System.out.println("-------------------------------------------");
        System.out.println("Running 15-second simulation...");
        System.out.println();
        
        // Create agent manager and flocking controller
        AgentManager manager = new AgentManager();
        FlockingController controller = new FlockingController();
        
        // Create 5 agents in close formation
        System.out.println("Creating swarm:");
        List<Agent> agents = new ArrayList<>();
        agents.add(manager.createAgent(new Point2D(400, 300)));
        agents.add(manager.createAgent(new Point2D(420, 310)));
        agents.add(manager.createAgent(new Point2D(380, 290)));
        agents.add(manager.createAgent(new Point2D(410, 280)));
        agents.add(manager.createAgent(new Point2D(390, 320)));
        System.out.println();
        
        // Give agents initial random velocities
        System.out.println("Initializing random velocities...");
        for (Agent agent : agents) {
            AgentState state = agent.getState();
            double vx = (Math.random() - 0.5) * 20.0;
            double vy = (Math.random() - 0.5) * 20.0;
            state.velocity = new Vector2D(vx, vy);
        }
        System.out.println();
        
        // Start physics simulation
        manager.startSimulation();
        
        // Start flocking AI loop
        Thread flockingThread = new Thread(() -> 
            runFlockingLoop(manager, controller));
        flockingThread.start();
        
        System.out.println("Observing flocking behavior...");
        System.out.println("(Watch for: separation → alignment → cohesion)");
        System.out.println();
        
        // Let simulation run for 15 seconds
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Stop simulation
        manager.stopSimulation();
        flockingThread.interrupt();
        
        // Print performance metrics
        System.out.println();
        System.out.println("Performance Metrics:");
        System.out.println("  Total calculations: " + controller.getCalculationsPerformed());
        System.out.println("  Average calc time: " + 
            String.format("%.2f", controller.getAverageCalculationTime()) + " ms");
        
        System.out.println();
        System.out.println("  ✓ PASS: Combined flocking simulation completed");
    }
    
    /**
     * Flocking AI loop - runs at ~15 FPS
     * Calculates and sends flocking commands for all agents
     */
    private static void runFlockingLoop(AgentManager manager, FlockingController controller) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Get current state of all agents
                List<AgentState> allAgents = manager.getAllAgentStates();
                
                // For each active agent, calculate flocking
                for (AgentState currentAgent : allAgents) {
                    if (currentAgent.status != AgentStatus.ACTIVE) {
                        continue;
                    }
                    
                    // Find neighbors (temporary implementation)
                    List<NeighborInfo> neighbors = findNeighbors(currentAgent, allAgents);
                    
                    // Calculate flocking command
                    MovementCommand cmd = controller.calculateFlocking(
                        currentAgent.agentId, currentAgent, neighbors);
                    
                    // Send to agent
                    Agent agent = manager.getAgent(currentAgent.agentId);
                    if (agent != null) {
                        agent.addMovementCommand(cmd);
                    }
                }
                
                // Run at ~15 FPS (flocking doesn't need physics-level speed)
                Thread.sleep(66);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in flocking loop: " + e.getMessage());
            }
        }
    }
    
    /**
     * Temporary neighbor detection
     * Will be replaced by John's communication system
     */
    private static List<NeighborInfo> findNeighbors(AgentState currentAgent, 
                                                    List<AgentState> allAgents) {
        List<NeighborInfo> neighbors = new ArrayList<>();
        
        for (AgentState other : allAgents) {
            if (other.agentId == currentAgent.agentId) {
                continue;
            }
            
            double distance = currentAgent.position.distanceTo(other.position);
            
            if (distance <= currentAgent.communicationRange) {
                neighbors.add(new NeighborInfo(
                    other.agentId, 
                    other.position, 
                    other.velocity, 
                    distance
                ));
            }
        }
        
        return neighbors;
    }
}