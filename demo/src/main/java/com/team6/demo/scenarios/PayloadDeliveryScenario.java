package com.team6.demo.scenarios;

import com.team6.demo.core.Environment;
import com.team6.demo.core.Position;
import com.team6.demo.obstacles.*;
import com.team6.demo.tasks.*;

import java.util.*;

/**
 * PayloadDeliveryScenario - Drones pick up and deliver payloads
 *
 * SETUP:
 * - Environment: 600m x 600m
 * - Drones: 3
 * - Warehouse: (300, 0)
 * - Delivery points: 3
 * - Obstacles: 4 buildings in grid pattern
 *
 * SUCCESS CRITERIA:
 * - All deliveries completed within time limit
 * - Zero collisions
 */
public class PayloadDeliveryScenario implements Scenario {
    private Environment environment;
    private List<Integer> droneIds;
    private List<PayloadDeliveryTask> deliveryTasks;
    private int completedDeliveries;

    private double simulationDuration;
    private int collisionCount;

    // Constants
    private static final double WORLD_SIZE = 600.0;
    private static final double MAX_ALTITUDE = 100.0;
    private static final double TICK_RATE = 10.0;
    private static final double DELTA_TIME = 1.0 / TICK_RATE;
    private static final double DRONE_ALTITUDE = 15.0;
    private static final double MAX_DURATION = 300.0; // Increased to 5 minutes for longer delivery routes

    public PayloadDeliveryScenario() {
        this.droneIds = new ArrayList<>();
        this.deliveryTasks = new ArrayList<>();
        this.completedDeliveries = 0;
        this.simulationDuration = 0.0;
        this.collisionCount = 0;
    }

    @Override
    public String getName() {
        return "Precision Payload Delivery";
    }

    @Override
    public void setup() {
        System.out.println("=== PAYLOAD DELIVERY SCENARIO ===");
        System.out.println("Environment: " + WORLD_SIZE + "m x " + WORLD_SIZE + "m");
        System.out.println("Drones: 3");
        System.out.println("Warehouse: (300, 0)");
        System.out.println("Delivery points: 3");
        System.out.println("Obstacles: 4 buildings in grid");
        System.out.println();

        // Create environment
        environment = new Environment(WORLD_SIZE, WORLD_SIZE, MAX_ALTITUDE);

        // Add grid of building obstacles
        setupObstacles();

        // Spawn drones at warehouse
        setupDrones();

        // Create delivery tasks
        setupDeliveryTasks();

        System.out.println("[T=0.0s] Delivery operations initiated...");
    }

    private void setupObstacles() {
        ObstacleManager obstacleManager = environment.getObstacleManager();

        // Grid of 4 buildings
        // Building 1: NW quadrant
        BuildingObstacle building1 = new BuildingObstacle(100, 350, 200, 450, 50, "Building-NW");
        obstacleManager.addObstacle(building1);

        // Building 2: NE quadrant
        BuildingObstacle building2 = new BuildingObstacle(400, 350, 500, 450, 45, "Building-NE");
        obstacleManager.addObstacle(building2);

        // Building 3: SW quadrant
        BuildingObstacle building3 = new BuildingObstacle(100, 150, 200, 250, 40, "Building-SW");
        obstacleManager.addObstacle(building3);

        // Building 4: Center
        BuildingObstacle building4 = new BuildingObstacle(250, 250, 350, 350, 55, "Building-Center");
        obstacleManager.addObstacle(building4);
    }

    private void setupDrones() {
        // Warehouse at (300, 0)
        Position warehouse = new Position(300, 0, DRONE_ALTITUDE);

        for (int i = 0; i < 3; i++) {
            int droneId = environment.spawnDrone(warehouse);
            droneIds.add(droneId);
        }
    }

    private void setupDeliveryTasks() {
        Position warehouse = new Position(300, 0, 0); // Ground level for pickup

        // Delivery point 1: (100, 400) - northwest
        Position delivery1 = new Position(100, 400, 0);
        PayloadDeliveryTask task1 = new PayloadDeliveryTask(warehouse, delivery1);
        task1.assignToDrone(droneIds.get(0));
        deliveryTasks.add(task1);

        // Delivery point 2: (500, 300) - east
        Position delivery2 = new Position(500, 300, 0);
        PayloadDeliveryTask task2 = new PayloadDeliveryTask(warehouse, delivery2);
        task2.assignToDrone(droneIds.get(1));
        deliveryTasks.add(task2);

        // Delivery point 3: (150, 550) - moved west to avoid center building
        Position delivery3 = new Position(150, 550, 0);
        PayloadDeliveryTask task3 = new PayloadDeliveryTask(warehouse, delivery3);
        task3.assignToDrone(droneIds.get(2));
        deliveryTasks.add(task3);
    }

