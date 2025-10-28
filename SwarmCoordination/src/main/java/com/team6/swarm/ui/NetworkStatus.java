/**
 * NETWORKSTATUS CLASS - Network Topology Visualization Data
 *
 * PURPOSE:
 * - Provides real-time network topology information for visualization
 * - Tracks active communication links between agents
 * - Monitors network health and performance metrics
 * - Enables network debugging and analysis
 *
 * STATUS INFORMATION:
 *
 * 1. TOPOLOGY DATA:
 *    - connections: List of active ConnectionInfo objects
 *    - networkGraph: Map of agent ID -> list of neighbor IDs
 *    - isolatedAgents: Agents with no connections
 *    - networkPartitions: Disconnected subgroups
 *
 * 2. CONNECTIVITY METRICS:
 *    - totalConnections: Number of active links
 *    - averageNeighbors: Mean connections per agent
 *    - networkDensity: How interconnected the network is (0.0-1.0)
 *    - networkDiameter: Maximum hops between any two agents
 *
 * 3. PERFORMANCE METRICS:
 *    - averageLatency: Mean message delivery time
 *    - packetLossRate: Percentage of failed messages
 *    - messagesThroughput: Messages per second
 *    - bandwidthUtilization: Network capacity usage
 *
 * 4. HEALTH INDICATORS:
 *    - networkHealth: Overall health score (0.0-1.0)
 *    - weakLinks: Connections with poor signal
 *    - criticalAgents: Agents essential for connectivity
 *    - redundancyLevel: Network fault tolerance
 *
 * 5. ACTIVITY TRACKING:
 *    - recentMessages: Recent message activity
 *    - messagesByType: Breakdown of message categories
 *    - busyAgents: Agents with high message traffic
 *    - idleAgents: Agents with no recent activity
 *
 * VISUALIZATION MODES:
 *
 * TOPOLOGY_VIEW:
 * - Show all connections as lines
 * - Color by signal strength
 * - Thickness by message volume
 * - Highlight weak links
 *
 * ACTIVITY_VIEW:
 * - Animate message flow
 * - Show message paths
 * - Highlight busy agents
 * - Display message queues
 *
 * HEALTH_VIEW:
 * - Color agents by connectivity
 * - Show isolated agents
 * - Highlight network partitions
 * - Display redundancy paths
 *
 * PERFORMANCE_VIEW:
 * - Show latency heatmap
 * - Display packet loss rates
 * - Visualize bandwidth usage
 * - Track throughput trends
 *
 * USAGE EXAMPLE:
 * NetworkStatus status = new NetworkStatus();
 * status.addConnection(connectionInfo);
 * status.updateMetrics();
 * visualizer.drawCommunicationLinks(status.connections);
 * statusPanel.displayNetworkHealth(status.networkHealth);
 *
 * INTEGRATION POINTS:
 * - Created by: CommunicationManager (John)
 * - Consumed by: NetworkVisualization, Visualizer, StatusPanel
 * - Published via: EventBus
 * - Updated: Every network topology refresh (typically 500ms)
 */
package com.team6.swarm.ui;

import com.team6.swarm.communication.ConnectionInfo;
import java.util.*;

public class NetworkStatus {
    // ==================== TOPOLOGY DATA ====================
    public List<ConnectionInfo> connections;
    public Map<Integer, List<Integer>> networkGraph;  // agent ID -> neighbor IDs
    public List<Integer> isolatedAgents;
    public List<List<Integer>> networkPartitions;  // Disconnected subgroups
    
    // ==================== CONNECTIVITY METRICS ====================
    public int totalConnections;
    public double averageNeighbors;
    public double networkDensity;  // 0.0 to 1.0
    public int networkDiameter;  // Max hops between any two agents
    public int totalAgents;
    
    // ==================== PERFORMANCE METRICS ====================
    public double averageLatency;  // milliseconds
    public double packetLossRate;  // 0.0 to 1.0
    public double messagesThroughput;  // messages per second
    public double bandwidthUtilization;  // 0.0 to 1.0
    
    // ==================== HEALTH INDICATORS ====================
    public double networkHealth;  // 0.0 to 1.0 (overall health score)
    public List<ConnectionInfo> weakLinks;  // Poor signal connections
    public List<Integer> criticalAgents;  // Essential for connectivity
    public double redundancyLevel;  // 0.0 to 1.0 (fault tolerance)
    
