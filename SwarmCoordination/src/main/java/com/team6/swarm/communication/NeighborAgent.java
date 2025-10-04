/**
 * NEIGHBORAGENT CLASS - Information About Nearby Agent
 *
 * PURPOSE:
 * - Represents information about a nearby agent that can be communicated with
 * - Provides distance, signal strength, and communication status
 * - Foundation for network topology and neighbor discovery
 *
 * CORE COMPONENTS:
 * 1. neighborId - Which agent is nearby
 * 2. distance - How far away they are (in units)
 * 3. signalStrength - Quality of connection (0.0 to 1.0)
 * 4. canCommunicate - Boolean if connection is good enough
 * 5. lastContact - When last message was exchanged
 * 6. connectionQuality - Overall connection health score
 *
 * SIGNAL STRENGTH CALCULATION:
 * - 1.0 = Perfect signal (very close, no interference)
 * - 0.8 = Good signal (reliable communication)
 * - 0.5 = Fair signal (some noise, occasional failures)
 * - 0.2 = Poor signal (unreliable, frequent failures)
 * - 0.0 = No signal (out of range or blocked)
 *
 * COMMUNICATION THRESHOLD:
 * - canCommunicate = true if signalStrength >= 0.3
 * - Below 0.3: Connection too weak for reliable communication
 * - Above 0.7: Excellent connection quality
 *
 * DISTANCE CALCULATION:
 * - Euclidean distance between agent positions
 * - Formula: sqrt((x2-x1)² + (y2-y1)²)
 * - Used for signal strength calculation
 * - Affects communication range and quality
 *
 * USAGE EXAMPLES:
 * - NeighborAgent neighbor = new NeighborAgent(5, 45.2, 0.8, true, System.currentTimeMillis());
 * - if (neighbor.canCommunicate) { sendMessage(neighbor.neighborId, message); }
 * - double quality = neighbor.getConnectionQuality();
 *
 * INTEGRATION POINTS:
 * - Lauren: Uses for flocking algorithms and neighbor awareness
 * - CommunicationManager: Maintains neighbor lists for routing
 * - Anthony: Displays connection lines in UI
 * - NetworkSimulator: Calculates signal strength based on distance
 */
package com.team6.swarm.communication;

public class NeighborAgent {
    public final int neighborId;
    public final double distance;
    public final double signalStrength;
    public final boolean canCommunicate;
    public final long lastContact;
    public final double connectionQuality;
    
    public NeighborAgent(int neighborId, double distance, double signalStrength, 
                        boolean canCommunicate, long lastContact) {
        this.neighborId = neighborId;
        this.distance = distance;
        this.signalStrength = signalStrength;
        this.canCommunicate = canCommunicate;
        this.lastContact = lastContact;
        this.connectionQuality = calculateConnectionQuality(signalStrength, distance);
    }
    
    /**
     * Calculate overall connection quality score
     * Combines signal strength and distance factors
     */
    private double calculateConnectionQuality(double signalStrength, double distance) {
        // Base quality from signal strength
        double baseQuality = signalStrength;
        
        // Distance penalty (closer is better)
        double distancePenalty = Math.min(0.2, distance / 1000.0); // Max 20% penalty
        
        // Time penalty (recent contact is better)
        long timeSinceContact = System.currentTimeMillis() - lastContact;
        double timePenalty = Math.min(0.1, timeSinceContact / 10000.0); // Max 10% penalty
        
        return Math.max(0.0, baseQuality - distancePenalty - timePenalty);
    }
    
    /**
     * Check if this neighbor is still active
     * Based on time since last contact
     */
    public boolean isActive() {
        long timeSinceContact = System.currentTimeMillis() - lastContact;
        return timeSinceContact < 30000; // Active if contacted within 30 seconds
    }
    
    /**
     * Check if this neighbor is reliable for communication
     * Based on signal strength and recent activity
     */
    public boolean isReliable() {
        return canCommunicate && signalStrength > 0.6 && isActive();
    }
    
    /**
     * Get the age of this neighbor information
     * Returns milliseconds since last contact
     */
    public long getAge() {
        return System.currentTimeMillis() - lastContact;
    }
    
    /**
     * Check if this neighbor is within optimal range
     * Optimal range is 50-80% of maximum communication range
     */
    public boolean isInOptimalRange(double maxRange) {
        double optimalMin = maxRange * 0.5;
        double optimalMax = maxRange * 0.8;
        return distance >= optimalMin && distance <= optimalMax;
    }
    
    /**
     * Get connection status description
     */
    public String getConnectionStatus() {
        if (!canCommunicate) return "OUT_OF_RANGE";
        if (signalStrength > 0.8) return "EXCELLENT";
        if (signalStrength > 0.6) return "GOOD";
        if (signalStrength > 0.4) return "FAIR";
        if (signalStrength > 0.2) return "POOR";
        return "VERY_POOR";
    }
    
    /**
     * Create updated neighbor with new contact time
     */
    public NeighborAgent updateContact() {
        return new NeighborAgent(neighborId, distance, signalStrength, 
                               canCommunicate, System.currentTimeMillis());
    }
    
    /**
     * Create updated neighbor with new signal strength
     */
    public NeighborAgent updateSignalStrength(double newSignalStrength) {
        boolean newCanCommunicate = newSignalStrength >= 0.3;
        return new NeighborAgent(neighborId, distance, newSignalStrength, 
                               newCanCommunicate, System.currentTimeMillis());
    }
    
    @Override
    public String toString() {
        return String.format("NeighborAgent{id=%d, distance=%.1f, signal=%.2f, canComm=%s, quality=%.2f}", 
                           neighborId, distance, signalStrength, canCommunicate, connectionQuality);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NeighborAgent that = (NeighborAgent) obj;
        return neighborId == that.neighborId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(neighborId);
    }
}
