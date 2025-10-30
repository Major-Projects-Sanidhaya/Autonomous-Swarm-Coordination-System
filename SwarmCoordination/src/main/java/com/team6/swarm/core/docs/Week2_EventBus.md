# EventBus Class - Complete Documentation

**Package:** `com.team6.swarm.core`
**Week:** 2 - Communication Integration
**File:** `EventBus.java`
**Purpose:** Central message routing system for communication between components

---

## 🎯 What Is This Class?

Think of `EventBus` as a **"Post Office" or "Message Board"** for your swarm system. Instead of components talking directly to each other (which creates tangled code), they post messages to the EventBus, and the EventBus delivers them to whoever wants to receive them.

### Real-World Analogy

**Without EventBus (Messy):**
```
Agent → calls → AgentManager
Agent → calls → SystemController
Agent → calls → PerformanceMonitor
Agent → calls → UI

Problem: Agent needs to know about EVERYONE!
```

**With EventBus (Clean):**
```
Agent → posts update to → EventBus → delivers to → {AgentManager, SystemController, PerformanceMonitor, UI}

Benefit: Agent just posts once, EventBus handles delivery!
```

It's like posting on a bulletin board - you write your message once, and anyone interested can read it!

---

## 📋 Understanding Publish-Subscribe Pattern

### The Concept

**Publisher** = Someone who posts news
**Subscriber** = Someone who wants to hear certain news
**EventBus** = The bulletin board that connects them

###Example:

1. **John subscribes** to "Agent Position Updates"
2. **Agent publishes** "I'm at position (100, 200)"
3. **EventBus delivers** the message to John
4. **John receives** the update automatically

---

## 🏗️ Class Structure

```java
public class EventBus {
    // Storage: Maps event types to their listeners
    private final Map<Class<?>, List<Consumer<?>>> subscribers;

    public EventBus() {
        this.subscribers = new ConcurrentHashMap<>();
    }
}
```

**What's happening here?**
- `Map<Class<?>, List<Consumer<?>>>` - For each type of event, we keep a list of listeners
- `ConcurrentHashMap` - Thread-safe map (multiple threads can use it safely)

---

## 🔍 Methods Explained

### 1. `subscribe()` - Register to Receive Events

**What it does:** Tell the EventBus "I want to hear about this type of event".

**Method signature:**
```java
public <T> void subscribe(Class<T> eventType, Consumer<T> listener)
```

**Breaking it down:**
- `<T>` - Generic type (works with any event type)
- `Class<T> eventType` - What kind of events? (e.g., AgentStateUpdate.class)
- `Consumer<T> listener` - Function to call when event happens

**Simple Example:**
```java
EventBus eventBus = new EventBus();

// Subscribe to agent updates
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("Agent " + update.agentId + " moved!");
});

// Now whenever someone publishes an AgentStateUpdate,
// your code above will run automatically!
```

**Real Example with Details:**
```java
// Subscribe to monitor agent positions
eventBus.subscribe(AgentStateUpdate.class, update -> {
    // This code runs EVERY TIME an update is published
    AgentState state = update.agentState;

    System.out.println("=== Agent Update Received ===");
    System.out.println("Agent ID: " + update.agentId);
    System.out.println("Position: (" + state.position.x + ", " +
                      state.position.y + ")");
    System.out.println("Battery: " + (state.batteryLevel * 100) + "%");
    System.out.println("Status: " + state.status);
});
```

**Multiple Subscribers:**
```java
// John subscribes (for communication)
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("[John] Updating communication network...");
    // John's code here
});

// Lauren subscribes (for intelligence)
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("[Lauren] Analyzing agent behavior...");
    // Lauren's code here
});

// Anthony subscribes (for UI)
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("[Anthony] Refreshing display...");
    // Anthony's code here
});

// When ONE update is published, ALL THREE receive it!
```

---

### 2. `publish()` - Send an Event

**What it does:** Post a message that all subscribers will receive.

