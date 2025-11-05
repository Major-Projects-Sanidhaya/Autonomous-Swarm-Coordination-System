# AgentState Class - Complete Documentation

**Package:** `com.team6.swarm.core`
**Week:** 1 - Foundation Files
**File:** `AgentState.java`
**Purpose:** Complete snapshot of an agent at any moment in time

---

## 🎯 What Is This Class?

Think of `AgentState` as the **"ID card" or "passport"** of each agent. Just like your ID card contains all your important information (name, photo, address, etc.), the `AgentState` class contains everything important about an agent at a specific moment in time.

### Real-World Analogy
Imagine you're managing delivery drones:
- **agentId** = Drone's serial number (e.g., "Drone #5")
- **position** = Current GPS coordinates
- **velocity** = How fast and in what direction it's flying
- **batteryLevel** = Battery percentage (0% to 100%)
- **status** = Is it flying? Broken? Charging?

---

## 📋 Class Overview

```java
public class AgentState {
    // Identity
    public int agentId;           // Unique ID number
    public String agentName;      // Display name

    // Physical properties
    public Point2D position;      // Where is it?
    public Vector2D velocity;     // How fast? Which direction?

    // Status
    public AgentStatus status;    // Active? Failed? Low battery?
    public double batteryLevel;   // 0.0 to 1.0 (0% to 100%)

    // Capabilities
    public double maxSpeed;           // Maximum speed limit
    public double communicationRange; // How far can it talk to others?

    // Timing
    public long lastUpdateTime;   // When was this data last updated?
}
```

---

## 🔍 Field-by-Field Explanation

### 1. `agentId` (int)
**What it is:** A unique number that identifies this specific agent.

**Think of it like:** Your student ID number or employee ID.

**Example:**
```java
agentState.agentId = 42;  // This is agent number 42
```

**Why it matters:** When you have 100 agents, you need a way to tell them apart!

---

### 2. `agentName` (String)
**What it is:** A human-readable name for the agent.

**Think of it like:** Your actual name (while agentId is like your ID number).

**Example:**
```java
agentState.agentName = "Agent_42";  // More readable than just "42"
```

**Why it matters:** Makes debugging easier - "Agent_42 failed" is clearer than "42 failed".

---

### 3. `position` (Point2D)
**What it is:** The agent's current location in 2D space (X, Y coordinates).

**Think of it like:** GPS coordinates, but simpler (just X and Y, no Z).

**Example:**
```java
agentState.position = new Point2D(100.0, 200.0);
// Agent is at X=100, Y=200
```

**Visual representation:**
```
Y-axis (up/down)
↑
300 |
200 | • (Agent is here at 100, 200)
100 |
  0 |_ _ _ _ _ _ _ → X-axis (left/right)
    0  100  200  300
```

**Why it matters:** You need to know where your agent is to display it, move it, or check if it's near something.

---

### 4. `velocity` (Vector2D)
**What it is:** How fast the agent is moving and in what direction.

**Think of it like:** Speed + direction. Not just "30 mph" but "30 mph heading northeast".

**Example:**
```java
agentState.velocity = new Vector2D(10.0, 5.0);
// Moving right (X) at 10 units/second
// Moving up (Y) at 5 units/second
```

**Visual representation:**
```
       ↗ (Agent moving this direction)
      /  velocity = (10, 5)
     /
    • Agent
```

**Why it matters:** If you know velocity, you can predict where the agent will be next!

---

### 5. `status` (AgentStatus)
**What it is:** The current operational state of the agent.

**Think of it like:** A status light (green = good, yellow = warning, red = error).

**Possible values:**
- `ACTIVE` 🟢 - Working normally
- `INACTIVE` ⚪ - Turned off or idle
- `FAILED` 🔴 - Broken, needs fixing
- `BATTERY_LOW` 🟡 - Running out of power
- `MAINTENANCE` 🟠 - Being serviced

**Example:**
```java
agentState.status = AgentStatus.ACTIVE;  // Agent is working fine
agentState.status = AgentStatus.BATTERY_LOW;  // Uh oh, battery is low!
```

**Why it matters:** You don't want to assign important tasks to a broken or low-battery agent!

---

### 6. `batteryLevel` (double)
**What it is:** The agent's battery charge level.

**Think of it like:** Your phone's battery percentage.

**Range:** 0.0 to 1.0
- `0.0` = 0% (dead)
- `0.5` = 50% (half charged)
- `1.0` = 100% (fully charged)

**Example:**
```java
agentState.batteryLevel = 0.75;  // 75% battery remaining

// Check if low battery
if (agentState.batteryLevel < 0.2) {
    System.out.println("Warning: Battery below 20%!");
}
```

**Why it matters:** Agents need power to work. Low battery = can't complete tasks!

---

### 7. `maxSpeed` (double)
**What it is:** The maximum speed this agent can travel.

**Think of it like:** A car's top speed (some cars go 120 mph, others only 80 mph).

**Example:**
```java
agentState.maxSpeed = 50.0;  // Can't go faster than 50 units/second

// Check if going too fast
double currentSpeed = agentState.velocity.magnitude();
if (currentSpeed > agentState.maxSpeed) {
    System.out.println("Agent is speeding! Slow down!");
}
```

