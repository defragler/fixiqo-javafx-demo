package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;

import java.util.*;

/**
 * Interface defining business logic operations related to clients (customers) in the Fixiqo service center system.
 *
 * <p>This service acts as a facade between the UI/controllers and the data access layer (repositories),
 * providing methods for retrieving client information, associated repair requests, and safe deletion
 * with cascading removal of related data.</p>
 *
 * <p>All methods should be considered thread-safe if implemented in a way that supports concurrent access.</p>
 */
public interface IClientsService {
    /**
     * Retrieves all registered clients in the system.
     *
     * @return list of all clients (may be empty, but never {@code null})
     */
    List<Client> getAll();

    /**
     * Finds a client by their unique identifier.
     *
     * @param clientId the unique identifier of the client
     * @return an {@link Optional} containing the client if found, or {@link Optional#empty()} otherwise
     */
    Optional<Client> getById(long clientId);

    /**
     * Retrieves all repair requests submitted by a specific client.
     *
     * @param clientId the unique identifier of the client
     * @return list of requests belonging to the client (empty list if none found or client doesn't exist)
     */
    List<Request> getRequestsByClientId(long clientId);

    /**
     * Creates a new client in the system.
     *
     * <p>This operation validates input data and ensures that the phone number
     * is unique across all clients.</p>
     *
     * @param fullName    full name of the client
     * @param phoneNumber client's phone number (must be unique)
     * @return the created {@link Client} entity
     * @throws ServiceException if validation fails or phone number already exists
     */
    Client create(String fullName, String phoneNumber);

    /**
     * Updates an existing client.
     *
     * <p>This operation modifies client's personal data while preserving
     * its unique identifier.</p>
     *
     * @param clientId    unique identifier of the client
     * @param fullName    updated full name
     * @param phoneNumber updated phone number (must remain unique)
     * @return updated {@link Client} entity
     * @throws ServiceException if client not found or validation fails
     */
    Client update(long clientId, String fullName, String phoneNumber);

    /**
     * Deletes a client without cascading removal of related data.
     *
     * <p><strong>Warning:</strong> This method removes only the client record.
     * Related requests and other entities will NOT be deleted.</p>
     *
     * @param clientId unique identifier of the client
     * @throws ServiceException if client not found
     */
    void delete(long clientId);

    /**
     * Deletes a client and all related data in a cascading manner.
     *
     * <p>This operation removes:
     * <ul>
     *     <li>the client record</li>
     *     <li>all repair requests submitted by this client</li>
     *     <li>all parts associated with those requests</li>
     *     <li>all additional options/services linked to those requests</li>
     * </ul></p>
     *
     * <p><strong>Warning:</strong> This is a destructive operation with no undo. Implementations should
     * ensure transactional consistency and proper logging/auditing if required.</p>
     *
     * @param clientId the unique identifier of the client to delete
     * @throws ServiceException (or specific subclass) if client not found or deletion fails
     */
    void deleteClientCascade(long clientId);
}
