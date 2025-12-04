package com.team6.swarm.communication;

/**
 * FAILURERECOVERYPOLICY CLASS - Lightweight Retry Support (Week 9–10)
 *
 * PURPOSE:
 * - Provides a simple retry mechanism for critical messages
 * - Reuses CommunicationManager.sendMessage for actual delivery
 * - Avoids duplicating routing or network simulation logic
 * - Uses exponential backoff to prevent resource exhaustion
 */
public class FailureRecoveryPolicy {

    private final CommunicationManager communicationManager;
    private final int maxRetriesForCritical;
    private static final long BASE_DELAY_MS = 10; // Base delay: 10ms

    public FailureRecoveryPolicy(CommunicationManager communicationManager, int maxRetriesForCritical) {
        if (communicationManager == null) {
            throw new IllegalArgumentException("communicationManager must not be null");
        }
        if (maxRetriesForCritical < 0) {
            throw new IllegalArgumentException("maxRetriesForCritical must be non-negative");
        }
        this.communicationManager = communicationManager;
        this.maxRetriesForCritical = maxRetriesForCritical;
    }

    /**
     * Sends a message with a fixed number of retry attempts using exponential backoff.
     *
     * NOTE:
     * - This method is intended for simulated/test environments.
     * - It does not change routing or network behaviour; it simply calls sendMessage again.
     * - Uses exponential backoff (10ms, 20ms, 40ms, ...) between retries to prevent resource exhaustion.
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
            
            // Don't delay after the last attempt
            if (attempts < maxRetries) {
                // Exponential backoff: delay = BASE_DELAY_MS * 2^attempts
                long delayMs = BASE_DELAY_MS * (1L << attempts);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false; // Interrupted, give up
                }
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


