package com.team6.swarm.ui;

import com.team6.swarm.core.Point2D;

import java.util.HashMap;
import java.util.Map;

/**
 * Week 2: Standardized user action format
 * Purpose: Translate UI actions to system commands
 * Author: Anthony (UI Team)
 */
public class UserEvent {
    
    /**
     * Types of user events
     */
    public enum EventType {
        // Mouse events
        PLACE_WAYPOINT,      // User clicks to add waypoint
        SPAWN_AGENT,         // Create new agent
        SELECT_AGENT,        // Click on agent for details
        REMOVE_AGENT,        // Delete agent
        
        // Parameter events
        ADJUST_PARAMETER,    // Change setting
        
        // Mission events
        START_MISSION,       // Begin mission
        STOP_MISSION,        // End mission
        PAUSE_MISSION,       // Pause mission
        
        // Formation events
        SET_FORMATION,       // Set formation type
        TOGGLE_FORMATION,    // Enable/disable formation
        
        // View events
        PAN_VIEW,            // Pan camera
        ZOOM_VIEW,           // Zoom camera
        RESET_VIEW,          // Reset camera to default
        
        // Selection events
        CLEAR_SELECTION,     // Clear agent selection
        SELECT_MULTIPLE,     // Box selection
        
        // System events
        TOGGLE_PAUSE,        // Pause/resume simulation
        RESET_SYSTEM,        // Reset entire system
        SAVE_STATE,          // Save current state
        LOAD_STATE,          // Load saved state
        
        // Voting events
        INITIATE_VOTE,       // Start a vote
        CAST_VOTE,           // Vote on decision
        
        // Debug events
        TOGGLE_DEBUG,        // Toggle debug visualization
        TOGGLE_GRID          // Toggle grid display
    }
    
    // Event properties
    private final EventType type;
    private final Point2D clickPosition;
    private final Map<String, Object> parameters;
    private final long timestamp;
    private final String sourceId;
    
    /**
     * Create a new user event
     */
    private UserEvent(Builder builder) {
        this.type = builder.type;
        this.clickPosition = builder.clickPosition;
        this.parameters = new HashMap<>(builder.parameters);
        this.timestamp = System.currentTimeMillis();
        this.sourceId = builder.sourceId;
    }
    
    // Getters
    
    public EventType getType() {
        return type;
    }
    
    public Point2D getClickPosition() {
        return clickPosition;
    }
    
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    public <T> T getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }
    
    @Override
    public String toString() {
        return String.format("UserEvent{type=%s, position=%s, params=%d, time=%d, source=%s}",
                type, clickPosition, parameters.size(), timestamp, sourceId);
    }
    
    /**
     * Builder for creating UserEvents
     */
    public static class Builder {
        private EventType type;
        private Point2D clickPosition;
        private Map<String, Object> parameters = new HashMap<>();
        private String sourceId = "UI";
        
        public Builder(EventType type) {
            this.type = type;
        }
        
        public Builder clickPosition(Point2D position) {
            this.clickPosition = position;
            return this;
        }
        
        public Builder clickPosition(double x, double y) {
            this.clickPosition = new Point2D(x, y);
            return this;
        }
        
        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }
        
        public Builder parameters(Map<String, Object> params) {
            this.parameters.putAll(params);
            return this;
        }
        
        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }
        
        // Convenience setters for common parameters
        
        public Builder agentId(String agentId) {
            return parameter("agentId", agentId);
        }
        
        public Builder parameterName(String name) {
            return parameter("parameterName", name);
        }
        
        public Builder parameterValue(Object value) {
            return parameter("parameterValue", value);
        }
        
        public Builder formationType(String formationType) {
            return parameter("formationType", formationType);
        }
        
        public Builder missionType(String missionType) {
            return parameter("missionType", missionType);
        }
        
        public Builder zoomFactor(double factor) {
            return parameter("zoomFactor", factor);
        }
        
        public Builder panDelta(double deltaX, double deltaY) {
            return parameter("panDeltaX", deltaX)
                   .parameter("panDeltaY", deltaY);
        }
        
        public Builder voteOption(String option) {
            return parameter("voteOption", option);
        }
        
        public UserEvent build() {
            if (type == null) {
                throw new IllegalStateException("Event type must be specified");
            }
            return new UserEvent(this);
        }
    }
    
    // Factory methods for common events
    
    public static UserEvent placeWaypoint(double x, double y) {
        return new Builder(EventType.PLACE_WAYPOINT)
                .clickPosition(x, y)
                .build();
    }
    
    public static UserEvent spawnAgent(double x, double y) {
        return new Builder(EventType.SPAWN_AGENT)
                .clickPosition(x, y)
                .build();
    }
    
    public static UserEvent selectAgent(String agentId, double x, double y) {
        return new Builder(EventType.SELECT_AGENT)
                .clickPosition(x, y)
                .agentId(agentId)
                .build();
    }
    
    public static UserEvent removeAgent(String agentId) {
        return new Builder(EventType.REMOVE_AGENT)
                .agentId(agentId)
                .build();
    }
    
    public static UserEvent adjustParameter(String paramName, Object value) {
        return new Builder(EventType.ADJUST_PARAMETER)
                .parameterName(paramName)
                .parameterValue(value)
                .build();
    }
    
    public static UserEvent startMission(String missionType) {
        return new Builder(EventType.START_MISSION)
                .missionType(missionType)
                .build();
    }
    
    public static UserEvent stopMission() {
        return new Builder(EventType.STOP_MISSION).build();
    }
    
    public static UserEvent togglePause() {
        return new Builder(EventType.TOGGLE_PAUSE).build();
    }
    
    public static UserEvent panView(double deltaX, double deltaY) {
        return new Builder(EventType.PAN_VIEW)
                .panDelta(deltaX, deltaY)
                .build();
    }
    
    public static UserEvent zoomView(double factor) {
        return new Builder(EventType.ZOOM_VIEW)
                .zoomFactor(factor)
                .build();
    }
    
    public static UserEvent clearSelection() {
        return new Builder(EventType.CLEAR_SELECTION).build();
    }
    
    public static UserEvent initiateVote(String voteType) {
        return new Builder(EventType.INITIATE_VOTE)
                .parameter("voteType", voteType)
                .build();
    }
    
    public static UserEvent castVote(String voteId, String option) {
        return new Builder(EventType.CAST_VOTE)
                .parameter("voteId", voteId)
                .voteOption(option)
                .build();
    }
}
