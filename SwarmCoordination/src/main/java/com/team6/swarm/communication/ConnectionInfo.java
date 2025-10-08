/**
 * CONNECTIONINFO CLASS - Active Communication Link Information
 *
 * PURPOSE:
 * - Information about an active communication link between two agents
 * - Provides data for network visualization and monitoring
 * - Tracks connection quality and message statistics
 *
 * CORE COMPONENTS:
 * 1. agentA, agentB - Which two agents are connected
 * 2. strength - Signal strength (0.0 to 1.0)
 * 3. isActive - Currently connected?
 * 4. establishedTime - When connection was formed
 * 5. lastMessageTime - When last message was sent
 * 6. messageCount - How many messages on this link
 * 7. averageLatency - Average message delivery time
 *
 * CONNECTION STATES:
 * - ACTIVE: Currently connected and communicating
 * - INACTIVE: Connection lost or agents out of range
 * - STALE: No recent activity (may be inactive)
 * - NEW: Recently established connection
 *
 * SIGNAL STRENGTH LEVELS:
 * - 1.0: Perfect signal (excellent communication)
 * - 0.8: Good signal (reliable communication)
 * - 0.5: Fair signal (some noise, occasional failures)
 * - 0.2: Poor signal (unreliable, frequent failures)
 * - 0.0: No signal (connection lost)
 *
 * USAGE EXAMPLES:
 * - ConnectionInfo conn = new ConnectionInfo(1, 2, 0.8, true, now, now, 5, 150.0);
 * - if (conn.isActive && conn.strength > 0.7) { useConnection(conn); }
 * - double quality = conn.getConnectionQuality();
 * - String status = conn.getConnectionStatus();
 *
 * INTEGRATION POINTS:
 * - Anthony: Uses for drawing connection lines in UI
 * - CommunicationManager: Maintains active connections
 * - NetworkSimulator: Updates signal strength
 * - Monitoring: Tracks network health and performance
 */
package com.team6.swarm.communication;

public class ConnectionInfo {
    public final int agentA;
    public final int agentB;
    public final double strength;
    public final boolean isActive;
    public final long establishedTime;
    public final long lastMessageTime;
    public final int messageCount;
    public final double averageLatency;
    
    public ConnectionInfo(int agentA, int agentB, double strength, boolean isActive, 
                         long establishedTime, long lastMessageTime, int messageCount, 
                         double averageLatency) {
        this.agentA = agentA;
        this.agentB = agentB;
        this.strength = Math.max(0.0, Math.min(1.0, strength)); // Clamp to [0,1]
        this.isActive = isActive;
        this.establishedTime = establishedTime;
        this.lastMessageTime = lastMessageTime;
        this.messageCount = messageCount;
        this.averageLatency = averageLatency;
    }
    
    /**
     * Get connection quality score (0.0 to 1.0)
     * Combines signal strength, activity, and latency
     */
    public double getConnectionQuality() {
        // Base quality from signal strength
        double baseQuality = strength;
        
        // Activity bonus (recent messages improve quality)
        long timeSinceLastMessage = System.currentTimeMillis() - lastMessageTime;
        double activityBonus = Math.max(0.0, 1.0 - (timeSinceLastMessage / 30000.0)); // 30 second baseline
        
        // Latency penalty (lower latency is better)
        double latencyPenalty = Math.min(0.2, averageLatency / 1000.0); // Max 20% penalty
        
        // Combine factors
        return Math.max(0.0, Math.min(1.0, baseQuality + (activityBonus * 0.2) - latencyPenalty));
    }
    
    /**
     * Check if connection is stale (no recent activity)
     */
    public boolean isStale() {
        long timeSinceLastMessage = System.currentTimeMillis() - lastMessageTime;
        return timeSinceLastMessage > 60000; // Stale if no activity for 1 minute
    }
    
    /**
     * Check if connection is new (recently established)
     */
    public boolean isNew() {
        long timeSinceEstablished = System.currentTimeMillis() - establishedTime;
        return timeSinceEstablished < 10000; // New if established within 10 seconds
    }
    
