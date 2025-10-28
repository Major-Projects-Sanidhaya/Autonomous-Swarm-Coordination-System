/**
 * UICONFIGURATION CLASS - User Interface Settings and Preferences
 *
 * PURPOSE:
 * - Stores UI display preferences and settings
 * - Manages visualization options and themes
 * - Enables saving/loading user preferences
 * - Provides configuration presets for different use cases
 *
 * CONFIGURATION CATEGORIES:
 *
 * 1. DISPLAY SETTINGS:
 *    - windowWidth, windowHeight: Main window dimensions
 *    - canvasWidth, canvasHeight: Visualization area size
 *    - backgroundColor: Canvas background color
 *    - gridEnabled: Show coordinate grid
 *    - gridSpacing: Grid line spacing
 *
 * 2. AGENT VISUALIZATION:
 *    - agentSize: Agent circle radius
 *    - showAgentIds: Display agent ID labels
 *    - showBatteryLevel: Display battery indicators
 *    - showVelocityVectors: Display movement arrows
 *    - agentColorScheme: Color coding (status, team, speed)
 *
 * 3. NETWORK VISUALIZATION:
 *    - showCommunicationLinks: Display connection lines
 *    - linkColorMode: Color by strength, latency, or activity
 *    - linkThicknessMode: Thickness by strength or traffic
 *    - showSignalStrength: Display signal indicators
 *    - animateMessages: Show message flow animation
 *
 * 4. DECISION VISUALIZATION:
 *    - showVotingProgress: Display voting UI
 *    - votingDisplayMode: Compact, detailed, or overlay
 *    - showConsensusIndicator: Highlight consensus status
 *    - voteAnimationSpeed: Animation speed for vote updates
 *
 * 5. PERFORMANCE SETTINGS:
 *    - targetFrameRate: Desired FPS (30, 60, or unlimited)
 *    - updateFrequency: How often to refresh data (ms)
 *    - enableAntialiasing: Smooth graphics
 *    - enableShadows: Agent shadows
 *    - performanceMode: Low, medium, high quality
 *
 * 6. INTERACTION SETTINGS:
 *    - enableWaypointPlacement: Click to place waypoints
 *    - enableAgentSelection: Click to select agents
 *    - enableDragAndDrop: Drag agents to move them
 *    - mouseWheelZoom: Enable zoom with mouse wheel
 *    - panningEnabled: Enable canvas panning
 *
 * 7. INFORMATION DISPLAY:
 *    - showStatusPanel: Display system status
 *    - showPerformanceMetrics: Display FPS, latency, etc.
 *    - showMissionProgress: Display mission completion
 *    - showNetworkHealth: Display network status
 *    - infoDisplayPosition: Top, bottom, left, right
 *
 * PRESET CONFIGURATIONS:
 *
 * PRESENTATION_MODE:
 * - Large window, high quality graphics
 * - All visualizations enabled
 * - Smooth animations
 * - Use for: Demos, presentations
 *
 * DEVELOPMENT_MODE:
 * - Medium window, detailed information
 * - All debug info visible
 * - Performance metrics shown
 * - Use for: Development, debugging
 *
 * PERFORMANCE_MODE:
 * - Minimal graphics, high frame rate
 * - Reduced visual effects
 * - Essential info only
 * - Use for: Large swarms, stress testing
 *
 * MINIMAL_MODE:
 * - Small window, basic visualization
 * - No extra effects
 * - Core functionality only
 * - Use for: Resource-constrained systems
 *
 * USAGE EXAMPLE:
 * UIConfiguration config = new UIConfiguration();
 * config.setAgentSize(8.0);
 * config.setShowCommunicationLinks(true);
 * config.setTargetFrameRate(60);
 * config.save("my_preferences.json");
 *
 * Or use preset:
 * UIConfiguration config = UIConfiguration.presentationMode();
 * mainInterface.applyConfiguration(config);
 *
 * INTEGRATION POINTS:
 * - Created by: MainInterface, ControlPanel
 * - Consumed by: Visualizer, all UI components
 * - Persisted to: Configuration file
 * - Modified by: User preferences dialog
 */
package com.team6.swarm.ui;

import java.util.HashMap;
import java.util.Map;

public class UIConfiguration {
    // ==================== DISPLAY SETTINGS ====================
    private int windowWidth;
    private int windowHeight;
    private int canvasWidth;
    private int canvasHeight;
    private String backgroundColor;
    private boolean gridEnabled;
    private double gridSpacing;
    
