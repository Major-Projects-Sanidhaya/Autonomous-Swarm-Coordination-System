package com.team6.swarm.intelligence.voting;

/**
 * PROPOSALSTATE ENUM - Lifecycle Tracking
 */
enum ProposalState {
    ACTIVE,          // Currently collecting votes
    COMPLETED,       // Consensus reached
    EXPIRED,         // Deadline passed
    CANCELLED        // Manually cancelled
}