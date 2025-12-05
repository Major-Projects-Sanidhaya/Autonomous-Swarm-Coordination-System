package com.team6.demo.core;

/**
 * Intruder - Represents an unauthorized entity entering the patrol area
 * Used in PerimeterPatrol scenario
 */
public class Intruder {
    private Position position;
    private final Position targetPosition;
    private final double speed;  // m/s
    private boolean detected;
    private int detectedByDroneId;

    /**
     * Create an intruder
     * @param startPosition Starting position
     * @param targetPosition Where the intruder is heading
     * @param speed Movement speed in m/s
     */
    public Intruder(Position startPosition, Position targetPosition, double speed) {
        this.position = startPosition;
        this.targetPosition = targetPosition;
        this.speed = speed;
        this.detected = false;
        this.detectedByDroneId = -1;
    }

    /**
     * Update intruder position - moves toward target
     */
    public void update(double deltaTime) {
        position = position.moveTo(targetPosition, speed, deltaTime);
    }

    /**
     * Mark intruder as detected by a drone
     */
    public void markDetected(int droneId) {
        if (!detected) {
            this.detected = true;
            this.detectedByDroneId = droneId;
        }
    }

    /**
     * Check if intruder has reached target
     */
    public boolean hasReachedTarget() {
        return position.isNear(targetPosition, 5.0);
    }

    // Getters
    public Position getPosition() {
        return position;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isDetected() {
        return detected;
    }

    public int getDetectedByDroneId() {
        return detectedByDroneId;
    }

    @Override
    public String toString() {
        return String.format("Intruder[pos=%s, detected=%s, detectedBy=%d]",
            position, detected, detectedByDroneId);
    }
}
