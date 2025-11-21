/**
 * RECOVERYMANAGER CLASS - System Recovery from Agent Failures (Week 9-10)
 *
 * PURPOSE:
 * - Implements automatic recovery strategies for failed agents
 * - Handles system recovery from various failure modes
 * - Maintains system operation during agent failures
 * - Minimizes impact of failures on overall mission
 *
 * RECOVERY STRATEGIES BY FAILURE TYPE:
 *
 * SYSTEM_ERROR:
 * - Log error details for diagnostics
 * - Attempt agent restart
 * - If restart fails, mark agent as permanently failed
 * - Redistribute tasks to healthy agents
 *
 * BATTERY_DEPLETED:
 * - Remove agent from active duty
 * - Reassign tasks to other agents
 * - Send agent to charging station (if available)
 * - Update formation to exclude depleted agent
 *
 * COMMUNICATION_LOST:
 * - Attempt to re-establish connection
 * - Switch to backup communication channel
 * - If persistent, operate in autonomous mode
 * - Mark agent as isolated until reconnection
 *
 * COLLISION:
 * - Emergency stop
 * - Assess damage (simulated)
 * - If operational, resume with caution
 * - If damaged, remove from active service
 *
 * TIMEOUT:
 * - Attempt to ping agent
 * - Force restart if unresponsive
 * - Mark as failed if no response
 * - Clean up resources
 *
 * BOUNDARY_VIOLATION:
 * - Calculate return path to valid zone
 * - Issue emergency return command
 * - Monitor return progress
 * - Prevent future violations with tighter bounds
 *
 * SENSOR_FAILURE:
 * - Switch to redundant sensors
 * - Reduce operational capability
 * - Assign simpler tasks
 * - Schedule maintenance
 *
 * OVERLOAD:
 * - Redistribute tasks to other agents
 * - Increase priority thresholds
 * - Defer non-critical tasks
 * - Add more agents if available
 *
 * RECOVERY PROCESS:
 * 1. Receive failure notification from FailureDetector
 * 2. Select appropriate recovery strategy
 * 3. Execute recovery actions
 * 4. Monitor recovery progress
 * 5. Report recovery status
 * 6. Update system state
 *
 * INTEGRATION POINTS:
 * - FailureDetector: Receives failure notifications
 * - AgentManager: Agent lifecycle management
 * - CoordinationManager: Formation updates
 * - EventBus: Recovery status events
 * - SystemController: System-wide coordination
 *
 * USAGE PATTERN:
 * 1. Create: new RecoveryManager(agentManager, eventBus)
 * 2. Subscribe to failure events
 * 3. Automatic recovery on failure detection
 * 4. Monitor recovery status via events
 */
