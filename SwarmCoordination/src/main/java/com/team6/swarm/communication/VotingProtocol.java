package com.team6.swarm.communication;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * VOTINGPROTOCOL CLASS - Lightweight Consensus Support (Week 7–8)
 *
 * PURPOSE:
 * - Thin coordination layer on top of existing vote messaging
 * - Tracks proposals, expected voters, responses, and deadlines
 * - Provides simple query APIs for consensus status/results
 *
 * DESIGN NOTES:
 * - Reuses CommunicationManager.broadcastVote, sendVoteResponse, getVoteMessages
 * - Does NOT implement its own routing or queueing logic (DRY)
 * - Uses Map-based payloads carried by existing Message / MessageType values
 */
public class VotingProtocol {

    private final CommunicationManager communicationManager;
    private final Map<String, VoteState> activeVotes = new HashMap<>();

    /**
     * Internal state for a single vote / consensus round.
     */
    private static class VoteState {
        final String proposalId;
        final int initiatorId;
        final Set<Integer> expectedVoters;
        final long deadlineMillis;
        final Map<Integer, Map<String, Object>> responses = new HashMap<>();

        VoteState(String proposalId, int initiatorId, Set<Integer> expectedVoters, long deadlineMillis) {
            this.proposalId = proposalId;
            this.initiatorId = initiatorId;
            this.expectedVoters = new HashSet<>(expectedVoters);
            this.deadlineMillis = deadlineMillis;
        }

        boolean isComplete() {
            return responses.keySet().containsAll(expectedVoters);
        }

        boolean isExpired(long now) {
            return now >= deadlineMillis;
        }
    }

    /**
     * Result snapshot for a vote.
     */
    public static class VoteResult {
        public final String proposalId;
        public final int initiatorId;
        public final Map<Integer, Map<String, Object>> responses;
        public final Set<Integer> expectedVoters;
        public final boolean complete;
        public final boolean expired;

        VoteResult(String proposalId,
                   int initiatorId,
                   Map<Integer, Map<String, Object>> responses,
                   Set<Integer> expectedVoters,
                   boolean complete,
                   boolean expired) {
            this.proposalId = proposalId;
            this.initiatorId = initiatorId;
            this.responses = Collections.unmodifiableMap(new HashMap<>(responses));
            this.expectedVoters = Collections.unmodifiableSet(new HashSet<>(expectedVoters));
            this.complete = complete;
            this.expired = expired;
        }
    }

    public VotingProtocol(CommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
    }

    /**
     * Starts a new vote by broadcasting a proposal and registering expected voters.
     *
     * REQUIRED PAYLOAD FIELDS:
     * - proposalId (String)
     * - deadline (Long, absolute timestamp in ms)
     *
     * The payload may include additional application-specific fields (question, options, etc.).
     */
    public void startVote(int initiatorId,
                          Map<String, Object> proposalPayload,
                          Set<Integer> expectedVoters) {
        Object proposalIdObj = proposalPayload.get("proposalId");
        Object deadlineObj = proposalPayload.get("deadline");

        if (!(proposalIdObj instanceof String) || !(deadlineObj instanceof Long)) {
            throw new IllegalArgumentException("proposalPayload must contain 'proposalId' (String) and 'deadline' (Long)");
        }

        String proposalId = (String) proposalIdObj;
        long deadlineMillis = (Long) deadlineObj;

        VoteState state = new VoteState(proposalId, initiatorId, expectedVoters, deadlineMillis);
        synchronized (activeVotes) {
            activeVotes.put(proposalId, state);
        }

        communicationManager.broadcastVote(initiatorId, proposalPayload);
    }

    /**
     * Records a response for a proposal, based on the response payload.
     *
     * REQUIRED PAYLOAD FIELD:
     * - proposalId (String) referencing the original proposal
     */
    public void recordResponse(int voterId, Map<String, Object> responsePayload) {
        Object proposalIdObj = responsePayload.get("proposalId");
        if (!(proposalIdObj instanceof String)) {
            throw new IllegalArgumentException("responsePayload must contain 'proposalId' (String)");
        }

        String proposalId = (String) proposalIdObj;
        VoteState state;
        synchronized (activeVotes) {
            state = activeVotes.get(proposalId);
        }
        if (state == null) {
            return; // Unknown or already completed/expired vote
        }

        synchronized (state) {
            state.responses.put(voterId, new HashMap<>(responsePayload));
        }
    }

    /**
     * Returns a snapshot of the current state for a given proposal.
     */
    public VoteResult getVoteResult(String proposalId) {
        VoteState state;
        synchronized (activeVotes) {
            state = activeVotes.get(proposalId);
        }
        if (state == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        boolean complete;
        boolean expired;
        Map<Integer, Map<String, Object>> responsesCopy;
        Set<Integer> expectedCopy;

        synchronized (state) {
            complete = state.isComplete();
            expired = state.isExpired(now);
            responsesCopy = new HashMap<>(state.responses);
            expectedCopy = new HashSet<>(state.expectedVoters);
        }

        return new VoteResult(
            state.proposalId,
            state.initiatorId,
            responsesCopy,
            expectedCopy,
            complete,
            expired
        );
    }

    /**
     * Removes expired votes from the active vote registry.
     */
    public void cleanupExpiredVotes() {
        long now = System.currentTimeMillis();
        synchronized (activeVotes) {
            activeVotes.values().removeIf(state -> state.isExpired(now));
        }
    }
}


