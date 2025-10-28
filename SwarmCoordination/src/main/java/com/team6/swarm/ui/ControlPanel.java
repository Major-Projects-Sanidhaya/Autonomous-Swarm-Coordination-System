package com.team6.swarm.ui;

import com.team6.swarm.core.SystemCommand;
import com.team6.swarm.core.CommandType;
import com.team6.swarm.core.SystemController;
import com.team6.swarm.core.EventBus;
import com.team6.swarm.core.Point2D;
import com.team6.swarm.communication.NetworkConfiguration;
import com.team6.swarm.intelligence.BehaviorConfiguration;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * CONTROLPANEL CLASS - User Control Interface
 *
 * PURPOSE: As per spec - Handles user controls and commands.
 * INTEGRATION: Sends commands via SystemController and EventBus.
 * ALIGNMENT: Uses folder's SystemController.java, EventBus.java.
 */
public class ControlPanel extends VBox {
    private SystemController systemController;
    private EventBus eventBus;

    private Button spawnAgentButton;
    private Button startStopButton;
    private Button clearWaypointsButton;
    private Slider separationWeightSlider;
    private Slider communicationRangeSlider;
    private Label statusLabel;

    public ControlPanel(SystemController systemController, EventBus eventBus) {
        this.systemController = systemController;
        this.eventBus = eventBus;
        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI() {
        spawnAgentButton = new Button("Spawn Agent");
        startStopButton = new Button("Start Simulation");
        clearWaypointsButton = new Button("Clear Waypoints");

        separationWeightSlider = new Slider(0.5, 3.0, 1.5);
        separationWeightSlider.setShowTickLabels(true);
        separationWeightSlider.setShowTickMarks(true);

        communicationRangeSlider = new Slider(50, 200, 100);
        communicationRangeSlider.setShowTickLabels(true);
        communicationRangeSlider.setShowTickMarks(true);

        statusLabel = new Label("System Ready");

        HBox buttonRow = new HBox(10, spawnAgentButton, startStopButton, clearWaypointsButton);
        HBox sliderRow1 = new HBox(10, new Label("Separation Weight:"), separationWeightSlider);
        HBox sliderRow2 = new HBox(10, new Label("Comm Range:"), communicationRangeSlider);

        this.getChildren().addAll(buttonRow, sliderRow1, sliderRow2, statusLabel);
        this.setSpacing(10);
    }

    private void setupEventHandlers() {
        spawnAgentButton.setOnAction(e -> handleSpawnAgent());
        startStopButton.setOnAction(e -> handleStartStop());
        clearWaypointsButton.setOnAction(e -> handleClearWaypoints());

        separationWeightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            handleParameterChange("separationWeight", newVal.doubleValue());
        });

        communicationRangeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            handleParameterChange("communicationRange", newVal.doubleValue());
        });
    }

    private void handleSpawnAgent() {
        try {
            SystemCommand cmd = new SystemCommand(CommandType.SPAWN_AGENT);
            cmd.parameters.put("position", new Point2D(Math.random() * 800, Math.random() * 600));
            systemController.executeCommand(cmd);
            statusLabel.setText("Agent spawned");
        } catch (Exception e) {
            statusLabel.setText("Error spawning agent");
        }
    }

    private void handleStartStop() {
        try {
            if (systemController.getState() == com.team6.swarm.core.SystemController.SimulationState.RUNNING) {
                systemController.pause();
                startStopButton.setText("Resume Simulation");
                statusLabel.setText("Simulation paused");
            } else {
                systemController.start();
                startStopButton.setText("Pause Simulation");
                statusLabel.setText("Simulation running");
            }
        } catch (Exception e) {
            statusLabel.setText("Error controlling simulation");
        }
    }

    private void handleClearWaypoints() {
        try {
            SystemCommand cmd = new SystemCommand(CommandType.CLEAR_WAYPOINTS);
            systemController.executeCommand(cmd);
            statusLabel.setText("Waypoints cleared");
        } catch (Exception e) {
            statusLabel.setText("Error clearing waypoints");
        }
    }

    private void handleParameterChange(String parameter, double value) {
        try {
            BehaviorConfiguration config = new BehaviorConfiguration();
            config.parameters.put(parameter, value);
            eventBus.publish(config);

            if (parameter.equals("communicationRange")) {
                NetworkConfiguration netConfig = new NetworkConfiguration();
                netConfig.parameters.put(parameter, value);
                eventBus.publish(netConfig);
            }
            statusLabel.setText(parameter + " updated to " + value);
        } catch (Exception e) {
            statusLabel.setText("Error updating parameter");
        }
    }

    public void handleWaypointPlacement(Point2D position) {
        try {
            SystemCommand cmd = new SystemCommand(CommandType.PLACE_WAYPOINT);
            cmd.parameters.put("position", position);
            systemController.executeCommand(cmd);
            statusLabel.setText("Waypoint placed at " + position);
        } catch (Exception e) {
            statusLabel.setText("Error placing waypoint");
        }
    }
}
