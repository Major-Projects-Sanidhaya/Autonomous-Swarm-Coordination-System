/**
 * FLOCKINGTEST CLASS - Component Integration Test
 *
 * PURPOSE:
 * - Tests FlockingController integration with Sanidhya's existing system
 * - Demonstrates how intelligence connects to the core Agent system
 * - Validates that flocking commands are properly executed by agents
 *
 * TEST SCENARIO:
 * 1. Creates AgentManager and spawns 5 agents in close proximity
 * 2. Initializes FlockingController for behavioral calculations
 * 3. Each frame: analyzes neighbors and generates flocking commands
 * 4. Sends MovementCommands to agents via their command queues
 * 5. Observes emergent flocking behavior over time
 *
 * INTEGRATION POINTS:
 * - Uses Sanidhya's AgentManager for agent lifecycle management
 * - Creates NeighborInfo from AgentState data (John's future interface)
 * - Generates MovementCommand objects that Agent.executeMovementCommand() handles
 * - Leverages existing Vector2D, Point2D, and physics simulation
 *
 * EXPECTED BEHAVIOR:
 * - Agents start clustered together (high separation forces)
 * - Separation forces push agents apart initially
 * - As agents spread out, alignment and cohesion take effect
 * - Emergent coordinated movement patterns develop
 * - Console shows flocking calculations and agent responses
 *
 * KEY VALIDATION POINTS:
 * - MovementCommand.type = FLOCKING_BEHAVIOR is handled correctly
 * - Vector2D forces are applied properly by Agent.applyForce()
 * - No compilation errors with existing class interfaces
 */
// src/main/java/com/team6/swarm/intelligence/FlockingTest.java
package com.team6.swarm.intelligence;

import com.team6.swarm.core.*;
import java.util.List;
import java.util.ArrayList;

public class FlockingTest {
    public static void main(String[] args) {
        System.out.println("Starting Lauren's Flocking Integration Test...");
        
        // Use Sanidhya's existing system
        AgentManager manager = new AgentManager();
        FlockingController flockingController = new FlockingController();
        
        // Create 5 agents in close formation to trigger flocking
        List<Agent> agents = new ArrayList<>();
        agents.add(manager.createAgent(new Point2D(400, 300))); // Center
        agents.add(manager.createAgent(new Point2D(420, 310))); // Close neighbors
        agents.add(manager.createAgent(new Point2D(380, 290)));
        agents.add(manager.createAgent(new Point2D(410, 280)));
        agents.add(manager.createAgent(new Point2D(390, 320)));
        
        // Give agents initial random velocities
        for (Agent agent : agents) {
            AgentState state = agent.getState();
            // Small random velocities to trigger interactions
            double vx = (Math.random() - 0.5) * 20.0;
            double vy = (Math.random() - 0.5) * 20.0;
            state.velocity = new Vector2D(vx, vy);
        }
        
        // Start the simulation
        manager.startSimulation();
        
        // Run Lauren's flocking AI loop
        new Thread(() -> runFlockingLoop(manager, flockingController)).start();
        
        // Let it run for 15 seconds to observe behavior
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Stop everything
        manager.stopSimulation();
        System.out.println("Flocking test completed!");
    }
    
    /**
     * Lauren's main AI loop - calculates and sends flocking commands
     */
    private static void runFlockingLoop(AgentManager manager, FlockingController controller) {
        while (true) {
            try {
                // Get current state of all agents
                List<AgentState> allAgents = manager.getAllAgentStates();
                
                // For each agent, calculate flocking behavior
                for (AgentState currentAgent : allAgents) {
                    if (currentAgent.status != AgentStatus.ACTIVE) {
                        continue; // Skip inactive agents
                    }
                    
                    // Create neighbor list (simulate John's communication system)
                    List<NeighborInfo> neighbors = findNeighbors(currentAgent, allAgents);
                    
                    // Calculate flocking command using Lauren's controller
                    MovementCommand flockingCmd = controller.calculateFlocking(
                        currentAgent.agentId, currentAgent, neighbors);
                    
                    // Send command to the agent
                    Agent agent = manager.getAgent(currentAgent.agentId);
                    if (agent != null) {
                        agent.addMovementCommand(flockingCmd);
                    }
                }
                
                // Run at ~15 FPS (flocking calculations don't need to be as fast as physics)
                Thread.sleep(66);
                
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("Error in flocking loop: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Simulate John's communication system - find neighbors within range
     * This will be replaced by actual communication system later
     */
    private static List<NeighborInfo> findNeighbors(AgentState currentAgent, List<AgentState> allAgents) {
        List<NeighborInfo> neighbors = new ArrayList<>();
        
        for (AgentState other : allAgents) {
            if (other.agentId == currentAgent.agentId) {
                continue; // Don't include self
            }
            
            double distance = currentAgent.position.distanceTo(other.position);
            
            // Only include neighbors within communication range
            if (distance <= currentAgent.communicationRange) {
                NeighborInfo neighbor = new NeighborInfo(
                    other.agentId, 
                    other.position, 
                    other.velocity, 
                    distance
                );
                neighbors.add(neighbor);
            }
        }
        
        return neighbors;
    }
}

/**
 * NeighborInfo - temporary implementation until John creates the real one
 * This matches the interface expected by FlockingController
 */
class NeighborInfo {
    public Point2D position;
    public Vector2D velocity; 
    public double distance;
    public int agentId;
    
    public NeighborInfo(int agentId, Point2D position, Vector2D velocity, double distance) {
        this.agentId = agentId;
        this.position = position;
        this.velocity = velocity;
        this.distance = distance;
    }
}