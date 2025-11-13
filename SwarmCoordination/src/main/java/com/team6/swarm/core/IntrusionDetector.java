package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class IntrusionDetector {

    private static final int MONITORING_WINDOW_SIZE = 100;
    private static final double ANOMALY_THRESHOLD = 2.5; // Standard deviations
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long THREAT_COOLDOWN_MS = 60000; // 1 minute

    private final Map<String, AgentBehaviorProfile> behaviorProfiles;
    private final List<SecurityThreat> detectedThreats;
    private final Map<String, Integer> failedAttempts;
    private final Map<String, Long> lastThreatTime;
    private final DetectionMetrics metrics;
    private final ScheduledExecutorService scheduler;

    private boolean autoResponseEnabled;
    private ThreatResponseHandler responseHandler;

    /**
     * Creates a new IntrusionDetector.
     */
    public IntrusionDetector() {
        this.behaviorProfiles = new ConcurrentHashMap<>();
        this.detectedThreats = Collections.synchronizedList(new ArrayList<>());
        this.failedAttempts = new ConcurrentHashMap<>();
        this.lastThreatTime = new ConcurrentHashMap<>();
        this.metrics = new DetectionMetrics();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.autoResponseEnabled = true;
    }

    /**
     * Starts intrusion detection monitoring.
     */
    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::analyzeAllProfiles, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Stops intrusion detection monitoring.
     */
    public void stopMonitoring() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Records agent activity for behavior analysis.
     *
     * @param agentId Agent identifier
     * @param activity Activity type
     * @param metadata Additional activity metadata
     */
    public void recordActivity(String agentId, ActivityType activity, Map<String, Object> metadata) {
        AgentBehaviorProfile profile = behaviorProfiles.computeIfAbsent(
            agentId, k -> new AgentBehaviorProfile(agentId));

        profile.recordActivity(activity, metadata);
        metrics.recordActivity();

        // Check for immediate threats
        checkForImmediateThreats(agentId, activity, metadata);
    }

    /**
     * Records a failed authentication attempt.
     *
     * @param agentId Agent identifier
     */
    public void recordFailedAuthentication(String agentId) {
        int attempts = failedAttempts.merge(agentId, 1, Integer::sum);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            SecurityThreat threat = new SecurityThreat(
                ThreatType.BRUTE_FORCE_ATTACK,
                ThreatSeverity.HIGH,
                agentId,
                String.format("Multiple failed authentication attempts (%d)", attempts)
            );
            reportThreat(threat);
        }
    }

    /**
     * Clears failed authentication attempts for an agent.
     *
     * @param agentId Agent identifier
     */
    public void clearFailedAttempts(String agentId) {
        failedAttempts.remove(agentId);
    }

    /**
     * Analyzes agent behavior for anomalies.
     *
     * @param agentId Agent identifier
     * @return List of detected anomalies
     */
    public List<BehaviorAnomaly> analyzeBehavior(String agentId) {
        AgentBehaviorProfile profile = behaviorProfiles.get(agentId);
        if (profile == null) {
            return Collections.emptyList();
        }

        return profile.detectAnomalies();
    }

    /**
     * Gets all detected threats.
     *
     * @return List of security threats
     */
    public List<SecurityThreat> getDetectedThreats() {
        synchronized (detectedThreats) {
            return new ArrayList<>(detectedThreats);
        }
    }

    /**
     * Gets recent threats for a specific agent.
     *
     * @param agentId Agent identifier
     * @param limit Maximum number of threats
     * @return List of security threats
     */
    public List<SecurityThreat> getAgentThreats(String agentId, int limit) {
        synchronized (detectedThreats) {
            return detectedThreats.stream()
                .filter(threat -> threat.agentId.equals(agentId))
                .limit(limit)
                .collect(Collectors.toList());
        }
    }

    /**
     * Gets detection metrics.
     *
     * @return Detection metrics
     */
    public DetectionMetrics getMetrics() {
        return metrics.copy();
    }

    /**
     * Sets the threat response handler.
     *
     * @param handler Response handler
     */
    public void setResponseHandler(ThreatResponseHandler handler) {
        this.responseHandler = handler;
    }

    /**
     * Enables or disables automatic threat response.
     *
     * @param enabled true to enable auto-response
     */
    public void setAutoResponseEnabled(boolean enabled) {
        this.autoResponseEnabled = enabled;
    }

    /**
     * Checks for immediate security threats.
     */
    private void checkForImmediateThreats(String agentId, ActivityType activity, Map<String, Object> metadata) {
        // Check for rapid message sending (potential spam/DDoS)
        if (activity == ActivityType.MESSAGE_SENT) {
            AgentBehaviorProfile profile = behaviorProfiles.get(agentId);
            if (profile != null && profile.getRecentActivityCount(ActivityType.MESSAGE_SENT, 1000) > 50) {
                SecurityThreat threat = new SecurityThreat(
                    ThreatType.DDOS_ATTEMPT,
                    ThreatSeverity.CRITICAL,
                    agentId,
                    "Excessive message sending detected (potential DDoS)"
                );
                reportThreat(threat);
            }
        }

        // Check for unauthorized access attempts
        if (activity == ActivityType.ACCESS_DENIED) {
            AgentBehaviorProfile profile = behaviorProfiles.get(agentId);
            if (profile != null && profile.getRecentActivityCount(ActivityType.ACCESS_DENIED, 5000) > 10) {
                SecurityThreat threat = new SecurityThreat(
                    ThreatType.UNAUTHORIZED_ACCESS,
                    ThreatSeverity.HIGH,
                    agentId,
                    "Multiple unauthorized access attempts"
                );
                reportThreat(threat);
            }
        }
    }

    /**
     * Analyzes all agent behavior profiles.
     */
    private void analyzeAllProfiles() {
        for (AgentBehaviorProfile profile : behaviorProfiles.values()) {
            List<BehaviorAnomaly> anomalies = profile.detectAnomalies();
            for (BehaviorAnomaly anomaly : anomalies) {
                if (anomaly.severity == AnomalySeverity.HIGH || anomaly.severity == AnomalySeverity.CRITICAL) {
                    SecurityThreat threat = new SecurityThreat(
                        ThreatType.ANOMALOUS_BEHAVIOR,
                        mapSeverity(anomaly.severity),
                        profile.agentId,
                        anomaly.description
                    );
                    reportThreat(threat);
                }
            }
        }
    }

    /**
     * Reports a security threat.
     */
    private void reportThreat(SecurityThreat threat) {
        // Check if we're in cooldown for this agent
        Long lastTime = lastThreatTime.get(threat.agentId);
        if (lastTime != null && System.currentTimeMillis() - lastTime < THREAT_COOLDOWN_MS) {
            return; // Skip to avoid duplicate alerts
        }

        synchronized (detectedThreats) {
            detectedThreats.add(threat);
            while (detectedThreats.size() > 1000) {
                detectedThreats.remove(0);
            }
        }

        lastThreatTime.put(threat.agentId, System.currentTimeMillis());
        metrics.recordThreatDetected();

        // Trigger automatic response if enabled
        if (autoResponseEnabled && responseHandler != null) {
            responseHandler.handleThreat(threat);
        }
    }

    /**
     * Maps anomaly severity to threat severity.
     */
    private ThreatSeverity mapSeverity(AnomalySeverity anomalySeverity) {
        switch (anomalySeverity) {
            case CRITICAL: return ThreatSeverity.CRITICAL;
            case HIGH: return ThreatSeverity.HIGH;
            case MEDIUM: return ThreatSeverity.MEDIUM;
            default: return ThreatSeverity.LOW;
        }
    }

    // Enums
    public enum ActivityType {
        MESSAGE_SENT, MESSAGE_RECEIVED, STATE_UPDATE, COMMAND_EXECUTED,
        ACCESS_GRANTED, ACCESS_DENIED, DATA_READ, DATA_WRITE
    }

    public enum ThreatType {
        BRUTE_FORCE_ATTACK, DDOS_ATTEMPT, UNAUTHORIZED_ACCESS,
        ANOMALOUS_BEHAVIOR, DATA_EXFILTRATION, PRIVILEGE_ESCALATION
    }

    public enum ThreatSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum AnomalySeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Inner classes
    private static class AgentBehaviorProfile {
        final String agentId;
        final Map<ActivityType, LinkedList<Long>> activityTimestamps;
        final Map<ActivityType, ActivityStatistics> activityStats;
        final long creationTime;

        AgentBehaviorProfile(String agentId) {
            this.agentId = agentId;
            this.activityTimestamps = new ConcurrentHashMap<>();
            this.activityStats = new ConcurrentHashMap<>();
            this.creationTime = System.currentTimeMillis();
        }

        void recordActivity(ActivityType activity, Map<String, Object> metadata) {
            long timestamp = System.currentTimeMillis();

            // Record timestamp
            LinkedList<Long> timestamps = activityTimestamps.computeIfAbsent(
                activity, k -> new LinkedList<>());
            timestamps.add(timestamp);

            // Keep only recent timestamps (last 5 minutes)
            while (!timestamps.isEmpty() && timestamp - timestamps.getFirst() > 300000) {
                timestamps.removeFirst();
            }

            // Update statistics
            ActivityStatistics stats = activityStats.computeIfAbsent(
                activity, k -> new ActivityStatistics());
            stats.recordActivity(timestamp);
        }

        int getRecentActivityCount(ActivityType activity, long timeWindowMs) {
            LinkedList<Long> timestamps = activityTimestamps.get(activity);
            if (timestamps == null || timestamps.isEmpty()) {
                return 0;
            }

            long threshold = System.currentTimeMillis() - timeWindowMs;
            return (int) timestamps.stream().filter(t -> t > threshold).count();
        }

        List<BehaviorAnomaly> detectAnomalies() {
            List<BehaviorAnomaly> anomalies = new ArrayList<>();

            for (Map.Entry<ActivityType, ActivityStatistics> entry : activityStats.entrySet()) {
                ActivityType activity = entry.getKey();
                ActivityStatistics stats = entry.getValue();

                if (stats.count < 10) continue; // Need enough data

                double mean = stats.getMean();
                double stdDev = stats.getStdDev();
                double currentRate = getRecentActivityCount(activity, 60000); // Last minute

                // Check if current rate is anomalous
                if (stdDev > 0 && Math.abs(currentRate - mean) > ANOMALY_THRESHOLD * stdDev) {
                    AnomalySeverity severity = currentRate > mean + 3 * stdDev ?
                        AnomalySeverity.CRITICAL : AnomalySeverity.HIGH;

                    anomalies.add(new BehaviorAnomaly(
                        activity,
                        severity,
                        String.format("Unusual %s rate: %.1f (expected: %.1f ± %.1f)",
                            activity, currentRate, mean, stdDev)
                    ));
                }
            }

            return anomalies;
        }
    }

    private static class ActivityStatistics {
        long count = 0;
        double sum = 0;
        double sumSquares = 0;

        void recordActivity(long timestamp) {
            count++;
            sum += 1.0;
            sumSquares += 1.0;
        }

        double getMean() {
            return count > 0 ? sum / count : 0.0;
        }

        double getStdDev() {
            if (count < 2) return 0.0;
            double mean = getMean();
            double variance = (sumSquares / count) - (mean * mean);
            return Math.sqrt(Math.max(0, variance));
        }
    }

    public static class BehaviorAnomaly {
        public final ActivityType activity;
        public final AnomalySeverity severity;
        public final String description;

        BehaviorAnomaly(ActivityType activity, AnomalySeverity severity, String description) {
            this.activity = activity;
            this.severity = severity;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", severity, activity, description);
        }
    }

    public static class SecurityThreat {
        public final ThreatType type;
        public final ThreatSeverity severity;
        public final String agentId;
        public final String description;
        public final long timestamp;

        public SecurityThreat(ThreatType type, ThreatSeverity severity, String agentId, String description) {
            this.type = type;
            this.severity = severity;
            this.agentId = agentId;
            this.description = description;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("[%s - %s] Agent %s: %s",
                new Date(timestamp), severity, agentId, description);
        }
    }

    public static class DetectionMetrics {
        private final AtomicLong activitiesRecorded = new AtomicLong(0);
        private final AtomicLong threatsDetected = new AtomicLong(0);

        void recordActivity() { activitiesRecorded.incrementAndGet(); }
        void recordThreatDetected() { threatsDetected.incrementAndGet(); }

        public long getActivitiesRecorded() { return activitiesRecorded.get(); }
        public long getThreatsDetected() { return threatsDetected.get(); }

        public DetectionMetrics copy() {
            DetectionMetrics copy = new DetectionMetrics();
            copy.activitiesRecorded.set(this.activitiesRecorded.get());
            copy.threatsDetected.set(this.threatsDetected.get());
            return copy;
        }

        @Override
        public String toString() {
            return String.format("DetectionMetrics[Activities: %d, Threats: %d]",
                activitiesRecorded.get(), threatsDetected.get());
        }
    }

    /**
     * Interface for handling detected threats.
     */
    public interface ThreatResponseHandler {
        void handleThreat(SecurityThreat threat);
    }
}