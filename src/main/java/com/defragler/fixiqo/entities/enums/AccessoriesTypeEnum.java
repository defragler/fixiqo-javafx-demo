package com.defragler.fixiqo.entities.enums;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

/**
 * Enumeration representing different types of accessories that can be added to device
 * in the Fixiqo service center system.
 *
 * <p>Each accessories type has a unique numeric identifier (used in database/storage)
 * and a human-readable name.</p>
 *
 * <p>Supported types include common consumer accessories as well as a catch-all "Other" category.</p>
 */
public enum AccessoriesTypeEnum {
    CASE(1, "Case"),
    CABLE(2, "USB Cable"),
    CHARGER(3, "Charger"),
    POWER_ADAPTER(4, "Power Adapter"),
    WIRELESS_CHARGER(5, "Wireless Charger"),
    DOCKING_STATION(6, "Docking Station"),
    MEMORY_CARD(7, "Memory Card"),
    ADAPTERS(8, "Adapters"),
    SPEAKER(9, "Speaker"),
    OTHER(10, "Other");

    private final int id;
    private final String type;

    /**
     * Constructs a AccessoriesType enum constant.
     *
     * @param id   unique numeric identifier (used in persistence layer)
     * @param type human-readable name of the accessories type
     */
    AccessoriesTypeEnum(int id, String type){
        this.id = id;
        this.type = type;
    }

    /**
     * Returns the numeric identifier of this accessories type.
     *
     * @return the ID associated with this enum constant
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the human-readable name of this accessories type.
     *
     * @return the display name (e.g. "Case", "Charge")
     */
    public String getType() {
        return type;
    }

    /**
     * Finds and returns the {@code AccessoriesType} corresponding to the given numeric ID.
     *
     * @param id the numeric identifier to look up
     * @return the matching {@code AccessoriesType} enum constant
     * @throws EntityException if no device type exists with the specified ID
     */
    public static AccessoriesTypeEnum fromId(int id) {
        for (AccessoriesTypeEnum type : values()) {
            if (type.id == id) return type;
        }
        throw new EntityException(ExceptionLevel.WARNING,"Invalid AccessoriesType id: " + id);
    }

    /**
     * Returns the human-readable name of this accessories type.
     * Convenience method equivalent to {@link #getType()}.
     *
     * @return the display name of the accessories type
     */
    @Override
    public String toString() {
        return type;
    }
}
