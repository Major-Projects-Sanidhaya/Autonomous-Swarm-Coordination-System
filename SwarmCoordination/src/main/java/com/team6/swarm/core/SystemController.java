/**
 * SYSTEM CONTROLLER - Master Orchestrator for Swarm Simulation
 *
 * PURPOSE:
 * - Central coordinator for entire swarm system
 * - Manages simulation lifecycle (start, pause, stop)
 * - Coordinates timing and synchronization across all agents
 * - Integrates all subsystems (agents, communication, visualization, metrics)
 *
 * MAIN COMPONENTS:
 * 1. AgentManager - Controls all agent instances
 * 2. EventBus - Message routing between components
 * 3. Simulation Loop - Main update cycle (30-60 FPS)
 * 4. SystemMetrics - Performance tracking
 * 5. State Management - Overall system state
 *
 * CORE FUNCTIONS:
 * 1. initialize() - Set up all subsystems
 * 2. start() - Begin simulation loop
 * 3. pause() - Temporarily halt updates
 * 4. stop() - Shutdown cleanly
 * 5. update(deltaTime) - Drive one simulation frame
 * 6. getSystemState() - Access current system status
 *
 * SIMULATION LOOP (runs 30-60 times/second):
 * 1. Calculate deltaTime since last frame
 * 2. Call agentManager.updateAll(deltaTime)
 * 3. Process EventBus messages
 * 4. Update SystemMetrics
 * 5. Publish VisualizationUpdate
 * 6. Sleep to maintain target FPS
 *
 * LIFECYCLE STATES:
 * - INITIALIZING: Setting up components
 * - READY: Initialized but not running
 * - RUNNING: Simulation loop active
 * - PAUSED: Loop suspended, state preserved
 * - STOPPED: Clean shutdown complete
 *
 * TIMING CONTROL:
 * - Target FPS: 60 (configurable)
 * - deltaTime calculation: currentTime - lastUpdateTime
 * - Frame skipping: If falling behind, limit deltaTime max
 * - Sleep: Thread.sleep() to maintain consistent frame rate
 *
 * INTEGRATION ARCHITECTURE:
 * SystemController
 *   ├── AgentManager (manages all agents)
 *   ├── EventBus (message routing)
 *   ├── SystemMetrics (performance monitoring)
 *   └── Update Loop (drives simulation)
 *
 * INITIALIZATION ORDER:
 * 1. Create EventBus
 * 2. Create AgentManager with EventBus reference
 * 3. Register EventBus subscribers
 * 4. Create initial agent population
 * 5. Initialize SystemMetrics
 * 6. Transition to READY state
 *
 * EXPECTED OUTPUTS:
 * - Console: "System started with 20 agents at 60 FPS"
 * - Metrics: "Update time: 8ms, Agents active: 20/20"
 * - Events: VisualizationUpdate published each frame
 * - State: Coordinates all agent updates in sync
 *
 * THREAD SAFETY:
 * - Main simulation loop runs on dedicated thread
 * - External commands (pause, stop) synchronized
 * - EventBus handles concurrent publish/subscribe
 */
package com.team6.swarm.core;

import com.team6.swarm.core.Point2D;
import com.team6.swarm.core.AgentStateUpdate;
import com.team6.swarm.core.TaskCompletionReport;
import com.team6.swarm.core.CommunicationEvent;

public class SystemController {
    private AgentManager agentManager;
    private EventBus eventBus;
    private SystemMetrics metrics;
    private SimulationState state;
    private int targetFPS = 60;
    private Thread simulationThread;
    private long lastUpdateTime;
    private volatile boolean running;

    public enum SimulationState {
        INITIALIZING,  // Setting up components
        READY,         // Initialized but not running
        RUNNING,       // Simulation loop active
        PAUSED,        // Loop suspended, state preserved
        STOPPED        // Clean shutdown complete
    }

    public SystemController() {
        this.state = SimulationState.INITIALIZING;
    }

    /**
     * Initialize all subsystems
     * Call this before start()
     */
    public void initialize() {
        System.out.println("SystemController: Initializing...");

        // 1. Create EventBus
        this.eventBus = new EventBus();

        // 2. Create AgentManager with EventBus reference
        this.agentManager = new AgentManager(eventBus);

        // 3. Create SystemMetrics
        this.metrics = new SystemMetrics();

        // 4. Register EventBus subscribers
        registerEventSubscribers();

        // 5. Create initial agent population (example: 5 agents)
        initializeAgents(5);

        this.state = SimulationState.READY;
        System.out.println("SystemController: Initialized successfully");
    }

