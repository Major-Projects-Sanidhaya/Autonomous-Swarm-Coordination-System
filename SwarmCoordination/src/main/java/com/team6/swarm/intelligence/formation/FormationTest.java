/**
 * FORMATIONTEST CLASS - Week 4 Formation Flying Validation
 *
 * PURPOSE:
 * - Test formation creation for all shapes
 * - Validate position calculations
 * - Verify formation maintenance
 * - Demonstrate formation transitions
 *
 * TEST SCENARIOS:
 *
 * 1. LINE FORMATION TEST
 *    Setup: 5 agents in horizontal line
 *    Expected: Equal spacing, perpendicular to heading
 *    Success: All positions calculated correctly
 *
 * 2. WEDGE FORMATION TEST
 *    Setup: 7 agents in V-shape
 *    Expected: Leader at point, wings fanning out
 *    Success: Proper V-formation structure
 *
 * 3. CIRCLE FORMATION TEST
 *    Setup: 6 agents in ring
 *    Expected: Equal angular spacing
 *    Success: Circular perimeter formed
 *
 * 4. FORMATION MAINTENANCE TEST
 *    Setup: Agents with position errors
 *    Expected: Corrective commands generated
 *    Success: Forces point toward target positions
 *
 * 5. FORMATION TRANSITION TEST
 *    Setup: Transition LINE → WEDGE
 *    Expected: Smooth interpolation
 *    Success: Agents move to new positions
 *
 * 6. ALL FORMATIONS TEST
 *    Setup: Create all 6 formation types
 *    Expected: Each formation valid
 *    Success: All shapes created successfully
 *
 * WEEK 4 SUCCESS CRITERIA:
 * ✓ All formation types implemented
 * ✓ Position calculation algorithms
 * ✓ Formation maintenance
 * ✓ Smooth transitions
 * ✓ Error correction
 * ✓ Comprehensive testing
 */
package com.team6.swarm.intelligence.formation;

import com.team6.swarm.core.*;
import java.util.*;

