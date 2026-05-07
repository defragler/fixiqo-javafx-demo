package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.device.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

/**
 * Default implementation of {@link IDeviceService} that provides
 * business logic for managing devices in the Fixiqo system.
 *
 * <p>This service acts as a layer between controllers (or UI) and repository,
 * ensuring that all device-related operations respect business rules.</p>
 *
 * <p><strong>Main responsibilities:</strong>
 * <ul>
 *     <li>CRUD operations for {@link Device}</li>
 *     <li>Ensuring uniqueness of IMEI / serial number</li>
 *     <li>Delegating persistence to {@link IDeviceRepository}</li>
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
 */
public class DeviceService implements IDeviceService {

    private final IDeviceRepository deviceRepository;

    /**
     * Constructs DeviceService with required repository dependency.
     *
     * @param deviceRepository repository used for device persistence
     */
    public DeviceService(IDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Retrieves all devices stored in the system.
     *
     * <p>This method directly delegates to the repository layer
     * without additional filtering or transformation.</p>
     *
     * @return list of all devices (empty list if none exist)
     */
    @Override
    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    /**
     * Retrieves a device by its unique identifier.
     *
     * <p>If device is not found, an empty {@link Optional} is returned.</p>
     *
     * @param id unique device ID
     * @return {@link Optional} containing the device if found, otherwise empty
     */
    @Override
    public Optional<Device> getById(long id) {
        return deviceRepository.findById(id);
    }

    /**
     * Creates and persists a new device.
     *
     * <p>Business rules enforced:
     * <ul>
     *     <li>If IMEI/serial number is provided, it must be unique</li>
     *     <li>A new ID is generated automatically</li>
     * </ul></p>
     *
     * <p>If a device with the same IMEI/SDN already exists,
     * the operation is rejected to prevent duplicates.</p>
     *
     * @param device device entity containing data to persist
     * @return persisted device with assigned ID
     *
     * @throws ServiceException if device with same IMEI/SDN already exists
     */
    @Override
    public Device create(Device device) {

        Optional.ofNullable(device.getImeiOrSdn())
              .filter(s -> !s.isBlank())
              .flatMap(deviceRepository::findByImeiOrSdn)
              .ifPresent(d -> {
                  throw new ServiceException(ExceptionLevel.ERROR,"Device with same IMEI/SDN already exists");
              });

        Device newDevice = new Device(
              device.getDeviceTypeId(),
              device.getBrand(),
              device.getModel(),
              device.getImeiOrSdn()
        );

        deviceRepository.create(newDevice);
        return newDevice;
    }

    /**
     * Updates an existing device.
     *
     * <p>This method assumes that:
     * <ul>
     *     <li>Device with given ID already exists</li>
     *     <li>All validation has been performed before invocation</li>
     * </ul></p>
     *
     * <p><strong>Note:</strong>
     * No IMEI uniqueness check is performed here. If needed,
     * it should be added to prevent conflicts during updates.</p>
     *
     * @param device device entity with updated values (must contain valid ID)
     */
    @Override
    public void update(Device device) {
        deviceRepository.update(device);
    }

    /**
     * Deletes a device by its unique identifier.
     *
     * <p><strong>Warning:</strong>
     * This operation does not check for existing references from {@link Request}.
     * If referential integrity is required, it should be enforced at:
     * <ul>
     *     <li>database level (foreign keys with constraints)</li>
     *     <li>or service layer (manual validation)</li>
     * </ul></p>
     *
     * @param id unique device ID
     */
    @Override
    public void delete(long id) {
        deviceRepository.delete(id);
    }

    /**
     * Finds a device by its IMEI or serial number.
     *
     * <p>This method is typically used to:
     * <ul>
     *     <li>avoid duplicate device creation</li>
     *     <li>reuse existing device records</li>
     * </ul></p>
     *
     * @param imeiOrSdn device identifier (IMEI or serial number)
     * @return device if found, or {@code null} otherwise
     */
    @Override
    public Optional<Device> findByImeiOrSdn(String imeiOrSdn) {
        return deviceRepository.findByImeiOrSdn(imeiOrSdn);
    }
}