/**
 * PHYSICSENGINE CLASS - Movement and Collision System
 *
 * PURPOSE:
 * - Handles basic physics simulation for agent movement
 * - Enforces world boundaries and collision detection
 * - Provides foundation for realistic agent behavior
 *
 * WORLD CONFIGURATION:
 * - WORLD_WIDTH: 800.0 units (X-axis boundary)
 * - WORLD_HEIGHT: 600.0 units (Y-axis boundary)
 * - Coordinate system: (0,0) at top-left, (800,600) at bottom-right
 *
 * CORE FUNCTIONS:
 * 1. updatePosition(state, deltaTime) - Integrates velocity to update position
 * 2. checkCollision(agent1, agent2) - Detects agent-to-agent collisions
 *
 * PHYSICS LOGIC:
 * Position Integration:
 * - newX = currentX + velocityX * deltaTime
 * - newY = currentY + velocityY * deltaTime
 * - Frame-rate independent using deltaTime
 *
 * Boundary Handling:
 * - If agent hits X boundary: velocity.x *= -1 (reverse X direction)
 * - If agent hits Y boundary: velocity.y *= -1 (reverse Y direction)
 * - Position clamped to valid range: [0, WORLD_WIDTH] and [0, WORLD_HEIGHT]
 *
 * Collision Detection:
 * - Simple distance-based collision (< 10.0 units)
 * - Uses Euclidean distance between agent positions
 * - Returns boolean: true if collision detected
 *
 * EXPECTED BEHAVIORS:
 * - Agent at (795, 300) moving right -> bounces off right wall, reverses velocity
 * - Agent at (400, -5) -> position clamped to (400, 0), velocity.y reversed
 * - Two agents 8 units apart -> checkCollision() returns true
 * - Two agents 15 units apart -> checkCollision() returns false
 *
 * PERFORMANCE:
 * - O(1) position updates per agent
 * - O(1) collision checks per agent pair
 * - Optimized for real-time simulation at 30-60 FPS
 *
 * FUTURE EXPANSIONS:
 * - Add acceleration/deceleration curves
 * - Implement obstacle avoidance
 * - Add momentum and friction effects
 * - Support for non-rectangular world shapes
 */
package com.team6.swarm.core;

public class PhysicsEngine {
    
    // Basic physics constants
    public static final double WORLD_WIDTH = 800.0;
    public static final double WORLD_HEIGHT = 600.0;
    
    public PhysicsEngine() {
        // Initialize physics engine
    }
    
    // Basic physics update - you'll expand this later
    public void updatePosition(AgentState state, double deltaTime) {
        // Update position based on velocity
        state.position.x += state.velocity.x * deltaTime;
        state.position.y += state.velocity.y * deltaTime;
        
        // Basic boundary checking
        if (state.position.x < 0 || state.position.x > WORLD_WIDTH) {
            state.velocity.x *= -1;  // bounce off wall
            state.position.x = Math.max(0, Math.min(WORLD_WIDTH, state.position.x));
        }
        
        if (state.position.y < 0 || state.position.y > WORLD_HEIGHT) {
            state.velocity.y *= -1;  // bounce off wall
            state.position.y = Math.max(0, Math.min(WORLD_HEIGHT, state.position.y));
        }
    }
    
    public boolean checkCollision(AgentState agent1, AgentState agent2) {
        double distance = agent1.position.distanceTo(agent2.position);
        return distance < 10.0;  // collision if agents within 10 units
    }
}