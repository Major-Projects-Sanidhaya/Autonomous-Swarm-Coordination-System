# AgentStatus Enum Documentation

**Package:** `com.team6.swarm.core`
**Week:** 1 - Foundation Files
**Purpose:** Define all possible states an agent can be in

---

## Overview

The `AgentStatus` enum represents the operational state of an agent at any given moment. Used throughout the system to check agent health and determine if agents can perform tasks.

---

## Enum Definition

```java
public enum AgentStatus
```

---

## Values

| Value | Description | Meaning |
|-------|-------------|---------|
| `ACTIVE` | Agent is fully operational | Can accept and execute tasks normally |
| `INACTIVE` | Agent is powered down or idle | Not participating in swarm operations |
| `FAILED` | Agent has encountered a critical error | Cannot operate, needs recovery |
| `BATTERY_LOW` | Battery level is critically low | Limited functionality, should recharge |
| `MAINTENANCE` | Agent is undergoing maintenance | Temporarily unavailable |

---

## Usage Examples

### Checking Agent Status
```java
Agent agent = agentManager.getAgent(1);
AgentStatus status = agent.getState().status;

if (status == AgentStatus.ACTIVE) {
    // Agent can accept tasks
    assignTask(agent);
} else if (status == AgentStatus.BATTERY_LOW) {
    // Send to charging station
    sendToChargingStation(agent);
} else if (status == AgentStatus.FAILED) {
    // Remove from active pool
    removeAgent(agent);
}
```

### Filtering Active Agents
```java
List<Agent> activeAgents = allAgents.stream()
    .filter(a -> a.getState().status == AgentStatus.ACTIVE)
    .collect(Collectors.toList());
```

### Status Transitions
```java
// Normal operation
agent.status = AgentStatus.ACTIVE;

// Battery depleted
if (batteryLevel < 0.2) {
    agent.status = AgentStatus.BATTERY_LOW;
}

// Critical failure
if (batteryLevel <= 0) {
    agent.status = AgentStatus.FAILED;
}
```

---

## State Transition Rules

```
INACTIVE → ACTIVE (on activation)
ACTIVE → BATTERY_LOW (battery < 20%)
ACTIVE → FAILED (critical error)
BATTERY_LOW → ACTIVE (after recharge)
BATTERY_LOW → FAILED (battery depleted)
MAINTENANCE → ACTIVE (after repairs)
FAILED → (terminal state, agent must be recreated)
```

---

## Integration Points

- **Used by:** Agent, AgentState, AgentCapabilities, SystemMetrics
- **Monitored by:** SystemController, AgentManager, PerformanceMonitor
- **Affects:** Task assignment, capability calculations, system metrics

---

## Design Pattern

**State Pattern** - Represents distinct states with different behaviors.

---
