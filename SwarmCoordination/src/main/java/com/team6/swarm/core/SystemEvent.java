/**
 * SYSTEMEVENT CLASS - System Event Notifications
 *
 * PURPOSE:
 * - Represents notable events in the swarm system for display and logging
 * - Provides real-time feedback to users about system operations
 * - Enables event logging, debugging, and system monitoring
 *
 * EVENT CATEGORIES:
 * 1. Agent Events - Agent created, destroyed, state changes
 * 2. System Events - Simulation started/stopped, configuration changes
 * 3. Performance Events - Performance warnings, errors
 * 4. Task Events - Task assignments, completions, failures
 * 5. Communication Events - Communication issues, network changes
 *
 * SEVERITY LEVELS:
 * - INFO: Normal operational events (agent spawned, task completed)
 * - WARNING: Potential issues (low battery, approaching boundary)
 * - ERROR: Actual problems (agent failed, collision detected)
 * - DEBUG: Detailed diagnostic information (for development)
 *
 * CORE FIELDS:
 * - eventType: Category of event (e.g., "AGENT_CREATED", "SYSTEM_STARTED")
 * - severity: Importance level (INFO, WARNING, ERROR, DEBUG)
 * - message: Human-readable description
 * - agentId: Related agent ID (null for system-wide events)
 * - timestamp: When event occurred
 * - metadata: Additional event-specific data
 *
 * USAGE EXAMPLES:
 * - SystemEvent.info("AGENT_CREATED", agentId, "Agent spawned at (100, 200)")
 * - SystemEvent.warning("BATTERY_LOW", agentId, "Battery at 15%")
 * - SystemEvent.error("COLLISION", agentId, "Collision detected with agent " + otherId)
 *
 * INTEGRATION:
 * - Published to EventBus for real-time notification
 * - Consumed by UI for event log display
 * - Stored in system logs for debugging
 * - Used for system health monitoring
 *
 * Week 4: User Interface Integration
 * Purpose: Provide users with real-time system status and notifications
 */
package com.team6.swarm.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SystemEvent {
    private final String eventType;
    private final String agentId;
    private final String message;
    private final long timestamp;
    private final Severity severity;
    private final Map<String, Object> metadata;

    /**
     * Severity levels for system events
     */
    public enum Severity {
        DEBUG,    // Detailed diagnostic information
        INFO,     // Normal operational events
        WARNING,  // Potential issues
        ERROR     // Actual problems
    }

    /**
     * Creates a new SystemEvent
     *
     * @param eventType The type of event (e.g., "AGENT_CREATED")
     * @param agentId The agent ID if applicable, null for system-wide events
     * @param message Human-readable description
     * @param severity The severity level
     */
    public SystemEvent(String eventType, String agentId, String message, Severity severity) {
        this.eventType = Objects.requireNonNull(eventType, "Event type cannot be null");
        this.agentId = agentId;
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }

    // Getters
    public String getEventType() {
        return eventType;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    /**
     * Add metadata to the event
     */
    public SystemEvent addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this; // Allow chaining
    }

    /**
     * Get metadata value by key
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Check if this event is related to a specific agent
     */
    public boolean hasAgent() {
        return agentId != null && !agentId.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SystemEvent{")
          .append("severity=").append(severity)
          .append(", type='").append(eventType).append('\'')
          .append(", message='").append(message).append('\'');

        if (hasAgent()) {
            sb.append(", agentId='").append(agentId).append('\'');
        }

        sb.append(", timestamp=").append(timestamp);

        if (!metadata.isEmpty()) {
            sb.append(", metadata=").append(metadata);
        }

        sb.append('}');
        return sb.toString();
    }

    // ====== Factory Methods for Common Events ======

    /**
     * Create an INFO level event
     */
    public static SystemEvent info(String eventType, String agentId, String message) {
        return new SystemEvent(eventType, agentId, message, Severity.INFO);
    }

    /**
     * Create an INFO level event for system-wide events
     */
    public static SystemEvent info(String eventType, String message) {
        return new SystemEvent(eventType, null, message, Severity.INFO);
    }

    /**
     * Create a WARNING level event
     */
    public static SystemEvent warning(String eventType, String agentId, String message) {
        return new SystemEvent(eventType, agentId, message, Severity.WARNING);
    }

    /**
     * Create a WARNING level event for system-wide events
     */
    public static SystemEvent warning(String eventType, String message) {
        return new SystemEvent(eventType, null, message, Severity.WARNING);
    }

    /**
     * Create an ERROR level event
     */
    public static SystemEvent error(String eventType, String agentId, String message) {
        return new SystemEvent(eventType, agentId, message, Severity.ERROR);
    }

    /**
     * Create an ERROR level event for system-wide events
     */
    public static SystemEvent error(String eventType, String message) {
        return new SystemEvent(eventType, null, message, Severity.ERROR);
    }

    /**
     * Create a DEBUG level event
     */
    public static SystemEvent debug(String eventType, String agentId, String message) {
        return new SystemEvent(eventType, agentId, message, Severity.DEBUG);
    }

    /**
     * Create a DEBUG level event for system-wide events
     */
    public static SystemEvent debug(String eventType, String message) {
        return new SystemEvent(eventType, null, message, Severity.DEBUG);
    }

    // ====== Predefined Event Type Constants ======

    public static final String AGENT_CREATED = "AGENT_CREATED";
    public static final String AGENT_DESTROYED = "AGENT_DESTROYED";
    public static final String AGENT_STATE_CHANGED = "AGENT_STATE_CHANGED";
    public static final String AGENT_FAILED = "AGENT_FAILED";

    public static final String SYSTEM_STARTED = "SYSTEM_STARTED";
    public static final String SYSTEM_STOPPED = "SYSTEM_STOPPED";
    public static final String SYSTEM_RESET = "SYSTEM_RESET";
    public static final String SYSTEM_CONFIGURED = "SYSTEM_CONFIGURED";

    public static final String TASK_ASSIGNED = "TASK_ASSIGNED";
    public static final String TASK_COMPLETED = "TASK_COMPLETED";
    public static final String TASK_FAILED = "TASK_FAILED";

    public static final String COLLISION_DETECTED = "COLLISION_DETECTED";
    public static final String BOUNDARY_REACHED = "BOUNDARY_REACHED";
    public static final String COMMUNICATION_FAILED = "COMMUNICATION_FAILED";

    public static final String PERFORMANCE_WARNING = "PERFORMANCE_WARNING";
    public static final String MEMORY_WARNING = "MEMORY_WARNING";
}
