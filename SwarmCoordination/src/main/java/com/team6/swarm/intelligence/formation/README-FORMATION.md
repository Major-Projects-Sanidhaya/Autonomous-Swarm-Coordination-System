Formation package — README-FORMATION.md

Overview

- Provides formation definitions and a controller to compute per-agent movement commands so agents can arrange and maintain geometric formations.

Primary classes

- FormationType.java

  - Enum describing supported formation types (e.g. LINE, VEE, CIRCLE, GRID). Used by higher-level code to request a formation.

- Formation.java

  - Data structure representing a formation instance: type, anchor point, spacing, orientation, and a list of assigned agent IDs.
  - Responsible for mapping logical formation slots to physical coordinates.

- FormationController.java

  - Provides methods to compute per-agent movement commands to reach assigned formation slots.
  - Inputs: `Formation`, agent state, formation parameters (spacing, anchor pose).
  - Outputs: MovementCommand or Vector2D for the agent to apply.
  - Responsibilities:
    - Assign agents to formation slots (optionally using simple heuristics).
    - Issue movement vectors that keep the formation cohesive while handling local disturbances.

- FormationTest.java
  - Lightweight test harness that demonstrates formation assignment and movement computations.

Key responsibilities and data shapes

- Formation maps logical slots to positions.
- FormationController.computeMovementToSlot(Agent agent, Formation formation, FormationParameters params)
  - Input:
    - Agent: {id, position, velocity}
    - Formation: {type, anchorPoint, spacing, orientation, slotAssignments}
    - FormationParameters: {slotTolerance, reassignThreshold}
  - Output: MovementCommand (MovementCommand contains: MovementType, targetPosition, maxSpeed)

Example usage (Java)

```java
Formation formation = new Formation(FormationType.VEE, anchor, spacing);
FormationController controller = new FormationController();
MovementCommand cmd = controller.computeMovementToSlot(agent, formation, params);
// agent executes cmd via physics/actuator layer
```

Important method signatures

```java
public class FormationController {
  public MovementCommand computeMovementToSlot(Agent agent, Formation formation, FormationParameters params);
  public Map<Integer, Point2D> assignSlots(List<Agent> agents, Formation formation);
}
```

Design notes and integration

- FormationController should not handle low-level physics—return movement commands and let the physics engine execute them.
- Use simple greedy slot assignment for small swarms, upgrade to Hungarian algorithm for larger teams needing optimal assignments.
- Combine formation-target movement with local flocking forces to handle collision avoidance and smooth motion.

Testing

- `FormationTest` demonstrates slot assignment and simple movement behaviors. Add unit tests for `assignSlots` to ensure deterministic assignments for given inputs.

Extension ideas

- Support dynamic formations where anchor points move over time and agent assignments are rebalanced incrementally.
- Add leader election so a single agent (or small group) provides anchor/orientation updates for the formation.
