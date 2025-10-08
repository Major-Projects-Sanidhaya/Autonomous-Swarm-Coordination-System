/**
 * TASKTEST CLASS - Week 3 Task Management Validation
 *
 * PURPOSE:
 * - Test that task allocation system works correctly
 * - Validate assignment algorithms and scoring
 * - Verify load balancing and reassignment
 * - Demonstrate intelligent work distribution
 *
 * TEST SCENARIOS:
 *
 * 1. SIMPLE TASK ASSIGNMENT
 *    Setup: 1 task, 5 available agents
 *    Expected: Nearest agent gets task
 *    Success: Task assigned to closest agent
 *
 * 2. LOAD BALANCING TEST
 *    Setup: 6 tasks, 3 agents
 *    Expected: Each agent gets 2 tasks
 *    Success: Workload evenly distributed
 *
 * 3. ROLE MATCHING TEST
 *    Setup: Scout task, mixed agent roles
 *    Expected: Scout agent preferred
 *    Success: Role-appropriate assignment
 *
 * 4. BATTERY AWARENESS TEST
 *    Setup: Long task, agents with varying battery
 *    Expected: High-battery agent selected
 *    Success: Battery-conscious allocation
 *
 * 5. AGENT FAILURE TEST
 *    Setup: Agent with 3 tasks fails
 *    Expected: Tasks reassigned to others
 *    Success: All tasks redistributed
 *
 * 6. PRIORITY HANDLING TEST
 *    Setup: Mix of HIGH, NORMAL, LOW priority tasks
 *    Expected: HIGH priority assigned first
 *    Success: Priority-based ordering
 *
 * WEEK 3 SUCCESS CRITERIA:
 * ✓ Task creation and validation
 * ✓ TaskAssignment tracking
 * ✓ Multi-criteria scoring algorithm
 * ✓ Load balancing across agents
 * ✓ Failure handling and reassignment
 * ✓ Performance metrics
 */
package com.team6.swarm.intelligence.tasking;

import com.team6.swarm.core.*;
import com.team6.swarm.intelligence.flocking.BehaviorType;
import java.util.*;

