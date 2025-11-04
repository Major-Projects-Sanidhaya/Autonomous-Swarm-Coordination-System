/**
 * AGENT CLASS - Individual Swarm Entity (Week 3 Complete)
 *
 * PURPOSE:
 * - Represents single autonomous agent in swarm system
 * - Handles movement, physics, battery management, and state updates
 * - Processes prioritized movement commands from Lauren's intelligence system
 * - Publishes task completion reports for feedback loop
 *
 * WEEK 3 ENHANCEMENTS:
 * - PriorityBlockingQueue for priority-based command execution
 * - Support for all 4 MovementTypes (MOVE_TO_TARGET, FLOCKING_BEHAVIOR, FORMATION_POSITION, AVOID_OBSTACLE)
 * - Task completion reporting via EventBus
 * - PhysicsEngine integration for realistic movement
 *
 * MAIN COMPONENTS:
 * 1. AgentState - Current agent status and properties
 * 2. PriorityBlockingQueue - Priority-based command queue
 * 3. PhysicsEngine - Movement and collision handling
 * 4. EventBus - Communication with other components
 * 5. Task tracking - Monitor current task execution
 *
 * CORE FUNCTIONS:
 * 1. update(deltaTime) - Main simulation loop called each frame
 * 2. addMovementCommand() - Queue new movement instructions (priority-aware)
 * 3. getState() - Access current agent state
 * 4. processCommands() - Execute queued movement commands by priority
 * 5. executeMovementCommand() - Handle all 4 MovementTypes
 * 6. completeTask() - Publish TaskCompletionReport
 *
 * UPDATE CYCLE LOGIC (called 30-60 times/second):
 * 1. Process pending movement commands (highest priority first)
 * 2. Update physics using PhysicsEngine
 * 3. Check if current task is complete
 * 4. Update battery level
 * 5. Publish state changes
 *
 * MOVEMENT EXECUTION:
 * - Commands processed by PRIORITY (EMERGENCY > HIGH > NORMAL > LOW)
 * - Within same priority, FIFO ordering
 * - Switch statement handles all MovementTypes
 * - PhysicsEngine enforces speed limits and boundaries
 *
 * TASK COMPLETION LOGIC:
 * - MOVE_TO_TARGET: Complete when within threshold of target
 * - FLOCKING_BEHAVIOR: Applied immediately (no discrete completion)
 * - FORMATION_POSITION: Complete when within threshold of formation position
 * - AVOID_OBSTACLE: Complete when no longer threatened
 */
package com.team6.swarm.core;

import java.util.concurrent.PriorityBlockingQueue;

public class Agent {
    private final AgentState state;
    private final PriorityBlockingQueue<MovementCommand> commandQueue;
    private final PhysicsEngine physics;
    private EventBus eventBus;  // For publishing reports
    private MovementCommand currentCommand;  // Track current task
    private long taskStartTime;

    // Task completion threshold
    private static final double ARRIVAL_THRESHOLD = 5.0;  // Distance to consider "arrived"
    private static final long COMMAND_TIMEOUT_MS = 30000;  // 30 seconds

    public Agent(int id, Point2D initialPosition) {
        this.state = new AgentState();
        this.state.agentId = id;
        this.state.agentName = "Agent_" + id;
        this.state.position = initialPosition;
        this.state.velocity = new Vector2D(0, 0);
        this.state.status = AgentStatus.ACTIVE;
        this.state.batteryLevel = 1.0;
        this.state.maxSpeed = 50.0;
        this.state.communicationRange = 100.0;
        this.state.lastUpdateTime = System.currentTimeMillis();

        // Use PriorityBlockingQueue for priority-based command execution
        this.commandQueue = new PriorityBlockingQueue<>();
        this.physics = new PhysicsEngine();
        this.eventBus = null;  // Set via setEventBus()
        this.currentCommand = null;
        this.taskStartTime = 0;
    }

    /**
     * Set the EventBus for publishing reports
     */
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Main update loop - called 30-60 times per second
     */
    public void update(double deltaTime) {
        // Process any pending movement commands (priority order)
        processCommands();

        // Check if current task is complete
        checkTaskCompletion();

        // Update physics using PhysicsEngine
        physics.updatePosition(state, deltaTime);

        // Update battery (decreases over time)
        updateBattery(deltaTime);

        // Update timestamp
        state.lastUpdateTime = System.currentTimeMillis();

        // Publish state update to EventBus
        publishStateUpdate();
    }

