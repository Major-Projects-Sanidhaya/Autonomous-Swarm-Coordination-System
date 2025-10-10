/**
 * Standardized message container for agent communication
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
    
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> expectedType) {
        Object value = metadata.get(key);
        if (value != null && expectedType.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    public boolean isExpired() {
        Integer ttl = getMetadata("ttl", Integer.class);
        if (ttl != null) {
            return (System.currentTimeMillis() - timestamp) > ttl;
        }
        return false;
    }
    
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
