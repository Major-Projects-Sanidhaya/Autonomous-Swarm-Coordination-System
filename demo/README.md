# ASCS Demo Application

## Overview
Autonomous Swarm Coordination System (ASCS) demonstration with multiple scenarios showcasing drone coordination, obstacle avoidance, and task execution.

## Requirements
- Java 17+
- Maven 3.6+

## Building
```bash
cd demo
mvn clean compile
```

## Running

### Interactive Menu
```bash
mvn exec:java -Dexec.mainClass="com.team6.demo.DemoMain"
```

### Command Line (Scenario Selection)
```bash
# Scenario 1: Search & Rescue
mvn exec:java -Dexec.mainClass="com.team6.demo.DemoMain" -Dexec.args="1"

# Scenario 2: Perimeter Patrol
mvn exec:java -Dexec.mainClass="com.team6.demo.DemoMain" -Dexec.args="2"
```

## Available Scenarios

### 1. Search and Rescue Mission
**Objective:** 3 drones search for 2 survivors while avoiding obstacles

**Environment:**
- 500m x 500m area
- 2 static building obstacles
- 1 moving vehicle obstacle

**Success Criteria:**
- All survivors found within 120 seconds
- Zero collisions

**Typical Runtime:** ~30 seconds

### 2. Perimeter Patrol with Intruder Detection
**Objective:** 4 drones patrol facility perimeter and detect/pursue intruders

**Environment:**
- 400m x 400m facility
- Central no-fly zone (80m radius)
- 4 patrol drones (one per edge)

**Behavior:**
- Continuous perimeter patrol
- Intruder spawns at T=30s
- Detecting drone pursues intruder

**Success Criteria:**
- Intruder detected within 50m range
- Immediate pursuit initiated
- Zero collisions

**Runtime:** 90 seconds

## Project Structure

```
demo/
├── src/main/java/com/team6/demo/
│   ├── DemoMain.java                    # Entry point with menu
│   ├── core/
│   │   ├── Position.java                # 3D coordinates
│   │   ├── Environment.java             # Simulation world
│   │   └── Intruder.java                # Intruder entity
│   ├── obstacles/
│   │   ├── Obstacle.java                # Base class
│   │   ├── ObstacleManager.java         # Obstacle management
│   │   ├── BuildingObstacle.java        # Rectangular buildings
│   │   ├── MovingObstacle.java          # Dynamic obstacles
│   │   └── NoFlyZone.java               # Restricted airspace
│   ├── tasks/
│   │   ├── Task.java                    # Task interface
│   │   ├── TaskStatus.java              # Task states
│   │   └── WaypointTask.java            # Navigation task
│   └── scenarios/
│       ├── Scenario.java                # Scenario interface
│       ├── SearchAndRescueScenario.java # Scenario 1
│       └── PerimeterPatrolScenario.java # Scenario 2
└── pom.xml
```

## Key Features

### Obstacle Avoidance
- **Multi-strategy avoidance:** Vertical climb and lateral movement
- **Path validation:** Check clearance before movement
- **Dynamic updates:** Moving obstacles tracked in real-time

### Drone Coordination
- **Autonomous navigation:** Drones move toward targets independently
- **Collision detection:** Real-time position validation
- **Task management:** WaypointTask system for goal-oriented behavior

### Integration with SwarmCoordination
- Uses existing `Agent`, `PhysicsEngine`, `AgentManager` classes
- Extends 2D `Point2D` system to 3D `Position`
- Leverages `Vector2D` for movement calculations
- Event-driven architecture via `EventBus`

## Example Output

### Scenario 1: Search & Rescue
```
=== SEARCH AND RESCUE SCENARIO ===
Environment: 500.0m x 500.0m
Drones: 3
Survivors: 2
Obstacles: 2 buildings, 1 moving vehicle

[T=0.0s] Starting simulation...
[T=5.1s] Drone-1 at (54.1, 54.1, 10.0) -> searching | ...
[T=28.7s] Drone-3 FOUND Survivor-2 at (450.0, 100.0, 0.0)!
[T=30.2s] Drone-2 FOUND Survivor-1 at (250.0, 400.0, 0.0)!

=== MISSION COMPLETE ===
Time: 30.3 seconds
Survivors found: 2/2
Collisions: 0
Status: SUCCESS
```

### Scenario 2: Perimeter Patrol
```
=== PERIMETER PATROL SCENARIO ===
Environment: 400.0m x 400.0m facility
Drones: 4 (perimeter patrol)
Central no-fly zone: radius 80m
Intruder spawns at T=30.0s

[T=0.0s] Patrol initiated...
[T=30.0s] ALERT: Intruder detected at perimeter (400.0, 200.0, 0.0)!
[T=31.9s] Drone-2 DETECTED intruder at (394.0, 200.0, 0.0) (distance: 48.8m)!
[T=31.9s] Drone-2 pursuing intruder...

=== PATROL COMPLETE ===
Duration: 90.1 seconds
Intruder detected: YES (by Drone-2)
Response: Immediate pursuit initiated
Status: SUCCESS
```

## Technical Details

### Simulation Parameters
- **Tick Rate:** 10 Hz (10 updates per second)
- **Drone Speed:** 12-15 m/s
- **Detection Radius:** 30-50m (scenario-dependent)
- **Altitude:** 10-15m (typical flight height)

### Obstacle Types
1. **BuildingObstacle:** Rectangular prism (static)
2. **MovingObstacle:** Spherical with velocity (dynamic)
3. **NoFlyZone:** Cylindrical restricted area (critical)

### Avoidance Strategies
1. **Vertical Avoidance:** Climb +3m when path blocked
2. **Lateral Avoidance:** Move perpendicular to target direction
3. **Combined Avoidance:** Use obstacle manager's vector field

## Development

### Adding New Scenarios
1. Create class implementing `Scenario` interface
2. Override `setup()`, `run()`, `isComplete()`, `printReport()`
3. Add to `DemoMain.createScenario()` switch statement

### Adding New Obstacles
1. Extend `Obstacle` abstract class
2. Implement `containsPoint()`, `getClosestPointTo()`, `getAvoidanceVector()`
3. Register with `ObstacleManager`

### Adding New Tasks
1. Implement `Task` interface
2. Add task-specific logic in `execute()`
3. Track progress in `getProgress()`

## License
Part of the ASCS project - Team 6
