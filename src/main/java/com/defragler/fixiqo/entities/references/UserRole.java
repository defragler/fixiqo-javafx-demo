package com.defragler.fixiqo.entities.references;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.enums.*;

/**
 * Represents a user role in the system.
 *
 * <p>Reference table storing role identifiers and codes.</p>
 * Localization is handled externally via i18n using the role code.</p>
 */
@ReferenceTable(values = UserRoleEnum.class)
@Table(name = "Roles")
@Indexes({
      @Index(name = "idx_roles_code", columns = {"code"}, unique = true)
})
public class UserRole {
    @Id(autoIncrement = false)
    @Column(name = "id", nullable = false)
    private int id;

    /**
     * Unique role code (e.g. ADMINISTRATOR, EMPLOYEE).
     * Used for business logic and localization keys.
     */
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    public UserRole() { }

    public UserRole(int id, String code) {
        this.id = id;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
