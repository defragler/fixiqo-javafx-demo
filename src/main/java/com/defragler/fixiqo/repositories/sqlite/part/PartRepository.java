package com.defragler.fixiqo.repositories.sqlite.part;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;
import java.util.*;

public class PartRepository extends SqliteRepository<Part, Long> implements IPartRepository {

    public PartRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(Part.class), 5 * 60_000L, 200);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "Parts";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Long getId(Part entity) {
        return entity.getId();
    }

    @Override
    public List<Part> findByRequestId(long requestId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE request_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, requestId);
                try (var rs = stmt.executeQuery()) {
                    List<Part> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }
}
