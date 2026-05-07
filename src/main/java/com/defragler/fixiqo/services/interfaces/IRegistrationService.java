package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.exceptions.*;

/**
 * Interface defining the contract for user registration operations in the Fixiqo service center system.
 *
 * <p>This service is responsible for securely creating new user accounts (employees or administrators)
 * with proper validation, uniqueness checks, password hashing, and role assignment.</p>
 *
 * <p><strong>Key responsibilities:</strong>
 * <ul>
 *     <li>Validate input data (username, password strength, email format)</li>
 *     <li>Ensure username and email are unique across the system</li>
 *     <li>Securely hash the plain-text password before storage (MUST use BCrypt or stronger)</li>
 *     <li>Assign the correct {@link UserRole} based on provided role ID</li>
 *     <li>Persist the new user and return the fully populated entity with generated ID</li>
 * </ul></p>
 *
 * <p><strong>Security requirements for implementations:</strong>
 * <ul>
 *     <li>Never store plain-text passwords</li>
 *     <li>Use strong, adaptive hashing algorithm (BCrypt preferred)</li>
 *     <li>Throw meaningful {@link ServiceException} with user-friendly messages</li>
 *     <li>Prevent enumeration attacks (same error message for duplicate username/email)</li>
 * </ul></p>
 */
public interface IRegistrationService {

    /**
     * Registers a new user in the system.
     *
     * <p>Full registration flow performed by implementations:
     * <ol>
     *     <li>Validate username, password and email format</li>
     *     <li>Check that username and email are not already taken</li>
     *     <li>Hash the plain-text password using secure algorithm</li>
     *     <li>Map roleId to {@link UserRole} enum value</li>
     *     <li>Create and persist the new {@link User} entity</li>
     *     <li>Return the created user with assigned database ID</li>
     * </ol></p>
     *
     * @param username unique login name (case-insensitive uniqueness recommended)
     * @param password plain-text password provided by the user (will be hashed)
     * @param email    user's email address (must be unique)
     * @param roleId   numeric identifier of the role (1 = Administrator, 2 = Employee)
     * @return fully populated {@link User} entity with generated ID and hashed password
     * @throws ServiceException if:
     *         <ul>
     *             <li>username or email is already in use</li>
     *             <li>roleId is invalid</li>
     *             <li>validation fails (weak password, invalid email format, etc.)</li>
     *             <li>persistence error occurs</li>
     *         </ul>
     */
    User register(String username, String password, String email, int roleId, String avatarPath) throws ServiceException;
}
