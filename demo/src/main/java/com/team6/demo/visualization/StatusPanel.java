package com.team6.demo.visualization;

import com.team6.demo.core.Environment;
import com.team6.demo.core.Position;
import com.team6.demo.scenarios.Scenario;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

/**
 * StatusPanel - Real-time status display for simulation
 *
 * Shows:
 * - Scenario name
 * - Simulation time
 * - Drone count and positions
 * - Mission progress
 */
public class StatusPanel extends VBox {
    private Label scenarioLabel;
    private Label timeLabel;
    private Label droneCountLabel;
    private Label statusLabel;
    private VBox droneListBox;

    public StatusPanel() {
        super(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-width: 1;");

        // Title
        Label title = new Label("STATUS");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Scenario name
        scenarioLabel = new Label("Scenario: --");
        scenarioLabel.setFont(Font.font(12));

        // Time
        timeLabel = new Label("Time: 0.0s");
        timeLabel.setFont(Font.font(12));

        // Drone count
        droneCountLabel = new Label("Drones: 0");
        droneCountLabel.setFont(Font.font(12));

        // Status
        statusLabel = new Label("Status: --");
        statusLabel.setFont(Font.font(12));
        statusLabel.setWrapText(true);

        // Drone list
        Label droneListTitle = new Label("DRONES");
        droneListTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        droneListBox = new VBox(5);
        droneListBox.setPadding(new Insets(5, 0, 0, 10));

        getChildren().addAll(
            title,
            new Separator(),
            scenarioLabel,
            timeLabel,
            droneCountLabel,
            statusLabel,
            new Separator(),
            droneListTitle,
            droneListBox
        );

        setMinWidth(250);
        setMaxWidth(300);
    }

    /**
     * Update panel with current scenario state
     */
    public void update(Scenario scenario) {
        if (scenario == null) return;

        // Scenario name
        scenarioLabel.setText("Scenario: " + scenario.getName());

        // Time
        timeLabel.setText(String.format("Time: %.1fs", scenario.getSimulationTime()));

        // Environment
        Environment env = scenario.getEnvironment();
        if (env != null) {
            Map<Integer, Position> drones = env.getAllDronePositions();

            // Drone count
            droneCountLabel.setText("Drones: " + drones.size());

            // Drone list
            droneListBox.getChildren().clear();
            for (Map.Entry<Integer, Position> entry : drones.entrySet()) {
                int id = entry.getKey();
                Position pos = entry.getValue();

                Label droneLabel = new Label(String.format(
                    "D%d: (%.0f, %.0f, %.0f)m",
                    id, pos.x, pos.y, pos.z
                ));
                droneLabel.setFont(Font.font("Monospaced", 10));
                droneListBox.getChildren().add(droneLabel);
            }
        }

        // Status info
        statusLabel.setText("Status: " + scenario.getStatusInfo());

        // Completion
        if (scenario.isComplete()) {
            statusLabel.setText(statusLabel.getText() + " [COMPLETE]");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: black;");
        }
    }

    /**
     * Clear all status display
     */
    public void clear() {
        scenarioLabel.setText("Scenario: --");
        timeLabel.setText("Time: 0.0s");
        droneCountLabel.setText("Drones: 0");
        statusLabel.setText("Status: --");
        droneListBox.getChildren().clear();
    }
}
