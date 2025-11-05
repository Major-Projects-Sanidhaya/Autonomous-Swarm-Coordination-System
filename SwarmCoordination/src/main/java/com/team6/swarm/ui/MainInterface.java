/**
 * MAININTERFACE CLASS - Main JavaFX Application
 *
 * PURPOSE:
 * - Entry point for the Distributed Multi-Agent System UI
 * - Sets up complete application window with all UI components
 * - Manages application lifecycle and event coordination
 * - Provides menu bar for file operations and settings
 *
 * LAYOUT STRUCTURE:
 * ┌─────────────────────────────────────────────────────────┐
 * │ Menu Bar: File | View | Tools | Help                    │
 * ├─────────────────────────────────────────────────────────┤
 * │ MissionPanel: Waypoint Planning & Mission Control       │
 * ├──────────────────────────────────┬──────────────────────┤
 * │                                  │                      │
 * │         Visualizer               │   StatusPanel:       │
 * │      (Canvas 800x600)            │   - Agent Count      │
 * │                                  │   - Network Health   │
 * │   [Agents, Links, Waypoints]     │   - Decision Status  │
 * │                                  │   - Performance      │
 * │                                  │                      │
 * ├──────────────────────────────────┴──────────────────────┤
 * │ ControlPanel: Spawn | Remove | Parameters | Simulation  │
 * └─────────────────────────────────────────────────────────┘
 *
 * INITIALIZATION SEQUENCE:
 * 1. Create EventBus for component communication
 * 2. Create SystemController with EventBus
 * 3. Initialize all system components
 * 4. Create UI components (Visualizer, ControlPanel, StatusPanel, MissionPanel)
 * 5. Set up layout and scene
 * 6. Start animation timer for 60 FPS rendering
 * 7. Display window
 *
 * ANIMATION LOOP:
 * - AnimationTimer runs at 60 FPS
 * - Calls visualizer.render() each frame
 * - Updates UI components with latest data
 * - Maintains smooth visualization
 *
 * MENU BAR:
 * File: New, Open, Save, Export, Exit
 * View: Toggle panels, Zoom, Reset view
 * Tools: Settings, Presets, Diagnostics
 * Help: About, Documentation, Shortcuts
 *
 * USAGE:
 * public static void main(String[] args) {
 *     launch(args);
 * }
 *
 * INTEGRATION:
 * - Coordinates all UI components
 * - Manages EventBus distribution
 * - Handles window events (close, resize)
 * - Provides global keyboard shortcuts
 */
package com.team6.swarm.ui;

import com.team6.swarm.core.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

import java.io.File;
import java.util.Optional;

public class MainInterface extends Application {
    // ==================== CORE COMPONENTS ====================
    private EventBus eventBus;
    private SystemController systemController;
    
    // ==================== UI COMPONENTS ====================
    private Visualizer visualizer;
    private ControlPanel controlPanel;
    private StatusPanel statusPanel;
    private MissionPanel missionPanel;
    
    // ==================== LAYOUT COMPONENTS ====================
    private BorderPane root;
    private MenuBar menuBar;
    private Scene scene;
    private Stage primaryStage;
    
    // ==================== ANIMATION ====================
    private AnimationTimer animationTimer;
    private long lastFrameTime;
    private int frameCount;
    
    // ==================== CONFIGURATION ====================
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final String APP_TITLE = "Distributed Multi-Agent System - Ground Control";
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * JavaFX start method - called after launch()
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        System.out.println("=".repeat(60));
        System.out.println("DISTRIBUTED MULTI-AGENT SYSTEM");
        System.out.println("Ground Control Interface - Phase 2");
        System.out.println("=".repeat(60));
        
        try {
            // Initialize core systems
            initializeCore();
            
            // Create UI components
            initializeUI();
            
            // Set up layout
            setupLayout();
            
            // Create scene and configure stage
            setupStage();
            
            // Set up event handlers
            setupEventHandlers();
            
            // Start animation loop
            startAnimationLoop();
            
            // Show window
            primaryStage.show();
            
            System.out.println("\n✓ Application started successfully!");
            System.out.println("  Window size: " + WINDOW_WIDTH + "x" + WINDOW_HEIGHT);
            System.out.println("  Target FPS: 60");
            System.out.println("  Simulation rate: 30 Hz");
            System.out.println("\nReady for user input...\n");
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }
    
