# ASCS Demo - Quick Start Guide

Get the swarm visualization running in under 5 minutes!

## Prerequisites

**Required:**
- Java 11 or higher ([Download](https://adoptium.net/))

**Optional (but recommended):**
- Maven 3.6+ ([Download](https://maven.apache.org/download.cgi))

---

## Method 1: Using Maven (Easiest)

If you have Maven installed:

```bash
cd demo
mvn clean javafx:run
```

**That's it!** The demo window should appear.

---

## Method 2: Without Maven

If you don't have Maven:

### Step 1: Download JavaFX

1. Go to https://gluonhq.com/products/javafx/
2. Download JavaFX SDK for your OS
3. Extract it somewhere (e.g., `~/javafx-sdk-17`)

### Step 2: Compile and Run

```bash
cd demo
./compile.sh /path/to/javafx-sdk-17/lib
./run-compiled.sh
```

Or manually:

```bash
# Compile
javac --module-path /path/to/javafx-sdk/lib \
      --add-modules javafx.controls \
      -d out \
      src/main/java/com/team6/swarm/demo/*.java

# Run
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls \
     -cp out \
     com.team6.swarm.demo.SwarmDemo
```

---

## What to Do Once Running

### Try These in Order:

1. **Watch the initial swarm** - 12 agents flocking naturally
2. **Click "A: Basic Flocking"** - See emergent behavior
3. **Adjust sliders** - Change separation/alignment/cohesion
4. **Click on canvas** - Set waypoints for all agents
5. **Click "B: Consensus Vote"** - Watch democratic decision-making
6. **Click "C: Network Degradation"** - See resilience in action
7. **Click "D: Formation Flying"** - Automated formation sequence

### Interactive Controls:

- **Spawn/Remove Agent buttons** - Add or remove drones
- **Formation buttons** - Instant formation changes
- **Network Quality slider** - Simulate communication issues
- **Click canvas** - Set target waypoints
- **Flocking sliders** - Tune behavior weights in real-time

---

## Troubleshooting

### "Module javafx.controls not found"
→ Install JavaFX SDK or use Maven method

### "Command not found: mvn"
→ Use Method 2 (without Maven)

### "No JavaFX runtime found"
→ Make sure you're using `--module-path` and `--add-modules` flags

### Slow performance
→ Reduce number of agents or disable communication links

---

## Next Steps

- Read the full [README.md](README.md) for detailed explanations
- Explore the code in `src/main/java/com/team6/swarm/demo/`
- Integrate concepts into the main ASCS project

---

**Enjoy watching your autonomous swarm! 🚁✨**
