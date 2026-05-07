package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;

import java.util.*;

/**
 * Service interface for managing devices in the Fixiqo system.
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>CRUD operations for devices</li>
 *     <li>Searching devices by type or identifier</li>
 *     <li>Ensuring uniqueness of IMEI/serial numbers</li>
 * </ul></p>
 */
public interface IDeviceService {

    /**
     * Retrieves all devices.
     *
     * @return list of devices
     */
    List<Device> getAll();

    /**
     * Finds device by ID.
     *
     * @param id device ID
     * @return optional device
     */
    Optional<Device> getById(long id);

    /**
     * Creates new device.
     *
     * @param device device entity
     * @return persisted device
     */
    Device create(Device device);

    /**
     * Updates existing device.
     *
     * @param device device entity with valid ID
     */
    void update(Device device);

    /**
     * Deletes device by ID.
     *
     * @param id device ID
     */
    void delete(long id);

    /**
     * Finds device by IMEI or serial number.
     *
     * @param imeiOrSdn identifier
     * @return device or null if not found
     */
    Optional<Device> findByImeiOrSdn(String imeiOrSdn);
}