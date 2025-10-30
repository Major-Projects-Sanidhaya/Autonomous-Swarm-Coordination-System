/**
 * SYSTEMCOMMAND CLASS - User Command to System
 *
 * PURPOSE:
 * - Encapsulates user commands from UI to system components
 * - Provides structured way to send instructions to agents
 * - Enables command validation and logging
 * - Supports undo/redo functionality (future enhancement)
 *
 * COMMAND FLOW:
 * 1. User interacts with UI (button click, parameter change, etc.)
 * 2. ControlPanel creates SystemCommand with appropriate type and parameters
 * 3. SystemController validates and dispatches command
 * 4. EventBus publishes command to relevant components
 * 5. Components execute command and publish results
 *
 * COMMAND TYPES:
 * - SPAWN_AGENT: Create new agent at specified position
 * - REMOVE_AGENT: Remove agent from system
 * - SET_BOUNDARIES: Define world boundaries
 * - START_SIMULATION: Begin simulation loop
 * - STOP_SIMULATION: Pause simulation
 * - PLACE_WAYPOINT: Add navigation waypoint
 * - CLEAR_WAYPOINTS: Remove all waypoints
 * - SET_FORMATION: Change swarm formation
 * - INITIATE_VOTE: Start voting process
 * - EMERGENCY_STOP: Immediate halt all agents
 *
 * PARAMETER EXAMPLES:
 * 
 * SPAWN_AGENT:
 * - position: Point2D(100, 200)
 * - maxSpeed: 50.0
 * - communicationRange: 100.0
 * - agentName: "Agent_5"
 *
 * PLACE_WAYPOINT:
 * - position: Point2D(500, 300)
 * - priority: 1
 * - radius: 20.0
 *
 * SET_FORMATION:
 * - formationType: "LINE" | "CIRCLE" | "V_FORMATION"
 * - spacing: 50.0
 * - leaderAgentId: 1
 *
 * VALIDATION:
 * - Checks required parameters present
 * - Validates parameter types and ranges
 * - Ensures command makes sense in current context
 *
 * USAGE EXAMPLE:
 * SystemCommand cmd = new SystemCommand();
 * cmd.type = CommandType.SPAWN_AGENT;
 * cmd.parameters.put("position", new Point2D(100, 100));
 * cmd.parameters.put("maxSpeed", 50.0);
 * cmd.timestamp = System.currentTimeMillis();
 * systemController.executeCommand(cmd);
 *
 * INTEGRATION POINTS:
 * - Created by: ControlPanel, MissionPanel, WaypointManager
 * - Validated by: SystemController
 * - Executed by: AgentManager (Sanidhaya), FlockingController (Lauren)
 * - Logged by: PerformanceMonitor
 */
package com.team6.swarm.ui;

import com.team6.swarm.core.Point2D;
import java.util.HashMap;
import java.util.Map;

public class SystemCommand {
    // Command identification
    public CommandType type;
    public String commandId;
    
    // Command data
    public Map<String, Object> parameters;
    public long timestamp;
    
    // Command metadata
    public String sourceComponent;  // Which UI component created this
    public int priority;  // 0=low, 1=normal, 2=high, 3=critical
    public boolean requiresConfirmation;  // Should user confirm before execution?
    
    // Execution tracking
    public boolean executed;
    public boolean successful;
    public String executionResult;
    public long executionTime;
    
    /**
     * Default constructor
     */
    public SystemCommand() {
        this.parameters = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.commandId = generateCommandId();
        this.sourceComponent = "UI";
        this.priority = 1;  // Normal priority
        this.requiresConfirmation = false;
        this.executed = false;
        this.successful = false;
    }
    
    /**
     * Constructor with command type
     */
    public SystemCommand(CommandType type) {
        this();
        this.type = type;
    }
    
    /**
     * Constructor with type and parameters
     */
    public SystemCommand(CommandType type, Map<String, Object> parameters) {
        this();
        this.type = type;
        this.parameters = new HashMap<>(parameters);
    }
    
    /**
     * Generate unique command ID
     */
    private String generateCommandId() {
        return "cmd_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 1000);
    }
    
