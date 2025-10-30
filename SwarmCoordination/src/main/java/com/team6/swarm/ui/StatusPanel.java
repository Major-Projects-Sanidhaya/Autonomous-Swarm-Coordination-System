/**
 * STATUSPANEL CLASS - System Information Display
 *
 * PURPOSE:
 * - Display real-time system metrics and status
 * - Show agent statistics and health
 * - Display network topology information
 * - Show voting/decision progress
 * - Display performance metrics
 * - Show mission progress
 *
 * SECTIONS:
 * 1. System Status: Agent counts, active/failed/battery low
 * 2. Network Health: Connections, density, isolated agents
 * 3. Decision Status: Active votes, progress, consensus
 * 4. Performance: FPS, update rate, latency, CPU, memory
 * 5. Mission Progress: Waypoints, tasks, formation, time
 *
 * UPDATE FREQUENCY:
 * - Agent stats: Every VisualizationUpdate (30 Hz)
 * - Network health: Every NetworkStatus (10 Hz)
 * - Decision status: Every DecisionStatus update
 * - Performance: Every second
 * - Mission progress: Every TaskCompletionReport
 *
 * FEATURES:
 * - Color coding (green=good, yellow=warning, red=critical)
 * - Progress bars for percentages
 * - Auto-scroll for decision history
 * - Expandable sections
 * - Real-time updates
 *
 * USAGE:
 * StatusPanel panel = new StatusPanel(eventBus, systemController);
 * VBox vbox = panel.getPanel();
 */
package com.team6.swarm.ui;

import com.team6.swarm.core.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

public class StatusPanel {
    // ==================== CORE COMPONENTS ====================
    private final EventBus eventBus;
    private final SystemController systemController;
    
    // ==================== UI COMPONENTS ====================
    private VBox panel;
    
    // System Status Labels
    private Label totalAgentsLabel;
    private Label activeAgentsLabel;
    private Label failedAgentsLabel;
    private Label batteryLowLabel;
    private ProgressBar systemHealthBar;
    
    // Network Health Labels
    private Label connectionsLabel;
    private Label avgNeighborsLabel;
    private Label networkDensityLabel;
    private Label isolatedAgentsLabel;
    private Label partitionsLabel;
    private ProgressBar networkHealthBar;
    
    // Decision Status Labels
    private Label activeVotesLabel;
    private Label currentQuestionLabel;
    private Label voteProgressLabel;
    private Label leadingOptionLabel;
    private Label timeRemainingLabel;
    private ProgressBar voteProgressBar;
    
    // Performance Labels
    private Label fpsLabel;
    private Label updateRateLabel;
    private Label latencyLabel;
    private Label cpuLabel;
    private Label memoryLabel;
    
    // Mission Progress Labels
    private Label waypointsLabel;
    private Label tasksCompleteLabel;
    private Label currentFormationLabel;
    private Label missionTimeLabel;
    private ProgressBar missionProgressBar;
    
    // ==================== DATA ====================
    private int totalAgents = 0;
    private int activeAgents = 0;
    private int failedAgents = 0;
    private int batteryLowAgents = 0;
    private int totalConnections = 0;
    private double avgNeighbors = 0.0;
    private double networkDensity = 0.0;
    private int isolatedAgents = 0;
    private int partitions = 1;
    private long missionStartTime = 0;
    
    /**
     * Constructor
     */
    public StatusPanel(EventBus eventBus, SystemController systemController) {
        this.eventBus = eventBus;
        this.systemController = systemController;
        
        // Create UI
        createPanel();
        
        // Set up event listeners
        setupEventListeners();
        
        // Start update timer
        startUpdateTimer();
        
        System.out.println("StatusPanel initialized");
    }
    
    /**
     * Create main panel with all status sections
     */
    private void createPanel() {
        panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f0f0f8; -fx-border-color: #c0c0d0; -fx-border-width: 1;");
        panel.setPrefWidth(280);
        
        // Create sections
        TitledPane systemPane = new TitledPane("System Status", createSystemStatus());
        systemPane.setExpanded(true);
        
        TitledPane networkPane = new TitledPane("Network Health", createNetworkHealth());
        networkPane.setExpanded(true);
        
        TitledPane decisionPane = new TitledPane("Decision Status", createDecisionStatus());
        decisionPane.setExpanded(false);
        
        TitledPane performancePane = new TitledPane("Performance", createPerformance());
        performancePane.setExpanded(false);
        
        TitledPane missionPane = new TitledPane("Mission Progress", createMissionProgress());
        missionPane.setExpanded(false);
        
        // Add all sections
        panel.getChildren().addAll(
            systemPane,
            networkPane,
            decisionPane,
            performancePane,
            missionPane
        );
    }
    
