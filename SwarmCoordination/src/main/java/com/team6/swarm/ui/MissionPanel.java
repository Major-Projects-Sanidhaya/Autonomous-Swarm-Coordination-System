/**
 * MISSIONPANEL CLASS - Mission Planning Interface
 *
 * PURPOSE:
 * - High-level mission planning and waypoint management
 * - Interactive waypoint placement and editing
 * - Mission templates and presets
 * - Real-time mission progress tracking
 * - Mission save/load functionality
 *
 * FEATURES:
 * - Waypoint list with status indicators (pending/in-progress/complete)
 * - Drag-and-drop waypoint reordering
 * - Click canvas to add waypoint
 * - Mission templates (Search, Patrol, Escort, Formation Demo)
 * - Mission parameters (formation, speed, priority, timeout)
 * - Mission control (start, pause, abort)
 * - Mission save/load (JSON format)
 * - Real-time progress tracking
 *
 * WAYPOINT MANAGEMENT:
 * - Add: Click canvas or enter coordinates
 * - Remove: Select and click remove button
 * - Reorder: Drag waypoints in list
 * - Edit: Double-click to edit properties
 * - Status: Pending → In Progress → Completed
 *
 * MISSION EXECUTION:
 * 1. User creates waypoint sequence
 * 2. Click "Start Mission"
 * 3. TaskAllocator assigns waypoints to agents
 * 4. Agents navigate to waypoints in order
 * 5. Progress displayed in real-time
 * 6. Mission completes when all waypoints reached
 *
 * USAGE:
 * MissionPanel panel = new MissionPanel(eventBus, systemController, visualizer);
 * HBox hbox = panel.getPanel();
 */
