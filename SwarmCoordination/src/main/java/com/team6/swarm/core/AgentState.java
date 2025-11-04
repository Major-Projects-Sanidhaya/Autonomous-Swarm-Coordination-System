/**
 * AGENTSTATE CLASS - Agent Data Container
 *
 * PURPOSE:
 * - Stores complete state information for individual agents
 * - Central data structure for agent properties and status
 * - Used for state synchronization across system components
 *
 * CORE DATA CATEGORIES:
 * 1. IDENTITY: agentId, agentName - unique identification
 * 2. PHYSICAL STATE: position, velocity, heading - spatial information
 * 3. CAPABILITIES: maxSpeed, maxTurnRate, communicationRange - agent limits
 * 4. STATUS: status enum, batteryLevel, lastUpdateTime - operational info
 *
 * LOGIC:
 * - Constructor initializes all fields with safe defaults
 * - Public fields allow direct access for performance
 * - State updated by Agent class during simulation loop
 * - Read by other components for decision making
 *
 * EXPECTED VALUES:
 * - position: Point2D(0-800, 0-600) within world bounds
 * - velocity: Vector2D with magnitude <= maxSpeed
 * - batteryLevel: 0.0 (empty) to 1.0 (full)
 * - maxSpeed: typically 50.0 units/second
 * - communicationRange: typically 100.0 units
 *
 * USAGE EXAMPLES:
 * - AgentState state = new AgentState(); // Creates default state
 * - if (state.batteryLevel < 0.2) { triggerLowBatteryMode(); }
 * - double distance = state.position.distanceTo(targetPos);
 *
 * THREAD SAFETY:
 * - Not thread-safe by design for performance
 * - Access should be synchronized by calling code if needed
 */
package com.team6.swarm.core;

public class AgentState {
    // Identity
    public int agentId;
    public String agentName;
    
    // Physical state
    public Point2D position;
    public Vector2D velocity;
    public double heading;  // radians
    
    // Capabilities
    public double maxSpeed;
    public double maxTurnRate;
    public double communicationRange;
    
    // Status
    public AgentStatus status;
    public double batteryLevel;  // 0.0 to 1.0
    public long lastUpdateTime;
    
    // Constructor
    public AgentState() {
        // Initialize with defaults
        this.position = new Point2D(0, 0);
        this.velocity = new Vector2D(0, 0);
        this.heading = 0.0;
        this.maxSpeed = 50.0;
        this.maxTurnRate = 1.5;
        this.communicationRange = 100.0;
        this.status = AgentStatus.ACTIVE;
        this.batteryLevel = 1.0;
        this.lastUpdateTime = System.currentTimeMillis();
    }
}