/**
 * AGENT STATE UPDATE - State Change Notification Data
 *
 * PURPOSE:
 * - Encapsulates agent state changes for event-driven communication
 * - Published through EventBus when agent state changes occur
 * - Enables decoupled notification of agent updates to interested components
 *
 * MAIN COMPONENTS:
 * 1. Agent Identification - Which agent changed
 * 2. State Snapshot - Current state data
 * 3. Timestamp - When the update occurred
 * 4. Change Type - What kind of update (position, status, battery, etc.)
 *
 * CORE DATA FIELDS:
 * - agentId: Unique identifier of the agent
 * - agentState: Complete current state of the agent
 * - updateType: Type of change (POSITION, STATUS, BATTERY, FULL_STATE)
 * - timestamp: When this update was generated
 *
 * USAGE PATTERN:
 * 1. Agent detects state change during update cycle
 * 2. Creates AgentStateUpdate with current state
 * 3. Publishes to EventBus
 * 4. Subscribers (UI, logger, communication) receive and process
 *
 * EXPECTED OUTPUTS:
 * - Published via EventBus to registered listeners
 * - Used by SystemController for centralized state tracking
 * - Used by visualization system for UI updates
 * - Used by communication system for broadcasting to other agents
 *
 * INTEGRATION POINTS:
 * - Created by: Agent.publishStateUpdate()
 * - Published to: EventBus
 * - Consumed by: SystemController, VisualizationUpdate, CommunicationEvent handlers
 */
package com.team6.swarm.core;

public class AgentStateUpdate {
    public int agentId;
    public AgentState agentState;
    public UpdateType updateType;
    public long timestamp;

    public enum UpdateType{
        POSITION, STATUS, BATTERY, FULL_STATE
    }
}
