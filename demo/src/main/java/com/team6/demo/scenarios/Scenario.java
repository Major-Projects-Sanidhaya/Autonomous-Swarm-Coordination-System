package com.team6.demo.scenarios;

import com.team6.demo.core.Environment;

/**
 * Scenario - Interface for demo scenarios
 * Each scenario sets up environment, runs simulation, and reports results
 */
public interface Scenario {
    /**
     * Setup the scenario (create environment, drones, obstacles, tasks)
     */
    void setup();

    /**
     * Run the scenario simulation
     */
    void run();

    /**
     * Check if scenario objectives are complete
     */
    boolean isComplete();

    /**
     * Print final scenario report
     */
    void printReport();

    /**
     * Get scenario name
     */
    String getName();

    // ========== VISUALIZATION SUPPORT ==========

    /**
     * Get the environment for visualization
     * @return Environment instance
     */
    Environment getEnvironment();

    /**
     * Get current simulation time in seconds
     * @return Simulation duration
     */
    double getSimulationTime();

    /**
     * Update simulation by one time step (for visualization control)
     * @param deltaTime Time step in seconds
     */
    void update(double deltaTime);

    /**
     * Get scenario-specific status info for display
     * @return Status string (e.g., "2/2 survivors found", "Team A: positioned")
     */
    String getStatusInfo();
}