**Method signature:**
```java
public <T> void publish(T event)
```

**Breaking it down:**
- `<T>` - Generic type (any event object)
- `T event` - The event object to send

**Simple Example:**
```java
// Create an update
AgentStateUpdate update = new AgentStateUpdate();
update.agentId = 5;
update.agentState = myAgent.getState();
update.timestamp = System.currentTimeMillis();

// Publish it - all subscribers receive it immediately
eventBus.publish(update);
```

**Complete Flow Example:**
```java
public class EventBusDemo {
    public static void main(String[] args) {
        EventBus eventBus = new EventBus();

        // Step 1: Someone subscribes
        eventBus.subscribe(AgentStateUpdate.class, update -> {
            System.out.println("Received update for agent " + update.agentId);
        });

        // Step 2: Create an event
        AgentStateUpdate update = new AgentStateUpdate();
        update.agentId = 1;
        update.agentState = new AgentState();
        update.timestamp = System.currentTimeMillis();

        // Step 3: Publish it
        System.out.println("Publishing update...");
        eventBus.publish(update);
        // Output: "Received update for agent 1"

        // The subscriber's code ran automatically!
    }
}
```

---

### 3. `unsubscribe()` - Stop Receiving Events

**What it does:** Tell the EventBus "I don't want these events anymore".

**Method signature:**
```java
public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener)
```

**Example:**
```java
// Store the listener so we can unsubscribe later
Consumer<AgentStateUpdate> myListener = update -> {
    System.out.println("Agent " + update.agentId + " updated");
};

// Subscribe
eventBus.subscribe(AgentStateUpdate.class, myListener);

// ... do some work ...

// Unsubscribe (stop receiving events)
eventBus.unsubscribe(AgentStateUpdate.class, myListener);

// Now myListener won't receive any more updates
```

---

### 4. `publishFiltered()` - Send to Specific Subscribers

**What it does:** Publish an event, but only deliver to subscribers that pass a test.

**Method signature:**
```java
public <T> void publishFiltered(T event, Predicate<Consumer<T>> filter)
```

**Use case:** Communication range limits!

**Example - Only nearby agents:**
```java
// Publish communication event only to agents in range
CommunicationEvent commEvent = new CommunicationEvent(...);
commEvent.senderId = 5;
commEvent.range = 100.0;  // 100 unit range

eventBus.publishFiltered(commEvent, listener -> {
    // Check if the listening agent is in range
    // Only deliver if within 100 units of sender
    return isAgentInRange(listener, commEvent.senderId, 100.0);
});
```

**Another example - Priority filtering:**
```java
// Only send high-priority alerts to certain subscribers
SystemEvent alert = SystemEvent.error("CRITICAL", "System failure!");

eventBus.publishFiltered(alert, listener -> {
    // Only deliver to priority subscribers
    return isPrioritySubscriber(listener);
});
```

---

### 5. `getSubscriberCount()` - Check How Many Listeners

**What it does:** Count how many subscribers are listening to a specific event type.

**Method signature:**
```java
public <T> int getSubscriberCount(Class<T> eventType)
```

**Example:**
```java
// Check how many are listening to agent updates
int count = eventBus.getSubscriberCount(AgentStateUpdate.class);
System.out.println(count + " components are listening to agent updates");

// Useful for debugging
if (count == 0) {
    System.out.println("Warning: No one is listening to these events!");
}
```

---

### 6. `clearAll()` - Remove All Subscribers

**What it does:** Removes all subscribers from all event types.

**Method signature:**
```java
public void clearAll()
```

**Example:**
```java
// Reset the event bus (useful for testing)
eventBus.clearAll();
System.out.println("All subscribers removed");

// Now the event bus is empty
```

---

## 💡 Common Usage Patterns

### Pattern 1: Basic Setup

