/**
 * TASK CLASS - Core Work Assignment (Week 5-6)
 *
 * PURPOSE:
 * - Represents assigned work for agents in the core system
 * - Simpler version than intelligence package Task (Lauren's)
 * - Enables agents to know what they're supposed to do
 * - Provides interface between core and intelligence systems
 *
 * DESIGN PATTERNS USED:
 * 1. Data Transfer Object (DTO) - Encapsulates task data
 * 2. State Pattern - TaskState enum for lifecycle management
 * 3. Value Object - Immutable once created (mostly)
 * 4. Builder Pattern - Factory methods for common task types
 *
 * TASK LIFECYCLE:
 * PENDING -> ASSIGNED -> IN_PROGRESS -> COMPLETED
 *                    |-> FAILED
 *                    |-> CANCELLED
 *
 * CORE PROPERTIES:
 * 1. taskId - Unique identifier
 * 2. assignedToAgent - Which agent has this task
 * 3. taskType - Category of work (MOVE, PATROL, GUARD, etc.)
 * 4. targetLocation - Where to go (for location-based tasks)
 * 5. parameters - Task-specific configuration
 * 6. state - Current lifecycle state
 *
 * USAGE PATTERNS:
 * 1. Create task:
 *    Task task = Task.createMoveTask("task_001", targetPoint);
 *
 * 2. Assign to agent:
 *    task.assignTo(agentId);
 *    agent.executeTask(task);
 *
 * 3. Track progress:
 *    task.markInProgress();
 *    task.updateProgress(0.5); // 50% complete
 *
 * 4. Complete:
 *    task.markCompleted();
 *
 * DIFFERENCE FROM INTELLIGENCE PACKAGE TASK:
 * - Core Task: Simple work assignment, agent-focused
 * - Intelligence Task: Complex planning, behavior-focused
 * - Core Task used for execution, Intelligence Task for planning
 *
 * INTEGRATION POINTS:
 * - Created by: Lauren's TaskAllocator or SystemController
 * - Assigned to: Individual agents via AgentManager
 * - Executed by: Agent.executeTask()
 * - Reported via: TaskCompletionReport
 */
package com.team6.swarm.core;

import java.util.HashMap;
import java.util.Map;

public class Task {
    // Identification
    public final String taskId;
    public final TaskType taskType;
    public final long createdTime;

    // Assignment
    private int assignedToAgent;
    private TaskState state;

    // Execution parameters
    public Point2D targetLocation;
    public Map<String, Object> parameters;

    // Tracking
    private long startTime;
    private long completionTime;
    private double progressPercentage;

    /**
     * Task types - categories of work
     */
    public enum TaskType {
        MOVE_TO_LOCATION,   // Simple navigation to point
        PATROL_ROUTE,       // Follow predefined path
        GUARD_POSITION,     // Stay at location
        FOLLOW_AGENT,       // Track another agent
        RETURN_TO_BASE      // Navigate to home position
    }

    /**
     * Task lifecycle states
     */
    public enum TaskState {
        PENDING,      // Created but not assigned
        ASSIGNED,     // Assigned to agent but not started
        IN_PROGRESS,  // Agent is executing
        COMPLETED,    // Successfully finished
        FAILED,       // Could not complete
        CANCELLED     // Aborted by system
    }

