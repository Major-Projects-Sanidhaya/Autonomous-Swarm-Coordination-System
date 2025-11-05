package com.team6.swarm.intelligence.voting;

/**
 * Enumerates the types of proposals that can be made in the swarm voting system.
 * Each type represents a category of decision, such as navigation, formation,
 * mission, emergency, or coordination, to facilitate distributed decision-making.
 */
public enum ProposalType {
    NAVIGATION,      // Path and obstacle decisions
    FORMATION,       // Shape and spacing changes
    MISSION,         // Task and priority decisions
    EMERGENCY,       // Safety and abort decisions
    COORDINATION     // Timing and synchronization
}
