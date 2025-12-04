package com.team6.swarm.ui;

/**
 * Week 5-6: End-to-end integration test
 * Purpose: Test all components work together
 * Author: Anthony (UI Team)
 */
public class IntegrationTest {
    
    private boolean testsPassed = true;
    private int testsRun = 0;
    private int testsFailed = 0;
    
    /**
     * Run all integration tests
     */
    public boolean runAllTests() {
        System.out.println("=== Starting Integration Tests ===\n");
        
        testAgentSpawning();
        testMessageFlow();
        testCompleteMission();
        testEventBusIntegration();
        testVisualizationUpdate();
        
        System.out.println("\n=== Integration Test Summary ===");
        System.out.println("Tests Run: " + testsRun);
        System.out.println("Tests Passed: " + (testsRun - testsFailed));
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("Overall: " + (testsPassed ? "PASS" : "FAIL"));
        
        return testsPassed;
    }
    
    /**
     * Test 1: Agent Spawning
     */
    private void testAgentSpawning() {
        System.out.println("Test 1: Agent Spawning");
        testsRun++;
        
        try {
            // 1. Simulate user click spawn button
            UserEvent spawnEvent = UserEvent.spawnAgent(100, 100);
            assertNotNull("Spawn event created", spawnEvent);
            
            // 2. Verify UI event created
            assertEquals("Event type", UserEvent.EventType.SPAWN_AGENT, spawnEvent.getType());
            
            // 3. Verify position
            assertNotNull("Click position", spawnEvent.getClickPosition());
            
            System.out.println("  ✓ Agent spawning test passed\n");
        } catch (AssertionError e) {
            testFailed("Agent Spawning", e.getMessage());
        }
    }
    
    /**
     * Test 2: Message Flow
     */
    private void testMessageFlow() {
        System.out.println("Test 2: Message Flow");
        testsRun++;
        
        try {
            // Test event bus
            EventBusManager eventBus = new EventBusManager();
            
            final boolean[] eventReceived = {false};
            
            // Subscribe to event
            eventBus.subscribe(EventBusManager.EventType.MESSAGE_SENT, 
                event -> eventReceived[0] = true, "TestListener");
            
            // Publish event
            eventBus.publish(EventBusManager.EventType.MESSAGE_SENT, "Test message");
            
            assertTrue("Event received", eventReceived[0]);
            
            System.out.println("  ✓ Message flow test passed\n");
        } catch (AssertionError e) {
            testFailed("Message Flow", e.getMessage());
        }
    }
    
    /**
     * Test 3: Complete Mission
     */
    private void testCompleteMission() {
        System.out.println("Test 3: Complete Mission");
        testsRun++;
        
        try {
            // 1. Create mission status panel
            MissionStatusPanel missionPanel = new MissionStatusPanel();
            assertNotNull("Mission panel created", missionPanel);
            
            // 2. Update mission
            missionPanel.updateMission("Test Mission", "IN_PROGRESS", 0.5, 5, 10, 120);
            
            // 3. Complete mission
            missionPanel.updateMission("Test Mission", "COMPLETED", 1.0, 10, 10, 0);
            
            System.out.println("  ✓ Complete mission test passed\n");
        } catch (AssertionError e) {
            testFailed("Complete Mission", e.getMessage());
        }
    }
    
    /**
     * Test 4: Event Bus Integration
     */
    private void testEventBusIntegration() {
        System.out.println("Test 4: Event Bus Integration");
        testsRun++;
        
        try {
            EventBusManager eventBus = new EventBusManager();
            
            // Test multiple subscriptions
            int sub1 = eventBus.subscribe(EventBusManager.EventType.AGENT_STATE_UPDATE, 
                e -> {}, "Component1");
            int sub2 = eventBus.subscribe(EventBusManager.EventType.AGENT_STATE_UPDATE, 
                e -> {}, "Component2");
            
            assertEquals("Subscription count", 2, 
                eventBus.getSubscriptionCount(EventBusManager.EventType.AGENT_STATE_UPDATE));
            
            // Test unsubscribe
            eventBus.unsubscribe(sub1);
            assertEquals("After unsubscribe", 1, 
                eventBus.getSubscriptionCount(EventBusManager.EventType.AGENT_STATE_UPDATE));
            
            System.out.println("  ✓ Event bus integration test passed\n");
        } catch (AssertionError e) {
            testFailed("Event Bus Integration", e.getMessage());
        }
    }
    
    /**
     * Test 5: Visualization Update
     */
    private void testVisualizationUpdate() {
        System.out.println("Test 5: Visualization Update");
        testsRun++;
        
        try {
            // Test camera controller
            CameraController camera = new CameraController();
            camera.pan(10, 10);
            assertEquals("Camera offset X", 10.0, camera.getOffsetX());
            assertEquals("Camera offset Y", 10.0, camera.getOffsetY());
            
            camera.reset();
            assertEquals("After reset X", 0.0, camera.getOffsetX());
            assertEquals("After reset Y", 0.0, camera.getOffsetY());
            
            System.out.println("  ✓ Visualization update test passed\n");
        } catch (AssertionError e) {
            testFailed("Visualization Update", e.getMessage());
        }
    }
    
    // Helper methods
    
    private void assertNotNull(String message, Object obj) {
        if (obj == null) {
            throw new AssertionError(message + ": Expected non-null");
        }
    }
    
    private void assertEquals(String message, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": Expected " + expected + " but got " + actual);
        }
    }
    
    private void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message + ": Expected true");
        }
    }
    
    private void testFailed(String testName, String reason) {
        System.out.println("  ✗ " + testName + " FAILED: " + reason + "\n");
        testsPassed = false;
        testsFailed++;
    }
    
    /**
     * Main entry point for running tests
     */
    public static void main(String[] args) {
        IntegrationTest test = new IntegrationTest();
        boolean success = test.runAllTests();
        System.exit(success ? 0 : 1);
    }
}
