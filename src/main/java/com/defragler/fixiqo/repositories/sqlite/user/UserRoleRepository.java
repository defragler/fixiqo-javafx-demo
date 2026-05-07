package com.defragler.fixiqo.repositories.sqlite.user;

import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

public class UserRoleRepository extends SqliteRepository<UserRole, Integer> implements IUserRoleRepository {

    public UserRoleRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(UserRole.class), 5 * 60_000L, 100);
        startCacheCleanup(60_000L);
    }

    @Override
    protected String tableName() {
        return "Roles";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Integer getId(UserRole entity) {
        return entity.getId();
    }

    @Override
    public Optional<UserRole> findByCode(String code) {
        return execute(conn -> {
            var stmt = conn.prepareStatement(
                  "SELECT * FROM " + tableName() + " WHERE code = ?"
            );
            stmt.setString(1, code);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapper.fromResultSet(rs));
            }

            return Optional.empty();
        });
    }
}