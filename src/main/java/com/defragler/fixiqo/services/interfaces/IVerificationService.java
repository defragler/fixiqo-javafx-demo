package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.services.enums.*;

/**
 * Interface defining the contract for user verification operations in the Fixiqo application.
 *
 * <p>This service manages the lifecycle of one-time verification codes sent via different channels
 * (email, SMS/phone) for purposes such as:
 * <ul>
 *     <li>account registration / email confirmation</li>
 *     <li>password reset / recovery</li>
 *     <li>two-factor authentication (2FA)</li>
 *     <li>phone number verification</li>
 * </ul></p>
 *
 * <p>Key features:
 * <ul>
 *     <li>generation and sending of short numeric codes</li>
 *     <li>attempt limiting and rate limiting per target</li>
 *     <li>code verification with expiration</li>
 *     <li>cleanup of verification data after success/failure/cancel</li>
 * </ul></p>
 *
 * <p><strong>Security considerations for implementations:</strong>
 * <ul>
 *     <li>codes should be short-lived (typically 5–15 minutes)</li>
 *     <li>limit attempts (usually 3–5) to prevent brute-force</li>
 *     <li>use constant-time comparison for code verification</li>
 *     <li>never log or expose codes</li>
 *     <li>store codes hashed (not plain text) if persisted</li>
 * </ul></p>
 */
public interface IVerificationService {
    /**
     * Generates a verification code and sends it to the target via the specified channel.
     *
     * <p>Behavior:
     * <ul>
     *     <li>generates new code (length depends on type: usually 6 digits for SMS, 8 for email)</li>
     *     <li>stores code (hashed) with expiration and attempt counter</li>
     *     <li>sends code via email/SMS</li>
     *     <li>resets attempt counter on successful send</li>
     * </ul></p>
     *
     * @param target the destination: email address or phone number
     * @param type   channel/type of verification ({@link VerificationType#EMAIL} or {@link VerificationType#PHONE})
     * @throws ServiceException if sending fails, rate limit exceeded, or target invalid
     */
    void sendCode(String target, VerificationType type);

    /**
     * Verifies the provided code against the stored one for the given target and type.
     *
     * <p>Behavior:
     * <ul>
     *     <li>checks if code matches (constant-time comparison)</li>
     *     <li>decrements remaining attempts on failure</li>
     *     <li>invalidates code on success (calls {@link #clear(String, VerificationType)})</li>
     *     <li>throws exception if attempts exhausted or code expired</li>
     * </ul></p>
     *
     * @param target the email or phone number used for verification
     * @param code   user-entered verification code
     * @param type   verification channel/type
     * @return {@code true} if code is correct and still valid, {@code false} otherwise
     * @throws ServiceException if attempts exhausted, code expired, or other validation error
     */
    boolean verifyCode(String target, String code, VerificationType type);

    /**
     * Requests to resend a new verification code to the same target.
     *
     * <p>Behavior:
     * <ul>
     *     <li>invalidates previous code</li>
     *     <li>generates and sends new code</li>
     *     <li>may enforce cooldown period (e.g. 60 seconds between resends)</li>
     * </ul></p>
     *
     * @param target the email or phone number
     * @param type   verification channel/type
     * @return {@code true} if resend was successful
     * @throws ServiceException if cooldown active, rate limit exceeded, or send fails
     */
    boolean resendCode(String target, VerificationType type);

    /**
     * Returns the number of remaining verification attempts for the current code.
     *
     * <p>Typically used in UI to show "Attempts left: 3/5" or block input after exhaustion.</p>
     *
     * @param target the email or phone number
     * @param type   verification channel/type
     * @return remaining attempts (0 if no active code or attempts exhausted)
     */
    int getAttemptsLeft(String target, VerificationType type);

    /**
     * Invalidates and clears verification data for the specified target and type.
     *
     * <p>Should be called:
     * <ul>
     *     <li>after successful verification</li>
     *     <li>when user cancels verification</li>
     *     <li>after max attempts reached</li>
     * </ul></p>
     *
     * @param target the email or phone number
     * @param type   verification channel/type
     */
    void clear(String target, VerificationType type);
}