    // ==================== ACTIVITY TRACKING ====================
    public int recentMessages;  // Messages in last second
    public Map<String, Integer> messagesByType;  // Message type -> count
    public List<Integer> busyAgents;  // High traffic agents
    public List<Integer> idleAgents;  // No recent activity
    
    // ==================== METADATA ====================
    public long timestamp;
    public long updateInterval;  // How often this is refreshed
    public String statusMessage;
    
    /**
     * Default constructor
     */
    public NetworkStatus() {
        this.connections = new ArrayList<>();
        this.networkGraph = new HashMap<>();
        this.isolatedAgents = new ArrayList<>();
        this.networkPartitions = new ArrayList<>();
        this.weakLinks = new ArrayList<>();
        this.criticalAgents = new ArrayList<>();
        this.busyAgents = new ArrayList<>();
        this.idleAgents = new ArrayList<>();
        this.messagesByType = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.updateInterval = 500;  // 500ms default
        this.networkHealth = 1.0;
        this.networkDensity = 0.0;
        this.redundancyLevel = 0.0;
    }
    
    // ==================== UPDATE METHODS ====================
    
    /**
     * Add connection to network status
     */
    public void addConnection(ConnectionInfo connection) {
        connections.add(connection);
        
        // Update network graph
        networkGraph.computeIfAbsent(connection.agentA, k -> new ArrayList<>())
                   .add(connection.agentB);
        networkGraph.computeIfAbsent(connection.agentB, k -> new ArrayList<>())
                   .add(connection.agentA);
        
        // Check if weak link
        if (connection.strength < 0.4 || !connection.isReliable()) {
            weakLinks.add(connection);
        }
    }
    
    /**
     * Update all network metrics
     */
    public void updateMetrics() {
        totalConnections = connections.size();
        
        // Calculate average neighbors
        if (!networkGraph.isEmpty()) {
            int totalNeighbors = networkGraph.values().stream()
                                            .mapToInt(List::size)
                                            .sum();
            averageNeighbors = (double) totalNeighbors / networkGraph.size();
        }
        
        // Calculate network density
        if (totalAgents > 1) {
            int maxPossibleConnections = totalAgents * (totalAgents - 1) / 2;
            networkDensity = maxPossibleConnections > 0 ? 
                           (double) totalConnections / maxPossibleConnections : 0.0;
        }
        
        // Find isolated agents
        isolatedAgents.clear();
        for (int agentId = 0; agentId < totalAgents; agentId++) {
            if (!networkGraph.containsKey(agentId) || 
                networkGraph.get(agentId).isEmpty()) {
                isolatedAgents.add(agentId);
            }
        }
        
        // Calculate average latency
        if (!connections.isEmpty()) {
            averageLatency = connections.stream()
                                       .mapToDouble(c -> c.averageLatency)
                                       .average()
                                       .orElse(0.0);
        }
        
        // Calculate network health
        calculateNetworkHealth();
        
        // Find critical agents (bridges)
        findCriticalAgents();
        
        // Calculate redundancy level
        calculateRedundancy();
        
        // Update timestamp
        timestamp = System.currentTimeMillis();
    }
    
    /**
     * Calculate overall network health score
     */
    private void calculateNetworkHealth() {
        double healthScore = 1.0;
        
        // Penalty for isolated agents
        if (totalAgents > 0) {
            double isolationPenalty = (double) isolatedAgents.size() / totalAgents * 0.3;
            healthScore -= isolationPenalty;
        }
        
        // Penalty for weak links
        if (!connections.isEmpty()) {
            double weakLinkPenalty = (double) weakLinks.size() / connections.size() * 0.2;
            healthScore -= weakLinkPenalty;
        }
        
        // Penalty for packet loss
        healthScore -= packetLossRate * 0.3;
        
        // Penalty for low density
        if (networkDensity < 0.3) {
            healthScore -= (0.3 - networkDensity) * 0.2;
        }
        
        networkHealth = Math.max(0.0, Math.min(1.0, healthScore));
    }
    
