Emergence package — README-EMERGENCE.md

Overview

- Provides high-level coordination and emergent-intelligence logic for the swarm. The package contains the coordinator, behavior conflict resolution, adaptive tuning, emergency handling and an integration harness used for validation.

Primary classes

- `SwarmCoordinator.java`

  - Central orchestrator. Runs the coordination loop and invokes sub-systems in order (flocking → formation → tasks → votes → obstacle-avoidance → priority resolution). Collects `AgentState` inputs and registers `MovementCommand`s with `BehaviorPriority`.

- `BehaviorPriority.java`

  - Collects `BehaviorRequest`s per agent and resolves conflicts using priorities, overrides (emergency) or blending when compatible. Returns the final `MovementCommand` for an agent.

- `BehaviorRequest.java`

  - Small container with `BehaviorType`, priority, `MovementCommand` and timestamp. Use getters to access fields.

- `ConflictResolution.java`

  - Lightweight record describing the resolution outcome (winning behavior, losers, resolution type) for traceability.

- `AdaptiveBehavior.java`

  - Self-tuning controller that consumes `PerformanceMetrics` snapshots and adjusts subsystem parameters (flocking, voting, tasking). Exposes small getters the tests consume (`getFlockingAdjustments()`, `getVotingAdjustments()`, `getTaskAdjustments()`, etc.).

- `CoordinatorStatus.java`

  - Read-only snapshot of coordinator state for UI/telemetry. Fields are private; `SwarmCoordinator` populates via package-private setters.

- `PerformanceMetrics.java`, `PerformanceSnapshot.java`, `PerformanceTrend.java`

  - Types used to collect metrics and analyze trends; `PerformanceSnapshot` is used by `AdaptiveBehavior` to maintain history.

- `ResolutionType.java`, `MovementCommandType.java`, `CoordinationMode.java`, `EmergencyType.java`

  - Small enums used by the package.

- `EmergentIntelligenceTest.java`

  - Integration harness that runs a suite of scenarios (priority resolution, emergency handling, blending, coordinator integration, adaptive tuning).

Key responsibilities and data shapes

- SwarmCoordinator maps `List<AgentState>` → subsystems → candidate `MovementCommand`s → final resolved `MovementCommand` per agent.
- `BehaviorRequest` shape: { behaviorType: BehaviorType, priority: int, command: MovementCommand, timestamp: long }
- `CoordinatorStatus` shape: { mode, active, updateCount, avgUpdateTime, emergencyCount, totalTasksAssigned, totalVotesProcessed, flockingCalculations, behaviorConflicts }

Important method signatures

```java
// Coordinator
public class SwarmCoordinator {
  public void update(double deltaTime, List<AgentState> agents);
  public CoordinatorStatus getStatus();
  public Formation createFormation(FormationType type, Point2D center, List<Integer> agentIds);
  public void transitionFormation(String formationId, FormationType newType);
}

// Behavior priority
public class BehaviorPriority {
  public void registerBehavior(int agentId, BehaviorType behaviorType, int priority, MovementCommand command);
  public MovementCommand resolveConflicts(int agentId);
}

// Adaptive behavior (example)
public class AdaptiveBehavior {
  public void enableAdaptation();
  public void update(PerformanceMetrics metrics);
  public int getTaskAdjustments();
}
```

Design notes and integration

- Formation logic and the concrete `Formation` class live in `com.team6.swarm.intelligence.formation`. `SwarmCoordinator` interacts with that subsystem via `FormationController` (e.g., `getAllFormations()`, `maintainFormation(...)`, `transitionFormation(...)`, and the cohesion helper `getFormationCohesion(...)`).
- `CoordinatorStatus` was made encapsulated: use the public getters for read-only purposes. Keep setters package-private.
- `BehaviorPriority` implementation was fixed to rely on `BehaviorRequest` getters (no direct field access) and records resolution outcomes using `ConflictResolution`.
- Avoid modifying `Formation` instances directly from emergence code; add helpers to `FormationController` if you need new operations.

Testing

- `EmergentIntelligenceTest.java` is the primary integration harness. It is a plain `main()` program that can be run after a non-UI compile. Tests validate emergency overrides, behavior blending, coordinator loops and adaptive tuning.

Example usage (Java)

```java
SwarmCoordinator coordinator = new SwarmCoordinator();
coordinator.startCoordination();
coordinator.update(0.033, agents); // call from simulation loop
CoordinatorStatus status = coordinator.getStatus();
```

How to run (non-UI compile)

```bash
cd SwarmCoordination
javac -Xlint:all $(find src/main/java -name '*.java' ! -path '*/swarm/ui/*') -d /tmp/swarm_build_out
java -cp /tmp/swarm_build_out com.team6.swarm.intelligence.emergence.EmergentIntelligenceTest
```

Extension ideas

- Add unit tests for `BehaviorPriority` blending and emergency override.
- Make `FormationController.getAllFormations()` return an unmodifiable collection if external mutation should be prevented.
- Convert stray file-level `/** ... */` comments to `/* ... */` or attach them to classes to eliminate dangling-javadoc warnings during compilation.

Testing notes

- The UI package depends on JavaFX; if JavaFX is not available exclude `swarm/ui` from compilation while running the harness.
