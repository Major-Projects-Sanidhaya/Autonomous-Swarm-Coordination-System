/**
 * AGENTSTATUS ENUM - Agent Operational States
 *
 * PURPOSE:
 * - Defines possible operational states for swarm agents
 * - Enables status-based decision making and monitoring
 * - Critical for system health monitoring and error handling
 *
 * STATUS DEFINITIONS:
 * 1. ACTIVE - Agent fully operational and responding to commands
 * 2. INACTIVE - Agent temporarily disabled but can be reactivated
 * 3. FAILED - Agent encountered critical error and needs intervention
 * 4. BATTERY_LOW - Agent has low power and may enter conservation mode
 * 5. MAINTENANCE - Agent temporarily offline for updates/repairs
 *
 * LOGIC:
 * - Used in conditional statements for agent behavior
 * - Determines if agent participates in swarm operations
 * - Triggers different response protocols based on status
 *
 * EXPECTED USAGE EXAMPLES:
 * - if (agent.getStatus() == AgentStatus.ACTIVE) { executeCommand(); }
 * - Filter active agents: agents.filter(a -> a.status == ACTIVE)
 * - Status transitions: ACTIVE -> BATTERY_LOW -> INACTIVE
 *
 * STATUS FLOW:
 * ACTIVE ↔ INACTIVE ↔ MAINTENANCE
 *   ↓         ↑
 * BATTERY_LOW ↑
 *   ↓
 * FAILED (requires manual reset)
 */
package com.team6.swarm.core;

public enum AgentStatus {
    ACTIVE, 
    INACTIVE, 
    FAILED, 
    BATTERY_LOW, 
    MAINTENANCE
}