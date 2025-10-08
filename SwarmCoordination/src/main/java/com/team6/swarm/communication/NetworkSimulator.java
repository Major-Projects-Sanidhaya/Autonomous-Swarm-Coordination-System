/**
 * NETWORKSIMULATOR CLASS - Realistic Network Conditions Simulation
 *
 * PURPOSE:
 * - Simulates realistic network conditions (range limits, delays, failures)
 * - Provides network physics for communication system
 * - Enables testing under imperfect conditions
 *
 * CORE SIMULATION PARAMETERS:
 * 1. communicationRange - Maximum distance for direct communication (default: 100 units)
 * 2. messageLatency - Simulated network delay (default: 100-200ms)
 * 3. failureRate - Percentage of messages that fail (default: 5%)
 * 4. interferenceLevel - Environmental factors affecting range (default: 0.1)
 * 5. signalDecay - How signal strength decreases with distance
 *
 * SIMULATION FEATURES:
 * - Distance-based communication range
 * - Signal strength calculation with interference
 * - Message failure simulation
 * - Network delay simulation
 * - Environmental interference effects
 *
 * SIGNAL STRENGTH CALCULATION:
 * - Base formula: signalStrength = 1.0 - (distance / maxRange)
 * - Interference penalty: signalStrength *= (1.0 - interferenceLevel)
 * - Minimum signal: 0.0 (no communication possible)
 * - Maximum signal: 1.0 (perfect communication)
 *
 * MESSAGE DELIVERY SIMULATION:
 * - Check if agents are in range
 * - Apply random failure based on failureRate
 * - Consider signal strength (weak signals more likely to fail)
 * - Calculate delivery delay based on distance and network conditions
 *
 * USAGE EXAMPLES:
 * - NetworkSimulator simulator = new NetworkSimulator(100.0, 0.05, 0.1);
 * - boolean canComm = simulator.canCommunicate(pos1, pos2, 100.0);
 * - double signal = simulator.calculateSignalStrength(distance, 100.0);
 * - boolean shouldDeliver = simulator.shouldDeliverMessage(sender, receiver, distance);
 * - long delay = simulator.calculateDelay(sender, receiver);
 *
 * INTEGRATION POINTS:
 * - CommunicationManager: Uses for range checking and delivery simulation
 * - NeighborAgent: Uses for signal strength calculation
 * - MessageRouter: Uses for delivery success/failure decisions
 * - Anthony: Uses for realistic network visualization
 */
package com.team6.swarm.communication;

import java.util.Random;

public class NetworkSimulator {
    private final double communicationRange;
    private final double failureRate;
    private final double interferenceLevel;
    private final double baseLatency;
    private final double latencyVariation;
    private final Random random;
    
    // Default constructor with standard parameters
    public NetworkSimulator() {
        this(100.0, 0.05, 0.1, 150.0, 50.0);
    }
    
    // Full constructor with custom parameters
    public NetworkSimulator(double communicationRange, double failureRate, 
                           double interferenceLevel, double baseLatency, double latencyVariation) {
        this.communicationRange = communicationRange;
        this.failureRate = Math.max(0.0, Math.min(1.0, failureRate)); // Clamp to [0,1]
        this.interferenceLevel = Math.max(0.0, Math.min(1.0, interferenceLevel)); // Clamp to [0,1]
        this.baseLatency = baseLatency;
        this.latencyVariation = latencyVariation;
        this.random = new Random();
    }
    
    /**
     * Check if two agents can communicate directly
     * Based on distance and communication range
     */
    public boolean canCommunicate(double distance, double maxRange) {
        return distance <= maxRange;
    }
    
    /**
     * Calculate signal strength between two agents
     * Based on distance and interference level
     */
    public double calculateSignalStrength(double distance, double maxRange) {
        if (distance > maxRange) return 0.0;
        
        // Base signal strength (linear decay)
        double baseSignal = 1.0 - (distance / maxRange);
        
        // Apply interference penalty
        double interferencePenalty = baseSignal * interferenceLevel;
        
        // Add random noise (±5%)
        double noise = (random.nextGaussian() * 0.05);
        
        // Calculate final signal strength
        double signalStrength = baseSignal - interferencePenalty + noise;
        
        // Clamp to valid range [0.0, 1.0]
        return Math.max(0.0, Math.min(1.0, signalStrength));
    }
    
