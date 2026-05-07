package com.defragler.fixiqo.repositories.sqlite.user;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.repositories.sqlite.*;
import com.defragler.fixiqo.services.interfaces.*;
import java.util.Optional;

/**
 * SQLite repository for User entity.
 * Provides thread-safe CRUD, batch operations, caching, and async methods.
 */
public class UserRepository extends SqliteRepository<User, Long> implements IUserRepository {

    public UserRepository(IDatabaseService databaseService) {
        super(databaseService, MapperFactory.getMapper(User.class), 5 * 60_000L, 200); // TTL 5 min, max 200 cached users
        startCacheCleanup(60_000L); // clean expired cache every 1 min
    }

    @Override
    protected String tableName() {
        return "Users";
    }

    @Override
    protected String idColumn() {
        return "id";
    }

    @Override
    protected Long getId(User entity) {
        return entity.getId();
    }

    /**
     * Finds a user by username.
     */
    public Optional<User> findByUsername(String username) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE username = ?";

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);

                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapper.fromResultSet(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    /**
     * Finds a user by email.
     */
    public Optional<User> findByEmail(String email) {
        return execute(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE email = ?";

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);

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
