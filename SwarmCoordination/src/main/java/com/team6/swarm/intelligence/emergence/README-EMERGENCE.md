## Emergent Intelligence (package: com.team6.swarm.intelligence.emergence)

This document describes the purpose and structure of the "emergence" package, how the classes interact, and a prioritized list of practical improvements you can apply to harden, test, and simplify the codebase.

---

## Intent and responsibilities

The emergence package contains high-level orchestration logic and cross-cutting intelligence services that drive adaptive, safety-aware swarm behavior. It coordinates subsystems such as flocking, formation, voting, and tasking and implements conflict resolution, adaptive tuning, and emergency handling.

Primary responsibilities:

- Central coordination (SwarmCoordinator)
- Behavior conflict resolution and blending (BehaviorPriority, BehaviorRequest)
- Adaptive tuning (AdaptiveBehavior)
- Emergency handling and modes (EmergencyType, CoordinationMode)
- Formation management (FormationController and Formation)
- Performance capture and trend analysis (PerformanceMetrics, PerformanceSnapshot, PerformanceTrend)
- Integration/integration tests and harnesses (EmergentIntelligenceTest)

---

## Key classes and brief descriptions

- `SwarmCoordinator`

  - Top-level orchestrator. Holds references to sub-systems: `FlockingController`, `VotingSystem`, `TaskAllocator`, `FormationController`, `BehaviorPriority`, `ObstacleAvoidance`, `LeaderFollower`, `AdaptiveBehavior` (when present).
  - Runs the coordination loop, collects `AgentState`s, invokes each subsystem in turn, resolves conflicts via `BehaviorPriority`, and dispatches `MovementCommand`s to agents.

- `BehaviorPriority`

  - Manages competing behavior requests for agents.
  - Accepts `BehaviorRequest` objects and decides which behavior(s) should control an agent (override, weighted blend, or sequential).

- `BehaviorRequest`

  - A small container class holding a requested `BehaviorType`, its `priority`, a `MovementCommand` candidate, and `timestamp`.

- `ConflictResolution`

  - Record for how a behavior conflict was resolved for traceability and debugging.

- `AdaptiveBehavior`

  - A controller that monitors `PerformanceMetrics` and periodically adjusts system parameters (flocking weights, voting thresholds, allocation heuristics) to improve metrics over time.

- `FormationController` + `Formation`

  - Manages geometric formations and maintains agent positions within them.
  - NOTE: Currently `Formation` is a package-private class declared inside `FormationController.java` (see Improvements section).

- `EmergentIntelligenceTest`

  - Integration harness (a suite of scenarios) that validates priority, emergency handling, blending, coordinator integration, and adaptive behavior.

- `PerformanceMetrics`, `PerformanceSnapshot`, `PerformanceTrend`

  - Captures live metrics and historical snapshots for trend detection. `PerformanceSnapshot` is an immutable record-like container.

- `MovementCommandType`, `ResolutionType`, `CoordinationMode`, `EmergencyType`

  - Small enums that express command and mode semantics used across subsystems.

- `CoordinatorStatus`
  - Lightweight status snapshot that surfaces current mode, counters and summary metrics for UI/debugging.

---

## How these pieces interact (high level)

1. External world / sensors / simulator produces `AgentState` values (position, velocity, battery, etc.).
2. `SwarmCoordinator` collects states at each loop tick and calls sub-controllers in a typical order: flocking -> formation -> tasks -> votes -> obstacle avoidance -> behavior priority.
3. Each sub-controller returns one or more candidate `MovementCommand`s for each agent.
4. `BehaviorPriority` takes candidate `BehaviorRequest`s and resolves conflicts (emergency overrides, weighted blending for compatible behaviors, or sequentially queuing lower-priority behaviors).
5. The `SwarmCoordinator` applies the final `MovementCommand` to the agent (via the core command dispatch), and also updates monitors, metrics and logs a `ConflictResolution` record when a conflict is resolved.
6. `AdaptiveBehavior` runs periodically; it consumes `PerformanceSnapshot` history and issues parameter adjustments (e.g., increase separation weight when collisionRate is high). These adjustments feed back into `FlockingParameters`, `TaskAllocator` heuristics and voting parameters.
7. `FormationController` maintains structural groups and may produce commands that must be blended with flocking results via `BehaviorPriority`.
8. `EmergentIntelligenceTest` runs end-to-end scenarios which exercise the above flow for validation.

---

## Suggested improvements and rationale (prioritized)

These recommendations are ordered from low-risk / high-value to higher-effort changes.

1. Make small public API and encapsulation improvements (HIGH priority, low risk)

   - Problem: Many small data-holder classes expose public mutable fields (e.g., `PerformanceMetrics`, `CoordinatorStatus`, `Formation` fields). This allows callers to mutate internal state unexpectedly.
   - Fixes:
     - Make fields private and add getters (and setters only where mutation is required).
     - Where appropriate, make classes immutable or use builder patterns for snapshots (`PerformanceSnapshot` is a good candidate to be immutable/final).
     - Return defensive copies for lists (e.g., `getActiveFormations()` should return an unmodifiableMap or a shallow copy).
   - Example change for `FormationController.getActiveFormations()`:
     ```java
     public Map<String, Formation> getActiveFormations() {
         return Collections.unmodifiableMap(activeFormations);
     }
     ```

