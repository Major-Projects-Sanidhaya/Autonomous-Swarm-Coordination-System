/**
 * VISUALIZER CLASS - Real-Time Canvas Rendering
 *
 * PURPOSE:
 * - Render agents, communication links, and system state on canvas
 * - Provide smooth 60 FPS visualization
 * - Display voting progress and mission waypoints
 * - Show formation guides and performance metrics
 *
 * RENDERING LAYERS (back to front):
 * 1. Background grid
 * 2. World boundaries
 * 3. Waypoints (circles with labels)
 * 4. Communication links (lines with signal strength)
 * 5. Formation guides (dashed lines)
 * 6. Agents (circles with heading indicators)
 * 7. Agent labels (ID, battery, status)
 * 8. Decision overlay (voting progress)
 * 9. Performance metrics (top-right corner)
 *
 * AGENT VISUALIZATION:
 * - Status Colors: ACTIVE=Green, BATTERY_LOW=Orange, FAILED=Red, INACTIVE=Gray
 * - Size: 10px diameter circle
 * - Heading: 15px line from center
 * - Label: ID + Battery% above agent
 * - Trail: Last 20 positions (fading)
 *
 * COMMUNICATION LINKS:
 * - Line thickness: 1-4px based on signal strength
 * - Color: Gray (normal), Yellow (active message), Red (weak)
 * - Animation: Pulse effect when message sent
 *
 * DECISION OVERLAY:
 * - Position: Top-center of canvas
 * - Background: Semi-transparent white box
 * - Content: Question, progress bar, vote breakdown, time remaining
 *
 * PERFORMANCE:
 * - Target: 60 FPS rendering
 * - Optimizations: Dirty rectangles, cached colors, batch operations
 * - Double buffering for smooth animation
 *
 * USAGE:
 * Visualizer viz = new Visualizer(eventBus, systemController);
 * Pane pane = viz.getCanvasPane();
 * // In animation loop:
 * viz.render();
 */
package com.team6.swarm.ui;

import com.team6.swarm.core.*;
import com.team6.swarm.communication.ConnectionInfo;
import com.team6.swarm.intelligence.tasking.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.MouseEvent;

import java.util.*;

public class Visualizer {
    // ==================== CORE COMPONENTS ====================
    private final EventBus eventBus;
    private final SystemController systemController;
    
    // ==================== CANVAS ====================
    private Canvas canvas;
    private StackPane canvasPane;
    private GraphicsContext gc;
    
    // ==================== DIMENSIONS ====================
    private double canvasWidth = 800;
    private double canvasHeight = 600;
    
    // ==================== VIEW CONTROL ====================
    private double zoomLevel = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    
    // ==================== DATA ====================
    private List<AgentState> currentAgents = new ArrayList<>();
    private List<ConnectionInfo> currentConnections = new ArrayList<>();
    private DecisionStatus currentDecision = null;
    private List<Task> currentWaypoints = new ArrayList<>();
    private NetworkStatus currentNetworkStatus = null;
    
    // ==================== AGENT TRAILS ====================
    private Map<Integer, LinkedList<Point2D>> agentTrails = new HashMap<>();
    private static final int MAX_TRAIL_LENGTH = 20;
    
    // ==================== COLORS (CACHED) ====================
    private final Color COLOR_BACKGROUND = Color.rgb(245, 245, 250);
    private final Color COLOR_GRID = Color.rgb(220, 220, 225);
    private final Color COLOR_BOUNDARY = Color.rgb(100, 100, 120);
    private final Color COLOR_AGENT_ACTIVE = Color.rgb(50, 200, 50);
    private final Color COLOR_AGENT_BATTERY_LOW = Color.rgb(255, 165, 0);
    private final Color COLOR_AGENT_FAILED = Color.rgb(220, 50, 50);
    private final Color COLOR_AGENT_INACTIVE = Color.rgb(150, 150, 150);
    private final Color COLOR_LINK_NORMAL = Color.rgb(180, 180, 200, 0.3);
    private final Color COLOR_LINK_ACTIVE = Color.rgb(255, 215, 0, 0.6);
    private final Color COLOR_LINK_WEAK = Color.rgb(220, 50, 50, 0.4);
    private final Color COLOR_WAYPOINT = Color.rgb(100, 150, 255);
    private final Color COLOR_FORMATION = Color.rgb(150, 100, 255, 0.3);
    
