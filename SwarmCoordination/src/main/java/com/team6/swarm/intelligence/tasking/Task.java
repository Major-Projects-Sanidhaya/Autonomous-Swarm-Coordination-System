/**
 * TASK CLASS - Unit of Work for Swarm Agents
 *
 * PURPOSE:
 * - Represents specific work to be performed by an agent
 * - Contains all information needed to execute the task
 * - Enables tracking of task progress and status
 *
 * TASK STRUCTURE:
 *
 * IDENTIFICATION:
 * - taskId: Unique identifier (e.g., "task_001", "patrol_042")
 * - taskType: Category of work (PATROL, SEARCH, GUARD, etc.)
 * - createdTime: When task was created
 *
 * LOCATION:
 * - targetLocation: Where to go (for location-based tasks)
 * - areaOfInterest: Region to cover (for area-based tasks)
 * - startLocation: Where task begins (optional)
 *
 * REQUIREMENTS:
 * - requiredRole: What behavior type needed (SCOUT, GUARD, etc.)
 * - minimumBattery: Battery level required to start
 * - estimatedDuration: Expected completion time (milliseconds)
 * - priority: How urgent (HIGH, NORMAL, LOW)
 *
 * EXECUTION:
 * - parameters: Task-specific configuration
 * - assignedTo: Which agent has this task (-1 if unassigned)
 * - status: Current state (PENDING, IN_PROGRESS, COMPLETED, etc.)
 *
 * TRACKING:
 * - startTime: When work began
 * - completionTime: When finished
 * - completionPercentage: Progress (0-100)
 * - lastUpdate: Most recent status change
 *
 * TASK TYPES:
 *
 * MOVE_TO_WAYPOINT:
 * - Simple navigation to specific point
 * - Parameters: targetLocation
 * - Example: "Go to charging station at (500, 300)"
 *
 * PATROL_AREA:
 * - Move around defined region
 * - Parameters: patrolPath (list of waypoints)
 * - Example: "Patrol perimeter with 4 corners"
 *
 * SEARCH_PATTERN:
 * - Systematic area coverage
 * - Parameters: searchArea, pattern type (grid, spiral)
 * - Example: "Grid search area 200x200 starting at (100,100)"
 *
 * MAINTAIN_FORMATION:
 * - Hold specific position in formation
 * - Parameters: formationPosition, leaderAgent
 * - Example: "Maintain right wing position"
 *
 * FOLLOW_LEADER:
 * - Track another agent's movement
 * - Parameters: leaderAgentId, followDistance
 * - Example: "Follow agent 1 at 30 unit distance"
 *
 * GUARD_POSITION:
 * - Stay at location and watch for threats
 * - Parameters: guardLocation, watchRadius
 * - Example: "Guard waypoint at (300, 400)"
 *
 * SCOUT_AHEAD:
 * - Explore forward of main swarm
 * - Parameters: scoutDistance, reportInterval
 * - Example: "Scout 100 units ahead, report every 5 seconds"
 *
 * PRIORITY LEVELS:
 *
 * HIGH:
 * - Emergency responses
 * - Critical mission objectives
 * - Safety-related tasks
 * - Execute immediately
 *
 * NORMAL:
 * - Standard mission tasks
 * - Regular patrol duties
 * - Formation maintenance
 * - Execute in order
 *
 * LOW:
 * - Optional objectives
 * - Efficiency improvements
 * - Non-critical exploration
 * - Execute when available
 *
 * USAGE PATTERN:
 *
 * 1. Create Task:
 * Task task = Task.createMoveToWaypoint("task_001", new Point2D(500, 300));
 *
 * 2. Set Requirements:
 * task.minimumBattery = 0.3;  // Need 30% battery
 * task.requiredRole = BehaviorType.SCOUT;
 *
 * 3. Assign to Agent:
 * task.assignedTo = 5;  // Agent 5 takes this task
 * task.status = TaskStatus.IN_PROGRESS;
 *
 * 4. Track Progress:
 * task.completionPercentage = 45;  // 45% done
 *
 * 5. Complete Task:
 * task.status = TaskStatus.COMPLETED;
 * task.completionTime = System.currentTimeMillis();
 *
 * INTEGRATION POINTS:
 * - Created by: User interface or mission planner
 * - Assigned by: TaskAllocator
 * - Executed by: Individual agents
 * - Tracked by: TaskAllocator and monitoring systems
 */
package com.team6.swarm.intelligence.tasking;

