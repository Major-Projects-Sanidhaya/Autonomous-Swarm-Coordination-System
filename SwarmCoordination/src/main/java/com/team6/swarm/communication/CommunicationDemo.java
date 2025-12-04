/**
 * COMMUNICATIONDEMO CLASS - Demo Scenario for Weeks 7-14 Features
 *
 * PURPOSE:
 * - Demonstrates consensus voting (Week 7-8)
 * - Shows fault tolerance with retry (Week 9-10)
 * - Displays performance metrics (Week 11-12)
 * - Validates network partition detection (Week 9-10)
 *
 * DEMO SCENARIO:
 * A swarm of 5 agents needs to vote on a formation strategy.
 * The system handles network failures gracefully and tracks performance.
 */
package com.team6.swarm.communication;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.Point2D;
import com.team6.swarm.core.AgentStatus;
import com.team6.swarm.core.Vector2D;
import java.util.*;

public class CommunicationDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Communication System Demo (Weeks 7-14) ===\n");
        
        // Setup: Create communication manager and agents
        CommunicationManager manager = new CommunicationManager();
        List<AgentState> agents = createSwarmAgents(5);
        manager.updateTopology(agents);
        
        System.out.println("✓ Swarm initialized: " + agents.size() + " agents");
        System.out.println("✓ Network topology updated\n");
        
        // ===== DEMO 1: Consensus Voting (Week 7-8) =====
        System.out.println("--- Demo 1: Consensus Voting (Week 7-8) ---");
        demonstrateConsensusVoting(manager, agents);
        
        // ===== DEMO 2: Fault Tolerance (Week 9-10) =====
        System.out.println("\n--- Demo 2: Fault Tolerance (Week 9-10) ---");
        demonstrateFaultTolerance(manager);
        
        // ===== DEMO 3: Performance Metrics (Week 11-12) =====
        System.out.println("\n--- Demo 3: Performance Metrics (Week 11-12) ---");
        demonstratePerformanceMetrics(manager);
        
        // ===== DEMO 4: Network Partition Detection (Week 9-10) =====
        System.out.println("\n--- Demo 4: Network Partition Detection (Week 9-10) ---");
        demonstratePartitionDetection(manager, agents);
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    /**
     * Demo 1: Consensus Voting
     * Shows how VotingProtocol tracks vote proposals and responses
     */
    private static void demonstrateConsensusVoting(CommunicationManager manager, List<AgentState> agents) {
        VotingProtocol protocol = new VotingProtocol(manager);
        
        // Create a vote proposal: "Which formation should we use?"
        Set<Integer> expectedVoters = new HashSet<>(Arrays.asList(2, 3, 4, 5));
        Map<String, Object> proposal = new HashMap<>();
        proposal.put("proposalId", "formation-vote-1");
        proposal.put("question", "Which formation should we use?");
        proposal.put("options", Arrays.asList("LINE", "V_SHAPE", "CIRCLE"));
        proposal.put("deadline", System.currentTimeMillis() + 10000);
        
        // Start the vote
        protocol.startVote(1, proposal, expectedVoters);
        System.out.println("  Agent 1 initiated vote: " + proposal.get("question"));
        System.out.println("  Expected voters: " + expectedVoters);
        
        // Process messages to deliver the vote proposal
        manager.processMessages();
        
        // Simulate responses from voters
        Map<String, Object> response2 = new HashMap<>();
        response2.put("proposalId", "formation-vote-1");
        response2.put("choice", "V_SHAPE");
        protocol.recordResponse(2, response2);
        System.out.println("  Agent 2 voted: V_SHAPE");
        
        Map<String, Object> response3 = new HashMap<>();
        response3.put("proposalId", "formation-vote-1");
        response3.put("choice", "V_SHAPE");
        protocol.recordResponse(3, response3);
        System.out.println("  Agent 3 voted: V_SHAPE");
        
        Map<String, Object> response4 = new HashMap<>();
        response4.put("proposalId", "formation-vote-1");
        response4.put("choice", "LINE");
        protocol.recordResponse(4, response4);
        System.out.println("  Agent 4 voted: LINE");
        
        Map<String, Object> response5 = new HashMap<>();
        response5.put("proposalId", "formation-vote-1");
        response5.put("choice", "V_SHAPE");
        protocol.recordResponse(5, response5);
        System.out.println("  Agent 5 voted: V_SHAPE");
        
        // Get vote result
        VotingProtocol.VoteResult result = protocol.getVoteResult("formation-vote-1");
        if (result == null) {
            System.out.println("  ⚠ Vote not found");
            return;
        }
        
        System.out.println("\n  Vote Result:");
        System.out.println("    Complete: " + result.complete);
        System.out.println("    Responses: " + result.responses.size() + "/" + result.expectedVoters.size());
        System.out.println("    Expired: " + result.expired);
        
        if (result.complete) {
            System.out.println("  ✓ Consensus reached!");
        } else {
            System.out.println("  ⚠ Waiting for more votes...");
        }
    }
    
    /**
     * Demo 2: Fault Tolerance
     * Shows how FailureRecoveryPolicy retries critical messages
     */
    private static void demonstrateFaultTolerance(CommunicationManager manager) {
        FailureRecoveryPolicy recovery = new FailureRecoveryPolicy(manager, 3);
        
        // Create a critical emergency message
        Map<String, Object> emergency = new HashMap<>();
        emergency.put("alert", "Obstacle detected ahead!");
        emergency.put("location", new Point2D(100, 100));
        emergency.put("priority", "CRITICAL");
        
        Message criticalMsg = new Message(MessageType.EMERGENCY_ALERT, emergency);
        OutgoingMessage outgoing = new OutgoingMessage(1, 2, criticalMsg, 1, 5, 30000);
        
        System.out.println("  Sending critical emergency alert (Agent 1 → Agent 2)");
        boolean sent = recovery.sendCritical(outgoing);
        System.out.println("  Message queued: " + sent);
        
        // Process messages (simulates delivery attempts)
        manager.processMessages();
        
        // Check if message was delivered
        List<IncomingMessage> history = manager.getMessageHistory();
        boolean delivered = history.stream().anyMatch(m -> 
            m.messageContent.type == MessageType.EMERGENCY_ALERT &&
            m.receiverId == 2
        );
        
        if (delivered) {
            System.out.println("  ✓ Critical message delivered successfully");
        } else {
            System.out.println("  ⚠ Message may need retry (simulated network conditions)");
        }
    }
    
    /**
     * Demo 3: Performance Metrics
     * Shows how CommunicationMetrics aggregates performance data
     */
    private static void demonstratePerformanceMetrics(CommunicationManager manager) {
        CommunicationMetrics metrics = new CommunicationMetrics(manager);
        
        // Send some messages to generate metrics
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("update", "status_" + i);
            Message msg = new Message(MessageType.STATUS_UPDATE, payload);
            OutgoingMessage outgoing = new OutgoingMessage(i, (i % 5) + 1, msg);
            manager.sendMessage(outgoing);
        }
        
        manager.processMessages();
        
        // Get metrics snapshot
        CommunicationMetrics.CommunicationMetricsSnapshot snapshot = metrics.getSnapshot();
        
        System.out.println("  Performance Metrics:");
        System.out.println("    Messages/sec: " + String.format("%.2f", snapshot.messagesPerSecond));
        System.out.println("    Avg Latency: " + String.format("%.1f", snapshot.averageLatency) + "ms");
        System.out.println("    Failure Rate: " + String.format("%.1f", snapshot.failureRate * 100) + "%");
        System.out.println("    Pending Messages: " + snapshot.pendingMessages);
        System.out.println("  ✓ Metrics collected successfully");
    }
    
    /**
     * Demo 4: Network Partition Detection
     * Shows how the system detects disconnected agent groups
     */
    private static void demonstratePartitionDetection(CommunicationManager manager, List<AgentState> agents) {
        // Get all network partitions
        List<Set<Integer>> partitions = manager.getNetworkPartitions();
        
        System.out.println("  Network Partitions Detected: " + partitions.size());
        for (int i = 0; i < partitions.size(); i++) {
            Set<Integer> partition = partitions.get(i);
            System.out.println("    Partition " + (i + 1) + ": " + partition.size() + " agents - " + partition);
        }
        
        // Check which partition a specific agent belongs to
        Set<Integer> agent1Partition = manager.getPartitionForAgent(1);
        if (agent1Partition != null) {
            System.out.println("  Agent 1 is in partition with " + agent1Partition.size() + " agents");
        } else {
            System.out.println("  ⚠ Agent 1 not found in any partition");
        }
        
        // Check reachability
        Set<Integer> reachable = manager.getReachableAgents(1, 5);
        System.out.println("  Agents reachable from Agent 1 (within 5 hops): " + reachable.size());
        System.out.println("  ✓ Partition detection working");
    }
    
    /**
     * Helper: Create a swarm of agents for testing
     */
    private static List<AgentState> createSwarmAgents(int count) {
        List<AgentState> agents = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            AgentState agent = new AgentState();
            agent.agentId = i;
            agent.agentName = "Agent_" + i;
            // Position agents close together for communication
            agent.position = new Point2D(i * 30, i * 30);
            agent.velocity = new Vector2D(5, 5);
            agent.status = AgentStatus.ACTIVE;
            agent.batteryLevel = 0.8;
            agent.maxSpeed = 50.0;
            agent.communicationRange = 200.0;
            agent.lastUpdateTime = System.currentTimeMillis();
            
            agents.add(agent);
        }
        
        return agents;
    }
}


