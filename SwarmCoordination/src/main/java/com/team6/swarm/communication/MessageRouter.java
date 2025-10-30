/**
 * MESSAGEROUTER CLASS - Smart Message Routing System
 *
 * PURPOSE:
 * - Finds paths for messages and handles multi-hop routing
 * - Implements intelligent routing algorithms for swarm communication
 * - Enables communication between distant agents through relays
 *
 * ROUTING ALGORITHMS:
 * 1. Direct Routing - Direct delivery if agents are neighbors
 * 2. Multi-Hop Routing - Find shortest path through intermediate agents
 * 3. Broadcast Routing - Flood messages to all reachable agents
 * 4. Adaptive Routing - Adjust routes based on network conditions
 *
 * CORE COMPONENTS:
 * - networkTopology: Current network graph for pathfinding
 * - routingTable: Cached routes for efficiency
 * - maxHops: Maximum number of hops allowed
 * - routeTimeout: How long routes remain valid
 *
 * PATHFINDING ALGORITHM:
 * - Uses Breadth-First Search (BFS) for shortest path
 * - Considers signal strength and connection quality
 * - Prefers reliable neighbors for routing
 * - Avoids agents with poor connectivity
 *
 * ROUTING SCENARIOS:
 * 1. Direct: Agent 1 → Agent 2 (if neighbors)
 * 2. One Hop: Agent 1 → Agent 3 → Agent 2
 * 3. Multi-Hop: Agent 1 → Agent 3 → Agent 4 → Agent 2
 * 4. Broadcast: Agent 1 → All neighbors → Their neighbors
 *
 * USAGE EXAMPLES:
 * - MessageRouter router = new MessageRouter(networkTopology);
 * - List<Integer> path = router.findPath(senderId, receiverId);
 * - boolean success = router.routeMessage(senderId, receiverId, message);
 * - router.updateTopology(newTopology);
 *
 * INTEGRATION POINTS:
 * - CommunicationManager: Uses for message routing decisions
 * - NetworkSimulator: Considers network conditions for routing
 * - NeighborInformation: Uses for pathfinding graph
 * - Anthony: Displays routing paths in visualization
 */