    /**
     * Find critical agents (whose removal would partition network)
     */
    private void findCriticalAgents() {
        criticalAgents.clear();
        
        // Simple heuristic: agents with high betweenness
        // (agents that connect otherwise disconnected groups)
        for (Map.Entry<Integer, List<Integer>> entry : networkGraph.entrySet()) {
            int agentId = entry.getKey();
            List<Integer> neighbors = entry.getValue();
            
            // If removing this agent would disconnect neighbors
            if (neighbors.size() >= 2) {
                boolean isCritical = false;
                for (int i = 0; i < neighbors.size(); i++) {
                    for (int j = i + 1; j < neighbors.size(); j++) {
                        int neighbor1 = neighbors.get(i);
                        int neighbor2 = neighbors.get(j);
                        
                        // Check if neighbor1 and neighbor2 are only connected through agentId
                        if (!areDirectlyConnected(neighbor1, neighbor2)) {
                            isCritical = true;
                            break;
                        }
                    }
                    if (isCritical) break;
                }
                
                if (isCritical) {
                    criticalAgents.add(agentId);
                }
            }
        }
    }
    
    /**
     * Check if two agents are directly connected
     */
    private boolean areDirectlyConnected(int agent1, int agent2) {
        List<Integer> neighbors = networkGraph.get(agent1);
        return neighbors != null && neighbors.contains(agent2);
    }
    
    /**
     * Calculate network redundancy level
     */
    private void calculateRedundancy() {
        if (totalAgents <= 1) {
            redundancyLevel = 0.0;
            return;
        }
        
        // Redundancy based on:
        // 1. Multiple paths between agents
        // 2. Low number of critical agents
        // 3. High network density
        
        double pathRedundancy = networkDensity;  // More connections = more paths
        double criticalPenalty = totalAgents > 0 ? 
                                (double) criticalAgents.size() / totalAgents : 0.0;
        
        redundancyLevel = Math.max(0.0, pathRedundancy - criticalPenalty * 0.5);
    }
    
    /**
     * Find network partitions (disconnected subgroups)
     */
    public void findNetworkPartitions() {
        networkPartitions.clear();
        Set<Integer> visited = new HashSet<>();
        
        for (Integer agentId : networkGraph.keySet()) {
            if (!visited.contains(agentId)) {
                List<Integer> partition = new ArrayList<>();
                explorePartition(agentId, visited, partition);
                if (!partition.isEmpty()) {
                    networkPartitions.add(partition);
                }
            }
        }
    }
    
    /**
     * Depth-first search to find connected component
     */
    private void explorePartition(int agentId, Set<Integer> visited, List<Integer> partition) {
        visited.add(agentId);
        partition.add(agentId);
        
        List<Integer> neighbors = networkGraph.get(agentId);
        if (neighbors != null) {
            for (Integer neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    explorePartition(neighbor, visited, partition);
                }
            }
        }
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Get connection between two agents
     */
    public ConnectionInfo getConnection(int agent1, int agent2) {
        for (ConnectionInfo conn : connections) {
            if ((conn.agentA == agent1 && conn.agentB == agent2) ||
                (conn.agentA == agent2 && conn.agentB == agent1)) {
                return conn;
            }
        }
        return null;
    }
    
    /**
     * Get neighbors of an agent
     */
    public List<Integer> getNeighbors(int agentId) {
        return networkGraph.getOrDefault(agentId, new ArrayList<>());
    }
    
    /**
     * Check if agent is isolated
     */
    public boolean isIsolated(int agentId) {
        return isolatedAgents.contains(agentId);
    }
    
    /**
     * Check if agent is critical
     */
    public boolean isCritical(int agentId) {
        return criticalAgents.contains(agentId);
    }
    
    /**
     * Get network health description
     */
    public String getHealthDescription() {
        if (networkHealth >= 0.8) return "EXCELLENT";
        if (networkHealth >= 0.6) return "GOOD";
        if (networkHealth >= 0.4) return "FAIR";
        if (networkHealth >= 0.2) return "POOR";
        return "CRITICAL";
    }
    
    /**
     * Get connectivity description
     */
    public String getConnectivityDescription() {
        if (isolatedAgents.isEmpty() && networkPartitions.size() <= 1) {
            return "FULLY CONNECTED";
        } else if (networkPartitions.size() > 1) {
            return "PARTITIONED (" + networkPartitions.size() + " groups)";
        } else if (!isolatedAgents.isEmpty()) {
            return "ISOLATED AGENTS (" + isolatedAgents.size() + ")";
        }
        return "CONNECTED";
    }
    
    /**
     * Get network summary
     */
    public String getSummary() {
        return String.format("Network: %d agents, %d connections, %.0f%% density, %s health", 
                           totalAgents, totalConnections, networkDensity * 100, 
                           getHealthDescription());
    }
    
