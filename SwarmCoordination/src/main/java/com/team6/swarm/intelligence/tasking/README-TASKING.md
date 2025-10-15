Tasking package — README-TASKING.md

Overview

- Manages representation, allocation, and lifecycle of tasks for the swarm. Contains allocator logic that scores agents for tasks, creates assignments, and tracks progress.

Primary classes

- Task.java

  - Represents a unit of work with attributes such as id, priority, required capabilities, location, and estimated duration.

- TaskStatus.java

  - Enum describing assignment lifecycle stages (PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED).

- TaskAssignment.java

  - Represents an assignment of a `Task` to an agent. Tracks status, progress, timestamps, and allows transitions (accept, complete, fail, cancel, reassign).

- TaskAllocator.java

  - Core allocator that scores agents for tasks and creates `TaskAssignment` instances.
  - Responsible for:
    - Calculating assignment scores using distance, agent load, battery, role matching, and task priority.
    - Maintaining `assignments` and `agentWorkload` maps.
    - Handling reassignment and workload balancing.

- TaskTest.java
  - Test harness exercising allocator behavior: nearest-agent assignment, workload balancing, role-matching, battery-aware assignment, and priority handling.

Key responsibilities and data shapes

- `Task` contains: {id:String, priority:int, location:Point2D, requiredCapabilities:Set<String>, estimatedDuration:int, minimumBattery:double}
- `TaskAssignment` contains: {task:Task, agentId:int, status:TaskStatus, assignedAt:long, progress:double}
- `TaskAllocator` is responsible for:
  - scoreAgentForTask(AgentState agent, Task task) → double
  - assignTask(Task task, int agentId) → TaskAssignment
  - getAgentLoad(int agentId) → int
  - removeFromAgentWorkload(int agentId, TaskAssignment assignment) → void

Important method signatures

```java
public class TaskAllocator {
  public Optional<TaskAssignment> assignTask(Task task);
  public double scoreAgentForTask(AgentState agent, Task task);
  public int getAgentLoad(int agentId); // counts non-terminal assignments
  public void addToAgentWorkload(int agentId, TaskAssignment assignment);
  public void removeFromAgentWorkload(int agentId, TaskAssignment assignment);
}
```

Example usage (Java)

```java
TaskAllocator allocator = new TaskAllocator(agentManager);
Optional<TaskAssignment> assignment = allocator.assignTask(task);
if (assignment.isPresent()) {
  // notify agent or system controller
}
```

Design notes and integration

- Score composition typically includes distance penalty, current workload penalty, battery suitability, and role/capability matching.
- `TaskAllocator` maintains `assignments` map and `agentWorkload` map; ensure completed/failed assignments are removed to keep workloads accurate.
- Consider persistence or event-sourcing if assignments must survive restarts.

Testing

- `TaskTest` exercises allocator heuristics: nearest-agent, workload balance, role preference, battery-aware decisions, and priority handling.
- Add unit tests for scoring logic and `getAgentLoad()` to prevent regressions.

Extension ideas

- Support multi-agent tasks, where `assignTask` returns a list of assignments.
- Add soft-preemption: allow higher-priority tasks to preempt lower-priority work with graceful cancellation notifications to agents.
