package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.sqlite.part.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;
import java.util.stream.*;

/**
 * Implementation of the {@link IPartsService} interface that provides business logic
 * for managing spare parts and materials used in repair requests in the Fixiqo service center system.
 *
 * <p>This service acts as a facade between the UI/admin screens and the parts repository,
 * handling CRUD operations for {@link Part} entities associated with specific repair requests.</p>
 *
 * <p>Designed for Dependency Injection (DI):
 * <ul>
 *     <li>Uses constructor injection for repository dependency</li>
 *     <li>Does not create repository internally</li>
 *     <li>Easily testable with mocks or alternative implementations</li>
 * </ul></p>
 *
 * <p><strong>Performance note:</strong>
 * The method {@link #getByRequestId(long)} loads **all** parts
 * into memory and filters them. For small datasets this is acceptable, but in production with
 * a large number of parts it is strongly recommended to add a repository method like
 * {@code findByRequestId(long)} for efficient indexed querying.</p>
 */
public class PartsService implements IPartsService {

    private final IPartRepository repository;

    /**
     * Constructs {@code PartsService} with required repository dependency.
     *
     * <p>Intended for use by DI containers.</p>
     *
     * @param repository repository for Part entities
     */
    public PartsService(IPartRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves a specific part by its unique identifier.
     *
     * @param id the unique identifier of the part
     * @return an {@link Optional} containing the part if found, or {@link Optional#empty()} otherwise
     */
    @Override
    public Optional<Part> getById(long id) {
        return repository.findById(id);
    }

    /**
     * Retrieves all spare parts associated with a specific repair request.
     *
     * @param requestId the unique identifier of the repair request
     * @return list of parts linked to the request (empty list if none found)
     */
    @Override
    public List<Part> getByRequestId(long requestId) {
        return repository.findAll().stream()
              .filter(p -> p.getRequestId() == requestId)
              .collect(Collectors.toList());
    }

    /**
     * Adds a new spare part to the system.
     *
     * <p>The implementation should ensure:
     * <ul>
     *     <li>valid requestId exists</li>
     *     <li>non-empty description</li>
     *     <li>non-negative price</li>
     * </ul>
     * These checks are typically performed in the repository or higher layers.</p>
     *
     * @param part the part entity to add (requestId must be set)
     */
    @Override
    public void add(Part part) {
        repository.create(part);
    }

    /**
     * Updates an existing spare part (e.g. change description or price).
     *
     * @param part the updated part entity (must have valid ID)
     */
    @Override
    public void update(Part part) {
        repository.update(part);
    }

    /**
     * Deletes a spare part by its unique identifier.
     *
     * <p>This operation only removes the part record — it does not affect the parent request
     * or other related entities.</p>
     *
     * @param id the unique identifier of the part to delete
     */
    @Override
    public void delete(long id) {
        repository.delete(id);
    }
}
