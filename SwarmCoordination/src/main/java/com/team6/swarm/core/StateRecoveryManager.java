package com.team6.swarm.core;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Week 8 Implementation: StateRecoveryManager
 *
 * Provides state snapshot and recovery capabilities for the swarm system,
 * enabling rollback to previous states and disaster recovery.
 *
 * Key Features:
 * - State snapshot creation and storage
 * - Point-in-time recovery
 * - Compressed state storage
 * - Automatic snapshot scheduling
 * - State diff tracking
 * - Rollback capability
 *
 * @author Team 6
 * @version Week 8
 */
public class StateRecoveryManager {

    private static final int MAX_SNAPSHOTS = 10;
    private static final int MAX_HISTORY_SIZE = 100;

    private final Map<String, List<StateSnapshot>> agentSnapshots;
    private final Map<String, AgentState> currentStates;
    private final List<SystemSnapshot> systemSnapshots;
    private final RecoveryMetrics metrics;

    private boolean compressionEnabled;
    private int maxSnapshotsPerAgent;

    public StateRecoveryManager() {
        this(MAX_SNAPSHOTS, true);
    }

    public StateRecoveryManager(int maxSnapshotsPerAgent, boolean compressionEnabled) {
        this.agentSnapshots = new ConcurrentHashMap<>();
        this.currentStates = new ConcurrentHashMap<>();
        this.systemSnapshots = Collections.synchronizedList(new ArrayList<>());
        this.metrics = new RecoveryMetrics();
        this.maxSnapshotsPerAgent = maxSnapshotsPerAgent;
        this.compressionEnabled = compressionEnabled;
    }

    /**
     * Creates a snapshot of an agent's state
     */
    public String createSnapshot(String agentId, AgentState state) {
        if (agentId == null || state == null) {
            return null;
        }

        try {
            // Clone the state to avoid reference issues
            AgentState clonedState = cloneAgentState(state);

            String snapshotId = generateSnapshotId(agentId);
            long timestamp = System.currentTimeMillis();

            StateSnapshot snapshot = new StateSnapshot(snapshotId, agentId, clonedState, timestamp);

            // Store snapshot
            List<StateSnapshot> snapshots = agentSnapshots.computeIfAbsent(
                agentId, k -> Collections.synchronizedList(new ArrayList<>()));

            synchronized (snapshots) {
                snapshots.add(snapshot);

                // Remove oldest if exceeding limit
                if (snapshots.size() > maxSnapshotsPerAgent) {
                    snapshots.remove(0);
                }
            }

            currentStates.put(agentId, clonedState);
            metrics.recordSnapshotCreated();

            return snapshotId;

        } catch (Exception e) {
            metrics.recordSnapshotFailed();
            return null;
        }
    }

    /**
     * Restores an agent to a specific snapshot
     */
    public AgentState restoreSnapshot(String agentId, String snapshotId) {
        List<StateSnapshot> snapshots = agentSnapshots.get(agentId);
        if (snapshots == null) {
            return null;
        }

        synchronized (snapshots) {
            for (StateSnapshot snapshot : snapshots) {
                if (snapshot.snapshotId.equals(snapshotId)) {
                    metrics.recordSnapshotRestored();
                    return cloneAgentState(snapshot.state);
                }
            }
        }

        return null;
    }

    /**
     * Restores an agent to the most recent snapshot
     */
    public AgentState restoreLatestSnapshot(String agentId) {
        List<StateSnapshot> snapshots = agentSnapshots.get(agentId);
        if (snapshots == null || snapshots.isEmpty()) {
            return null;
        }

        synchronized (snapshots) {
            StateSnapshot latest = snapshots.get(snapshots.size() - 1);
            metrics.recordSnapshotRestored();
            return cloneAgentState(latest.state);
        }
    }

    /**
     * Restores an agent to a specific point in time
     */
    public AgentState restoreToTimestamp(String agentId, long timestamp) {
        List<StateSnapshot> snapshots = agentSnapshots.get(agentId);
        if (snapshots == null) {
            return null;
        }

        synchronized (snapshots) {
            // Find the snapshot closest to but not after the target timestamp
            StateSnapshot closest = null;
            long closestDiff = Long.MAX_VALUE;

            for (StateSnapshot snapshot : snapshots) {
                if (snapshot.timestamp <= timestamp) {
                    long diff = timestamp - snapshot.timestamp;
                    if (diff < closestDiff) {
                        closest = snapshot;
                        closestDiff = diff;
                    }
                }
            }

            if (closest != null) {
                metrics.recordSnapshotRestored();
                return cloneAgentState(closest.state);
            }
        }

        return null;
    }

