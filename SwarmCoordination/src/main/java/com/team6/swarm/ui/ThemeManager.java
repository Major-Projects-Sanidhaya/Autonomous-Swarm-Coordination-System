package com.team6.swarm.ui;

import javafx.scene.paint.Color;

/**
 * Week 7-8: Visual theme and color schemes
 * Purpose: Customizable appearance
 * Author: Anthony (UI Team)
 */
public class ThemeManager {
    
    public enum Theme {
        LIGHT,
        DARK,
        HIGH_CONTRAST,
        CUSTOM
    }
    
    private Theme currentTheme = Theme.LIGHT;
    private ThemeColors colors;
    
    public ThemeManager() {
        applyTheme(Theme.LIGHT);
    }
    
    /**
     * Apply a theme
     */
    public void applyTheme(Theme theme) {
        this.currentTheme = theme;
        
        switch (theme) {
            case LIGHT -> colors = createLightTheme();
            case DARK -> colors = createDarkTheme();
            case HIGH_CONTRAST -> colors = createHighContrastTheme();
            case CUSTOM -> colors = createCustomTheme();
        }
    }
    
    private ThemeColors createLightTheme() {
        ThemeColors theme = new ThemeColors();
        theme.backgroundColor = Color.WHITE;
        theme.gridColor = Color.LIGHTGRAY;
        theme.textColor = Color.BLACK;
        theme.agentActiveColor = Color.GREEN;
        theme.agentBatteryLowColor = Color.YELLOW;
        theme.agentFailedColor = Color.RED;
        theme.agentInactiveColor = Color.GRAY;
        theme.linkColor = Color.BLUE;
        theme.selectionColor = Color.GOLD;
        return theme;
    }
    
    private ThemeColors createDarkTheme() {
        ThemeColors theme = new ThemeColors();
        theme.backgroundColor = Color.rgb(30, 30, 30);
        theme.gridColor = Color.rgb(60, 60, 60);
        theme.textColor = Color.WHITE;
        theme.agentActiveColor = Color.LIME;
        theme.agentBatteryLowColor = Color.YELLOW;
        theme.agentFailedColor = Color.rgb(255, 100, 100);
        theme.agentInactiveColor = Color.DARKGRAY;
        theme.linkColor = Color.CYAN;
        theme.selectionColor = Color.ORANGE;
        return theme;
    }
    
    private ThemeColors createHighContrastTheme() {
        ThemeColors theme = new ThemeColors();
        theme.backgroundColor = Color.BLACK;
        theme.gridColor = Color.WHITE;
        theme.textColor = Color.WHITE;
        theme.agentActiveColor = Color.LIME;
        theme.agentBatteryLowColor = Color.YELLOW;
        theme.agentFailedColor = Color.RED;
        theme.agentInactiveColor = Color.WHITE;
        theme.linkColor = Color.CYAN;
        theme.selectionColor = Color.MAGENTA;
        return theme;
    }
    
    private ThemeColors createCustomTheme() {
        return createLightTheme(); // Default to light
    }
    
    public ThemeColors getColors() {
        return colors;
    }
    
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Theme color configuration
     */
    public static class ThemeColors {
        public Color backgroundColor;
        public Color gridColor;
        public Color textColor;
        public Color agentActiveColor;
        public Color agentBatteryLowColor;
        public Color agentFailedColor;
        public Color agentInactiveColor;
        public Color linkColor;
        public Color selectionColor;
    }
}
