/**
 * FAILURETYPE ENUM - Categories of Agent Failures (Week 9-10)
 *
 * PURPOSE:
 * - Defines all possible types of agent failures in the swarm system
 * - Enables systematic classification of failure conditions
 * - Allows different recovery strategies based on failure type
 * - Provides diagnostic information for system monitoring
 *
 * FAILURE CATEGORIES:
 *
 * SYSTEM_ERROR:
 * - Internal software errors or exceptions
 * - Agent update loop crashes
 * - Memory allocation failures
 * - Unexpected state transitions
 * - Recovery: Restart agent, log error details
 *
 * BATTERY_DEPLETED:
 * - Battery level reaches zero
 * - Agent cannot continue operations
 * - Predictable failure with warning signs
 * - Recovery: Replace agent, send to charging station
 *
 * COMMUNICATION_LOST:
 * - Lost connection to communication network
 * - Cannot send/receive messages
 * - Agent isolated from swarm
 * - Recovery: Attempt reconnection, relocate agent
 *
 * COLLISION:
 * - Physical collision with obstacle or other agent
 * - Potential damage to hardware
 * - Immediate action required
 * - Recovery: Emergency stop, damage assessment
 *
 * TIMEOUT:
 * - Agent unresponsive for extended period
 * - Heartbeat signal missing
 * - Possible hang or infinite loop
 * - Recovery: Force restart, mark as failed
 *
 * BOUNDARY_VIOLATION:
 * - Agent exceeded operational boundaries
 * - Left designated safe zone
 * - Potential loss of agent
 * - Recovery: Return to boundary, emergency recall
 *
 * SENSOR_FAILURE:
 * - Sensor malfunction or invalid readings
 * - Cannot perceive environment correctly
 * - Degraded operational capability
 * - Recovery: Switch to backup sensors, limit operations
 *
 * OVERLOAD:
 * - Too many tasks assigned
 * - CPU/memory overload
 * - Performance degradation
 * - Recovery: Redistribute tasks, reduce load
 *
 * FAILURE SEVERITY LEVELS:
 *
 * CRITICAL (Immediate Action Required):
 * - COLLISION
 * - BOUNDARY_VIOLATION
 * - SYSTEM_ERROR
 *
 * WARNING (Degraded Operation):
 * - BATTERY_DEPLETED
 * - SENSOR_FAILURE
 * - COMMUNICATION_LOST
 *
 * RECOVERABLE (Automatic Recovery):
 * - TIMEOUT
 * - OVERLOAD
 *
 * USAGE PATTERN:
 * - FailureDetector identifies failure
 * - Classifies as FailureType
 * - RecoveryManager applies appropriate strategy
 * - SystemEvent published for monitoring
 *
 * INTEGRATION POINTS:
 * - Used by: FailureDetector for classification
 * - Used by: RecoveryManager for recovery strategy selection
 * - Used by: SystemEvent for failure reporting
 * - Used by: Anthony's UI for status display
 */
package com.team6.swarm.core;

public enum FailureType {
    /**
     * Internal system error or exception
     * Severity: CRITICAL
     */
    SYSTEM_ERROR,

    /**
     * Battery completely depleted
     * Severity: WARNING
     */
    BATTERY_DEPLETED,

    /**
     * Lost communication with network
     * Severity: WARNING
     */
    COMMUNICATION_LOST,

    /**
     * Physical collision detected
     * Severity: CRITICAL
     */
    COLLISION,

    /**
     * Agent unresponsive/timed out
     * Severity: RECOVERABLE
     */
    TIMEOUT,

    /**
     * Exceeded operational boundaries
     * Severity: CRITICAL
     */
    BOUNDARY_VIOLATION,

    /**
     * Sensor malfunction
     * Severity: WARNING
     */
    SENSOR_FAILURE,

    /**
     * System overload (too many tasks)
     * Severity: RECOVERABLE
     */
    OVERLOAD;

    /**
     * Get severity level of this failure type
     */
    public Severity getSeverity() {
        switch (this) {
            case COLLISION:
            case BOUNDARY_VIOLATION:
            case SYSTEM_ERROR:
                return Severity.CRITICAL;

            case BATTERY_DEPLETED:
            case SENSOR_FAILURE:
            case COMMUNICATION_LOST:
                return Severity.WARNING;

            case TIMEOUT:
            case OVERLOAD:
                return Severity.RECOVERABLE;

            default:
                return Severity.WARNING;
        }
    }

    /**
     * Check if failure requires immediate action
     */
    public boolean isCritical() {
        return getSeverity() == Severity.CRITICAL;
    }

    /**
     * Check if failure is automatically recoverable
     */
    public boolean isRecoverable() {
        return getSeverity() == Severity.RECOVERABLE;
    }

    /**
     * Get human-readable description
     */
    public String getDescription() {
        switch (this) {
            case SYSTEM_ERROR:
                return "Internal system error or software exception";
            case BATTERY_DEPLETED:
                return "Battery level reached zero";
            case COMMUNICATION_LOST:
                return "Lost connection to communication network";
            case COLLISION:
                return "Physical collision with obstacle or agent";
            case TIMEOUT:
                return "Agent unresponsive or timed out";
            case BOUNDARY_VIOLATION:
                return "Agent exceeded operational boundaries";
            case SENSOR_FAILURE:
                return "Sensor malfunction or invalid readings";
            case OVERLOAD:
                return "System overload - too many tasks";
            default:
                return "Unknown failure type";
        }
    }

    /**
     * Get recommended recovery action
     */
    public String getRecoveryAction() {
        switch (this) {
            case SYSTEM_ERROR:
                return "Restart agent, log error details";
            case BATTERY_DEPLETED:
                return "Replace agent or send to charging station";
            case COMMUNICATION_LOST:
                return "Attempt reconnection, relocate if needed";
            case COLLISION:
                return "Emergency stop, assess damage";
            case TIMEOUT:
                return "Force restart, mark as failed if persistent";
            case BOUNDARY_VIOLATION:
                return "Return to boundary, emergency recall";
            case SENSOR_FAILURE:
                return "Switch to backup sensors, limit operations";
            case OVERLOAD:
                return "Redistribute tasks, reduce workload";
            default:
                return "Assess and determine appropriate action";
        }
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        switch (this) {
            case SYSTEM_ERROR:
                return "System Error";
            case BATTERY_DEPLETED:
                return "Battery Depleted";
            case COMMUNICATION_LOST:
                return "Communication Lost";
            case COLLISION:
                return "Collision Detected";
            case TIMEOUT:
                return "Agent Timeout";
            case BOUNDARY_VIOLATION:
                return "Boundary Violation";
            case SENSOR_FAILURE:
                return "Sensor Failure";
            case OVERLOAD:
                return "System Overload";
            default:
                return "Unknown Failure";
        }
    }

    /**
     * Severity levels for failures
     */
    public enum Severity {
        CRITICAL,      // Immediate action required
        WARNING,       // Degraded operation
        RECOVERABLE    // Automatic recovery possible
    }
}
