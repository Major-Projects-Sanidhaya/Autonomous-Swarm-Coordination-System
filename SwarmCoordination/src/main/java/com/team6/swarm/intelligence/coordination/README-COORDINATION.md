Coordination package — README-COORDINATION.md

Overview
- The `coordination` package contains algorithms and helpers focused on short-range coordination, obstacle avoidance, leader/follower patterns, and search patterns that enable agents to operate safely and cooperatively in shared space.
- These classes provide deterministic, local decision primitives that higher-level systems (tasking, formation, flocking) can call to remain collision-free and to coordinate roles (leaders/followers).

Primary classes
- AvoidanceDirection.java
- AvoidanceManeuver.java
- AvoidanceState.java
- AvoidanceStrategy.java
- CoordinationTest.java
- FollowerState.java
- FormationRole.java
- LeaderFollower.java
- LeaderSelectionReason.java
- Obstacle.java
- ObstacleAvoidance.java
- ObstacleType.java
- SearchPattern.java
- SearchPatternType.java

Responsibilities and concept summary
- Obstacle modeling (Obstacle, ObstacleType)
  - Represent obstacles detected in the environment, their geometry and metadata used by avoidance logic.

- Local avoidance (ObstacleAvoidance, AvoidanceStrategy, AvoidanceState, AvoidanceDirection, AvoidanceManeuver)
  - Compute safe steering/velocity changes for an agent to avoid collisions while attempting to preserve its higher-level goal.
  - Encapsulates stateful maneuvers (e.g., temporarily sidestep, slow down, or perform an escape arc).

- Leader/follower and role management (LeaderFollower, FollowerState, FormationRole, LeaderSelectionReason)
  - Support leader selection and follower behavior where a small subset of agents provide guidance or anchor formation anchors.
  - Provide reasons for leader selection (battery, proximity, role preference) to enable explainability and reproducible choices.

- Search patterns (SearchPattern, SearchPatternType)
  - Provide pre-defined patterns (lawnmower, spiral, expanding grid) for area coverage tasks.
  - Offer methods to compute the next waypoint for an agent given pattern state.

- Test harness (CoordinationTest)
  - Small suite to exercise avoidance strategies and leader/follower behaviors; useful for manual verification and regression checks.

Important method signatures and snippets

Obstacle representation
```java
public class Obstacle {
  private int id;
  private Point2D center;
  private double radius; // approximate for circular obstacles
  private ObstacleType type;

  // getters, constructors, helpers
}
```

Basic avoidance strategy interface
```java
public interface AvoidanceStrategy {
  // Compute a maneuver (delta velocity or steering) to avoid the obstacle(s)
  AvoidanceManeuver planAvoidance(Agent agent, List<Obstacle> nearby, AvoidanceState state, double dt);
}
```

AvoidanceManeuver data shape
```java
public class AvoidanceManeuver {
  public final Vector2D deltaVelocity; // suggested change
  public final boolean requiresImmediate; // whether maneuver must be enacted now
  public final String description; // human-readable reason

  public AvoidanceManeuver(Vector2D dv, boolean imm, String desc) { ... }
}
```

Leader/Follower helper
```java
public class LeaderFollower {
  // pick a leader from a list of candidates and provide assignment of followers
  public Optional<Integer> selectLeader(List<AgentState> candidates, LeaderSelectionCriteria criteria);

  // compute follower target for a follower agent given leader state and formation role
  public Point2D followerTarget(AgentState leader, FormationRole role, double spacing);
}
```

Search pattern usage
```java
SearchPattern pattern = SearchPattern.create(SearchPatternType.LAWNMOWER, areaBounds, spacing);
Point2D next = pattern.nextWaypoint(agentState);
```

How this package fits into the system
- Safety/locality: Coordination primitives should be called by motion planners (e.g., `SystemController` or `FlockingController`) before applying newly computed velocities to ensure they are safe.
- Role coordination: Leader selection informs formation/ tasking systems which agent(s) should take responsibility for decision points.
- Search & coverage: `SearchPattern` produces deterministic coverage routes that `TaskAllocator` or mission planners can assign as tasks.

Design notes and guidelines
- Keep avoidance decisions local and fast: these functions are called frequently and should be low-overhead.
- Favor deterministic outputs (given same inputs) to make unit tests reproducible.
- Keep state encapsulated in `AvoidanceState` so maneuvers can be multi-step without leaking internal counters into other systems.

Testing recommendations
- Unit tests for `ObstacleAvoidance` with contrived obstacle arrangements to validate that computed `deltaVelocity` removes intersection risk.
- Simulation tests (CoordinationTest) that step multiple agents forward and assert no collisions occur over many timesteps.

Extension ideas
- Add advanced geometric obstacle shapes (polygons) and more efficient spatial indexing (e.g., k-d tree, grid) for large-scale avoidance.
- Provide a small visualization utility that highlights computed avoidance vectors during tests to debug behavior.

Notes
- If you add new avoidance strategies, ensure they implement `AvoidanceStrategy` and are registered by any factory method used by `ObstacleAvoidance`.
- Document leader-selection policies in `LeaderSelectionReason` so the system's choice can be audited.
