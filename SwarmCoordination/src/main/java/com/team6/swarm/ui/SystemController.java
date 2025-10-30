/**
 * SYSTEMCONTROLLER CLASS - Integration Hub and Command Router
 *
 * PURPOSE:
 * - Central coordination point for all system components
 * - Routes commands from UI to appropriate subsystems
 * - Manages simulation loop and timing
 * - Publishes visualization updates to UI
 * - Handles component lifecycle and error recovery
 *
 * ARCHITECTURE:
 * SystemController acts as the "brain" connecting:
 * - Anthony's UI components (MainInterface, Visualizer, ControlPanel)
 * - Sanidhaya's Core System (AgentManager, PhysicsEngine)
 * - John's Communication System (CommunicationManager, NetworkSimulator)
 * - Lauren's Intelligence (VotingSystem, FlockingController, FormationController)
 *
 * COMMAND ROUTING:
 * User Action → ControlPanel → SystemCommand → SystemController → Target Component
 *
 * EVENT FLOW:
 * Component Update → EventBus → SystemController → Aggregate Data → UI Update
 *
 * SIMULATION LOOP (30 FPS):
 * 1. Calculate deltaTime
 * 2. Update all agents (physics, movement)
 * 3. Update communication topology
 * 4. Process flocking behaviors
 * 5. Check voting timeouts
 * 6. Update formations
 * 7. Process tasks
 * 8. Publish visualization update
 * 9. Sleep to maintain 30 FPS
 *
 * INTEGRATION POINTS:
 * - Receives: SystemCommand from UI
 * - Sends: VisualizationUpdate, NetworkStatus, DecisionStatus to UI
 * - Coordinates: All system components through EventBus
 *
 * USAGE EXAMPLE:
 * EventBus eventBus = new EventBus();
 * SystemController controller = new SystemController(eventBus);
 * controller.initialize();
 * controller.startSimulation();
 * // ... user interactions ...
 * controller.stopSimulation();
 */
package com.team6.swarm.ui;

