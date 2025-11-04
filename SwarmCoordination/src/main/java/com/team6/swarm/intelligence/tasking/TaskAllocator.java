/**
 * TASKALLOCATOR CLASS - Intelligent Work Distribution System
 *
 * PURPOSE:
 * - Assigns tasks to agents intelligently based on multiple criteria
 * - Balances workload across swarm to prevent overloading
 * - Handles task reassignment when agents fail
 * - Maximizes efficiency and mission success
 *
 * ALLOCATION STRATEGIES:
 *
 * 1. NEAREST AGENT STRATEGY:
 *    - Find closest available agent to task location
 *    - Minimizes travel time and energy
 *    - Best for location-based tasks
 *    - Score = (maxDistance - distance) / maxDistance * 30
 *
 * 2. CAPABILITY MATCHING:
 *    - Match task requirements to agent capabilities
 *    - Scout tasks → Scout agents
 *    - Guard tasks → Guard agents
 *    - Ensures task can be completed
 *    - Score bonus = +25 for role match
 *
 * 3. LOAD BALANCING:
 *    - Count current active tasks per agent
 *    - Assign to least busy agent
 *    - Prevents agent overload
 *    - Score = (maxLoad - currentLoad) / maxLoad * 25
 *
 * 4. BATTERY AWARENESS:
 *    - Don't assign to low-battery agents
 *    - Long tasks → high-battery agents
 *    - Critical for mission completion
 *    - Score = batteryLevel * 20
 *
 * ALLOCATION ALGORITHM:
 *
 * For each available agent:
 *   score = 0
 *   
 *   // Distance scoring (closer is better)
 *   distance = calculateDistance(agent, task)
 *   score += (maxDistance - distance) / maxDistance * 30
 *   
 *   // Load balancing (less busy is better)
 *   currentLoad = getAgentLoad(agent)
 *   score += (maxLoad - currentLoad) / maxLoad * 25
 *   
 *   // Battery scoring (more battery is better)
 *   score += agent.batteryLevel * 20
 *   
 *   // Role matching (right skills = bonus)
 *   if (agent.role matches task.requiredRole):
 *     score += 25
 *   
 *   // Select agent with highest score
 *
 * FAILURE HANDLING:
 *
 * When Agent Fails:
 * 1. Detect failure (battery low, malfunction, stuck)
 * 2. Get all tasks assigned to failed agent
 * 3. Mark tasks as PENDING for reassignment
 * 4. Run allocation algorithm to find new agents
 * 5. Reassign tasks with priority for critical ones
 * 6. Update task history with reassignment
 *
 * WORKLOAD MANAGEMENT:
 *
 * Track Per Agent:
 * - Active tasks count
 * - Total estimated workload time
 * - Completion rate
 * - Average task duration
 *
 * Balance Strategy:
 * - No agent should have >3x tasks vs average
 * - High-priority tasks distributed evenly
 * - Monitor for bottlenecks
 *
 * USAGE PATTERNS:
 *
 * Initialize:
 * TaskAllocator allocator = new TaskAllocator();
 *
 * Assign Single Task:
 * Task task = Task.createMoveToWaypoint("task_001", target);
 * TaskAssignment assignment = allocator.assignTask(task, availableAgents);
 *
 * Assign Multiple Tasks:
 * List<TaskAssignment> assignments = 
 *     allocator.assignTasks(taskList, availableAgents);
 *
 * Handle Failure:
 * allocator.reassignAgentTasks(failedAgentId, remainingAgents);
 *
 * Check Workload:
 * int load = allocator.getAgentLoad(agentId);
 * boolean balanced = allocator.isWorkloadBalanced();
 *
 * INTEGRATION POINTS:
 * - Receives: List of tasks from mission planner
 * - Receives: AgentState list from Sanidhaya
 * - Sends: TaskAssignment to agents
 * - Reports: Workload metrics to Anthony
 */
package com.team6.swarm.intelligence.tasking;

