package com.team6.swarm.ui;

import com.team6.swarm.communication.ConnectionInfo;
import com.team6.swarm.communication.Message;
import com.team6.swarm.core.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Week 3: Draw communication links between agents
 * Purpose: Visualize the communication network
 * Author: Anthony (UI Team)
 */
public class NetworkRenderer {
    
    // Message animation state
    private final List<MessageAnimation> activeAnimations = new ArrayList<>();
    
    // Configuration
    private boolean showCommunicationLinks = true;
    private boolean showMessageAnimations = true;
    private boolean showNetworkRanges = false;
    
    // Visual constants
    private static final double MESSAGE_SPEED = 200.0; // pixels per second
    private static final double MESSAGE_ICON_SIZE = 6.0;
    private static final double WEAK_SIGNAL_THRESHOLD = 0.3;
    private static final double MEDIUM_SIGNAL_THRESHOLD = 0.7;
    
    /**
     * Draw communication links between agents
     */
    public void drawCommunicationLinks(GraphicsContext gc, List<ConnectionInfo> connections, 
                                      Map<Integer, Point2D> agentPositions) {
        if (!showCommunicationLinks || connections == null) {
            return;
        }
        
        for (ConnectionInfo conn : connections) {
            drawConnection(gc, conn, agentPositions);
        }
    }
    
    /**
     * Draw a single connection
     * Note: Requires agent positions to be passed separately since ConnectionInfo only has agent IDs
     */
    private void drawConnection(GraphicsContext gc, ConnectionInfo conn, Map<Integer, Point2D> agentPositions) {
        Point2D from = agentPositions.get(conn.agentA);
        Point2D to = agentPositions.get(conn.agentB);
        if (from == null || to == null) return;
        double strength = conn.strength;
        
        // Determine line properties based on signal strength
        Color lineColor;
        double lineWidth;
        boolean dashed = false;
        
        if (strength >= MEDIUM_SIGNAL_THRESHOLD) {
            // Strong signal - green, solid line
            lineColor = Color.GREEN;
            lineWidth = 2.0;
        } else if (strength >= WEAK_SIGNAL_THRESHOLD) {
            // Medium signal - yellow, thinner line
            lineColor = Color.YELLOW;
            lineWidth = 1.5;
        } else {
            // Weak signal - red, dashed line
            lineColor = Color.RED;
            lineWidth = 1.0;
            dashed = true;
        }
        
        // Apply transparency based on strength
        double opacity = 0.3 + (strength * 0.4);
        lineColor = new Color(lineColor.getRed(), lineColor.getGreen(), 
                             lineColor.getBlue(), opacity);
        
        gc.setStroke(lineColor);
        gc.setLineWidth(lineWidth);
        
        if (dashed) {
            gc.setLineDashes(5, 5);
        }
        
        gc.strokeLine(from.x, from.y, to.x, to.y);
        
        if (dashed) {
            gc.setLineDashes(null); // Reset
        }
    }
    
    /**
     * Animate a message being sent
     */
    public void animateMessage(Message msg, Point2D from, Point2D to) {
        if (!showMessageAnimations) {
            return;
        }
        
        MessageAnimation animation = new MessageAnimation(msg, from, to);
        activeAnimations.add(animation);
    }
    
    /**
     * Update and render message animations
     */
    public void updateAndRenderAnimations(GraphicsContext gc, double deltaTime) {
        if (!showMessageAnimations) {
            return;
        }
        
        List<MessageAnimation> toRemove = new ArrayList<>();
        
        for (MessageAnimation anim : activeAnimations) {
            anim.update(deltaTime);
            
            if (anim.isComplete()) {
                toRemove.add(anim);
            } else {
                anim.render(gc);
            }
        }
        
        activeAnimations.removeAll(toRemove);
    }
    
