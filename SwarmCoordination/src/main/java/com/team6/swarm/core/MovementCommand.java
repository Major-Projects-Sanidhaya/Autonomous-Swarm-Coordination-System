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
 * 3. parameters - Flexible key-value store for command data
 * 4. timestamp - Command creation time for ordering/timeout
 *
 * LOGIC:
 * - Commands are created by AI/control systems
 * - Queued in agent's command queue (thread-safe)
 * - Processed during agent update loop
 * - Parameters vary based on MovementType
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
 *
 * EXPECTED USAGE:
 * MovementCommand cmd = new MovementCommand();
 * cmd.agentId = 5;
 * cmd.type = MovementType.MOVE_TO_TARGET;
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

public class MovementCommand {
    public int agentId;
    public MovementType type;
    public Map<String, Object> parameters;
    public long timestamp;
    
    public MovementCommand() {
        this.parameters = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
}