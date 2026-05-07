package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;

import java.util.*;

/**
 * Interface defining business logic operations for managing additional options/services
 * associated with repair requests in the Fixiqo service center system.
 *
 * <p>Options represent extra paid services or modifications performed during repair, such as:
 * "Screen protector installation", "Diagnostic fee", "Express service", "Data backup", etc.</p>
 *
 * <p>This service acts as a facade between the UI layer and the underlying repository,
 * providing CRUD operations specifically scoped to options linked to a particular request.</p>
 *
 * <p>All modification methods ({@link #add}, {@link #update}, {@link #delete}) should be
 * considered transactional where applicable and should perform necessary validation.</p>
 */
public interface IOptionsService {
    /**
     * Finds a specific option/service by its unique identifier.
     *
     * @param id the unique identifier of the option
     * @return an {@link Optional} containing the option if found, or {@link Optional#empty()} otherwise
     */
    Optional<Option> getById(long id);

    /**
     * Retrieves all additional options/services associated with a specific repair request.
     *
     * @param requestId the unique identifier of the repair request
     * @return list of options linked to the request (empty list if none found)
     */
    List<Option> getByRequestId(long requestId);

    /**
     * Adds a new option/service to a repair request.
     *
     * <p>The implementation should:
     * <ul>
     *     <li>validate the option data (non-empty description, non-negative price, valid requestId)</li>
     *     <li>ensure the referenced request exists</li>
     *     <li>generate or assign ID if needed</li>
     *     <li>persist the new option</li>
     * </ul></p>
     *
     * @param option the option entity to add (requestId must be set)
     * @throws ServiceException (or specific subclass) if validation fails or request not found
     */
    void add(Option option);

    /**
     * Updates an existing option/service.
     *
     * <p>Typically used to change description or price after initial creation
     * (e.g. when actual cost differs from estimate).</p>
     *
     * @param option the updated option entity (must have valid ID)
     * @throws ServiceException (or specific subclass) if option not found or validation fails
     */
    void update(Option option);

    /**
     * Deletes an option/service by its unique identifier.
     *
     * <p>This operation only removes the option record — it does not affect the parent request.</p>
     *
     * @param id the unique identifier of the option to delete
     * @throws ServiceException (or specific subclass) if option not found
     */
    void delete(long id);
}
