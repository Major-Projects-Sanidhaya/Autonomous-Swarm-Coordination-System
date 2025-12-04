package com.team6.swarm.ui;

import com.team6.swarm.core.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;

/**
 * Week 4: Display current mission progress
 * Purpose: Users track mission completion
 * Author: Anthony (UI Team)
 */
public class MissionStatusPanel extends VBox {
    
    private Label missionNameLabel;
    private Label statusLabel;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Label waypointsLabel;
    private Label timeLabel;
    private VBox agentListBox;
    
    public MissionStatusPanel() {
        super(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1;");
        
        createComponents();
        layoutComponents();
    }
    
    private void createComponents() {
        missionNameLabel = new Label("No Active Mission");
        missionNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        statusLabel = new Label("Status: IDLE");
        statusLabel.setFont(Font.font("Arial", 12));
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        
        progressLabel = new Label("Progress: 0%");
        
        waypointsLabel = new Label("Waypoints: 0/0");
        timeLabel = new Label("Time: --");
        
        agentListBox = new VBox(5);
        Label agentListTitle = new Label("Participating Agents:");
        agentListTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        agentListBox.getChildren().add(agentListTitle);
    }
    
    private void layoutComponents() {
        getChildren().addAll(
            missionNameLabel,
            statusLabel,
            progressBar,
            progressLabel,
            waypointsLabel,
            timeLabel,
            agentListBox
        );
    }
    
    public void updateMission(String name, String status, double progress,
                             int waypointsCompleted, int totalWaypoints,
                             long estimatedTimeSeconds) {
        missionNameLabel.setText("Mission: " + name);
        statusLabel.setText("Status: " + status);
        
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("Progress: %.0f%%", progress * 100));
        
        waypointsLabel.setText(String.format("Waypoints: %d/%d", 
                                            waypointsCompleted, totalWaypoints));
        
        long minutes = estimatedTimeSeconds / 60;
        long seconds = estimatedTimeSeconds % 60;
        timeLabel.setText(String.format("Estimated Time: %dm %ds", minutes, seconds));
        
        // Set color based on status
        statusLabel.setTextFill(getStatusColor(status));
    }
    
    public void updateAgentTasks(Map<String, Task> agentTasks) {
        // Clear existing agent entries (keep title)
        if (agentListBox.getChildren().size() > 1) {
            agentListBox.getChildren().subList(1, agentListBox.getChildren().size()).clear();
        }
        
        // Add agent entries
        for (Map.Entry<String, Task> entry : agentTasks.entrySet()) {
            String agentId = entry.getKey();
            Task task = entry.getValue();
            
            Label agentLabel = new Label(String.format("• %s: %s (%.0f%% complete)", 
                                                      agentId, task.getDescription(),
                                                      task.getProgress() * 100));
            agentLabel.setFont(Font.font("Arial", 11));
            agentListBox.getChildren().add(agentLabel);
        }
    }
    
    public void clearMission() {
        missionNameLabel.setText("No Active Mission");
        statusLabel.setText("Status: IDLE");
        progressBar.setProgress(0);
        progressLabel.setText("Progress: 0%");
        waypointsLabel.setText("Waypoints: 0/0");
        timeLabel.setText("Time: --");
        
        if (agentListBox.getChildren().size() > 1) {
            agentListBox.getChildren().subList(1, agentListBox.getChildren().size()).clear();
        }
    }
    
    private Color getStatusColor(String status) {
        return switch (status.toUpperCase()) {
            case "IN_PROGRESS", "RUNNING" -> Color.BLUE;
            case "COMPLETED", "SUCCESS" -> Color.GREEN;
            case "FAILED", "ERROR" -> Color.RED;
            case "PAUSED" -> Color.ORANGE;
            default -> Color.BLACK;
        };
    }
}
