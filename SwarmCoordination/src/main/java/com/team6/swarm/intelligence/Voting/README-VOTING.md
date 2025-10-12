Voting package — README-VOTING.md

Overview

- Provides proposal creation, response collection, quorum/timeouts, weighted tallying and tie-resolution. Produces `VoteResult` objects that other systems can act on.

Primary classes

- VoteProposal.java

  - Data object representing a proposal (id, proposerId, options, required quorum, timeout, metadata).

- VoteResponse.java

  - Individual agent responses containing chosen option, weight (for weighted voting), and timestamp.

- VoteResult.java

  - Encapsulates the computed consensus result (winning option, percentage, consensus boolean, fallback action if any).

- VotingParameters.java

  - Tunable parameters controlling quorum thresholds, consensus percentage requirement, timeout durations, and tiebreaker configuration.

- VotingSystem.java

  - Orchestrates the voting process:
    - Accepts `VoteProposal`s, records `VoteResponse`s, enforces timeouts and quorum checks, computes weighted consensus, and resolves ties according to `VotingParameters`.
    - Produces `VoteResult`s for execution by other systems (e.g., `TaskAllocator` or `SystemController`).

- VotingTest.java
  - Test harness that runs a sequence of voting scenarios (simple consensus, no-consensus, unanimous, tie, timeout, weighted voting) and prints results and metrics.

Key responsibilities and data shapes

- `VoteProposal`: {id:String, proposerId:int, options:List<String>, quorum:int, timeoutMs:long, metadata:Map}
- `VoteResponse`: {proposalId:String, responderId:int, choice:String, weight:double, timestamp:long}
- `VoteResult`: {proposalId:String, winningOption:String, consensus:boolean, percentages:Map<String,Double>, fallback:String}

Important method signatures

```java
public class VotingSystem {
  public void submitProposal(VoteProposal proposal);
  public void submitResponse(VoteResponse response);
  public Optional<VoteResult> evaluateProposal(String proposalId);
}
```

Example usage (Java)

```java
VotingSystem vs = new VotingSystem(agentManager);
vs.submitProposal(proposal);
// agents call vs.submitResponse(...) as they vote
Optional<VoteResult> result = vs.evaluateProposal(proposal.getId());
if (result.isPresent()) {
  // take action based on result
}
```

Design notes and integration

- Voting must be tolerant to missing/delayed responses. Use `VotingParameters` to tune quorum and timeout behavior.
- Tie resolution is pluggable (e.g., lowest-agent-id wins, random seed, or predefined tiebreaker strategy).
- Voting is a coordination primitive; results feed into `TaskAllocator` and higher-level planning modules.

Testing

- `VotingTest` runs a suite of scenarios: unanimous, majority, tie, timeout, weighted votes.
- Make tests deterministic by simulating time and responses rather than relying on wall-clock time.

Extension ideas

- Add alternative voting strategies (ranked-choice, approval voting) behind a configuration flag.
- Publish voting events to `EventBus` for transparency and debugging.