```java
public class SwarmSystem {
    private EventBus eventBus;
    private AgentManager agentManager;

    public void initialize() {
        // 1. Create event bus
        eventBus = new EventBus();

        // 2. Create components with event bus reference
        agentManager = new AgentManager(eventBus);

        // 3. Set up subscribers
        setupEventSubscribers();
    }

    private void setupEventSubscribers() {
        // Subscribe to different event types
        eventBus.subscribe(AgentStateUpdate.class, this::handleAgentUpdate);
        eventBus.subscribe(TaskCompletionReport.class, this::handleTaskComplete);
        eventBus.subscribe(SystemEvent.class, this::handleSystemEvent);
    }

    private void handleAgentUpdate(AgentStateUpdate update) {
        // Handle agent updates
    }

    private void handleTaskComplete(TaskCompletionReport report) {
        // Handle task completions
    }

    private void handleSystemEvent(SystemEvent event) {
        // Handle system events
    }
}
```

---

### Pattern 2: Agent Publishing Updates

```java
public class Agent {
    private AgentState state;
    private EventBus eventBus;

    public void update(double deltaTime) {
        // Update agent position
        updatePosition(deltaTime);

        // Publish state change
        publishStateUpdate();
    }

    private void publishStateUpdate() {
        // Create update message
        AgentStateUpdate update = new AgentStateUpdate();
        update.agentId = state.agentId;
        update.agentState = state;
        update.updateType = AgentStateUpdate.UpdateType.POSITION_ONLY;
        update.timestamp = System.currentTimeMillis();

        // Publish to event bus
        // Everyone subscribed will receive this!
        eventBus.publish(update);
    }
}
```

---

### Pattern 3: Multiple Event Types

```java
public class SystemMonitor {
    private EventBus eventBus;

    public void setupMonitoring() {
        // Monitor different types of events

        // 1. Agent updates
        eventBus.subscribe(AgentStateUpdate.class, this::monitorAgentState);

        // 2. Task completions
        eventBus.subscribe(TaskCompletionReport.class, this::monitorTasks);

        // 3. System events
        eventBus.subscribe(SystemEvent.class, this::monitorSystemHealth);

        // 4. Performance metrics
        eventBus.subscribe(SystemMetrics.class, this::monitorPerformance);
    }

    private void monitorAgentState(AgentStateUpdate update) {
        System.out.println("[Monitor] Agent " + update.agentId + " state changed");
    }

    private void monitorTasks(TaskCompletionReport report) {
        System.out.println("[Monitor] Task " + report.taskId + ": " + report.status);
    }

    private void monitorSystemHealth(SystemEvent event) {
        if (event.getSeverity() == SystemEvent.Severity.ERROR) {
            System.err.println("[Monitor] ERROR: " + event.getMessage());
        }
    }

    private void monitorPerformance(SystemMetrics metrics) {
        System.out.println("[Monitor] FPS: " + metrics.updatesPerSecond);
    }
}
```

---

### Pattern 4: Error Handling in Subscribers

```java
// The EventBus automatically catches exceptions in subscribers!

eventBus.subscribe(AgentStateUpdate.class, update -> {
    try {
        // Your code that might fail
        processUpdate(update);
    } catch (Exception e) {
        // Handle your own errors
        System.err.println("Error processing update: " + e.getMessage());
        e.printStackTrace();
    }
});

// Even if one subscriber throws an exception,
// other subscribers still receive the event!
```

---

## 🎓 Beginner Concepts Explained

### What is `Consumer<T>`?

**Consumer** is a function that:
- Takes one input (of type T)
- Returns nothing (void)
- Just "consumes" or processes the input

**Example:**
```java
// This is a Consumer<String>
Consumer<String> printer = message -> {
    System.out.println(message);
};

// Use it:
printer.accept("Hello!");  // Prints: Hello!

// In EventBus, the Consumer processes events:
Consumer<AgentStateUpdate> updater = update -> {
    // Process the update
    handleUpdate(update);
};
```

---

### What is Lambda Expression `->` ?

