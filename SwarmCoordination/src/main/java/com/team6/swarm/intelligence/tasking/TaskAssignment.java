/**
 * TASKASSIGNMENT CLASS - Links Task to Specific Agent
 *
 * PURPOSE:
 * - Binds a task to the agent performing it
 * - Tracks assignment history and performance
 * - Enables workload monitoring and reassignment
 *
 * ASSIGNMENT STRUCTURE:
 *
 * CORE DATA:
 * - task: The work to be done
 * - assignedAgentId: Which agent has this task
 * - assignmentTime: When assignment was made
 * - acceptedTime: When agent acknowledged task
 *
 * STATUS TRACKING:
 * - status: Current task state (from TaskStatus enum)
 * - completionPercentage: How much done (0-100)
 * - lastUpdate: Most recent status change
 * - estimatedCompletion: Expected finish time
 *
 * PERFORMANCE:
 * - actualDuration: How long task actually took
 * - efficiency: Performance vs estimate (1.0 = on time)
 * - failureCount: Number of times task failed
 * - reassignmentCount: How many times reassigned
 *
 * ASSIGNMENT REASONS:
 *
 * NEAREST_AGENT:
 * - Agent closest to task location
 * - Minimizes travel time
 * - Most common assignment
 *
 * ROLE_MATCH:
 * - Agent has required role (SCOUT, GUARD)
 * - Capability-based selection
 * - Ensures task success
 *
 * LOAD_BALANCE:
 * - Agent has fewest current tasks
 * - Prevents agent overload
 * - Fair work distribution
 *
 * BATTERY_OPTIMAL:
 * - Agent has sufficient battery
 * - Long tasks to high-battery agents
 * - Mission completion focus
 *
 * MANUAL:
 * - User explicitly assigned task
 * - Override automatic allocation
 * - Special mission requirements
 *
 * LIFECYCLE:
 *
 * 1. Creation:
 * TaskAssignment assignment = new TaskAssignment(task, agentId);
 * assignment.assignmentReason = AssignmentReason.NEAREST_AGENT;
 *
 * 2. Acceptance:
 * assignment.accept();  // Agent acknowledges task
 *
 * 3. Progress Updates:
 * assignment.updateProgress(45);  // 45% complete
 *
 * 4. Completion:
 * assignment.complete();  // Task finished
 * // OR
 * assignment.fail("Battery depleted");  // Task failed
 *
 * 5. Performance Tracking:
 * double efficiency = assignment.getEfficiency();
 * long duration = assignment.getActualDuration();
 *
 * REASSIGNMENT:
 *
 * If agent fails or becomes unavailable:
 * TaskAssignment newAssignment = assignment.reassign(newAgentId);
 * newAssignment.reassignmentCount++;
 *
 * USAGE PATTERNS:
 *
 * Track Agent Workload:
 * int agentTaskCount = assignments.stream()
 *     .filter(a -> a.assignedAgentId == agentId)
 *     .filter(a -> a.status.isActive())
 *     .count();
 *
 * Find Overdue Tasks:
 * List<TaskAssignment> overdue = assignments.stream()
 *     .filter(a -> a.isOverdue())
 *     .collect(Collectors.toList());
 *
 * Calculate Agent Efficiency:
 * double avgEfficiency = assignments.stream()
 *     .filter(a -> a.assignedAgentId == agentId)
 *     .filter(a -> a.status == TaskStatus.COMPLETED)
 *     .mapToDouble(a -> a.getEfficiency())
 *     .average().orElse(1.0);
 *
 * INTEGRATION POINTS:
 * - Created by: TaskAllocator
 * - Modified by: Agents during execution
 * - Read by: Performance monitoring
 * - Used by: Anthony's UI for workload display
 */
package com.team6.swarm.intelligence.tasking;

public class TaskAssignment {
    // Core assignment data
    public Task task;
    public int assignedAgentId;
    public long assignmentTime;
    public long acceptedTime;
    
    // Status tracking
    public TaskStatus status;
    public int completionPercentage;
    public long lastUpdate;
    public long estimatedCompletion;
    
    // Performance metrics
    public long actualDuration;
    public double efficiency;  // 1.0 = perfect, < 1.0 = faster, > 1.0 = slower
    public int failureCount;
    public int reassignmentCount;
    
    // Assignment details
    public AssignmentReason assignmentReason;
    public double assignmentScore;  // Score from allocation algorithm
    
    // Back-reference to allocator so assignment can notify when it becomes terminal
    private TaskAllocator allocator;
    
    /**
     * Constructor for new assignment
     */
    public TaskAssignment(Task task, int agentId) {
        this.task = task;
        this.assignedAgentId = agentId;
        this.assignmentTime = System.currentTimeMillis();
        this.acceptedTime = 0;
        this.status = TaskStatus.PENDING;
        this.completionPercentage = 0;
        this.lastUpdate = assignmentTime;
        this.failureCount = 0;
        this.reassignmentCount = 0;
        this.efficiency = 1.0;
        
        // Calculate estimated completion
        if (task.estimatedDuration > 0) {
            this.estimatedCompletion = assignmentTime + task.estimatedDuration;
        } else {
            this.estimatedCompletion = 0;  // No time limit
        }
    }
    
    /**
     * Full constructor with assignment reason
     */
    public TaskAssignment(Task task, int agentId, AssignmentReason reason, double score) {
        this(task, agentId);
        this.assignmentReason = reason;
        this.assignmentScore = score;
    }
    
    // ==================== LIFECYCLE METHODS ====================
    