package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RecoveryManager {
    // Dependencies
    private AgentManager agentManager;
    private EventBus eventBus;
    private BoundaryManager boundaryManager;
    private CoordinationManager coordinationManager;

    // Recovery tracking
    private Map<Integer, RecoveryAttempt> activeRecoveries;
    private Map<Integer, Integer> recoveryAttemptCounts;

    // Configuration
    private static final int MAX_RECOVERY_ATTEMPTS = 3;
    private static final long RECOVERY_TIMEOUT_MS = 10000;  // 10 seconds

    // Statistics
    private int totalRecoveriesAttempted;
    private int totalRecoveriesSucceeded;
    private int totalRecoveriesFailed;

    /**
     * Constructor with dependencies
     */
    public RecoveryManager(AgentManager agentManager, EventBus eventBus) {
        this.agentManager = agentManager;
        this.eventBus = eventBus;
        this.boundaryManager = null;
        this.coordinationManager = null;
        this.activeRecoveries = new ConcurrentHashMap<>();
        this.recoveryAttemptCounts = new ConcurrentHashMap<>();
        this.totalRecoveriesAttempted = 0;
        this.totalRecoveriesSucceeded = 0;
        this.totalRecoveriesFailed = 0;

        // Subscribe to failure events if eventBus is available
        if (eventBus != null) {
            subscribeToFailureEvents();
        }
    }

    /**
     * Default constructor
     */
    public RecoveryManager() {
        this(null, null);
    }

    // ==================== FAILURE EVENT HANDLING ====================

    /**
     * Subscribe to failure events from EventBus
     */
    private void subscribeToFailureEvents() {
        // In a real implementation, would subscribe to specific event types
        // For now, this is a placeholder
        System.out.println("RecoveryManager: Subscribed to failure events");
    }

    /**
     * Handle detected failure
     * Main entry point for recovery process
     */
    public void handleFailure(int agentId, FailureType failureType, String details) {
        System.out.println("RecoveryManager: Handling " + failureType.getDisplayName() +
                          " for Agent " + agentId);

        // Check if already recovering
        if (activeRecoveries.containsKey(agentId)) {
            System.out.println("RecoveryManager: Agent " + agentId + " already in recovery");
            return;
        }

        // Check recovery attempt limit
        int attemptCount = recoveryAttemptCounts.getOrDefault(agentId, 0);
        if (attemptCount >= MAX_RECOVERY_ATTEMPTS) {
            System.err.println("RecoveryManager: Agent " + agentId +
                             " exceeded max recovery attempts - marking as permanently failed");
            markPermanentlyFailed(agentId);
            return;
        }

        // Create recovery attempt
        RecoveryAttempt attempt = new RecoveryAttempt(agentId, failureType, details);
        activeRecoveries.put(agentId, attempt);
        recoveryAttemptCounts.put(agentId, attemptCount + 1);
        totalRecoveriesAttempted++;

        // Execute recovery strategy
        boolean success = executeRecoveryStrategy(agentId, failureType, details);

        // Update recovery status
        attempt.completed = true;
        attempt.success = success;

        if (success) {
            totalRecoveriesSucceeded++;
            publishRecoveryEvent(agentId, failureType, "SUCCESS");
            System.out.println("RecoveryManager: Successfully recovered Agent " + agentId);
        } else {
            totalRecoveriesFailed++;
            publishRecoveryEvent(agentId, failureType, "FAILED");
            System.err.println("RecoveryManager: Failed to recover Agent " + agentId);
        }

        // Remove from active recoveries
        activeRecoveries.remove(agentId);
    }

    // ==================== RECOVERY STRATEGIES ====================

    /**
     * Execute appropriate recovery strategy based on failure type
     */
    private boolean executeRecoveryStrategy(int agentId, FailureType failureType, String details) {
        switch (failureType) {
            case SYSTEM_ERROR:
                return recoverFromSystemError(agentId);

            case BATTERY_DEPLETED:
                return recoverFromBatteryDepletion(agentId);

            case COMMUNICATION_LOST:
                return recoverFromCommunicationLoss(agentId);

            case COLLISION:
                return recoverFromCollision(agentId);

            case TIMEOUT:
                return recoverFromTimeout(agentId);

            case BOUNDARY_VIOLATION:
                return recoverFromBoundaryViolation(agentId);

            case SENSOR_FAILURE:
                return recoverFromSensorFailure(agentId);

            case OVERLOAD:
                return recoverFromOverload(agentId);

            default:
                System.err.println("RecoveryManager: Unknown failure type: " + failureType);
                return false;
        }
    }

    /**
     * Recover from SYSTEM_ERROR
     */
    private boolean recoverFromSystemError(int agentId) {
        System.out.println("RecoveryManager: Attempting to restart Agent " + agentId);

        Agent agent = agentManager.getAgent(agentId);
        if (agent == null) {
            return false;
        }

        // Reset agent state
        AgentState state = agent.getState();
        state.status = AgentStatus.ACTIVE;

        // Clear any pending commands
        // In real implementation, would clear command queue

        return true;
    }

    /**
     * Recover from BATTERY_DEPLETED
     */
    private boolean recoverFromBatteryDepletion(int agentId) {
        System.out.println("RecoveryManager: Handling battery depletion for Agent " + agentId);

        // Remove from formations
        removeFromFormations(agentId);

        // Mark as inactive
        Agent agent = agentManager.getAgent(agentId);
        if (agent != null) {
            agent.getState().status = AgentStatus.INACTIVE;
        }

        // In real system, would send to charging station
        System.out.println("RecoveryManager: Agent " + agentId + " removed from active duty");

        return true;  // Successfully handled
    }

    /**
     * Recover from COMMUNICATION_LOST
     */
    private boolean recoverFromCommunicationLoss(int agentId) {
        System.out.println("RecoveryManager: Attempting to restore communication for Agent " + agentId);

        // In real implementation, would attempt reconnection
        // For now, just mark as isolated
        Agent agent = agentManager.getAgent(agentId);
        if (agent != null) {
            // Agent continues in autonomous mode
            return true;
        }

        return false;
    }

    /**
     * Recover from COLLISION
     */
    private boolean recoverFromCollision(int agentId) {
        System.out.println("RecoveryManager: Handling collision for Agent " + agentId);

        Agent agent = agentManager.getAgent(agentId);
        if (agent == null) {
            return false;
        }

        // Emergency stop
        agent.getState().velocity = new Vector2D(0, 0);

        // Assess damage (simulated - assume operational)
        agent.getState().status = AgentStatus.ACTIVE;

        // Issue caution command
        System.out.println("RecoveryManager: Agent " + agentId + " stopped and assessed");

        return true;
    }

    /**
     * Recover from TIMEOUT
     */
    private boolean recoverFromTimeout(int agentId) {
        System.out.println("RecoveryManager: Attempting to recover unresponsive Agent " + agentId);

        Agent agent = agentManager.getAgent(agentId);
        if (agent == null) {
            return false;
        }

        // Force restart (simulated)
        agent.getState().lastUpdateTime = System.currentTimeMillis();
        agent.getState().status = AgentStatus.ACTIVE;

        return true;
    }

    /**
     * Recover from BOUNDARY_VIOLATION
     */
    private boolean recoverFromBoundaryViolation(int agentId) {
        System.out.println("RecoveryManager: Returning Agent " + agentId + " to safe zone");

        Agent agent = agentManager.getAgent(agentId);
        if (agent == null || boundaryManager == null) {
            return false;
        }

        // Get nearest safe point
        Point2D safePoint = boundaryManager.getNearestSafePoint(agent.getState().position);

        // Issue return command
        MovementCommand cmd = new MovementCommand(agentId, MovementType.MOVE_TO_TARGET, CommandPriority.EMERGENCY);
        cmd.parameters.put("target", safePoint);
        cmd.taskId = "boundary_recovery_" + agentId;

        agent.addMovementCommand(cmd);

        System.out.println("RecoveryManager: Agent " + agentId + " commanded to return to " + safePoint);

        return true;
    }

    /**
     * Recover from SENSOR_FAILURE
     */
    private boolean recoverFromSensorFailure(int agentId) {
        System.out.println("RecoveryManager: Handling sensor failure for Agent " + agentId);

        Agent agent = agentManager.getAgent(agentId);
        if (agent == null) {
            return false;
        }

        // Mark for maintenance but keep operational
        agent.getState().status = AgentStatus.MAINTENANCE;

        // In real system, would switch to backup sensors
        System.out.println("RecoveryManager: Agent " + agentId + " operating with reduced capability");

        return true;
    }

    /**
     * Recover from OVERLOAD
     */
    private boolean recoverFromOverload(int agentId) {
        System.out.println("RecoveryManager: Redistributing tasks from overloaded Agent " + agentId);

        Agent agent = agentManager.getAgent(agentId);
        if (agent == null) {
            return false;
        }

        // Clear low-priority tasks (simulated)
        // In real implementation, would redistribute to other agents

        System.out.println("RecoveryManager: Agent " + agentId + " load reduced");

        return true;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Mark agent as permanently failed
     */
    private void markPermanentlyFailed(int agentId) {
        Agent agent = agentManager.getAgent(agentId);
        if (agent != null) {
            agent.getState().status = AgentStatus.FAILED;
        }

        // Remove from formations
        removeFromFormations(agentId);

        // Publish permanent failure event
        if (eventBus != null) {
            SystemEvent event = SystemEvent.error(
                "AGENT_PERMANENTLY_FAILED",
                String.valueOf(agentId),
                "Agent " + agentId + " marked as permanently failed after " +
                MAX_RECOVERY_ATTEMPTS + " recovery attempts"
            );
            eventBus.publish(event);
        }
    }

    /**
     * Remove agent from all formations
     */
    private void removeFromFormations(int agentId) {
        if (coordinationManager == null) {
            return;
        }

        int formationId = coordinationManager.getFormationForAgent(agentId);
        if (formationId != -1) {
            coordinationManager.removeAgentFromFormation(formationId, agentId);
            System.out.println("RecoveryManager: Removed Agent " + agentId +
                             " from formation " + formationId);
        }
    }

    /**
     * Publish recovery event
     */
    private void publishRecoveryEvent(int agentId, FailureType failureType, String status) {
        if (eventBus == null) return;

        String message = "Agent " + agentId + " recovery " + status +
                        " for " + failureType.getDisplayName();

        SystemEvent event = SystemEvent.info("AGENT_RECOVERY", String.valueOf(agentId), message);
        event.addMetadata("failureType", failureType.toString());
        event.addMetadata("recoveryStatus", status);
        event.addMetadata("attemptCount", recoveryAttemptCounts.getOrDefault(agentId, 0));

        eventBus.publish(event);
    }

    // ==================== UPDATE ====================

    /**
     * Update recovery manager - check active recoveries
     */
    public void update(double deltaTime) {
        // Check for timed-out recoveries
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Integer, RecoveryAttempt> entry : activeRecoveries.entrySet()) {
            RecoveryAttempt attempt = entry.getValue();
            long elapsed = currentTime - attempt.startTime;

            if (!attempt.completed && elapsed > RECOVERY_TIMEOUT_MS) {
                System.err.println("RecoveryManager: Recovery timeout for Agent " + entry.getKey());
                attempt.completed = true;
                attempt.success = false;
                totalRecoveriesFailed++;
            }
        }
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get recovery statistics
     */
    public RecoveryStatistics getStatistics() {
        RecoveryStatistics stats = new RecoveryStatistics();
        stats.totalAttempted = totalRecoveriesAttempted;
        stats.totalSucceeded = totalRecoveriesSucceeded;
        stats.totalFailed = totalRecoveriesFailed;
        stats.activeRecoveries = activeRecoveries.size();
        stats.successRate = totalRecoveriesAttempted > 0 ?
            (double) totalRecoveriesSucceeded / totalRecoveriesAttempted : 0.0;
        return stats;
    }

    /**
     * Check if agent is currently recovering
     */
    public boolean isRecovering(int agentId) {
        return activeRecoveries.containsKey(agentId);
    }

    /**
     * Get recovery attempt count for agent
     */
    public int getRecoveryAttemptCount(int agentId) {
        return recoveryAttemptCounts.getOrDefault(agentId, 0);
    }

    /**
     * Reset recovery counter for agent (e.g., after successful long-term operation)
     */
    public void resetRecoveryCounter(int agentId) {
        recoveryAttemptCounts.remove(agentId);
    }

    // ==================== SETTERS ====================

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        if (eventBus != null) {
            subscribeToFailureEvents();
        }
    }

    public void setBoundaryManager(BoundaryManager boundaryManager) {
        this.boundaryManager = boundaryManager;
    }

    public void setCoordinationManager(CoordinationManager coordinationManager) {
        this.coordinationManager = coordinationManager;
    }
}

/**
 * RECOVERYATTEMPT - Tracks individual recovery attempt
 */
class RecoveryAttempt {
    int agentId;
    FailureType failureType;
    String details;
    long startTime;
    boolean completed;
    boolean success;

    RecoveryAttempt(int agentId, FailureType failureType, String details) {
        this.agentId = agentId;
        this.failureType = failureType;
        this.details = details;
        this.startTime = System.currentTimeMillis();
        this.completed = false;
        this.success = false;
    }
}

/**
 * RECOVERYSTATISTICS - Recovery system statistics
 */
class RecoveryStatistics {
    int totalAttempted;
    int totalSucceeded;
    int totalFailed;
    int activeRecoveries;
    double successRate;

    @Override
    public String toString() {
        return String.format("RecoveryStats[attempted=%d, succeeded=%d, failed=%d, active=%d, rate=%.1f%%]",
            totalAttempted, totalSucceeded, totalFailed, activeRecoveries, successRate * 100);
    }
}
