package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;

import java.util.*;

/**
 * Interface defining business logic operations for managing user's
 *
 * <p>This service serves as a high-level facade between the UI/admin screens and the underlying user repository.
 * It provides CRUD operations specifically tailored for user management, including secure creation and update
 * of user accounts with password handling and role assignment.</p>
 *
 * <p>All methods that modify data (create, update, delete) should be considered transactional where applicable
 * and should perform appropriate validation (uniqueness of username/email, password strength, etc.).</p>
 *
 * <p><strong>Security note:</strong> Passwords passed to {@link #create} and {@link #update} are expected to be
 * plain text. Implementations must hash them before storage using a secure algorithm (preferably BCrypt).</p>
 */
public interface IUserService {
    /**
     * Retrieves all user's registered in the system.
     *
     * <p>May include both
     * depending on business requirements.</p>
     *
     * @return list of all user's entities (empty list if none exist)
     */
    List<User> getAll();

    /**
     * Finds an user's by their unique identifier.
     *
     * @param id the unique identifier of the user
     * @return an {@link Optional} containing the user if found, or {@link Optional#empty()} otherwise
     */
    Optional<User> getById(long id);

    /**
     * Creates a new user account.
     *
     * <p>The implementation should:
     * <ul>
     *     <li>validate username and email uniqueness</li>
     *     <li>hash the provided plain-text password</li>
     *     <li>assign the specified role</li>
     *     <li>persist the new user</li>
     * </ul></p>
     *
     * @param username unique login name for the new user
     * @param password plain-text password (will be hashed before storage)
     * @param email    user's email address
     * @param roleId   role to assign
     * @return the created and persisted {@link User} entity with generated ID
     * @throws ServiceException (or specific subclass) if validation fails (duplicate username/email, weak password, etc.)
     */
    User create(String username,
          String password,
          String email,
          int roleId,
          byte[] avatar);

    /**
     * Updates an existing user account.
     *
     * <p>Behavior for fields:
     * <ul>
     *     <li>{@code username} and {@code email} — should be checked for uniqueness (excluding current user)</li>
     *     <li>{@code password} — if provided (non-empty), should be hashed and updated; if empty/null, password remains unchanged</li>
     *     <li>{@code role} — can be changed</li>
     * </ul></p>
     *
     * @param id       unique identifier of the user to update
     * @param username new or unchanged username
     * @param password new plain-text password (or empty/null to keep current)
     * @param email    new or unchanged email address
     * @param roleId   new or unchanged role
     * @return the updated {@link User} entity
     * @throws ServiceException (or specific subclass) if user not found or validation fails
     */
    User update(long id,
          String username,
          String password,
          String email,
          Integer roleId,
          byte[] avatar,
          Boolean isActive);

    /**
     * Deletes an user by their unique identifier.
     *
     * <p><strong>Warning:</strong> This operation is destructive and irreversible.
     * Depending on business rules, implementations may need to:
     * <ul>
     *     <li>check if user has active requests/assignments</li>
     *     <li>prevent deletion of the last administrator</li>
     *     <li>log the deletion event</li>
     * </ul></p>
     *
     * @param id the unique identifier of the user to delete
     * @throws ServiceException (or specific subclass) if user not found or deletion is not allowed
     */
    void delete(long id);

    /**
     * Safe delete a user by their isActivity column.
     *
     * @param id the unique identifier of the user to safe delete
     * @throws ServiceException (or specific subclass) if user not found or deletion is not allowed
     */
    void deactivate(long id);
}
