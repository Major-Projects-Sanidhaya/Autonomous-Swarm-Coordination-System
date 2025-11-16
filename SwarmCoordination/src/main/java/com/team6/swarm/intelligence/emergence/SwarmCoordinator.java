/**
 * SWARMCOORDINATOR CLASS - High-Level Swarm Behavior Management
 *
 * PURPOSE:
 * - Central orchestrator for all intelligence systems
 * - Coordinates flocking, voting, tasks, formations
 * - Manages behavior priorities and conflicts
 * - Provides unified interface for swarm control
 *
 * RESPONSIBILITIES:
 *
 * 1. BEHAVIOR COORDINATION:
 *    - Integrate flocking with task execution
 *    - Maintain formations during missions
 *    - Handle emergency responses
 *    - Resolve behavior conflicts
 *
 * 2. DECISION MANAGEMENT:
 *    - Initiate votes when needed
 *    - Execute voting results
 *    - Coordinate consensus decisions
 *    - Handle decision timeouts
 *
 * 3. TASK OVERSIGHT:
 *    - Monitor task progress
 *    - Reassign failed tasks
 *    - Balance workload
 *    - Track mission completion
 *
 * 4. FORMATION CONTROL:
 *    - Setup and maintain formations
 *    - Adapt formations to situations
 *    - Handle formation transitions
 *    - Monitor formation integrity
 *
 * COORDINATION LOOP (runs 10-15 times per second):
 * 1. Get current agent states
 * 2. Update flocking behaviors
 * 3. Check for pending votes
 * 4. Monitor task progress
 * 5. Maintain formations
 * 6. Handle emergencies
 * 7. Resolve behavior conflicts
 * 8. Send movement commands
 *
 * USAGE PATTERNS:
 *
 * Initialize:
 * SwarmCoordinator coordinator = new SwarmCoordinator();
 *
 * Start Coordination:
 * coordinator.startCoordination();
 *
 * Update (called each frame):
 * coordinator.update(deltaTime, agents);
 *
 * Emergency Response:
 * coordinator.handleEmergency(EmergencyType.COLLISION, affectedAgents);
 *
 * INTEGRATION POINTS:
 * - Uses: FlockingController for swarm movement
 * - Uses: VotingSystem for decisions
 * - Uses: TaskAllocator for work distribution
 * - Uses: FormationController for geometric coordination
 * - Uses: BehaviorPriority for conflict resolution
 */
package com.team6.swarm.intelligence.emergence;

import com.team6.swarm.core.AgentState;
import com.team6.swarm.core.Point2D;
import com.team6.swarm.core.MovementCommand;
import com.team6.swarm.core.AgentStatus;
import com.team6.swarm.core.MovementType;
import com.team6.swarm.intelligence.flocking.*;
import com.team6.swarm.intelligence.voting.*;
import com.team6.swarm.intelligence.tasking.*;
import com.team6.swarm.intelligence.formation.*;
import com.team6.swarm.intelligence.coordination.*;
import java.util.*;

public class SwarmCoordinator {
    // Sub-systems
    private FlockingController flockingController;
    private VotingSystem votingSystem;
    private TaskAllocator taskAllocator;
    private FormationController formationController;
    private BehaviorPriority behaviorPriority;
    private ObstacleAvoidance obstacleAvoidance;
    private LeaderFollower leaderFollower;
    
    // State tracking
    private CoordinationMode currentMode;
    private boolean coordinationActive;
    private long lastUpdateTime;
    
    // Performance tracking
    private int updateCount;
    private double averageUpdateTime;
    private int emergencyResponseCount;
    
    /**
     * Constructor - Initialize all sub-systems
     */
    public SwarmCoordinator() {
        this.flockingController = new FlockingController();
        this.votingSystem = new VotingSystem();
        this.taskAllocator = new TaskAllocator();
        this.formationController = new FormationController();
        this.behaviorPriority = new BehaviorPriority();
        this.obstacleAvoidance = new ObstacleAvoidance();
        this.leaderFollower = new LeaderFollower();
        
        this.currentMode = CoordinationMode.AUTONOMOUS;
        this.coordinationActive = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.updateCount = 0;
        this.averageUpdateTime = 0.0;
        this.emergencyResponseCount = 0;
        
        System.out.println("SwarmCoordinator initialized with all sub-systems");
    }
    
    // ==================== COORDINATION CONTROL ====================
    
