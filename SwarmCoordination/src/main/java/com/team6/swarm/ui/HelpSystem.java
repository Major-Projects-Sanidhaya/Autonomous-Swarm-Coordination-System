package com.team6.swarm.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Week 13-14: User documentation and tutorials
 * Purpose: Users need guidance
 * Author: Anthony (UI Team)
 */
public class HelpSystem {
    
    /**
     * Create help panel
     */
    public VBox createHelpPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        
        Label title = new Label("Help & Documentation");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        TabPane tabPane = new TabPane();
        
        // Quick start tutorial
        Tab quickStartTab = new Tab("Quick Start", createQuickStartGuide());
        quickStartTab.setClosable(false);
        
        // Keyboard shortcuts
        Tab shortcutsTab = new Tab("Shortcuts", createShortcutsGuide());
        shortcutsTab.setClosable(false);
        
        // Features
        Tab featuresTab = new Tab("Features", createFeaturesGuide());
        featuresTab.setClosable(false);
        
        tabPane.getTabs().addAll(quickStartTab, shortcutsTab, featuresTab);
        
        panel.getChildren().addAll(title, tabPane);
        
        return panel;
    }
    
    /**
     * Create quick start guide
     */
    private ScrollPane createQuickStartGuide() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        content.getChildren().addAll(
            createHelpSection("Step 1: Spawn Agents", 
                "• Click 'Spawn Agent' button\n" +
                "• Or click on canvas\n" +
                "• Or press 'A' key\n" +
                "• Watch agents appear"),
            
            createHelpSection("Step 2: Observe Flocking", 
                "• Agents naturally group together\n" +
                "• They move in coordinated patterns\n" +
                "• Avoid collisions automatically"),
            
            createHelpSection("Step 3: Place Waypoints", 
                "• Click on canvas to add waypoints\n" +
                "• Agents navigate to waypoints\n" +
                "• Mission progress updates in panel"),
            
            createHelpSection("Step 4: Adjust Parameters", 
                "• Use sliders to tune behavior\n" +
                "• Changes take effect in real-time\n" +
                "• Experiment with different settings")
        );
        
        return new ScrollPane(content);
    }
    
    /**
     * Create keyboard shortcuts guide
     */
    private ScrollPane createShortcutsGuide() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        content.getChildren().addAll(
            createHelpSection("Agent Controls", 
                "SPACE - Start/stop simulation\n" +
                "A - Spawn agent at mouse\n" +
                "D - Delete selected agent\n" +
                "ESC - Clear selection"),
            
            createHelpSection("View Controls", 
                "Arrow Keys - Pan camera\n" +
                "Mouse Drag - Pan camera\n" +
                "Scroll Wheel - Zoom\n" +
                "R - Reset view"),
            
            createHelpSection("Special Functions", 
                "F - Toggle formation mode\n" +
                "V - Initiate vote\n" +
                "G - Toggle grid\n" +
                "T - Toggle debug info")
        );
        
        return new ScrollPane(content);
    }
    
    /**
     * Create features guide
     */
    private ScrollPane createFeaturesGuide() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        content.getChildren().addAll(
            createHelpSection("Flocking Behavior", 
                "Agents exhibit emergent flocking behavior through:\n" +
                "• Separation - Avoid crowding neighbors\n" +
                "• Alignment - Steer towards average heading\n" +
                "• Cohesion - Move towards center of mass"),
            
            createHelpSection("Communication", 
                "Agents communicate within range:\n" +
                "• Blue links show active connections\n" +
                "• Message log displays activity\n" +
                "• Network pulses visualize broadcasts"),
            
            createHelpSection("Decision Making", 
                "Collective decisions through voting:\n" +
                "• Agents vote on navigation choices\n" +
                "• Consensus required for action\n" +
                "• Results displayed in real-time")
        );
        
        return new ScrollPane(content);
    }
    
    /**
     * Create a help section
     */
    private VBox createHelpSection(String title, String content) {
        VBox section = new VBox(5);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Arial", 11));
        contentLabel.setWrapText(true);
        
        section.getChildren().addAll(titleLabel, contentLabel);
        
        return section;
    }
    
    /**
     * Show context-sensitive help tooltip
     */
    public Tooltip createTooltip(String helpText) {
        Tooltip tooltip = new Tooltip(helpText);
        tooltip.setFont(Font.font("Arial", 11));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);
        return tooltip;
    }
}
