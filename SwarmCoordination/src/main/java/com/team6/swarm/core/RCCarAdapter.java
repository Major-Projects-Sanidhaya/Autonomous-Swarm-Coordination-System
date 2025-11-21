/**
 * RCCARADAPTER - Physical RC Car Hardware Implementation (Week 11-12)
 *
 * PURPOSE:
 * - Implements HardwareInterface for physical RC cars
 * - Provides real hardware control via serial/wireless communication
 * - Enables physical demonstrations and deployments
 * - Optional implementation for hardware availability
 *
 * HARDWARE SUPPORT:
 * - Generic RC car platforms with serial control
 * - Arduino-based RC car controllers
 * - Raspberry Pi controlled vehicles
 * - ESP32/ESP8266 wireless RC cars
 * - Bluetooth/WiFi enabled vehicles
 *
 * COMMUNICATION:
 * - Serial port (USB/UART) communication
 * - Wireless (WiFi/Bluetooth) communication
 * - Command/response protocol
 * - Heartbeat monitoring
 * - Error detection and recovery
 *
 * COMMAND PROTOCOL:
 * - ASCII or binary protocol
 * - Format: "CMD:param1,param2\n"
 * - Examples:
 *   - "MOVE:50,0\n" - Forward at 50% speed
 *   - "TURN:30\n" - Turn right 30 degrees
 *   - "STOP\n" - Emergency stop
 *   - "STATUS\n" - Request status update
 *
 * FEATURES:
 * - Automatic reconnection on disconnect
 * - Command queuing for reliability
 * - Position tracking via onboard sensors
 * - Battery monitoring
 * - Collision detection
 *
 * LIMITATIONS:
 * - Physical constraints (speed, turn radius)
 * - Communication latency
 * - Limited precision compared to simulation
 * - Requires hardware calibration
 * - Environmental factors (friction, surface)
 *
 * SAFETY:
 * - Watchdog timer for connection loss
 * - Emergency stop capability
 * - Boundary enforcement
 * - Collision avoidance
 * - Battery low warnings
 *
 * USAGE:
 * RCCarAdapter adapter = new RCCarAdapter("COM3");
 * adapter.initialize(agentId, config);
 * adapter.setVelocity(0.5, 0.2);  // 50% forward, 20% right turn
 * adapter.update(deltaTime);
 * adapter.shutdown();
 *
 * HARDWARE SETUP REQUIRED:
 * 1. RC car with microcontroller (Arduino/ESP32)
 * 2. Motor driver for vehicle control
 * 3. Position sensors (GPS/encoders/IMU)
 * 4. Communication module (Serial/WiFi/Bluetooth)
 * 5. Battery monitoring circuit
 * 6. Safety features (emergency stop)
 *
 * NOTE: This is a template implementation
 * Actual hardware integration requires:
 * - Hardware-specific drivers
 * - Communication protocol implementation
 * - Sensor integration code
 * - Calibration procedures
 */
package com.team6.swarm.core;

public class RCCarAdapter implements HardwareInterface {

    // Agent identification
    private int agentId;
    private boolean initialized;

    // Communication
    private String portName;
    private boolean connected;
    private Object serialPort;  // Would be actual serial port object

    // State tracking
    private Point2D position;
    private Vector2D velocity;
    private double heading;
    private double batteryLevel;

    // Configuration
    private HardwareConfig config;
    private HardwareCapabilities capabilities;

    // Command tracking
    private long lastCommandTime;
    private long lastStatusUpdate;

    // Error handling
    private String lastError;
    private int reconnectAttempts;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;

    // Watchdog
    private static final long WATCHDOG_TIMEOUT_MS = 2000;
    private long lastHeartbeat;

    /**
     * Constructor with port name
     */
    public RCCarAdapter(String portName) {
        this.portName = portName;
        this.initialized = false;
        this.connected = false;
        this.serialPort = null;
        this.position = new Point2D(0, 0);
        this.velocity = new Vector2D(0, 0);
        this.heading = 0.0;
        this.batteryLevel = 1.0;
        this.config = new HardwareConfig();
        this.capabilities = createCapabilities();
        this.lastError = null;
        this.reconnectAttempts = 0;
        this.lastCommandTime = System.currentTimeMillis();
        this.lastStatusUpdate = System.currentTimeMillis();
        this.lastHeartbeat = System.currentTimeMillis();
    }

