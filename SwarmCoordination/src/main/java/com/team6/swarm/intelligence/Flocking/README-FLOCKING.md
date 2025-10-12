Flocking package — README-FLOCKING.md

Overview

- Implements local flocking behaviors (separation, alignment, cohesion) used by agents to produce smooth, collision-free group motion. The package provides a controller that takes neighbor states and returns a steering force for a single agent.

Primary classes

- BehaviorType.java

  - Enum describing the flocking sub-behaviors (e.g. SEPARATION, ALIGNMENT, COHESION, COMBINED). Used to tune which behavior(s) are active.

- FlockingParameters.java

  - Holds tunable constants (weights, radii, time-steps, damping, etc.) for the flocking calculations. Centralized place to tune swarm behavior without touching algorithm code.

- FlockingController.java

  - Main class that computes flocking force vectors for a single agent.
  - Inputs: agent state (position, velocity), neighbor list and their states, and `FlockingParameters`.
  - Outputs: force vector (Vector2D or Point2D) representing the desired acceleration/steering to apply.
  - Responsibilities:
    - Calculate separation, alignment, and cohesion components.
    - Weight and combine components according to `FlockingParameters`.
    - Provide small helper methods used by the visualization or higher-level controllers.

- FlockingTest.java
  - Informal test harness that runs short flocking scenarios and prints agent states and computed force components.
  - Useful for manual testing and visual inspection.

Key responsibilities and data shapes

- FlockingController.computeFlockingForce(Agent agent, List<NeighborAgent> neighbors, FlockingParameters params)
  - Input:
    - Agent: { id:int, position:Point2D, velocity:Vector2D, battery:double }
    - neighbors: List<NeighborAgent> (same fields as Agent but lightweight)
    - params: FlockingParameters (weights, radii, caps)
  - Output: Vector2D (x,y) steering/force to apply

Example usage (Java)

```java
// compute a steering vector for an agent
FlockingController controller = new FlockingController();
FlockingParameters params = FlockingParameters.defaultParams();
Vector2D force = controller.computeFlockingForce(agent, neighbors, params);
// apply force via agent's physics subsystem
physics.applyForce(agent.getId(), force);
```

Important method signatures

```java
public class FlockingController {
  public Vector2D computeFlockingForce(Agent agent, List<NeighborAgent> neighbors, FlockingParameters params);

  // small helpers
  private Vector2D separation(Agent a, List<NeighborAgent> neighbors, double radius, double weight);
  private Vector2D alignment(Agent a, List<NeighborAgent> neighbors, double radius, double weight);
  private Vector2D cohesion(Agent a, List<NeighborAgent> neighbors, double radius, double weight);
}
```

Design notes and integration

- Keep the controller stateless: pass in all inputs and return pure outputs to make testing deterministic.
- Use `FlockingParameters` to tune behavior at runtime (weights and radii).
- Combine the returned force with higher-level movement commands (from Tasking or Formation) using weighted blending or priority rules.

Testing

- `FlockingTest` is a manual/visual harness that prints agent states and components (sep/align/cohes).
- Consider adding a small unit test that asserts known outputs for contrived neighbor layouts.

Extension ideas

- Add obstacle avoidance as a separate method and include it in the final blended output.
- Add an adaptive weighting scheme that reduces cohesion at high speeds.
