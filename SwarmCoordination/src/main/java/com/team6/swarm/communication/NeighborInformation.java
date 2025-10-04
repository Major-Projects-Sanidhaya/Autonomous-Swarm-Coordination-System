/**
 * Complete neighbor information for a single agent
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
    
    private double calculateAverageSignalStrength() {
        if (neighbors.isEmpty()) return 0.0;
        
        double totalSignal = neighbors.stream()
            .mapToDouble(n -> n.signalStrength)
            .sum();
        
        return totalSignal / neighbors.size();
    }
    
    private double calculateNetworkQuality() {
        if (neighbors.isEmpty()) return 0.0;
        
        double neighborRatio = Math.min(1.0, neighborCount / 8.0);
        double signalWeight = averageSignalStrength;
        
        return neighborRatio * signalWeight;
    }
    
    public List<NeighborAgent> getCommunicatingNeighbors() {
        return neighbors.stream()
            .filter(neighbor -> neighbor.canCommunicate)
            .collect(Collectors.toList());
    }
    
    public List<NeighborAgent> getReliableNeighbors() {
        return neighbors.stream()
            .filter(neighbor -> neighbor.isReliable())
            .collect(Collectors.toList());
    }
    
    public List<NeighborAgent> getActiveNeighbors() {
        return neighbors.stream()
            .filter(neighbor -> neighbor.isActive())
            .collect(Collectors.toList());
    }
    
    public NeighborAgent getNeighbor(int neighborId) {
        return neighbors.stream()
            .filter(n -> n.neighborId == neighborId)
            .findFirst()
            .orElse(null);
    }
    
    public boolean hasNeighbor(int neighborId) {
        return neighbors.stream()
            .anyMatch(n -> n.neighborId == neighborId);
    }
    
    public boolean isWellConnected() {
        return getReliableNeighbors().size() >= 3 && networkQuality > 0.6;
    }
    
    public boolean isIsolated() {
        return neighborCount <= 1 || networkQuality < 0.2;
    }
    
    @Override
    public String toString() {
        return String.format("NeighborInformation{agentId=%d, neighbors=%d, quality=%.2f, avgSignal=%.2f}", 
                           agentId, neighborCount, networkQuality, averageSignalStrength);
    }
}
