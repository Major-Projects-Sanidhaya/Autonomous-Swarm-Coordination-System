package com.team6.swarm.ui;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.AgentStatus;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Week 1: Specialized rendering for agents
 * Purpose: Complex agent visualization needs dedicated code
 * Author: Anthony (UI Team)
 */
public class AgentRenderer {
    
    // Configuration constants
    private static final double AGENT_RADIUS = 8.0;
    private static final double DIRECTION_ARROW_LENGTH = 15.0;
    private static final double COMM_RANGE_RADIUS = 100.0;
    private static final double BATTERY_ARC_THICKNESS = 3.0;
    private static final int TRAIL_MAX_POINTS = 30; // ~1 second at 30 FPS
    private static final double SELECTION_HIGHLIGHT_WIDTH = 3.0;
    
    // Animation state
    private Map<String, Double> agentOpacity = new HashMap<>();
    private Map<String, Queue<TrailPoint>> agentTrails = new HashMap<>();
    private String selectedAgentId = null;
    private long pulseStartTime = 0;
    
    // Theme colors
    private ThemeColors theme = new ThemeColors();
    
    /**
     * Draw a basic agent with status-based coloring
     */
    public void drawAgent(GraphicsContext gc, AgentState agent) {
        if (agent == null) return;
        
        double x = agent.position.x;
        double y = agent.position.y;
        
        // Get or initialize opacity for fade effects
        String agentId = String.valueOf(agent.agentId);
        agentOpacity.putIfAbsent(agentId, 1.0);
        double opacity = agentOpacity.get(agentId);
        
        // Draw communication range if selected
        if (agentId.equals(selectedAgentId)) {
            drawCommunicationRange(gc, x, y, opacity);
        }
        
        // Draw trail if exists
        drawTrail(gc, agentId, opacity);
        
        // Draw agent body
        Color agentColor = getStatusColor(agent.status);
        gc.setFill(withOpacity(agentColor, opacity));
        gc.fillOval(x - AGENT_RADIUS, y - AGENT_RADIUS, 
                    AGENT_RADIUS * 2, AGENT_RADIUS * 2);
        
        // Draw outline
        gc.setStroke(withOpacity(Color.BLACK, opacity));
        gc.setLineWidth(2.0);
        gc.strokeOval(x - AGENT_RADIUS, y - AGENT_RADIUS, 
                      AGENT_RADIUS * 2, AGENT_RADIUS * 2);
        
        // Draw direction arrow
        drawDirectionArrow(gc, agent, opacity);
        
        // Draw battery indicator
        drawBatteryIndicator(gc, x, y, agent.batteryLevel * 100, opacity);
        
        // Draw selection highlight
        if (agentId.equals(selectedAgentId)) {
            drawSelectionHighlight(gc, x, y, opacity);
        }
        
        // Draw role icon (role would need to be added to AgentState or tracked separately)
        // drawRoleIcon(gc, x, y, "", opacity);
        
        // Draw label
        drawAgentLabel(gc, agent, opacity);
    }
    
    /**
     * Draw enhanced agent with all visual indicators
     */
    public void drawEnhancedAgent(GraphicsContext gc, AgentState agent, 
                                   boolean showCommunicationRange, 
                                   boolean showTrail) {
        if (agent == null) return;
        
        String agentId = String.valueOf(agent.agentId);
        double x = agent.position.x;
        double y = agent.position.y;
        
        // Update trail
        if (showTrail) {
            updateTrail(agentId, x, y);
        }
        
        // Draw all components
        drawAgent(gc, agent);
    }
    
