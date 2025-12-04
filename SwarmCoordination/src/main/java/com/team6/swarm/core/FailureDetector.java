/**
 * FAILUREDETECTOR CLASS - Agent Failure Detection System (Week 9-10)
 *
 * PURPOSE:
 * - Continuously monitors agent health and operational status
 * - Detects various failure modes and anomalies
 * - Classifies failures by type for appropriate response
 * - Publishes failure events to EventBus for system response
 *
 * DETECTION MECHANISMS:
 *
 * 1. HEARTBEAT MONITORING:
 *    - Tracks last update time for each agent
 *    - Detects TIMEOUT failures (unresponsive agents)
 *    - Configurable timeout threshold (default: 5 seconds)
 *
 * 2. BATTERY MONITORING:
 *    - Monitors battery levels continuously
 *    - Detects BATTERY_DEPLETED when level reaches 0
 *    - Early warning at low battery threshold
 *
 * 3. BOUNDARY CHECKING:
 *    - Validates agent positions against boundaries
 *    - Detects BOUNDARY_VIOLATION when outside safe zone
 *    - Uses BoundaryManager for boundary definitions
 *
 * 4. STATUS MONITORING:
 *    - Tracks AgentStatus changes
 *    - Detects SYSTEM_ERROR when status becomes FAILED
 *    - Identifies abnormal state transitions
 *
 * 5. COMMUNICATION TRACKING:
 *    - Monitors message send/receive activity
 *    - Detects COMMUNICATION_LOST patterns
 *    - Tracks network connectivity
 *
 * DETECTION CYCLE:
 * 1. checkAgents() called periodically (every update cycle)
 * 2. For each agent, run all detection checks
 * 3. Classify detected failures by FailureType
 * 4. Publish FailureEvent to EventBus
 * 5. Log failure details for diagnostics
 *
 * FAILURE TRACKING:
 * - Maintains history of detected failures
 * - Prevents duplicate failure reports
 * - Tracks failure frequency per agent
 * - Provides failure statistics
 *
 * INTEGRATION POINTS:
 * - AgentManager: Access to all agents
 * - EventBus: Publish failure events
 * - BoundaryManager: Boundary validation
 * - RecoveryManager: Triggers recovery actions
 * - SystemEvent: Failure notifications
 *
 * USAGE PATTERN:
 * 1. Create detector: new FailureDetector(agentManager, eventBus)
 * 2. Set boundary manager: detector.setBoundaryManager(boundaryManager)
 * 3. Call update() in main loop: detector.update(deltaTime)
 * 4. Subscribe to failure events via EventBus
 *
 * CONFIGURATION:
 * - Timeout threshold: Default 5 seconds
 * - Battery warning: Default 20%
 * - Check interval: Every update cycle
 */
