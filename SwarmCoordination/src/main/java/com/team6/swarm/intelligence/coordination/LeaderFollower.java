/**
 * LEADERFOLLOWER CLASS - Hierarchical Coordination Pattern
 *
 * PURPOSE:
 * - Implement leader-follower coordination for swarm
 * - Enable hierarchical decision-making structure
 * - Maintain formation relative to designated leader
 * - Handle leader failure and automatic succession
 */
package com.team6.swarm.intelligence.coordination;

import com.team6.swarm.core.*;
import java.util.*;

public class LeaderFollower {
    // Leadership tracking
    private int currentLeaderId;
    private LeaderSelectionReason selectionReason;
    private long leadershipStartTime;
    private int leadershipChangeCount;
    
    // Follower management
    private Map<Integer, FollowerState> followers;
    private List<Integer> leadershipHistory;
    
    // Formation parameters
    private static final double DEFAULT_FOLLOW_DISTANCE = 50.0;
    private static final double POSITION_TOLERANCE = 10.0;
    private static final double SMOOTH_FACTOR = 0.3;
    
    // Leader monitoring
    private long lastLeaderUpdate;
    private static final long LEADER_TIMEOUT = 5000;
    
    public LeaderFollower() {
        this.currentLeaderId = -1;
        this.followers = new HashMap<>();
        this.leadershipHistory = new ArrayList<>();
        this.leadershipChangeCount = 0;
        this.lastLeaderUpdate = System.currentTimeMillis();
    }
    
    /**
     * SET LEADER
     */
    public void setLeader(int agentId, LeaderSelectionReason reason) {
        if (currentLeaderId == agentId) {
            System.out.println("Agent " + agentId + " is already the leader");
            return;
        }
        
        if (currentLeaderId != -1) {
            leadershipHistory.add(currentLeaderId);
        }
        
        currentLeaderId = agentId;
        selectionReason = reason;
        leadershipStartTime = System.currentTimeMillis();
        leadershipChangeCount++;
        lastLeaderUpdate = System.currentTimeMillis();
        
        System.out.println(String.format(
            "Leader changed to Agent %d (Reason: %s) - Leadership change #%d",
            agentId, reason, leadershipChangeCount
        ));
    }
    
    /**
     * SELECT LEADER AUTOMATICALLY
     */
    public void selectLeader(List<AgentState> agents, LeaderSelectionReason reason) {
        if (agents == null || agents.isEmpty()) {
            System.err.println("Cannot select leader: no agents available");
            return;
        }
        
        AgentState bestAgent = null;
        double bestScore = -1;
        
        for (AgentState agent : agents) {
            if (agent.status != AgentStatus.ACTIVE) {
                continue;
            }
            
            double score = scoreAgentForLeadership(agent, agents, reason);
            
            if (score > bestScore) {
                bestScore = score;
                bestAgent = agent;
            }
        }
        
        if (bestAgent != null) {
            setLeader(bestAgent.agentId, reason);
        } else {
            System.err.println("No suitable leader found");
        }
    }
    
    /**
     * SCORE AGENT FOR LEADERSHIP
     */
    private double scoreAgentForLeadership(AgentState agent, List<AgentState> allAgents,
                                          LeaderSelectionReason reason) {
        double score = 0;
        
        switch (reason) {
            case HIGHEST_BATTERY:
                score = agent.batteryLevel * 100;
                break;
                
            case CENTRAL_POSITION:
                Point2D swarmCenter = calculateSwarmCenter(allAgents);
                double distanceToCenter = agent.position.distanceTo(swarmCenter);
                score = 100 - distanceToCenter;
                break;
                
            case BEST_SENSORS:
                score = 50;
                break;
                
            case LOWEST_ID:
                score = 1000 - agent.agentId;
                break;
                
            case AUTO:
                score = agent.batteryLevel * 40;
                Point2D center = calculateSwarmCenter(allAgents);
                double dist = agent.position.distanceTo(center);
                score += Math.max(0, 100 - dist) * 0.3;
                score += (1000 - agent.agentId) * 0.01;
                break;
                
            default:
                score = agent.batteryLevel * 50;
        }
        
        return score;
    }
    