    /**
     * Constructor - create new task
     */
    public Task(String taskId, TaskType taskType) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.createdTime = System.currentTimeMillis();
        this.state = TaskState.PENDING;
        this.assignedToAgent = -1;
        this.parameters = new HashMap<>();
        this.progressPercentage = 0.0;
    }

    /**
     * Full constructor with location
     */
    public Task(String taskId, TaskType taskType, Point2D targetLocation) {
        this(taskId, taskType);
        this.targetLocation = targetLocation;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Create simple move task
     */
    public static Task createMoveTask(String taskId, Point2D target) {
        Task task = new Task(taskId, TaskType.MOVE_TO_LOCATION, target);
        task.parameters.put("arrivalThreshold", 5.0); // 5 units
        return task;
    }

    /**
     * Create patrol task
     */
    public static Task createPatrolTask(String taskId, Point2D... waypoints) {
        Task task = new Task(taskId, TaskType.PATROL_ROUTE);
        task.parameters.put("waypoints", waypoints);
        task.parameters.put("loop", true); // Repeat patrol
        return task;
    }

    /**
     * Create guard task
     */
    public static Task createGuardTask(String taskId, Point2D position) {
        Task task = new Task(taskId, TaskType.GUARD_POSITION, position);
        task.parameters.put("guardRadius", 10.0); // Stay within 10 units
        task.parameters.put("duration", 300000L); // 5 minutes default
        return task;
    }

    /**
     * Create follow task
     */
    public static Task createFollowTask(String taskId, int targetAgentId) {
        Task task = new Task(taskId, TaskType.FOLLOW_AGENT);
        task.parameters.put("targetAgentId", targetAgentId);
        task.parameters.put("followDistance", 30.0); // Stay 30 units behind
        return task;
    }

    /**
     * Create return to base task
     */
    public static Task createReturnTask(String taskId, Point2D baseLocation) {
        Task task = new Task(taskId, TaskType.RETURN_TO_BASE, baseLocation);
        task.parameters.put("priority", "HIGH");
        return task;
    }

    // ==================== LIFECYCLE MANAGEMENT ====================

    /**
     * Assign task to agent
     */
    public void assignTo(int agentId) {
        if (state != TaskState.PENDING) {
            throw new IllegalStateException("Cannot assign task in state: " + state);
        }
        this.assignedToAgent = agentId;
        this.state = TaskState.ASSIGNED;
    }

    /**
     * Mark task as started
     */
    public void markInProgress() {
        if (state != TaskState.ASSIGNED) {
            throw new IllegalStateException("Cannot start task in state: " + state);
        }
        this.state = TaskState.IN_PROGRESS;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Update task progress (0.0 to 1.0)
     */
    public void updateProgress(double progress) {
        this.progressPercentage = Math.max(0.0, Math.min(1.0, progress));

        // Auto-complete when reaching 100%
        if (progressPercentage >= 1.0 && state == TaskState.IN_PROGRESS) {
            markCompleted();
        }
    }

    /**
     * Mark task as completed
     */
    public void markCompleted() {
        if (state != TaskState.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete task in state: " + state);
        }
        this.state = TaskState.COMPLETED;
        this.completionTime = System.currentTimeMillis();
        this.progressPercentage = 1.0;
    }

    /**
     * Mark task as failed
     */
    public void markFailed(String reason) {
        this.state = TaskState.FAILED;
        this.completionTime = System.currentTimeMillis();
        this.parameters.put("failureReason", reason);
    }

    /**
     * Cancel task
     */
    public void cancel() {
        if (state == TaskState.COMPLETED || state == TaskState.FAILED) {
            throw new IllegalStateException("Cannot cancel task in state: " + state);
        }
        this.state = TaskState.CANCELLED;
        this.completionTime = System.currentTimeMillis();
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if task is active
     */
    public boolean isActive() {
        return state == TaskState.IN_PROGRESS;
    }

    /**
     * Check if task is complete
     */
    public boolean isComplete() {
        return state == TaskState.COMPLETED;
    }

    /**
     * Check if task is pending
     */
    public boolean isPending() {
        return state == TaskState.PENDING;
    }

    /**
     * Check if task is assigned
     */
    public boolean isAssigned() {
        return assignedToAgent >= 0;
    }

    /**
     * Get task duration in milliseconds
     */
    public long getDuration() {
        if (startTime == 0) return 0;
        if (completionTime > 0) return completionTime - startTime;
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Get age of task in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - createdTime;
    }

    /**
     * Validate task has required parameters
     */
    public boolean validate() {
        if (taskId == null || taskId.isEmpty()) return false;
        if (taskType == null) return false;

        // Type-specific validation
        switch (taskType) {
            case MOVE_TO_LOCATION:
            case GUARD_POSITION:
            case RETURN_TO_BASE:
                return targetLocation != null;

            case PATROL_ROUTE:
                return parameters.containsKey("waypoints");

            case FOLLOW_AGENT:
                return parameters.containsKey("targetAgentId");

            default:
                return true;
        }
    }

    // ==================== GETTERS ====================

    public int getAssignedToAgent() { return assignedToAgent; }
    public TaskState getState() { return state; }
    public double getProgressPercentage() { return progressPercentage; }
    public long getStartTime() { return startTime; }
    public long getCompletionTime() { return completionTime; }

    @Override
    public String toString() {
        return String.format("Task[%s: %s | State: %s | Agent: %s | Progress: %.0f%%]",
            taskId, taskType, state,
            assignedToAgent >= 0 ? "Agent_" + assignedToAgent : "Unassigned",
            progressPercentage * 100);
    }

    /**
     * Get detailed task information
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append(String.format("Task ID: %s\n", taskId));
        info.append(String.format("Type: %s\n", taskType));
        info.append(String.format("State: %s\n", state));
        info.append(String.format("Assigned to: %s\n",
            assignedToAgent >= 0 ? "Agent_" + assignedToAgent : "Unassigned"));
        info.append(String.format("Progress: %.1f%%\n", progressPercentage * 100));
        if (targetLocation != null) {
            info.append(String.format("Target: (%.1f, %.1f)\n",
                targetLocation.x, targetLocation.y));
        }
        if (startTime > 0) {
            info.append(String.format("Duration: %.2fs\n", getDuration() / 1000.0));
        }
        info.append(String.format("Age: %.2fs\n", getAge() / 1000.0));
        return info.toString();
    }
}
