package com.team6.swarm.intelligence.formation;

/**
 * FORMATIONROLE ENUM - Agent's role within formation
 */
public enum FormationRole {
    LEADER,      // Front/center position
    WING,        // Side positions
    FOLLOWER,    // Following positions
    PERIMETER,   // Outer edge
    INTERIOR     // Inner positions
}