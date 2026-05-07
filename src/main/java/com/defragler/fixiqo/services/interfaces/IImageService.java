package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.exceptions.*;

/**
 * Service responsible for handling image processing operations within the application.
 *
 * <p>This service provides functionality for loading, converting, validating, and
 * preparing images for storage (e.g., in SQLite as BLOB) and for UI display.</p>
 *
 * <p><strong>Main responsibilities:</strong>
 * <ul>
 *     <li>Load image files from the file system</li>
 *     <li>Convert images to byte arrays for database storage</li>
 *     <li>Convert byte arrays back to image formats for rendering</li>
 *     <li>Validate image size and format</li>
 *     <li>Ensure images meet application constraints (e.g., max size)</li>
 * </ul></p>
 *
 * <p><strong>Usage:</strong>
 * Typically used during user registration or profile update when assigning avatar images.</p>
 */
public interface IImageService {

    /**
     * Loads an image from the given file path and converts it into a byte array.
     *
     * <p>This method should:
     * <ul>
     *     <li>Validate that the file exists</li>
     *     <li>Ensure it is a supported image format</li>
     *     <li>Check size constraints</li>
     *     <li>Convert the image into byte[]</li>
     * </ul></p>
     *
     * @param path absolute or relative file path
     * @return image as byte array
     * @throws ServiceException if file is invalid, unsupported, or cannot be read
     */
    byte[] loadImage(String path) throws ServiceException;

    /**
     * Validates raw image byte data.
     *
     * <p>Checks:
     * <ul>
     *     <li>Non-null and non-empty</li>
     *     <li>Maximum size constraints</li>
     * </ul></p>
     *
     * @param data image data
     * @throws ServiceException if validation fails
     */
    void validate(byte[] data) throws ServiceException;

    /**
     * Saves image byte data to the file system.
     *
     * @param data image data
     * @param path target file path
     * @throws ServiceException if saving fails
     */
    void save(byte[] data, String path) throws ServiceException;

    /**
     * Checks whether the provided file path points to a valid image.
     *
     * @param path file path
     * @return true if valid image, false otherwise
     */
    boolean isValidImage(String path);

    /**
     * Returns a default avatar image used when user has not provided one.
     *
     * <p>This method should return a pre-defined image (e.g., bundled resource)
     * that represents a generic user avatar.</p>
     *
     * <p>Typical use cases:</p>
     * <ul>
     *     <li>User registration without avatar</li>
     *     <li>Fallback for corrupted or missing image data</li>
     *     <li>UI placeholder rendering</li>
     * </ul>
     *
     * @return default avatar as byte array (never null)
     * @throws ServiceException if default avatar cannot be loaded
     */
    byte[] getDefaultAvatar();

    /**
     * Attempts to load an image from the provided path,
     * falling back to a default avatar if loading fails.
     *
     * <p>Behavior:</p>
     * <ul>
     *     <li>If path is null or empty → returns default avatar</li>
     *     <li>If image loading fails → returns default avatar</li>
     *     <li>If image is valid → returns loaded image</li>
     * </ul>
     *
     * <p>This method is safe to use in UI flows where avatar is optional.</p>
     *
     * @param path image file path
     * @return image byte array or default avatar if unavailable
     */
    byte[] loadImageOrDefault(String path);
}