    /**
     * Get detailed status report
     */
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NETWORK STATUS ===\n\n");
        
        sb.append("Topology:\n");
        sb.append(String.format("  Total Agents: %d\n", totalAgents));
        sb.append(String.format("  Total Connections: %d\n", totalConnections));
        sb.append(String.format("  Average Neighbors: %.1f\n", averageNeighbors));
        sb.append(String.format("  Network Density: %.1f%%\n", networkDensity * 100));
        sb.append(String.format("  Network Diameter: %d hops\n", networkDiameter));
        
        sb.append("\nConnectivity:\n");
        sb.append(String.format("  Status: %s\n", getConnectivityDescription()));
        sb.append(String.format("  Isolated Agents: %d\n", isolatedAgents.size()));
        sb.append(String.format("  Network Partitions: %d\n", networkPartitions.size()));
        
        sb.append("\nPerformance:\n");
        sb.append(String.format("  Average Latency: %.1fms\n", averageLatency));
        sb.append(String.format("  Packet Loss Rate: %.1f%%\n", packetLossRate * 100));
        sb.append(String.format("  Throughput: %.1f msg/s\n", messagesThroughput));
        sb.append(String.format("  Bandwidth Usage: %.1f%%\n", bandwidthUtilization * 100));
        
        sb.append("\nHealth:\n");
        sb.append(String.format("  Overall Health: %.1f%% (%s)\n", 
                               networkHealth * 100, getHealthDescription()));
        sb.append(String.format("  Weak Links: %d\n", weakLinks.size()));
        sb.append(String.format("  Critical Agents: %d\n", criticalAgents.size()));
        sb.append(String.format("  Redundancy Level: %.1f%%\n", redundancyLevel * 100));
        
        sb.append("\nActivity:\n");
        sb.append(String.format("  Recent Messages: %d\n", recentMessages));
        sb.append(String.format("  Busy Agents: %d\n", busyAgents.size()));
        sb.append(String.format("  Idle Agents: %d\n", idleAgents.size()));
        
        return sb.toString();
    }
    
    /**
     * Get visualization data
     */
    public Map<String, Object> getVisualizationData() {
        Map<String, Object> data = new HashMap<>();
        data.put("connections", connections);
        data.put("networkGraph", networkGraph);
        data.put("isolatedAgents", isolatedAgents);
        data.put("weakLinks", weakLinks);
        data.put("criticalAgents", criticalAgents);
        data.put("networkHealth", networkHealth);
        data.put("networkDensity", networkDensity);
        data.put("averageLatency", averageLatency);
        return data;
    }
    
    /**
     * Clone status
     */
    public NetworkStatus clone() {
        NetworkStatus clone = new NetworkStatus();
        clone.connections = new ArrayList<>(this.connections);
        clone.networkGraph = new HashMap<>(this.networkGraph);
        clone.isolatedAgents = new ArrayList<>(this.isolatedAgents);
        clone.networkPartitions = new ArrayList<>(this.networkPartitions);
        clone.totalConnections = this.totalConnections;
        clone.averageNeighbors = this.averageNeighbors;
        clone.networkDensity = this.networkDensity;
        clone.networkDiameter = this.networkDiameter;
        clone.totalAgents = this.totalAgents;
        clone.averageLatency = this.averageLatency;
        clone.packetLossRate = this.packetLossRate;
        clone.messagesThroughput = this.messagesThroughput;
        clone.bandwidthUtilization = this.bandwidthUtilization;
        clone.networkHealth = this.networkHealth;
        clone.weakLinks = new ArrayList<>(this.weakLinks);
        clone.criticalAgents = new ArrayList<>(this.criticalAgents);
        clone.redundancyLevel = this.redundancyLevel;
        clone.recentMessages = this.recentMessages;
        clone.messagesByType = new HashMap<>(this.messagesByType);
        clone.busyAgents = new ArrayList<>(this.busyAgents);
        clone.idleAgents = new ArrayList<>(this.idleAgents);
        clone.statusMessage = this.statusMessage;
        return clone;
    }
    
    @Override
    public String toString() {
        return String.format("NetworkStatus{agents=%d, connections=%d, health=%.0f%%, %s}", 
                           totalAgents, totalConnections, networkHealth * 100, 
                           getConnectivityDescription());
    }
}
