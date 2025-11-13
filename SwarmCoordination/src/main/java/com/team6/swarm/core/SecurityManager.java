package com.team6.swarm.core;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Week 8 Implementation: SecurityManager
 *
 * Provides comprehensive security features including encryption, authentication,
 * access control, and security monitoring for the swarm system.
 *
 * Key Features:
 * - AES-256 encryption for secure communication
 * - SHA-256 password hashing
 * - Token-based authentication
 * - Role-based access control (RBAC)
 * - Security audit logging
 * - Brute force protection
 *
 * @author Team 6
 * @version Week 8
 */
public class SecurityManager {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int TOKEN_LENGTH = 32;
    private static final long TOKEN_EXPIRY_MS = 3600000; // 1 hour
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private final Map<String, AgentCredentials> agentCredentials;
    private final Map<String, SecurityToken> activeTokens;
    private final Map<String, Set<Permission>> agentPermissions;
    private final Map<String, Integer> loginAttempts;
    private final List<SecurityAuditLog> auditLogs;
    private final SecretKey masterKey;
    private final SecurityMetrics metrics;

    public SecurityManager() {
        this.agentCredentials = new ConcurrentHashMap<>();
        this.activeTokens = new ConcurrentHashMap<>();
        this.agentPermissions = new ConcurrentHashMap<>();
        this.loginAttempts = new ConcurrentHashMap<>();
        this.auditLogs = Collections.synchronizedList(new ArrayList<>());
        this.masterKey = generateMasterKey();
        this.metrics = new SecurityMetrics();
    }

