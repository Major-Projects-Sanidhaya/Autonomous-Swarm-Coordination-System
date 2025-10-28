/**
 * COMMANDTYPE ENUM - System Command Categories
 *
 * PURPOSE:
 * - Defines all possible user commands in the system
 * - Provides type safety for command handling
 * - Enables command routing and validation
 * - Supports command categorization and filtering
 *
 * COMMAND CATEGORIES:
 *
 * 1. AGENT MANAGEMENT:
 *    - SPAWN_AGENT: Create new agent
 *    - REMOVE_AGENT: Delete existing agent
 *    - CONFIGURE_AGENT: Modify agent parameters
 *
 * 2. SIMULATION CONTROL:
 *    - START_SIMULATION: Begin/resume simulation
 *    - STOP_SIMULATION: Pause simulation
 *    - RESET_SIMULATION: Clear all and restart
 *    - STEP_SIMULATION: Advance one frame (debug mode)
 *
 * 3. NAVIGATION:
 *    - PLACE_WAYPOINT: Add navigation target
 *    - REMOVE_WAYPOINT: Delete waypoint
 *    - CLEAR_WAYPOINTS: Remove all waypoints
 *    - SET_TARGET: Direct agent to specific location
 *
 * 4. FORMATION CONTROL:
 *    - SET_FORMATION: Change swarm formation pattern
 *    - BREAK_FORMATION: Return to free movement
 *    - ROTATE_FORMATION: Rotate formation angle
 *
 * 5. DECISION MAKING:
 *    - INITIATE_VOTE: Start voting process
 *    - CANCEL_VOTE: Abort active vote
 *    - FORCE_DECISION: Override voting (emergency)
 *
 * 6. CONFIGURATION:
 *    - SET_BOUNDARIES: Define world limits
 *    - UPDATE_PARAMETERS: Change algorithm settings
 *    - SET_COMMUNICATION_RANGE: Modify comm range
 *    - SET_SPEED_LIMIT: Change max speed
 *
 * 7. EMERGENCY:
 *    - EMERGENCY_STOP: Immediate halt all agents
 *    - EMERGENCY_SCATTER: Disperse agents
 *    - EMERGENCY_GATHER: Bring agents together
 *
 * 8. MONITORING:
 *    - EXPORT_DATA: Save simulation data
 *    - TAKE_SNAPSHOT: Capture current state
 *    - START_RECORDING: Begin data recording
 *    - STOP_RECORDING: End data recording
 *
 * USAGE EXAMPLE:
 * SystemCommand cmd = new SystemCommand();
 * cmd.type = CommandType.SPAWN_AGENT;
 * 
 * switch(cmd.type) {
 *     case SPAWN_AGENT:
 *         handleSpawnAgent(cmd);
 *         break;
 *     case EMERGENCY_STOP:
 *         handleEmergencyStop(cmd);
 *         break;
 * }
 *
 * INTEGRATION POINTS:
 * - Used by: SystemCommand, SystemController, ControlPanel
 * - Validated by: CommandDispatcher
 * - Routed by: UIEventHandler
 */
package com.team6.swarm.ui;

public enum CommandType {
    // ==================== AGENT MANAGEMENT ====================
    /**
     * Create a new agent at specified position
     * Required parameters: position (Point2D)
     * Optional parameters: maxSpeed, communicationRange, agentName
     */
    SPAWN_AGENT("Spawn Agent", "agent"),
    
    /**
     * Remove an agent from the system
     * Required parameters: agentId (int)
     */
    REMOVE_AGENT("Remove Agent", "agent"),
    
    /**
     * Modify agent configuration
     * Required parameters: agentId (int)
     * Optional parameters: maxSpeed, communicationRange, etc.
     */
    CONFIGURE_AGENT("Configure Agent", "agent"),
    
    // ==================== SIMULATION CONTROL ====================
    /**
     * Start or resume the simulation
     * No required parameters
     */
    START_SIMULATION("Start Simulation", "simulation"),
    
    /**
     * Pause the simulation
     * No required parameters
     */
    STOP_SIMULATION("Stop Simulation", "simulation"),
    
    /**
     * Reset simulation to initial state
     * No required parameters
     */
    RESET_SIMULATION("Reset Simulation", "simulation"),
    
    /**
     * Advance simulation by one frame (debug mode)
     * No required parameters
     */
    STEP_SIMULATION("Step Simulation", "simulation"),
    
    // ==================== NAVIGATION ====================
    /**
     * Place a waypoint for agents to navigate to
     * Required parameters: position (Point2D)
     * Optional parameters: priority, radius
     */
    PLACE_WAYPOINT("Place Waypoint", "navigation"),
    
    /**
     * Remove a specific waypoint
     * Required parameters: waypointId (int)
     */
    REMOVE_WAYPOINT("Remove Waypoint", "navigation"),
    