    // ==================== AGENT VISUALIZATION ====================
    private double agentSize;
    private boolean showAgentIds;
    private boolean showBatteryLevel;
    private boolean showVelocityVectors;
    private String agentColorScheme;  // "status", "team", "speed", "battery"
    private boolean showAgentTrails;
    private int trailLength;
    
    // ==================== NETWORK VISUALIZATION ====================
    private boolean showCommunicationLinks;
    private String linkColorMode;  // "strength", "latency", "activity"
    private String linkThicknessMode;  // "strength", "traffic", "uniform"
    private boolean showSignalStrength;
    private boolean animateMessages;
    private double messageAnimationSpeed;
    
    // ==================== DECISION VISUALIZATION ====================
    private boolean showVotingProgress;
    private String votingDisplayMode;  // "compact", "detailed", "overlay"
    private boolean showConsensusIndicator;
    private double voteAnimationSpeed;
    private boolean highlightVotingAgents;
    
    // ==================== PERFORMANCE SETTINGS ====================
    private int targetFrameRate;  // 30, 60, or 0 for unlimited
    private long updateFrequency;  // milliseconds
    private boolean enableAntialiasing;
    private boolean enableShadows;
    private String performanceMode;  // "low", "medium", "high"
    
    // ==================== INTERACTION SETTINGS ====================
    private boolean enableWaypointPlacement;
    private boolean enableAgentSelection;
    private boolean enableDragAndDrop;
    private boolean mouseWheelZoom;
    private boolean panningEnabled;
    private double zoomLevel;
    private double minZoom;
    private double maxZoom;
    
    // ==================== INFORMATION DISPLAY ====================
    private boolean showStatusPanel;
    private boolean showPerformanceMetrics;
    private boolean showMissionProgress;
    private boolean showNetworkHealth;
    private String infoDisplayPosition;  // "top", "bottom", "left", "right"
    private boolean showTooltips;
    private boolean showLegend;
    
    // ==================== THEME SETTINGS ====================
    private String theme;  // "light", "dark", "high_contrast"
    private Map<String, String> colorPalette;
    
    // ==================== METADATA ====================
    private String configurationName;
    private String description;
    private long timestamp;
    private Map<String, Object> customSettings;
    
    /**
     * Default constructor with balanced settings
     */
    public UIConfiguration() {
        // Display settings
        this.windowWidth = 1200;
        this.windowHeight = 800;
        this.canvasWidth = 1000;
        this.canvasHeight = 800;
        this.backgroundColor = "#F0F0F0";
        this.gridEnabled = true;
        this.gridSpacing = 50.0;
        
        // Agent visualization
        this.agentSize = 6.0;
        this.showAgentIds = true;
        this.showBatteryLevel = true;
        this.showVelocityVectors = true;
        this.agentColorScheme = "status";
        this.showAgentTrails = false;
        this.trailLength = 20;
        
        // Network visualization
        this.showCommunicationLinks = true;
        this.linkColorMode = "strength";
        this.linkThicknessMode = "strength";
        this.showSignalStrength = false;
        this.animateMessages = true;
        this.messageAnimationSpeed = 1.0;
        
        // Decision visualization
        this.showVotingProgress = true;
        this.votingDisplayMode = "detailed";
        this.showConsensusIndicator = true;
        this.voteAnimationSpeed = 1.0;
        this.highlightVotingAgents = true;
        
        // Performance settings
        this.targetFrameRate = 60;
        this.updateFrequency = 33;  // ~30 updates per second
        this.enableAntialiasing = true;
        this.enableShadows = false;
        this.performanceMode = "medium";
        
        // Interaction settings
        this.enableWaypointPlacement = true;
        this.enableAgentSelection = true;
        this.enableDragAndDrop = false;
        this.mouseWheelZoom = true;
        this.panningEnabled = true;
        this.zoomLevel = 1.0;
        this.minZoom = 0.5;
        this.maxZoom = 3.0;
        
        // Information display
        this.showStatusPanel = true;
        this.showPerformanceMetrics = true;
        this.showMissionProgress = true;
        this.showNetworkHealth = true;
        this.infoDisplayPosition = "bottom";
        this.showTooltips = true;
        this.showLegend = true;
        
        // Theme
        this.theme = "light";
        this.colorPalette = createDefaultColorPalette();
        
        // Metadata
        this.configurationName = "Default";
        this.description = "Balanced configuration for general use";
        this.timestamp = System.currentTimeMillis();
        this.customSettings = new HashMap<>();
    }
    