    /**
     * START COORDINATION
     * Begin intelligence coordination loop
     */
    public void startCoordination() {
        if (coordinationActive) {
            System.out.println("Coordination already active");
            return;
        }
        
        coordinationActive = true;
        lastUpdateTime = System.currentTimeMillis();
        
        System.out.println("SwarmCoordinator: Coordination started");
    }
    
    /**
     * STOP COORDINATION
     */
    public void stopCoordination() {
        coordinationActive = false;
        System.out.println("SwarmCoordinator: Coordination stopped");
    }
    
    /**
     * MAIN UPDATE LOOP
     * Called 10-15 times per second
     */
    public void update(double deltaTime, List<AgentState> agents) {
        if (!coordinationActive) return;
        
        long startTime = System.currentTimeMillis();
        updateCount++;
        
        // 1. Update flocking behaviors
        updateFlocking(agents);
        
        // 2. Check for pending votes and expire timeouts
        votingSystem.expireProposals();
        
        // 3. Monitor task progress
        updateTasks(agents);
        
        // 4. Maintain formations if active
        updateFormations(agents);
        
        // 5. Check for emergencies
        checkEmergencies(agents);
        
        // 6. Resolve behavior conflicts
        resolveBehaviorConflicts(agents);
        
        // Update performance metrics
        long updateTime = System.currentTimeMillis() - startTime;
        averageUpdateTime = (averageUpdateTime * (updateCount - 1) + updateTime) / updateCount;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    // ==================== FLOCKING COORDINATION ====================
    
    /**
     * UPDATE FLOCKING
     * Calculate flocking forces for all agents
     */
    private void updateFlocking(List<AgentState> agents) {
        for (AgentState agent : agents) {
            if (agent.status != AgentStatus.ACTIVE) continue;
            
            // Get neighbors (temporary - will use John's system)
            List<NeighborInfo> neighbors = findNeighbors(agent, agents);
            
            // Calculate flocking command
            MovementCommand flockingCmd = flockingController.calculateFlocking(
                agent.agentId, agent, neighbors);
            
            // Register with behavior priority
            behaviorPriority.registerBehavior(
                agent.agentId, 
                BehaviorType.FLOCKING,
                BehaviorType.FLOCKING.getPriority(),
                flockingCmd
            );
        }
    }
    
    /**
     * Temporary neighbor detection (will be replaced by John's system)
     */
    private List<NeighborInfo> findNeighbors(AgentState agent, List<AgentState> allAgents) {
        List<NeighborInfo> neighbors = new ArrayList<>();
        
        for (AgentState other : allAgents) {
            if (other.agentId == agent.agentId) continue;
            
            double distance = agent.position.distanceTo(other.position);
            if (distance <= agent.communicationRange) {
                neighbors.add(new NeighborInfo(
                    other.agentId,
                    other.position,
                    other.velocity,
                    distance
                ));
            }
        }
        
        return neighbors;
    }
    
    // ==================== VOTING COORDINATION ====================
    
    /**
     * INITIATE SWARM VOTE
     * Start democratic decision process
     */
    public String initiateVote(String question, List<String> options, ProposalType type) {
        return votingSystem.initiateVote(question, options, type);
    }
    
    /**
     * PROCESS AGENT VOTE
     * Record individual vote response
     */
    public void processVote(VoteResponse response) {
        votingSystem.processVote(response);
    }
    
    /**
     * CHECK VOTE STATUS
     * Get current voting state
     */
    public VoteResult checkVoteStatus(String proposalId) {
        return votingSystem.checkConsensus(proposalId);
    }
    
    // ==================== TASK COORDINATION ====================
    
    /**
     * UPDATE TASKS
     * Monitor task progress and handle completion/failure
     */
    private void updateTasks(List<AgentState> agents) {
        // Check for completed or failed assignments
        Map<String, TaskAssignment> assignments = taskAllocator.getAllAssignments();
        
        for (TaskAssignment assignment : assignments.values()) {
            if (assignment.isActive()) {
                // Check if task is overdue
                if (assignment.isOverdue()) {
                    System.out.println("Task " + assignment.task.taskId + 
                                      " overdue for Agent " + assignment.assignedAgentId);
                }
            }
        }
        
        // Check for failed agents and reassign their tasks
        List<AgentState> activeAgents = new ArrayList<>();
        for (AgentState agent : agents) {
            if (agent.status == AgentStatus.ACTIVE) {
                activeAgents.add(agent);
            } else if (agent.status == AgentStatus.FAILED) {
                // Reassign this agent's tasks
                taskAllocator.reassignAgentTasks(agent.agentId, activeAgents);
            }
        }
    }
    
    /**
     * ASSIGN TASK TO SWARM
     * Allocate work to agents
     */
    public TaskAssignment assignTask(Task task, List<AgentState> agents) {
        return taskAllocator.assignTask(task, agents);
    }
    
    /**
     * ASSIGN MULTIPLE TASKS
     */
    public List<TaskAssignment> assignTasks(List<Task> tasks, List<AgentState> agents) {
        return taskAllocator.assignTasks(tasks, agents);
    }
    
    // ==================== FORMATION COORDINATION ====================
    
    /**
     * UPDATE FORMATIONS
     * Maintain formation integrity
     */
    private void updateFormations(List<AgentState> agents) {
        // Get active formations from the formation controller
        Collection<Formation> formations = formationController.getAllFormations();

        for (Formation formation : formations) {
            // Check formation integrity
            double cohesion = formationController.getFormationCohesion(formation, agents);
            
            if (cohesion < 0.7) {
                System.out.println("Formation " + formation.formationId + 
                                  " cohesion low: " + String.format("%.2f", cohesion));
                
                // Generate correction commands
                List<MovementCommand> corrections = 
                    formationController.maintainFormation(formation, agents);
                
                // Register formation commands with priority
                for (MovementCommand cmd : corrections) {
                    behaviorPriority.registerBehavior(
                        cmd.agentId,
                        BehaviorType.FORMATION,
                        BehaviorType.FORMATION.getPriority(),
                        cmd
                    );
                }
            }
        }
    }
    
    /**
     * CREATE FORMATION
     * Setup new formation pattern
     */
    public Formation createFormation(FormationType type, Point2D center, 
                                    List<Integer> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            throw new IllegalArgumentException("agentIds must not be null or empty");
        }
        if (agentIds == null || agentIds.isEmpty()) {
            throw new IllegalArgumentException("agentIds must not be null or empty");
        }
        return formationController.createFormation(type, center, agentIds);
    }
    
