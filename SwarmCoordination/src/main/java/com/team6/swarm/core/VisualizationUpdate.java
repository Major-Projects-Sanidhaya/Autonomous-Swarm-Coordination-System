/**
 * VISUALIZATIONUPDATE CLASS - UI Data Package
 *
 * PURPOSE:
 * - Packages simulation data for UI/visualization components
 * - Provides snapshot of system state at specific timestamp
 * - Decouples simulation logic from presentation layer
 *
 * CORE COMPONENTS:
 * 1. allAgents - List of current AgentState objects
 * 2. systemMetrics - Performance and operational statistics
 * 3. timestamp - Exact time when snapshot was created
 *
 * DATA FLOW:
 * 1. AgentManager.getVisualizationUpdate() creates instance
 * 2. Populates with current agent states and calculated metrics
 * 3. UI components consume this data for rendering
 * 4. Timestamp enables frame rate calculations and data aging
 *
 * LOGIC:
 * - Constructor auto-sets timestamp to current system time
 * - Provides immutable snapshot of simulation state
 * - All data represents single point in time (atomic view)
 *
 * EXPECTED CONTENTS:
 * allAgents examples:
 * - [AgentState{id=1, pos=(150,200), status=ACTIVE},
 *    AgentState{id=2, pos=(300,100), status=BATTERY_LOW}]
 *
 * systemMetrics examples:
 * - totalAgents: 5
 * - activeAgents: 4
 * - averageSpeed: 23.5
 * - updatesPerSecond: 30
 *
 * timestamp example: 1640995200000 (milliseconds since epoch)
 *
 * USAGE PATTERNS:
 * - Generated 30-60 times per second for real-time visualization
 * - Consumed by graphics rendering pipeline
 * - Used for performance monitoring dashboards
 * - Enables replay functionality if stored
 *
 * MEMORY CONSIDERATIONS:
 * - Contains references to agent states (not copies)
 * - Should be consumed quickly to avoid memory buildup
 * - Timestamp helps identify stale data
 *
 * THREAD SAFETY:
 * - Immutable after creation (timestamp set in constructor)
 * - Agent states may be modified by simulation thread
 * - UI should copy data if long-term storage needed
 */
package com.team6.swarm.core;

import java.util.List;

public class VisualizationUpdate {
    public List<AgentState> allAgents;
    public SystemMetrics systemMetrics;
    public long timestamp;
    
    public VisualizationUpdate() {
        this.timestamp = System.currentTimeMillis();
    }
}