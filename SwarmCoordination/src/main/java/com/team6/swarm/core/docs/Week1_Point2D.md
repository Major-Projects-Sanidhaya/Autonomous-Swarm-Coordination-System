# Point2D Class Documentation

**Package:** `com.team6.swarm.core`
**Week:** 1 - Foundation Files
**Purpose:** Represents X,Y coordinates in 2D space

---

## Overview

The `Point2D` class is a fundamental data structure that represents a position in 2D Cartesian coordinates. Every agent has a position, every target has coordinates, and this class is used throughout the system for spatial calculations.

---

## Class Definition

```java
public class Point2D
```

---

## Fields

| Field | Type | Access | Description |
|-------|------|--------|-------------|
| `x` | `double` | public | X-coordinate in 2D space |
| `y` | `double` | public | Y-coordinate in 2D space |

---

## Constructors

### `Point2D(double x, double y)`
Creates a new point at the specified coordinates.

**Parameters:**
- `x` - The X-coordinate
- `y` - The Y-coordinate

**Example:**
```java
Point2D position = new Point2D(100.0, 200.0);
```

---

## Methods

### `distanceTo(Point2D other)`
Calculates the Euclidean distance between this point and another point.

**Parameters:**
- `other` - The other point to measure distance to

**Returns:** `double` - The distance between the two points

**Formula:** √((x₂-x₁)² + (y₂-y₁)²)

**Example:**
```java
Point2D p1 = new Point2D(0, 0);
Point2D p2 = new Point2D(3, 4);
double distance = p1.distanceTo(p2); // Returns 5.0
```

---

### `add(Vector2D vector)`
Returns a new point offset by the given vector.

**Parameters:**
- `vector` - The vector to add to this point

**Returns:** `Point2D` - A new point at the offset position

**Example:**
```java
Point2D pos = new Point2D(10, 20);
Vector2D offset = new Vector2D(5, 5);
Point2D newPos = pos.add(offset); // (15, 25)
```

---

### `subtract(Point2D other)`
Calculates the vector from another point to this point.

**Parameters:**
- `other` - The point to subtract from this point

**Returns:** `Vector2D` - The vector difference

**Example:**
```java
Point2D p1 = new Point2D(10, 10);
Point2D p2 = new Point2D(5, 5);
Vector2D diff = p1.subtract(p2); // Vector2D(5, 5)
```

---

### `toString()`
Returns a string representation of the point.

**Returns:** `String` - Format: "(x, y)"

**Example:**
```java
Point2D p = new Point2D(100.5, 200.3);
System.out.println(p.toString()); // Output: "(100.5, 200.3)"
```

---

## Usage Examples

### Basic Usage
```java
// Create a position
Point2D agentPosition = new Point2D(400.0, 300.0);

// Calculate distance to target
Point2D targetPosition = new Point2D(500.0, 400.0);
double distance = agentPosition.distanceTo(targetPosition);

// Move position by velocity
Vector2D velocity = new Vector2D(10.0, 5.0);
Point2D newPosition = agentPosition.add(velocity);
```

### In Agent System
```java
// Initialize agent at spawn point
Point2D spawnPoint = new Point2D(100, 100);
Agent agent = new Agent(1, spawnPoint);

// Check if agent reached target
Point2D target = new Point2D(200, 200);
if (agent.getState().position.distanceTo(target) < 5.0) {
    System.out.println("Agent reached target!");
}
```

---

## Integration Points

- **Used by:** Agent, AgentState, Task, BoundaryManager, PhysicsEngine
- **Dependencies:** None (fundamental class)
- **Related classes:** Vector2D (represents direction/velocity)

---

## Performance Considerations

- **Memory:** 16 bytes (2 doubles)
- **Distance calculation:** O(1) time complexity
- **Thread-safe:** Immutable operations (add/subtract return new objects)

---

## Design Pattern

**Value Object Pattern** - Represents a value with no conceptual identity. Two points with the same coordinates are considered equal.

---
