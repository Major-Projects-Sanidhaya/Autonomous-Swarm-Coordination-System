/**
 * Central communication hub for swarm coordination
 */
package com.team6.swarm.communication;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.Point2D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class CommunicationManager {
    private final Map<Integer, NeighborInformation> networkTopology;
    private final PriorityBlockingQueue<OutgoingMessage> messageQueue;
    private final NetworkSimulator networkSimulator;
    private final List<IncomingMessage> messageHistory;
    private final Map<Integer, Point2D> agentPositions;
    private final Map<Integer, MessageListener> messageListeners;
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
        this.messageListeners = new ConcurrentHashMap<>();
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
            notifyMessageListener(message.receiverId, incoming);
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

    /**
     * Gets the partition (connected component) containing the given agent.
     * This is a convenience wrapper around getNetworkPartitions().
     *
     * @param agentId The agent to locate
     * @return Set of agent IDs in the same partition, or null if not found
     */
    public Set<Integer> getPartitionForAgent(int agentId) {
        List<Set<Integer>> partitions = getNetworkPartitions();
        for (Set<Integer> partition : partitions) {
            if (partition.contains(agentId)) {
                return partition;
            }
        }
        return null;
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
    
    // ============================================================
    // WEEK 4: Agent State Integration - Message Listener Callbacks
    // ============================================================
    
    /**
     * Registers a callback listener for incoming messages.
     * Enables other components to receive messages without polling.
     * 
     * @param agentId The agent ID to listen for
     * @param listener Callback interface for message delivery
     */
    public void registerMessageListener(int agentId, MessageListener listener) {
        messageListeners.put(agentId, listener);
    }
    
    /**
     * Removes a message listener for an agent.
     * 
     * @param agentId The agent ID to stop listening for
     */
    public void unregisterMessageListener(int agentId) {
        messageListeners.remove(agentId);
    }
    
    /**
     * Notifies the registered listener for an agent when a message arrives.
     * 
     * @param agentId The agent receiving the message
     * @param message The incoming message
     */
    private void notifyMessageListener(int agentId, IncomingMessage message) {
        MessageListener listener = messageListeners.get(agentId);
        if (listener != null) {
            listener.onMessageReceived(message);
        }
    }
    
    // ============================================================
    // WEEK 5: Voting Integration - Vote Message Support
    // ============================================================
    
    /**
     * Broadcasts a vote proposal to all agents within communication range.
     * Uses MessageType.VOTE_PROPOSAL with high priority routing.
     * 
     * @param senderId Agent initiating the vote
     * @param voteProposal The vote data (question, options, deadline)
     * @return true if broadcast queued successfully
     */
    public boolean broadcastVote(int senderId, Map<String, Object> voteProposal) {
        Message message = new Message(MessageType.VOTE_PROPOSAL, voteProposal);
        OutgoingMessage outgoing = new OutgoingMessage(
            senderId, -1, message, 2, 5, 30000);
        return sendMessage(outgoing);
    }
    
    /**
     * Sends a vote response from one agent to the vote initiator.
     * Uses MessageType.VOTE_RESPONSE with high priority.
     * 
     * @param voterId Agent responding to vote
     * @param initiatorId Agent who started the vote
     * @param voteResponse Response data (proposalId, choice)
     * @return true if response queued successfully
     */
    public boolean sendVoteResponse(int voterId, int initiatorId, Map<String, Object> voteResponse) {
        Message message = new Message(MessageType.VOTE_RESPONSE, voteResponse);
        OutgoingMessage outgoing = new OutgoingMessage(
            voterId, initiatorId, message, 2, 5, 30000);
        return sendMessage(outgoing);
    }
    
    /**
     * Retrieves all vote-related messages for an agent from history.
     * Filters by VOTE_PROPOSAL and VOTE_RESPONSE message types.
     * 
     * @param agentId Agent to get vote messages for
     * @return List of vote-related incoming messages
     */
    public List<IncomingMessage> getVoteMessages(int agentId) {
        List<IncomingMessage> voteMessages = new ArrayList<>();
        for (IncomingMessage message : messageHistory) {
            if (message.receiverId == agentId && 
                (message.messageContent.type == MessageType.VOTE_PROPOSAL ||
                 message.messageContent.type == MessageType.VOTE_RESPONSE)) {
                voteMessages.add(message);
            }
        }
        return voteMessages;
    }
    
    // ============================================================
    // WEEK 6: Mission Coordination - Task and Network Utilities
    // ============================================================
    
    /**
     * Broadcasts a task assignment to target agents or all agents.
     * Uses MessageType.TASK_ASSIGNMENT with configurable priority.
     * 
     * @param senderId Agent or system assigning the task
     * @param taskData Task information (taskId, type, location, requirements)
     * @param targetAgents List of agent IDs to assign, or null for broadcast
     * @return true if assignment queued successfully
     */
    public boolean broadcastTaskAssignment(int senderId, Map<String, Object> taskData, List<Integer> targetAgents) {
        Message message = new Message(MessageType.TASK_ASSIGNMENT, taskData);
        
        if (targetAgents == null || targetAgents.isEmpty()) {
            // Broadcast to all
            OutgoingMessage outgoing = new OutgoingMessage(
                senderId, -1, message, 2, 5, 60000);
            return sendMessage(outgoing);
        } else {
            // Send to specific agents
            boolean allQueued = true;
            for (Integer targetId : targetAgents) {
                OutgoingMessage outgoing = new OutgoingMessage(
                    senderId, targetId, message, 2, 5, 60000);
                if (!sendMessage(outgoing)) {
                    allQueued = false;
                }
            }
            return allQueued;
        }
    }
    
    /**
     * Gets all agents reachable from a given agent (direct or multi-hop).
     * Uses BFS to find all paths within hop limit.
     * 
     * @param agentId Source agent
     * @param maxHops Maximum hops to search (default 5)
     * @return Set of reachable agent IDs
     */
    public Set<Integer> getReachableAgents(int agentId, int maxHops) {
        Set<Integer> reachable = new HashSet<>();
        Queue<MessageRouter.PathNode> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        
        queue.offer(new MessageRouter.PathNode(agentId, 0, 0.0));
        visited.add(agentId);
        
        while (!queue.isEmpty()) {
            MessageRouter.PathNode current = queue.poll();
            reachable.add(current.agentId);
            
            if (current.hops >= maxHops) {
                continue;
            }
            
            NeighborInformation neighbors = networkTopology.get(current.agentId);
            if (neighbors != null) {
                for (NeighborAgent neighbor : neighbors.getCommunicatingNeighbors()) {
                    if (!visited.contains(neighbor.neighborId)) {
                        visited.add(neighbor.neighborId);
                        queue.offer(new MessageRouter.PathNode(neighbor.neighborId, current.hops + 1, 0.0));
                    }
                }
            }
        }
        
        return reachable;
    }
    
    /**
     * Identifies network partitions (disconnected groups of agents).
     * Useful for mission planning to understand swarm fragmentation.
     * 
     * @return List of partitions, each containing agent IDs in that partition
     */
    public List<Set<Integer>> getNetworkPartitions() {
        List<Set<Integer>> partitions = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        
        for (Integer agentId : networkTopology.keySet()) {
            if (!visited.contains(agentId)) {
                // BFS to find all connected agents
                Set<Integer> partition = getReachableAgents(agentId, 1000);
                partitions.add(partition);
                visited.addAll(partition);
            }
        }
        
        return partitions;
    }
    
    @Override
    public String toString() {
        return String.format("CommunicationManager{agents=%d, pending=%d, simulator=%s}", 
                           networkTopology.size(), messageQueue.size(), networkSimulator);
    }
}
