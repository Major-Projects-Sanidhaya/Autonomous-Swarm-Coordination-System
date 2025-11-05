# Vector2D Class Documentation

**Package:** `com.team6.swarm.core`
**Week:** 1 - Foundation Files
**Purpose:** Represents direction and magnitude (velocity, force, acceleration)

---

## Overview

The `Vector2D` class represents a 2D vector with magnitude and direction. Used extensively for physics calculations, movement, flocking behaviors, and force application.

---

## Class Definition

```java
public class Vector2D
```

---

## Fields

| Field | Type | Access | Description |
|-------|------|--------|-------------|
| `x` | `double` | public | X-component of the vector |
| `y` | `double` | public | Y-component of the vector |

---

## Constructors

### `Vector2D(double x, double y)`
Creates a new vector with the specified components.

**Parameters:**
- `x` - The X-component
- `y` - The Y-component

**Example:**
```java
Vector2D velocity = new Vector2D(10.0, 5.0);
```

---

## Methods

### `add(Vector2D other)`
Adds another vector to this vector (vector addition).

**Parameters:**
- `other` - The vector to add

**Returns:** `Vector2D` - A new vector representing the sum

**Example:**
```java
Vector2D v1 = new Vector2D(3, 4);
Vector2D v2 = new Vector2D(1, 2);
Vector2D sum = v1.add(v2); // (4, 6)
```

---

### `subtract(Vector2D other)`
Subtracts another vector from this vector.

**Parameters:**
- `other` - The vector to subtract

**Returns:** `Vector2D` - A new vector representing the difference

**Example:**
```java
Vector2D v1 = new Vector2D(5, 5);
Vector2D v2 = new Vector2D(2, 3);
Vector2D diff = v1.subtract(v2); // (3, 2)
```

---

### `multiply(double scalar)`
Multiplies this vector by a scalar value (scales the vector).

**Parameters:**
- `scalar` - The scalar multiplier

**Returns:** `Vector2D` - A new scaled vector

**Example:**
```java
Vector2D v = new Vector2D(3, 4);
Vector2D scaled = v.multiply(2.0); // (6, 8)
```

---

### `magnitude()`
Calculates the length/magnitude of the vector.

**Returns:** `double` - The magnitude

**Formula:** √(x² + y²)

**Example:**
```java
Vector2D v = new Vector2D(3, 4);
double length = v.magnitude(); // Returns 5.0
```

---

### `normalize()`
Returns a unit vector (magnitude = 1) in the same direction.

**Returns:** `Vector2D` - Normalized vector

**Note:** Returns (0, 0) if magnitude is zero

**Example:**
```java
Vector2D v = new Vector2D(3, 4);
Vector2D unit = v.normalize(); // (0.6, 0.8)
```

---

### `dot(Vector2D other)`
Calculates the dot product with another vector.

**Parameters:**
- `other` - The other vector

**Returns:** `double` - The dot product

**Formula:** x₁*x₂ + y₁*y₂

**Example:**
```java
Vector2D v1 = new Vector2D(1, 2);
Vector2D v2 = new Vector2D(3, 4);
double dotProduct = v1.dot(v2); // Returns 11.0
```

---

### `limit(double maxMagnitude)`
Limits the vector's magnitude to a maximum value.

**Parameters:**
- `maxMagnitude` - The maximum allowed magnitude

**Returns:** `Vector2D` - Limited vector

**Example:**
```java
Vector2D v = new Vector2D(10, 10); // magnitude ≈ 14.14
Vector2D limited = v.limit(5.0);   // magnitude = 5.0
```

---

### `toString()`
Returns a string representation of the vector.

**Returns:** `String` - Format: "Vector2D(x, y)"

**Example:**
```java
Vector2D v = new Vector2D(3.5, 4.2);
System.out.println(v); // Output: "Vector2D(3.5, 4.2)"
```

---

## Usage Examples

### Physics Calculations
```java
// Agent velocity
Vector2D velocity = new Vector2D(10, 0); // Moving right at 10 units/sec

// Apply force (acceleration)
Vector2D force = new Vector2D(0, 5); // Upward force
velocity = velocity.add(force.multiply(0.1)); // Apply with damping

// Limit to max speed
double maxSpeed = 50.0;
velocity = velocity.limit(maxSpeed);
```

### Flocking Behavior
```java
// Separation force (avoid neighbors)
Vector2D separation = new Vector2D(-5, 0);

// Alignment force (match neighbor velocity)
Vector2D alignment = new Vector2D(10, 5);

// Cohesion force (move toward center)
Vector2D cohesion = new Vector2D(3, 2);

// Combine forces
Vector2D totalForce = separation
    .add(alignment.multiply(0.5))
    .add(cohesion.multiply(0.3));
```

### Steering Behaviors
```java
// Calculate desired velocity toward target
Point2D current = new Point2D(0, 0);
Point2D target = new Point2D(10, 10);

Vector2D desired = new Vector2D(
    target.x - current.x,
    target.y - current.y
).normalize().multiply(maxSpeed);

// Steering force
Vector2D currentVelocity = new Vector2D(5, 0);
Vector2D steer = desired.subtract(currentVelocity);
```

---

## Integration Points

- **Used by:** Agent, PhysicsEngine, MovementCommand, flocking algorithms
- **Dependencies:** None (fundamental class)
- **Related classes:** Point2D (represents position)

---

## Mathematical Properties

### Vector Operations
- **Addition:** Component-wise sum
- **Subtraction:** Component-wise difference
- **Scalar Multiplication:** Scale both components
- **Dot Product:** Measures alignment (0 = perpendicular, 1 = parallel)

### Common Use Cases
- **Velocity:** Direction and speed of movement
- **Force:** Applied to change velocity
- **Acceleration:** Rate of velocity change
- **Direction:** Normalized vector (magnitude = 1)

---

## Performance Considerations

- **Memory:** 16 bytes (2 doubles)
- **Operations:** All O(1) time complexity
- **Immutable:** Operations return new vectors (thread-safe)
- **Normalization:** Requires square root (relatively expensive)

---

## Design Pattern

**Value Object Pattern** - Immutable mathematical vector with value semantics.

---
