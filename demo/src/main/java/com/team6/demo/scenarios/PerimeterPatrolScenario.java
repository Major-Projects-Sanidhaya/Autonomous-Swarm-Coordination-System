package com.team6.demo.scenarios;

import com.team6.demo.core.Environment;
import com.team6.demo.core.Position;
import com.team6.demo.core.Intruder;
import com.team6.demo.obstacles.*;

import java.util.*;

/**
 * PerimeterPatrolScenario - Drones patrol facility perimeter and detect intruders
 *
 * SETUP:
 * - Environment: 400m x 400m square facility
 * - Drones: 4 (one per side)
 * - Central no-fly zone: circle at (200,200) with 80m radius
 * - Intruder appears at T=30s
 *
 * SUCCESS CRITERIA:
 * - Intruder detected within 10 seconds of appearance
 * - Continuous patrol coverage
 */
public class PerimeterPatrolScenario implements Scenario {
    private Environment environment;
    private List<Integer> droneIds;
    private Map<Integer, PatrolEdge> dronePatrolAssignments;
    private Intruder intruder;
    private boolean intruderSpawned;
    private double intruderSpawnTime;

    private double simulationDuration;
    private int collisionCount;

    // Constants
    private static final double WORLD_SIZE = 400.0;
    private static final double MAX_ALTITUDE = 100.0;
    private static final double TICK_RATE = 10.0;
    private static final double DELTA_TIME = 1.0 / TICK_RATE;
    private static final double DRONE_SPEED = 12.0; // m/s
    private static final double DRONE_ALTITUDE = 15.0; // m
    private static final double DETECTION_RADIUS = 50.0; // m
    private static final double INTRUDER_SPAWN_TIME = 30.0; // seconds
    private static final double SIMULATION_DURATION = 90.0; // seconds

    // Patrol edges
    private enum EdgeType { NORTH, EAST, SOUTH, WEST }

    private static class PatrolEdge {
        EdgeType edge;
        Position start;
        Position end;
        boolean forward; // Direction of patrol

        PatrolEdge(EdgeType edge, Position start, Position end) {
            this.edge = edge;
            this.start = start;
            this.end = end;
            this.forward = true;
        }

        Position getCurrentTarget(Position dronePos) {
            // If close to target, switch direction
            Position target = forward ? end : start;
            if (dronePos.isNear(target, 10.0)) {
                forward = !forward;
                target = forward ? end : start;
            }
            return target;
        }
    }

    public PerimeterPatrolScenario() {
        this.droneIds = new ArrayList<>();
        this.dronePatrolAssignments = new HashMap<>();
        this.simulationDuration = 0.0;
        this.collisionCount = 0;
        this.intruderSpawned = false;
        this.intruderSpawnTime = INTRUDER_SPAWN_TIME;
    }

    @Override
    public String getName() {
        return "Perimeter Patrol with Intruder Detection";
    }

    @Override
    public void setup() {
        System.out.println("=== PERIMETER PATROL SCENARIO ===");
        System.out.println("Environment: " + WORLD_SIZE + "m x " + WORLD_SIZE + "m facility");
        System.out.println("Drones: 4 (perimeter patrol)");
        System.out.println("Central no-fly zone: radius 80m");
        System.out.println("Intruder spawns at T=" + INTRUDER_SPAWN_TIME + "s");
        System.out.println();

        // Create environment
        environment = new Environment(WORLD_SIZE, WORLD_SIZE, MAX_ALTITUDE);

        // Add central no-fly zone
        setupNoFlyZone();

        // Spawn patrol drones
        setupPatrolDrones();

        System.out.println("[T=0.0s] Patrol initiated...");
    }

    private void setupNoFlyZone() {
        Position center = new Position(WORLD_SIZE / 2, WORLD_SIZE / 2, 0);
        NoFlyZone centralZone = new NoFlyZone(center, 80.0, "Central-Building");
        environment.getObstacleManager().addObstacle(centralZone);
    }

    private void setupPatrolDrones() {
        // Drone 1: North edge (y=380), patrols east-west
        Position north1 = new Position(50, 380, DRONE_ALTITUDE);
        Position north2 = new Position(350, 380, DRONE_ALTITUDE);
        int drone1 = environment.spawnDrone(north1);
        droneIds.add(drone1);
        dronePatrolAssignments.put(drone1, new PatrolEdge(EdgeType.NORTH, north1, north2));

        // Drone 2: East edge (x=380), patrols north-south
        Position east1 = new Position(380, 350, DRONE_ALTITUDE);
        Position east2 = new Position(380, 50, DRONE_ALTITUDE);
        int drone2 = environment.spawnDrone(east1);
        droneIds.add(drone2);
        dronePatrolAssignments.put(drone2, new PatrolEdge(EdgeType.EAST, east1, east2));

        // Drone 3: South edge (y=20), patrols west-east
        Position south1 = new Position(350, 20, DRONE_ALTITUDE);
        Position south2 = new Position(50, 20, DRONE_ALTITUDE);
        int drone3 = environment.spawnDrone(south1);
        droneIds.add(drone3);
        dronePatrolAssignments.put(drone3, new PatrolEdge(EdgeType.SOUTH, south1, south2));

        // Drone 4: West edge (x=20), patrols south-north
        Position west1 = new Position(20, 50, DRONE_ALTITUDE);
        Position west2 = new Position(20, 350, DRONE_ALTITUDE);
        int drone4 = environment.spawnDrone(west1);
        droneIds.add(drone4);
        dronePatrolAssignments.put(drone4, new PatrolEdge(EdgeType.WEST, west1, west2));
    }

