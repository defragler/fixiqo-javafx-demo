package com.defragler.fixiqo.services.interfaces;

import java.sql.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Provides low-level database access and transaction management.
 *
 * <p>This service is responsible only for connection lifecycle
 * and transaction boundaries. Business logic must be implemented
 * in repositories.</p>
 */
public interface IDatabaseService extends AutoCloseable {

    /**
     * Opens a new JDBC connection.
     *
     * @return new database connection
     */
    Connection getConnection();

    void execute(String sql);
    
    /**
     * Executes an operation inside a transaction.
     *
     * @param action transactional code
     */
    void runInTransaction(Consumer<Connection> action);

    /**
     * Executes a transactional operation returning a result.
     *
     * @param action transactional code
     * @param <T> result type
     * @return result from transaction
     */
    <T> T runInTransaction(Function<Connection, T> action);

    /**
     * Executes a transactional operation asynchronously.
     *
     * @param action transactional code
     * @return CompletableFuture with result
     */
    default CompletableFuture<Void> runInTransactionAsync(Consumer<Connection> action) {
        return CompletableFuture.runAsync(() -> runInTransaction(action));
    }

    /**
     * Executes a transactional operation returning a result asynchronously.
     *
     * @param action transactional code
     * @param <T> result type
     * @return CompletableFuture with result
     */
    default <T> CompletableFuture<T> runInTransactionAsync(Function<Connection, T> action) {
        return CompletableFuture.supplyAsync(() -> runInTransaction(action));
    }

    /**
     * Closes the database service.
     */
    @Override
    void close() throws SQLException;
}