import com.team6.swarm.core.Point2D;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.team6.swarm.intelligence.flocking.BehaviorType;

public class Task {
    // Identification
    public String taskId;
    public TaskType taskType;
    public long createdTime;
    
    // Location (for location-based tasks)
    public Point2D targetLocation;
    public List<Point2D> areaOfInterest;  // For area-based tasks
    public Point2D startLocation;
    
    // Requirements
    public BehaviorType requiredRole;      // What type of agent needed
    public double minimumBattery;          // Minimum battery to start
    public long estimatedDuration;         // Expected time to complete (ms)
    public TaskPriority priority;
    
    // Execution
    public Map<String, Object> parameters; // Task-specific config
    public int assignedTo;                 // Agent ID (-1 = unassigned)
    public TaskStatus status;
    
    // Tracking
    public long startTime;
    public long completionTime;
    public int completionPercentage;       // 0-100
    public long lastUpdate;
    
    /**
     * Basic constructor
     */
    public Task(String taskId, TaskType taskType) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.createdTime = System.currentTimeMillis();
        this.status = TaskStatus.PENDING;
        this.assignedTo = -1;
        this.completionPercentage = 0;
        this.priority = TaskPriority.NORMAL;
        this.parameters = new HashMap<>();
        this.areaOfInterest = new ArrayList<>();
        this.minimumBattery = 0.2;  // Default: need 20% battery
        this.estimatedDuration = 60000;  // Default: 1 minute
        this.lastUpdate = createdTime;
    }
    
    /**
     * Full constructor
     */
    public Task(String taskId, TaskType taskType, Point2D targetLocation,
                TaskPriority priority, BehaviorType requiredRole,
                double minimumBattery, long estimatedDuration) {
        this(taskId, taskType);
        this.targetLocation = targetLocation;
        this.priority = priority;
        this.requiredRole = requiredRole;
        this.minimumBattery = minimumBattery;
        this.estimatedDuration = estimatedDuration;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Create simple waypoint task
     */
    public static Task createMoveToWaypoint(String taskId, Point2D target) {
        Task task = new Task(taskId, TaskType.MOVE_TO_WAYPOINT);
        task.targetLocation = target;
        task.estimatedDuration = 30000;  // 30 seconds
        task.priority = TaskPriority.NORMAL;
        return task;
    }
    
    /**
     * Create patrol task
     */
    public static Task createPatrolArea(String taskId, List<Point2D> patrolPath) {
        Task task = new Task(taskId, TaskType.PATROL_AREA);
        task.areaOfInterest = new ArrayList<>(patrolPath);
        task.parameters.put("patrolPath", patrolPath);
        task.parameters.put("loops", -1);  // Infinite loops
        task.estimatedDuration = 120000;  // 2 minutes
        task.priority = TaskPriority.NORMAL;
        return task;
    }
    
    /**
     * Create search task
     */
    public static Task createSearchPattern(String taskId, Point2D searchCenter,
                                          double searchRadius, String pattern) {
        Task task = new Task(taskId, TaskType.SEARCH_PATTERN);
        task.targetLocation = searchCenter;
        task.parameters.put("searchRadius", searchRadius);
        task.parameters.put("pattern", pattern);  // "grid", "spiral", etc.
        task.estimatedDuration = 90000;  // 1.5 minutes
        task.priority = TaskPriority.NORMAL;
        task.requiredRole = BehaviorType.SCOUT;
        return task;
    }
    
    /**
     * Create guard position task
     */
    public static Task createGuardPosition(String taskId, Point2D guardLocation,
                                          double watchRadius) {
        Task task = new Task(taskId, TaskType.GUARD_POSITION);
        task.targetLocation = guardLocation;
        task.parameters.put("watchRadius", watchRadius);
        task.estimatedDuration = 300000;  // 5 minutes
        task.priority = TaskPriority.HIGH;
        task.requiredRole = BehaviorType.GUARD;
        return task;
    }
    
    /**
     * Create follow leader task
     */
    public static Task createFollowLeader(String taskId, int leaderAgentId,
                                          double followDistance) {
        Task task = new Task(taskId, TaskType.FOLLOW_LEADER);
        task.parameters.put("leaderAgentId", leaderAgentId);
        task.parameters.put("followDistance", followDistance);
        task.estimatedDuration = -1;  // Indefinite
        task.priority = TaskPriority.NORMAL;
        task.requiredRole = BehaviorType.FOLLOWER;
        return task;
    }
    
    /**
     * Create scout ahead task
     */
    public static Task createScoutAhead(String taskId, double scoutDistance) {
        Task task = new Task(taskId, TaskType.SCOUT_AHEAD);
        task.parameters.put("scoutDistance", scoutDistance);
        task.parameters.put("reportInterval", 5000);  // Report every 5 sec
        task.estimatedDuration = 60000;  // 1 minute
        task.priority = TaskPriority.HIGH;
        task.requiredRole = BehaviorType.SCOUT;
        return task;
    }
    
    // ==================== TASK MANAGEMENT ====================
    
    /**
     * Start task execution
     */
    public void start(int agentId) {
        this.assignedTo = agentId;
        this.status = TaskStatus.IN_PROGRESS;
        this.startTime = System.currentTimeMillis();
        this.lastUpdate = startTime;
    }
    
    /**
     * Update task progress
     */
    public void updateProgress(int percentage) {
        this.completionPercentage = Math.max(0, Math.min(100, percentage));
        this.lastUpdate = System.currentTimeMillis();
        
        if (completionPercentage >= 100) {
            complete();
        }
    }
    
    /**
     * Mark task as completed
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completionPercentage = 100;
        this.completionTime = System.currentTimeMillis();
        this.lastUpdate = completionTime;
    }
    
    /**
     * Mark task as failed
     */
    public void fail(String reason) {
        this.status = TaskStatus.FAILED;
        this.parameters.put("failureReason", reason);
        this.lastUpdate = System.currentTimeMillis();
    }
    
    /**
     * Cancel task
     */
    public void cancel() {
        this.status = TaskStatus.CANCELLED;
        this.lastUpdate = System.currentTimeMillis();
    }
    
    /**
     * Block task (can't proceed due to obstacle/issue)
     */
    public void block(String reason) {
        this.status = TaskStatus.BLOCKED;
        this.parameters.put("blockReason", reason);
        this.lastUpdate = System.currentTimeMillis();
    }
    
    /**
     * Check if task is active (in progress)
     */
    public boolean isActive() {
        return status == TaskStatus.IN_PROGRESS;
    }
    
    /**
     * Check if task is complete
     */
    public boolean isComplete() {
        return status == TaskStatus.COMPLETED;
    }
    
    /**
     * Check if task is pending assignment
     */
    public boolean isPending() {
        return status == TaskStatus.PENDING && assignedTo == -1;
    }
    
    /**
     * Get time elapsed since start
     */
    public long getElapsedTime() {
        if (startTime == 0) return 0;
        if (completionTime > 0) return completionTime - startTime;
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Check if task is overdue
     */
    public boolean isOverdue() {
        if (estimatedDuration <= 0) return false;  // No time limit
        if (!isActive()) return false;
        return getElapsedTime() > estimatedDuration;
    }
    
    /**
     * Validate task has required information
     */
    public boolean validate() {
        if (taskId == null || taskId.isEmpty()) return false;
        if (taskType == null) return false;
        
        // Location-based tasks need target
        if (taskType == TaskType.MOVE_TO_WAYPOINT || 
            taskType == TaskType.GUARD_POSITION) {
            if (targetLocation == null) return false;
        }
        
        // Area-based tasks need area
        if (taskType == TaskType.PATROL_AREA || 
            taskType == TaskType.SEARCH_PATTERN) {
            if (areaOfInterest == null || areaOfInterest.isEmpty()) {
                if (targetLocation == null) return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Task[%s: %s | Status: %s | Assigned: %s | Progress: %d%% | Priority: %s]",
            taskId, taskType, status,
            assignedTo >= 0 ? "Agent " + assignedTo : "Unassigned",
            completionPercentage, priority
        );
    }
}

/**
 * TASKTYPE ENUM - Categories of work
 */
enum TaskType {
    MOVE_TO_WAYPOINT,    // Simple navigation
    PATROL_AREA,         // Area monitoring
    SEARCH_PATTERN,      // Systematic search
    MAINTAIN_FORMATION,  // Formation flying
    FOLLOW_LEADER,       // Follow another agent
    GUARD_POSITION,      // Static guard duty
    SCOUT_AHEAD          // Forward exploration
}

/**
 * TASKPRIORITY ENUM - Urgency levels
 */
enum TaskPriority {
    HIGH,      // Critical, execute immediately
    NORMAL,    // Standard priority
    LOW        // Optional, execute when available
}