package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.user.*;
import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

/**
 * Implementation of the {@link IUserService} interface that provides
 * business logic for managing user's in the application.
 *
 * <p>This service handles CRUD operations for {@link User} entities with additional validation,
 * uniqueness checks for username/email, and ID generation for new user's.</p>
 *
 * <p>Designed for Dependency Injection (DI):
 * <ul>
 *     <li>All dependencies are injected via constructor</li>
 *     <li>Does not create repositories or services internally</li>
 *     <li>Easily testable with mocks</li>
 * </ul></p>
 * 
 * <p><strong>Security note:</strong>
 * Passwords are passed in plain text to this service.
 * The current implementation stores them unhashed — this is only acceptable for development
 * or testing. In production, passwords **must** be hashed (preferably using BCrypt) before saving.
 * Consider injecting a {@code PasswordEncoder} and hashing in {@link #create} and {@link #update}.</p>
 *
 * <p><strong>Performance note:</strong>
 * Several methods load **all** user's into memory for filtering
 * and ID generation. For small user bases this is fine, but for scalability add indexed lookups
 * (e.g. findByUsername) to the repository.</p>
 */
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IImageService imageService;
    private final IEncryptionService encryptionService;
    private final IValidationService validationService;

    /**
     * Constructs UserService with required dependencies.
     *
     * @param userRepository     repository for user entities
     * @param validationService  service for validating username, password, email formats
     */
    public UserService(IUserRepository userRepository,
          IImageService imageService,
          IEncryptionService encryptionService,
          IValidationService validationService) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.encryptionService = encryptionService;
        this.validationService = validationService;
    }

    /**
     * Returns all registered user's in the system.
     *
     * @return list of all user's (empty list if none exist)
     */
    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by their unique identifier.
     *
     * @param id the unique identifier of the user
     * @return Optional containing the user if found, or empty otherwise
     */
    @Override
    public Optional<User> getById(long id) {
        return userRepository.findById(id);
    }

    /**
     * Creates a new user account after validation and uniqueness checks.
     *
     * <p>Process:
     * <ol>
     *     <li>Validates username, password and email format</li>
     *     <li>Checks username uniqueness</li>
     *     <li>Generates new ID</li>
     *     <li>Creates and persists user</li>
     * </ol></p>
     *
     * @param username unique login name
     * @param password plain-text password
     * @param email    user's email address
     * @param roleId   role to assign
     * @param avatar   avatar image
     * @return created and persisted {@link User} entity
     * @throws ServiceException if validation fails or username is already taken
     */
    @Override
    public User create(String username, String password, String email, int roleId, byte[] avatar) {

        validationService.validateLogin(username);
        validationService.validatePassword(password);
        validationService.validateEmail(email);

        userRepository.findByUsername(username)
              .ifPresent(u -> {
                  throw new ServiceException(ExceptionLevel.ERROR,
                        "User with username '" + username + "' already exists"
                  );
              });

        String hashedPassword =
              encryptionService.hashData(password, HashAlgorithm.BCrypt);

        byte[] finalAvatar = (avatar != null) ? avatar : imageService.getDefaultAvatar();

        User user = new User(
              username,
              hashedPassword,
              email,
              roleId,
              finalAvatar,
              true
        );

        userRepository.create(user);
        return user;
    }

    /**
     * Updates an existing user account.
     *
     * <p>Process:
     * <ol>
     *     <li>Finds existing user by ID</li>
     *     <li>Validates new values</li>
     *     <li>Checks username uniqueness (excluding current user)</li>
     *     <li>Creates updated user object and persists changes</li>
     * </ol></p>
     *
     * @param id       ID of the user to update
     * @param username new or unchanged username
     * @param password new or unchanged password (plain text)
     * @param email    new or unchanged email
     * @param roleId   new or unchanged role
     * @param isActive new or unchanged activity flag
     * @return updated {@link User} entity
     * @throws ServiceException if user not found or username is taken by another user
     */
    @Override
    public User update(long id, String username, String password, String email, Integer roleId, byte[] avatar, Boolean isActive) {

        User existing = userRepository.findById(id)
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,"User with id " + id + " not found")
              );

        // --- USERNAME ---
        if (username != null && !username.isBlank()) {
            validationService.validateLogin(username);
            userRepository.findByUsername(username)
                  .filter(u -> u.getId() != id)
                  .ifPresent(u -> {
                      throw new ServiceException(ExceptionLevel.ERROR,
                            "User with username '" + username + "' already exists"
                      );
                  });

            existing.setUsername(username);
        }

        // --- PASSWORD ---
        if (password != null && !password.isBlank()) {
            validationService.validatePassword(password);
            existing.setPasswordHash(
                  encryptionService.hashData(password, HashAlgorithm.BCrypt)
            );
        }

        // --- EMAIL ---
        if (email != null && !email.isBlank()) {
            validationService.validateEmail(email);
            existing.setEmail(email);
        }

        // --- ROLE ---
        if (roleId != null) {
            existing.setRole(roleId);
        }

        // --- AVATAR ---
        if (avatar != null) {
            existing.setAvatar(avatar);
        } else if (existing.getAvatar() == null) {
            existing.setAvatar(imageService.getDefaultAvatar());
        }

        // --- ACTIVE ---
        if (isActive != null) {
            existing.setIsActive(isActive);
        }

        userRepository.update(existing);
        return existing;
    }

    /**
     * Deletes a user by their unique identifier.
     *
     * <p>No cascading deletion is performed — only the user record is removed.
     * Related business logic (e.g. preventing deletion of last admin) should be
     * handled at a higher level (controller or UI).</p>
     *
     * @param id the unique identifier of the user to delete
     */
    @Override
    public void delete(long id) {
        userRepository.delete(id);
    }

    /**
     * Safe delete a user by their isActivity column.
     *
     * @param id the unique identifier of the user to safe delete
     */
    @Override
    public void deactivate(long id) {
        User user = userRepository.findById(id)
              .orElseThrow(() -> new ServiceException(ExceptionLevel.ERROR,"User not found"));

        if (!user.getIsActive()) {
            return;
        }

        user.setIsActive(false);
        userRepository.update(user);
    }
}
