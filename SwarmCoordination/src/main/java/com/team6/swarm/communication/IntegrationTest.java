/**
 * INTEGRATIONTEST CLASS - Communication System Integration Validation
 *
 * PURPOSE:
 * - Validates communication integration methods across all weeks
 * - Tests message listener callbacks (Week 4)
 * - Tests voting message support (Week 5)
 * - Tests mission coordination utilities (Week 6)
 *
 * INTEGRATION TESTS:
 * 1. Week 4: Message listener registration and callback delivery
 * 2. Week 5: Vote proposal broadcasting and response handling
 * 3. Week 6: Task assignment routing and network partition detection
 */
package com.team6.swarm.communication;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.Point2D;
import com.team6.swarm.core.AgentStatus;
import com.team6.swarm.core.Vector2D;
import java.util.*;

public class IntegrationTest {
    
    public static void main(String[] args) {
        System.out.println("=== Communication System Integration Tests ===");
        
        try {
            // Test Week 4: Message Listeners
            testMessageListeners();
            
            // Test Week 5: Voting Messages
            testVotingMessages();
            
            // Test Week 6: Mission Coordination
            testMissionCoordination();
            
            System.out.println("\n=== All Integration Tests Passed ===");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    // ===== WEEK 4: MESSAGE LISTENERS =====
    private static void testMessageListeners() {
        System.out.println("\n--- Week 4: Message Listeners ---");
        
        CommunicationManager manager = new CommunicationManager();
        
        // Create test agent states
        List<AgentState> agents = createTestAgents();
        manager.updateTopology(agents);
        
        // Test listener registration
        List<IncomingMessage> receivedMessages = new ArrayList<>();
        
        manager.registerMessageListener(2, (message) -> {
            receivedMessages.add(message);
        });
        
        // Send a test message
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "message");
        Message message = new Message(MessageType.POSITION_UPDATE, payload);
        OutgoingMessage outgoing = new OutgoingMessage(1, 2, message);
        
        boolean sent = manager.sendMessage(outgoing);
        assert sent : "Message should be sent";
        
        // Process messages
        manager.processMessages();
        
        // Verify listener was called
        assert receivedMessages.size() > 0 : "Listener should receive message";
        assert receivedMessages.get(0).receiverId == 2 : "Message should be for agent 2";
        
        // Test listener unregistration
        manager.unregisterMessageListener(2);
        manager.clearPendingMessages();
        receivedMessages.clear();
        
        manager.sendMessage(outgoing);
        manager.processMessages();
        
        // Verify listener no longer receives
        assert receivedMessages.size() == 0 : "Listener should not receive after unregister";
        
        System.out.println("✓ Message listener tests passed");
    }
    
    // ===== WEEK 5: VOTING MESSAGES =====
    private static void testVotingMessages() {
        System.out.println("\n--- Week 5: Voting Messages ---");
        
        CommunicationManager manager = new CommunicationManager();
        List<AgentState> agents = createTestAgents();
        manager.updateTopology(agents);
        
        // Test broadcast vote
        Map<String, Object> voteProposal = new HashMap<>();
        voteProposal.put("question", "Go left or right?");
        voteProposal.put("options", Arrays.asList("LEFT", "RIGHT"));
        voteProposal.put("deadline", System.currentTimeMillis() + 10000);
        
        boolean broadcast = manager.broadcastVote(1, voteProposal);
        assert broadcast : "Vote broadcast should succeed";
        
        // Process messages
        manager.processMessages();
        
        // Test send vote response
        Map<String, Object> voteResponse = new HashMap<>();
        voteResponse.put("proposalId", "vote123");
        voteResponse.put("choice", "LEFT");
        
        boolean response = manager.sendVoteResponse(2, 1, voteResponse);
        assert response : "Vote response should succeed";
        
        manager.processMessages();
        
        // Test get vote messages
        List<IncomingMessage> voteMessages = manager.getVoteMessages(2);
        assert voteMessages.size() > 0 : "Should have vote messages for agent 2";
        
        System.out.println("✓ Voting message tests passed");
    }
    
    // ===== WEEK 6: MISSION COORDINATION =====
    private static void testMissionCoordination() {
        System.out.println("\n--- Week 6: Mission Coordination ---");
        
        CommunicationManager manager = new CommunicationManager();
        List<AgentState> agents = createTestAgents();
        manager.updateTopology(agents);
        
        // Test broadcast task assignment
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("taskId", "T001");
        taskData.put("type", "PATROL");
        taskData.put("location", new Point2D(500, 500));
        taskData.put("priority", "HIGH");
        
        // Test broadcast to all
        boolean broadcast = manager.broadcastTaskAssignment(1, taskData, null);
        assert broadcast : "Task broadcast should succeed";
        
        // Test targeted assignment
        List<Integer> targets = Arrays.asList(2, 3);
        boolean targeted = manager.broadcastTaskAssignment(1, taskData, targets);
        assert targeted : "Targeted task assignment should succeed";
        
        manager.processMessages();
        
        // Test get reachable agents
        Set<Integer> reachable = manager.getReachableAgents(1, 5);
        assert reachable.contains(1) : "Should include self";
        assert reachable.size() >= 3 : "Should have multiple reachable agents";
        
        // Test network partitions
        List<Set<Integer>> partitions = manager.getNetworkPartitions();
        assert partitions.size() > 0 : "Should have at least one partition";
        assert partitions.get(0).size() >= 3 : "Partition should contain multiple agents";
        
        System.out.println("✓ Mission coordination tests passed");
    }
    
    // ===== HELPER METHODS =====
    private static List<AgentState> createTestAgents() {
        List<AgentState> agents = new ArrayList<>();
        
        // Create 5 test agents in close proximity
        for (int i = 1; i <= 5; i++) {
            AgentState agent = new AgentState();
            agent.agentId = i;
            agent.agentName = "Agent_" + i;
            agent.position = new Point2D(i * 50, i * 50); // Close together
            agent.velocity = new Vector2D(10, 5);
            agent.status = AgentStatus.ACTIVE;
            agent.batteryLevel = 0.8;
            agent.maxSpeed = 50.0;
            agent.communicationRange = 200.0; // Wide range for testing
            agent.lastUpdateTime = System.currentTimeMillis();
            
            agents.add(agent);
        }
        
        return agents;
    }
}

