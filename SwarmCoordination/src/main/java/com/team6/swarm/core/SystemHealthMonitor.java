package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.management.*;

/**
 * Week 7 Implementation: SystemHealthMonitor
 *
 * Real-time system health monitoring with comprehensive metrics tracking,
 * alert system, and resource usage monitoring.
 *
 * @author Team 6
 * @version Week 7
 */
public class SystemHealthMonitor {

    private static final int HEALTH_CHECK_INTERVAL_MS = 1000;
    private static final int HISTORY_SIZE = 100;
    private static final double CPU_WARNING_THRESHOLD = 0.70;
    private static final double CPU_CRITICAL_THRESHOLD = 0.85;
    private static final double MEMORY_WARNING_THRESHOLD = 0.70;
    private static final double MEMORY_CRITICAL_THRESHOLD = 0.85;

    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;
    private final RuntimeMXBean runtimeBean;
    private final OperatingSystemMXBean osBean;

    private final Map<String, AgentHealthStatus> agentHealthMap;
    private final List<HealthAlert> activeAlerts;
    private final LinkedList<HealthSnapshot> healthHistory;
    private final List<HealthListener> listeners;
    private final ScheduledExecutorService scheduler;
    private final HealthMetrics metrics;

    private volatile boolean monitoring;
    private volatile SystemHealthStatus currentStatus;

    public SystemHealthMonitor() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.runtimeBean = ManagementFactory.getRuntimeMXBean();
        this.osBean = ManagementFactory.getOperatingSystemMXBean();

        this.agentHealthMap = new ConcurrentHashMap<>();
        this.activeAlerts = Collections.synchronizedList(new ArrayList<>());
        this.healthHistory = new LinkedList<>();
        this.listeners = Collections.synchronizedList(new ArrayList<>());
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.metrics = new HealthMetrics();

