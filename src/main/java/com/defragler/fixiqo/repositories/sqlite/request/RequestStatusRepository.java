package com.defragler.fixiqo.repositories.sqlite.request;

import com.defragler.fixiqo.entities.references.RequestStatus;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;

public class RequestStatusRepository extends SqliteRepository<RequestStatus, Integer> implements IRequestStatusRepository {

    public RequestStatusRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(RequestStatus.class), 5 * 60_000L, 100);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "RequestStatuses";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Integer getId(RequestStatus entity) {
        return entity.getId();
    }

    public RequestStatus findByCode(String code) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE code = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapper.fromResultSet(rs);
                    }
                    return null;
                }
            }
        });
    }
}
