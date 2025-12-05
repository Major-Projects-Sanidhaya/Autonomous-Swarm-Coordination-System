package com.team6.demo;

import com.team6.demo.scenarios.*;

import java.util.Scanner;

/**
 * DemoMain - Entry point for ASCS Demo
 * Allows user to select and run different scenarios
 */
public class DemoMain {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  AUTONOMOUS SWARM COORDINATION SYSTEM (ASCS)              ║");
        System.out.println("║  Demo Application                                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Select scenario
        Scenario scenario = selectScenario(args);

        if (scenario == null) {
            System.out.println("No scenario selected. Exiting.");
            return;
        }

        // Setup
        scenario.setup();

        // Run simulation
        scenario.run();

        // Print final report
        scenario.printReport();

        System.out.println();
        System.out.println("Demo complete.");
    }

    private static Scenario selectScenario(String[] args) {
        // Check for command-line argument
        if (args.length > 0) {
            try {
                int choice = Integer.parseInt(args[0]);
                return createScenario(choice);
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument. Expected scenario number (1-5).");
            }
        }

        // Interactive menu
        System.out.println("Available Scenarios:");
        System.out.println("  1. Search and Rescue Mission");
        System.out.println("     - 3 drones search for 2 survivors");
        System.out.println("     - Navigate around buildings and moving obstacles");
        System.out.println();
        System.out.println("  2. Perimeter Patrol with Intruder Detection");
        System.out.println("     - 4 drones patrol facility perimeter");
        System.out.println("     - Detect and pursue intruder");
        System.out.println();
        System.out.println("  3. Precision Payload Delivery");
        System.out.println("     - 3 drones deliver packages from warehouse");
        System.out.println("     - Navigate grid of buildings");
        System.out.println();
        System.out.println("  4. Agricultural Field Survey");
        System.out.println("     - 4 drones survey field in formation");
        System.out.println("     - Lawn-mower pattern coverage");
        System.out.println();
        System.out.println("  5. Emergency Response Coordination");
        System.out.println("     - 6 drones (3 teams) coordinate response");
        System.out.println("     - Dynamic failure handling");
        System.out.println();
        System.out.print("Select scenario (1-5): ");

        try (Scanner scanner = new Scanner(System.in)) {
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                return createScenario(choice);
            }
        } catch (Exception e) {
            System.out.println("Error reading input: " + e.getMessage());
        }

        return null;
    }

    private static Scenario createScenario(int choice) {
        switch (choice) {
            case 1:
                return new SearchAndRescueScenario();
            case 2:
                return new PerimeterPatrolScenario();
            case 3:
                return new PayloadDeliveryScenario();
            case 4:
                return new AgriculturalSurveyScenario();
            case 5:
                return new EmergencyResponseScenario();
            default:
                System.out.println("Invalid choice. Please select 1-5.");
                return null;
        }
    }
}
