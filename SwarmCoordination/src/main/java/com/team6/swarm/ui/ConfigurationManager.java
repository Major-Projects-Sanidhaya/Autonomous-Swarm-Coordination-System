package com.team6.swarm.ui;

import com.team6.swarm.core.AgentState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Week 9-10: Manage system settings
 * Purpose: Users want to save their setups
 * Author: Anthony (UI Team)
 */
public class ConfigurationManager {
    
    private static final String CONFIG_DIR = "config";
    private static final String DEFAULT_CONFIG = "default_config.json";
    
    private final Gson gson;
    private Configuration currentConfig;
    
    public ConfigurationManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        currentConfig = createDefaultConfiguration();
    }
    
    /**
     * Save configuration to file
     */
    public boolean saveConfiguration(String filename) {
        try {
            File configDir = new File(CONFIG_DIR);
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            File configFile = new File(configDir, filename);
            try (Writer writer = new FileWriter(configFile)) {
                gson.toJson(currentConfig, writer);
            }
            
            System.out.println("ConfigurationManager: Configuration saved to " + filename);
            return true;
        } catch (IOException e) {
            System.err.println("ConfigurationManager: Failed to save configuration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load configuration from file
     */
    public boolean loadConfiguration(String filename) {
        try {
            File configFile = new File(CONFIG_DIR, filename);
            if (!configFile.exists()) {
                System.err.println("ConfigurationManager: Configuration file not found: " + filename);
                return false;
            }
            
            try (Reader reader = new FileReader(configFile)) {
                currentConfig = gson.fromJson(reader, Configuration.class);
            }
            
            System.out.println("ConfigurationManager: Configuration loaded from " + filename);
            return true;
        } catch (IOException e) {
            System.err.println("ConfigurationManager: Failed to load configuration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reset to default configuration
     */
    public void resetToDefaults() {
        currentConfig = createDefaultConfiguration();
        System.out.println("ConfigurationManager: Reset to default configuration");
    }
    
    /**
     * Create default configuration
     */
    private Configuration createDefaultConfiguration() {
        Configuration config = new Configuration();
        config.agentParameters = createDefaultAgentParameters();
        config.behaviorParameters = createDefaultBehaviorParameters();
        config.uiPreferences = createDefaultUIPreferences();
        return config;
    }
    
    private Map<String, Object> createDefaultAgentParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("maxSpeed", 5.0);
        params.put("initialBattery", 100.0);
        params.put("communicationRange", 100.0);
        return params;
    }
    
    private Map<String, Object> createDefaultBehaviorParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("separation", 0.5);
        params.put("alignment", 0.5);
        params.put("cohesion", 0.5);
        return params;
    }
    
    private Map<String, Object> createDefaultUIPreferences() {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("theme", "LIGHT");
        prefs.put("showGrid", true);
        prefs.put("showTrails", true);
        return prefs;
    }
    
    public Configuration getCurrentConfig() {
        return currentConfig;
    }
    
    /**
     * Configuration data class
     */
    public static class Configuration {
        public Map<String, Object> agentParameters;
        public Map<String, Object> behaviorParameters;
        public Map<String, Object> uiPreferences;
        public Map<String, Object> networkSettings;
        
        public Configuration() {
            agentParameters = new HashMap<>();
            behaviorParameters = new HashMap<>();
            uiPreferences = new HashMap<>();
            networkSettings = new HashMap<>();
        }
    }
}
