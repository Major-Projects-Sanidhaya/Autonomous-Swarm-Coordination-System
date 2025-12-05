package com.team6.demo.scenarios;

import com.team6.demo.core.Environment;
import com.team6.demo.core.Position;
import com.team6.demo.obstacles.*;
import com.team6.demo.tasks.*;

import java.util.*;

/**
 * SearchAndRescueScenario - Drones search for survivors while avoiding obstacles
 *
 * SETUP:
 * - Environment: 500m x 500m
 * - Drones: 3
 * - Survivors: 2 (at fixed locations)
 * - Obstacles: 2 buildings, 1 moving vehicle
 *
 * SUCCESS CRITERIA:
 * - All survivors found within 120 seconds
 * - Zero collisions
 */
public class SearchAndRescueScenario implements Scenario {
    private Environment environment;
    private List<Integer> droneIds;
    private List<Position> survivorLocations;
    private Set<Integer> foundSurvivors;
    private List<WaypointTask> searchTasks;

    private double simulationDuration;
    private int collisionCount;
    private boolean complete;

    // Constants
    private static final double WORLD_SIZE = 500.0;
    private static final double MAX_ALTITUDE = 100.0;
    private static final double DETECTION_RADIUS = 30.0;
    private static final double MAX_DURATION = 120.0; // seconds
    private static final double TICK_RATE = 10.0; // 10 ticks per second
    private static final double DELTA_TIME = 1.0 / TICK_RATE;
    private static final double DRONE_SPEED = 15.0; // m/s
    private static final double DRONE_ALTITUDE = 10.0; // m

    public SearchAndRescueScenario() {
        this.droneIds = new ArrayList<>();
        this.survivorLocations = new ArrayList<>();
        this.foundSurvivors = new HashSet<>();
        this.searchTasks = new ArrayList<>();
        this.simulationDuration = 0.0;
        this.collisionCount = 0;
        this.complete = false;
    }

    @Override
    public String getName() {
        return "Search and Rescue Mission";
    }

    @Override
    public void setup() {
        System.out.println("=== SEARCH AND RESCUE SCENARIO ===");
        System.out.println("Environment: " + WORLD_SIZE + "m x " + WORLD_SIZE + "m");
        System.out.println("Drones: 3");
        System.out.println("Survivors: 2");
        System.out.println("Obstacles: 2 buildings, 1 moving vehicle");
        System.out.println();

        // Create environment
        environment = new Environment(WORLD_SIZE, WORLD_SIZE, MAX_ALTITUDE);

        // Add obstacles
        setupObstacles();

        // Spawn drones
        setupDrones();

        // Place survivors
        setupSurvivors();

        // Create search tasks
        setupSearchTasks();

        System.out.println("[T=0.0s] Starting simulation...");
    }

    private void setupObstacles() {
        ObstacleManager obstacleManager = environment.getObstacleManager();

        // Building 1: corners (100,100) to (180,180), height 40m
        BuildingObstacle building1 = new BuildingObstacle(100, 100, 180, 180, 40, "Building-1");
        obstacleManager.addObstacle(building1);

        // Building 2: corners (300,250) to (400,350), height 35m
        BuildingObstacle building2 = new BuildingObstacle(300, 250, 400, 350, 35, "Building-2");
        obstacleManager.addObstacle(building2);

        // Moving vehicle: starts (0, 250), moves east at 5 m/s, radius 15m
        MovingObstacle vehicle = new MovingObstacle(
            new Position(0, 250, 0),
            15.0,  // radius
            5.0,   // velocity X (east)
            0.0,   // velocity Y
            0.0,   // velocity Z
            "Vehicle-1"
        );
        obstacleManager.addObstacle(vehicle);
    }

    private void setupDrones() {
        // Spawn 3 drones at origin, at flight altitude
        for (int i = 0; i < 3; i++) {
            Position startPos = new Position(0, 0, DRONE_ALTITUDE);
            int droneId = environment.spawnDrone(startPos);
            droneIds.add(droneId);
        }
    }

    private void setupSurvivors() {
        // Survivor 1: (250, 400)
        survivorLocations.add(new Position(250, 400, 0));

        // Survivor 2: (450, 100)
        survivorLocations.add(new Position(450, 100, 0));
    }

    private void setupSearchTasks() {
        // Create waypoint tasks for different search areas
        // Each drone gets a different search zone
        Position zone1 = new Position(150, 150, DRONE_ALTITUDE);
        Position zone2 = new Position(250, 400, DRONE_ALTITUDE);
        Position zone3 = new Position(450, 100, DRONE_ALTITUDE);

        WaypointTask task1 = new WaypointTask(zone1, 50.0);
        WaypointTask task2 = new WaypointTask(zone2, 50.0);
        WaypointTask task3 = new WaypointTask(zone3, 50.0);

        task1.assignToDrone(droneIds.get(0));
        task2.assignToDrone(droneIds.get(1));
        task3.assignToDrone(droneIds.get(2));

        searchTasks.add(task1);
        searchTasks.add(task2);
        searchTasks.add(task3);
    }

