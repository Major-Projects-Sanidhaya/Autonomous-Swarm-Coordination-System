package com.team6.swarm.ui;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.AgentStatus;
import com.team6.swarm.core.Point2D;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Week 1: Test basic visualization works
 * Purpose: Verify rendering system functionality
 * Author: Anthony (UI Team)
 */
public class SimpleVisTest extends Application {
    
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;
    
    private Canvas canvas;
    private GraphicsContext gc;
    private AgentRenderer agentRenderer;
    private List<AgentState> testAgents;
    
    @Override
    public void start(Stage primaryStage) {
        // Create main layout
        BorderPane root = new BorderPane();
        
        // Create canvas
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        root.setCenter(canvas);
        
        // Initialize renderer
        agentRenderer = new AgentRenderer();
        
        // Create test agents
        createTestAgents();
        
        // Initial render
        renderTestScene();
        
        // Set up scene and stage
        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        primaryStage.setTitle("Simple Visualization Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("SimpleVisTest: Window created successfully");
        System.out.println("SimpleVisTest: Displaying " + testAgents.size() + " test agents");
    }
    
    /**
     * Create test agents with various states
     */
    private void createTestAgents() {
        testAgents = new ArrayList<>();
        
        // Agent 1: Active agent in top-left
        AgentState agent1 = createTestAgent("Agent-1", 100, 100, 
                                           AgentStatus.ACTIVE, 85.0, "LEADER", 45);
        testAgents.add(agent1);
        
        // Agent 2: Battery low agent in top-right
        AgentState agent2 = createTestAgent("Agent-2", 700, 100, 
                                           AgentStatus.BATTERY_LOW, 25.0, "SCOUT", 90);
        testAgents.add(agent2);
        
        // Agent 3: Failed agent in center
        AgentState agent3 = createTestAgent("Agent-3", 400, 300, 
                                           AgentStatus.FAILED, 0.0, "GUARD", 180);
        testAgents.add(agent3);
        
        // Agent 4: Inactive agent in bottom-left
        AgentState agent4 = createTestAgent("Agent-4", 100, 500, 
                                           AgentStatus.INACTIVE, 100.0, "", 270);
        testAgents.add(agent4);
        
        // Agent 5: Active agent in bottom-right
        AgentState agent5 = createTestAgent("Agent-5", 700, 500, 
                                           AgentStatus.ACTIVE, 60.0, "", 315);
        testAgents.add(agent5);
    }
    
    /**
     * Create a test agent with specified parameters
     */
    private AgentState createTestAgent(String id, double x, double y, 
                                       AgentStatus status, double battery, 
                                       String role, double heading) {
        AgentState agent = new AgentState();
        agent.agentId = Integer.parseInt(id.split("-")[1]); // Parse ID from "Agent-1" format
        agent.agentName = id;
        agent.position = new Point2D(x, y);
        agent.status = status;
        agent.batteryLevel = battery / 100.0; // Convert percentage to 0.0-1.0
        agent.heading = Math.toRadians(heading);
        return agent;
    }
    
    /**
     * Render the test scene
     */
    private void renderTestScene() {
        // Clear canvas with white background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Draw grid for reference
        drawGrid();
        
        // Draw title
        drawTitle();
        
        // Draw all test agents
        for (AgentState agent : testAgents) {
            agentRenderer.drawAgent(gc, agent);
        }
        
        // Draw legend
        drawLegend();
        
        System.out.println("SimpleVisTest: Frame rendered");
    }
    
    /**
     * Draw background grid
     */
    private void drawGrid() {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        
        // Vertical lines
        for (int x = 0; x < CANVAS_WIDTH; x += 50) {
            gc.strokeLine(x, 0, x, CANVAS_HEIGHT);
        }
        
        // Horizontal lines
        for (int y = 0; y < CANVAS_HEIGHT; y += 50) {
            gc.strokeLine(0, y, CANVAS_WIDTH, y);
        }
    }
    
    /**
     * Draw title
     */
    private void drawTitle() {
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 20));
        gc.fillText("Simple Visualization Test - Static Agents", 10, 30);
    }
    
    /**
     * Draw legend explaining agent colors
     */
    private void drawLegend() {
        int legendX = 10;
        int legendY = CANVAS_HEIGHT - 120;
        int legendWidth = 200;
        int legendHeight = 110;
        
        // Background
        gc.setFill(new Color(1, 1, 1, 0.9));
        gc.fillRect(legendX, legendY, legendWidth, legendHeight);
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(legendX, legendY, legendWidth, legendHeight);
        
        // Title
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 12));
        gc.fillText("Legend:", legendX + 10, legendY + 20);
        
        // Status colors
        int yOffset = legendY + 40;
        drawLegendItem("ACTIVE", Color.GREEN, legendX + 10, yOffset);
        drawLegendItem("BATTERY_LOW", Color.YELLOW, legendX + 10, yOffset + 20);
        drawLegendItem("FAILED", Color.RED, legendX + 10, yOffset + 40);
        drawLegendItem("INACTIVE", Color.GRAY, legendX + 10, yOffset + 60);
    }
    
    /**
     * Draw a single legend item
     */
    private void drawLegendItem(String label, Color color, int x, int y) {
        // Color box
        gc.setFill(color);
        gc.fillRect(x, y - 8, 12, 12);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x, y - 8, 12, 12);
        
        // Label
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 10));
        gc.fillText(label, x + 20, y + 2);
    }
    
    /**
     * Main entry point for testing
     */
    public static void main(String[] args) {
        System.out.println("Starting Simple Visualization Test...");
        launch(args);
    }
}
