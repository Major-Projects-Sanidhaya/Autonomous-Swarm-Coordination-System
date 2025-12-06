package com.team6.swarm.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Week 5-6: Manage inter-component messaging
 * Purpose: Components need to communicate efficiently
 * Author: Anthony (UI Team)
 */
public class EventBusManager {
    
    /**
     * Event types for system-wide communication
     */
    public enum EventType {
        // Agent events
        AGENT_STATE_UPDATE,
        AGENT_SPAWNED,
        AGENT_REMOVED,
        AGENT_FAILED,
        
        // Communication events
        COMMUNICATION_EVENT,
        MESSAGE_SENT,
        MESSAGE_RECEIVED,
        NETWORK_STATUS_UPDATE,
        
        // Decision events
        DECISION_STATUS_UPDATE,
        VOTE_STARTED,
        VOTE_COMPLETED,
        CONSENSUS_REACHED,
        
        // Mission events
        MISSION_STARTED,
        MISSION_COMPLETED,
        MISSION_FAILED,
        MISSION_PROGRESS_UPDATE,
        
        // System events
        SYSTEM_STATUS_UPDATE,
        SYSTEM_ERROR,
        SYSTEM_WARNING,
        
        // User events
        USER_COMMAND,
        UI_UPDATE_REQUIRED
    }
    
    /**
     * Event wrapper class
     */
    public static class Event {
        private final EventType type;
        private final Object data;
        private final long timestamp;
        private final String sourceId;
        
        public Event(EventType type, Object data, String sourceId) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.sourceId = sourceId;
        }
        
        public EventType getType() { return type; }
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }
        public String getSourceId() { return sourceId; }
        
        @SuppressWarnings("unchecked")
        public <T> T getData(Class<T> clazz) {
            if (data != null && clazz.isInstance(data)) {
                return (T) data;
            }
            return null;
        }
    }
    
    /**
     * Event listener interface
     */
    @FunctionalInterface
    public interface EventListener {
        void onEvent(Event event);
    }
    
    // Subscription management
    private final Map<EventType, List<Subscription>> subscriptions = new ConcurrentHashMap<>();
    private int nextSubscriptionId = 1;
    
    // Event history (for debugging)
    private final List<Event> eventHistory = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY_SIZE = 1000;
    private boolean recordHistory = true;
    
    /**
     * Subscription wrapper
     */
    private static class Subscription {
        final int id;
        final EventListener listener;
        final String subscriberId;
        
        Subscription(int id, EventListener listener, String subscriberId) {
            this.id = id;
            this.listener = listener;
            this.subscriberId = subscriberId;
        }
    }
    
    /**
     * Publish an event to all subscribers
     */
    public void publish(EventType type, Object data) {
        publish(type, data, "SYSTEM");
    }
    
    /**
     * Publish an event with source ID
     */
    public void publish(EventType type, Object data, String sourceId) {
        Event event = new Event(type, data, sourceId);
        
        // Record in history
        if (recordHistory) {
            eventHistory.add(event);
            if (eventHistory.size() > MAX_HISTORY_SIZE) {
                eventHistory.remove(0);
            }
        }
        
        // Notify subscribers
        List<Subscription> subs = subscriptions.get(type);
        if (subs != null) {
            for (Subscription sub : subs) {
                try {
                    sub.listener.onEvent(event);
                } catch (Exception e) {
                    System.err.println("EventBusManager: Error in subscriber " + 
                                     sub.subscriberId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Subscribe to an event type
     */
    public int subscribe(EventType type, EventListener listener) {
        return subscribe(type, listener, "UNKNOWN");
    }
    
    /**
     * Subscribe with subscriber ID
     */
    public int subscribe(EventType type, EventListener listener, String subscriberId) {
        int subscriptionId = nextSubscriptionId++;
        
        Subscription subscription = new Subscription(subscriptionId, listener, subscriberId);
        
        subscriptions.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                    .add(subscription);
        
        System.out.println("EventBusManager: " + subscriberId + " subscribed to " + type);
        
        return subscriptionId;
    }
    
    /**
     * Subscribe to multiple event types
     */
    public List<Integer> subscribe(EventListener listener, String subscriberId, 
                                   EventType... types) {
        List<Integer> subscriptionIds = new ArrayList<>();
        for (EventType type : types) {
            subscriptionIds.add(subscribe(type, listener, subscriberId));
        }
        return subscriptionIds;
    }
    
    /**
     * Unsubscribe from an event type
     */
    public void unsubscribe(int subscriptionId) {
        for (List<Subscription> subs : subscriptions.values()) {
            subs.removeIf(sub -> sub.id == subscriptionId);
        }
    }
    
    /**
     * Unsubscribe all listeners for a subscriber ID
     */
    public void unsubscribeAll(String subscriberId) {
        for (List<Subscription> subs : subscriptions.values()) {
            subs.removeIf(sub -> sub.subscriberId.equals(subscriberId));
        }
        System.out.println("EventBusManager: " + subscriberId + " unsubscribed from all events");
    }
    
    /**
     * Clear all subscriptions
     */
    public void clearAllSubscriptions() {
        subscriptions.clear();
        System.out.println("EventBusManager: All subscriptions cleared");
    }
    
    /**
     * Get event history
     */
    public List<Event> getEventHistory() {
        return new ArrayList<>(eventHistory);
    }
    
    /**
     * Clear event history
     */
    public void clearHistory() {
        eventHistory.clear();
    }
    
    /**
     * Enable/disable history recording
     */
    public void setRecordHistory(boolean record) {
        this.recordHistory = record;
    }
    
    /**
     * Get subscription count for event type
     */
    public int getSubscriptionCount(EventType type) {
        List<Subscription> subs = subscriptions.get(type);
        return subs != null ? subs.size() : 0;
    }
    
    /**
     * Get total subscription count
     */
    public int getTotalSubscriptionCount() {
        return subscriptions.values().stream()
                          .mapToInt(List::size)
                          .sum();
    }
}