    /**
     * Registers a new agent with credentials
     */
    public boolean registerAgent(String agentId, String password) {
        if (agentId == null || password == null || password.length() < 6) {
            metrics.recordAuthFailure();
            return false;
        }

        if (agentCredentials.containsKey(agentId)) {
            logAudit(agentId, "REGISTRATION_FAILED", "Agent already exists");
            return false;
        }

        try {
            String passwordHash = hashPassword(password);
            String salt = generateSalt();
            AgentCredentials credentials = new AgentCredentials(agentId, passwordHash, salt);

            agentCredentials.put(agentId, credentials);
            agentPermissions.put(agentId, new HashSet<>(Arrays.asList(Permission.READ, Permission.WRITE)));

            logAudit(agentId, "REGISTRATION_SUCCESS", "Agent registered successfully");
            metrics.recordAgentRegistered();
            return true;
        } catch (Exception e) {
            logAudit(agentId, "REGISTRATION_ERROR", "Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticates an agent and returns a security token
     */
    public String authenticateAgent(String agentId, String password) {
        if (agentId == null || password == null) {
            metrics.recordAuthFailure();
            return null;
        }

        // Check for brute force attempts
        int attempts = loginAttempts.getOrDefault(agentId, 0);
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            logAudit(agentId, "AUTH_BLOCKED", "Too many failed attempts");
            return null;
        }

        AgentCredentials credentials = agentCredentials.get(agentId);
        if (credentials == null) {
            loginAttempts.put(agentId, attempts + 1);
            logAudit(agentId, "AUTH_FAILED", "Agent not found");
            metrics.recordAuthFailure();
            return null;
        }

        try {
            String passwordHash = hashPassword(password);
            if (!passwordHash.equals(credentials.passwordHash)) {
                loginAttempts.put(agentId, attempts + 1);
                logAudit(agentId, "AUTH_FAILED", "Invalid password");
                metrics.recordAuthFailure();
                return null;
            }

            // Successful authentication
            loginAttempts.remove(agentId);
            String token = generateToken();
            SecurityToken securityToken = new SecurityToken(token, agentId,
                System.currentTimeMillis() + TOKEN_EXPIRY_MS);
            activeTokens.put(token, securityToken);

            logAudit(agentId, "AUTH_SUCCESS", "Agent authenticated");
            metrics.recordAuthSuccess();
            return token;

        } catch (Exception e) {
            logAudit(agentId, "AUTH_ERROR", "Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates a security token
     */
    public boolean validateToken(String agentId, String token) {
        SecurityToken securityToken = activeTokens.get(token);
        if (securityToken == null) {
            return false;
        }

        if (System.currentTimeMillis() > securityToken.expiryTime) {
            activeTokens.remove(token);
            logAudit(agentId, "TOKEN_EXPIRED", "Token expired");
            return false;
        }

        return securityToken.agentId.equals(agentId);
    }

    /**
     * Encrypts a message using AES encryption
     */
    public String encryptMessage(String agentId, String message) {
        if (!isAuthenticated(agentId)) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey);
            byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            metrics.recordEncryption();
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logAudit(agentId, "ENCRYPTION_ERROR", "Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decrypts an encrypted message
     */
    public String decryptMessage(String agentId, String encryptedMessage) {
        if (!isAuthenticated(agentId)) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, masterKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedMessage);
            byte[] decrypted = cipher.doFinal(decoded);
            metrics.recordDecryption();
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logAudit(agentId, "DECRYPTION_ERROR", "Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Grants a permission to an agent
     */
    public void grantPermission(String agentId, Permission permission) {
        Set<Permission> permissions = agentPermissions.computeIfAbsent(
            agentId, k -> new HashSet<>());
        permissions.add(permission);
        logAudit(agentId, "PERMISSION_GRANTED", "Permission: " + permission);
    }

    /**
     * Revokes a permission from an agent
     */
    public void revokePermission(String agentId, Permission permission) {
        Set<Permission> permissions = agentPermissions.get(agentId);
        if (permissions != null) {
            permissions.remove(permission);
            logAudit(agentId, "PERMISSION_REVOKED", "Permission: " + permission);
        }
    }

    /**
     * Checks if an agent has a specific permission
     */
    public boolean hasPermission(String agentId, Permission permission) {
        Set<Permission> permissions = agentPermissions.get(agentId);
        return permissions != null && permissions.contains(permission);
    }

    /**
     * Checks if an agent is authenticated
     */
    public boolean isAuthenticated(String agentId) {
        for (SecurityToken token : activeTokens.values()) {
            if (token.agentId.equals(agentId) &&
                System.currentTimeMillis() <= token.expiryTime) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs out an agent by invalidating their tokens
     */
    public void logout(String agentId) {
        activeTokens.entrySet().removeIf(entry -> entry.getValue().agentId.equals(agentId));
        logAudit(agentId, "LOGOUT", "Agent logged out");
    }

    /**
     * Gets security metrics
     */
    public SecurityMetrics getMetrics() {
        return metrics.copy();
    }

    /**
     * Gets recent audit logs
     */
    public List<SecurityAuditLog> getAuditLogs(int limit) {
        synchronized (auditLogs) {
            int start = Math.max(0, auditLogs.size() - limit);
            return new ArrayList<>(auditLogs.subList(start, auditLogs.size()));
        }
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] token = new byte[TOKEN_LENGTH];
        random.nextBytes(token);
        return Base64.getEncoder().encodeToString(token);
    }

    private SecretKey generateMasterKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (Exception e) {
            // Fallback to a fixed key (not recommended for production)
            byte[] keyBytes = new byte[32];
            new SecureRandom().nextBytes(keyBytes);
            return new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
        }
    }

    private void logAudit(String agentId, String action, String details) {
        SecurityAuditLog log = new SecurityAuditLog(
            System.currentTimeMillis(), agentId, action, details);
        synchronized (auditLogs) {
            auditLogs.add(log);
            // Keep only last 1000 logs
            if (auditLogs.size() > 1000) {
                auditLogs.remove(0);
            }
        }
    }

    public enum Permission {
        READ,
        WRITE,
        EXECUTE,
        ADMIN,
        DELETE,
        CONFIGURE
    }

    private static class AgentCredentials {
        final String agentId;
        final String passwordHash;
        final String salt;
        final long createdTime;

        AgentCredentials(String agentId, String passwordHash, String salt) {
            this.agentId = agentId;
            this.passwordHash = passwordHash;
            this.salt = salt;
            this.createdTime = System.currentTimeMillis();
        }
    }

    private static class SecurityToken {
        final String token;
        final String agentId;
        final long expiryTime;

        SecurityToken(String token, String agentId, long expiryTime) {
            this.token = token;
            this.agentId = agentId;
            this.expiryTime = expiryTime;
        }
    }

    public static class SecurityAuditLog {
        public final long timestamp;
        public final String agentId;
        public final String action;
        public final String details;

        SecurityAuditLog(long timestamp, String agentId, String action, String details) {
            this.timestamp = timestamp;
            this.agentId = agentId;
            this.action = action;
            this.details = details;
        }

        @Override
        public String toString() {
            return String.format("[%d] %s - %s: %s", timestamp, agentId, action, details);
        }
    }

    public static class SecurityMetrics {
        private long agentsRegistered = 0;
        private long authSuccesses = 0;
        private long authFailures = 0;
        private long encryptionOperations = 0;
        private long decryptionOperations = 0;

        void recordAgentRegistered() { agentsRegistered++; }
        void recordAuthSuccess() { authSuccesses++; }
        void recordAuthFailure() { authFailures++; }
        void recordEncryption() { encryptionOperations++; }
        void recordDecryption() { decryptionOperations++; }

        public long getAgentsRegistered() { return agentsRegistered; }
        public long getAuthSuccesses() { return authSuccesses; }
        public long getAuthFailures() { return authFailures; }
        public long getEncryptionOperations() { return encryptionOperations; }
        public long getDecryptionOperations() { return decryptionOperations; }

        public double getAuthSuccessRate() {
            long total = authSuccesses + authFailures;
            return total > 0 ? (double) authSuccesses / total : 0.0;
        }

        public SecurityMetrics copy() {
            SecurityMetrics copy = new SecurityMetrics();
            copy.agentsRegistered = this.agentsRegistered;
            copy.authSuccesses = this.authSuccesses;
            copy.authFailures = this.authFailures;
            copy.encryptionOperations = this.encryptionOperations;
            copy.decryptionOperations = this.decryptionOperations;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("SecurityMetrics[Registered: %d, Auth: %d/%d (%.1f%%), Encryption: %d, Decryption: %d]",
                agentsRegistered, authSuccesses, authSuccesses + authFailures,
                getAuthSuccessRate() * 100, encryptionOperations, decryptionOperations);
        }
    }
}
