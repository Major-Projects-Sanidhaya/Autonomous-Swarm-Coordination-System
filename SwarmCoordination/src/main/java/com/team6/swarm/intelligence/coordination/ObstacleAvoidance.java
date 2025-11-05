/**
 * OBSTACLEAVOIDANCE CLASS - Coordinated Navigation Around Obstacles
 *
 * PURPOSE:
 * - Coordinate swarm navigation around obstacles
 * - Maintain formation integrity during avoidance
 * - Provide multiple avoidance strategies based on obstacle type
 * - Enable collective decision-making for navigation
 */
package com.team6.swarm.intelligence.coordination;

import com.team6.swarm.core.*;
import java.util.*;

public class ObstacleAvoidance {
    // Avoidance parameters
    private static final double DETECTION_RANGE = 100.0;
    private static final double OBSTACLE_BUFFER = 20.0;
    private static final double AVOIDANCE_FORCE_MAX = 30.0;
    private static final double CRITICAL_DISTANCE = 30.0;
    
    // Obstacle tracking
    private Map<String, Obstacle> detectedObstacles;
    private Map<Integer, AvoidanceState> agentStates;
    
    // Strategy selection
    private AvoidanceStrategy currentStrategy;
    
    public ObstacleAvoidance() {
        this.detectedObstacles = new HashMap<>();
        this.agentStates = new HashMap<>();
        this.currentStrategy = AvoidanceStrategy.INDIVIDUAL;
    }
    
    /**
     * CALCULATE INDIVIDUAL AVOIDANCE FORCE
     */
    public static Vector2D calculateIndividualAvoidance(Point2D agentPosition,
                                                        Vector2D agentVelocity,
                                                        List<Obstacle> detectedObstacles) {
        Vector2D totalAvoidance = new Vector2D(0, 0);
        
        if (detectedObstacles == null || detectedObstacles.isEmpty()) {
            return totalAvoidance;
        }
        
        for (Obstacle obstacle : detectedObstacles) {
            double distance = agentPosition.distanceTo(obstacle.position);
            
            if (distance > DETECTION_RANGE) {
                continue;
            }
            
            // Calculate avoidance vector (away from obstacle)
            Vector2D avoidanceDir = new Vector2D(
                agentPosition.x - obstacle.position.x,
                agentPosition.y - obstacle.position.y
            );
            
            // Normalize
            double magnitude = avoidanceDir.magnitude();
            if (magnitude > 0) {
                avoidanceDir = new Vector2D(
                    avoidanceDir.x / magnitude,
                    avoidanceDir.y / magnitude
                );
            }
            
            // Stronger avoidance for closer obstacles
            double strength = 0;
            if (distance < CRITICAL_DISTANCE) {
                strength = AVOIDANCE_FORCE_MAX;
            } else {
                strength = AVOIDANCE_FORCE_MAX * 
                          (DETECTION_RANGE - distance) / DETECTION_RANGE;
            }
            
            // Add to total avoidance
            totalAvoidance = new Vector2D(
                totalAvoidance.x + avoidanceDir.x * strength,
                totalAvoidance.y + avoidanceDir.y * strength
            );
        }
        
        return totalAvoidance;
    }
    
    /**
     * PREDICT COLLISION
     */
    public static boolean predictCollision(Point2D position, Vector2D velocity,
                                          Obstacle obstacle, double timeHorizon) {
        double futureX = position.x + velocity.x * timeHorizon;
        double futureY = position.y + velocity.y * timeHorizon;
        Point2D futurePosition = new Point2D(futureX, futureY);
        
        double distance = futurePosition.distanceTo(obstacle.position);
        return distance < (obstacle.radius + OBSTACLE_BUFFER);
    }
    
    /**
     * PLAN COLLECTIVE AVOIDANCE MANEUVER
     */
    public AvoidanceManeuver planCollectiveAvoidance(Point2D swarmCenter, Obstacle obstacle,
                                                      List<AgentState> agents) {
        Vector2D toObstacle = new Vector2D(
            obstacle.position.x - swarmCenter.x,
            obstacle.position.y - swarmCenter.y
        );
        
        Vector2D swarmDirection = calculateSwarmDirection(agents);
        double crossProduct = toObstacle.x * swarmDirection.y - toObstacle.y * swarmDirection.x;
        
        AvoidanceDirection direction = crossProduct > 0 ? 
            AvoidanceDirection.LEFT : AvoidanceDirection.RIGHT;
        
        Point2D maneuverPoint = calculateManeuverWaypoint(
            swarmCenter, obstacle, direction);
        
        AvoidanceManeuver maneuver = new AvoidanceManeuver(
            "avoid_" + System.currentTimeMillis(),
            obstacle,
            direction,
            maneuverPoint,
            agents.size()
        );
        
        System.out.println(String.format(
            "Collective Avoidance Planned: Go %s around obstacle at (%.1f, %.1f)",
            direction, obstacle.position.x, obstacle.position.y
        ));
        
        return maneuver;
    }
    
