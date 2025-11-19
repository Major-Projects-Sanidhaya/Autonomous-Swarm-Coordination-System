package com.team6.swarm.core;

import java.util.*;

/**
 * Week 8 Implementation: Integration Test
 *
 * Comprehensive integration tests for all Week 8 components including
 * security testing, fault tolerance, state recovery, and system validation.
 *
 * Tests:
 * - SecurityManager authentication and encryption
 * - IntrusionDetector anomaly detection
 * - FaultTolerance failure detection and recovery
 * - StateRecoveryManager snapshot and restore
 * - SwarmAnalytics behavioral analysis
 * - MetricsCollector data collection
 * - SystemConfiguration management
 * - SystemValidator validation
 * - Stress testing and final integration
 *
 * @author Team 6
 * @version Week 8
 */
public class Week8IntegrationTest {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Week 8 Integration Test - Autonomous Swarm Coordination System");
        System.out.println("=".repeat(80));
        System.out.println();

        Week8IntegrationTest test = new Week8IntegrationTest();

        try {
            test.runAllTests();
            System.out.println("\n" + "=".repeat(80));
            System.out.println("ALL WEEK 8 TESTS COMPLETED SUCCESSFULLY");
            System.out.println("=".repeat(80));
        } catch (Exception e) {
            System.err.println("\n" + "=".repeat(80));
            System.err.println("TESTS FAILED: " + e.getMessage());
            System.err.println("=".repeat(80));
            e.printStackTrace();
        }
    }

    public void runAllTests() throws Exception {
        testSecurityManager();
        testIntrusionDetector();
        testFaultTolerance();
        testStateRecoveryManager();
        testMetricsCollector();
        testSystemConfiguration();
        testSystemValidator();
        testIntegrationWithAllComponents();
        performStressTesting();
    }

    public void testSecurityManager() {
        System.out.println("[TEST] SecurityManager");
        System.out.println("-".repeat(80));

        SecurityManager securityManager = new SecurityManager();

        // Test agent registration
        boolean registered = securityManager.registerAgent("agent1", "password123");
        assert registered : "Agent registration failed";
        System.out.println("  ✓ Agent registration successful");

        // Test authentication
        String token = securityManager.authenticateAgent("agent1", "password123");
        assert token != null : "Authentication failed";
        System.out.println("  ✓ Authentication successful: " + token.substring(0, 20) + "...");

        // Test token validation
        boolean validToken = securityManager.validateToken("agent1", token);
        assert validToken : "Token validation failed";
        System.out.println("  ✓ Token validation successful");

        // Test encryption/decryption
        String message = "Hello, secure world!";
        String encrypted = securityManager.encryptMessage("agent1", message);
        String decrypted = securityManager.decryptMessage("agent1", encrypted);
        assert message.equals(decrypted) : "Encryption/decryption failed";
        System.out.println("  ✓ Message encryption/decryption successful");

        // Test permissions
        securityManager.grantPermission("agent1", SecurityManager.Permission.ADMIN);
        boolean hasPermission = securityManager.hasPermission("agent1", SecurityManager.Permission.ADMIN);
        assert hasPermission : "Permission grant failed";
        System.out.println("  ✓ Permission management successful");

        // Test metrics
        SecurityManager.SecurityMetrics metrics = securityManager.getMetrics();
        System.out.println("  ✓ Security metrics: " + metrics);

        System.out.println("  ✓ SecurityManager test passed\n");
    }

    public void testIntrusionDetector() throws Exception {
        System.out.println("[TEST] IntrusionDetector");
        System.out.println("-".repeat(80));

        IntrusionDetector detector = new IntrusionDetector();

        // Test activity recording
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");

        for (int i = 0; i < 10; i++) {
            detector.recordActivity("agent1", IntrusionDetector.ActivityType.MESSAGE_SENT, metadata);
        }
        System.out.println("  ✓ Activity recording successful");

        // Test failed authentication recording
        detector.recordFailedAuthentication("agent2");
        detector.recordFailedAuthentication("agent2");
        detector.recordFailedAuthentication("agent2");
        System.out.println("  ✓ Failed authentication tracking successful");

        // Test behavior analysis
        List<IntrusionDetector.BehaviorAnomaly> anomalies = detector.analyzeBehavior("agent1");
        System.out.println("  ✓ Behavior analysis: " + anomalies.size() + " anomalies detected");

        // Test threat detection
        detector.startMonitoring();
        Thread.sleep(1000);
        detector.stopMonitoring();

        List<IntrusionDetector.SecurityThreat> threats = detector.getDetectedThreats();
        System.out.println("  ✓ Threat detection: " + threats.size() + " threats detected");

        // Test metrics
        IntrusionDetector.DetectionMetrics metrics = detector.getMetrics();
        System.out.println("  ✓ Detection metrics: " + metrics);

        System.out.println("  ✓ IntrusionDetector test passed\n");
    }

    public void testFaultTolerance() throws Exception {
        System.out.println("[TEST] FaultTolerance");
        System.out.println("-".repeat(80));

        FaultTolerance faultTolerance = new FaultTolerance();

        // Test agent registration
        faultTolerance.registerAgent("agent1");
        faultTolerance.registerAgent("agent2");
        System.out.println("  ✓ Agent registration successful");

        // Test heartbeat recording
        faultTolerance.recordHeartbeat("agent1");
        FaultTolerance.AgentStatus status = faultTolerance.getAgentStatus("agent1");
        assert status == FaultTolerance.AgentStatus.ACTIVE : "Agent should be active";
        System.out.println("  ✓ Heartbeat recording successful");

        // Test failure reporting
        faultTolerance.reportFailure("agent2", "Test failure");
        status = faultTolerance.getAgentStatus("agent2");
        assert status == FaultTolerance.AgentStatus.FAILED : "Agent should be failed";
        System.out.println("  ✓ Failure reporting successful");

        // Test restart
        boolean restarted = faultTolerance.restartAgent("agent2");
        assert restarted : "Agent restart failed";
        System.out.println("  ✓ Agent restart initiated");

        // Test failure history
        List<FaultTolerance.FailureEvent> history = faultTolerance.getFailureHistory(10);
        System.out.println("  ✓ Failure history: " + history.size() + " events");

        // Test metrics
        FaultTolerance.FaultToleranceMetrics metrics = faultTolerance.getMetrics();
        System.out.println("  ✓ Fault tolerance metrics: " + metrics);

        faultTolerance.stopMonitoring();
        System.out.println("  ✓ FaultTolerance test passed\n");
    }

    public void testStateRecoveryManager() {
        System.out.println("[TEST] StateRecoveryManager");
        System.out.println("-".repeat(80));

        StateRecoveryManager recoveryManager = new StateRecoveryManager();

        // Create test agent state
        Point2D position = new Point2D(100, 200);
        AgentState state = new AgentState(position);

        // Test snapshot creation
        String snapshotId = recoveryManager.createSnapshot("agent1", state);
        assert snapshotId != null : "Snapshot creation failed";
        System.out.println("  ✓ Snapshot created: " + snapshotId);

        // Test state restoration
        AgentState restored = recoveryManager.restoreLatest("agent1");
        assert restored != null : "State restoration failed";
        assert restored.getPosition().equals(position) : "Restored state doesn't match";
        System.out.println("  ✓ State restoration successful");

        // Create multiple snapshots
        for (int i = 0; i < 5; i++) {
            Point2D newPos = new Point2D(100 + i * 10, 200 + i * 10);
            AgentState newState = new AgentState(newPos);
            recoveryManager.createSnapshot("agent1", newState);
        }

        // Test snapshot count
        int count = recoveryManager.getSnapshotCount("agent1");
        assert count == 6 : "Snapshot count incorrect";
        System.out.println("  ✓ Multiple snapshots created: " + count);

        // Test snapshot IDs
        List<String> snapshotIds = recoveryManager.getSnapshotIds("agent1");
        System.out.println("  ✓ Snapshot IDs retrieved: " + snapshotIds.size());

        // Test metrics
        StateRecoveryManager.RecoveryMetrics metrics = recoveryManager.getMetrics();
        System.out.println("  ✓ Recovery metrics: " + metrics);

        System.out.println("  ✓ StateRecoveryManager test passed\n");
    }

    public void testMetricsCollector() throws Exception {
        System.out.println("[TEST] MetricsCollector");
        System.out.println("-".repeat(80));

        MetricsCollector collector = new MetricsCollector();

        // Test metric recording
        for (int i = 0; i < 100; i++) {
            collector.recordMetric("test.metric", Math.random() * 100);
        }
        System.out.println("  ✓ Metric recording successful");

        // Test statistics
        MetricsCollector.MetricStatistics stats = collector.getStatistics("test.metric");
        assert stats != null : "Statistics retrieval failed";
        System.out.println("  ✓ Statistics: " + stats);

        // Test recent values
        List<MetricsCollector.DataPoint> recentDataPoints = collector.getRecentDataPoints("test.metric", 10);
        List<Double> recent = new ArrayList<>();
        for (MetricsCollector.DataPoint dp : recentDataPoints) {
            recent.add(dp.getValue());
        }
        assert recent.size() == 10 : "Recent values count incorrect";
        System.out.println("  ✓ Recent values retrieved: " + recent.size());

        // Test metric names
        Set<String> names = collector.getMetricNames();
        assert names.contains("test.metric") : "Metric name not found";
        System.out.println("  ✓ Metric names: " + names);

        // Test batch recording
        Map<String, Double> batchMetrics = new HashMap<>();
        batchMetrics.put("cpu.usage", 50.0);
        batchMetrics.put("memory.usage", 70.0);
        batchMetrics.put("thread.count", 10.0);
        collector.recordMetrics(batchMetrics);
        System.out.println("  ✓ Batch metrics recorded");

        // Test metrics
        MetricsCollector.CollectorMetrics metrics = collector.getCollectorMetrics();
        System.out.println("  ✓ Collector metrics: " + metrics);

        collector.stopCollection();
        System.out.println("  ✓ MetricsCollector test passed\n");
    }

    public void testSystemConfiguration() {
        System.out.println("[TEST] SystemConfiguration");
        System.out.println("-".repeat(80));

        SystemConfiguration config = new SystemConfiguration();

        // Test parameter retrieval
        int maxAgents = config.getInt("maxAgents");
        assert maxAgents == 1000 : "Default maxAgents incorrect";
        System.out.println("  ✓ Default parameter: maxAgents = " + maxAgents);

        // Test parameter setting
        config.setParameter("testParam", 42);
        int testParam = config.getInt("testParam");
        assert testParam == 42 : "Parameter setting failed";
        System.out.println("  ✓ Parameter setting successful");

        // Test different types
        config.setParameter("doubleParam", 3.14);
        config.setParameter("boolParam", true);
        config.setParameter("stringParam", "test");

        assert config.getDouble("doubleParam") == 3.14 : "Double parameter failed";
        assert config.getBoolean("boolParam") : "Boolean parameter failed";
        assert config.getString("stringParam").equals("test") : "String parameter failed";
        System.out.println("  ✓ Multiple parameter types supported");

        // Test parameter existence
        assert config.hasParameter("maxAgents") : "Parameter existence check failed";
        System.out.println("  ✓ Parameter existence check successful");

        // Test parameter names
        Set<String> names = config.getParameterNames();
        System.out.println("  ✓ Parameter names: " + names.size() + " parameters");

        // Test locking
        config.lock();
        try {
            config.setParameter("lockedParam", 100);
            assert false : "Should have thrown exception when locked";
        } catch (IllegalStateException e) {
            System.out.println("  ✓ Configuration locking works");
        }
        config.unlock();

        System.out.println("  ✓ SystemConfiguration test passed\n");
    }

    public void testSystemValidator() {
        System.out.println("[TEST] SystemValidator");
        System.out.println("-".repeat(80));

        SystemValidator validator = new SystemValidator();
        SystemConfiguration config = new SystemConfiguration();

        // Test system validation
        SystemValidator.ValidationResult result = validator.validateSystem(config);
        System.out.println("  ✓ System validation: " + result);
        System.out.println("    - Valid: " + result.valid);
        System.out.println("    - Errors: " + result.errors.size());
        System.out.println("    - Warnings: " + result.warnings.size());

        // Test configuration validation
        SystemValidator.ValidationResult configResult = validator.validateConfiguration(config);
        System.out.println("  ✓ Configuration validation: " + configResult.valid);

        // Test memory validation
        SystemValidator.ValidationResult memResult = validator.validateMemory();
        System.out.println("  ✓ Memory validation: " + memResult.valid);

        // Test thread pool validation
        SystemValidator.ValidationResult threadResult = validator.validateThreadPool();
        System.out.println("  ✓ Thread pool validation: " + threadResult.valid);

        // Test performance baseline
        SystemValidator.ValidationResult perfResult = validator.validatePerformanceBaseline();
        System.out.println("  ✓ Performance baseline: " + perfResult.valid);

        // Test metrics
        SystemValidator.ValidationMetrics metrics = validator.getMetrics();
        System.out.println("  ✓ Validation metrics: " + metrics);

        System.out.println("  ✓ SystemValidator test passed\n");
    }

    public void testIntegrationWithAllComponents() throws Exception {
        System.out.println("[TEST] Integration - All Week 8 Components");
        System.out.println("-".repeat(80));

        // Initialize all components
        SecurityManager security = new SecurityManager();
        IntrusionDetector intrusion = new IntrusionDetector();
        FaultTolerance faultTolerance = new FaultTolerance();
        StateRecoveryManager recovery = new StateRecoveryManager();
        MetricsCollector metrics = new MetricsCollector();
        SystemConfiguration config = new SystemConfiguration();
        SystemValidator validator = new SystemValidator();

        // Create and secure agents
        int agentCount = 50;
        for (int i = 0; i < agentCount; i++) {
            String agentId = "agent" + i;
            String password = "pass" + i;

            // Register with security
            security.registerAgent(agentId, password);
            String token = security.authenticateAgent(agentId, password);

            // Register with fault tolerance
            faultTolerance.registerAgent(agentId);
            faultTolerance.recordHeartbeat(agentId);

            // Create state snapshot
            Point2D pos = new Point2D(Math.random() * 1000, Math.random() * 1000);
            AgentState state = new AgentState(pos);
            recovery.createSnapshot(agentId, state);

            // Record metrics
            metrics.recordMetric("agent.position.x", pos.getX());
            metrics.recordMetric("agent.position.y", pos.getY());

            // Record activity
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("authenticated", true);
            intrusion.recordActivity(agentId, IntrusionDetector.ActivityType.MESSAGE_SENT, metadata);
        }

        System.out.println("  ✓ Created and secured " + agentCount + " agents");

        // Validate system
        SystemValidator.ValidationResult validationResult = validator.validateSystem(config);
        System.out.println("  ✓ System validation: " + (validationResult.valid ? "PASSED" : "FAILED"));

        // Get all metrics
        System.out.println("  ✓ Security: " + security.getMetrics());
        System.out.println("  ✓ Intrusion Detection: " + intrusion.getMetrics());
        System.out.println("  ✓ Fault Tolerance: " + faultTolerance.getMetrics());
        System.out.println("  ✓ Recovery: " + recovery.getMetrics());
        System.out.println("  ✓ Metrics Collection: " + metrics.getCollectorMetrics());

        // Cleanup
        faultTolerance.stopMonitoring();
        intrusion.stopMonitoring();
        metrics.stopCollection();

        System.out.println("  ✓ Integration test passed\n");
    }

    public void performStressTesting() throws Exception {
        System.out.println("[TEST] Stress Testing");
        System.out.println("=".repeat(80));

        // Test with high load
        int stressAgentCount = 100;
        System.out.println("Testing with " + stressAgentCount + " agents...");

        long startTime = System.currentTimeMillis();

        SecurityManager security = new SecurityManager();
        StateRecoveryManager recovery = new StateRecoveryManager();

        for (int i = 0; i < stressAgentCount; i++) {
            String agentId = "stress_agent" + i;
            security.registerAgent(agentId, "password");
            security.authenticateAgent(agentId, "password");

            Point2D pos = new Point2D(Math.random() * 1000, Math.random() * 1000);
            AgentState state = new AgentState(pos);
            recovery.createSnapshot(agentId, state);
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("  ✓ Stress test completed in " + duration + "ms");
        System.out.println("  ✓ Average time per agent: " + String.format("%.2f", (double) duration / stressAgentCount) + "ms");

        System.out.println("  ✓ Stress testing passed\n");
    }
}