Lambda is shorthand for writing simple functions.

**Old way (anonymous class):**
```java
eventBus.subscribe(AgentStateUpdate.class, new Consumer<AgentStateUpdate>() {
    @Override
    public void accept(AgentStateUpdate update) {
        System.out.println("Agent " + update.agentId);
    }
});
```

**New way (lambda):**
```java
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("Agent " + update.agentId);
});
```

**Breaking down the lambda:**
```java
update -> { ... }
  ↑        ↑
  |        |
input   what to do with it
```

---

### What is `Class<T>`?

`Class<T>` represents the type itself (not an instance).

**Example:**
```java
// This is an INSTANCE of AgentStateUpdate
AgentStateUpdate update = new AgentStateUpdate();

// This is the CLASS/TYPE of AgentStateUpdate
Class<AgentStateUpdate> updateClass = AgentStateUpdate.class;

// We use .class to identify which TYPE of events we want
eventBus.subscribe(AgentStateUpdate.class, ...);
//                 ↑
//                 The TYPE, not an instance
```

---

### Thread Safety - Why `ConcurrentHashMap`?

**Problem without thread safety:**
```
Thread 1: Publishing event
Thread 2: Adding subscriber
Thread 3: Removing subscriber

→ CRASH! Concurrent modification!
```

**Solution: ConcurrentHashMap**
- Multiple threads can read/write safely
- No crashes from concurrent access
- Perfect for multi-threaded simulations!

---

## 🔗 Integration Examples

### Integration 1: With AgentManager

```java
public class AgentManager {
    private EventBus eventBus;
    private Map<Integer, Agent> agents;

    public AgentManager(EventBus eventBus) {
        this.eventBus = eventBus;

        // Subscribe to system commands
        eventBus.subscribe(SystemCommand.class, this::handleCommand);
    }

    private void handleCommand(SystemCommand cmd) {
        switch (cmd.commandType) {
            case SPAWN_AGENT:
                Point2D position = (Point2D) cmd.getParameter("position");
                createAgent(position);
                break;

            case REMOVE_AGENT:
                int agentId = (Integer) cmd.getParameter("agentId");
                removeAgent(agentId);
                break;
        }
    }

    private void createAgent(Point2D position) {
        Agent agent = new Agent(nextId++, position);
        agents.put(agent.getState().agentId, agent);

        // Publish creation event
        SystemEvent event = SystemEvent.info(
            "AGENT_CREATED",
            String.valueOf(agent.getState().agentId),
            "New agent created at " + position
        );
        eventBus.publish(event);
    }
}
```

---

### Integration 2: With SystemController

```java
public class SystemController {
    private EventBus eventBus;
    private SystemMetrics metrics;

    public void initialize() {
        eventBus = new EventBus();
        metrics = new SystemMetrics();

        // Subscribe to all events for monitoring
        eventBus.subscribe(AgentStateUpdate.class, this::trackStateUpdate);
        eventBus.subscribe(TaskCompletionReport.class, this::trackTaskCompletion);
    }

    private void trackStateUpdate(AgentStateUpdate update) {
        metrics.recordStateUpdate();
    }

    private void trackTaskCompletion(TaskCompletionReport report) {
        metrics.recordTaskCompletion(report.status);
    }
}
```

---

## 📊 Performance Considerations

### Subscriber Count Impact

```java
// Few subscribers (fast)
1 event published → delivered to 3 subscribers → Fast!

// Many subscribers (slower)
1 event published → delivered to 100 subscribers → Takes time!
```

**Best practice:** Only subscribe to events you actually need!

---

### Event Frequency

```java
// Low frequency (fine)
10 events per second → Easy to handle

// High frequency (heavy)
1000 events per second → CPU intensive!
```

**Optimization:** Batch updates when possible.

---

## 🎯 Key Takeaways

