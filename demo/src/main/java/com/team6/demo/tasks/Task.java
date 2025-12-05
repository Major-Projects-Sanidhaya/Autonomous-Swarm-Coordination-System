package com.team6.demo.tasks;

/**
 * Task - Interface for all drone tasks
 * Tasks represent actions that drones must complete
 */
public interface Task {
    /**
     * Get unique task ID
     */
    int getId();

    /**
     * Get current task status
     */
    TaskStatus getStatus();

    /**
     * Get ID of the drone assigned to this task
     */
    int getAssignedDroneId();

    /**
     * Assign task to a drone
     */
    void assignToDrone(int droneId);

    /**
     * Execute task for one time step
     * @param deltaTime Time elapsed since last update (seconds)
     * @return true if task is complete, false otherwise
     */
    boolean execute(double deltaTime);

    /**
     * Get task completion progress (0.0 to 1.0)
     */
    double getProgress();

    /**
     * Get task description
     */
    String getDescription();
}
