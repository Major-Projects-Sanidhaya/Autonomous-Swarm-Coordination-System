package com.team6.swarm.ui;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.AgentStatus;  // From folder
import com.team6.swarm.core.EventBus;
import com.team6.swarm.core.Point2D;
import com.team6.swarm.core.Vector2D;
import com.team6.swarm.communication.ConnectionInfo;  // From folder
import com.team6.swarm.communication.NetworkStatus;  // New, added below
import com.team6.swarm.intelligence.DecisionStatus;  // New, added below
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.util.List;

/**
 * VISUALIZER CLASS - Real-Time Swarm Visualization
 *
 * PURPOSE: As per spec - Renders agents, links, decisions.
 * INTEGRATION: Receives updates via EventBus, handles mouse for waypoints.
 * ALIGNMENT: Uses folder's AgentState.java, ConnectionInfo.java, Point2D.java.
 */
public class Visualizer {
    private Canvas canvas;
    private GraphicsContext gc;
    private EventBus eventBus;
    private ControlPanel controlPanel;  // For waypoint handling

    // World dimensions (match PhysicsEngine.java in folder)
    private static final double WORLD_WIDTH = 800.0;
    private static final double WORLD_HEIGHT = 600.0;

    public Visualizer(EventBus eventBus) {
        this.eventBus = eventBus;
        this.canvas = new Canvas(WORLD_WIDTH, WORLD_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();

        // Handle mouse clicks for waypoints (integrates with ControlPanel.java)
        canvas.setOnMouseClicked(this::handleMouseClick);

        // Initial clear
        clearCanvas();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setControlPanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    private void handleMouseClick(MouseEvent event) {
        if (controlPanel != null) {
            Point2D position = new Point2D(event.getX(), event.getY());
            controlPanel.handleWaypointPlacement(position);
        }
    }

    public void updateDisplay(com.team6.swarm.core.VisualizationUpdate update) {
        clearCanvas();
        drawBoundaries();
        for (AgentState agent : update.allAgents) {
            drawAgent(agent);
        }
    }

    public void updateNetworkDisplay(NetworkStatus status) {
        for (ConnectionInfo connection : status.connections) {
            drawCommunicationLink(connection);
        }
    }

    public void updateDecisionDisplay(DecisionStatus decision) {
        if (decision.activeVotes > 0) {
            drawVotingIndicator(decision);
        }
    }

    private void clearCanvas() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    }

    private void drawBoundaries() {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.setLineDashes(5);
        gc.strokeRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        gc.setLineDashes(0);
    }

    private void drawAgent(AgentState agent) {
        double x = agent.position.x;
        double y = agent.position.y;
        double radius = 10 + (agent.batteryLevel * 5);

        // Color based on status (matches AgentStatus.java in folder)
        Color color = switch (agent.status) {
            case ACTIVE -> Color.GREEN;
            case FAILED -> Color.RED;
            case BATTERY_LOW -> Color.YELLOW;
            default -> Color.BLUE;
        };

        gc.setFill(color);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(agent.agentId), x - 3, y + 3);

        if (agent.velocity.magnitude() > 0) {
            Vector2D vel = agent.velocity.normalize().multiply(radius);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, x + vel.x, y + vel.y);
        }
    }

    private void drawCommunicationLink(ConnectionInfo connection) {
        // Simplified: Assume positions are fetched or stored (integrate with CommunicationManager.java)
        double x1 = 0, y1 = 0, x2 = 0, y2 = 0;  // Placeholder - enhance with agent positions
        Color color = connection.strength > 0.7 ? Color.GREEN : Color.YELLOW;
        double thickness = connection.strength * 3;
        gc.setStroke(color);
        gc.setLineWidth(thickness);
        gc.strokeLine(x1, y1, x2, y2);
    }

    private void drawVotingIndicator(DecisionStatus decision) {
        gc.setFill(Color.CYAN);
        gc.fillText("Voting Active: " + decision.activeVotes + " proposals", 10, 20);
    }
}