    /**
     * Agent accepts the task
     */
    public void accept() {
        this.acceptedTime = System.currentTimeMillis();
        this.status = TaskStatus.IN_PROGRESS;
        this.lastUpdate = acceptedTime;
        
        // Update task itself
        task.start(assignedAgentId);
    }
    
    /**
     * Update task progress
     */
    public void updateProgress(int percentage) {
        this.completionPercentage = Math.max(0, Math.min(100, percentage));
        this.lastUpdate = System.currentTimeMillis();
        
        // Update task itself
        task.updateProgress(percentage);
        this.status = task.status;
        
        if (completionPercentage >= 100) {
            complete();
        }
    }
    
    /**
     * Mark assignment as completed
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completionPercentage = 100;
        this.lastUpdate = System.currentTimeMillis();
        
        // Calculate actual duration and efficiency
        if (acceptedTime > 0) {
            this.actualDuration = lastUpdate - acceptedTime;
            
            if (task.estimatedDuration > 0) {
                this.efficiency = (double) actualDuration / task.estimatedDuration;
            }
        }
        
        // Update task itself
        task.complete();

        // Notify allocator so it can remove this assignment from agent workload
        if (allocator != null) {
            allocator.notifyAssignmentTerminal(this);
        }
    }
    
    /**
     * Mark assignment as failed
     */
    public void fail(String reason) {
        this.status = TaskStatus.FAILED;
        this.failureCount++;
        this.lastUpdate = System.currentTimeMillis();
        
        // Update task itself
        task.fail(reason);

        // Notify allocator to remove from workload
        if (allocator != null) {
            allocator.notifyAssignmentTerminal(this);
        }
    }
    
    /**
     * Cancel assignment
     */
    public void cancel() {
        this.status = TaskStatus.CANCELLED;
        this.lastUpdate = System.currentTimeMillis();
        
        // Update task itself
        task.cancel();

        // Notify allocator to remove from workload
        if (allocator != null) {
            allocator.notifyAssignmentTerminal(this);
        }
    }
    
    /**
     * Block assignment temporarily
     */
    public void block(String reason) {
        this.status = TaskStatus.BLOCKED;
        this.lastUpdate = System.currentTimeMillis();
        
        // Update task itself
        task.block(reason);
    }
    
    /**
     * Resume blocked assignment
     */
    public void resume() {
        if (this.status == TaskStatus.BLOCKED) {
            this.status = TaskStatus.IN_PROGRESS;
            task.status = TaskStatus.IN_PROGRESS;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Check if assignment is active
     */
    public boolean isActive() {
        return status.isActive();
    }
    
    /**
     * Check if assignment is complete
     */
    public boolean isComplete() {
        return status == TaskStatus.COMPLETED;
    }
    
    /**
     * Check if assignment is overdue
     */
    public boolean isOverdue() {
        if (estimatedCompletion == 0) return false;  // No deadline
        if (status.isTerminal()) return false;  // Already finished
        return System.currentTimeMillis() > estimatedCompletion;
    }
    
    /**
     * Get time since assignment
     */
    public long getTimeSinceAssignment() {
        return System.currentTimeMillis() - assignmentTime;
    }
    
    /**
     * Get time since acceptance
     */
    public long getTimeSinceAcceptance() {
        if (acceptedTime == 0) return 0;
        if (status.isTerminal() && lastUpdate > acceptedTime) {
            return lastUpdate - acceptedTime;
        }
        return System.currentTimeMillis() - acceptedTime;
    }
    
    /**
     * Get actual duration (only valid after completion)
     */
    public long getActualDuration() {
        return actualDuration;
    }
    
    /**
     * Get efficiency rating
     */
    public double getEfficiency() {
        return efficiency;
    }
    
    /**
     * Get time remaining until deadline
     */
    public long getTimeRemaining() {
        if (estimatedCompletion == 0) return -1;  // No deadline
        long remaining = estimatedCompletion - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * Get urgency level (0.0 = not urgent, 1.0 = very urgent)
     */
    public double getUrgency() {
        if (estimatedCompletion == 0) return 0.0;
        
        long timeRemaining = getTimeRemaining();
        long totalTime = task.estimatedDuration;
        
        if (totalTime <= 0) return 0.0;
        
        double urgency = 1.0 - ((double) timeRemaining / totalTime);
        return Math.max(0.0, Math.min(1.0, urgency));
    }
    
    // ==================== REASSIGNMENT ====================
    
    /**
     * Create new assignment for different agent
     * Used when original agent fails or becomes unavailable
     */
    public TaskAssignment reassign(int newAgentId, AssignmentReason reason) {
        // Mark this assignment as cancelled
        this.cancel();
        
        // Create new assignment
        TaskAssignment newAssignment = new TaskAssignment(task, newAgentId, reason, 0);
        newAssignment.reassignmentCount = this.reassignmentCount + 1;
        newAssignment.failureCount = this.failureCount;
        
        // Reset task to pending
        task.status = TaskStatus.PENDING;
        task.assignedTo = newAgentId;
        task.completionPercentage = 0;
        
        return newAssignment;
    }

    /**
     * Set a reference to the allocator that created this assignment.
     * The allocator will be notified when the assignment reaches a terminal state
     * so it can remove the assignment from per-agent workload lists.
     */
    public void setAllocator(TaskAllocator allocator) {
        this.allocator = allocator;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TaskAssignment[Task: %s | Agent: %d | Status: %s | Progress: %d%% | Efficiency: %.2f]",
            task.taskId, assignedAgentId, status, completionPercentage, efficiency
        );
    }
}
