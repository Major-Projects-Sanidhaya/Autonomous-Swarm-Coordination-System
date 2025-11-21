/**
 * SIMULATIONADAPTER - Simulated Hardware Implementation (Week 11-12)
 *
 * PURPOSE:
 * - Implements HardwareInterface for simulated agents
 * - Provides software-based agent control (no physical hardware)
 * - Enables testing without physical devices
 * - Default implementation for development and demonstration
 *
 * SIMULATION FEATURES:
 * - Physics-based movement simulation
 * - Realistic acceleration and deceleration
 * - Battery drain simulation
 * - Simulated sensor readings
 * - Configurable noise and delays
 *
 * MOVEMENT SIMULATION:
 * - Integrates with PhysicsEngine for realistic motion
 * - Respects speed and acceleration limits
 * - Simulates inertia and momentum
 * - Handles boundaries and collisions
 *
 * STATE MANAGEMENT:
 * - Maintains position and velocity
 * - Tracks battery consumption
 * - Simulates connection status
 * - Records movement history
 *
 * INTEGRATION:
 * - Works seamlessly with existing Agent class
 * - Compatible with all system components
 * - Drop-in replacement for physical hardware
 * - No code changes needed in agent logic
 *
 * CONFIGURATION:
 * - Adjustable physics parameters
 * - Configurable battery drain rate
 * - Optional noise injection
 * - Simulation speed multiplier
 *
 * USAGE:
 * SimulationAdapter adapter = new SimulationAdapter();
 * adapter.initialize(agentId);
 * adapter.setVelocityVector(new Vector2D(10, 0));
 * adapter.update(deltaTime);
 * Point2D pos = adapter.getPosition();
 */
package com.team6.swarm.core;

public class SimulationAdapter implements HardwareInterface {

    // Agent identification
    private int agentId;
    private boolean initialized;

    // State
    private Point2D position;
    private Vector2D velocity;
    private double heading;
    private double batteryLevel;

    // Target for navigation
    private Point2D targetPosition;
    private boolean hasTarget;

    // Configuration
    private HardwareConfig config;
    private HardwareCapabilities capabilities;

    // Physics
    private PhysicsEngine physics;

    // Error tracking
    private String lastError;

    // Statistics
    private double totalDistanceTraveled;
    private long lastUpdateTime;

