/**
 * AGENTMANAGER CLASS - Swarm Coordination Controller
 *
 * PURPOSE:
 * - Manages lifecycle of multiple agents in the swarm
 * - Controls simulation loop and timing (30 FPS)
 * - Provides centralized access to agent data for other components
 * - Coordinates agent updates and system-wide operations
 *
 * CORE FUNCTIONS:
 * 1. createAgent(position) - Spawns new agent at specified location
 * 2. removeAgent(id) - Safely removes agent from simulation
 * 3. startSimulation() - Begins main simulation loop in separate thread
 * 4. stopSimulation() - Gracefully halts simulation
 * 5. getAllAgentStates() - Returns current state of all agents
 * 6. getVisualizationUpdate() - Packages data for UI components
 *
 * SIMULATION LOOP LOGIC:
 * - Runs in separate thread at ~30 FPS (33ms sleep)
 * - Calculates deltaTime for frame-rate independent physics
 * - Updates all agents sequentially each frame
 * - Handles thread interruption gracefully
 *
 * AGENT LIFECYCLE:
 * 1. createAgent() -> assigns unique ID, stores in ConcurrentHashMap
 * 2. Agent participates in simulation loop updates
 * 3. removeAgent() -> safely removes from active agent pool
 *
 * THREAD SAFETY:
 * - Uses ConcurrentHashMap for thread-safe agent storage
 * - Simulation runs in dedicated thread
 * - Agent creation/removal safe during simulation
 *
 * EXPECTED OUTPUTS:
 * - Console: "Created Agent_1 at (100.0, 200.0)"
 * - Console: "Removed Agent 3"
 * - VisualizationUpdate with current agent states and metrics
 * - System metrics: totalAgents, activeAgents count
 *
 * PERFORMANCE:
 * - Target: 30 FPS simulation rate
 * - Scales with agent count (O(n) per frame)
 * - Memory: ConcurrentHashMap overhead + agent states
 *
 * INTEGRATION POINTS:
 * - UI Components: getVisualizationUpdate() for rendering
 * - AI Systems: access agents for command injection
 * - Metrics: SystemMetrics generation for monitoring
 */
// src/main/java/com/team6/swarm/core/AgentManager.java
package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AgentManager {
    private Map<Integer, Agent> agents;
    private int nextAgentId;
    private boolean simulationRunning;
    
    public AgentManager() {
        this.agents = new ConcurrentHashMap<>();
        this.nextAgentId = 1;
        this.simulationRunning = false;
    }
    
    public Agent createAgent(Point2D position) {
        Agent agent = new Agent(nextAgentId++, position);
        agents.put(agent.getState().agentId, agent);
        
        System.out.println("Created " + agent.getState().agentName + 
                          " at " + position);
        return agent;
    }
    
    public void removeAgent(int agentId) {
        Agent removed = agents.remove(agentId);
        if (removed != null) {
            System.out.println("Removed Agent " + agentId);
        }
    }
    
    public Agent getAgent(int agentId) {
        return agents.get(agentId);
    }
    
    public List<AgentState> getAllAgentStates() {
        List<AgentState> states = new ArrayList<>();
        for (Agent agent : agents.values()) {
            states.add(agent.getState());
        }
        return states;
    }
    
    public void startSimulation() {
        simulationRunning = true;
        // Start the main simulation loop
        new Thread(this::simulationLoop).start();
    }
    
    public void stopSimulation() {
        simulationRunning = false;
    }
    
    private void simulationLoop() {
        long lastTime = System.currentTimeMillis();
        
        while (simulationRunning) {
            long currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastTime) / 1000.0; // Convert to seconds
            
            // Update all agents
            for (Agent agent : agents.values()) {
                agent.update(deltaTime);
            }
            
            lastTime = currentTime;
            
            // Sleep to maintain ~30 FPS
            try {
                Thread.sleep(33); // ~30 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    // For Anthony's UI - get data for visualization
    public VisualizationUpdate getVisualizationUpdate() {
        VisualizationUpdate update = new VisualizationUpdate();
        update.allAgents = getAllAgentStates();
        update.timestamp = System.currentTimeMillis();
        
        // Add system metrics
        update.systemMetrics = new SystemMetrics();
        update.systemMetrics.totalAgents = agents.size();
        update.systemMetrics.activeAgents = (int) agents.values().stream()
            .filter(a -> a.getState().status == AgentStatus.ACTIVE)
            .count();
        
        return update;
    }
}