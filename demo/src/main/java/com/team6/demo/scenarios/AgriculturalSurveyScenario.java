package com.team6.demo.scenarios;

import com.team6.demo.core.Environment;
import com.team6.demo.core.Position;
import com.team6.demo.obstacles.*;
import com.team6.demo.tasks.*;

import java.util.*;

/**
 * AgriculturalSurveyScenario - Drones survey field in formation
 *
 * SETUP:
 * - Environment: 800m x 400m field
 * - Drones: 4 in line formation
 * - Obstacles: 1 linear irrigation line, 1 moving tractor
 *
 * SUCCESS CRITERIA:
 * - 100% field coverage
 * - Formation maintained
 * - Zero collisions
 */
public class AgriculturalSurveyScenario implements Scenario {
    private Environment environment;
    private List<Integer> droneIds;
    private List<AreaSurveyTask> surveyTasks;

    private double simulationDuration;
    private int collisionCount;

    // Constants
    private static final double FIELD_WIDTH = 800.0;
    private static final double FIELD_LENGTH = 400.0;
    private static final double MAX_ALTITUDE = 100.0;
    private static final double TICK_RATE = 10.0;
    private static final double DELTA_TIME = 1.0 / TICK_RATE;
    private static final double SURVEY_ALTITUDE = 20.0;
    private static final double DRONE_SPEED = 10.0; // m/s (steady survey speed)
    private static final double SWEEP_SPACING = 50.0; // meters between passes
    private static final double MAX_DURATION = 300.0; // 5 minutes

    public AgriculturalSurveyScenario() {
        this.droneIds = new ArrayList<>();
        this.surveyTasks = new ArrayList<>();
        this.simulationDuration = 0.0;
        this.collisionCount = 0;
    }

    @Override
    public String getName() {
        return "Agricultural Field Survey";
    }

    @Override
    public void setup() {
        System.out.println("=== AGRICULTURAL FIELD SURVEY ===");
        System.out.println("Environment: " + FIELD_WIDTH + "m x " + FIELD_LENGTH + "m field");
        System.out.println("Drones: 4 (line formation)");
        System.out.println("Survey pattern: Lawn-mower");
        System.out.println("Obstacles: Irrigation line, moving tractor");
        System.out.println();

        // Create environment
        environment = new Environment(FIELD_WIDTH, FIELD_LENGTH, MAX_ALTITUDE);

        // Add obstacles
        setupObstacles();

        // Spawn drones in formation
        setupDrones();

        // Create survey tasks
        setupSurveyTasks();

        System.out.println("[T=0.0s] Survey operations started...");
    }

    private void setupObstacles() {
        ObstacleManager obstacleManager = environment.getObstacleManager();

        // Linear irrigation line at x=400 (center of field)
        // Represented as a thin building obstacle
        BuildingObstacle irrigation = new BuildingObstacle(395, 0, 405, FIELD_LENGTH, 5, "Irrigation-Line");
        obstacleManager.addObstacle(irrigation);

        // Moving tractor
        Position tractorStart = new Position(100, 50, 0);
        MovingObstacle tractor = new MovingObstacle(
            tractorStart,
            10.0,  // radius
            2.0,   // velocity X (slow)
            1.0,   // velocity Y
            0.0,   // velocity Z
            "Tractor"
        );
        obstacleManager.addObstacle(tractor);
    }

    private void setupDrones() {
        // 4 drones in line formation, evenly spaced
        double spacing = FIELD_WIDTH / 5.0; // Divide field into 5 sections for 4 drones

        for (int i = 0; i < 4; i++) {
            double startX = spacing * (i + 1);
            Position startPos = new Position(startX, 0, SURVEY_ALTITUDE);
            int droneId = environment.spawnDrone(startPos);
            droneIds.add(droneId);
        }
    }

    private void setupSurveyTasks() {
        // Each drone surveys its vertical strip of the field
        double stripWidth = FIELD_WIDTH / 4.0;

        for (int i = 0; i < 4; i++) {
            double minX = stripWidth * i;
            double maxX = stripWidth * (i + 1);

            Position areaMin = new Position(minX, 0, 0);
            Position areaMax = new Position(maxX, FIELD_LENGTH, 0);

            AreaSurveyTask task = new AreaSurveyTask(areaMin, areaMax, SWEEP_SPACING, SURVEY_ALTITUDE);
            task.assignToDrone(droneIds.get(i));
            surveyTasks.add(task);
        }
    }

