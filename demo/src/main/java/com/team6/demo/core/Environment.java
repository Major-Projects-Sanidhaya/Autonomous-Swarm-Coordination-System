package com.team6.demo.core;

import com.team6.demo.obstacles.ObstacleManager;
import com.team6.swarm.core.*;
import java.util.*;

/**
 * Environment - Simulation world container
 * Manages world boundaries, obstacles, drones, and simulation time
 */
public class Environment {
    // World dimensions
    private final double width;
    private final double length;
    private final double maxAltitude;

    // Components
    private final ObstacleManager obstacleManager;
    private final AgentManager agentManager;
    private final EventBus eventBus;

    // Drone tracking with 3D positions
    private final Map<Integer, Position> dronePositions;

    // Simulation time
    private double simulationTime;

    public Environment(double width, double length, double maxAltitude) {
        this.width = width;
        this.length = length;
        this.maxAltitude = maxAltitude;

        this.obstacleManager = new ObstacleManager();
        this.eventBus = new EventBus();
        this.agentManager = new AgentManager(eventBus);
        this.dronePositions = new HashMap<>();
        this.simulationTime = 0.0;
    }

    /**
     * Spawn a drone at specified 3D position
     * Returns the agent ID
     */
    public int spawnDrone(Position position) {
        if (!isValidPosition(position)) {
            throw new IllegalArgumentException("Invalid spawn position: " + position);
        }

        // Create agent using existing AgentManager
        Agent agent = agentManager.createAgent(position.toPoint2D());
        int agentId = agent.getState().agentId;

        // Track 3D position
        dronePositions.put(agentId, position.copy());

        return agentId;
    }

    /**
     * Update drone 3D position
     */
    public void updateDronePosition(int droneId, Position newPosition) {
        if (isValidPosition(newPosition)) {
            dronePositions.put(droneId, newPosition.copy());

            // Update 2D position in agent state
            Agent agent = agentManager.getAgent(droneId);
            if (agent != null) {
                agent.getState().position = newPosition.toPoint2D();
            }
        }
    }

    /**
     * Get drone's current 3D position
     */
    public Position getDronePosition(int droneId) {
        return dronePositions.getOrDefault(droneId, new Position(0, 0, 0)).copy();
    }

    /**
     * Check if position is valid (within bounds and no obstacles)
     */
    public boolean isValidPosition(Position position) {
        // Check bounds
        if (position.x < 0 || position.x > width ||
            position.y < 0 || position.y > length ||
            position.z < 0 || position.z > maxAltitude) {
            return false;
        }

        // Check obstacles
        return !obstacleManager.checkCollision(position);
    }

    /**
     * Check if path between two positions is clear
     */
    public boolean isPathClear(Position start, Position end) {
        return obstacleManager.getPathClear(start, end);
    }

    /**
     * Update simulation by one time step
     */
    public void update(double deltaTime) {
        // Update simulation time
        simulationTime += deltaTime;

        // Update dynamic obstacles
        obstacleManager.updateDynamicObstacles(deltaTime);

        // Update all agents through AgentManager
        agentManager.updateAll(deltaTime);
    }

    /**
     * Get all active drone IDs
     */
    public List<Integer> getAllDroneIds() {
        return new ArrayList<>(dronePositions.keySet());
    }

    /**
     * Get all drone positions (for visualization)
     */
    public Map<Integer, Position> getAllDronePositions() {
        // Return copy to prevent external modification
        Map<Integer, Position> copy = new HashMap<>();
        for (Map.Entry<Integer, Position> entry : dronePositions.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }

    /**
     * Get agent by ID
     */
    public Agent getAgent(int droneId) {
        return agentManager.getAgent(droneId);
    }

    /**
     * Get all agent states
     */
    public List<AgentState> getAllAgentStates() {
        return agentManager.getAllAgentStates();
    }

    /**
     * Remove drone from environment
     */
    public void removeDrone(int droneId) {
        dronePositions.remove(droneId);
        agentManager.removeAgent(droneId);
    }

    // Getters
    public double getWidth() { return width; }
    public double getLength() { return length; }
    public double getMaxAltitude() { return maxAltitude; }
    public double getSimulationTime() { return simulationTime; }
    public ObstacleManager getObstacleManager() { return obstacleManager; }
    public AgentManager getAgentManager() { return agentManager; }
    public EventBus getEventBus() { return eventBus; }
}