    /**
     * Clear all waypoints
     * No required parameters
     */
    CLEAR_WAYPOINTS("Clear Waypoints", "navigation"),
    
    /**
     * Set direct target for specific agent
     * Required parameters: agentId (int), target (Point2D)
     */
    SET_TARGET("Set Target", "navigation"),
    
    // ==================== FORMATION CONTROL ====================
    /**
     * Change swarm formation pattern
     * Required parameters: formationType (String)
     * Optional parameters: spacing, leaderAgentId
     */
    SET_FORMATION("Set Formation", "formation"),
    
    /**
     * Break formation and return to free movement
     * No required parameters
     */
    BREAK_FORMATION("Break Formation", "formation"),
    
    /**
     * Rotate formation by specified angle
     * Required parameters: angle (double)
     */
    ROTATE_FORMATION("Rotate Formation", "formation"),
    
    // ==================== DECISION MAKING ====================
    /**
     * Initiate a voting process
     * Required parameters: question (String), options (List<String>)
     * Optional parameters: timeout, consensusThreshold
     */
    INITIATE_VOTE("Initiate Vote", "decision"),
    
    /**
     * Cancel an active vote
     * Required parameters: proposalId (String)
     */
    CANCEL_VOTE("Cancel Vote", "decision"),
    
    /**
     * Force a decision without voting (emergency)
     * Required parameters: decision (String)
     */
    FORCE_DECISION("Force Decision", "decision"),
    
    // ==================== CONFIGURATION ====================
    /**
     * Set world boundaries
     * Required parameters: width (double), height (double)
     */
    SET_BOUNDARIES("Set Boundaries", "configuration"),
    
    /**
     * Update algorithm parameters
     * Required parameters: parameterName (String), value (Object)
     */
    UPDATE_PARAMETERS("Update Parameters", "configuration"),
    
    /**
     * Set communication range for all agents
     * Required parameters: range (double)
     */
    SET_COMMUNICATION_RANGE("Set Communication Range", "configuration"),
    
    /**
     * Set maximum speed for all agents
     * Required parameters: maxSpeed (double)
     */
    SET_SPEED_LIMIT("Set Speed Limit", "configuration"),
    
    // ==================== EMERGENCY ====================
    /**
     * Immediately stop all agents (critical priority)
     * No required parameters
     */
    EMERGENCY_STOP("Emergency Stop", "emergency"),
    
    /**
     * Scatter agents to avoid collision/danger
     * No required parameters
     */
    EMERGENCY_SCATTER("Emergency Scatter", "emergency"),
    
    /**
     * Gather all agents to central location
     * Optional parameters: gatherPoint (Point2D)
     */
    EMERGENCY_GATHER("Emergency Gather", "emergency"),
    
    // ==================== MONITORING ====================
    /**
     * Export simulation data to file
     * Required parameters: filename (String)
     */
    EXPORT_DATA("Export Data", "monitoring"),
    
    /**
     * Capture current system state
     * No required parameters
     */
    TAKE_SNAPSHOT("Take Snapshot", "monitoring"),
    
    /**
     * Start recording simulation data
     * Optional parameters: filename (String)
     */
    START_RECORDING("Start Recording", "monitoring"),
    
    /**
     * Stop recording simulation data
     * No required parameters
     */
    STOP_RECORDING("Stop Recording", "monitoring");
    
    // ==================== ENUM PROPERTIES ====================
    
    private final String displayName;
    private final String category;
    
    /**
     * Constructor
     */
    CommandType(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }
    
    /**
     * Get human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get command category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Check if command is in specific category
     */
    public boolean isCategory(String category) {
        return this.category.equalsIgnoreCase(category);
    }
    
    /**
     * Check if command is emergency type
     */
    public boolean isEmergency() {
        return category.equals("emergency");
    }
    
    /**
     * Check if command affects agents
     */
    public boolean affectsAgents() {
        return category.equals("agent") || 
               category.equals("navigation") || 
               category.equals("formation");
    }
    
    /**
     * Check if command affects simulation
     */
    public boolean affectsSimulation() {
        return category.equals("simulation") || 
               category.equals("configuration");
    }
    
    /**
     * Get all commands in a category
     */
    public static CommandType[] getByCategory(String category) {
        return java.util.Arrays.stream(values())
            .filter(cmd -> cmd.category.equalsIgnoreCase(category))
            .toArray(CommandType[]::new);
    }
    
    /**
     * Get command by display name
     */
    public static CommandType fromDisplayName(String displayName) {
        for (CommandType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Get all categories
     */
    public static String[] getAllCategories() {
        return java.util.Arrays.stream(values())
            .map(cmd -> cmd.category)
            .distinct()
            .toArray(String[]::new);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
