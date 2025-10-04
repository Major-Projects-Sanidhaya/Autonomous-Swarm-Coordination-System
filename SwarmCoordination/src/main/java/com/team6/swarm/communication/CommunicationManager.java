/**
 * COMMUNICATIONMANAGER CLASS - Central Communication Hub
 *
 * PURPOSE:
 * - Central hub that manages all communication in the swarm
 * - Coordinates network topology, message routing, and delivery
 * - Provides interface between agents and communication system
 *
 * CORE RESPONSIBILITIES:
 * 1. Network Topology Management - Track which agents can communicate
 * 2. Message Routing - Deliver messages between agents
 * 3. Neighbor Discovery - Maintain neighbor relationships
 * 4. Network Simulation - Apply realistic network conditions
 * 5. Message History - Track recent messages for debugging
 *
 * KEY COMPONENTS:
 * - networkTopology: Map of agent ID to NeighborInformation
 * - messageQueue: Priority queue for pending messages
 * - networkSimulator: Network physics simulation
 * - messageHistory: Recent messages for analysis
 * - agentPositions: Current positions of all agents
 *
 * MESSAGE FLOW:
 * 1. Receive OutgoingMessage from Lauren/Agent
 * 2. Check if direct delivery possible (in range?)
 * 3. If not, find multi-hop route (if enabled)
 * 4. Apply network simulation (delays, failures)
 * 5. Create IncomingMessage for delivery
 * 6. Notify recipient of message arrival
 *
 * NETWORK TOPOLOGY UPDATES:
 * - Called whenever agent positions change
 * - Recalculates distances between all agents
 * - Updates neighbor relationships
 * - Notifies when topology changes
 *
 * USAGE EXAMPLES:
 * - CommunicationManager manager = new CommunicationManager();
 * - manager.updateTopology(agentStates);
 * - manager.sendMessage(outgoingMessage);
 * - NeighborInformation neighbors = manager.getNeighbors(agentId);
 * - List<ConnectionInfo> connections = manager.getActiveConnections();
 *
 * INTEGRATION POINTS:
 * - Lauren: Sends messages, receives neighbor information
 * - Sanidhaya: Provides agent position updates
 * - Anthony: Receives connection information for visualization
 * - NetworkSimulator: Uses for realistic network conditions
 */
package com.team6.swarm.communication;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.Point2D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

public class CommunicationManager {
    private final Map<Integer, NeighborInformation> networkTopology;
    private final PriorityBlockingQueue<OutgoingMessage> messageQueue;
    private final NetworkSimulator networkSimulator;
    private final List<IncomingMessage> messageHistory;
    private final Map<Integer, Point2D> agentPositions;
    private final int maxHistorySize;
    
    public CommunicationManager() {
        this(new NetworkSimulator());
    }
    
    public CommunicationManager(NetworkSimulator networkSimulator) {
        this.networkTopology = new ConcurrentHashMap<>();
        this.messageQueue = new PriorityBlockingQueue<>(100, 
            Comparator.comparingInt(msg -> msg.priority));
        this.networkSimulator = networkSimulator;
        this.messageHistory = new ArrayList<>();
        this.agentPositions = new ConcurrentHashMap<>();
        this.maxHistorySize = 1000;
    }
    
    /**
     * Update network topology based on current agent positions
     * Called whenever agent positions change
     */
    public void updateTopology(List<AgentState> allAgents) {
        // Update agent positions
        for (AgentState agent : allAgents) {
            agentPositions.put(agent.agentId, agent.position);
        }
        
        // Recalculate neighbor relationships for each agent
        for (AgentState agent : allAgents) {
            NeighborInformation neighbors = calculateNeighbors(agent.agentId, allAgents);
            networkTopology.put(agent.agentId, neighbors);
        }
    }
    
    /**
     * Calculate neighbors for a specific agent
     */
    private NeighborInformation calculateNeighbors(int agentId, List<AgentState> allAgents) {
        List<NeighborAgent> neighbors = new ArrayList<>();
        Point2D agentPosition = agentPositions.get(agentId);
        
        if (agentPosition == null) {
            return new NeighborInformation(agentId, neighbors);
        }
        
        for (AgentState otherAgent : allAgents) {
            if (otherAgent.agentId == agentId) continue;
            
            Point2D otherPosition = otherAgent.position;
            double distance = agentPosition.distanceTo(otherPosition);
            
            // Check if in communication range
            if (networkSimulator.canCommunicate(distance, networkSimulator.getCommunicationRange())) {
                double signalStrength = networkSimulator.calculateSignalStrength(
                    distance, networkSimulator.getCommunicationRange());
                boolean canCommunicate = signalStrength >= 0.3;
                
                NeighborAgent neighbor = new NeighborAgent(
                    otherAgent.agentId, distance, signalStrength, 
                    canCommunicate, System.currentTimeMillis());
                neighbors.add(neighbor);
            }
        }
        
        return new NeighborInformation(agentId, neighbors);
    }
    
    /**
     * Send a message (add to queue for processing)
     */
    public boolean sendMessage(OutgoingMessage message) {
        if (message.isExpired()) {
            return false;
        }
        
        return messageQueue.offer(message);
    }
    
