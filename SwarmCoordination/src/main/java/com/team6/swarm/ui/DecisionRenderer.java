package com.team6.swarm.ui;

import com.team6.swarm.intelligence.VoteStatus;
import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;

/**
 * Week 4: Visualize active votes and decisions
 * Purpose: Make collective intelligence visible
 * Author: Anthony (UI Team)
 */
public class DecisionRenderer {
    
    private VoteStatus currentVote;
    private final Map<String, Boolean> agentVoteStatus = new HashMap<>();
    private long voteStartTime = 0;
    
    // Visual constants
    private static final double VOTE_ICON_SIZE = 12.0;
    private static final Color VOTED_COLOR = Color.GREEN;
    private static final Color PENDING_COLOR = Color.ORANGE;
    private static final Color ABSTAINED_COLOR = Color.GRAY;
    
    /**
     * Draw voting status on agents
     */
    public void drawVotingIndicators(GraphicsContext gc, String agentId, 
                                     double x, double y, boolean hasVoted, 
                                     String voteChoice) {
        if (currentVote == null) {
            return;
        }
        
        // Position for vote indicator
        double iconX = x + 15;
        double iconY = y - 15;
        
        if (hasVoted) {
            // Green check mark
            gc.setStroke(VOTED_COLOR);
            gc.setLineWidth(2.0);
            drawCheckMark(gc, iconX, iconY);
            
            // Show choice in speech bubble
            if (voteChoice != null) {
                drawSpeechBubble(gc, iconX + 10, iconY, voteChoice);
            }
        } else {
            // Clock icon (hasn't voted)
            gc.setStroke(PENDING_COLOR);
            gc.setLineWidth(1.5);
            drawClockIcon(gc, iconX, iconY);
        }
    }
    
    /**
     * Draw check mark symbol
     */
    private void drawCheckMark(GraphicsContext gc, double x, double y) {
        double size = VOTE_ICON_SIZE;
        gc.strokeLine(x, y + size/2, x + size/3, y + size);
        gc.strokeLine(x + size/3, y + size, x + size, y);
    }
    
    /**
     * Draw clock icon
     */
    private void drawClockIcon(GraphicsContext gc, double x, double y) {
        double radius = VOTE_ICON_SIZE / 2;
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.strokeLine(x, y, x, y - radius * 0.7); // Hour hand
        gc.strokeLine(x, y, x + radius * 0.5, y); // Minute hand
    }
    
    /**
     * Draw speech bubble with vote choice
     */
    private void drawSpeechBubble(GraphicsContext gc, double x, double y, String text) {
        double bubbleWidth = 30;
        double bubbleHeight = 20;
        
        // Background
        gc.setFill(new Color(1, 1, 1, 0.9));
        gc.fillRoundRect(x, y - bubbleHeight/2, bubbleWidth, bubbleHeight, 5, 5);
        
        // Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(x, y - bubbleHeight/2, bubbleWidth, bubbleHeight, 5, 5);
        
        // Text
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText(text, x + 5, y + 5);
    }
    
    /**
     * Draw decision outcome animation
     */
    public void drawDecisionOutcome(GraphicsContext gc, double centerX, double centerY, 
                                    String outcome, double animationProgress) {
        // Flash effect
        double opacity = Math.sin(animationProgress * Math.PI * 4) * 0.5 + 0.5;
        
        // Banner background
        double bannerWidth = 300;
        double bannerHeight = 60;
        gc.setFill(new Color(0, 0.7, 0, opacity * 0.8));
        gc.fillRoundRect(centerX - bannerWidth/2, centerY - bannerHeight/2, 
                        bannerWidth, bannerHeight, 10, 10);
        
        // Border
        gc.setStroke(new Color(1, 1, 1, opacity));
        gc.setLineWidth(3.0);
        gc.strokeRoundRect(centerX - bannerWidth/2, centerY - bannerHeight/2, 
                          bannerWidth, bannerHeight, 10, 10);
        
        // Text
        gc.setFill(new Color(1, 1, 1, opacity));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText("Decision: " + outcome, centerX - 100, centerY + 10);
        
        // Celebration particles
        if (animationProgress < 0.5) {
            drawCelebrationParticles(gc, centerX, centerY, animationProgress);
        }
    }
    
    /**
     * Draw celebration particles
     */
    private void drawCelebrationParticles(GraphicsContext gc, double centerX, 
                                          double centerY, double progress) {
        int particleCount = 20;
        for (int i = 0; i < particleCount; i++) {
            double angle = (i / (double) particleCount) * Math.PI * 2;
            double distance = progress * 100;
            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + Math.sin(angle) * distance;
            double size = 5 - progress * 3;
            
            gc.setFill(new Color(1, 1, 0, 1 - progress * 2));
            gc.fillOval(x - size/2, y - size/2, size, size);
        }
    }
    
    /**
     * Create voting panel UI component
     */
    public VBox createVotingPanel(VoteStatus vote) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.95); " +
                      "-fx-border-color: black; -fx-border-width: 2;");
        
        // Title
        Label titleLabel = new Label("Active Vote: " + vote.getQuestion());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Options with progress bars
        for (String option : vote.getOptions()) {
            VBox optionBox = createOptionDisplay(vote, option);
            panel.getChildren().add(optionBox);
        }
        
        // Status
        Label statusLabel = new Label("Status: " + vote.getStatus());
        statusLabel.setFont(Font.font("Arial", 12));
        
        // Time remaining
        long timeRemaining = vote.getTimeRemaining();
        Label timeLabel = new Label(String.format("Time remaining: %d seconds", timeRemaining / 1000));
        
        // Consensus threshold
        Label thresholdLabel = new Label(String.format("Consensus threshold: %.0f%%", 
                                                       vote.getConsensusThreshold() * 100));
        
        panel.getChildren().addAll(titleLabel, statusLabel, timeLabel, thresholdLabel);
        
        return panel;
    }
    
    /**
     * Create display for a vote option
     */
    private VBox createOptionDisplay(VoteStatus vote, String option) {
        VBox box = new VBox(5);
        
        int voteCount = vote.getVoteCount(option);
        int totalVotes = vote.getTotalVotes();
        double percentage = totalVotes > 0 ? (voteCount / (double) totalVotes) * 100 : 0;
        
        Label label = new Label(String.format("%s: %.0f%% (%d votes)", 
                                             option, percentage, voteCount));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        ProgressBar progressBar = new ProgressBar(percentage / 100.0);
        progressBar.setPrefWidth(200);
        
        box.getChildren().addAll(label, progressBar);
        
        return box;
    }
    
    /**
     * Set current vote
     */
    public void setCurrentVote(VoteStatus vote) {
        this.currentVote = vote;
        this.voteStartTime = System.currentTimeMillis();
        this.agentVoteStatus.clear();
    }
    
    /**
     * Update agent vote status
     */
    public void updateAgentVoteStatus(String agentId, boolean hasVoted) {
        agentVoteStatus.put(agentId, hasVoted);
    }
    
    /**
     * Clear current vote
     */
    public void clearVote() {
        this.currentVote = null;
        this.agentVoteStatus.clear();
    }
    
    /**
     * Check if agent has voted
     */
    public boolean hasAgentVoted(String agentId) {
        return agentVoteStatus.getOrDefault(agentId, false);
    }
}
