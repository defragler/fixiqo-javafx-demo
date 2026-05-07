package com.defragler.fixiqo.services;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.io.*;
import java.nio.file.*;
import java.awt.image.*;
import javax.imageio.*;

/**
 * Default implementation of {@link IImageService}.
 *
 * <p>This service provides image processing capabilities including loading,
 * validation, and conversion to/from byte arrays.</p>
 *
 * <p><strong>Design:</strong>
 * <ul>
 *     <li>Stateless service</li>
 *     <li>Safe for reuse and DI</li>
 *     <li>No internal caching</li>
 * </ul></p>
 *
 * <p><strong>Constraints:</strong>
 * <ul>
 *     <li>Maximum image size: 2MB</li>
 *     <li>Supported formats: those recognized by {@link ImageIO}</li>
 * </ul></p>
 */
public class ImageService implements IImageService {

    private static final int MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2MB

    /**
     * Loads an image from disk and converts it into a byte array.
     *
     * @param path file path
     * @return image as byte[]
     * @throws ServiceException if file is invalid or cannot be read
     */
    @Override
    public byte[] loadImage(String path) throws ServiceException {

        if (path == null || path.isBlank()) {
            throw new ServiceException(ExceptionLevel.ERROR, "Image path cannot be empty");
        }

        Path filePath = Paths.get(path);

        if (!Files.exists(filePath)) {
            throw new ServiceException(ExceptionLevel.ERROR, "Image file does not exist");
        }

        if (!isValidImage(path)) {
            throw new ServiceException(ExceptionLevel.ERROR, "Unsupported or corrupted image");
        }

        try {
            byte[] data = Files.readAllBytes(filePath);
            validate(data);
            return data;
        } catch (IOException e) {
            throw new ServiceException(ExceptionLevel.ERROR, "Failed to read image", e);
        }
    }

    /**
     * Validates image byte array.
     *
     * @param data image data
     * @throws ServiceException if validation fails
     */
    @Override
    public void validate(byte[] data) throws ServiceException {

        if (data == null || data.length == 0) {
            throw new ServiceException(ExceptionLevel.ERROR, "Image data is empty");
        }

        if (data.length > MAX_SIZE_BYTES) {
            throw new ServiceException(ExceptionLevel.ERROR, "Image exceeds maximum size (2MB)");
        }
    }

    /**
     * Saves image data to disk.
     *
     * @param data image bytes
     * @param path target path
     * @throws ServiceException if write fails
     */
    @Override
    public void save(byte[] data, String path) throws ServiceException {

        validate(data);

        try {
            Files.write(Paths.get(path), data);
        } catch (IOException e) {
            throw new ServiceException(ExceptionLevel.ERROR, "Failed to save image", e);
        }
    }

    /**
     * Checks whether the file is a valid image using ImageIO.
     *
     * @param path file path
     * @return true if valid image
     */
    @Override
    public boolean isValidImage(String path) {

        try (InputStream is = Files.newInputStream(Paths.get(path))) {
            BufferedImage image = ImageIO.read(is);
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public byte[] getDefaultAvatar() {
        try (InputStream is = getClass().getResourceAsStream("/images/default-avatar.png")) {
            if (is == null) {
                throw new ServiceException(
                      ExceptionLevel.ERROR,
                      "Default avatar resource not found"
                );
            }

            byte[] data = is.readAllBytes();
            validate(data);
            return data;

        } catch (IOException e) {
            throw new ServiceException(
                  ExceptionLevel.ERROR,
                  "Failed to load default avatar",
                  e
            );
        }
    }

    @Override
    public byte[] loadImageOrDefault(String path) {
        if (path == null || path.isBlank()) {
            return getDefaultAvatar();
        }

        try {
            return loadImage(path);
        } catch (ServiceException e) {
            return getDefaultAvatar();
        }
    }
}