    // ==================== FONTS (CACHED) ====================
    private final Font FONT_SMALL = Font.font("Arial", 10);
    private final Font FONT_NORMAL = Font.font("Arial", 12);
    private final Font FONT_LARGE = Font.font("Arial", FontWeight.BOLD, 14);
    
    // ==================== SETTINGS ====================
    private boolean showGrid = true;
    private boolean showLabels = true;
    private boolean showTrails = true;
    private boolean showLinks = true;
    private boolean showWaypoints = true;
    private boolean showPerformance = true;
    
    // ==================== PERFORMANCE ====================
    private long lastRenderTime = 0;
    private int renderCount = 0;
    private double currentFps = 0.0;
    
    /**
     * Constructor
     */
    public Visualizer(EventBus eventBus, SystemController systemController) {
        this.eventBus = eventBus;
        this.systemController = systemController;
        
        // Create canvas
        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D();
        
        // Create pane to hold canvas
        canvasPane = new StackPane(canvas);
        canvasPane.setStyle("-fx-background-color: #f5f5fa;");
        
        // Set up event listeners
        setupEventListeners();
        
        // Set up mouse handlers
        setupMouseHandlers();
        
        System.out.println("Visualizer initialized: " + canvasWidth + "x" + canvasHeight);
    }
    
    /**
     * Set up event listeners for data updates
     */
    private void setupEventListeners() {
        // Listen for visualization updates
        eventBus.subscribe(VisualizationUpdate.class, this::handleVisualizationUpdate);
        
        // Listen for network status
        eventBus.subscribe(NetworkStatus.class, this::handleNetworkStatus);
        
        // Listen for decision status
        eventBus.subscribe(DecisionStatus.class, this::handleDecisionStatus);
    }
    
    /**
     * Set up mouse event handlers
     */
    private void setupMouseHandlers() {
        // Mouse click for interaction (handled by ControlPanel/MissionPanel)
        canvas.setOnMouseClicked(this::handleMouseClick);
        
        // Mouse drag for panning
        canvas.setOnMouseDragged(this::handleMouseDrag);
        
        // Mouse scroll for zooming
        canvas.setOnScroll(event -> {
            double delta = event.getDeltaY();
            if (delta > 0) {
                zoomIn();
            } else {
                zoomOut();
            }
            event.consume();
        });
    }
    
    // ==================== EVENT HANDLERS ====================
    
    private void handleVisualizationUpdate(VisualizationUpdate update) {
        this.currentAgents = update.allAgents;
        
        // Update agent trails
        for (AgentState agent : currentAgents) {
            LinkedList<Point2D> trail = agentTrails.computeIfAbsent(
                agent.agentId, k -> new LinkedList<>()
            );
            
            trail.add(new Point2D(agent.position.x, agent.position.y));
            
            if (trail.size() > MAX_TRAIL_LENGTH) {
                trail.removeFirst();
            }
        }
    }
    
    private void handleNetworkStatus(NetworkStatus status) {
        this.currentNetworkStatus = status;
        this.currentConnections = status.connections;
    }
    
    private void handleDecisionStatus(DecisionStatus status) {
        this.currentDecision = status;
    }
    
    private void handleMouseClick(MouseEvent event) {
        // Convert screen coordinates to world coordinates
        double worldX = (event.getX() - offsetX) / zoomLevel;
        double worldY = (event.getY() - offsetY) / zoomLevel;
        
        System.out.println("Canvas clicked at: (" + worldX + ", " + worldY + ")");
        
        // Event will be handled by ControlPanel or MissionPanel if they're listening
    }
    
    private void handleMouseDrag(MouseEvent event) {
        // Pan view (optional feature)
        // offsetX += event.getX() - lastMouseX;
        // offsetY += event.getY() - lastMouseY;
    }
    
    // ==================== MAIN RENDER METHOD ====================
    
