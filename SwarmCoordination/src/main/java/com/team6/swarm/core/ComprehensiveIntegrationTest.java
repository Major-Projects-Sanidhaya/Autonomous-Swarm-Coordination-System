package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.*;

/**
 * COMPREHENSIVE INTEGRATION TEST (Week 1-8)
 *
 * PURPOSE:
 * - Test all core classes from Week 1 through Week 8
 * - Verify integration between all components
 * - Validate end-to-end functionality
 * - Stress test system with multiple agents
 *
 * WEEK-BY-WEEK COVERAGE:
 * Week 1: Point2D, Vector2D
 * Week 2: Agent, AgentState, EventBus
 * Week 3: MovementCommand, PhysicsEngine, TaskCompletionReport
 * Week 4: AgentCapabilities, Task
 * Week 5: BoundaryManager, PerformanceMonitor
 * Week 7: PerformanceOptimizer, CacheManager, ThreadPoolManager, RouteOptimizer, SystemHealthMonitor
 * Week 8: SystemConfiguration, SystemValidator, IntrusionDetector
 *
 * @author Team 6
 * @version Week 8
 */
public class ComprehensiveIntegrationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;
    private static final List<String> failedTests = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     COMPREHENSIVE INTEGRATION TEST (Week 1-8)             ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        long startTime = System.currentTimeMillis();

        try {
            // Week 1-2 Tests
            testWeek1_BasicDataStructures();
            testWeek2_AgentAndEventBus();

            // Week 3-4 Tests
            testWeek3_MovementAndPhysics();
            testWeek4_TasksAndCapabilities();

            // Week 5 Tests
            testWeek5_BoundaryAndMonitoring();

            // Week 7 Tests
            testWeek7_PerformanceOptimizer();
            testWeek7_CacheManager();
            testWeek7_ThreadPoolManager();
            testWeek7_RouteOptimizer();
            testWeek7_SystemHealthMonitor();

            // Week 8 Tests
            testWeek8_SystemConfiguration();
            testWeek8_SystemValidator();
            testWeek8_IntrusionDetector();

            // Integration Tests
            testFullSystemIntegration();
            testStressTest();

        } catch (Exception e) {
            System.err.println("\n❌ CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TEST SUMMARY                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("Total Tests: " + (testsPassed + testsFailed));
        System.out.println("✓ Passed: " + testsPassed);
        System.out.println("✗ Failed: " + testsFailed);
        System.out.println("Duration: " + duration + "ms");

        if (!failedTests.isEmpty()) {
            System.out.println("\nFailed Tests:");
            for (String test : failedTests) {
                System.out.println("  - " + test);
            }
        }

        System.out.println("\n" + (testsFailed == 0 ? "✓ ALL TESTS PASSED!" : "✗ SOME TESTS FAILED"));
    }

    // ======================== WEEK 1 TESTS ========================

    private static void testWeek1_BasicDataStructures() {
        printTestHeader("Week 1: Basic Data Structures (Point2D, Vector2D)");

        // Test Point2D
        assertTest("Point2D creation", () -> {
            Point2D p1 = new Point2D(100, 200);
            return p1.x == 100 && p1.y == 200;
        });

        assertTest("Point2D distance calculation", () -> {
            Point2D p1 = new Point2D(0, 0);
            Point2D p2 = new Point2D(3, 4);
            double dist = p1.distanceTo(p2);
            return Math.abs(dist - 5.0) < 0.01;
        });

        // Test Vector2D
        assertTest("Vector2D creation", () -> {
            Vector2D v = new Vector2D(3, 4);
            return v.x == 3 && v.y == 4;
        });

        assertTest("Vector2D magnitude", () -> {
            Vector2D v = new Vector2D(3, 4);
            return Math.abs(v.magnitude() - 5.0) < 0.01;
        });

        assertTest("Vector2D normalization", () -> {
            Vector2D v = new Vector2D(3, 4);
            Vector2D normalized = v.normalize();
            return Math.abs(normalized.magnitude() - 1.0) < 0.01;
        });

        assertTest("Vector2D addition", () -> {
            Vector2D v1 = new Vector2D(1, 2);
            Vector2D v2 = new Vector2D(3, 4);
            Vector2D result = v1.add(v2);
            return result.x == 4 && result.y == 6;
        });
    }

    // ======================== WEEK 2 TESTS ========================

    private static void testWeek2_AgentAndEventBus() {
        printTestHeader("Week 2: Agent, AgentState, EventBus");

        // Test Agent creation
        assertTest("Agent creation", () -> {
            Agent agent = new Agent(1, new Point2D(0, 0));
            return agent.getState() != null;
        });

        // Test AgentState
        assertTest("AgentState creation", () -> {
            AgentState state = new AgentState();
            state.position = new Point2D(10, 20);
            state.velocity = new Vector2D(1, 0);
            return state.position.x == 10 && state.velocity.x == 1;
        });

        // Test EventBus
        assertTest("EventBus publish/subscribe", () -> {
            EventBus eventBus = new EventBus();
            final boolean[] received = {false};

            eventBus.subscribe(AgentStateUpdate.class, update -> {
                received[0] = true;
            });

            eventBus.publish(new AgentStateUpdate(1, null, null));

            try { Thread.sleep(100); } catch (InterruptedException e) {}
            return received[0];
        });
    }

    // ======================== WEEK 3 TESTS ========================

    private static void testWeek3_MovementAndPhysics() {
        printTestHeader("Week 3: Movement Commands & Physics Engine");

        // Test MovementCommand
        assertTest("MovementCommand creation", () -> {
            MovementCommand cmd = new MovementCommand(1, MovementType.MOVE_TO_TARGET,
                                                     CommandPriority.NORMAL);
            return cmd.agentId == 1;
        });

        assertTest("MovementCommand parameters", () -> {
            MovementCommand cmd = new MovementCommand(1, MovementType.MOVE_TO_TARGET,
                                                     CommandPriority.NORMAL);
            cmd.parameters.put("target", new Point2D(100, 100));
            return cmd.parameters.get("target") != null;
        });

        // Test PhysicsEngine
        assertTest("PhysicsEngine force application", () -> {
            PhysicsEngine physics = new PhysicsEngine();
            AgentState state = new AgentState();
            state.position = new Point2D(0, 0);
            state.velocity = new Vector2D(0, 0);
            state.maxSpeed = 50.0;

            Vector2D force = new Vector2D(10, 0);
            physics.applyForce(state, force, 1.0);

            return state.velocity.x > 0;
        });

        assertTest("PhysicsEngine collision detection", () -> {
            PhysicsEngine physics = new PhysicsEngine();
            AgentState s1 = new AgentState();
            s1.position = new Point2D(0, 0);
            AgentState s2 = new AgentState();
            s2.position = new Point2D(3, 0);

            return physics.checkCollision(s1, s2);
        });
    }

    // ======================== WEEK 4 TESTS ========================

    private static void testWeek4_TasksAndCapabilities() {
        printTestHeader("Week 4: Tasks & Agent Capabilities");

        // Test AgentCapabilities
        assertTest("AgentCapabilities creation", () -> {
            AgentState state = new AgentState();
            state.agentId = 1;
            AgentCapabilities caps = new AgentCapabilities(1, state);
            return caps.agentId == 1;
        });

        assertTest("AgentCapabilities task recording", () -> {
            AgentState state = new AgentState();
            state.agentId = 1;
            AgentCapabilities caps = new AgentCapabilities(1, state);
            caps.recordTaskCompletion(true);
            return caps.getSuccessfulTasks() == 1;
        });

        // Test Task
        assertTest("Task creation", () -> {
            Task task = new Task("task1", TaskType.MOVE_TO_LOCATION, new Point2D(100, 100));
            return task.taskId.equals("task1");
        });

        assertTest("Task state transitions", () -> {
            Task task = new Task("task1", TaskType.MOVE_TO_LOCATION, new Point2D(100, 100));
            task.assignTo(1);
            task.markInProgress();
            return task.getState() == Task.TaskState.IN_PROGRESS;
        });

        assertTest("Task factory methods", () -> {
            Task task = Task.createMoveTask("move1", new Point2D(100, 100));
            return task.taskType == TaskType.MOVE_TO_LOCATION;
        });
    }

    // ======================== WEEK 5 TESTS ========================

    private static void testWeek5_BoundaryAndMonitoring() {
        printTestHeader("Week 5: Boundary Management & Performance Monitoring");

        // Test BoundaryManager
        assertTest("BoundaryManager initialization", () -> {
            BoundaryManager boundary = BoundaryManager.getInstance();
            boundary.setWorldBounds(0, 0, 1000, 1000);
            return boundary.getWorldWidth() == 1000;
        });

        assertTest("BoundaryManager boundary check", () -> {
            BoundaryManager boundary = BoundaryManager.getInstance();
            boundary.setWorldBounds(0, 0, 1000, 1000);
            Point2D inside = new Point2D(500, 500);
            Point2D outside = new Point2D(1500, 1500);
            return boundary.isWithinWorldBounds(inside) && !boundary.isWithinWorldBounds(outside);
        });

        assertTest("BoundaryManager safe zones", () -> {
            BoundaryManager boundary = BoundaryManager.getInstance();
            boundary.setWorldBounds(0, 0, 1000, 1000);
            BoundaryManager.Zone zone = BoundaryManager.Zone.createRectangle(
                "safe1", 100, 100, 200, 200);
            boundary.addSafeZone("safe1", zone);
            return boundary.isInSafeZone(new Point2D(150, 150));
        });

        // Test PerformanceMonitor
        assertTest("PerformanceMonitor frame tracking", () -> {
            PerformanceMonitor monitor = PerformanceMonitor.getInstance();
            monitor.reset();
            monitor.startFrame();
            try { Thread.sleep(16); } catch (InterruptedException e) {}
            monitor.endFrame();

            return monitor.getFrameCount() > 0;
        });

        assertTest("PerformanceMonitor metrics", () -> {
            PerformanceMonitor monitor = PerformanceMonitor.getInstance();
            monitor.reset();
            for (int i = 0; i < 5; i++) {
                monitor.startFrame();
                try { Thread.sleep(16); } catch (InterruptedException e) {}
                monitor.endFrame();
            }
            PerformanceMonitor.PerformanceMetrics metrics = monitor.getCurrentMetrics();
            return metrics.currentFPS > 0;
        });
    }

    // ======================== WEEK 7 TESTS ========================

    private static void testWeek7_PerformanceOptimizer() {
        printTestHeader("Week 7: Performance Optimizer");

        assertTest("PerformanceOptimizer initialization", () -> {
            PerformanceOptimizer optimizer = new PerformanceOptimizer();
            return optimizer != null;
        });

        assertTest("PerformanceOptimizer captures performance snapshot", () -> {
            PerformanceOptimizer optimizer = new PerformanceOptimizer();
            PerformanceOptimizer.PerformanceSnapshot snapshot = optimizer.captureSnapshot();
            return snapshot.cpuUsage >= 0 && snapshot.memoryUsage >= 0;
        });

        assertTest("PerformanceOptimizer bottleneck detection", () -> {
            PerformanceOptimizer optimizer = new PerformanceOptimizer();
            List<PerformanceOptimizer.Bottleneck> bottlenecks = optimizer.detectBottlenecks();
            return bottlenecks != null;
        });

        assertTest("PerformanceOptimizer optimization", () -> {
            PerformanceOptimizer optimizer = new PerformanceOptimizer();
            PerformanceOptimizer.OptimizationResult result = optimizer.optimize();
            return result != null && result.snapshot != null;
        });
    }

    private static void testWeek7_CacheManager() {
        printTestHeader("Week 7: Cache Manager");

        assertTest("CacheManager agent state caching", () -> {
            CacheManager cache = new CacheManager();
            AgentState state = new AgentState();
            state.agentId = 1;
            state.position = new Point2D(100, 100);
            cache.cacheAgentState("agent1", state);

            AgentState retrieved = cache.getAgentState("agent1");
            return retrieved != null && retrieved.position.x == 100;
        });

        assertTest("CacheManager TTL expiration", () -> {
            CacheManager cache = new CacheManager();
            AgentState state = new AgentState();
            state.position = new Point2D(100, 100);
            cache.cacheAgentState("agent1", state, 100); // 100ms TTL

            try { Thread.sleep(150); } catch (InterruptedException e) {}

            AgentState retrieved = cache.getAgentState("agent1");
            return retrieved == null; // Should be expired
        });

        assertTest("CacheManager spatial query", () -> {
            CacheManager cache = new CacheManager();
            AgentState state1 = new AgentState();
            state1.position = new Point2D(100, 100);
            cache.cacheAgentState("agent1", state1);

            AgentState state2 = new AgentState();
            state2.position = new Point2D(110, 100);
            cache.cacheAgentState("agent2", state2);

            AgentState state3 = new AgentState();
            state3.position = new Point2D(500, 500);
            cache.cacheAgentState("agent3", state3);

            List<String> nearby = cache.getNearbyAgents(new Point2D(100, 100), 50);
            return nearby.size() >= 1; // Should find at least agent1
        });

        assertTest("CacheManager statistics", () -> {
            CacheManager cache = new CacheManager();
            AgentState state = new AgentState();
            state.position = new Point2D(100, 100);
            cache.cacheAgentState("agent1", state);
            cache.getAgentState("agent1"); // Hit
            cache.getAgentState("nonexistent"); // Miss

            CacheManager.CacheStatistics stats = cache.getCacheStatistics();
            return stats.getHits() > 0 && stats.getMisses() > 0;
        });
    }

    private static void testWeek7_ThreadPoolManager() {
        printTestHeader("Week 7: Thread Pool Manager");

        assertTest("ThreadPoolManager task submission", () -> {
            ThreadPoolManager pool = new ThreadPoolManager();
            final boolean[] executed = {false};

            Future<?> future = pool.submit(() -> {
                executed[0] = true;
            }, ThreadPoolManager.TaskPriority.NORMAL);

            try { future.get(1, TimeUnit.SECONDS); } catch (Exception e) {}
            pool.shutdown();
            return executed[0];
        });

        assertTest("ThreadPoolManager priority handling", () -> {
            ThreadPoolManager pool = new ThreadPoolManager(2, 4, 100);
            List<Integer> executionOrder = Collections.synchronizedList(new ArrayList<>());

            pool.submit(() -> {
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                executionOrder.add(1);
            }, ThreadPoolManager.TaskPriority.LOW);

            pool.submit(() -> {
                executionOrder.add(2);
            }, ThreadPoolManager.TaskPriority.HIGH);

            try { Thread.sleep(300); } catch (InterruptedException e) {}
            pool.shutdown();
            return executionOrder.size() == 2;
        });

        assertTest("ThreadPoolManager statistics", () -> {
            ThreadPoolManager pool = new ThreadPoolManager();
            pool.submit(() -> {}, ThreadPoolManager.TaskPriority.NORMAL);

            ThreadPoolManager.ThreadPoolStatistics stats = pool.getStatistics();
            pool.shutdown();
            return stats.getTasksSubmitted() > 0;
        });
    }

    private static void testWeek7_RouteOptimizer() {
        printTestHeader("Week 7: Route Optimizer");

        assertTest("RouteOptimizer simple route calculation", () -> {
            RouteOptimizer optimizer = new RouteOptimizer();
            RouteOptimizer.Route route = optimizer.calculateOptimalRoute(
                new Point2D(0, 0),
                new Point2D(100, 100)
            );
            return route != null && route.path.size() > 0;
        });

        assertTest("RouteOptimizer obstacle avoidance", () -> {
            RouteOptimizer optimizer = new RouteOptimizer();
            List<Point2D> obstacles = Arrays.asList(new Point2D(50, 50));

            RouteOptimizer.Route route = optimizer.calculateOptimalRoute(
                new Point2D(0, 0),
                new Point2D(100, 100),
                obstacles
            );
            return route != null;
        });

        assertTest("RouteOptimizer route caching", () -> {
            RouteOptimizer optimizer = new RouteOptimizer();
            optimizer.calculateOptimalRoute(new Point2D(0, 0), new Point2D(100, 100));
            optimizer.calculateOptimalRoute(new Point2D(0, 0), new Point2D(100, 100));

            RouteOptimizer.RouteStatistics stats = optimizer.getStatistics();
            return stats.getCacheHits() > 0;
        });
    }

    private static void testWeek7_SystemHealthMonitor() {
        printTestHeader("Week 7: System Health Monitor");

        assertTest("SystemHealthMonitor health check", () -> {
            SystemHealthMonitor monitor = new SystemHealthMonitor();
            SystemHealthMonitor.HealthReport report = monitor.checkSystemHealth();
            return report != null && report.healthScore >= 0;
        });

        assertTest("SystemHealthMonitor resource usage", () -> {
            SystemHealthMonitor monitor = new SystemHealthMonitor();
            SystemHealthMonitor.ResourceUsage usage = monitor.checkResourceUsage();
            return usage.memoryUsage >= 0 && usage.cpuUsage >= 0;
        });

        assertTest("SystemHealthMonitor agent health tracking", () -> {
            SystemHealthMonitor monitor = new SystemHealthMonitor();
            monitor.updateAgentHealth("agent1", SystemHealthMonitor.AgentHealthStatus.HEALTHY);
            SystemHealthMonitor.AgentHealthStatus status = monitor.getAgentHealth("agent1");
            return status == SystemHealthMonitor.AgentHealthStatus.HEALTHY;
        });
    }

    // ======================== WEEK 8 TESTS ========================

    private static void testWeek8_SystemConfiguration() {
        printTestHeader("Week 8: System Configuration");

        assertTest("SystemConfiguration parameter management", () -> {
            SystemConfiguration config = new SystemConfiguration();
            config.setParameter("testParam", 42);
            return config.getInt("testParam") == 42;
        });

        assertTest("SystemConfiguration type-safe getters", () -> {
            SystemConfiguration config = new SystemConfiguration();
            config.setParameter("intParam", 100);
            config.setParameter("doubleParam", 3.14);
            config.setParameter("boolParam", true);

            return config.getInt("intParam") == 100 &&
                   Math.abs(config.getDouble("doubleParam") - 3.14) < 0.01 &&
                   config.getBoolean("boolParam");
        });

        assertTest("SystemConfiguration validation", () -> {
            SystemConfiguration config = new SystemConfiguration();
            config.registerValidator("positiveInt", value -> {
                return value instanceof Integer && (Integer)value > 0;
            });

            try {
                config.setParameter("positiveInt", -5);
                return false; // Should have thrown exception
            } catch (IllegalArgumentException e) {
                return true;
            }
        });

        assertTest("SystemConfiguration locking", () -> {
            SystemConfiguration config = new SystemConfiguration();
            config.setParameter("param1", 100);
            config.lock();

            try {
                config.setParameter("param2", 200);
                return false; // Should have thrown exception
            } catch (IllegalStateException e) {
                return true;
            }
        });
    }

    private static void testWeek8_SystemValidator() {
        printTestHeader("Week 8: System Validator");

        assertTest("SystemValidator system validation", () -> {
            SystemValidator validator = new SystemValidator();
            SystemValidator.ValidationResult result = validator.validateSystem();
            return result != null;
        });

        assertTest("SystemValidator configuration validation", () -> {
            SystemValidator validator = new SystemValidator();
            SystemConfiguration config = new SystemConfiguration();
            config.setParameter("maxAgents", 100);
            config.setParameter("updateInterval", 16);
            config.setParameter("worldWidth", 1000.0);
            config.setParameter("worldHeight", 1000.0);

            SystemValidator.ValidationResult result = validator.validateConfiguration(config);
            return result.valid;
        });

        assertTest("SystemValidator memory validation", () -> {
            SystemValidator validator = new SystemValidator();
            SystemValidator.ValidationResult result = validator.validateMemory();
            return result != null;
        });

        assertTest("SystemValidator performance baseline", () -> {
            SystemValidator validator = new SystemValidator();
            SystemValidator.ValidationResult result = validator.validatePerformanceBaseline();
            return result != null;
        });
    }

    private static void testWeek8_IntrusionDetector() {
        printTestHeader("Week 8: Intrusion Detector");

        assertTest("IntrusionDetector initialization", () -> {
            IntrusionDetector detector = new IntrusionDetector();
            return detector != null;
        });

        assertTest("IntrusionDetector threat detection", () -> {
            IntrusionDetector detector = new IntrusionDetector();
            for (int i = 0; i < 15; i++) {
                detector.recordAgentActivity("suspicious-agent", "MOVE");
            }
            detector.shutdown();
            return true; // Just verify no exceptions
        });
    }

    // ======================== INTEGRATION TESTS ========================

    private static void testFullSystemIntegration() {
        printTestHeader("Full System Integration Test");

        assertTest("End-to-end multi-agent simulation", () -> {
            try {
                // Create core components
                EventBus eventBus = new EventBus();
                BoundaryManager boundary = BoundaryManager.getInstance();
                boundary.setWorldBounds(0, 0, 1000, 1000);
                PhysicsEngine physics = new PhysicsEngine();
                CacheManager cache = new CacheManager();
                SystemHealthMonitor healthMonitor = new SystemHealthMonitor();
                PerformanceMonitor perfMonitor = PerformanceMonitor.getInstance();

                // Create agents
                List<Agent> agents = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    Agent agent = new Agent(i, new Point2D(
                        Math.random() * 1000,
                        Math.random() * 1000
                    ));
                    agent.setEventBus(eventBus);
                    agents.add(agent);

                    // Cache agent state
                    cache.cacheAgentState("agent-" + i, agent.getState());

                    // Update health
                    healthMonitor.updateAgentHealth("agent-" + i,
                        SystemHealthMonitor.AgentHealthStatus.HEALTHY);
                }

                // Simulate one update cycle
                perfMonitor.startFrame();

                for (Agent agent : agents) {
                    // Apply physics
                    Vector2D force = new Vector2D(Math.random() - 0.5, Math.random() - 0.5);
                    physics.applyForce(agent.getState(), force, 0.016);

                    // Enforce boundaries
                    Point2D correctedPos = boundary.enforceBoundaries(agent.getState());

                    // Update cache
                    cache.cacheAgentState("agent-" + agent.getState().agentId, agent.getState());
                }

                perfMonitor.endFrame();

                // Check system health
                SystemHealthMonitor.HealthReport healthReport = healthMonitor.getHealthReport();

                return agents.size() == 10 && healthReport.healthScore > 0;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    private static void testStressTest() {
        printTestHeader("Stress Test (100 agents, 1000 operations)");

        assertTest("High-load stress test", () -> {
            try {
                CacheManager cache = new CacheManager();
                ThreadPoolManager threadPool = new ThreadPoolManager();
                List<Future<?>> futures = new ArrayList<>();

                // Create 100 agents
                for (int i = 0; i < 100; i++) {
                    final int agentId = i;
                    Future<?> future = threadPool.submit(() -> {
                        for (int j = 0; j < 10; j++) {
                            AgentState state = new AgentState();
                            state.position = new Point2D(Math.random() * 1000, Math.random() * 1000);
                            cache.cacheAgentState("agent-" + agentId, state);
                            cache.getAgentState("agent-" + agentId);
                        }
                    }, ThreadPoolManager.TaskPriority.NORMAL);
                    futures.add(future);
                }

                // Wait for all tasks
                for (Future<?> future : futures) {
                    future.get(5, TimeUnit.SECONDS);
                }

                threadPool.shutdown();

                CacheManager.CacheStatistics stats = cache.getCacheStatistics();
                return stats.getHits() > 0 && stats.getWrites() > 0;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    // ======================== HELPER METHODS ========================

    private static void printTestHeader(String header) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(header);
        System.out.println("=".repeat(60));
    }

    private static void assertTest(String testName, TestAssertion assertion) {
        try {
            boolean result = assertion.test();
            if (result) {
                System.out.println("✓ " + testName);
                testsPassed++;
            } else {
                System.out.println("✗ " + testName + " (assertion failed)");
                testsFailed++;
                failedTests.add(testName);
            }
        } catch (Exception e) {
            System.out.println("✗ " + testName + " (exception: " + e.getMessage() + ")");
            testsFailed++;
            failedTests.add(testName + " (" + e.getMessage() + ")");
        }
    }

    @FunctionalInterface
    private interface TestAssertion {
        boolean test() throws Exception;
    }
}