    /**
     * Add movement command to priority queue
     */
    public void addMovementCommand(MovementCommand command) {
        if (command.agentId != state.agentId) {
            System.err.println("Warning: Command for agent " + command.agentId +
                             " sent to agent " + state.agentId);
            return;
        }
        commandQueue.offer(command);
    }

    /**
     * Get current agent state
     */
    public AgentState getState() {
        return state;
    }

    /**
     * Process commands from priority queue
     * Only process one command per update cycle to allow smooth execution
     */
    private void processCommands() {
        // If currently executing a command, don't start a new one yet
        if (currentCommand != null) {
            return;
        }

        // Get next command from priority queue
        MovementCommand cmd = commandQueue.poll();
        if (cmd != null) {
            // Check if command is stale (timeout)
            if (cmd.isStale(COMMAND_TIMEOUT_MS)) {
                System.out.println("Agent " + state.agentId + ": Command timed out - " + cmd);
                completeTask(cmd, TaskCompletionReport.CompletionStatus.TIMEOUT);
                return;
            }

            // Execute the command
            currentCommand = cmd;
            taskStartTime = System.currentTimeMillis();
            executeMovementCommand(cmd);
        }
    }

    /**
     * Execute movement command based on type
     * Handles all 4 MovementTypes from Week 3
     */
    private void executeMovementCommand(MovementCommand cmd) {
        switch (cmd.type) {
            case MOVE_TO_TARGET:
                Point2D target = (Point2D) cmd.parameters.get("target");
                if (target != null) {
                    moveToward(target);
                } else {
                    System.err.println("Agent " + state.agentId + ": MOVE_TO_TARGET missing 'target' parameter");
                    completeTask(cmd, TaskCompletionReport.CompletionStatus.FAILED);
                }
                break;

            case FLOCKING_BEHAVIOR:
                Vector2D force = (Vector2D) cmd.parameters.get("combinedForce");
                if (force != null) {
                    applyForceFromCommand(force);
                    // Flocking is continuous, complete immediately
                    completeTask(cmd, TaskCompletionReport.CompletionStatus.SUCCESS);
                } else {
                    System.err.println("Agent " + state.agentId + ": FLOCKING_BEHAVIOR missing 'combinedForce' parameter");
                    completeTask(cmd, TaskCompletionReport.CompletionStatus.FAILED);
                }
                break;

            case FORMATION_POSITION:
                Point2D formationPos = (Point2D) cmd.parameters.get("formationPos");
                if (formationPos != null) {
                    moveToward(formationPos);
                } else {
                    System.err.println("Agent " + state.agentId + ": FORMATION_POSITION missing 'formationPos' parameter");
                    completeTask(cmd, TaskCompletionReport.CompletionStatus.FAILED);
                }
                break;

            case AVOID_OBSTACLE:
                Point2D obstacle = (Point2D) cmd.parameters.get("obstacle");
                Vector2D avoidanceForce = (Vector2D) cmd.parameters.get("avoidanceForce");

                if (obstacle != null && avoidanceForce != null) {
                    // Apply avoidance force immediately
                    applyForceFromCommand(avoidanceForce);
                    completeTask(cmd, TaskCompletionReport.CompletionStatus.SUCCESS);
                } else if (obstacle != null) {
                    // Calculate avoidance if force not provided
                    Vector2D fleeForce = physics.flee(state, obstacle, state.maxSpeed);
                    applyForceFromCommand(fleeForce);
                    completeTask(cmd, TaskCompletionReport.CompletionStatus.SUCCESS);
                } else {
                    System.err.println("Agent " + state.agentId + ": AVOID_OBSTACLE missing 'obstacle' parameter");
                    completeTask(cmd, TaskCompletionReport.CompletionStatus.FAILED);
                }
                break;

            default:
                System.err.println("Agent " + state.agentId + ": Unknown MovementType - " + cmd.type);
                completeTask(cmd, TaskCompletionReport.CompletionStatus.FAILED);
                break;
        }
    }

