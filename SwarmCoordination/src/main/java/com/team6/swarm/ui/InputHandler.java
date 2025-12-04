package com.team6.swarm.ui;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Week 2: Process mouse and keyboard input
 * Purpose: Convert JavaFX events to UserEvents
 * Author: Anthony (UI Team)
 */
public class InputHandler {
    
    // Event listeners
    private final List<Consumer<UserEvent>> eventListeners = new ArrayList<>();
    
    // Mouse state
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private double dragStartX = 0;
    private double dragStartY = 0;
    private boolean isDragging = false;
    
    // Keyboard state
    private final Map<KeyCode, Boolean> keysPressed = new HashMap<>();
    
    // Configuration
    private double clickThreshold = 5.0; // pixels - distinguish click from drag
    private boolean enableDragPan = true;
    private boolean enableKeyboardShortcuts = true;
    
    // Agent detection callback
    private AgentDetector agentDetector;
    
    /**
     * Initialize input handlers for a scene
     */
    public void initialize(Scene scene, Canvas canvas) {
        // Mouse event handlers
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseClicked(this::handleMouseClicked);
        
        // Scroll event handler
        canvas.setOnScroll(this::handleScroll);
        
        // Keyboard event handlers
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
        
        System.out.println("InputHandler: Initialized for scene");
    }
    
    /**
     * Handle mouse pressed event
     */
    private void handleMousePressed(MouseEvent event) {
        dragStartX = event.getX();
        dragStartY = event.getY();
        lastMouseX = event.getX();
        lastMouseY = event.getY();
        isDragging = false;
    }
    
    /**
     * Handle mouse dragged event
     */
    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getX() - lastMouseX;
        double deltaY = event.getY() - lastMouseY;
        
        // Check if movement exceeds threshold
        double totalDelta = Math.sqrt(
            Math.pow(event.getX() - dragStartX, 2) + 
            Math.pow(event.getY() - dragStartY, 2)
        );
        
        if (totalDelta > clickThreshold) {
            isDragging = true;
        }
        
