package com.defragler.fixiqo.entities.references;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.enums.*;

/**
 * Represents a accessories type in the system.
 *
 * <p>Reference table storing accessories type identifiers and codes.</p>
 */
@ReferenceTable(values = AccessoriesTypeEnum.class)
@Table(name = "AccessoriesTypes")
@Indexes({
      @Index(name = "idx_accessories_types_code", columns = {"code"}, unique = true)
})
public class AccessoriesType {
    @Id(autoIncrement = false)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    public AccessoriesType() { }

    public AccessoriesType(int id, String code) {
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