    /**
     * Check if current task is complete
     */
    private void checkTaskCompletion() {
        if (currentCommand == null) {
            return;
        }

        boolean isComplete = false;

        switch (currentCommand.type) {
            case MOVE_TO_TARGET:
                Point2D target = (Point2D) currentCommand.parameters.get("target");
                if (target != null) {
                    double distance = state.position.distanceTo(target);
                    if (distance < ARRIVAL_THRESHOLD) {
                        isComplete = true;
                    }
                }
                break;

            case FORMATION_POSITION:
                Point2D formationPos = (Point2D) currentCommand.parameters.get("formationPos");
                if (formationPos != null) {
                    double distance = state.position.distanceTo(formationPos);
                    if (distance < ARRIVAL_THRESHOLD) {
                        isComplete = true;
                    }
                }
                break;

            case FLOCKING_BEHAVIOR:
            case AVOID_OBSTACLE:
                // These complete immediately in executeMovementCommand
                break;
        }

        if (isComplete) {
            completeTask(currentCommand, TaskCompletionReport.CompletionStatus.SUCCESS);
        }
    }

    /**
     * Complete task and publish report
     */
    private void completeTask(MovementCommand cmd, TaskCompletionReport.CompletionStatus status) {
        if (cmd == null) return;

        // Calculate duration
        double duration = (System.currentTimeMillis() - taskStartTime) / 1000.0;

        // Create completion report
        String taskId = cmd.taskId != null ? cmd.taskId : "cmd_" + cmd.timestamp;
        TaskCompletionReport report = new TaskCompletionReport(taskId, state.agentId, status);
        report.setDuration(duration);
        report.addResult("commandType", cmd.type.toString());
        report.addResult("priority", cmd.priority.toString());
        report.addResult("finalPosition", new Point2D(state.position.x, state.position.y));
        report.addResult("batteryLevel", state.batteryLevel);

        // Publish to EventBus
        if (eventBus != null) {
            eventBus.publish(report);
        }

        // Log completion
        System.out.println("Agent " + state.agentId + ": Task " + status + " - " + cmd.type +
                         " (duration: " + String.format("%.2f", duration) + "s)");

        // Clear current command
        if (currentCommand == cmd) {
            currentCommand = null;
        }
    }

    /**
     * Move toward target using seek steering
     */
    private void moveToward(Point2D target) {
        if (target == null) return;

        // Use PhysicsEngine's seek function
        Vector2D seekForce = physics.seek(state, target, state.maxSpeed);
        state.velocity = state.velocity.add(seekForce.multiply(0.1));  // Apply with damping

        // Limit to max speed
        physics.limitVelocity(state, state.maxSpeed);
    }

    /**
     * Apply force from movement command
     */
    private void applyForceFromCommand(Vector2D force) {
        if (force == null) return;

        // Apply force to velocity
        state.velocity = state.velocity.add(force);

        // Limit to max speed
        physics.limitVelocity(state, state.maxSpeed);
    }

    /**
     * Update battery level
     */
    private void updateBattery(double deltaTime) {
        // Battery depletes based on speed and time
        double consumption = (state.velocity.magnitude() / state.maxSpeed) * 0.001 * deltaTime;
        state.batteryLevel = Math.max(0, state.batteryLevel - consumption);

        // Update status based on battery
        if (state.batteryLevel < 0.2 && state.batteryLevel > 0) {
            state.status = AgentStatus.BATTERY_LOW;
        } else if (state.batteryLevel <= 0) {
            state.status = AgentStatus.FAILED;
            state.velocity = new Vector2D(0, 0);  // Stop moving
        } else {
            state.status = AgentStatus.ACTIVE;
        }
    }

    /**
     * Publish state update to EventBus
     */
    private void publishStateUpdate() {
        if (eventBus != null) {
            AgentStateUpdate update = new AgentStateUpdate();
            update.agentId = state.agentId;
            update.agentState = state;
            update.updateType = AgentStateUpdate.UpdateType.FULL_STATE;
            update.timestamp = System.currentTimeMillis();
            eventBus.publish(update);
        }
    }

    /**
     * Get current command queue size (for debugging)
     */
    public int getQueueSize() {
        return commandQueue.size();
    }

    /**
     * Check if agent is currently executing a command
     */
    public boolean isBusy() {
        return currentCommand != null;
    }
}
