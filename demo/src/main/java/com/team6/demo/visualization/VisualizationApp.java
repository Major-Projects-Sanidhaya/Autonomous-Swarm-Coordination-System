package com.team6.demo.visualization;

import com.team6.demo.scenarios.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * VisualizationApp - Main JavaFX application for visual demo
 *
 * Layout:
 * - Top: Scenario selector and controls
 * - Center: Canvas (left 70%) + StatusPanel (right 30%)
 * - Bottom: Control buttons
 */
public class VisualizationApp extends Application {
    // UI Components
    private SimulationCanvas canvas;
    private StatusPanel statusPanel;
    private ComboBox<String> scenarioSelector;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private ComboBox<String> speedSelector;
    private Label fpsLabel;

    // Simulation state
    private Scenario currentScenario;
    private AnimationTimer animationTimer;
    private boolean isPaused;
    private double speedMultiplier;
    private long lastFrameTime;
    private int frameCount;
    private long lastFpsTime;

    // Constants
    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ASCS - Visual Demo");

        // Create UI components
        createComponents();

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(createTopPanel());
        root.setCenter(createCenterPanel());
        root.setBottom(createBottomPanel());

        // Scene
        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> stopAnimation());
        primaryStage.show();

        // Initialize
        isPaused = true;
        speedMultiplier = 1.0;
        updateButtonStates();
    }

    private void createComponents() {
        // Canvas
        canvas = new SimulationCanvas(CANVAS_WIDTH, CANVAS_HEIGHT);

        // Status panel
        statusPanel = new StatusPanel();

        // Scenario selector
        scenarioSelector = new ComboBox<>();
        scenarioSelector.getItems().addAll(
            "1. Search and Rescue Mission",
            "2. Perimeter Patrol with Intruder Detection",
            "3. Precision Payload Delivery",
            "4. Agricultural Field Survey",
            "5. Emergency Response Coordination"
        );
        scenarioSelector.setValue("1. Search and Rescue Mission");
        scenarioSelector.setOnAction(e -> resetSimulation());

        // Buttons
        startButton = new Button("Start");
        startButton.setOnAction(e -> startSimulation());

        pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> togglePause());

        resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetSimulation());

        // Speed selector
        speedSelector = new ComboBox<>();
        speedSelector.getItems().addAll("0.5x", "1x", "2x", "5x", "10x");
        speedSelector.setValue("1x");
        speedSelector.setOnAction(e -> updateSpeed());

        // FPS label
        fpsLabel = new Label("FPS: 0");
        fpsLabel.setStyle("-fx-font-family: monospace;");
    }

    private VBox createTopPanel() {
        Label title = new Label("Autonomous Swarm Coordination System - Visual Demo");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox selectorBox = new HBox(10);
        selectorBox.setAlignment(Pos.CENTER_LEFT);
        selectorBox.setPadding(new Insets(5));
        selectorBox.getChildren().addAll(
            new Label("Scenario:"),
            scenarioSelector,
            new Label("Speed:"),
            speedSelector,
            fpsLabel
        );

        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(10));
        topPanel.setStyle("-fx-background-color: #EEEEEE;");
        topPanel.getChildren().addAll(title, new Separator(), selectorBox);

        return topPanel;
    }

    private BorderPane createCenterPanel() {
        BorderPane centerPanel = new BorderPane();

        // Canvas on left
        StackPane canvasPane = new StackPane(canvas);
        canvasPane.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-width: 1;");
        canvasPane.setPadding(new Insets(10));

        // Status panel on right
        centerPanel.setCenter(canvasPane);
        centerPanel.setRight(statusPanel);

        return centerPanel;
    }

    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setStyle("-fx-background-color: #EEEEEE;");
        bottomPanel.getChildren().addAll(
            startButton,
            pauseButton,
            resetButton
        );

        return bottomPanel;
    }

    private void startSimulation() {
        if (currentScenario == null) {
            createScenario();
        }

        if (animationTimer == null) {
            setupAnimationTimer();
        }

        isPaused = false;
        animationTimer.start();
        updateButtonStates();
    }

    private void togglePause() {
        isPaused = !isPaused;
        updateButtonStates();
    }

    private void resetSimulation() {
        stopAnimation();
        currentScenario = null;
        canvas.clearTrails();
        statusPanel.clear();
        isPaused = true;
        updateButtonStates();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    private void createScenario() {
        int scenarioIndex = scenarioSelector.getSelectionModel().getSelectedIndex();

        switch (scenarioIndex) {
            case 0: currentScenario = new SearchAndRescueScenario(); break;
            case 1: currentScenario = new PerimeterPatrolScenario(); break;
            case 2: currentScenario = new PayloadDeliveryScenario(); break;
            case 3: currentScenario = new AgriculturalSurveyScenario(); break;
            case 4: currentScenario = new EmergencyResponseScenario(); break;
            default: return;
        }

        currentScenario.setup();

        // Configure canvas for this scenario's world size
        double worldWidth = currentScenario.getEnvironment().getWidth();
        double worldHeight = currentScenario.getEnvironment().getLength();
        canvas.setWorldSize(worldWidth, worldHeight);
    }

    private void setupAnimationTimer() {
        lastFrameTime = System.nanoTime();
        lastFpsTime = System.nanoTime();
        frameCount = 0;

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPaused || currentScenario == null) {
                    return;
                }

                // Calculate delta time
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;

                // Apply speed multiplier
                double simulationDelta = deltaTime * speedMultiplier;

                // Update scenario (single step)
                currentScenario.update(simulationDelta);

                // Render
                canvas.render(currentScenario.getEnvironment());
                statusPanel.update(currentScenario);

                // FPS counter
                frameCount++;
                if (now - lastFpsTime >= 1_000_000_000L) {
                    fpsLabel.setText(String.format("FPS: %d", frameCount));
                    frameCount = 0;
                    lastFpsTime = now;
                }

                // Check completion
                if (currentScenario.isComplete()) {
                    isPaused = true;
                    updateButtonStates();
                    showCompletionDialog();
                }
            }
        };
    }

    private void updateSpeed() {
        String speedText = speedSelector.getValue();
        speedMultiplier = Double.parseDouble(speedText.replace("x", ""));
    }

    private void updateButtonStates() {
        startButton.setDisable(!isPaused);
        pauseButton.setDisable(isPaused);
        resetButton.setDisable(currentScenario == null && isPaused);
    }

    private void showCompletionDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Scenario Complete");
        alert.setHeaderText("Simulation Finished");
        alert.setContentText(currentScenario.getName() + " has completed!\n\n" +
                            currentScenario.getStatusInfo());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
