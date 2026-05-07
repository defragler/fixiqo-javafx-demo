package com.defragler.fixiqo.repositories.sqlite.device;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

public class DeviceRepository extends SqliteRepository<Device, Long> implements IDeviceRepository {

    public DeviceRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(Device.class), 5 * 60_000L, 200);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "Devices";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Long getId(Device entity) {
        return entity.getId();
    }

    @Override
    public List<Device> findByDeviceTypeId(long deviceTypeId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE device_type_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, deviceTypeId);
                try (var rs = stmt.executeQuery()) {
                    List<Device> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }

    @Override
    public Optional<Device> findByImeiOrSdn(String imeiOrSdn) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE imei_or_sdn = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, imeiOrSdn);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapper.fromResultSet(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
}