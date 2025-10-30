/**
 * MESSAGELISTENER INTERFACE - Callback for Incoming Messages
 *
 * PURPOSE:
 * - Enables event-driven message delivery to components
 * - Allows components to receive messages without polling
 * - Supports decoupled communication architecture
 *
 * USAGE:
 * Components (agents, intelligence systems, UI) can register listeners
 * to receive messages asynchronously when they arrive.
 *
 * EXAMPLE:
 * <pre>
 * communicationManager.registerMessageListener(agentId, (message) -> {
 *     // Process incoming message
 *     handleIncomingMessage(message);
 * });
 * </pre>
 *
 * INTEGRATION POINTS:
 * - Lauren's VotingSystem: Listen for VOTE_PROPOSAL messages
 * - Lauren's FlockingController: Listen for position updates
 * - Agents: Listen for TASK_ASSIGNMENT messages
 * - UI System: Monitor all message activity
 */
package com.team6.swarm.communication;

public interface MessageListener {
    /**
     * Called when a message is delivered to the registered agent.
     * 
     * @param message The incoming message with all delivery details
     */
    void onMessageReceived(IncomingMessage message);
}

