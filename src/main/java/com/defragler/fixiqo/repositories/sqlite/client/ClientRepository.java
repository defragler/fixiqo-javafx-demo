package com.defragler.fixiqo.repositories.sqlite.client;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;
import java.util.*;

public class ClientRepository extends SqliteRepository<Client, Long> implements IClientRepository{

    public ClientRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(Client.class), 5 * 60_000L, 200);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "Clients";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Long getId(Client entity) {
        return entity.getId();
    }
    
    public List<Client> findByFullName(String fullName) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE full_name = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fullName);
                try (var rs = stmt.executeQuery()) {
                    List<Client> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.fromResultSet(rs));
                    }
                    return list;
                }
            }
        });
    }

    @Override
    public Optional<Client> findByPhoneNumber(String phoneNumber) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE phone_number = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phoneNumber);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapper.fromResultSet(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        });
    }

    @Override
    public Optional<Client> findByFullNameAndPhone(String fullName, String phoneNumber) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE full_name = ? AND phone_number = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fullName);
                stmt.setString(2, phoneNumber);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapper.fromResultSet(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        });
    }
}
