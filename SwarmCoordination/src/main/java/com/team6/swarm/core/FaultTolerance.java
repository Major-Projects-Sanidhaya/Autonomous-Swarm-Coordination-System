package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.*;

/**
 * Week 8 Implementation: FaultTolerance
 *
 * Provides fault detection, automatic recovery, and resilience features
 * for the swarm system to handle agent failures gracefully.
 *
 * Key Features:
 * - Heartbeat monitoring for failure detection
 * - Automatic agent recovery and restart
 * - Failure history tracking
 * - Health status monitoring
 * - Graceful degradation
 * - Circuit breaker pattern
 *
 * @author Team 6
 * @version Week 8
 */
public class FaultTolerance {

    private static final long HEARTBEAT_INTERVAL_MS = 1000; // 1 second
    private static final long HEARTBEAT_TIMEOUT_MS = 5000; // 5 seconds
    private static final int MAX_RECOVERY_ATTEMPTS = 3;
    private static final long RECOVERY_BACKOFF_MS = 2000; // 2 seconds

    private final Map<String, AgentHealthStatus> agentHealth;
    private final Map<String, Long> lastHeartbeat;
    private final Map<String, Integer> recoveryAttempts;
    private final List<FailureEvent> failureHistory;
    private final ScheduledExecutorService monitorExecutor;
    private final FaultToleranceMetrics metrics;

    private volatile boolean monitoring;
    private FailureHandler failureHandler;

    public FaultTolerance() {
        this.agentHealth = new ConcurrentHashMap<>();
        this.lastHeartbeat = new ConcurrentHashMap<>();
        this.recoveryAttempts = new ConcurrentHashMap<>();
        this.failureHistory = Collections.synchronizedList(new ArrayList<>());
        this.monitorExecutor = Executors.newScheduledThreadPool(2);
        this.metrics = new FaultToleranceMetrics();
        this.monitoring = false;
    }

    /**
     * Starts fault tolerance monitoring
     */
    public void startMonitoring() {
        if (monitoring) {
            return;
        }

        monitoring = true;
        monitorExecutor.scheduleAtFixedRate(
            this::checkHeartbeats,
            HEARTBEAT_INTERVAL_MS,
            HEARTBEAT_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );

        metrics.recordMonitoringStarted();
    }

