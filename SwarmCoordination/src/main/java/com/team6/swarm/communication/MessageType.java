/**
 * MESSAGETYPE ENUM - Agent Communication Message Types
 *
 * PURPOSE:
 * - Defines all types of messages agents can send to each other
 * - Enables different handling for different message types
 * - Foundation for swarm communication protocols
 *
 * MESSAGE TYPES:
 * 1. POSITION_UPDATE - Agent sharing its current location
 * 2. VOTE_PROPOSAL - Asking other agents to vote on a decision
 * 3. VOTE_RESPONSE - Responding to a vote proposal
 * 4. TASK_ASSIGNMENT - Intelligence system assigning work to agents
 * 5. FORMATION_COMMAND - Instructions for formation flying/positioning
 * 6. EMERGENCY_ALERT - Urgent notifications requiring immediate attention
 * 7. STATUS_UPDATE - General status information (battery, health, etc.)
 * 8. ACKNOWLEDGMENT - Confirmation that a message was received
 *
 * PRIORITY LEVELS:
 * - EMERGENCY_ALERT: Highest priority (immediate delivery)
 * - VOTE_PROPOSAL, VOTE_RESPONSE: High priority (swarm decisions)
 * - TASK_ASSIGNMENT, FORMATION_COMMAND: Normal priority
 * - POSITION_UPDATE, STATUS_UPDATE: Low priority (frequent updates)
 * - ACKNOWLEDGMENT: Lowest priority (confirmations)
 *
 * USAGE EXAMPLES:
 * - Lauren creates VOTE_PROPOSAL messages for swarm decisions
 * - Agents send POSITION_UPDATE messages for flocking behavior
 * - System sends EMERGENCY_ALERT for critical situations
 * - Anthony displays different icons/colors based on message type
 *
 * EXPECTED PARAMETERS BY TYPE:
 * - POSITION_UPDATE: Point2D position, Vector2D velocity
 * - VOTE_PROPOSAL: String proposal, Map<String, Object> options
 * - VOTE_RESPONSE: String proposalId, Object vote, String reasoning
 * - TASK_ASSIGNMENT: String taskId, String taskType, Map<String, Object> parameters
 * - FORMATION_COMMAND: Point2D targetPosition, String formationType
 * - EMERGENCY_ALERT: String alertType, String description, Point2D location
 * - STATUS_UPDATE: AgentStatus status, double batteryLevel, Map<String, Object> metrics
 * - ACKNOWLEDGMENT: String originalMessageId, boolean success, String details
 */
package com.team6.swarm.communication;

public enum MessageType {
    POSITION_UPDATE,      // Agent location sharing
    VOTE_PROPOSAL,        // Swarm decision proposals
    VOTE_RESPONSE,        // Vote responses
    TASK_ASSIGNMENT,      // Work assignments
    FORMATION_COMMAND,    // Formation instructions
    EMERGENCY_ALERT,      // Urgent notifications
    STATUS_UPDATE,        // General status info
    ACKNOWLEDGMENT        // Message confirmations
}