package com.team6.swarm.ui;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.tasking.Task;
import com.team6.swarm.intelligence.tasking.TaskStatus;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class MissionPanel {
    // ==================== CORE COMPONENTS ====================
    private final EventBus eventBus;
    private final SystemController systemController;
    private final Visualizer visualizer;
    
    // ==================== UI COMPONENTS ====================
    private HBox panel;
    private ComboBox<String> missionSelector;
    private ListView<String> waypointListView;
    private ObservableList<String> waypointList;
    private Label missionStatusLabel;
    private ProgressBar missionProgressBar;
    private Button startButton;
    private Button pauseButton;
    private Button abortButton;
    
    // ==================== MISSION DATA ====================
    private List<Waypoint> waypoints = new ArrayList<>();
    private String currentMissionName = "Untitled Mission";
    private boolean missionActive = false;
    private boolean waypointPlacementMode = false;
    private int nextWaypointId = 1;
    
    // ==================== MISSION PARAMETERS ====================
    private String selectedFormation = "LINE";
    private String selectedSpeed = "NORMAL";
    private String selectedPriority = "NORMAL";
    private int missionTimeout = 300; // seconds
    
    /**
     * Constructor
     */
    public MissionPanel(EventBus eventBus, SystemController systemController, Visualizer visualizer) {
        this.eventBus = eventBus;
        this.systemController = systemController;
        this.visualizer = visualizer;
        
        // Create UI
        createPanel();
        
        // Set up event listeners
        setupEventListeners();
        
        System.out.println("MissionPanel initialized");
    }
    
    /**
     * Create main panel
     */
    private void createPanel() {
        panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #e8f0e8; -fx-border-color: #c0d0c0; -fx-border-width: 1;");
        panel.setPrefHeight(120);
        
        // Left section: Mission selector and controls
        VBox leftSection = createLeftSection();
        
        // Center section: Waypoint list
        VBox centerSection = createCenterSection();
        
        // Right section: Mission parameters and control
        VBox rightSection = createRightSection();
        
        panel.getChildren().addAll(leftSection, centerSection, rightSection);
    }
    
    // ==================== SECTION CREATORS ====================
    
    private VBox createLeftSection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(5));
        box.setPrefWidth(250);
        
        // Mission selector
        Label selectorLabel = new Label("Mission:");
        selectorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        missionSelector = new ComboBox<>();
        missionSelector.getItems().addAll(
            "Untitled Mission",
            "Search Pattern",
            "Patrol Route",
            "Escort Mission",
            "Formation Demo"
        );
        missionSelector.setValue("Untitled Mission");
        missionSelector.setPrefWidth(180);
        missionSelector.setOnAction(e -> loadMissionTemplate(missionSelector.getValue()));
        
        HBox selectorBox = new HBox(5);
        selectorBox.getChildren().addAll(selectorLabel, missionSelector);
        
        // Mission control buttons
        HBox controlBox = new HBox(5);
        
        Button newButton = new Button("New");
        newButton.setOnAction(e -> createNewMission());
        
        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> loadMission());
        
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveMission());
        
        controlBox.getChildren().addAll(newButton, loadButton, saveButton);
        
        // Mission status
        missionStatusLabel = new Label("Status: Ready");
        missionStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        missionStatusLabel.setTextFill(Color.BLUE);
        
        missionProgressBar = new ProgressBar(0.0);
        missionProgressBar.setPrefWidth(240);
        missionProgressBar.setStyle("-fx-accent: blue;");
        
        box.getChildren().addAll(
            selectorBox,
            controlBox,
            new Separator(),
            missionStatusLabel,
            missionProgressBar
        );
        
        return box;
    }
    
    private VBox createCenterSection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(5));
        box.setPrefWidth(350);
        
        // Waypoint list header
        Label listLabel = new Label("Waypoints:");
        listLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        // Waypoint list
        waypointList = FXCollections.observableArrayList();
        waypointListView = new ListView<>(waypointList);
        waypointListView.setPrefHeight(60);
        waypointListView.setPlaceholder(new Label("No waypoints. Click 'Add Waypoint' or click canvas."));
        
        // Waypoint management buttons
        HBox buttonBox = new HBox(5);
        
        Button addButton = new Button("Add Waypoint");
        addButton.setOnAction(e -> enableWaypointPlacement());
        
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> removeSelectedWaypoint());
        
        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> clearAllWaypoints());
        
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> editSelectedWaypoint());
        
        buttonBox.getChildren().addAll(addButton, removeButton, editButton, clearButton);
        
        box.getChildren().addAll(listLabel, waypointListView, buttonBox);
        
        return box;
    }
    
    private VBox createRightSection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(5));
        box.setPrefWidth(300);
        
        // Mission parameters
        Label paramsLabel = new Label("Mission Parameters:");
        paramsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        GridPane paramsGrid = new GridPane();
        paramsGrid.setHgap(10);
        paramsGrid.setVgap(5);
        
        // Formation
        Label formationLabel = new Label("Formation:");
        ComboBox<String> formationCombo = new ComboBox<>();
        formationCombo.getItems().addAll("LINE", "CIRCLE", "V_FORMATION", "GRID", "FREE");
        formationCombo.setValue("LINE");
        formationCombo.setOnAction(e -> selectedFormation = formationCombo.getValue());
        
        // Speed
        Label speedLabel = new Label("Speed:");
        ComboBox<String> speedCombo = new ComboBox<>();
        speedCombo.getItems().addAll("SLOW", "NORMAL", "FAST");
        speedCombo.setValue("NORMAL");
        speedCombo.setOnAction(e -> selectedSpeed = speedCombo.getValue());
        
        // Priority
        Label priorityLabel = new Label("Priority:");
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("LOW", "NORMAL", "HIGH", "CRITICAL");
        priorityCombo.setValue("NORMAL");
        priorityCombo.setOnAction(e -> selectedPriority = priorityCombo.getValue());
        
        // Timeout
        Label timeoutLabel = new Label("Timeout (s):");
        TextField timeoutField = new TextField("300");
        timeoutField.setPrefWidth(80);
        timeoutField.textProperty().addListener((obs, old, val) -> {
            try {
                missionTimeout = Integer.parseInt(val);
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        });
        
        paramsGrid.add(formationLabel, 0, 0);
        paramsGrid.add(formationCombo, 1, 0);
        paramsGrid.add(speedLabel, 0, 1);
        paramsGrid.add(speedCombo, 1, 1);
        paramsGrid.add(priorityLabel, 0, 2);
        paramsGrid.add(priorityCombo, 1, 2);
        paramsGrid.add(timeoutLabel, 0, 3);
        paramsGrid.add(timeoutField, 1, 3);
        
        // Mission control buttons
        HBox controlBox = new HBox(5);
        controlBox.setAlignment(Pos.CENTER);
        
        startButton = new Button("▶ Start Mission");
        startButton.setStyle("-fx-background-color: #90ee90; -fx-font-weight: bold;");
        startButton.setPrefWidth(100);
        startButton.setOnAction(e -> startMission());
        
        pauseButton = new Button("⏸ Pause");
        pauseButton.setPrefWidth(80);
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> pauseMission());
        
        abortButton = new Button("⏹ Abort");
        abortButton.setPrefWidth(80);
        abortButton.setStyle("-fx-background-color: #ffb6c1;");
        abortButton.setDisable(true);
        abortButton.setOnAction(e -> abortMission());
        
        controlBox.getChildren().addAll(startButton, pauseButton, abortButton);
        
        box.getChildren().addAll(
            paramsLabel,
            paramsGrid,
            new Separator(),
            controlBox
        );
        
        return box;
    }
    
    // ==================== WAYPOINT MANAGEMENT ====================
    
    private void enableWaypointPlacement() {
        waypointPlacementMode = true;
        
        // Set up canvas click handler
        visualizer.getCanvas().setOnMouseClicked(event -> {
            if (waypointPlacementMode) {
                double worldX = event.getX() / visualizer.getZoomLevel();
                double worldY = event.getY() / visualizer.getZoomLevel();
                
                addWaypoint(worldX, worldY);
                waypointPlacementMode = false;
                visualizer.getCanvas().setOnMouseClicked(null);
            }
        });
        
        updateMissionStatus("Click canvas to place waypoint", Color.BLUE);
    }
    
    private void addWaypoint(double x, double y) {
        Waypoint waypoint = new Waypoint();
        waypoint.id = nextWaypointId++;
        waypoint.position = new Point2D(x, y);
        waypoint.status = WaypointStatus.PENDING;
        waypoint.radius = 20.0;
        
        waypoints.add(waypoint);
        updateWaypointList();
        
        // Create task for TaskAllocator
        SystemCommand cmd = new SystemCommand(CommandType.PLACE_WAYPOINT);
        cmd.addParameter("position", waypoint.position);
        cmd.addParameter("radius", waypoint.radius);
        systemController.executeCommand(cmd);
        
        updateMissionStatus("Waypoint " + waypoint.id + " added", Color.GREEN);
    }
    
    private void removeSelectedWaypoint() {
        int selectedIndex = waypointListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < waypoints.size()) {
            Waypoint removed = waypoints.remove(selectedIndex);
            updateWaypointList();
            updateMissionStatus("Waypoint " + removed.id + " removed", Color.ORANGE);
        }
    }
    
    private void clearAllWaypoints() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Waypoints");
        alert.setHeaderText("Clear all waypoints?");
        alert.setContentText("This will remove all waypoints from the mission.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            waypoints.clear();
            updateWaypointList();
            
            SystemCommand cmd = new SystemCommand(CommandType.CLEAR_WAYPOINTS);
            systemController.executeCommand(cmd);
            
            updateMissionStatus("All waypoints cleared", Color.ORANGE);
        }
    }
    
    private void editSelectedWaypoint() {
        int selectedIndex = waypointListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < waypoints.size()) {
            Waypoint waypoint = waypoints.get(selectedIndex);
            
            Dialog<Waypoint> dialog = new Dialog<>();
            dialog.setTitle("Edit Waypoint");
            dialog.setHeaderText("Edit Waypoint " + waypoint.id);
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            
            TextField xField = new TextField(String.valueOf(waypoint.position.x));
            TextField yField = new TextField(String.valueOf(waypoint.position.y));
            TextField radiusField = new TextField(String.valueOf(waypoint.radius));
            
            grid.add(new Label("X:"), 0, 0);
            grid.add(xField, 1, 0);
            grid.add(new Label("Y:"), 0, 1);
            grid.add(yField, 1, 1);
            grid.add(new Label("Radius:"), 0, 2);
            grid.add(radiusField, 1, 2);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    try {
                        waypoint.position.x = Double.parseDouble(xField.getText());
                        waypoint.position.y = Double.parseDouble(yField.getText());
                        waypoint.radius = Double.parseDouble(radiusField.getText());
                        return waypoint;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            });
            
            Optional<Waypoint> result = dialog.showAndWait();
            if (result.isPresent()) {
                updateWaypointList();
                updateMissionStatus("Waypoint " + waypoint.id + " updated", Color.BLUE);
            }
        }
    }
    
    private void updateWaypointList() {
        waypointList.clear();
        
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);
            String statusIcon = getStatusIcon(wp.status);
            String text = String.format("%d. %s WP%d (%.0f, %.0f) - %s",
                i + 1, statusIcon, wp.id, wp.position.x, wp.position.y, wp.status);
            waypointList.add(text);
        }
        
        // Update progress
        updateMissionProgress();
    }
    
    private String getStatusIcon(WaypointStatus status) {
        switch (status) {
            case COMPLETED:
                return "☑";
            case IN_PROGRESS:
                return "⧗";
            case PENDING:
            default:
                return "☐";
        }
    }
    
    // ==================== MISSION CONTROL ====================
    
    private void startMission() {
        if (waypoints.isEmpty()) {
            showError("Cannot start mission: No waypoints defined");
            return;
        }
        
        missionActive = true;
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        abortButton.setDisable(false);
        
        // Set formation
        SystemCommand formationCmd = new SystemCommand(CommandType.SET_FORMATION);
        formationCmd.addParameter("formationType", selectedFormation);
        systemController.executeCommand(formationCmd);
        
        // Start simulation if not running
        if (!systemController.isSimulationRunning()) {
            SystemCommand startCmd = new SystemCommand(CommandType.START_SIMULATION);
            systemController.executeCommand(startCmd);
        }
        
        updateMissionStatus("Mission started: " + currentMissionName, Color.GREEN);
        
        // Notify StatusPanel
        if (systemController != null) {
            // StatusPanel will be notified through events
        }
    }
    
    private void pauseMission() {
        missionActive = false;
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        
        updateMissionStatus("Mission paused", Color.ORANGE);
    }
    
    private void abortMission() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Abort Mission");
        alert.setHeaderText("Abort current mission?");
        alert.setContentText("This will stop the mission and reset all waypoints.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            missionActive = false;
            startButton.setDisable(false);
            pauseButton.setDisable(true);
            abortButton.setDisable(true);
            
            // Reset waypoint statuses
            for (Waypoint wp : waypoints) {
                wp.status = WaypointStatus.PENDING;
            }
            updateWaypointList();
            
            updateMissionStatus("Mission aborted", Color.RED);
        }
    }
    
    private void updateMissionProgress() {
        int total = waypoints.size();
        int completed = 0;
        
        for (Waypoint wp : waypoints) {
            if (wp.status == WaypointStatus.COMPLETED) {
                completed++;
            }
        }
        
        double progress = total > 0 ? (double) completed / total : 0.0;
        missionProgressBar.setProgress(progress);
        
        // Update color
        if (progress >= 1.0) {
            missionProgressBar.setStyle("-fx-accent: green;");
            if (missionActive) {
                updateMissionStatus("Mission completed!", Color.GREEN);
                missionActive = false;
                startButton.setDisable(false);
                pauseButton.setDisable(true);
                abortButton.setDisable(true);
            }
        } else if (progress > 0.0) {
            missionProgressBar.setStyle("-fx-accent: blue;");
        }
    }
    
    private void updateMissionStatus(String message, Color color) {
        Platform.runLater(() -> {
            missionStatusLabel.setText("Status: " + message);
            missionStatusLabel.setTextFill(color);
        });
    }
    
    // ==================== MISSION TEMPLATES ====================
    
    private void loadMissionTemplate(String templateName) {
        currentMissionName = templateName;
        
        switch (templateName) {
            case "Search Pattern":
                loadSearchPattern();
                break;
            case "Patrol Route":
                loadPatrolRoute();
                break;
            case "Escort Mission":
                loadEscortMission();
                break;
            case "Formation Demo":
                loadFormationDemo();
                break;
            default:
                // Untitled Mission - do nothing
                break;
        }
    }
    
    private void loadSearchPattern() {
        clearAllWaypoints();
        
        double width = systemController.getWorldWidth();
        double height = systemController.getWorldHeight();
        
        // Create grid search pattern
        addWaypoint(width * 0.2, height * 0.2);
        addWaypoint(width * 0.8, height * 0.2);
        addWaypoint(width * 0.8, height * 0.5);
        addWaypoint(width * 0.2, height * 0.5);
        addWaypoint(width * 0.2, height * 0.8);
        addWaypoint(width * 0.8, height * 0.8);
        
        updateMissionStatus("Search Pattern loaded", Color.BLUE);
    }
    
    private void loadPatrolRoute() {
        clearAllWaypoints();
        
        double width = systemController.getWorldWidth();
        double height = systemController.getWorldHeight();
        
        // Create perimeter patrol
        addWaypoint(width * 0.1, height * 0.1);
        addWaypoint(width * 0.9, height * 0.1);
        addWaypoint(width * 0.9, height * 0.9);
        addWaypoint(width * 0.1, height * 0.9);
        
        updateMissionStatus("Patrol Route loaded", Color.BLUE);
    }
    
    private void loadEscortMission() {
        clearAllWaypoints();
        
        double width = systemController.getWorldWidth();
        double height = systemController.getWorldHeight();
        
        // Create escort path
        addWaypoint(width * 0.1, height * 0.5);
        addWaypoint(width * 0.5, height * 0.5);
        addWaypoint(width * 0.9, height * 0.5);
        
        selectedFormation = "V_FORMATION";
        updateMissionStatus("Escort Mission loaded", Color.BLUE);
    }
    
    private void loadFormationDemo() {
        clearAllWaypoints();
        
        double width = systemController.getWorldWidth();
        double height = systemController.getWorldHeight();
        
        // Create formation demo path
        addWaypoint(width * 0.3, height * 0.3);
        addWaypoint(width * 0.7, height * 0.3);
        addWaypoint(width * 0.7, height * 0.7);
        addWaypoint(width * 0.3, height * 0.7);
        
        updateMissionStatus("Formation Demo loaded", Color.BLUE);
    }
    
    // ==================== MISSION SAVE/LOAD ====================
    
    private void createNewMission() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New Mission");
        alert.setHeaderText("Create new mission?");
        alert.setContentText("This will clear current waypoints.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            waypoints.clear();
            updateWaypointList();
            currentMissionName = "Untitled Mission";
            missionSelector.setValue("Untitled Mission");
            updateMissionStatus("New mission created", Color.BLUE);
        }
    }
    
    private void saveMission() {
        // TODO: Implement JSON save functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Mission");
        alert.setHeaderText("Save Mission");
        alert.setContentText("Mission save functionality - To be implemented");
        alert.showAndWait();
    }
    
    private void loadMission() {
        // TODO: Implement JSON load functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Load Mission");
        alert.setHeaderText("Load Mission");
        alert.setContentText("Mission load functionality - To be implemented");
        alert.showAndWait();
    }
    
    // ==================== EVENT LISTENERS ====================
    
    private void setupEventListeners() {
        // Listen for task completion reports
        eventBus.subscribe(TaskCompletionReport.class, this::handleTaskCompletion);
    }
    
    private void handleTaskCompletion(TaskCompletionReport report) {
        // Update waypoint statuses based on task completion
        // This will be fully implemented when TaskAllocator provides detailed reports
        Platform.runLater(() -> {
            updateMissionProgress();
        });
    }
    
    // ==================== HELPER METHODS ====================
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Mission Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // ==================== GETTERS ====================
    
    public HBox getPanel() {
        return panel;
    }
    
    public List<Waypoint> getWaypoints() {
        return new ArrayList<>(waypoints);
    }
    
    public boolean isMissionActive() {
        return missionActive;
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Waypoint data class
     */
    public static class Waypoint {
        public int id;
        public Point2D position;
        public WaypointStatus status;
        public double radius;
        public int assignedAgentId = -1;
    }
    
    /**
     * Waypoint status enum
     */
    public enum WaypointStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }
}