    /**
     * Initialize core system components
     */
    private void initializeCore() {
        System.out.println("\n[1/5] Initializing core systems...");
        
        // Create EventBus for component communication
        eventBus = new EventBus();
        System.out.println("  ✓ EventBus created");
        
        // Create SystemController
        systemController = new SystemController(eventBus);
        System.out.println("  ✓ SystemController created");
        
        // Initialize all system components
        systemController.initialize();
        System.out.println("  ✓ All components initialized");
    }
    
    /**
     * Initialize UI components
     */
    private void initializeUI() {
        System.out.println("\n[2/5] Creating UI components...");
        
        // Create Visualizer (canvas for agent display)
        visualizer = new Visualizer(eventBus, systemController);
        System.out.println("  ✓ Visualizer created");
        
        // Create ControlPanel (user controls)
        controlPanel = new ControlPanel(eventBus, systemController, visualizer);
        System.out.println("  ✓ ControlPanel created");
        
        // Create StatusPanel (system metrics)
        statusPanel = new StatusPanel(eventBus, systemController);
        System.out.println("  ✓ StatusPanel created");
        
        // Create MissionPanel (waypoint planning)
        missionPanel = new MissionPanel(eventBus, systemController, visualizer);
        System.out.println("  ✓ MissionPanel created");
    }
    
    /**
     * Set up application layout
     */
    private void setupLayout() {
        System.out.println("\n[3/5] Setting up layout...");
        
        // Create menu bar
        menuBar = createMenuBar();
        
        // Create main layout (BorderPane)
        root = new BorderPane();
        
        // Top: Menu bar and Mission panel
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, missionPanel.getPanel());
        root.setTop(topContainer);
        
        // Center: Visualizer
        root.setCenter(visualizer.getCanvasPane());
        
        // Right: Status panel
        root.setRight(statusPanel.getPanel());
        
        // Bottom: Control panel
        root.setBottom(controlPanel.getPanel());
        