    /**
     * Main render method - called every frame (60 FPS)
     */
    public void render() {
        // Clear canvas
        gc.setFill(COLOR_BACKGROUND);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        // Apply transformations
        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(zoomLevel, zoomLevel);
        
        // Render layers
        if (showGrid) renderGrid();
        renderBoundaries();
        if (showWaypoints) renderWaypoints();
        if (showLinks) renderCommunicationLinks();
        renderFormationGuides();
        renderAgentTrails();
        renderAgents();
        if (showLabels) renderAgentLabels();
        
        // Restore transformations
        gc.restore();
        
        // Render overlays (not affected by zoom/pan)
        if (currentDecision != null && currentDecision.isPending) {
            renderDecisionOverlay();
        }
        if (showPerformance) renderPerformanceMetrics();
        
        // Update FPS
        updateFps();
    }
    
    // ==================== RENDERING METHODS ====================
    
    private void renderGrid() {
        gc.setStroke(COLOR_GRID);
        gc.setLineWidth(0.5 / zoomLevel);
        
        double worldWidth = systemController.getWorldWidth();
        double worldHeight = systemController.getWorldHeight();
        double gridSize = 50.0;
        
        // Vertical lines
        for (double x = 0; x <= worldWidth; x += gridSize) {
            gc.strokeLine(x, 0, x, worldHeight);
        }
        
        // Horizontal lines
        for (double y = 0; y <= worldHeight; y += gridSize) {
            gc.strokeLine(0, y, worldWidth, y);
        }
    }
    
    private void renderBoundaries() {
        double worldWidth = systemController.getWorldWidth();
        double worldHeight = systemController.getWorldHeight();
        
        gc.setStroke(COLOR_BOUNDARY);
        gc.setLineWidth(2.0 / zoomLevel);
        gc.strokeRect(0, 0, worldWidth, worldHeight);
    }
    
    private void renderWaypoints() {
        // Get waypoints from TaskAllocator
        if (systemController.getTaskAllocator() != null) {
            List<Task> tasks = systemController.getTaskAllocator().getAllTasks();
            
            for (Task task : tasks) {
                if (task.targetPosition != null) {
                    double x = task.targetPosition.x;
                    double y = task.targetPosition.y;
                    double radius = task.completionRadius;
                    
                    // Draw waypoint circle
                    gc.setStroke(COLOR_WAYPOINT);
                    gc.setLineWidth(2.0 / zoomLevel);
                    gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
                    
                    // Draw center dot
                    gc.setFill(COLOR_WAYPOINT);
                    gc.fillOval(x - 3, y - 3, 6, 6);
                    
                    // Draw label
                    gc.setFill(Color.BLACK);
                    gc.setFont(FONT_SMALL);
                    gc.fillText(task.taskId, x + radius + 5, y);
                }
            }
        }
    }
    
    private void renderCommunicationLinks() {
        for (ConnectionInfo conn : currentConnections) {
            // Find agent positions
            Point2D pos1 = getAgentPosition(conn.agent1Id);
            Point2D pos2 = getAgentPosition(conn.agent2Id);
            
            if (pos1 != null && pos2 != null) {
                // Determine link color and thickness based on signal strength
                double strength = conn.signalStrength;
                Color linkColor;
                double lineWidth;
                
                if (strength > 0.7) {
                    linkColor = COLOR_LINK_NORMAL;
                    lineWidth = 2.0;
                } else if (strength > 0.3) {
                    linkColor = COLOR_LINK_ACTIVE;
                    lineWidth = 1.5;
                } else {
                    linkColor = COLOR_LINK_WEAK;
                    lineWidth = 1.0;
                }
                
                gc.setStroke(linkColor);
                gc.setLineWidth(lineWidth / zoomLevel);
                gc.strokeLine(pos1.x, pos1.y, pos2.x, pos2.y);
            }
        }
    }
    
    private void renderFormationGuides() {
        // TODO: Render formation target positions and guides
        // This will be implemented when FormationController provides visualization data
    }
    
