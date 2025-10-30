# Core Package Documentation Index

**Package:** `com.team6.swarm.core`
**Version:** Week 1-6 Complete
**Last Updated:** 2025-10-30
**Author:** Sanidhaya (Team 6)

---

## 📚 Documentation Files

### Main Documentation
1. **[COMPLETE_API_REFERENCE.md](COMPLETE_API_REFERENCE.md)** - Complete API documentation for all 27 classes
2. **[QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)** - Beginner-friendly guide with examples
3. **[README.md](README.md)** - Documentation structure overview

---

## 📊 Project Statistics

| Metric | Value |
|--------|-------|
| **Total Classes** | 27 |
| **Total Lines of Code** | ~4,500+ |
| **Weeks Completed** | 1-6 (100%) |
| **Design Patterns** | 10+ |
| **Test Files** | 3 |
| **Documentation Files** | 3 |

---

## 🗂️ Class Organization

### By Week

| Week | Files | Purpose |
|------|-------|---------|
| **Week 1** | 7 files | Foundation: Basic data structures & agent creation |
| **Week 2** | 5 files | Communication: EventBus integration |
| **Week 3** | 5 files | Movement: Command processing & physics |
| **Week 4** | 5 files | UI Integration: Visualization & metrics |
| **Week 5-6** | 4 files | Advanced: Capabilities, tasks, boundaries, performance |

### By Category

#### Data Structures (2)
- Point2D - 2D coordinates
- Vector2D - Direction and magnitude

#### Enums (5)
- AgentStatus - Agent operational states
- MovementType - Types of movement commands
- CommandPriority - Command urgency levels
- CommandType - User command types
- + Inner enums in other classes

#### Core Classes (9)
- Agent - Individual autonomous agent
- AgentState - Agent snapshot data
- AgentManager - Agent lifecycle management
- PhysicsEngine - Movement physics
- EventBus - Event routing
- SystemController - System orchestration
- Task - Work assignments
- AgentCapabilities - Performance tracking
- BoundaryManager - Spatial constraints

#### Communication Classes (3)
- AgentStateUpdate - Position change messages
- CommunicationEvent - Agent-to-agent messages
- TaskCompletionReport - Task feedback

#### UI/Metrics Classes (4)
- VisualizationUpdate - Display data package
- SystemMetrics - Performance metrics
- SystemEvent - Event notifications
- SystemCommand - User commands

#### Monitoring Classes (1)
- PerformanceMonitor - System performance tracking

#### Test Classes (3)
- SimpleTest - Basic functionality test
- Week3IntegrationTest - Movement integration test
- Week4IntegrationTest - UI integration test

---

## 🎯 Quick Navigation

### For Beginners
1. Start with [QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)
2. Run SimpleTest.java
3. Try the examples in the quick start guide

### For Developers
1. Review [COMPLETE_API_REFERENCE.md](COMPLETE_API_REFERENCE.md)
2. Understand the design patterns section
3. Study the integration points

### For Architects
1. Review the architecture overview in Quick Start
2. Check design patterns summary in API Reference
3. Review thread safety notes

---

## 🔍 Finding What You Need

