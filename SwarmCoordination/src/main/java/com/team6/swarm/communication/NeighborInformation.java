/**
 * NEIGHBORINFORMATION CLASS - Complete Neighbor List for One Agent
 *
 * PURPOSE:
 * - Bundles all neighbor data together for efficiency
 * - Provides complete network topology view for one agent
 * - Enables neighbor-based algorithms and routing decisions
 *
 * CORE COMPONENTS:
 * 1. agentId - Which agent this neighbor information is about
 * 2. neighbors - List of NeighborAgent objects
 * 3. topologyUpdateTime - When this information was calculated
 * 4. networkQuality - Overall network health for this agent
 * 5. neighborCount - Number of active neighbors
 * 6. averageSignalStrength - Mean signal quality across all neighbors
 *
 * NETWORK QUALITY CALCULATION:
 * - Based on number of neighbors and their signal strengths
 * - Formula: (activeNeighbors / 8) * averageSignalStrength
 * - 8 is the target number of neighbors for optimal connectivity
 * - Quality ranges from 0.0 (isolated) to 1.0 (excellent connectivity)
 *
 * NEIGHBOR CATEGORIES:
 * - Active: Can communicate and recently contacted
 * - Reliable: High signal strength and stable connection
 * - Marginal: Weak signal but still functional
 * - Lost: Out of range or inactive
 *
 * UPDATE FREQUENCY:
 * - Calculated every time agent positions change (30-60 times per second)
 * - Only sent to Lauren when neighbors actually change (reduce traffic)
 * - Cached to avoid redundant calculations
 *
 * USAGE EXAMPLES:
 * - NeighborInformation info = communicationManager.getNeighbors(agentId);
 * - List<NeighborAgent> reliable = info.getReliableNeighbors();
 * - double quality = info.getNetworkQuality();
 * - if (info.isWellConnected()) { executeFlockingBehavior(); }
 *
 * INTEGRATION POINTS:
 * - Lauren: Uses for flocking algorithms and swarm coordination
 * - CommunicationManager: Maintains and updates neighbor lists
 * - Anthony: Displays network topology in UI
 * - NetworkSimulator: Calculates neighbor relationships
 */