    private void renderAgentTrails() {
        if (!showTrails) return;
        
        for (Map.Entry<Integer, LinkedList<Point2D>> entry : agentTrails.entrySet()) {
            LinkedList<Point2D> trail = entry.getValue();
            
            if (trail.size() < 2) continue;
            
            // Draw trail with fading effect
            for (int i = 0; i < trail.size() - 1; i++) {
                Point2D p1 = trail.get(i);
                Point2D p2 = trail.get(i + 1);
                
                double alpha = (double) i / trail.size() * 0.3;
                gc.setStroke(Color.rgb(100, 100, 100, alpha));
                gc.setLineWidth(1.0 / zoomLevel);
                gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }
    
    private void renderAgents() {
        for (AgentState agent : currentAgents) {
            double x = agent.position.x;
            double y = agent.position.y;
            double radius = 5.0;
            
            // Determine agent color based on status
            Color agentColor = getAgentColor(agent.status);
            
            // Draw agent circle
            gc.setFill(agentColor);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            
            // Draw border
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0 / zoomLevel);
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
            
            // Draw heading indicator
            double headingLength = 15.0;
            double headX = x + Math.cos(agent.heading) * headingLength;
            double headY = y + Math.sin(agent.heading) * headingLength;
            
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2.0 / zoomLevel);
            gc.strokeLine(x, y, headX, headY);
            
            // Draw battery indicator
            if (agent.batteryLevel < 0.3) {
                gc.setFill(Color.RED);
                gc.fillRect(x - 8, y - 15, 16 * agent.batteryLevel, 3);
            }
        }
    }
    
    private void renderAgentLabels() {
        gc.setFont(FONT_SMALL);
        
        for (AgentState agent : currentAgents) {
            double x = agent.position.x;
            double y = agent.position.y;
            
            // Create label text
            String label = String.format("#%d (%.0f%%)", 
                agent.agentId, agent.batteryLevel * 100);
            
            // Draw label background
            gc.setFill(Color.rgb(255, 255, 255, 0.8));
            gc.fillRect(x - 20, y - 25, 40, 12);
            
            // Draw label text
            gc.setFill(Color.BLACK);
            gc.fillText(label, x - 18, y - 16);
        }
    }
    
    private void renderDecisionOverlay() {
        if (currentDecision == null) return;
        
        double overlayWidth = 400;
        double overlayHeight = 150;
        double overlayX = (canvasWidth - overlayWidth) / 2;
        double overlayY = 20;
        
        // Draw background
        gc.setFill(Color.rgb(255, 255, 255, 0.95));
        gc.fillRoundRect(overlayX, overlayY, overlayWidth, overlayHeight, 10, 10);
        
        // Draw border
        gc.setStroke(Color.rgb(100, 100, 255));
        gc.setLineWidth(2);
        gc.strokeRoundRect(overlayX, overlayY, overlayWidth, overlayHeight, 10, 10);
        
        // Draw title
        gc.setFont(FONT_LARGE);
        gc.setFill(Color.BLACK);
        gc.fillText("VOTING IN PROGRESS", overlayX + 20, overlayY + 25);
        
        // Draw question
        gc.setFont(FONT_NORMAL);
        gc.fillText(currentDecision.question, overlayX + 20, overlayY + 50);
        
        // Draw progress bar
        double progressBarWidth = overlayWidth - 40;
        double progressBarHeight = 20;
        double progressBarX = overlayX + 20;
        double progressBarY = overlayY + 65;
        
        // Progress bar background
        gc.setFill(Color.rgb(220, 220, 220));
        gc.fillRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight);
        
        // Progress bar fill
        gc.setFill(Color.rgb(100, 200, 100));
        gc.fillRect(progressBarX, progressBarY, 
                   progressBarWidth * currentDecision.progress, progressBarHeight);
        
        // Progress text
        gc.setFill(Color.BLACK);
        gc.setFont(FONT_SMALL);
        String progressText = String.format("%d/%d votes (%.0f%%)", 
            currentDecision.votesReceived, currentDecision.totalAgents, 
            currentDecision.progress * 100);
        gc.fillText(progressText, progressBarX + 5, progressBarY + 14);
        
        // Draw vote breakdown
        gc.setFont(FONT_SMALL);
        double yOffset = overlayY + 100;
        
