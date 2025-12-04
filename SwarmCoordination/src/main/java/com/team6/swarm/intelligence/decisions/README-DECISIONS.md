# Decision Intelligence Package Overview

**Package**: `com.team6.swarm.intelligence.decisions`  
**Developer**: Lauren (Week 9-10)  
**Purpose**: Advanced decision-making mechanisms beyond simple voting

---

## 📋 Package Contents

### Core Classes

1. **DecisionEngine.java** - Main decision-making system
2. **ConflictResolver.java** - Deadlock and disagreement handler
3. **Decision Support Classes** - Data structures for decision-making
4. **DecisionTest.java** - Comprehensive test suite

---

## 🎯 What This Package Does

This package provides **three sophisticated decision-making mechanisms** that go beyond simple majority voting:

### 1. 🏆 Weighted Voting

**Problem**: Not all opinions should carry equal weight  
**Solution**: Vote weight based on expertise, battery, proximity, and confidence

**How It Works**:

```
Vote Weight = Base × Expertise × Battery × Proximity × Confidence

Expertise Multipliers:
- Scout voting on navigation: 1.5x weight
- Leader voting on formation: 1.5x weight
- Regular agent: 1.0x weight

Battery Impact:
- High battery (90%): Full weight
- Low battery (20%): 0.5x weight

Result: Expert opinions influence decisions more
```

**Example**:

- 1 Scout + 1 Leader vote LEFT (weighted: 1.87)
- 3 Regular agents vote RIGHT (weighted: 1.26)
- **Winner**: LEFT (expert opinions prevail despite being outnumbered)

### 2. 📊 Multi-Criteria Decision Analysis

**Problem**: Decisions involve multiple competing factors  
**Solution**: Score each option on weighted criteria

**How It Works**:

```
For each option:
  Score = Σ(Criterion_Score × Criterion_Weight)

Example Criteria:
- Distance (40% weight): Shorter is better
- Battery Cost (30% weight): Less consumption is better
- Time (20% weight): Faster is better
- Risk (10% weight): Safer is better

Result: Best overall option considering all factors
```

**Example**:

```
Route A: Distance=80, Battery=70, Time=85, Risk=90 → Score: 79.5
Route B: Distance=90, Battery=85, Time=75, Risk=80 → Score: 83.5
Route C: Distance=70, Battery=65, Time=90, Risk=85 → Score: 75.0

Winner: Route B (best balance across all criteria)
```

### 3. 💰 Auction-Based Task Allocation

**Problem**: How to efficiently assign tasks to agents?  
**Solution**: Agents bid based on their cost, lowest wins

**How It Works**:

```
Agent Cost Calculation:
  Base Cost = Distance × 0.5
  Battery Penalty = Cost × (2.0 - battery_level)
  Role Mismatch = Cost × 1.3 (if wrong role)

  Final Cost = Base Cost × Battery Penalty × Role Mismatch

Bid Quality:
  Quality = 50 + (battery × 30) + (active bonus) + (distance bonus)

Winner: Lowest cost bid (or best quality if costs similar)
```

**Example**:

```
Task at (400, 300):
- Agent 1: distance=50, battery=0.4 → cost=40.0
- Agent 2: distance=100, battery=0.9 → cost=27.5 ← WINS
- Agent 3: distance=200, battery=0.6 → cost=140.0

Winner: Agent 2 (lowest total cost)
```

---

## 🔄 Conflict Resolution

When votes fail to reach consensus, the **ConflictResolver** provides fallback strategies:

### Strategy 1: Leader Fallback

- Leader makes final decision
- Breaks tie votes
- Emergency decision-making

### Strategy 2: Compromise

- Find middle ground between options
- Weighted blend based on vote distribution
- Example: 57% LEFT, 43% RIGHT → "LEFT_SLIGHT"

### Strategy 3: Revote

- Modify options based on first vote
- Narrow choices from 4 → top 2-3
- Maximum 3 attempts before fallback

### Strategy 4: Multi-Stage

- Break decision into smaller parts
- Eliminate least popular option first
- Progressive refinement toward consensus

### Strategy 5: Hybrid

- Combine multiple strategies
- Try compromise, then leader fallback
- Adaptive approach

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    DecisionEngine                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Weighted   │  │ Multi-Crit.  │  │   Auction    │ │
│  │    Voting    │  │   Analysis   │  │  Allocation  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────┐
│                 ConflictResolver                         │
│  ┌─────────┐  ┌──────────┐  ┌────────┐  ┌───────────┐│
│  │ Leader  │  │Compromise│  │ Revote │  │Multi-Stage││
│  │Fallback │  │          │  │        │  │           ││
│  └─────────┘  └──────────┘  └────────┘  └───────────┘│
└─────────────────────────────────────────────────────────┘
```

---

## 📦 Data Structures

### DecisionContext

- Setup for multi-criteria decisions
- Defines options and evaluation criteria
- Specifies criterion weights

### DecisionCriterion

- Individual evaluation factor
- Type (distance, battery, time, risk, coverage)
- Weight (importance: 0.0 to 1.0)

### WeightedVoteResult

- Outcome of weighted voting
- Vote weights per option
- Individual agent weights

### MultiCriteriaResult

- Best option from analysis
- Detailed score breakdown
- Score margin over second place

### AuctionResult

- Winning bid and agent
- All submitted bids
- Cost savings achieved

### TaskBid

- Agent's bid for task
- Cost calculation
- Quality score

---

## 🔗 Integration Points

### Inputs from Other Systems:

- **VotingSystem**: Vote proposals and responses
- **AgentState**: Battery, position, status
- **Task**: Requirements, location, priority
- **LeaderFollower**: Current leader for fallback

### Outputs to Other Systems:

- **TaskAllocator**: Auction winners for assignment
- **MovementCommands**: Decisions translated to actions
- **UI/Monitoring**: Decision results and metrics

### Data Flow:

```
VoteProposal → DecisionEngine → WeightedVoteResult → Execute
    ↓                               ↓
