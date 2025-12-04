/**
 * INTEGRATIONTEST CLASS - Communication System Integration Validation
 *
 * PURPOSE:
 * - Validates communication integration methods across all weeks (Weeks 4-12)
 * - Comprehensive integration test suite for production readiness
 *
 * INTEGRATION TESTS:
 * 1. Week 4: Message listener registration and callback delivery
 * 2. Week 5: Vote proposal broadcasting and response handling
 * 3. Week 6: Task assignment routing and network partition detection
 * 4. Week 7-8: Consensus support (VotingProtocol integration)
 * 5. Week 9-10: Fault tolerance (FailureRecoveryPolicy, partition helpers)
 * 6. Week 11-12: Performance metrics aggregation
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
            
            // Test Week 7–8: Consensus Support
            testConsensusSupport();

            // Test Week 9–10: Fault Tolerance
            testFaultTolerance();
            
            // Test Week 11–12: Performance Metrics
            testPerformanceMetrics();
            
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
    
    // ===== WEEK 7–8: CONSENSUS SUPPORT =====
    private static void testConsensusSupport() {
        System.out.println("\n--- Week 7–8: Consensus Support ---");
        
        CommunicationManager manager = new CommunicationManager();
        List<AgentState> agents = createTestAgents();
        manager.updateTopology(agents);
        
        VotingProtocol protocol = new VotingProtocol(manager);
        
        // Define expected voters (agents 2 and 3)
        Set<Integer> expectedVoters = new HashSet<>();
        expectedVoters.add(2);
        expectedVoters.add(3);
        
        // Create proposal payload with required fields
        Map<String, Object> proposal = new HashMap<>();
        proposal.put("proposalId", "vote-consensus-1");
        proposal.put("question", "Choose formation");
        proposal.put("options", Arrays.asList("LINE", "V_SHAPE"));
        proposal.put("deadline", System.currentTimeMillis() + 10000L);
        
        // Start vote via protocol (reuses broadcastVote)
        protocol.startVote(1, proposal, expectedVoters);
        manager.processMessages();
        
        // Simulate responses from expected voters
        Map<String, Object> response2 = new HashMap<>();
        response2.put("proposalId", "vote-consensus-1");
        response2.put("choice", "LINE");
        protocol.recordResponse(2, response2);
        
        Map<String, Object> response3 = new HashMap<>();
        response3.put("proposalId", "vote-consensus-1");
        response3.put("choice", "V_SHAPE");
        protocol.recordResponse(3, response3);
        
        VotingProtocol.VoteResult result = protocol.getVoteResult("vote-consensus-1");
        assert result != null : "Vote result should not be null";
        assert result.complete : "Vote should be complete when all expected voters respond";
        assert !result.expired : "Vote should not be expired before deadline";
        assert result.responses.size() == 2 : "Should have responses from two voters";
        
        // Test duplicate proposalId prevention
        try {
            protocol.startVote(1, proposal, expectedVoters);
            assert false : "Should throw exception for duplicate proposalId";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("already in progress") : "Exception should mention duplicate proposalId";
        }
        
        // Test empty expectedVoters prevention
        try {
            protocol.startVote(1, proposal, new HashSet<>());
            assert false : "Should throw exception for empty expectedVoters";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("must not be null or empty") : "Exception should mention empty expectedVoters";
        }
        
        // Test null expectedVoters prevention
        try {
            protocol.startVote(1, proposal, null);
            assert false : "Should throw exception for null expectedVoters";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("must not be null or empty") : "Exception should mention null expectedVoters";
        }
        
        // Test unauthorized voter prevention
        Map<String, Object> unauthorizedResponse = new HashMap<>();
        unauthorizedResponse.put("proposalId", "vote-consensus-1");
        unauthorizedResponse.put("choice", "LINE");
        try {
            protocol.recordResponse(99, unauthorizedResponse); // Agent 99 not in expectedVoters
            assert false : "Should throw exception for unauthorized voter";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("not in the expected voters") : "Exception should mention unauthorized voter";
        }
        
        System.out.println("✓ Consensus support tests passed");
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
    
    // ===== WEEK 9–10: FAULT TOLERANCE =====
    private static void testFaultTolerance() {
        System.out.println("\n--- Week 9–10: Fault Tolerance ---");
        
        CommunicationManager manager = new CommunicationManager();
        List<AgentState> agents = createTestAgents();
        manager.updateTopology(agents);
        
        FailureRecoveryPolicy recoveryPolicy = new FailureRecoveryPolicy(manager, 3);
        
        // Create a critical message (e.g., EMERGENCY_ALERT) between two agents
        Map<String, Object> payload = new HashMap<>();
        payload.put("alertType", "CRITICAL");
        payload.put("description", "Test emergency");
        Message message = new Message(MessageType.EMERGENCY_ALERT, payload);
        
        OutgoingMessage outgoing = new OutgoingMessage(1, 2, message);
        
        boolean sent = recoveryPolicy.sendCritical(outgoing);
        assert sent : "Critical message should be accepted by the queue with retries";
        
        manager.processMessages();
        
        // Validate that at least one message was delivered (using history)
        List<IncomingMessage> history = manager.getMessageHistory();
        boolean found = history.stream().anyMatch(m ->
            m.originalSenderId == 1 &&
            m.receiverId == 2 &&
            m.messageContent.type == MessageType.EMERGENCY_ALERT
        );
        
        assert found : "Critical emergency alert should appear in message history";
        
        // Validate partition helper returns a non-empty partition for an existing agent
        Set<Integer> partition = manager.getPartitionForAgent(1);
        assert partition != null && partition.size() > 0 : "Partition for agent 1 should exist and be non-empty";
        
        System.out.println("✓ Fault tolerance tests passed");
    }
    
    // ===== WEEK 11–12: PERFORMANCE METRICS =====
    private static void testPerformanceMetrics() {
        System.out.println("\n--- Week 11–12: Performance Metrics ---");
        
        CommunicationManager manager = new CommunicationManager();
        List<AgentState> agents = createTestAgents();
        manager.updateTopology(agents);
        
        // Create metrics aggregator
        CommunicationMetrics metrics = new CommunicationMetrics(manager);
        
        // Send some test messages to generate history
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("test", "message1");
        Message msg1 = new Message(MessageType.POSITION_UPDATE, payload1);
        OutgoingMessage outgoing1 = new OutgoingMessage(1, 2, msg1);
        manager.sendMessage(outgoing1);
        
        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("test", "message2");
        Message msg2 = new Message(MessageType.STATUS_UPDATE, payload2);
        OutgoingMessage outgoing2 = new OutgoingMessage(2, 3, msg2);
        manager.sendMessage(outgoing2);
        
        // Process messages
        manager.processMessages();
        
        // Get metrics snapshot
        CommunicationMetrics.CommunicationMetricsSnapshot snapshot = metrics.getSnapshot();
        
        // Validate metrics are non-negative and reasonable
        assert snapshot.messagesPerSecond >= 0 : "Messages per second should be non-negative";
        assert snapshot.averageLatency >= 0 : "Average latency should be non-negative";
        assert snapshot.failureRate >= 0 && snapshot.failureRate <= 1 : "Failure rate should be between 0 and 1";
        assert snapshot.pendingMessages >= 0 : "Pending messages should be non-negative";
        int snapshotPending = snapshot.pendingMessages;
        int managerPending = manager.getPendingMessageCount();
        assert snapshotPending == managerPending : 
            String.format("Pending message count mismatch: snapshot=%d, manager=%d", snapshotPending, managerPending);
        
        // Validate individual metric methods
        double msgsPerSec = metrics.getMessagesPerSecond();
        double avgLatency = metrics.getAverageLatency();
        int pending = metrics.getPendingMessageCount();
        
        assert msgsPerSec >= 0 : "getMessagesPerSecond should return non-negative value";
        assert avgLatency >= 0 : "getAverageLatency should return non-negative value";
        assert pending >= 0 : "getPendingMessageCount should return non-negative value";
        
        System.out.println("✓ Performance metrics tests passed");
        System.out.println("  Snapshot: " + snapshot);
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

