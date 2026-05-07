package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.client.*;
import com.defragler.fixiqo.repositories.sqlite.option.*;
import com.defragler.fixiqo.repositories.sqlite.part.*;
import com.defragler.fixiqo.repositories.sqlite.request.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;
import java.util.stream.*;

/**
 * Default implementation of {@link IClientsService} providing business logic
 * for managing clients and their related entities.
 *
 * <p>This service acts as a facade between the UI layer and data repositories.</p>
 *
 * <p><strong>Designed for Dependency Injection (DI):</strong>
 * <ul>
 *     <li>Uses constructor injection for all dependencies</li>
 *     <li>Does not create dependencies internally</li>
 *     <li>Can be easily mocked or replaced in tests</li>
 * </ul></p>
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *     <li>Retrieving clients and their details</li>
 *     <li>Fetching requests associated with a client</li>
 *     <li>Handling cascading deletion of client-related data</li>
 * </ul></p>
 *
 * <p><strong>Performance note:</strong>
 * Current implementation loads all entities into memory for filtering.
 * For large datasets, repository-level filtering (e.g. {@code findByClientId})
 * is strongly recommended.</p>
 */
public class ClientsService implements IClientsService {
    private final IClientRepository clientRepository;
    private final IRequestRepository requestRepository;
    private final IOptionRepository optionRepository;
    private final IPartRepository partRepository;

    /**
     * Constructs {@code ClientsService} with required dependencies.
     *
     * <p>This constructor is intended to be used by a DI container.</p>
     *
     * @param clientRepository  repository for client entities
     * @param requestRepository repository for repair requests
     * @param optionRepository  repository for additional services/options
     * @param partRepository    repository for spare parts
     */
    public ClientsService(IClientRepository clientRepository,
          IRequestRepository requestRepository,
          IOptionRepository optionRepository,
          IPartRepository partRepository) {
        this.clientRepository = clientRepository;
        this.requestRepository = requestRepository;
        this.optionRepository = optionRepository;
        this.partRepository = partRepository;
    }

    /**
     * Returns all registered clients in the system.
     *
     * @return list of all clients (empty list if none exist)
     */
    @Override
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    /**
     * Finds a client by their unique identifier.
     *
     * @param clientId the unique identifier of the client
     * @return Optional containing the client if found, or empty otherwise
     */
    @Override
    public Optional<Client> getById(long clientId) {
        return clientRepository.findById(clientId);
    }

    /**
     * Retrieves all repair requests submitted by the specified client.
     *
     * <p>Throws exception if client does not exist.</p>
     *
     * @param clientId the unique identifier of the client
     * @return list of requests belonging to the client
     * @throws ServiceException if client with given ID is not found
     */
    @Override
    public List<Request> getRequestsByClientId(long clientId) {
        if (clientRepository.findById(clientId).isEmpty()) {
            throw new ServiceException(ExceptionLevel.ERROR,"Client with ID " + clientId + " not found");
        }

        return requestRepository.findAll().stream()
              .filter(r -> r.getClientId() == clientId)
              .collect(Collectors.toList());
    }

    /**
     * Creates a new client after validation and uniqueness checks.
     *
     * <p>Process:
     * <ol>
     *     <li>Validates full name and phone number</li>
     *     <li>Checks phone number uniqueness</li>
     *     <li>Creates and persists new client</li>
     * </ol></p>
     *
     * @param fullName    full name of the client
     * @param phoneNumber client's phone number
     * @return created and persisted {@link Client} entity
     * @throws ServiceException if validation fails or phone number already exists
     */
    @Override
    public Client create(String fullName, String phoneNumber) {

        if (fullName == null || fullName.isBlank()) {
            throw new ServiceException(ExceptionLevel.ERROR,"Client name cannot be empty");
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new ServiceException(ExceptionLevel.ERROR,"Phone number cannot be empty");
        }

        clientRepository.findByPhoneNumber(phoneNumber)
              .ifPresent(c -> {
                  throw new ServiceException(ExceptionLevel.ERROR,
                        "Client with phone number '" + phoneNumber + "' already exists"
                  );
              });

        Client client = new Client(fullName, phoneNumber);

        clientRepository.create(client);
        return client;
    }

