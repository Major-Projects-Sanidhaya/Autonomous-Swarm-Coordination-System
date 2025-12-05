# ASCS Codebase Exploration - Quick Start Summary

## What You Need to Know

### 1. Project Overview
The Autonomous Swarm Coordination System is a **multi-agent simulation framework** built in Java with:
- 75+ Java files organized in 5 packages
- Event-driven architecture using publish-subscribe pattern
- Full JavaFX visualization with real-time rendering
- Team-based development (4 developers, separate components)

### 2. Core Architecture at a Glance

```
┌─────────────┐
│  MainUI     │ (Anthony) - JavaFX App with visualization
└──────┬──────┘
       │ EventBus (central message hub)
       │
   ┌───┴─────────────────────────┐
   │                              │
┌──▼──┐           ┌──────────┐  ┌─▼──────────┐
│Core │ ◄────────►│   Comm   │  │Intelligence│
│(San)│ (CmdFlow) │  (John)  │  │  (Lauren)  │
└─────┘           └──────────┘  └────────────┘
  • Agent          • Messages      • Flocking
  • Physics        • Topology      • Voting
  • Commands       • Network       • Tasks
```

### 3. Key Design Pattern: EventBus

**What:** Publish-subscribe message routing system  
**Why:** Decouples components - they don't need direct references  
**How:** 
```java
// Publisher publishes, doesn't care who listens
eventBus.publish(new AgentStateUpdate(...));

// Subscriber listens, doesn't know who publishes
eventBus.subscribe(AgentStateUpdate.class, update -> {
  // handle update
});
```

### 4. The Five Core Packages

| Package | Owner | Purpose | Key Classes |
|---------|-------|---------|------------|
| **core** | Sanidhaya | Agent simulation & physics | Agent, AgentManager, SystemController, PhysicsEngine |
| **communication** | John | Network & messaging | CommunicationManager, Message, NetworkSimulator |
| **intelligence** | Lauren | Behavior algorithms | FlockingController, VotingSystem, TaskAllocator |
| **ui** | Anthony | User interface | MainInterface, Visualizer, ControlPanel, StatusPanel |
| **core** | Everyone | Data structures | EventBus, MovementCommand, AgentState, Point2D |

### 5. Agent Lifecycle (Per Frame ~30-60 FPS)

```
Agent.update(deltaTime)
  1. processCommands()        ← Dequeue highest priority command
  2. checkTaskCompletion()    ← Did target task complete?
  3. physics.updatePosition() ← Integrate velocity to position
  4. updateBattery()          ← Deplete based on movement
  5. publishStateUpdate()     ← Broadcast to EventBus
```

### 6. Movement Command Flow

```
User/System
  ↓
Creates MovementCommand
  ↓
agent.addMovementCommand(cmd)
  ↓
PriorityBlockingQueue (ordered by priority)
  ↓
Agent processes (highest priority first)
  ↓
Executes based on type:
  • MOVE_TO_TARGET      → Use seek() steering
  • FLOCKING_BEHAVIOR   → Apply force directly
  • FORMATION_POSITION  → Seek formation slot
  • AVOID_OBSTACLE      → Use flee() steering
```

### 7. EventBus in Action: Task Completion Example

```
AGENT                          EVENTBUS               SYSTEMCONTROLLER
  │                              │                         │
  ├─ Task complete ──────────┐   │                         │
  │                          │   │                         │
  ├─ Create report ◄─────────┘   │                         │
  │  TaskCompletionReport         │                         │
  │                               │                         │
  ├─ eventBus.publish(report) ───►│                         │
  │                               │                         │
  │                               ├─ Find all subscribers   │
  │                               │  of TaskCompletion      │
  │                               │  Report                 │
  │                               │                         │
  │                               ├─ Deliver to each ─────►│
  │                               │                         │
  │                               │    handleTaskCompletion()
  │                               │    metrics.record()
  │                               │    console.log()
```

**Key insight:** Agent doesn't know SystemController exists!  
Components are completely decoupled.

### 8. Flocking Algorithm (3 Rules)

**Input:** Position, velocity, nearby agents  
**Output:** MovementCommand with combined flocking force