### "How do I create an agent?"
→ [QUICK_START_GUIDE.md](QUICK_START_GUIDE.md#creating-your-first-agent)
→ [COMPLETE_API_REFERENCE.md - Agent Class](COMPLETE_API_REFERENCE.md#agent-class)

### "How does the event system work?"
→ [COMPLETE_API_REFERENCE.md - EventBus Class](COMPLETE_API_REFERENCE.md#eventbus-class)
→ [QUICK_START_GUIDE.md - Example 3](QUICK_START_GUIDE.md#example-3-event-driven-monitoring)

### "How do I move an agent?"
→ [QUICK_START_GUIDE.md - Moving an Agent](QUICK_START_GUIDE.md#1-moving-an-agent)
→ [COMPLETE_API_REFERENCE.md - MovementCommand](COMPLETE_API_REFERENCE.md#movementcommand-class)

### "How do I set boundaries?"
→ [QUICK_START_GUIDE.md - Setting Boundaries](QUICK_START_GUIDE.md#4-setting-boundaries)
→ [COMPLETE_API_REFERENCE.md - BoundaryManager](COMPLETE_API_REFERENCE.md#boundarymanager-class)

### "How do I monitor performance?"
→ [QUICK_START_GUIDE.md - Example 4](QUICK_START_GUIDE.md#example-4-performance-monitoring)
→ [COMPLETE_API_REFERENCE.md - PerformanceMonitor](COMPLETE_API_REFERENCE.md#performancemonitor-class)

### "What functions does [Class] have?"
→ [COMPLETE_API_REFERENCE.md](COMPLETE_API_REFERENCE.md) - Search for class name

---

## 📖 Documentation Coverage

### Complete Documentation (100%)

All 27 classes have:
- ✅ Purpose description
- ✅ Field documentation
- ✅ Method signatures
- ✅ Method descriptions
- ✅ Usage examples
- ✅ Integration points
- ✅ Design patterns used

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────┐
│           SystemController                   │
│  (Orchestrates entire system)                │
└───────────┬─────────────────────────────────┘
            │
    ┌───────┴────────┬───────────────┬─────────────┐
    │                │               │             │
┌───▼────┐    ┌─────▼──────┐   ┌───▼────┐   ┌────▼────┐
│ Agent  │    │  EventBus  │   │Physics │   │Boundary │
│Manager │    │            │   │Engine  │   │Manager  │
└───┬────┘    └─────┬──────┘   └────────┘   └─────────┘
    │               │
┌───▼────────┐      │
│  Agent(s)  │◄─────┘
│            │
│ - State    │
│ - Commands │
│ - Physics  │
└────────────┘

Additional Components:
- PerformanceMonitor (monitors all)
- Task (work assignments)
- AgentCapabilities (performance tracking)
```

---

## 🔧 Integration Points

### With John's Communication Package
- AgentStateUpdate → sends position data
- CommunicationEvent ← receives messages
- EventBus → central message routing

### With Lauren's Intelligence Package
- MovementCommand ← receives movement instructions
- TaskCompletionReport → sends task feedback
- AgentCapabilities → provides capability data for task assignment
- Task → executes assigned work

### With Anthony's UI Package
- SystemCommand ← receives user commands
- VisualizationUpdate → sends display data
- SystemMetrics → sends performance data
- SystemEvent → sends event notifications

---

## 🎨 Design Patterns Used

| Pattern | Purpose | Classes |
|---------|---------|---------|
| **Singleton** | Single system instance | BoundaryManager, PerformanceMonitor |
| **Observer** | Event notification | EventBus, all event classes |
| **Factory** | Object creation | AgentManager, Task factory methods |
| **DTO** | Data transfer | AgentState, SystemMetrics, AgentCapabilities |
| **Strategy** | Algorithm selection | PhysicsEngine (boundary modes), BoundaryManager (enforcement) |
| **Command** | Encapsulate requests | MovementCommand, SystemCommand |
| **State** | State management | AgentStatus, TaskState, SimulationState |
| **Value Object** | Immutable values | Point2D, Vector2D |
| **Pub-Sub** | Loose coupling | EventBus system-wide |
| **Priority Queue** | Ordered processing | Agent command queue |

---

## 🧪 Testing

### Test Files Available
1. **SimpleTest.java** - Basic agent creation and movement
2. **Week3IntegrationTest.java** - Movement command integration
3. **Week4IntegrationTest.java** - UI integration

### How to Run Tests
```java
// Run simple test
java com.team6.swarm.core.SimpleTest

// Run integration tests
// (Import into IDE and run as JUnit tests)
```

---

## 🚀 Performance Characteristics

### Scalability
- **Agents**: Tested up to 50 agents
- **Target FPS**: 30-60 FPS
- **Memory**: ~100MB for 50 agents
- **Thread Safety**: Full concurrent access support

### Optimization Features
- Auto-optimization based on performance
- Configurable FPS targeting
- Memory pressure detection
- Adaptive update rates

---

## 📋 Checklist: Using the Documentation

- [ ] Read Quick Start Guide
- [ ] Run SimpleTest.java
- [ ] Try "Creating Your First Agent" example
- [ ] Try "Running a Simulation" example
- [ ] Review API Reference for classes you'll use
- [ ] Study integration points for your component
- [ ] Review design patterns section
- [ ] Understand thread safety notes
- [ ] Run integration tests

---

## 🆘 Getting Help

### Common Issues
1. **"Agents not moving"** → See [Troubleshooting](QUICK_START_GUIDE.md#troubleshooting)
2. **"Low FPS"** → Check PerformanceMonitor section
3. **"Memory issues"** → See optimization features
4. **"Boundary violations"** → Review BoundaryManager docs

### Where to Look
- **Concepts**: QUICK_START_GUIDE.md
- **Functions**: COMPLETE_API_REFERENCE.md
- **Examples**: QUICK_START_GUIDE.md (Examples section)
- **Issues**: Troubleshooting section

---

## 📝 Documentation Conventions

### Code Examples
All code examples are complete and runnable (unless marked as pseudocode).

### Method Signatures
Format: `methodName(paramType param)` → `ReturnType`

### Access Modifiers
- **public** - Available to all
- **private** - Internal use only
- **(none)** - Package-private

### Parameter Types
- Primitives: `int`, `double`, `boolean`, `long`
- Objects: `String`, `Point2D`, `Agent`, etc.
- Collections: `List<T>`, `Map<K,V>`, etc.

---

## 📅 Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-10-30 | Initial documentation release (Weeks 1-6) |

---

## 🎓 Learning Path

### Beginner
1. Quick Start Guide
2. SimpleTest.java example
3. Creating agents example
4. Moving agents example

### Intermediate
1. Complete API Reference
2. Event system examples
3. Task management
4. Boundary management

### Advanced
1. Design patterns section
2. Performance optimization
3. Thread safety considerations
4. Custom integration

---

## 📧 Documentation Feedback

For documentation improvements or corrections:
- Review the source code comments
- Check for updates in project repository
- Consult team members

---

**Last Updated:** 2025-10-30
**Documentation Version:** 1.0
**Code Version:** Week 1-6 Complete

---