    /**
     * Determine if a message should be delivered
     * Considers range, failure rate, and signal strength
     */
    public boolean shouldDeliverMessage(double distance, double maxRange) {
        // Check if in range
        if (!canCommunicate(distance, maxRange)) {
            return false;
        }
        
        // Calculate signal strength
        double signalStrength = calculateSignalStrength(distance, maxRange);
        
        // Base failure rate
        double baseFailure = failureRate;
        
        // Increase failure rate for weak signals
        double signalPenalty = (1.0 - signalStrength) * 0.3; // Up to 30% additional failure
        
        // Total failure probability
        double totalFailureRate = baseFailure + signalPenalty;
        
        // Random failure decision
        return random.nextDouble() > totalFailureRate;
    }
    
    /**
     * Calculate message delivery delay
     * Based on distance and network conditions
     */
    public long calculateDelay(double distance, double maxRange) {
        // Base latency
        double delay = baseLatency;
        
        // Distance penalty (longer distances = more delay)
        double distancePenalty = (distance / maxRange) * 50.0; // Up to 50ms additional delay
        
        // Random variation
        double variation = (random.nextGaussian() * latencyVariation);
        
        // Calculate total delay
        double totalDelay = delay + distancePenalty + variation;
        
        // Ensure minimum delay of 10ms
        return Math.max(10, (long) totalDelay);
    }
    
    /**
     * Simulate network conditions for a message
     * Returns delivery result with delay and signal strength
     */
    public DeliveryResult simulateDelivery(double distance, double maxRange) {
        double signalStrength = calculateSignalStrength(distance, maxRange);
        boolean willDeliver = shouldDeliverMessage(distance, maxRange);
        long delay = willDeliver ? calculateDelay(distance, maxRange) : 0;
        
        return new DeliveryResult(willDeliver, signalStrength, delay);
    }
    
    /**
     * Get communication range
     */
    public double getCommunicationRange() {
        return communicationRange;
    }
    
    /**
     * Get failure rate
     */
    public double getFailureRate() {
        return failureRate;
    }
    
    /**
     * Get interference level
     */
    public double getInterferenceLevel() {
        return interferenceLevel;
    }
    
    /**
     * Get base latency
     */
    public double getBaseLatency() {
        return baseLatency;
    }
    
    /**
     * Get latency variation
     */
    public double getLatencyVariation() {
        return latencyVariation;
    }
    
    /**
     * Create a high-quality network simulator (low failure, low interference)
     */
    public static NetworkSimulator createHighQualityNetwork() {
        return new NetworkSimulator(120.0, 0.01, 0.05, 100.0, 25.0);
    }
    
    /**
     * Create a poor-quality network simulator (high failure, high interference)
     */
    public static NetworkSimulator createPoorQualityNetwork() {
        return new NetworkSimulator(80.0, 0.15, 0.3, 300.0, 100.0);
    }
    
    /**
     * Create a realistic network simulator (moderate conditions)
     */
    public static NetworkSimulator createRealisticNetwork() {
        return new NetworkSimulator(100.0, 0.05, 0.1, 150.0, 50.0);
    }
    
    /**
     * Delivery result container
     */
    public static class DeliveryResult {
        public final boolean willDeliver;
        public final double signalStrength;
        public final long delay;
        
        public DeliveryResult(boolean willDeliver, double signalStrength, long delay) {
            this.willDeliver = willDeliver;
            this.signalStrength = signalStrength;
            this.delay = delay;
        }
        
        @Override
        public String toString() {
            return String.format("DeliveryResult{deliver=%s, signal=%.2f, delay=%dms}", 
                               willDeliver, signalStrength, delay);
        }
    }
    
    @Override
    public String toString() {
        return String.format("NetworkSimulator{range=%.1f, failure=%.2f, interference=%.2f, latency=%.1f±%.1f}", 
                           communicationRange, failureRate, interferenceLevel, baseLatency, latencyVariation);
    }
}
