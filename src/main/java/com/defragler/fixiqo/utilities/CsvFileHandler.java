package com.defragler.fixiqo.utilities;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility class for reading from and writing to CSV files in a thread-safe and atomic manner.
 * Supports custom delimiters and ensures the file exists on initialization.
 *
 * <p>All write operations are performed atomically using a temporary file and atomic move.</p>
 *
 * <p>Throws {@link UtilityException} on any I/O related errors.</p>
 */
public final class CsvFileHandler {

    private final Path filePath;
    private final String delimiter;

    /**
     * Creates a new CsvFileHandler with the default comma (",") delimiter.
     *
     * @param filePath path to the CSV file
     */
    public CsvFileHandler(String filePath) {
        this(filePath, ",");
    }

    /**
     * Creates a new CsvFileHandler with a custom delimiter.
     *
     * @param filePath  path to the CSV file
     * @param delimiter character or string used to separate fields
     */
    public CsvFileHandler(String filePath, String delimiter) {
        this.filePath = Paths.get(filePath);
        this.delimiter = delimiter;
        ensureFileExists();
    }

    /* ---------------- public API ---------------- */

    /**
     * Reads all data rows from the CSV file, skipping the header line.
     *
     * <p>Returns an empty list if the file is empty or does not exist (after creation).</p>
     *
     * @return list of string arrays, where each array represents one row of data
     * @throws UtilityException if an I/O error occurs while reading the file
     */
    public List<String[]> readAll() {
        if (isEmpty()) {
            return new ArrayList<>();
        }

        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(line.split(delimiter, -1));
            }
        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to read CSV file", e);
        }

        return rows;
    }

    /**
     * Atomically writes the provided rows along with the header to the CSV file.
     * Uses a temporary file and atomic move to ensure data integrity.
     *
     * <p>File is locked during the entire write operation to prevent concurrent modifications.</p>
     *
     * @param rows   list of data rows to write (each row as String[])
     * @param header array of column names to write as the first line
     * @throws UtilityException if an I/O error occurs during writing or moving files
     */
    public void writeAllAtomic(List<String[]> rows, String[] header) {
        Path tempFile = Paths.get(filePath + ".tmp");

        try (FileChannel channel = FileChannel.open(
              filePath,
              StandardOpenOption.WRITE,
              StandardOpenOption.CREATE
        );
              FileLock lock = channel.lock()) {

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {

                writer.write(String.join(delimiter, header));
                writer.newLine();

                for (String[] row : rows) {
                    writer.write(String.join(delimiter, row));
                    writer.newLine();
                }

            }

            Files.move(
                  tempFile,
                  filePath,
                  StandardCopyOption.REPLACE_EXISTING,
                  StandardCopyOption.ATOMIC_MOVE
            );

        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to write CSV atomically", e);
        }
    }

    /**
     * Checks whether the CSV file is empty or does not exist.
     *
     * @return {@code true} if the file is missing or has zero bytes, {@code false} otherwise
     * @throws UtilityException if an I/O error occurs while checking the file
     */
    public boolean isEmpty() {
        try {
            return !Files.exists(filePath) || Files.size(filePath) == 0;
        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to check CSV file", e);
        }
    }

    /* ---------------- helpers ---------------- */

    /**
     * Ensures that the target CSV file and its parent directories exist.
     * Creates missing directories and the file itself if necessary.
     *
     * @throws UtilityException if directory or file creation fails
     */
    private void ensureFileExists() {
        try {
            if (!Files.exists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to create CSV file", e);
        }
    }
}
