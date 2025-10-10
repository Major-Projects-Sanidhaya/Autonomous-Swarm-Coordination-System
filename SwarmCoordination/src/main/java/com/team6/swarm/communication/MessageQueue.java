/**
 * Priority-based message queue for agent communication
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
    
    public OutgoingMessage dequeue() {
        OutgoingMessage message = queue.poll();
        if (message != null) {
            if (message.isExpired()) {
                messageStatus.put(message.messageContent.messageId, MessageStatus.EXPIRED);
                totalExpired.incrementAndGet();
                return null;
            }
            
            messageStatus.put(message.messageContent.messageId, MessageStatus.SENT);
            totalDequeued.incrementAndGet();
        }
        
        return message;
    }
    
    public OutgoingMessage peek() {
        OutgoingMessage message = queue.peek();
        if (message != null && message.isExpired()) {
            return null;
        }
        return message;
    }
    
    public int size() {
        return queue.size();
    }
    
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    public void clear() {
        queue.clear();
        messageStatus.clear();
    }
    
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
    
    public void markFailed(String messageId) {
        messageStatus.put(messageId, MessageStatus.FAILED);
        totalFailed.incrementAndGet();
    }
    
    public void markSent(String messageId) {
        messageStatus.put(messageId, MessageStatus.SENT);
    }
    
    public MessageStatus getMessageStatus(String messageId) {
        return messageStatus.getOrDefault(messageId, MessageStatus.UNKNOWN);
    }
    
    public QueueStatistics getStatistics() {
        return new QueueStatistics(
            size(),
            totalEnqueued.get(),
            totalDequeued.get(),
            totalFailed.get(),
            totalExpired.get()
        );
    }
    
    
    public enum MessageStatus {
        PENDING, SENT, FAILED, EXPIRED, UNKNOWN
    }
    
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