    @Override
    public void run() {
        int tickCount = 0;
        int printInterval = (int)(15.0 * TICK_RATE); // Print every 15 seconds

        while (!isComplete() && simulationDuration < MAX_DURATION) {
            // Move drones
            moveDrones();

            // Check collisions
            checkCollisions();

            // Update environment
            environment.update(DELTA_TIME);
            simulationDuration += DELTA_TIME;

            // Print status
            if (tickCount % printInterval == 0) {
                printStatus();
            }

            tickCount++;
        }
    }

    private void moveDrones() {
        for (int i = 0; i < droneIds.size(); i++) {
            int droneId = droneIds.get(i);
            AreaSurveyTask task = surveyTasks.get(i);

            if (task.getStatus() == TaskStatus.COMPLETED) {
                continue;
            }

            Position currentPos = environment.getDronePosition(droneId);
            Position targetWaypoint = task.getCurrentWaypoint();

            // Move toward waypoint
            Position newPos = currentPos.moveTo(targetWaypoint, DRONE_SPEED, DELTA_TIME);

            // Obstacle avoidance
            if (environment.isValidPosition(newPos) && environment.isPathClear(currentPos, newPos)) {
                environment.updateDronePosition(droneId, newPos);
                task.updateDronePosition(newPos);
            } else {
                // Avoidance: climb slightly
                Position avoidPos = new Position(newPos.x, newPos.y, currentPos.z + 2);
                if (environment.isValidPosition(avoidPos)) {
                    environment.updateDronePosition(droneId, avoidPos);
                    task.updateDronePosition(avoidPos);
                }
            }

            // Update task
            task.execute(DELTA_TIME);
        }
    }

    private void checkCollisions() {
        for (int droneId : droneIds) {
            Position dronePos = environment.getDronePosition(droneId);
            if (!environment.isValidPosition(dronePos)) {
                collisionCount++;
            }
        }
    }

    private void printStatus() {
        System.out.printf("[T=%.1fs] ", simulationDuration);

        int totalCoverage = 0;
        for (int i = 0; i < droneIds.size(); i++) {
            AreaSurveyTask task = surveyTasks.get(i);
            int coverage = task.getCoveragePercent();
            totalCoverage += coverage;
            System.out.printf("D%d:%d%% | ", droneIds.get(i), coverage);
        }

        int avgCoverage = totalCoverage / droneIds.size();
        System.out.printf("Avg Coverage: %d%%%n", avgCoverage);
    }

    @Override
    public boolean isComplete() {
        // Check if all drones completed their survey
        for (AreaSurveyTask task : surveyTasks) {
            if (task.getStatus() != TaskStatus.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void printReport() {
        System.out.println();
        System.out.println("=== FIELD SURVEY COMPLETE ===");
        System.out.printf("Duration: %.1f seconds%n", simulationDuration);

        int totalCoverage = 0;
        for (int i = 0; i < surveyTasks.size(); i++) {
            AreaSurveyTask task = surveyTasks.get(i);
            int coverage = task.getCoveragePercent();
            totalCoverage += coverage;
            System.out.printf("Drone-%d coverage: %d%%%n", droneIds.get(i), coverage);
        }

        int avgCoverage = totalCoverage / surveyTasks.size();
        System.out.printf("Average coverage: %d%%%n", avgCoverage);
        System.out.printf("Collisions: %d%n", collisionCount);

        boolean success = avgCoverage == 100 &&
                         collisionCount == 0 &&
                         simulationDuration <= MAX_DURATION;

        System.out.println("Status: " + (success ? "SUCCESS" : "FAILED"));
    }

    // ========== VISUALIZATION SUPPORT ==========

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public double getSimulationTime() {
        return simulationDuration;
    }

    @Override
    public void update(double deltaTime) {
        moveDrones();
        checkCollisions();
        environment.update(deltaTime);
        simulationDuration += deltaTime;
    }

    @Override
    public String getStatusInfo() {
        int totalCoverage = 0;
        for (AreaSurveyTask task : surveyTasks) {
            totalCoverage += task.getCoveragePercent();
        }
        int avgCoverage = totalCoverage / surveyTasks.size();
        return String.format("Coverage: %d%%", avgCoverage);
    }
}
