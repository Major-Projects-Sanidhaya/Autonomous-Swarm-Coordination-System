package com.team6.swarm.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Autonomous Swarm Coordination System - Interactive Demo
 * Demonstrates flocking, consensus voting, network resilience, and formations.
 */
public class SwarmDemo extends Application {

    // Canvas dimensions
    private static final int CANVAS_WIDTH = 900;
    private static final int CANVAS_HEIGHT = 700;

    // Simulation state
    private List<DemoAgent> agents = new ArrayList<>();
    private int agentIdCounter = 0;
    private double networkQuality = 1.0; // 1.0 = perfect, 0.0 = no communication
    private Random random = new Random();

    // Voting state
    private boolean votingInProgress = false;
    private int votingOption1 = 0;
    private int votingOption2 = 0;
    private int votingStartTime = 0;
    private static final int VOTING_DURATION = 180; // frames (3 seconds at 60fps)

    // UI Components
    private Canvas canvas;
    private GraphicsContext gc;
    private Label agentCountLabel;
    private Label fpsLabel;
    private Label consensusLabel;
    private Label networkHealthLabel;
    private ProgressBar consensusProgress;
    private Slider separationSlider;
    private Slider alignmentSlider;
    private Slider cohesionSlider;
    private Slider networkSlider;
    private CheckBox showLinksCheckBox;

    // Performance tracking
    private long lastFrameTime = 0;
    private int frameCount = 0;
    private long fpsUpdateTime = 0;
    private double currentFPS = 0;

    // Current scenario
    private String currentScenario = "None";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ASCS - Autonomous Swarm Demo");

        // Create main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");

        // Create canvas for visualization
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        canvas.setStyle("-fx-background-color: #0a0a0a;");

        // Allow clicking on canvas to set waypoints
        canvas.setOnMouseClicked(event -> {
            for (DemoAgent agent : agents) {
                agent.setTarget(event.getX(), event.getY());
            }
        });

        // Center: Canvas
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-background-color: #0a0a0a;");
        root.setCenter(canvasContainer);

        // Top: Title and stats
        root.setTop(createTopPanel());

        // Right: Controls
        root.setRight(createControlPanel());

        // Bottom: Scenario buttons
        root.setBottom(createScenarioPanel());

        // Initialize with some agents
        spawnInitialAgents(12);