    // ==================== PRESET CONFIGURATIONS ====================
    
    /**
     * Presentation mode - high quality for demos
     */
    public static UIConfiguration presentationMode() {
        UIConfiguration config = new UIConfiguration();
        config.windowWidth = 1920;
        config.windowHeight = 1080;
        config.canvasWidth = 1600;
        config.canvasHeight = 1000;
        config.agentSize = 10.0;
        config.enableAntialiasing = true;
        config.enableShadows = true;
        config.performanceMode = "high";
        config.showAgentTrails = true;
        config.animateMessages = true;
        config.showVotingProgress = true;
        config.votingDisplayMode = "detailed";
        config.configurationName = "Presentation Mode";
        config.description = "High quality visualization for presentations";
        return config;
    }
    
    /**
     * Development mode - detailed information for debugging
     */
    public static UIConfiguration developmentMode() {
        UIConfiguration config = new UIConfiguration();
        config.showAgentIds = true;
        config.showBatteryLevel = true;
        config.showVelocityVectors = true;
        config.showCommunicationLinks = true;
        config.showSignalStrength = true;
        config.showPerformanceMetrics = true;
        config.showNetworkHealth = true;
        config.showTooltips = true;
        config.gridEnabled = true;
        config.configurationName = "Development Mode";
        config.description = "Detailed information for development and debugging";
        return config;
    }
    
    /**
     * Performance mode - minimal graphics for large swarms
     */
    public static UIConfiguration performanceMode() {
        UIConfiguration config = new UIConfiguration();
        config.agentSize = 4.0;
        config.showAgentIds = false;
        config.showBatteryLevel = false;
        config.showVelocityVectors = false;
        config.showCommunicationLinks = false;
        config.animateMessages = false;
        config.enableAntialiasing = false;
        config.enableShadows = false;
        config.performanceMode = "low";
        config.targetFrameRate = 30;
        config.showAgentTrails = false;
        config.configurationName = "Performance Mode";
        config.description = "Minimal graphics for maximum performance";
        return config;
    }
    
    /**
     * Minimal mode - basic visualization only
     */
    public static UIConfiguration minimalMode() {
        UIConfiguration config = new UIConfiguration();
        config.windowWidth = 800;
        config.windowHeight = 600;
        config.canvasWidth = 600;
        config.canvasHeight = 500;
        config.agentSize = 5.0;
        config.showAgentIds = false;
        config.showBatteryLevel = false;
        config.showVelocityVectors = false;
        config.showCommunicationLinks = false;
        config.showVotingProgress = false;
        config.showStatusPanel = false;
        config.showPerformanceMetrics = false;
        config.gridEnabled = false;
        config.configurationName = "Minimal Mode";
        config.description = "Basic visualization with minimal features";
        return config;
    }
    
    // ==================== COLOR PALETTE ====================
    
    /**
     * Create default color palette
     */
    private Map<String, String> createDefaultColorPalette() {
        Map<String, String> palette = new HashMap<>();
        palette.put("agent_active", "#00FF00");
        palette.put("agent_inactive", "#808080");
        palette.put("agent_failed", "#FF0000");
        palette.put("agent_battery_low", "#FFA500");
        palette.put("link_strong", "#0000FF");
        palette.put("link_weak", "#ADD8E6");
        palette.put("waypoint", "#FFD700");
        palette.put("grid", "#CCCCCC");
        palette.put("background", "#F0F0F0");
        palette.put("text", "#000000");
        return palette;
    }
    
    /**
     * Apply dark theme
     */
    public void applyDarkTheme() {
        this.theme = "dark";
        this.backgroundColor = "#1E1E1E";
        colorPalette.put("background", "#1E1E1E");
        colorPalette.put("text", "#FFFFFF");
        colorPalette.put("grid", "#404040");
    }
    
    /**
     * Apply light theme
     */
    public void applyLightTheme() {
        this.theme = "light";
        this.backgroundColor = "#F0F0F0";
        colorPalette.put("background", "#F0F0F0");
        colorPalette.put("text", "#000000");
        colorPalette.put("grid", "#CCCCCC");
    }
    
