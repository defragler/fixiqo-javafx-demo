package com.defragler.fixiqo.repositories.sqlite.request;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

public class RequestRepository extends SqliteRepository<Request, Long> implements IRequestRepository {

    public RequestRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(Request.class), 5 * 60_000L, 200);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "Requests";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Long getId(Request entity) {
        return entity.getId();
    }

    @Override
    public List<Request> findByClientId(long clientId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE client_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, clientId);
                try (var rs = stmt.executeQuery()) {
                    List<Request> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }

    @Override
    public List<Request> findByDeviceId(long deviceId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE device_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, deviceId);
                try (var rs = stmt.executeQuery()) {
                    List<Request> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }

    @Override
    public List<Request> findByStatusId(long statusId) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE status_id = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, statusId);
                try (var rs = stmt.executeQuery()) {
                    List<Request> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }
}
