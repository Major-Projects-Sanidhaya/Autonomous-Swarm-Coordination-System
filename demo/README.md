# ASCS Demo Visualization

**Autonomous Swarm Coordination System - Interactive Demo**

A stunning 2D visualization showcasing autonomous drone swarm behaviors including flocking, consensus voting, network resilience, and formation flying.

![Demo Preview](https://img.shields.io/badge/Status-Demo-brightgreen) ![Java](https://img.shields.io/badge/Java-11+-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-17-blue)

---

## Quick Start (< 5 minutes)

### Prerequisites
- **Java 11 or higher** ([Download here](https://adoptium.net/))
- **Maven 3.6+** ([Download here](https://maven.apache.org/download.cgi))

Check your installations:
```bash
java -version    # Should be 11+
mvn -version     # Should be 3.6+
```

### Run the Demo

**Option 1: Using Maven (Recommended)**
```bash
cd demo
mvn clean javafx:run
```

**Option 2: Build and Run JAR**
```bash
cd demo
mvn clean package
java --module-path $PATH_TO_FX --add-modules javafx.controls -jar target/ascs-demo-1.0-SNAPSHOT.jar
```

**Option 3: Quick Run Script**

On **macOS/Linux**:
```bash
cd demo
chmod +x run.sh
./run.sh
```

On **Windows**:
```bash
cd demo
run.bat
```

---

## What You'll See

### Main Interface

```
┌─────────────────────────────────────────────────────────────────┐
│ Autonomous Swarm Coordination System - Interactive Demo         │
│ Agents: 12  |  FPS: 60  |  Consensus: Idle  |  Network: 100%   │
├─────────────────────────────────────┬───────────────────────────┤
│                                     │  Agent Controls           │
│                                     │  [Spawn] [Remove]         │
│         CANVAS (900x700)            │                           │
│     • Agents with flocking          │  Formation Presets        │
│     • Communication links           │  [Line] [V] [Circle] [Grid]
│     • Real-time movement            │                           │
│     • Click to set waypoints        │  Flocking Parameters      │
│                                     │  Separation: ━━●━━━       │
│                                     │  Alignment:  ━━●━━━       │
│                                     │  Cohesion:   ━━●━━━       │
│                                     │                           │
│                                     │  Network Quality          │
│                                     │  Quality: ━━━━━●         │
│                                     │                           │
│                                     │  Visualization            │
│                                     │  ☑ Show Comm Links        │
│                                     │  [Clear Waypoints]        │
├─────────────────────────────────────┴───────────────────────────┤
│ [A: Basic Flocking] [B: Consensus] [C: Network] [D: Formation] │
└─────────────────────────────────────────────────────────────────┘
```

---

## Demo Scenarios

### Scenario A: Basic Flocking
**What it demonstrates:** Emergent swarm behavior from simple rules

**Algorithm:** Reynolds' Boids
- **Separation:** Avoid crowding neighbors
- **Alignment:** Steer towards average heading
- **Cohesion:** Move towards center of mass

**How to run:**
1. Click **"A: Basic Flocking"** button
2. Watch agents self-organize into a cohesive swarm
3. Try adjusting sliders to see behavior changes

**What to look for:**
- Smooth, coordinated movement without central control
- Agents maintain spacing while staying together
- Natural-looking flock dynamics

---

### Scenario B: Consensus Voting
**What it demonstrates:** Distributed decision-making without a leader

**Algorithm:** Democratic consensus
- Each agent votes randomly (Option A or B)
- Progress bar shows voting completion (3 seconds)
- Final result displayed with vote counts

**How to run:**
1. Click **"B: Consensus Vote"** button
2. Agents turn ORANGE (voting in progress)
3. Agents turn LIME green (decision made)
4. Result shown: "Consensus: Option A (7 vs 5)"

**What to look for:**
- Color changes indicating voting states
- Real-time consensus progress
- Final vote tally

---

### Scenario C: Network Degradation
**What it demonstrates:** Swarm adaptation to communication failures

**Algorithm:** Simulated packet loss
- Network quality drops from 100% → 30%
- Communication links flicker/disappear
- Agents turn RED when connection is poor
- Network recovers back to 100%

**How to run:**
1. Click **"C: Network Degradation"** button
2. Watch network quality decrease
3. Observe swarm adaptation
4. See recovery as network restores

**What to look for:**
- Communication links becoming sparse
- Agents changing to RED state
- Swarm maintaining cohesion despite packet loss
- Graceful recovery

---

### Scenario D: Formation Flying
**What it demonstrates:** Coordinated movement and precision control

**Algorithm:** Waypoint-based formation control
- Automatically sequences through formations:
  1. Line formation (3 seconds)
  2. V-formation (3 seconds)
  3. Circle formation (3 seconds)
  4. Grid formation (3 seconds)

**How to run:**
1. Click **"D: Formation Flying"** button
2. Sit back and watch the choreographed sequence

**What to look for:**
- Smooth transitions between formations
- Agents converging to precise positions
- Coordinated timing across all agents
- Professional-looking maneuvers

---

## Interactive Controls

### Agent Management
- **Spawn Agent:** Add a new agent with random position/velocity
- **Remove Agent:** Remove the most recently added agent

### Manual Formations
- **Line:** Horizontal line formation
- **V-Formation:** Classic bird flight pattern
- **Circle:** Circular perimeter formation
- **Grid:** Organized grid pattern

### Flocking Parameters
Adjust the three core flocking behaviors in real-time:

- **Separation (0-3):** How strongly agents avoid each other
  - Low (0): Agents clump together
  - High (3): Strong personal space

- **Alignment (0-3):** How strongly agents match neighbors' velocity
  - Low (0): Chaotic individual movement
  - High (3): Synchronized movement

- **Cohesion (0-3):** How strongly agents move toward swarm center
  - Low (0): Swarm spreads out
  - High (3): Tight clustering

### Network Quality (0-1)
Simulate communication issues:
- **1.0 (100%):** Perfect communication
- **0.5 (50%):** 50% packet loss
- **0.3 (30%):** Severe network degradation

### Waypoint Setting
- **Click anywhere on canvas:** All agents fly to clicked point
- **Clear All Waypoints:** Stop target-seeking behavior

---

## Visual Elements

### Agent Colors
- **CYAN:** Active, normal operation
- **ORANGE:** Voting in progress
- **LIME GREEN:** Decision made
- **RED:** Network connectivity issues

### Communication Links
- **Blue lines:** Active communication between agents
- **Opacity:** Indicates network quality
- **Toggle:** Use checkbox to show/hide

### Agent Representation
- **Circle:** Agent body
- **Line:** Heading direction
- **Faint circle:** Communication radius (when < 10 agents)

---

## Key Algorithms Explained

### 1. Flocking (Reynolds' Boids, 1986)

Each agent calculates three steering forces:

```java
// Separation: Avoid nearby agents
for each neighbor within SEPARATION_RADIUS:
    force += (my_position - neighbor_position) / distance

// Alignment: Match neighbor velocities
for each neighbor within ALIGNMENT_RADIUS:
    average_velocity += neighbor_velocity
force += (average_velocity - my_velocity)

// Cohesion: Move toward center of mass
for each neighbor within COHESION_RADIUS:
    center_of_mass += neighbor_position
force += (center_of_mass - my_position)
```

**Parameters:**
- Separation radius: 25 pixels
- Alignment radius: 50 pixels
- Cohesion radius: 50 pixels
- Communication radius: 100 pixels

---

### 2. Consensus Voting

Simple majority vote with visualization:

```java
1. Voting starts: all agents vote (random A or B)
2. Agents colored ORANGE (voting state)
3. Progress bar shows time elapsed
4. After 3 seconds: tally votes
5. Agents colored GREEN (decision made)
6. Display result: "Option A (7 vs 5)"
```

**Real-world applications:**
- Target selection
- Path planning decisions
- Task allocation

---

### 3. Network Simulation

Probabilistic packet loss model:

```java
// For each communication attempt:
if (random() < network_quality) {
    // Message delivered
    apply_neighbor_influence()
} else {
    // Packet lost
    ignore_neighbor()
}
```

**Effects:**
- Network quality 1.0 → 0.3: gradual degradation
- Agents adapt by using available information
- Swarm remains functional even at 30% quality

---

### 4. Formation Control

Target-based waypoint seeking:

```java
// Calculate desired velocity toward target
desired = (target - position).normalize() * max_speed

// Steering force
steering = desired - current_velocity

// Apply force (limited to prevent jerky movement)
acceleration += limit(steering, max_force)
```

**Formation types:**
- **Line:** Evenly spaced along horizontal axis
- **V:** Leader at tip, followers in diagonal lines
- **Circle:** Evenly distributed on circular perimeter
- **Grid:** Organized rows and columns

---

## Performance Notes

- **Target FPS:** 60
- **Recommended agents:** 10-20 (smooth on most hardware)
- **Maximum tested:** 50 agents (may vary by system)
- **Physics:** Fixed timestep (1.0) for stability
- **Rendering:** JavaFX Canvas with hardware acceleration

**Optimization tips:**
- Disable communication links for swarms > 20 agents
- Lower network quality = less computation
- Formation controls pause organic flocking

---

## Keyboard Shortcuts

Currently, all controls are GUI-based. Future versions may include:
- `Space`: Pause/Resume
- `R`: Reset simulation
- `1-4`: Quick scenario selection
- `+/-`: Add/remove agents

---

## Troubleshooting

### "No JavaFX runtime found"
**Solution:** Install JavaFX or use `mvn javafx:run` which handles dependencies.

### Slow performance / Low FPS
**Solutions:**
- Reduce number of agents
- Disable communication links
- Close other applications
- Check Java version (11+ recommended)

### "Module not found" error
**Solution:** Ensure you're using `mvn javafx:run` or properly set module path:
```bash
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls \
     -jar ascs-demo-1.0-SNAPSHOT.jar
```

### Window doesn't appear
**Solution:** Check your display settings. Try:
```bash
mvn clean install
mvn javafx:run
```

---

## Integration with Main ASCS Project

This demo is **standalone** but designed for easy integration:

### Architecture Alignment
- Uses similar package structure: `com.team6.swarm.demo`
- Agent class mirrors main project's `Agent.java`
- Ready for EventBus integration

### Integration Steps
1. Copy `DemoAgent.java` concepts into main `Agent.java`
2. Replace simulation loop with EventBus subscriptions
3. Connect to existing `BehaviorConfiguration` classes
4. Integrate visualization into main UI framework

### Reusable Components
- **Flocking algorithm** → `SwarmCoordination/.../intelligence/`
- **Rendering code** → `SwarmCoordination/.../ui/Visualizer.java`
- **Controls** → `SwarmCoordination/.../ui/ControlPanel.java`

---

## Project Structure

```
demo/
├── pom.xml                          # Maven build configuration
├── README.md                        # This file
├── run.sh                           # macOS/Linux launcher
├── run.bat                          # Windows launcher
└── src/main/java/com/team6/swarm/demo/
    ├── DemoAgent.java              # Agent with flocking behavior
    └── SwarmDemo.java              # Main JavaFX application
```

**Lines of code:**
- `DemoAgent.java`: ~300 lines
- `SwarmDemo.java`: ~700 lines
- **Total: ~1000 lines of production-ready code**

---

## Technical Details

### Dependencies
- **JavaFX 17.0.2:** UI framework
- **Java 11+:** Runtime environment
- **Maven:** Build tool

### Build System
- Maven 3.6+
- Targets Java 11 bytecode
- Includes JavaFX Maven Plugin for easy running
- Shade plugin creates fat JAR

### Code Quality
- Clean separation of concerns
- Documented with JavaDoc comments
- Follows Java conventions
- No external dependencies beyond JavaFX

---

## Credits

**Developed for:** Autonomous Swarm Coordination System (ASCS)
**Team 6:**
- Sanidhaya Sharma (Core)
- John (Communication)
- Lauren (Intelligence)
- Anthony (UI)

**Algorithms:**
- Flocking: Based on Craig Reynolds' Boids (1986)
- Consensus: Democratic voting model
- Network simulation: Probabilistic packet loss

---

## Future Enhancements

**Planned features:**
- 3D visualization option
- More formation types (diamond, wedge, etc.)
- Advanced consensus algorithms (Raft, Byzantine)
- Obstacle avoidance
- Task allocation visualization
- Network topology display (graph view)
- Record/replay functionality
- Export to video

---

## License

Part of the ASCS project. See main repository for license details.

---

## Support

**Issues?** Open an issue on the main ASCS repository:
https://github.com/Major-Projects-Sanidhaya/Autonomous-Swarm-Coordination-System

**Questions?** Contact the team through the repository.

---

**Enjoy the demo! Watch your swarm come to life! 🚁✨**
