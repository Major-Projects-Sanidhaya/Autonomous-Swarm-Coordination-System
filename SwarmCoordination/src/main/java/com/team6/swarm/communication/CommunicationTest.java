/**
 * COMMUNICATIONTEST CLASS - Basic Message System Validation
 *
 * PURPOSE:
 * - Test the fundamental message system components
 * - Verify message creation, wrapping, and delivery simulation
 * - Validate that all data is preserved correctly through the system
 *
 * TEST SCENARIOS:
 * 1. Message Creation - Test different MessageTypes and payloads
 * 2. OutgoingMessage Wrapping - Test routing information attachment
 * 3. IncomingMessage Delivery - Test delivery receipt creation
 * 4. Data Preservation - Verify all data survives the round trip
 * 5. Priority Handling - Test message priority assignment
 * 6. Expiration Logic - Test message expiration and TTL
 *
 * EXPECTED OUTPUTS:
 * - All message types created successfully
 * - Routing information attached correctly
 * - Delivery details calculated properly
 * - Data integrity maintained throughout
 * - Priority and expiration logic working
 *
 * VALIDATION POINTS:
 * - Message IDs are unique
 * - Timestamps are reasonable
 * - Payload data is preserved
 * - Metadata is accessible
 * - Priority levels are respected
 * - Expiration times are calculated correctly
 */
