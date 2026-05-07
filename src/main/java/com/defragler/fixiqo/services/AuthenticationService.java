package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.user.*;
import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

/**
 * Default implementation of {@link IAuthenticationService} responsible for user authentication.
 *
 * <p>This service verifies user credentials by comparing the provided plain-text password
 * with the stored hashed password using the configured {@link IEncryptionService}.</p>
 *
 * <p><strong>Designed for Dependency Injection (DI):</strong>
 * <ul>
 *     <li>All dependencies are provided via constructor injection</li>
 *     <li>No internal instantiation of dependencies (no "new")</li>
 *     <li>Fully testable and replaceable with mocks or alternative implementations</li>
 * </ul></p>
 *
 * <p><strong>Security features:</strong>
 * <ul>
 *     <li>Uses {@link HashAlgorithm#BCrypt} for password verification</li>
 *     <li>Case-insensitive username comparison (can be adjusted if strict case sensitivity is required)</li>
 *     <li>Returns generic error message to avoid leaking authentication details</li>
 * </ul></p>
 *
 * <p><strong>Performance note:</strong>
 * This implementation loads all users into memory and performs filtering in-memory.
 * For large datasets, consider adding a repository method like {@code findByUsername()}.</p>
 */
public class AuthenticationService implements IAuthenticationService {

    private final IUserRepository userRepository;
    private final IEncryptionService encryptionService;

    /**
     * Constructs {@code AuthenticationService} with required dependencies.
     *
     * <p>This constructor is intended to be used by a DI container.</p>
     *
     * @param userRepository repository for accessing user data
     * @param encryptionService service responsible for hashing and verifying passwords
     */
    public AuthenticationService(IUserRepository userRepository, IEncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Authenticates a user by verifying the provided username and password.
     *
     * <p>Process:
     * <ol>
     *     <li>Loads all users from repository</li>
     *     <li>Filters by case-insensitive username match</li>
     *     <li>Verifies password using BCrypt comparison</li>
     *     <li>Returns the first matching user or throws exception</li>
     * </ol></p>
     *
     * <p><strong>Security:</strong> Uses constant-time comparison via BCrypt to prevent timing attacks.
     * Returns generic error message to avoid leaking information about user existence.</p>
     *
     * @param username the username (case-insensitive)
     * @param password the pain-text password to verify
     * @return authenticated {@link User} entity if credentials are valid
     * @throws ServiceException if username not found or password does not match
     */
    @Override
    public User login(String username, String password) throws ServiceException {

        Optional<User> userOpt = userRepository.findAll().stream()
              .filter(u -> u.getUsername().equalsIgnoreCase(username))
              .findFirst();

        if (userOpt.isEmpty()) {
            throw new ServiceException(ExceptionLevel.ERROR,"Invalid username or password.");
        }

        User user = userOpt.get();

        if (!user.getIsActive() ||
              !encryptionService.verifyData(password, user.getPasswordHash(), HashAlgorithm.BCrypt)) {
            throw new ServiceException(ExceptionLevel.ERROR,"Invalid username or password.");
        }

        return user;
    }
}
