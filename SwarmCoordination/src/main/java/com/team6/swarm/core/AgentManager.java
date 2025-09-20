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