    /**
     * Check if connection is reliable
     */
    public boolean isReliable() {
        return isActive && strength > 0.6 && !isStale();
    }
    
    /**
     * Get connection age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - establishedTime;
    }
    
    /**
     * Get time since last message in milliseconds
     */
    public long getTimeSinceLastMessage() {
        return System.currentTimeMillis() - lastMessageTime;
    }
    
    /**
     * Get connection status description
     */
    public String getConnectionStatus() {
        if (!isActive) return "INACTIVE";
        if (isStale()) return "STALE";
        if (isNew()) return "NEW";
        if (strength > 0.8) return "EXCELLENT";
        if (strength > 0.6) return "GOOD";
        if (strength > 0.4) return "FAIR";
        if (strength > 0.2) return "POOR";
        return "VERY_POOR";
    }
    
    /**
     * Get signal strength level
     */
    public String getSignalLevel() {
        if (strength >= 0.9) return "EXCELLENT";
        if (strength >= 0.7) return "GOOD";
        if (strength >= 0.5) return "FAIR";
        if (strength >= 0.3) return "POOR";
        return "VERY_POOR";
    }
    
    /**
     * Get connection health score
     */
    public String getHealthScore() {
        double quality = getConnectionQuality();
        if (quality >= 0.8) return "EXCELLENT";
        if (quality >= 0.6) return "GOOD";
        if (quality >= 0.4) return "FAIR";
        if (quality >= 0.2) return "POOR";
        return "CRITICAL";
    }
    
    /**
     * Create updated connection with new message
     */
    public ConnectionInfo updateMessage(double newLatency) {
        double newAverageLatency = (averageLatency * messageCount + newLatency) / (messageCount + 1);
        return new ConnectionInfo(agentA, agentB, strength, isActive, 
                                establishedTime, System.currentTimeMillis(), 
                                messageCount + 1, newAverageLatency);
    }
    
    /**
     * Create updated connection with new signal strength
     */
    public ConnectionInfo updateSignalStrength(double newStrength) {
        return new ConnectionInfo(agentA, agentB, newStrength, isActive, 
                                establishedTime, lastMessageTime, messageCount, averageLatency);
    }
    
    /**
     * Create updated connection with new activity status
     */
    public ConnectionInfo updateActivity(boolean newIsActive) {
        return new ConnectionInfo(agentA, agentB, strength, newIsActive, 
                                establishedTime, lastMessageTime, messageCount, averageLatency);
    }
    
    /**
     * Get connection identifier (for hashing and comparison)
     */
    public String getConnectionId() {
        // Ensure consistent ordering (smaller ID first)
        if (agentA < agentB) {
            return agentA + "-" + agentB;
        } else {
            return agentB + "-" + agentA;
        }
    }
    
    /**
     * Check if this connection involves a specific agent
     */
    public boolean involvesAgent(int agentId) {
        return agentA == agentId || agentB == agentId;
    }
    
    /**
     * Get the other agent in this connection
     */
    public int getOtherAgent(int agentId) {
        if (agentA == agentId) return agentB;
        if (agentB == agentId) return agentA;
        return -1; // Agent not in this connection
    }
    
    /**
     * Get connection summary for logging
     */
    public String getSummary() {
        return String.format("Connection{%s: strength=%.2f, active=%s, messages=%d, latency=%.1fms, quality=%.2f}", 
                           getConnectionId(), strength, isActive, messageCount, averageLatency, getConnectionQuality());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConnectionInfo that = (ConnectionInfo) obj;
        return getConnectionId().equals(that.getConnectionId());
    }
    
    @Override
    public int hashCode() {
        return getConnectionId().hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("ConnectionInfo{%s, strength=%.2f, active=%s, messages=%d, latency=%.1fms}", 
                           getConnectionId(), strength, isActive, messageCount, averageLatency);
    }
}