1. **EventBus decouples components** - They don't need to know about each other
2. **Publish-Subscribe pattern** - One publisher, many subscribers
3. **Type-safe** - Can't subscribe to wrong event type
4. **Thread-safe** - Multiple threads can use it safely
5. **Automatic delivery** - Subscribers get events automatically
6. **Error isolation** - One subscriber's error doesn't affect others

---

## 🐛 Common Mistakes

### Mistake 1: Forgetting to subscribe

```java
// WRONG - Nothing happens!
EventBus eventBus = new EventBus();
eventBus.publish(new AgentStateUpdate());  // No subscribers!

// RIGHT - Subscribe first
eventBus.subscribe(AgentStateUpdate.class, update -> {
    System.out.println("Received!");
});
eventBus.publish(new AgentStateUpdate());  // Now it works!
```

---

### Mistake 2: Publishing wrong type

```java
// Subscribe to AgentStateUpdate
eventBus.subscribe(AgentStateUpdate.class, update -> {
    // This code
});

// But publish SystemEvent
eventBus.publish(new SystemEvent(...));  // Subscriber won't receive this!
```

---

### Mistake 3: Memory leaks

```java
// BAD - Subscriber never removed
for (int i = 0; i < 1000; i++) {
    eventBus.subscribe(AgentStateUpdate.class, update -> {
        // Subscriber stays in memory forever!
    });
}

// GOOD - Unsubscribe when done
Consumer<AgentStateUpdate> listener = update -> { ... };
eventBus.subscribe(AgentStateUpdate.class, listener);
// ... use it ...
eventBus.unsubscribe(AgentStateUpdate.class, listener);
```

---

## 📝 Complete Example

```java
public class EventBusCompleteExample {
    public static void main(String[] args) {
        // 1. Create event bus
        EventBus eventBus = new EventBus();

        // 2. Set up subscribers
        System.out.println("=== Setting up subscribers ===");

        eventBus.subscribe(AgentStateUpdate.class, update -> {
            System.out.println("[Subscriber 1] Agent " + update.agentId +
                " at (" + update.agentState.position.x + ", " +
                update.agentState.position.y + ")");
        });

        eventBus.subscribe(AgentStateUpdate.class, update -> {
            System.out.println("[Subscriber 2] Battery: " +
                (update.agentState.batteryLevel * 100) + "%");
        });

        // 3. Check subscriber count
        int count = eventBus.getSubscriberCount(AgentStateUpdate.class);
        System.out.println("Number of subscribers: " + count);

        // 4. Publish events
        System.out.println("\n=== Publishing events ===");

        for (int i = 1; i <= 3; i++) {
            AgentStateUpdate update = new AgentStateUpdate();
            update.agentId = i;

            AgentState state = new AgentState();
            state.position = new Point2D(i * 100, i * 50);
            state.batteryLevel = 1.0 - (i * 0.1);

            update.agentState = state;
            update.timestamp = System.currentTimeMillis();

            System.out.println("\nPublishing update for agent " + i + ":");
            eventBus.publish(update);
        }

        // 5. Cleanup
        System.out.println("\n=== Cleanup ===");
        eventBus.clearAll();
        System.out.println("All subscribers cleared");
    }
}
```

**Output:**
```
=== Setting up subscribers ===
Number of subscribers: 2

=== Publishing events ===

Publishing update for agent 1:
[Subscriber 1] Agent 1 at (100.0, 50.0)
[Subscriber 2] Battery: 90.0%

Publishing update for agent 2:
[Subscriber 1] Agent 2 at (200.0, 100.0)
[Subscriber 2] Battery: 80.0%

Publishing update for agent 3:
[Subscriber 1] Agent 3 at (300.0, 150.0)
[Subscriber 2] Battery: 70.0%

=== Cleanup ===
All subscribers cleared
```

---

**Next Steps:**
- Read about **AgentStateUpdate.java** to understand the message format
- Learn about **SystemController.java** to see the complete system setup
- Study **CommunicationEvent.java** for agent-to-agent messaging

---
