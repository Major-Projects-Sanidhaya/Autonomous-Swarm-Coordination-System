/**
 * CONTROLPANEL CLASS - User Control Interface
 *
 * PURPOSE:
 * - Provide intuitive controls for all system operations
 * - Allow real-time parameter adjustments
 * - Enable agent spawning and management
 * - Support voting and formation control
 * - Display command history
 *
 * LAYOUT SECTIONS:
 * 1. Agent Controls: Spawn, Remove, Configure
 * 2. Simulation Controls: Start, Stop, Reset, Speed
 * 3. Behavior Parameters: Separation, Alignment, Cohesion, Speed
 * 4. Network Parameters: Comm Range, Latency, Packet Loss
 * 5. Formation Controls: Line, Circle, V-Formation, Grid
 * 6. Voting Controls: Initiate Vote, Threshold
 * 7. Preset Configurations: Tight, Loose, Emergency, Fast
 * 8. Command History: Last 20 commands
 *
 * FEATURES:
 * - Real-time slider updates
 * - Preset configurations
 * - Keyboard shortcuts
 * - Command history display
 * - Mouse click agent spawning
 * - Vote dialog with custom questions
 *
 * INTEGRATION:
 * - Creates SystemCommand objects
 * - Publishes BehaviorConfiguration
 * - Publishes NetworkConfiguration
 * - Listens for command execution results
 *
 * USAGE:
 * ControlPanel panel = new ControlPanel(eventBus, systemController, visualizer);
 * VBox vbox = panel.getPanel();
 */
package com.team6.swarm.ui;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.voting.ProposalType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;

import java.util.*;

public class ControlPanel {
    // ==================== CORE COMPONENTS ====================
    private final EventBus eventBus;
    private final SystemController systemController;
    private final Visualizer visualizer;
    
    // ==================== UI COMPONENTS ====================
    private VBox panel;
    private ListView<String> commandHistoryView;
    private Label statusLabel;
    
    // ==================== CONTROLS ====================
    private Button startButton;
    private Button stopButton;
    private Slider speedSlider;
    private Slider separationSlider;
    private Slider alignmentSlider;
    private Slider cohesionSlider;
    private Slider maxSpeedSlider;
    private Slider commRangeSlider;
    private Slider latencySlider;
    private Slider packetLossSlider;
    private Slider consensusThresholdSlider;
    
    // ==================== STATE ====================
    private boolean spawnModeEnabled = false;
    private List<String> commandHistory = new ArrayList<>();
    
    /**
     * Constructor
     */
    public ControlPanel(EventBus eventBus, SystemController systemController, Visualizer visualizer) {
        this.eventBus = eventBus;
        this.systemController = systemController;
        this.visualizer = visualizer;
        
        // Create UI
        createPanel();
        
        // Set up event listeners
        setupEventListeners();
        
        System.out.println("ControlPanel initialized");
    }
    
    /**
     * Create main panel with all controls
     */
    private void createPanel() {
        panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #e8e8f0; -fx-border-color: #c0c0d0; -fx-border-width: 1;");
        panel.setPrefHeight(200);
        
        // Create sections
        HBox topRow = new HBox(10);
        topRow.getChildren().addAll(
            createAgentControls(),
            createSimulationControls(),
            createFormationControls(),
            createVotingControls()
        );
        
        TitledPane behaviorPane = new TitledPane("Behavior Parameters", createBehaviorParameters());
        behaviorPane.setExpanded(false);
        
        TitledPane networkPane = new TitledPane("Network Parameters", createNetworkParameters());
        networkPane.setExpanded(false);
        
        TitledPane presetsPane = new TitledPane("Presets & History", createPresetsAndHistory());
        presetsPane.setExpanded(false);
        
        // Status label
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.GREEN);
        