    /**
     * Creates a system-wide snapshot of all agents
     */
    public String createSystemSnapshot() {
        String snapshotId = "system_" + System.currentTimeMillis();
        Map<String, AgentState> systemState = new HashMap<>();

        for (Map.Entry<String, AgentState> entry : currentStates.entrySet()) {
            systemState.put(entry.getKey(), cloneAgentState(entry.getValue()));
        }

        SystemSnapshot snapshot = new SystemSnapshot(
            snapshotId,
            System.currentTimeMillis(),
            systemState
        );

        synchronized (systemSnapshots) {
            systemSnapshots.add(snapshot);

            // Keep only last MAX_HISTORY_SIZE snapshots
            if (systemSnapshots.size() > MAX_HISTORY_SIZE) {
                systemSnapshots.remove(0);
            }
        }

        metrics.recordSystemSnapshotCreated();
        return snapshotId;
    }

    /**
     * Restores all agents to a system snapshot
     */
    public Map<String, AgentState> restoreSystemSnapshot(String snapshotId) {
        synchronized (systemSnapshots) {
            for (SystemSnapshot snapshot : systemSnapshots) {
                if (snapshot.snapshotId.equals(snapshotId)) {
                    Map<String, AgentState> restoredStates = new HashMap<>();

                    for (Map.Entry<String, AgentState> entry : snapshot.agentStates.entrySet()) {
                        restoredStates.put(entry.getKey(), cloneAgentState(entry.getValue()));
                    }

                    metrics.recordSystemSnapshotRestored();
                    return restoredStates;
                }
            }
        }

        return null;
    }

    /**
     * Gets all snapshots for an agent
     */
    public List<StateSnapshot> getAgentSnapshots(String agentId) {
        List<StateSnapshot> snapshots = agentSnapshots.get(agentId);
        if (snapshots == null) {
            return Collections.emptyList();
        }

        synchronized (snapshots) {
            return new ArrayList<>(snapshots);
        }
    }

    /**
     * Gets snapshot count for an agent
     */
    public int getSnapshotCount(String agentId) {
        List<StateSnapshot> snapshots = agentSnapshots.get(agentId);
        return snapshots != null ? snapshots.size() : 0;
    }

    /**
     * Clears all snapshots for an agent
     */
    public void clearAgentSnapshots(String agentId) {
        agentSnapshots.remove(agentId);
        currentStates.remove(agentId);
        metrics.recordSnapshotsCleared();
    }

    /**
     * Clears all snapshots
     */
    public void clearAllSnapshots() {
        agentSnapshots.clear();
        currentStates.clear();
        systemSnapshots.clear();
        metrics.recordSnapshotsCleared();
    }

    /**
     * Gets recovery metrics
     */
    public RecoveryMetrics getMetrics() {
        return metrics.copy();
    }

