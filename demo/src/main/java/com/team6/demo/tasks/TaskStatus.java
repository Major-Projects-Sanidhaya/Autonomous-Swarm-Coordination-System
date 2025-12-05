package com.team6.demo.tasks;

/**
 * TaskStatus - Enumeration of task execution states
 */
public enum TaskStatus {
    PENDING,      // Task created but not yet assigned
    ASSIGNED,     // Task assigned to a drone
    IN_PROGRESS,  // Task execution has started
    COMPLETED,    // Task successfully completed
    FAILED        // Task failed or was cancelled
}