import com.team6.swarm.core.*;
import com.team6.swarm.communication.*;
import com.team6.swarm.intelligence.Flocking.*;
import com.team6.swarm.intelligence.formation.*;
import com.team6.swarm.intelligence.tasking.*;
import com.team6.swarm.intelligence.voting.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SystemController {
    // ==================== CORE COMPONENTS ====================
    private final EventBus eventBus;
    private AgentManager agentManager;
    private CommunicationManager communicationManager;
    private VotingSystem votingSystem;
    private FlockingController flockingController;
    private FormationController formationController;
    private TaskAllocator taskAllocator;
    
    // ==================== SIMULATION STATE ====================
    private boolean simulationRunning;
    private Thread simulationThread;
    private long lastUpdateTime;
    private double simulationSpeed;
    
    // ==================== CONFIGURATION ====================
    private BehaviorConfiguration behaviorConfig;
    private NetworkConfiguration networkConfig;
    private UIConfiguration uiConfig;
    
    // ==================== WORLD PARAMETERS ====================
    private double worldWidth;
    private double worldHeight;
    
    // ==================== PERFORMANCE TRACKING ====================
    private int frameCount;
    private long lastFpsTime;
    private double currentFps;
    private int commandsExecuted;
    
    // ==================== COMMAND HISTORY ====================
    private final List<SystemCommand> commandHistory;
    private static final int MAX_HISTORY = 100;
    
    /**
     * Constructor - Initialize with EventBus
     */
    public SystemController(EventBus eventBus) {
        this.eventBus = eventBus;
        this.simulationRunning = false;
        this.simulationSpeed = 1.0;
        this.worldWidth = 800.0;
        this.worldHeight = 600.0;
        this.commandHistory = new ArrayList<>();
        this.commandsExecuted = 0;
        this.currentFps = 0.0;
        this.lastFpsTime = System.currentTimeMillis();
        
        // Initialize default configurations
        this.behaviorConfig = new BehaviorConfiguration();
        this.networkConfig = new NetworkConfiguration();
        this.uiConfig = new UIConfiguration();
        
        // Subscribe to events
        setupEventListeners();
    }
    
    /**
     * Initialize all system components
     */
    public void initialize() {
        System.out.println("SystemController: Initializing components...");
        
        // Initialize Sanidhaya's Core System
        agentManager = new AgentManager(eventBus);
        System.out.println("  ✓ AgentManager initialized");
        
        // Initialize John's Communication System
        communicationManager = new CommunicationManager(eventBus);
        System.out.println("  ✓ CommunicationManager initialized");
        
        // Initialize Lauren's Intelligence Systems
        votingSystem = new VotingSystem();
        flockingController = new FlockingController();
        formationController = new FormationController();
        taskAllocator = new TaskAllocator();
        System.out.println("  ✓ Intelligence systems initialized");
        
        System.out.println("SystemController: All components initialized successfully!");
    }
    
    /**
     * Set up event listeners for component communication
     */
    private void setupEventListeners() {
        // Listen for SystemCommands from UI
        eventBus.subscribe(SystemCommand.class, this::executeCommand);
        
        // Listen for configuration changes
        eventBus.subscribe(BehaviorConfiguration.class, this::updateBehaviorConfiguration);
        eventBus.subscribe(NetworkConfiguration.class, this::updateNetworkConfiguration);
        eventBus.subscribe(UIConfiguration.class, this::updateUIConfiguration);
        
        // Listen for agent state updates
        eventBus.subscribe(AgentStateUpdate.class, this::handleAgentStateUpdate);
        
        // Listen for vote results
        eventBus.subscribe(VoteResult.class, this::handleVoteResult);
    }
    
    // ==================== COMMAND EXECUTION ====================
    
    /**
     * Execute system command - Main command router
     */
    public void executeCommand(SystemCommand cmd) {
        if (cmd == null || !cmd.validate()) {
            System.err.println("Invalid command rejected: " + cmd);
            if (cmd != null) {
                cmd.markExecuted(false, "Validation failed");
            }
            return;
        }
        
        System.out.println("Executing: " + cmd.getDescription());
        
        try {
            boolean success = false;
            String result = "";
            
            switch (cmd.type) {
                case SPAWN_AGENT:
                    result = handleSpawnAgent(cmd);
                    success = true;
                    break;
                    
                case REMOVE_AGENT:
                    result = handleRemoveAgent(cmd);
                    success = true;
                    break;
                    
                case CONFIGURE_AGENT:
                    result = handleConfigureAgent(cmd);
                    success = true;
                    break;
                    
                case SET_BOUNDARIES:
                    result = handleSetBoundaries(cmd);
                    success = true;
                    break;
                    
                case START_SIMULATION:
                    result = handleStartSimulation(cmd);
                    success = true;
                    break;
                    
                case STOP_SIMULATION:
                    result = handleStopSimulation(cmd);
                    success = true;
                    break;
                    
                case RESET_SIMULATION:
                    result = handleResetSimulation(cmd);
                    success = true;
                    break;
                    
                case PLACE_WAYPOINT:
                    result = handlePlaceWaypoint(cmd);
                    success = true;
                    break;
                    
                case CLEAR_WAYPOINTS:
                    result = handleClearWaypoints(cmd);
                    success = true;
                    break;
                    
                case SET_FORMATION:
                    result = handleSetFormation(cmd);
                    success = true;
                    break;
                    
                case BREAK_FORMATION:
                    result = handleBreakFormation(cmd);
                    success = true;
                    break;
                    
                case INITIATE_VOTE:
                    result = handleInitiateVote(cmd);
                    success = true;
                    break;
                    
                case EMERGENCY_STOP:
                    result = handleEmergencyStop(cmd);
                    success = true;
                    break;
                    
                default:
                    result = "Unknown command type: " + cmd.type;
                    success = false;
                    System.err.println(result);
            }
            
            cmd.markExecuted(success, result);
            commandsExecuted++;
            
            // Add to history
            addToHistory(cmd);
            
            System.out.println("  → " + result);
            
        } catch (Exception e) {
            String error = "Command execution failed: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            cmd.markExecuted(false, error);
        }
    }
    
    // ==================== COMMAND HANDLERS ====================
    
    private String handleSpawnAgent(SystemCommand cmd) {
        Point2D position = cmd.getParameter("position", Point2D.class);
        if (position == null) {
            position = new Point2D(
                Math.random() * worldWidth,
                Math.random() * worldHeight
            );
        }
        
        double maxSpeed = cmd.getParameter("maxSpeed", 50.0);
        double commRange = cmd.getParameter("communicationRange", 100.0);
        
        Agent agent = agentManager.createAgent(position);
        agent.getState().maxSpeed = maxSpeed;
        agent.getState().communicationRange = commRange;
        
        return "Spawned " + agent.getState().agentName + " at " + position;
    }
    
    private String handleRemoveAgent(SystemCommand cmd) {
        Integer agentId = cmd.getParameter("agentId", Integer.class);
        if (agentId == null) {
            return "Error: No agent ID specified";
        }
        
        agentManager.removeAgent(agentId);
        return "Removed Agent " + agentId;
    }
    
    private String handleConfigureAgent(SystemCommand cmd) {
        Integer agentId = cmd.getParameter("agentId", Integer.class);
        if (agentId == null) {
            return "Error: No agent ID specified";
        }
        
        Agent agent = agentManager.getAgent(agentId);
        if (agent == null) {
            return "Error: Agent " + agentId + " not found";
        }
        
        // Update agent parameters
        if (cmd.hasParameter("maxSpeed")) {
            agent.getState().maxSpeed = cmd.getParameter("maxSpeed", 50.0);
        }
        if (cmd.hasParameter("communicationRange")) {
            agent.getState().communicationRange = cmd.getParameter("communicationRange", 100.0);
        }
        
        return "Configured Agent " + agentId;
    }
    
    private String handleSetBoundaries(SystemCommand cmd) {
        Double width = cmd.getParameter("width", Double.class);
        Double height = cmd.getParameter("height", Double.class);
        
        if (width != null) this.worldWidth = width;
        if (height != null) this.worldHeight = height;
        
        return String.format("World boundaries set to %.0f x %.0f", worldWidth, worldHeight);
    }
    
    private String handleStartSimulation(SystemCommand cmd) {
        if (simulationRunning) {
            return "Simulation already running";
        }
        
        startSimulation();
        return "Simulation started";
    }
    
    private String handleStopSimulation(SystemCommand cmd) {
        if (!simulationRunning) {
            return "Simulation not running";
        }
        
        stopSimulation();
        return "Simulation stopped";
    }
    
    private String handleResetSimulation(SystemCommand cmd) {
        stopSimulation();
        
        // Clear all agents
        List<AgentState> agents = agentManager.getAllAgentStates();
        for (AgentState state : agents) {
            agentManager.removeAgent(state.agentId);
        }
        
        // Reset configurations
        behaviorConfig = new BehaviorConfiguration();
        networkConfig = new NetworkConfiguration();
        
        // Clear command history
        commandHistory.clear();
        commandsExecuted = 0;
        
        return "Simulation reset";
    }
    
    private String handlePlaceWaypoint(SystemCommand cmd) {
        Point2D position = cmd.getParameter("position", Point2D.class);
        if (position == null) {
            return "Error: No position specified";
        }
        
        Integer priority = cmd.getParameter("priority", 1);
        Double radius = cmd.getParameter("radius", 20.0);
        
        Task waypoint = new Task();
        waypoint.taskId = "waypoint_" + System.currentTimeMillis();
        waypoint.targetPosition = position;
        waypoint.priority = priority;
        waypoint.completionRadius = radius;
        
        taskAllocator.addTask(waypoint);
        
        return "Waypoint placed at " + position;
    }
    
    private String handleClearWaypoints(SystemCommand cmd) {
        taskAllocator.clearAllTasks();
        return "All waypoints cleared";
    }
    
    private String handleSetFormation(SystemCommand cmd) {
        String formationType = cmd.getParameter("formationType", "LINE");
        Double spacing = cmd.getParameter("spacing", 50.0);
        
        FormationType type;
        try {
            type = FormationType.valueOf(formationType);
        } catch (IllegalArgumentException e) {
            return "Error: Invalid formation type: " + formationType;
        }
        
        Formation formation = new Formation(type);
        formation.spacing = spacing;
        
        formationController.setFormation(formation, agentManager.getAllAgentStates());
        
        return "Formation set to " + formationType;
    }
    
    private String handleBreakFormation(SystemCommand cmd) {
        formationController.breakFormation();
        return "Formation broken";
    }
    
    private String handleInitiateVote(SystemCommand cmd) {
        String question = cmd.getParameter("question", String.class);
        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) cmd.parameters.get("options");
        String proposalType = cmd.getParameter("proposalType", "COORDINATION");
        
        if (question == null || options == null || options.size() < 2) {
            return "Error: Invalid vote parameters";
        }
        
        ProposalType type;
        try {
            type = ProposalType.valueOf(proposalType);
        } catch (IllegalArgumentException e) {
            type = ProposalType.COORDINATION;
        }
        
        String proposalId = votingSystem.initiateVote(question, options, type);
        
        return "Vote initiated: " + proposalId;
    }
    
    private String handleEmergencyStop(SystemCommand cmd) {
        // Stop all agents immediately
        for (AgentState state : agentManager.getAllAgentStates()) {
            Agent agent = agentManager.getAgent(state.agentId);
            if (agent != null) {
                agent.getState().velocity = new Vector2D(0, 0);
            }
        }
        
        // Stop simulation
        stopSimulation();
        
        return "EMERGENCY STOP EXECUTED";
    }
    
    // ==================== CONFIGURATION HANDLERS ====================
    
    private void updateBehaviorConfiguration(BehaviorConfiguration config) {
        this.behaviorConfig = config;
        
        // Update flocking controller
        if (flockingController != null) {
            FlockingParameters params = new FlockingParameters();
            params.separationWeight = config.getParameter("separationWeight", 1.5);
            params.alignmentWeight = config.getParameter("alignmentWeight", 1.0);
            params.cohesionWeight = config.getParameter("cohesionWeight", 1.0);
            params.separationRadius = config.getParameter("separationRadius", 30.0);
            params.alignmentRadius = config.getParameter("alignmentRadius", 50.0);
            params.cohesionRadius = config.getParameter("cohesionRadius", 80.0);
            
            flockingController.setParameters(params);
        }
        
        System.out.println("Behavior configuration updated");
    }
    
    private void updateNetworkConfiguration(NetworkConfiguration config) {
        this.networkConfig = config;
        
        // Update communication manager
        if (communicationManager != null) {
            communicationManager.updateConfiguration(config);
        }
        
        System.out.println("Network configuration updated");
    }
    
    private void updateUIConfiguration(UIConfiguration config) {
        this.uiConfig = config;
        System.out.println("UI configuration updated");
    }
    
    // ==================== EVENT HANDLERS ====================
    
    private void handleAgentStateUpdate(AgentStateUpdate update) {
        // Update communication topology when agents move
        if (communicationManager != null) {
            communicationManager.updateTopology(agentManager.getAllAgentStates());
        }
    }
    
    private void handleVoteResult(VoteResult result) {
        System.out.println("Vote result: " + result.getSummaryMessage());
        
        // Publish decision status to UI
        DecisionStatus status = new DecisionStatus();
        status.proposalId = result.proposalId;
        status.consensusReached = result.consensusReached;
        status.winningOption = result.winningOption;
        status.isExecuted = true;
        
        eventBus.publish(status);
    }
    
    // ==================== SIMULATION LOOP ====================
    
    /**
     * Start simulation loop in separate thread
     */
    public void startSimulation() {
        if (simulationRunning) {
            return;
        }
        
        simulationRunning = true;
        lastUpdateTime = System.currentTimeMillis();
        
        simulationThread = new Thread(this::simulationLoop, "SimulationThread");
        simulationThread.setDaemon(true);
        simulationThread.start();
        
        System.out.println("Simulation started");
    }
    
    /**
     * Stop simulation loop
     */
    public void stopSimulation() {
        if (!simulationRunning) {
            return;
        }
        
        simulationRunning = false;
        
        if (simulationThread != null) {
            try {
                simulationThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Simulation stopped");
    }
    
    /**
     * Main simulation loop - runs at 30 FPS
     */
    private void simulationLoop() {
        while (simulationRunning) {
            long currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastUpdateTime) / 1000.0 * simulationSpeed;
            
            try {
                // Update all agents
                if (agentManager != null) {
                    agentManager.updateAll(deltaTime);
                }
                
                // Update communication topology
                if (communicationManager != null) {
                    communicationManager.updateTopology(agentManager.getAllAgentStates());
                }
                
                // Process flocking behaviors
                if (flockingController != null) {
                    List<AgentState> agents = agentManager.getAllAgentStates();
                    for (AgentState state : agents) {
                        List<NeighborAgent> neighbors = getNeighbors(state);
                        MovementCommand cmd = flockingController.calculateFlocking(
                            state.agentId, neighbors
                        );
                        
                        if (cmd != null) {
                            Agent agent = agentManager.getAgent(state.agentId);
                            if (agent != null) {
                                agent.applyMovementCommand(cmd);
                            }
                        }
                    }
                }
                
                // Update formations
                if (formationController != null) {
                    formationController.update(agentManager.getAllAgentStates(), deltaTime);
                }
                
                // Process tasks
                if (taskAllocator != null) {
                    taskAllocator.update(agentManager.getAllAgentStates(), deltaTime);
                }
                
                // Check voting timeouts
                if (votingSystem != null) {
                    votingSystem.expireProposals();
                }
                
                // Publish visualization update
                publishVisualizationUpdate();
                
                // Update FPS counter
                updateFpsCounter();
                
            } catch (Exception e) {
                System.err.println("Error in simulation loop: " + e.getMessage());
                e.printStackTrace();
            }
            
            lastUpdateTime = currentTime;
            
            // Sleep to maintain 30 FPS
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Get neighbors for an agent (within communication range)
     */
    private List<NeighborAgent> getNeighbors(AgentState agent) {
        List<NeighborAgent> neighbors = new ArrayList<>();
        List<AgentState> allAgents = agentManager.getAllAgentStates();
        
        for (AgentState other : allAgents) {
            if (other.agentId == agent.agentId) {
                continue;
            }
            
            double distance = agent.position.distanceTo(other.position);
            if (distance <= agent.communicationRange) {
                NeighborAgent neighbor = new NeighborAgent();
                neighbor.agentId = other.agentId;
                neighbor.position = other.position;
                neighbor.velocity = other.velocity;
                neighbor.distance = distance;
                neighbors.add(neighbor);
            }
        }
        
        return neighbors;
    }
    
    /**
     * Publish visualization update to UI
     */
    private void publishVisualizationUpdate() {
        if (agentManager == null) {
            return;
        }
        
        VisualizationUpdate update = agentManager.getVisualizationUpdate();
        update.systemMetrics.fps = currentFps;
        update.systemMetrics.commandsExecuted = commandsExecuted;
        
        eventBus.publish(update);
        
        // Publish network status
        if (communicationManager != null) {
            NetworkStatus networkStatus = communicationManager.getNetworkStatus();
            eventBus.publish(networkStatus);
        }
        
        // Publish decision status if voting active
        if (votingSystem != null) {
            Map<String, VoteProposal> activeVotes = votingSystem.getActiveProposals();
            for (Map.Entry<String, VoteProposal> entry : activeVotes.entrySet()) {
                VoteResult result = votingSystem.checkConsensus(entry.getKey());
                
                DecisionStatus status = new DecisionStatus();
                status.proposalId = entry.getKey();
                status.question = entry.getValue().question;
                status.options = entry.getValue().options;
                status.totalAgents = agentManager.getAgentCount();
                status.votesReceived = votingSystem.getVotesForProposal(entry.getKey()).size();
                status.consensusReached = result.consensusReached;
                status.winningOption = result.winningOption;
                status.isPending = !result.consensusReached;
                status.updateProgress();
                
                eventBus.publish(status);
            }
        }
    }
    
    /**
     * Update FPS counter
     */
    private void updateFpsCounter() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastFpsTime >= 1000) {
            currentFps = frameCount / ((currentTime - lastFpsTime) / 1000.0);
            frameCount = 0;
            lastFpsTime = currentTime;
        }
    }
    
    // ==================== COMMAND HISTORY ====================
    
    private void addToHistory(SystemCommand cmd) {
        commandHistory.add(cmd);
        if (commandHistory.size() > MAX_HISTORY) {
            commandHistory.remove(0);
        }
    }
    
    public List<SystemCommand> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }
    
    // ==================== GETTERS ====================
    
    public boolean isSimulationRunning() {
        return simulationRunning;
    }
    
    public double getSimulationSpeed() {
        return simulationSpeed;
    }
    
    public void setSimulationSpeed(double speed) {
        this.simulationSpeed = Math.max(0.1, Math.min(5.0, speed));
    }
    
    public double getWorldWidth() {
        return worldWidth;
    }
    
    public double getWorldHeight() {
        return worldHeight;
    }
    
    public AgentManager getAgentManager() {
        return agentManager;
    }
    
    public CommunicationManager getCommunicationManager() {
        return communicationManager;
    }
    
    public VotingSystem getVotingSystem() {
        return votingSystem;
    }
    
    public FlockingController getFlockingController() {
        return flockingController;
    }
    
    public FormationController getFormationController() {
        return formationController;
    }
    
    public TaskAllocator getTaskAllocator() {
        return taskAllocator;
    }
    
    public BehaviorConfiguration getBehaviorConfiguration() {
        return behaviorConfig;
    }
    
    public NetworkConfiguration getNetworkConfiguration() {
        return networkConfig;
    }
    
    public UIConfiguration getUIConfiguration() {
        return uiConfig;
    }
    
    public double getCurrentFps() {
        return currentFps;
    }
    
    public int getCommandsExecuted() {
        return commandsExecuted;
    }
}
