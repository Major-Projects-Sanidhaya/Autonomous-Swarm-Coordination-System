/**
 * Central communication hub for swarm coordination
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
    
    public void updateTopology(List<AgentState> allAgents) {
        for (AgentState agent : allAgents) {
            agentPositions.put(agent.agentId, agent.position);
        }
        
        for (AgentState agent : allAgents) {
            NeighborInformation neighbors = calculateNeighbors(agent.agentId, allAgents);
            networkTopology.put(agent.agentId, neighbors);
        }
    }
    
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
            
            if (networkSimulator.canCommunicate(distance, networkSimulator.getCommunicationRange())) {
                double signalStrength = networkSimulator.calculateSignalStrength(distance, networkSimulator.getCommunicationRange());
                boolean canCommunicate = signalStrength >= 0.3;
                
                NeighborAgent neighbor = new NeighborAgent(
                    otherAgent.agentId, distance, signalStrength, canCommunicate, System.currentTimeMillis());
                neighbors.add(neighbor);
            }
        }
        
        return new NeighborInformation(agentId, neighbors);
    }
    
    public boolean sendMessage(OutgoingMessage message) {
        if (message.isExpired()) {
            return false;
        }
        
        return messageQueue.offer(message);
    }
    
    public void processMessages() {
        while (!messageQueue.isEmpty()) {
            OutgoingMessage message = messageQueue.poll();
            if (message != null && !message.isExpired()) {
                deliverMessage(message);
            }
        }
    }
    
    private void deliverMessage(OutgoingMessage message) {
        if (message.isBroadcast()) {
            deliverBroadcast(message);
        } else {
            deliverDirect(message);
        }
    }
    
    private void deliverDirect(OutgoingMessage message) {
        Point2D senderPos = agentPositions.get(message.senderId);
        Point2D receiverPos = agentPositions.get(message.receiverId);
        
        if (senderPos == null || receiverPos == null) {
            return;
        }
        
        double distance = senderPos.distanceTo(receiverPos);
        NetworkSimulator.DeliveryResult result = networkSimulator.simulateDelivery(distance, networkSimulator.getCommunicationRange());
        
        if (result.willDeliver) {
            List<Integer> routePath = new ArrayList<>();
            IncomingMessage incoming = new IncomingMessage(
                message.receiverId, message.senderId, message.messageContent, 
                routePath, result.signalStrength);
            
            addToHistory(incoming);
            System.out.println("Delivered message: " + incoming);
        } else {
            System.out.println("Message delivery failed: " + message);
        }
    }
    
    private void deliverBroadcast(OutgoingMessage message) {
        NeighborInformation neighbors = networkTopology.get(message.senderId);
        if (neighbors == null) return;
        
        for (NeighborAgent neighbor : neighbors.getCommunicatingNeighbors()) {
            OutgoingMessage individualMessage = new OutgoingMessage(
                message.senderId, neighbor.neighborId, message.messageContent, 
                message.priority, message.maxHops, message.expirationTime - System.currentTimeMillis());
            
            deliverDirect(individualMessage);
        }
    }
    
    public NeighborInformation getNeighbors(int agentId) {
        return networkTopology.get(agentId);
    }
    
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
    
    private void addToHistory(IncomingMessage message) {
        messageHistory.add(message);
        if (messageHistory.size() > maxHistorySize) {
            messageHistory.remove(0);
        }
    }
    
    public List<IncomingMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
    
    public int getPendingMessageCount() {
        return messageQueue.size();
    }
    
    public void clearPendingMessages() {
        messageQueue.clear();
    }
    
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
