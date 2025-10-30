# Swarm Coordination System - Complete Beginner's Guide

**For:** New Java developers and freshers
**Purpose:** Understand the entire swarm agent system from scratch
**Reading Time:** 45-60 minutes

---

## 📚 Table of Contents

1. [Introduction](#introduction)
2. [Week 1: Foundation - The Basics](#week-1-foundation)
3. [Week 2: Communication - Talking to Each Other](#week-2-communication)
4. [Week 3: Movement - Making Things Move](#week-3-movement)
5. [Week 4: User Interface - Showing It](#week-4-user-interface)
6. [Week 5-6: Advanced - Making It Better](#week-5-6-advanced)
7. [Putting It All Together](#putting-it-all-together)
8. [Common Patterns](#common-patterns)
9. [Glossary](#glossary)

---

## Introduction

### What Are We Building?

Imagine you're controlling a swarm of delivery drones (like a mini Amazon fleet). Each drone:
- Knows where it is 📍
- Can move around 🚁
- Has a battery 🔋
- Can talk to nearby drones 📡
- Gets tasks to complete 📦
- Avoids crashing into things 🚫

This system lets you create, control, and coordinate many such "agents" (drones) working together!

### How to Read This Guide

- **💡 Yellow boxes** = Important concepts
- **📝 Green boxes** = Code examples
- **⚠️ Red boxes** = Common mistakes
- **🎯 Blue boxes** = Key takeaways

Don't try to understand everything at once! Read one section, try the examples, then move to the next.

---

## Week 1: Foundation

### The Big Picture

Think of building with LEGO:
- **Point2D** = The position of a LEGO brick
- **Vector2D** = The direction you're pushing it
- **AgentState** = All the information about one brick
- **Agent** = The brick itself that can move
- **AgentManager** = The box holding all your bricks

###1. Point2D - Where Is It?

**Real-world analogy:** GPS coordinates, but simpler (just X and Y).

```java
// Creating a position
Point2D homeBase = new Point2D(0, 0);        // At the origin
Point2D targetLocation = new Point2D(100, 50); // 100 units right, 50 up

// Measuring distance
double distance = homeBase.distanceTo(targetLocation);
System.out.println("Distance: " + distance); // Prints: Distance: 111.8

// Moving a position
Vector2D movement = new Vector2D(10, 5);     // Move 10 right, 5 up
Point2D newPosition = homeBase.add(movement);
System.out.println("New position: " + newPosition); // (10, 5)
```

**Think of it like:**
```
Y (up/down)
↑
100|
 50|        • target (100, 50)
  0|• home
   |___________________→ X (left/right)
   0       50      100
```

### 2. Vector2D - How Is It Moving?

**Real-world analogy:** Not just speed, but speed + direction (like "60 mph northeast").

```java
// Creating velocity (speed + direction)
Vector2D velocity = new Vector2D(10, 5);
// Moving 10 units/second to the right
// Moving 5 units/second upward

// How fast is it going total?
double speed = velocity.magnitude();
System.out.println("Speed: " + speed); // Prints: 11.18 units/second

// Getting a direction without speed (unit vector)
Vector2D direction = velocity.normalize();
// This is like saying "northeast" without saying "how fast"

// Combining velocities (like wind + thrust)
Vector2D wind = new Vector2D(2, 3);
Vector2D thrust = new Vector2D(8, 2);
Vector2D totalVelocity = wind.add(thrust); // (10, 5)
```

**Visual:**
```
      ↗ velocity (10, 5)
     /  "Moving this way at 11.18 units/sec"
    /
   • agent
```

### 3. AgentStatus - What's Its Condition?

**Real-world analogy:** Traffic light status (green = go, yellow = warning, red = stop).

```java
// Possible statuses
AgentStatus.ACTIVE       // 🟢 All good, working normally
AgentStatus.BATTERY_LOW  // 🟡 Warning! Battery low
AgentStatus.FAILED       // 🔴 Broken! Can't work
AgentStatus.INACTIVE     // ⚪ Turned off
AgentStatus.MAINTENANCE  // 🟠 Being repaired

// Using it
AgentState state = new AgentState();
state.status = AgentStatus.ACTIVE;

// Checking status
if (state.status == AgentStatus.BATTERY_LOW) {
    System.out.println("Send to charging station!");
}
```

### 4. AgentState - The Complete Picture

**Real-world analogy:** Your driver's license - it has ALL your info in one place.

```java
// Creating an agent's "ID card"
AgentState state = new AgentState();

// Fill in the information
state.agentId = 1;                           // ID number
state.agentName = "Scout_1";                 // Name
state.position = new Point2D(100, 200);      // Where it is
state.velocity = new Vector2D(10, 5);        // How it's moving
state.batteryLevel = 0.85;                   // 85% battery
state.maxSpeed = 50.0;                       // Speed limit
state.communicationRange = 100.0;            // Radio range
state.status = AgentStatus.ACTIVE;           // Status
state.lastUpdateTime = System.currentTimeMillis(); // When updated

// Reading the information
System.out.println("Agent " + state.agentId + " is at (" +
    state.position.x + ", " + state.position.y + ")");
System.out.println("Battery: " + (state.batteryLevel * 100) + "%");
```

**Think of it like a dashboard:**
```
╔═══════════════════════════════╗
║      Agent ID: 1              ║
║      Name: Scout_1            ║
╠═══════════════════════════════╣
║ Location: (100, 200)          ║
║ Velocity: (10, 5)             ║
║ Battery: █████████░ 85%       ║
║ Status: 🟢 ACTIVE             ║
╚═══════════════════════════════╝
```

### 5. Agent - The Actual Robot

**Real-world analogy:** The actual drone flying around (not just its information).

```java
// Creating an agent
Point2D startPosition = new Point2D(0, 0);
Agent agent = new Agent(1, startPosition);  // ID=1, at origin

// The agent updates itself every frame
agent.update(0.016);  // Update with 16ms time passed (≈60 FPS)

// Giving it a command
MovementCommand cmd = new MovementCommand(
    agent.getState().agentId,
    MovementType.MOVE_TO_TARGET,
    CommandPriority.NORMAL
);
cmd.addParameter("target", new Point2D(100, 100));
agent.addMovementCommand(cmd);

// The agent will now move toward (100, 100)!
```

**What happens inside `update()`:**
```
1. Process pending commands
2. Update position based on velocity
3. Drain battery a little bit
4. Check if tasks are complete
5. Tell everyone about the update
```

### 6. AgentManager - The Boss

**Real-world analogy:** Fleet manager who oversees all the drones.

```java
// Create the manager
AgentManager manager = new AgentManager();

// Spawn agents
Agent agent1 = manager.createAgent(new Point2D(0, 0));
Agent agent2 = manager.createAgent(new Point2D(100, 0));
Agent agent3 = manager.createAgent(new Point2D(200, 0));

System.out.println("Created " + manager.getAgentCount() + " agents");

// Start the simulation (agents will update automatically)
manager.startSimulation();

// Wait 10 seconds
Thread.sleep(10000);

// Stop the simulation
manager.stopSimulation();

// Get all agent states
List<AgentState> allStates = manager.getAllAgentStates();
for (AgentState state : allStates) {
    System.out.println("Agent " + state.agentId +
        " at (" + state.position.x + ", " + state.position.y + ")");
}
```

---

## Week 2: Communication

### The Big Picture

Week 1 created agents. Week 2 makes them **talk to each other**.

**Problem:** How does Agent 1 tell Agent 2 where it is?
**Solution:** EventBus - a message board where agents post updates!

### 1. EventBus - The Message Board

**Real-world analogy:** School bulletin board or Discord server.

```java
// Create the message board
EventBus eventBus = new EventBus();

// Person 1: "I want to hear about agent updates"
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("Agent " + update.agentId + " moved!");
});

// Person 2: Also wants to hear about them
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("I heard agent " + update.agentId + " moved too!");
});

// Agent: "I moved!" (posts to the board)
AgentStateUpdate update = new AgentStateUpdate();
update.agentId = 5;
update.agentState = someAgent.getState();
eventBus.publish(update);

// Both Person 1 and Person 2 get the message automatically!
```

**The flow:**
```
Agent → publish(update) → EventBus → delivers to → {Subscriber1, Subscriber2, Subscriber3}
```

### 2. AgentStateUpdate - Position Change Message

**Real-world analogy:** A text message saying "I'm at Starbucks now".

```java
// Creating an update message
AgentStateUpdate update = new AgentStateUpdate();
update.agentId = 1;                          // Which agent
update.agentState = agent.getState();        // Complete current state
update.updateType = UpdateType.POSITION_ONLY; // Just position changed
update.timestamp = System.currentTimeMillis(); // When

// Send it
eventBus.publish(update);

// Anyone subscribed receives it:
eventBus.subscribe(AgentStateUpdate.class, update -> {
    Point2D pos = update.agentState.position;
    System.out.println("Agent " + update.agentId +
        " is now at (" + pos.x + ", " + pos.y + ")");
});
```

### 3. CommunicationEvent - Agent-to-Agent Messages

**Real-world analogy:** Walkie-talkie messages between drones.

```java
// Agent 5 sends message to Agent 3
CommunicationEvent message = new CommunicationEvent(
    5,      // sender (Agent 5)
    3,      // receiver (Agent 3)
    "HELP", // message type
    "I'm low on battery!", // actual message
    100.0   // communication range
);

// Publish it
eventBus.publish(message);

// Agent 3 receives it (if subscribed)
eventBus.subscribe(CommunicationEvent.class, event -> {
    if (event.receiverId == 3) {
        System.out.println("Message from Agent " + event.senderId +
            ": " + event.message);
    }
});
```

### 4. SystemController - The Master Conductor

**Real-world analogy:** The control tower at an airport.

```java
// Create and initialize the whole system
SystemController controller = new SystemController();
controller.initialize();  // Sets up everything

// Get the pieces
EventBus eventBus = controller.getEventBus();
AgentManager agentManager = controller.getAgentManager();

// Start the simulation
controller.start();  // Everything starts running!

// The simulation is now running at 60 FPS
// Agents are updating, messages are flowing

// Later, stop it
controller.stop();
```

**What happens in `initialize()`:**
```
1. Create EventBus
2. Create AgentManager
3. Create initial agents
4. Set up event subscriptions
5. Initialize performance monitoring
6. Set system state to READY
```

---

## Week 3: Movement

### The Big Picture

Weeks 1-2 created agents and made them communicate. Week 3 makes them **move intelligently**.

### 1. MovementCommand - Instructions for Movement

**Real-world analogy:** Instructions to a taxi driver ("Go to Main Street").

```java
// Create a movement command
MovementCommand cmd = new MovementCommand(
    agentId,                        // Which agent
    MovementType.MOVE_TO_TARGET,    // What to do
    CommandPriority.NORMAL          // How urgent
);

// Add details
cmd.addParameter("target", new Point2D(500, 300));

// Send to agent
agent.addMovementCommand(cmd);

// The agent will start moving toward (500, 300)!
```

**Command types:**
```java
MovementType.MOVE_TO_TARGET     // "Go to this point"
MovementType.FLOCKING_BEHAVIOR  // "Move with the group"
MovementType.FORMATION_POSITION // "Get in formation"
MovementType.AVOID_OBSTACLE     // "Dodge this obstacle!"
```

**Priority levels:**
```java
CommandPriority.EMERGENCY  // "DO THIS NOW!" (collision avoidance)
CommandPriority.HIGH       // "Important!" (critical tasks)
CommandPriority.NORMAL     // "Regular task"
CommandPriority.LOW        // "When you have time"
```

### 2. PhysicsEngine - Making Movement Realistic

**Real-world analogy:** The physics in a video game (gravity, momentum, collisions).

```java
PhysicsEngine physics = new PhysicsEngine();

// Apply force to agent (like pushing it)
Vector2D force = new Vector2D(10, 0);  // Push right
physics.applyForce(agentState, force, deltaTime);

// Update position based on velocity
physics.updatePosition(agentState, deltaTime);

// Keep speed within limits
physics.limitVelocity(agentState, maxSpeed);

// Handle boundaries (bounce off walls)
physics.checkBoundaries(agentState);

// Check for collisions
if (physics.checkCollision(agent1State, agent2State)) {
    System.out.println("Collision detected!");
}
```

**Steering behaviors:**
```java
// Seek: Move toward a target
Vector2D seekForce = physics.seek(agentState, target, desiredSpeed);

// Flee: Run away from danger
Vector2D fleeForce = physics.flee(agentState, threat, desiredSpeed);

// Separation: Keep distance from neighbors
Vector2D separationForce = physics.separationForce(agentState, neighbors);
```

### 3. TaskCompletionReport - Feedback Loop

**Real-world analogy:** Delivery confirmation ("Package delivered!").

```java
// Agent completes a task
TaskCompletionReport report = new TaskCompletionReport(
    "task_001",                          // Task ID
    5,                                   // Agent ID
    TaskCompletionReport.CompletionStatus.SUCCESS
);

// Add details
report.setDuration(15.5);  // Took 15.5 seconds
report.addResult("distance traveled", 250.0);
report.addResult("battery used", 0.05);

// Publish it
eventBus.publish(report);

// Intelligence system receives it and learns
eventBus.subscribe(TaskCompletionReport.class, report -> {
    if (report.status == CompletionStatus.SUCCESS) {
        System.out.println("Agent " + report.agentId +
            " completed task in " + report.duration + " seconds");
    }
});
```

---

## Week 4: User Interface

### The Big Picture

Weeks 1-3 built the backend. Week 4 connects it to the **user interface**.

### 1. SystemCommand - User Input

**Real-world analogy:** Buttons you click in the UI.

```java
// User clicks "Spawn Agent"
SystemCommand cmd = new SystemCommand(CommandType.SPAWN_AGENT);
cmd.addParameter("position", new Point2D(400, 300));
eventBus.publish(cmd);

// System receives and handles it
eventBus.subscribe(SystemCommand.class, command -> {
    switch (command.commandType) {
        case SPAWN_AGENT:
            Point2D pos = (Point2D) command.getParameter("position");
            agentManager.createAgent(pos);
            break;

        case REMOVE_AGENT:
            int id = (Integer) command.getParameter("agentId");
            agentManager.removeAgent(id);
            break;

        case START_SIMULATION:
            controller.start();
            break;
    }
});
```

### 2. VisualizationUpdate - Display Data

**Real-world analogy:** The data your screen needs to draw everything.

```java
// Get visualization data (called 30-60 times per second)
VisualizationUpdate update = agentManager.getVisualizationUpdate();

// It contains:
// - All agent states (positions, velocities, etc.)
// - System metrics (FPS, memory, etc.)
// - Timestamp

// UI uses this to draw
for (AgentState state : update.allAgents) {
    drawAgent(state.position.x, state.position.y, state.status);
}

// Display metrics
System.out.println("FPS: " + update.systemMetrics.updatesPerSecond);
System.out.println("Active agents: " + update.systemMetrics.activeAgents);
```

### 3. SystemEvent - Notifications

**Real-world analogy:** Pop-up notifications ("Agent created", "Error occurred").

```java
// Subscribe to system events
eventBus.subscribe(SystemEvent.class, event -> {
    // Different severity levels
    switch (event.getSeverity()) {
        case ERROR:
            System.err.println("❌ ERROR: " + event.getMessage());
            break;

        case WARNING:
            System.out.println("⚠️  WARNING: " + event.getMessage());
            break;

        case INFO:
            System.out.println("ℹ️  INFO: " + event.getMessage());
            break;
    }
});

// Publishing events
eventBus.publish(SystemEvent.info("AGENT_CREATED",
    "Agent 5 created at (100, 200)"));

eventBus.publish(SystemEvent.warning("BATTERY_LOW",
    "Agent 3 battery at 15%"));

eventBus.publish(SystemEvent.error("COLLISION",
    "Agents 2 and 7 collided!"));
```

### 4. SystemMetrics - Performance Data

**Real-world analogy:** Task Manager showing CPU/memory usage.

```java
SystemMetrics metrics = controller.getMetrics();

// Check performance
System.out.println("Total agents: " + metrics.totalAgents);
System.out.println("Active agents: " + metrics.activeAgents);
System.out.println("FPS: " + metrics.updatesPerSecond);
System.out.println("Memory usage: " + (metrics.memoryUsage * 100) + "%");
System.out.println("System load: " + (metrics.systemLoad * 100) + "%");

// Alert if performance is bad
if (metrics.updatesPerSecond < 30) {
    System.out.println("⚠️  Performance warning: Low FPS!");
}

if (metrics.memoryUsage > 0.9) {
    System.out.println("⚠️  Memory warning: Usage above 90%!");
}
```

---

## Week 5-6: Advanced

### The Big Picture

Weeks 1-4 built a working system. Weeks 5-6 make it **smarter and more robust**.

### 1. AgentCapabilities - Performance Assessment

**Real-world analogy:** Employee performance review.

```java
// Check what an agent can do
AgentCapabilities capabilities = new AgentCapabilities(
    agentId,
    agent.getState()
);

// Overall capability (0.0 to 1.0)
double overall = capabilities.getOverallCapability();
System.out.println("Overall capability: " + (overall * 100) + "%");

// Check specific capabilities
System.out.println("Movement: " + capabilities.getMovementCapability());
System.out.println("Communication: " + capabilities.getCommunicationCapability());
System.out.println("Battery: " + capabilities.getBatteryCapacity());
System.out.println("Reliability: " + capabilities.getReliabilityScore());

// Can it perform a task?
if (capabilities.canPerformTask("SCOUT")) {
    assignTask(agent, scoutTask);
} else {
    System.out.println("Agent can't handle this task");
}

// Check status
if (capabilities.isCritical()) {
    System.out.println("⚠️  Agent needs immediate attention!");
}
```

### 2. Task - Work Assignments

**Real-world analogy:** To-do list item.

```java
// Create a task
Task task = Task.createMoveTask("task_001", new Point2D(500, 300));

// Assign to agent
task.assignTo(agentId);
System.out.println("Task assigned: " + task.getState());  // ASSIGNED

// Agent starts working
task.markInProgress();
System.out.println("Task started: " + task.getState());  // IN_PROGRESS

// Update progress
task.updateProgress(0.5);  // 50% complete

// Complete it
task.markCompleted();
System.out.println("Task done: " + task.getState());  // COMPLETED

// Lifecycle:
// PENDING → ASSIGNED → IN_PROGRESS → COMPLETED
//                              ↓
//                           FAILED
```

**Different task types:**
```java
// Move to location
Task move = Task.createMoveTask("move1", targetPoint);

// Patrol a route
Task patrol = Task.createPatrolTask("patrol1", waypoint1, waypoint2, waypoint3);

// Guard a position
Task guard = Task.createGuardTask("guard1", guardPoint);

// Follow another agent
Task follow = Task.createFollowTask("follow1", leaderAgentId);

// Return to base
Task return = Task.createReturnTask("return1", baseLocation);
```

### 3. BoundaryManager - Spatial Constraints

**Real-world analogy:** Invisible fences for dogs, or airport restricted zones.

```java
// Get the boundary manager (singleton)
BoundaryManager bm = BoundaryManager.getInstance();
bm.setEventBus(eventBus);

// Set world boundaries
bm.setWorldBounds(0, 0, 800, 600);
// Agents can't go outside this area

// Add safe zone (where agents can operate)
Zone safeZone = Zone.createRectangle("operational", 50, 50, 750, 550);
bm.addSafeZone("main_area", safeZone);

// Add restricted zone (where agents can't go)
Zone obstacle = Zone.createCircle("obstacle", new Point2D(400, 300), 50);
bm.addRestrictedZone("obstacle1", obstacle);

// Check if position is valid
Point2D agentPos = agent.getState().position;
if (!bm.isPositionValid(agentPos)) {
    System.out.println("Agent is in invalid location!");

    // Get nearest safe point
    Point2D safePoint = bm.getNearestSafePoint(agentPos);
    // Move agent to safe point
}

// Enforcement modes:
bm.setEnforcementMode(EnforcementMode.SOFT);      // Warning only
bm.setEnforcementMode(EnforcementMode.MEDIUM);    // Gently push back
bm.setEnforcementMode(EnforcementMode.HARD);      // Stop at boundary
bm.setEnforcementMode(EnforcementMode.TELEPORT);  // Instant relocation
```

### 4. PerformanceMonitor - System Optimization

**Real-world analogy:** Car dashboard showing RPM, temperature, oil pressure.

```java
// Get the performance monitor (singleton)
PerformanceMonitor pm = PerformanceMonitor.getInstance();
pm.setEventBus(eventBus);
pm.setAutoOptimize(true);  // Automatically optimize when needed

// In your game loop:
while (running) {
    pm.startFrame();  // Mark frame start

    // Do all your updates
    agentManager.updateAll(deltaTime);

    pm.endFrame();  // Mark frame end

    // Check performance
    if (pm.isPerformanceCritical()) {
        System.out.println("⚠️  CRITICAL: System struggling!");
    }
}

// Get detailed metrics
PerformanceMonitor.PerformanceMetrics metrics = pm.getCurrentMetrics();
System.out.println("Current FPS: " + metrics.currentFPS);
System.out.println("Average FPS: " + metrics.averageFPS);
System.out.println("Memory: " + (metrics.memoryUsagePercent * 100) + "%");
System.out.println("Status: " + metrics.status);

// Get full report
System.out.println(pm.getPerformanceReport());
```

---

## Putting It All Together

### Complete Example: Mini Swarm System

```java
public class MiniSwarmSystem {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Starting Swarm System ===\n");

        // 1. Initialize the system
        SystemController controller = new SystemController();
        controller.initialize();
        System.out.println("✓ System initialized");

        // 2. Get references
        EventBus eventBus = controller.getEventBus();
        AgentManager agentManager = controller.getAgentManager();
        BoundaryManager boundaryManager = BoundaryManager.getInstance();
        PerformanceMonitor perfMonitor = PerformanceMonitor.getInstance();

        // 3. Set up boundaries
        boundaryManager.setWorldBounds(0, 0, 800, 600);
        System.out.println("✓ Boundaries set: 800x600");

        // 4. Subscribe to events
        eventBus.subscribe(AgentStateUpdate.class, update -> {
            AgentState state = update.agentState;
            System.out.printf("[Update] Agent %d at (%.0f, %.0f), Battery: %.0f%%\n",
                update.agentId, state.position.x, state.position.y,
                state.batteryLevel * 100);
        });

        eventBus.subscribe(SystemEvent.class, event -> {
            if (event.getSeverity() == SystemEvent.Severity.ERROR) {
                System.err.println("[Event] ❌ " + event.getMessage());
            }
        });

        System.out.println("✓ Event subscribers registered\n");

        // 5. Create agents
        System.out.println("=== Creating Agents ===");
        for (int i = 0; i < 3; i++) {
            Point2D pos = new Point2D(
                100 + (i * 250),
                300
            );
            Agent agent = agentManager.createAgent(pos);
            System.out.println("Created Agent_" + agent.getState().agentId +
                " at (" + pos.x + ", " + pos.y + ")");
        }

        // 6. Give them tasks
        System.out.println("\n=== Assigning Tasks ===");
        List<AgentState> allAgents = agentManager.getAllAgentStates();
        for (AgentState state : allAgents) {
            // Create move command
            MovementCommand cmd = new MovementCommand(
                state.agentId,
                MovementType.MOVE_TO_TARGET,
                CommandPriority.NORMAL
            );
            Point2D target = new Point2D(
                400,  // All move toward center
                300
            );
            cmd.addParameter("target", target);

            // Send to agent
            Agent agent = agentManager.getAgent(state.agentId);
            agent.addMovementCommand(cmd);
            System.out.println("Agent_" + state.agentId +
                " commanded to move to (400, 300)");
        }

        // 7. Start simulation
        System.out.println("\n=== Starting Simulation ===");
        controller.start();
        System.out.println("✓ Simulation running at 60 FPS\n");

        // 8. Monitor for 10 seconds
        System.out.println("=== Monitoring (10 seconds) ===");
        for (int i = 1; i <= 10; i++) {
            Thread.sleep(1000);

            PerformanceMonitor.PerformanceMetrics metrics =
                perfMonitor.getCurrentMetrics();

            System.out.printf("[%ds] FPS: %.1f, Memory: %.1f%%, Agents: %d/%d\n",
                i, metrics.currentFPS, metrics.memoryUsagePercent * 100,
                metrics.activeAgents, metrics.totalAgents);
        }

        // 9. Stop simulation
        System.out.println("\n=== Stopping Simulation ===");
        controller.stop();

        // 10. Final report
        System.out.println("\n=== Final Report ===");
        for (AgentState state : agentManager.getAllAgentStates()) {
            System.out.printf("Agent_%d: Final position (%.0f, %.0f), Battery: %.0f%%\n",
                state.agentId, state.position.x, state.position.y,
                state.batteryLevel * 100);
        }

        System.out.println("\n" + perfMonitor.getPerformanceReport());
        System.out.println("=== System Shutdown Complete ===");
    }
}
```

---

## Common Patterns

### Pattern 1: Creating and Managing Agents

```java
// 1. Create manager
AgentManager manager = new AgentManager(eventBus);

// 2. Spawn agents
Agent agent1 = manager.createAgent(new Point2D(0, 0));
Agent agent2 = manager.createAgent(new Point2D(100, 0));

// 3. Get agent by ID
Agent agent = manager.getAgent(1);

// 4. Get all agents
List<AgentState> all = manager.getAllAgentStates();

// 5. Remove agent
manager.removeAgent(1);
```

### Pattern 2: Event Communication

```java
// 1. Subscribe
eventBus.subscribe(AgentStateUpdate.class, update -> {
    // Handle update
});

// 2. Publish
AgentStateUpdate update = new AgentStateUpdate();
update.agentId = 1;
update.agentState = agent.getState();
eventBus.publish(update);

// 3. Unsubscribe (if needed)
eventBus.unsubscribe(AgentStateUpdate.class, listener);
```

### Pattern 3: Moving Agents

```java
// 1. Create command
MovementCommand cmd = new MovementCommand(
    agentId,
    MovementType.MOVE_TO_TARGET,
    CommandPriority.NORMAL
);

// 2. Add parameters
cmd.addParameter("target", new Point2D(500, 300));

// 3. Send to agent
agent.addMovementCommand(cmd);

// 4. Agent processes automatically in update loop
```

### Pattern 4: Task Management

```java
// 1. Create task
Task task = Task.createMoveTask("task1", targetPoint);

// 2. Assign
task.assignTo(agentId);

// 3. Start
task.markInProgress();

// 4. Update progress
task.updateProgress(0.5);  // 50%

// 5. Complete
task.markCompleted();
```

---

## Glossary

**Agent** - Individual autonomous entity (like a robot or drone)

**AgentState** - Snapshot of an agent's properties at one moment

**EventBus** - Message routing system (publish-subscribe pattern)

**DTO (Data Transfer Object)** - Simple object that just holds data

**Lambda Expression** - Shorthand way to write small functions (the `->` syntax)

**Generic Type** - Code that works with any type (the `<T>` syntax)

**Consumer** - Function that takes input but returns nothing

**Singleton** - Class that has only one instance for the whole program

**Thread-Safe** - Safe to use from multiple threads simultaneously

**FPS (Frames Per Second)** - How many times per second the simulation updates

**deltaTime** - Time elapsed since last frame (in seconds)

**Magnitude** - Length of a vector (the "speed" part of velocity)

**Normalize** - Convert vector to length 1 (keep direction, lose speed)

**Steering Behavior** - Algorithm for intelligent movement (seek, flee, etc.)

**Pub-Sub** - Publish-Subscribe pattern (post messages, others receive)

---

## Tips for Success

1. **Start Simple** - Begin with one agent, then add more
2. **Use Print Statements** - Debug by printing what's happening
3. **Read Error Messages** - They tell you what's wrong!
4. **Test One Thing at a Time** - Don't change everything at once
5. **Use the Examples** - Copy and modify the examples above
6. **Ask Questions** - If confused, refer back to this guide

---

## Next Steps

1. ✅ Read this guide
2. ✅ Run SimpleTest.java
3. ✅ Try the MiniSwarmSystem example
4. ✅ Create your own simple simulation
5. ✅ Read specific class documentation for details
6. ✅ Experiment and have fun!

---

**Remember:** Every expert was once a beginner. Take your time, practice, and you'll understand it all!

Good luck! 🚀

---