    /**
     * Default constructor (uses default port)
     */
    public RCCarAdapter() {
        this("COM3");  // Default Windows port
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

        // Open communication port
        if (!openConnection()) {
            lastError = "Failed to open connection to " + portName;
            System.err.println("RCCarAdapter: " + lastError);
            return false;
        }

        // Send initialization command
        if (!sendCommand("INIT:" + agentId)) {
            lastError = "Failed to initialize RC car";
            System.err.println("RCCarAdapter: " + lastError);
            return false;
        }

        // Wait for response
        String response = waitForResponse(1000);
        if (response == null || !response.startsWith("OK")) {
            lastError = "RC car initialization failed: " + response;
            System.err.println("RCCarAdapter: " + lastError);
            return false;
        }

        this.initialized = true;
        this.connected = true;
        this.lastHeartbeat = System.currentTimeMillis();

        System.out.println("RCCarAdapter: Initialized Agent " + agentId + " on " + portName);
        return true;
    }

    @Override
    public boolean isInitialized() {
        return initialized && connected;
    }

    // ==================== MOVEMENT CONTROL ====================

    @Override
    public boolean setVelocity(double linearVelocity, double angularVelocity) {
        if (!isInitialized()) {
            lastError = "Adapter not initialized or disconnected";
            return false;
        }

        // Scale to percentages (-100 to 100)
        int linear = (int) (linearVelocity * 100);
        int angular = (int) (angularVelocity * 100);

        // Clamp values
        linear = Math.max(-100, Math.min(100, linear));
        angular = Math.max(-100, Math.min(100, angular));

        // Send command to RC car
        String cmd = String.format("MOVE:%d,%d", linear, angular);
        return sendCommand(cmd);
    }

    @Override
    public boolean setVelocityVector(Vector2D velocity) {
        if (!isInitialized()) {
            lastError = "Adapter not initialized or disconnected";
            return false;
        }

        // Convert velocity vector to linear and angular velocities
        double speed = velocity.magnitude();
        double angle = Math.atan2(velocity.y, velocity.x);

        // Calculate angular difference from current heading
        double angleDiff = angle - heading;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

        double linearVel = speed / config.maxSpeed;
        double angularVel = angleDiff / Math.PI;

        return setVelocity(linearVel, angularVel);
    }

    @Override
    public boolean setTargetPosition(Point2D target) {
        if (!isInitialized()) {
            lastError = "Adapter not initialized or disconnected";
            return false;
        }

        // Send target position to RC car (if it supports autonomous navigation)
        String cmd = String.format("TARGET:%.2f,%.2f", target.x, target.y);
        return sendCommand(cmd);
    }

    @Override
    public boolean emergencyStop() {
        if (!connected) {
            return false;
        }

        // Send emergency stop command
        boolean success = sendCommand("ESTOP");
        velocity = new Vector2D(0, 0);

        System.out.println("RCCarAdapter: Emergency stop for Agent " + agentId);
        return success;
    }

    // ==================== STATUS QUERIES ====================

    @Override
    public Point2D getPosition() {
        // Request position from RC car
        requestStatus();
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
        requestStatus();
        return batteryLevel;
    }

    @Override
    public HardwareStatus getStatus() {
        requestStatus();

        HardwareStatus status = new HardwareStatus();
        status.position = getPosition();
        status.velocity = getVelocity();
        status.heading = heading;
        status.batteryLevel = batteryLevel;
        status.isConnected = connected;
        status.isMoving = velocity.magnitude() > 0.1;
        status.errorMessage = lastError;
        status.lastUpdateTime = lastStatusUpdate;

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
     * Create capabilities for RC car hardware
     */
    private HardwareCapabilities createCapabilities() {
        HardwareCapabilities caps = new HardwareCapabilities();
        caps.maxSpeed = 30.0;  // RC cars typically slower than simulation
        caps.maxAcceleration = 5.0;
        caps.maxTurnRate = Math.PI / 2;  // 90 degrees per second
        caps.communicationRange = 50.0;  // Limited by RC range
        caps.hasPositionControl = true;
        caps.hasVelocityControl = true;
        caps.hasOrientationControl = true;
        caps.availableSensors = new String[]{"IMU", "Encoders"};

        return caps;
    }

    // ==================== LIFECYCLE ====================

    @Override
    public void update(double deltaTime) {
        if (!initialized) {
            return;
        }

        // Check watchdog
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHeartbeat > WATCHDOG_TIMEOUT_MS) {
            System.err.println("RCCarAdapter: Watchdog timeout for Agent " + agentId);
            handleConnectionLoss();
            return;
        }

        // Send periodic heartbeat
        if (currentTime - lastCommandTime > 500) {
            sendCommand("PING");
        }

        // Request status update
        if (currentTime - lastStatusUpdate > 100) {  // 10 Hz
            requestStatus();
        }
    }

