package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Week 8 Implementation: SwarmAnalytics
 *
 * Provides advanced analytics and behavioral analysis for the swarm system,
 * enabling pattern recognition, anomaly detection, and performance insights.
 *
 * Key Features:
 * - Swarm behavioral pattern analysis
 * - Agent efficiency metrics
 * - Formation quality assessment
 * - Communication pattern analysis
 * - Predictive analytics
 * - Anomaly detection in swarm behavior
 *
 * @author Team 6
 * @version Week 8
 */
public class SwarmAnalytics {

    private static final int PATTERN_HISTORY_SIZE = 100;
    private static final double ANOMALY_THRESHOLD = 2.5; // Standard deviations

    private final Map<String, AgentAnalytics> agentAnalytics;
    private final List<SwarmBehaviorSnapshot> behaviorHistory;
    private final AnalyticsMetrics metrics;

    public SwarmAnalytics() {
        this.agentAnalytics = new ConcurrentHashMap<>();
        this.behaviorHistory = Collections.synchronizedList(new ArrayList<>());
        this.metrics = new AnalyticsMetrics();
    }

    /**
     * Records agent activity for analytics
     */
    public void recordAgentActivity(String agentId, AgentState state, String activityType) {
        AgentAnalytics analytics = agentAnalytics.computeIfAbsent(
            agentId, k -> new AgentAnalytics(agentId));

        analytics.recordActivity(activityType, state);
        metrics.recordActivity();
    }

    /**
     * Analyzes current swarm behavior
     */
    public SwarmBehaviorSnapshot analyzeSwarmBehavior(List<AgentState> agentStates) {
        if (agentStates == null || agentStates.isEmpty()) {
            return null;
        }

        long timestamp = System.currentTimeMillis();
        int agentCount = agentStates.size();

        // Calculate swarm center
        Point2D swarmCenter = calculateSwarmCenter(agentStates);

        // Calculate swarm spread
        double swarmSpread = calculateSwarmSpread(agentStates, swarmCenter);

        // Calculate average velocity
        double avgVelocity = calculateAverageVelocity(agentStates);

        // Calculate cohesion score
        double cohesion = calculateCohesionScore(agentStates, swarmCenter);

        // Calculate alignment score
        double alignment = calculateAlignmentScore(agentStates);

        // Calculate separation quality
        double separation = calculateSeparationScore(agentStates);

        SwarmBehaviorSnapshot snapshot = new SwarmBehaviorSnapshot(
            timestamp, agentCount, swarmCenter, swarmSpread,
            avgVelocity, cohesion, alignment, separation
        );

        synchronized (behaviorHistory) {
            behaviorHistory.add(snapshot);
            if (behaviorHistory.size() > PATTERN_HISTORY_SIZE) {
                behaviorHistory.remove(0);
            }
        }

        metrics.recordAnalysis();
        return snapshot;
    }

    /**
     * Gets efficiency metrics for a specific agent
     */
    public AgentEfficiencyMetrics getAgentEfficiency(String agentId) {
        AgentAnalytics analytics = agentAnalytics.get(agentId);
        if (analytics == null) {
            return null;
        }

        return analytics.calculateEfficiency();
    }

