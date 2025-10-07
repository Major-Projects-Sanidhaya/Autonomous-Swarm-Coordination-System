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

import java.util.Map;

/* it follows a pubsub pattern */

public class EventBus {
    private Map<Class<?>, List<Consumer<?>>>
}
