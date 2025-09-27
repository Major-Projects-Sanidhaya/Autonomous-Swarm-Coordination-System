/**
 * AGENT CLASS - Individual Swarm Entity
 *
 * PURPOSE:
 * - Represents single autonomous agent in swarm system
 * - Handles movement, physics, battery management, and state updates
 * - Core building block for multi-agent coordination
 *
 * MAIN COMPONENTS:
 * 1. AgentState - Current agent status and properties
 * 2. MovementCommand Queue - Thread-safe command processing
 * 3. PhysicsEngine - Movement and collision handling
 * 4. Update Loop - 30-60 FPS simulation cycle
 *
 * CORE FUNCTIONS:
 * 1. update(deltaTime) - Main simulation loop called each frame
 * 2. addMovementCommand() - Queue new movement instructions
 * 3. getState() - Access current agent state
 * 4. processCommands() - Execute queued movement commands
 * 5. updatePhysics() - Position/velocity integration
 * 6. updateBattery() - Power consumption simulation
 *
 * UPDATE CYCLE LOGIC (called 30-60 times/second):
 * 1. Process pending movement commands from queue
 * 2. Update physics (position based on velocity)
 * 3. Update battery level based on movement
 * 4. Update timestamp and publish state changes
 *
 * MOVEMENT EXECUTION:
 * - Commands processed in FIFO order from thread-safe queue
 * - Switch statement handles different MovementTypes
 * - Physics engine enforces speed limits and boundaries
 * - Collision detection with world boundaries (bounce effect)
 *
 * BATTERY SIMULATION:
 * - Consumption rate proportional to speed
 * - Low battery (< 20%) triggers BATTERY_LOW status
 * - Formula: consumption = (speed/maxSpeed) * 0.001 * deltaTime
 *
 * EXPECTED OUTPUTS:
 * - Console: "Agent 1 at (150.5, 200.3) battery: 0.85"
 * - State updates: position changes based on velocity
 * - Status changes: ACTIVE -> BATTERY_LOW when power < 20%
 * - Boundary handling: velocity reversal when hitting walls
 *
 * THREAD SAFETY:
 * - Command queue is ConcurrentLinkedQueue (thread-safe)
 * - State access should be synchronized externally if needed
 * - Update method should only be called from simulation thread
 */
// src/main/java/com/team6/swarm/core/Agent.java
package com.team6.swarm.core;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class Agent {
    private AgentState state;
    private Queue<MovementCommand> commandQueue;
    private PhysicsEngine physics;
    
    public Agent(int id, Point2D initialPosition) {
        this.state = new AgentState();
        this.state.agentId = id;
        this.state.agentName = "Agent_" + id;
        this.state.position = initialPosition;
        this.state.velocity = new Vector2D(0, 0);
        this.state.status = AgentStatus.ACTIVE;
        this.state.batteryLevel = 1.0;
        this.state.maxSpeed = 50.0;
        this.state.communicationRange = 100.0;
        this.state.lastUpdateTime = System.currentTimeMillis();
        
        this.commandQueue = new ConcurrentLinkedQueue<>();
        this.physics = new PhysicsEngine();
    }
    
    // Your main update loop - called 30-60 times per second
    public void update(double deltaTime) {
        // Process any pending movement commands
        processCommands();
        
        // Update physics (position, velocity)
        updatePhysics(deltaTime);
        
        // Update battery (decreases over time)
        updateBattery(deltaTime);
        
        // Update timestamp
        state.lastUpdateTime = System.currentTimeMillis();
        
        // Notify other components of state change
        publishStateUpdate();
    }
    
    public void addMovementCommand(MovementCommand command) {
        commandQueue.offer(command);
    }
    
    public AgentState getState() {
        return state; // You might want to return a copy for thread safety
    }
    
    private void processCommands() {
        while (!commandQueue.isEmpty()) {
            MovementCommand cmd = commandQueue.poll();
            executeMovementCommand(cmd);
        }
    }
    
    private void executeMovementCommand(MovementCommand cmd) {
        // This is where Lauren's decisions get executed
        // For now, implement basic movement
        switch (cmd.type) {
            case MOVE_TO_TARGET:
                Point2D target = (Point2D) cmd.parameters.get("target");
                moveToward(target);
                break;
            case FLOCKING_BEHAVIOR:
                Vector2D force = (Vector2D) cmd.parameters.get("combinedForce");
                applyForce(force);
                break;
            // Add more cases as needed
        }
    }
    
    private void moveToward(Point2D target) {
        // Simple movement toward target
        double dx = target.x - state.position.x;
        double dy = target.y - state.position.y;
        Vector2D direction = new Vector2D(dx, dy).normalize();
        state.velocity = direction.multiply(state.maxSpeed * 0.8); // 80% of max speed
    }
    
    private void applyForce(Vector2D force) {
        // Apply Lauren's flocking forces
        state.velocity = state.velocity.add(force);
        
        // Limit to max speed
        if (state.velocity.magnitude() > state.maxSpeed) {
            state.velocity = state.velocity.normalize().multiply(state.maxSpeed);
        }
    }
    
    private void updatePhysics(double deltaTime) {
        // Update position based on velocity
        state.position.x += state.velocity.x * deltaTime;
        state.position.y += state.velocity.y * deltaTime;
        
        // Check boundaries (bounce off walls for now)
        checkBoundaries();
    }
    
    private void checkBoundaries() {
        // Simple boundary checking - bounce off walls
        if (state.position.x < 0 || state.position.x > 800) {
            state.velocity.x *= -1;
            state.position.x = Math.max(0, Math.min(800, state.position.x));
        }
        if (state.position.y < 0 || state.position.y > 600) {
            state.velocity.y *= -1;
            state.position.y = Math.max(0, Math.min(600, state.position.y));
        }
    }
    
    private void updateBattery(double deltaTime) {
        // Battery depletes based on speed and time
        double consumption = (state.velocity.magnitude() / state.maxSpeed) * 0.001 * deltaTime;
        state.batteryLevel = Math.max(0, state.batteryLevel - consumption);
        
        if (state.batteryLevel < 0.2) {
            state.status = AgentStatus.BATTERY_LOW;
        }
    }
    
    private void publishStateUpdate() {
        // This will notify John's communication system
        // For now, just print (you'll implement event system later)
        System.out.println("Agent " + state.agentId + " at " + state.position + 
                          " battery: " + String.format("%.2f", state.batteryLevel));
    }
}