VoteResponses              (if failed)
    ↓                               ↓
AgentStates → ConflictResolver → Resolution → Execute
```

---

## 🧪 Testing

### Test Coverage:

1. **Weighted Voting Test**

   - Verifies expert opinions have higher weight
   - Validates Scout (1.5x) > Regular agents (1.0x)
   - Checks mathematical relationships

2. **Multi-Criteria Test**

   - Confirms highest score wins
   - Validates criterion weighting
   - Checks score calculations

3. **Auction Test**

   - Verifies lowest cost wins
   - Validates distance + battery penalties
   - Checks bid quality calculations

4. **Conflict Resolution Tests**
   - Leader fallback breaks ties
   - Compromise creates middle ground
   - Revote narrows options

### Validation Approach:

- ✅ All tests use real calculated values
- ✅ No hardcoded expected results
- ✅ Verify mathematical relationships
- ✅ Check outcomes match input data

---

## 📊 Performance Metrics

### Tracked Metrics:

- Total decisions made
- Weighted voting decisions
- Multi-criteria decisions
- Auction decisions
- Conflict resolutions (by strategy)
- Resolution success rate

### Example Output:

```
DecisionEngine[Total: 45 | Weighted: 18 | MultiCriteria: 15 | Auction: 12]
ConflictResolver[Total: 8 | Leader: 5 | Compromise: 2 | Revote: 1 | Failed: 0]
```

---

## 🎓 Key Concepts

### Expertise-Based Weighting

Not all votes are equal. Scouts know navigation better, leaders know strategy better.

### Multi-Criteria Optimization

Real decisions involve trade-offs. This system balances competing factors mathematically.

### Market-Based Allocation

Agents "bid" for tasks based on their actual cost, leading to efficient resource allocation.

### Graceful Degradation

If sophisticated methods fail, simpler fallbacks ensure decisions still get made.

---

## 🚀 Usage Examples

### Example 1: Navigation Decision with Weighted Voting

```java
DecisionEngine engine = new DecisionEngine();
VoteProposal proposal = createNavigationProposal();
Map<Integer, VoteResponse> responses = collectVotes();

// Scout and leader votes weigh more
WeightedVoteResult result = engine.conductWeightedVote(
    proposal, responses, agents);

if (result.consensusReached) {
    executeNavigation(result.winningOption);
}
```

### Example 2: Route Selection with Multi-Criteria

```java
DecisionContext decision = engine.createDecision(
    "route_001", "Select optimal route", routes);

decision.addCriterion("Distance", CriterionType.DISTANCE, 0.4);
decision.addCriterion("Battery", CriterionType.BATTERY_COST, 0.3);
decision.addCriterion("Time", CriterionType.TIME, 0.2);
decision.addCriterion("Risk", CriterionType.RISK, 0.1);

MultiCriteriaResult result = engine.evaluateMultiCriteria(
    decision, agents);

selectRoute(result.bestOption);
```

### Example 3: Task Allocation via Auction

```java
Task task = Task.createSearchPattern("search_001", center, radius, "grid");
AuctionResult result = engine.conductAuction(task, agents);

if (result != null) {
    assignTaskToAgent(task, result.winningBid.agentId);
    System.out.println("Saved: " + result.getCostSavings());
}
```

### Example 4: Handling Vote Failure

```java
ConflictResolver resolver = new ConflictResolver();
VoteResult failedVote = checkConsensus(proposal);

if (!failedVote.consensusReached) {
    ResolutionResult resolution = resolver.resolveConflict(
        failedVote, proposal, agents, leaderSystem);

    if (resolution.resolved) {
        executeDecision(resolution.chosenOption);
    }
}
```

---

## 🎯 Design Goals Achieved

✅ **Go beyond simple voting**: Three sophisticated decision mechanisms  
✅ **Expert opinions matter**: Weighted voting based on context  
✅ **Multi-factor decisions**: Evaluate trade-offs mathematically  
✅ **Efficient allocation**: Market-based task assignment  
✅ **Handle deadlocks**: Multiple fallback strategies  
✅ **Testable**: All logic validated with real calculations  
✅ **Scalable**: Works with 5-100+ agents  
✅ **Extensible**: Easy to add new criteria or strategies

---

## 📚 Related Packages

- **`intelligence.voting`**: Basic democratic voting (Week 2)
- **`intelligence.tasking`**: Task definition and tracking (Week 3)
- **`intelligence.flocking`**: Behavior types for expertise weighting (Week 1)
- **`intelligence.coordination`**: Leader-follower for conflict resolution (Week 5-6)

---

## 🔮 Future Enhancements

Potential improvements for future development:

1. **Learning from History**: Track decision outcomes, adjust weights
2. **Fuzzy Logic**: Handle uncertain or partial information
3. **Game Theory**: Strategic decision-making in competitive scenarios
4. **Neural Networks**: Learn optimal criterion weights
5. **Real-Time Adaptation**: Adjust strategies based on mission phase

---

## 📖 Summary

The **Decision Intelligence Package** transforms the swarm from a simple voting system into a sophisticated decision-making collective. By incorporating **expert weighting**, **multi-criteria analysis**, and **market-based allocation**, the swarm can handle complex decisions that involve trade-offs, expertise differences, and resource constraints.

When simple consensus fails, the **ConflictResolver** ensures decisions still get made through leader fallback, compromise, or iterative refinement.

**Result**: A swarm that makes smart, context-aware decisions like a human team would! 🧠✨

---

**Week 9-10 Complete** ✓  
Next: Week 11-12 (Performance & Polish)