    @Override
    public void run() {
        int tickCount = 0;
        int printInterval = (int)(5.0 * TICK_RATE); // Print every 5 seconds

        while (!isComplete() && simulationDuration < SIMULATION_DURATION) {
            // Spawn intruder at specified time
            if (!intruderSpawned && simulationDuration >= intruderSpawnTime) {
                spawnIntruder();
            }

            // Move patrol drones
            patrolDrones();

            // Update intruder if spawned
            if (intruderSpawned && intruder != null) {
                intruder.update(DELTA_TIME);
                checkIntruderDetection();
            }

            // Update environment
            environment.update(DELTA_TIME);
            simulationDuration += DELTA_TIME;

            // Print status every 5 seconds
            if (tickCount % printInterval == 0) {
                printStatus();
            }

            tickCount++;
        }
    }

    private void spawnIntruder() {
        // Intruder starts at edge, moves toward center
        Position startPos = new Position(400, 200, 0);
        Position targetPos = new Position(200, 200, 0);
        intruder = new Intruder(startPos, targetPos, 3.0);
        intruderSpawned = true;
        System.out.printf("[T=%.1fs] ALERT: Intruder detected at perimeter %s!%n",
            simulationDuration, startPos);
    }

    private void patrolDrones() {
        for (int droneId : droneIds) {
            Position currentPos = environment.getDronePosition(droneId);
            PatrolEdge patrol = dronePatrolAssignments.get(droneId);

            // Get current patrol target
            Position target = patrol.getCurrentTarget(currentPos);

            // Move toward target
            Position newPos = currentPos.moveTo(target, DRONE_SPEED, DELTA_TIME);

            // Check for valid position
            if (environment.isValidPosition(newPos) && environment.isPathClear(currentPos, newPos)) {
                environment.updateDronePosition(droneId, newPos);
            } else {
                // If hitting no-fly zone, stay at current position
                // (patrol routes should avoid it)
            }
        }
    }

    private void checkIntruderDetection() {
        if (intruder == null || intruder.isDetected()) {
            return;
        }

        Position intruderPos = intruder.getPosition();

        for (int droneId : droneIds) {
            Position dronePos = environment.getDronePosition(droneId);
            double distance = dronePos.horizontalDistanceTo(intruderPos);

            if (distance <= DETECTION_RADIUS) {
                intruder.markDetected(droneId);
                System.out.printf("[T=%.1fs] Drone-%d DETECTED intruder at %s (distance: %.1fm)!%n",
                    simulationDuration, droneId, intruderPos, distance);
                System.out.printf("[T=%.1fs] Drone-%d pursuing intruder...%n",
                    simulationDuration, droneId);

                // Update drone to follow intruder
                PatrolEdge pursuit = new PatrolEdge(
                    EdgeType.NORTH, // Doesn't matter for pursuit
                    intruderPos,
                    intruder.getTargetPosition()
                );
                dronePatrolAssignments.put(droneId, pursuit);
                break;
            }
        }
    }

    private void printStatus() {
        System.out.printf("[T=%.1fs] ", simulationDuration);
        for (int i = 0; i < droneIds.size(); i++) {
            int droneId = droneIds.get(i);
            Position pos = environment.getDronePosition(droneId);
            PatrolEdge patrol = dronePatrolAssignments.get(droneId);
            String status = (intruder != null && intruder.isDetected() && intruder.getDetectedByDroneId() == droneId)
                ? "pursuing"
                : "patrolling " + patrol.edge;
            System.out.printf("D%d:%s | ", droneId, status);
        }
        if (intruderSpawned && intruder != null) {
            System.out.printf("Intruder:%s", intruder.getPosition());
        }
        System.out.println();
    }

    @Override
    public boolean isComplete() {
        // Scenario completes when intruder reaches target or is intercepted
        if (intruder != null && intruder.hasReachedTarget()) {
            return true;
        }
        return simulationDuration >= SIMULATION_DURATION;
    }

    @Override
    public void printReport() {
        System.out.println();
        System.out.println("=== PATROL COMPLETE ===");
        System.out.printf("Duration: %.1f seconds%n", simulationDuration);

        if (intruder != null) {
            System.out.printf("Intruder spawned at: T=%.1fs%n", intruderSpawnTime);
            if (intruder.isDetected()) {
                System.out.printf("Intruder detected: YES (by Drone-%d)%n", intruder.getDetectedByDroneId());
                System.out.println("Response: Immediate pursuit initiated");
            } else {
                System.out.println("Intruder detected: NO");
            }

            if (intruder.hasReachedTarget()) {
                System.out.println("Intruder reached target: YES (BREACH!)");
            } else {
                System.out.println("Intruder reached target: NO");
            }
        }

        System.out.printf("Collisions: %d%n", collisionCount);

        boolean success = (intruder == null || intruder.isDetected()) && collisionCount == 0;
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
        if (!intruderSpawned && simulationDuration >= INTRUDER_SPAWN_TIME) {
            spawnIntruder();
        }

        patrolDrones();

        if (intruder != null) {
            intruder.update(deltaTime);
            checkIntruderDetection();
        }

        environment.update(deltaTime);
        simulationDuration += deltaTime;
    }

    @Override
    public String getStatusInfo() {
        if (intruder != null && intruder.isDetected()) {
            return String.format("Intruder detected by D%d!", intruder.getDetectedByDroneId());
        } else if (intruderSpawned) {
            return "Intruder active - searching";
        }
        return "Patrolling";
    }
}