    /**
     * EXECUTE COLLECTIVE MANEUVER
     */
    public List<MovementCommand> executeCollectiveManeuver(AvoidanceManeuver maneuver, 
                                                           List<AgentState> agents) {
        List<MovementCommand> commands = new ArrayList<>();
        
        for (AgentState agent : agents) {
            Point2D targetPosition = calculateFormationOffset(
                agent, maneuver.maneuverWaypoint, agents);
            
            MovementCommand cmd = new MovementCommand();
            cmd.agentId = agent.agentId;
            cmd.type = MovementType.MOVE_TO_TARGET;
            cmd.parameters.put("target", targetPosition);
            
            commands.add(cmd);
        }
        
        System.out.println(String.format(
            "Collective Maneuver Executed: %d agents moving to avoid obstacle",
            commands.size()
        ));
        
        return commands;
    }
    
    /**
     * Calculate maneuver waypoint
     */
    private Point2D calculateManeuverWaypoint(Point2D swarmCenter, Obstacle obstacle,
                                             AvoidanceDirection direction) {
        Vector2D toObstacle = new Vector2D(
            obstacle.position.x - swarmCenter.x,
            obstacle.position.y - swarmCenter.y
        );
        
        // Perpendicular vector (rotate 90 degrees)
        Vector2D perpendicular;
        if (direction == AvoidanceDirection.LEFT) {
            perpendicular = new Vector2D(-toObstacle.y, toObstacle.x);
        } else {
            perpendicular = new Vector2D(toObstacle.y, -toObstacle.x);
        }
        
        // Normalize and scale
        double magnitude = Math.sqrt(perpendicular.x * perpendicular.x +
                                    perpendicular.y * perpendicular.y);
        if (magnitude > 0) {
            perpendicular = new Vector2D(
                perpendicular.x / magnitude,
                perpendicular.y / magnitude
            );
        }
        
        double offsetDistance = obstacle.radius + OBSTACLE_BUFFER + 30.0;
        double waypointX = obstacle.position.x + perpendicular.x * offsetDistance;
        double waypointY = obstacle.position.y + perpendicular.y * offsetDistance;
        
        return new Point2D(waypointX, waypointY);
    }
    
    /**
     * Calculate swarm's average direction
     */
    private Vector2D calculateSwarmDirection(List<AgentState> agents) {
        double totalX = 0;
        double totalY = 0;
        
        for (AgentState agent : agents) {
            totalX += agent.velocity.x;
            totalY += agent.velocity.y;
        }
        
        if (agents.size() > 0) {
            totalX /= agents.size();
            totalY /= agents.size();
        }
        
        return new Vector2D(totalX, totalY);
    }
    
    /**
     * Calculate agent's position maintaining formation offset
     */
    private Point2D calculateFormationOffset(AgentState agent, Point2D maneuverWaypoint,
                                            List<AgentState> agents) {
        Point2D swarmCenter = calculateSwarmCenter(agents);
        
        Vector2D offset = new Vector2D(
            agent.position.x - swarmCenter.x,
            agent.position.y - swarmCenter.y
        );
        
        return new Point2D(
            maneuverWaypoint.x + offset.x,
            maneuverWaypoint.y + offset.y
        );
    }
    
    /**
     * Calculate swarm center position
     */
    private Point2D calculateSwarmCenter(List<AgentState> agents) {
        double sumX = 0;
        double sumY = 0;
        
        for (AgentState agent : agents) {
            sumX += agent.position.x;
            sumY += agent.position.y;
        }
        
        if (agents.size() > 0) {
            sumX /= agents.size();
            sumY /= agents.size();
        }
        
        return new Point2D(sumX, sumY);
    }
    
    /**
     * CALCULATE PATH AROUND OBSTACLES
     */
    public static List<Point2D> calculatePath(Point2D start, Point2D goal,
                                             List<Obstacle> obstacles) {
        List<Point2D> path = new ArrayList<>();
        path.add(start);
        
        if (isPathClear(start, goal, obstacles)) {
            path.add(goal);
            return path;
        }
        
        List<Obstacle> blockingObstacles = findBlockingObstacles(start, goal, obstacles);
        
        for (Obstacle obstacle : blockingObstacles) {
            Point2D avoidancePoint = generateAvoidanceWaypoint(start, goal, obstacle);
            path.add(avoidancePoint);
        }
        
        path.add(goal);
        
        System.out.println(String.format(
            "Pathfinding: Generated path with %d waypoints around %d obstacles",
            path.size(), blockingObstacles.size()
        ));
        
        return path;
    }
    
