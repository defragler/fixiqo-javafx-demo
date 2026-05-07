package com.defragler.fixiqo.services;

import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

/**
 * In-memory implementation of the {@link IVerificationService} interface that handles generation,
 * sending, verification and lifecycle management of short-lived one-time verification codes.
 *
 * <p>This service is suitable for development, testing or low-traffic production environments.
 * Codes are stored in a {@link HashMap} with automatic expiration and attempt limiting.
 * In production, consider replacing in-memory storage with Redis (with TTL) or database table.</p>
 *
 * <p><strong>Security features:</strong>
 * <ul>
 *     <li>Codes are 6-digit random numbers</li>
 *     <li>5-minute lifetime (configurable)</li>
 *     <li>3 verification attempts per code</li>
 *     <li>60-second cooldown between resends</li>
 *     <li>Automatic cleanup after success/failure/cancel</li>
 * </ul></p>
 *
 * <p><strong>Limitations of current implementation:</strong>
 * <ul>
 *     <li>no persistence → codes lost on restart</li>
 *     <li>no rate limiting across IP/users</li>
 *     <li>debug-only "sending" (prints to console)</li>
 * </ul></p>
 */
public class VerificationService implements IVerificationService {

    // ---------------- INNER CLASS ----------------
    /**
     * Internal data structure holding verification code state for a single target/channel.
     */
    private static class VerificationEntry {
        private String code;
        private long expiresAt;
        private long lastSentAt;
        private int attemptsLeft;

        private VerificationEntry(String code, long expiresAt, long lastSentAt, int attemptsLeft) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.lastSentAt = lastSentAt;
            this.attemptsLeft = attemptsLeft;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }

        private boolean canResend(long now) {
            return now - lastSentAt >= RESEND_COOLDOWN_MS;
        }

        private void reset(String newCode) {
            this.code = newCode;
            this.expiresAt = System.currentTimeMillis() + CODE_LIFETIME_MS;
            this.lastSentAt = System.currentTimeMillis();
            this.attemptsLeft = MAX_ATTEMPTS;
        }
    }

    
    private static final long CODE_LIFETIME_MS = 5 * 60 * 1000; // 5 minutes
    private static final long RESEND_COOLDOWN_MS = 60 * 1000;
    
    private static final int MAX_ATTEMPTS = 3;
    
    private final Map<String, VerificationEntry> storage = new HashMap<>();
    private final ICommunicationService communicationService;

    public VerificationService(ICommunicationService communicationService) {
        this.communicationService = communicationService;
    }
    
    // ================= PUBLIC API =================
    /**
     * Generates a new verification code and sends it to the target via the specified channel.
     * If an active code already exists, a new one is generated only if cooldown period passed.
     *
     * @param target email address or phone number
     * @param type   delivery channel ({@link VerificationType#EMAIL} or {@link VerificationType#PHONE})
     */
    @Override
    public void sendCode(String target, VerificationType type) {
        String key = buildKey(target, type);

        long now = System.currentTimeMillis();
        VerificationEntry existing = storage.get(key);

        if (existing != null && !existing.canResend(now)) {
            return;
        }

        VerificationEntry entry = new VerificationEntry(
              generateCode(),
              now + CODE_LIFETIME_MS,
              now,
              MAX_ATTEMPTS
        );

        storage.put(key, entry);
        dispatch(target, entry.code, type);
    }

    /**
     * Requests to resend a new verification code to the same target.
     * Succeeds only if previous code exists and resend cooldown has passed.
     *
     * @param target email or phone number
     * @param type   verification channel
     * @return {@code true} if new code was generated and sent, {@code false} otherwise
     */
    @Override
    public boolean resendCode(String target, VerificationType type) {
        String key = buildKey(target, type);
        VerificationEntry entry = storage.get(key);

        if (entry == null || !entry.canResend(System.currentTimeMillis())) {
            return false;
        }

        entry.reset(generateCode());
        dispatch(target, entry.code, type);
        return true;
    }

    /**
     * Verifies the user-provided code against the stored one.
     *
     * <p>Behavior:
     * <ul>
     *     <li>code must match exactly</li>
     *     <li>entry must not be expired</li>
     *     <li>attempts left > 0</li>
     *     <li>on success → clears entry</li>
     *     <li>on failure → decrements attempts</li>
     * </ul></p>
     *
     * @param target   email or phone number
     * @param inputCode code entered by user
     * @param type     verification channel
     * @return {@code true} if code is correct and valid, {@code false} otherwise
     */
    @Override
    public boolean verifyCode(String target, String inputCode, VerificationType type) {
        String key = buildKey(target, type);
        VerificationEntry entry = storage.get(key);

        if (entry == null || entry.isExpired() || entry.attemptsLeft <= 0) {
            storage.remove(key);
            return false;
        }

        if (entry.code.equals(inputCode)) {
            storage.remove(key);
            return true;
        }

        entry.attemptsLeft--;
        return false;
    }

    /**
     * Returns the number of remaining verification attempts for the current active code.
     *
     * @param target email or phone
     * @param type   verification type
     * @return remaining attempts (0 if no active code or attempts exhausted)
     */
    @Override
    public int getAttemptsLeft(String target, VerificationType type) {
        VerificationEntry entry = storage.get(buildKey(target, type));
        return entry != null ? entry.attemptsLeft : 0;
    }

    /**
     * Removes verification data for the given target and type.
     *
     * <p>Should be called after successful verification, user cancel, or max attempts reached.</p>
     *
     * @param target email or phone number
     * @param type   verification channel
     */
    @Override
    public void clear(String target, VerificationType type) {
        storage.remove(buildKey(target, type));
    }

    // ================= INTERNAL =================
    /**
     * Generates a 6-digit numeric verification code.
     *
     * @return random 6-digit string
     */
    private String generateCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    /**
     * Generates a 6-digit numeric verification code.
     *
     * @return random 6-digit string
     */
    private String buildKey(String target, VerificationType type) {
        return type.name() + ":" + target;
    }

    /**
     * Dispatches (sends) the verification code via the appropriate channel.
     *
     * <p>Delegates delivery to {@link ICommunicationService}.</p>
     *
     * @param target destination (email/phone)
     * @param code   verification code
     * @param type   EMAIL or PHONE
     */
    private void dispatch(String target, String code, VerificationType type) {
        communicationService.sendVerificationCode(target, code, type);
    }
}
