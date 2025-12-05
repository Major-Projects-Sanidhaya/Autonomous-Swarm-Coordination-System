package com.team6.demo.tasks;

import com.team6.demo.core.Position;

/**
 * PayloadDeliveryTask - Pick up payload from one location and deliver to another
 * Drone speed is reduced when carrying payload
 */
public class PayloadDeliveryTask implements Task {
    private static int nextId = 1;

    private final int taskId;
    private final Position pickupLocation;
    private final Position dropoffLocation;
    private TaskStatus status;
    private int assignedDroneId;
    private Position currentDronePosition;
    private boolean hasPayload;
    private final double arrivalThreshold;

    // Speed modifiers
    public static final double NORMAL_SPEED = 12.0;  // m/s
    public static final double LOADED_SPEED = 8.0;   // m/s (slower when carrying payload)

    /**
     * Create a payload delivery task
     * @param pickupLocation Where to pick up the payload
     * @param dropoffLocation Where to deliver the payload
     */
    public PayloadDeliveryTask(Position pickupLocation, Position dropoffLocation) {
        this.taskId = nextId++;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.status = TaskStatus.PENDING;
        this.assignedDroneId = -1;
        this.hasPayload = false;
        this.arrivalThreshold = 15.0;  // Increased from 5.0 to handle obstacles
        this.currentDronePosition = null;
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
     * Update the current drone position
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

        // Phase 1: Move to pickup location
        if (!hasPayload) {
            double distanceToPickup = currentDronePosition.distanceTo(pickupLocation);
            if (distanceToPickup <= arrivalThreshold) {
                hasPayload = true;
                return false; // Continue to delivery
            }
        }

        // Phase 2: Move to dropoff location
        if (hasPayload) {
            double distanceToDropoff = currentDronePosition.distanceTo(dropoffLocation);
            if (distanceToDropoff <= arrivalThreshold) {
                status = TaskStatus.COMPLETED;
                return true;
            }
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

        // Calculate total distance
        double pickupToDropoff = pickupLocation.distanceTo(dropoffLocation);

        if (!hasPayload) {
            // Progress to pickup (0.0 to 0.5)
            double startToPickup = pickupLocation.distanceTo(new Position(0, 0, 10)); // Assuming start at origin
            double currentToPickup = currentDronePosition.distanceTo(pickupLocation);
            double pickupProgress = 1.0 - (currentToPickup / startToPickup);
            return Math.max(0.0, Math.min(0.5, pickupProgress * 0.5));
        } else {
            // Progress to dropoff (0.5 to 1.0)
            double currentToDropoff = currentDronePosition.distanceTo(dropoffLocation);
            double dropoffProgress = 1.0 - (currentToDropoff / pickupToDropoff);
            return 0.5 + Math.max(0.0, Math.min(0.5, dropoffProgress * 0.5));
        }
    }

    @Override
    public String getDescription() {
        return String.format("Deliver payload from %s to %s", pickupLocation, dropoffLocation);
    }

    // Getters
    public Position getPickupLocation() {
        return pickupLocation;
    }

    public Position getDropoffLocation() {
        return dropoffLocation;
    }

    public boolean hasPayload() {
        return hasPayload;
    }

    /**
     * Get current target position (pickup or dropoff depending on state)
     */
    public Position getCurrentTarget() {
        return hasPayload ? dropoffLocation : pickupLocation;
    }

    /**
     * Get current speed for this task (slower when loaded)
     */
    public double getCurrentSpeed() {
        return hasPayload ? LOADED_SPEED : NORMAL_SPEED;
    }

    @Override
    public String toString() {
        return String.format("PayloadDeliveryTask[id=%d, pickup=%s, dropoff=%s, hasPayload=%s, status=%s]",
            taskId, pickupLocation, dropoffLocation, hasPayload, status);
    }
}