    /**
     * Exports a snapshot to bytes (for storage/transmission)
     */
    public byte[] exportSnapshot(String agentId, String snapshotId) {
        List<StateSnapshot> snapshots = agentSnapshots.get(agentId);
        if (snapshots == null) {
            return null;
        }

        synchronized (snapshots) {
            for (StateSnapshot snapshot : snapshots) {
                if (snapshot.snapshotId.equals(snapshotId)) {
                    try {
                        return serializeState(snapshot.state);
                    } catch (IOException e) {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Imports a snapshot from bytes
     */
    public String importSnapshot(String agentId, byte[] data) {
        try {
            AgentState state = deserializeState(data);
            return createSnapshot(agentId, state);
        } catch (Exception e) {
            return null;
        }
    }

    private AgentState cloneAgentState(AgentState original) {
        if (original == null) {
            return null;
        }

        AgentState clone = new AgentState();
        clone.agentId = original.agentId;
        clone.agentName = original.agentName;
        clone.position = new Point2D(original.position.x, original.position.y);
        clone.velocity = new Vector2D(original.velocity.x, original.velocity.y);
        clone.heading = original.heading;
        clone.maxSpeed = original.maxSpeed;
        clone.maxTurnRate = original.maxTurnRate;
        clone.communicationRange = original.communicationRange;
        clone.status = original.status;
        clone.batteryLevel = original.batteryLevel;
        clone.lastUpdateTime = original.lastUpdateTime;

        return clone;
    }

    private String generateSnapshotId(String agentId) {
        return agentId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private byte[] serializeState(AgentState state) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (compressionEnabled) {
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
                 ObjectOutputStream oos = new ObjectOutputStream(gzipOut)) {
                writeStateToStream(oos, state);
            }
        } else {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                writeStateToStream(oos, state);
            }
        }

        return baos.toByteArray();
    }

    private AgentState deserializeState(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        if (compressionEnabled) {
            try (GZIPInputStream gzipIn = new GZIPInputStream(bais);
                 ObjectInputStream ois = new ObjectInputStream(gzipIn)) {
                return readStateFromStream(ois);
            }
        } else {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return readStateFromStream(ois);
            }
        }
    }

    private void writeStateToStream(ObjectOutputStream oos, AgentState state) throws IOException {
        oos.writeInt(state.agentId);
        oos.writeUTF(state.agentName != null ? state.agentName : "");
        oos.writeDouble(state.position.x);
        oos.writeDouble(state.position.y);
        oos.writeDouble(state.velocity.x);
        oos.writeDouble(state.velocity.y);
        oos.writeDouble(state.heading);
        oos.writeDouble(state.maxSpeed);
        oos.writeDouble(state.maxTurnRate);
        oos.writeDouble(state.communicationRange);
        oos.writeUTF(state.status.name());
        oos.writeDouble(state.batteryLevel);
        oos.writeLong(state.lastUpdateTime);
    }

    private AgentState readStateFromStream(ObjectInputStream ois) throws IOException {
        AgentState state = new AgentState();
        state.agentId = ois.readInt();
        state.agentName = ois.readUTF();
        state.position = new Point2D(ois.readDouble(), ois.readDouble());
        state.velocity = new Vector2D(ois.readDouble(), ois.readDouble());
        state.heading = ois.readDouble();
        state.maxSpeed = ois.readDouble();
        state.maxTurnRate = ois.readDouble();
        state.communicationRange = ois.readDouble();
        state.status = AgentStatus.valueOf(ois.readUTF());
        state.batteryLevel = ois.readDouble();
        state.lastUpdateTime = ois.readLong();
        return state;
    }

    public static class StateSnapshot {
        public final String snapshotId;
        public final String agentId;
        public final AgentState state;
        public final long timestamp;

        public StateSnapshot(String snapshotId, String agentId, AgentState state, long timestamp) {
            this.snapshotId = snapshotId;
            this.agentId = agentId;
            this.state = state;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("Snapshot[%s, agent=%s, time=%d]", snapshotId, agentId, timestamp);
        }
    }

    private static class SystemSnapshot {
        final String snapshotId;
        final long timestamp;
        final Map<String, AgentState> agentStates;

        SystemSnapshot(String snapshotId, long timestamp, Map<String, AgentState> agentStates) {
            this.snapshotId = snapshotId;
            this.timestamp = timestamp;
            this.agentStates = agentStates;
        }
    }

    public static class RecoveryMetrics {
        private long snapshotsCreated = 0;
        private long snapshotsFailed = 0;
        private long snapshotsRestored = 0;
        private long systemSnapshotsCreated = 0;
        private long systemSnapshotsRestored = 0;
        private long snapshotsCleared = 0;

        void recordSnapshotCreated() { snapshotsCreated++; }
        void recordSnapshotFailed() { snapshotsFailed++; }
        void recordSnapshotRestored() { snapshotsRestored++; }
        void recordSystemSnapshotCreated() { systemSnapshotsCreated++; }
        void recordSystemSnapshotRestored() { systemSnapshotsRestored++; }
        void recordSnapshotsCleared() { snapshotsCleared++; }

        public long getSnapshotsCreated() { return snapshotsCreated; }
        public long getSnapshotsFailed() { return snapshotsFailed; }
        public long getSnapshotsRestored() { return snapshotsRestored; }
        public long getSystemSnapshotsCreated() { return systemSnapshotsCreated; }
        public long getSystemSnapshotsRestored() { return systemSnapshotsRestored; }

        public double getSnapshotSuccessRate() {
            long total = snapshotsCreated + snapshotsFailed;
            return total > 0 ? (double) snapshotsCreated / total : 0.0;
        }

        public RecoveryMetrics copy() {
            RecoveryMetrics copy = new RecoveryMetrics();
            copy.snapshotsCreated = this.snapshotsCreated;
            copy.snapshotsFailed = this.snapshotsFailed;
            copy.snapshotsRestored = this.snapshotsRestored;
            copy.systemSnapshotsCreated = this.systemSnapshotsCreated;
            copy.systemSnapshotsRestored = this.systemSnapshotsRestored;
            copy.snapshotsCleared = this.snapshotsCleared;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("RecoveryMetrics[Created: %d, Restored: %d, System: %d/%d, Success Rate: %.1f%%]",
                snapshotsCreated, snapshotsRestored, systemSnapshotsCreated, systemSnapshotsRestored,
                getSnapshotSuccessRate() * 100);
        }
    }
}
