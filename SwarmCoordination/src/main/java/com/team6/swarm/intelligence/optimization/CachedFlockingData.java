package com.team6.swarm.intelligence.optimization;

import com.team6.swarm.core.Vector2D;
import java.util.Set;

/**
 * CACHED FLOCKING DATA
 */
public class CachedFlockingData {
    Vector2D force;
    Set<Integer> neighborIds;
    long timestamp;
}
