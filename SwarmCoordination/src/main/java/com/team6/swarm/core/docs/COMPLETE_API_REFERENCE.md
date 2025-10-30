# Complete Core Package API Reference

**Package:** `com.team6.swarm.core`
**Total Classes:** 27 files
**Documentation Version:** 1.0
**Last Updated:** 2025-10-30

---

## Table of Contents

### Week 1: Foundation Files
1. [Point2D](#point2d-class)
2. [Vector2D](#vector2d-class)
3. [AgentStatus](#agentstatus-enum)
4. [AgentState](#agentstate-class)
5. [Agent](#agent-class)
6. [AgentManager](#agentmanager-class)
7. [SimpleTest](#simpletest-class)

### Week 2: Communication Integration
8. [AgentStateUpdate](#agentstateupdate-class)
9. [CommunicationEvent](#communicationevent-class)
10. [EventBus](#eventbus-class)
11. [SystemController](#systemcontroller-class)

### Week 3: Movement Commands
12. [MovementCommand](#movementcommand-class)
13. [MovementType](#movementtype-enum)
14. [CommandPriority](#commandpriority-enum)
15. [PhysicsEngine](#physicsengine-class)
16. [TaskCompletionReport](#taskcompletionreport-class)

### Week 4: User Interface Integration
17. [SystemCommand](#systemcommand-class)
18. [CommandType](#commandtype-enum)
19. [VisualizationUpdate](#visualizationupdate-class)
20. [SystemMetrics](#systemmetrics-class)
21. [SystemEvent](#systemevent-class)

### Week 5-6: Advanced Features
22. [AgentCapabilities](#agentcapabilities-class)
23. [Task](#task-class)
24. [BoundaryManager](#boundarymanager-class)
25. [PerformanceMonitor](#performancemonitor-class)

---

# Week 1: Foundation Files

## Point2D Class

**Purpose:** Represents X,Y coordinates in 2D space

### Fields
- `double x` - X-coordinate
- `double y` - Y-coordinate

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `Point2D(double x, double y)` | Constructor | Creates point at coordinates |
| `distanceTo(Point2D other)` | `double` | Euclidean distance to another point |
| `add(Vector2D vector)` | `Point2D` | Returns new point offset by vector |
| `subtract(Point2D other)` | `Vector2D` | Vector from other point to this |
| `toString()` | `String` | String representation "(x, y)" |

---

## Vector2D Class

**Purpose:** Represents direction and magnitude (velocity, force)

### Fields
- `double x` - X-component
- `double y` - Y-component

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `Vector2D(double x, double y)` | Constructor | Creates vector with components |
| `add(Vector2D other)` | `Vector2D` | Vector addition |
| `subtract(Vector2D other)` | `Vector2D` | Vector subtraction |
| `multiply(double scalar)` | `Vector2D` | Scalar multiplication |
| `magnitude()` | `double` | Length of vector |
| `normalize()` | `Vector2D` | Unit vector in same direction |
| `dot(Vector2D other)` | `double` | Dot product |
| `limit(double max)` | `Vector2D` | Limit magnitude to max |
| `toString()` | `String` | String representation |

---

## AgentStatus Enum

**Purpose:** Define all possible agent states

### Values
- `ACTIVE` - Fully operational
- `INACTIVE` - Powered down/idle
- `FAILED` - Critical error
- `BATTERY_LOW` - Low battery warning
- `MAINTENANCE` - Under maintenance

---

## AgentState Class

**Purpose:** Complete snapshot of agent at any moment (the "passport")

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `agentId` | `int` | Unique identifier |
| `agentName` | `String` | Display name |
| `position` | `Point2D` | Current position |
| `velocity` | `Vector2D` | Current velocity |
| `status` | `AgentStatus` | Operational status |
| `batteryLevel` | `double` | Battery (0.0-1.0) |
| `maxSpeed` | `double` | Maximum speed limit |
| `communicationRange` | `double` | Radio range |
| `lastUpdateTime` | `long` | Last update timestamp |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `AgentState()` | Constructor | Creates default agent state |
| `toString()` | `String` | Formatted state information |

---

## Agent Class

**Purpose:** The actual agent - can move, receive commands, update itself

### Fields (Private)
- `AgentState state` - Current agent state
- `PriorityBlockingQueue<MovementCommand> commandQueue` - Priority command queue
- `PhysicsEngine physics` - Physics calculations
- `EventBus eventBus` - Event publishing
- `MovementCommand currentCommand` - Active command
- `long taskStartTime` - Task start timestamp

### Constants
- `ARRIVAL_THRESHOLD = 5.0` - Distance to consider "arrived"
- `COMMAND_TIMEOUT_MS = 30000` - 30 seconds timeout

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `Agent(int id, Point2D position)` | Constructor | Creates agent at position |
| `setEventBus(EventBus bus)` | `void` | Set event bus reference |
| `update(double deltaTime)` | `void` | Main update loop (30-60 FPS) |
| `addMovementCommand(MovementCommand cmd)` | `void` | Queue movement command |
| `getState()` | `AgentState` | Get current state |
| `getQueueSize()` | `int` | Get command queue size |
| `isBusy()` | `boolean` | Check if executing command |
| `processCommands()` | `void` (private) | Process queued commands |
| `executeMovementCommand(cmd)` | `void` (private) | Execute specific command |
| `checkTaskCompletion()` | `void` (private) | Check if task complete |
| `completeTask(cmd, status)` | `void` (private) | Publish completion report |
| `moveToward(target)` | `void` (private) | Seek steering behavior |
| `applyForceFromCommand(force)` | `void` (private) | Apply force to velocity |
| `updateBattery(deltaTime)` | `void` (private) | Deplete battery |
| `publishStateUpdate()` | `void` (private) | Publish state to EventBus |

### Update Cycle Logic
1. Process pending movement commands (highest priority first)
2. Check if current task is complete
3. Update physics using PhysicsEngine
4. Update battery level
5. Publish state changes

---

## AgentManager Class

**Purpose:** Factory and registry for all agents - creates, tracks, removes agents

### Fields (Private)
- `Map<Integer, Agent> agents` - Thread-safe agent registry (ConcurrentHashMap)
- `int nextAgentId` - ID counter
- `boolean simulationRunning` - Simulation state flag
- `EventBus eventBus` - Event bus reference

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `AgentManager()` | Constructor | Creates empty manager |
| `AgentManager(EventBus bus)` | Constructor | Creates with event bus |
| `createAgent(Point2D pos)` | `Agent` | Create agent at position |
| `createAgent(int id, Point2D pos)` | `Agent` | Create agent with specific ID |
| `removeAgent(int id)` | `void` | Remove agent from system |
| `getAgent(int id)` | `Agent` | Get agent by ID |
| `getAllAgentStates()` | `List<AgentState>` | Get all agent states |
| `startSimulation()` | `void` | Start simulation loop |
| `stopSimulation()` | `void` | Stop simulation |
| `updateAll(double deltaTime)` | `void` | Update all agents |
| `getAgentCount()` | `int` | Get total agent count |
| `getVisualizationUpdate()` | `VisualizationUpdate` | Package data for UI |
| `simulationLoop()` | `void` (private) | Main simulation thread |

---

## SimpleTest Class

**Purpose:** Test basic agent system works

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `main(String[] args)` | `void` (static) | Test entry point |

### Test Sequence
1. Create 3 agents at random positions
2. Give them movement commands
3. Run simulation for 10 seconds
4. Verify agents moved correctly

---

# Week 2: Communication Integration

## AgentStateUpdate Class

**Purpose:** Message format to tell John when agent positions change

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `agentId` | `int` | Agent identifier |
| `agentState` | `AgentState` | Complete agent state |
| `updateType` | `UpdateType` | Type of update |
| `timestamp` | `long` | Update timestamp |

### Enums
- `UpdateType`: `FULL_STATE`, `POSITION_ONLY`, `STATUS_CHANGE`

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `AgentStateUpdate()` | Constructor | Creates empty update |
| `toString()` | `String` | String representation |

---

## CommunicationEvent Class

**Purpose:** Message format to receive notifications from John

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `senderId` | `int` | Sending agent ID |
| `receiverId` | `int` | Receiving agent ID |
| `messageType` | `String` | Type of message |
| `message` | `Object` | Message payload |
| `timestamp` | `long` | Event timestamp |
| `range` | `double` | Communication range |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `CommunicationEvent(...)` | Constructor | Creates communication event |
| `toString()` | `String` | String representation |

---

## EventBus Class

**Purpose:** Simple system to send messages between components (Publish-Subscribe)

### Fields (Private)
- `Map<Class<?>, List<Consumer<?>>> subscribers` - Event listeners (ConcurrentHashMap)

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `EventBus()` | Constructor | Creates event bus |
| `subscribe(Class<T> type, Consumer<T> listener)` | `void` | Subscribe to event type |
| `unsubscribe(Class<T> type, Consumer<T> listener)` | `void` | Unsubscribe from events |
| `publish(T event)` | `void` | Publish event to subscribers |
| `publishFiltered(T event, Predicate filter)` | `void` | Publish to filtered subscribers |
| `getSubscriberCount(Class<T> type)` | `int` | Get subscriber count |
| `clearAll()` | `void` | Clear all subscribers |

### Usage Pattern
```java
// Subscribe
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("Agent " + update.agentId + " updated");
});

// Publish
eventBus.publish(new AgentStateUpdate(...));
```

---

## SystemController Class

**Purpose:** Coordinates the entire system - starts/stops everything

### Fields (Private)
- `AgentManager agentManager` - Agent management
- `EventBus eventBus` - Event routing
- `SystemMetrics metrics` - Performance tracking
- `SimulationState state` - System state
- `int targetFPS` - Target frame rate (60)
- `Thread simulationThread` - Simulation thread
- `volatile boolean running` - Running flag

### Enums
- `SimulationState`: `INITIALIZING`, `READY`, `RUNNING`, `PAUSED`, `STOPPED`

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `SystemController()` | Constructor | Creates controller |
| `initialize()` | `void` | Initialize all subsystems |
| `start()` | `void` | Start simulation loop |
| `pause()` | `void` | Pause simulation |
| `resume()` | `void` | Resume from pause |
| `stop()` | `void` | Stop and cleanup |
| `setTargetFPS(int fps)` | `void` | Set target FPS |
| `getState()` | `SimulationState` | Get current state |
| `getEventBus()` | `EventBus` | Get event bus |
| `getAgentManager()` | `AgentManager` | Get agent manager |
| `getMetrics()` | `SystemMetrics` | Get metrics |
| `initializeAgents(int count)` | `void` (private) | Create initial agents |
| `registerEventSubscribers()` | `void` (private) | Register event handlers |
| `runSimulationLoop()` | `void` (private) | Main simulation loop |
| `maintainFrameRate(long start)` | `void` (private) | Sleep for FPS control |
| `handleAgentStateUpdate(update)` | `void` (private) | Handle state updates |
| `handleTaskCompletion(report)` | `void` (private) | Handle task reports |
| `handleCommunication(event)` | `void` (private) | Handle comm events |

---

# Week 3: Movement Commands Integration

## MovementCommand Class

**Purpose:** Standardized format for Lauren to give movement instructions

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `agentId` | `int` | Target agent |
| `type` | `MovementType` | Command type |
| `priority` | `CommandPriority` | Urgency level |
| `parameters` | `Map<String, Object>` | Command parameters |
| `timestamp` | `long` | Creation time |
| `taskId` | `String` | Optional task ID |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `MovementCommand(id, type, priority)` | Constructor | Creates command |
| `addParameter(key, value)` | `void` | Add parameter |
| `getParameter(key)` | `Object` | Get parameter |
| `isStale(timeout)` | `boolean` | Check if timed out |
| `compareTo(other)` | `int` | Priority comparison |
| `toString()` | `String` | String representation |

---

## MovementType Enum

**Purpose:** All possible types of movement commands

### Values
- `MOVE_TO_TARGET` - Simple navigation
- `FLOCKING_BEHAVIOR` - Apply flocking forces
- `FORMATION_POSITION` - Move to formation position
- `AVOID_OBSTACLE` - Emergency avoidance

---

## CommandPriority Enum

**Purpose:** Define urgency levels for commands

### Values
- `EMERGENCY` - Highest priority (collision avoidance)
- `HIGH` - Important tasks
- `NORMAL` - Standard operations
- `LOW` - Optional tasks

---

## PhysicsEngine Class

**Purpose:** Handles realistic movement, collisions, boundaries

### Constants
- `WORLD_WIDTH = 800.0`
- `WORLD_HEIGHT = 600.0`
- `COLLISION_DISTANCE = 10.0`
- `SEPARATION_DISTANCE = 30.0`

### Enums
- `BoundaryMode`: `BOUNCE`, `WRAP`, `CLAMP`

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `PhysicsEngine()` | Constructor | Creates engine (BOUNCE mode) |
| `PhysicsEngine(BoundaryMode mode)` | Constructor | Creates with mode |
| `applyForce(state, force, deltaTime)` | `void` | Apply force to velocity |
| `updatePosition(state, deltaTime)` | `void` | Integrate velocity to position |
| `limitVelocity(state, maxSpeed)` | `void` | Cap velocity at max |
| `checkBoundaries(state)` | `void` | Enforce boundaries |
| `checkCollision(agent1, agent2)` | `boolean` | Detect collision |
| `separationForce(agent, others)` | `Vector2D` | Calculate separation |
| `seek(agent, target, speed)` | `Vector2D` | Steering toward target |
| `flee(agent, threat, speed)` | `Vector2D` | Steering away from threat |
| `setBoundaryMode(mode)` | `void` | Set boundary mode |
| `getBoundaryMode()` | `BoundaryMode` | Get boundary mode |

---

## TaskCompletionReport Class

**Purpose:** Tell Lauren when agents complete or fail tasks

### Enums
- `CompletionStatus`: `SUCCESS`, `FAILED`, `TIMEOUT`, `CANCELLED`

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `taskId` | `String` | Task identifier |
| `agentId` | `int` | Agent who performed task |
| `status` | `CompletionStatus` | Completion status |
| `timestamp` | `long` | Completion time |
| `duration` | `double` | Task duration (seconds) |
| `results` | `Map<String, Object>` | Task results |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `TaskCompletionReport(taskId, agentId, status)` | Constructor | Creates report |
| `setDuration(double seconds)` | `void` | Set duration |
| `addResult(key, value)` | `void` | Add result data |
| `getResult(key)` | `Object` | Get result data |
| `toString()` | `String` | String representation |

---

# Week 4: User Interface Integration

## SystemCommand Class

**Purpose:** Standardized format for Anthony to send user commands

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `commandType` | `CommandType` | Type of command |
| `parameters` | `Map<String, Object>` | Command parameters |
| `timestamp` | `long` | Command timestamp |
| `userId` | `String` | User who issued command |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `SystemCommand(CommandType type)` | Constructor | Creates command |
| `addParameter(key, value)` | `void` | Add parameter |
| `getParameter(key)` | `Object` | Get parameter |
| `toString()` | `String` | String representation |

---

## CommandType Enum

**Purpose:** All possible user commands

### Values
- `SPAWN_AGENT` - Create new agent
- `REMOVE_AGENT` - Delete agent
- `SET_BOUNDARIES` - Configure world bounds
- `START_SIMULATION` - Begin simulation
- `STOP_SIMULATION` - End simulation
- `PAUSE_SIMULATION` - Pause simulation

---

## VisualizationUpdate Class

**Purpose:** Complete system state for Anthony's display

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `allAgents` | `List<AgentState>` | All agent states |
| `systemMetrics` | `SystemMetrics` | Performance data |
| `timestamp` | `long` | Update timestamp |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `VisualizationUpdate()` | Constructor | Creates update |
| `toString()` | `String` | String representation |

---

## SystemMetrics Class

**Purpose:** Performance information (CPU, memory, FPS)

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `totalAgents` | `int` | Total agent count |
| `activeAgents` | `int` | Active agent count |
| `averageSpeed` | `double` | Mean velocity |
| `systemLoad` | `double` | CPU load (0.0-1.0) |
| `updatesPerSecond` | `int` | Update frequency |
| `memoryUsage` | `double` | Memory usage (0.0-1.0) |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `SystemMetrics()` | Constructor | Creates metrics |
| `update(agentCount, deltaTime)` | `void` | Update metrics |
| `recordStateUpdate()` | `void` | Record state update |
| `recordTaskCompletion(status)` | `void` | Record task completion |
| `recordCommunication()` | `void` | Record comm event |
| `getStateUpdateCount()` | `int` | Get state update count |
| `getTaskCompletionCount()` | `int` | Get task count |
| `getCommunicationCount()` | `int` | Get comm count |

---

## SystemEvent Class

**Purpose:** Notable events for display (agent created, failed, etc.)

### Enums
- `Severity`: `DEBUG`, `INFO`, `WARNING`, `ERROR`

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | `String` | Event type |
| `agentId` | `String` | Related agent |
| `message` | `String` | Description |
| `timestamp` | `long` | Event time |
| `severity` | `Severity` | Importance |
| `metadata` | `Map<String, Object>` | Additional data |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `SystemEvent(type, agentId, msg, severity)` | Constructor | Creates event |
| `getEventType()` | `String` | Get event type |
| `getAgentId()` | `String` | Get agent ID |
| `getMessage()` | `String` | Get message |
| `getTimestamp()` | `long` | Get timestamp |
| `getSeverity()` | `Severity` | Get severity |
| `getMetadata()` | `Map` | Get metadata map |
| `addMetadata(key, value)` | `SystemEvent` | Add metadata (chainable) |
| `getMetadata(key)` | `Object` | Get metadata value |
| `hasAgent()` | `boolean` | Check if agent-related |
| `toString()` | `String` | String representation |
| `info(type, agentId, msg)` (static) | `SystemEvent` | Create INFO event |
| `warning(type, agentId, msg)` (static) | `SystemEvent` | Create WARNING event |
| `error(type, agentId, msg)` (static) | `SystemEvent` | Create ERROR event |
| `debug(type, agentId, msg)` (static) | `SystemEvent` | Create DEBUG event |

### Event Type Constants
- `AGENT_CREATED`, `AGENT_DESTROYED`, `AGENT_STATE_CHANGED`, `AGENT_FAILED`
- `SYSTEM_STARTED`, `SYSTEM_STOPPED`, `SYSTEM_RESET`, `SYSTEM_CONFIGURED`
- `TASK_ASSIGNED`, `TASK_COMPLETED`, `TASK_FAILED`
- `COLLISION_DETECTED`, `BOUNDARY_REACHED`, `COMMUNICATION_FAILED`
- `PERFORMANCE_WARNING`, `MEMORY_WARNING`

---

# Week 5-6: Advanced Features

## AgentCapabilities Class

**Purpose:** Detailed information about what each agent can currently do

### Fields

| Field | Type | Access | Description |
|-------|------|--------|-------------|
| `agentId` | `int` | public final | Agent identifier |
| `timestamp` | `long` | public final | Snapshot timestamp |
| `movementCapability` | `double` | private | Movement effectiveness (0-1) |
| `communicationCapability` | `double` | private | Radio effectiveness (0-1) |
| `batteryCapacity` | `double` | private | Battery level (0-1) |
| `processingCapacity` | `double` | private | CPU availability (0-1) |
| `taskLoadFactor` | `double` | private | How busy (0-1) |
| `reliabilityScore` | `double` | private | Success rate (0-1) |

### Constants
- `CRITICAL_THRESHOLD = 0.2`
- `DEGRADED_THRESHOLD = 0.5`
- `OPTIMAL_THRESHOLD = 0.8`

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `AgentCapabilities(id, state)` | Constructor | Create from agent state |
| `updateFromState(AgentState state)` | `void` | Refresh capabilities |
| `recordTaskCompletion(boolean success)` | `void` | Update reliability |
| `setTaskLoadFactor(double load)` | `void` | Set task load |
| `canPerformTask(String taskType)` | `boolean` | Check if capable |
| `getOverallCapability()` | `double` | Get weighted average |
| `isCritical()` | `boolean` | Check if critical |
| `isDegraded()` | `boolean` | Check if degraded |
| `isOptimal()` | `boolean` | Check if optimal |
| `getCapabilityStatus()` | `String` | Get status string |
| `setTaskCapability(String type, boolean capable)` | `void` | Enable/disable task type |
| `getDetailedReport()` | `String` | Get full report |
| `getMovementCapability()` | `double` | Get movement capability |
| `getCommunicationCapability()` | `double` | Get comm capability |
| `getBatteryCapacity()` | `double` | Get battery level |
| `getProcessingCapacity()` | `double` | Get CPU capacity |
| `getTaskLoadFactor()` | `double` | Get task load |
| `getReliabilityScore()` | `double` | Get reliability |
| `getTotalTasksAttempted()` | `int` | Get total tasks |
| `getSuccessfulTasks()` | `int` | Get successful tasks |
| `getFailedTasks()` | `int` | Get failed tasks |
| `isFunctional()` | `boolean` | Check if functional |
| `toString()` | `String` | String representation |

---

## Task Class

**Purpose:** Represents assigned work for agents

### Enums
- `TaskType`: `MOVE_TO_LOCATION`, `PATROL_ROUTE`, `GUARD_POSITION`, `FOLLOW_AGENT`, `RETURN_TO_BASE`
- `TaskState`: `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`, `CANCELLED`

### Fields

| Field | Type | Access | Description |
|-------|------|--------|-------------|
| `taskId` | `String` | public final | Task identifier |
| `taskType` | `TaskType` | public final | Type of task |
| `createdTime` | `long` | public final | Creation timestamp |
| `assignedToAgent` | `int` | private | Assigned agent ID |
| `state` | `TaskState` | private | Lifecycle state |
| `targetLocation` | `Point2D` | public | Target location |
| `parameters` | `Map<String, Object>` | public | Task parameters |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `Task(String id, TaskType type)` | Constructor | Create task |
| `Task(String id, TaskType type, Point2D target)` | Constructor | Create with location |
| `createMoveTask(id, target)` (static) | `Task` | Factory: move task |
| `createPatrolTask(id, waypoints...)` (static) | `Task` | Factory: patrol task |
| `createGuardTask(id, position)` (static) | `Task` | Factory: guard task |
| `createFollowTask(id, targetAgentId)` (static) | `Task` | Factory: follow task |
| `createReturnTask(id, baseLocation)` (static) | `Task` | Factory: return task |
| `assignTo(int agentId)` | `void` | Assign to agent |
| `markInProgress()` | `void` | Start task |
| `updateProgress(double progress)` | `void` | Update progress (0-1) |
| `markCompleted()` | `void` | Complete task |
| `markFailed(String reason)` | `void` | Fail task |
| `cancel()` | `void` | Cancel task |
| `isActive()` | `boolean` | Check if active |
| `isComplete()` | `boolean` | Check if complete |
| `isPending()` | `boolean` | Check if pending |
| `isAssigned()` | `boolean` | Check if assigned |
| `getDuration()` | `long` | Get duration (ms) |
| `getAge()` | `long` | Get age (ms) |
| `validate()` | `boolean` | Validate parameters |
| `getAssignedToAgent()` | `int` | Get assigned agent |
| `getState()` | `TaskState` | Get state |
| `getProgressPercentage()` | `double` | Get progress |
| `getStartTime()` | `long` | Get start time |
| `getCompletionTime()` | `long` | Get completion time |
| `toString()` | `String` | String representation |
| `getDetailedInfo()` | `String` | Detailed information |

---

## BoundaryManager Class

**Purpose:** Manages world boundaries and safe zones

**Design Pattern:** Singleton

### Enums
- `EnforcementMode`: `SOFT`, `MEDIUM`, `HARD`, `TELEPORT`
- `ZoneType`: `SAFE`, `RESTRICTED`, `CHARGING`, `MISSION`, `SPAWN`
- `ZoneShape`: `RECTANGLE`, `CIRCLE`

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getInstance()` (static) | `BoundaryManager` | Get singleton instance |
| `setEventBus(EventBus bus)` | `void` | Set event bus |
| `setWorldBounds(minX, minY, maxX, maxY)` | `void` | Define world boundaries |
| `getWorldWidth()` | `double` | Get world width |
| `getWorldHeight()` | `double` | Get world height |
| `isWithinWorldBounds(Point2D pos)` | `boolean` | Check if in bounds |
| `addSafeZone(String id, Zone zone)` | `void` | Add safe zone |
| `addRestrictedZone(String id, Zone zone)` | `void` | Add restricted zone |
| `addSpecialZone(String id, Zone zone, ZoneType type)` | `void` | Add special zone |
| `removeZone(String id)` | `void` | Remove zone |
| `clearAllZones()` | `void` | Clear all zones |
| `isPositionValid(Point2D pos)` | `boolean` | Check if position valid |
| `isInSafeZone(Point2D pos)` | `boolean` | Check if in safe zone |
| `isInRestrictedZone(Point2D pos)` | `boolean` | Check if restricted |
| `enforceBoundaries(AgentState agent)` | `Point2D` | Enforce boundaries |
| `getNearestSafePoint(Point2D pos)` | `Point2D` | Find nearest safe point |
| `getViolationCount(int agentId)` | `int` | Get agent violations |
| `getTotalViolations()` | `int` | Get total violations |
| `resetViolationTracking()` | `void` | Reset violation counters |
| `setEnforcementMode(EnforcementMode mode)` | `void` | Set enforcement mode |
| `getEnforcementMode()` | `EnforcementMode` | Get enforcement mode |
| `getWorldMinX()` | `double` | Get min X |
| `getWorldMinY()` | `double` | Get min Y |
| `getWorldMaxX()` | `double` | Get max X |
| `getWorldMaxY()` | `double` | Get max Y |
| `getSafeZones()` | `Map<String, Zone>` | Get safe zones |
| `getRestrictedZones()` | `Map<String, Zone>` | Get restricted zones |
| `getSpecialZones()` | `Map<String, Zone>` | Get special zones |

### Inner Class: Zone

| Method | Return Type | Description |
|--------|-------------|-------------|
| `createRectangle(id, minX, minY, maxX, maxY)` (static) | `Zone` | Create rectangular zone |
| `createCircle(id, center, radius)` (static) | `Zone` | Create circular zone |
| `contains(Point2D point)` | `boolean` | Check if point in zone |
| `toString()` | `String` | String representation |

---

## PerformanceMonitor Class

**Purpose:** Track system performance and optimize

**Design Pattern:** Singleton

### Enums
- `PerformanceStatus`: `OPTIMAL`, `NORMAL`, `DEGRADED`, `CRITICAL`
- `OptimizationLevel`: `NONE`, `LIGHT`, `MODERATE`, `AGGRESSIVE`

### Constants
- `OPTIMAL_FPS = 50.0`
- `NORMAL_FPS = 30.0`
- `DEGRADED_FPS = 20.0`
- `OPTIMAL_MEMORY = 0.7`
- `NORMAL_MEMORY = 0.8`
- `CRITICAL_MEMORY = 0.9`
- `HISTORY_SIZE = 60` (frames)

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getInstance()` (static) | `PerformanceMonitor` | Get singleton instance |
| `setEventBus(EventBus bus)` | `void` | Set event bus |
| `startFrame()` | `void` | Mark frame start |
| `endFrame()` | `void` | Mark frame end |
| `updateAgentCounts(int active, int total)` | `void` | Update agent counts |
| `isPerformanceDegraded()` | `boolean` | Check if degraded |
| `isPerformanceCritical()` | `boolean` | Check if critical |
| `isPerformanceOptimal()` | `boolean` | Check if optimal |
| `getCurrentMetrics()` | `PerformanceMetrics` | Get metrics snapshot |
| `getPerformanceReport()` | `String` | Get full report |
| `reset()` | `void` | Reset all metrics |
| `setTargetFPS(int fps)` | `void` | Set target FPS |
| `setAutoOptimize(boolean enable)` | `void` | Enable auto-optimize |
| `setOptimizationLevel(OptimizationLevel level)` | `void` | Set optimization |
| `getCurrentFPS()` | `double` | Get current FPS |
| `getAverageFPS()` | `double` | Get average FPS |
| `getMinFPS()` | `double` | Get minimum FPS |
| `getMaxFPS()` | `double` | Get maximum FPS |
| `getMemoryUsagePercent()` | `double` | Get memory usage |
| `getAverageFrameTime()` | `double` | Get avg frame time |
| `getCurrentStatus()` | `PerformanceStatus` | Get status |
| `getOptimizationLevel()` | `OptimizationLevel` | Get optimization |
| `getFrameCount()` | `long` | Get frame count |
| `getActiveAgentCount()` | `int` | Get active agents |
| `getTotalAgentCount()` | `int` | Get total agents |

### Inner Class: PerformanceMetrics

| Field | Type | Description |
|-------|------|-------------|
| `currentFPS` | `double` | Current FPS |
| `averageFPS` | `double` | Average FPS |
| `minFPS` | `double` | Minimum FPS |
| `maxFPS` | `double` | Maximum FPS |
| `averageFrameTime` | `double` | Average frame time (ms) |
| `maxFrameTime` | `double` | Maximum frame time (ms) |
| `memoryUsagePercent` | `double` | Memory usage (0-1) |
| `heapUsed` | `long` | Heap used (bytes) |
| `heapMax` | `long` | Max heap (bytes) |
| `activeAgents` | `int` | Active agent count |
| `totalAgents` | `int` | Total agent count |
| `status` | `PerformanceStatus` | Performance status |
| `frameCount` | `long` | Total frames |

---

## Quick Reference: Common Operations

### Creating and Managing Agents
```java
// Create agent manager
AgentManager manager = new AgentManager(eventBus);

// Spawn agent
Agent agent = manager.createAgent(new Point2D(100, 100));

// Get agent state
AgentState state = agent.getState();

// Remove agent
manager.removeAgent(agentId);
```

### Movement Commands
```java
// Create movement command
MovementCommand cmd = new MovementCommand(
    agentId,
    MovementType.MOVE_TO_TARGET,
    CommandPriority.NORMAL
);
cmd.addParameter("target", new Point2D(200, 200));

// Queue command
agent.addMovementCommand(cmd);
```

### Event Communication
```java
// Subscribe to events
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("Agent " + update.agentId + " updated");
});

// Publish event
eventBus.publish(new AgentStateUpdate(...));
```

### Task Management
```java
// Create task
Task task = Task.createMoveTask("task_001", targetPosition);

// Assign to agent
task.assignTo(agentId);
task.markInProgress();

// Update progress
task.updateProgress(0.5); // 50%

// Complete
task.markCompleted();
```

### Boundary Management
```java
// Get boundary manager
BoundaryManager bm = BoundaryManager.getInstance();

// Set world bounds
bm.setWorldBounds(0, 0, 800, 600);

// Add safe zone
Zone safeZone = Zone.createRectangle("op1", 50, 50, 750, 550);
bm.addSafeZone("operational", safeZone);

// Check validity
if (!bm.isPositionValid(position)) {
    Point2D safe = bm.getNearestSafePoint(position);
}
```

### Performance Monitoring
```java
// Get monitor
PerformanceMonitor pm = PerformanceMonitor.getInstance();

// Track frame
pm.startFrame();
// ... do work ...
pm.endFrame();

// Check status
if (pm.isPerformanceDegraded()) {
    pm.setOptimizationLevel(OptimizationLevel.AGGRESSIVE);
}

// Get report
System.out.println(pm.getPerformanceReport());
```

---

## Design Patterns Summary

| Pattern | Classes Using It |
|---------|------------------|
| **Singleton** | BoundaryManager, PerformanceMonitor |
| **Observer** | EventBus, all event classes |
| **Factory** | AgentManager, Task (factory methods) |
| **DTO** | AgentState, SystemMetrics, AgentCapabilities |
| **Strategy** | PhysicsEngine (BoundaryMode), BoundaryManager (EnforcementMode) |
| **Command** | MovementCommand, SystemCommand |
| **State** | AgentStatus, TaskState, SimulationState |
| **Value Object** | Point2D, Vector2D |
| **Pub-Sub** | EventBus system-wide |
| **Priority Queue** | Agent (command processing) |

---

## Thread Safety Notes

- **ConcurrentHashMap**: AgentManager, EventBus, BoundaryManager
- **CopyOnWriteArrayList**: EventBus subscribers
- **PriorityBlockingQueue**: Agent command queue
- **Volatile flags**: SystemController.running
- **Synchronized methods**: Singleton getInstance(), state transitions

---

**End of API Reference**
