package com.team6.demo.tasks;

import com.team6.demo.core.Position;

/**
 * WaypointTask - Navigate to a specific target position
 * Simple task for moving a drone to a waypoint
 */
public class WaypointTask implements Task {
    private static int nextId = 1;

    private final int taskId;
    private final Position targetPosition;
    private TaskStatus status;
    private int assignedDroneId;
    private Position currentDronePosition;
    private final double arrivalThreshold;

    /**
     * Create a waypoint navigation task
     * @param targetPosition Target position to reach
     * @param arrivalThreshold Distance threshold to consider task complete (meters)
     */
    public WaypointTask(Position targetPosition, double arrivalThreshold) {
        this.taskId = nextId++;
        this.targetPosition = targetPosition;
        this.status = TaskStatus.PENDING;
        this.assignedDroneId = -1;
        this.arrivalThreshold = arrivalThreshold;
        this.currentDronePosition = null;
    }

    /**
     * Create waypoint task with default threshold of 5 meters
     */
    public WaypointTask(Position targetPosition) {
        this(targetPosition, 5.0);
    }

    @Override
    public int getId() {
        return taskId;
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public int getAssignedDroneId() {
        return assignedDroneId;
    }

    @Override
    public void assignToDrone(int droneId) {
        this.assignedDroneId = droneId;
        this.status = TaskStatus.ASSIGNED;
    }

    /**
     * Update the current drone position for progress tracking
     */
    public void updateDronePosition(Position position) {
        this.currentDronePosition = position;
        if (status == TaskStatus.ASSIGNED) {
            status = TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public boolean execute(double deltaTime) {
        if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
            return true;
        }

        if (currentDronePosition == null) {
            return false;
        }

        // Check if drone has reached target
        double distance = currentDronePosition.distanceTo(targetPosition);
        if (distance <= arrivalThreshold) {
            status = TaskStatus.COMPLETED;
            return true;
        }

        status = TaskStatus.IN_PROGRESS;
        return false;
    }

    @Override
    public double getProgress() {
        if (currentDronePosition == null) {
            return 0.0;
        }

        if (status == TaskStatus.COMPLETED) {
            return 1.0;
        }

        // Simple progress based on distance (this is approximate)
        // Note: Would need start position for accurate progress
        double distance = currentDronePosition.distanceTo(targetPosition);
        if (distance < arrivalThreshold) {
            return 1.0;
        }

        // Inverse distance as rough progress indicator
        return Math.max(0.0, Math.min(1.0, arrivalThreshold / distance));
    }

    @Override
    public String getDescription() {
        return String.format("Navigate to waypoint %s", targetPosition);
    }

    // Getters
    public Position getTargetPosition() {
        return targetPosition;
    }

    public double getArrivalThreshold() {
        return arrivalThreshold;
    }

    @Override
    public String toString() {
        return String.format("WaypointTask[id=%d, target=%s, status=%s, assignedTo=%d]",
            taskId, targetPosition, status, assignedDroneId);
    }
}
