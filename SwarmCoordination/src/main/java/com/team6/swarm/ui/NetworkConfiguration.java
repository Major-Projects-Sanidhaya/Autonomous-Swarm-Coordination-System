/**
 * NETWORKCONFIGURATION CLASS - Communication System Settings
 *
 * PURPOSE:
 * - Configures communication parameters for agent network
 * - Controls message routing and delivery behavior
 * - Simulates realistic network conditions (latency, packet loss)
 * - Enables testing of fault tolerance and network resilience
 *
 * PARAMETER CATEGORIES:
 *
 * 1. RANGE PARAMETERS:
 *    - maxRange: Maximum communication distance (50-300)
 *    - minSignalStrength: Minimum signal for reliable communication (0.0-1.0)
 *    - rangeDecayRate: How quickly signal degrades with distance (0.0-1.0)
 *
 * 2. MESSAGE PARAMETERS:
 *    - maxMessageSize: Maximum message payload size in bytes (100-10000)
 *    - messageTimeout: Time before message expires (100-5000 ms)
 *    - maxQueueSize: Maximum pending messages per agent (10-100)
 *    - priorityLevels: Number of message priority levels (1-5)
 *
 * 3. NETWORK SIMULATION:
 *    - baseLatency: Minimum message delivery time (0-100 ms)
 *    - latencyVariance: Random latency variation (0-200 ms)
 *    - packetLossRate: Probability of message loss (0.0-0.5)
 *    - duplicateRate: Probability of duplicate messages (0.0-0.1)
 *
 * 4. TOPOLOGY PARAMETERS:
 *    - updateFrequency: How often to recalculate network topology (100-1000 ms)
 *    - maxNeighbors: Maximum simultaneous connections per agent (3-20)
 *    - allowMultihop: Enable message routing through intermediaries
 *    - maxHops: Maximum relay hops for multihop (1-5)
 *
 * 5. RELIABILITY PARAMETERS:
 *    - enableRetransmission: Retry failed messages
 *    - maxRetries: Maximum retransmission attempts (0-5)
 *    - acknowledgmentTimeout: Time to wait for ACK (100-2000 ms)
 *    - enableErrorCorrection: Use error correction codes
 *
 * PRESET CONFIGURATIONS:
 *
 * IDEAL_NETWORK:
 * - Maximum range, zero latency, no packet loss
 * - Use for: Algorithm testing, baseline performance
 *
 * REALISTIC_NETWORK:
 * - Moderate range, realistic latency, low packet loss
 * - Use for: Normal operation, production deployment
 *
 * DEGRADED_NETWORK:
 * - Limited range, high latency, significant packet loss
 * - Use for: Stress testing, fault tolerance validation
 *
 * SHORT_RANGE:
 * - Very limited range, forces multihop communication
 * - Use for: Testing routing algorithms, network partitions
 *
 * USAGE EXAMPLE:
 * NetworkConfiguration config = new NetworkConfiguration();
 * config.setMaxRange(150.0);
 * config.setPacketLossRate(0.05);
 * config.setBaseLatency(20);
 * eventBus.publish(config);
 *
 * Or use preset:
 * NetworkConfiguration config = NetworkConfiguration.realisticNetwork();
 * systemController.applyNetworkConfiguration(config);
 *
 * INTEGRATION POINTS:
 * - Created by: ControlPanel, ParameterPanel
 * - Consumed by: CommunicationManager (John), NetworkSimulator (John)
 * - Validated by: SystemController
 * - Monitored by: NetworkVisualization, PerformanceMonitor
 */
package com.team6.swarm.ui;

import java.util.HashMap;
import java.util.Map;

public class NetworkConfiguration {
    // ==================== RANGE PARAMETERS ====================
    private double maxRange;
    private double minSignalStrength;
    private double rangeDecayRate;
    
    // ==================== MESSAGE PARAMETERS ====================
    private int maxMessageSize;
    private long messageTimeout;
    private int maxQueueSize;
    private int priorityLevels;
    
    // ==================== NETWORK SIMULATION ====================
    private long baseLatency;
    private long latencyVariance;
    private double packetLossRate;
    private double duplicateRate;
    
    // ==================== TOPOLOGY PARAMETERS ====================
    private long updateFrequency;
    private int maxNeighbors;
    private boolean allowMultihop;
    private int maxHops;
    