        if (isDragging && enableDragPan) {
            // Pan camera
            UserEvent panEvent = UserEvent.panView(deltaX, deltaY);
            notifyListeners(panEvent);
        }
        
        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }
    
    /**
     * Handle mouse released event
     */
    private void handleMouseReleased(MouseEvent event) {
        isDragging = false;
    }
    
    /**
     * Handle mouse click event
     */
    private void handleMouseClicked(MouseEvent event) {
        if (isDragging) {
            return; // Don't process as click if it was a drag
        }
        
        double x = event.getX();
        double y = event.getY();
        
        if (event.getButton() == MouseButton.PRIMARY) {
            // Left click
            handleLeftClick(x, y, event);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            // Right click
            handleRightClick(x, y, event);
        }
    }
    
    /**
     * Handle left mouse click
     */
    private void handleLeftClick(double x, double y, MouseEvent event) {
        // Check if clicked on an agent
        String agentId = detectAgentAt(x, y);
        
        if (agentId != null) {
            // Clicked on agent - select it
            UserEvent selectEvent = UserEvent.selectAgent(agentId, x, y);
            notifyListeners(selectEvent);
            System.out.println("InputHandler: Agent selected: " + agentId);
        } else {
            // Clicked on empty space
            if (isShiftPressed()) {
                // Shift + click = spawn agent
                UserEvent spawnEvent = UserEvent.spawnAgent(x, y);
                notifyListeners(spawnEvent);
                System.out.println("InputHandler: Spawn agent at (" + x + ", " + y + ")");
            } else {
                // Regular click = place waypoint
                UserEvent waypointEvent = UserEvent.placeWaypoint(x, y);
                notifyListeners(waypointEvent);
                System.out.println("InputHandler: Waypoint placed at (" + x + ", " + y + ")");
            }
        }
    }
    
    /**
     * Handle right mouse click
     */
    private void handleRightClick(double x, double y, MouseEvent event) {
        // Context menu or special actions
        String agentId = detectAgentAt(x, y);
        
        if (agentId != null) {
            // Right-click on agent - show context menu or remove
            if (isControlPressed()) {
                UserEvent removeEvent = UserEvent.removeAgent(agentId);
                notifyListeners(removeEvent);
                System.out.println("InputHandler: Remove agent: " + agentId);
            }
        }
    }
    
    /**
     * Handle scroll event (zoom)
     */
    private void handleScroll(ScrollEvent event) {
        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
        
        UserEvent zoomEvent = UserEvent.zoomView(zoomFactor);
        notifyListeners(zoomEvent);
        
        event.consume();
    }
    
    /**
     * Handle key pressed event
     */
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        keysPressed.put(code, true);
        
        if (!enableKeyboardShortcuts) {
            return;
        }
        
        // Handle keyboard shortcuts
        switch (code) {
            case SPACE:
                // Start/stop simulation
                UserEvent toggleEvent = UserEvent.togglePause();
                notifyListeners(toggleEvent);
                System.out.println("InputHandler: Toggle pause");
                break;
                
            case A:
                // Spawn agent at mouse position
                UserEvent spawnEvent = UserEvent.spawnAgent(lastMouseX, lastMouseY);
                notifyListeners(spawnEvent);
                System.out.println("InputHandler: Spawn agent (keyboard)");
                break;
                
            case D:
                // Delete selected agent
                UserEvent deleteEvent = new UserEvent.Builder(UserEvent.EventType.REMOVE_AGENT)
                        .parameter("removeSelected", true)
                        .build();
                notifyListeners(deleteEvent);
                System.out.println("InputHandler: Delete selected agent");
                break;
                
            case F:
                // Toggle formation mode
                UserEvent formationEvent = new UserEvent.Builder(UserEvent.EventType.TOGGLE_FORMATION)
                        .build();
                notifyListeners(formationEvent);
                System.out.println("InputHandler: Toggle formation");
                break;
                
            case V:
                // Initiate vote
                UserEvent voteEvent = UserEvent.initiateVote("navigation");
                notifyListeners(voteEvent);
                System.out.println("InputHandler: Initiate vote");
                break;
                
            case ESCAPE:
                // Clear selection
                UserEvent clearEvent = UserEvent.clearSelection();
                notifyListeners(clearEvent);
                System.out.println("InputHandler: Clear selection");
                break;
                
            case R:
                // Reset view
                UserEvent resetEvent = new UserEvent.Builder(UserEvent.EventType.RESET_VIEW)
                        .build();
                notifyListeners(resetEvent);
                System.out.println("InputHandler: Reset view");
                break;
                
            case G:
                // Toggle grid
                UserEvent gridEvent = new UserEvent.Builder(UserEvent.EventType.TOGGLE_GRID)
                        .build();
                notifyListeners(gridEvent);
                System.out.println("InputHandler: Toggle grid");
                break;
                
            case T:
                // Toggle debug
                UserEvent debugEvent = new UserEvent.Builder(UserEvent.EventType.TOGGLE_DEBUG)
                        .build();
                notifyListeners(debugEvent);
                System.out.println("InputHandler: Toggle debug");
                break;
                
            // Arrow keys for panning
            case UP:
                notifyListeners(UserEvent.panView(0, -10));
                break;
            case DOWN:
                notifyListeners(UserEvent.panView(0, 10));
                break;
            case LEFT:
                notifyListeners(UserEvent.panView(-10, 0));
                break;
            case RIGHT:
                notifyListeners(UserEvent.panView(10, 0));
                break;
                
            default:
                // No action for other keys
                break;
        }
        
        event.consume();
    }
    
    /**
     * Handle key released event
     */
    private void handleKeyReleased(KeyEvent event) {
        keysPressed.put(event.getCode(), false);
        event.consume();
    }
    
    /**
     * Detect if an agent is at the given position
     */
    private String detectAgentAt(double x, double y) {
        if (agentDetector != null) {
            return agentDetector.detectAgentAt(x, y);
        }
        return null;
    }
    
    /**
     * Check if Shift key is pressed
     */
    private boolean isShiftPressed() {
        return keysPressed.getOrDefault(KeyCode.SHIFT, false);
    }
    
    /**
     * Check if Control key is pressed
     */
    private boolean isControlPressed() {
        return keysPressed.getOrDefault(KeyCode.CONTROL, false);
    }
    
    /**
     * Check if Alt key is pressed
     */
    private boolean isAltPressed() {
        return keysPressed.getOrDefault(KeyCode.ALT, false);
    }
    
    /**
     * Add event listener
     */
    public void addEventListener(Consumer<UserEvent> listener) {
        eventListeners.add(listener);
    }
    
    /**
     * Remove event listener
     */
    public void removeEventListener(Consumer<UserEvent> listener) {
        eventListeners.remove(listener);
    }
    
    /**
     * Notify all listeners of an event
     */
    private void notifyListeners(UserEvent event) {
        for (Consumer<UserEvent> listener : eventListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                System.err.println("InputHandler: Error in event listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Set agent detector
     */
    public void setAgentDetector(AgentDetector detector) {
        this.agentDetector = detector;
    }
    
    /**
     * Enable/disable drag panning
     */
    public void setDragPanEnabled(boolean enabled) {
        this.enableDragPan = enabled;
    }
    
    /**
     * Enable/disable keyboard shortcuts
     */
    public void setKeyboardShortcutsEnabled(boolean enabled) {
        this.enableKeyboardShortcuts = enabled;
    }
    
    /**
     * Set click threshold for distinguishing clicks from drags
     */
    public void setClickThreshold(double threshold) {
        this.clickThreshold = threshold;
    }
    
    /**
     * Interface for detecting agents at screen positions
     */
    public interface AgentDetector {
        String detectAgentAt(double x, double y);
    }
}