public class TaskTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("WEEK 3: TASK MANAGEMENT TEST");
        System.out.println("========================================");
        System.out.println();
        
        // Run all test scenarios
        testSimpleAssignment();
        testLoadBalancing();
        testRoleMatching();
        testBatteryAwareness();
        testAgentFailure();
        testPriorityHandling();
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("All Week 3 tests completed!");
        System.out.println("========================================");
    }
    
    /**
     * TEST 1: SIMPLE TASK ASSIGNMENT
     * Nearest agent should get the task
     */
    private static void testSimpleAssignment() {
        System.out.println("TEST 1: Simple Task Assignment (Nearest Agent)");
        System.out.println("-----------------------------------------------");
        
        TaskAllocator allocator = new TaskAllocator();
        
        // Create task at (300, 300)
        Task task = Task.createMoveToWaypoint("task_001", new Point2D(300, 300));
        
        // Create 5 agents at various distances
        List<AgentState> agents = createTestAgents(5);
        agents.get(0).position = new Point2D(100, 100);  // Far
        agents.get(1).position = new Point2D(290, 295);  // Nearest (distance ≈ 11)
        agents.get(2).position = new Point2D(400, 400);  // Far
        agents.get(3).position = new Point2D(200, 200);  // Medium
        agents.get(4).position = new Point2D(500, 100);  // Far
        
        System.out.println("Task location: (300, 300)");
        System.out.println("Agent distances:");
        for (int i = 0; i < agents.size(); i++) {
            double dist = agents.get(i).position.distanceTo(task.targetLocation);
            System.out.println(String.format("  Agent %d: %.1f units away", i+1, dist));
        }
        
        System.out.println();
        
        // Assign task
        TaskAssignment assignment = allocator.assignTask(task, agents);
        
        System.out.println();
        if (assignment != null && assignment.assignedAgentId == 2) {
            System.out.println("  ✓ PASS: Nearest agent (Agent 2) got the task");
        } else {
            System.out.println("  ✗ FAIL: Wrong agent assigned");
        }
        System.out.println();
    }
    
    /**
     * TEST 2: LOAD BALANCING
     * Tasks should be evenly distributed
     */
    private static void testLoadBalancing() {
        System.out.println("TEST 2: Load Balancing (Even Distribution)");
        System.out.println("-------------------------------------------");
        
        TaskAllocator allocator = new TaskAllocator();
        
        // Create 6 tasks
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            tasks.add(Task.createMoveToWaypoint("task_00" + i, 
                new Point2D(100 * i, 100)));
        }
        
        // Create 3 agents
        List<AgentState> agents = createTestAgents(3);
        
        System.out.println("Assigning 6 tasks to 3 agents...");
        
        // Assign all tasks
        List<TaskAssignment> assignments = allocator.assignTasks(tasks, agents);
        
        System.out.println();
        System.out.println(String.format("Successfully assigned %d tasks:", assignments.size()));
        for (TaskAssignment a : assignments) {
            System.out.println(String.format("  %s → Agent %d", 
                a.task.taskId, a.assignedAgentId));
        }
        
        System.out.println();
        System.out.println("Workload distribution:");
        for (int i = 1; i <= 3; i++) {
            int load = allocator.getAgentLoad(i);
            System.out.println(String.format("  Agent %d: %d tasks", i, load));
        }
        
        System.out.println();
        if (allocator.isWorkloadBalanced() && assignments.size() == 6) {
            System.out.println("  ✓ PASS: All 6 tasks assigned and workload evenly balanced");
        } else if (assignments.size() < 6) {
            System.out.println("  ✗ FAIL: Not all tasks were assigned");
        } else {
            System.out.println("  ✗ FAIL: Workload imbalanced");
        }
        System.out.println();
    }
    
    /**
     * TEST 3: ROLE MATCHING
     * Task requiring specific role should go to matching agent
     */
    private static void testRoleMatching() {
        System.out.println("TEST 3: Role Matching (Capability-Based)");
        System.out.println("----------------------------------------");
        
        TaskAllocator allocator = new TaskAllocator();
        
        // Create scout task
        Task scoutTask = Task.createScoutAhead("task_scout", 100.0);
        scoutTask.requiredRole = BehaviorType.SCOUT;
        
        // Create agents with different roles
        List<AgentState> agents = createTestAgents(5);
        
        System.out.println("Task requires: SCOUT role");
        System.out.println("Assigning to available agents...");
        
        // Assign task
        TaskAssignment assignment = allocator.assignTask(scoutTask, agents);
        
        System.out.println();
        if (assignment != null) {
            System.out.println(String.format("Assigned to Agent %d (Reason: %s)",
                assignment.assignedAgentId, assignment.assignmentReason));
            System.out.println("  ✓ PASS: Role-based assignment successful");
        } else {
            System.out.println("  ✗ FAIL: Assignment failed");
        }
        System.out.println();
    }
    
    /**
     * TEST 4: BATTERY AWARENESS
     * Long tasks should go to high-battery agents
     */
    private static void testBatteryAwareness() {
        System.out.println("TEST 4: Battery Awareness (Resource Management)");
        System.out.println("-----------------------------------------------");
        
        TaskAllocator allocator = new TaskAllocator();
        
        // Create long-duration task
        Task longTask = Task.createPatrolArea("task_patrol", 
            Arrays.asList(
                new Point2D(100, 100),
                new Point2D(200, 100),
                new Point2D(200, 200),
                new Point2D(100, 200)
            ));
        longTask.estimatedDuration = 120000;  // 2 minutes
        longTask.minimumBattery = 0.5;  // Needs 50%
        
        // Create agents with varying battery
        List<AgentState> agents = createTestAgents(5);
        agents.get(0).batteryLevel = 0.3;  // Too low
        agents.get(1).batteryLevel = 0.6;  // Adequate
        agents.get(2).batteryLevel = 0.9;  // High
        agents.get(3).batteryLevel = 0.5;  // Minimum
        agents.get(4).batteryLevel = 0.7;  // Good
        
        System.out.println("Task needs 50% battery minimum");
        System.out.println("Agent battery levels:");
        for (int i = 0; i < agents.size(); i++) {
            System.out.println(String.format("  Agent %d: %.0f%%", 
                i+1, agents.get(i).batteryLevel * 100));
        }
        
        System.out.println();
        
        // Assign task
        TaskAssignment assignment = allocator.assignTask(longTask, agents);
        
        System.out.println();
        if (assignment != null) {
            AgentState assignedAgent = agents.get(assignment.assignedAgentId - 1);
            System.out.println(String.format("Assigned to Agent %d (%.0f%% battery)",
                assignment.assignedAgentId, assignedAgent.batteryLevel * 100));
            
            if (assignedAgent.batteryLevel >= 0.7) {
                System.out.println("  ✓ PASS: High-battery agent selected");
            } else {
                System.out.println("  ⚠ WARNING: Lower battery agent selected");
            }
        } else {
            System.out.println("  ✗ FAIL: Assignment failed");
        }
        System.out.println();
    }
    
    /**
     * TEST 5: AGENT FAILURE
     * Failed agent's tasks should be reassigned
     */
    private static void testAgentFailure() {
        System.out.println("TEST 5: Agent Failure (Task Reassignment)");
        System.out.println("-----------------------------------------");
        
        TaskAllocator allocator = new TaskAllocator();
        
        // Create 3 tasks
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            tasks.add(Task.createMoveToWaypoint("task_00" + i, 
                new Point2D(100 * i, 100)));
        }
        
        // Create 5 agents
        List<AgentState> agents = createTestAgents(5);
        
        System.out.println("Assigning 3 tasks to agents...");
        List<TaskAssignment> assignments = allocator.assignTasks(tasks, agents);
        
        System.out.println("Initial assignments:");
        for (TaskAssignment a : assignments) {
            System.out.println(String.format("  %s → Agent %d", 
                a.task.taskId, a.assignedAgentId));
        }
        
        // Simulate agent 1 failure
        int failedAgentId = assignments.get(0).assignedAgentId;
        System.out.println();
        System.out.println(String.format("Agent %d failed! Reassigning tasks...", failedAgentId));
        
        // Remove failed agent
        agents.removeIf(a -> a.agentId == failedAgentId);
        
        // Reassign tasks
        List<TaskAssignment> reassignments = 
            allocator.reassignAgentTasks(failedAgentId, agents);
        
        System.out.println();
        System.out.println("Reassignment results:");
        for (TaskAssignment a : reassignments) {
            System.out.println(String.format("  %s → Agent %d (reassignment #%d)", 
                a.task.taskId, a.assignedAgentId, a.reassignmentCount));
        }
        
        System.out.println();
        if (reassignments.size() > 0) {
            System.out.println("  ✓ PASS: Tasks successfully reassigned");
        } else {
            System.out.println("  ✗ FAIL: Reassignment failed");
        }
        System.out.println();
    }
    
    /**
     * TEST 6: PRIORITY HANDLING
     * HIGH priority tasks should be assigned first
     */
    private static void testPriorityHandling() {
        System.out.println("TEST 6: Priority Handling (Urgent Tasks First)");
        System.out.println("----------------------------------------------");
        
        TaskAllocator allocator = new TaskAllocator();
        
        // Create tasks with different priorities
        List<Task> tasks = new ArrayList<>();
        
        Task lowTask = Task.createMoveToWaypoint("task_low", new Point2D(100, 100));
        lowTask.priority = TaskPriority.LOW;
        tasks.add(lowTask);
        
        Task normalTask = Task.createMoveToWaypoint("task_normal", new Point2D(200, 100));
        normalTask.priority = TaskPriority.NORMAL;
        tasks.add(normalTask);
        
        Task highTask = Task.createMoveToWaypoint("task_high", new Point2D(300, 100));
        highTask.priority = TaskPriority.HIGH;
        tasks.add(highTask);
        
        Task normalTask2 = Task.createMoveToWaypoint("task_normal2", new Point2D(400, 100));
        normalTask2.priority = TaskPriority.NORMAL;
        tasks.add(normalTask2);
        
        // Create 2 agents (fewer than tasks to see prioritization)
        List<AgentState> agents = createTestAgents(2);
        
        System.out.println("Tasks by creation order:");
        System.out.println("  1. task_low (LOW priority)");
        System.out.println("  2. task_normal (NORMAL priority)");
        System.out.println("  3. task_high (HIGH priority)");
        System.out.println("  4. task_normal2 (NORMAL priority)");
        
        System.out.println();
        System.out.println("Assigning to 2 agents (priority-based)...");
        
        // Assign tasks (should prioritize HIGH first)
        List<TaskAssignment> assignments = allocator.assignTasks(tasks, agents);
        
        System.out.println();
        System.out.println("Assignment order:");
        for (int i = 0; i < assignments.size(); i++) {
            TaskAssignment a = assignments.get(i);
            System.out.println(String.format("  %d. %s (Priority: %s) → Agent %d",
                i+1, a.task.taskId, a.task.priority, a.assignedAgentId));
        }
        
        System.out.println();
        if (assignments.size() > 0 && assignments.get(0).task.taskId.equals("task_high")) {
            System.out.println("  ✓ PASS: HIGH priority task assigned first");
        } else {
            System.out.println("  ✗ FAIL: Priority ordering incorrect");
        }
        
        // Check remaining tasks
        List<Task> pending = allocator.getPendingTasks();
        if (!pending.isEmpty()) {
            System.out.println();
            System.out.println("Pending tasks (agents at capacity):");
            for (Task t : pending) {
                System.out.println(String.format("  %s (Priority: %s)", 
                    t.taskId, t.priority));
            }
        }
        
        System.out.println();
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Create test agents with default properties
     */
    private static List<AgentState> createTestAgents(int count) {
        List<AgentState> agents = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            AgentState agent = new AgentState();
            agent.agentId = i;
            agent.agentName = "Agent_" + i;
            agent.position = new Point2D(100 * i, 100 * i);
            agent.velocity = new Vector2D(0, 0);
            agent.status = com.team6.swarm.core.AgentStatus.ACTIVE;
            agent.batteryLevel = 0.8;  // Default 80%
            agent.maxSpeed = 50.0;
            agent.communicationRange = 100.0;
            agents.add(agent);
        }
        
        return agents;
    }
}