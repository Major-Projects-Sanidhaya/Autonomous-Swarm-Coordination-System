/**
 * MESSAGEQUEUE CLASS - Priority Message Queue Management
 *
 * PURPOSE:
 * - Manages pending messages in priority order
 * - Ensures emergency messages are processed first
 * - Provides thread-safe message queuing and processing
 *
 * PRIORITY LEVELS:
 * 1. EMERGENCY (1) - Critical alerts, system failures
 * 2. HIGH (2) - Vote proposals, task assignments
 * 3. NORMAL (3) - Position updates, status reports
 * 4. LOW (4) - Acknowledgments, routine updates
 * 5. BACKGROUND (5) - Non-urgent maintenance messages
 *
 * CORE FEATURES:
 * - Priority-based ordering (lower number = higher priority)
 * - Thread-safe operations using PriorityBlockingQueue
 * - Message status tracking (pending, sent, failed)
 * - Automatic expiration handling
 * - Queue size monitoring
 *
 * MESSAGE STATUS:
 * - PENDING: Waiting in queue for processing
 * - SENT: Successfully delivered
 * - FAILED: Delivery failed (network issues, etc.)
 * - EXPIRED: Message exceeded time-to-live
 *
 * USAGE EXAMPLES:
 * - MessageQueue queue = new MessageQueue();
 * - queue.enqueue(outgoingMessage);
 * - OutgoingMessage next = queue.dequeue();
 * - int count = queue.size();
 * - queue.clearExpiredMessages();
 *
 * INTEGRATION POINTS:
 * - CommunicationManager: Uses for message queuing
 * - MessageRouter: Processes messages from queue
 * - NetworkSimulator: Considers queue status for delivery
 * - Anthony: Displays queue status in monitoring UI
 */
