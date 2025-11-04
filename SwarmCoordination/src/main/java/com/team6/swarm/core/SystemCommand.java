package com.team6.swarm.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Standardized format for commands sent from Anthony's User Interface to the core system.
 * Encapsulates user actions and converts UI interactions into system operations.
 *
 * Week 4: User Interface Integration
 * Purpose: Bridge between UI events and core system operations
 *
 * Integration: Anthony's UI → SystemCommand → SystemController
 *
 * @author Team 6 - Sanidhaya (Core System)
 */
public class SystemCommand {
    private final CommandType type;
    private final String targetAgentId;
    private final Map<String, Object> parameters;
    private final long timestamp;
    private final String commandId;

    /**
     * Creates a new system command with specified type and parameters
     *
     * @param type The type of command to execute
     * @param targetAgentId Optional agent ID if command targets specific agent (can be null)
     * @param parameters Additional parameters for the command
     */
    public SystemCommand(CommandType type, String targetAgentId, Map<String, Object> parameters) {
        this.type = Objects.requireNonNull(type, "CommandType cannot be null");
        this.targetAgentId = targetAgentId;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.commandId = generateCommandId();
    }

    /**
     * Creates a system command without target agent (for global commands)
     *
     * @param type The type of command to execute
     * @param parameters Additional parameters for the command
     */
    public SystemCommand(CommandType type, Map<String, Object> parameters) {
        this(type, null, parameters);
    }

    /**
     * Creates a simple system command without parameters
     *
     * @param type The type of command to execute
     */
    public SystemCommand(CommandType type) {
        this(type, null, new HashMap<>());
    }

    /**
     * Generate unique command ID for tracking
     */
    private String generateCommandId() {
        return type.name() + "_" + timestamp + "_" + (int)(Math.random() * 10000);
    }

    // Getters
    public CommandType getType() {
        return type;
    }

    public String getTargetAgentId() {
        return targetAgentId;
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCommandId() {
        return commandId;
    }

    /**
     * Check if this command targets a specific agent
     */
    public boolean hasTargetAgent() {
        return targetAgentId != null && !targetAgentId.isEmpty();
    }

    /**
     * Get a parameter value by key
     *
     * @param key The parameter key
     * @return The parameter value, or null if not found
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * Get a parameter with type casting
     *
     * @param key The parameter key
     * @param type The expected type
     * @return The parameter value cast to type T
     * @throws ClassCastException if parameter cannot be cast to type
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * Get a parameter with default value if not present
     *
     * @param key The parameter key
     * @param defaultValue The default value to return if key not found
     * @return The parameter value or default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameterOrDefault(String key, T defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Check if a parameter exists
     */
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    /**
     * Get the number of parameters
     */
    public int getParameterCount() {
        return parameters.size();
    }

    @Override
    public String toString() {
        return "SystemCommand{" +
                "type=" + type +
                ", targetAgentId='" + targetAgentId + '\'' +
                ", parameters=" + parameters +
                ", timestamp=" + timestamp +
                ", commandId='" + commandId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemCommand that = (SystemCommand) o;
        return Objects.equals(commandId, that.commandId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId);
    }

    // ====== Factory Methods for Common Commands ======

    /**
     * Create a SPAWN_AGENT command at specific position
     */
    public static SystemCommand spawnAgent(double x, double y) {
        Map<String, Object> params = new HashMap<>();
        params.put("x", x);
        params.put("y", y);
        return new SystemCommand(CommandType.SPAWN_AGENT, params);
    }

    /**
     * Create a SPAWN_AGENT command with Point2D
     */
    public static SystemCommand spawnAgent(Point2D position) {
        Map<String, Object> params = new HashMap<>();
        params.put("position", position);
        params.put("x", position.x);
        params.put("y", position.y);
        return new SystemCommand(CommandType.SPAWN_AGENT, params);
    }

    /**
     * Create a REMOVE_AGENT command for specific agent
     */
    public static SystemCommand removeAgent(String agentId) {
        return new SystemCommand(CommandType.REMOVE_AGENT, agentId, new HashMap<>());
    }

    /**
     * Create a SET_BOUNDARIES command
     */
    public static SystemCommand setBoundaries(double width, double height) {
        Map<String, Object> params = new HashMap<>();
        params.put("width", width);
        params.put("height", height);
        return new SystemCommand(CommandType.SET_BOUNDARIES, params);
    }

    /**
     * Create a START_SIMULATION command
     */
    public static SystemCommand startSimulation() {
        return new SystemCommand(CommandType.START_SIMULATION);
    }

    /**
     * Create a STOP_SIMULATION command
     */
    public static SystemCommand stopSimulation() {
        return new SystemCommand(CommandType.STOP_SIMULATION);
    }

    /**
     * Create a RESET_SYSTEM command
     */
    public static SystemCommand resetSystem() {
        return new SystemCommand(CommandType.RESET_SYSTEM);
    }

    /**
     * Create a SET_TARGET command for specific agent
     */
    public static SystemCommand setTarget(String agentId, Point2D target) {
        Map<String, Object> params = new HashMap<>();
        params.put("target", target);
        params.put("targetX", target.x);
        params.put("targetY", target.y);
        return new SystemCommand(CommandType.SET_TARGET, agentId, params);
    }

    /**
     * Create a SET_SPEED command to change simulation speed
     */
    public static SystemCommand setSpeed(double timeScale) {
        Map<String, Object> params = new HashMap<>();
        params.put("timeScale", timeScale);
        return new SystemCommand(CommandType.SET_SPEED, params);
    }
}
