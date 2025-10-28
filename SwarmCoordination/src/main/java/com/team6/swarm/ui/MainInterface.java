package com.team6.swarm.ui;

import com.team6.swarm.core.SystemController;
import com.team6.swarm.core.EventBus;
import com.team6.swarm.core.SystemController.SimulationState;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * MAININTERFACE CLASS - Primary JavaFX Application for Swarm Control
 *
 * PURPOSE: As per spec - Entry point for UI and system integration.
 * INTEGRATION: Uses SystemController for simulation, EventBus for events.
 * ALIGNMENT: Matches folder's SystemController.java and EventBus.java.
 */
public class MainInterface extends Application {
    private Visualizer visualizer;
    private ControlPanel controlPanel;
    private SystemController systemController;
    private EventBus eventBus;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize system components (matches SystemController.java)
            eventBus = new EventBus();
            systemController = new SystemController();
            systemController.initialize();

            // Create UI components
            visualizer = new Visualizer(eventBus);
            controlPanel = new ControlPanel(systemController, eventBus);

            // Layout: BorderPane with controls on top, visualization in center
            BorderPane root = new BorderPane();
            root.setTop(controlPanel);
            root.setCenter(visualizer.getCanvas());

            // Scene setup
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Team 6 - Distributed Multi-Agent System");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Start simulation loop in background (integrates with SystemController.java)
            systemController.start();

            // Register for updates from other components (uses EventBus.java)
            registerEventListeners();
        } catch (Exception e) {
            System.err.println("Error starting UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerEventListeners() {
        // Listen for visualization updates from Sanidhaya (matches VisualizationUpdate.java)
        eventBus.subscribe(com.team6.swarm.core.VisualizationUpdate.class, update -> {
            visualizer.updateDisplay((com.team6.swarm.core.VisualizationUpdate) update);
        });

        // Listen for network status from John (uses new NetworkStatus.java)
        eventBus.subscribe(com.team6.swarm.communication.NetworkStatus.class, status -> {
            visualizer.updateNetworkDisplay((com.team6.swarm.communication.NetworkStatus) status);
        });

        // Listen for decision status from Lauren (uses new DecisionStatus.java)
        eventBus.subscribe(com.team6.swarm.intelligence.DecisionStatus.class, decision -> {
            visualizer.updateDecisionDisplay((com.team6.swarm.intelligence.DecisionStatus) decision);
        });
    }

    @Override
    public void stop() {
        // Clean shutdown (integrates with SystemController.java)
        systemController.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
