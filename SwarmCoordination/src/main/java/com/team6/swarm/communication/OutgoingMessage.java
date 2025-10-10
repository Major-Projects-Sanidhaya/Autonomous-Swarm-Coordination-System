/**
 * OUTGOINGMESSAGE CLASS - Message with Routing Information
 *
 * PURPOSE:
 * - Wraps Message with routing metadata for delivery
 * - Specifies sender, receiver, and delivery parameters
 * - Enables multi-hop routing and priority handling
 *
 * CORE COMPONENTS:
 * 1. senderId - Which agent is sending the message
 * 2. receiverId - Which agent should receive (-1 = broadcast to all)
 * 3. messageContent - The actual Message object
 * 4. priority - Delivery priority (1=highest, 5=lowest)
 * 5. maxHops - Maximum number of relay agents allowed
 * 6. expirationTime - When message becomes invalid
 *
 * ROUTING SCENARIOS:
 * 1. Direct Delivery: Agent 1 → Agent 2 (if in communication range)
 * 2. Broadcast: Agent 1 → All nearby agents (receiverId = -1)
 * 3. Multi-hop: Agent 1 → Agent 2 → Agent 3 (if 1 and 3 out of range)
 * 4. Flooding: Agent 1 → Neighbors → Their neighbors (for discovery)
 *
 * PRIORITY LEVELS:
 * 1. EMERGENCY - Critical alerts, system failures
 * 2. HIGH - Vote proposals, task assignments
 * 3. NORMAL - Position updates, status reports
 * 4. LOW - Acknowledgments, routine updates
 * 5. BACKGROUND - Non-urgent maintenance messages
 *
 * HOP LIMITS:
 * - maxHops = 0: Direct delivery only
 * - maxHops = 1: One relay allowed
 * - maxHops = -1: Unlimited hops (use with caution)
 * - Default: 2 hops for most messages
 *
 * EXPIRATION HANDLING:
 * - Messages expire after expirationTime
 * - Expired messages are discarded from queues
 * - Prevents stale messages from being delivered
 * - Default: 30 seconds for most messages
 *
 * USAGE EXAMPLES:
 * // Direct message
 * OutgoingMessage direct = new OutgoingMessage(1, 2, message, 2, 0, 30000);
 * 
 * // Broadcast message
 * OutgoingMessage broadcast = new OutgoingMessage(1, -1, message, 3, 1, 10000);
 * 
 * // Multi-hop message
 * OutgoingMessage multiHop = new OutgoingMessage(1, 5, message, 2, 3, 60000);
 *
 * THREAD SAFETY:
 * - Immutable after creation (fields are final)
 * - Safe for concurrent queuing and processing
 * - Message content should be immutable or thread-safe
 */
package com.team6.swarm.communication;

public class OutgoingMessage {
    public final int senderId;
    public final int receiverId;
    public final Message messageContent;
    public final int priority;
    public final int maxHops;
    public final long expirationTime;
    
    public OutgoingMessage(int senderId, int receiverId, Message messageContent) {
        this(senderId, receiverId, messageContent, messageContent.getPriority(), 2, 30000);
    }
    
    public OutgoingMessage(int senderId, int receiverId, Message messageContent, 
                          int priority, int maxHops, long expirationTimeMs) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageContent = messageContent;
        this.priority = priority;
        this.maxHops = maxHops;
        this.expirationTime = System.currentTimeMillis() + expirationTimeMs;
    }
    
    /**
     * Check if this is a broadcast message
     */
    public boolean isBroadcast() {
        return receiverId == -1;
    }
    
    /**
     * Check if message has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
    
    /**
     * Check if message allows multi-hop routing
     */
    public boolean allowsMultiHop() {
        return maxHops > 0;
    }
    
    /**
     * Get remaining time until expiration (in milliseconds)
     */
    public long getRemainingTime() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }
    
    /**
     * Create a relayed version of this message (for multi-hop routing)
     */
    public OutgoingMessage createRelay(int newSenderId) {
        return new OutgoingMessage(newSenderId, receiverId, messageContent, 
                                 priority, maxHops - 1, expirationTime);
    }
    
    @Override
    public String toString() {
        return String.format("OutgoingMessage{sender=%d, receiver=%d, type=%s, priority=%d, hops=%d}", 
                           senderId, receiverId, messageContent.type, priority, maxHops);
    }
}