    /**
     * CHECK LEADER STATUS
     */
    public boolean isLeaderHealthy(List<AgentState> agents) {
        if (currentLeaderId == -1) {
            return false;
        }
        
        AgentState leader = null;
        for (AgentState agent : agents) {
            if (agent.agentId == currentLeaderId) {
                leader = agent;
                break;
            }
        }
        
        if (leader == null) {
            System.err.println("Leader Agent " + currentLeaderId + " not found!");
            return false;
        }
        
        if (leader.status != AgentStatus.ACTIVE) {
            System.err.println("Leader Agent " + currentLeaderId + " is not active: " + leader.status);
            return false;
        }
        
        if (leader.batteryLevel < 0.2) {
            System.err.println("Leader Agent " + currentLeaderId + " has low battery: " + 
                             (leader.batteryLevel * 100) + "%");
            return false;
        }
        
        long timeSinceUpdate = System.currentTimeMillis() - lastLeaderUpdate;
        if (timeSinceUpdate > LEADER_TIMEOUT) {
            System.err.println("Leader Agent " + currentLeaderId + " timeout: " + 
                              timeSinceUpdate + "ms since last update");
            return false;
        }
        
        return true;
    }
    
    /**
     * HANDLE LEADER FAILURE
     */
    public void handleLeaderFailure(List<AgentState> remainingAgents) {
        System.out.println("Leader failure detected! Selecting new leader...");
        
        remainingAgents.removeIf(a -> a.agentId == currentLeaderId);
        
        if (remainingAgents.isEmpty()) {
            System.err.println("No agents remaining to become leader!");
            currentLeaderId = -1;
            return;
        }
        
        selectLeader(remainingAgents, LeaderSelectionReason.HIGHEST_BATTERY);
        
        if (currentLeaderId != -1) {
            updateFollowerOffsets(remainingAgents);
        }
    }
    
    public void updateLeaderStatus() {
        lastLeaderUpdate = System.currentTimeMillis();
    }
    
    /**
     * ADD FOLLOWER
     */
    public void addFollower(int agentId, double offsetX, double offsetY) {
        FollowerState state = new FollowerState(agentId);
        state.offsetFromLeader = new Vector2D(offsetX, offsetY);
        double dist = Math.sqrt(offsetX * offsetX + offsetY * offsetY);
        // If offsets are zero (agent exactly at leader), use default follow distance
        state.followDistance = dist < 0.001 ? DEFAULT_FOLLOW_DISTANCE : dist;
        
        followers.put(agentId, state);
        
        System.out.println(String.format(
            "Agent %d added as follower (Offset: %.1f, %.1f)",
            agentId, offsetX, offsetY
        ));
    }
    
    public void addFollowerAuto(int agentId, Point2D agentPosition, Point2D leaderPosition) {
        double offsetX = agentPosition.x - leaderPosition.x;
        double offsetY = agentPosition.y - leaderPosition.y;
        addFollower(agentId, offsetX, offsetY);
    }
    
    public void removeFollower(int agentId) {
        followers.remove(agentId);
        System.out.println("Agent " + agentId + " removed from followers");
    }
    
    /**
     * UPDATE FOLLOWER OFFSETS
     */
    private void updateFollowerOffsets(List<AgentState> agents) {
        AgentState newLeader = null;
        for (AgentState agent : agents) {
            if (agent.agentId == currentLeaderId) {
                newLeader = agent;
                break;
            }
        }
        
        if (newLeader == null) return;
        
        for (AgentState agent : agents) {
            if (agent.agentId == currentLeaderId) continue;
            
            FollowerState state = followers.get(agent.agentId);
            if (state != null) {
                double offsetX = agent.position.x - newLeader.position.x;
                double offsetY = agent.position.y - newLeader.position.y;
                state.offsetFromLeader = new Vector2D(offsetX, offsetY);
                state.followDistance = Math.sqrt(offsetX * offsetX + offsetY * offsetY);
            }
        }
        
        System.out.println("Follower offsets updated for new leader");
    }
    