    // ==================== RELIABILITY PARAMETERS ====================
    private boolean enableRetransmission;
    private int maxRetries;
    private long acknowledgmentTimeout;
    private boolean enableErrorCorrection;
    
    // ==================== METADATA ====================
    private String configurationName;
    private String description;
    private long timestamp;
    private Map<String, Object> customParameters;
    
    /**
     * Default constructor with realistic network parameters
     */
    public NetworkConfiguration() {
        // Default range parameters
        this.maxRange = 100.0;
        this.minSignalStrength = 0.3;
        this.rangeDecayRate = 0.5;
        
        // Default message parameters
        this.maxMessageSize = 1024;  // 1KB
        this.messageTimeout = 1000;  // 1 second
        this.maxQueueSize = 50;
        this.priorityLevels = 3;
        
        // Default network simulation (realistic)
        this.baseLatency = 20;  // 20ms
        this.latencyVariance = 30;  // ±30ms
        this.packetLossRate = 0.02;  // 2% loss
        this.duplicateRate = 0.01;  // 1% duplicates
        
        // Default topology parameters
        this.updateFrequency = 500;  // 500ms
        this.maxNeighbors = 10;
        this.allowMultihop = true;
        this.maxHops = 3;
        
        // Default reliability parameters
        this.enableRetransmission = true;
        this.maxRetries = 2;
        this.acknowledgmentTimeout = 500;  // 500ms
        this.enableErrorCorrection = false;
        
        // Metadata
        this.configurationName = "Default";
        this.description = "Realistic network configuration";
        this.timestamp = System.currentTimeMillis();
        this.customParameters = new HashMap<>();
    }
    
    // ==================== PRESET CONFIGURATIONS ====================
    
    /**
     * Ideal network preset - perfect communication
     */
    public static NetworkConfiguration idealNetwork() {
        NetworkConfiguration config = new NetworkConfiguration();
        config.maxRange = 300.0;
        config.minSignalStrength = 0.0;
        config.baseLatency = 0;
        config.latencyVariance = 0;
        config.packetLossRate = 0.0;
        config.duplicateRate = 0.0;
        config.enableRetransmission = false;
        config.configurationName = "Ideal Network";
        config.description = "Perfect communication for testing";
        return config;
    }
    
    /**
     * Realistic network preset - normal operation
     */
    public static NetworkConfiguration realisticNetwork() {
        NetworkConfiguration config = new NetworkConfiguration();
        config.maxRange = 150.0;
        config.baseLatency = 20;
        config.latencyVariance = 30;
        config.packetLossRate = 0.02;
        config.duplicateRate = 0.01;
        config.enableRetransmission = true;
        config.maxRetries = 2;
        config.configurationName = "Realistic Network";
        config.description = "Normal network conditions";
        return config;
    }
    
    /**
     * Degraded network preset - stress testing
     */
    public static NetworkConfiguration degradedNetwork() {
        NetworkConfiguration config = new NetworkConfiguration();
        config.maxRange = 80.0;
        config.minSignalStrength = 0.5;
        config.baseLatency = 50;
        config.latencyVariance = 100;
        config.packetLossRate = 0.15;
        config.duplicateRate = 0.05;
        config.enableRetransmission = true;
        config.maxRetries = 3;
        config.configurationName = "Degraded Network";
        config.description = "Poor network conditions for stress testing";
        return config;
    }
    
    /**
     * Short range preset - forces multihop
     */
    public static NetworkConfiguration shortRange() {
        NetworkConfiguration config = new NetworkConfiguration();
        config.maxRange = 60.0;
        config.allowMultihop = true;
        config.maxHops = 5;
        config.baseLatency = 15;
        config.latencyVariance = 20;
        config.packetLossRate = 0.05;
        config.configurationName = "Short Range";
        config.description = "Limited range requiring multihop routing";
        return config;
    }
    
    /**
     * High reliability preset - mission critical
     */
    public static NetworkConfiguration highReliability() {
        NetworkConfiguration config = new NetworkConfiguration();
        config.maxRange = 120.0;
        config.enableRetransmission = true;
        config.maxRetries = 5;
        config.acknowledgmentTimeout = 1000;
        config.enableErrorCorrection = true;
        config.packetLossRate = 0.01;
        config.priorityLevels = 5;
        config.configurationName = "High Reliability";
        config.description = "Maximum reliability for critical operations";
        return config;
    }
    
    // ==================== GETTERS ====================
    