package com.team6.swarm.communication;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageQueue {
    private final PriorityBlockingQueue<OutgoingMessage> queue;
    private final Map<String, MessageStatus> messageStatus;
    private final AtomicInteger totalEnqueued;
    private final AtomicInteger totalDequeued;
    private final AtomicInteger totalFailed;
    private final AtomicInteger totalExpired;
    
    public MessageQueue() {
        this.queue = new PriorityBlockingQueue<>(100, 
            Comparator.comparingInt(msg -> msg.priority));
        this.messageStatus = new ConcurrentHashMap<>();
        this.totalEnqueued = new AtomicInteger(0);
        this.totalDequeued = new AtomicInteger(0);
        this.totalFailed = new AtomicInteger(0);
        this.totalExpired = new AtomicInteger(0);
    }
    
    /**
     * Add message to queue with priority ordering
     */
    public boolean enqueue(OutgoingMessage message) {
        if (message.isExpired()) {
            totalExpired.incrementAndGet();
            return false;
        }
        
        boolean added = queue.offer(message);
        if (added) {
            messageStatus.put(message.messageContent.messageId, MessageStatus.PENDING);
            totalEnqueued.incrementAndGet();
        }
        
        return added;
    }
    
    /**
     * Remove and return next highest priority message
     */
    public OutgoingMessage dequeue() {
        OutgoingMessage message = queue.poll();
        if (message != null) {
            if (message.isExpired()) {
                messageStatus.put(message.messageContent.messageId, MessageStatus.EXPIRED);
                totalExpired.incrementAndGet();
                return null; // Don't return expired messages
            }
            
            messageStatus.put(message.messageContent.messageId, MessageStatus.SENT);
            totalDequeued.incrementAndGet();
        }
        
        return message;
    }
    
    /**
     * Peek at next message without removing it
     */
    public OutgoingMessage peek() {
        OutgoingMessage message = queue.peek();
        if (message != null && message.isExpired()) {
            return null; // Don't peek at expired messages
        }
        return message;
    }
    
    /**
     * Get current queue size
     */
    public int size() {
        return queue.size();
    }
    
    /**
     * Check if queue is empty
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    /**
     * Clear all messages from queue
     */
    public void clear() {
        queue.clear();
        messageStatus.clear();
    }
    
    /**
     * Remove expired messages from queue
     */
    public int clearExpiredMessages() {
        int removedCount = 0;
        Iterator<OutgoingMessage> iterator = queue.iterator();
        
        while (iterator.hasNext()) {
            OutgoingMessage message = iterator.next();
            if (message.isExpired()) {
                iterator.remove();
                messageStatus.put(message.messageContent.messageId, MessageStatus.EXPIRED);
                totalExpired.incrementAndGet();
                removedCount++;
            }
        }
        
        return removedCount;
    }
    
    /**
     * Mark message as failed
     */
    public void markFailed(String messageId) {
        messageStatus.put(messageId, MessageStatus.FAILED);
        totalFailed.incrementAndGet();
    }
    
    /**
     * Mark message as sent
     */
    public void markSent(String messageId) {
        messageStatus.put(messageId, MessageStatus.SENT);
    }
    
    /**
     * Get message status
     */
    public MessageStatus getMessageStatus(String messageId) {
        return messageStatus.getOrDefault(messageId, MessageStatus.UNKNOWN);
    }
    
    /**
     * Get queue statistics
     */
    public QueueStatistics getStatistics() {
        return new QueueStatistics(
            size(),
            totalEnqueued.get(),
            totalDequeued.get(),
            totalFailed.get(),
            totalExpired.get()
        );
    }
    
    /**
     * Get messages by priority level
     */
    public Map<Integer, Integer> getMessagesByPriority() {
        Map<Integer, Integer> priorityCount = new HashMap<>();
        
        for (OutgoingMessage message : queue) {
            int priority = message.priority;
            priorityCount.put(priority, priorityCount.getOrDefault(priority, 0) + 1);
        }
        
        return priorityCount;
    }
    
    /**
     * Get messages by type
     */
    public Map<MessageType, Integer> getMessagesByType() {
        Map<MessageType, Integer> typeCount = new HashMap<>();
        
        for (OutgoingMessage message : queue) {
            MessageType type = message.messageContent.type;
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }
        
        return typeCount;
    }
    
    /**
     * Get average queue processing time
     */
    public double getAverageProcessingTime() {
        if (totalDequeued.get() == 0) return 0.0;
        
        // This is a simplified calculation
        // In real implementation, you'd track actual processing times
        return 100.0; // 100ms average
    }
    
    /**
     * Check if queue is healthy
     * Healthy = not too many failed/expired messages
     */
    public boolean isHealthy() {
        int total = totalEnqueued.get();
        if (total == 0) return true;
        
        double failureRate = (double) totalFailed.get() / total;
        double expirationRate = (double) totalExpired.get() / total;
        
        return failureRate < 0.1 && expirationRate < 0.2; // Less than 10% failure, 20% expiration
    }
    
    /**
     * Get queue health status
     */
    public String getHealthStatus() {
        if (isHealthy()) return "HEALTHY";
        if (size() > 100) return "OVERLOADED";
        if (totalFailed.get() > totalEnqueued.get() * 0.1) return "HIGH_FAILURE_RATE";
        if (totalExpired.get() > totalEnqueued.get() * 0.2) return "HIGH_EXPIRATION_RATE";
        return "UNKNOWN";
    }
    
    /**
     * Message status enum
     */
    public enum MessageStatus {
        PENDING,    // Waiting in queue
        SENT,       // Successfully delivered
        FAILED,     // Delivery failed
        EXPIRED,    // Message expired
        UNKNOWN     // Status not tracked
    }
    
    /**
     * Queue statistics container
     */
    public static class QueueStatistics {
        public final int currentSize;
        public final int totalEnqueued;
        public final int totalDequeued;
        public final int totalFailed;
        public final int totalExpired;
        
        public QueueStatistics(int currentSize, int totalEnqueued, int totalDequeued, 
                             int totalFailed, int totalExpired) {
            this.currentSize = currentSize;
            this.totalEnqueued = totalEnqueued;
            this.totalDequeued = totalDequeued;
            this.totalFailed = totalFailed;
            this.totalExpired = totalExpired;
        }
        
        public double getSuccessRate() {
            if (totalDequeued == 0) return 0.0;
            return (double) totalDequeued / (totalDequeued + totalFailed);
        }
        
        public double getFailureRate() {
            if (totalEnqueued == 0) return 0.0;
            return (double) totalFailed / totalEnqueued;
        }
        
        public double getExpirationRate() {
            if (totalEnqueued == 0) return 0.0;
            return (double) totalExpired / totalEnqueued;
        }
        
        @Override
        public String toString() {
            return String.format("QueueStats{size=%d, enqueued=%d, dequeued=%d, failed=%d, expired=%d}", 
                               currentSize, totalEnqueued, totalDequeued, totalFailed, totalExpired);
        }
    }
    
    @Override
    public String toString() {
        return String.format("MessageQueue{size=%d, enqueued=%d, dequeued=%d, failed=%d, expired=%d}", 
                           size(), totalEnqueued.get(), totalDequeued.get(), totalFailed.get(), totalExpired.get());
    }
}