    /**
     * TRANSITION FORMATION
     * Smoothly change formation shape
     */
    public void transitionFormation(String formationId, FormationType newType) {
        Formation current = formationController.getFormation(formationId);
        if (current == null) {
            throw new IllegalArgumentException("Unknown formationId: " + formationId);
        }
        // Use a sensible default duration for transitions (5 seconds)
        long defaultDurationMs = 5000L;
        formationController.transitionFormation(current, newType, defaultDurationMs);
    }
    
    // ==================== EMERGENCY HANDLING ====================
    
    /**
     * CHECK EMERGENCIES
     * Detect and respond to critical situations
     */
    private void checkEmergencies(List<AgentState> agents) {
        for (AgentState agent : agents) {
            // Check battery level
            if (agent.batteryLevel < 0.15 && agent.status == AgentStatus.ACTIVE) {
                handleEmergency(EmergencyType.CRITICAL_BATTERY,
                              Arrays.asList(agent.agentId));
            }
            // if (detectImminentCollision(agent)) {
            
            // Check for collisions (placeholder - needs sensor data)
            // if (detectImminent Collision(agent)) {
            //     handleEmergency(EmergencyType.COLLISION, Arrays.asList(agent.agentId));
            // }
        }
    }
    
    /**
     * HANDLE EMERGENCY
     * Immediate response to critical situation
     */
    public void handleEmergency(EmergencyType type, List<Integer> affectedAgentIds) {
        emergencyResponseCount++;
        
        System.out.println("EMERGENCY: " + type + " affecting " + 
                          affectedAgentIds.size() + " agents");
        
        switch (type) {
            case COLLISION:
                handleCollisionEmergency(affectedAgentIds);
                break;
                
            case CRITICAL_BATTERY:
                handleBatteryEmergency(affectedAgentIds);
                break;
                
            case SWARM_SEPARATION:
                handleSeparationEmergency(affectedAgentIds);
                break;
                
            case AGENT_FAILURE:
                handleAgentFailure(affectedAgentIds);
                break;
                
            case MISSION_ABORT:
                handleMissionAbort();
                break;
        }
    }
    