    public double getMaxRange() { return maxRange; }
    public double getMinSignalStrength() { return minSignalStrength; }
    public double getRangeDecayRate() { return rangeDecayRate; }
    public int getMaxMessageSize() { return maxMessageSize; }
    public long getMessageTimeout() { return messageTimeout; }
    public int getMaxQueueSize() { return maxQueueSize; }
    public int getPriorityLevels() { return priorityLevels; }
    public long getBaseLatency() { return baseLatency; }
    public long getLatencyVariance() { return latencyVariance; }
    public double getPacketLossRate() { return packetLossRate; }
    public double getDuplicateRate() { return duplicateRate; }
    public long getUpdateFrequency() { return updateFrequency; }
    public int getMaxNeighbors() { return maxNeighbors; }
    public boolean isAllowMultihop() { return allowMultihop; }
    public int getMaxHops() { return maxHops; }
    public boolean isEnableRetransmission() { return enableRetransmission; }
    public int getMaxRetries() { return maxRetries; }
    public long getAcknowledgmentTimeout() { return acknowledgmentTimeout; }
    public boolean isEnableErrorCorrection() { return enableErrorCorrection; }
    public String getConfigurationName() { return configurationName; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
    
    // ==================== SETTERS WITH VALIDATION ====================
    
    public void setMaxRange(double range) {
        this.maxRange = clamp(range, 50.0, 300.0);
    }
    
    public void setMinSignalStrength(double strength) {
        this.minSignalStrength = clamp(strength, 0.0, 1.0);
    }
    
    public void setRangeDecayRate(double rate) {
        this.rangeDecayRate = clamp(rate, 0.0, 1.0);
    }
    
    public void setMaxMessageSize(int size) {
        this.maxMessageSize = (int) clamp(size, 100, 10000);
    }
    
    public void setMessageTimeout(long timeout) {
        this.messageTimeout = (long) clamp(timeout, 100, 5000);
    }
    
    public void setMaxQueueSize(int size) {
        this.maxQueueSize = (int) clamp(size, 10, 100);
    }
    
    public void setPriorityLevels(int levels) {
        this.priorityLevels = (int) clamp(levels, 1, 5);
    }
    
    public void setBaseLatency(long latency) {
        this.baseLatency = (long) clamp(latency, 0, 100);
    }
    
    public void setLatencyVariance(long variance) {
        this.latencyVariance = (long) clamp(variance, 0, 200);
    }
    
    public void setPacketLossRate(double rate) {
        this.packetLossRate = clamp(rate, 0.0, 0.5);
    }
    
    public void setDuplicateRate(double rate) {
        this.duplicateRate = clamp(rate, 0.0, 0.1);
    }
    
    public void setUpdateFrequency(long frequency) {
        this.updateFrequency = (long) clamp(frequency, 100, 1000);
    }
    
    public void setMaxNeighbors(int neighbors) {
        this.maxNeighbors = (int) clamp(neighbors, 3, 20);
    }
    
    public void setAllowMultihop(boolean allow) {
        this.allowMultihop = allow;
    }
    
    public void setMaxHops(int hops) {
        this.maxHops = (int) clamp(hops, 1, 5);
    }
    
    public void setEnableRetransmission(boolean enable) {
        this.enableRetransmission = enable;
    }
    
    public void setMaxRetries(int retries) {
        this.maxRetries = (int) clamp(retries, 0, 5);
    }
    
    public void setAcknowledgmentTimeout(long timeout) {
        this.acknowledgmentTimeout = (long) clamp(timeout, 100, 2000);
    }
    
    public void setEnableErrorCorrection(boolean enable) {
        this.enableErrorCorrection = enable;
    }
    
    public void setConfigurationName(String name) {
        this.configurationName = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // ==================== CUSTOM PARAMETERS ====================
    
    public void setCustomParameter(String key, Object value) {
        customParameters.put(key, value);
    }
    
    public Object getCustomParameter(String key) {
        return customParameters.get(key);
    }
    
    public Map<String, Object> getCustomParameters() {
        return new HashMap<>(customParameters);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Validate all parameters
     */
    public boolean validate() {
        return maxRange >= 50.0 && maxRange <= 300.0 &&
               minSignalStrength >= 0.0 && minSignalStrength <= 1.0 &&
               maxMessageSize >= 100 && maxMessageSize <= 10000 &&
               messageTimeout >= 100 && messageTimeout <= 5000 &&
               packetLossRate >= 0.0 && packetLossRate <= 0.5 &&
               duplicateRate >= 0.0 && duplicateRate <= 0.1 &&
               maxNeighbors >= 3 && maxNeighbors <= 20 &&
               maxHops >= 1 && maxHops <= 5;
    }
    
    /**
     * Calculate expected latency for given distance
     */
    public long calculateExpectedLatency(double distance) {
        if (distance > maxRange) {
            return -1;  // Out of range
        }
        
        // Base latency + distance factor + random variance
        double distanceFactor = (distance / maxRange) * latencyVariance;
        long variance = (long) (Math.random() * latencyVariance);
        return baseLatency + (long) distanceFactor + variance;
    }
    
    /**
     * Calculate signal strength for given distance
     */
    public double calculateSignalStrength(double distance) {
        if (distance > maxRange) {
            return 0.0;
        }
        
        // Exponential decay based on distance
        double normalizedDistance = distance / maxRange;
        return Math.max(0.0, 1.0 - Math.pow(normalizedDistance, 1.0 + rangeDecayRate));
    }
    
    /**
     * Check if message should be dropped (packet loss simulation)
     */
    public boolean shouldDropMessage() {
        return Math.random() < packetLossRate;
    }
    
    /**
     * Check if message should be duplicated
     */
    public boolean shouldDuplicateMessage() {
        return Math.random() < duplicateRate;
    }
    
    /**
     * Clone configuration
     */
    public NetworkConfiguration clone() {
        NetworkConfiguration clone = new NetworkConfiguration();
        clone.maxRange = this.maxRange;
        clone.minSignalStrength = this.minSignalStrength;
        clone.rangeDecayRate = this.rangeDecayRate;
        clone.maxMessageSize = this.maxMessageSize;
        clone.messageTimeout = this.messageTimeout;
        clone.maxQueueSize = this.maxQueueSize;
        clone.priorityLevels = this.priorityLevels;
        clone.baseLatency = this.baseLatency;
        clone.latencyVariance = this.latencyVariance;
        clone.packetLossRate = this.packetLossRate;
        clone.duplicateRate = this.duplicateRate;
        clone.updateFrequency = this.updateFrequency;
        clone.maxNeighbors = this.maxNeighbors;
        clone.allowMultihop = this.allowMultihop;
        clone.maxHops = this.maxHops;
        clone.enableRetransmission = this.enableRetransmission;
        clone.maxRetries = this.maxRetries;
        clone.acknowledgmentTimeout = this.acknowledgmentTimeout;
        clone.enableErrorCorrection = this.enableErrorCorrection;
        clone.configurationName = this.configurationName + " (Copy)";
        clone.description = this.description;
        clone.customParameters = new HashMap<>(this.customParameters);
        return clone;
    }
    
    /**
     * Get configuration summary
     */
    public String getSummary() {
        return String.format("%s: Range=%.0f, Latency=%dms, Loss=%.1f%%", 
                           configurationName, maxRange, baseLatency, packetLossRate * 100);
    }
    
    /**
     * Get detailed description
     */
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration: ").append(configurationName).append("\n");
        sb.append("Description: ").append(description).append("\n\n");
        sb.append("Range:\n");
        sb.append(String.format("  Max Range: %.0f\n", maxRange));
        sb.append(String.format("  Min Signal: %.2f\n", minSignalStrength));
        sb.append("\nLatency:\n");
        sb.append(String.format("  Base: %dms\n", baseLatency));
        sb.append(String.format("  Variance: ±%dms\n", latencyVariance));
        sb.append("\nReliability:\n");
        sb.append(String.format("  Packet Loss: %.1f%%\n", packetLossRate * 100));
        sb.append(String.format("  Duplicates: %.1f%%\n", duplicateRate * 100));
        sb.append(String.format("  Retransmission: %s\n", enableRetransmission ? "Enabled" : "Disabled"));
        sb.append("\nTopology:\n");
        sb.append(String.format("  Max Neighbors: %d\n", maxNeighbors));
        sb.append(String.format("  Multihop: %s\n", allowMultihop ? "Enabled" : "Disabled"));
        if (allowMultihop) {
            sb.append(String.format("  Max Hops: %d\n", maxHops));
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("NetworkConfiguration{name='%s', range=%.0f, latency=%dms, loss=%.2f%%}", 
                           configurationName, maxRange, baseLatency, packetLossRate * 100);
    }
}
