package com.defragler.fixiqo.entities;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.references.DeviceType;

@Table(name = "Devices")
@Indexes({
      @Index(name = "idx_type_id", columns = {"type_id"}),
      @Index(name = "idx_imei_or_sdn", columns = {"imei_or_sdn"})
})
public class Device {
    @Id(autoIncrement = true)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "type_id", nullable = false)
    @ForeignKey(target = DeviceType.class, column = "id")
    private long deviceTypeId;

    @Column(name = "manufacturer", length = 50, nullable = false)
    private String brand;

    @Column(name = "model", length = 50, nullable = false)
    private String model;

    @Column(name = "imei_or_sdn", length = 50)
    private String imeiOrSdn;

    public Device() { }

    public Device(long deviceTypeId, String brand, String model, String imeiOrSdn) {
        this.deviceTypeId = deviceTypeId;
        this.brand = brand;
        this.model = model;
        this.imeiOrSdn = imeiOrSdn;
    }

    public long getId() { return id; }

    public long getDeviceTypeId() { return deviceTypeId; }
    public void setDeviceTypeId(long deviceTypeId) { this.deviceTypeId = deviceTypeId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getImeiOrSdn() { return imeiOrSdn; }
    public void setImeiOrSdn(String imeiOrSdn) { this.imeiOrSdn = imeiOrSdn; }
}