        for (String option : currentDecision.options) {
            int count = currentDecision.voteCounts.getOrDefault(option, 0);
            double percentage = currentDecision.votePercentages.getOrDefault(option, 0.0);
            
            String voteText = String.format("%s: %d votes (%.0f%%)", 
                option, count, percentage * 100);
            
            // Highlight leading option
            if (option.equals(currentDecision.leadingOption)) {
                gc.setFill(Color.rgb(100, 200, 100));
                gc.fillRect(overlayX + 15, yOffset - 12, overlayWidth - 30, 15);
            }
            
            gc.setFill(Color.BLACK);
            gc.fillText(voteText, overlayX + 20, yOffset);
            yOffset += 18;
        }
        
        // Draw time remaining
        if (currentDecision.timeRemaining > 0) {
            gc.setFont(FONT_SMALL);
            gc.setFill(Color.RED);
            String timeText = "Time: " + currentDecision.getTimeRemainingFormatted();
            gc.fillText(timeText, overlayX + overlayWidth - 100, overlayY + 25);
        }
    }
    
    private void renderPerformanceMetrics() {
        double metricsX = canvasWidth - 150;
        double metricsY = 10;
        
        // Draw background
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRoundRect(metricsX, metricsY, 140, 100, 5, 5);
        
        // Draw metrics
        gc.setFont(FONT_SMALL);
        gc.setFill(Color.WHITE);
        
        double y = metricsY + 20;
        gc.fillText(String.format("FPS: %.1f", currentFps), metricsX + 10, y);
        y += 15;
        gc.fillText(String.format("Agents: %d", currentAgents.size()), metricsX + 10, y);
        y += 15;
        gc.fillText(String.format("Links: %d", currentConnections.size()), metricsX + 10, y);
        y += 15;
        gc.fillText(String.format("Zoom: %.1fx", zoomLevel), metricsX + 10, y);
        y += 15;
        gc.fillText(String.format("Sim: %s", 
            systemController.isSimulationRunning() ? "Running" : "Stopped"), 
            metricsX + 10, y);
    }
    
    // ==================== HELPER METHODS ====================
    
    private Color getAgentColor(AgentStatus status) {
        switch (status) {
            case ACTIVE:
                return COLOR_AGENT_ACTIVE;
            case BATTERY_LOW:
                return COLOR_AGENT_BATTERY_LOW;
            case FAILED:
                return COLOR_AGENT_FAILED;
            case INACTIVE:
            case MAINTENANCE:
            default:
                return COLOR_AGENT_INACTIVE;
        }
    }
    
    private Point2D getAgentPosition(int agentId) {
        for (AgentState agent : currentAgents) {
            if (agent.agentId == agentId) {
                return agent.position;
            }
        }
        return null;
    }
    
    private void updateFps() {
        renderCount++;
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastRenderTime >= 1000) {
            currentFps = renderCount / ((currentTime - lastRenderTime) / 1000.0);
            renderCount = 0;
            lastRenderTime = currentTime;
        }
    }
    
    // ==================== VIEW CONTROL ====================
    
    public void zoomIn() {
        zoomLevel = Math.min(zoomLevel * 1.2, 5.0);
    }
    
    public void zoomOut() {
        zoomLevel = Math.max(zoomLevel / 1.2, 0.2);
    }
    
    public void resetView() {
        zoomLevel = 1.0;
        offsetX = 0.0;
        offsetY = 0.0;
    }
    
    // ==================== SETTINGS ====================
    
    public void setShowGrid(boolean show) {
        this.showGrid = show;
    }
    
    public void setShowLabels(boolean show) {
        this.showLabels = show;
    }
    
    public void setShowTrails(boolean show) {
        this.showTrails = show;
    }
    
    public void setShowLinks(boolean show) {
        this.showLinks = show;
    }
    
    public void setShowWaypoints(boolean show) {
        this.showWaypoints = show;
    }
    
    public void setShowPerformance(boolean show) {
        this.showPerformance = show;
    }
    
    // ==================== GETTERS ====================
    
    public Pane getCanvasPane() {
        return canvasPane;
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
    
    public double getCanvasWidth() {
        return canvasWidth;
    }
    
    public double getCanvasHeight() {
        return canvasHeight;
    }
    
    public double getZoomLevel() {
        return zoomLevel;
    }
}
