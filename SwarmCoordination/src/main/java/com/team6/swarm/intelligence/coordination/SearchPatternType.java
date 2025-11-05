package com.team6.swarm.intelligence.coordination;

/**
 * SEARCHPATTERNTYPE ENUM - Available search strategies
 */
public enum SearchPatternType {
    GRID,                 // Systematic grid coverage
    SPIRAL,               // Expanding spiral from center
    EXPANDING_PERIMETER,  // Growing circle search
    RANDOM_WALK           // Semi-random exploration
}