package com.defragler.fixiqo.entities.enums;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

/**
 * Enumeration representing user roles in the Fixiqo service center system.
 *
 * <p>Defines the access levels and responsibilities of system users:</p>
 * <ul>
 *     <li>{@link #ADMINISTRATOR} — full access, including user management, system configuration, etc.</li>
 *     <li>{@link #MANAGER} — middle level access, including user, request, parts and options management, etc.</li>
 *     <li>{@link #EMPLOYEE} — standard access for performing repair tasks, viewing requests, etc.</li>
 * </ul>
 *
 * <p>Each role has a unique numeric identifier (used in persistence) and a display name.</p>
 */
public enum UserRoleEnum {
    ADMINISTRATOR(1,"Administrator"),
    MANAGER(2,"Manager"),
    EMPLOYEE(3,"Employee");

    private final int id;
    private final String name;

    /**
     * Constructs a UserRole enum constant.
     *
     * @param id   unique numeric identifier (used in database/storage)
     * @param name human-readable name of the role
     */
    UserRoleEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the numeric identifier of this role.
     *
     * @return the ID associated with this role
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the human-readable name of this role.
     *
     * @return the display name (e.g. "Administrator", "Employee")
     */
    public String getName() {
        return name;
    }

    /**
     * Finds and returns the {@code UserRole} corresponding to the given numeric ID.
     *
     * @param id the numeric identifier to look up
     * @return the matching {@code UserRole} enum constant
     * @throws EntityException if no role exists with the specified ID
     */
    public static UserRoleEnum fromId(int id) {
        for (UserRoleEnum role : values()) {
            if (role.getId() == id) return role;
        }
        throw new EntityException(ExceptionLevel.WARNING,"Invalid UserRole id: " + id);
    }

    /**
     * Returns the human-readable name of this role.
     * Equivalent to calling {@link #getName()}.
     *
     * @return the display name of the role
     */
    @Override
    public String toString() {
        return name;
    }
}