    @Override
    public void run() {
        int tickCount = 0;
        int printInterval = (int)(10.0 * TICK_RATE); // Print every 10 seconds

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
            PayloadDeliveryTask task = deliveryTasks.get(i);

            if (task.getStatus() == TaskStatus.COMPLETED) {
                continue; // Skip completed tasks
            }

            Position currentPos = environment.getDronePosition(droneId);
            Position target = task.getCurrentTarget();
            double speed = task.getCurrentSpeed();


            // Adjust target altitude for flight
            Position flightTarget = new Position(target.x, target.y, DRONE_ALTITUDE);

            // Move toward target
            Position newPos = currentPos.moveTo(flightTarget, speed, DELTA_TIME);

            // Obstacle avoidance with multiple strategies
            boolean moved = false;

            if (environment.isValidPosition(newPos) && environment.isPathClear(currentPos, newPos)) {
                environment.updateDronePosition(droneId, newPos);
                Position groundPos = new Position(newPos.x, newPos.y, target.z);
                task.updateDronePosition(groundPos);
                moved = true;
            } else {
                // Strategy 1: Try climbing to safe altitude above all buildings (60m)
                Position avoidUp = new Position(newPos.x, newPos.y, 60.0);
                if (environment.isValidPosition(avoidUp) && environment.isPathClear(currentPos, avoidUp)) {
                    environment.updateDronePosition(droneId, avoidUp);
                    Position groundPos = new Position(avoidUp.x, avoidUp.y, target.z);
                    task.updateDronePosition(groundPos);
                    moved = true;
                } else {
                    // If 60m fails, try incremental climb
                    avoidUp = new Position(newPos.x, newPos.y, currentPos.z + 10);
                    if (environment.isValidPosition(avoidUp) && environment.isPathClear(currentPos, avoidUp)) {
                        environment.updateDronePosition(droneId, avoidUp);
                        Position groundPos = new Position(avoidUp.x, avoidUp.y, target.z);
                        task.updateDronePosition(groundPos);
                        moved = true;
                    }
                }

                // Strategy 2: Try lateral movement (perpendicular to current direction)
                if (!moved) {
                    double dx = flightTarget.x - currentPos.x;
                    double dy = flightTarget.y - currentPos.y;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist > 0.1) {
                        // Move perpendicular to avoid obstacle
                        double perpX = -dy / dist * speed * DELTA_TIME;
                        double perpY = dx / dist * speed * DELTA_TIME;
                        Position lateral = new Position(currentPos.x + perpX, currentPos.y + perpY, currentPos.z);

                        if (environment.isValidPosition(lateral) && environment.isPathClear(currentPos, lateral)) {
                            environment.updateDronePosition(droneId, lateral);
                            Position groundPos = new Position(lateral.x, lateral.y, target.z);
                            task.updateDronePosition(groundPos);
                            moved = true;
                        }
                    }
                }

                // Strategy 3: If still stuck, try the opposite perpendicular direction
                if (!moved) {
                    double dx = flightTarget.x - currentPos.x;
                    double dy = flightTarget.y - currentPos.y;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist > 0.1) {
                        double perpX = dy / dist * speed * DELTA_TIME;
                        double perpY = -dx / dist * speed * DELTA_TIME;
                        Position lateral = new Position(currentPos.x + perpX, currentPos.y + perpY, currentPos.z);

                        if (environment.isValidPosition(lateral) && environment.isPathClear(currentPos, lateral)) {
                            environment.updateDronePosition(droneId, lateral);
                            Position groundPos = new Position(lateral.x, lateral.y, target.z);
                            task.updateDronePosition(groundPos);
                            moved = true;
                        }
                    }
                }
            }

            // Update task and check for completion
            if (task.execute(DELTA_TIME)) {
                if (task.getStatus() == TaskStatus.COMPLETED) {
                    completedDeliveries++;
                    String phase = task.hasPayload() ? "DELIVERED" : "PICKED UP";
                    System.out.printf("[T=%.1fs] Drone-%d %s payload at %s!%n",
                        simulationDuration, droneId, phase, task.getCurrentTarget());

                    if (task.hasPayload()) {
                        System.out.printf("[T=%.1fs] Delivery %d/%d complete!%n",
                            simulationDuration, completedDeliveries, deliveryTasks.size());
                    }
                }
            } else {
                // Check if we just picked up payload
                if (task.hasPayload() && !currentPos.isNear(task.getPickupLocation(), 20.0)) {
                    // Already reported pickup above
                }
            }
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
        for (int i = 0; i < droneIds.size(); i++) {
            int droneId = droneIds.get(i);
            PayloadDeliveryTask task = deliveryTasks.get(i);
            String status = task.getStatus() == TaskStatus.COMPLETED ? "complete" :
                           (task.hasPayload() ? "delivering" : "to pickup");
            System.out.printf("D%d:%s | ", droneId, status);
        }
        System.out.printf("Completed: %d/%d%n", completedDeliveries, deliveryTasks.size());
    }

    @Override
    public boolean isComplete() {
        return completedDeliveries == deliveryTasks.size();
    }

    @Override
    public void printReport() {
        System.out.println();
        System.out.println("=== DELIVERY OPERATIONS COMPLETE ===");
        System.out.printf("Duration: %.1f seconds%n", simulationDuration);
        System.out.printf("Deliveries completed: %d/%d%n", completedDeliveries, deliveryTasks.size());
        System.out.printf("Collisions: %d%n", collisionCount);

        boolean success = completedDeliveries == deliveryTasks.size() &&
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
        return String.format("Deliveries: %d/%d", completedDeliveries, deliveryTasks.size());
    }
}
