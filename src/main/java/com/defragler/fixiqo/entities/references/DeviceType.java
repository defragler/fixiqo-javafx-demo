package com.defragler.fixiqo.entities.references;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.enums.*;

/**
 * Represents a device type in the system.
 *
 * <p>Reference table storing device type identifiers and codes.</p>
 */
@ReferenceTable(values = DeviceTypeEnum.class)
@Table(name = "DeviceTypes")
@Indexes({
      @Index(name = "idx_device_types_code", columns = {"code"}, unique = true)
})
public class DeviceType {
    @Id(autoIncrement = false)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    public DeviceType() { }

    public DeviceType(int id, String code) {
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