    /**
     * Process pending messages in the queue
     * Called regularly by the simulation loop
     */
    public void processMessages() {
        while (!messageQueue.isEmpty()) {
            OutgoingMessage message = messageQueue.poll();
            if (message != null && !message.isExpired()) {
                deliverMessage(message);
            }
        }
    }
    
    /**
     * Deliver a message to its destination
     */
    private void deliverMessage(OutgoingMessage message) {
        if (message.isBroadcast()) {
            deliverBroadcast(message);
        } else {
            deliverDirect(message);
        }
    }
    
    /**
     * Deliver a direct message to a specific agent
     */
    private void deliverDirect(OutgoingMessage message) {
        Point2D senderPos = agentPositions.get(message.senderId);
        Point2D receiverPos = agentPositions.get(message.receiverId);
        
        if (senderPos == null || receiverPos == null) {
            return; // Agent not found
        }
        
        double distance = senderPos.distanceTo(receiverPos);
        
        // Simulate delivery
        NetworkSimulator.DeliveryResult result = networkSimulator.simulateDelivery(
            distance, networkSimulator.getCommunicationRange());
        
        if (result.willDeliver) {
            // Create delivery receipt
            List<Integer> routePath = new ArrayList<>();
            IncomingMessage incoming = new IncomingMessage(
                message.receiverId, message.senderId, message.messageContent, 
                routePath, result.signalStrength);
            
            // Add to history
            addToHistory(incoming);
            
            // Notify recipient (in real implementation, this would trigger an event)
            System.out.println("Delivered message: " + incoming);
        } else {
            System.out.println("Message delivery failed: " + message);
        }
    }
    
    /**
     * Deliver a broadcast message to all neighbors
     */
    private void deliverBroadcast(OutgoingMessage message) {
        NeighborInformation neighbors = networkTopology.get(message.senderId);
        if (neighbors == null) return;
        
        for (NeighborAgent neighbor : neighbors.getCommunicatingNeighbors()) {
            // Create individual message for each neighbor
            OutgoingMessage individualMessage = new OutgoingMessage(
                message.senderId, neighbor.neighborId, message.messageContent, 
                message.priority, message.maxHops, message.expirationTime);
            
            deliverDirect(individualMessage);
        }
    }
    
    /**
     * Get neighbor information for a specific agent
     */
    public NeighborInformation getNeighbors(int agentId) {
        return networkTopology.get(agentId);
    }
    
    /**
     * Get all active connections for visualization
     */
    public List<ConnectionInfo> getActiveConnections() {
        List<ConnectionInfo> connections = new ArrayList<>();
        
        for (NeighborInformation neighbors : networkTopology.values()) {
            for (NeighborAgent neighbor : neighbors.getCommunicatingNeighbors()) {
                ConnectionInfo connection = new ConnectionInfo(
                    neighbors.agentId, neighbor.neighborId, 
                    neighbor.signalStrength, true, 
                    System.currentTimeMillis(), System.currentTimeMillis(), 1, 100.0);
                connections.add(connection);
            }
        }
        
        return connections;
    }
    
    /**
     * Get network statistics
     */
    public NetworkStatistics getNetworkStatistics() {
        int totalAgents = networkTopology.size();
        int totalConnections = 0;
        double averageSignalStrength = 0.0;
        int isolatedAgents = 0;
        
        for (NeighborInformation neighbors : networkTopology.values()) {
            totalConnections += neighbors.neighborCount;
            averageSignalStrength += neighbors.averageSignalStrength;
            if (neighbors.isIsolated()) {
                isolatedAgents++;
            }
        }
        
        if (totalAgents > 0) {
            averageSignalStrength /= totalAgents;
        }
        
        return new NetworkStatistics(totalAgents, totalConnections, 
                                   averageSignalStrength, isolatedAgents);
    }
    
    /**
     * Add message to history (with size limit)
     */
    private void addToHistory(IncomingMessage message) {
        messageHistory.add(message);
        if (messageHistory.size() > maxHistorySize) {
            messageHistory.remove(0); // Remove oldest
        }
    }
    
    /**
     * Get recent message history
     */
    public List<IncomingMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
    
    /**
     * Get pending message count
     */
    public int getPendingMessageCount() {
        return messageQueue.size();
    }
    
    /**
     * Clear all pending messages
     */
    public void clearPendingMessages() {
        messageQueue.clear();
    }
    
    /**
     * Network statistics container
     */
    public static class NetworkStatistics {
        public final int totalAgents;
        public final int totalConnections;
        public final double averageSignalStrength;
        public final int isolatedAgents;
        
        public NetworkStatistics(int totalAgents, int totalConnections, 
                               double averageSignalStrength, int isolatedAgents) {
            this.totalAgents = totalAgents;
            this.totalConnections = totalConnections;
            this.averageSignalStrength = averageSignalStrength;
            this.isolatedAgents = isolatedAgents;
        }
        
        @Override
        public String toString() {
            return String.format("NetworkStats{agents=%d, connections=%d, avgSignal=%.2f, isolated=%d}", 
                               totalAgents, totalConnections, averageSignalStrength, isolatedAgents);
        }
    }
    
    @Override
    public String toString() {
        return String.format("CommunicationManager{agents=%d, pending=%d, simulator=%s}", 
                           networkTopology.size(), messageQueue.size(), networkSimulator);
    }
}
