package com.defragler.fixiqo.repositories.sqlite.accessories;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.repositories.*;

import java.util.*;

public class AccessoriesRepository extends SqliteRepository<Accessories, Long>
      implements IAccessoriesRepository {

    public AccessoriesRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(Accessories.class),
              5 * 60_000L, 200);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "Accessories";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Long getId(Accessories entity) {
        return entity.getId();
    }

    @Override
    public List<Accessories> findByRequestId(long requestId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE request_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, requestId);

                try (var rs = stmt.executeQuery()) {
                    List<Accessories> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }

    @Override
    public List<Accessories> findByAccessoriesTypeId(long accessoriesTypeId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE accessories_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, accessoriesTypeId);

                try (var rs = stmt.executeQuery()) {
                    List<Accessories> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }

    @Override
    public void deleteByRequestId(long requestId) {
        execute(conn -> {
            String sql = "DELETE FROM " + tableName() + " WHERE request_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, requestId);
                stmt.executeUpdate();
                return null;
            }
        });
    }
}