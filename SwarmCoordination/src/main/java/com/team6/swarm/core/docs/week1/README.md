# Week 1: Foundation Files

## Purpose
Create the fundamental building blocks that everyone will use.

**Goal**: Create agents that can be instantiated and move in 2D space.

## Success Criteria
- Run SimpleTest and see 3 agents moving around
- Agents have positions that change over time
- No crashes, basic movement works

## Files Overview

### 1. Point2D.java
**Package**: `com.team6.swarm.core`

**Purpose**: Represents X,Y coordinates in 2D space

**Why needed**: Every agent has a position, every target has coordinates

**Used by**: All team members for positions and locations

**Key responsibility**: Store coordinates, calculate distances between points

**Key Methods**:
- Constructor with x, y coordinates
- `distanceTo(Point2D other)` - Calculate distance between two points
- `getX()`, `getY()` - Accessor methods

---

### 2. Vector2D.java
**Package**: `com.team6.swarm.core`

**Purpose**: Represents direction and magnitude (like velocity or force)

**Why needed**: Agents move with velocity, Lauren calculates movement forces

**Used by**: Physics calculations, flocking algorithms, movement commands

**Key responsibility**: Vector math operations (add, multiply, normalize)

**Key Methods**:
- Constructor with dx, dy components
- `add(Vector2D other)` - Vector addition
- `multiply(double scalar)` - Scalar multiplication
- `normalize()` - Convert to unit vector
- `magnitude()` - Get vector length

---

### 3. AgentStatus.java (enum)
**Package**: `com.team6.swarm.core`

**Purpose**: Define all possible states an agent can be in

**Why needed**: System needs to track if agents are working, failed, low battery

**Used by**: All components to check agent health

**Values**:
- `ACTIVE` - Agent is functioning normally
- `INACTIVE` - Agent is temporarily disabled
- `FAILED` - Agent has encountered an error
- `BATTERY_LOW` - Agent needs recharging
- `MAINTENANCE` - Agent is under maintenance

---

### 4. AgentState.java
**Package**: `com.team6.swarm.core`

**Purpose**: Complete snapshot of an agent at any moment

**Why needed**: This is the "passport" of each agent - contains all info others need

**Used by**: Everyone needs to know agent position, status, capabilities

**Key responsibility**: Store agent ID, position, velocity, battery, status, capabilities

**Key Fields**:
- `agentId` - Unique identifier
- `position` - Current Point2D location
- `velocity` - Current Vector2D movement
- `batteryLevel` - Current battery percentage (0-100)
- `status` - Current AgentStatus
- `capabilities` - List of agent capabilities

---

### 5. Agent.java
**Package**: `com.team6.swarm.core`

**Purpose**: The actual agent - can move, receive commands, update itself

**Why needed**: This is your main "robot" - it executes Lauren's movement commands

**Used by**: You create them, Lauren commands them, John gets their positions

**Key responsibility**: Update position every frame, process movement commands, manage battery

**Key Methods**:
- `update(double deltaTime)` - Update agent state each frame
- `setVelocity(Vector2D velocity)` - Set movement velocity
- `getState()` - Get current AgentState snapshot
- `consumeBattery(double amount)` - Reduce battery level

---

### 6. AgentManager.java
**Package**: `com.team6.swarm.core`

**Purpose**: Factory and registry for all agents - creates, tracks, removes agents

**Why needed**: Someone needs to manage the swarm (create/destroy agents)

**Used by**: Anthony sends spawn commands here, everyone queries for agent lists

**Key responsibility**: Create agents on command, track all agents, provide agent lists

**Key Methods**:
- `createAgent(Point2D initialPosition)` - Create new agent
- `removeAgent(String agentId)` - Remove agent from system
- `getAllAgents()` - Get list of all active agents
- `getAgent(String agentId)` - Get specific agent by ID
- `updateAll(double deltaTime)` - Update all agents

**Thread Safety**: Uses `ConcurrentHashMap` for agent storage

---

### 7. SimpleTest.java
**Package**: `com.team6.swarm.core`

**Purpose**: Test your basic agent system works

**Why needed**: Verify agents can be created and move before integrating with others

**Used by**: Just you for testing

**Key responsibility**: Create 3 agents, make them move, verify it works

**Test Steps**:
1. Create AgentManager
2. Spawn 3 agents at different positions
3. Set velocities for each agent
4. Run update loop for several frames
5. Verify positions change correctly
6. Print agent states to console

---

## Dependencies

```
Point2D ← AgentState ← Agent ← AgentManager
Vector2D ← AgentState ← Agent
AgentStatus ← AgentState
```

## Design Principles

### 1. Single Responsibility
Each class has one clear job:
- Agent handles agent behavior
- AgentManager handles agent collection
- Point2D handles position
- Vector2D handles direction/velocity

### 2. Clear Interfaces
- Public methods are what other components use
- Private methods are internal implementation
- Data classes (like AgentState) are mostly public fields for easy access

### 3. Thread Safety
- Use ConcurrentHashMap for agent collections
- AgentState should be immutable or carefully synchronized

### 4. Performance Oriented
- Update loops run 30-60 times per second
- Avoid creating objects in update loops
- Use efficient data structures for frequent operations

## Next Steps

After Week 1 completion:
- **Week 2**: Integration with John's Communication System
- **Week 3**: Integration with Lauren's Intelligence System
- **Week 4**: Integration with Anthony's User Interface
