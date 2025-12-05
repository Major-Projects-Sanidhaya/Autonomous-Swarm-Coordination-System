# ASCS Documentation Index

## Overview
This directory contains comprehensive documentation of the Autonomous Swarm Coordination System (ASCS) codebase, generated November 5, 2025.

## Documentation Files

### 1. QUICK_START_SUMMARY.md (377 lines, 13KB)
**Purpose:** Fast-track understanding of the entire system  
**Time to Read:** 30-45 minutes  
**Best For:** Getting up to speed quickly, understanding architecture at a glance

**Contains:**
- Project overview and core architecture
- EventBus pattern explanation
- Agent lifecycle and command flow
- Flocking algorithm, voting system, task allocation explained
- Performance stats and physics constants
- UI layout and data structures
- Demo creation options
- Common questions and answers

**Start Here If:** You want to understand the system in 30 minutes

---

### 2. CODEBASE_ANALYSIS.md (1056 lines, 35KB)
**Purpose:** Comprehensive deep-dive into entire codebase  
**Time to Read:** 2-3 hours  
**Best For:** In-depth understanding, architectural decisions, integration points

**Contains:**
- Detailed project structure and directory layout
- Team component assignments
- Design patterns (EventBus, Agent-based, Command, Physics)
- Key classes with full explanations (Agent.java, SystemController.java, etc)
- EventBus detailed mechanics with examples
- Communication system architecture
- Flocking controller algorithm walkthrough
- Voting system lifecycle and consensus algorithms
- Task allocation scoring and workload balancing
- Main interface and visualizer details
- Data structures and flow diagrams
- Performance characteristics (time/space complexity)
- Integration contracts between components
- Existing reusable components
- Detailed EventBus example scenario
- Recommended demo approaches
- Quick reference file paths
- Architecture diagram

**Start Here If:** You're developing features, integrating components, or need deep understanding

---

### 3. README.md (350 lines, 14KB)
**Purpose:** Component-specific documentation for Anthony's UI layer  
**Best For:** Understanding the UI and integration component

**Contains:**
- Overview of Anthony's UI responsibilities
- Architecture diagram
- Phase-based implementation plan
- Core data structures documentation
- Command patterns and types
- Configuration systems
- Data flow between components
- Integration points with other teams
- Event system description
- Development guidelines

---

## How to Use This Documentation

### Scenario 1: "I need to understand this system fast"
1. Read QUICK_START_SUMMARY.md (30 min)
2. Review sections 2-3 (Architecture & Design Patterns)
3. Review section 4 (Key Classes)
4. You're ready to explore code

### Scenario 2: "I'm implementing a new feature"
1. Read QUICK_START_SUMMARY.md sections 1-6 (15 min)
2. Read CODEBASE_ANALYSIS.md sections 1-3 (30 min)
3. Read CODEBASE_ANALYSIS.md section 3 for relevant class (15 min)
4. Review integration points (section 6)
5. Examine actual code files

### Scenario 3: "I'm debugging a problem"
1. Check QUICK_START_SUMMARY.md section 20 (Common Questions)
2. Review CODEBASE_ANALYSIS.md relevant class section
3. Check integration contracts (section 6)
4. Look at EventBus example (section 8) if messaging related

### Scenario 4: "I'm creating a demo"
1. Read QUICK_START_SUMMARY.md sections 16-18 (Demo creation & progression)
2. Review CODEBASE_ANALYSIS.md section 7 (Existing reusable components)
3. Start with Option 1 (minimal demo)
4. Reference quick file paths (section 10) for implementation

### Scenario 5: "I need to understand a specific component"
1. Look up component in this index
2. Find relevant sections in CODEBASE_ANALYSIS.md
3. Review quick reference file paths
4. Examine source code files

---

## Key Statistics

| Metric | Value |
|--------|-------|
| Total Java Files | 75+ |
| Core Packages | 5 |
| Event Types | 7+ |
| Team Members | 4 |
| Simulation FPS | 30-60 |
| World Size | 800x600 |
| Max Agents | 50-100 |
| Documentation Lines | 1783 |
| Documentation Size | 62KB |

---

## Component Quick Reference

### Sanidhaya's Core System
**Files:** `/core/Agent.java`, `/core/AgentManager.java`, `/core/SystemController.java`  
**Key Classes:** Agent, AgentManager, SystemController, PhysicsEngine, EventBus  
**Responsibility:** Agent simulation, physics, command processing

**Documentation:**
- QUICK_START.md sections 5-6
- CODEBASE_ANALYSIS.md sections 3.1, 3.2, 3.3

---

### John's Communication System
**Files:** `/communication/CommunicationManager.java`, `/communication/NetworkSimulator.java`  
**Key Classes:** CommunicationManager, Message, NetworkSimulator, NeighborInformation  
**Responsibility:** Message routing, network topology, neighbor discovery

