package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.sqlite.option.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;
import java.util.stream.*;

/**
 * Implementation of the {@link IOptionsService} interface that provides business logic
 * for managing additional options/services associated with repair requests in the Fixiqo system.
 *
 * <p>Options represent extra paid services, modifications or fees linked to a specific repair request
 * (e.g. "Protective glass installation", "Express repair", "Diagnostic fee", "Data recovery attempt").</p>
 *
 * <p>This service acts as a thin facade between the UI/admin layer and the options repository,
 * delegating CRUD operations to the underlying storage while keeping the business layer clean.</p>
 *
 * <p>Designed for Dependency Injection (DI):
 * <ul>
 *     <li>Uses constructor injection for the repository dependency</li>
 *     <li>Does not instantiate repository internally</li>
 *     <li>Can be easily mocked in tests or replaced by another implementation</li>
 * </ul></p>
 *
 * <p><strong>Performance note:</strong>
 * The method {@link #getByRequestId(long)} currently loads
 * **all** options into memory and filters them. For small datasets this is acceptable,
 * but in production with many options it is strongly recommended to add a repository method
 * like {@code findByRequestId(long)} for efficient indexed querying.</p>
 */
public class OptionsService implements IOptionsService {

    private final IOptionRepository repository;

    /**
     * Constructs {@code OptionsService} with required repository.
     *
     * <p>Intended for use by DI containers.</p>
     *
     * @param repository repository for Option entities
     */
    public OptionsService(IOptionRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves a specific option/service by its unique identifier.
     *
     * @param id the unique identifier of the option
     * @return an {@link Optional} containing the option if found,
     *         or {@link Optional#empty()} otherwise
     */
    @Override
    public Optional<Option> getById(long id) {
        return repository.findById(id);
    }

    /**
     * Retrieves all additional options/services associated with a specific repair request.
     *
     * @param requestId the unique identifier of the repair request
     * @return list of options linked to the request (empty list if none found)
     */
    @Override
    public List<Option> getByRequestId(long requestId) {
        return repository.findAll().stream()
              .filter(o -> o.getRequestId() == requestId)
              .collect(Collectors.toList());
    }

    /**
     * Adds a new option/service to a repair request.
     *
     * <p>The implementation should ensure (typically in repository or higher layers):
     * <ul>
     *     <li>valid requestId exists</li>
     *     <li>non-empty description</li>
     *     <li>non-negative price</li>
     * </ul></p>
     *
     * @param option the option entity to add (requestId must be set)
     */
    @Override
    public void add(Option option) {
        repository.create(option);
    }

    /**
     * Updates an existing option/service (e.g. change description or price).
     *
     * @param option the updated option entity (must have valid ID)
     */
    @Override
    public void update(Option option) {
        repository.update(option);
    }

    /**
     * Deletes an option/service by its unique identifier.
     *
     * <p>This operation only removes the option record — it does not affect
     * the parent request or other related entities.</p>
     *
     * @param id the unique identifier of the option to delete
     */
    @Override
    public void delete(long id) {
        repository.delete(id);
    }
}
