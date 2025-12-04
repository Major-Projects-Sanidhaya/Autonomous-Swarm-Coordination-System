package com.team6.swarm.ui;

import com.team6.swarm.core.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Week 7-8: Visual polish and animations
 * Purpose: Make system impressive and intuitive
 * Author: Anthony (UI Team)
 */
public class VisualizationEffects {
    
    private final Map<String, AgentTrail> agentTrails = new HashMap<>();
    private final List<FormationGuide> formationGuides = new ArrayList<>();
    private final List<CommunicationPulse> communicationPulses = new ArrayList<>();
    private final List<EmergencyAlert> emergencyAlerts = new ArrayList<>();
    
    /**
     * Draw agent trail
     */
    public void drawAgentTrail(GraphicsContext gc, String agentId, Point2D currentPos, Color color) {
        AgentTrail trail = agentTrails.computeIfAbsent(agentId, k -> new AgentTrail());
        trail.addPoint(currentPos);
        trail.render(gc, color);
    }
    
    /**
     * Draw formation guides
     */
    public void drawFormationGuides(GraphicsContext gc, List<Point2D> targetPositions) {
        for (Point2D target : targetPositions) {
            // Ghost circle
            gc.setStroke(new Color(0.5, 0.5, 1.0, 0.3));
            gc.setLineWidth(2.0);
            gc.setLineDashes(5, 5);
            gc.strokeOval(target.getX() - 8, target.getY() - 8, 16, 16);
            gc.setLineDashes(null);
        }
    }
    
    /**
     * Add communication pulse
     */
    public void addCommunicationPulse(Point2D origin) {
        communicationPulses.add(new CommunicationPulse(origin));
    }
    
    /**
     * Update and render communication pulses
     */
    public void updateCommunicationPulses(GraphicsContext gc, double deltaTime) {
        Iterator<CommunicationPulse> it = communicationPulses.iterator();
        while (it.hasNext()) {
            CommunicationPulse pulse = it.next();
            pulse.update(deltaTime);
            if (pulse.isExpired()) {
                it.remove();
            } else {
                pulse.render(gc);
            }
        }
    }
    
    /**
     * Add emergency alert
     */
    public void addEmergencyAlert(Point2D location, String message) {
        emergencyAlerts.add(new EmergencyAlert(location, message));
    }
    
    /**
     * Update and render emergency alerts
     */
    public void updateEmergencyAlerts(GraphicsContext gc, double deltaTime) {
        Iterator<EmergencyAlert> it = emergencyAlerts.iterator();
        while (it.hasNext()) {
            EmergencyAlert alert = it.next();
            alert.update(deltaTime);
            if (alert.isExpired()) {
                it.remove();
            } else {
                alert.render(gc);
            }
        }
    }
    
    /**
     * Clear all effects
     */
    public void clearAll() {
        agentTrails.clear();
        formationGuides.clear();
        communicationPulses.clear();
        emergencyAlerts.clear();
    }
    
    // Inner classes for effects
    
    private static class AgentTrail {
        private final Queue<TrailPoint> points = new LinkedList<>();
        private static final int MAX_POINTS = 30;
        private static final double FADE_DURATION = 1.0; // seconds
        
        void addPoint(Point2D point) {
            points.add(new TrailPoint(point, System.currentTimeMillis()));
            while (points.size() > MAX_POINTS) {
                points.poll();
            }
        }
        
        void render(GraphicsContext gc, Color baseColor) {
            if (points.size() < 2) return;
            
            TrailPoint[] pointArray = points.toArray(new TrailPoint[0]);
            long currentTime = System.currentTimeMillis();
            
            for (int i = 0; i < pointArray.length - 1; i++) {
                double age = (currentTime - pointArray[i].timestamp) / 1000.0;
                double opacity = Math.max(0, 1.0 - (age / FADE_DURATION));
                
                Color trailColor = new Color(baseColor.getRed(), baseColor.getGreen(), 
                                            baseColor.getBlue(), opacity * 0.5);
                gc.setStroke(trailColor);
                gc.setLineWidth(2.0);
                gc.strokeLine(pointArray[i].pos.getX(), pointArray[i].pos.getY(),
                            pointArray[i + 1].pos.getX(), pointArray[i + 1].pos.getY());
            }
        }
        
        private record TrailPoint(Point2D pos, long timestamp) {}
    }
    
    private static class CommunicationPulse {
        private final Point2D origin;
        private double radius;
        private double lifetime;
        private static final double MAX_RADIUS = 150.0;
        private static final double EXPANSION_SPEED = 100.0; // pixels/second
        private static final double MAX_LIFETIME = MAX_RADIUS / EXPANSION_SPEED;
        
        CommunicationPulse(Point2D origin) {
            this.origin = origin;
            this.radius = 0;
            this.lifetime = 0;
        }
        
        void update(double deltaTime) {
            radius += EXPANSION_SPEED * deltaTime;
            lifetime += deltaTime;
        }
        
        void render(GraphicsContext gc) {
            double opacity = Math.max(0, 1.0 - (lifetime / MAX_LIFETIME));
            gc.setStroke(new Color(0.3, 0.6, 1.0, opacity));
            gc.setLineWidth(2.0);
            gc.strokeOval(origin.getX() - radius, origin.getY() - radius, 
                         radius * 2, radius * 2);
        }
        
        boolean isExpired() {
            return lifetime >= MAX_LIFETIME;
        }
    }
    
    private static class EmergencyAlert {
        private final Point2D location;
        private final String message;
        private double lifetime;
        private static final double FLASH_DURATION = 3.0; // seconds
        
        EmergencyAlert(Point2D location, String message) {
            this.location = location;
            this.message = message;
            this.lifetime = 0;
        }
        
        void update(double deltaTime) {
            lifetime += deltaTime;
        }
        
        void render(GraphicsContext gc) {
            // Flashing effect
            double flashPhase = (lifetime % 0.5) / 0.5;
            double opacity = flashPhase < 0.5 ? 1.0 : 0.5;
            
            // Red circle
            gc.setStroke(new Color(1, 0, 0, opacity));
            gc.setLineWidth(4.0);
            gc.strokeOval(location.getX() - 20, location.getY() - 20, 40, 40);
            
            // Alert icon
            gc.setFill(new Color(1, 0, 0, opacity));
            gc.fillText("⚠", location.getX() - 10, location.getY() + 5);
        }
        
        boolean isExpired() {
            return lifetime >= FLASH_DURATION;
        }
    }
}