        this.monitoring = false;
        this.currentStatus = SystemHealthStatus.HEALTHY;
    }

    public void startMonitoring() {
        if (monitoring) return;
        monitoring = true;
        scheduler.scheduleAtFixedRate(this::performHealthCheck, 0, HEALTH_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public void stopMonitoring() {
        monitoring = false;
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

    public HealthReport checkSystemHealth() {
        HealthSnapshot snapshot = captureHealthSnapshot();
        synchronized (healthHistory) {
            healthHistory.add(snapshot);
            while (healthHistory.size() > HISTORY_SIZE) {
                healthHistory.removeFirst();
            }
        }
        List<HealthIssue> issues = analyzeHealth(snapshot);
        double healthScore = calculateHealthScore(snapshot, issues);
        SystemHealthStatus status = determineStatus(healthScore);
        if (status != currentStatus) {
            notifyStatusChange(currentStatus, status);
            currentStatus = status;
        }
        processHealthIssues(issues);
        return new HealthReport(snapshot, issues, activeAlerts, healthScore, status);
    }

    public HealthReport getHealthReport() {
        return checkSystemHealth();
    }

    public void registerHealthListener(HealthListener listener) {
        listeners.add(listener);
    }

    public void unregisterHealthListener(HealthListener listener) {
        listeners.remove(listener);
    }

    public void updateAgentHealth(String agentId, AgentHealthStatus status) {
        agentHealthMap.put(agentId, status);
        metrics.recordAgentHealthUpdate();
    }

    public AgentHealthStatus getAgentHealth(String agentId) {
        return agentHealthMap.get(agentId);
    }

    public ResourceUsage checkResourceUsage() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        double cpuLoad = osBean.getSystemLoadAverage();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        double cpuUsageEstimate = cpuLoad > 0 ? Math.min(1.0, cpuLoad / availableProcessors) : 0.0;
        return new ResourceUsage(cpuUsageEstimate, (double) heapUsage.getUsed() / heapUsage.getMax(),
            heapUsage.getUsed(), heapUsage.getMax(), nonHeapUsage.getUsed(),
            threadBean.getThreadCount(), threadBean.getPeakThreadCount(), runtimeBean.getUptime());
    }

    public HealthMetrics getHealthMetrics() {
        return metrics.copy();
    }

    public void clearAlerts() {
        synchronized (activeAlerts) {
            activeAlerts.clear();
        }
    }

    private void performHealthCheck() {
        try {
            checkSystemHealth();
            metrics.recordHealthCheck();
        } catch (Exception e) {
            System.err.println("Error during health check: " + e.getMessage());
        }
    }

    private HealthSnapshot captureHealthSnapshot() {
        ResourceUsage resources = checkResourceUsage();
        int healthyAgents = 0, warningAgents = 0, criticalAgents = 0;
        for (AgentHealthStatus status : agentHealthMap.values()) {
            switch (status) {
                case HEALTHY: healthyAgents++; break;
                case WARNING: warningAgents++; break;
                case CRITICAL: criticalAgents++; break;
            }
        }
        return new HealthSnapshot(System.currentTimeMillis(), resources, agentHealthMap.size(),
            healthyAgents, warningAgents, criticalAgents);
    }

    private List<HealthIssue> analyzeHealth(HealthSnapshot snapshot) {
        List<HealthIssue> issues = new ArrayList<>();
        if (snapshot.resources.cpuUsage > CPU_CRITICAL_THRESHOLD) {
            issues.add(new HealthIssue(IssueType.CPU_OVERLOAD, IssueSeverity.CRITICAL,
                String.format("CPU usage is critical: %.1f%%", snapshot.resources.cpuUsage * 100),
                "Reduce agent count or optimize computations"));
        } else if (snapshot.resources.cpuUsage > CPU_WARNING_THRESHOLD) {
            issues.add(new HealthIssue(IssueType.CPU_OVERLOAD, IssueSeverity.WARNING,
                String.format("CPU usage is high: %.1f%%", snapshot.resources.cpuUsage * 100),
                "Monitor CPU usage and consider optimizations"));
        }
        if (snapshot.resources.memoryUsage > MEMORY_CRITICAL_THRESHOLD) {
            issues.add(new HealthIssue(IssueType.MEMORY_PRESSURE, IssueSeverity.CRITICAL,
                String.format("Memory usage is critical: %.1f%%", snapshot.resources.memoryUsage * 100),
                "Increase heap size or reduce memory consumption"));
        } else if (snapshot.resources.memoryUsage > MEMORY_WARNING_THRESHOLD) {
            issues.add(new HealthIssue(IssueType.MEMORY_PRESSURE, IssueSeverity.WARNING,
                String.format("Memory usage is high: %.1f%%", snapshot.resources.memoryUsage * 100),
                "Monitor memory usage and consider garbage collection"));
        }
        int threadThreshold = Runtime.getRuntime().availableProcessors() * 3;
        if (snapshot.resources.threadCount > threadThreshold) {
            issues.add(new HealthIssue(IssueType.THREAD_EXHAUSTION, IssueSeverity.WARNING,
                String.format("Thread count is high: %d (threshold: %d)", snapshot.resources.threadCount, threadThreshold),
                "Reduce thread pool size or optimize task scheduling"));
        }
        if (snapshot.criticalAgents > 0) {
            issues.add(new HealthIssue(IssueType.AGENT_FAILURE, IssueSeverity.CRITICAL,
                String.format("%d agents in critical state", snapshot.criticalAgents),
                "Investigate and restart failing agents"));
        } else if (snapshot.totalAgents > 0 && snapshot.warningAgents > snapshot.totalAgents * 0.2) {
            issues.add(new HealthIssue(IssueType.AGENT_DEGRADATION, IssueSeverity.WARNING,
                String.format("%d agents in warning state (%.1f%%)", snapshot.warningAgents,
                    (double) snapshot.warningAgents / snapshot.totalAgents * 100),
                "Monitor agent performance and investigate issues"));
        }
        return issues;
    }

    private double calculateHealthScore(HealthSnapshot snapshot, List<HealthIssue> issues) {
        double score = 100.0;
        score -= snapshot.resources.cpuUsage * 20;
        score -= snapshot.resources.memoryUsage * 20;
        for (HealthIssue issue : issues) {
            switch (issue.severity) {
                case CRITICAL: score -= 20; break;
                case WARNING: score -= 10; break;
                case INFO: score -= 5; break;
            }
        }
        if (snapshot.totalAgents > 0) {
            double unhealthyRatio = (double) (snapshot.warningAgents + snapshot.criticalAgents) / snapshot.totalAgents;
            score -= unhealthyRatio * 30;
        }
        return Math.max(0, Math.min(100, score));
    }

    private SystemHealthStatus determineStatus(double healthScore) {
        if (healthScore >= 80) return SystemHealthStatus.HEALTHY;
        if (healthScore >= 60) return SystemHealthStatus.DEGRADED;
        if (healthScore >= 40) return SystemHealthStatus.WARNING;
        return SystemHealthStatus.CRITICAL;
    }

    private void processHealthIssues(List<HealthIssue> issues) {
        synchronized (activeAlerts) {
            activeAlerts.removeIf(alert -> issues.stream().noneMatch(issue -> issue.type == alert.issueType));
            for (HealthIssue issue : issues) {
                if (issue.severity == IssueSeverity.CRITICAL || issue.severity == IssueSeverity.WARNING) {
                    boolean alertExists = activeAlerts.stream().anyMatch(alert -> alert.issueType == issue.type);
                    if (!alertExists) {
                        HealthAlert alert = new HealthAlert(issue.type, issue.severity, issue.description);
                        activeAlerts.add(alert);
                        notifyAlert(alert);
                        metrics.recordAlert();
                    }
                }
            }
        }
    }

    private void notifyStatusChange(SystemHealthStatus oldStatus, SystemHealthStatus newStatus) {
        for (HealthListener listener : listeners) {
            try {
                listener.onStatusChanged(oldStatus, newStatus);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyAlert(HealthAlert alert) {
        for (HealthListener listener : listeners) {
            try {
                listener.onAlert(alert);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    public enum SystemHealthStatus { HEALTHY, DEGRADED, WARNING, CRITICAL }
    public enum AgentHealthStatus { HEALTHY, WARNING, CRITICAL, UNKNOWN }
    public enum IssueType { CPU_OVERLOAD, MEMORY_PRESSURE, THREAD_EXHAUSTION, AGENT_FAILURE, AGENT_DEGRADATION, NETWORK_ISSUES, DISK_ISSUES }
    public enum IssueSeverity { INFO, WARNING, CRITICAL }

    public static class HealthSnapshot {
        public final long timestamp;
        public final ResourceUsage resources;
        public final int totalAgents, healthyAgents, warningAgents, criticalAgents;

        public HealthSnapshot(long timestamp, ResourceUsage resources, int totalAgents,
                            int healthyAgents, int warningAgents, int criticalAgents) {
            this.timestamp = timestamp;
            this.resources = resources;
            this.totalAgents = totalAgents;
            this.healthyAgents = healthyAgents;
            this.warningAgents = warningAgents;
            this.criticalAgents = criticalAgents;
        }

        @Override
        public String toString() {
            return String.format("HealthSnapshot[Agents: %d/%d/%d (H/W/C), CPU: %.1f%%, Memory: %.1f%%]",
                healthyAgents, warningAgents, criticalAgents, resources.cpuUsage * 100, resources.memoryUsage * 100);
        }
    }

    public static class ResourceUsage {
        public final double cpuUsage, memoryUsage;
        public final long memoryUsed, memoryMax, nonHeapMemory;
        public final int threadCount, peakThreadCount;
        public final long uptime;

        public ResourceUsage(double cpuUsage, double memoryUsage, long memoryUsed, long memoryMax,
                           long nonHeapMemory, int threadCount, int peakThreadCount, long uptime) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.memoryUsed = memoryUsed;
            this.memoryMax = memoryMax;
            this.nonHeapMemory = nonHeapMemory;
            this.threadCount = threadCount;
            this.peakThreadCount = peakThreadCount;
            this.uptime = uptime;
        }

        @Override
        public String toString() {
            return String.format("ResourceUsage[CPU: %.1f%%, Memory: %.1f%%, Threads: %d, Uptime: %dms]",
                cpuUsage * 100, memoryUsage * 100, threadCount, uptime);
        }
    }

    public static class HealthIssue {
        public final IssueType type;
        public final IssueSeverity severity;
        public final String description, recommendation;

        public HealthIssue(IssueType type, IssueSeverity severity, String description, String recommendation) {
            this.type = type;
            this.severity = severity;
            this.description = description;
            this.recommendation = recommendation;
        }

        @Override
        public String toString() {
            return String.format("[%s - %s] %s", severity, type, description);
        }
    }

    public static class HealthAlert {
        public final IssueType issueType;
        public final IssueSeverity severity;
        public final String message;
        public final long timestamp;

        public HealthAlert(IssueType issueType, IssueSeverity severity, String message) {
            this.issueType = issueType;
            this.severity = severity;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("HealthAlert[%s - %s] %s", severity, issueType, message);
        }
    }

    public static class HealthReport {
        public final HealthSnapshot snapshot;
        public final List<HealthIssue> issues;
        public final List<HealthAlert> activeAlerts;
        public final double healthScore;
        public final SystemHealthStatus status;

        public HealthReport(HealthSnapshot snapshot, List<HealthIssue> issues, List<HealthAlert> activeAlerts,
                          double healthScore, SystemHealthStatus status) {
            this.snapshot = snapshot;
            this.issues = new ArrayList<>(issues);
            this.activeAlerts = new ArrayList<>(activeAlerts);
            this.healthScore = healthScore;
            this.status = status;
        }

        @Override
        public String toString() {
            return String.format("HealthReport[Status: %s, Score: %.1f/100, Issues: %d, Alerts: %d]",
                status, healthScore, issues.size(), activeAlerts.size());
        }
    }

    public static class HealthMetrics {
        private final AtomicLong healthChecks = new AtomicLong(0);
        private final AtomicLong agentHealthUpdates = new AtomicLong(0);
        private final AtomicLong alertsGenerated = new AtomicLong(0);

        void recordHealthCheck() { healthChecks.incrementAndGet(); }
        void recordAgentHealthUpdate() { agentHealthUpdates.incrementAndGet(); }
        void recordAlert() { alertsGenerated.incrementAndGet(); }

        public long getHealthChecks() { return healthChecks.get(); }
        public long getAgentHealthUpdates() { return agentHealthUpdates.get(); }
        public long getAlertsGenerated() { return alertsGenerated.get(); }

        public HealthMetrics copy() {
            HealthMetrics copy = new HealthMetrics();
            copy.healthChecks.set(this.healthChecks.get());
            copy.agentHealthUpdates.set(this.agentHealthUpdates.get());
            copy.alertsGenerated.set(this.alertsGenerated.get());
            return copy;
        }

        @Override
        public String toString() {
            return String.format("HealthMetrics[Checks: %d, Updates: %d, Alerts: %d]",
                healthChecks.get(), agentHealthUpdates.get(), alertsGenerated.get());
        }
    }

    public interface HealthListener {
        void onStatusChanged(SystemHealthStatus oldStatus, SystemHealthStatus newStatus);
        void onAlert(HealthAlert alert);
    }
}