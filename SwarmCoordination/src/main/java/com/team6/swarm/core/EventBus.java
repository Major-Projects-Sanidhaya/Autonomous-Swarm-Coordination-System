/**
 * EVENT BUS - Central Event Distribution System
 *
 * PURPOSE:
 * - Decouples components through publish-subscribe pattern
 * - Routes messages between agents without direct references
 * - Enables extensible system architecture (add listeners without modifying senders)
 *
 * MAIN COMPONENTS:
 * 1. Subscriber Registry - Maps event types to listener lists
 * 2. Publication Methods - Post events to subscribers
 * 3. Thread Safety - Concurrent access handling
 * 4. Event Filtering - Deliver only relevant events to each subscriber
 *
 * CORE FUNCTIONS:
 * 1. subscribe(EventType, Listener) - Register interest in event type
 * 2. unsubscribe(EventType, Listener) - Remove listener
 * 3. publish(Event) - Broadcast event to all subscribers
 * 4. publishFiltered(Event, Filter) - Send to subset of subscribers
 *
 * EVENT TYPES HANDLED:
 * - AgentStateUpdate: Agent state changes
 * - CommunicationEvent: Agent-to-agent messages
 * - VisualizationUpdate: UI rendering updates
 * - TaskCompletionReport: Task status notifications
 * - SystemMetrics: Performance monitoring data
 *
 * PUBLISH-SUBSCRIBE PATTERN:
 * Publisher side:
 *   eventBus.publish(new AgentStateUpdate(...));
 *
 * Subscriber side:
 *   eventBus.subscribe(AgentStateUpdate.class, update -> {
 *       // Handle the update
 *   });
 *
 * THREAD SAFETY:
 * - Subscriber list uses ConcurrentHashMap or synchronized collections
 * - publish() iterates safely over concurrent modifications
 * - Listeners should be thread-safe (could be called from any thread)
 *
 * FILTERING EXAMPLE:
 * For CommunicationEvents with range limits:
 *   eventBus.publishFiltered(commEvent, listener -> {
 *       return listener.getAgent().getPosition()
 *              .distanceTo(sender.getPosition()) <= range;
 *   });
 *
 * INTEGRATION POINTS:
 * - Used by: All system components
 * - Publishers: Agent, SystemController
 * - Subscribers: Agents, UI, Logging, Communication system
 * - Created by: SystemController during initialization
 */
package com.team6.swarm.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventBus {
    // Thread-safe map: Event class type -> List of listeners
    // ConcurrentHashMap allows concurrent read/write of map
    // CopyOnWriteArrayList allows iteration during modification
    private final Map<Class<?>, List<Consumer<?>>> subscribers;

    public EventBus() {
        this.subscribers = new ConcurrentHashMap<>();
    }

    /**
     * Subscribe to events of a specific type
     * @param eventType The class of events to listen for (e.g., AgentStateUpdate.class)
     * @param listener The callback function to handle the event
     */
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                   .add(listener);
    }

    /**
     * Unsubscribe from events of a specific type
     * @param eventType The class of events to stop listening for
     * @param listener The callback function to remove
     */
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<?>> listeners = subscribers.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Publish an event to all subscribers of that event type
     * @param event The event object to publish
     */
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        Class<?> eventType = event.getClass();
        List<Consumer<?>> listeners = subscribers.get(eventType);

        if (listeners != null) {
            // CopyOnWriteArrayList allows safe iteration
            for (Consumer<?> listener : listeners) {
                try {
                    ((Consumer<T>) listener).accept(event);
                } catch (Exception e) {
                    // Don't let one listener's exception break others
                    System.err.println("Error in event listener: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Publish an event only to subscribers that pass the filter
     * Used for range-limited communication
     * @param event The event object to publish
     * @param filter Predicate to determine which listeners should receive the event
     */
    @SuppressWarnings("unchecked")
    public <T> void publishFiltered(T event, Predicate<Consumer<T>> filter) {
        Class<?> eventType = event.getClass();
        List<Consumer<?>> listeners = subscribers.get(eventType);

        if (listeners != null) {
            for (Consumer<?> listener : listeners) {
                Consumer<T> typedListener = (Consumer<T>) listener;
                try {
                    if (filter.test(typedListener)) {
                        typedListener.accept(event);
                    }
                } catch (Exception e) {
                    System.err.println("Error in filtered event listener: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get the number of subscribers for a specific event type
     * Useful for debugging and testing
     */
    public <T> int getSubscriberCount(Class<T> eventType) {
        List<Consumer<?>> listeners = subscribers.get(eventType);
        return listeners != null ? listeners.size() : 0;
    }

    /**
     * Clear all subscribers (useful for testing)
     */
    public void clearAll() {
        subscribers.clear();
    }
}