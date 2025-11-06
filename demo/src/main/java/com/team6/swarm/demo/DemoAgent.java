package com.team6.swarm.demo;

import javafx.scene.paint.Color;
import java.util.List;
import java.util.ArrayList;

/**
 * Autonomous agent with flocking behavior for swarm demo.
 * Implements Reynolds' three flocking rules: separation, alignment, cohesion.
 */
public class DemoAgent {
    // Agent state
    private String id;
    private double x, y;           // Position
    private double vx, vy;         // Velocity
    private double ax, ay;         // Acceleration
    private AgentState state;

    // Physical constraints
    private static final double MAX_SPEED = 3.0;
    private static final double MAX_FORCE = 0.1;
    private static final double AGENT_SIZE = 8.0;

    // Perception ranges
    private static final double SEPARATION_RADIUS = 25.0;
    private static final double ALIGNMENT_RADIUS = 50.0;
    private static final double COHESION_RADIUS = 50.0;
    private static final double COMMUNICATION_RADIUS = 100.0;

    // Behavior weights (configurable)
    private double separationWeight = 1.5;
    private double alignmentWeight = 1.0;
    private double cohesionWeight = 1.0;
    private double targetWeight = 0.5;

    // Target/waypoint
    private Double targetX = null;
    private Double targetY = null;

    // Voting state
    private boolean isVoting = false;
    private int voteChoice = -1; // -1 = no vote

    public enum AgentState {
        ACTIVE(Color.CYAN),
        VOTING(Color.ORANGE),
        DECISION_MADE(Color.LIME),
        NETWORK_ISSUE(Color.RED);

        public final Color color;
        AgentState(Color color) { this.color = color; }
    }

    /**
     * Create a new agent with random position and velocity
     */
    public DemoAgent(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;

        // Random initial velocity
        double angle = Math.random() * 2 * Math.PI;
        double speed = 1.0 + Math.random();
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;

        this.ax = 0;
        this.ay = 0;
        this.state = AgentState.ACTIVE;
    }

    /**
     * Update agent physics and behavior
     */
    public void update(List<DemoAgent> neighbors, double deltaTime,
                      double canvasWidth, double canvasHeight) {
        // Reset acceleration
        ax = 0;
        ay = 0;

        // Apply flocking behaviors
        applyFlocking(neighbors);

        // Apply target seeking if waypoint exists
        if (targetX != null && targetY != null) {
            applyTargetSeeking();
        }

        // Update velocity
        vx += ax * deltaTime;
        vy += ay * deltaTime;

        // Limit speed
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed > MAX_SPEED) {
            vx = (vx / speed) * MAX_SPEED;
            vy = (vy / speed) * MAX_SPEED;
        }

        // Update position
        x += vx * deltaTime;
        y += vy * deltaTime;