    /**
     * Draw network topology visualization
     */
    public void drawNetworkTopology(GraphicsContext gc, List<Point2D> agentPositions) {
        if (!showNetworkRanges || agentPositions == null) {
            return;
        }
        
        // Draw communication range circles
        for (Point2D pos : agentPositions) {
            drawNetworkRange(gc, pos);
        }
    }
    
    /**
     * Draw communication range circle around an agent
     */
    private void drawNetworkRange(GraphicsContext gc, Point2D position) {
        double range = 100.0; // Default communication range
        
        gc.setStroke(new Color(0.5, 0.5, 1.0, 0.2));
        gc.setLineWidth(1.0);
        gc.setLineDashes(3, 3);
        
        gc.strokeOval(position.x - range, position.y - range,
                     range * 2, range * 2);
        
        gc.setLineDashes(null);
    }
    
    /**
     * Draw broadcast wave effect
     */
    public void drawBroadcastWave(GraphicsContext gc, Point2D origin, double radius, double opacity) {
        gc.setStroke(new Color(0.3, 0.6, 1.0, opacity));
        gc.setLineWidth(2.0);
        
        gc.strokeOval(origin.x - radius, origin.y - radius,
                     radius * 2, radius * 2);
    }
    
    // Configuration methods
    
    public void setShowCommunicationLinks(boolean show) {
        this.showCommunicationLinks = show;
    }
    
    public void setShowMessageAnimations(boolean show) {
        this.showMessageAnimations = show;
    }
    
    public void setShowNetworkRanges(boolean show) {
        this.showNetworkRanges = show;
    }
    
    /**
     * Message animation class
     */
    private static class MessageAnimation {
        private final Message message;
        private final Point2D fromPos;
        private final Point2D toPos;
        private double progress; // 0.0 to 1.0
        private final Color color;
        
        MessageAnimation(Message message, Point2D from, Point2D to) {
            this.message = message;
            this.fromPos = from;
            this.toPos = to;
            this.progress = 0.0;
            this.color = getMessageTypeColor(message);
        }
        
        void update(double deltaTime) {
            double distance = Math.sqrt(
                Math.pow(toPos.x - fromPos.x, 2) +
                Math.pow(toPos.y - fromPos.y, 2)
            );
            
            double progressIncrement = (MESSAGE_SPEED * deltaTime) / distance;
            progress += progressIncrement;
            
            if (progress > 1.0) {
                progress = 1.0;
            }
        }
        
        void render(GraphicsContext gc) {
            // Interpolate position
            double currentX = fromPos.x + (toPos.x - fromPos.x) * progress;
            double currentY = fromPos.y + (toPos.y - fromPos.y) * progress;
            
            // Draw message icon
            gc.setFill(color);
            gc.fillOval(currentX - MESSAGE_ICON_SIZE / 2, 
                       currentY - MESSAGE_ICON_SIZE / 2,
                       MESSAGE_ICON_SIZE, MESSAGE_ICON_SIZE);
            
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.0);
            gc.strokeOval(currentX - MESSAGE_ICON_SIZE / 2, 
                         currentY - MESSAGE_ICON_SIZE / 2,
                         MESSAGE_ICON_SIZE, MESSAGE_ICON_SIZE);
        }
        
        boolean isComplete() {
            return progress >= 1.0;
        }
        
        private Color getMessageTypeColor(Message msg) {
            if (msg == null) {
                return Color.GRAY;
            }
            
            return switch (msg.type) {
                case POSITION_UPDATE -> Color.BLUE;
                case VOTE_REQUEST, VOTE_RESPONSE -> Color.PURPLE;
                case TASK_ASSIGNMENT -> Color.ORANGE;
                case STATUS_UPDATE -> Color.CYAN;
                case EMERGENCY -> Color.RED;
                default -> Color.GRAY;
            };
        }
    }
    
    /**
     * Clear all active animations
     */
    public void clearAnimations() {
        activeAnimations.clear();
    }
    
    /**
     * Get count of active message animations
     */
    public int getActiveAnimationCount() {
        return activeAnimations.size();
    }
}