        System.out.println("  ✓ Layout configured");
    }
    
    /**
     * Set up stage and scene
     */
    private void setupStage() {
        System.out.println("\n[4/5] Configuring window...");
        
        // Create scene
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Apply CSS styling (optional - can be added later)
        // scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        // Configure stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        
        // Handle window close
        primaryStage.setOnCloseRequest(event -> {
            handleExit();
        });
        
        System.out.println("  ✓ Window configured");
    }
    
    /**
     * Set up event handlers and keyboard shortcuts
     */
    private void setupEventHandlers() {
        System.out.println("\n[5/5] Setting up event handlers...");
        
        // Global keyboard shortcuts
        scene.setOnKeyPressed(event -> {
            // Space: Start/Stop simulation
            if (event.getCode() == KeyCode.SPACE) {
                if (systemController.isSimulationRunning()) {
                    systemController.stopSimulation();
                } else {
                    systemController.startSimulation();
                }
                event.consume();
            }
            
            // R: Reset simulation
            else if (event.getCode() == KeyCode.R && event.isControlDown()) {
                handleReset();
                event.consume();
            }
            
            // S: Spawn agent at center
            else if (event.getCode() == KeyCode.S && event.isControlDown()) {
                controlPanel.spawnAgentAtCenter();
                event.consume();
            }
            
            // V: Initiate vote
            else if (event.getCode() == KeyCode.V && event.isControlDown()) {
                controlPanel.showVoteDialog();
                event.consume();
            }
            
            // F: Formation menu
            else if (event.getCode() == KeyCode.F && event.isControlDown()) {
                controlPanel.showFormationMenu();
                event.consume();
            }
            
            // Escape: Emergency stop
            else if (event.getCode() == KeyCode.ESCAPE) {
                handleEmergencyStop();
                event.consume();
            }
        });
        
        System.out.println("  ✓ Event handlers configured");
        System.out.println("\nKeyboard Shortcuts:");
        System.out.println("  Space       - Start/Stop simulation");
        System.out.println("  Ctrl+R      - Reset simulation");
        System.out.println("  Ctrl+S      - Spawn agent");
        System.out.println("  Ctrl+V      - Initiate vote");
        System.out.println("  Ctrl+F      - Formation menu");
        System.out.println("  Escape      - Emergency stop");
    }
    
    /**
     * Start animation loop for 60 FPS rendering
     */
    private void startAnimationLoop() {
        lastFrameTime = System.nanoTime();
        frameCount = 0;
        
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate delta time
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;
                
                // Render visualizer
                visualizer.render();
                
                // Update frame count
                frameCount++;
            }
        };
        
        animationTimer.start();
        System.out.println("\n✓ Animation loop started (60 FPS target)");
    }
    
    /**
     * Create menu bar
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File Menu
        Menu fileMenu = new Menu("File");
        
        MenuItem newItem = new MenuItem("New Simulation");
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newItem.setOnAction(e -> handleNew());
        
        MenuItem openItem = new MenuItem("Open...");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        openItem.setOnAction(e -> handleOpen());
        
        MenuItem saveItem = new MenuItem("Save...");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        saveItem.setOnAction(e -> handleSave());
        
        MenuItem exportItem = new MenuItem("Export Data...");
        exportItem.setOnAction(e -> handleExport());
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        exitItem.setOnAction(e -> handleExit());
        
        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), 
                                   exportItem, new SeparatorMenuItem(), exitItem);
        
        // View Menu
        Menu viewMenu = new Menu("View");
        
        CheckMenuItem showStatusItem = new CheckMenuItem("Show Status Panel");
        showStatusItem.setSelected(true);
        showStatusItem.setOnAction(e -> toggleStatusPanel(showStatusItem.isSelected()));
        
        CheckMenuItem showMissionItem = new CheckMenuItem("Show Mission Panel");
        showMissionItem.setSelected(true);
        showMissionItem.setOnAction(e -> toggleMissionPanel(showMissionItem.isSelected()));
        
        MenuItem resetViewItem = new MenuItem("Reset View");
        resetViewItem.setOnAction(e -> visualizer.resetView());
        
        MenuItem zoomInItem = new MenuItem("Zoom In");
        zoomInItem.setAccelerator(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN));
        zoomInItem.setOnAction(e -> visualizer.zoomIn());
        
        MenuItem zoomOutItem = new MenuItem("Zoom Out");
        zoomOutItem.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
        zoomOutItem.setOnAction(e -> visualizer.zoomOut());
        
        viewMenu.getItems().addAll(showStatusItem, showMissionItem, new SeparatorMenuItem(),
                                   resetViewItem, zoomInItem, zoomOutItem);
        
        // Tools Menu
        Menu toolsMenu = new Menu("Tools");
        
        MenuItem presetsItem = new MenuItem("Behavior Presets...");
        presetsItem.setOnAction(e -> controlPanel.showPresetsDialog());
        
        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.setOnAction(e -> showSettingsDialog());
        
        MenuItem diagnosticsItem = new MenuItem("Diagnostics");
        diagnosticsItem.setOnAction(e -> showDiagnostics());
        
        toolsMenu.getItems().addAll(presetsItem, settingsItem, new SeparatorMenuItem(), diagnosticsItem);
        
        // Help Menu
        Menu helpMenu = new Menu("Help");
        
        MenuItem shortcutsItem = new MenuItem("Keyboard Shortcuts");
        shortcutsItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        shortcutsItem.setOnAction(e -> showShortcuts());
        
        MenuItem documentationItem = new MenuItem("Documentation");
        documentationItem.setOnAction(e -> showDocumentation());
        
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAbout());
        
        helpMenu.getItems().addAll(shortcutsItem, documentationItem, new SeparatorMenuItem(), aboutItem);
        
        // Add all menus to menu bar
        menuBar.getMenus().addAll(fileMenu, viewMenu, toolsMenu, helpMenu);
        
        return menuBar;
    }
    
    // ==================== MENU HANDLERS ====================
    
    private void handleNew() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New Simulation");
        alert.setHeaderText("Start new simulation?");
        alert.setContentText("This will reset the current simulation and clear all agents.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SystemCommand cmd = new SystemCommand(CommandType.RESET_SIMULATION);
            systemController.executeCommand(cmd);
        }
    }
    
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Simulation");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            // TODO: Implement file loading
            System.out.println("Loading from: " + file.getAbsolutePath());
        }
    }
    
    private void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Simulation");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            // TODO: Implement file saving
            System.out.println("Saving to: " + file.getAbsolutePath());
        }
    }
    
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            // TODO: Implement data export
            System.out.println("Exporting to: " + file.getAbsolutePath());
        }
    }
    
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved changes will be lost.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stop();
            Platform.exit();
        }
    }
    
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Simulation");
        alert.setHeaderText("Reset simulation?");
        alert.setContentText("This will clear all agents and reset parameters.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SystemCommand cmd = new SystemCommand(CommandType.RESET_SIMULATION);
            systemController.executeCommand(cmd);
        }
    }
    
    private void handleEmergencyStop() {
        SystemCommand cmd = SystemCommand.emergencyStop();
        systemController.executeCommand(cmd);
        
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Emergency Stop");
        alert.setHeaderText("EMERGENCY STOP ACTIVATED");
        alert.setContentText("All agents stopped. Simulation halted.");
        alert.showAndWait();
    }
    
    private void toggleStatusPanel(boolean show) {
        if (show) {
            root.setRight(statusPanel.getPanel());
        } else {
            root.setRight(null);
        }
    }
    
    private void toggleMissionPanel(boolean show) {
        VBox topContainer = (VBox) root.getTop();
        if (show) {
            if (!topContainer.getChildren().contains(missionPanel.getPanel())) {
                topContainer.getChildren().add(missionPanel.getPanel());
            }
        } else {
            topContainer.getChildren().remove(missionPanel.getPanel());
        }
    }
    
    private void showSettingsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Application Settings");
        alert.setContentText("Settings dialog - To be implemented");
        alert.showAndWait();
    }
    
    private void showDiagnostics() {
        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("=== SYSTEM DIAGNOSTICS ===\n\n");
        diagnostics.append("Simulation Running: ").append(systemController.isSimulationRunning()).append("\n");
        diagnostics.append("Simulation Speed: ").append(systemController.getSimulationSpeed()).append("x\n");
        diagnostics.append("Current FPS: ").append(String.format("%.1f", systemController.getCurrentFps())).append("\n");
        diagnostics.append("Commands Executed: ").append(systemController.getCommandsExecuted()).append("\n");
        diagnostics.append("Agent Count: ").append(systemController.getAgentManager().getAgentCount()).append("\n");
        diagnostics.append("World Size: ").append(systemController.getWorldWidth()).append(" x ").append(systemController.getWorldHeight()).append("\n");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Diagnostics");
        alert.setHeaderText("System Diagnostics");
        alert.setContentText(diagnostics.toString());
        alert.showAndWait();
    }
    
    private void showShortcuts() {
        String shortcuts = """
            KEYBOARD SHORTCUTS
            
            Simulation Control:
              Space       - Start/Stop simulation
              Ctrl+R      - Reset simulation
              Escape      - Emergency stop
            
            Agent Control:
              Ctrl+S      - Spawn agent at center
              Click       - Spawn agent at mouse (when enabled)
            
            Mission Control:
              Ctrl+W      - Add waypoint at mouse
              Ctrl+V      - Initiate vote
              Ctrl+F      - Formation menu
            
            View Control:
              Ctrl++      - Zoom in
              Ctrl+-      - Zoom out
              Ctrl+0      - Reset view
            
            File Operations:
              Ctrl+N      - New simulation
              Ctrl+O      - Open file
              Ctrl+Shift+S - Save file
              Ctrl+Q      - Exit application
            
            Help:
              F1          - Show this help
            """;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Keyboard Shortcuts");
        alert.setHeaderText("Available Keyboard Shortcuts");
        alert.setContentText(shortcuts);
        alert.showAndWait();
    }
    
    private void showDocumentation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Documentation");
        alert.setHeaderText("User Documentation");
        alert.setContentText("Documentation available in README.md and PHASE2_IMPLEMENTATION_PLAN.md");
        alert.showAndWait();
    }
    
    private void showAbout() {
        String about = """
            DISTRIBUTED MULTI-AGENT SYSTEM
            Ground Control Interface
            
            Version: Phase 2 (Weeks 5-8)
            
            Team 6 - Software Engineering Project
            
            Components:
            • Anthony - User Interface & Integration
            • Sanidhaya - Core Agent System
            • John - Communication System
            • Lauren - Swarm Intelligence
            
            Features:
            • Real-time agent visualization
            • Democratic decision making (voting)
            • Mission planning with waypoints
            • Formation flying
            • Network topology visualization
            • Performance monitoring
            
            © 2024 Team 6
            """;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Distributed Multi-Agent System");
        alert.setContentText(about);
        alert.showAndWait();
    }
    
    /**
     * JavaFX stop method - called when application closes
     */
    @Override
    public void stop() {
        System.out.println("\nShutting down application...");
        
        // Stop animation timer
        if (animationTimer != null) {
            animationTimer.stop();
            System.out.println("  ✓ Animation timer stopped");
        }
        
        // Stop simulation
        if (systemController != null) {
            systemController.stopSimulation();
            System.out.println("  ✓ Simulation stopped");
        }
        
        System.out.println("✓ Application shutdown complete");
    }
}