        // Boundary wrapping (toroidal world)
        if (x < 0) x = canvasWidth;
        if (x > canvasWidth) x = 0;
        if (y < 0) y = canvasHeight;
        if (y > canvasHeight) y = 0;
    }

    /**
     * Apply Reynolds' flocking rules
     */
    private void applyFlocking(List<DemoAgent> neighbors) {
        double sepX = 0, sepY = 0;
        double aliX = 0, aliY = 0;
        double cohX = 0, cohY = 0;

        int separationCount = 0;
        int alignmentCount = 0;
        int cohesionCount = 0;

        for (DemoAgent other : neighbors) {
            if (other == this) continue;

            double dist = distance(other);

            // Separation: steer away from nearby agents
            if (dist < SEPARATION_RADIUS && dist > 0) {
                double diffX = x - other.x;
                double diffY = y - other.y;
                // Weight by distance (closer = stronger repulsion)
                sepX += diffX / dist;
                sepY += diffY / dist;
                separationCount++;
            }

            // Alignment: match velocity with nearby agents
            if (dist < ALIGNMENT_RADIUS) {
                aliX += other.vx;
                aliY += other.vy;
                alignmentCount++;
            }

            // Cohesion: steer towards average position of neighbors
            if (dist < COHESION_RADIUS) {
                cohX += other.x;
                cohY += other.y;
                cohesionCount++;
            }
        }

        // Average and apply separation
        if (separationCount > 0) {
            sepX /= separationCount;
            sepY /= separationCount;
            double[] force = limitForce(sepX, sepY);
            ax += force[0] * separationWeight;
            ay += force[1] * separationWeight;
        }

        // Average and apply alignment
        if (alignmentCount > 0) {
            aliX /= alignmentCount;
            aliY /= alignmentCount;
            // Steer towards desired velocity
            double steerX = aliX - vx;
            double steerY = aliY - vy;
            double[] force = limitForce(steerX, steerY);
            ax += force[0] * alignmentWeight;
            ay += force[1] * alignmentWeight;
        }

        // Average and apply cohesion
        if (cohesionCount > 0) {
            cohX /= cohesionCount;
            cohY /= cohesionCount;
            // Steer towards center of mass
            double steerX = cohX - x;
            double steerY = cohY - y;
            double[] force = limitForce(steerX, steerY);
            ax += force[0] * cohesionWeight;
            ay += force[1] * cohesionWeight;
        }
    }

    /**
     * Apply force towards target waypoint
     */
    private void applyTargetSeeking() {
        double desiredX = targetX - x;
        double desiredY = targetY - y;

        double dist = Math.sqrt(desiredX * desiredX + desiredY * desiredY);
        if (dist < 10) {
            // Reached target
            targetX = null;
            targetY = null;
            return;
        }

        // Normalize and scale to max speed
        desiredX = (desiredX / dist) * MAX_SPEED;
        desiredY = (desiredY / dist) * MAX_SPEED;

        // Steering = Desired - Current velocity
        double steerX = desiredX - vx;
        double steerY = desiredY - vy;

        double[] force = limitForce(steerX, steerY);
        ax += force[0] * targetWeight;
        ay += force[1] * targetWeight;
    }

    /**
     * Limit force magnitude to MAX_FORCE
     */
    private double[] limitForce(double fx, double fy) {
        double magnitude = Math.sqrt(fx * fx + fy * fy);
        if (magnitude > MAX_FORCE) {
            fx = (fx / magnitude) * MAX_FORCE;
            fy = (fy / magnitude) * MAX_FORCE;
        }
        return new double[]{fx, fy};
    }

    /**
     * Calculate distance to another agent
     */
    public double distance(DemoAgent other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Check if this agent can communicate with another
     */
    public boolean canCommunicateWith(DemoAgent other) {
        return distance(other) <= COMMUNICATION_RADIUS;
    }

    /**
     * Get heading angle in radians
     */
    public double getHeading() {
        return Math.atan2(vy, vx);
    }

    /**
     * Move agent to specific formation position
     */
    public void moveToFormation(double targetX, double targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    // Getters and setters
    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public AgentState getState() { return state; }
    public void setState(AgentState state) { this.state = state; }

    public boolean isVoting() { return isVoting; }
    public void setVoting(boolean voting) {
        this.isVoting = voting;
        if (voting) {
            this.state = AgentState.VOTING;
        }
    }

    public int getVoteChoice() { return voteChoice; }
    public void setVoteChoice(int choice) {
        this.voteChoice = choice;
        if (choice >= 0) {
            this.state = AgentState.DECISION_MADE;
        }
    }

    public void setSeparationWeight(double w) { this.separationWeight = w; }
    public void setAlignmentWeight(double w) { this.alignmentWeight = w; }
    public void setCohesionWeight(double w) { this.cohesionWeight = w; }

    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
    }

    public void clearTarget() {
        this.targetX = null;
        this.targetY = null;
    }

    public static double getSize() { return AGENT_SIZE; }
    public static double getCommunicationRadius() { return COMMUNICATION_RADIUS; }
}
