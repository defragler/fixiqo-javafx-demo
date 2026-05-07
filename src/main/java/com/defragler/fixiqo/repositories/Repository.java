package com.defragler.fixiqo.repositories;

import com.defragler.fixiqo.exceptions.*;
import java.util.*;

/**
 * Generic repository interface defining standard CRUD (Create, Read, Update, Delete) operations
 * for managing entities of type {@code T} with identifier of type {@code ID}.
 *
 * <p>This interface serves as a contract for all concrete repositories in the Fixiqo application
 * (CSV-based, in-memory, future database-backed implementations, etc.).</p>
 *
 * <p>All methods are expected to be thread-safe in implementations where concurrent access is possible.</p>
 *
 * @param <T>  the type of entity this repository manages
 * @param <ID> the type of the entity's unique identifier (typically {@link Long})
 */
public interface Repository<T, ID> {
    
    /**
     * Persists a new entity.
     *
     * @param entity the entity to create and save
     * @throws RepositoryException (or specific subclass) if creation fails (e.g. duplicate ID, I/O error)
     */
    T create(T entity);

    /**
     * Retrieves an entity by its unique identifier.
     *
     * @param id the identifier of the entity to find
     * @return an {@link Optional} containing the entity if found, or {@link Optional#empty()} if not found
     * @throws RepositoryException (or specific subclass) if retrieval fails (e.g. I/O error)
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities managed by this repository.
     *
     * @return a list of all entities (may be empty, but never {@code null})
     * @throws RepositoryException (or specific subclass) if reading fails
     */
    List<T> findAll();

    /**
     * Updates an existing entity.
     *
     * <p>The entity must already exist (identified by its ID). If no matching entity is found,
     * implementations should throw an exception.</p>
     *
     * @param entity the updated entity data
     * @throws RepositoryException (or specific subclass) if entity not found or update fails
     */
    void update(T entity);

    /**
     * Deletes an entity by its identifier.
     *
     * <p>If no entity with the given ID exists, implementations should throw an exception.</p>
     *
     * @param id the identifier of the entity to delete
     * @throws RepositoryException (or specific subclass) if entity not found or deletion fails
     */
    void delete(ID id);
}