    @Override
    public void run() {
        int tickCount = 0;
        int printInterval = (int)(5.0 * TICK_RATE); // Print every 5 seconds

        while (!isComplete() && simulationDuration < MAX_DURATION) {
            // Move drones toward their targets
            moveDrones();

            // Check for survivor detection
            checkSurvivorDetection();

            // Check for collisions
            checkCollisions();

            // Update environment
            environment.update(DELTA_TIME);
            simulationDuration += DELTA_TIME;

            // Print status every 5 seconds
            if (tickCount % printInterval == 0) {
                printStatus();
            }

            tickCount++;
        }

        // Mark as complete
        complete = true;
    }

    private void moveDrones() {
        for (int i = 0; i < droneIds.size(); i++) {
            int droneId = droneIds.get(i);
            WaypointTask task = searchTasks.get(i);

            Position currentPos = environment.getDronePosition(droneId);
            Position targetPos = task.getTargetPosition();

            // Simple movement toward target
            Position newPos = currentPos.moveTo(targetPos, DRONE_SPEED, DELTA_TIME);

            // Check for obstacle avoidance
            if (environment.isValidPosition(newPos) && environment.isPathClear(currentPos, newPos)) {
                environment.updateDronePosition(droneId, newPos);
                task.updateDronePosition(newPos);
            } else {
                // Try multiple avoidance strategies
                boolean moved = false;

                // Strategy 1: Move up
                Position avoidUp = new Position(newPos.x, newPos.y, currentPos.z + 3);
                if (!moved && environment.isValidPosition(avoidUp)) {
                    environment.updateDronePosition(droneId, avoidUp);
                    task.updateDronePosition(avoidUp);
                    moved = true;
                }

                // Strategy 2: Move around (perpendicular to target direction)
                if (!moved) {
                    double dx = targetPos.x - currentPos.x;
                    double dy = targetPos.y - currentPos.y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > 0.1) {
                        // Perpendicular direction
                        double perpX = -dy / dist * DRONE_SPEED * DELTA_TIME;
                        double perpY = dx / dist * DRONE_SPEED * DELTA_TIME;
                        Position avoidSide = new Position(currentPos.x + perpX, currentPos.y + perpY, currentPos.z);
                        if (environment.isValidPosition(avoidSide)) {
                            environment.updateDronePosition(droneId, avoidSide);
                            task.updateDronePosition(avoidSide);
                            moved = true;
                        }
                    }
                }

                if (moved) {
                    System.out.printf("[T=%.1fs] Drone-%d avoided obstacle%n",
                        simulationDuration, droneId);
                }
            }

            // Update task status
            task.execute(DELTA_TIME);
        }
    }

    private void checkSurvivorDetection() {
        for (int i = 0; i < survivorLocations.size(); i++) {
            if (foundSurvivors.contains(i)) {
                continue; // Already found
            }

            Position survivorPos = survivorLocations.get(i);

            // Check each drone
            for (int droneId : droneIds) {
                Position dronePos = environment.getDronePosition(droneId);
                double distance = dronePos.horizontalDistanceTo(survivorPos);

                if (distance <= DETECTION_RADIUS) {
                    foundSurvivors.add(i);
                    System.out.printf("[T=%.1fs] Drone-%d FOUND Survivor-%d at %s!%n",
                        simulationDuration, droneId, i + 1, survivorPos);
                    break;
                }
            }
        }
    }

    private void checkCollisions() {
        for (int droneId : droneIds) {
            Position dronePos = environment.getDronePosition(droneId);

            // Check obstacle collisions
            if (!environment.isValidPosition(dronePos)) {
                collisionCount++;
            }
        }
    }

    private void printStatus() {
        System.out.printf("[T=%.1fs] ", simulationDuration);
        for (int droneId : droneIds) {
            Position pos = environment.getDronePosition(droneId);
            System.out.printf("Drone-%d at %s -> searching | ", droneId, pos);
        }
        System.out.println();
    }

    @Override
    public boolean isComplete() {
        return foundSurvivors.size() == survivorLocations.size() ||
               simulationDuration >= MAX_DURATION;
    }

    @Override
    public void printReport() {
        System.out.println();
        System.out.println("=== MISSION COMPLETE ===");
        System.out.printf("Time: %.1f seconds%n", simulationDuration);
        System.out.printf("Survivors found: %d/%d%n", foundSurvivors.size(), survivorLocations.size());
        System.out.printf("Collisions: %d%n", collisionCount);

        boolean success = foundSurvivors.size() == survivorLocations.size() &&
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
        // Single time step for visual mode
        moveDrones();
        checkSurvivorDetection();
        checkCollisions();
        environment.update(deltaTime);
        simulationDuration += deltaTime;
    }

    @Override
    public String getStatusInfo() {
        return String.format("%d/%d survivors found", foundSurvivors.size(), survivorLocations.size());
    }
}