    /**
     * UPDATE FOLLOWERS
     */
    public List<MovementCommand> updateFollowers(AgentState leader, 
                                                List<AgentState> followerAgents) {
        List<MovementCommand> commands = new ArrayList<>();
        
        if (leader == null || leader.agentId != currentLeaderId) {
            System.err.println("Invalid leader provided for follower update");
            return commands;
        }
        
        updateLeaderStatus();
        
        for (AgentState follower : followerAgents) {
            if (follower.agentId == currentLeaderId) continue;
            
            FollowerState state = followers.get(follower.agentId);
            if (state == null) {
                addFollowerAuto(follower.agentId, follower.position, leader.position);
                state = followers.get(follower.agentId);
            }
            
            Point2D targetPosition = new Point2D(
                leader.position.x + state.offsetFromLeader.x,
                leader.position.y + state.offsetFromLeader.y
            );
            
            double positionError = follower.position.distanceTo(targetPosition);
            
            if (positionError > POSITION_TOLERANCE) {
                Point2D smoothTarget = interpolatePosition(
                    follower.position, targetPosition, SMOOTH_FACTOR);
                
                MovementCommand cmd = new MovementCommand();
                cmd.agentId = follower.agentId;
                cmd.type = MovementType.MOVE_TO_TARGET;
                cmd.parameters.put("target", smoothTarget);
                
                commands.add(cmd);
                state.lastCorrectionTime = System.currentTimeMillis();
                state.correctionCount++;
            }
        }
        
        return commands;
    }
    
    /**
     * CALCULATE FOLLOWER TARGET VELOCITY
     */
    public Vector2D calculateFollowerVelocity(AgentState follower, AgentState leader) {
        FollowerState state = followers.get(follower.agentId);
        if (state == null) {
            return new Vector2D(0, 0);
        }
        
        Point2D targetPosition = new Point2D(
            leader.position.x + state.offsetFromLeader.x,
            leader.position.y + state.offsetFromLeader.y
        );
        
        Vector2D toTarget = new Vector2D(
            targetPosition.x - follower.position.x,
            targetPosition.y - follower.position.y
        );
        
        double distance = toTarget.magnitude();
        if (distance < 0.001) {
            return leader.velocity;
        }
        
        toTarget = new Vector2D(
            toTarget.x / distance,
            toTarget.y / distance
        );
        
        double desiredSpeed = leader.velocity.magnitude();
        if (distance > POSITION_TOLERANCE) {
            desiredSpeed += distance * 0.1;
        }
        
        return new Vector2D(
            toTarget.x * desiredSpeed,
            toTarget.y * desiredSpeed
        );
    }
    
    /**
     * INTERPOLATE POSITION
     */
    private Point2D interpolatePosition(Point2D current, Point2D target, double factor) {
        double newX = current.x + (target.x - current.x) * factor;
        double newY = current.y + (target.y - current.y) * factor;
        return new Point2D(newX, newY);
    }
    
    /**
     * CALCULATE SWARM CENTER
     */
    private Point2D calculateSwarmCenter(List<AgentState> agents) {
        double sumX = 0;
        double sumY = 0;
        
        for (AgentState agent : agents) {
            sumX += agent.position.x;
            sumY += agent.position.y;
        }
        
        if (agents.size() > 0) {
            sumX /= agents.size();
            sumY /= agents.size();
        }
        
        return new Point2D(sumX, sumY);
    }
    
    /**
     * SETUP V-FORMATION
     */
    public void setupVFormation(List<AgentState> agents, double spacing) {
        if (agents.isEmpty()) return;
        
        AgentState leader = agents.get(0);
        setLeader(leader.agentId, LeaderSelectionReason.MANUAL);
        
        int leftSide = 0;
        int rightSide = 0;
        
        for (int i = 1; i < agents.size(); i++) {
            AgentState agent = agents.get(i);
            
            if (i % 2 == 1) {
                leftSide++;
                double offsetX = -spacing * leftSide * 0.7;
                double offsetY = -spacing * leftSide;
                addFollower(agent.agentId, offsetX, offsetY);
            } else {
                rightSide++;
                double offsetX = spacing * rightSide * 0.7;
                double offsetY = -spacing * rightSide;
                addFollower(agent.agentId, offsetX, offsetY);
            }
        }
        
        System.out.println(String.format(
            "V-Formation established: 1 leader + %d followers",
            agents.size() - 1
        ));
    }
    
