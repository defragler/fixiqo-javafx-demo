package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.client.*;
import com.defragler.fixiqo.repositories.sqlite.device.*;
import com.defragler.fixiqo.repositories.sqlite.option.*;
import com.defragler.fixiqo.repositories.sqlite.part.*;
import com.defragler.fixiqo.repositories.sqlite.request.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.time.*;
import java.util.*;

/**
 * Implementation of the {@link IRequestsService} interface that provides core business logic
 * for managing repair requests in the Fixiqo service center system.
 *
 * <p>This service handles:
 * <ul>
 *     <li>CRUD operations for {@link Request} entities</li>
 *     <li>Status transitions with automatic timestamp updates</li>
 *     <li>Cascading deletion of related parts and options</li>
 *     <li>Convenient request creation with automatic client lookup/registration</li>
 * </ul></p>
 *
 * <p>Designed for Dependency Injection (DI):
 * <ul>
 *     <li>All dependencies are injected via constructor</li>
 *     <li>Does not create repositories or services internally</li>
 *     <li>Easily testable with mocks</li>
 * </ul></p>
 * 
 * <p><strong>Performance note:</strong>
 * Several methods (e.g. {@link #delete(long)}, {@link #getAll()})
 * load all entities into memory for filtering or cascading. For small datasets this is acceptable,
 * but in production add specialized repository queries (e.g. findByClientId, findByRequestId)
 * to avoid full collection loading.</p>
 *
 * <p><strong>Business rules enforced:</strong>
 * <ul>
 *     <li>Initial status = ACCEPTED</li>
 *     <li>{@code dateReceived} set automatically on creation</li>
 *     <li>{@code dateIssued} set automatically when status changes to ISSUED</li>
 * </ul></p>
 */
public class RequestsService implements IRequestsService {

    private final IClientRepository clientRepository;
    private final IDeviceRepository deviceRepository;
    private final IRequestRepository requestRepository;
    private final IOptionRepository optionRepository;
    private final IPartRepository partRepository;
    private final IStatusService statusService;
    private final IValidationService validationService;

    /**
     * Constructs RequestsService with all required repository and validation dependencies.
     *
     * @param clientRepository   repository for client entities
     * @param deviceRepository   repository for device entities
     * @param requestRepository  repository for repair request entities
     * @param requestStatusHistoryRepository  repository for repair request entities
     * @param optionRepository   repository for additional options/services
     * @param partRepository     repository for spare parts
     * @param validationService  service for input validation (phone, etc.)
     */
    public RequestsService(IClientRepository clientRepository,
          IDeviceRepository deviceRepository,
          IRequestRepository requestRepository,
          IRequestStatusHistoryRepository requestStatusHistoryRepository,
          IOptionRepository optionRepository,
          IPartRepository partRepository,
          IStatusService statusService,
          IValidationService validationService) {
        this.clientRepository = clientRepository;
        this.deviceRepository = deviceRepository;
        this.requestRepository = requestRepository;
        this.optionRepository = optionRepository;
        this.partRepository = partRepository;
        this.statusService = statusService;
        this.validationService = validationService;
    }

    /**
     * Returns all repair requests in the system.
     *
     * @return list of all requests (empty list if none exist)
     */
    @Override
    public List<Request> getAll() {
        return requestRepository.findAll();
    }

    /**
     * Finds a specific repair request by its unique identifier.
     *
     * @param id the unique identifier of the request
     * @return Optional containing the request if found, or empty otherwise
     */
    @Override
    public Optional<Request> getById(long id) {
        return requestRepository.findById(id);
    }

    /**
     * Retrieves all repair requests associated with a specific client.
     *
     * <p>This method delegates to repository layer and avoids
     * full collection loading, improving performance for large datasets.</p>
     *
     * @param clientId unique identifier of the client
     * @return list of requests belonging to the client (empty list if none exist)
     */
    @Override
    public List<Request> getByClientId(long clientId) {
        return requestRepository.findByClientId(clientId);
    }

