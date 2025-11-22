# Task Classes - Usage Guide

## Two Different Task Classes

Your project has **two separate Task classes** with different purposes:

### 1. Core Task (`com.team6.swarm.core.Task`)

**Purpose**: Simple task execution for agents
**Package**: `com.team6.swarm.core`
**Created by**: Sanidhya (Week 5-6)
**Used by**: Agent execution, simple movement commands

**Key Features**:

- Simple task types (MOVE, PATROL, GUARD)
- Basic lifecycle (PENDING → IN_PROGRESS → COMPLETED)
- Lightweight for agent execution
- Direct integration with Agent class

**Example Usage**:

```java
import com.team6.swarm.core.Task;

Task task = Task.createMoveTask("task_001", targetPoint);
agent.executeTask(task);
```

### 2. Intelligence Task (`com.team6.swarm.intelligence.tasking.Task`)

**Purpose**: Complex planning and allocation
**Package**: `com.team6.swarm.intelligence.tasking`
**Created by**: Lauren (Week 3)
**Used by**: TaskAllocator, DecisionEngine, planning systems

**Key Features**:

- Complex task types (SCOUT_AHEAD, SEARCH_PATTERN, etc.)
- Role requirements (requiredRole: SCOUT, GUARD)
- Priority levels (HIGH, NORMAL, LOW)
- Battery requirements (minimumBattery)
- Rich metadata for intelligent allocation

**Example Usage**:

```java
import com.team6.swarm.intelligence.tasking.Task;
import com.team6.swarm.intelligence.tasking.TaskPriority;

Task task = Task.createSearchPattern("search_001", center, radius, "grid");
task.priority = TaskPriority.HIGH;
task.requiredRole = BehaviorType.SCOUT;
```

---

## When to Use Which?

### Use Core Task when:

- ✓ Agent is executing simple movement
- ✓ Direct command from user/system
- ✓ Basic navigation or patrol
- ✓ Need lightweight task object

### Use Intelligence Task when:

- ✓ Task allocation and planning
- ✓ Need role matching (SCOUT, GUARD)
- ✓ Priority-based scheduling
- ✓ Battery-aware assignment
- ✓ Decision-making systems
- ✓ Auction-based allocation

---

## Import Guidelines

### For Core Package Files:

```java
import com.team6.swarm.core.Task;
```

### For Intelligence Package Files:

```java
import com.team6.swarm.intelligence.tasking.Task;
import com.team6.swarm.intelligence.tasking.TaskStatus;
import com.team6.swarm.intelligence.tasking.TaskPriority;
import com.team6.swarm.intelligence.tasking.TaskType;
```

---

## File Location Reference

### Core Task:

```
SwarmCoordination/src/main/java/com/team6/swarm/core/Task.java
```

### Intelligence Task:

```
SwarmCoordination/src/main/java/com/team6/swarm/intelligence/tasking/Task.java
```

---

## Design Pattern

This follows the **Layered Architecture** pattern:

```
┌─────────────────────────────────────┐
│   Intelligence Layer (Planning)     │
│   - Complex task allocation         │
│   - Decision making                 │
│   - Uses: intelligence.tasking.Task │
└─────────────┬───────────────────────┘
              │ creates/schedules
              ↓
┌─────────────────────────────────────┐
│   Core Layer (Execution)            │
│   - Agent movement                  │
│   - Simple commands                 │
│   - Uses: core.Task                 │
└─────────────────────────────────────┘
```

---

## Common Confusion Points

### ❌ Wrong:

```java
// In DecisionEngine.java
import com.team6.swarm.core.Task;  // WRONG! Too simple
```

### ✓ Correct:

```java
// In DecisionEngine.java
import com.team6.swarm.intelligence.tasking.Task;  // CORRECT!
```

---

## Quick Decision Tree

```
Need to work with tasks?
│
├─ For planning/allocation/decisions?
│  └─ Use: com.team6.swarm.intelligence.tasking.Task
│
└─ For agent execution/simple commands?
   └─ Use: com.team6.swarm.core.Task
```

---

## Team Member Guide

- **Sanidhya**: Use core.Task for agent execution
- **Lauren**: Use intelligence.tasking.Task for allocation
- **John**: Can reference either depending on context
- **Anthony**: Display both types in UI appropriately

---

## Summary

| Feature     | Core Task | Intelligence Task             |
| ----------- | --------- | ----------------------------- |
| Package     | `core`    | `intelligence.tasking`        |
| Complexity  | Simple    | Complex                       |
| Use Case    | Execution | Planning                      |
| Priority    | No        | Yes (HIGH/NORMAL/LOW)         |
| Role Match  | No        | Yes (SCOUT/GUARD/etc)         |
| Battery Req | No        | Yes                           |
| Created By  | Sanidhya  | Lauren                        |
| Used By     | Agents    | TaskAllocator, DecisionEngine |

**Remember**: When in doubt, intelligence systems (DecisionEngine, TaskAllocator, VotingSystem) use the **intelligence.tasking.Task**!