        // Add all sections
        panel.getChildren().addAll(
            statusLabel,
            topRow,
            behaviorPane,
            networkPane,
            presetsPane
        );
    }
    
    // ==================== SECTION CREATORS ====================
    
    private VBox createAgentControls() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-background-color: white; -fx-border-color: #d0d0e0; -fx-border-width: 1;");
        
        Label title = new Label("Agent Controls");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Button spawnButton = new Button("Spawn Agent");
        spawnButton.setPrefWidth(120);
        spawnButton.setOnAction(e -> handleSpawnAgent());
        
        Button removeButton = new Button("Remove Agent");
        removeButton.setPrefWidth(120);
        removeButton.setOnAction(e -> handleRemoveAgent());
        
        Button spawnModeButton = new Button("Spawn Mode");
        spawnModeButton.setPrefWidth(120);
        spawnModeButton.setOnAction(e -> toggleSpawnMode(spawnModeButton));
        
        box.getChildren().addAll(title, spawnButton, removeButton, spawnModeButton);
        return box;
    }
    
    private VBox createSimulationControls() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-background-color: white; -fx-border-color: #d0d0e0; -fx-border-width: 1;");
        
        Label title = new Label("Simulation");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        startButton = new Button("▶ Start");
        startButton.setPrefWidth(120);
        startButton.setStyle("-fx-background-color: #90ee90;");
        startButton.setOnAction(e -> handleStart());
        
        stopButton = new Button("⏸ Stop");
        stopButton.setPrefWidth(120);
        stopButton.setStyle("-fx-background-color: #ffb6c1;");
        stopButton.setOnAction(e -> handleStop());
        stopButton.setDisable(true);
        
        Button resetButton = new Button("↻ Reset");
        resetButton.setPrefWidth(120);
        resetButton.setOnAction(e -> handleReset());
        
        HBox speedBox = new HBox(5);
        Label speedLabel = new Label("Speed:");
        speedSlider = new Slider(0.1, 5.0, 1.0);
        speedSlider.setPrefWidth(80);
        speedSlider.setShowTickLabels(false);
        speedSlider.valueProperty().addListener((obs, old, val) -> {
            systemController.setSimulationSpeed(val.doubleValue());
        });
        speedBox.getChildren().addAll(speedLabel, speedSlider);
        
        box.getChildren().addAll(title, startButton, stopButton, resetButton, speedBox);
        return box;
    }
    
    private VBox createFormationControls() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-background-color: white; -fx-border-color: #d0d0e0; -fx-border-width: 1;");
        
        Label title = new Label("Formation");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Button lineButton = new Button("Line");
        lineButton.setPrefWidth(120);
        lineButton.setOnAction(e -> setFormation("LINE"));
        
        Button circleButton = new Button("Circle");
        circleButton.setPrefWidth(120);
        circleButton.setOnAction(e -> setFormation("CIRCLE"));
        
        Button vButton = new Button("V-Formation");
        vButton.setPrefWidth(120);
        vButton.setOnAction(e -> setFormation("V_FORMATION"));
        
        Button breakButton = new Button("Break");
        breakButton.setPrefWidth(120);
        breakButton.setOnAction(e -> breakFormation());
        
        box.getChildren().addAll(title, lineButton, circleButton, vButton, breakButton);
        return box;
    }
    
    private VBox createVotingControls() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-background-color: white; -fx-border-color: #d0d0e0; -fx-border-width: 1;");
        
        Label title = new Label("Voting");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Button voteButton = new Button("Initiate Vote");
        voteButton.setPrefWidth(120);
        voteButton.setOnAction(e -> showVoteDialog());
        
        HBox thresholdBox = new HBox(5);
        Label thresholdLabel = new Label("Threshold:");
        consensusThresholdSlider = new Slider(0.5, 1.0, 0.6);
        consensusThresholdSlider.setPrefWidth(80);
        consensusThresholdSlider.setShowTickLabels(false);
        thresholdBox.getChildren().addAll(thresholdLabel, consensusThresholdSlider);
        
        box.getChildren().addAll(title, voteButton, thresholdBox);
        return box;
    }
    
    private VBox createBehaviorParameters() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        
        separationSlider = createParameterSlider("Separation Weight", 0.0, 3.0, 1.5, 
            val -> updateBehaviorParameter("separationWeight", val));
        
        alignmentSlider = createParameterSlider("Alignment Weight", 0.0, 3.0, 1.0,
            val -> updateBehaviorParameter("alignmentWeight", val));
        
        cohesionSlider = createParameterSlider("Cohesion Weight", 0.0, 3.0, 1.0,
            val -> updateBehaviorParameter("cohesionWeight", val));
        
        maxSpeedSlider = createParameterSlider("Max Speed", 10.0, 100.0, 50.0,
            val -> updateBehaviorParameter("maxSpeed", val));
        
        box.getChildren().addAll(separationSlider, alignmentSlider, cohesionSlider, maxSpeedSlider);
        return box;
    }
    
    private VBox createNetworkParameters() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        
        commRangeSlider = createParameterSlider("Comm Range", 50.0, 300.0, 100.0,
            val -> updateNetworkParameter("maxRange", val));
        
        latencySlider = createParameterSlider("Latency (ms)", 0.0, 100.0, 10.0,
            val -> updateNetworkParameter("baseLatency", val));
        
        packetLossSlider = createParameterSlider("Packet Loss (%)", 0.0, 50.0, 5.0,
            val -> updateNetworkParameter("packetLossRate", val / 100.0));
        
        box.getChildren().addAll(commRangeSlider, latencySlider, packetLossSlider);
        return box;
    }
    
    private VBox createPresetsAndHistory() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        
        // Presets
        Label presetsLabel = new Label("Behavior Presets:");
        presetsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        HBox presetsBox = new HBox(5);
        Button tightButton = new Button("Tight");
        tightButton.setOnAction(e -> applyPreset("TIGHT"));
        Button looseButton = new Button("Loose");
        looseButton.setOnAction(e -> applyPreset("LOOSE"));
        Button emergencyButton = new Button("Emergency");
        emergencyButton.setOnAction(e -> applyPreset("EMERGENCY"));
        Button fastButton = new Button("Fast");
        fastButton.setOnAction(e -> applyPreset("FAST"));
        presetsBox.getChildren().addAll(tightButton, looseButton, emergencyButton, fastButton);
        
        // Command history
        Label historyLabel = new Label("Command History:");
        historyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        commandHistoryView = new ListView<>();
        commandHistoryView.setPrefHeight(80);
        commandHistoryView.setItems(FXCollections.observableArrayList(commandHistory));
        
        box.getChildren().addAll(presetsLabel, presetsBox, historyLabel, commandHistoryView);
        return box;
    }
    
    private HBox createParameterSlider(String label, double min, double max, double initial,
                                       java.util.function.Consumer<Double> onChange) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(label + ":");
        nameLabel.setPrefWidth(150);
        
        Slider slider = new Slider(min, max, initial);
        slider.setPrefWidth(200);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit((max - min) / 4);
        
        Label valueLabel = new Label(String.format("%.2f", initial));
        valueLabel.setPrefWidth(50);
        
        slider.valueProperty().addListener((obs, old, val) -> {
            valueLabel.setText(String.format("%.2f", val.doubleValue()));
            onChange.accept(val.doubleValue());
        });
        
        box.getChildren().addAll(nameLabel, slider, valueLabel);
        return box;
    }
    
    // ==================== EVENT HANDLERS ====================
    
    private void handleSpawnAgent() {
        SystemCommand cmd = SystemCommand.spawnAgent(
            new Point2D(
                Math.random() * systemController.getWorldWidth(),
                Math.random() * systemController.getWorldHeight()
            ),
            50.0
        );
        executeCommand(cmd);
    }
    
    public void spawnAgentAtCenter() {
        SystemCommand cmd = SystemCommand.spawnAgent(
            new Point2D(
                systemController.getWorldWidth() / 2,
                systemController.getWorldHeight() / 2
            ),
            50.0
        );
        executeCommand(cmd);
    }
    
    private void handleRemoveAgent() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove Agent");
        dialog.setHeaderText("Enter Agent ID to remove:");
        dialog.setContentText("Agent ID:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(idStr -> {
            try {
                int agentId = Integer.parseInt(idStr);
                SystemCommand cmd = SystemCommand.removeAgent(agentId);
                executeCommand(cmd);
            } catch (NumberFormatException e) {
                showError("Invalid agent ID: " + idStr);
            }
        });
    }
    
    private void toggleSpawnMode(Button button) {
        spawnModeEnabled = !spawnModeEnabled;
        
        if (spawnModeEnabled) {
            button.setText("Spawn Mode: ON");
            button.setStyle("-fx-background-color: #90ee90;");
            
            // Add mouse click handler to canvas
            visualizer.getCanvas().setOnMouseClicked(event -> {
                if (spawnModeEnabled) {
                    double worldX = event.getX() / visualizer.getZoomLevel();
                    double worldY = event.getY() / visualizer.getZoomLevel();
                    
                    SystemCommand cmd = SystemCommand.spawnAgent(
                        new Point2D(worldX, worldY), 50.0
                    );
                    executeCommand(cmd);
                }
            });
        } else {
            button.setText("Spawn Mode");
            button.setStyle("");
            visualizer.getCanvas().setOnMouseClicked(null);
        }
    }
    
    private void handleStart() {
        SystemCommand cmd = new SystemCommand(CommandType.START_SIMULATION);
        executeCommand(cmd);
        
        startButton.setDisable(true);
        stopButton.setDisable(false);
        updateStatus("Simulation running", Color.GREEN);
    }
    
    private void handleStop() {
        SystemCommand cmd = new SystemCommand(CommandType.STOP_SIMULATION);
        executeCommand(cmd);
        
        startButton.setDisable(false);
        stopButton.setDisable(true);
        updateStatus("Simulation stopped", Color.ORANGE);
    }
    
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Simulation");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will remove all agents and reset parameters.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SystemCommand cmd = new SystemCommand(CommandType.RESET_SIMULATION);
            executeCommand(cmd);
            
            startButton.setDisable(false);
            stopButton.setDisable(true);
            updateStatus("Simulation reset", Color.BLUE);
        }
    }
    
    private void setFormation(String formationType) {
        SystemCommand cmd = new SystemCommand(CommandType.SET_FORMATION);
        cmd.addParameter("formationType", formationType);
        cmd.addParameter("spacing", 50.0);
        executeCommand(cmd);
    }
    
    private void breakFormation() {
        SystemCommand cmd = new SystemCommand(CommandType.BREAK_FORMATION);
        executeCommand(cmd);
    }
    
    public void showVoteDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Initiate Vote");
        dialog.setHeaderText("Create a voting proposal");
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField questionField = new TextField();
        questionField.setPromptText("What should the swarm decide?");
        questionField.setPrefWidth(300);
        
        TextField option1Field = new TextField();
        option1Field.setPromptText("Option 1");
        
        TextField option2Field = new TextField();
        option2Field.setPromptText("Option 2");
        
        TextField option3Field = new TextField();
        option3Field.setPromptText("Option 3 (optional)");
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("NAVIGATION", "FORMATION", "MISSION", "COORDINATION", "EMERGENCY");
        typeCombo.setValue("COORDINATION");
        
        grid.add(new Label("Question:"), 0, 0);
        grid.add(questionField, 1, 0);
        grid.add(new Label("Option 1:"), 0, 1);
        grid.add(option1Field, 1, 1);
        grid.add(new Label("Option 2:"), 0, 2);
        grid.add(option2Field, 1, 2);
        grid.add(new Label("Option 3:"), 0, 3);
        grid.add(option3Field, 1, 3);
        grid.add(new Label("Type:"), 0, 4);
        grid.add(typeCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Map<String, Object> result = new HashMap<>();
                result.put("question", questionField.getText());
                
                List<String> options = new ArrayList<>();
                if (!option1Field.getText().isEmpty()) options.add(option1Field.getText());
                if (!option2Field.getText().isEmpty()) options.add(option2Field.getText());
                if (!option3Field.getText().isEmpty()) options.add(option3Field.getText());
                
                result.put("options", options);
                result.put("type", typeCombo.getValue());
                return result;
            }
            return null;
        });
        
        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String question = (String) data.get("question");
            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) data.get("options");
            String type = (String) data.get("type");
            
            if (question != null && !question.isEmpty() && options.size() >= 2) {
                SystemCommand cmd = new SystemCommand(CommandType.INITIATE_VOTE);
                cmd.addParameter("question", question);
                cmd.addParameter("options", options);
                cmd.addParameter("proposalType", type);
                executeCommand(cmd);
            } else {
                showError("Invalid vote parameters. Need question and at least 2 options.");
            }
        });
    }
    
    public void showFormationMenu() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("LINE", 
            "LINE", "CIRCLE", "V_FORMATION", "GRID");
        dialog.setTitle("Formation Selection");
        dialog.setHeaderText("Choose formation type:");
        dialog.setContentText("Formation:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::setFormation);
    }
    
    public void showPresetsDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("TIGHT",
            "TIGHT", "LOOSE", "EMERGENCY", "FAST");
        dialog.setTitle("Behavior Presets");
        dialog.setHeaderText("Choose preset configuration:");
        dialog.setContentText("Preset:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::applyPreset);
    }
    
    private void updateBehaviorParameter(String param, double value) {
        BehaviorConfiguration config = systemController.getBehaviorConfiguration();
        config.parameters.put(param, value);
        eventBus.publish(config);
    }
    
    private void updateNetworkParameter(String param, double value) {
        NetworkConfiguration config = systemController.getNetworkConfiguration();
        config.parameters.put(param, value);
        eventBus.publish(config);
    }
    
    private void applyPreset(String presetName) {
        BehaviorConfiguration config = BehaviorConfiguration.getPreset(presetName);
        
        // Update sliders
        separationSlider.setValue(config.getParameter("separationWeight", 1.5));
        alignmentSlider.setValue(config.getParameter("alignmentWeight", 1.0));
        cohesionSlider.setValue(config.getParameter("cohesionWeight", 1.0));
        maxSpeedSlider.setValue(config.getParameter("maxSpeed", 50.0));
        
        // Publish configuration
        eventBus.publish(config);
        
        updateStatus("Preset applied: " + presetName, Color.BLUE);
    }
    
    private void executeCommand(SystemCommand cmd) {
        systemController.executeCommand(cmd);
        
        // Add to history
        commandHistory.add(0, cmd.getSummary());
        if (commandHistory.size() > 20) {
            commandHistory.remove(20);
        }
        commandHistoryView.setItems(FXCollections.observableArrayList(commandHistory));
    }
    
    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // ==================== EVENT LISTENERS ====================
    
    private void setupEventListeners() {
        // Listen for command execution results
        eventBus.subscribe(SystemCommand.class, cmd -> {
            if (cmd.executed) {
                if (cmd.successful) {
                    updateStatus(cmd.executionResult, Color.GREEN);
                } else {
                    updateStatus("Error: " + cmd.executionResult, Color.RED);
                }
            }
        });
    }
    
    // ==================== GETTERS ====================
    
    public VBox getPanel() {
        return panel;
    }
}
