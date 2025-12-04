package com.team6.swarm.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Week 13-14: Automated demo sequences
 * Purpose: Impress during presentation
 * Author: Anthony (UI Team)
 */
public class DemoController {
    
    private Timeline demoTimeline;
    private int currentStep = 0;
    private boolean autoPilotMode = false;
    
    /**
     * Start automated demo
     */
    public void startDemo(ScenarioManager.ScenarioType scenarioType) {
        autoPilotMode = true;
        currentStep = 0;
        
        // Create demo timeline
        demoTimeline = new Timeline();
        
        switch (scenarioType) {
            case BASIC_FLOCKING -> setupFlockingDemo();
            case FORMATION_FLYING -> setupFormationDemo();
            case SEARCH_MISSION -> setupSearchDemo();
            default -> setupGenericDemo();
        }
        
        demoTimeline.play();
    }
    
    /**
     * Setup flocking demo sequence
     */
    private void setupFlockingDemo() {
        addDemoStep(0, "Initializing swarm system...");
        addDemoStep(2, "Spawning agents...");
        addDemoStep(4, "Enabling flocking behavior...");
        addDemoStep(6, "Agents naturally coordinate their movement");
        addDemoStep(10, "Notice cohesion, separation, and alignment");
        addDemoStep(15, "Demo complete!");
    }
    
    /**
     * Setup formation demo sequence
     */
    private void setupFormationDemo() {
        addDemoStep(0, "Deploying agents in line formation...");
        addDemoStep(3, "Transitioning to wedge formation...");
        addDemoStep(7, "Formation maintained during movement");
        addDemoStep(12, "Precision coordination demonstrated");
        addDemoStep(15, "Demo complete!");
    }
    
    /**
     * Setup search mission demo
     */
    private void setupSearchDemo() {
        addDemoStep(0, "Deploying search team...");
        addDemoStep(2, "Initiating grid search pattern...");
        addDemoStep(5, "Agents coordinating task allocation...");
        addDemoStep(10, "Search area coverage: 75%");
        addDemoStep(15, "Mission complete!");
    }
    
    /**
     * Setup generic demo
     */
    private void setupGenericDemo() {
        addDemoStep(0, "Starting demonstration...");
        addDemoStep(10, "Demo in progress...");
        addDemoStep(20, "Demo complete!");
    }
    
    /**
     * Add a demo step
     */
    private void addDemoStep(double secondsFromStart, String narration) {
        KeyFrame keyFrame = new KeyFrame(
            Duration.seconds(secondsFromStart),
            event -> {
                System.out.println("Demo Step " + currentStep + ": " + narration);
                showNarration(narration);
                currentStep++;
            }
        );
        demoTimeline.getKeyFrames().add(keyFrame);
    }
    
    /**
     * Show narration overlay
     */
    private void showNarration(String text) {
        // This would be implemented to show overlay on the UI
        System.out.println("NARRATION: " + text);
    }
    
    /**
     * Stop demo
     */
    public void stopDemo() {
        if (demoTimeline != null) {
            demoTimeline.stop();
        }
        autoPilotMode = false;
        currentStep = 0;
    }
    
    /**
     * Create demo control panel
     */
    public VBox createDemoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        Label title = new Label("Demo Controls");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Button startFlockingBtn = new Button("Start Flocking Demo");
        startFlockingBtn.setOnAction(e -> startDemo(ScenarioManager.ScenarioType.BASIC_FLOCKING));
        
        Button startFormationBtn = new Button("Start Formation Demo");
        startFormationBtn.setOnAction(e -> startDemo(ScenarioManager.ScenarioType.FORMATION_FLYING));
        
        Button startSearchBtn = new Button("Start Search Demo");
        startSearchBtn.setOnAction(e -> startDemo(ScenarioManager.ScenarioType.SEARCH_MISSION));
        
        Button stopBtn = new Button("Stop Demo");
        stopBtn.setOnAction(e -> stopDemo());
        
        panel.getChildren().addAll(title, startFlockingBtn, startFormationBtn, 
                                   startSearchBtn, stopBtn);
        
        return panel;
    }
    
    public boolean isAutoPilotMode() {
        return autoPilotMode;
    }
}
