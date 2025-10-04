/**
 * INCOMINGMESSAGE CLASS - Message with Delivery Details
 *
 * PURPOSE:
 * - Wraps delivered Message with transmission metadata
 * - Provides delivery receipt information for recipients
 * - Enables quality assessment and network monitoring
 *
 * CORE COMPONENTS:
 * 1. receiverId - Which agent received this message
 * 2. originalSenderId - Who originally sent the message
 * 3. messageContent - The actual Message object
 * 4. routePath - List of agent IDs that relayed the message
 * 5. signalStrength - Quality of the received signal (0.0 to 1.0)
 * 6. actualDeliveryTime - When the message actually arrived
 * 7. transmissionDelay - How long the message took to arrive
 *
 * DELIVERY METRICS:
 * - signalStrength: 1.0 = perfect signal, 0.0 = barely readable
 * - transmissionDelay: Time from creation to delivery (milliseconds)
 * - routePath: Shows multi-hop path taken (empty for direct delivery)
 * - actualDeliveryTime: Timestamp when message was received
 *
 * QUALITY ASSESSMENT:
 * - High signal strength (>0.8): Reliable data, trust message content
 * - Medium signal strength (0.4-0.8): Some noise, verify critical data
 * - Low signal strength (<0.4): Unreliable, request retransmission
 * - Long transmission delay: Network congestion or routing issues
 * - Many hops in routePath: Network topology problems
 *
 * ROUTE PATH EXAMPLES:
 * - Direct delivery: routePath = [] (empty)
 * - One hop: routePath = [2] (Agent 2 relayed)
 * - Two hops: routePath = [2, 3] (Agent 2 → Agent 3 → destination)
 * - Broadcast: routePath = [] (direct from sender)
 *
 * USAGE PATTERNS:
 * 1. Quality Check: if (incoming.signalStrength > 0.7) { trust message; }
 * 2. Network Monitoring: track average transmissionDelay
 * 3. Route Analysis: analyze routePath for network optimization
 * 4. Acknowledgment: send ACK back to originalSenderId
 * 5. Retransmission: request resend if signalStrength too low
 *
 * INTEGRATION POINTS:
 * - Lauren: Uses signalStrength to weight decision inputs
 * - Anthony: Displays routePath and signalStrength in UI
 * - Network Monitoring: Tracks delivery metrics for optimization
 * - Debugging: RoutePath helps diagnose communication issues
 *
 * THREAD SAFETY:
 * - Immutable after creation (fields are final)
 * - Safe for concurrent processing by recipients
 * - Message content should be immutable or thread-safe
 */
package com.team6.swarm.communication;

import java.util.List;
import java.util.ArrayList;

public class IncomingMessage {
    public final int receiverId;
    public final int originalSenderId;
    public final Message messageContent;
    public final List<Integer> routePath;
    public final double signalStrength;
    public final long actualDeliveryTime;
    public final long transmissionDelay;
    
    public IncomingMessage(int receiverId, int originalSenderId, Message messageContent, 
                          List<Integer> routePath, double signalStrength) {
        this.receiverId = receiverId;
        this.originalSenderId = originalSenderId;
        this.messageContent = messageContent;
        this.routePath = new ArrayList<>(routePath);
        this.signalStrength = signalStrength;
        this.actualDeliveryTime = System.currentTimeMillis();
        this.transmissionDelay = actualDeliveryTime - messageContent.timestamp;
    }
    
    /**
     * Check if this was a direct delivery (no relays)
     */
    public boolean isDirectDelivery() {
        return routePath.isEmpty();
    }
    
    /**
     * Get the number of hops this message took
     */
    public int getHopCount() {
        return routePath.size();
    }
    
    /**
     * Check if the signal quality is reliable
     */
    public boolean isReliable() {
        return signalStrength > 0.7;
    }
    
    /**
     * Check if the signal quality is acceptable
     */
    public boolean isAcceptable() {
        return signalStrength > 0.4;
    }
    
    /**
     * Check if the message arrived quickly
     */
    public boolean isFastDelivery() {
        return transmissionDelay < 1000; // Less than 1 second
    }
    
    /**
     * Get the last relay agent (if any)
     */
    public Integer getLastRelay() {
        return routePath.isEmpty() ? null : routePath.get(routePath.size() - 1);
    }
    
    /**
     * Get the first relay agent (if any)
     */
    public Integer getFirstRelay() {
        return routePath.isEmpty() ? null : routePath.get(0);
    }
    
    /**
     * Check if a specific agent was involved in routing
     */
    public boolean wasRelayedBy(int agentId) {
        return routePath.contains(agentId);
    }
    
    /**
     * Get delivery quality score (0.0 to 1.0)
     * Combines signal strength and delivery speed
     */
    public double getDeliveryQuality() {
        double speedScore = Math.max(0.0, 1.0 - (transmissionDelay / 5000.0)); // 5 second baseline
        return (signalStrength * 0.7) + (speedScore * 0.3);
    }
    
    @Override
    public String toString() {
        return String.format("IncomingMessage{receiver=%d, sender=%d, type=%s, hops=%d, signal=%.2f, delay=%dms}", 
                           receiverId, originalSenderId, messageContent.type, getHopCount(), 
                           signalStrength, transmissionDelay);
    }
}
