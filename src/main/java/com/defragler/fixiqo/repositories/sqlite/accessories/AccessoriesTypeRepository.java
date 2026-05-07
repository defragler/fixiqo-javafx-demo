package com.defragler.fixiqo.repositories.sqlite.accessories;

import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.repositories.*;

public class AccessoriesTypeRepository extends SqliteRepository<AccessoriesType, Integer>
      implements IAccessoriesTypeRepository {

    public AccessoriesTypeRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(AccessoriesType.class),
              5 * 60_000L, 100);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "AccessoriesTypes";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Integer getId(AccessoriesType entity) {
        return entity.getId();
    }

    @Override
    public AccessoriesType findByCode(String code) {
        return execute(conn ->
              MapperFactory.getMapper(AccessoriesType.class)
                    .fromResultSet(
                          conn.prepareStatement(
                                "SELECT * FROM " + tableName() + " WHERE code = ?"
                          ).executeQuery()
                    )
        );
    }
}