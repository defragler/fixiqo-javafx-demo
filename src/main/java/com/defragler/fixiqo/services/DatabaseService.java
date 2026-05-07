package com.defragler.fixiqo.services;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.io.*;
import java.sql.*;
import java.util.function.*;
import java.util.logging.*;

/**
 * SQLite database service implementation.
 *
 * <p>Provides thread-safe connection creation, transaction management, async support, and logging.</p>
 */
public class DatabaseService implements IDatabaseService {

    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());

    private final String url;

    /**
     * Constructs a database service with a path to SQLite database file.
     *
     * @param dbPath path to SQLite database file (can be relative or absolute)
     */
    public DatabaseService(String dbPath) {
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                LOGGER.warning("Failed to create database directory: " + parentDir.getAbsolutePath());
            }
        }
        this.url = "jdbc:sqlite:" + dbPath;
    }

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create connection to database", e);
            throw new ServiceException(ExceptionLevel.ERROR,"Failed to create connection", e);
        }
    }

    @Override
    public void execute(String sql) {
        runInTransaction(conn -> {
            try (var stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new ServiceException(ExceptionLevel.ERROR,"Failed to execute SQL: " + sql, e);
            }
        });
    }

    @Override
    public void runInTransaction(Consumer<Connection> action) {
        try (Connection connection = getConnection()) {

            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                action.accept(connection);
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Rollback failed", rollbackEx);
                }
                LOGGER.log(Level.SEVERE, "Transaction failed", e);
                throw new ServiceException(ExceptionLevel.ERROR,"Transaction failed", e);
            } finally {
                connection.setAutoCommit(oldAutoCommit);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database access error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T runInTransaction(Function<Connection, T> action) {
        try (Connection connection = getConnection()) {

            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                T result = action.apply(connection);
                connection.commit();
                return result;
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Rollback failed", rollbackEx);
                }
                LOGGER.log(Level.SEVERE, "Transaction failed", e);
                throw new ServiceException(ExceptionLevel.ERROR,"Transaction failed", e);
            } finally {
                connection.setAutoCommit(oldAutoCommit);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database access error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        LOGGER.info("DatabaseService closed");
    }
}
