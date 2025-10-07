/**
 * COMMUNICATION EVENT - Agent-to-Agent Message Data
 *
 * PURPOSE:
 * - Represents messages exchanged between agents in the swarm
 * - Enables decentralized coordination through information sharing
 * - Supports range-limited communication (agents only hear nearby neighbors)
 *
 * MAIN COMPONENTS:
 * 1. Sender Information - Which agent sent the message
 * 2. Message Content - Data being communicated
 * 3. Message Type - Purpose/category of message
 * 4. Communication Range - How far the message travels
 * 5. Timestamp - When message was sent
 *
 * MESSAGE TYPES:
 * - STATE_BROADCAST: "Here's my position/velocity/status"
 * - TASK_ASSIGNMENT: "You should do task X"
 * - FORMATION_UPDATE: "New formation target position"
 * - COLLISION_WARNING: "I'm about to hit you!"
 * - TASK_COMPLETE: "I finished my assigned task"
 *
 * CORE FIELDS:
 * - senderId: Agent that created this message
 * - messageType: Category of communication
 * - payload: Actual message data (Map<String, Object>)
 * - broadcastRange: Max distance message travels
 * - timestamp: Message creation time
 *
 * COMMUNICATION FLOW:
 * 1. Agent creates CommunicationEvent
 * 2. Publishes to EventBus
 * 3. EventBus delivers to agents within communication range
 * 4. Receiving agents process message based on type
 *
 * RANGE FILTERING:
 * - Only agents within sendingAgent.communicationRange receive message
 * - Distance calculated using Point2D.distanceTo()
 * - Enables realistic local communication constraints
 *
 * INTEGRATION POINTS:
 * - Created by: Agents during update cycle
 * - Published to: EventBus
 * - Filtered by: Range-based delivery system
 * - Consumed by: Other agents within range
 */
package com.team6.swarm.core;

import java.util.Map;

public class CommunicationEvent {
    public int senderId;
    public Messagetype messagetype;
    public Map<String, Object> payload; 
    public double broadcastRange;
    public long timestamp;

    public enum Messagetype{
        STATE_BROADCAST,
        TASK_ASSIGNMENT,
        FORMATION_UPDATE,
        COLLISION_WARNING,
        TASK_COMPLETE
    }
}

