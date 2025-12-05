package com.team6.demo.scenarios;

import com.team6.demo.core.Environment;

/**
 * BaseScenario - Abstract base class providing default implementations
 * for visualization support methods
 */
public abstract class BaseScenario implements Scenario {
    protected Environment environment;
    protected double simulationDuration;

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public double getSimulationTime() {
        return simulationDuration;
    }

    /**
     * Default implementation - subclasses should override for scenario-specific logic
     */
    @Override
    public void update(double deltaTime) {
        // Default: do nothing (scenarios control their own loops)
        // Visual mode will override this
    }

    /**
     * Default implementation - subclasses should override for specific status
     */
    @Override
    public String getStatusInfo() {
        return "Running...";
    }
}