    /**
     * Stops fault tolerance monitoring
     */
    public void stopMonitoring() {
        monitoring = false;
        monitorExecutor.shutdown();
        try {
            if (!monitorExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                monitorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        metrics.recordMonitoringStopped();
    }

    /**
     * Registers an agent for monitoring
     */
    public void registerAgent(String agentId) {
        agentHealth.put(agentId, AgentHealthStatus.ACTIVE);
        lastHeartbeat.put(agentId, System.currentTimeMillis());
        recoveryAttempts.put(agentId, 0);
        metrics.recordAgentRegistered();
    }

    /**
     * Unregisters an agent from monitoring
     */
    public void unregisterAgent(String agentId) {
        agentHealth.remove(agentId);
        lastHeartbeat.remove(agentId);
        recoveryAttempts.remove(agentId);
        metrics.recordAgentUnregistered();
    }

    /**
     * Records a heartbeat from an agent
     */
    public void recordHeartbeat(String agentId) {
        if (!agentHealth.containsKey(agentId)) {
            registerAgent(agentId);
        }

        lastHeartbeat.put(agentId, System.currentTimeMillis());

        // If agent was failed, mark as recovered
        if (agentHealth.get(agentId) == AgentHealthStatus.FAILED) {
            agentHealth.put(agentId, AgentHealthStatus.RECOVERING);
            recoveryAttempts.put(agentId, 0);
            recordFailureEvent(agentId, FailureType.RECOVERY, "Agent recovered");
            metrics.recordAgentRecovered();
        } else if (agentHealth.get(agentId) == AgentHealthStatus.RECOVERING) {
            agentHealth.put(agentId, AgentHealthStatus.ACTIVE);
        }
    }

    /**
     * Manually marks an agent as failed
     */
    public void markAgentFailed(String agentId, String reason) {
        AgentHealthStatus previousStatus = agentHealth.get(agentId);
        agentHealth.put(agentId, AgentHealthStatus.FAILED);
        recordFailureEvent(agentId, FailureType.MANUAL, reason);

        if (previousStatus != AgentHealthStatus.FAILED) {
            metrics.recordAgentFailed();
            attemptRecovery(agentId);
        }
    }

    /**
     * Gets the current health status of an agent
     */
    public AgentHealthStatus getAgentStatus(String agentId) {
        return agentHealth.getOrDefault(agentId, AgentHealthStatus.UNKNOWN);
    }

    /**
     * Gets all agents with a specific health status
     */
    public List<String> getAgentsByStatus(AgentHealthStatus status) {
        List<String> agents = new ArrayList<>();
        for (Map.Entry<String, AgentHealthStatus> entry : agentHealth.entrySet()) {
            if (entry.getValue() == status) {
                agents.add(entry.getKey());
            }
        }
        return agents;
    }

    /**
     * Gets recent failure history
     */
    public List<FailureEvent> getFailureHistory(int limit) {
        synchronized (failureHistory) {
            int start = Math.max(0, failureHistory.size() - limit);
            return new ArrayList<>(failureHistory.subList(start, failureHistory.size()));
        }
    }

    /**
     * Sets a custom failure handler
     */
    public void setFailureHandler(FailureHandler handler) {
        this.failureHandler = handler;
    }

    /**
     * Gets fault tolerance metrics
     */
    public FaultToleranceMetrics getMetrics() {
        return metrics.copy();
    }

    /**
     * Checks if the system is healthy
     */
    public boolean isSystemHealthy() {
        long failedCount = agentHealth.values().stream()
            .filter(status -> status == AgentHealthStatus.FAILED)
            .count();
        long totalCount = agentHealth.size();

        return totalCount == 0 || (double) failedCount / totalCount < 0.3; // <30% failed
    }

    private void checkHeartbeats() {
        if (!monitoring) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, Long> entry : lastHeartbeat.entrySet()) {
            String agentId = entry.getKey();
            long lastBeat = entry.getValue();

            if (currentTime - lastBeat > HEARTBEAT_TIMEOUT_MS) {
                AgentHealthStatus currentStatus = agentHealth.get(agentId);

                if (currentStatus != AgentHealthStatus.FAILED) {
                    agentHealth.put(agentId, AgentHealthStatus.FAILED);
                    recordFailureEvent(agentId, FailureType.HEARTBEAT_TIMEOUT,
                        "No heartbeat for " + (currentTime - lastBeat) + "ms");
                    metrics.recordAgentFailed();
                    attemptRecovery(agentId);
                }
            }
        }
    }

    private void attemptRecovery(String agentId) {
        int attempts = recoveryAttempts.getOrDefault(agentId, 0);

        if (attempts >= MAX_RECOVERY_ATTEMPTS) {
            recordFailureEvent(agentId, FailureType.RECOVERY_FAILED,
                "Max recovery attempts reached");
            metrics.recordRecoveryFailed();
            return;
        }

        // Schedule recovery attempt
        monitorExecutor.schedule(() -> {
            performRecovery(agentId);
        }, RECOVERY_BACKOFF_MS * (attempts + 1), TimeUnit.MILLISECONDS);

        recoveryAttempts.put(agentId, attempts + 1);
    }

    private void performRecovery(String agentId) {
        if (failureHandler != null) {
            try {
                boolean recovered = failureHandler.handleFailure(agentId);
                if (recovered) {
                    agentHealth.put(agentId, AgentHealthStatus.RECOVERING);
                    recordFailureEvent(agentId, FailureType.RECOVERY,
                        "Recovery initiated");
                    metrics.recordRecoveryAttempted();
                } else {
                    attemptRecovery(agentId); // Retry
                }
            } catch (Exception e) {
                recordFailureEvent(agentId, FailureType.RECOVERY_ERROR,
                    "Recovery error: " + e.getMessage());
                attemptRecovery(agentId); // Retry
            }
        }
    }

    private void recordFailureEvent(String agentId, FailureType type, String details) {
        FailureEvent event = new FailureEvent(
            System.currentTimeMillis(),
            agentId,
            type,
            details
        );

        synchronized (failureHistory) {
            failureHistory.add(event);
            // Keep only last 500 events
            if (failureHistory.size() > 500) {
                failureHistory.remove(0);
            }
        }
    }

    public enum AgentHealthStatus {
        ACTIVE,
        DEGRADED,
        RECOVERING,
        FAILED,
        UNKNOWN
    }

    public enum FailureType {
        HEARTBEAT_TIMEOUT,
        MANUAL,
        RECOVERY,
        RECOVERY_FAILED,
        RECOVERY_ERROR,
        SYSTEM_ERROR
    }

    public interface FailureHandler {
        boolean handleFailure(String agentId);
    }

    public static class FailureEvent {
        public final long timestamp;
        public final String agentId;
        public final FailureType type;
        public final String details;

        public FailureEvent(long timestamp, String agentId, FailureType type, String details) {
            this.timestamp = timestamp;
            this.agentId = agentId;
            this.type = type;
            this.details = details;
        }

        @Override
        public String toString() {
            return String.format("[%d] %s - %s: %s", timestamp, agentId, type, details);
        }
    }

    public static class FaultToleranceMetrics {
        private long agentsRegistered = 0;
        private long agentsUnregistered = 0;
        private long agentsFailed = 0;
        private long agentsRecovered = 0;
        private long recoveryAttempts = 0;
        private long recoveryFailures = 0;
        private long monitoringStarts = 0;
        private long monitoringStops = 0;

        void recordAgentRegistered() { agentsRegistered++; }
        void recordAgentUnregistered() { agentsUnregistered++; }
        void recordAgentFailed() { agentsFailed++; }
        void recordAgentRecovered() { agentsRecovered++; }
        void recordRecoveryAttempted() { recoveryAttempts++; }
        void recordRecoveryFailed() { recoveryFailures++; }
        void recordMonitoringStarted() { monitoringStarts++; }
        void recordMonitoringStopped() { monitoringStops++; }

        public long getAgentsRegistered() { return agentsRegistered; }
        public long getAgentsUnregistered() { return agentsUnregistered; }
        public long getAgentsFailed() { return agentsFailed; }
        public long getAgentsRecovered() { return agentsRecovered; }
        public long getRecoveryAttempts() { return recoveryAttempts; }
        public long getRecoveryFailures() { return recoveryFailures; }

        public double getRecoverySuccessRate() {
            return recoveryAttempts > 0 ?
                (double) (recoveryAttempts - recoveryFailures) / recoveryAttempts : 0.0;
        }

        public FaultToleranceMetrics copy() {
            FaultToleranceMetrics copy = new FaultToleranceMetrics();
            copy.agentsRegistered = this.agentsRegistered;
            copy.agentsUnregistered = this.agentsUnregistered;
            copy.agentsFailed = this.agentsFailed;
            copy.agentsRecovered = this.agentsRecovered;
            copy.recoveryAttempts = this.recoveryAttempts;
            copy.recoveryFailures = this.recoveryFailures;
            copy.monitoringStarts = this.monitoringStarts;
            copy.monitoringStops = this.monitoringStops;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("FaultToleranceMetrics[Registered: %d, Failed: %d, Recovered: %d, Recovery Rate: %.1f%%]",
                agentsRegistered, agentsFailed, agentsRecovered, getRecoverySuccessRate() * 100);
        }
    }
}
