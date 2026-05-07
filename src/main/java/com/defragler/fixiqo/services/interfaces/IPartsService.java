package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;

import java.util.*;

/**
 * Interface defining business logic operations for managing spare parts and materials
 * used during repair requests in the Fixiqo service center system.
 *
 * <p>Parts represent physical components or consumables replaced/installed during repair,
 * such as "LCD screen", "Battery", "Charging port", "Back cover", "Screws set", etc.</p>
 *
 * <p>This service serves as a high-level facade between the UI/admin layer and the underlying
 * parts repository, providing CRUD operations scoped to parts associated with specific repair requests.</p>
 *
 * <p>All modification methods ({@link #add}, {@link #update}, {@link #delete}) should perform
 * necessary validation (valid request reference, non-negative price, non-empty description, etc.)
 * and may be transactional in concrete implementations.</p>
 */
public interface IPartsService {
    /**
     * Retrieves a specific spare part by its unique identifier.
     *
     * @param id the unique identifier of the part
     * @return an {@link Optional} containing the part if found,
     *         or {@link Optional#empty()} otherwise
     */
    Optional<Part> getById(long id);

    /**
     * Retrieves a specific spare part by its unique identifier.
     *
     * @param id the unique identifier of the part
     * @return an {@link Optional} containing the part if found,
     *         or {@link Optional#empty()} otherwise
     */
    List<Part> getByRequestId(long requestId);

    /**
     * Adds a new spare part to a repair request.
     *
     * <p>The implementation should ensure (typically in service or repository layer):
     * <ul>
     *     <li>the referenced request exists</li>
     *     <li>description is not empty</li>
     *     <li>price is non-negative</li>
     *     <li>ID is generated if necessary</li>
     * </ul></p>
     *
     * @param part the part entity to add (requestId must be set)
     */
    void add(Part part);

    /**
     * Updates an existing spare part (e.g. change description or price after correction).
     *
     * <p>The part must already exist (identified by its ID).</p>
     *
     * @param part the updated part entity (must have valid ID)
     */
    void update(Part part);

    /**
     * Deletes a spare part by its unique identifier.
     *
     * <p>This operation only removes the part record — it does not affect
     * the parent request or other related entities (options, client, etc.).</p>
     *
     * @param id the unique identifier of the part to delete
     */
    void delete(long id);
}
