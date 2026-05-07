package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.exceptions.*;

import java.util.*;

/**
 * Interface defining business logic operations for managing repair requests (заявки на ремонт)
 * in the Fixiqo service center system.
 *
 * <p>This service acts as the central facade for all operations related to repair requests,
 * including CRUD, status transitions, and convenient creation with automatic client handling.</p>
 *
 * <p>Key responsibilities:
 * <ul>
 *     <li>Listing and retrieving requests</li>
 *     <li>Creating new requests (with or without pre-existing client)</li>
 *     <li>Updating request details (device info, description, etc.)</li>
 *     <li>Changing request status (ACCEPTED → IN_PROGRESS → COMPLETED → ISSUED, etc.)</li>
 *     <li>Safe deletion of requests</li>
 * </ul></p>
 *
 * <p>All modification methods should perform necessary validation (valid client, device type,
 * non-empty description, correct status transitions) and may be transactional in implementations.</p>
 */
public interface IRequestsService {
    /**
     * Retrieves all repair requests in the system.
     *
     * @return list of all requests (empty list if none exist)
     */
    List<Request> getAll();

    /**
     * Finds a specific repair request by its unique identifier.
     *
     * @param id the unique identifier of the request
     * @return an {@link Optional} containing the request if found,
     *         or {@link Optional#empty()} otherwise
     */
    Optional<Request> getById(long id);

    /**
     * Retrieves all repair requests associated with a specific client.
     *
     * <p>This method provides an optimized way to fetch requests
     * without loading all records into memory.</p>
     *
     * @param clientId unique identifier of the client
     * @return list of requests belonging to the client (empty list if none exist)
     */
    List<Request> getByClientId(long clientId);

    /**
     * Creates and persists a new repair request.
     *
     * <p>The implementation should:
     * <ul>
     *     <li>validate required fields (clientId, deviceType, etc.)</li>
     *     <li>set creation timestamp ({@code dateReceived})</li>
     *     <li>set initial status (usually ACCEPTED)</li>
     *     <li>generate ID if necessary</li>
     * </ul></p>
     *
     * @param request the request entity to create (clientId must be set)
     * @return the persisted request with assigned ID
     */
    Request create(Request request);

    /**
     * Updates an existing repair request.
     *
     * <p>Typically used to modify device details, problem description or other non-status fields.
     * Status changes should go through {@link #changeStatus(long, int)}.</p>
     *
     * @param request the updated request entity (must have valid ID)
     */
    void update(Request request);

    /**
     * Deletes a repair request by its unique identifier.
     *
     * <p><strong>Warning:</strong> Depending on business rules, implementations may need to:
     * <ul>
     *     <li>perform cascading deletion of related parts/options</li>
     *     <li>check if request is in terminal state (COMPLETED/ISSUED)</li>
     *     <li>log the deletion event</li>
     * </ul></p>
     *
     * @param id the unique identifier of the request to delete
     */
    void delete(long id);

    /**
     * Changes the status of an existing repair request.
     *
     * <p>The implementation should:
     * <ul>
     *     <li>validate allowed status transitions (business workflow rules)</li>
     *     <li>update {@code dateIssued} when transitioning to ISSUED</li>
     *     <li>possibly trigger notifications or other side effects</li>
     * </ul></p>
     *
     * @param requestId  the unique identifier of the request
     * @param newStatusId  the new status to set
     * @throws RuntimeException (or specific subclass) if request not found or transition invalid
     */
    void changeStatus(long requestId, int newStatusId);

    /**
     * Convenient method for creating a new repair request together with client registration
     * (or lookup if client already exists).
     *
     * <p>This method is especially useful when client data is provided during request creation
     * without prior client registration in the system.</p>
     *
     * <p>Implementation should:
     * <ul>
     *     <li>search for existing client by phone number (or name + phone)</li>
     *     <li>create new client if none found</li>
     *     <li>create request linked to clientId</li>
     *     <li>set initial status and dateReceived</li>
     * </ul></p>
     *
     * @param fullName         client's full name
     * @param phoneNumber      client's phone number (key for lookup/identification)
     * @param deviceType       type of device being repaired
     * @param deviceBrand      manufacturer/brand of the device
     * @param deviceModel      model name/number
     * @param deviceIMEIorSDN  IMEI (phones) or serial number/other identifier
     * @param description      problem description provided by the client
     * @return created {@link Request} entity with assigned ID
     * 
     * @throws ServiceException (or specific subclass) if validation fails
     */
    void createRequestWithClient(
          String fullName,
          String phoneNumber,
          int deviceType,
          String deviceBrand,
          String deviceModel,
          String deviceIMEIorSDN,
          String description,
          long dateReceived
    );
}
