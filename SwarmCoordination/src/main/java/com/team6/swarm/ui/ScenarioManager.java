package com.team6.swarm.ui;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.Point2D;
import java.util.*;

/**
 * Week 9-10: Pre-built demo scenarios
 * Purpose: Quick impressive demos
 * Author: Anthony (UI Team)
 */
public class ScenarioManager {
    
    public enum ScenarioType {
        BASIC_FLOCKING,
        OBSTACLE_NAVIGATION,
        FORMATION_FLYING,
        SEARCH_MISSION,
        AGENT_FAILURE
    }
    
    /**
     * Load a scenario
     */
    public Scenario loadScenario(ScenarioType type) {
        return switch (type) {
            case BASIC_FLOCKING -> createBasicFlockingScenario();
            case OBSTACLE_NAVIGATION -> createObstacleNavigationScenario();
            case FORMATION_FLYING -> createFormationFlyingScenario();
            case SEARCH_MISSION -> createSearchMissionScenario();
            case AGENT_FAILURE -> createAgentFailureScenario();
        };
    }
    
    private Scenario createBasicFlockingScenario() {
        Scenario scenario = new Scenario();
        scenario.name = "Basic Flocking";
        scenario.description = "5 agents demonstrating flocking behavior";
        
        // Create 5 agents in random positions
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            Point2D pos = new Point2D(
                random.nextDouble() * 400 - 200,
                random.nextDouble() * 400 - 200
            );
            scenario.agentPositions.add(pos);
        }
        
        scenario.parameters.put("flockingEnabled", true);
        scenario.parameters.put("obstaclesEnabled", false);
        
        return scenario;
    }
    
    private Scenario createObstacleNavigationScenario() {
        Scenario scenario = new Scenario();
        scenario.name = "Obstacle Navigation";
        scenario.description = "8 agents navigating around obstacles with voting";
        
        // Create 8 agents
        for (int i = 0; i < 8; i++) {
            Point2D pos = new Point2D(-300 + i * 40, 0);
            scenario.agentPositions.add(pos);
        }
        
        // Add obstacle
        scenario.obstacles.add(new Point2D(0, 0));
        scenario.waypoints.add(new Point2D(300, 0));
        
        scenario.parameters.put("votingEnabled", true);
        
        return scenario;
    }
    
    private Scenario createFormationFlyingScenario() {
        Scenario scenario = new Scenario();
        scenario.name = "Formation Flying";
        scenario.description = "6 agents maintaining formation";
        
        // Create line formation
        for (int i = 0; i < 6; i++) {
            Point2D pos = new Point2D(i * 30 - 75, 0);
            scenario.agentPositions.add(pos);
        }
        
        scenario.parameters.put("formationType", "WEDGE");
        scenario.parameters.put("formationEnabled", true);
        
        return scenario;
    }
    
    private Scenario createSearchMissionScenario() {
        Scenario scenario = new Scenario();
        scenario.name = "Search Mission";
        scenario.description = "10 agents conducting grid search";
        
        // Create 10 agents
        for (int i = 0; i < 10; i++) {
            Point2D pos = new Point2D((i % 5) * 40 - 100, (i / 5) * 40 - 40);
            scenario.agentPositions.add(pos);
        }
        
        // Add search waypoints
        for (int x = -200; x <= 200; x += 100) {
            for (int y = -200; y <= 200; y += 100) {
                scenario.waypoints.add(new Point2D(x, y));
            }
        }
        
        scenario.parameters.put("missionType", "SEARCH");
        
        return scenario;
    }
    
    private Scenario createAgentFailureScenario() {
        Scenario scenario = new Scenario();
        scenario.name = "Agent Failure Recovery";
        scenario.description = "7 agents with 2 failing mid-mission";
        
        // Create 7 agents
        for (int i = 0; i < 7; i++) {
            Point2D pos = new Point2D(i * 40 - 120, 0);
            scenario.agentPositions.add(pos);
        }
        
        scenario.waypoints.add(new Point2D(0, 200));
        scenario.parameters.put("failAgents", Arrays.asList(2, 4));
        scenario.parameters.put("failTime", 5000); // 5 seconds
        
        return scenario;
    }
    
    /**
     * Scenario data class
     */
    public static class Scenario {
        public String name;
        public String description;
        public List<Point2D> agentPositions = new ArrayList<>();
        public List<Point2D> waypoints = new ArrayList<>();
        public List<Point2D> obstacles = new ArrayList<>();
        public Map<String, Object> parameters = new HashMap<>();
    }
}