**Documentation:**
- QUICK_START.md section 17 (integration)
- CODEBASE_ANALYSIS.md sections 3.4, 6.1

---

### Lauren's Swarm Intelligence
**Files:** `/intelligence/Flocking/`, `/intelligence/Voting/`, `/intelligence/tasking/`  
**Key Classes:** FlockingController, VotingSystem, TaskAllocator, FormationController  
**Responsibility:** Behavior algorithms, consensus, task assignment

**Documentation:**
- QUICK_START.md sections 8-10
- CODEBASE_ANALYSIS.md sections 3.5, 3.6, 3.7

---

### Anthony's User Interface
**Files:** `/ui/MainInterface.java`, `/ui/Visualizer.java`, `/ui/ControlPanel.java`  
**Key Classes:** MainInterface, Visualizer, ControlPanel, StatusPanel, MissionPanel  
**Responsibility:** JavaFX UI, visualization, user controls

**Documentation:**
- QUICK_START.md section 12
- CODEBASE_ANALYSIS.md sections 3.8, 3.9
- README.md (full file)

---

## Event Types Reference

| Event Type | Publisher | Subscribers | Purpose |
|------------|-----------|-------------|---------|
| AgentStateUpdate | Agent | SystemController, Visualizer | Position & status changes |
| TaskCompletionReport | Agent | SystemController, Metrics | Task completion notification |
| CommunicationEvent | CommunicationManager | Agents, SystemController | Inter-agent messages |
| VisualizationUpdate | AgentManager | Visualizer | Render data |
| NetworkStatus | CommunicationManager | Visualizer, Metrics | Topology information |
| DecisionStatus | VotingSystem | Visualizer, UI | Voting progress |
| SystemMetrics | SystemController | UI, Monitoring | Performance data |

**Documentation:** CODEBASE_ANALYSIS.md section 2.1

