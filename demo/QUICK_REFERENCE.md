# ASCS Demo - Quick Reference Card

## 🚀 Run Demo (Choose One)

```bash
# Method 1: Maven (Recommended)
cd demo
mvn clean javafx:run

# Method 2: Quick Script
cd demo
./run.sh

# Method 3: Pre-built JAR
cd demo
java -jar target/ascs-demo-1.0-SNAPSHOT.jar
```

---

## 🎮 Demo Scenarios (Click Buttons)

| Button | Duration | What It Does |
|--------|----------|--------------|
| **A: Basic Flocking** | Continuous | Shows emergent swarm behavior |
| **B: Consensus Vote** | 3 seconds | Democratic voting, shows results |
| **C: Network Degradation** | 10 seconds | Tests resilience (100%→30%→100%) |
| **D: Formation Flying** | 12 seconds | Auto-sequences 4 formations |

---

## 🎛️ Interactive Controls

| Control | Function |
|---------|----------|
| **Spawn Agent** | Add new agent at random position |
| **Remove Agent** | Remove most recent agent |
| **Line / V / Circle / Grid** | Set formation manually |
| **Separation/Alignment/Cohesion** | Adjust flocking behavior (sliders) |
| **Network Quality** | Simulate communication issues (slider) |
| **Click Canvas** | Set waypoint for all agents |
| **Clear Waypoints** | Remove all targets |
| **Show Comm Links** | Toggle blue communication lines |

---

## 🎨 Agent Colors

| Color | Meaning |
|-------|---------|
| **CYAN** | Active, normal operation |
| **ORANGE** | Voting in progress |
| **LIME GREEN** | Decision made |
| **RED** | Network connectivity issues |

---

## 🔧 Build Commands

```bash
# Verify setup
cd demo
./verify.sh

# Clean and compile
mvn clean compile

# Build JAR
mvn clean package

# Run tests (if any)
mvn test
```

---

## 📊 Performance Expectations

- **FPS:** 60
- **Default agents:** 12
- **Maximum tested:** 50 agents
- **Startup time:** ~2 seconds
- **Memory usage:** ~150 MB

---

## 🐛 Troubleshooting

**Demo won't start:**
```bash
java -version    # Check Java 11+
mvn -version     # Check Maven 3.6+
./verify.sh      # Run verification
```

**Window doesn't appear:**
```bash
mvn clean install
mvn javafx:run
```

**Slow performance:**
- Remove agents (< 20 recommended)
- Disable communication links
- Close other applications

---

## 📁 File Locations

```
demo/
├── src/main/java/com/team6/swarm/demo/
│   ├── DemoAgent.java       ← Agent logic
│   └── SwarmDemo.java       ← Main UI
├── README.md                 ← Full documentation
├── ARCHITECTURE.md           ← Technical details
├── run.sh                    ← Launch script
└── pom.xml                   ← Maven config
```

---

## 💡 Presentation Tips

1. **Start with autonomous flocking** (10 seconds of watching)
2. **Show Scenario A** - Adjust sliders in real-time
3. **Show Scenario B** - Quick consensus demonstration
4. **Show Scenario C** - Network resilience
5. **Show Scenario D** - Formation sequence (most impressive)
6. **Interactive demo** - Spawn agents, set waypoints, formations

**Total time:** 5-6 minutes

---

## 🔗 Key Algorithms

**Flocking (Reynolds' Boids):**
- Separation: Avoid crowding (25px)
- Alignment: Match velocity (50px)
- Cohesion: Stay together (50px)

**Consensus:**
- Democratic majority vote
- 3-second duration
- Visual progress indication

**Network:**
- Probabilistic packet loss
- Graceful degradation
- Communication radius: 100px

**Formations:**
- Line, V, Circle, Grid
- Smooth transitions
- Waypoint-based targeting

---

## 📞 Documentation

- **Quick Start:** `demo/QUICKSTART.md`
- **User Guide:** `demo/README.md`
- **Architecture:** `demo/ARCHITECTURE.md`
- **Integration:** `demo/INTEGRATION_COMPLETE.md`
- **Summary:** `../DEMO_COMPLETE_SUMMARY.md`

---

## ✅ Success Checklist

Before presentation:
- [ ] Run `./verify.sh` - all checks pass
- [ ] Test all 4 scenarios - work correctly
- [ ] Check FPS display - shows ~60
- [ ] Test interactive controls - respond immediately
- [ ] Review presentation flow - 5-6 minutes

---

**Ready to impress! 🚁✨**