    /**
     * Gets the most efficient agents
     */
    public List<String> getTopPerformingAgents(int limit) {
        return agentAnalytics.values().stream()
            .map(a -> new AbstractMap.SimpleEntry<>(a.agentId, a.calculateEfficiency().overallScore))
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Detects behavioral anomalies in the swarm
     */
    public List<BehaviorAnomaly> detectAnomalies() {
        List<BehaviorAnomaly> anomalies = new ArrayList<>();

        synchronized (behaviorHistory) {
            if (behaviorHistory.size() < 10) {
                return anomalies; // Not enough data
            }

            // Calculate statistics for recent behavior
            List<Double> cohesionValues = new ArrayList<>();
            List<Double> alignmentValues = new ArrayList<>();
main

            for (SwarmBehaviorSnapshot snapshot : behaviorHistory) {
                cohesionValues.add(snapshot.cohesionScore);
                alignmentValues.add(snapshot.alignmentScore);
 main
            }

            // Check for anomalies
            double cohesionMean = calculateMean(cohesionValues);
            double cohesionStdDev = calculateStdDev(cohesionValues, cohesionMean);
            SwarmBehaviorSnapshot latest = behaviorHistory.get(behaviorHistory.size() - 1);

            if (Math.abs(latest.cohesionScore - cohesionMean) > ANOMALY_THRESHOLD * cohesionStdDev) {
                anomalies.add(new BehaviorAnomaly(
                    "COHESION_ANOMALY",
                    "Unusual cohesion score: " + latest.cohesionScore,
                    latest.timestamp
                ));
            }

            double alignmentMean = calculateMean(alignmentValues);
            double alignmentStdDev = calculateStdDev(alignmentValues, alignmentMean);

            if (Math.abs(latest.alignmentScore - alignmentMean) > ANOMALY_THRESHOLD * alignmentStdDev) {
                anomalies.add(new BehaviorAnomaly(
                    "ALIGNMENT_ANOMALY",
                    "Unusual alignment score: " + latest.alignmentScore,
                    latest.timestamp
                ));
            }
        }

        if (!anomalies.isEmpty()) {
            metrics.recordAnomaly();
        }

        return anomalies;
    }

    /**
     * Gets behavioral trends over time
     */
    public BehaviorTrends analyzeTrends() {
        synchronized (behaviorHistory) {
            if (behaviorHistory.size() < 5) {
                return null;
            }

            int size = behaviorHistory.size();
            SwarmBehaviorSnapshot oldest = behaviorHistory.get(0);
            SwarmBehaviorSnapshot newest = behaviorHistory.get(size - 1);

            double cohesionTrend = newest.cohesionScore - oldest.cohesionScore;
            double alignmentTrend = newest.alignmentScore - oldest.alignmentScore;
            double spreadTrend = newest.swarmSpread - oldest.swarmSpread;
            double velocityTrend = newest.averageVelocity - oldest.averageVelocity;

            return new BehaviorTrends(
                cohesionTrend > 0 ? "IMPROVING" : "DECLINING",
                alignmentTrend > 0 ? "IMPROVING" : "DECLINING",
                spreadTrend > 0 ? "EXPANDING" : "CONTRACTING",
                velocityTrend > 0 ? "ACCELERATING" : "DECELERATING"
            );
        }
    }

    /**
     * Gets recent behavior snapshots
     */
    public List<SwarmBehaviorSnapshot> getRecentBehavior(int count) {
        synchronized (behaviorHistory) {
            int size = behaviorHistory.size();
            int start = Math.max(0, size - count);
            return new ArrayList<>(behaviorHistory.subList(start, size));
        }
    }

    /**
     * Gets analytics metrics
     */
    public AnalyticsMetrics getMetrics() {
        metrics.trackedAgents = agentAnalytics.size();
        metrics.behaviorSnapshots = behaviorHistory.size();
        return metrics.copy();
    }

    /**
     * Clears analytics data for an agent
     */
    public void clearAgentAnalytics(String agentId) {
        agentAnalytics.remove(agentId);
    }

    /**
     * Clears all analytics data
     */
    public void clearAll() {
        agentAnalytics.clear();
        behaviorHistory.clear();
    }

    private Point2D calculateSwarmCenter(List<AgentState> agents) {
        double sumX = 0, sumY = 0;
        for (AgentState agent : agents) {
            sumX += agent.position.x;
            sumY += agent.position.y;
        }
        return new Point2D(sumX / agents.size(), sumY / agents.size());
    }

    private double calculateSwarmSpread(List<AgentState> agents, Point2D center) {
        double sumDistances = 0;
        for (AgentState agent : agents) {
            sumDistances += agent.position.distanceTo(center);
        }
        return sumDistances / agents.size();
    }

    private double calculateAverageVelocity(List<AgentState> agents) {
        double sumSpeed = 0;
        for (AgentState agent : agents) {
            sumSpeed += agent.velocity.magnitude();
        }
        return sumSpeed / agents.size();
    }

    private double calculateCohesionScore(List<AgentState> agents, Point2D center) {
        double maxDist = 1000.0; // Maximum expected distance
        double avgDist = calculateSwarmSpread(agents, center);
        return Math.max(0, 1.0 - (avgDist / maxDist));
    }

    private double calculateAlignmentScore(List<AgentState> agents) {
        if (agents.size() < 2) return 1.0;

        Vector2D avgDirection = new Vector2D(0, 0);
        for (AgentState agent : agents) {
            if (agent.velocity.magnitude() > 0.1) {
                Vector2D normalized = agent.velocity.normalize();
                avgDirection = avgDirection.add(normalized);
            }
        }

        double alignment = avgDirection.magnitude() / agents.size();
        return Math.min(1.0, alignment);
    }

    private double calculateSeparationScore(List<AgentState> agents) {
        if (agents.size() < 2) return 1.0;

        int tooCloseCount = 0;
        double minSeparation = 20.0;

        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                double dist = agents.get(i).position.distanceTo(agents.get(j).position);
                if (dist < minSeparation) {
                    tooCloseCount++;
                }
            }
        }

