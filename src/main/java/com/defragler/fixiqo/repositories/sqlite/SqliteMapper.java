package com.defragler.fixiqo.repositories.sqlite;

import java.sql.*;
import java.util.*;

/**
 * Generic mapper interface to convert between SQLite ResultSet rows and domain entities.
 *
 * @param <T> the type of entity this mapper converts
 */
public interface SqliteMapper<T> {

    /**
     * Converts a single ResultSet row into a domain entity.
     *
     * @param rs the ResultSet positioned at the current row
     * @return fully constructed entity object
     * @throws SQLException if database access fails
     */
    T fromResultSet(ResultSet rs) throws SQLException;

    /**
     * Converts an entity into a map of column names to values for database insertion or update.
     *
     * @param entity the entity to convert
     * @return map of column names to values
     */
    Map<String, Object> toColumnMap(T entity);
}
