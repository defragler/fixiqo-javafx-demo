package com.defragler.fixiqo.repositories.sqlite.request;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

public class RequestStatusHistoryRepository extends SqliteRepository<RequestStatusHistory, Long> implements IRequestStatusHistoryRepository {

    public RequestStatusHistoryRepository(IDatabaseService databaseService) {
        super(databaseService,
              MapperFactory.getMapper(RequestStatusHistory.class),
              5 * 60_000L,
              200);

        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "RequestStatusHistory";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Long getId(RequestStatusHistory entity) {
        return entity.getId();
    }

    @Override
    public List<RequestStatusHistory> findByRequestId(long requestId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() +
                  " WHERE request_id = ? ORDER BY changed_at ASC";

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, requestId);

                try (var rs = stmt.executeQuery()) {
                    List<RequestStatusHistory> list = new ArrayList<>();

                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }

                    return list;
                }
            }
        });
    }

    public Optional<RequestStatusHistory> findLatestByRequestId(long requestId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() +
                  " WHERE request_id = ? ORDER BY changed_at DESC LIMIT 1";

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, requestId);

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
