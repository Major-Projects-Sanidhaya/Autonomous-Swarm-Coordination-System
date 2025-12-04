package com.team6.swarm.communication;

/**
 * FAILURERECOVERYPOLICY CLASS - Lightweight Retry Support (Week 9–10)
 *
 * PURPOSE:
 * - Provides a simple retry mechanism for critical messages
 * - Reuses CommunicationManager.sendMessage for actual delivery
 * - Avoids duplicating routing or network simulation logic
 */
public class FailureRecoveryPolicy {

    private final CommunicationManager communicationManager;
    private final int maxRetriesForCritical;

    public FailureRecoveryPolicy(CommunicationManager communicationManager, int maxRetriesForCritical) {
        this.communicationManager = communicationManager;
        this.maxRetriesForCritical = maxRetriesForCritical;
    }

    /**
     * Sends a message with a fixed number of retry attempts.
     *
     * NOTE:
     * - This method is intended for simulated/test environments.
     * - It does not change routing or network behaviour; it simply calls sendMessage again.
     *
     * @param message The outgoing message to send
     * @param maxRetries Maximum number of retry attempts (0 = try once)
     * @return true if sendMessage succeeds at least once, false otherwise
     */
    public boolean sendWithRetry(OutgoingMessage message, int maxRetries) {
        int attempts = 0;
        while (attempts <= maxRetries) {
            boolean sent = communicationManager.sendMessage(message);
            if (sent) {
                return true;
            }
            attempts++;
        }
        return false;
    }

    /**
     * Sends a critical message using the default retry limit.
     *
     * @param message The outgoing message to send
     * @return true if sendMessage succeeds at least once, false otherwise
     */
    public boolean sendCritical(OutgoingMessage message) {
        return sendWithRetry(message, maxRetriesForCritical);
    }
}


