/**
 * COMMUNICATIONSYSTEMTEST CLASS - Comprehensive Unit Tests
 *
 * PURPOSE:
 * - Comprehensive unit tests for all communication system components
 * - Validates functionality, edge cases, and integration
 * - Ensures system reliability and correctness
 *
 * TEST COVERAGE:
 * 1. Message System (Message, MessageType, OutgoingMessage, IncomingMessage)
 * 2. Network Topology (NeighborAgent, NeighborInformation)
 * 3. Network Simulation (NetworkSimulator)
 * 4. Communication Management (CommunicationManager)
 * 5. Message Routing (MessageRouter, MessageQueue)
 * 6. Connection Management (ConnectionInfo)
 *
 * TEST SCENARIOS:
 * - Basic functionality tests
 * - Edge case handling
 * - Error condition testing
 * - Performance validation
 * - Integration testing
 *
 * EXPECTED OUTPUTS:
 * - All tests pass
 * - No exceptions or errors
 * - Proper error handling
 * - Performance within acceptable limits
 */
package com.team6.swarm.communication;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.Point2D;
import com.team6.swarm.core.Vector2D;
import com.team6.swarm.core.AgentStatus;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CommunicationSystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== Communication System Comprehensive Tests ===");
        
        try {
            // Test 1: Message System
            testMessageSystem();
            
            // Test 2: Network Topology
            testNetworkTopology();
            
            // Test 3: Network Simulation
            testNetworkSimulation();
            
            // Test 4: Communication Management
            testCommunicationManagement();
            
            // Test 5: Message Routing
            testMessageRouting();
            
            // Test 6: Connection Management
            testConnectionManagement();
            
            // Test 7: Integration Tests
            testIntegration();
            
            // Test 8: Performance Tests
            testPerformance();
            
            System.out.println("\n=== All Tests Passed Successfully ===");
            
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    // ===== TEST 1: MESSAGE SYSTEM =====
    private static void testMessageSystem() {
        System.out.println("\n--- Test 1: Message System ---");
        
        // Test MessageType enum
        assert MessageType.values().length == 8 : "Should have 8 message types";
        assert MessageType.EMERGENCY_ALERT != null : "Emergency alert should exist";
        
        // Test Message creation
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "data");
        Message message = new Message(MessageType.POSITION_UPDATE, payload);
        
        assert message.messageId != null : "Message ID should not be null";
        assert message.type == MessageType.POSITION_UPDATE : "Message type should match";
        assert message.payload.equals(payload) : "Payload should match";
        assert message.timestamp > 0 : "Timestamp should be positive";
        assert !message.isExpired() : "New message should not be expired";
        
        // Test OutgoingMessage
        OutgoingMessage outgoing = new OutgoingMessage(1, 2, message);
        assert outgoing.senderId == 1 : "Sender ID should match";
        assert outgoing.receiverId == 2 : "Receiver ID should match";
        assert outgoing.messageContent == message : "Message content should match";
        assert !outgoing.isBroadcast() : "Should not be broadcast";
        assert outgoing.allowsMultiHop() : "Should allow multi-hop";
        
        // Test IncomingMessage
        List<Integer> routePath = Arrays.asList(3, 4);
        IncomingMessage incoming = new IncomingMessage(2, 1, message, routePath, 0.8);
        assert incoming.receiverId == 2 : "Receiver ID should match";
        assert incoming.originalSenderId == 1 : "Original sender ID should match";
        assert incoming.getHopCount() == 2 : "Hop count should be 2";
        assert incoming.isReliable() : "Should be reliable";
        // Test basic delivery functionality
        assert incoming.getHopCount() > 0 : "Should have taken some hops";
        
        System.out.println("✓ Message system tests passed");
    }
    
    // ===== TEST 2: NETWORK TOPOLOGY =====
    private static void testNetworkTopology() {
        System.out.println("\n--- Test 2: Network Topology ---");
        
        // Test NeighborAgent
        NeighborAgent neighbor = new NeighborAgent(5, 45.2, 0.8, true, System.currentTimeMillis());
        assert neighbor.neighborId == 5 : "Neighbor ID should match";
        assert neighbor.distance == 45.2 : "Distance should match";
        assert neighbor.signalStrength == 0.8 : "Signal strength should match";
        assert neighbor.canCommunicate : "Should be able to communicate";
        assert neighbor.isReliable() : "Should be reliable";
        assert neighbor.getConnectionStatus().equals("GOOD") : "Connection status should be GOOD";
        
        // Test NeighborInformation
        List<NeighborAgent> neighbors = Arrays.asList(
            new NeighborAgent(2, 30.0, 0.9, true, System.currentTimeMillis()),
            new NeighborAgent(3, 50.0, 0.7, true, System.currentTimeMillis()),
            new NeighborAgent(4, 80.0, 0.4, true, System.currentTimeMillis())
        );
        
        NeighborInformation neighborInfo = new NeighborInformation(1, neighbors);
        assert neighborInfo.agentId == 1 : "Agent ID should match";
        assert neighborInfo.neighborCount == 3 : "Should have 3 neighbors";
        assert neighborInfo.getCommunicatingNeighbors().size() == 3 : "All should be communicating";
        assert neighborInfo.getReliableNeighbors().size() == 2 : "Should have 2 reliable neighbors";
        assert neighborInfo.isWellConnected() : "Should be well connected";
        assert neighborInfo.getReliableNeighbors().size() > 0 : "Should have reliable neighbors";
        
        System.out.println("✓ Network topology tests passed");
    }
    
    // ===== TEST 3: NETWORK SIMULATION =====
    private static void testNetworkSimulation() {
        System.out.println("\n--- Test 3: Network Simulation ---");
        
        // Test NetworkSimulator
        NetworkSimulator simulator = new NetworkSimulator(100.0, 0.05, 0.1, 150.0, 50.0);
        assert simulator.getCommunicationRange() == 100.0 : "Communication range should match";
        assert simulator.getFailureRate() == 0.05 : "Failure rate should match";
        assert simulator.getInterferenceLevel() == 0.1 : "Interference level should match";
        
        // Test communication range
        assert simulator.canCommunicate(50.0, 100.0) : "Should be able to communicate within range";
        assert !simulator.canCommunicate(150.0, 100.0) : "Should not communicate beyond range";
        
        // Test signal strength calculation
        double signal = simulator.calculateSignalStrength(50.0, 100.0);
        assert signal > 0.0 && signal <= 1.0 : "Signal strength should be in valid range";
        assert signal > 0.4 : "Signal strength should be reasonable for 50% range";
        
        // Test delivery simulation
        NetworkSimulator.DeliveryResult result = simulator.simulateDelivery(50.0, 100.0);
        assert result.signalStrength > 0.0 : "Signal strength should be positive";
        assert result.delay > 0 : "Delay should be positive";
        
        System.out.println("✓ Network simulation tests passed");
    }
    
    // ===== TEST 4: COMMUNICATION MANAGEMENT =====
    private static void testCommunicationManagement() {
        System.out.println("\n--- Test 4: Communication Management ---");
        
        // Test CommunicationManager
        CommunicationManager manager = new CommunicationManager();
        assert manager.getPendingMessageCount() == 0 : "Should start with no pending messages";
        
        // Test message sending
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "data");
        Message message = new Message(MessageType.POSITION_UPDATE, payload);
        OutgoingMessage outgoing = new OutgoingMessage(1, 2, message);
        
        boolean sent = manager.sendMessage(outgoing);
        assert sent : "Message should be sent successfully";
        assert manager.getPendingMessageCount() == 1 : "Should have 1 pending message";
        
        // Test topology update
        List<AgentState> agents = createTestAgents();
        manager.updateTopology(agents);
        
        NeighborInformation neighbors = manager.getNeighbors(1);
        assert neighbors != null : "Should have neighbor information";
        assert neighbors.agentId == 1 : "Agent ID should match";
        
        // Test network statistics
        CommunicationManager.NetworkStatistics stats = manager.getNetworkStatistics();
        assert stats.totalAgents > 0 : "Should have agents";
        assert stats.totalConnections >= 0 : "Should have non-negative connections";
        
        System.out.println("✓ Communication management tests passed");
    }
    
    // ===== TEST 5: MESSAGE ROUTING =====
    private static void testMessageRouting() {
        System.out.println("\n--- Test 5: Message Routing ---");
        
        // Test MessageRouter
        Map<Integer, NeighborInformation> topology = createTestTopology();
        MessageRouter router = new MessageRouter(topology, 5, 30000);
        
        // Test path finding
        List<Integer> path = router.findPath(1, 3);
        assert path != null : "Should find a path";
        assert path.isEmpty() || path.size() <= 5 : "Path should not exceed max hops";
        
        // Test reachable agents
        Set<Integer> reachable = router.findReachableAgents(1);
        assert reachable.contains(1) : "Should include sender";
        assert reachable.size() > 1 : "Should have multiple reachable agents";
        
        // Test MessageQueue
        MessageQueue queue = new MessageQueue();
        assert queue.isEmpty() : "Should start empty";
        assert queue.size() == 0 : "Size should be 0";
        
        // Test message queuing
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "data");
        Message message = new Message(MessageType.POSITION_UPDATE, payload);
        OutgoingMessage outgoing = new OutgoingMessage(1, 2, message);
        
        boolean enqueued = queue.enqueue(outgoing);
        assert enqueued : "Message should be enqueued";
        assert queue.size() == 1 : "Size should be 1";
        assert !queue.isEmpty() : "Should not be empty";
        
        // Test message dequeuing
        OutgoingMessage dequeued = queue.dequeue();
        assert dequeued != null : "Message should be dequeued";
        assert queue.size() == 0 : "Size should be 0 again";
        assert queue.isEmpty() : "Should be empty again";
        
        System.out.println("✓ Message routing tests passed");
    }
    
    // ===== TEST 6: CONNECTION MANAGEMENT =====
    private static void testConnectionManagement() {
        System.out.println("\n--- Test 6: Connection Management ---");
        
        // Test ConnectionInfo
        long now = System.currentTimeMillis();
        ConnectionInfo connection = new ConnectionInfo(1, 2, 0.8, true, now, now, 5, 150.0);
        
        assert connection.agentA == 1 : "Agent A should match";
        assert connection.agentB == 2 : "Agent B should match";
        assert connection.strength == 0.8 : "Strength should match";
        assert connection.isActive : "Should be active";
        assert connection.messageCount == 5 : "Message count should match";
        assert connection.averageLatency == 150.0 : "Average latency should match";
        
        // Test connection quality
        double quality = connection.getConnectionQuality();
        assert quality > 0.0 && quality <= 1.0 : "Quality should be in valid range";
        assert quality > 0.5 : "Quality should be reasonable";
        
        // Test connection status
        String status = connection.getConnectionStatus();
        assert status != null : "Status should not be null";
        assert status.equals("GOOD") : "Status should be GOOD";
        
        // Test connection updates
        ConnectionInfo updated = connection.updateMessage(100.0);
        assert updated.messageCount == 6 : "Message count should increase";
        assert updated.averageLatency < 150.0 : "Average latency should decrease";
        
        // Test connection ID
        String connectionId = connection.getConnectionId();
        assert connectionId.equals("1-2") : "Connection ID should be 1-2";
        
        System.out.println("✓ Connection management tests passed");
    }
    
    // ===== TEST 7: INTEGRATION TESTS =====
    private static void testIntegration() {
        System.out.println("\n--- Test 7: Integration Tests ---");
        
        // Test end-to-end message flow
        CommunicationManager manager = new CommunicationManager();
        List<AgentState> agents = createTestAgents();
        manager.updateTopology(agents);
        
        // Create and send message
        Map<String, Object> payload = new HashMap<>();
        payload.put("position", "test");
        Message message = new Message(MessageType.POSITION_UPDATE, payload);
        OutgoingMessage outgoing = new OutgoingMessage(1, 2, message);
        
        boolean sent = manager.sendMessage(outgoing);
        assert sent : "Message should be sent";
        
        // Process messages
        manager.processMessages();
        
        // Check message history
        List<IncomingMessage> history = manager.getMessageHistory();
        assert history.size() > 0 : "Should have message history";
        
        System.out.println("✓ Integration tests passed");
    }
    
    // ===== TEST 8: PERFORMANCE TESTS =====
    private static void testPerformance() {
        System.out.println("\n--- Test 8: Performance Tests ---");
        
        // Test message creation performance
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("test", i);
            Message message = new Message(MessageType.POSITION_UPDATE, payload);
            OutgoingMessage outgoing = new OutgoingMessage(1, 2, message);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assert duration < 1000 : "1000 messages should be created in under 1 second";
        
        // Test network simulation performance
        NetworkSimulator simulator = new NetworkSimulator();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            simulator.simulateDelivery(50.0, 100.0);
        }
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        assert duration < 500 : "1000 simulations should complete in under 500ms";
        
        System.out.println("✓ Performance tests passed");
    }
    
    // ===== HELPER METHODS =====
    
    private static List<AgentState> createTestAgents() {
        List<AgentState> agents = new ArrayList<>();
        
        // Create 5 test agents
        for (int i = 1; i <= 5; i++) {
            AgentState agent = new AgentState();
            agent.agentId = i;
            agent.agentName = "Agent_" + i;
            agent.position = new Point2D(i * 50, i * 30);
            agent.velocity = new Vector2D(10, 5);
            agent.status = AgentStatus.ACTIVE;
            agent.batteryLevel = 0.8;
            agent.maxSpeed = 50.0;
            agent.communicationRange = 100.0;
            agent.lastUpdateTime = System.currentTimeMillis();
            
            agents.add(agent);
        }
        
        return agents;
    }
    
    private static Map<Integer, NeighborInformation> createTestTopology() {
        Map<Integer, NeighborInformation> topology = new HashMap<>();
        
        // Create simple topology: 1-2-3-4-5
        List<NeighborAgent> neighbors1 = Arrays.asList(
            new NeighborAgent(2, 50.0, 0.8, true, System.currentTimeMillis())
        );
        topology.put(1, new NeighborInformation(1, neighbors1));
        
        List<NeighborAgent> neighbors2 = Arrays.asList(
            new NeighborAgent(1, 50.0, 0.8, true, System.currentTimeMillis()),
            new NeighborAgent(3, 50.0, 0.8, true, System.currentTimeMillis())
        );
        topology.put(2, new NeighborInformation(2, neighbors2));
        
        List<NeighborAgent> neighbors3 = Arrays.asList(
            new NeighborAgent(2, 50.0, 0.8, true, System.currentTimeMillis()),
            new NeighborAgent(4, 50.0, 0.8, true, System.currentTimeMillis())
        );
        topology.put(3, new NeighborInformation(3, neighbors3));
        
        List<NeighborAgent> neighbors4 = Arrays.asList(
            new NeighborAgent(3, 50.0, 0.8, true, System.currentTimeMillis()),
            new NeighborAgent(5, 50.0, 0.8, true, System.currentTimeMillis())
        );
        topology.put(4, new NeighborInformation(4, neighbors4));
        
        List<NeighborAgent> neighbors5 = Arrays.asList(
            new NeighborAgent(4, 50.0, 0.8, true, System.currentTimeMillis())
        );
        topology.put(5, new NeighborInformation(5, neighbors5));
        
        return topology;
    }
}