2. Move package-private inner classes to their own files (MEDIUM priority)

   - Problem: `Formation` is currently declared as a non-public inner/top-level class inside `FormationController.java`. That can cause compiler "auxiliary-class" warnings and makes the class harder to reuse or test.
   - Fix: Extract `Formation` to `Formation.java` as a top-level public class in the `formation` or `emergence` package (choose the appropriate package). Keep its constructor and fields private with getters.

3. Replace System.out.println with a logger (LOW friction, high value)

   - Problem: `FormationController.transitionFormation(...)` and other classes use `System.out.println` for status messages. Consider using `java.util.logging.Logger` or SLF4J if available.
   - Fix:
     ```java
     private static final Logger logger = Logger.getLogger(FormationController.class.getName());
     // then
     logger.info("Transitioning formation " + formationId + " to " + newType);
     ```

4. Add small unit tests (HIGH priority)

   - Add unit tests for:
     - `BehaviorPriority` (conflict resolution, emergency override, blending)
     - `FormationController` (create, transition, getActiveFormations defensive copy)
     - `AdaptiveBehavior` (parameter adjustments based on given metrics)
   - Use a lightweight test harness if no framework is present; otherwise add JUnit using a minimal test dependency.

5. Defensive and null-checking and validation helpers (LOW/MEDIUM)

   - Add parameter validation (Objects.requireNonNull, guard where lists are used). Improve factory methods to validate inputs.

6. Concurrency and thread safety (MEDIUM)

   - If `SwarmCoordinator` is used from multiple threads (e.g., simulation + UI), consider `ConcurrentHashMap` for collections that are modified/read concurrently, or ensure coordination loop runs on a single thread and document this contract.

7. Improve AdaptiveBehavior practicality and testability (MEDIUM)

   - Provide a minimal public API for injecting metrics in tests (e.g., `update(PerformanceMetrics snapshot)`), and separate pure math/heuristic code into helper class(es) that can be unit tested.

8. Code and javadoc hygiene (HIGH priority for warnings)

   - The project has many dangling doc-comment warnings. Reattach javadoc to the declarations, or convert stray /\*_ comments _/ to block comments /\* \*/ where appropriate.
   - Example: If a comment is standalone above a package declaration, move it inside the class or convert to a standard multi-line comment.

9. Remove duplicate enums or unify types (LOW)

   - `MovementCommandType` exists in emergence. Check whether core `MovementType` in `com.team6.swarm.core` duplicates semantics. If so, unify to avoid confusion (either reuse core or mark this enum as emergent-specific).

10. Make `PerformanceSnapshot` truly immutable (LOW)
    - Make fields final, and don't expose mutating references. e.g.,
      ```java
      public final class PerformanceSnapshot {
          private final PerformanceMetrics metrics;
          private final long timestamp;
          // constructor and getters
      }
      ```
    - If `PerformanceMetrics` is mutable, copy it into an immutable representation or document that snapshots must copy.

---

## Quick refactor suggestions with minimal code changes

- Extract `Formation` to `formation/Formation.java` with fields private and final where possible.
- Change `FormationController.getActiveFormations()` to return `Collections.unmodifiableMap(activeFormations)`.
- Convert `PerformanceMetrics` to either:
  - a) immutable DTO (final fields, constructor that sets everything), or
  - b) provide a `copy()` method and ensure snapshots store copies.
- Introduce `Logger` usage. Replace quick `System.out.println` calls.

---

## Example usage patterns

- Create coordinator and start loop:

```java
SwarmCoordinator coordinator = new SwarmCoordinator();
coordinator.startCoordination();
// call coordinator.update(...) from simulation loop
```

- Request a formation:

```java
FormationController fc = new FormationController();
Formation f = fc.createFormation(FormationType.V_FORMATION, new Point2D(0,0), Arrays.asList(1,2,3,4));
Map<String, Formation> active = fc.getActiveFormations();
```

- Query or snapshot performance:

```java
PerformanceSnapshot snap = new PerformanceSnapshot(currentMetrics, System.currentTimeMillis());
```

---

## Recommended next steps (practical plan)

1. Make the small encapsulation and API changes (fields -> private, defensive copies). Run tests/compile. (1-2 hours)
2. Extract `Formation` to its own file and make `Formation` immutable or properly encapsulated. Update references. (30-60 minutes)
3. Replace `System.out.println` with `Logger` usage across the package. (15-30 minutes)
4. Triage and fix dangling javadoc comments (scan compiler warnings). (30-90 minutes depending on count)
5. Add unit tests for `BehaviorPriority` and `FormationController`. (1-3 hours)
