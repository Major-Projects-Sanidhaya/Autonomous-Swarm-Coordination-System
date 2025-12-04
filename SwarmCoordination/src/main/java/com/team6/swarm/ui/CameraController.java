package com.team6.swarm.ui;

import com.team6.swarm.core.Point2D;

/**
 * Week 7-8: View control and navigation
 * Purpose: Users need to explore the world
 * Author: Anthony (UI Team)
 */
public class CameraController {
    
    // Camera properties
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 1.0;
    
    // Bounds
    private double minZoom = 0.25;
    private double maxZoom = 4.0;
    
    // Follow mode
    private boolean followMode = false;
    private String followAgentId = null;
    private Point2D followTarget = null;
    
    // View mode
    public enum ViewMode {
        FREE,           // User controlled
        FOLLOW_AGENT,   // Follow selected agent
        FOLLOW_CENTER,  // Follow formation center
        AUTO            // Auto-pan to action
    }
    
    private ViewMode viewMode = ViewMode.FREE;
    
    /**
     * Pan camera by delta
     */
    public void pan(double deltaX, double deltaY) {
        if (viewMode == ViewMode.FREE) {
            offsetX += deltaX / zoom;
            offsetY += deltaY / zoom;
        }
    }
    
    /**
     * Zoom camera
     */
    public void zoom(double factor, double centerX, double centerY) {
        double oldZoom = zoom;
        zoom *= factor;
        
        // Clamp zoom
        zoom = Math.max(minZoom, Math.min(maxZoom, zoom));
        
        // Adjust offset to zoom toward mouse position
        double zoomDelta = zoom / oldZoom - 1.0;
        offsetX -= (centerX / oldZoom) * zoomDelta;
        offsetY -= (centerY / oldZoom) * zoomDelta;
    }
    
    /**
     * Set zoom level directly
     */
    public void setZoom(double zoom) {
        this.zoom = Math.max(minZoom, Math.min(maxZoom, zoom));
    }
    
    /**
     * Reset camera to default
     */
    public void reset() {
        offsetX = 0;
        offsetY = 0;
        zoom = 1.0;
        viewMode = ViewMode.FREE;
        followMode = false;
        followAgentId = null;
    }
    
    /**
     * Start following an agent
     */
    public void followAgent(String agentId, Point2D position) {
        this.followMode = true;
        this.followAgentId = agentId;
        this.followTarget = position;
        this.viewMode = ViewMode.FOLLOW_AGENT;
    }
    
    /**
     * Update follow position
     */
    public void updateFollowTarget(Point2D position) {
        if (followMode && position != null) {
            this.followTarget = position;
        }
    }
    
    /**
     * Stop following
     */
    public void stopFollow() {
        this.followMode = false;
        this.followAgentId = null;
        this.viewMode = ViewMode.FREE;
    }
    
    /**
     * Transform world coordinates to screen coordinates
     */
    public Point2D worldToScreen(Point2D world, double canvasWidth, double canvasHeight) {
        double screenX = (world.getX() + offsetX) * zoom + canvasWidth / 2;
        double screenY = (world.getY() + offsetY) * zoom + canvasHeight / 2;
        return new Point2D(screenX, screenY);
    }
    
    /**
     * Transform screen coordinates to world coordinates
     */
    public Point2D screenToWorld(Point2D screen, double canvasWidth, double canvasHeight) {
        double worldX = (screen.getX() - canvasWidth / 2) / zoom - offsetX;
        double worldY = (screen.getY() - canvasHeight / 2) / zoom - offsetY;
        return new Point2D(worldX, worldY);
    }
    
    /**
     * Update camera for follow mode
     */
    public void update(double deltaTime) {
        if (followMode && followTarget != null) {
            // Smooth follow
            double smoothing = 0.1;
            offsetX += (followTarget.getX() - offsetX) * smoothing;
            offsetY += (followTarget.getY() - offsetY) * smoothing;
        }
    }
    
    // Getters
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getZoom() { return zoom; }
    public ViewMode getViewMode() { return viewMode; }
    public boolean isFollowing() { return followMode; }
}