    /**
     * Validate command has required parameters
     */
    public boolean validate() {
        if (type == null) {
            return false;
        }
        
        // Check type-specific required parameters
        switch (type) {
            case SPAWN_AGENT:
                return parameters.containsKey("position");
                
            case REMOVE_AGENT:
                return parameters.containsKey("agentId");
                
            case SET_BOUNDARIES:
                return parameters.containsKey("width") && 
                       parameters.containsKey("height");
                
            case PLACE_WAYPOINT:
                return parameters.containsKey("position");
                
            case SET_FORMATION:
                return parameters.containsKey("formationType");
                
            case INITIATE_VOTE:
                return parameters.containsKey("question") && 
                       parameters.containsKey("options");
                
            case START_SIMULATION:
            case STOP_SIMULATION:
            case CLEAR_WAYPOINTS:
            case EMERGENCY_STOP:
                return true;  // No required parameters
                
            default:
                return true;
        }
    }
    
    /**
     * Add parameter to command
     */
    public SystemCommand addParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;  // Allow chaining
    }
    
    /**
     * Get parameter with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Get parameter with default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, T defaultValue) {
        Object value = parameters.get(key);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Check if parameter exists
     */
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }
    
    /**
     * Mark command as executed
     */
    public void markExecuted(boolean successful, String result) {
        this.executed = true;
        this.successful = successful;
        this.executionResult = result;
        this.executionTime = System.currentTimeMillis();
    }
    
    /**
     * Get execution duration in milliseconds
     */
    public long getExecutionDuration() {
        if (executed) {
            return executionTime - timestamp;
        }
        return 0;
    }
    
    /**
     * Check if command is critical (requires immediate execution)
     */
    public boolean isCritical() {
        return priority >= 3 || type == CommandType.EMERGENCY_STOP;
    }
    
    /**
     * Get command description for logging
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString());
        
        if (!parameters.isEmpty()) {
            sb.append(" [");
            boolean first = true;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * Get command summary for UI display
     */
    public String getSummary() {
        String status = executed ? (successful ? "✓" : "✗") : "⋯";
        return String.format("%s %s", status, getDescription());
    }
    
    /**
     * Clone command (useful for retry or undo)
     */
    public SystemCommand clone() {
        SystemCommand clone = new SystemCommand(this.type);
        clone.parameters = new HashMap<>(this.parameters);
        clone.sourceComponent = this.sourceComponent;
        clone.priority = this.priority;
        clone.requiresConfirmation = this.requiresConfirmation;
        return clone;
    }
    
    /**
     * Create spawn agent command (convenience method)
     */
    public static SystemCommand spawnAgent(Point2D position, double maxSpeed) {
        SystemCommand cmd = new SystemCommand(CommandType.SPAWN_AGENT);
        cmd.addParameter("position", position);
        cmd.addParameter("maxSpeed", maxSpeed);
        return cmd;
    }
    
    /**
     * Create remove agent command (convenience method)
     */
    public static SystemCommand removeAgent(int agentId) {
        SystemCommand cmd = new SystemCommand(CommandType.REMOVE_AGENT);
        cmd.addParameter("agentId", agentId);
        return cmd;
    }
    
    /**
     * Create place waypoint command (convenience method)
     */
    public static SystemCommand placeWaypoint(Point2D position) {
        SystemCommand cmd = new SystemCommand(CommandType.PLACE_WAYPOINT);
        cmd.addParameter("position", position);
        return cmd;
    }
    
    /**
     * Create emergency stop command (convenience method)
     */
    public static SystemCommand emergencyStop() {
        SystemCommand cmd = new SystemCommand(CommandType.EMERGENCY_STOP);
        cmd.priority = 3;  // Critical priority
        return cmd;
    }
    
    @Override
    public String toString() {
        return String.format("SystemCommand{id=%s, type=%s, params=%d, executed=%s, successful=%s}", 
                           commandId, type, parameters.size(), executed, successful);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SystemCommand that = (SystemCommand) obj;
        return commandId.equals(that.commandId);
    }
    
    @Override
    public int hashCode() {
        return commandId.hashCode();
    }
}