package com.team6.swarm.communication;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class NeighborInformation {
    public final int agentId;
    public final List<NeighborAgent> neighbors;
    public final long topologyUpdateTime;
    public final double networkQuality;
    public final int neighborCount;
    public final double averageSignalStrength;
    
    public NeighborInformation(int agentId, List<NeighborAgent> neighbors) {
        this.agentId = agentId;
        this.neighbors = new ArrayList<>(neighbors);
        this.topologyUpdateTime = System.currentTimeMillis();
        this.neighborCount = neighbors.size();
        this.averageSignalStrength = calculateAverageSignalStrength();
        this.networkQuality = calculateNetworkQuality();
    }
    
    /**
     * Calculate average signal strength across all neighbors
     */
    private double calculateAverageSignalStrength() {
        if (neighbors.isEmpty()) return 0.0;
        
        double totalSignal = neighbors.stream()
            .mapToDouble(n -> n.signalStrength)
            .sum();
        
        return totalSignal / neighbors.size();
    }
    
    /**
     * Calculate overall network quality for this agent
     * Based on number of neighbors and their signal strengths
     */
    private double calculateNetworkQuality() {
        if (neighbors.isEmpty()) return 0.0;
        
        // Target 8 neighbors for optimal connectivity
        double neighborRatio = Math.min(1.0, neighborCount / 8.0);
        
        // Weight by average signal strength
        double signalWeight = averageSignalStrength;
        
        // Combine factors
        return neighborRatio * signalWeight;
    }
    
    /**
     * Get all neighbors that can communicate
     */
    public List<NeighborAgent> getCommunicatingNeighbors() {
        return neighbors.stream()
            .filter(neighbor -> neighbor.canCommunicate)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all reliable neighbors (good signal and active)
     */
    public List<NeighborAgent> getReliableNeighbors() {
        return neighbors.stream()
            .filter(neighbor -> neighbor.isReliable())
            .collect(Collectors.toList());
    }
    
    /**
     * Get all active neighbors (recently contacted)
     */
    public List<NeighborAgent> getActiveNeighbors() {
        return neighbors.stream()
            .filter(neighbor -> neighbor.isActive())
            .collect(Collectors.toList());
    }
    
    /**
     * Get neighbors within optimal range
     */
    public List<NeighborAgent> getOptimalRangeNeighbors(double maxRange) {
        return neighbors.stream()
            .filter(neighbor -> neighbor.isInOptimalRange(maxRange))
            .collect(Collectors.toList());
    }
    
    /**
     * Get neighbor by ID
     */
    public NeighborAgent getNeighbor(int neighborId) {
        return neighbors.stream()
            .filter(n -> n.neighborId == neighborId)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Check if agent has a specific neighbor
     */
    public boolean hasNeighbor(int neighborId) {
        return neighbors.stream()
            .anyMatch(n -> n.neighborId == neighborId);
    }
    
    /**
     * Check if agent is well connected
     * Well connected = 3+ reliable neighbors with good network quality
     */
    public boolean isWellConnected() {
        return getReliableNeighbors().size() >= 3 && networkQuality > 0.6;
    }
    
    /**
     * Check if agent is isolated
     * Isolated = 0-1 neighbors or very poor network quality
     */
    public boolean isIsolated() {
        return neighborCount <= 1 || networkQuality < 0.2;
    }
    
    /**
     * Check if agent is moderately connected
     * Moderate = 2-4 neighbors with fair network quality
     */
    public boolean isModeratelyConnected() {
        return neighborCount >= 2 && neighborCount <= 4 && networkQuality >= 0.2 && networkQuality <= 0.6;
    }
    
    /**
     * Get connection status description
     */
    public String getConnectionStatus() {
        if (isIsolated()) return "ISOLATED";
        if (isWellConnected()) return "WELL_CONNECTED";
        if (isModeratelyConnected()) return "MODERATELY_CONNECTED";
        return "POORLY_CONNECTED";
    }
    
    /**
     * Get the best neighbor (highest connection quality)
     */
    public NeighborAgent getBestNeighbor() {
        return neighbors.stream()
            .max((n1, n2) -> Double.compare(n1.connectionQuality, n2.connectionQuality))
            .orElse(null);
    }
    
    /**
     * Get the closest neighbor
     */
    public NeighborAgent getClosestNeighbor() {
        return neighbors.stream()
            .min((n1, n2) -> Double.compare(n1.distance, n2.distance))
            .orElse(null);
    }
    
    /**
     * Get the most reliable neighbor
     */
    public NeighborAgent getMostReliableNeighbor() {
        return neighbors.stream()
            .filter(neighbor -> neighbor.isReliable())
            .max((n1, n2) -> Double.compare(n1.signalStrength, n2.signalStrength))
            .orElse(null);
    }
    
    /**
     * Get age of this neighbor information
     */
    public long getAge() {
        return System.currentTimeMillis() - topologyUpdateTime;
    }
    
    /**
     * Check if this information is stale
     * Stale if older than 5 seconds
     */
    public boolean isStale() {
        return getAge() > 5000;
    }
    
    /**
     * Get summary statistics
     */
    public String getSummary() {
        return String.format("NeighborInfo{agent=%d, neighbors=%d, quality=%.2f, status=%s, avgSignal=%.2f}", 
                           agentId, neighborCount, networkQuality, getConnectionStatus(), averageSignalStrength);
    }
    
    @Override
    public String toString() {
        return String.format("NeighborInformation{agentId=%d, neighbors=%d, quality=%.2f, avgSignal=%.2f}", 
                           agentId, neighborCount, networkQuality, averageSignalStrength);
    }
}
