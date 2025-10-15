/**
 * MOVEMENTCOMMAND CLASS - Agent Instruction Container
 *
 * PURPOSE:
 * - Encapsulates movement instructions for individual agents
 * - Implements command pattern for decoupled agent control
 * - Enables queuing and batch processing of agent commands
 *
 * CORE COMPONENTS:
 * 1. agentId - Target agent identifier
 * 2. type - MovementType enum specifying behavior
 * 3. priority - CommandPriority for urgency handling
 * 4. parameters - Flexible key-value store for command data
 * 5. timestamp - Command creation time for ordering/timeout
 *
 * LOGIC:
 * - Commands are created by AI/control systems
 * - Queued in agent's command queue (priority-based)
 * - Processed during agent update loop
 * - Parameters vary based on MovementType
 * - Priority ensures critical commands execute first
 *
 * PARAMETER EXAMPLES BY TYPE:
 * MOVE_TO_TARGET:
 *   - "target" -> Point2D(x, y)
 * FLOCKING_BEHAVIOR:
 *   - "combinedForce" -> Vector2D(fx, fy)
 *   - "neighbors" -> List<AgentState>
 * FORMATION_POSITION:
 *   - "formationPos" -> Point2D(x, y)
 *   - "leaderPos" -> Point2D(x, y)
 * AVOID_OBSTACLE:
 *   - "obstacle" -> Point2D(x, y)
 *   - "avoidanceForce" -> Vector2D(fx, fy)
 *
 * EXPECTED USAGE:
 * MovementCommand cmd = new MovementCommand();
 * cmd.agentId = 5;
 * cmd.type = MovementType.MOVE_TO_TARGET;
 * cmd.priority = CommandPriority.NORMAL;
 * cmd.parameters.put("target", new Point2D(200, 300));
 * agent.addMovementCommand(cmd);
 *
 * TIMING:
 * - timestamp auto-set to System.currentTimeMillis()
 * - Used for command aging and timeout detection
 * - Helps prevent stale commands from executing
 */
package com.team6.swarm.core;

import java.util.Map;
import java.util.HashMap;

public class MovementCommand implements Comparable<MovementCommand> {
    public int agentId;
    public MovementType type;
    public CommandPriority priority;
    public Map<String, Object> parameters;
    public long timestamp;
    public String taskId;  // Optional: Link to task system

    public MovementCommand() {
        this.parameters = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.priority = CommandPriority.NORMAL;  // Default priority
        this.taskId = null;
    }

    public MovementCommand(int agentId, MovementType type, CommandPriority priority) {
        this();
        this.agentId = agentId;
        this.type = type;
        this.priority = priority;
    }

    /**
     * Compare commands by priority for PriorityQueue
     * Higher priority (EMERGENCY) comes before lower priority (LOW)
     */
    @Override
    public int compareTo(MovementCommand other) {
        // Compare priorities (EMERGENCY=3, HIGH=2, NORMAL=1, LOW=0)
        int priorityDiff = other.priority.ordinal() - this.priority.ordinal();

        // If same priority, older commands first (FIFO within same priority)
        if (priorityDiff == 0) {
            return Long.compare(this.timestamp, other.timestamp);
        }

        return priorityDiff;
    }

    /**
     * Check if command is stale (older than timeout)
     */
    public boolean isStale(long timeoutMs) {
        return (System.currentTimeMillis() - timestamp) > timeoutMs;
    }

    @Override
    public String toString() {
        return String.format("MovementCommand[agent=%d, type=%s, priority=%s, taskId=%s]",
                agentId, type, priority, taskId);
    }
}