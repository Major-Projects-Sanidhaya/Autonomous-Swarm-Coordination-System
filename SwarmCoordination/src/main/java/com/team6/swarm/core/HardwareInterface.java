/**
 * HARDWAREINTERFACE - Abstract Hardware Control Interface (Week 11-12)
 *
 * PURPOSE:
 * - Defines unified interface for controlling agents (simulated or physical)
 * - Enables seamless switching between simulation and hardware
 * - Abstracts hardware-specific implementation details
 * - Supports multiple hardware platforms (RC cars, drones, robots)
 *
 * DESIGN PATTERN: Adapter Pattern
 * - HardwareInterface: Abstract interface
 * - SimulationAdapter: Implementation for simulated agents
 * - RCCarAdapter: Implementation for physical RC cars
 * - DroneAdapter: Implementation for drones (future)
 *
 * CORE OPERATIONS:
 * 1. initialize() - Set up hardware/simulation
 * 2. move() - Send movement command to agent
 * 3. stop() - Emergency stop
 * 4. getPosition() - Get current position
 * 5. getStatus() - Get hardware status
 * 6. shutdown() - Clean shutdown
 *
 * MOVEMENT COMMANDS:
 * - Linear velocity (forward/backward)
 * - Angular velocity (turning)
 * - Target position (navigation)
 * - Velocity vector (direct control)
 *
 * STATUS INFORMATION:
 * - Position and velocity
 * - Battery level
 * - Connection status
 * - Sensor readings
 * - Error states
 *
 * HARDWARE CAPABILITIES:
 * - Speed limits
 * - Turn radius
 * - Acceleration limits
 * - Communication range
 * - Sensor availability
 *
 * USAGE PATTERN:
 * 1. Create adapter:
 *    HardwareInterface hw = new SimulationAdapter();
 *    // or
 *    HardwareInterface hw = new RCCarAdapter(portName);
 *
 * 2. Initialize:
 *    hw.initialize(agentId);
 *
 * 3. Control:
 *    hw.move(velocity);
 *    hw.setTargetPosition(target);
 *
 * 4. Monitor:
 *    Point2D pos = hw.getPosition();
 *    HardwareStatus status = hw.getStatus();
 *
 * 5. Shutdown:
 *    hw.shutdown();
 *
 * INTEGRATION POINTS:
 * - Agent: Uses hardware interface for physical control
 * - PhysicsEngine: Works with SimulationAdapter
 * - SystemController: Manages hardware initialization
 * - FailureDetector: Monitors hardware status
 *
 * THREAD SAFETY:
 * - All implementations must be thread-safe
 * - Commands may come from multiple threads
 * - Status queries must be non-blocking
 */
package com.team6.swarm.core;

public interface HardwareInterface {

    // ==================== INITIALIZATION ====================

    /**
     * Initialize hardware for specific agent
     *
     * @param agentId Unique agent identifier
     * @return true if initialization successful
     */
    boolean initialize(int agentId);

    /**
     * Initialize with configuration parameters
     *
     * @param agentId Unique agent identifier
     * @param config Configuration parameters (port, baudrate, etc.)
     * @return true if initialization successful
     */
    boolean initialize(int agentId, HardwareConfig config);

    /**
     * Check if hardware is initialized and ready
     *
     * @return true if ready for commands
     */
    boolean isInitialized();

    // ==================== MOVEMENT CONTROL ====================

    /**
     * Set linear and angular velocity
     *
     * @param linearVelocity Forward/backward speed (-1.0 to 1.0)
     * @param angularVelocity Turning rate (-1.0 to 1.0, negative = left)
     * @return true if command accepted
     */
    boolean setVelocity(double linearVelocity, double angularVelocity);

    /**
     * Set velocity vector (2D movement)
     *
     * @param velocity Velocity vector (x, y components)
     * @return true if command accepted
     */
    boolean setVelocityVector(Vector2D velocity);

    /**
     * Move to target position
     *
     * @param target Target position
     * @return true if command accepted
     */
    boolean setTargetPosition(Point2D target);

    /**
     * Emergency stop - halt all movement immediately
     *
     * @return true if stop executed
     */
    boolean emergencyStop();

    // ==================== STATUS QUERIES ====================

    /**
     * Get current position
     *
     * @return Current position, or null if unavailable
     */
    Point2D getPosition();

    /**
     * Get current velocity
     *
     * @return Current velocity vector, or null if unavailable
     */
    Vector2D getVelocity();