**Why it matters:** Prevents agents from going unrealistically fast. Each agent might have different capabilities.

---

### 8. `communicationRange` (double)
**What it is:** How far this agent can send/receive messages from other agents.

**Think of it like:** Walkie-talkie range. Some can reach 1 mile, others only 500 feet.

**Example:**
```java
agentState.communicationRange = 100.0;  // Can talk to agents within 100 units

// Check if two agents can communicate
double distance = agent1.position.distanceTo(agent2.position);
if (distance <= agent1.communicationRange) {
    System.out.println("Agents can talk to each other!");
}
```

**Why it matters:** In a swarm, agents need to coordinate. They can only talk if they're in range!

---

### 9. `lastUpdateTime` (long)
**What it is:** A timestamp of when this state was last updated.

**Think of it like:** The "Last Modified" date on a file.

**Value:** Milliseconds since January 1, 1970 (Unix timestamp).

**Example:**
```java
agentState.lastUpdateTime = System.currentTimeMillis();
// Records the current time

// Check how old the data is
long currentTime = System.currentTimeMillis();
long age = currentTime - agentState.lastUpdateTime;
if (age > 5000) {  // 5000 milliseconds = 5 seconds
    System.out.println("Warning: Data is more than 5 seconds old!");
}
```

**Why it matters:** In fast-moving simulations, old data is useless. This helps detect stale information.

---

## 🛠️ Methods (Functions)

### Constructor: `AgentState()`
**What it does:** Creates a new, empty AgentState with default values.

**Example:**
```java
AgentState state = new AgentState();
// Now you have a blank "form" to fill out
```

**What gets set automatically:**
- `batteryLevel = 1.0` (100% charged)
- `status = AgentStatus.ACTIVE` (working normally)
- All other fields are null or 0

---

### `toString()` - String
**What it does:** Converts all the agent information into a readable text format.

**Example:**
```java
AgentState state = new AgentState();
state.agentId = 5;
state.agentName = "Scout_5";
state.position = new Point2D(100, 200);
state.batteryLevel = 0.8;

System.out.println(state.toString());
// Output: "Agent_5 at (100.0, 200.0), Battery: 80%, Status: ACTIVE"
```

**Why it's useful:** Makes debugging easier. You can quickly see all the agent's information.

---

## 💡 Common Usage Patterns

### Pattern 1: Creating and Initializing an Agent State

```java
// Create new agent state
AgentState state = new AgentState();

// Fill in the details
state.agentId = 1;
state.agentName = "Agent_1";
state.position = new Point2D(400, 300);      // Start at center
state.velocity = new Vector2D(0, 0);          // Not moving yet
state.status = AgentStatus.ACTIVE;            // Ready to work
state.batteryLevel = 1.0;                     // Fully charged
state.maxSpeed = 50.0;                        // Max speed limit
state.communicationRange = 100.0;             // Communication distance
state.lastUpdateTime = System.currentTimeMillis();  // Current time
```

---

### Pattern 2: Checking Agent Health

```java
public boolean isAgentHealthy(AgentState state) {
    // Check battery
    if (state.batteryLevel < 0.2) {
        System.out.println("Agent battery is low!");
        return false;
    }

    // Check status
    if (state.status == AgentStatus.FAILED) {
        System.out.println("Agent has failed!");
        return false;
    }

    // Check if data is fresh (less than 10 seconds old)
    long age = System.currentTimeMillis() - state.lastUpdateTime;
    if (age > 10000) {  // 10 seconds
        System.out.println("Agent data is too old!");
        return false;
    }

    return true;  // Agent is healthy!
}
```

---

### Pattern 3: Updating Agent Position (Movement)

```java
public void updateAgentPosition(AgentState state, double deltaTime) {
    // deltaTime = how much time has passed (in seconds)

    // Move the agent based on its velocity
    // New position = old position + (velocity × time)
    double newX = state.position.x + (state.velocity.x * deltaTime);
    double newY = state.position.y + (state.velocity.y * deltaTime);

    state.position = new Point2D(newX, newY);

    // Update the timestamp
    state.lastUpdateTime = System.currentTimeMillis();
}

// Example usage:
AgentState myAgent = new AgentState();
myAgent.position = new Point2D(0, 0);
myAgent.velocity = new Vector2D(10, 5);  // Moving right and up

// After 1 second:
updateAgentPosition(myAgent, 1.0);
// Agent is now at (10, 5)

// After another 0.5 seconds:
updateAgentPosition(myAgent, 0.5);
// Agent is now at (15, 7.5)
```

---

### Pattern 4: Draining Battery Over Time

