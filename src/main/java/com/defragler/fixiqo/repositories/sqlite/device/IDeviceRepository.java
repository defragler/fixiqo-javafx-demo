package com.defragler.fixiqo.repositories.sqlite.device;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import java.util.*;

public interface IDeviceRepository extends Repository<Device, Long> {

    List<Device> findByDeviceTypeId(long deviceTypeId);

    Optional<Device> findByImeiOrSdn(String imeiOrSdn);
}