package com.defragler.fixiqo.entities.enums;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

/**
 * Enumeration representing different types of devices that can be accepted for repair
 * in the Fixiqo service center system.
 *
 * <p>Each device type has a unique numeric identifier (used in database/storage)
 * and a human-readable name.</p>
 *
 * <p>Supported types include common consumer electronics as well as a catch-all "Other" category.</p>
 */
public enum DeviceTypeEnum {
    DESKTOP(1,"Desktop"),
    LAPTOP(2,"Laptop"),
    TABLET(3,"Tablet"),
    PHONE(4,"Phone"),
    OTHER(5,"Other");

    private final int id;
    private final String type;

    /**
     * Constructs a DeviceType enum constant.
     *
     * @param id   unique numeric identifier (used in persistence layer)
     * @param type human-readable name of the device type
     */
    DeviceTypeEnum(int id, String type){
        this.id = id;
        this.type = type;
    }

    /**
     * Returns the numeric identifier of this device type.
     *
     * @return the ID associated with this enum constant
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the human-readable name of this device type.
     *
     * @return the display name (e.g. "Laptop", "Phone")
     */
    public String getType() {
        return type;
    }

    /**
     * Finds and returns the {@code DeviceType} corresponding to the given numeric ID.
     *
     * @param id the numeric identifier to look up
     * @return the matching {@code DeviceType} enum constant
     * @throws EntityException if no device type exists with the specified ID
     */
    public static DeviceTypeEnum fromId(int id) {
        for (DeviceTypeEnum type : values()) {
            if (type.id == id) return type;
        }
        throw new EntityException(ExceptionLevel.WARNING,"Invalid DeviceType id: " + id);
    }

    /**
     * Returns the human-readable name of this device type.
     * Convenience method equivalent to {@link #getType()}.
     *
     * @return the display name of the device type
     */
    @Override
    public String toString() {
        return type;
    }
}
