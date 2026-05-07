package com.defragler.fixiqo.utilities;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

import java.io.*;
import java.util.*;
import java.nio.channels.*;
import java.nio.file.*;

import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;

/**
 * Utility class for reading from and writing to JSON files in a thread-safe and atomic manner.
 *
 * <p>Uses Jackson ObjectMapper for serialization and deserialization.</p>
 *
 * <p>All write operations are performed atomically using a temporary file and atomic move.</p>
 *
 * <p>Throws {@link UtilityException} on any I/O related errors.</p>
 */
public final class JsonFileHandler {

    private final Path filePath;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new JsonFileHandler.
     *
     * @param filePath path to the JSON file
     */
    public JsonFileHandler(String filePath) {
        this.filePath = Paths.get(filePath);
        this.objectMapper = new ObjectMapper();
        ensureFileExists();
    }

    /* ---------------- public API ---------------- */

    /**
     * Reads all data from the JSON file into a list of objects.
     *
     * <p>Returns an empty list if the file is empty.</p>
     *
     * @param typeReference type reference describing the target list type
     * @param <T>           type of objects stored in JSON
     * @return list of deserialized objects
     * @throws UtilityException if an I/O or parsing error occurs
     */
    public <T> List<T> readAll(TypeReference<List<T>> typeReference) {
        if (isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(filePath.toFile(), typeReference);
        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to read JSON file", e);
        }
    }

    /**
     * Atomically writes the provided list of objects to the JSON file.
     *
     * <p>File is locked during the entire write operation to prevent concurrent modifications.</p>
     *
     * @param data list of objects to serialize and write
     * @param <T>  type of objects
     * @throws UtilityException if an I/O error occurs during writing or moving files
     */
    public <T> void writeAllAtomic(List<T> data) {
        Path tempFile = Paths.get(filePath + ".tmp");

        try (FileChannel channel = FileChannel.open(
              filePath,
              StandardOpenOption.WRITE,
              StandardOpenOption.CREATE
        );
              FileLock lock = channel.lock()) {

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, data);
            }

            Files.move(
                  tempFile,
                  filePath,
                  StandardCopyOption.REPLACE_EXISTING,
                  StandardCopyOption.ATOMIC_MOVE
            );

        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to write JSON atomically", e);
        }
    }

    /**
     * Checks whether the JSON file is empty or does not exist.
     *
     * @return {@code true} if the file is missing or has zero bytes, {@code false} otherwise
     * @throws UtilityException if an I/O error occurs while checking the file
     */
    public boolean isEmpty() {
        try {
            return !Files.exists(filePath) || Files.size(filePath) == 0;
        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to check JSON file", e);
        }
    }

    /* ---------------- helpers ---------------- */

    /**
     * Ensures that the target JSON file and its parent directories exist.
     *
     * @throws UtilityException if directory or file creation fails
     */
    private void ensureFileExists() {
        try {
            if (!Files.exists(filePath)) {
                if (filePath.getParent() != null) {
                    Files.createDirectories(filePath.getParent());
                }
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to create JSON file", e);
        }
    }
}