    /**
     * Handle collision emergency
     */
    private void handleCollisionEmergency(List<Integer> agentIds) {
        for (Integer agentId : agentIds) {
            // Create emergency avoidance command
            MovementCommand emergencyCmd = new MovementCommand();
            emergencyCmd.agentId = agentId;
            emergencyCmd.type = MovementType.AVOID_OBSTACLE;
            emergencyCmd.parameters.put("emergencyStop", true);
            
            // Register with highest priority
            behaviorPriority.registerBehavior(
                agentId,
                BehaviorType.EVADING,
                100,  // Emergency priority
                emergencyCmd
            );
        }
    }
    
    /**
     * Handle battery emergency
     */
    private void handleBatteryEmergency(List<Integer> agentIds) {
        // Initiate vote for mission continuation
        String proposalId = votingSystem.initiateVote(
            "Critical battery levels detected. Continue mission or return all?",
            Arrays.asList("RETURN_ALL", "RETURN_LOW_ONLY", "CONTINUE"),
            ProposalType.EMERGENCY
        );
        
        System.out.println("Battery emergency vote initiated: " + proposalId);
        
        // For now, return low battery agents
        for (Integer agentId : agentIds) {
            MovementCommand returnCmd = new MovementCommand();
            returnCmd.agentId = agentId;
            returnCmd.type = MovementType.MOVE_TO_TARGET;
            returnCmd.parameters.put("target", new Point2D(50, 50));  // Base location
            returnCmd.parameters.put("reason", "BATTERY_LOW");
            
            behaviorPriority.registerBehavior(
                agentId,
                BehaviorType.RETURNING,
                90,  // Critical priority
                returnCmd
            );
        }
    }
    
    /**
     * Handle swarm separation
     */
    private void handleSeparationEmergency(List<Integer> agentIds) {
        System.out.println("Swarm separation detected - initiating regrouping");
        
        // Calculate swarm center
        // Apply cohesion forces to separated agents
        // May need to pause mission temporarily
    }
    
    /**
     * Handle agent failure
     */
    private void handleAgentFailure(List<Integer> agentIds) {
        for (Integer agentId : agentIds) {
            System.out.println("Agent " + agentId + " failed - reassigning tasks");
            
            // Tasks will be reassigned in updateTasks()
            // Check if failed agent was leader
            if (leaderFollower.isLeader(agentId)) {
                System.out.println("Leader failed! Initiating succession...");
                // Leadership transition handled by LeaderFollower
            }
        }
    }
    
    /**
     * Handle mission abort
     */
    private void handleMissionAbort() {
        System.out.println("MISSION ABORT - All agents return to base");
        
        // Cancel all tasks
        // Set all agents to RETURNING
        // Formation: tight column for efficient return
        
        setCoordinationMode(CoordinationMode.EMERGENCY);
    }
    
    // ==================== BEHAVIOR CONFLICT RESOLUTION ====================
    
    /**
     * RESOLVE BEHAVIOR CONFLICTS
     * Apply priority system to agent commands
     */
    private void resolveBehaviorConflicts(List<AgentState> agents) {
        for (AgentState agent : agents) {
            if (agent.status != AgentStatus.ACTIVE) continue;
            
            // Get resolved command for this agent
            MovementCommand finalCommand = behaviorPriority.resolveConflicts(agent.agentId);
            
            if (finalCommand != null) {
                // Send command to agent (integration point with Sanidhaya's system)
                // agent.addMovementCommand(finalCommand);
            }
        }
    }
    
    // ==================== COORDINATION MODES ====================
    
    /**
     * SET COORDINATION MODE
     * Change overall swarm behavior mode
     */
    public void setCoordinationMode(CoordinationMode mode) {
        if (currentMode == mode) return;
        
        System.out.println("Coordination mode changing: " + currentMode + " -> " + mode);
        
        currentMode = mode;
        
        // Apply mode-specific adjustments
        switch (mode) {
            case AUTONOMOUS:
                // Agents make independent decisions
                enableAutonomousBehavior();
                break;
                
            case FORMATION_STRICT:
                // Strict formation maintenance
                enableStrictFormation();
                break;
                
            case EXPLORATION:
                // Spread out, maximize coverage
                enableExplorationMode();
                break;
                
            case EMERGENCY:
                // Safety first, return to base
                enableEmergencyMode();
                break;
                
            case MISSION_FOCUSED:
                // Task completion priority
                enableMissionMode();
                break;
        }
    }
    
    /**
     * Enable autonomous behavior
     */
    private void enableAutonomousBehavior() {
        // Increase flocking weights
        FlockingParameters params = flockingController.getParameters();
        params.cohesionWeight = 1.0;
        params.alignmentWeight = 1.0;
        params.separationWeight = 1.5;
        flockingController.updateParameters(params);
        
        System.out.println("Autonomous mode: Balanced flocking enabled");
    }
    