        int totalPairs = agents.size() * (agents.size() - 1) / 2;
        return 1.0 - ((double) tooCloseCount / totalPairs);
    }

    private double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double calculateStdDev(List<Double> values, double mean) {
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }

    private static class AgentAnalytics {
        final String agentId;
        final Map<String, Integer> activityCounts;
        final List<Double> velocityHistory;
        final long createdTime;

        AgentAnalytics(String agentId) {
            this.agentId = agentId;
            this.activityCounts = new ConcurrentHashMap<>();
            this.velocityHistory = Collections.synchronizedList(new ArrayList<>());
            this.createdTime = System.currentTimeMillis();
        }

        void recordActivity(String type, AgentState state) {
            activityCounts.merge(type, 1, Integer::sum);

            if (state != null && state.velocity != null) {
                double speed = state.velocity.magnitude();
                synchronized (velocityHistory) {
                    velocityHistory.add(speed);
                    if (velocityHistory.size() > 100) {
                        velocityHistory.remove(0);
                    }
                }
            }
        }

        AgentEfficiencyMetrics calculateEfficiency() {
            int totalActivities = activityCounts.values().stream()
                .mapToInt(Integer::intValue).sum();

            double avgVelocity = 0;
            synchronized (velocityHistory) {
                if (!velocityHistory.isEmpty()) {
                    avgVelocity = velocityHistory.stream()
                        .mapToDouble(Double::doubleValue).average().orElse(0.0);
                }
            }

            double activityScore = Math.min(1.0, totalActivities / 100.0);
            double velocityScore = Math.min(1.0, avgVelocity / 50.0);
            double overallScore = (activityScore + velocityScore) / 2.0;

            return new AgentEfficiencyMetrics(
                agentId, totalActivities, avgVelocity, activityScore, velocityScore, overallScore
            );
        }
    }

    public static class SwarmBehaviorSnapshot {
        public final long timestamp;
        public final int agentCount;
        public final Point2D swarmCenter;
        public final double swarmSpread;
        public final double averageVelocity;
        public final double cohesionScore;
        public final double alignmentScore;
        public final double separationScore;

        public SwarmBehaviorSnapshot(long timestamp, int agentCount, Point2D swarmCenter,
                                    double swarmSpread, double averageVelocity,
                                    double cohesionScore, double alignmentScore, double separationScore) {
            this.timestamp = timestamp;
            this.agentCount = agentCount;
            this.swarmCenter = swarmCenter;
            this.swarmSpread = swarmSpread;
            this.averageVelocity = averageVelocity;
            this.cohesionScore = cohesionScore;
            this.alignmentScore = alignmentScore;
            this.separationScore = separationScore;
        }

        @Override
        public String toString() {
            return String.format("SwarmBehavior[agents=%d, cohesion=%.2f, alignment=%.2f, separation=%.2f]",
                agentCount, cohesionScore, alignmentScore, separationScore);
        }
    }

    public static class AgentEfficiencyMetrics {
        public final String agentId;
        public final int totalActivities;
        public final double averageVelocity;
        public final double activityScore;
        public final double velocityScore;
        public final double overallScore;

        public AgentEfficiencyMetrics(String agentId, int totalActivities, double averageVelocity,
                                     double activityScore, double velocityScore, double overallScore) {
            this.agentId = agentId;
            this.totalActivities = totalActivities;
            this.averageVelocity = averageVelocity;
            this.activityScore = activityScore;
            this.velocityScore = velocityScore;
            this.overallScore = overallScore;
        }

        @Override
        public String toString() {
            return String.format("Efficiency[%s: overall=%.2f, activity=%.2f, velocity=%.2f]",
                agentId, overallScore, activityScore, velocityScore);
        }
    }

    public static class BehaviorAnomaly {
        public final String type;
        public final String description;
        public final long timestamp;

        public BehaviorAnomaly(String type, String description, long timestamp) {
            this.type = type;
            this.description = description;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("Anomaly[%s: %s at %d]", type, description, timestamp);
        }
    }

    public static class BehaviorTrends {
        public final String cohesionTrend;
        public final String alignmentTrend;
        public final String spreadTrend;
        public final String velocityTrend;

        public BehaviorTrends(String cohesionTrend, String alignmentTrend,
                            String spreadTrend, String velocityTrend) {
            this.cohesionTrend = cohesionTrend;
            this.alignmentTrend = alignmentTrend;
            this.spreadTrend = spreadTrend;
            this.velocityTrend = velocityTrend;
        }

        @Override
        public String toString() {
            return String.format("Trends[cohesion=%s, alignment=%s, spread=%s, velocity=%s]",
                cohesionTrend, alignmentTrend, spreadTrend, velocityTrend);
        }
    }

    public static class AnalyticsMetrics {
        private long activitiesRecorded = 0;
        private long analysesPerformed = 0;
        private long anomaliesDetected = 0;
        private int trackedAgents = 0;
        private int behaviorSnapshots = 0;

        void recordActivity() { activitiesRecorded++; }
        void recordAnalysis() { analysesPerformed++; }
        void recordAnomaly() { anomaliesDetected++; }

        public long getActivitiesRecorded() { return activitiesRecorded; }
        public long getAnalysesPerformed() { return analysesPerformed; }
        public long getAnomaliesDetected() { return anomaliesDetected; }
        public int getTrackedAgents() { return trackedAgents; }
        public int getBehaviorSnapshots() { return behaviorSnapshots; }

        public AnalyticsMetrics copy() {
            AnalyticsMetrics copy = new AnalyticsMetrics();
            copy.activitiesRecorded = this.activitiesRecorded;
            copy.analysesPerformed = this.analysesPerformed;
            copy.anomaliesDetected = this.anomaliesDetected;
            copy.trackedAgents = this.trackedAgents;
            copy.behaviorSnapshots = this.behaviorSnapshots;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("AnalyticsMetrics[Agents: %d, Activities: %d, Analyses: %d, Anomalies: %d]",
                trackedAgents, activitiesRecorded, analysesPerformed, anomaliesDetected);
        }
    }
}