import com.team6.swarm.core.AgentState;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaskAllocator {
    // Task tracking
    private final Map<String, TaskAssignment> assignments;
    private final Map<Integer, List<TaskAssignment>> agentWorkload;
    private final Queue<Task> pendingTasks;
    
    // Configuration
    private static final int MAX_TASKS_PER_AGENT = 3;
    private static final double MIN_BATTERY_FOR_TASK = 0.2;
    
    // Performance metrics
    private int totalTasksAssigned;
    private int totalReassignments;
    private int failedAssignments;
    
    /**
     * Constructor
     */
    public TaskAllocator() {
        this.assignments = new ConcurrentHashMap<>();
        this.agentWorkload = new ConcurrentHashMap<>();
        this.pendingTasks = new LinkedList<>();
        this.totalTasksAssigned = 0;
        this.totalReassignments = 0;
        this.failedAssignments = 0;
    }
    
    /**
     * ASSIGN TASK TO BEST AGENT
     * Main allocation algorithm
     *
     * @param task Task to assign
     * @param availableAgents List of agents that can work
     * @return TaskAssignment if successful, null if no suitable agent
     */
    public TaskAssignment assignTask(Task task, List<AgentState> availableAgents) {
        if (task == null || availableAgents == null || availableAgents.isEmpty()) {
            System.err.println("Cannot assign task: invalid parameters");
            return null;
        }
        
        // Validate task
        if (!task.validate()) {
            System.err.println("Cannot assign invalid task: " + task.taskId);
            return null;
        }
        
        // Find best agent for this task
        AgentState bestAgent = null;
        double bestScore = -1;
        AssignmentReason bestReason = AssignmentReason.NEAREST_AGENT;
        
        for (AgentState agent : availableAgents) {
            // Skip if agent doesn't meet basic requirements
            if (!isAgentEligible(agent, task)) {
                continue;
            }
            
            // Calculate assignment score
            double score = calculateAssignmentScore(agent, task);
            
            if (score > bestScore) {
                bestScore = score;
                bestAgent = agent;
                bestReason = determineAssignmentReason(agent, task, score);
            }
        }
        
        // No suitable agent found
        if (bestAgent == null) {
            System.out.println("No eligible agent found for task: " + task.taskId);
            pendingTasks.offer(task);  // Add to pending queue
            return null;
        }
        
        // Create assignment
        TaskAssignment assignment = new TaskAssignment(
            task, bestAgent.agentId, bestReason, bestScore);
        // Set allocator reference so the assignment can notify back when it ends
        assignment.setAllocator(this);
        
        // Track assignment
        assignments.put(task.taskId, assignment);
        addToAgentWorkload(bestAgent.agentId, assignment);
        totalTasksAssigned++;
        
        // Log assignment
        System.out.println(String.format(
            "Assigned %s to Agent %d (Score: %.2f, Reason: %s)",
            task.taskId, bestAgent.agentId, bestScore, bestReason
        ));
        
        return assignment;
    }
    
    /**
     * ASSIGN MULTIPLE TASKS
     * Distributes list of tasks across available agents
     */
    public List<TaskAssignment> assignTasks(List<Task> tasks, List<AgentState> availableAgents) {
        List<TaskAssignment> results = new ArrayList<>();
        
        // Sort tasks by priority (HIGH first, then NORMAL, then LOW)
        List<Task> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort((t1, t2) -> {
            // Custom comparator: HIGH = 2, NORMAL = 1, LOW = 0
            int p1 = getPriorityValue(t1.priority);
            int p2 = getPriorityValue(t2.priority);
            return Integer.compare(p2, p1);  // Reverse order (highest first)
        });
        
        for (Task task : sortedTasks) {
            TaskAssignment assignment = assignTask(task, availableAgents);
            if (assignment != null) {
                results.add(assignment);
            }
        }
        
        return results;
    }
    
    /**
     * Convert priority to numeric value for sorting
     */
    private int getPriorityValue(TaskPriority priority) {
        switch (priority) {
            case HIGH: return 2;
            case NORMAL: return 1;
            case LOW: return 0;
            default: return 1;
        }
    }
    
    /**
     * CHECK IF AGENT IS ELIGIBLE FOR TASK
     * Basic requirements validation
     */
    private boolean isAgentEligible(AgentState agent, Task task) {
        // Check agent is active
        if (agent.status != com.team6.swarm.core.AgentStatus.ACTIVE) {
            return false;
        }
        
        // Check battery level
        // Enforce a safety floor on minimum battery required
        double requiredBattery = Math.max(task.minimumBattery, MIN_BATTERY_FOR_TASK);
        if (agent.batteryLevel < requiredBattery) {
            return false;
        }
        
        // Check workload limit
        int currentLoad = getAgentLoad(agent.agentId);
        if (currentLoad >= MAX_TASKS_PER_AGENT) {
            return false;
        }
        
        // Check role requirement (if specified)
        if (task.requiredRole != null) {
            // For now, we'll allow any active agent
            // Later integrate with agent's current behavior
        }
        
        return true;
    }
    
    /**
     * CALCULATE ASSIGNMENT SCORE
     * Multi-criteria scoring algorithm
     */
    private double calculateAssignmentScore(AgentState agent, Task task) {
        double score = 0.0;
        
        // 1. DISTANCE SCORING (30 points max)
        if (task.targetLocation != null) {
            double distance = agent.position.distanceTo(task.targetLocation);
            double maxDistance = 500.0;  // Assumed world size
            double distanceScore = Math.max(0, (maxDistance - distance) / maxDistance * 30);
            score += distanceScore;
        } else {
            score += 15;  // Neutral score for non-location tasks
        }
        
        // 2. LOAD BALANCING (25 points max)
        int currentLoad = getAgentLoad(agent.agentId);
        double loadScore = (MAX_TASKS_PER_AGENT - currentLoad) / 
                          (double) MAX_TASKS_PER_AGENT * 25;
        score += loadScore;
        
        // 3. BATTERY LEVEL (20 points max)
        double batteryScore = agent.batteryLevel * 20;
        score += batteryScore;
        
        // 4. ROLE MATCHING (25 points bonus)
        if (task.requiredRole != null) {
            // For now, assume any active agent can do any task
            // Later: check agent's current behavior type
            score += 12;  // Partial role match
        }
        
        return score;
    }
    
    /**
     * DETERMINE ASSIGNMENT REASON
     * Identify primary reason agent was chosen
     */
    private AssignmentReason determineAssignmentReason(AgentState agent, 
                                                      Task task, double score) {
        // Check distance
        if (task.targetLocation != null) {
            double distance = agent.position.distanceTo(task.targetLocation);
            if (distance < 50) {
                return AssignmentReason.NEAREST_AGENT;
            }
        }
        
        // Check role
        if (task.requiredRole != null) {
            return AssignmentReason.ROLE_MATCH;
        }
        
        // Check load
        int currentLoad = getAgentLoad(agent.agentId);
        if (currentLoad == 0) {
            return AssignmentReason.LOAD_BALANCE;
        }
        
        // Check battery
        if (agent.batteryLevel > 0.8 && task.estimatedDuration > 60000) {
            return AssignmentReason.BATTERY_OPTIMAL;
        }
        
        return AssignmentReason.NEAREST_AGENT;  // Default
    }
    
    /**
     * REASSIGN AGENT TASKS
     * Handle when agent fails - reassign all their tasks
     */
    public List<TaskAssignment> reassignAgentTasks(int failedAgentId, 
                                                    List<AgentState> availableAgents) {
        List<TaskAssignment> reassignments = new ArrayList<>();
        
        // Get all tasks for failed agent
        List<TaskAssignment> failedTasks = agentWorkload.getOrDefault(
            failedAgentId, new ArrayList<>());
        
        System.out.println(String.format(
            "Reassigning %d tasks from failed Agent %d",
            failedTasks.size(), failedAgentId
        ));
        
        // Remove from agent workload
        agentWorkload.remove(failedAgentId);
        
        // Reassign each task
        for (TaskAssignment oldAssignment : failedTasks) {
            if (oldAssignment.status.isTerminal()) {
                continue;  // Skip already completed/cancelled tasks
            }
            
            // Try to find new agent
            TaskAssignment newAssignment = assignTask(oldAssignment.task, availableAgents);
            
            if (newAssignment != null) {
                newAssignment.reassignmentCount = oldAssignment.reassignmentCount + 1;
                newAssignment.assignmentReason = AssignmentReason.REASSIGNMENT;
                reassignments.add(newAssignment);
                totalReassignments++;
            } else {
                failedAssignments++;
                System.err.println("Failed to reassign task: " + oldAssignment.task.taskId);
            }
        }
        
        return reassignments;
    }
    
    /**
     * REDISTRIBUTE TASKS
     * Balance workload across all agents
     */
    public void redistributeTasks(List<AgentState> availableAgents) {
        // Get all active assignments
        List<TaskAssignment> activeAssignments = new ArrayList<>();
        for (TaskAssignment assignment : assignments.values()) {
            if (assignment.isActive()) {
                activeAssignments.add(assignment);
            }
        }
        
        // Check if redistribution needed
        if (!isWorkloadBalanced()) {
            System.out.println("Workload unbalanced - redistributing tasks");
            
            // For now, just log the imbalance
            // Full redistribution would require cancelling and reassigning
            for (Map.Entry<Integer, List<TaskAssignment>> entry : agentWorkload.entrySet()) {
                System.out.println(String.format(
                    "  Agent %d: %d tasks", entry.getKey(), entry.getValue().size()
                ));
            }
        }
    }
    
    // ==================== WORKLOAD MANAGEMENT ====================
    
    /**
     * Get agent's current task count
     */
    public int getAgentLoad(int agentId) {
        List<TaskAssignment> tasks = agentWorkload.get(agentId);
        if (tasks == null) return 0;

        // Count any assignment that is not in a terminal state. When an assignment
        // is created it starts as PENDING (agent hasn't accepted yet), but it
        // should still contribute to the agent's workload for allocation and
        // balancing purposes. Therefore include all non-terminal assignments.
        return (int) tasks.stream()
            .filter(t -> !t.status.isTerminal())
            .count();
    }
    
    /**
     * Get agent's total workload time
     */
    public long getAgentWorkloadTime(int agentId) {
        List<TaskAssignment> tasks = agentWorkload.get(agentId);
        if (tasks == null) return 0;
        // Sum estimated durations for non-terminal assignments (pending/in-progress/blocked)
        return tasks.stream()
            .filter(t -> !t.status.isTerminal())
            .mapToLong(t -> t.task.estimatedDuration)
            .sum();
    }
    
    /**
     * Check if workload is balanced across agents
     */
    public boolean isWorkloadBalanced() {
        if (agentWorkload.isEmpty()) return true;
        
        // Calculate average load
        double avgLoad = agentWorkload.values().stream()
            // Use non-terminal counts for each agent so the average reflects
            // real pending/in-progress workload rather than total history size.
            .mapToInt(list -> (int) list.stream().filter(t -> !t.status.isTerminal()).count())
            .average()
            .orElse(0.0);
        
        // Check if any agent has >2x average
        for (List<TaskAssignment> tasks : agentWorkload.values()) {
            int nonTerminal = (int) tasks.stream().filter(t -> !t.status.isTerminal()).count();
            if (nonTerminal > avgLoad * 2) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Add assignment to agent's workload
     */
    private void addToAgentWorkload(int agentId, TaskAssignment assignment) {
        // Ensure an assignment list exists for the agent, then add.
        List<TaskAssignment> list = agentWorkload.get(agentId);
        if (list == null) {
            list = new ArrayList<>();
            agentWorkload.put(agentId, list);
        }
        list.add(assignment);
    }

    /**
     * Remove assignment from an agent's workload list.
     * Safe to call multiple times; silently returns if not present.
     */
    public void removeFromAgentWorkload(int agentId, TaskAssignment assignment) {
        List<TaskAssignment> tasks = agentWorkload.get(agentId);
        if (tasks != null) {
            tasks.remove(assignment);
            // If list becomes empty, remove the key to keep map small
            if (tasks.isEmpty()) {
                agentWorkload.remove(agentId);
            }
        }
    }

    /**
     * Called by TaskAssignment when it reaches a terminal state so the allocator
     * can remove the assignment from per-agent workload tracking.
     */
    public void notifyAssignmentTerminal(TaskAssignment assignment) {
        if (assignment == null) return;
        removeFromAgentWorkload(assignment.assignedAgentId, assignment);
    }
    
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Get all assignments
     */
    public Map<String, TaskAssignment> getAllAssignments() {
        return new HashMap<>(assignments);
    }
    
    /**
     * Get assignments for specific agent
     */
    public List<TaskAssignment> getAgentAssignments(int agentId) {
        return new ArrayList<>(agentWorkload.getOrDefault(agentId, new ArrayList<>()));
    }
    
    /**
     * Get specific assignment by task ID
     */
    public TaskAssignment getAssignment(String taskId) {
        return assignments.get(taskId);
    }
    
    /**
     * Get all pending tasks
     */
    public List<Task> getPendingTasks() {
        return new ArrayList<>(pendingTasks);
    }
    
    // ==================== PERFORMANCE METRICS ====================
    
    public int getTotalTasksAssigned() {
        return totalTasksAssigned;
    }
    
    public int getTotalReassignments() {
        return totalReassignments;
    }
    
    public int getFailedAssignments() {
        return failedAssignments;
    }
    
    public double getAssignmentSuccessRate() {
        int total = totalTasksAssigned + failedAssignments;
        return total > 0 ? (double) totalTasksAssigned / total : 1.0;
    }
    
    public void resetMetrics() {
        totalTasksAssigned = 0;
        totalReassignments = 0;
        failedAssignments = 0;
    }
}