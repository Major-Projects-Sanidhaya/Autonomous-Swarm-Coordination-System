/**
 * TASKSTATUS ENUM - Task Lifecycle States
 *
 * PURPOSE:
 * - Defines all possible states a task can be in
 * - Enables status-based task filtering and management
 * - Critical for task progress tracking and system monitoring
 *
 * STATUS DEFINITIONS:
 *
 * PENDING:
 * - Task created but not yet assigned
 * - Waiting for agent allocation
 * - In task queue
 * - assignedTo == -1
 *
 * IN_PROGRESS:
 * - Task currently being executed
 * - Agent actively working on it
 * - Progress tracking active
 * - 0% < completionPercentage < 100%
 *
 * COMPLETED:
 * - Task successfully finished
 * - Objective achieved
 * - completionPercentage == 100%
 * - completionTime set
 *
 * FAILED:
 * - Task could not be completed
 * - Agent encountered insurmountable obstacle
 * - Requires human intervention or task redesign
 * - failureReason stored in parameters
 *
 * CANCELLED:
 * - Task manually stopped
 * - No longer needed
 * - Mission changed or task obsolete
 * - User or system initiated
 *
 * BLOCKED:
 * - Task cannot proceed temporarily
 * - Waiting for obstacle resolution
 * - Agent ready but path blocked
 * - blockReason stored in parameters
 *
 * STATUS FLOW:
 *
 * Normal Flow:
 * PENDING → IN_PROGRESS → COMPLETED
 *
 * Failure Flow:
 * PENDING → IN_PROGRESS → FAILED
 *
 * Cancellation Flow:
 * PENDING → CANCELLED
 * IN_PROGRESS → CANCELLED
 *
 * Block Flow:
 * IN_PROGRESS → BLOCKED → IN_PROGRESS → COMPLETED
 *
 * USAGE PATTERNS:
 *
 * Create Task:
 * task.status = TaskStatus.PENDING
 *
 * Assign to Agent:
 * task.status = TaskStatus.IN_PROGRESS
 *
 * Complete Task:
 * task.status = TaskStatus.COMPLETED
 *
 * Handle Failure:
 * task.status = TaskStatus.FAILED
 *
 * Temporary Block:
 * task.status = TaskStatus.BLOCKED
 * // Later when unblocked:
 * task.status = TaskStatus.IN_PROGRESS
 *
 * FILTERING EXAMPLES:
 *
 * Get all active tasks:
 * tasks.stream()
 *      .filter(t -> t.status == TaskStatus.IN_PROGRESS)
 *      .collect(Collectors.toList());
 *
 * Get pending tasks:
 * tasks.stream()
 *      .filter(t -> t.status == TaskStatus.PENDING)
 *      .collect(Collectors.toList());
 *
 * Get completed tasks:
 * tasks.stream()
 *      .filter(t -> t.status == TaskStatus.COMPLETED)
 *      .collect(Collectors.toList());
 *
 * INTEGRATION POINTS:
 * - Read by: TaskAllocator for task selection
 * - Read by: Anthony's UI for status display
 * - Modified by: Agents during task execution
 * - Used by: Performance monitoring systems
 */
package com.team6.swarm.intelligence.tasking;

public enum TaskStatus {
    /**
     * Task created, waiting for assignment
     */
    PENDING,
    
    /**
     * Task currently being executed by agent
     */
    IN_PROGRESS,
    
    /**
     * Task successfully completed
     */
    COMPLETED,
    
    /**
     * Task failed to complete
     */
    FAILED,
    
    /**
     * Task manually cancelled
     */
    CANCELLED,
    
    /**
     * Task temporarily blocked, cannot proceed
     */
    BLOCKED;
    
    /**
     * Check if this is a terminal state
     * (task is finished, no further work)
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
    
    /**
     * Check if this is an active state
     * (task is being worked on)
     */
    public boolean isActive() {
        return this == IN_PROGRESS || this == BLOCKED;
    }
    
    /**
     * Check if task can be reassigned
     */
    public boolean canReassign() {
        return this == PENDING || this == BLOCKED || this == FAILED;
    }
    
    /**
     * Get display color for UI
     */
    public String getDisplayColor() {
        switch (this) {
            case PENDING: return "#FFA500";      // Orange - waiting
            case IN_PROGRESS: return "#4A90E2";  // Blue - active
            case COMPLETED: return "#7ED321";    // Green - success
            case FAILED: return "#D0021B";       // Red - failure
            case CANCELLED: return "#9B9B9B";    // Gray - cancelled
            case BLOCKED: return "#F5A623";      // Yellow - blocked
            default: return "#000000";           // Black - unknown
        }
    }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        switch (this) {
            case PENDING: return "Pending";
            case IN_PROGRESS: return "In Progress";
            case COMPLETED: return "Completed";
            case FAILED: return "Failed";
            case CANCELLED: return "Cancelled";
            case BLOCKED: return "Blocked";
            default: return "Unknown";
        }
    }
}