```
1. SEPARATION (avoid crowding)
   For neighbors within 30 units:
   - Calculate vector pointing away
   - Weight by inverse distance (closer = stronger push)
   - Sum all repulsion vectors

2. ALIGNMENT (match velocity)
   For neighbors within 50 units:
   - Average their velocities
   - Create steering force toward average

3. COHESION (stay together)
   For neighbors within 80 units:
   - Calculate center of mass
   - Create force toward center

Combined Force = (sep × sepWeight) + (align × alignWeight) + (cohes × cohWeight)
```

### 9. Voting System (Democratic Decisions)

```
1. INITIATE: Create VoteProposal, broadcast question + options
2. COLLECT: Agents respond with votes  
3. CONSENSUS: Calculate: winningVotes / totalVotes >= threshold?
4. EXECUTE: Implement winning option

Thresholds:
  • Simple Majority: 50%+ (quick decisions)
  • Supermajority: 60%+ (important decisions)
  • Unanimous: 100% (critical safety)

Timeout Fallback if no consensus:
  • Leader decides
  • Status quo (no change)
  • Fail-safe default
  • Random selection
  • Revote
```

### 10. Task Allocation Algorithm

**For each task, score each agent:**
```
score = 0
score += (maxDist - distance) / maxDist × 30     // Proximity (30 pts)
score += (maxLoad - load) / maxLoad × 25         // Load balance (25 pts)
score += batteryLevel × 20                       // Battery (20 pts)
score += (roleMatches ? 25 : 12)                // Role fit (12-25 pts)

Pick agent with highest score
```

**Eligibility:**
- Status = ACTIVE
- Battery ≥ minimum required
- Task queue < 3 items
- Capable of task type

### 11. Physics Engine Constants

```
World Size:        800 × 600 units
Collision Dist:    10 units
Separation Dist:   30 units
Max Agent Speed:   50 units/second
Comm Range:        100 units
Battery Life:      Decreases with movement speed
Boundary Modes:    BOUNCE (reflect), WRAP (teleport), CLAMP (stop)
```

### 12. UI Layout

```
┌─────────────────────────────────────────┐
│ File | View | Tools | Help              │  ← Menu Bar
├─────────────────────────────────────────┤
│          Mission Control Panel           │  ← MissionPanel
├──────────────────────────┬───────────────┤
│                          │               │
│      Visualizer          │ StatusPanel   │  ← Center: 2D Canvas | Right: Metrics
│   (800x600 Canvas)       │ (Metrics)     │
│                          │               │
├──────────────────────────┴───────────────┤
│     ControlPanel (Spawn, Remove, etc)    │  ← Bottom: Controls
└──────────────────────────────────────────┘
```

### 13. Data Structures Overview

**AgentState:**
- Position (Point2D), Velocity (Vector2D)
- Status, Battery level
- Max speed, communication range
- Heading (direction angle)

**MovementCommand:**
- Target agent ID
- Type (MOVE_TO_TARGET, FLOCKING, etc)
- Priority (EMERGENCY, HIGH, NORMAL, LOW)
- Parameters (flexible key-value map)
- Timestamp (for timeout detection)

**Point2D/Vector2D:**
- Simple 2D math vectors
- Used everywhere for positions and forces

### 14. Files You Should Know

**MUST READ (10-15 min each):**
- `core/Agent.java` - How agents work
- `core/EventBus.java` - How messaging works
- `ui/MainInterface.java` - How UI starts
- `ui/Visualizer.java` - How rendering works

**SHOULD READ (5-10 min each):**
- `core/SystemController.java` - How orchestration works
- `intelligence/Voting/VotingSystem.java` - How voting works
- `intelligence/Flocking/FlockingController.java` - How flocking works

**CAN SKIM:**
- Communication, UI panels, support files

### 15. Performance Stats

| Metric | Value |
|--------|-------|
| Target Rendering FPS | 60 |
| Simulation FPS | 30 |
| World Size | 800 × 600 |
| Typical Agent Count | 5-20 |
| Max Recommended | 50-100 |
| Bottleneck | O(n²) communication topology |
| Typical Agent Size | ~500 bytes |

### 16. How to Create a Demo

