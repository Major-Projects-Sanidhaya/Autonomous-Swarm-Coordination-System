/**
 * MESSAGE CLASS - Standardized Message Container
 *
 * PURPOSE:
 * - Standardized container for all communication between agents
 * - Encapsulates message content with metadata
 * - Foundation for all inter-agent communication
 *
 * CORE COMPONENTS:
 * 1. messageId - Unique identifier for tracking and deduplication
 * 2. type - MessageType enum specifying message category
 * 3. payload - The actual data content (Object for flexibility)
 * 4. timestamp - When the message was created
 * 5. metadata - Optional extra information (Map for extensibility)
 *
 * MESSAGE LIFECYCLE:
 * 1. Creation: Lauren/Agent creates Message with content
 * 2. Wrapping: Wrapped in OutgoingMessage with routing info
 * 3. Routing: CommunicationManager routes to destination
 * 4. Delivery: Wrapped in IncomingMessage with delivery details
 * 5. Processing: Recipient processes the original Message
 *
 * PAYLOAD EXAMPLES BY TYPE:
 * POSITION_UPDATE:
 *   - payload: Map<String, Object> with "position" (Point2D), "velocity" (Vector2D)
 * VOTE_PROPOSAL:
 *   - payload: Map<String, Object> with "proposal" (String), "options" (List<String>)
 * TASK_ASSIGNMENT:
 *   - payload: Map<String, Object> with "taskId" (String), "taskType" (String), "parameters" (Map)
 * EMERGENCY_ALERT:
 *   - payload: Map<String, Object> with "alertType" (String), "description" (String), "location" (Point2D)
 *
 * METADATA USAGE:
 * - "priority": Integer (1=highest, 5=lowest)
 * - "ttl": Integer (time-to-live in milliseconds)
 * - "encryption": Boolean (whether message is encrypted)
 * - "source": String (original creator of message)
 * - "version": String (message format version)
 *
 * THREAD SAFETY:
 * - Immutable after creation (fields are final)
 * - Safe for concurrent access and queuing
 * - Payload should be immutable or thread-safe
 *
 * EXPECTED USAGE:
 * Message msg = new Message(MessageType.POSITION_UPDATE, positionData);
 * OutgoingMessage outMsg = new OutgoingMessage(senderId, receiverId, msg);
 * CommunicationManager.sendMessage(outMsg);
 */
package com.team6.swarm.communication;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Message {
    public final String messageId;
    public final MessageType type;
    public final Object payload;
    public final long timestamp;
    public final Map<String, Object> metadata;
    
    public Message(MessageType type, Object payload) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }
    
    public Message(MessageType type, Object payload, Map<String, Object> metadata) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>(metadata);
    }
    
    /**
     * Get metadata value with type safety
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> expectedType) {
        Object value = metadata.get(key);
        if (value != null && expectedType.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Check if message has expired based on TTL
     */
    public boolean isExpired() {
        Integer ttl = getMetadata("ttl", Integer.class);
        if (ttl != null) {
            return (System.currentTimeMillis() - timestamp) > ttl;
        }
        return false;
    }
    
    /**
     * Get message priority (1=highest, 5=lowest, default=3)
     */
    public int getPriority() {
        Integer priority = getMetadata("priority", Integer.class);
        return priority != null ? priority : 3;
    }
    
    @Override
    public String toString() {
        return String.format("Message{id=%s, type=%s, timestamp=%d, payload=%s}", 
                           messageId, type, timestamp, payload);
    }
}
