package com.team6.swarm.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Week 8 Implementation: SystemConfiguration
 *
 * Runtime configuration management system with validation,
 * dynamic reconfiguration, and parameter tuning capabilities.
 *
 * Key Features:
 * - Runtime configuration management
 * - Parameter validation
 * - Configuration persistence
 * - Dynamic reconfiguration
 * - Type-safe parameter access
 *
 * @author Team 6
 * @version Week 8
 */
public class SystemConfiguration {

    private final Map<String, ConfigParameter> parameters;
    private final Map<String, ConfigValidator> validators;
    private final List<ConfigChangeListener> listeners;
    private boolean locked;

    public SystemConfiguration() {
        this.parameters = new ConcurrentHashMap<>();
        this.validators = new ConcurrentHashMap<>();
        this.listeners = Collections.synchronizedList(new ArrayList<>());
        this.locked = false;
        initializeDefaults();
    }

    private void initializeDefaults() {
        setParameter("maxAgents", 1000);
        setParameter("updateInterval", 16);
        setParameter("physicsEnabled", true);
        setParameter("communicationRange", 100.0);
        setParameter("worldWidth", 1000.0);
        setParameter("worldHeight", 1000.0);
        setParameter("threadPoolSize", Runtime.getRuntime().availableProcessors());
        setParameter("cacheSize", 5000);
        setParameter("snapshotInterval", 30000L);
        setParameter("debugMode", false);
    }

    public void setParameter(String key, Object value) {
        if (locked) {
            throw new IllegalStateException("Configuration is locked");
        }

        if (validators.containsKey(key)) {
            if (!validators.get(key).validate(value)) {
                throw new IllegalArgumentException("Invalid value for " + key + ": " + value);
            }
        }

        Object oldValue = getParameter(key);
        parameters.put(key, new ConfigParameter(key, value));
        notifyListeners(key, oldValue, value);
    }

    public Object getParameter(String key) {
        ConfigParameter param = parameters.get(key);
        return param != null ? param.value : null;
    }

    public int getInt(String key) {
        Object value = getParameter(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        throw new IllegalArgumentException("Parameter " + key + " is not an integer");
    }

    public double getDouble(String key) {
        Object value = getParameter(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Parameter " + key + " is not a double");
    }

    public boolean getBoolean(String key) {
        Object value = getParameter(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IllegalArgumentException("Parameter " + key + " is not a boolean");
    }

    public long getLong(String key) {
        Object value = getParameter(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Parameter " + key + " is not a long");
    }

    public String getString(String key) {
        Object value = getParameter(key);
        return value != null ? value.toString() : null;
    }

    public void registerValidator(String key, ConfigValidator validator) {
        validators.put(key, validator);
    }

    public void registerListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public Set<String> getParameterNames() {
        return new HashSet<>(parameters.keySet());
    }

    public Map<String, Object> getAllParameters() {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, ConfigParameter> entry : parameters.entrySet()) {
            result.put(entry.getKey(), entry.getValue().value);
        }
        return result;
    }

    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    public void removeParameter(String key) {
        if (locked) {
            throw new IllegalStateException("Configuration is locked");
        }
        parameters.remove(key);
        validators.remove(key);
    }

    public void reset() {
        if (locked) {
            throw new IllegalStateException("Configuration is locked");
        }
        parameters.clear();
        validators.clear();
        initializeDefaults();
    }

    private void notifyListeners(String key, Object oldValue, Object newValue) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigChanged(key, oldValue, newValue);
            } catch (Exception e) {
                System.err.println("Error notifying config listener: " + e.getMessage());
            }
        }
    }

    private static class ConfigParameter {
        final String key;
        final Object value;
        final long timestamp;

        ConfigParameter(String key, Object value) {
            this.key = key;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public interface ConfigValidator {
        boolean validate(Object value);
    }

    public interface ConfigChangeListener {
        void onConfigChanged(String key, Object oldValue, Object newValue);
    }

    @Override
    public String toString() {
        return String.format("SystemConfiguration[%d parameters, locked=%s]",
            parameters.size(), locked);
    }
}