    /**
     * SETUP LINE FORMATION
     */
    public void setupLineFormation(List<AgentState> agents, double spacing) {
        if (agents.isEmpty()) return;
        
        AgentState leader = agents.get(0);
        setLeader(leader.agentId, LeaderSelectionReason.MANUAL);
        
        for (int i = 1; i < agents.size(); i++) {
            AgentState agent = agents.get(i);
            double offsetX = 0;
            double offsetY = -spacing * i;
            addFollower(agent.agentId, offsetX, offsetY);
        }
        
        System.out.println(String.format(
            "Line Formation established: 1 leader + %d followers",
            agents.size() - 1
        ));
    }
    
    /**
     * SETUP COLUMN FORMATION
     */
    public void setupColumnFormation(List<AgentState> agents, double spacing) {
        if (agents.isEmpty()) return;
        
        AgentState leader = agents.get(0);
        setLeader(leader.agentId, LeaderSelectionReason.MANUAL);
        
        int leftColumn = 0;
        int rightColumn = 0;
        
        for (int i = 1; i < agents.size(); i++) {
            AgentState agent = agents.get(i);
            
            if (i % 2 == 1) {
                leftColumn++;
                double offsetX = -spacing * 0.5;
                double offsetY = -spacing * leftColumn;
                addFollower(agent.agentId, offsetX, offsetY);
            } else {
                rightColumn++;
                double offsetX = spacing * 0.5;
                double offsetY = -spacing * rightColumn;
                addFollower(agent.agentId, offsetX, offsetY);
            }
        }
        
        System.out.println(String.format(
            "Column Formation established: 1 leader + %d followers in 2 columns",
            agents.size() - 1
        ));
    }
    
    // ==================== QUERY METHODS ====================
    
    public int getCurrentLeader() {
        return currentLeaderId;
    }
    
    public AgentState getLeaderState(List<AgentState> agents) {
        for (AgentState agent : agents) {
            if (agent.agentId == currentLeaderId) {
                return agent;
            }
        }
        return null;
    }
    
    public boolean isLeader(int agentId) {
        return agentId == currentLeaderId;
    }
    
    public int getFollowerCount() {
        return followers.size();
    }
    
    public List<Integer> getFollowerIds() {
        return new ArrayList<>(followers.keySet());
    }
    
    public FollowerState getFollowerState(int agentId) {
        return followers.get(agentId);
    }
    
    public List<Integer> getLeadershipHistory() {
        return new ArrayList<>(leadershipHistory);
    }

    /**
     * Get the reason the current leader was selected (may be null if none)
     */
    public LeaderSelectionReason getSelectionReason() {
        return selectionReason;
    }
    
    public long getLeadershipDuration() {
        if (leadershipStartTime == 0) return 0;
        return System.currentTimeMillis() - leadershipStartTime;
    }
    
    /**
     * GET FORMATION COHESION
     */
    public double getFormationCohesion(List<AgentState> agents) {
        if (followers.isEmpty()) return 1.0;
        
        AgentState leader = getLeaderState(agents);
        if (leader == null) return 0.0;
        
        double totalError = 0;
        int count = 0;
        
        for (AgentState agent : agents) {
            if (agent.agentId == currentLeaderId) continue;
            
            FollowerState state = followers.get(agent.agentId);
            if (state == null) continue;
            
            Point2D expectedPosition = new Point2D(
                leader.position.x + state.offsetFromLeader.x,
                leader.position.y + state.offsetFromLeader.y
            );
            
            double error = agent.position.distanceTo(expectedPosition);
            totalError += error;
            count++;
        }
        
        if (count == 0) return 1.0;
        
        double avgError = totalError / count;
        double cohesion = Math.max(0, 1.0 - (avgError / 100.0));
        
        return cohesion;
    }
    
    public void clearFollowers() {
        followers.clear();
        System.out.println("All followers cleared");
    }
    
    public void reset() {
        currentLeaderId = -1;
        followers.clear();
        leadershipHistory.clear();
        leadershipChangeCount = 0;
        System.out.println("Leader-Follower system reset");
    }
    
    @Override
    public String toString() {
        return String.format(
            "LeaderFollower[Leader: %s | Followers: %d | Changes: %d | Duration: %ds]",
            currentLeaderId >= 0 ? "Agent " + currentLeaderId : "None",
            followers.size(),
            leadershipChangeCount,
            getLeadershipDuration() / 1000
        );
    }
}