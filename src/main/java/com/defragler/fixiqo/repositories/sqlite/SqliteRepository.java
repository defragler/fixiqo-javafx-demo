package com.defragler.fixiqo.repositories.sqlite;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.utilities.*;

import java.lang.reflect.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;
import java.util.stream.*;

/**
 * Advanced, generic, thread-safe SQLite repository providing CRUD operations,
 * batch insert/update/delete, LRU + TTL cache, async support, validation, logging,
 * and type conversion.
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 */
public abstract class SqliteRepository<T, ID> implements Repository<T, ID> {

    private static final Logger LOGGER = Logger.getLogger(SqliteRepository.class.getName());

    protected final IDatabaseService databaseService;
    protected final SqliteMapper<T> mapper;
    private final ReentrantLock lock = new ReentrantLock();

    // Cache with LRU + TTL support
    private final Optional<Long> ttlMillis; // null = no TTL
    private final Map<ID, CacheItem<T>> cacheLRU;
    private final int cacheMaxSize;

    protected SqliteRepository(IDatabaseService databaseService, SqliteMapper<T> mapper, Long ttlMillis, int cacheMaxSize) {
        this.databaseService = databaseService;
        this.mapper = mapper;
        this.ttlMillis = Optional.ofNullable(ttlMillis);
        this.cacheMaxSize = cacheMaxSize;
        this.cacheLRU = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<ID, CacheItem<T>> eldest) {
                boolean remove = size() > SqliteRepository.this.cacheMaxSize || eldest.getValue().isExpired();
                if (remove) LOGGER.fine("Cache LRU evicted ID: " + eldest.getKey());
                return remove;
            }
        });
    }

    protected abstract String tableName();
    protected abstract String idColumn();
    protected abstract ID getId(T entity);

    protected void validateEntity(T entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");

        EntityMetadata meta = EntityMetadata.of(entity.getClass());

        Map<String, Field> columns = meta.getColumns();
        Map<String, ForeignKey> foreignKeys = meta.getForeignKeys();

        for (Map.Entry<String, Field> entry : columns.entrySet()) {
            Field field = entry.getValue();
            Column column = field.getAnnotation(Column.class);

            if (column == null) continue;

            try {
                Object value = field.get(entity);

                // NULLABLE CHECK
                if (!column.nullable() && value == null) {
                    throw new RepositoryException(ExceptionLevel.ERROR,"Field '" + field.getName() + "' cannot be null");
                }

                // LENGTH CHECK
                if (value instanceof String && column.length() > 0 && ((String) value).length() > column.length()) {
                    throw new RepositoryException(ExceptionLevel.ERROR,
                          "Field '" + field.getName() + "' exceeds max length " + column.length()
                    );
                }

                // DEFAULT VALUE
                if (value == null && !column.defaultValue().isEmpty()) {
                    field.set(entity, convertToFieldType(field.getType(), column.defaultValue()));
                }

                // UNIQUE CHECK
                if (column.unique()) {

                    Object fieldValue = field.get(entity);
                    Object idValue = getId(entity);

                    String sql = "SELECT COUNT(1) FROM " + meta.getTableName() + " WHERE " + column.name() + " = ? AND " + idColumn() + " != ?";

                    long count = databaseService.runInTransaction(conn -> {
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setObject(1, fieldValue);
                            stmt.setObject(2, idValue);

                            try (ResultSet rs = stmt.executeQuery()) {
                                return rs.next() ? rs.getLong(1) : 0;
                            }
                        } catch (SQLException e) {
                            throw new RepositoryException(ExceptionLevel.ERROR,"SqliteRepository ", e);
                        }
                    });

                    if (count > 0) {
                        throw new RepositoryException(ExceptionLevel.ERROR,
                              "Unique constraint violated for field '" + field.getName() + "'"
                        );
                    }
                }

                // FOREIGN KEY CHECK
                ForeignKey fk = foreignKeys.get(entry.getKey());
                if (fk != null && value != null) {
                    String fkTable = EntityMetadata.of(fk.target()).getTableName();
                    String fkColumn = fk.column();
                    String sql = "SELECT COUNT(1) FROM " + fkTable + " WHERE " + fkColumn + " = ?";
                    long fkCount = databaseService.runInTransaction(conn -> {
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setObject(1, value);
                            try (ResultSet rs = stmt.executeQuery()) {
                                return rs.next() ? rs.getLong(1) : 0;
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    if (fkCount == 0) {
                        throw new RepositoryException(ExceptionLevel.ERROR,
                              "Foreign key constraint violated: field '" + field.getName() +
                                    "' references non-existing " + fkTable + "(" + fkColumn + ")"
                        );
                    }
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Converts a String default value to the appropriate field type
     */
    private Object convertToFieldType(Class<?> type, String value) {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        return value;
    }

    // ---------------- CRUD ---------------- //
    @Override
    public Optional<T> findById(ID id) {
        if (id == null) return Optional.empty();

        return getCache(id).or(() -> databaseService.runInTransaction(conn -> {
            String sql = "SELECT * FROM " + tableName() + " WHERE " + idColumn() + " = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        T entity = mapperFromResultSet(rs);
                        putCache(entity);
                        return Optional.of(entity);
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Find by ID failed", e);
                throw new RepositoryException(ExceptionLevel.ERROR,"Find by ID failed", e);
            }
        }));
    }

    @Override
    public List<T> findAll() {
        return databaseService.runInTransaction(conn -> {
            List<T> result = new ArrayList<>();
            String sql = "SELECT * FROM " + tableName();
            try (PreparedStatement stmt = conn.prepareStatement(sql); 
                  ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    T entity = mapperFromResultSet(rs);
                    result.add(entity);
                    putCache(entity);
                }
                return result;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Find all failed", e);
                throw new RepositoryException(ExceptionLevel.ERROR,"Find all failed", e);
            }
        });
    }

    @Override
    public T create(T entity) {
        validateEntity(entity);

        return databaseService.runInTransaction(conn -> {
            try {
                executeInsertAndReturnId(entity, conn);
                return entity;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Create failed", e);
                throw new RepositoryException(ExceptionLevel.ERROR,"Create failed", e);
            }
        });
    }

    @Override
    public void update(T entity) {
        validateEntity(entity);
        databaseService.runInTransaction(conn -> {
            try { executeUpdate(entity, conn); }
            catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Update failed", e);
                throw new RepositoryException(ExceptionLevel.ERROR,"Update failed", e);
            }
        });
    }

    @Override
    public void delete(ID id) {
        if (id == null) return;
        databaseService.runInTransaction(conn -> {
            String sql = "DELETE FROM " + tableName() + " WHERE " + idColumn() + " = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
                cacheLRU.remove(id);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Delete failed", e);
                throw new RepositoryException(ExceptionLevel.ERROR,"Delete failed", e);
            }
        });
    }

    // ---------------- Batch ---------------- //

    /**
     * Batch insert entities using PreparedStatement batching.
     *
     * @param entities list of entities to insert
     */
    public void batchCreate(List<T> entities) {
        if (entities == null || entities.isEmpty()) return;
        databaseService.runInTransaction(conn -> {
            for (T entity : entities) validateEntity(entity);
            T first = entities.get(0);
            Map<String, Object> columnsTemplate = mapper.toColumnMap(first);
            String colNames = String.join(", ", columnsTemplate.keySet());
            String placeholders = columnsTemplate.keySet().stream().map(c -> "?").collect(Collectors.joining(", "));
            String sql = "INSERT INTO " + tableName() + " (" + colNames + ") VALUES (" + placeholders + ")";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (T entity : entities) {
                    setParams(stmt, mapper.toColumnMap(entity).values());
                    stmt.addBatch();
                    putCache(entity);
                }
                stmt.executeBatch();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Batch create failed", e);
                throw new RepositoryException(ExceptionLevel.ERROR,"Batch create failed", e);
            }
        });
    }

    /**
     * Batch update entities using PreparedStatement batching.
     *
     * @param entities list of entities to update
     */
    public void batchUpdate(List<T> entities) {
        if (entities == null || entities.isEmpty()) return;
        databaseService.runInTransaction(conn -> {
            for (T entity : entities) validateEntity(entity);

            T first = entities.get(0);
            Map<String, Object> columnsTemplate = mapper.toColumnMap(first);
            columnsTemplate.remove(idColumn());
            String setClause = columnsTemplate.keySet().stream().map(c -> c + " = ?").collect(Collectors.joining(", "));
            String sql = "UPDATE " + tableName() + " SET " + setClause + " WHERE " + idColumn() + " = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (T entity : entities) {
                    Map<String, Object> cols = mapper.toColumnMap(entity);
                    cols.remove(idColumn());
                    setParams(stmt, cols.values());
                    stmt.setObject(cols.size() + 1, getId(entity));
                    stmt.addBatch();
                    putCache(entity);
                }
                stmt.executeBatch();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Batch update failed", e);
                throw new RepositoryException(ExceptionLevel.ERROR,"Batch update failed", e);
            }
        });
    }

    /**
     * Batch delete entities by list of IDs using PreparedStatement batching.
     *
     * @param ids list of entity IDs to delete
     */
    public void batchDelete(List<ID> ids) {
        if (ids == null || ids.isEmpty()) return;
        databaseService.runInTransaction(conn -> {
            String sql = "DELETE FROM " + tableName() + " WHERE " + idColumn() + " = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (ID id : ids) {
                    stmt.setObject(1, id);
                    stmt.addBatch();
                    cacheLRU.remove(id);
                }
                stmt.executeBatch();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Batch delete failed", e);
                throw new RepositoryException(ExceptionLevel.ERROR,"Batch delete failed", e);
            }
        });
    }

    // ---------------- Async CRUD ---------------- //

    public CompletableFuture<Void> createAsync(T entity) { return CompletableFuture.runAsync(() -> create(entity)); }
    public CompletableFuture<Void> updateAsync(T entity) { return CompletableFuture.runAsync(() -> update(entity)); }
    public CompletableFuture<Void> deleteAsync(ID id) { return CompletableFuture.runAsync(() -> delete(id)); }
    public CompletableFuture<List<T>> findAllAsync() { return CompletableFuture.supplyAsync(this::findAll); }
    public CompletableFuture<Optional<T>> findByIdAsync(ID id) { return CompletableFuture.supplyAsync(() -> findById(id)); }
    public CompletableFuture<Void> batchCreateAsync(List<T> entities) { return CompletableFuture.runAsync(() -> batchCreate(entities)); }
    public CompletableFuture<Void> batchUpdateAsync(List<T> entities) { return CompletableFuture.runAsync(() -> batchUpdate(entities)); }
    public CompletableFuture<Void> batchDeleteAsync(List<ID> ids) { return CompletableFuture.runAsync(() -> batchDelete(ids)); }

    // ---------------- Utility ---------------- //

    private void setParams(PreparedStatement stmt, Collection<Object> values) throws SQLException {
        int index = 1;
        for (Object value : values) stmt.setObject(index++, convertForSql(value));
    }

    private Object convertForSql(Object value) {
        if (value instanceof LocalDateTime) return UnixDateTimeConverter.toUnixSeconds((LocalDateTime) value);
        if (value instanceof Boolean) return ((Boolean) value) ? 1 : 0;
        if (value instanceof Enum<?>) return ((Enum<?>) value).name();
        return value;
    }

    private T mapperFromResultSet(ResultSet rs) throws SQLException {
        T entity = mapper.fromResultSet(rs);
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.getType() == LocalDateTime.class) {
                    Object raw = field.get(entity);
                    if (raw instanceof Long) field.set(entity, UnixDateTimeConverter.fromUnixSeconds((Long) raw));
                }
                if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                    Object raw = field.get(entity);
                    if (raw instanceof Number) field.set(entity, ((Number) raw).intValue() != 0);
                }
                if (field.getType().isEnum()) {
                    Object raw = field.get(entity);
                    if (raw instanceof String) {
                        Object enumValue = Enum.valueOf((Class<Enum>) field.getType(), (String) raw);
                        field.set(entity, enumValue);
                    }
                }
            } catch (IllegalAccessException e) {
                LOGGER.log(Level.WARNING, "Field conversion failed for " + field.getName(), e);
            }
        }
        return entity;
    }

    private void executeInsert(T entity, Connection conn) throws SQLException {
        Map<String, Object> columns = mapper.toColumnMap(entity);
        String colNames = String.join(", ", columns.keySet());
        String placeholders = columns.keySet().stream().map(c -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + tableName() + " (" + colNames + ") VALUES (" + placeholders + ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, columns.values());
            stmt.executeUpdate();
            putCache(entity);
        }
    }

    private ID executeInsertAndReturnId(T entity, Connection conn) throws SQLException {
        Map<String, Object> columns = mapper.toColumnMap(entity);
        columns.remove(idColumn());
        String colNames = String.join(", ", columns.keySet());
        String placeholders = columns.keySet().stream().map(c -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + tableName() + " (" + colNames + ") VALUES (" + placeholders + ")";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParams(stmt, columns.values());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Object generatedId = keys.getObject(1);

                    setGeneratedId(entity, generatedId);
                    putCache(entity);

                    return (ID) generatedId;
                } else {
                    throw new RepositoryException(ExceptionLevel.ERROR,"Failed to retrieve generated ID");
                }
            }
        }
    }

    private void executeUpdate(T entity, Connection conn) throws SQLException {
        Map<String, Object> columns = mapper.toColumnMap(entity);
        columns.remove(idColumn());
        String setClause = columns.keySet().stream().map(c -> c + " = ?").collect(Collectors.joining(", "));
        String sql = "UPDATE " + tableName() + " SET " + setClause + " WHERE " + idColumn() + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, columns.values());
            stmt.setObject(columns.size() + 1, getId(entity));
            stmt.executeUpdate();
            putCache(entity);
        }
    }

    private void setGeneratedId(T entity, Object idValue) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    field.set(entity, convertIdType(field.getType(), idValue));
                    return;
                } catch (IllegalAccessException e) {
                    throw new RepositoryException(ExceptionLevel.ERROR,"Failed to set generated ID", e);
                }
            }
        }
    }

    private Object convertIdType(Class<?> type, Object value) {
        if (type == Long.class || type == long.class) return ((Number) value).longValue();
        if (type == Integer.class || type == int.class) return ((Number) value).intValue();
        return value;
    }

    private void putCache(T entity) {
        if (entity == null) return;
        ID id = getId(entity);
        long expireAt = ttlMillis.map(t -> System.currentTimeMillis() + t).orElse(Long.MAX_VALUE);
        cacheLRU.put(id, new CacheItem<>(entity, expireAt));
    }

    private Optional<T> getCache(ID id) {
        CacheItem<T> item = cacheLRU.get(id);
        if (item != null && !item.isExpired()) return Optional.of(item.value);
        cacheLRU.remove(id);
        return Optional.empty();
    }

    /** TTL cache wrapper */
    private static class CacheItem<T> {
        final T value;
        final long expireAt;

        CacheItem(T value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean isExpired() { return System.currentTimeMillis() > expireAt; }
    }

    // ---------------- TTL Cache Cleanup ---------------- //

    private final ScheduledExecutorService cacheCleaner = Executors.newSingleThreadScheduledExecutor();

    /**
     * Starts automatic cleanup of expired cache entries.
     * @param cleanupIntervalMillis interval in milliseconds between cleanup runs
     */
    protected void startCacheCleanup(long cleanupIntervalMillis) {
        cacheCleaner.scheduleAtFixedRate(this::cleanupCache, cleanupIntervalMillis, cleanupIntervalMillis, TimeUnit.MILLISECONDS);
    }

    /** Stops the cache cleanup thread. Call on repository shutdown. */
    protected void stopCacheCleanup() {
        cacheCleaner.shutdownNow();
    }

    /** Removes expired cache entries and logs them */
    private void cleanupCache() {
        synchronized (cacheLRU) {
            Iterator<Map.Entry<ID, CacheItem<T>>> it = cacheLRU.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ID, CacheItem<T>> entry = it.next();
                if (entry.getValue().isExpired()) {
                    it.remove();
                    LOGGER.fine("Cache expired and removed ID: " + entry.getKey());
                }
            }
        }
    }

    protected <R> R wrapSql(SqlSupplier<R> supplier) {
        try {
            return supplier.get();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected interface SqlSupplier<R> {
        R get() throws SQLException;
    }

    public <R> R execute(SqlFunction<Connection, R> action) {
        return wrapSql(() -> {
            try (var conn = databaseService.getConnection()) {
                return action.apply(conn);
            }
        });
    }

    @FunctionalInterface
    public interface SqlFunction<C, R> {
        R apply(C c) throws SQLException;
    }
}