    /**
     * Create initial population of agents
     */
    private void initializeAgents(int count) {
        for (int i = 1; i <= count; i++) {
            Point2D position = new Point2D(
                Math.random() * 800,  // Random x position
                Math.random() * 600   // Random y position
            );
            agentManager.createAgent(i, position);
        }
        System.out.println("Created " + count + " agents");
    }

    /**
     * Register event listeners for system-wide events
     */
    private void registerEventSubscribers() {
        // Subscribe to AgentStateUpdate events
        eventBus.subscribe(AgentStateUpdate.class, this::handleAgentStateUpdate);

        // Subscribe to TaskCompletionReport events
        eventBus.subscribe(TaskCompletionReport.class, this::handleTaskCompletion);

        // Subscribe to CommunicationEvent events
        eventBus.subscribe(CommunicationEvent.class, this::handleCommunication);
    }

    /**
     * Start the simulation loop
     */
    public void start() {
        if (state != SimulationState.READY && state != SimulationState.PAUSED) {
            System.err.println("Cannot start: System must be READY or PAUSED");
            return;
        }

        this.state = SimulationState.RUNNING;
        this.running = true;
        this.lastUpdateTime = System.currentTimeMillis();

        // Run simulation in separate thread
        simulationThread = new Thread(this::runSimulationLoop, "SimulationThread");
        simulationThread.start();

        System.out.println("SystemController: Started simulation at " + targetFPS + " FPS");
    }

    /**
     * Main simulation loop - runs in separate thread
     */
    private void runSimulationLoop() {
        while (running && state == SimulationState.RUNNING) {
            long frameStart = System.currentTimeMillis();

            // Calculate time since last frame (in seconds)
            double deltaTime = (frameStart - lastUpdateTime) / 1000.0;
            lastUpdateTime = frameStart;

            // Limit deltaTime to prevent huge jumps
            deltaTime = Math.min(deltaTime, 0.1); // Max 100ms per frame

            // Update all agents
            agentManager.updateAll(deltaTime);

            // Update metrics
            metrics.update(agentManager.getAgentCount(), deltaTime);

            // Maintain target frame rate
            maintainFrameRate(frameStart);
        }
    }

    /**
     * Sleep to maintain consistent frame rate
     */
    private void maintainFrameRate(long frameStart) {
        long frameTime = System.currentTimeMillis() - frameStart;
        long targetFrameTime = 1000 / targetFPS;
        long sleepTime = targetFrameTime - frameTime;

        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Pause the simulation
     */
    public synchronized void pause() {
        if (state == SimulationState.RUNNING) {
            this.state = SimulationState.PAUSED;
            System.out.println("SystemController: Paused");
        }
    }

    /**
     * Resume from pause
     */
    public synchronized void resume() {
        if (state == SimulationState.PAUSED) {
            this.state = SimulationState.RUNNING;
            this.lastUpdateTime = System.currentTimeMillis(); // Reset time
            System.out.println("SystemController: Resumed");
        }
    }

    /**
     * Stop the simulation and clean up
     */
    public void stop() {
        System.out.println("SystemController: Stopping...");
        this.running = false;
        this.state = SimulationState.STOPPED;

        // Wait for simulation thread to finish
        if (simulationThread != null) {
            try {
                simulationThread.join(2000); // Wait max 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("SystemController: Stopped");
    }

    // Event handlers
    private void handleAgentStateUpdate(AgentStateUpdate update) {
        // SystemController can log or track state changes
        // For now, just track in metrics
        metrics.recordStateUpdate();
    }

    private void handleTaskCompletion(TaskCompletionReport report) {
        System.out.println("Task completed: " + report);
        metrics.recordTaskCompletion(report.status);
    }

    private void handleCommunication(CommunicationEvent event) {
        // SystemController can monitor communication
        metrics.recordCommunication();
    }

    // Getters
    public SimulationState getState() {
        return state;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public SystemMetrics getMetrics() {
        return metrics;
    }

    public void setTargetFPS(int fps) {
        this.targetFPS = fps;
    }
}

