/**
 * PHYSICSENGINE CLASS - Movement and Collision System
 *
 * PURPOSE:
 * - Handles realistic physics simulation for agent movement
 * - Enforces world boundaries and collision detection
 * - Provides foundation for smooth, realistic agent behavior
 * - Manages force application and velocity limiting
 *
 * WORLD CONFIGURATION:
 * - WORLD_WIDTH: 800.0 units (X-axis boundary)
 * - WORLD_HEIGHT: 600.0 units (Y-axis boundary)
 * - COLLISION_DISTANCE: 10.0 units (agents closer than this collide)
 * - Coordinate system: (0,0) at top-left, (800,600) at bottom-right
 *
 * CORE FUNCTIONS:
 * 1. applyForce(state, force, deltaTime) - Apply force to velocity
 * 2. updatePosition(state, deltaTime) - Integrate velocity to position
 * 3. limitVelocity(state, maxSpeed) - Cap velocity at max speed
 * 4. checkBoundaries(state, bounceMode) - Handle world boundaries
 * 5. checkCollision(agent1, agent2) - Detect collisions
 * 6. separationForce(state, otherStates) - Calculate repulsion from nearby agents
 *
 * PHYSICS LOGIC:
 * Force Application (F = ma, assuming mass = 1):
 * - newVelocity = velocity + force * deltaTime
 *
 * Position Integration:
 * - newPosition = position + velocity * deltaTime
 * - Frame-rate independent using deltaTime
 *
 * Boundary Handling Modes:
 * - BOUNCE: Reverse velocity component when hitting wall
 * - WRAP: Teleport to opposite side (pac-man style)
 * - CLAMP: Stop at boundary, zero velocity
 *
 * Collision Detection:
 * - Distance-based: distance < COLLISION_DISTANCE
 * - Uses Euclidean distance between positions
 *
 * EXPECTED BEHAVIORS:
 * - Agent at (795, 300) moving right -> bounces off right wall
 * - Force Vector2D(10, 0) applied -> velocity increases rightward
 * - Velocity exceeds maxSpeed -> normalized and scaled to maxSpeed
 * - Two agents 8 units apart -> checkCollision() returns true
 *
 * PERFORMANCE:
 * - O(1) position updates per agent
 * - O(1) collision checks per agent pair
 * - O(n) separation force calculation (n = nearby agents)
 * - Optimized for real-time simulation at 30-60 FPS
 */
package com.team6.swarm.core;

import java.util.List;

public class PhysicsEngine {

    // Physics constants
    public static final double WORLD_WIDTH = 800.0;
    public static final double WORLD_HEIGHT = 600.0;
    public static final double COLLISION_DISTANCE = 10.0;
    public static final double SEPARATION_DISTANCE = 30.0;  // For flocking

    // Boundary handling modes
    public enum BoundaryMode {
        BOUNCE,  // Reflect off walls
        WRAP,    // Wrap around to opposite side
        CLAMP    // Stop at boundary
    }

    private BoundaryMode boundaryMode;

    public PhysicsEngine() {
        this.boundaryMode = BoundaryMode.BOUNCE;
    }

    public PhysicsEngine(BoundaryMode mode) {
        this.boundaryMode = mode;
    }

    /**
     * Apply a force to an agent's velocity
     * F = ma, assuming mass = 1.0
     */
    public void applyForce(AgentState state, Vector2D force, double deltaTime) {
        if (force == null) return;

        // Apply force to velocity (F = ma, m = 1)
        state.velocity = state.velocity.add(force.multiply(deltaTime));

        // Limit to max speed
        limitVelocity(state, state.maxSpeed);
    }

    /**
     * Update position based on velocity
     * Uses Euler integration (good enough for games/simulations)
     */
    public void updatePosition(AgentState state, double deltaTime) {
        // Integrate velocity into position
        state.position.x += state.velocity.x * deltaTime;
        state.position.y += state.velocity.y * deltaTime;

        // Handle boundaries
        checkBoundaries(state);
    }

    /**
     * Limit velocity to max speed
     */
    public void limitVelocity(AgentState state, double maxSpeed) {
        double speed = state.velocity.magnitude();

        if (speed > maxSpeed) {
            state.velocity = state.velocity.normalize().multiply(maxSpeed);
        }
    }

