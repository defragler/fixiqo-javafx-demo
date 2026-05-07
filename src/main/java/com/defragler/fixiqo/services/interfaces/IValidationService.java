package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.exceptions.*;

/**
 * Interface defining validation rules for common user input fields used during registration,
 * profile updates, and request creation in the Fixiqo service center system.
 *
 * <p>This service centralizes input validation logic to ensure:
 * <ul>
 *     <li>consistent rules across all layers (UI, services, API)</li>
 *     <li>proper format and security requirements (length, allowed characters, patterns)</li>
 *     <li>meaningful error messages that can be shown to the user</li>
 * </ul></p>
 *
 * <p>Implementations should throw specific exceptions (e.g. {@link ServiceException})
 * with user-friendly messages when validation fails. This allows callers to handle errors
 * gracefully (display messages in UI, return HTTP 400 responses, etc.).</p>
 *
 * <p>Typical validation rules (recommended, but can be adjusted in implementation):
 * <ul>
 *     <li>Login/username: 4–30 chars, letters, digits, underscores, hyphens</li>
 *     <li>Password: min 8 chars, at least one uppercase, lowercase, digit (possibly special char)</li>
 *     <li>Email: standard RFC 5322 format</li>
 *     <li>Phone: international format or Ukrainian (+380...), 9–13 digits after code</li>
 * </ul></p>
 */
public interface IValidationService {
    /**
     * Validates a login/username string.
     *
     * <p>Checks:
     * <ul>
     *     <li>not null or empty</li>
     *     <li>length within allowed range (e.g. 4–30 characters)</li>
     *     <li>contains only allowed characters (letters, digits, _, -)</li>
     *     <li>does not start/end with special characters (optional)</li>
     * </ul></p>
     *
     * @param login the login/username to validate
     * @throws ServiceException (or specific subclass) with descriptive message if invalid
     */
    void validateLogin(String login);

    /**
     * Validates a password string according to security requirements.
     *
     * <p>Checks:
     * <ul>
     *     <li>not null or empty</li>
     *     <li>minimum length (usually 8–12 characters)</li>
     *     <li>contains mix of uppercase, lowercase, digits (and optionally special characters)</li>
     *     <li>no common/weak patterns (optional advanced check)</li>
     * </ul></p>
     *
     * <p><strong>Note:</strong> This method validates strength/format only.
     * Actual hashing and storage is handled by {@link IEncryptionService}.</p>
     *
     * @param password the plain-text password to validate
     * @throws ServiceException if password does not meet security requirements
     */
    void validatePassword(String password);

    /**
     * Validates an email address string.
     *
     * <p>Checks:
     * <ul>
     *     <li>matches standard email pattern (local-part@domain.tld)</li>
     *     <li>not null or empty</li>
     *     <li>length within reasonable limits</li>
     *     <li>valid domain structure (optional DNS check in advanced impl)</li>
     * </ul></p>
     *
     * @param email the email address to validate
     * @throws ServiceException if email format is invalid
     */
    void validateEmail(String email);

    /**
     * Validates a phone number string (primarily for Ukrainian format).
     *
     * <p>Checks:
     * <ul>
     *     <li>not null or empty</li>
     *     <li>contains only digits (possibly with + prefix)</li>
     *     <li>matches Ukrainian mobile format (+380 XX XXX XX XX) or international E.164</li>
     *     <li>correct length (usually 12 digits with country code)</li>
     * </ul></p>
     *
     * @param phoneNumber the phone number to validate
     * @throws ServiceException if phone number format is invalid
     */
    void validatePhone(String phoneNumber);
}
