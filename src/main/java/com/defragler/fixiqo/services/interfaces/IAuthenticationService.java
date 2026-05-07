package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;

/**
 * Interface defining the contract for user authentication operations in the application.
 *
 * <p>This service is responsible for verifying user credentials and returning an authenticated
 * {@link User} entity upon successful login. It serves as the primary entry point for
 * authentication flows in the system (login screen, API endpoints, etc.).</p>
 *
 * <p><strong>Security note:</strong> Implementations should <strong>never</strong> store or return
 * passwords in plain text after verification. Password comparison must be performed using
 * secure hashing (preferably BCrypt, Argon2, or PBKDF2). The {@code password} parameter is
 * expected to be plain text only at the moment of login attempt.</p>
 *
 * <p>Typical implementation flow:
 * <ol>
 *     <li>Find user by username</li>
 *     <li>Verify password hash match</li>
 *     <li>Optionally check account status (locked, expired, etc.)</li>
 *     <li>Return fully loaded {@link User} entity or throw exception</li>
 * </ol></p>
 */
public interface IAuthenticationService {
    /**
     * Authenticates a user based on provided username and password.
     *
     * <p>On success, returns a fully populated {@link User} object containing user details
     * and role information necessary for authorization and session creation.</p>
     *
     * <p>On failure (wrong credentials, non-existent user, locked account, etc.),
     * throws {@link ServiceException} with a user-friendly message that can be
     * displayed in the UI.</p>
     *
     * <p><strong>Important:</strong> This method should not distinguish between
     * "user not found" and "wrong password" for security reasons — use the same generic
     * error message in both cases (e.g. "Invalid username or password").</p>
     *
     * @param username the username (login) provided by the user
     * @param password the plain-text password provided by the user
     * @return authenticated {@link User} entity if credentials are valid
     * @throws ServiceException if authentication fails for any reason
     *         (invalid credentials, account locked, internal error, etc.)
     */
    User login(String username, String password) throws ServiceException;
}