    // ==================== SECTION CREATORS ====================
    
    private VBox createSystemStatus() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        
        totalAgentsLabel = createStatusLabel("Total Agents: 0");
        activeAgentsLabel = createStatusLabel("Active: 0", Color.GREEN);
        failedAgentsLabel = createStatusLabel("Failed: 0", Color.RED);
        batteryLowLabel = createStatusLabel("Battery Low: 0", Color.ORANGE);
        
        Label healthLabel = new Label("System Health:");
        healthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        systemHealthBar = new ProgressBar(1.0);
        systemHealthBar.setPrefWidth(240);
        systemHealthBar.setStyle("-fx-accent: green;");
        
        box.getChildren().addAll(
            totalAgentsLabel,
            activeAgentsLabel,
            failedAgentsLabel,
            batteryLowLabel,
            new Separator(),
            healthLabel,
            systemHealthBar
        );
        
        return box;
    }
    
    private VBox createNetworkHealth() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        
        connectionsLabel = createStatusLabel("Connections: 0");
        avgNeighborsLabel = createStatusLabel("Avg Neighbors: 0.0");
        networkDensityLabel = createStatusLabel("Density: 0%");
        isolatedAgentsLabel = createStatusLabel("Isolated: 0", Color.ORANGE);
        partitionsLabel = createStatusLabel("Partitions: 1");
        
        Label healthLabel = new Label("Network Health:");
        healthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        networkHealthBar = new ProgressBar(1.0);
        networkHealthBar.setPrefWidth(240);
        networkHealthBar.setStyle("-fx-accent: green;");
        
        box.getChildren().addAll(
            connectionsLabel,
            avgNeighborsLabel,
            networkDensityLabel,
            isolatedAgentsLabel,
            partitionsLabel,
            new Separator(),
            healthLabel,
            networkHealthBar
        );
        
        return box;
    }
    
    private VBox createDecisionStatus() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        
        activeVotesLabel = createStatusLabel("Active Votes: 0");
        currentQuestionLabel = createStatusLabel("No active votes");
        currentQuestionLabel.setWrapText(true);
        currentQuestionLabel.setPrefWidth(240);
        
        voteProgressLabel = createStatusLabel("Progress: 0/0 (0%)");
        leadingOptionLabel = createStatusLabel("Leading: None");
        timeRemainingLabel = createStatusLabel("Time: --");
        
        Label progressLabel = new Label("Vote Progress:");
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        voteProgressBar = new ProgressBar(0.0);
        voteProgressBar.setPrefWidth(240);
        voteProgressBar.setStyle("-fx-accent: blue;");
        
        box.getChildren().addAll(
            activeVotesLabel,
            new Separator(),
            currentQuestionLabel,
            voteProgressLabel,
            leadingOptionLabel,
            timeRemainingLabel,
            new Separator(),
            progressLabel,
            voteProgressBar
        );
        
        return box;
    }
    
    private VBox createPerformance() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        
        fpsLabel = createStatusLabel("FPS: 0.0");
        updateRateLabel = createStatusLabel("Update Rate: 0 Hz");
        latencyLabel = createStatusLabel("Latency: 0 ms");
        cpuLabel = createStatusLabel("CPU: 0%");
        memoryLabel = createStatusLabel("Memory: 0 MB");
        
        box.getChildren().addAll(
            fpsLabel,
            updateRateLabel,
            latencyLabel,
            cpuLabel,
            memoryLabel
        );
        
        return box;
    }
    
    private VBox createMissionProgress() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        
        waypointsLabel = createStatusLabel("Waypoints: 0 / 0");
        tasksCompleteLabel = createStatusLabel("Tasks Complete: 0");
        currentFormationLabel = createStatusLabel("Formation: None");
        missionTimeLabel = createStatusLabel("Mission Time: 00:00");
        
        Label progressLabel = new Label("Mission Progress:");
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        missionProgressBar = new ProgressBar(0.0);
        missionProgressBar.setPrefWidth(240);
        missionProgressBar.setStyle("-fx-accent: purple;");
        
        box.getChildren().addAll(
            waypointsLabel,
            tasksCompleteLabel,
            currentFormationLabel,
            missionTimeLabel,
            new Separator(),
            progressLabel,
            missionProgressBar
        );
        
        return box;
    }
    
    private Label createStatusLabel(String text) {
        return createStatusLabel(text, Color.BLACK);
    }
    
    private Label createStatusLabel(String text, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 11));
        label.setTextFill(color);
        return label;
    }
    
    // ==================== EVENT LISTENERS ====================
    
    private void setupEventListeners() {
        // Listen for visualization updates
        eventBus.subscribe(VisualizationUpdate.class, this::handleVisualizationUpdate);
        
        // Listen for network status
        eventBus.subscribe(NetworkStatus.class, this::handleNetworkStatus);
        
        // Listen for decision status
        eventBus.subscribe(DecisionStatus.class, this::handleDecisionStatus);
        
        // Listen for task completion
        eventBus.subscribe(TaskCompletionReport.class, this::handleTaskCompletion);
    }
    
    private void handleVisualizationUpdate(VisualizationUpdate update) {
        Platform.runLater(() -> {
            // Update agent statistics
            totalAgents = update.allAgents.size();
            activeAgents = 0;
            failedAgents = 0;
            batteryLowAgents = 0;
            
            for (AgentState agent : update.allAgents) {
                switch (agent.status) {
                    case ACTIVE:
                        activeAgents++;
                        break;
                    case FAILED:
                        failedAgents++;
                        break;
                    case BATTERY_LOW:
                        batteryLowAgents++;
                        break;
                }
            }
            
            // Update labels
            totalAgentsLabel.setText("Total Agents: " + totalAgents);
            activeAgentsLabel.setText("Active: " + activeAgents);
            failedAgentsLabel.setText("Failed: " + failedAgents);
            batteryLowLabel.setText("Battery Low: " + batteryLowAgents);
            
            // Update system health bar
            double health = totalAgents > 0 ? (double) activeAgents / totalAgents : 1.0;
            systemHealthBar.setProgress(health);
            
            // Update health bar color
            if (health > 0.8) {
                systemHealthBar.setStyle("-fx-accent: green;");
            } else if (health > 0.5) {
                systemHealthBar.setStyle("-fx-accent: orange;");
            } else {
                systemHealthBar.setStyle("-fx-accent: red;");
            }
            
            // Update performance metrics
            if (update.systemMetrics != null) {
                fpsLabel.setText(String.format("FPS: %.1f", update.systemMetrics.fps));
                updateRateLabel.setText("Update Rate: 30 Hz");
            }
        });
    }
    
    private void handleNetworkStatus(NetworkStatus status) {
        Platform.runLater(() -> {
            // Update network statistics
            totalConnections = status.connections.size();
            avgNeighbors = status.averageNeighbors;
            networkDensity = status.networkDensity;
            isolatedAgents = status.isolatedAgents;
            partitions = status.partitions;
            
            // Update labels
            connectionsLabel.setText("Connections: " + totalConnections);
            avgNeighborsLabel.setText(String.format("Avg Neighbors: %.1f", avgNeighbors));
            networkDensityLabel.setText(String.format("Density: %.0f%%", networkDensity * 100));
            isolatedAgentsLabel.setText("Isolated: " + isolatedAgents);
            partitionsLabel.setText("Partitions: " + partitions);
            
            // Update network health bar
            double health = status.overallHealth;
            networkHealthBar.setProgress(health);
            
            // Update health bar color
            if (health > 0.7) {
                networkHealthBar.setStyle("-fx-accent: green;");
            } else if (health > 0.4) {
                networkHealthBar.setStyle("-fx-accent: orange;");
            } else {
                networkHealthBar.setStyle("-fx-accent: red;");
            }
            
            // Update isolated agents label color
            if (isolatedAgents > 0) {
                isolatedAgentsLabel.setTextFill(Color.RED);
            } else {
                isolatedAgentsLabel.setTextFill(Color.GREEN);
            }
        });
    }
    
    private void handleDecisionStatus(DecisionStatus status) {
        Platform.runLater(() -> {
            // Count active votes
            int activeVotes = status.isPending ? 1 : 0;
            activeVotesLabel.setText("Active Votes: " + activeVotes);
            
            if (status.isPending) {
                // Update current vote information
                currentQuestionLabel.setText("\"" + status.question + "\"");
                
                voteProgressLabel.setText(String.format("Progress: %d/%d (%.0f%%)",
                    status.votesReceived, status.totalAgents, status.progress * 100));
                
                if (status.leadingOption != null) {
                    leadingOptionLabel.setText(String.format("Leading: %s (%.0f%%)",
                        status.leadingOption, status.leadingPercentage * 100));
                } else {
                    leadingOptionLabel.setText("Leading: None");
                }
                
                timeRemainingLabel.setText("Time: " + status.getTimeRemainingFormatted());
                
                // Update progress bar
                voteProgressBar.setProgress(status.progress);
                
                // Color code based on consensus
                if (status.consensusReached) {
                    voteProgressBar.setStyle("-fx-accent: green;");
                    leadingOptionLabel.setTextFill(Color.GREEN);
                } else if (status.isExpired) {
                    voteProgressBar.setStyle("-fx-accent: red;");
                    timeRemainingLabel.setTextFill(Color.RED);
                } else {
                    voteProgressBar.setStyle("-fx-accent: blue;");
                    leadingOptionLabel.setTextFill(Color.BLUE);
                }
            } else {
                // No active votes
                currentQuestionLabel.setText("No active votes");
                voteProgressLabel.setText("Progress: 0/0 (0%)");
                leadingOptionLabel.setText("Leading: None");
                timeRemainingLabel.setText("Time: --");
                voteProgressBar.setProgress(0.0);
            }
        });
    }
    
    private void handleTaskCompletion(TaskCompletionReport report) {
        Platform.runLater(() -> {
            // Update mission progress
            // This will be fully implemented when TaskAllocator provides completion reports
            tasksCompleteLabel.setText("Tasks Complete: " + report.completedTasks);
        });
    }
    
    // ==================== UPDATE TIMER ====================
    
    private void startUpdateTimer() {
        // Update performance metrics every second
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updatePerformanceMetrics());
            }
        }, 1000, 1000);
    }
    
    private void updatePerformanceMetrics() {
        // Update FPS (already updated from VisualizationUpdate)
        
        // Update CPU and memory
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        memoryLabel.setText(String.format("Memory: %d MB", usedMemory));
        
        // CPU usage (simplified - would need more complex calculation for accurate CPU%)
        cpuLabel.setText("CPU: --");
        
        // Update latency (from network status if available)
        latencyLabel.setText("Latency: -- ms");
        
        // Update mission time
        if (missionStartTime > 0) {
            long elapsed = (System.currentTimeMillis() - missionStartTime) / 1000;
            long minutes = elapsed / 60;
            long seconds = elapsed % 60;
            missionTimeLabel.setText(String.format("Mission Time: %02d:%02d", minutes, seconds));
        }
    }
    
    // ==================== PUBLIC METHODS ====================
    
    public void startMissionTimer() {
        missionStartTime = System.currentTimeMillis();
    }
    
    public void stopMissionTimer() {
        missionStartTime = 0;
        missionTimeLabel.setText("Mission Time: 00:00");
    }
    
    public void updateWaypointProgress(int completed, int total) {
        Platform.runLater(() -> {
            waypointsLabel.setText(String.format("Waypoints: %d / %d", completed, total));
            
            double progress = total > 0 ? (double) completed / total : 0.0;
            missionProgressBar.setProgress(progress);
            
            // Update color
            if (progress >= 1.0) {
                missionProgressBar.setStyle("-fx-accent: green;");
            } else if (progress > 0.0) {
                missionProgressBar.setStyle("-fx-accent: blue;");
            } else {
                missionProgressBar.setStyle("-fx-accent: gray;");
            }
        });
    }
    
    public void updateFormation(String formationType) {
        Platform.runLater(() -> {
            currentFormationLabel.setText("Formation: " + formationType);
        });
    }
    
    // ==================== GETTERS ====================
    
    public VBox getPanel() {
        return panel;
    }
}
