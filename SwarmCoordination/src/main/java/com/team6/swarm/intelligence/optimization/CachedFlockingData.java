/**
 * CACHEDFLOCKINGDATA CLASS - Cached Flocking Force Data
 *
 * PURPOSE:
 * - Stores calculated flocking force for reuse
 * - Tracks neighbor configuration for cache validation
 * - Includes timestamp for cache expiration
 */
package com.team6.swarm.intelligence.optimization;

import com.team6.swarm.core.Vector2D;
import java.util.Set;

public class CachedFlockingData {
    private Vector2D force;
    private Set<Integer> neighborIds;
    private long timestamp;
    
    public CachedFlockingData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public Vector2D getForce() {
        return force;
    }
    
    public void setForce(Vector2D force) {
        this.force = force;
    }
    
    public Set<Integer> getNeighborIds() {
        return neighborIds;
    }
    
    public void setNeighborIds(Set<Integer> neighborIds) {
        this.neighborIds = neighborIds;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}