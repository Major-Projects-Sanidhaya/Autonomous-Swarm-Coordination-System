/**
 * BOUNDARYMANAGER CLASS - World Boundaries and Safe Zone Management (Week 5-6)
 *
 * PURPOSE:
 * - Manages world boundaries and operational limits
 * - Defines restricted areas and safe zones
 * - Enforces movement constraints and spatial rules
 * - Integrates with PhysicsEngine for boundary enforcement
 *
 * DESIGN PATTERNS USED:
 * 1. Singleton Pattern - Single boundary configuration for entire system
 * 2. Strategy Pattern - Different boundary enforcement strategies
 * 3. Observer Pattern - Notify when agents violate boundaries
 * 4. Composite Pattern - Complex zones composed of simple shapes
 *
 * CORE FUNCTIONS:
 * 1. defineWorldBounds() - Set overall simulation area
 * 2. addSafeZone() - Define allowed operational areas
 * 3. addRestrictedZone() - Define forbidden areas
 * 4. checkBoundaryViolation() - Test if position is valid
 * 5. getNearestSafePoint() - Find closest valid location
 *
 * ZONE TYPES:
 * 1. World Boundary - Overall simulation limits
 * 2. Safe Zone - Areas where agents can operate
 * 3. Restricted Zone - Forbidden areas (obstacles, hazards)
 * 4. Charging Zone - Special areas for recharging
 * 5. Mission Zone - Task-specific operational areas
 *
 * BOUNDARY ENFORCEMENT:
 * - SOFT: Warn but allow violations (logging only)
 * - MEDIUM: Push agents back to valid positions
 * - HARD: Stop agents at boundary, reverse direction
 * - TELEPORT: Move violating agents to safe location
 *
 * USAGE PATTERNS:
 * 1. Initialize boundaries:
 *    boundaryManager.setWorldBounds(0, 0, 800, 600);
 *
 * 2. Add safe zone:
 *    boundaryManager.addSafeZone("operationalArea", rect);
 *
 * 3. Check validity:
 *    if (!boundaryManager.isPositionValid(agentPos)) { relocate(); }
 *
 * 4. Handle violation:
 *    Point2D safe = boundaryManager.getNearestSafePoint(violatingPos);
 *
 * INTEGRATION POINTS:
 * - Used by: PhysicsEngine for boundary checks
 * - Configured by: SystemController and user commands
 * - Monitored by: SystemMetrics for violation tracking
 * - Visualized by: Anthony's UI for operator awareness
 */