    /**
     * Get current heading (direction agent is facing)
     *
     * @return Heading in radians (0 = East)
     */
    double getHeading();

    /**
     * Get battery level
     *
     * @return Battery level (0.0 to 1.0), or -1 if unavailable
     */
    double getBatteryLevel();

    /**
     * Get comprehensive hardware status
     *
     * @return Status object with all hardware information
     */
    HardwareStatus getStatus();

    // ==================== CAPABILITIES ====================

    /**
     * Get hardware capabilities
     *
     * @return Capabilities object describing limits and features
     */
    HardwareCapabilities getCapabilities();

    /**
     * Check if specific feature is supported
     *
     * @param feature Feature name (e.g., "GPS", "IMU", "Camera")
     * @return true if feature available
     */
    boolean hasFeature(String feature);

    // ==================== LIFECYCLE ====================

    /**
     * Update hardware state (called every frame)
     *
     * @param deltaTime Time since last update (seconds)
     */
    void update(double deltaTime);

    /**
     * Shutdown hardware cleanly
     * Release resources, close connections
     */
    void shutdown();

    /**
     * Reset hardware to initial state
     *
     * @return true if reset successful
     */
    boolean reset();

    // ==================== ERROR HANDLING ====================

    /**
     * Check if hardware has errors
     *
     * @return true if errors present
     */
    boolean hasErrors();

    /**
     * Get last error message
     *
     * @return Error description, or null if no errors
     */
    String getLastError();

    /**
     * Clear error state
     */
    void clearErrors();
}

/**
 * HARDWARECONFIG - Configuration parameters for hardware
 */
class HardwareConfig {
    // Communication
    public String portName;           // Serial port or network address
    public int baudRate;              // Serial baud rate
    public int timeout;               // Communication timeout (ms)

    // Physical limits
    public double maxSpeed;           // Maximum speed (units/sec)
    public double maxAcceleration;    // Maximum acceleration
    public double maxTurnRate;        // Maximum turning rate (rad/sec)
    public double wheelbase;          // Distance between wheels (m)

    // Sensors
    public boolean hasGPS;
    public boolean hasIMU;
    public boolean hasCamera;
    public boolean hasRangefinder;

    // Operational
    public double updateRate;         // Control update rate (Hz)
    public boolean enableLogging;     // Log hardware commands

    /**
     * Default configuration
     */
    public HardwareConfig() {
        this.portName = "COM1";
        this.baudRate = 115200;
        this.timeout = 1000;
        this.maxSpeed = 50.0;
        this.maxAcceleration = 10.0;
        this.maxTurnRate = Math.PI;
        this.wheelbase = 0.2;
        this.hasGPS = false;
        this.hasIMU = true;
        this.hasCamera = false;
        this.hasRangefinder = false;
        this.updateRate = 30.0;
        this.enableLogging = true;
    }
}

/**
 * HARDWARESTATUS - Current hardware status
 */
class HardwareStatus {
    public Point2D position;
    public Vector2D velocity;
    public double heading;
    public double batteryLevel;
    public boolean isConnected;
    public boolean isMoving;
    public String errorMessage;
    public long lastUpdateTime;

    public HardwareStatus() {
        this.position = new Point2D(0, 0);
        this.velocity = new Vector2D(0, 0);
        this.heading = 0.0;
        this.batteryLevel = 1.0;
        this.isConnected = true;
        this.isMoving = false;
        this.errorMessage = null;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("HardwareStatus[pos=%s, vel=%s, battery=%.1f%%, connected=%s]",
            position, velocity, batteryLevel * 100, isConnected);
    }
}

/**
 * HARDWARECAPABILITIES - Hardware capability description
 */
class HardwareCapabilities {
    public double maxSpeed;
    public double maxAcceleration;
    public double maxTurnRate;
    public double communicationRange;
    public boolean hasPositionControl;
    public boolean hasVelocityControl;
    public boolean hasOrientationControl;
    public String[] availableSensors;

    public HardwareCapabilities() {
        this.maxSpeed = 50.0;
        this.maxAcceleration = 10.0;
        this.maxTurnRate = Math.PI;
        this.communicationRange = 100.0;
        this.hasPositionControl = true;
        this.hasVelocityControl = true;
        this.hasOrientationControl = true;
        this.availableSensors = new String[]{"IMU"};
    }

    @Override
    public String toString() {
        return String.format("Capabilities[speed=%.1f, accel=%.1f, turn=%.2f rad/s, sensors=%d]",
            maxSpeed, maxAcceleration, maxTurnRate, availableSensors.length);
    }
}