**Minimal (1-2 days):**
```java
SystemController controller = new SystemController();
controller.initialize();
controller.start();
// Spawn agents, give movement commands
Thread.sleep(60000); // Run 60 seconds
controller.stop();
```

**With Visualization (2-3 days):**
```java
// Launch MainInterface - it handles everything
public static void main(String[] args) {
  MainInterface.main(args);
}
```

**With Specific Feature (3-5 days):**
- Add FlockingController for flocking demo
- Add VotingSystem for voting demo  
- Add TaskAllocator for task allocation demo
- Add CommunicationManager for network demo

### 17. Integration Contracts

**Sanidhaya (Core) ←→ John (Comm):**
- Core sends: AgentStateUpdate (positions)
- Comm sends: NeighborInformation (who to talk to)

**John (Comm) ←→ Lauren (Intelligence):**
- Comm sends: NeighborInformation
- Lauren sends: MovementCommand, VoteProposal

**Lauren (Intel) ←→ Sanidhaya (Core):**
- Lauren sends: MovementCommand, TaskAssignment
- Core sends: AgentState, TaskCompletionReport

**All ←→ Anthony (UI):**
- All send: VisualizationUpdate, NetworkStatus, DecisionStatus, SystemMetrics
- UI sends: SystemCommand, BehaviorConfiguration, NetworkConfiguration

### 18. Recommended Demo Progression

1. **Start:** Basic agent movement (1-2 days)
   - Create SystemController
   - Spawn 5-10 agents
   - Give MOVE_TO_TARGET commands
   - Show position updates

2. **Add:** Flocking behavior (1 day)
   - Integrate FlockingController
   - Calculate flocking forces each frame
   - Observe emergent group behavior

3. **Add:** Communication topology (1 day)
   - Integrate CommunicationManager
   - Visualize neighbor connections
   - Show signal strength

4. **Add:** Voting system (1-2 days)
   - Agents vote on navigation direction
   - Show consensus reach
   - Execute collective decision

5. **Add:** Task allocation (1-2 days)
   - Create tasks at various locations
   - Allocate to agents intelligently
   - Track workload balancing

### 19. Key Insights

1. **Loose Coupling via EventBus** - Core strength of architecture
2. **Component Independence** - Each team can work separately
3. **Priority-Based Commands** - Critical commands execute first
4. **Physics-Based Movement** - Realistic, frame-rate independent
5. **Democratic Decision Making** - Consensus voting for swarm decisions
6. **Intelligent Allocation** - Multi-criteria task assignment
7. **Real-Time Visualization** - 60 FPS rendering capability

### 20. Common Questions

**Q: How do agents communicate?**  
A: Through CommunicationManager + EventBus. Agents broadcast/send messages, neighbors receive them based on communication range.

**Q: Can agents make decisions?**  
A: Yes! VotingSystem enables democratic decisions. Agents vote on proposals (navigation, formation, mission).

**Q: What happens when an agent fails?**  
A: TaskAllocator detects it (battery low or stuck) and reassigns tasks to other agents.

**Q: Can I modify behavior at runtime?**  
A: Yes! FlockingParameters, VotingParameters, etc can be updated dynamically.

**Q: How is it thread-safe?**  
A: EventBus uses ConcurrentHashMap and CopyOnWriteArrayList. SystemController runs simulation in separate thread.

---

## Files Available

**Full Analysis:** `/ASCS/CODEBASE_ANALYSIS.md` (1056 lines)  
**This Summary:** `/ASCS/QUICK_START_SUMMARY.md` (THIS FILE)  
**Project Root:** `/ASCS/`  
**Source Code:** `/ASCS/SwarmCoordination/src/main/java/com/team6/swarm/`

---

## Next Steps

1. **Read CODEBASE_ANALYSIS.md** - Get comprehensive understanding
2. **Review key files** (Agent.java, EventBus.java, MainInterface.java)
3. **Run existing tests** (SimpleTest.java, VotingTest.java, etc)
4. **Create demo** - Pick an option from section 16
5. **Extend system** - Add new features or improve existing ones

---

**Document Generated:** November 5, 2025  
**System Version:** Phase 2 Implementation  
**Status:** Architecture Complete, Most Core Components Finished