    @Override
    public void shutdown() {
        if (connected) {
            sendCommand("SHUTDOWN");
            closeConnection();
        }

        initialized = false;
        connected = false;

        System.out.println("RCCarAdapter: Shutdown for Agent " + agentId);
    }

    @Override
    public boolean reset() {
        if (!connected) {
            return false;
        }

        sendCommand("RESET");
        velocity = new Vector2D(0, 0);
        lastError = null;

        System.out.println("RCCarAdapter: Reset for Agent " + agentId);
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

    // ==================== COMMUNICATION METHODS ====================

    /**
     * Open connection to RC car
     * NOTE: Actual implementation would use serial port libraries
     */
    private boolean openConnection() {
        // TODO: Implement actual serial port opening
        // Example using libraries like jSerialComm or RXTX:
        // serialPort = SerialPort.getCommPort(portName);
        // serialPort.setBaudRate(config.baudRate);
        // return serialPort.openPort();

        System.out.println("RCCarAdapter: Opening connection to " + portName +
                          " (SIMULATED - requires hardware)");

        // For this template, simulate connection
        connected = true;
        return true;
    }

    /**
     * Close connection to RC car
     */
    private void closeConnection() {
        // TODO: Implement actual serial port closing
        // if (serialPort != null) {
        //     serialPort.closePort();
        // }

        connected = false;
        serialPort = null;
    }

    /**
     * Send command to RC car
     */
    private boolean sendCommand(String command) {
        if (!connected) {
            return false;
        }

        // TODO: Implement actual command sending
        // Example:
        // byte[] bytes = (command + "\n").getBytes();
        // return serialPort.writeBytes(bytes, bytes.length) >= 0;

        System.out.println("RCCarAdapter: Sending command: " + command + " (SIMULATED)");

        lastCommandTime = System.currentTimeMillis();
        lastHeartbeat = System.currentTimeMillis();

        return true;
    }

    /**
     * Wait for response from RC car
     */
    private String waitForResponse(long timeoutMs) {
        // TODO: Implement actual response reading
        // Example:
        // long startTime = System.currentTimeMillis();
        // while (System.currentTimeMillis() - startTime < timeoutMs) {
        //     if (serialPort.bytesAvailable() > 0) {
        //         byte[] buffer = new byte[serialPort.bytesAvailable()];
        //         serialPort.readBytes(buffer, buffer.length);
        //         return new String(buffer);
        //     }
        // }

        // Simulated response
        return "OK";
    }

    /**
     * Request status update from RC car
     */
    private void requestStatus() {
        if (!connected) {
            return;
        }

        sendCommand("STATUS");

        // TODO: Parse response and update state
        // Response format: "STATUS:x,y,vx,vy,heading,battery"
        // Example parsing:
        // String response = waitForResponse(100);
        // if (response != null && response.startsWith("STATUS:")) {
        //     String[] parts = response.substring(7).split(",");
        //     position.x = Double.parseDouble(parts[0]);
        //     position.y = Double.parseDouble(parts[1]);
        //     velocity.x = Double.parseDouble(parts[2]);
        //     velocity.y = Double.parseDouble(parts[3]);
        //     heading = Double.parseDouble(parts[4]);
        //     batteryLevel = Double.parseDouble(parts[5]);
        // }

        lastStatusUpdate = System.currentTimeMillis();
    }

    /**
     * Handle connection loss
     */
    private void handleConnectionLoss() {
        connected = false;
        lastError = "Connection lost";

        // Attempt reconnection
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            System.out.println("RCCarAdapter: Attempting reconnection (" +
                             reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS + ")");

            reconnectAttempts++;

            if (openConnection()) {
                System.out.println("RCCarAdapter: Reconnected successfully");
                connected = true;
                reconnectAttempts = 0;
                lastHeartbeat = System.currentTimeMillis();
            }
        } else {
            System.err.println("RCCarAdapter: Max reconnection attempts reached");
            initialized = false;
        }
    }

    @Override
    public String toString() {
        return String.format("RCCarAdapter[agent=%d, port=%s, connected=%s, pos=%s]",
            agentId, portName, connected, position);
    }
}