---

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│                  ASCS System                     │
├─────────────────────────────────────────────────┤
│                                                   │
│              MainInterface (Anthony)             │
│           ↓ EventBus (Central Hub)              │
│                                                   │
│  ┌──────────────┬─────────────┬──────────────┐ │
│  │              │             │              │ │
│  │   Core       │     Comm    │ Intelligence │ │
│  │ (Sanidhaya)  │   (John)    │   (Lauren)   │ │
│  │              │             │              │ │
│  │ • Agent      │ • Messages  │ • Flocking   │ │
│  │ • Physics    │ • Topology  │ • Voting     │ │
│  │ • Commands   │ • Network   │ • Tasks      │ │
│  │              │             │              │ │
│  └──────────────┴─────────────┴──────────────┘ │
│                                                   │
└─────────────────────────────────────────────────┘
```

**Full Diagram:** CODEBASE_ANALYSIS.md section 11

---

## File Locations (Absolute Paths)

All files start with: `/Users/sanidhyasharma/Documents/ASCS/`

### Documentation
- `QUICK_START_SUMMARY.md` - Fast-track guide
- `CODEBASE_ANALYSIS.md` - Comprehensive analysis
- `DOCUMENTATION_INDEX.md` - This file
- `README.md` - UI component documentation

### Source Code
- `SwarmCoordination/src/main/java/com/team6/swarm/core/`
- `SwarmCoordination/src/main/java/com/team6/swarm/communication/`
- `SwarmCoordination/src/main/java/com/team6/swarm/intelligence/`
- `SwarmCoordination/src/main/java/com/team6/swarm/ui/`

### Tests
- `SwarmCoordination/src/test/java/com/team6/swarm/`

**Full Paths Reference:** CODEBASE_ANALYSIS.md section 10

---

## Key Design Patterns

### 1. EventBus (Publish-Subscribe)
**Location:** CODEBASE_ANALYSIS.md section 2.1  
**Purpose:** Decouple components through message routing  
**Example:** CODEBASE_ANALYSIS.md section 8

### 2. Agent-Based Architecture
**Location:** CODEBASE_ANALYSIS.md section 2.2  
**Purpose:** Model swarm as collection of autonomous agents  
**Files:** Agent.java, AgentManager.java

### 3. Command Pattern
**Location:** CODEBASE_ANALYSIS.md section 2.3  
**Purpose:** Encapsulate movement instructions  
**File:** MovementCommand.java

### 4. Physics Engine
**Location:** CODEBASE_ANALYSIS.md section 2.4  
**Purpose:** Handle realistic movement and collisions  
**File:** PhysicsEngine.java

**Full Explanation:** QUICK_START_SUMMARY.md section 3

---

## Performance Guidelines

**Rendering:** Target 60 FPS (JavaFX AnimationTimer)  
**Simulation:** Target 30 FPS (SystemController loop)  
**Agent Count:** 5-20 typical, up to 50-100 maximum  
**World Size:** 800x600 units  
**Bottleneck:** O(n²) communication topology calculation

**Details:** CODEBASE_ANALYSIS.md section 5, QUICK_START_SUMMARY.md section 15

---

## Common Tasks

### Task: Understand how agents move
1. QUICK_START.md section 5 (Agent Lifecycle)
2. QUICK_START.md section 6 (Movement Command Flow)
3. CODEBASE_ANALYSIS.md section 3.1 (Agent.java)
4. Review: `/core/Agent.java`, `/core/PhysicsEngine.java`

### Task: Understand voting system
1. QUICK_START.md section 9 (Voting Algorithm)
2. CODEBASE_ANALYSIS.md section 3.6 (VotingSystem.java)
3. Review: `/intelligence/Voting/VotingSystem.java`

### Task: Understand flocking
1. QUICK_START.md section 8 (Flocking Algorithm)
2. CODEBASE_ANALYSIS.md section 3.5 (FlockingController.java)
3. Review: `/intelligence/Flocking/FlockingController.java`

### Task: Create a demo
1. QUICK_START.md section 16 (How to Create Demo)
2. QUICK_START.md section 18 (Demo Progression)
3. CODEBASE_ANALYSIS.md section 7 (Existing Components)
4. Choose option and implement

### Task: Debug a problem
1. QUICK_START.md section 20 (Common Questions)
2. Find relevant component in this index
3. Read relevant sections from CODEBASE_ANALYSIS.md
4. Review source code and EventBus flow

---

## Getting Started Paths

### Path 1: Quick Understanding (1 hour)
1. Read QUICK_START_SUMMARY.md sections 1-4 (10 min)
2. Read QUICK_START_SUMMARY.md sections 5-6 (10 min)
3. Read QUICK_START_SUMMARY.md sections 8-10 (15 min)
4. Skim source files listed in section 14 (15 min)
5. Review QUICK_START.md section 16 (10 min)

### Path 2: Thorough Understanding (3 hours)
1. Read QUICK_START_SUMMARY.md (45 min)
2. Read CODEBASE_ANALYSIS.md sections 1-3 (45 min)
3. Read CODEBASE_ANALYSIS.md sections 3.1-3.7 (60 min)
4. Read CODEBASE_ANALYSIS.md sections 6-8 (30 min)

### Path 3: Deep Dive with Implementation (6+ hours)
1. Complete Path 2 (3 hours)
2. Read all of CODEBASE_ANALYSIS.md (2 hours)
3. Review README.md (30 min)
4. Examine source code files (30 min)

### Path 4: Quick for Specific Feature
1. Read QUICK_START.md sections 1-3 (15 min)
2. Find component in this index
3. Read relevant CODEBASE_ANALYSIS.md section (30 min)
4. Review CODEBASE_ANALYSIS.md section 6 (integration points) (15 min)
5. Examine source code (30 min)

---

## Important Concepts

### EventBus Central Hub
The EventBus is the **heart of the system**. All components communicate through it.  
Details: CODEBASE_ANALYSIS.md sections 2.1, 3.3, 8

### Agent Lifecycle
Agents follow a strict update cycle each frame.  
Details: QUICK_START.md section 5, CODEBASE_ANALYSIS.md section 3.1

### Priority-Based Commands
Commands are executed based on priority, not FIFO.  
Details: CODEBASE_ANALYSIS.md section 2.3

### Team-Based Architecture
Each team member owns a component and communicates via EventBus.  
Details: QUICK_START.md section 4, integration contracts

### Emergent Behavior
Simple local rules (flocking, voting) create complex global behavior.  
Details: QUICK_START.md sections 8-10

---

## Version Information

**System Version:** Phase 2 Implementation  
**Status:** Architecture Complete, Most Core Components Finished  
**Documentation Generated:** November 5, 2025  
**Documentation Version:** 1.0

---

## Notes

- All file paths in documentation use absolute paths starting with `/Users/sanidhyasharma/Documents/ASCS/`
- Documentation reflects current codebase as of November 5, 2025
- Line numbers in references may change as code evolves
- Key concepts are consistent across all phases

---

## Quick Links

- **Project Root:** `/Users/sanidhyasharma/Documents/ASCS/`
- **Source Code:** `/Users/sanidhyasharma/Documents/ASCS/SwarmCoordination/src/main/java/com/team6/swarm/`
- **Core Package:** `...swarm/core/`
- **Communication Package:** `...swarm/communication/`
- **Intelligence Package:** `...swarm/intelligence/`
- **UI Package:** `...swarm/ui/`

---

**End of Documentation Index**

