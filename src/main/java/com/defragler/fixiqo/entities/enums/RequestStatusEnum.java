package com.defragler.fixiqo.entities.enums;

import com.defragler.fixiqo.exceptions.EntityException;

/**
 * Enumeration representing the possible statuses of a repair request in the Fixiqo service center system.
 *
 * <p>Each status has a unique numeric identifier (used for persistence) and a human-readable name.</p>
 *
 * <p>The statuses reflect the typical lifecycle of a repair request:</p>
 * <ul>
 *     <li>ACCEPTED → WAITING_PARTS → IN_PROGRESS → COMPLETED → ISSUED → RETURNED</li>
 * </ul>
 */
public enum RequestStatusEnum {
    ACCEPTED(1,"Accepted"),
    WAITING_PARTS(2,"Waiting parts"),
    IN_PROGRESS(3,"In progress"),
    COMPLETED(4,"Completed"),
    ISSUED(5,"Issued"),
    RETURNED(6,"Returned");

    private final int id;
    private final String status;

    /**
     * Constructs a RequestStatus enum constant.
     *
     * @param id     unique numeric identifier (used in database/storage)
     * @param status human-readable name of the request status
     */
    RequestStatusEnum(int id, String status) {
        this.id = id;
        this.status = status;
    }

    /**
     * Returns the numeric identifier of this status.
     *
     * @return the ID associated with this enum constant
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the human-readable name of this status.
     *
     * @return the display name (e.g. "In progress", "Completed")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Finds and returns the {@code RequestStatus} corresponding to the given numeric ID.
     *
     * @param id the numeric identifier to look up
     * @return the matching {@code RequestStatus} enum constant
     * @throws EntityException if no device type exists with the specified ID
     */
    public static RequestStatusEnum fromId(int id) {
        for (RequestStatusEnum status : values()) {
            if (status.id == id) return status;
        }
        return ACCEPTED;
    }

    /**
     * Returns the human-readable name of this status.
     * Equivalent to calling {@link #getStatus()}.
     *
     * @return the display name of the status
     */
    @Override
    public String toString() {
        return status;
    }
}
