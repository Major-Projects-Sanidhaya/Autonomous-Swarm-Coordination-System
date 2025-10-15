package com.team6.swarm.core;

/**
 * Enumeration of all possible user commands that can be sent from Anthony's UI.
 * Each command type represents a distinct user action or system control operation.
 *
 * Week 4: User Interface Integration
 * Purpose: Categorize different types of user interactions for proper handling
 *
 * @author Team 6 - Sanidhaya (Core System)
 */
public enum CommandType {
    /**
     * Create a new agent at specified position
     * Parameters: position (Point2D), or x/y coordinates
     */
    SPAWN_AGENT,

    /**
     * Remove an existing agent from the system
     * Parameters: agentId (String)
     */
    REMOVE_AGENT,

    /**
     * Define or update the movement boundaries for agents
     * Parameters: width (double), height (double), boundaries (List of Point2D)
     */
    SET_BOUNDARIES,

    /**
     * Start or resume the simulation
     * Parameters: none
     */
    START_SIMULATION,

    /**
     * Stop or pause the simulation
     * Parameters: none
     */
    STOP_SIMULATION,

    /**
     * Reset the entire system (remove all agents, clear state)
     * Parameters: none
     */
    RESET_SYSTEM,

    /**
     * Set target position for specific agent or group
     * Parameters: agentId (String) or groupId (String), target (Point2D)
     */
    SET_TARGET,

    /**
     * Change simulation speed/time scale
     * Parameters: timeScale (double)
     */
    SET_SPEED,

    /**
     * Enable/disable visualization of specific elements
     * Parameters: elementType (String), visible (boolean)
     */
    TOGGLE_VISUALIZATION,

    /**
     * Request formation of specific pattern
     * Parameters: formationType (String), agentIds (List<String>)
     */
    REQUEST_FORMATION,

    /**
     * Update system configuration parameters
     * Parameters: configKey (String), configValue (Object)
     */
    UPDATE_CONFIG
}