package com.team6.swarm.communication;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class CommunicationTest {
    
    public static void main(String[] args) {
        System.out.println("=== Communication System Test ===");
        
        // Test 1: Message Creation
        testMessageCreation();
        
        // Test 2: OutgoingMessage Wrapping
        testOutgoingMessageWrapping();
        
        // Test 3: IncomingMessage Delivery
        testIncomingMessageDelivery();
        
        // Test 4: Data Preservation
        testDataPreservation();
        
        // Test 5: Priority and Expiration
        testPriorityAndExpiration();
        
        System.out.println("\n=== All Tests Completed Successfully ===");
    }
    
    private static void testMessageCreation() {
        System.out.println("\n--- Test 1: Message Creation ---");
        
        // Test different message types
        for (MessageType type : MessageType.values()) {
            Map<String, Object> payload = createTestPayload(type);
            Message message = new Message(type, payload);
            
            System.out.println("Created " + type + " message: " + message.messageId);
            assert message.messageId != null : "Message ID should not be null";
            assert message.type == type : "Message type should match";
            assert message.payload != null : "Payload should not be null";
            assert message.timestamp > 0 : "Timestamp should be positive";
        }
        
        System.out.println("✓ All message types created successfully");
    }
    
    private static void testOutgoingMessageWrapping() {
        System.out.println("\n--- Test 2: OutgoingMessage Wrapping ---");
        
        // Create a test message
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "data");
        Message message = new Message(MessageType.POSITION_UPDATE, payload);
        
        // Test direct message
        OutgoingMessage direct = new OutgoingMessage(1, 2, message);
        System.out.println("Direct message: " + direct);
        assert direct.senderId == 1 : "Sender ID should be 1";
        assert direct.receiverId == 2 : "Receiver ID should be 2";
        assert direct.messageContent == message : "Message content should match";
        assert !direct.isBroadcast() : "Should not be broadcast";
        
        // Test broadcast message
        OutgoingMessage broadcast = new OutgoingMessage(1, -1, message);
        System.out.println("Broadcast message: " + broadcast);
        assert broadcast.isBroadcast() : "Should be broadcast";
        
        // Test multi-hop message
        OutgoingMessage multiHop = new OutgoingMessage(1, 5, message, 2, 3, 60000);
        System.out.println("Multi-hop message: " + multiHop);
        assert multiHop.maxHops == 3 : "Max hops should be 3";
        assert multiHop.allowsMultiHop() : "Should allow multi-hop";
        
        System.out.println("✓ OutgoingMessage wrapping successful");
    }
    
    private static void testIncomingMessageDelivery() {
        System.out.println("\n--- Test 3: IncomingMessage Delivery ---");
        
        // Create test message
        Map<String, Object> payload = new HashMap<>();
        payload.put("position", "test");
        Message message = new Message(MessageType.POSITION_UPDATE, payload);
        
        // Test direct delivery
        List<Integer> emptyPath = new ArrayList<>();
        IncomingMessage direct = new IncomingMessage(2, 1, message, emptyPath, 0.9);
        System.out.println("Direct delivery: " + direct);
        assert direct.isDirectDelivery() : "Should be direct delivery";
        assert direct.getHopCount() == 0 : "Hop count should be 0";
        assert direct.isReliable() : "Should be reliable";
        
        // Test multi-hop delivery
        List<Integer> routePath = new ArrayList<>();
        routePath.add(3);
        routePath.add(4);
        IncomingMessage multiHop = new IncomingMessage(5, 1, message, routePath, 0.6);
        System.out.println("Multi-hop delivery: " + multiHop);
        assert !multiHop.isDirectDelivery() : "Should not be direct delivery";
        assert multiHop.getHopCount() == 2 : "Hop count should be 2";
        assert multiHop.wasRelayedBy(3) : "Should be relayed by agent 3";
        assert multiHop.getFirstRelay() == 3 : "First relay should be 3";
        assert multiHop.getLastRelay() == 4 : "Last relay should be 4";
        
        System.out.println("✓ IncomingMessage delivery successful");
    }
    
    private static void testDataPreservation() {
        System.out.println("\n--- Test 4: Data Preservation ---");
        
        // Create complex payload
        Map<String, Object> originalPayload = new HashMap<>();
        originalPayload.put("position", "test");
        originalPayload.put("velocity", 42.0);
        originalPayload.put("battery", 0.85);
        
        // Create message with metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("priority", 2);
        metadata.put("ttl", 30000);
        metadata.put("source", "test");
        
        Message originalMessage = new Message(MessageType.STATUS_UPDATE, originalPayload, metadata);
        
        // Wrap in OutgoingMessage
        OutgoingMessage outgoing = new OutgoingMessage(1, 2, originalMessage);
        
        // Simulate delivery as IncomingMessage
        List<Integer> routePath = new ArrayList<>();
        routePath.add(3);
        IncomingMessage incoming = new IncomingMessage(2, 1, originalMessage, routePath, 0.8);
        
        // Verify data preservation
        Message deliveredMessage = incoming.messageContent;
        assert deliveredMessage.messageId.equals(originalMessage.messageId) : "Message ID should be preserved";
        assert deliveredMessage.type == originalMessage.type : "Message type should be preserved";
        assert deliveredMessage.payload.equals(originalPayload) : "Payload should be preserved";
        assert deliveredMessage.timestamp == originalMessage.timestamp : "Timestamp should be preserved";
        
        // Verify metadata preservation
        assert deliveredMessage.getMetadata("priority", Integer.class) == 2 : "Priority should be preserved";
        assert deliveredMessage.getMetadata("ttl", Integer.class) == 30000 : "TTL should be preserved";
        assert deliveredMessage.getMetadata("source", String.class).equals("test") : "Source should be preserved";
        
        System.out.println("✓ Data preservation successful");
    }
    
    private static void testPriorityAndExpiration() {
        System.out.println("\n--- Test 5: Priority and Expiration ---");
        
        // Test message priority
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "priority");
        Message message = new Message(MessageType.EMERGENCY_ALERT, payload);
        
        // Test priority assignment
        assert message.getPriority() == 3 : "Default priority should be 3";
        
        // Test expiration logic
        assert !message.isExpired() : "New message should not be expired";
        
        // Test OutgoingMessage expiration
        OutgoingMessage shortTTL = new OutgoingMessage(1, 2, message, 1, 0, 1000);
        assert !shortTTL.isExpired() : "New outgoing message should not be expired";
        assert shortTTL.getRemainingTime() > 0 : "Should have remaining time";
        
        // Test relay creation
        OutgoingMessage relay = shortTTL.createRelay(3);
        assert relay.senderId == 3 : "Relay sender should be 3";
        assert relay.maxHops == -1 : "Relay should have reduced hops";
        
        System.out.println("✓ Priority and expiration logic successful");
    }
    
    private static Map<String, Object> createTestPayload(MessageType type) {
        Map<String, Object> payload = new HashMap<>();
        
        switch (type) {
            case POSITION_UPDATE:
                payload.put("position", "test_position");
                payload.put("velocity", "test_velocity");
                break;
            case VOTE_PROPOSAL:
                payload.put("proposal", "test_proposal");
                payload.put("options", "test_options");
                break;
            case TASK_ASSIGNMENT:
                payload.put("taskId", "test_task");
                payload.put("taskType", "test_type");
                break;
            case EMERGENCY_ALERT:
                payload.put("alertType", "test_alert");
                payload.put("description", "test_description");
                break;
            default:
                payload.put("data", "test_data");
                break;
        }
        
        return payload;
    }
}