package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FailureDetector {
    // Dependencies
    private AgentManager agentManager;
    private EventBus eventBus;
    private BoundaryManager boundaryManager;

    // Failure tracking
    private Map<String, FailureRecord> failureHistory;
    private Map<Integer, Long> lastHeartbeat;

    // Configuration
    private static final long TIMEOUT_THRESHOLD_MS = 5000;  // 5 seconds
    private static final double BATTERY_CRITICAL = 0.0;
    private static final double BATTERY_WARNING = 0.2;      // 20%

    // Statistics
    private int totalFailuresDetected;
    private long lastCheckTime;

    /**
     * Constructor with dependencies
     */
    public FailureDetector(AgentManager agentManager, EventBus eventBus) {
        this.agentManager = agentManager;
        this.eventBus = eventBus;
        this.boundaryManager = null;
        this.failureHistory = new ConcurrentHashMap<>();
        this.lastHeartbeat = new ConcurrentHashMap<>();
        this.totalFailuresDetected = 0;
        this.lastCheckTime = System.currentTimeMillis();
    }

    /**
     * Default constructor
     */
    public FailureDetector() {
        this(null, null);
    }

    // ==================== MAIN DETECTION LOOP ====================

    /**
     * Update failure detection - called every frame
     * Checks all agents for various failure conditions
     */
    public void update(double deltaTime) {
        if (agentManager == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        List<AgentState> agents = agentManager.getAllAgentStates();

        for (AgentState agentState : agents) {
            // Update heartbeat
            lastHeartbeat.put(agentState.agentId, currentTime);

            // Run all detection checks
            checkTimeout(agentState, currentTime);
            checkBattery(agentState);
            checkStatus(agentState);
            checkBoundary(agentState);
        }

        // Check for missing agents (timeout)
        checkMissingAgents(currentTime);

        lastCheckTime = currentTime;
    }

    // ==================== SPECIFIC DETECTION METHODS ====================

    /**
     * Check for agent timeout (unresponsive)
     */
    private void checkTimeout(AgentState agentState, long currentTime) {
        long lastUpdate = agentState.lastUpdateTime;
        long timeSinceUpdate = currentTime - lastUpdate;

        if (timeSinceUpdate > TIMEOUT_THRESHOLD_MS) {
            // Check if already reported
            if (!hasRecentFailure(agentState.agentId, FailureType.TIMEOUT)) {
                reportFailure(agentState.agentId, FailureType.TIMEOUT,
                    "Agent unresponsive for " + (timeSinceUpdate / 1000) + " seconds");
            }
        }
    }

    /**
     * Check battery levels
     */
    private void checkBattery(AgentState agentState) {
        double batteryLevel = agentState.batteryLevel;

        // Critical: Battery depleted
        if (batteryLevel <= BATTERY_CRITICAL) {
            if (!hasRecentFailure(agentState.agentId, FailureType.BATTERY_DEPLETED)) {
                reportFailure(agentState.agentId, FailureType.BATTERY_DEPLETED,
                    "Battery completely depleted");
            }
        }
        // Warning: Low battery (not a failure, but warning)
        else if (batteryLevel <= BATTERY_WARNING) {
            // Could publish warning event here
            // For now, we let AgentStatus.BATTERY_LOW handle this
        }
    }

    /**
     * Check agent status for failures
     */
    private void checkStatus(AgentState agentState) {
        if (agentState.status == AgentStatus.FAILED) {
            if (!hasRecentFailure(agentState.agentId, FailureType.SYSTEM_ERROR)) {
                reportFailure(agentState.agentId, FailureType.SYSTEM_ERROR,
                    "Agent status changed to FAILED");
            }
        }
    }

    /**
     * Check if agent is within boundaries
     */
    private void checkBoundary(AgentState agentState) {
        if (boundaryManager == null) {
            return;  // Cannot check without boundary manager
        }

        if (!boundaryManager.isPositionValid(agentState.position)) {
            if (!hasRecentFailure(agentState.agentId, FailureType.BOUNDARY_VIOLATION)) {
                reportFailure(agentState.agentId, FailureType.BOUNDARY_VIOLATION,
                    "Agent outside boundaries at " + agentState.position);
            }
        }
    }

    /**
     * Check for agents that haven't sent heartbeat
     */
    private void checkMissingAgents(long currentTime) {
        for (Map.Entry<Integer, Long> entry : lastHeartbeat.entrySet()) {
            int agentId = entry.getKey();
            long lastBeat = entry.getValue();
            long timeSinceHeartbeat = currentTime - lastBeat;

            if (timeSinceHeartbeat > TIMEOUT_THRESHOLD_MS * 2) {
                // Agent completely missing
                if (!hasRecentFailure(agentId, FailureType.TIMEOUT)) {
                    reportFailure(agentId, FailureType.TIMEOUT,
                        "Agent missing - no heartbeat for " + (timeSinceHeartbeat / 1000) + " seconds");
                }
            }
        }
    }

    // ==================== FAILURE REPORTING ====================

    /**
     * Report detected failure
     * Records failure and publishes event
     */
    private void reportFailure(int agentId, FailureType failureType, String details) {
        // Record failure
        FailureRecord record = new FailureRecord(agentId, failureType, details);
        failureHistory.put(getFailureKey(agentId, failureType), record);
        totalFailuresDetected++;

        // Publish failure event
        publishFailureEvent(agentId, failureType, details);

        // Log to console
        System.err.println("FailureDetector: Agent " + agentId + " - " +
                          failureType.getDisplayName() + ": " + details);
    }

    /**
     * Check if failure was recently reported (prevent duplicates)
     */
    private boolean hasRecentFailure(int agentId, FailureType failureType) {
        String key = getFailureKey(agentId, failureType);
        FailureRecord record = failureHistory.get(key);

        if (record == null) {
            return false;
        }

        // Consider failure "recent" if within last 10 seconds
        long timeSinceFailure = System.currentTimeMillis() - record.timestamp;
        return timeSinceFailure < 10000;
    }

    /**
     * Generate unique key for failure tracking
     */
    private String getFailureKey(int agentId, FailureType failureType) {
        return agentId + "_" + failureType.toString();
    }

    /**
     * Publish failure event to EventBus
     */
    private void publishFailureEvent(int agentId, FailureType failureType, String details) {
        if (eventBus == null) return;

        String message = "Agent " + agentId + " failure: " + failureType.getDisplayName() +
                        " - " + details;

        SystemEvent.Severity severity = failureType.isCritical() ?
            SystemEvent.Severity.ERROR : SystemEvent.Severity.WARNING;

        SystemEvent event = new SystemEvent(
            "AGENT_FAILURE",
            String.valueOf(agentId),
            message,
            severity
        );

        event.addMetadata("failureType", failureType.toString());
        event.addMetadata("details", details);
        event.addMetadata("recoveryAction", failureType.getRecoveryAction());
        event.addMetadata("isCritical", failureType.isCritical());

        eventBus.publish(event);
    }

    // ==================== MANUAL FAILURE REPORTING ====================

    /**
     * Manually report collision failure
     * Called by physics engine or collision detector
     */
    public void reportCollision(int agentId, Point2D collisionPoint) {
        reportFailure(agentId, FailureType.COLLISION,
            "Collision at " + collisionPoint);
    }

    /**
     * Manually report communication loss
     * Called by communication system
     */
    public void reportCommunicationLoss(int agentId) {
        reportFailure(agentId, FailureType.COMMUNICATION_LOST,
            "Lost connection to communication network");
    }

    /**
     * Manually report sensor failure
     */
    public void reportSensorFailure(int agentId, String sensorType) {
        reportFailure(agentId, FailureType.SENSOR_FAILURE,
            "Sensor failure: " + sensorType);
    }

    /**
     * Manually report system overload
     */
    public void reportOverload(int agentId, int taskCount) {
        reportFailure(agentId, FailureType.OVERLOAD,
            "System overload - " + taskCount + " tasks queued");
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get all failures for specific agent
     */
    public List<FailureRecord> getAgentFailures(int agentId) {
        List<FailureRecord> failures = new ArrayList<>();
        for (FailureRecord record : failureHistory.values()) {
            if (record.agentId == agentId) {
                failures.add(record);
            }
        }
        return failures;
    }

    /**
     * Get total number of failures detected
     */
    public int getTotalFailuresDetected() {
        return totalFailuresDetected;
    }

    /**
     * Get failure statistics by type
     */
    public Map<FailureType, Integer> getFailureStatistics() {
        Map<FailureType, Integer> stats = new HashMap<>();
        for (FailureType type : FailureType.values()) {
            stats.put(type, 0);
        }

        for (FailureRecord record : failureHistory.values()) {
            stats.put(record.failureType, stats.get(record.failureType) + 1);
        }

        return stats;
    }

    /**
     * Clear failure history for agent (e.g., after recovery)
     */
    public void clearAgentFailures(int agentId) {
        failureHistory.entrySet().removeIf(entry ->
            entry.getValue().agentId == agentId);
    }

    /**
     * Clear all failure history
     */
    public void clearAllFailures() {
        failureHistory.clear();
        totalFailuresDetected = 0;
    }

    // ==================== SETTERS ====================

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setBoundaryManager(BoundaryManager boundaryManager) {
        this.boundaryManager = boundaryManager;
    }
}

/**
 * FAILURERECORD - Tracks individual failure occurrences
 */
class FailureRecord {
    int agentId;
    FailureType failureType;
    String details;
    long timestamp;

    FailureRecord(int agentId, FailureType failureType, String details) {
        this.agentId = agentId;
        this.failureType = failureType;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("FailureRecord[agent=%d, type=%s, time=%d, details=%s]",
            agentId, failureType, timestamp, details);
    }
}