        // Start animation loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
                updateFPS(now);
            }
        };
        timer.start();

        // Create and show scene
        Scene scene = new Scene(root, 1300, 800);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Create top panel with title and stats
     */
    private VBox createTopPanel() {
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(15));
        topPanel.setStyle("-fx-background-color: #2d2d2d;");

        Label titleLabel = new Label("Autonomous Swarm Coordination System - Interactive Demo");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        // Stats row
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        agentCountLabel = new Label("Agents: 0");
        agentCountLabel.setTextFill(Color.CYAN);
        agentCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        fpsLabel = new Label("FPS: 60");
        fpsLabel.setTextFill(Color.LIME);
        fpsLabel.setFont(Font.font("Arial", 14));

        consensusLabel = new Label("Consensus: Idle");
        consensusLabel.setTextFill(Color.ORANGE);
        consensusLabel.setFont(Font.font("Arial", 14));

        networkHealthLabel = new Label("Network: Healthy (100%)");
        networkHealthLabel.setTextFill(Color.LIME);
        networkHealthLabel.setFont(Font.font("Arial", 14));

        statsBox.getChildren().addAll(agentCountLabel, fpsLabel, consensusLabel, networkHealthLabel);

        topPanel.getChildren().addAll(titleLabel, statsBox);
        return topPanel;
    }

    /**
     * Create right control panel
     */
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setPrefWidth(350);
        controlPanel.setStyle("-fx-background-color: #2d2d2d;");

        // Agent controls section
        Label agentLabel = new Label("Agent Controls");
        agentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        agentLabel.setTextFill(Color.WHITE);

        HBox agentButtons = new HBox(10);
        Button spawnButton = createStyledButton("Spawn Agent", Color.LIME);
        Button removeButton = createStyledButton("Remove Agent", Color.TOMATO);
        spawnButton.setOnAction(e -> spawnAgent());
        removeButton.setOnAction(e -> removeAgent());
        agentButtons.getChildren().addAll(spawnButton, removeButton);

        // Formation controls
        Label formationLabel = new Label("Formation Presets");
        formationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        formationLabel.setTextFill(Color.WHITE);

        GridPane formationGrid = new GridPane();
        formationGrid.setHgap(10);
        formationGrid.setVgap(10);

        Button lineButton = createStyledButton("Line", Color.CYAN);
        Button vFormButton = createStyledButton("V-Formation", Color.CYAN);
        Button circleButton = createStyledButton("Circle", Color.CYAN);
        Button gridButton = createStyledButton("Grid", Color.CYAN);

        lineButton.setOnAction(e -> formationLine());
        vFormButton.setOnAction(e -> formationV());
        circleButton.setOnAction(e -> formationCircle());
        gridButton.setOnAction(e -> formationGrid());

        formationGrid.add(lineButton, 0, 0);
        formationGrid.add(vFormButton, 1, 0);
        formationGrid.add(circleButton, 0, 1);
        formationGrid.add(gridButton, 1, 1);

        // Flocking parameters
        Label flockingLabel = new Label("Flocking Parameters");
        flockingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        flockingLabel.setTextFill(Color.WHITE);

        VBox sliderBox = new VBox(10);
        sliderBox.getChildren().addAll(
            createSliderControl("Separation:", 0, 3, 1.5, val -> {
                for (DemoAgent a : agents) a.setSeparationWeight(val);
            }),
            createSliderControl("Alignment:", 0, 3, 1.0, val -> {
                for (DemoAgent a : agents) a.setAlignmentWeight(val);
            }),
            createSliderControl("Cohesion:", 0, 3, 1.0, val -> {
                for (DemoAgent a : agents) a.setCohesionWeight(val);
            })
        );

        // Network quality
        Label networkLabel = new Label("Network Quality");
        networkLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        networkLabel.setTextFill(Color.WHITE);

        VBox networkBox = new VBox(10);
        networkBox.getChildren().add(
            createSliderControl("Quality:", 0, 1, 1.0, val -> {
                networkQuality = val;
                updateNetworkHealthDisplay();
            })
        );

        // Visualization options
        Label vizLabel = new Label("Visualization");
        vizLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        vizLabel.setTextFill(Color.WHITE);

        showLinksCheckBox = new CheckBox("Show Communication Links");
        showLinksCheckBox.setSelected(true);
        showLinksCheckBox.setTextFill(Color.WHITE);

        Button clearTargetsButton = createStyledButton("Clear All Waypoints", Color.ORANGE);
        clearTargetsButton.setOnAction(e -> {
            for (DemoAgent agent : agents) agent.clearTarget();
        });

        // Instructions
        Label instructionsLabel = new Label("Instructions:");
        instructionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        instructionsLabel.setTextFill(Color.YELLOW);

        Label instructionsText = new Label(
            "• Click canvas to set waypoint\n" +
            "• Use sliders to adjust behavior\n" +
            "• Try scenario buttons below\n" +
            "• Lower network quality to simulate\n  communication issues"
        );
        instructionsText.setTextFill(Color.LIGHTGRAY);
        instructionsText.setFont(Font.font("Arial", 11));

        // Add all sections
        controlPanel.getChildren().addAll(
            agentLabel, agentButtons,
            new Separator(),
            formationLabel, formationGrid,
            new Separator(),
            flockingLabel, sliderBox,
            new Separator(),
            networkLabel, networkBox,
            new Separator(),
            vizLabel, showLinksCheckBox, clearTargetsButton,
            new Separator(),
            instructionsLabel, instructionsText
        );

        return controlPanel;
    }

    /**
     * Create scenario button panel
     */
    private HBox createScenarioPanel() {
        HBox scenarioPanel = new HBox(15);
        scenarioPanel.setPadding(new Insets(15));
        scenarioPanel.setAlignment(Pos.CENTER);
        scenarioPanel.setStyle("-fx-background-color: #2d2d2d;");

        Label label = new Label("Demo Scenarios:");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.WHITE);

        Button scenarioA = createStyledButton("A: Basic Flocking", Color.DODGERBLUE);
        Button scenarioB = createStyledButton("B: Consensus Vote", Color.ORANGE);
        Button scenarioC = createStyledButton("C: Network Degradation", Color.TOMATO);
        Button scenarioD = createStyledButton("D: Formation Flying", Color.MEDIUMPURPLE);

        scenarioA.setOnAction(e -> runScenarioA());
        scenarioB.setOnAction(e -> runScenarioB());
        scenarioC.setOnAction(e -> runScenarioC());
        scenarioD.setOnAction(e -> runScenarioD());

        scenarioPanel.getChildren().addAll(label, scenarioA, scenarioB, scenarioC, scenarioD);
        return scenarioPanel;
    }

    /**
     * Create styled button
     */
    private Button createStyledButton(String text, Color color) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 8 15 8 15; -fx-cursor: hand;",
            toHexString(color)
        ));
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle() + "-fx-opacity: 1.0;"));
        return button;
    }

    /**
     * Create slider control with label
     */
    private VBox createSliderControl(String label, double min, double max, double initial,
                                     SliderCallback callback) {
        VBox box = new VBox(5);
        Label labelNode = new Label(label + " " + String.format("%.2f", initial));
        labelNode.setTextFill(Color.LIGHTGRAY);

        Slider slider = new Slider(min, max, initial);
        slider.setShowTickMarks(false);
        slider.setShowTickLabels(false);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            labelNode.setText(label + " " + String.format("%.2f", newVal.doubleValue()));
            callback.onChange(newVal.doubleValue());
        });

        box.getChildren().addAll(labelNode, slider);
        return box;
    }

    /**
     * Update simulation
     */
    private void update() {
        double deltaTime = 1.0; // Fixed time step for stability

        // Update each agent
        for (DemoAgent agent : agents) {
            // Get neighbors within perception range
            List<DemoAgent> neighbors = getNeighbors(agent);
            agent.update(neighbors, deltaTime, CANVAS_WIDTH, CANVAS_HEIGHT);
        }

        // Update voting if in progress
        if (votingInProgress) {
            updateVoting();
        }

        // Update stats
        agentCountLabel.setText("Agents: " + agents.size());
    }

    /**
     * Get neighbors for an agent (considering network quality)
     */
    private List<DemoAgent> getNeighbors(DemoAgent agent) {
        return agents.stream()
            .filter(other -> other != agent)
            .filter(other -> agent.canCommunicateWith(other))
            .filter(other -> random.nextDouble() < networkQuality) // Network packet loss
            .collect(Collectors.toList());
    }

    /**
     * Update voting state
     */
    private void updateVoting() {
        votingStartTime++;

        if (votingStartTime >= VOTING_DURATION) {
            // Voting complete
            votingInProgress = false;
            String result = votingOption1 > votingOption2 ? "Option A" :
                           votingOption2 > votingOption1 ? "Option B" : "Tie";
            consensusLabel.setText("Consensus: " + result + " (" +
                                  votingOption1 + " vs " + votingOption2 + ")");

            // Reset agent states
            for (DemoAgent agent : agents) {
                agent.setVoting(false);
                agent.setState(DemoAgent.AgentState.ACTIVE);
            }
        } else {
            double progress = (double) votingStartTime / VOTING_DURATION;
            consensusLabel.setText(String.format("Voting: %.0f%% complete", progress * 100));
        }
    }

    /**
     * Render the simulation
     */
    private void render() {
        // Clear canvas
        gc.setFill(Color.rgb(10, 10, 10));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw grid
        gc.setStroke(Color.rgb(30, 30, 30));
        gc.setLineWidth(1);
        for (int i = 0; i < CANVAS_WIDTH; i += 50) {
            gc.strokeLine(i, 0, i, CANVAS_HEIGHT);
        }
        for (int i = 0; i < CANVAS_HEIGHT; i += 50) {
            gc.strokeLine(0, i, CANVAS_WIDTH, i);
        }

        // Draw communication links
        if (showLinksCheckBox.isSelected()) {
            gc.setLineWidth(1);
            for (DemoAgent agent : agents) {
                for (DemoAgent other : agents) {
                    if (agent != other && agent.canCommunicateWith(other) &&
                        random.nextDouble() < networkQuality) {
                        gc.setStroke(Color.rgb(50, 50, 150, 0.2));
                        gc.strokeLine(agent.getX(), agent.getY(), other.getX(), other.getY());
                    }
                }
            }
        }

        // Draw agents
        for (DemoAgent agent : agents) {
            drawAgent(agent);
        }

        // Draw scenario label
        if (!currentScenario.equals("None")) {
            gc.setFill(Color.rgb(255, 255, 255, 0.9));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.fillText("Scenario: " + currentScenario, 10, 25);
        }
    }

    /**
     * Draw a single agent
     */
    private void drawAgent(DemoAgent agent) {
        double x = agent.getX();
        double y = agent.getY();
        double size = DemoAgent.getSize();
        double heading = agent.getHeading();

        // Get color based on state
        Color color = agent.getState().color;

        // Apply network quality effect
        if (networkQuality < 0.5) {
            agent.setState(DemoAgent.AgentState.NETWORK_ISSUE);
            color = agent.getState().color;
        }

        // Draw agent body (circle)
        gc.setFill(color);
        gc.fillOval(x - size/2, y - size/2, size, size);

        // Draw heading indicator (triangle)
        double tipX = x + Math.cos(heading) * size * 1.5;
        double tipY = y + Math.sin(heading) * size * 1.5;

        gc.setStroke(color.brighter());
        gc.setLineWidth(2);
        gc.strokeLine(x, y, tipX, tipY);

        // Draw communication radius (faint)
        if (agents.size() < 10) { // Only show for small swarms
            gc.setStroke(Color.rgb(100, 100, 100, 0.1));
            gc.setLineWidth(1);
            double radius = DemoAgent.getCommunicationRadius();
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    /**
     * Update FPS counter
     */
    private void updateFPS(long now) {
        frameCount++;
        if (now - fpsUpdateTime >= 1_000_000_000) { // Update every second
            currentFPS = frameCount;
            fpsLabel.setText("FPS: " + (int) currentFPS);
            frameCount = 0;
            fpsUpdateTime = now;
        }
    }

    /**
     * Update network health display
     */
    private void updateNetworkHealthDisplay() {
        int percentage = (int) (networkQuality * 100);
        networkHealthLabel.setText("Network: " +
            (percentage > 80 ? "Healthy" : percentage > 50 ? "Degraded" : "Poor") +
            " (" + percentage + "%)");

        Color color = percentage > 80 ? Color.LIME :
                     percentage > 50 ? Color.ORANGE : Color.RED;
        networkHealthLabel.setTextFill(color);
    }

    // ==================== Agent Management ====================

    private void spawnInitialAgents(int count) {
        for (int i = 0; i < count; i++) {
            spawnAgent();
        }
    }

    private void spawnAgent() {
        double x = 100 + random.nextDouble() * (CANVAS_WIDTH - 200);
        double y = 100 + random.nextDouble() * (CANVAS_HEIGHT - 200);
        agents.add(new DemoAgent("Agent-" + agentIdCounter++, x, y));
    }

    private void removeAgent() {
        if (!agents.isEmpty()) {
            agents.remove(agents.size() - 1);
        }
    }

    // ==================== Formations ====================

    private void formationLine() {
        double startX = 100;
        double y = CANVAS_HEIGHT / 2.0;
        double spacing = 50;

        for (int i = 0; i < agents.size(); i++) {
            agents.get(i).moveToFormation(startX + i * spacing, y);
        }
        currentScenario = "Line Formation";
    }

    private void formationV() {
        double centerX = CANVAS_WIDTH / 2.0;
        double startY = 200;
        double spacing = 40;

        for (int i = 0; i < agents.size(); i++) {
            double offsetX = (i / 2) * spacing * (i % 2 == 0 ? 1 : -1);
            double offsetY = (i / 2) * spacing;
            agents.get(i).moveToFormation(centerX + offsetX, startY + offsetY);
        }
        currentScenario = "V-Formation";
    }

    private void formationCircle() {
        double centerX = CANVAS_WIDTH / 2.0;
        double centerY = CANVAS_HEIGHT / 2.0;
        double radius = 150;

        for (int i = 0; i < agents.size(); i++) {
            double angle = (2 * Math.PI * i) / agents.size();
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            agents.get(i).moveToFormation(x, y);
        }
        currentScenario = "Circle Formation";
    }

    private void formationGrid() {
        int cols = (int) Math.ceil(Math.sqrt(agents.size()));
        double spacing = 60;
        double startX = CANVAS_WIDTH / 2.0 - (cols * spacing) / 2.0;
        double startY = CANVAS_HEIGHT / 2.0 - (cols * spacing) / 2.0;

        for (int i = 0; i < agents.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            agents.get(i).moveToFormation(startX + col * spacing, startY + row * spacing);
        }
        currentScenario = "Grid Formation";
    }

    // ==================== Scenarios ====================

    /**
     * Scenario A: Basic Flocking - Demonstrate emergent behavior
     */
    private void runScenarioA() {
        currentScenario = "A: Basic Flocking";

        // Reset all agents
        for (DemoAgent agent : agents) {
            agent.clearTarget();
            agent.setState(DemoAgent.AgentState.ACTIVE);
            agent.setSeparationWeight(1.5);
            agent.setAlignmentWeight(1.0);
            agent.setCohesionWeight(1.0);
        }

        networkQuality = 1.0;
        updateNetworkHealthDisplay();
        consensusLabel.setText("Consensus: Idle - Watch emergent behavior!");
    }

    /**
     * Scenario B: Consensus Voting - Simulate distributed decision making
     */
    private void runScenarioB() {
        currentScenario = "B: Consensus Voting";

        // Start voting
        votingInProgress = true;
        votingStartTime = 0;
        votingOption1 = 0;
        votingOption2 = 0;

        // Each agent votes randomly
        for (DemoAgent agent : agents) {
            agent.setVoting(true);
            int vote = random.nextInt(2);
            agent.setVoteChoice(vote);

            if (vote == 0) votingOption1++;
            else votingOption2++;
        }

        consensusLabel.setText("Voting: Started");
    }

    /**
     * Scenario C: Network Degradation - Show adaptation to communication issues
     */
    private void runScenarioC() {
        currentScenario = "C: Network Degradation";

        // Gradually degrade network
        new Thread(() -> {
            try {
                for (int i = 10; i >= 3; i--) {
                    final double quality = i / 10.0;
                    javafx.application.Platform.runLater(() -> {
                        networkQuality = quality;
                        updateNetworkHealthDisplay();
                    });
                    Thread.sleep(500);
                }

                Thread.sleep(2000);

                // Restore network
                for (int i = 3; i <= 10; i++) {
                    final double quality = i / 10.0;
                    javafx.application.Platform.runLater(() -> {
                        networkQuality = quality;
                        updateNetworkHealthDisplay();
                    });
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        consensusLabel.setText("Consensus: Testing resilience...");
    }

    /**
     * Scenario D: Formation Flying - Demonstrate coordinated movement
     */
    private void runScenarioD() {
        currentScenario = "D: Formation Flying";

        // Sequence through formations
        new Thread(() -> {
            try {
                javafx.application.Platform.runLater(this::formationLine);
                Thread.sleep(3000);

                javafx.application.Platform.runLater(this::formationV);
                Thread.sleep(3000);

                javafx.application.Platform.runLater(this::formationCircle);
                Thread.sleep(3000);

                javafx.application.Platform.runLater(this::formationGrid);
                Thread.sleep(3000);

                javafx.application.Platform.runLater(() -> {
                    currentScenario = "D: Formation Flying - Complete";
                    for (DemoAgent agent : agents) {
                        agent.clearTarget();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        consensusLabel.setText("Consensus: Coordinated movement active");
    }

    // ==================== Utilities ====================

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    @FunctionalInterface
    interface SliderCallback {
        void onChange(double value);
    }

    // ==================== Main ====================

    public static void main(String[] args) {
        launch(args);
    }
}
