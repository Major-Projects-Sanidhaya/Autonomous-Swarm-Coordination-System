# Autonomous Swarm Coordination System - Contributor Guide

**Project:** Distributed Multi-Agent Swarm Coordination System
**Module:** Core Agent System (`com.team6.swarm.core`)
**Version:** 1.0 (Weeks 1-6 Complete)
**Last Updated:** 2025-10-30

---

## Table of Contents

- [Introduction](#introduction)
- [Project Architecture](#project-architecture)
- [Development Workflow](#development-workflow)
- [Codebase Structure](#codebase-structure)
- [Contributing Guidelines](#contributing-guidelines)
- [Design Principles](#design-principles)
- [Code Standards](#code-standards)
- [Testing Requirements](#testing-requirements)
- [Common Development Tasks](#common-development-tasks)
- [Troubleshooting](#troubleshooting)
- [Resources](#resources)

---

## Introduction

### Project Overview

The Autonomous Swarm Coordination System is an enterprise-grade platform for managing and coordinating multiple autonomous agents in real-time. The system enables sophisticated multi-agent behaviors including formation flying, task allocation, collision avoidance, and dynamic boundary management.

**Key Features:**
- Real-time agent coordination (30-60 FPS)
- Event-driven architecture with publish-subscribe pattern
- Realistic physics simulation with multiple boundary modes
- Advanced performance monitoring and auto-optimization
- Modular design supporting 100+ concurrent agents
- Thread-safe operations for multi-threaded environments

### Module Responsibilities

| Module | Team Lead | Responsibility |
|--------|-----------|----------------|
| **Core** | Sanidhaya | Agent lifecycle, physics engine, system orchestration, performance monitoring |
| **Communication** | John | Network layer, agent-to-agent messaging, range-based communication |
| **Intelligence** | Lauren | AI behaviors, task allocation, flocking algorithms, path planning |
| **UI** | Anthony | Visualization, user input handling, real-time rendering |

### Technology Stack

- **Language:** Java 8+
- **Concurrency:** `java.util.concurrent` (ConcurrentHashMap, CopyOnWriteArrayList, PriorityBlockingQueue)
- **Architecture:** Event-driven, Observer pattern, Singleton pattern, Factory pattern
- **Build Tool:** Maven/Gradle (project dependent)
- **Testing:** JUnit 5

---

## Project Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    SystemController                          │
│  • System lifecycle management                               │
│  • Component orchestration                                   │
│  • Event coordination                                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┬─────────────┐
        │               │               │             │
┌───────▼────────┐ ┌───▼─────────┐ ┌──▼──────┐ ┌────▼────────┐
│ AgentManager   │ │  EventBus   │ │ Physics │ │  Boundary   │
│                │ │             │ │ Engine  │ │  Manager    │
│• Agent CRUD    │ │• Pub-Sub    │ │• Motion │ │• Spatial    │
│• Lifecycle     │ │• Routing    │ │• Forces │ │  Constraints│
│• Simulation    │ │• Filtering  │ │• Bounds │ │• Zones      │
└───────┬────────┘ └───┬─────────┘ └─────────┘ └─────────────┘
        │              │
        │              │
┌───────▼────────────┐ │
│   Agent Pool       │◄┘
│                    │
│  ┌──────────────┐  │
│  │ Agent        │  │
│  │ • State      │  │
│  │ • Commands   │  │
│  │ • Physics    │  │
│  │ • Tasks      │  │
│  └──────────────┘  │
└────────────────────┘

Additional Services:
• PerformanceMonitor → System-wide metrics
• BoundaryManager → Spatial management
• Task System → Work assignment
• Capabilities → Performance assessment
```

### Event Flow Architecture

```
Publisher (Agent)
    │
    │ publish(AgentStateUpdate)
    ▼
EventBus (Central Hub)
    │
    ├─► Subscriber 1 (John's Communication)
    ├─► Subscriber 2 (Lauren's Intelligence)
    ├─► Subscriber 3 (Anthony's UI)
    └─► Subscriber 4 (SystemController)
```

### Module Integration Points

**Core → Communication:**
- `AgentStateUpdate` events published for position changes
- `CommunicationEvent` events received for inter-agent messages

**Core → Intelligence:**
- `MovementCommand` events received for agent control
- `TaskCompletionReport` events published for feedback
- `AgentCapabilities` data provided for task allocation

**Core → UI:**
- `VisualizationUpdate` data stream for rendering
- `SystemMetrics` data for performance displays
- `SystemEvent` notifications for user feedback
- `SystemCommand` events received for user actions

---

## Development Workflow

### Initial Setup

1. **Repository Setup**
   ```bash
   git clone <repository-url>
   cd Autonomous-Swarm-Coordination-System
   ```

2. **IDE Configuration**
   - **IntelliJ IDEA:** Import as Maven/Gradle project
   - **Eclipse:** Import existing Maven/Gradle project
   - **VS Code:** Install Java Extension Pack

3. **Build Verification**
   ```bash
   mvn clean compile
   # or
   gradle build
   ```

4. **Run Tests**
   ```bash
   mvn test
   # or
   gradle test
   ```

### Branch Strategy

```
main (production-ready)
  ├── develop (integration branch)
  │     ├── feature/agent-collision-detection
  │     ├── feature/adaptive-performance
  │     ├── bugfix/boundary-enforcement
  │     └── enhancement/capability-metrics
```

**Branch Naming Conventions:**
- `feature/<descriptive-name>` - New features
- `bugfix/<issue-number>-<description>` - Bug fixes
- `enhancement/<descriptive-name>` - Improvements to existing features
- `refactor/<component-name>` - Code refactoring
- `docs/<what-changed>` - Documentation updates

### Contribution Process

1. **Create Feature Branch**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Implement Changes**
   - Write code following standards (see [Code Standards](#code-standards))
   - Add comprehensive JavaDoc comments
   - Include unit tests
   - Update relevant documentation

3. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: Add agent collision prediction algorithm

   - Implemented predictive collision detection
   - Added configurable look-ahead time parameter
   - Integrated with PhysicsEngine
   - Added unit tests covering edge cases

   Refs: #123"
   ```

4. **Push and Create Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```
   - Create PR on GitHub/GitLab
   - Fill out PR template
   - Request review from team lead

5. **Code Review Process**
   - Address reviewer comments
   - Make requested changes
   - Update PR

6. **Merge**
   - Squash commits if needed
   - Merge to develop branch
   - Delete feature branch

---

## Codebase Structure

### Package Organization

```
com.team6.swarm.core/
├── Agent.java                    # Individual agent implementation
├── AgentManager.java             # Agent lifecycle management
├── AgentState.java               # Agent data snapshot (DTO)
├── AgentStatus.java              # Agent state enum
├── AgentCapabilities.java        # Performance assessment
│
├── Point2D.java                  # 2D position representation
├── Vector2D.java                 # 2D vector mathematics
│
├── EventBus.java                 # Publish-subscribe hub
├── AgentStateUpdate.java         # Position update events
├── CommunicationEvent.java       # Inter-agent messages
│
├── SystemController.java         # System orchestration
├── SystemMetrics.java            # Performance metrics
├── SystemEvent.java              # System notifications
├── SystemCommand.java            # User commands
│
├── MovementCommand.java          # Agent movement instructions
├── MovementType.java             # Movement command types
├── CommandPriority.java          # Command priority levels
│
├── PhysicsEngine.java            # Physics simulation
├── BoundaryManager.java          # Spatial constraints
├── PerformanceMonitor.java       # System performance tracking
│
├── Task.java                     # Work assignment
├── TaskCompletionReport.java     # Task feedback
│
├── VisualizationUpdate.java      # UI data package
├── CommandType.java              # User command types
│
└── docs/                         # Documentation
    ├── Week1_Point2D.md
    ├── Week1_Vector2D.md
    ├── Week1_AgentStatus.md
    ├── Week1_AgentState.md
    ├── Week2_EventBus.md
    ├── COMPLETE_API_REFERENCE.md
    ├── QUICK_START_GUIDE.md
    ├── BEGINNER_FRIENDLY_COMPLETE_GUIDE.md
    └── CONTRIBUTOR_GUIDE.md (this file)
```

### Class Categories

**Data Structures (2):**
- `Point2D` - Immutable 2D coordinate
- `Vector2D` - Immutable 2D vector with mathematical operations

**Core Entities (5):**
- `Agent` - Active agent entity with update loop
- `AgentState` - Agent data snapshot (DTO)
- `AgentManager` - Agent pool management
- `AgentCapabilities` - Performance metrics
- `Task` - Work assignment representation

**System Services (6):**
- `SystemController` - Main system coordinator
- `EventBus` - Message routing infrastructure
- `PhysicsEngine` - Physics simulation
- `BoundaryManager` - Spatial constraint management (Singleton)
- `PerformanceMonitor` - System health monitoring (Singleton)
- `SystemMetrics` - Performance data aggregation

**Communication (4):**
- `AgentStateUpdate` - Position change notification
- `CommunicationEvent` - Agent-to-agent message
- `SystemEvent` - System-wide notification
- `SystemCommand` - User input command

**Movement (4):**
- `MovementCommand` - Agent movement instruction
- `MovementType` - Command type enumeration
- `CommandPriority` - Priority level enumeration
- `TaskCompletionReport` - Task execution feedback

**UI Integration (3):**
- `VisualizationUpdate` - Rendering data package
- `SystemMetrics` - Performance display data
- `CommandType` - User command enumeration

---

## Contributing Guidelines

### Finding Work

**Issue Tracking:**
- Check project board for open issues
- Look for `good-first-issue` labels for beginners
- Check `help-wanted` labels for priority items

**Task Categories:**
- **Feature Development:** Implementing new functionality
- **Bug Fixes:** Resolving defects
- **Refactoring:** Improving code quality
- **Testing:** Adding test coverage
- **Documentation:** Updating docs
- **Performance:** Optimization work

### Making Changes

#### 1. Understanding Requirements

Before coding:
- Read the issue description thoroughly
- Review acceptance criteria
- Check existing related code
- Ask clarifying questions if needed

#### 2. Locating Relevant Code

**Finding the right file:**
```java
// Use file headers to understand purpose
/**
 * AGENT CLASS - Individual Swarm Entity (Week 3 Complete)
 *
 * PURPOSE:
 * - Represents single autonomous agent in swarm system
 * ...
 */
```

**Understanding dependencies:**
- Check imports to see relationships
- Read class-level JavaDoc
- Look for integration points section in docs

#### 3. Following Existing Patterns

**Example: Adding a method to Agent.java**

```java
// Existing pattern in Agent.java
public void addMovementCommand(MovementCommand command) {
    if (command.agentId != state.agentId) {
        System.err.println("Warning: Command mismatch");
        return;
    }
    commandQueue.offer(command);
}

// Your new method following the same pattern
/**
 * Checks if agent can accept a new command based on queue capacity
 * @param maxQueueSize Maximum allowed queue size
 * @return true if queue has capacity, false otherwise
 */
public boolean canAcceptCommand(int maxQueueSize) {
    if (commandQueue.size() >= maxQueueSize) {
        System.err.println("Warning: Command queue full for agent " + state.agentId);
        return false;
    }
    return true;
}
```

#### 4. Writing Tests

**Unit Test Structure:**
```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class AgentTest {
    private Agent agent;
    private Point2D startPosition;

    @BeforeEach
    public void setUp() {
        startPosition = new Point2D(0, 0);
        agent = new Agent(1, startPosition);
    }

    @Test
    public void testCanAcceptCommand_WithinCapacity() {
        // Given: Empty command queue
        // When: Check if can accept with max size 10
        boolean result = agent.canAcceptCommand(10);

        // Then: Should return true
        assertTrue(result, "Agent should accept commands when queue is empty");
    }

    @Test
    public void testCanAcceptCommand_AtCapacity() {
        // Given: Full command queue
        for (int i = 0; i < 10; i++) {
            MovementCommand cmd = new MovementCommand(1,
                MovementType.MOVE_TO_TARGET, CommandPriority.NORMAL);
            agent.addMovementCommand(cmd);
        }

        // When: Check if can accept with max size 10
        boolean result = agent.canAcceptCommand(10);

        // Then: Should return false
        assertFalse(result, "Agent should not accept commands when queue is full");
    }
}
```

#### 5. Documentation Updates

**JavaDoc Requirements:**
```java
/**
 * Brief one-line description of what the method does
 *
 * <p>More detailed explanation if needed, including:
 * <ul>
 *   <li>Algorithm description</li>
 *   <li>Performance characteristics</li>
 *   <li>Thread safety considerations</li>
 *   <li>Usage examples</li>
 * </ul>
 *
 * @param paramName Description of parameter including valid ranges
 * @return Description of return value including special cases
 * @throws ExceptionType Description of when exception is thrown
 * @see RelatedClass#relatedMethod()
 * @since 1.1
 */
public ReturnType methodName(ParamType paramName) {
    // Implementation
}
```

**Class Documentation:**
```java
/**
 * CLASSNAME - Brief Purpose (Week N)
 *
 * PURPOSE:
 * - Primary responsibility
 * - Secondary responsibility
 *
 * DESIGN PATTERNS:
 * - Pattern 1: How it's used
 * - Pattern 2: How it's used
 *
 * INTEGRATION POINTS:
 * - Used by: ClassA, ClassB
 * - Uses: ClassC, ClassD
 *
 * THREAD SAFETY:
 * - Thread-safe operations: method1, method2
 * - Requires synchronization: method3
 *
 * PERFORMANCE:
 * - Time complexity: O(n)
 * - Space complexity: O(1)
 *
 * @author Your Name
 * @version 1.0
 * @since Week N
 */
public class ClassName {
    // Implementation
}
```

---

## Design Principles

### 1. Single Responsibility Principle

Each class has one clear purpose:
```java
// ✅ Good: Clear single responsibility
public class AgentManager {
    // Manages agent lifecycle only
    public Agent createAgent(Point2D position) { }
    public void removeAgent(int id) { }
    public Agent getAgent(int id) { }
}

// ❌ Bad: Multiple responsibilities
public class AgentManager {
    public Agent createAgent(Point2D position) { }
    public void updatePhysics(double deltaTime) { }  // Should be in PhysicsEngine
    public void renderAgent(Graphics g) { }          // Should be in UI
}
```

### 2. Event-Driven Architecture

Use EventBus for loose coupling:
```java
// ✅ Good: Loose coupling via events
public class Agent {
    private EventBus eventBus;

    private void publishStateUpdate() {
        AgentStateUpdate update = new AgentStateUpdate();
        update.agentState = state;
        eventBus.publish(update);  // Anyone can subscribe
    }
}

// ❌ Bad: Tight coupling with direct calls
public class Agent {
    private CommunicationManager commManager;
    private IntelligenceManager intelManager;
    private UIManager uiManager;

    private void notifyStateChange() {
        commManager.handleUpdate(state);  // Knows about everyone
        intelManager.handleUpdate(state);
        uiManager.handleUpdate(state);
    }
}
```

### 3. Immutability for Data Objects

```java
// ✅ Good: Immutable data structures
public class Point2D {
    public final double x;
    public final double y;

    public Point2D add(Vector2D vector) {
        return new Point2D(x + vector.x, y + vector.y);  // Returns new instance
    }
}

// ❌ Bad: Mutable data can cause bugs
public class Point2D {
    public double x;
    public double y;

    public void add(Vector2D vector) {
        x += vector.x;  // Modifies in place - dangerous!
        y += vector.y;
    }
}
```

### 4. Thread Safety

```java
// ✅ Good: Thread-safe collections
public class AgentManager {
    private final Map<Integer, Agent> agents = new ConcurrentHashMap<>();

    public void addAgent(Agent agent) {
        agents.put(agent.getId(), agent);  // Thread-safe
    }
}

// ✅ Good: Synchronized access
public class PerformanceMonitor {
    private volatile boolean running;

    public synchronized void start() {
        running = true;
    }
}
```

### 5. Defensive Programming

```java
// ✅ Good: Validate inputs
public void setMaxSpeed(double maxSpeed) {
    if (maxSpeed <= 0) {
        throw new IllegalArgumentException("Max speed must be positive");
    }
    if (maxSpeed > MAX_ALLOWED_SPEED) {
        throw new IllegalArgumentException("Max speed exceeds limit: " + MAX_ALLOWED_SPEED);
    }
    this.maxSpeed = maxSpeed;
}

// ✅ Good: Check for null
public void addMovementCommand(MovementCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    commandQueue.offer(command);
}
```

---

## Code Standards

### Naming Conventions

```java
// Classes and Interfaces: PascalCase
public class AgentManager { }
public interface Updatable { }

// Methods: camelCase, verb-based
public void updatePosition() { }
public boolean isActive() { }
public AgentState getState() { }

// Variables: camelCase
private int agentCount;
private double batteryLevel;

// Constants: UPPER_SNAKE_CASE
public static final double MAX_SPEED = 100.0;
public static final int DEFAULT_CAPACITY = 50;

// Packages: lowercase
package com.team6.swarm.core;

// Enum values: UPPER_SNAKE_CASE
public enum AgentStatus {
    ACTIVE,
    BATTERY_LOW,
    FAILED
}
```

### Code Organization

```java
public class ClassName {
    // 1. Static constants
    public static final double CONSTANT = 1.0;
    private static final int PRIVATE_CONSTANT = 2;

    // 2. Static fields
    private static int instanceCount = 0;

    // 3. Instance fields (by visibility)
    public final int publicField;
    protected double protectedField;
    private String privateField;

    // 4. Constructors (ordered by parameter count)
    public ClassName() { }
    public ClassName(int param) { }
    public ClassName(int param1, String param2) { }

    // 5. Static factory methods (if any)
    public static ClassName create() { }

    // 6. Public methods (grouped by functionality)
    public void update() { }
    public void render() { }

    // 7. Protected methods
    protected void internalMethod() { }

    // 8. Private methods
    private void helperMethod() { }

    // 9. Getters and setters
    public int getField() { return publicField; }
    public void setField(int value) { this.privateField = value; }

    // 10. Inner classes/enums
    private static class InnerClass { }
    public enum InnerEnum { }
}
```

### Formatting Standards

```java
// Indentation: 4 spaces (no tabs)
public void method() {
    if (condition) {
        doSomething();
    }
}

// Braces: K&R style (opening brace on same line)
if (condition) {
    // code
} else {
    // code
}

// Line length: Maximum 120 characters
// Break long lines at logical points
public void methodWithManyParameters(
        int param1,
        String param2,
        double param3) {
    // Implementation
}

// Blank lines: Use to separate logical sections
public class Example {
    private int field1;
    private int field2;

    public Example() { }  // Blank line before constructor

    public void method1() { }  // Blank line between methods

    public void method2() { }
}

// Whitespace: Use consistently
int x = 5 + 3;  // ✅ Spaces around operators
int x=5+3;      // ❌ No spaces

method(a, b, c);  // ✅ Space after comma
method(a,b,c);    // ❌ No space after comma
```

### Error Handling

```java
// ✅ Good: Specific exception types
public void processAgent(int agentId) throws AgentNotFoundException {
    Agent agent = agents.get(agentId);
    if (agent == null) {
        throw new AgentNotFoundException("Agent not found: " + agentId);
    }
    agent.update();
}

// ✅ Good: Logging with context
try {
    updatePhysics(deltaTime);
} catch (PhysicsException e) {
    logger.error("Physics update failed for agent " + agentId, e);
    handlePhysicsError(e);
}

// ✅ Good: Fail fast
public void setVelocity(Vector2D velocity) {
    Objects.requireNonNull(velocity, "Velocity cannot be null");
    if (!isValidVelocity(velocity)) {
        throw new IllegalArgumentException("Invalid velocity: " + velocity);
    }
    this.velocity = velocity;
}

// ❌ Bad: Silent failures
try {
    doSomething();
} catch (Exception e) {
    // Do nothing - error is hidden!
}

// ❌ Bad: Generic exceptions
public void processAgent() throws Exception {  // Too generic!
    // ...
}
```

---

## Testing Requirements

### Test Coverage Expectations

- **Minimum:** 70% line coverage
- **Target:** 85% line coverage
- **Critical paths:** 100% coverage

### Test Structure

```java
@DisplayName("Agent - Individual Swarm Entity Tests")
public class AgentTest {
    private Agent agent;
    private EventBus mockEventBus;
    private Point2D startPosition;

    @BeforeEach
    void setUp() {
        mockEventBus = mock(EventBus.class);
        startPosition = new Point2D(100, 100);
        agent = new Agent(1, startPosition);
        agent.setEventBus(mockEventBus);
    }

    @Nested
    @DisplayName("Movement Tests")
    class MovementTests {
        @Test
        @DisplayName("Should update position based on velocity")
        void testPositionUpdate() {
            // Given
            agent.getState().velocity = new Vector2D(10, 0);
            Point2D initialPosition = agent.getState().position;

            // When
            agent.update(1.0);  // 1 second

            // Then
            Point2D newPosition = agent.getState().position;
            assertEquals(initialPosition.x + 10, newPosition.x, 0.01);
            assertEquals(initialPosition.y, newPosition.y, 0.01);
        }

        @Test
        @DisplayName("Should limit velocity to max speed")
        void testVelocityLimit() {
            // Given
            double maxSpeed = 50.0;
            agent.getState().maxSpeed = maxSpeed;
            agent.getState().velocity = new Vector2D(100, 100);  // Exceeds limit

            // When
            agent.update(0.1);

            // Then
            double actualSpeed = agent.getState().velocity.magnitude();
            assertTrue(actualSpeed <= maxSpeed,
                "Speed should not exceed max speed");
        }
    }

    @Nested
    @DisplayName("Command Processing Tests")
    class CommandTests {
        @Test
        @DisplayName("Should process commands by priority")
        void testCommandPriority() {
            // Given
            MovementCommand lowPriority = new MovementCommand(
                1, MovementType.MOVE_TO_TARGET, CommandPriority.LOW);
            MovementCommand highPriority = new MovementCommand(
                1, MovementType.MOVE_TO_TARGET, CommandPriority.HIGH);

            // When
            agent.addMovementCommand(lowPriority);
            agent.addMovementCommand(highPriority);

            // Then
            // High priority should be processed first
            // (Verify through state changes or mock verification)
        }
    }

    @Nested
    @DisplayName("Battery Management Tests")
    class BatteryTests {
        @Test
        @DisplayName("Should drain battery during movement")
        void testBatteryDrain() {
            // Given
            double initialBattery = 1.0;
            agent.getState().batteryLevel = initialBattery;
            agent.getState().velocity = new Vector2D(50, 50);

            // When
            agent.update(10.0);  // 10 seconds

            // Then
            assertTrue(agent.getState().batteryLevel < initialBattery,
                "Battery should drain during movement");
        }

        @Test
        @DisplayName("Should change status when battery is low")
        void testLowBatteryStatus() {
            // Given
            agent.getState().batteryLevel = 0.15;  // 15%

            // When
            agent.update(0.1);

            // Then
            assertEquals(AgentStatus.BATTERY_LOW, agent.getState().status,
                "Status should change to BATTERY_LOW");
        }
    }
}
```

### Test Categories

**1. Unit Tests**
- Test individual methods in isolation
- Use mocks for dependencies
- Fast execution (<100ms per test)

**2. Integration Tests**
- Test multiple components together
- Verify event flows
- Test complete workflows

**3. Performance Tests**
- Verify scalability (100+ agents)
- Check FPS under load
- Monitor memory usage

**4. Edge Case Tests**
- Null inputs
- Boundary conditions
- Invalid states

---

## Common Development Tasks

### Task 1: Adding a New Method

**Requirement:** Add method to check if agent reached target

**Implementation:**
```java
/**
 * Checks if the agent has reached the specified target position within a threshold
 *
 * <p>This method is useful for determining task completion when an agent
 * is moving toward a specific destination. The threshold allows for slight
 * variations in final position.
 *
 * @param target The target position to check against
 * @param threshold The distance threshold for "reached" determination (must be positive)
 * @return true if agent is within threshold distance of target, false otherwise
 * @throws IllegalArgumentException if threshold is negative or zero
 * @since 1.1
 */
public boolean hasReachedTarget(Point2D target, double threshold) {
    if (threshold <= 0) {
        throw new IllegalArgumentException("Threshold must be positive: " + threshold);
    }
    if (target == null) {
        return false;
    }

    double distance = state.position.distanceTo(target);
    return distance <= threshold;
}
```

**Test:**
```java
@Test
@DisplayName("Should return true when agent is within threshold of target")
void testHasReachedTarget_WithinThreshold() {
    // Given
    Agent agent = new Agent(1, new Point2D(0, 0));
    Point2D target = new Point2D(3, 4);
    double threshold = 10.0;

    // When
    boolean reached = agent.hasReachedTarget(target, threshold);

    // Then
    assertTrue(reached, "Agent at (0,0) should be within 10 units of (3,4)");
}

@Test
@DisplayName("Should return false when agent is beyond threshold")
void testHasReachedTarget_BeyondThreshold() {
    // Given
    Agent agent = new Agent(1, new Point2D(0, 0));
    Point2D target = new Point2D(100, 100);
    double threshold = 10.0;

    // When
    boolean reached = agent.hasReachedTarget(target, threshold);

    // Then
    assertFalse(reached, "Agent at (0,0) should not be within 10 units of (100,100)");
}
```

### Task 2: Adding Event Handling

**Requirement:** Handle collision events in SystemController

**Implementation:**
```java
// 1. Define event class (if doesn't exist)
public class CollisionEvent {
    public final int agentId1;
    public final int agentId2;
    public final Point2D collisionPoint;
    public final long timestamp;

    public CollisionEvent(int agentId1, int agentId2, Point2D point) {
        this.agentId1 = agentId1;
        this.agentId2 = agentId2;
        this.collisionPoint = point;
        this.timestamp = System.currentTimeMillis();
    }
}

// 2. Add subscription in SystemController
private void registerEventSubscribers() {
    // Existing subscriptions...
    eventBus.subscribe(CollisionEvent.class, this::handleCollision);
}

private void handleCollision(CollisionEvent event) {
    logger.warn("Collision detected between Agent {} and Agent {} at position ({}, {})",
        event.agentId1, event.agentId2,
        event.collisionPoint.x, event.collisionPoint.y);

    // Record metric
    metrics.recordCollision();

    // Publish system event for UI
    SystemEvent sysEvent = SystemEvent.warning(
        SystemEvent.COLLISION_DETECTED,
        String.valueOf(event.agentId1),
        String.format("Collision with Agent %d at (%.1f, %.1f)",
            event.agentId2, event.collisionPoint.x, event.collisionPoint.y)
    );
    eventBus.publish(sysEvent);

    // Optional: Take corrective action
    resolveCollision(event.agentId1, event.agentId2);
}

private void resolveCollision(int agentId1, int agentId2) {
    Agent agent1 = agentManager.getAgent(agentId1);
    Agent agent2 = agentManager.getAgent(agentId2);

    if (agent1 != null && agent2 != null) {
        // Reverse velocities
        Vector2D vel1 = agent1.getState().velocity;
        Vector2D vel2 = agent2.getState().velocity;

        agent1.getState().velocity = vel1.multiply(-0.5);
        agent2.getState().velocity = vel2.multiply(-0.5);
    }
}

// 3. Publish collision event from PhysicsEngine
public void checkCollisions(List<AgentState> agents) {
    for (int i = 0; i < agents.size(); i++) {
        for (int j = i + 1; j < agents.size(); j++) {
            if (checkCollision(agents.get(i), agents.get(j))) {
                Point2D midpoint = calculateMidpoint(
                    agents.get(i).position,
                    agents.get(j).position
                );

                CollisionEvent event = new CollisionEvent(
                    agents.get(i).agentId,
                    agents.get(j).agentId,
                    midpoint
                );
                eventBus.publish(event);
            }
        }
    }
}
```

### Task 3: Performance Optimization

**Requirement:** Optimize agent update loop

**Before (O(n²) collision checks):**
```java
public void updateAll(double deltaTime) {
    // Update each agent
    for (Agent agent : agents.values()) {
        agent.update(deltaTime);
    }

    // Check all collisions (slow!)
    for (Agent a1 : agents.values()) {
        for (Agent a2 : agents.values()) {
            if (a1.getId() != a2.getId()) {
                checkCollision(a1, a2);
            }
        }
    }
}
```

**After (Spatial partitioning):**
```java
public void updateAll(double deltaTime) {
    // Update each agent
    for (Agent agent : agents.values()) {
        agent.update(deltaTime);
    }

    // Spatial partitioning for collision detection
    SpatialGrid grid = new SpatialGrid(WORLD_WIDTH, WORLD_HEIGHT, CELL_SIZE);

    // Insert agents into grid
    for (Agent agent : agents.values()) {
        grid.insert(agent);
    }

    // Check collisions only for nearby agents
    for (Agent agent : agents.values()) {
        List<Agent> nearby = grid.getNearby(agent);
        for (Agent other : nearby) {
            if (agent.getId() != other.getId()) {
                checkCollision(agent, other);
            }
        }
    }
}

// Spatial grid implementation
private static class SpatialGrid {
    private final int cellSize;
    private final Map<Point, List<Agent>> grid;

    public void insert(Agent agent) {
        Point cell = getCell(agent.getState().position);
        grid.computeIfAbsent(cell, k -> new ArrayList<>()).add(agent);
    }

    public List<Agent> getNearby(Agent agent) {
        Point cell = getCell(agent.getState().position);
        List<Agent> nearby = new ArrayList<>();

        // Check current cell and adjacent cells
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Point adjacentCell = new Point(cell.x + dx, cell.y + dy);
                List<Agent> cellAgents = grid.get(adjacentCell);
                if (cellAgents != null) {
                    nearby.addAll(cellAgents);
                }
            }
        }
        return nearby;
    }
}
```

---

## Troubleshooting

### Common Issues

**Issue: NullPointerException in Agent.update()**
```java
// Problem
AgentState state = agent.getState();
double x = state.position.x;  // NPE if position is null

// Solution
AgentState state = agent.getState();
if (state != null && state.position != null) {
    double x = state.position.x;
} else {
    logger.error("Invalid agent state for agent {}", agent.getId());
    return;
}
```

**Issue: ConcurrentModificationException**
```java
// Problem
for (Agent agent : agents.values()) {
    if (shouldRemove(agent)) {
        agents.remove(agent.getId());  // Modifying during iteration!
    }
}

// Solution 1: Use Iterator
Iterator<Agent> iterator = agents.values().iterator();
while (iterator.hasNext()) {
    Agent agent = iterator.next();
    if (shouldRemove(agent)) {
        iterator.remove();  // Safe removal
    }
}

// Solution 2: Collect and remove
List<Integer> toRemove = new ArrayList<>();
for (Agent agent : agents.values()) {
    if (shouldRemove(agent)) {
        toRemove.add(agent.getId());
    }
}
for (int id : toRemove) {
    agents.remove(id);
}
```

**Issue: Events not received by subscribers**
```java
// Problem: Subscribing after events are published
eventBus.publish(new AgentStateUpdate());  // Published first
eventBus.subscribe(AgentStateUpdate.class, this::handle);  // Subscribe too late!

// Solution: Subscribe before publishing
eventBus.subscribe(AgentStateUpdate.class, this::handle);  // Subscribe first
eventBus.publish(new AgentStateUpdate());  // Now it works
```

**Issue: Memory leak from event listeners**
```java
// Problem: Not unsubscribing
for (int i = 0; i < 1000; i++) {
    eventBus.subscribe(AgentStateUpdate.class, update -> {
        // This listener stays in memory forever!
    });
}

// Solution: Unsubscribe when done
Consumer<AgentStateUpdate> listener = update -> { /* handle */ };
eventBus.subscribe(AgentStateUpdate.class, listener);
// ... use it ...
eventBus.unsubscribe(AgentStateUpdate.class, listener);  // Clean up
```

### Debugging Tips

**Enable verbose logging:**
```java
// Add to agent update
public void update(double deltaTime) {
    if (DEBUG_MODE) {
        logger.debug("Agent {} updating: pos={}, vel={}, battery={}",
            state.agentId, state.position, state.velocity, state.batteryLevel);
    }
    // ... rest of update
}
```

**Use performance profiler:**
```bash
# Run with profiling enabled
java -agentlib:hprof=cpu=samples,depth=10 MyMainClass
```

**Check thread safety:**
```bash
# Run with thread sanitizer
java -XX:+ShowMessageBoxOnError -XX:+UnlockDiagnosticVMOptions MyMainClass
```

---

## Resources

### Documentation
- [Complete API Reference](COMPLETE_API_REFERENCE.md)
- [Quick Start Guide](QUICK_START_GUIDE.md)
- [Beginner-Friendly Guide](BEGINNER_FRIENDLY_COMPLETE_GUIDE.md)
- [Week-specific Documentation](.) (Week1_*.md, Week2_*.md, etc.)

### Design Patterns
- Observer Pattern: EventBus implementation
- Singleton Pattern: BoundaryManager, PerformanceMonitor
- Factory Pattern: Task creation methods
- Strategy Pattern: PhysicsEngine boundary modes

### External Resources
- [Java Concurrency in Practice](https://jcip.net/)
- [Effective Java (3rd Edition)](https://www.oreilly.com/library/view/effective-java-3rd/9780134686097/)
- [Clean Code](https://www.oreilly.com/library/view/clean-code/9780136083238/)

### Team Contacts
- **Core Team Lead:** Sanidhaya
- **Communication Team:** John
- **Intelligence Team:** Lauren
- **UI Team:** Anthony

---

## Summary

This guide provides comprehensive information for contributing to the Autonomous Swarm Coordination System's core module. Key points:

1. **Understand** the architecture and design principles
2. **Follow** coding standards and best practices
3. **Test** thoroughly with comprehensive coverage
4. **Document** all changes with clear JavaDoc
5. **Collaborate** through code reviews and team communication

**Quality over speed.** Write maintainable, well-tested code that integrates seamlessly with the existing system.

---

**Version:** 1.0
**Last Updated:** 2025-10-30
**Maintained by:** Core Team Lead

---