package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoundaryManager {
    // Singleton instance
    private static BoundaryManager instance;

    // World boundaries
    private double worldMinX;
    private double worldMinY;
    private double worldMaxX;
    private double worldMaxY;

    // Zone management
    private Map<String, Zone> safeZones;
    private Map<String, Zone> restrictedZones;
    private Map<String, Zone> specialZones;

    // Enforcement settings
    private EnforcementMode enforcementMode;
    private EventBus eventBus;

    // Violation tracking
    private int totalViolations;
    private Map<Integer, Integer> violationsByAgent;

    /**
     * Enforcement modes
     */
    public enum EnforcementMode {
        SOFT,      // Warn only, allow violations
        MEDIUM,    // Gently push back to valid area
        HARD,      // Stop at boundary, reverse direction
        TELEPORT   // Move to nearest safe point
    }

    /**
     * Zone types
     */
    public enum ZoneType {
        SAFE,         // Allowed operational area
        RESTRICTED,   // Forbidden area
        CHARGING,     // Battery recharge station
        MISSION,      // Task-specific area
        SPAWN         // Agent creation area
    }

    /**
     * Private constructor for Singleton pattern
     */
    private BoundaryManager() {
        // Default world boundaries (800x600)
        this.worldMinX = 0.0;
        this.worldMinY = 0.0;
        this.worldMaxX = 800.0;
        this.worldMaxY = 600.0;

        this.safeZones = new ConcurrentHashMap<>();
        this.restrictedZones = new ConcurrentHashMap<>();
        this.specialZones = new ConcurrentHashMap<>();

        this.enforcementMode = EnforcementMode.MEDIUM;
        this.totalViolations = 0;
        this.violationsByAgent = new ConcurrentHashMap<>();
    }

    /**
     * Get singleton instance
     */
    public static synchronized BoundaryManager getInstance() {
        if (instance == null) {
            instance = new BoundaryManager();
        }
        return instance;
    }

    /**
     * Set EventBus for violation notifications
     */
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    // ==================== WORLD BOUNDARY MANAGEMENT ====================

    /**
     * Set world boundaries
     */
    public void setWorldBounds(double minX, double minY, double maxX, double maxY) {
        if (maxX <= minX || maxY <= minY) {
            throw new IllegalArgumentException("Invalid boundary dimensions");
        }

        this.worldMinX = minX;
        this.worldMinY = minY;
        this.worldMaxX = maxX;
        this.worldMaxY = maxY;

        System.out.println(String.format("World bounds set: (%.1f,%.1f) to (%.1f,%.1f)",
            minX, minY, maxX, maxY));
    }

    /**
     * Get world width
     */
    public double getWorldWidth() {
        return worldMaxX - worldMinX;
    }

    /**
     * Get world height
     */
    public double getWorldHeight() {
        return worldMaxY - worldMinY;
    }

    /**
     * Check if position is within world bounds
     */
    public boolean isWithinWorldBounds(Point2D position) {
        return position.x >= worldMinX && position.x <= worldMaxX &&
               position.y >= worldMinY && position.y <= worldMaxY;
    }

    // ==================== ZONE MANAGEMENT ====================

    /**
     * Add safe zone (allowed operational area)
     */
    public void addSafeZone(String zoneId, Zone zone) {
        safeZones.put(zoneId, zone);
        System.out.println("Added safe zone: " + zoneId);
    }

    /**
     * Add restricted zone (forbidden area)
     */
    public void addRestrictedZone(String zoneId, Zone zone) {
        restrictedZones.put(zoneId, zone);
        System.out.println("Added restricted zone: " + zoneId);
    }

    /**
     * Add special zone (charging, mission, etc.)
     */
    public void addSpecialZone(String zoneId, Zone zone, ZoneType type) {
        zone.zoneType = type;
        specialZones.put(zoneId, zone);
        System.out.println("Added special zone: " + zoneId + " (" + type + ")");
    }

    /**
     * Remove zone
     */
    public void removeZone(String zoneId) {
        safeZones.remove(zoneId);
        restrictedZones.remove(zoneId);
        specialZones.remove(zoneId);
    }

    /**
     * Clear all zones
     */
    public void clearAllZones() {
        safeZones.clear();
        restrictedZones.clear();
        specialZones.clear();
        System.out.println("All zones cleared");
    }

    // ==================== BOUNDARY CHECKING ====================

    /**
     * Check if position is valid (within bounds and not restricted)
     */
    public boolean isPositionValid(Point2D position) {
        // Must be within world bounds
        if (!isWithinWorldBounds(position)) {
            return false;
        }

        // Must not be in restricted zone
        if (isInRestrictedZone(position)) {
            return false;
        }

        // If safe zones are defined, must be in one
        if (!safeZones.isEmpty()) {
            return isInSafeZone(position);
        }

        return true;
    }

    /**
     * Check if position is in any safe zone
     */
    public boolean isInSafeZone(Point2D position) {
        if (safeZones.isEmpty()) {
            return true; // No safe zones defined = everywhere is safe
        }

        for (Zone zone : safeZones.values()) {
            if (zone.contains(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if position is in any restricted zone
     */
    public boolean isInRestrictedZone(Point2D position) {
        for (Zone zone : restrictedZones.values()) {
            if (zone.contains(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check boundary violation and handle according to mode
     */
    public Point2D enforceBoundaries(AgentState agent) {
        Point2D position = agent.position;

        if (isPositionValid(position)) {
            return position; // No violation
        }

        // Record violation
        recordViolation(agent.agentId);

        // Handle according to enforcement mode
        switch (enforcementMode) {
            case SOFT:
                return handleSoftEnforcement(agent, position);

            case MEDIUM:
                return handleMediumEnforcement(agent, position);

            case HARD:
                return handleHardEnforcement(agent, position);

            case TELEPORT:
                return handleTeleportEnforcement(agent, position);

            default:
                return position;
        }
    }

    /**
     * Soft enforcement - warn only
     */
    private Point2D handleSoftEnforcement(AgentState agent, Point2D position) {
        System.out.println("Warning: Agent " + agent.agentId + " violating boundaries at " + position);
        publishViolationEvent(agent.agentId, position);
        return position; // Allow violation
    }

    /**
     * Medium enforcement - push back gently
     */
    private Point2D handleMediumEnforcement(AgentState agent, Point2D position) {
        Point2D safePoint = getNearestSafePoint(position);

        // Gradually move toward safe point
        double pushFactor = 0.1; // 10% correction per frame
        double newX = position.x + (safePoint.x - position.x) * pushFactor;
        double newY = position.y + (safePoint.y - position.y) * pushFactor;

        agent.position = new Point2D(newX, newY);
        return agent.position;
    }

    /**
     * Hard enforcement - stop at boundary
     */
    private Point2D handleHardEnforcement(AgentState agent, Point2D position) {
        Point2D safePoint = getNearestSafePoint(position);

        // Clamp to boundary
        agent.position = safePoint;

        // Reverse velocity component that caused violation
        if (position.x < worldMinX || position.x > worldMaxX) {
            agent.velocity.x *= -1;
        }
        if (position.y < worldMinY || position.y > worldMaxY) {
            agent.velocity.y *= -1;
        }

        return safePoint;
    }

    /**
     * Teleport enforcement - instant relocation
     */
    private Point2D handleTeleportEnforcement(AgentState agent, Point2D position) {
        Point2D safePoint = getNearestSafePoint(position);
        agent.position = safePoint;
        agent.velocity = new Vector2D(0, 0); // Stop movement
        System.out.println("Agent " + agent.agentId + " teleported to safe location: " + safePoint);
        return safePoint;
    }

    /**
     * Get nearest valid point from given position
     */
    public Point2D getNearestSafePoint(Point2D position) {
        // Clamp to world bounds first
        double x = Math.max(worldMinX, Math.min(worldMaxX, position.x));
        double y = Math.max(worldMinY, Math.min(worldMaxY, position.y));
        Point2D clamped = new Point2D(x, y);

        // If in restricted zone, find nearest safe point
        if (isInRestrictedZone(clamped)) {
            return findNearestPointOutsideRestrictedZones(clamped);
        }

        return clamped;
    }

    /**
     * Find nearest point outside all restricted zones
     */
    private Point2D findNearestPointOutsideRestrictedZones(Point2D position) {
        // Sample points in expanding radius
        double searchRadius = 10.0;
        int maxAttempts = 20;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Sample 8 directions around position
            for (int angle = 0; angle < 360; angle += 45) {
                double rad = Math.toRadians(angle);
                double x = position.x + Math.cos(rad) * searchRadius;
                double y = position.y + Math.sin(rad) * searchRadius;
                Point2D candidate = new Point2D(x, y);

                if (isPositionValid(candidate)) {
                    return candidate;
                }
            }
            searchRadius += 10.0; // Expand search
        }

        // Fallback: center of world
        return new Point2D((worldMinX + worldMaxX) / 2, (worldMinY + worldMaxY) / 2);
    }

    // ==================== VIOLATION TRACKING ====================

    /**
     * Record boundary violation
     */
    private void recordViolation(int agentId) {
        totalViolations++;
        violationsByAgent.merge(agentId, 1, Integer::sum);
    }

    /**
     * Publish violation event
     */
    private void publishViolationEvent(int agentId, Point2D position) {
        if (eventBus != null) {
            SystemEvent event = SystemEvent.warning(
                "BOUNDARY_VIOLATION",
                String.valueOf(agentId),
                "Agent " + agentId + " violated boundary at " + position
            );
            eventBus.publish(event);
        }
    }

    /**
     * Get violation count for agent
     */
    public int getViolationCount(int agentId) {
        return violationsByAgent.getOrDefault(agentId, 0);
    }

    /**
     * Get total violations
     */
    public int getTotalViolations() {
        return totalViolations;
    }

    /**
     * Reset violation tracking
     */
    public void resetViolationTracking() {
        totalViolations = 0;
        violationsByAgent.clear();
    }

    // ==================== CONFIGURATION ====================

    /**
     * Set enforcement mode
     */
    public void setEnforcementMode(EnforcementMode mode) {
        this.enforcementMode = mode;
        System.out.println("Boundary enforcement mode: " + mode);
    }

    public EnforcementMode getEnforcementMode() {
        return enforcementMode;
    }

    // ==================== GETTERS ====================

    public double getWorldMinX() { return worldMinX; }
    public double getWorldMinY() { return worldMinY; }
    public double getWorldMaxX() { return worldMaxX; }
    public double getWorldMaxY() { return worldMaxY; }

    public Map<String, Zone> getSafeZones() { return new HashMap<>(safeZones); }
    public Map<String, Zone> getRestrictedZones() { return new HashMap<>(restrictedZones); }
    public Map<String, Zone> getSpecialZones() { return new HashMap<>(specialZones); }

    // ==================== ZONE CLASS ====================

    /**
     * Zone - represents a spatial area
     */
    public static class Zone {
        public String zoneId;
        public ZoneType zoneType;
        public ZoneShape shape;

        // Rectangular zone parameters
        public double minX, minY, maxX, maxY;

        // Circular zone parameters
        public Point2D center;
        public double radius;

        /**
         * Create rectangular zone
         */
        public static Zone createRectangle(String id, double minX, double minY,
                                          double maxX, double maxY) {
            Zone zone = new Zone();
            zone.zoneId = id;
            zone.shape = ZoneShape.RECTANGLE;
            zone.minX = minX;
            zone.minY = minY;
            zone.maxX = maxX;
            zone.maxY = maxY;
            return zone;
        }

        /**
         * Create circular zone
         */
        public static Zone createCircle(String id, Point2D center, double radius) {
            Zone zone = new Zone();
            zone.zoneId = id;
            zone.shape = ZoneShape.CIRCLE;
            zone.center = center;
            zone.radius = radius;
            return zone;
        }

        /**
         * Check if point is within zone
         */
        public boolean contains(Point2D point) {
            switch (shape) {
                case RECTANGLE:
                    return point.x >= minX && point.x <= maxX &&
                           point.y >= minY && point.y <= maxY;

                case CIRCLE:
                    double distance = point.distanceTo(center);
                    return distance <= radius;

                default:
                    return false;
            }
        }

        @Override
        public String toString() {
            if (shape == ZoneShape.RECTANGLE) {
                return String.format("Zone[%s: Rectangle (%.1f,%.1f)-(%.1f,%.1f)]",
                    zoneId, minX, minY, maxX, maxY);
            } else {
                return String.format("Zone[%s: Circle center=(%.1f,%.1f) radius=%.1f]",
                    zoneId, center.x, center.y, radius);
            }
        }
    }

    /**
     * Zone shapes
     */
    public enum ZoneShape {
        RECTANGLE,
        CIRCLE
    }
}