    /**
     * Updates an existing client.
     *
     * <p>Process:
     * <ol>
     *     <li>Finds existing client by ID</li>
     *     <li>Validates new values</li>
     *     <li>Checks phone number uniqueness (excluding current client)</li>
     *     <li>Applies changes and persists updated client</li>
     * </ol></p>
     *
     * @param clientId    unique identifier of the client to update
     * @param fullName    new or unchanged full name
     * @param phoneNumber new or unchanged phone number
     * @return updated {@link Client} entity
     * @throws ServiceException if client not found or validation fails
     */
    @Override
    public Client update(long clientId, String fullName, String phoneNumber) {

        Client existing = clientRepository.findById(clientId)
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,"Client with ID " + clientId + " not found")
              );

        // --- FULL NAME ---
        if (fullName != null && !fullName.isBlank()) {
            existing.setFullName(fullName);
        } else {
            throw new ServiceException(ExceptionLevel.ERROR,"Client name cannot be empty");
        }

        // --- PHONE ---
        if (phoneNumber != null && !phoneNumber.isBlank()) {

            clientRepository.findByPhoneNumber(phoneNumber)
                  .filter(c -> c.getId() != clientId)
                  .ifPresent(c -> {
                      throw new ServiceException(ExceptionLevel.ERROR,
                            "Client with phone number '" + phoneNumber + "' already exists"
                      );
                  });

            existing.setPhoneNumber(phoneNumber);
        } else {
            throw new ServiceException(ExceptionLevel.ERROR,"Phone number cannot be empty");
        }

        clientRepository.update(existing);
        return existing;
    }

    /**
     * Deletes a client by their unique identifier.
     *
     * <p>No cascading deletion is performed — only the client record is removed.
     * Related entities (requests, parts, options) remain unchanged.</p>
     *
     * @param clientId the unique identifier of the client to delete
     * @throws ServiceException if client not found
     */
    @Override
    public void delete(long clientId) {

        Client client = clientRepository.findById(clientId)
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,"Client with ID " + clientId + " not found")
              );

        clientRepository.delete(client.getId());
    }

    /**
     * Deletes a client and performs cascading deletion of all related data:
     * <ul>
     *     <li>all repair requests of this client</li>
     *     <li>all parts associated with those requests</li>
     *     <li>all additional options/services linked to those requests</li>
     *     <li>the client record itself</li>
     * </ul>
     *
     * <p><strong>Important:</strong> This is a destructive operation with no undo.
     * All data related to the client will be permanently removed from the storage.</p>
     *
     * @param clientId the unique identifier of the client to delete
     * @throws ServiceException if client with given ID is not found
     */
    @Override
    public void deleteClientCascade(long clientId) {
        Client client = clientRepository.findById(clientId)
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,"Client with ID " + clientId + " not found")
              );

        List<Request> clientRequests = requestRepository.findAll().stream()
              .filter(r -> r.getClientId() == clientId)
              .toList();

        for (Request request : clientRequests) {
            deleteRequestCascade(request.getId());
        }

        clientRepository.delete(client.getId());
    }

    /**
     * Helper method that performs cascading deletion of a single request and all its related data:
     * options, parts and the request itself.
     *
     * <p>Used internally by {@link #deleteClientCascade(long)}.</p>
     *
     * @param requestId the unique identifier of the request to delete
     */
    private void deleteRequestCascade(long requestId) {

        optionRepository.findAll().stream()
              .filter(o -> o.getRequestId() == requestId)
              .map(Option::getId)
              .forEach(optionRepository::delete);

        partRepository.findAll().stream()
              .filter(p -> p.getRequestId() == requestId)
              .map(Part::getId)
              .forEach(partRepository::delete);

        requestRepository.delete(requestId);
    }
}
