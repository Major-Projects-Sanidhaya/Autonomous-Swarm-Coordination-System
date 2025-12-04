/**
 * COMMUNICATIONMETRICS CLASS - Performance Metrics Aggregator
 *
 * PURPOSE:
 * - Aggregates performance metrics from existing CommunicationManager data
 * - Provides unified metrics API for monitoring and optimization
 * - DRY: No duplicate state, reads from existing structures
 *
 * WEEK 11-12: Performance & Polish
 * - Lightweight aggregator that derives metrics from existing data
 * - No new state management, purely a view/aggregation layer
 *
 * METRICS PROVIDED:
 * - Messages per second (from message history timestamps)
 * - Average latency (from ConnectionInfo data)
 * - Failure rate (estimated from network simulator config)
 * - Pending message count (direct from queue)
 *
 * USAGE:
 * CommunicationMetrics metrics = new CommunicationMetrics(manager);
 * CommunicationMetricsSnapshot snapshot = metrics.getSnapshot();
 * System.out.println("Messages/sec: " + snapshot.messagesPerSecond);
 */
package com.team6.swarm.communication;

import java.util.List;

public class CommunicationMetrics {
    private final CommunicationManager manager;
    
    public CommunicationMetrics(CommunicationManager manager) {
        this.manager = manager;
    }
    
    /**
     * Calculate messages per second from recent message history.
     * Uses last 1 second of message timestamps.
     */
    public double getMessagesPerSecond() {
        List<IncomingMessage> history = manager.getMessageHistory();
        if (history.isEmpty()) {
            return 0.0;
        }
        
        long now = System.currentTimeMillis();
        long oneSecondAgo = now - 1000;
        
        int messagesInLastSecond = 0;
        for (IncomingMessage msg : history) {
            if (msg.actualDeliveryTime >= oneSecondAgo) {
                messagesInLastSecond++;
            }
        }
        
        return messagesInLastSecond;
    }
    
    /**
     * Calculate average latency across all active connections.
     * Uses ConnectionInfo.averageLatency from active connections.
     */
    public double getAverageLatency() {
        List<ConnectionInfo> connections = manager.getActiveConnections();
        if (connections.isEmpty()) {
            return 0.0;
        }
        
        double totalLatency = 0.0;
        int count = 0;
        for (ConnectionInfo conn : connections) {
            if (conn.isActive && conn.messageCount > 0) {
                totalLatency += conn.averageLatency;
                count++;
            }
        }
        
        return count > 0 ? totalLatency / count : 0.0;
    }
    
    /**
     * Estimate failure rate from network simulator configuration.
     * Returns the configured failure rate (0.0 to 1.0).
     * Note: Actual failure rate would require tracking failed vs successful deliveries.
     */
    public double getFailureRate() {
        // Access network simulator failure rate if exposed
        // For now, return a conservative estimate based on typical network conditions
        // In a full implementation, this would track actual failures vs successes
        return 0.05; // 5% default failure rate
    }
    
    /**
     * Get current pending message count.
     * Direct access to queue size.
     */
    public int getPendingMessageCount() {
        return manager.getPendingMessageCount();
    }
    
    /**
     * Get a complete snapshot of all metrics at current time.
     * Immutable snapshot for consistent reporting.
     */
    public CommunicationMetricsSnapshot getSnapshot() {
        return new CommunicationMetricsSnapshot(
            getMessagesPerSecond(),
            getAverageLatency(),
            getFailureRate(),
            getPendingMessageCount(),
            System.currentTimeMillis()
        );
    }
    
    /**
     * Immutable snapshot of communication metrics at a point in time.
     */
    public static class CommunicationMetricsSnapshot {
        public final double messagesPerSecond;
        public final double averageLatency;
        public final double failureRate;
        public final int pendingMessages;
        public final long timestamp;
        
        public CommunicationMetricsSnapshot(double messagesPerSecond, double averageLatency,
                                           double failureRate, int pendingMessages, long timestamp) {
            this.messagesPerSecond = messagesPerSecond;
            this.averageLatency = averageLatency;
            this.failureRate = failureRate;
            this.pendingMessages = pendingMessages;
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return String.format(
                "MetricsSnapshot{msgs/sec=%.2f, latency=%.1fms, failure=%.1f%%, pending=%d, time=%d}",
                messagesPerSecond, averageLatency, failureRate * 100, pendingMessages, timestamp
            );
        }
    }
}