    /**
     * Check if direct path between two points is clear
     */
    private static boolean isPathClear(Point2D start, Point2D goal,
                                      List<Obstacle> obstacles) {
        for (Obstacle obstacle : obstacles) {
            if (lineIntersectsObstacle(start, goal, obstacle)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Find obstacles that block direct path
     */
    private static List<Obstacle> findBlockingObstacles(Point2D start, Point2D goal,
                                                        List<Obstacle> obstacles) {
        List<Obstacle> blocking = new ArrayList<>();
        
        for (Obstacle obstacle : obstacles) {
            if (lineIntersectsObstacle(start, goal, obstacle)) {
                blocking.add(obstacle);
            }
        }
        
        return blocking;
    }
    
    /**
     * Check if line segment intersects obstacle
     */
    private static boolean lineIntersectsObstacle(Point2D start, Point2D end,
                                                  Obstacle obstacle) {
        Vector2D lineDir = new Vector2D(end.x - start.x, end.y - start.y);
        Vector2D toObstacle = new Vector2D(
            obstacle.position.x - start.x,
            obstacle.position.y - start.y
        );
        
        double lineLengthSq = lineDir.x * lineDir.x + lineDir.y * lineDir.y;
        if (lineLengthSq < 0.001) return false;
        
        double t = Math.max(0, Math.min(1,
            (toObstacle.x * lineDir.x + toObstacle.y * lineDir.y) / lineLengthSq
        ));
        
        Point2D closestPoint = new Point2D(
            start.x + t * lineDir.x,
            start.y + t * lineDir.y
        );
        
        double distance = closestPoint.distanceTo(obstacle.position);
        return distance < (obstacle.radius + OBSTACLE_BUFFER);
    }
    
    /**
     * Generate waypoint to avoid obstacle
     */
    private static Point2D generateAvoidanceWaypoint(Point2D start, Point2D goal,
                                                     Obstacle obstacle) {
        Vector2D toGoal = new Vector2D(goal.x - start.x, goal.y - start.y);
        Vector2D perpendicular = new Vector2D(-toGoal.y, toGoal.x);
        
        // Normalize
        double magnitude = Math.sqrt(perpendicular.x * perpendicular.x +
                                    perpendicular.y * perpendicular.y);
        if (magnitude > 0) {
            perpendicular = new Vector2D(
                perpendicular.x / magnitude,
                perpendicular.y / magnitude
            );
        }
        
        double offsetDist = obstacle.radius + OBSTACLE_BUFFER + 20.0;
        
        Point2D option1 = new Point2D(
            obstacle.position.x + perpendicular.x * offsetDist,
            obstacle.position.y + perpendicular.y * offsetDist
        );
        
        Point2D option2 = new Point2D(
            obstacle.position.x - perpendicular.x * offsetDist,
            obstacle.position.y - perpendicular.y * offsetDist
        );
        
        return goal.distanceTo(option1) < goal.distanceTo(option2) ? option1 : option2;
    }
    
    /**
     * SELECT AVOIDANCE STRATEGY
     */
    public AvoidanceStrategy selectStrategy(Obstacle obstacle, List<AgentState> agents) {
        if (obstacle.radius < 30 && agents.size() <= 5) {
            return AvoidanceStrategy.INDIVIDUAL;
        }
        
        Point2D swarmCenter = calculateSwarmCenter(agents);
        double distanceToObstacle = swarmCenter.distanceTo(obstacle.position);
        
        if (obstacle.radius > 50 && distanceToObstacle < 150) {
            return AvoidanceStrategy.COLLECTIVE;
        }
        
        if (detectedObstacles.size() > 3) {
            return AvoidanceStrategy.PATHFINDING;
        }
        
        return AvoidanceStrategy.INDIVIDUAL;
    }
    
    public void setStrategy(AvoidanceStrategy strategy) {
        this.currentStrategy = strategy;
        System.out.println("Avoidance strategy changed to: " + strategy);
    }
    
    /**
     * REGISTER OBSTACLE
     */
    public void registerObstacle(Obstacle obstacle) {
        detectedObstacles.put(obstacle.id, obstacle);
        System.out.println(String.format(
            "Obstacle registered: %s at (%.1f, %.1f) with radius %.1f",
            obstacle.id, obstacle.position.x, obstacle.position.y, obstacle.radius
        ));
    }
    
    public List<Obstacle> getAllObstacles() {
        return new ArrayList<>(detectedObstacles.values());
    }
    
    public List<Obstacle> getObstaclesNear(Point2D position, double range) {
        List<Obstacle> nearbyObstacles = new ArrayList<>();
        
        for (Obstacle obstacle : detectedObstacles.values()) {
            if (position.distanceTo(obstacle.position) < range) {
                nearbyObstacles.add(obstacle);
            }
        }
        
        return nearbyObstacles;
    }
    
    public void clearObstacles() {
        detectedObstacles.clear();
    }
    
    @Override
    public String toString() {
        return String.format(
            "ObstacleAvoidance[Strategy: %s | Obstacles: %d | Agents: %d]",
            currentStrategy, detectedObstacles.size(), agentStates.size()
        );
    }
}