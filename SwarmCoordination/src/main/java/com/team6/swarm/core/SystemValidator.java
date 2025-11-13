package com.team6.swarm.core;

import java.util.*;

/**
 * Week 8 Implementation: SystemValidator
 *
 * System consistency validation and performance baseline verification
 * with comprehensive validation rules and health checks.
 *
 * Key Features:
 * - System consistency validation
 * - Performance baseline verification
 * - Configuration validation
 * - Dependency checking
 * - Integration validation
 *
 * @author Team 6
 * @version Week 8
 */
public class SystemValidator {

    private final List<ValidationRule> validationRules;
    private ValidationMetrics metrics;

    public SystemValidator() {
        this.validationRules = new ArrayList<>();
        this.metrics = new ValidationMetrics();
        initializeDefaultRules();
    }

    private void initializeDefaultRules() {
        // Add default validation rules
        validationRules.add(new ValidationRule("ConfigurationCheck",
            "Verify system configuration is valid",
            ValidationSeverity.CRITICAL));

        validationRules.add(new ValidationRule("PerformanceBaseline",
            "Check performance meets baseline requirements",
            ValidationSeverity.HIGH));

        validationRules.add(new ValidationRule("MemoryCheck",
            "Verify sufficient memory available",
            ValidationSeverity.HIGH));

        validationRules.add(new ValidationRule("ThreadPoolCheck",
            "Verify thread pool configuration",
            ValidationSeverity.MEDIUM));
    }

    public ValidationResult validateSystem() {
        return validateSystem(null);
    }

    public ValidationResult validateSystem(SystemConfiguration config) {
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean valid = true;

        // Validate configuration
        if (config != null) {
            ValidationResult configResult = validateConfiguration(config);
            errors.addAll(configResult.errors);
            warnings.addAll(configResult.warnings);
            if (!configResult.valid) {
                valid = false;
            }
        }

        // Validate memory
        ValidationResult memoryResult = validateMemory();
        errors.addAll(memoryResult.errors);
        warnings.addAll(memoryResult.warnings);
        if (!memoryResult.valid) {
            valid = false;
        }

        // Validate thread pool
        ValidationResult threadResult = validateThreadPool();
        errors.addAll(threadResult.errors);
        warnings.addAll(threadResult.warnings);
        if (!threadResult.valid) {
            valid = false;
        }

        // Validate performance baseline
        ValidationResult perfResult = validatePerformanceBaseline();
        warnings.addAll(perfResult.warnings);

        long duration = System.currentTimeMillis() - startTime;
        metrics.recordValidation(valid, duration);

        return new ValidationResult(valid, errors, warnings, duration);
    }

