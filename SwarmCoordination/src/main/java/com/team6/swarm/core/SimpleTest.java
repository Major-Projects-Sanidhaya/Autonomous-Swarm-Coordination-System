// src/main/java/com/team6/swarm/core/SimpleTest.java
package com.team6.swarm.core;

public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("Starting simple agent test...");
        
        // Create agent manager
        AgentManager manager = new AgentManager();
        
        // Create 3 test agents
        Agent agent1 = manager.createAgent(new Point2D(100, 100));
        Agent agent2 = manager.createAgent(new Point2D(200, 150));
        Agent agent3 = manager.createAgent(new Point2D(150, 200));
        
        // Give them some simple movement commands
        MovementCommand cmd1 = new MovementCommand();
        cmd1.agentId = 1;
        cmd1.type = MovementType.MOVE_TO_TARGET;
        cmd1.parameters = Map.of("target", new Point2D(300, 300));
        
        agent1.addMovementCommand(cmd1);
        
        // Start simulation
        manager.startSimulation();
        
        // Let it run for 10 seconds
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Stop simulation
        manager.stopSimulation();
        System.out.println("Test completed!");
    }
}