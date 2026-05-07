package com.defragler.fixiqo.repositories.sqlite.device;

import com.defragler.fixiqo.entities.references.DeviceType;
import com.defragler.fixiqo.repositories.*;

public interface IDeviceTypeRepository extends Repository<DeviceType, Integer> {
    DeviceType findByCode(String code);
}