    /**
     * Draw direction arrow showing agent heading
     */
    private void drawDirectionArrow(GraphicsContext gc, AgentState agent, double opacity) {
        double x = agent.position.x;
        double y = agent.position.y;
        double angle = agent.heading; // heading in radians
        
        double endX = x + Math.cos(angle) * DIRECTION_ARROW_LENGTH;
        double endY = y + Math.sin(angle) * DIRECTION_ARROW_LENGTH;
        
        gc.setStroke(withOpacity(Color.BLACK, opacity));
        gc.setLineWidth(2.0);
        gc.strokeLine(x, y, endX, endY);
        
        // Arrow head
        double arrowSize = 5.0;
        double angle1 = angle + Math.PI * 0.75;
        double angle2 = angle - Math.PI * 0.75;
        
        double arrow1X = endX + Math.cos(angle1) * arrowSize;
        double arrow1Y = endY + Math.sin(angle1) * arrowSize;
        double arrow2X = endX + Math.cos(angle2) * arrowSize;
        double arrow2Y = endY + Math.sin(angle2) * arrowSize;
        
        gc.strokeLine(endX, endY, arrow1X, arrow1Y);
        gc.strokeLine(endX, endY, arrow2X, arrow2Y);
    }
    
    /**
     * Draw battery level indicator as colored arc
     */
    private void drawBatteryIndicator(GraphicsContext gc, double x, double y, 
                                       double batteryLevel, double opacity) {
        double arcRadius = AGENT_RADIUS + 4;
        double arcExtent = 360.0 * (batteryLevel / 100.0);
        
        Color batteryColor;
        if (batteryLevel > 60) {
            batteryColor = Color.GREEN;
        } else if (batteryLevel > 30) {
            batteryColor = Color.YELLOW;
        } else {
            batteryColor = Color.RED;
        }
        
        gc.setStroke(withOpacity(batteryColor, opacity));
        gc.setLineWidth(BATTERY_ARC_THICKNESS);
        gc.strokeArc(x - arcRadius, y - arcRadius, 
                     arcRadius * 2, arcRadius * 2, 
                     90, -arcExtent, javafx.scene.shape.ArcType.OPEN);
    }
    
    /**
     * Draw communication range as dashed circle
     */
    private void drawCommunicationRange(GraphicsContext gc, double x, double y, double opacity) {
        gc.setStroke(withOpacity(Color.LIGHTBLUE, opacity * 0.5));
        gc.setLineWidth(1.0);
        gc.setLineDashes(5, 5);
        gc.strokeOval(x - COMM_RANGE_RADIUS, y - COMM_RANGE_RADIUS,
                      COMM_RANGE_RADIUS * 2, COMM_RANGE_RADIUS * 2);
        gc.setLineDashes(null); // Reset dashes
    }
    
    /**
     * Draw selection highlight around agent
     */
    private void drawSelectionHighlight(GraphicsContext gc, double x, double y, double opacity) {
        // Pulse effect
        long currentTime = System.currentTimeMillis();
        double pulsePhase = ((currentTime - pulseStartTime) % 1000) / 1000.0;
        double pulseScale = 1.0 + Math.sin(pulsePhase * Math.PI * 2) * 0.2;
        
        double highlightRadius = AGENT_RADIUS * pulseScale + 5;
        
        gc.setStroke(withOpacity(Color.YELLOW, opacity));
        gc.setLineWidth(SELECTION_HIGHLIGHT_WIDTH);
        gc.strokeOval(x - highlightRadius, y - highlightRadius,
                      highlightRadius * 2, highlightRadius * 2);
    }
    
    /**
     * Draw role icon (leader, scout, guard)
     */
    private void drawRoleIcon(GraphicsContext gc, double x, double y, String role, double opacity) {
        if (role == null || role.isEmpty()) return;
        
        double iconSize = 6;
        double iconX = x + AGENT_RADIUS;
        double iconY = y - AGENT_RADIUS;
        
        gc.setFill(withOpacity(Color.WHITE, opacity));
        gc.fillOval(iconX - iconSize/2, iconY - iconSize/2, iconSize, iconSize);
        
        gc.setFill(withOpacity(Color.BLACK, opacity));
        gc.setFont(new Font("Arial", 8));
        gc.setTextAlign(TextAlignment.CENTER);
        
        String iconChar = switch (role.toUpperCase()) {
            case "LEADER" -> "L";
            case "SCOUT" -> "S";
            case "GUARD" -> "G";
            default -> "?";
        };
        
        gc.fillText(iconChar, iconX, iconY + 3);
    }
    