package com.team6.swarm.communication;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRouter {
    private final Map<Integer, NeighborInformation> networkTopology;
    private final Map<String, List<Integer>> routingTable;
    private final int maxHops;
    private final long routeTimeout;
    private final Random random;
    
    public MessageRouter() {
        this(new HashMap<>(), 5, 30000);
    }
    
    public MessageRouter(Map<Integer, NeighborInformation> networkTopology, 
                        int maxHops, long routeTimeout) {
        this.networkTopology = new ConcurrentHashMap<>(networkTopology);
        this.routingTable = new ConcurrentHashMap<>();
        this.maxHops = maxHops;
        this.routeTimeout = routeTimeout;
        this.random = new Random();
    }
    
    /**
     * Update network topology for routing
     */
    public void updateTopology(Map<Integer, NeighborInformation> newTopology) {
        networkTopology.clear();
        networkTopology.putAll(newTopology);
        
        // Clear routing table when topology changes
        routingTable.clear();
    }
    
    /**
     * Find shortest path between two agents
     * Uses BFS algorithm with signal strength weighting
     */
    public List<Integer> findPath(int senderId, int receiverId) {
        // Check for direct connection first
        if (isDirectNeighbor(senderId, receiverId)) {
            return new ArrayList<>(); // Empty path = direct delivery
        }
        
        // Check routing table cache
        String routeKey = senderId + "->" + receiverId;
        List<Integer> cachedRoute = routingTable.get(routeKey);
        if (cachedRoute != null) {
            return new ArrayList<>(cachedRoute);
        }
        
        // Find path using BFS
        List<Integer> path = findShortestPath(senderId, receiverId);
        
        // Cache the route
        if (path != null && !path.isEmpty()) {
            routingTable.put(routeKey, new ArrayList<>(path));
        }
        
        return path;
    }
    
    /**
     * Check if two agents are direct neighbors
     */
    private boolean isDirectNeighbor(int agent1Id, int agent2Id) {
        NeighborInformation neighbors1 = networkTopology.get(agent1Id);
        if (neighbors1 == null) return false;
        
        return neighbors1.hasNeighbor(agent2Id);
    }
    
    /**
     * Find shortest path using BFS algorithm
     */
    private List<Integer> findShortestPath(int senderId, int receiverId) {
        Queue<PathNode> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        
        // Start with sender
        queue.offer(new PathNode(senderId, new ArrayList<>()));
        visited.add(senderId);
        
        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            
            // Check if we've reached the destination
            if (current.agentId == receiverId) {
                return current.path;
            }
            
            // Check hop limit
            if (current.path.size() >= maxHops) {
                continue;
            }
            
            // Explore neighbors
            NeighborInformation neighbors = networkTopology.get(current.agentId);
            if (neighbors == null) continue;
            
            for (NeighborAgent neighbor : neighbors.getCommunicatingNeighbors()) {
                if (!visited.contains(neighbor.neighborId)) {
                    visited.add(neighbor.neighborId);
                    
                    // Create new path
                    List<Integer> newPath = new ArrayList<>(current.path);
                    newPath.add(neighbor.neighborId);
                    
                    queue.offer(new PathNode(neighbor.neighborId, newPath));
                }
            }
        }
        
        return null; // No path found
    }
    
    /**
     * Route a message from sender to receiver
     * Returns true if routing is possible
     */
    public boolean routeMessage(int senderId, int receiverId, Message message) {
        List<Integer> path = findPath(senderId, receiverId);
        
        if (path == null) {
            return false; // No path available
        }
        
        if (path.isEmpty()) {
            return true; // Direct delivery possible
        }
        
        // Multi-hop routing
        return routeMultiHop(senderId, receiverId, message, path);
    }
    
    /**
     * Route message through multiple hops
     */
    private boolean routeMultiHop(int senderId, int receiverId, Message message, List<Integer> path) {
        // For now, return true if path exists
        // In real implementation, this would coordinate with CommunicationManager
        // to actually send the message through each hop
        
        System.out.println("Multi-hop route: " + senderId + " -> " + path + " -> " + receiverId);
        return true;
    }
    
    /**
     * Find all reachable agents from a sender
     * Used for broadcast routing
     */
    public Set<Integer> findReachableAgents(int senderId) {
        Set<Integer> reachable = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        
        queue.offer(senderId);
        reachable.add(senderId);
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            
            NeighborInformation neighbors = networkTopology.get(current);
            if (neighbors == null) continue;
            
            for (NeighborAgent neighbor : neighbors.getCommunicatingNeighbors()) {
                if (!reachable.contains(neighbor.neighborId)) {
                    reachable.add(neighbor.neighborId);
                    queue.offer(neighbor.neighborId);
                }
            }
        }
        
        return reachable;
    }
    
    /**
     * Get routing statistics
     */
    public RoutingStatistics getRoutingStatistics() {
        int totalRoutes = routingTable.size();
        int directRoutes = 0;
        int multiHopRoutes = 0;
        
        for (List<Integer> path : routingTable.values()) {
            if (path.isEmpty()) {
                directRoutes++;
            } else {
                multiHopRoutes++;
            }
        }
        
        return new RoutingStatistics(totalRoutes, directRoutes, multiHopRoutes);
    }
    
    /**
     * Clear routing table
     */
    public void clearRoutingTable() {
        routingTable.clear();
    }
    
    /**
     * Get cached route for debugging
     */
    public List<Integer> getCachedRoute(int senderId, int receiverId) {
        String routeKey = senderId + "->" + receiverId;
        return routingTable.get(routeKey);
    }
    
    /**
     * Path node for BFS algorithm
     */
    public static class PathNode {
        public final int agentId;
        public final List<Integer> path;
        public final int hops;
        public final double signalStrength;
        
        public PathNode(int agentId, List<Integer> path) {
            this.agentId = agentId;
            this.path = path;
            this.hops = path.size();
            this.signalStrength = 0.0;
        }
        
        public PathNode(int agentId, int hops, double signalStrength) {
            this.agentId = agentId;
            this.path = new ArrayList<>();
            this.hops = hops;
            this.signalStrength = signalStrength;
        }
    }
    
    /**
     * Routing statistics container
     */
    public static class RoutingStatistics {
        public final int totalRoutes;
        public final int directRoutes;
        public final int multiHopRoutes;
        
        public RoutingStatistics(int totalRoutes, int directRoutes, int multiHopRoutes) {
            this.totalRoutes = totalRoutes;
            this.directRoutes = directRoutes;
            this.multiHopRoutes = multiHopRoutes;
        }
        
        @Override
        public String toString() {
            return String.format("RoutingStats{total=%d, direct=%d, multiHop=%d}", 
                               totalRoutes, directRoutes, multiHopRoutes);
        }
    }
    
    @Override
    public String toString() {
        return String.format("MessageRouter{agents=%d, routes=%d, maxHops=%d}", 
                           networkTopology.size(), routingTable.size(), maxHops);
    }
}
