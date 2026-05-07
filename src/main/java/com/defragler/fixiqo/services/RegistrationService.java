package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.user.*;
import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

/**
 * Implementation of the {@link IRegistrationService} interface responsible for registering new users
 * (employees or administrators) in the Fixiqo service center system.
 *
 * <p>This service performs full registration flow including:
 * <ul>
 *     <li>Input validation (username, password strength, email format)</li>
 *     <li>Uniqueness checks for username and email</li>
 *     <li>Secure password hashing using BCrypt</li>
 *     <li>UserRole assignment based on numeric ID</li>
 *     <li>Automatic ID generation</li>
 *     <li>Persistence via repository</li>
 * </ul></p>
 *
 * <p>Designed for Dependency Injection (DI):
 * <ul>
 *     <li>All dependencies are injected via constructor</li>
 *     <li>Does not create repositories or services internally</li>
 *     <li>Easily testable with mocks</li>
 * </ul></p>
 *
 * <p><strong>Security features:</strong>
 * <ul>
 *     <li>Uses BCrypt for password hashing (adaptive, salted, secure against brute-force)</li>
 *     <li>Generic error messages to prevent user enumeration attacks</li>
 *     <li>Case-insensitive uniqueness checks for username/email</li>
 * </ul></p>
 *
 * <p><strong>Performance note:</strong> Current implementation loads **all** users for uniqueness checks
 * and ID generation. For scalability add repository methods like {@code findByUsername} and
 * {@code getMaxId()} to avoid full collection loading.</p>
 */
public class RegistrationService implements IRegistrationService {

    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final IImageService imageService;
    private final IValidationService validationService;
    private final IEncryptionService encryptionService;

    /**
     * Constructs RegistrationService with required dependencies.
     *
     * <p>Intended for DI container usage.</p>
     *
     * @param userRepository     repository for user entities
     * @param imageService     repository for user entities
     * @param validationService  service for input validation (login, password, email)
     * @param encryptionService  service for secure password hashing (BCrypt)
     */
    public RegistrationService(IUserRepository userRepository,
          IUserRoleRepository userRoleRepository,
          IImageService imageService,
          IValidationService validationService,
          IEncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.imageService = imageService;
        this.validationService = validationService;
        this.encryptionService = encryptionService;
    }

    /**
     * Registers a new user after performing all necessary validations and security checks.
     *
     * <p>Registration flow:
     * <ol>
     *     <li>Validates username, password and email format via {@link IValidationService}</li>
     *     <li>Checks username and email for uniqueness (case-insensitive)</li>
     *     <li>Hashes the plain-text password using BCrypt</li>
     *     <li>Maps roleId to {@link UserRole} enum value</li>
     *     <li>Generates next available ID</li>
     *     <li>Creates and persists new {@link User} entity</li>
     *     <li>Returns the created user with assigned ID</li>
     * </ol></p>
     *
     * <p><strong>Important:</strong> Throws generic {@link ServiceException} with user-friendly
     * message for all failure cases (to avoid leaking information about existing users).</p>
     *
     * @param username unique login name (will be checked for uniqueness)
     * @param password plain-text password (will be hashed with BCrypt)
     * @param email    user's email address (will be checked for uniqueness)
     * @param roleId   numeric role identifier (1 = Administrator, 2 = Employee)
     * @return created and persisted {@link User} entity with generated ID and hashed password
     * @throws ServiceException if:
     *         <ul>
     *             <li>validation fails (weak password, invalid format, etc.)</li>
     *             <li>username or email is already taken</li>
     *             <li>invalid roleId</li>
     *             <li>persistence error occurs</li>
     *         </ul>
     */
    @Override
    public User register(String username,
          String password,
          String email,
          int roleId,
          String avatarPath) throws ServiceException {

        try {
            validationService.validateLogin(username);
            validationService.validatePassword(password);
            validationService.validateEmail(email);
        } catch (ServiceException e) {
            throw new ServiceException(ExceptionLevel.ERROR,e.getMessage());
        }

        // --- USERNAME UNIQUE ---
        userRepository.findByUsername(username).ifPresent(_ -> {
                  throw new ServiceException(ExceptionLevel.ERROR,"Username already taken");
              });

        // --- EMAIL UNIQUE ---
        userRepository.findByEmail(email).ifPresent(_ -> {
                  throw new ServiceException(ExceptionLevel.ERROR,"Email already taken");
              });

        // --- PASSWORD HASH ---
        String hashedPassword = encryptionService.hashData(password, HashAlgorithm.BCrypt);

        // --- ROLE ---
        userRoleRepository.findById(roleId).orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,"Invalid role")
              );

        // --- AVATAR ---
        byte[] avatar = imageService.loadImageOrDefault(avatarPath);

        User user = new User(
              username,
              hashedPassword,
              email,
              roleId,
              avatar,
              true
        );

        userRepository.create(user);
        return user;
    }
}