```java
public void drainBattery(AgentState state, double deltaTime) {
    // Battery drains faster when moving faster
    double speed = state.velocity.magnitude();
    double drainRate = 0.001;  // Base drain rate per second

    // Drain more when moving at high speed
    double speedFactor = speed / state.maxSpeed;  // 0.0 to 1.0
    double totalDrain = drainRate * (1.0 + speedFactor) * deltaTime;

    // Update battery level
    state.batteryLevel -= totalDrain;

    // Don't let it go below 0
    if (state.batteryLevel < 0) {
        state.batteryLevel = 0;
        state.status = AgentStatus.FAILED;  // Dead battery
    }

    // Warning at 20%
    if (state.batteryLevel < 0.2 && state.status == AgentStatus.ACTIVE) {
        state.status = AgentStatus.BATTERY_LOW;
    }
}
```

---

## 🎓 Beginner Tips

### Tip 1: Why use `public` fields?
```java
public double batteryLevel;  // Anyone can access this directly
```

**Reason:** This is a **Data Transfer Object (DTO)**. It's like a simple container to hold data and pass it around. We want easy access to read and modify the values.

**Alternative approach (more complex):**
```java
private double batteryLevel;  // Hidden

public double getBatteryLevel() {  // Getter
    return batteryLevel;
}

public void setBatteryLevel(double level) {  // Setter
    this.batteryLevel = level;
}
```
For a DTO, the simple `public` approach is fine!

---

### Tip 2: Understanding `Point2D` vs `Vector2D`

**Point2D** = A location (where something IS)
- "The treasure is at coordinates (100, 50)"

**Vector2D** = A direction with magnitude (how something MOVES)
- "Walk 10 steps north and 5 steps east"

```java
Point2D startPosition = new Point2D(0, 0);      // Where I am
Vector2D velocity = new Vector2D(10, 5);        // How I'm moving
Point2D newPosition = startPosition.add(velocity);  // Where I'll be
```

---

### Tip 3: Battery Math

Battery is stored as 0.0 to 1.0, but we think in percentages:

```java
// Setting battery to 75%
state.batteryLevel = 0.75;

// Converting to percentage for display
int percentage = (int)(state.batteryLevel * 100);
System.out.println("Battery: " + percentage + "%");  // "Battery: 75%"

// Converting from percentage input
int userInput = 50;  // User enters 50%
state.batteryLevel = userInput / 100.0;  // Store as 0.5
```

---

## 🔗 Integration with Other Classes

### Used by:
- **Agent.java** - The agent keeps its state in an AgentState object
- **AgentManager.java** - Manages multiple agent states
- **PhysicsEngine.java** - Updates position and velocity
- **PerformanceMonitor.java** - Reads state for metrics

### Works with:
- **Point2D** - Stores the position
- **Vector2D** - Stores the velocity
- **AgentStatus** - Stores the operational status

---

## 📝 Complete Example: Agent Dashboard

```java
public class AgentDashboard {
    public static void displayAgentInfo(AgentState state) {
        System.out.println("╔═══════════════════════════════════╗");
        System.out.println("║      AGENT INFORMATION            ║");
        System.out.println("╠═══════════════════════════════════╣");
        System.out.println("║ ID: " + state.agentId);
        System.out.println("║ Name: " + state.agentName);
        System.out.println("║");
        System.out.println("║ Position: (" + state.position.x + ", " +
                          state.position.y + ")");
        System.out.println("║ Velocity: (" + state.velocity.x + ", " +
                          state.velocity.y + ")");
        System.out.println("║");
        System.out.println("║ Status: " + state.status);
        System.out.println("║ Battery: " + (int)(state.batteryLevel * 100) + "%");
        System.out.println("║");
        System.out.println("║ Max Speed: " + state.maxSpeed);
        System.out.println("║ Comm Range: " + state.communicationRange);
        System.out.println("╚═══════════════════════════════════╝");
    }

    public static void main(String[] args) {
        // Create an agent state
        AgentState myAgent = new AgentState();
        myAgent.agentId = 1;
        myAgent.agentName = "Scout_Alpha";
        myAgent.position = new Point2D(250, 150);
        myAgent.velocity = new Vector2D(15, 8);
        myAgent.status = AgentStatus.ACTIVE;
        myAgent.batteryLevel = 0.85;
        myAgent.maxSpeed = 50.0;
        myAgent.communicationRange = 120.0;

        // Display it
        displayAgentInfo(myAgent);
    }
}
```

**Output:**
```
╔═══════════════════════════════════╗
║      AGENT INFORMATION            ║
╠═══════════════════════════════════╣
║ ID: 1
║ Name: Scout_Alpha
║
║ Position: (250.0, 150.0)
║ Velocity: (15.0, 8.0)
║
║ Status: ACTIVE
║ Battery: 85%
║
║ Max Speed: 50.0
║ Comm Range: 120.0
╚═══════════════════════════════════╝
```

---

## 🎯 Key Takeaways

1. **AgentState is a data container** - It just holds information, it doesn't "do" anything.
2. **All fields are public** - Easy to read and modify (it's a simple data object).
3. **It's a snapshot** - Captures agent info at one moment in time.
4. **Think of it as an ID card** - Contains all the important facts about an agent.
5. **Updated frequently** - In a 60 FPS simulation, this gets updated 60 times per second!

---

**Next Steps:**
- Read about **Agent.java** to see how AgentState is used
- Learn about **Point2D.java** and **Vector2D.java** for the math
- Understand **AgentStatus.java** for the status values

---
