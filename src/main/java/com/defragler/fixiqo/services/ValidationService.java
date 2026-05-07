package com.defragler.fixiqo.services;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.regex.*;

/**
 * Implementation of the {@link IValidationService} interface that enforces format and security rules
 * for common user input fields (login, password, email, phone) in the Fixiqo application.
 *
 * <p>This service is used during registration, profile updates, request creation and other places
 * where user-provided data needs validation before persistence or processing.</p>
 *
 * <p><strong>Validation rules (as implemented):</strong>
 * <ul>
 *     <li>Login: 3–20 characters, only letters, digits, ., _, -</li>
 *     <li>Password: min 8 chars, at least 1 uppercase, 1 digit, 1 special character</li>
 *     <li>Email: basic format check (local-part@domain.tld)</li>
 *     <li>Phone: 10–15 digits, optional leading '+'</li>
 * </ul></p>
 *
 * <p>All methods throw {@link ServiceException} with user-friendly messages suitable
 * for direct display in the UI. Validation is strict but not overly complex — can be extended
 * if stricter rules are required (e.g. password strength scoring, email domain check).</p>
 */
public class ValidationService implements IValidationService {
    // Login: 3–20 chars, letters, digits, _, -, .
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,20}$");

    // Password: min 8 chars, 1 uppercase, 1 digit, 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+{}\\[\\]:;\"',.<>/?\\\\|~-]).{8,}$");

    // Email (case-insensitive)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", Pattern.CASE_INSENSITIVE);

    // Phone: optional +, 10–15 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{10,15}$");

    /**
     * Validates a login/username string.
     *
     * <p>Rules:
     * <ul>
     *     <li>not null/blank</li>
     *     <li>3–20 characters</li>
     *     <li>only letters, digits, ., _, -</li>
     * </ul></p>
     *
     * @param login the login string to validate
     * @throws ServiceException with descriptive message if invalid
     */
    @Override
    public void validateLogin(String login) {
        checkNotBlank(login, "Login cannot be empty");

        if (!LOGIN_PATTERN.matcher(login).matches()) {
            throw new ServiceException(ExceptionLevel.ERROR,"Login must be 3–20 characters and contain only letters, digits, '.', '_' or '-'");
        }
    }

    /**
     * Validates a password string according to minimum security requirements.
     *
     * <p>Rules:
     * <ul>
     *     <li>not null/blank</li>
     *     <li>at least 8 characters</li>
     *     <li>contains at least one uppercase letter, one digit and one special character</li>
     * </ul></p>
     *
     * @param password the plain-text password to validate
     * @throws ServiceException if password does not meet requirements
     */
    @Override
    public void validatePassword(String password) {
        checkNotBlank(password, "Password cannot be empty");

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ServiceException(ExceptionLevel.ERROR,"Password must be at least 8 characters long and contain one uppercase letter, one digit and one special character");
        }
    }

    /**
     * Validates an email address string.
     *
     * <p>Uses basic pattern check (local-part@domain.tld).
     * Does not verify actual deliverability or MX records.</p>
     *
     * @param email the email address to validate
     * @throws ServiceException if format is invalid
     */
    @Override
    public void validateEmail(String email) {
        checkNotBlank(email, "Email cannot be empty");

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ServiceException(ExceptionLevel.ERROR,"Invalid email format");
        }
    }

    /**
     * Validates a phone number string (primarily for Ukrainian/international format).
     *
     * <p>Rules:
     * <ul>
     *     <li>not null/blank</li>
     *     <li>10–15 digits</li>
     *     <li>optional leading '+'</li>
     * </ul></p>
     *
     * @param phoneNumber the phone number to validate
     * @throws ServiceException if format is invalid
     */
    @Override
    public void validatePhone(String phoneNumber) {
        checkNotBlank(phoneNumber, "Phone number cannot be empty");

        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new ServiceException(ExceptionLevel.ERROR,"Phone number must contain 10–15 digits and may start with '+'");
        }
    }

    /**
     * Helper method to check that a string is not null and not blank.
     *
     * @param value   string to check
     * @param message error message to use in exception
     * @throws ServiceException if value is blank or null
     */
    private void checkNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ServiceException(ExceptionLevel.ERROR,message);
        }
    }
}