    /**
     * Creates and persists a new repair request.
     *
     * <p>Ensures:
     * <ul>
     *     <li>client with given ID exists</li>
     *     <li>initial status is ACCEPTED if not specified</li>
     *     <li>{@code dateReceived} is set to current time if not provided</li>
     * </ul></p>
     *
     * @param request the request data to create
     * @return persisted request with assigned ID
     * @throws ServiceException if client does not exist
     */
    @Override
    public Request create(Request request) {
        clientRepository.findById(request.getClientId())
              .orElseThrow(() -> new ServiceException(ExceptionLevel.ERROR,"Client not found"));

        deviceRepository.findById(request.getDeviceId())
              .orElseThrow(() -> new ServiceException(ExceptionLevel.ERROR,"Device not found"));
        
        Request newRequest = new Request(
              request.getClientId(),
              request.getDeviceId(),
              request.getDescription(),
              request.getStatusId() != 0 ? request.getStatusId() : statusService.getAcceptedStatusId(),
              request.getDateReceived() != 0 ? request.getDateReceived() : Instant.now().getEpochSecond(),
              request.getDateIssued()
        );

        requestRepository.create(newRequest);
        statusService.recordInitialStatus(
              newRequest.getId(),
              newRequest.getStatusId()
        );
        return newRequest;
    }

    /**
     * Updates an existing repair request.
     *
     * <p>Typically used for modifying device info or description.
     * Status changes should be done via {@link #changeStatus(long, int)}.</p>
     *
     * @param request the updated request entity (must have valid ID)
     */
    @Override
    public void update(Request request) {
        requestRepository.update(request);
    }

    /**
     * Deletes a repair request and all related data in cascade mode:
     * <ul>
     *     <li>all parts associated with the request</li>
     *     <li>all options/services linked to the request</li>
     *     <li>the request itself</li>
     * </ul>
     *
     * <p><strong>Warning:</strong> This is a destructive operation with no undo.</p>
     *
     * @param id the unique identifier of the request to delete
     * @throws ServiceException if request not found
     */
    @Override
    public void delete(long id) {

        Request request = requestRepository.findById(id)
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,"Request with ID " + id + " not found")
              );

        optionRepository.findAll().stream()
              .filter(o -> o.getRequestId() == id)
              .map(Option::getId)
              .forEach(optionRepository::delete);

        partRepository.findAll().stream()
              .filter(p -> p.getRequestId() == id)
              .map(Part::getId)
              .forEach(partRepository::delete);

        requestRepository.delete(request.getId());
    }

    /**
     * Changes the status of a repair request and updates related timestamps if necessary.
     *
     * <p>Special handling:
     * <ul>
     *     <li>When new status is ISSUED, sets {@code dateIssued} to current time</li>
     * </ul></p>
     *
     * @param requestId  ID of the request to update
     * @param newStatusId  new status value
     * @throws ServiceException if request not found
     */
    @Override
    public void changeStatus(long requestId, int newStatusId) {
        statusService.changeStatus(requestId, newStatusId);
    }

    /**
     * Creates request with automatic client creation/lookup using updated Client entity.
     *
     * <p>Logic:
     * <ul>
     *     <li>Validates phone number</li>
     *     <li>Searches for existing client by fullName + phoneNumber</li>
     *     <li>Creates new client if none found</li>
     *     <li>Creates request linked to client ID with status ACCEPTED</li>
     * </ul></p>
     *
     * @param fullName         full name of the client
     * @param phoneNumber      client's phone number
     * @param type             device type
     * @param brand            device brand
     * @param model            device model
     * @param imeiOrSdn        device identifier
     * @param description      problem description
     */
    @Override
    public void createRequestWithClient(
          String fullName,
          String phoneNumber,
          int type,
          String brand,
          String model,
          String imeiOrSdn,
          String description,
          long dateReceived
    ) {
        validationService.validatePhone(phoneNumber);

        // --- CLIENT ---
        Client client = clientRepository
              .findByFullNameAndPhone(fullName, phoneNumber)
              .orElseGet(() -> {
                  Client c = new Client(fullName, phoneNumber);
                  clientRepository.create(c);
                  return c;
              });

        // --- DEVICE ---
        Device device = Optional.ofNullable(imeiOrSdn)
              .filter(s -> !s.isBlank())
              .flatMap(deviceRepository::findByImeiOrSdn)
              .orElseGet(() -> {
                  Device d = new Device(
                        type,
                        brand,
                        model,
                        imeiOrSdn
                  );
                  deviceRepository.create(d);
                  return d;
              });

        long finalDateReceived = (dateReceived <= 0)
              ? Instant.now().getEpochSecond()
              : dateReceived;

        // --- REQUEST ---
        Request request = new Request(
              client.getId(),
              device.getId(),
              description,
              statusService.getAcceptedStatusId(),
              finalDateReceived,
              null
        );

        requestRepository.create(request);
        statusService.changeStatus(request.getId(), statusService.getAcceptedStatusId());
    }
}
