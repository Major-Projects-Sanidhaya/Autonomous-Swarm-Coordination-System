# Quick Start Guide - Core Agent System

This guide will help you get started with the Swarm Coordination Core Agent System.

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Basic Setup](#basic-setup)
3. [Creating Your First Agent](#creating-your-first-agent)
4. [Running a Simulation](#running-a-simulation)
5. [Common Operations](#common-operations)
6. [Complete Examples](#complete-examples)
7. [Troubleshooting](#troubleshooting)

---

## System Overview

### What is this?

The Core Agent System provides the foundation for multi-agent swarm coordination. It includes:

- **Agent Management**: Create, update, and remove agents
- **Physics Engine**: Realistic movement and collision detection
- **Event System**: Decoupled communication between components
- **Task Management**: Assign and track work
- **Performance Monitoring**: Real-time system metrics
- **Boundary Management**: World limits and safe zones

### Architecture

```
SystemController
├── AgentManager (manages all agents)
│   └── Agent (individual agents)
├── EventBus (message routing)
├── PhysicsEngine (movement physics)
├── BoundaryManager (spatial constraints)
└── PerformanceMonitor (system metrics)
```

---

## Basic Setup

### Step 1: Initialize the System

```java
// Create and initialize system controller
SystemController controller = new SystemController();
controller.initialize(); // Creates agents, event bus, etc.
```

### Step 2: Start the Simulation

```java
// Start the simulation loop (runs at 60 FPS)
controller.start();

// The system is now running!
```

### Step 3: Stop When Done

```java
// Stop the simulation cleanly
controller.stop();
```

---

## Creating Your First Agent

### Manual Agent Creation

```java
// Get agent manager
AgentManager manager = controller.getAgentManager();

// Create agent at position (400, 300)
Point2D spawnPoint = new Point2D(400, 300);
Agent agent = manager.createAgent(spawnPoint);

// Get agent ID
int agentId = agent.getState().agentId;
System.out.println("Created agent " + agentId);
```

### Setting Initial Properties

```java
// Get agent state
AgentState state = agent.getState();

// Modify properties
state.maxSpeed = 100.0;              // Max speed
state.communicationRange = 150.0;    // Radio range
state.batteryLevel = 1.0;            // Full battery

System.out.println("Agent configured: " + state);
```

---

## Running a Simulation

### Complete Simulation Example

```java
public class MyFirstSimulation {
    public static void main(String[] args) {
        // 1. Create system
        SystemController controller = new SystemController();
        controller.initialize();

        // 2. Get managers
        AgentManager agentManager = controller.getAgentManager();
        EventBus eventBus = controller.getEventBus();

        // 3. Create agents
        for (int i = 0; i < 5; i++) {
            Point2D pos = new Point2D(
                Math.random() * 800,
                Math.random() * 600
            );
            Agent agent = agentManager.createAgent(pos);
            System.out.println("Created " + agent.getState().agentName);
        }

        // 4. Subscribe to events
        eventBus.subscribe(AgentStateUpdate.class, update -> {
            System.out.println("Agent " + update.agentId +
                " at (" + update.agentState.position.x + ", " +
                update.agentState.position.y + ")");
        });

        // 5. Start simulation
        controller.start();

        // 6. Run for 10 seconds
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 7. Stop simulation
        controller.stop();

        System.out.println("Simulation complete!");
    }
}
```

---

## Common Operations

### 1. Moving an Agent

```java
// Create movement command
MovementCommand cmd = new MovementCommand(
    agentId,
    MovementType.MOVE_TO_TARGET,
    CommandPriority.NORMAL
);

// Set target position
Point2D target = new Point2D(500, 400);
cmd.addParameter("target", target);

// Send to agent
agent.addMovementCommand(cmd);
```

### 2. Monitoring Agent Status

```java
// Get current state
AgentState state = agent.getState();

// Check status
switch (state.status) {
    case ACTIVE:
        System.out.println("Agent is active");
        break;
    case BATTERY_LOW:
        System.out.println("Battery low: " + state.batteryLevel);
        break;
    case FAILED:
        System.out.println("Agent has failed!");
        break;
}
```

### 3. Creating Tasks

```java
// Create a patrol task
Point2D[] waypoints = {
    new Point2D(100, 100),
    new Point2D(700, 100),
    new Point2D(700, 500),
    new Point2D(100, 500)
};
Task patrolTask = Task.createPatrolTask("patrol_001", waypoints);

// Assign to agent
patrolTask.assignTo(agentId);
patrolTask.markInProgress();

// Check progress later
if (patrolTask.isComplete()) {
    System.out.println("Task completed!");
}
```

### 4. Setting Boundaries

```java
// Get boundary manager
BoundaryManager bm = BoundaryManager.getInstance();

// Set world boundaries
bm.setWorldBounds(0, 0, 800, 600);

// Add a safe zone (rectangular)
Zone safeZone = Zone.createRectangle(
    "operational_area",
    50, 50,    // min X, Y
    750, 550   // max X, Y
);
bm.addSafeZone("main_area", safeZone);

// Add restricted zone (circular obstacle)
Zone obstacle = Zone.createCircle(
    "obstacle",
    new Point2D(400, 300),  // center
    50.0                     // radius
);
bm.addRestrictedZone("obstacle_1", obstacle);
```

### 5. Performance Monitoring

```java
// Get performance monitor
PerformanceMonitor pm = PerformanceMonitor.getInstance();
pm.setEventBus(eventBus);

// Enable auto-optimization
pm.setAutoOptimize(true);

// In your update loop:
pm.startFrame();
// ... do work ...
pm.endFrame();

// Check performance
if (pm.isPerformanceDegraded()) {
    System.out.println("Performance warning!");
    System.out.println(pm.getPerformanceReport());
}
```

### 6. Checking Agent Capabilities

```java
// Create capabilities snapshot
AgentState state = agent.getState();
AgentCapabilities capabilities = new AgentCapabilities(
    agentId,
    state
);

// Check if agent can perform task
if (capabilities.canPerformTask("SCOUT")) {
    System.out.println("Agent can scout");
}

// Get overall capability
double overall = capabilities.getOverallCapability();
System.out.println("Overall capability: " + (overall * 100) + "%");

// Get detailed report
System.out.println(capabilities.getDetailedReport());
```

---

## Complete Examples

### Example 1: Simple Movement Test

```java
public class SimpleMovementTest {
    public static void main(String[] args) throws InterruptedException {
        // Setup
        SystemController controller = new SystemController();
        controller.initialize();
        AgentManager manager = controller.getAgentManager();

        // Create agent at origin
        Agent agent = manager.createAgent(new Point2D(0, 0));
        int agentId = agent.getState().agentId;

        // Create movement command to (100, 100)
        MovementCommand cmd = new MovementCommand(
            agentId,
            MovementType.MOVE_TO_TARGET,
            CommandPriority.NORMAL
        );
        cmd.addParameter("target", new Point2D(100, 100));

        // Start simulation
        controller.start();

        // Send command
        agent.addMovementCommand(cmd);

        // Wait and check position every second
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            AgentState state = agent.getState();
            System.out.printf("Position: (%.1f, %.1f)\n",
                state.position.x, state.position.y);
        }

        // Cleanup
        controller.stop();
    }
}
```

### Example 2: Multi-Agent Swarm

```java
public class SwarmSimulation {
    public static void main(String[] args) throws InterruptedException {
        // Setup
        SystemController controller = new SystemController();
        controller.initialize();
        AgentManager manager = controller.getAgentManager();
        EventBus eventBus = controller.getEventBus();

        // Create 10 agents in a circle
        List<Agent> agents = new ArrayList<>();
        int numAgents = 10;
        double radius = 200;
        Point2D center = new Point2D(400, 300);

        for (int i = 0; i < numAgents; i++) {
            double angle = (2 * Math.PI * i) / numAgents;
            double x = center.x + radius * Math.cos(angle);
            double y = center.y + radius * Math.sin(angle);

            Agent agent = manager.createAgent(new Point2D(x, y));
            agents.add(agent);
        }

        // Make them move to center
        for (Agent agent : agents) {
            MovementCommand cmd = new MovementCommand(
                agent.getState().agentId,
                MovementType.MOVE_TO_TARGET,
                CommandPriority.NORMAL
            );
            cmd.addParameter("target", center);
            agent.addMovementCommand(cmd);
        }

        // Subscribe to task completions
        eventBus.subscribe(TaskCompletionReport.class, report -> {
            System.out.println("Agent " + report.agentId +
                " completed task: " + report.status);
        });

        // Run simulation
        controller.start();
        Thread.sleep(30000); // 30 seconds
        controller.stop();

        // Final positions
        System.out.println("\nFinal positions:");
        for (Agent agent : agents) {
            AgentState state = agent.getState();
            System.out.printf("Agent %d: (%.1f, %.1f)\n",
                state.agentId, state.position.x, state.position.y);
        }
    }
}
```

### Example 3: Event-Driven Monitoring

```java
public class EventMonitoringExample {
    public static void main(String[] args) throws InterruptedException {
        SystemController controller = new SystemController();
        controller.initialize();
        EventBus eventBus = controller.getEventBus();

        // Count events
        AtomicInteger stateUpdates = new AtomicInteger(0);
        AtomicInteger taskCompletions = new AtomicInteger(0);

        // Subscribe to state updates
        eventBus.subscribe(AgentStateUpdate.class, update -> {
            stateUpdates.incrementAndGet();
        });

        // Subscribe to task completions
        eventBus.subscribe(TaskCompletionReport.class, report -> {
            taskCompletions.incrementAndGet();
            System.out.println("Task " + report.taskId +
                ": " + report.status);
        });

        // Subscribe to system events
        eventBus.subscribe(SystemEvent.class, event -> {
            if (event.getSeverity() == SystemEvent.Severity.ERROR) {
                System.err.println("ERROR: " + event.getMessage());
            }
        });

        // Run simulation
        controller.start();
        Thread.sleep(10000);
        controller.stop();

        // Print statistics
        System.out.println("\nEvent Statistics:");
        System.out.println("State Updates: " + stateUpdates.get());
        System.out.println("Task Completions: " + taskCompletions.get());
    }
}
```

### Example 4: Performance Monitoring

```java
public class PerformanceExample {
    public static void main(String[] args) throws InterruptedException {
        SystemController controller = new SystemController();
        controller.initialize();

        // Setup performance monitoring
        PerformanceMonitor pm = PerformanceMonitor.getInstance();
        pm.setEventBus(controller.getEventBus());
        pm.setAutoOptimize(true);
        pm.setTargetFPS(60);

        // Create many agents to stress test
        AgentManager manager = controller.getAgentManager();
        for (int i = 0; i < 50; i++) {
            Point2D pos = new Point2D(
                Math.random() * 800,
                Math.random() * 600
            );
            manager.createAgent(pos);
        }

        // Subscribe to performance events
        controller.getEventBus().subscribe(SystemEvent.class, event -> {
            if (event.getEventType().equals("PERFORMANCE_STATUS_CHANGED")) {
                System.out.println(event.getMessage());
            }
        });

        // Run and monitor
        controller.start();

        for (int i = 0; i < 30; i++) {
            Thread.sleep(1000);

            PerformanceMonitor.PerformanceMetrics metrics =
                pm.getCurrentMetrics();

            System.out.printf("FPS: %.1f | Memory: %.1f%% | Status: %s\n",
                metrics.currentFPS,
                metrics.memoryUsagePercent * 100,
                metrics.status);
        }

        controller.stop();

        // Final report
        System.out.println("\n" + pm.getPerformanceReport());
    }
}
```

---

## Troubleshooting

### Problem: Agents not moving

**Possible causes:**
1. Simulation not started: Call `controller.start()`
2. No movement commands sent
3. Agent status is FAILED or INACTIVE
4. Battery depleted

**Solution:**
```java
// Check simulation state
System.out.println("State: " + controller.getState());

// Check agent status
AgentState state = agent.getState();
System.out.println("Status: " + state.status);
System.out.println("Battery: " + state.batteryLevel);

// Check command queue
System.out.println("Queue size: " + agent.getQueueSize());
```

### Problem: High memory usage

**Possible causes:**
1. Too many agents
2. Memory leak in event listeners
3. Large history buffers

**Solution:**
```java
// Monitor memory
PerformanceMonitor pm = PerformanceMonitor.getInstance();
System.out.println("Memory: " + pm.getMemoryUsagePercent());

// Enable aggressive optimization
pm.setOptimizationLevel(
    PerformanceMonitor.OptimizationLevel.AGGRESSIVE
);

// Suggest GC
System.gc();
```

### Problem: Low FPS

**Possible causes:**
1. Too many agents
2. Complex calculations
3. Event storm

**Solution:**
```java
// Check FPS
PerformanceMonitor pm = PerformanceMonitor.getInstance();
System.out.println("FPS: " + pm.getCurrentFPS());

// Reduce agent count
// Enable auto-optimization
pm.setAutoOptimize(true);

// Lower target FPS
controller.setTargetFPS(30);
```

### Problem: Boundary violations

**Possible causes:**
1. Boundaries not set
2. Wrong enforcement mode
3. No safe zones defined

**Solution:**
```java
// Set boundaries
BoundaryManager bm = BoundaryManager.getInstance();
bm.setWorldBounds(0, 0, 800, 600);

// Check enforcement
System.out.println("Mode: " + bm.getEnforcementMode());

// Check violations
System.out.println("Violations: " + bm.getTotalViolations());
```

---

## Next Steps

1. **Read the complete API reference**: [COMPLETE_API_REFERENCE.md](COMPLETE_API_REFERENCE.md)
2. **Explore the test files**: Week3IntegrationTest.java, Week4IntegrationTest.java
3. **Integrate with other systems**:
   - John's communication package
   - Lauren's intelligence package
   - Anthony's UI package

---

## Additional Resources

- **Source Code**: `src/main/java/com/team6/swarm/core/`
- **Tests**: `src/test/java/`
- **Project Documentation**: `Sanidhaya_doc.pdf`

---

## Support

For questions or issues:
1. Check the API reference
2. Review the source code comments
3. Run the integration tests
4. Consult the project documentation

---

**Happy coding! 🚀**
