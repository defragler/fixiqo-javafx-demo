package com.defragler.fixiqo.repositories.sqlite.option;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;
import java.util.*;

public class OptionRepository extends SqliteRepository<Option, Long> implements IOptionRepository {

    public OptionRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(Option.class), 5 * 60_000L, 200);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "Options";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Long getId(Option entity) {
        return entity.getId();
    }

    public List<Option> findByRequestId(long requestId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE request_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, requestId);
                try (var rs = stmt.executeQuery()) {
                    List<Option> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }
}