    /**
     * Handle world boundaries based on current mode
     */
    public void checkBoundaries(AgentState state) {
        switch (boundaryMode) {
            case BOUNCE:
                handleBounceBoundaries(state);
                break;
            case WRAP:
                handleWrapBoundaries(state);
                break;
            case CLAMP:
                handleClampBoundaries(state);
                break;
        }
    }

    private void handleBounceBoundaries(AgentState state) {
        // Bounce off walls
        if (state.position.x < 0 || state.position.x > WORLD_WIDTH) {
            state.velocity.x *= -1;
            state.position.x = Math.max(0, Math.min(WORLD_WIDTH, state.position.x));
        }

        if (state.position.y < 0 || state.position.y > WORLD_HEIGHT) {
            state.velocity.y *= -1;
            state.position.y = Math.max(0, Math.min(WORLD_HEIGHT, state.position.y));
        }
    }

    private void handleWrapBoundaries(AgentState state) {
        // Wrap around to opposite side
        if (state.position.x < 0) state.position.x = WORLD_WIDTH;
        if (state.position.x > WORLD_WIDTH) state.position.x = 0;
        if (state.position.y < 0) state.position.y = WORLD_HEIGHT;
        if (state.position.y > WORLD_HEIGHT) state.position.y = 0;
    }

    private void handleClampBoundaries(AgentState state) {
        // Clamp to boundaries and stop
        boolean hitBoundary = false;

        if (state.position.x < 0) {
            state.position.x = 0;
            state.velocity.x = 0;
            hitBoundary = true;
        } else if (state.position.x > WORLD_WIDTH) {
            state.position.x = WORLD_WIDTH;
            state.velocity.x = 0;
            hitBoundary = true;
        }

        if (state.position.y < 0) {
            state.position.y = 0;
            state.velocity.y = 0;
            hitBoundary = true;
        } else if (state.position.y > WORLD_HEIGHT) {
            state.position.y = WORLD_HEIGHT;
            state.velocity.y = 0;
            hitBoundary = true;
        }
    }

    /**
     * Check if two agents are colliding
     */
    public boolean checkCollision(AgentState agent1, AgentState agent2) {
        double distance = agent1.position.distanceTo(agent2.position);
        return distance < COLLISION_DISTANCE;
    }

    /**
     * Calculate separation force to avoid nearby agents
     * Used for flocking behavior
     */
    public Vector2D separationForce(AgentState agent, List<AgentState> others) {
        Vector2D separationForce = new Vector2D(0, 0);
        int count = 0;

        for (AgentState other : others) {
            if (other.agentId == agent.agentId) continue;

            double distance = agent.position.distanceTo(other.position);

            if (distance < SEPARATION_DISTANCE && distance > 0) {
                // Calculate vector pointing away from neighbor
                Vector2D diff = new Vector2D(
                    agent.position.x - other.position.x,
                    agent.position.y - other.position.y
                );

                // Weight by distance (closer = stronger repulsion)
                diff = diff.normalize().multiply(1.0 / distance);
                separationForce = separationForce.add(diff);
                count++;
            }
        }

        if (count > 0) {
            separationForce = separationForce.multiply(1.0 / count);
        }

        return separationForce;
    }

    /**
     * Calculate steering force toward target
     * Returns force that will move agent toward target at desired speed
     */
    public Vector2D seek(AgentState agent, Point2D target, double desiredSpeed) {
        // Calculate desired velocity
        Vector2D desired = new Vector2D(
            target.x - agent.position.x,
            target.y - agent.position.y
        );

        desired = desired.normalize().multiply(desiredSpeed);

        // Steering force = desired - current
        Vector2D steer = desired.subtract(agent.velocity);

        return steer;
    }

    /**
     * Calculate steering force away from target
     */
    public Vector2D flee(AgentState agent, Point2D threat, double desiredSpeed) {
        // Opposite of seek
        Vector2D desired = new Vector2D(
            agent.position.x - threat.x,
            agent.position.y - threat.y
        );

        desired = desired.normalize().multiply(desiredSpeed);
        Vector2D steer = desired.subtract(agent.velocity);

        return steer;
    }

    // Getters and setters
    public void setBoundaryMode(BoundaryMode mode) {
        this.boundaryMode = mode;
    }

    public BoundaryMode getBoundaryMode() {
        return boundaryMode;
    }
}