    /**
     * Enable strict formation
     */
    private void enableStrictFormation() {
        // Reduce flocking, increase formation correction
        FlockingParameters params = flockingController.getParameters();
        params.cohesionWeight = 0.5;
        params.alignmentWeight = 1.5;
        params.separationWeight = 2.0;
        flockingController.updateParameters(params);
        
        System.out.println("Formation mode: Strict positioning enabled");
    }
    
    /**
     * Enable exploration mode
     */
    private void enableExplorationMode() {
        // Reduce cohesion, increase exploration
        FlockingParameters params = flockingController.getParameters();
        params.cohesionWeight = 0.7;
        params.alignmentWeight = 0.8;
        params.separationWeight = 1.2;
        flockingController.updateParameters(params);
        
        System.out.println("Exploration mode: Spreading enabled");
    }
    
    /**
     * Enable emergency mode
     */
    private void enableEmergencyMode() {
        // Maximum cohesion, tight formation
        FlockingParameters params = flockingController.getParameters();
        params.cohesionWeight = 2.0;
        params.alignmentWeight = 1.8;
        params.separationWeight = 2.5;
        params.maxSpeed = 60.0;  // Faster movement
        flockingController.updateParameters(params);
        
        System.out.println("Emergency mode: Maximum cohesion, faster movement");
    }
    
    /**
     * Enable mission mode
     */
    private void enableMissionMode() {
        // Task priority, moderate flocking
        FlockingParameters params = flockingController.getParameters();
        params.cohesionWeight = 0.8;
        params.alignmentWeight = 1.0;
        params.separationWeight = 1.5;
        flockingController.updateParameters(params);
        
        System.out.println("Mission mode: Task-focused coordination");
    }
    
    // ==================== SUB-SYSTEM ACCESS ====================
    
    public FlockingController getFlockingController() {
        return flockingController;
    }
    
    public VotingSystem getVotingSystem() {
        return votingSystem;
    }
    
    public TaskAllocator getTaskAllocator() {
        return taskAllocator;
    }
    
    public FormationController getFormationController() {
        return formationController;
    }
    
    public BehaviorPriority getBehaviorPriority() {
        return behaviorPriority;
    }
    
    public ObstacleAvoidance getObstacleAvoidance() {
        return obstacleAvoidance;
    }
    
    public LeaderFollower getLeaderFollower() {
        return leaderFollower;
    }
    
    // ==================== STATUS QUERIES ====================
    
    public CoordinationMode getCurrentMode() {
        return currentMode;
    }
    
    public boolean isCoordinationActive() {
        return coordinationActive;
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public int getUpdateCount() {
        return updateCount;
    }
    
    public double getAverageUpdateTime() {
        return averageUpdateTime;
    }
    
    public int getEmergencyResponseCount() {
        return emergencyResponseCount;
    }
    
    /**
     * GET SYSTEM STATUS
     * Comprehensive status report
     */
    public CoordinatorStatus getStatus() {
    CoordinatorStatus status = new CoordinatorStatus();
    // Use package-private setters to populate the status snapshot (fields are intentionally encapsulated).
    status.setMode(currentMode);
    status.setActive(coordinationActive);
    status.setUpdateCount(updateCount);
    status.setAverageUpdateTime(averageUpdateTime);
    status.setEmergencyCount(emergencyResponseCount);

    status.setTotalTasksAssigned(taskAllocator.getTotalTasksAssigned());
    status.setTotalVotesProcessed(votingSystem.getTotalVotesProcessed());
    status.setFlockingCalculations(flockingController.getCalculationsPerformed());
    status.setBehaviorConflicts(behaviorPriority.getTotalConflicts());
        
        return status;
    }
    
    /**
     * RESET METRICS
     */
    public void resetMetrics() {
        updateCount = 0;
        averageUpdateTime = 0.0;
        emergencyResponseCount = 0;
        
        flockingController.resetPerformanceMetrics();
        votingSystem.resetPerformanceMetrics();
        taskAllocator.resetMetrics();
        behaviorPriority.resetMetrics();
    }
    
    @Override
    public String toString() {
        return String.format(
            "SwarmCoordinator[Mode: %s | Active: %s | Updates: %d | Avg Time: %.2fms | Emergencies: %d]",
            currentMode, coordinationActive, updateCount, averageUpdateTime, emergencyResponseCount
        );
    }
}