    public ValidationResult validateConfiguration(SystemConfiguration config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            // Check required parameters
            if (!config.hasParameter("maxAgents")) {
                errors.add("Missing required parameter: maxAgents");
            } else {
                int maxAgents = config.getInt("maxAgents");
                if (maxAgents <= 0) {
                    errors.add("maxAgents must be positive");
                } else if (maxAgents > 10000) {
                    warnings.add("maxAgents is very high (" + maxAgents + "), may cause performance issues");
                }
            }

            if (!config.hasParameter("updateInterval")) {
                errors.add("Missing required parameter: updateInterval");
            } else {
                int updateInterval = config.getInt("updateInterval");
                if (updateInterval < 1) {
                    errors.add("updateInterval must be at least 1ms");
                } else if (updateInterval > 1000) {
                    warnings.add("updateInterval is high (" + updateInterval + "ms), may cause lag");
                }
            }

            if (!config.hasParameter("worldWidth") || !config.hasParameter("worldHeight")) {
                errors.add("Missing world dimensions");
            } else {
                double width = config.getDouble("worldWidth");
                double height = config.getDouble("worldHeight");
                if (width <= 0 || height <= 0) {
                    errors.add("World dimensions must be positive");
                }
            }

        } catch (Exception e) {
            errors.add("Configuration validation error: " + e.getMessage());
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    public ValidationResult validateMemory() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double memoryUsageRatio = (double) usedMemory / maxMemory;

        if (maxMemory < 100 * 1024 * 1024) { // Less than 100MB
            errors.add("Insufficient max memory: " + (maxMemory / (1024 * 1024)) + "MB");
        }

        if (memoryUsageRatio > 0.95) {
            errors.add("Memory usage critical: " + String.format("%.1f%%", memoryUsageRatio * 100));
        } else if (memoryUsageRatio > 0.85) {
            warnings.add("Memory usage high: " + String.format("%.1f%%", memoryUsageRatio * 100));
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    public ValidationResult validateThreadPool() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        if (availableProcessors < 2) {
            warnings.add("Only " + availableProcessors + " processor(s) available");
        }

        Thread.activeCount();
        int threadCount = Thread.activeCount();

        if (threadCount > availableProcessors * 10) {
            warnings.add("High thread count: " + threadCount + " threads active");
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    public ValidationResult validatePerformanceBaseline() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check if system can handle minimum performance requirements
        long startTime = System.nanoTime();
        int iterations = 100000;
        double sum = 0;

        for (int i = 0; i < iterations; i++) {
            sum += Math.sqrt(i);
        }

        long duration = (System.nanoTime() - startTime) / 1000000; // Convert to ms

        if (duration > 100) {
            warnings.add("Performance baseline test took " + duration + "ms (expected < 100ms)");
        }

        return new ValidationResult(true, errors, warnings);
    }

    public void addValidationRule(ValidationRule rule) {
        validationRules.add(rule);
    }

    public void removeValidationRule(String ruleName) {
        validationRules.removeIf(rule -> rule.name.equals(ruleName));
    }

    public List<ValidationRule> getValidationRules() {
        return new ArrayList<>(validationRules);
    }

    public ValidationMetrics getMetrics() {
        return metrics.copy();
    }

    public enum ValidationSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public static class ValidationRule {
        public final String name;
        public final String description;
        public final ValidationSeverity severity;

        public ValidationRule(String name, String description, ValidationSeverity severity) {
            this.name = name;
            this.description = description;
            this.severity = severity;
        }
    }

    public static class ValidationResult {
        public final boolean valid;
        public final List<String> errors;
        public final List<String> warnings;
        public final long durationMs;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this(valid, errors, warnings, 0);
        }

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings, long durationMs) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
            this.durationMs = durationMs;
        }

        @Override
        public String toString() {
            return String.format("ValidationResult[valid=%s, errors=%d, warnings=%d, duration=%dms]",
                valid, errors.size(), warnings.size(), durationMs);
        }
    }

    public static class ValidationMetrics {
        private long totalValidations = 0;
        private long successfulValidations = 0;
        private long failedValidations = 0;
        private long totalDurationMs = 0;

        void recordValidation(boolean success, long durationMs) {
            totalValidations++;
            if (success) {
                successfulValidations++;
            } else {
                failedValidations++;
            }
            totalDurationMs += durationMs;
        }

        public long getTotalValidations() { return totalValidations; }
        public long getSuccessfulValidations() { return successfulValidations; }
        public long getFailedValidations() { return failedValidations; }
        public double getAverageDurationMs() {
            return totalValidations > 0 ? (double) totalDurationMs / totalValidations : 0.0;
        }

        public ValidationMetrics copy() {
            ValidationMetrics copy = new ValidationMetrics();
            copy.totalValidations = this.totalValidations;
            copy.successfulValidations = this.successfulValidations;
            copy.failedValidations = this.failedValidations;
            copy.totalDurationMs = this.totalDurationMs;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("ValidationMetrics[Total: %d, Success: %d, Failed: %d, Avg: %.2fms]",
                totalValidations, successfulValidations, failedValidations, getAverageDurationMs());
        }
    }
}