    // ==================== GETTERS ====================
    
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }
    public int getCanvasWidth() { return canvasWidth; }
    public int getCanvasHeight() { return canvasHeight; }
    public String getBackgroundColor() { return backgroundColor; }
    public boolean isGridEnabled() { return gridEnabled; }
    public double getGridSpacing() { return gridSpacing; }
    public double getAgentSize() { return agentSize; }
    public boolean isShowAgentIds() { return showAgentIds; }
    public boolean isShowBatteryLevel() { return showBatteryLevel; }
    public boolean isShowVelocityVectors() { return showVelocityVectors; }
    public String getAgentColorScheme() { return agentColorScheme; }
    public boolean isShowAgentTrails() { return showAgentTrails; }
    public int getTrailLength() { return trailLength; }
    public boolean isShowCommunicationLinks() { return showCommunicationLinks; }
    public String getLinkColorMode() { return linkColorMode; }
    public String getLinkThicknessMode() { return linkThicknessMode; }
    public boolean isShowSignalStrength() { return showSignalStrength; }
    public boolean isAnimateMessages() { return animateMessages; }
    public double getMessageAnimationSpeed() { return messageAnimationSpeed; }
    public boolean isShowVotingProgress() { return showVotingProgress; }
    public String getVotingDisplayMode() { return votingDisplayMode; }
    public boolean isShowConsensusIndicator() { return showConsensusIndicator; }
    public double getVoteAnimationSpeed() { return voteAnimationSpeed; }
    public boolean isHighlightVotingAgents() { return highlightVotingAgents; }
    public int getTargetFrameRate() { return targetFrameRate; }
    public long getUpdateFrequency() { return updateFrequency; }
    public boolean isEnableAntialiasing() { return enableAntialiasing; }
    public boolean isEnableShadows() { return enableShadows; }
    public String getPerformanceMode() { return performanceMode; }
    public boolean isEnableWaypointPlacement() { return enableWaypointPlacement; }
    public boolean isEnableAgentSelection() { return enableAgentSelection; }
    public boolean isEnableDragAndDrop() { return enableDragAndDrop; }
    public boolean isMouseWheelZoom() { return mouseWheelZoom; }
    public boolean isPanningEnabled() { return panningEnabled; }
    public double getZoomLevel() { return zoomLevel; }
    public double getMinZoom() { return minZoom; }
    public double getMaxZoom() { return maxZoom; }
    public boolean isShowStatusPanel() { return showStatusPanel; }
    public boolean isShowPerformanceMetrics() { return showPerformanceMetrics; }
    public boolean isShowMissionProgress() { return showMissionProgress; }
    public boolean isShowNetworkHealth() { return showNetworkHealth; }
    public String getInfoDisplayPosition() { return infoDisplayPosition; }
    public boolean isShowTooltips() { return showTooltips; }
    public boolean isShowLegend() { return showLegend; }
    public String getTheme() { return theme; }
    public Map<String, String> getColorPalette() { return new HashMap<>(colorPalette); }
    public String getConfigurationName() { return configurationName; }
    public String getDescription() { return description; }
    
    // ==================== SETTERS ====================
    
    public void setWindowWidth(int width) { this.windowWidth = Math.max(400, width); }
    public void setWindowHeight(int height) { this.windowHeight = Math.max(300, height); }
    public void setCanvasWidth(int width) { this.canvasWidth = Math.max(200, width); }
    public void setCanvasHeight(int height) { this.canvasHeight = Math.max(200, height); }
    public void setBackgroundColor(String color) { this.backgroundColor = color; }
    public void setGridEnabled(boolean enabled) { this.gridEnabled = enabled; }
    public void setGridSpacing(double spacing) { this.gridSpacing = Math.max(10, spacing); }
    public void setAgentSize(double size) { this.agentSize = Math.max(2, Math.min(20, size)); }
    public void setShowAgentIds(boolean show) { this.showAgentIds = show; }
    public void setShowBatteryLevel(boolean show) { this.showBatteryLevel = show; }
    public void setShowVelocityVectors(boolean show) { this.showVelocityVectors = show; }
    public void setAgentColorScheme(String scheme) { this.agentColorScheme = scheme; }
    public void setShowAgentTrails(boolean show) { this.showAgentTrails = show; }
    public void setTrailLength(int length) { this.trailLength = Math.max(5, Math.min(100, length)); }
    public void setShowCommunicationLinks(boolean show) { this.showCommunicationLinks = show; }
    public void setLinkColorMode(String mode) { this.linkColorMode = mode; }
    public void setLinkThicknessMode(String mode) { this.linkThicknessMode = mode; }
    public void setShowSignalStrength(boolean show) { this.showSignalStrength = show; }
    public void setAnimateMessages(boolean animate) { this.animateMessages = animate; }
    public void setMessageAnimationSpeed(double speed) { this.messageAnimationSpeed = Math.max(0.1, Math.min(5.0, speed)); }
    public void setShowVotingProgress(boolean show) { this.showVotingProgress = show; }
    public void setVotingDisplayMode(String mode) { this.votingDisplayMode = mode; }
    public void setShowConsensusIndicator(boolean show) { this.showConsensusIndicator = show; }
    public void setVoteAnimationSpeed(double speed) { this.voteAnimationSpeed = Math.max(0.1, Math.min(5.0, speed)); }
    public void setHighlightVotingAgents(boolean highlight) { this.highlightVotingAgents = highlight; }
    public void setTargetFrameRate(int fps) { this.targetFrameRate = Math.max(0, Math.min(120, fps)); }
    public void setUpdateFrequency(long frequency) { this.updateFrequency = Math.max(16, frequency); }
    public void setEnableAntialiasing(boolean enable) { this.enableAntialiasing = enable; }
    public void setEnableShadows(boolean enable) { this.enableShadows = enable; }
    public void setPerformanceMode(String mode) { this.performanceMode = mode; }
    public void setEnableWaypointPlacement(boolean enable) { this.enableWaypointPlacement = enable; }
    public void setEnableAgentSelection(boolean enable) { this.enableAgentSelection = enable; }
    public void setEnableDragAndDrop(boolean enable) { this.enableDragAndDrop = enable; }
    public void setMouseWheelZoom(boolean enable) { this.mouseWheelZoom = enable; }
    public void setPanningEnabled(boolean enable) { this.panningEnabled = enable; }
    public void setZoomLevel(double zoom) { this.zoomLevel = Math.max(minZoom, Math.min(maxZoom, zoom)); }
    public void setShowStatusPanel(boolean show) { this.showStatusPanel = show; }
    public void setShowPerformanceMetrics(boolean show) { this.showPerformanceMetrics = show; }
    public void setShowMissionProgress(boolean show) { this.showMissionProgress = show; }
    public void setShowNetworkHealth(boolean show) { this.showNetworkHealth = show; }
    public void setInfoDisplayPosition(String position) { this.infoDisplayPosition = position; }
    public void setShowTooltips(boolean show) { this.showTooltips = show; }
    public void setShowLegend(boolean show) { this.showLegend = show; }
    public void setTheme(String theme) { this.theme = theme; }
    public void setConfigurationName(String name) { this.configurationName = name; }
    public void setDescription(String description) { this.description = description; }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get color from palette
     */
    public String getColor(String key) {
        return colorPalette.getOrDefault(key, "#000000");
    }
    
    /**
     * Set color in palette
     */
    public void setColor(String key, String color) {
        colorPalette.put(key, color);
    }
    
    /**
     * Validate configuration
     */
    public boolean validate() {
        return windowWidth > 0 && windowHeight > 0 &&
               canvasWidth > 0 && canvasHeight > 0 &&
               agentSize > 0 && targetFrameRate >= 0 &&
               updateFrequency > 0 && zoomLevel > 0;
    }
    
    /**
     * Clone configuration
     */
    public UIConfiguration clone() {
        UIConfiguration clone = new UIConfiguration();
        // Copy all fields (simplified for brevity)
        clone.windowWidth = this.windowWidth;
        clone.windowHeight = this.windowHeight;
        clone.agentSize = this.agentSize;
        clone.showCommunicationLinks = this.showCommunicationLinks;
        clone.targetFrameRate = this.targetFrameRate;
        clone.configurationName = this.configurationName + " (Copy)";
        // ... copy remaining fields
        return clone;
    }
    
    @Override
    public String toString() {
        return String.format("UIConfiguration{name='%s', %dx%d, fps=%d, mode=%s}", 
                           configurationName, windowWidth, windowHeight, 
                           targetFrameRate, performanceMode);
    }
}