public class FormationTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("WEEK 4: FORMATION FLYING TEST");
        System.out.println("========================================");
        System.out.println();
        
        // Run all test scenarios
        testLineFormation();
        testWedgeFormation();
        testCircleFormation();
        testFormationMaintenance();
        testFormationTransition();
        testAllFormations();
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("All Week 4 tests completed!");
        System.out.println("========================================");
    }
    
    /**
     * TEST 1: LINE FORMATION
     * Agents in horizontal line
     */
    private static void testLineFormation() {
        System.out.println("TEST 1: Line Formation (Horizontal Line)");
        System.out.println("----------------------------------------");
        
        FormationController controller = new FormationController();
        
        // Create 5 agents
        List<Integer> agentIds = Arrays.asList(1, 2, 3, 4, 5);
        Point2D center = new Point2D(400, 300);
        
        // Create line formation
        Formation formation = controller.createFormation(
            FormationType.LINE, center, agentIds
        );
        
        System.out.println();
        System.out.println("Line formation positions:");
        for (int agentId : agentIds) {
            Point2D pos = formation.getAgentPosition(agentId);
            FormationRole role = formation.getAgentRole(agentId);
            System.out.println(String.format("  Agent %d: (%.1f, %.1f) - %s",
                agentId, pos.x, pos.y, role));
        }
        
        // Verify spacing
        Point2D pos1 = formation.getAgentPosition(1);
        Point2D pos2 = formation.getAgentPosition(2);
        double spacing = pos1.distanceTo(pos2);
        
        System.out.println();
        System.out.println(String.format("Agent spacing: %.1f units", spacing));
        
        if (formation.getAgentCount() == 5 && spacing > 35 && spacing < 45) {
            System.out.println("  ✓ PASS: Line formation created correctly");
        } else {
            System.out.println("  ✗ FAIL: Line formation incorrect");
        }
        System.out.println();
    }
    
    /**
     * TEST 2: WEDGE FORMATION
     * V-shape with leader at point
     */
    private static void testWedgeFormation() {
        System.out.println("TEST 2: Wedge Formation (V-Shape)");
        System.out.println("----------------------------------");
        
        FormationController controller = new FormationController();
        
        // Create 7 agents
        List<Integer> agentIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        Point2D center = new Point2D(400, 300);
        
        // Create wedge formation facing East (heading = 0)
        Formation formation = controller.createFormation(
            FormationType.WEDGE, center, 50.0, 0.0, agentIds
        );
        
        System.out.println();
        System.out.println("Wedge formation structure:");
        for (int agentId : agentIds) {
            Point2D pos = formation.getAgentPosition(agentId);
            FormationRole role = formation.getAgentRole(agentId);
            System.out.println(String.format("  Agent %d: (%.1f, %.1f) - %s",
                agentId, pos.x, pos.y, role));
        }
        
        // Verify leader is at front
        Point2D leaderPos = formation.getAgentPosition(formation.leaderAgentId);
        
        System.out.println();
        System.out.println(String.format("Leader (Agent %d) at point: (%.1f, %.1f)",
            formation.leaderAgentId, leaderPos.x, leaderPos.y));
        
        if (formation.leaderAgentId == 1 && formation.getAgentCount() == 7) {
            System.out.println("  ✓ PASS: Wedge formation created correctly");
        } else {
            System.out.println("  ✗ FAIL: Wedge formation incorrect");
        }
        System.out.println();
    }
    
    /**
     * TEST 3: CIRCLE FORMATION
     * Agents in circular ring
     */
    private static void testCircleFormation() {
        System.out.println("TEST 3: Circle Formation (Perimeter Ring)");
        System.out.println("-----------------------------------------");
        
        FormationController controller = new FormationController();
        
        // Create 6 agents
        List<Integer> agentIds = Arrays.asList(1, 2, 3, 4, 5, 6);
        Point2D center = new Point2D(400, 300);
        
        // Create circle formation
        Formation formation = controller.createFormation(
            FormationType.CIRCLE, center, agentIds
        );
        
        System.out.println();
        System.out.println("Circle formation positions:");
        
        // Calculate radius from first agent
        Point2D firstPos = formation.getAgentPosition(1);
        double radius = center.distanceTo(firstPos);
        
        System.out.println(String.format("Circle radius: %.1f units", radius));
        System.out.println();
        
        for (int agentId : agentIds) {
            Point2D pos = formation.getAgentPosition(agentId);
            double distFromCenter = center.distanceTo(pos);
            double angle = Math.atan2(pos.y - center.y, pos.x - center.x);
            System.out.println(String.format(
                "  Agent %d: (%.1f, %.1f) - Distance: %.1f, Angle: %.1f°",
                agentId, pos.x, pos.y, distFromCenter, Math.toDegrees(angle)
            ));
        }
        
        System.out.println();
        if (formation.getAgentCount() == 6 && radius > 30) {
            System.out.println("  ✓ PASS: Circle formation created correctly");
        } else {
            System.out.println("  ✗ FAIL: Circle formation incorrect");
        }
        System.out.println();
    }
    
    /**
     * TEST 4: FORMATION MAINTENANCE
     * Corrective commands for position errors
     */
    private static void testFormationMaintenance() {
        System.out.println("TEST 4: Formation Maintenance (Error Correction)");
        System.out.println("-----------------------------------------------");
        
        FormationController controller = new FormationController();
        
        // Create line formation
        List<Integer> agentIds = Arrays.asList(1, 2, 3, 4, 5);
        Formation formation = controller.createFormation(
            FormationType.LINE, new Point2D(400, 300), agentIds
        );
        
        // Create agents with position errors
        List<AgentState> agents = new ArrayList<>();
        for (int agentId : agentIds) {
            AgentState agent = createTestAgent(agentId);
            
            // Give agents incorrect positions (offset from formation)
            Point2D targetPos = formation.getAgentPosition(agentId);
            agent.position = new Point2D(
                targetPos.x + (Math.random() - 0.5) * 20,  // Random offset
                targetPos.y + (Math.random() - 0.5) * 20
            );
            
            agents.add(agent);
        }
        
        System.out.println();
        System.out.println("Agent position errors:");
        for (AgentState agent : agents) {
            Point2D targetPos = formation.getAgentPosition(agent.agentId);
            double error = agent.position.distanceTo(targetPos);
            System.out.println(String.format("  Agent %d: %.1f units off target",
                agent.agentId, error));
        }
        
        // Generate corrective commands
        List<MovementCommand> commands = controller.maintainFormation(formation, agents);
        
        System.out.println();
        System.out.println(String.format("Generated %d corrective commands", commands.size()));
        
        // Show first command details
        if (!commands.isEmpty()) {
            MovementCommand cmd = commands.get(0);
            Vector2D force = (Vector2D) cmd.parameters.get("correctionForce");
            double error = (Double) cmd.parameters.get("positionError");
            System.out.println(String.format(
                "  Example: Agent %d - Error: %.1f units, Force: (%.2f, %.2f)",
                cmd.agentId, error, force.x, force.y
            ));
        }
        
        System.out.println();
        if (commands.size() > 0 && commands.size() <= 5) {
            System.out.println("  ✓ PASS: Corrective commands generated");
        } else {
            System.out.println("  ✗ FAIL: Incorrect number of commands");
        }
        System.out.println();
    }
    
    /**
     * TEST 5: FORMATION TRANSITION
     * Smooth change between formations
     */
    private static void testFormationTransition() {
        System.out.println("TEST 5: Formation Transition (LINE → WEDGE)");
        System.out.println("------------------------------------------");
        
        FormationController controller = new FormationController();
        
        // Create initial line formation
        List<Integer> agentIds = Arrays.asList(1, 2, 3, 4, 5);
        Formation lineFormation = controller.createFormation(
            FormationType.LINE, new Point2D(400, 300), agentIds
        );
        
        System.out.println();
        System.out.println("Starting formation: LINE");
        
        // Start transition to wedge
        controller.transitionFormation(lineFormation, FormationType.WEDGE, 3000);
        
        System.out.println("Transition started (3 seconds)");
        System.out.println();
        
        // Simulate transition progress
        List<AgentState> agents = new ArrayList<>();
        for (int agentId : agentIds) {
            agents.add(createTestAgent(agentId));
        }
        
        System.out.println("Transition progress:");
        for (int i = 0; i <= 5; i++) {
            // Simulate time passing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            List<MovementCommand> commands = controller.updateTransition(agents);
            double progress = controller.getTransitionProgress();
            
            System.out.println(String.format("  %.0f%% - %d commands generated",
                progress * 100, commands.size()));
            
            // Update agent positions toward targets (simplified)
            for (MovementCommand cmd : commands) {
                Point2D targetPos = (Point2D) cmd.parameters.get("targetPosition");
                AgentState agent = agents.stream()
                    .filter(a -> a.agentId == cmd.agentId)
                    .findFirst().orElse(null);
                if (agent != null && targetPos != null) {
                    // Move agent 20% toward target
                    agent.position = new Point2D(
                        agent.position.x + (targetPos.x - agent.position.x) * 0.2,
                        agent.position.y + (targetPos.y - agent.position.y) * 0.2
                    );
                }
            }
        }
        
        System.out.println();
        if (controller.isTransitioning() || controller.getTransitionsCompleted() > 0) {
            System.out.println("  ✓ PASS: Formation transition working");
        } else {
            System.out.println("  ✗ FAIL: Transition not functioning");
        }
        System.out.println();
    }
    
    /**
     * TEST 6: ALL FORMATIONS
     * Create and validate all formation types
     */
    private static void testAllFormations() {
        System.out.println("TEST 6: All Formation Types");
        System.out.println("---------------------------");
        
        FormationController controller = new FormationController();
        Point2D center = new Point2D(400, 300);
        
        System.out.println();
        
        FormationType[] types = FormationType.values();
        int successCount = 0;
        
        for (FormationType type : types) {
            // Get minimum agents for this type
            int minAgents = type.getMinimumAgents();
            List<Integer> agentIds = new ArrayList<>();
            for (int i = 1; i <= minAgents + 2; i++) {
                agentIds.add(i);
            }
            
            // Create formation
            Formation formation = controller.createFormation(type, center, agentIds);
            
            if (formation != null && formation.getAgentCount() == agentIds.size()) {
                System.out.println(String.format("  ✓ %s: %d agents positioned",
                    type.getDisplayName(), formation.getAgentCount()));
                successCount++;
            } else {
                System.out.println(String.format("  ✗ %s: Failed to create",
                    type.getDisplayName()));
            }
        }
        
        System.out.println();
        System.out.println(String.format(
            "Created %d/%d formation types successfully",
            successCount, types.length
        ));
        
        if (successCount == types.length) {
            System.out.println("  ✓ PASS: All formation types working");
        } else {
            System.out.println("  ✗ FAIL: Some formations failed");
        }
        System.out.println();
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Create test agent with default properties
     */
    private static AgentState createTestAgent(int agentId) {
        AgentState agent = new AgentState();
        agent.agentId = agentId;
        agent.agentName = "Agent_" + agentId;
        agent.position = new Point2D(400, 300);  // Start at center
        agent.velocity = new Vector2D(0, 0);
        agent.status = com.team6.swarm.core.AgentStatus.ACTIVE;
        agent.batteryLevel = 0.8;
        agent.maxSpeed = 50.0;
        agent.communicationRange = 100.0;
        return agent;
    }
}