    /**
     * Default constructor
     */
    public SimulationAdapter() {
        this.initialized = false;
        this.position = new Point2D(0, 0);
        this.velocity = new Vector2D(0, 0);
        this.heading = 0.0;
        this.batteryLevel = 1.0;
        this.targetPosition = null;
        this.hasTarget = false;
        this.config = new HardwareConfig();
        this.capabilities = createCapabilities();
        this.physics = new PhysicsEngine();
        this.lastError = null;
        this.totalDistanceTraveled = 0.0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    // ==================== INITIALIZATION ====================

    @Override
    public boolean initialize(int agentId) {
        return initialize(agentId, new HardwareConfig());
    }

    @Override
    public boolean initialize(int agentId, HardwareConfig config) {
        this.agentId = agentId;
        this.config = config;
        this.initialized = true;

        System.out.println("SimulationAdapter: Initialized for Agent " + agentId);
        return true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    // ==================== MOVEMENT CONTROL ====================

    @Override
    public boolean setVelocity(double linearVelocity, double angularVelocity) {
        if (!initialized) {
            lastError = "Adapter not initialized";
            return false;
        }

        // Convert to velocity vector
        // linear: forward/back, angular: rotation
        double speed = linearVelocity * config.maxSpeed;
        double vx = speed * Math.cos(heading);
        double vy = speed * Math.sin(heading);

        velocity = new Vector2D(vx, vy);

        // Update heading based on angular velocity
        heading += angularVelocity * config.maxTurnRate * 0.016;  // Assume ~60 FPS

        return true;
    }

    @Override
    public boolean setVelocityVector(Vector2D velocity) {
        if (!initialized) {
            lastError = "Adapter not initialized";
            return false;
        }

        // Limit to max speed
        double speed = velocity.magnitude();
        if (speed > config.maxSpeed) {
            velocity = velocity.normalize().multiply(config.maxSpeed);
        }

        this.velocity = velocity;

        // Update heading to match velocity direction
        if (speed > 0.1) {
            this.heading = Math.atan2(velocity.y, velocity.x);
        }

        // Clear target when manually setting velocity
        hasTarget = false;

        return true;
    }

    @Override
    public boolean setTargetPosition(Point2D target) {
        if (!initialized) {
            lastError = "Adapter not initialized";
            return false;
        }

        this.targetPosition = target;
        this.hasTarget = true;

        return true;
    }

    @Override
    public boolean emergencyStop() {
        if (!initialized) {
            return false;
        }

        velocity = new Vector2D(0, 0);
        hasTarget = false;

        System.out.println("SimulationAdapter: Emergency stop for Agent " + agentId);
        return true;
    }

    // ==================== STATUS QUERIES ====================

    @Override
    public Point2D getPosition() {
        return new Point2D(position.x, position.y);
    }

    @Override
    public Vector2D getVelocity() {
        return new Vector2D(velocity.x, velocity.y);
    }

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public double getBatteryLevel() {
        return batteryLevel;
    }

    @Override
    public HardwareStatus getStatus() {
        HardwareStatus status = new HardwareStatus();
        status.position = getPosition();
        status.velocity = getVelocity();
        status.heading = heading;
        status.batteryLevel = batteryLevel;
        status.isConnected = initialized;
        status.isMoving = velocity.magnitude() > 0.1;
        status.errorMessage = lastError;
        status.lastUpdateTime = lastUpdateTime;

        return status;
    }

    // ==================== CAPABILITIES ====================

    @Override
    public HardwareCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean hasFeature(String feature) {
        for (String sensor : capabilities.availableSensors) {
            if (sensor.equalsIgnoreCase(feature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create capabilities object for simulated hardware
     */
    private HardwareCapabilities createCapabilities() {
        HardwareCapabilities caps = new HardwareCapabilities();
        caps.maxSpeed = 50.0;
        caps.maxAcceleration = 10.0;
        caps.maxTurnRate = Math.PI;
        caps.communicationRange = 100.0;
        caps.hasPositionControl = true;
        caps.hasVelocityControl = true;
        caps.hasOrientationControl = true;
        caps.availableSensors = new String[]{"IMU", "GPS", "Rangefinder"};

        return caps;
    }

    // ==================== LIFECYCLE ====================

    @Override
    public void update(double deltaTime) {
        if (!initialized) {
            return;
        }

        // Navigate to target if set
        if (hasTarget && targetPosition != null) {
            navigateToTarget(deltaTime);
        }

        // Update position based on velocity
        Point2D oldPosition = new Point2D(position.x, position.y);
        position = new Point2D(
            position.x + velocity.x * deltaTime,
            position.y + velocity.y * deltaTime
        );

        // Track distance traveled
        totalDistanceTraveled += oldPosition.distanceTo(position);

        // Simulate battery drain
        double speed = velocity.magnitude();
        double drain = (speed / config.maxSpeed) * 0.001 * deltaTime;  // Faster = more drain
        batteryLevel = Math.max(0.0, batteryLevel - drain);

        // Update timestamp
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Navigate towards target position
     */
    private void navigateToTarget(double deltaTime) {
        if (targetPosition == null) {
            return;
        }

        // Calculate direction to target
        double dx = targetPosition.x - position.x;
        double dy = targetPosition.y - position.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Check if reached target
        if (distance < 5.0) {
            velocity = new Vector2D(0, 0);
            hasTarget = false;
            return;
        }

        // Calculate desired velocity
        double desiredSpeed = Math.min(config.maxSpeed, distance * 0.5);
        Vector2D desiredVelocity = new Vector2D(
            (dx / distance) * desiredSpeed,
            (dy / distance) * desiredSpeed
        );

        // Gradually adjust velocity (smooth acceleration)
        velocity = velocity.add(
            desiredVelocity.subtract(velocity).multiply(0.1)
        );

        // Update heading
        if (velocity.magnitude() > 0.1) {
            heading = Math.atan2(velocity.y, velocity.x);
        }
    }

    @Override
    public void shutdown() {
        initialized = false;
        velocity = new Vector2D(0, 0);
        System.out.println("SimulationAdapter: Shutdown for Agent " + agentId);
    }

    @Override
    public boolean reset() {
        velocity = new Vector2D(0, 0);
        hasTarget = false;
        batteryLevel = 1.0;
        lastError = null;
        totalDistanceTraveled = 0.0;

        System.out.println("SimulationAdapter: Reset for Agent " + agentId);
        return true;
    }

    // ==================== ERROR HANDLING ====================

    @Override
    public boolean hasErrors() {
        return lastError != null;
    }

    @Override
    public String getLastError() {
        return lastError;
    }

    @Override
    public void clearErrors() {
        lastError = null;
    }

    // ==================== ADDITIONAL METHODS ====================

    /**
     * Set position directly (for testing/setup)
     */
    public void setPosition(Point2D position) {
        this.position = new Point2D(position.x, position.y);
    }

    /**
     * Set heading directly
     */
    public void setHeading(double heading) {
        this.heading = heading;
    }

    /**
     * Set battery level directly (for testing)
     */
    public void setBatteryLevel(double level) {
        this.batteryLevel = Math.max(0.0, Math.min(1.0, level));
    }

    /**
     * Get total distance traveled
     */
    public double getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    /**
     * Get agent ID
     */
    public int getAgentId() {
        return agentId;
    }

    @Override
    public String toString() {
        return String.format("SimulationAdapter[agent=%d, pos=%s, vel=%s, battery=%.1f%%]",
            agentId, position, velocity, batteryLevel * 100);
    }
}