    /**
     * Draw agent label with ID and status
     */
    private void drawAgentLabel(GraphicsContext gc, AgentState agent, double opacity) {
        double x = agent.position.x;
        double y = agent.position.y;
        
        gc.setFill(withOpacity(Color.BLACK, opacity));
        gc.setFont(new Font("Arial", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        
        String label = agent.agentName != null ? agent.agentName : String.valueOf(agent.agentId);
        gc.fillText(label, x, y - AGENT_RADIUS - 5);
        
        // Battery percentage below
        String battery = String.format("%.0f%%", agent.batteryLevel * 100);
        gc.setFont(new Font("Arial", 8));
        gc.fillText(battery, x, y + AGENT_RADIUS + 12);
    }
    
    /**
     * Update trail with new position
     */
    private void updateTrail(String agentId, double x, double y) {
        Queue<TrailPoint> trail = agentTrails.computeIfAbsent(agentId, k -> new LinkedList<>());
        
        trail.add(new TrailPoint(x, y, System.currentTimeMillis()));
        
        // Remove old points
        while (trail.size() > TRAIL_MAX_POINTS) {
            trail.poll();
        }
    }
    
    /**
     * Draw agent trail showing recent path
     */
    private void drawTrail(GraphicsContext gc, String agentId, double opacity) {
        Queue<TrailPoint> trail = agentTrails.get(agentId);
        if (trail == null || trail.size() < 2) return;
        
        TrailPoint[] points = trail.toArray(new TrailPoint[0]);
        
        for (int i = 0; i < points.length - 1; i++) {
            double age = (System.currentTimeMillis() - points[i].timestamp) / 1000.0;
            double trailOpacity = Math.max(0, 1.0 - age) * opacity;
            
            gc.setStroke(withOpacity(Color.LIGHTGRAY, trailOpacity));
            gc.setLineWidth(2.0);
            gc.strokeLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y);
        }
    }
    
    /**
     * Get color based on agent status
     */
    private Color getStatusColor(AgentStatus status) {
        return switch (status) {
            case ACTIVE -> theme.activeColor;
            case BATTERY_LOW -> theme.batteryLowColor;
            case FAILED -> theme.failedColor;
            case INACTIVE -> theme.inactiveColor;
            default -> Color.GRAY;
        };
    }
    
    /**
     * Create color with specified opacity
     */
    private Color withOpacity(Color color, double opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 
                        color.getOpacity() * opacity);
    }
    
    /**
     * Fade in animation for agent spawn
     */
    public void fadeIn(String agentId, double duration) {
        agentOpacity.put(agentId, 0.0);
        // Animation would be handled in update loop
    }
    
    /**
     * Fade out animation for agent despawn
     */
    public void fadeOut(String agentId, double duration) {
        // Animation would be handled in update loop
    }
    
    /**
     * Select an agent for highlighting
     */
    public void selectAgent(String agentId) {
        this.selectedAgentId = agentId;
        this.pulseStartTime = System.currentTimeMillis();
    }
    
    /**
     * Clear agent selection
     */
    public void clearSelection() {
        this.selectedAgentId = null;
    }
    
    /**
     * Update animations (call every frame)
     */
    public void update(double deltaTime) {
        // Update fade animations, pulse effects, etc.
    }
    
    /**
     * Set theme colors
     */
    public void setTheme(ThemeColors theme) {
        this.theme = theme;
    }
    
    /**
     * Trail point for agent path history
     */
    private static class TrailPoint {
        final double x;
        final double y;
        final long timestamp;
        
        TrailPoint(double x, double y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Theme color configuration
     */
    public static class ThemeColors {
        public Color activeColor = Color.GREEN;
        public Color batteryLowColor = Color.YELLOW;
        public Color failedColor = Color.RED;
        public Color inactiveColor = Color.GRAY;
    }
}
