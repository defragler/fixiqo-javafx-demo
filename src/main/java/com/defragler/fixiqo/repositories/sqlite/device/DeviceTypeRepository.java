package com.defragler.fixiqo.repositories.sqlite.device;

import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;

public class DeviceTypeRepository extends SqliteRepository<DeviceType, Integer> implements IDeviceTypeRepository  {

    public DeviceTypeRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(DeviceType.class), 5 * 60_000L, 100);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "DeviceTypes";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Integer getId(DeviceType entity) {
        return entity.getId();
    }

    public DeviceType findByCode(String code) {
        return execute(conn -> MapperFactory.getMapper(DeviceType.class)
              .fromResultSet(conn.prepareStatement(
                    "SELECT * FROM " + tableName() + " WHERE code = ?")
                    .executeQuery()));
    }
}
