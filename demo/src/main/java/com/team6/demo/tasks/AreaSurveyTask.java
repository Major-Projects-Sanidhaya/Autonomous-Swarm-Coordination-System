package com.team6.demo.tasks;

import com.team6.demo.core.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * AreaSurveyTask - Survey a rectangular area using lawn-mower pattern
 * Tracks coverage percentage as drone moves through the area
 */
public class AreaSurveyTask implements Task {
    private static int nextId = 1;

    private final int taskId;
    private final Position areaMin;  // Bottom-left corner
    private final Position areaMax;  // Top-right corner
    private final double sweepSpacing; // Distance between sweep lines
    private TaskStatus status;
    private int assignedDroneId;
    private Position currentDronePosition;

    // Lawn-mower pattern waypoints
    private final List<Position> waypoints;
    private int currentWaypointIndex;
    private final double waypointThreshold;

    /**
     * Create an area survey task
     * @param areaMin Bottom-left corner of survey area
     * @param areaMax Top-right corner of survey area
     * @param sweepSpacing Distance between sweep lines (meters)
     * @param altitude Survey altitude
     */
    public AreaSurveyTask(Position areaMin, Position areaMax, double sweepSpacing, double altitude) {
        this.taskId = nextId++;
        this.areaMin = areaMin;
        this.areaMax = areaMax;
        this.sweepSpacing = sweepSpacing;
        this.status = TaskStatus.PENDING;
        this.assignedDroneId = -1;
        this.currentDronePosition = null;
        this.waypoints = new ArrayList<>();
        this.currentWaypointIndex = 0;
        this.waypointThreshold = 5.0;

        // Generate lawn-mower pattern waypoints
        generateWaypoints(altitude);
    }

    /**
     * Generate waypoints for lawn-mower survey pattern
     */
    private void generateWaypoints(double altitude) {
        double y = areaMin.y;
        boolean leftToRight = true;

        while (y <= areaMax.y) {
            if (leftToRight) {
                // Sweep left to right
                waypoints.add(new Position(areaMin.x, y, altitude));
                waypoints.add(new Position(areaMax.x, y, altitude));
            } else {
                // Sweep right to left
                waypoints.add(new Position(areaMax.x, y, altitude));
                waypoints.add(new Position(areaMin.x, y, altitude));
            }

            y += sweepSpacing;
            leftToRight = !leftToRight;
        }
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

        if (currentDronePosition == null || waypoints.isEmpty()) {
            return false;
        }

        // Check if current waypoint reached
        Position currentWaypoint = waypoints.get(currentWaypointIndex);
        if (currentDronePosition.isNear(currentWaypoint, waypointThreshold)) {
            currentWaypointIndex++;

            // Check if all waypoints completed
            if (currentWaypointIndex >= waypoints.size()) {
                status = TaskStatus.COMPLETED;
                return true;
            }
        }

        status = TaskStatus.IN_PROGRESS;
        return false;
    }

    @Override
    public double getProgress() {
        if (waypoints.isEmpty()) {
            return 0.0;
        }

        if (status == TaskStatus.COMPLETED) {
            return 1.0;
        }

        return (double) currentWaypointIndex / waypoints.size();
    }

    @Override
    public String getDescription() {
        return String.format("Survey area from %s to %s", areaMin, areaMax);
    }

    /**
     * Get current target waypoint
     */
    public Position getCurrentWaypoint() {
        if (currentWaypointIndex < waypoints.size()) {
            return waypoints.get(currentWaypointIndex);
        }
        return waypoints.get(waypoints.size() - 1);
    }

    /**
     * Get total area being surveyed (square meters)
     */
    public double getSurveyArea() {
        double width = areaMax.x - areaMin.x;
        double height = areaMax.y - areaMin.y;
        return width * height;
    }

    /**
     * Get percentage of area covered
     */
    public int getCoveragePercent() {
        return (int) (getProgress() * 100);
    }

    // Getters
    public Position getAreaMin() {
        return areaMin;
    }

    public Position getAreaMax() {
        return areaMax;
    }

    public int getWaypointCount() {
        return waypoints.size();
    }

    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }

    @Override
    public String toString() {
        return String.format("AreaSurveyTask[id=%d, area=%s to %s, coverage=%d%%, status=%s]",
            taskId, areaMin, areaMax, getCoveragePercent(), status);
    }
}
