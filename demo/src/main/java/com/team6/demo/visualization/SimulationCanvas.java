package com.team6.demo.visualization;

import com.team6.demo.core.Environment;
import com.team6.demo.core.Position;
import com.team6.demo.obstacles.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

/**
 * SimulationCanvas - 2D top-down visualization of drone simulation
 *
 * Draws:
 * - Background grid
 * - Obstacles (buildings, no-fly zones, moving obstacles)
 * - Drones with trails
 * - Mission targets
 */
public class SimulationCanvas extends Canvas {
    private static final Color BACKGROUND = Color.web("#F5F5F5");
    private static final Color GRID_LINE = Color.web("#DDDDDD");
    private static final Color BUILDING = Color.web("#555555");
    private static final Color NO_FLY_ZONE = Color.web("#FF000050");
    private static final Color DRONE = Color.web("#2196F3");
    private static final Color DRONE_TRAIL = Color.web("#2196F380");

    private double worldWidth;
    private double worldHeight;
    private double scale; // pixels per meter

    // Drone trails: droneId -> list of recent positions
    private Map<Integer, LinkedList<Position>> droneTrails;
    private static final int MAX_TRAIL_LENGTH = 20;

    public SimulationCanvas(double width, double height) {
        super(width, height);
        this.droneTrails = new HashMap<>();
    }

    /**
     * Set world dimensions for scaling
     */
    public void setWorldSize(double worldWidth, double worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        // Calculate scale to fit world in canvas
        double scaleX = getWidth() / worldWidth;
        double scaleY = getHeight() / worldHeight;
        this.scale = Math.min(scaleX, scaleY) * 0.95; // 95% to leave margin
    }

    /**
     * Render the current environment state
     */
    public void render(Environment environment) {
        if (environment == null) return;

        GraphicsContext gc = getGraphicsContext2D();

        // Clear canvas
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // Draw grid
        drawGrid(gc);

        // Draw obstacles
        drawObstacles(gc, environment.getObstacleManager());

        // Draw drones with trails
        drawDrones(gc, environment);
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(GRID_LINE);
        gc.setLineWidth(1);

        double gridSpacing = 50.0; // meters

        // Vertical lines
        for (double x = 0; x <= worldWidth; x += gridSpacing) {
            double screenX = toScreenX(x);
            gc.strokeLine(screenX, 0, screenX, getHeight());
        }

        // Horizontal lines
        for (double y = 0; y <= worldHeight; y += gridSpacing) {
            double screenY = toScreenY(y);
            gc.strokeLine(0, screenY, getWidth(), screenY);
        }
    }

    private void drawObstacles(GraphicsContext gc, ObstacleManager obstacleManager) {
        if (obstacleManager == null) return;

        for (Obstacle obstacle : obstacleManager.getAllObstacles()) {
            if (obstacle instanceof BuildingObstacle) {
                drawBuilding(gc, (BuildingObstacle) obstacle);
            } else if (obstacle instanceof NoFlyZone) {
                drawNoFlyZone(gc, (NoFlyZone) obstacle);
            } else if (obstacle instanceof MovingObstacle) {
                drawMovingObstacle(gc, (MovingObstacle) obstacle);
            } else if (obstacle instanceof ExpandingObstacle) {
                drawExpandingObstacle(gc, (ExpandingObstacle) obstacle);
            }
        }
    }

    private void drawBuilding(GraphicsContext gc, BuildingObstacle building) {
        double x1 = toScreenX(building.getXMin());
        double y1 = toScreenY(building.getYMax()); // Flip Y
        double width = (building.getXMax() - building.getXMin()) * scale;
        double height = (building.getYMax() - building.getYMin()) * scale;

        gc.setFill(BUILDING);
        gc.fillRect(x1, y1, width, height);

        // Label
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(building.getName(), x1 + width/2, y1 + height/2);
    }

    private void drawNoFlyZone(GraphicsContext gc, NoFlyZone zone) {
        double centerX = toScreenX(zone.getPosition().x);
        double centerY = toScreenY(zone.getPosition().y);
        double radius = zone.getRadius() * scale;

        gc.setFill(NO_FLY_ZONE);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    private void drawMovingObstacle(GraphicsContext gc, MovingObstacle obstacle) {
        double centerX = toScreenX(obstacle.getPosition().x);
        double centerY = toScreenY(obstacle.getPosition().y);
        double radius = obstacle.getRadius() * scale;

        gc.setFill(Color.ORANGE);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        gc.setStroke(Color.DARKORANGE);
        gc.setLineWidth(1);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    private void drawExpandingObstacle(GraphicsContext gc, ExpandingObstacle obstacle) {
        double centerX = toScreenX(obstacle.getPosition().x);
        double centerY = toScreenY(obstacle.getPosition().y);
        double radius = obstacle.getRadius() * scale;

        gc.setFill(Color.web("#FF6666", 0.4));
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Label
        gc.setFill(Color.DARKRED);
        gc.setFont(Font.font(12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.0fm", obstacle.getRadius()), centerX, centerY);
    }

    private void drawDrones(GraphicsContext gc, Environment environment) {
        Map<Integer, Position> drones = environment.getAllDronePositions();

        for (Map.Entry<Integer, Position> entry : drones.entrySet()) {
            int droneId = entry.getKey();
            Position pos = entry.getValue();

            // Update trail
            droneTrails.putIfAbsent(droneId, new LinkedList<>());
            LinkedList<Position> trail = droneTrails.get(droneId);
            trail.addFirst(pos);
            if (trail.size() > MAX_TRAIL_LENGTH) {
                trail.removeLast();
            }

            // Draw trail
            drawDroneTrail(gc, trail);

            // Draw drone
            double screenX = toScreenX(pos.x);
            double screenY = toScreenY(pos.y);
            double size = 15; // pixels

            // Circle
            gc.setFill(DRONE);
            gc.fillOval(screenX - size/2, screenY - size/2, size, size);

            // Border
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeOval(screenX - size/2, screenY - size/2, size, size);

            // Label
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("D" + droneId, screenX, screenY - size/2 - 5);
        }
    }

    private void drawDroneTrail(GraphicsContext gc, LinkedList<Position> trail) {
        if (trail.size() < 2) return;

        gc.setStroke(DRONE_TRAIL);
        gc.setLineWidth(2);

        Position prev = trail.getFirst();
        for (int i = 1; i < trail.size(); i++) {
            Position curr = trail.get(i);
            double x1 = toScreenX(prev.x);
            double y1 = toScreenY(prev.y);
            double x2 = toScreenX(curr.x);
            double y2 = toScreenY(curr.y);

            gc.strokeLine(x1, y1, x2, y2);
            prev = curr;
        }
    }

    /**
     * Clear all drone trails
     */
    public void clearTrails() {
        droneTrails.clear();
    }

    // Coordinate conversion: world coordinates to screen pixels
    private double toScreenX(double worldX) {
        return worldX * scale;
    }

    private double toScreenY(double worldY) {
        // Flip Y axis (JavaFX Y=0 is top, world Y=0 is bottom)
        return getHeight() - (worldY * scale);
    }
}
