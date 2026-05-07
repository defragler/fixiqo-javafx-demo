package com.defragler.fixiqo.repositories;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Metadata extractor for entities: table name, columns, primary key, foreign keys,
 * unique constraints, and indexes.
 */
public class EntityMetadata {

    private static final Map<Class<?>, EntityMetadata> CACHE = new HashMap<>();

    private final String tableName;
    private final Field idField;
    private final Map<String, Field> columns;
    private final Map<String, ForeignKey> foreignKeys;
    private final List<UniqueConstraint> uniqueConstraints;
    private final List<Index> indexes;

    private EntityMetadata(Class<?> type) {
        Table table = type.getAnnotation(Table.class);
        if (table == null) {
            throw new RepositoryException(ExceptionLevel.ERROR,"Missing @Table annotation on " + type.getSimpleName());
        }
        
        this.tableName = table.name();
        this.columns = new LinkedHashMap<>();
        this.foreignKeys = new HashMap<>();
        this.uniqueConstraints = new ArrayList<>();
        this.indexes = new ArrayList<>();

        Field id = null;

        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(NotMapped.class)) continue;
            field.setAccessible(true);

            // Primary Key
            if (field.isAnnotationPresent(Id.class)) {
                id = field;
            }

            // Columns
            Column column = field.getAnnotation(Column.class);
            String columnName = column != null ? column.name() : field.getName();
            columns.put(columnName, field);

            // Foreign Keys
            if (field.isAnnotationPresent(ForeignKey.class)) {
                foreignKeys.put(columnName, field.getAnnotation(ForeignKey.class));
            }
        }

        this.idField = id;

        // UniqueConstraints (class-level)
        UniqueConstraint uc = type.getAnnotation(UniqueConstraint.class);
        UniqueConstraints ucs = type.getAnnotation(UniqueConstraints.class);
        if (uc != null) uniqueConstraints.add(uc);
        if (ucs != null) Collections.addAll(uniqueConstraints, ucs.value());

        // Indexes (class-level)
        Index idx = type.getAnnotation(Index.class);
        Indexes idxs = type.getAnnotation(Indexes.class);
        if (idx != null) indexes.add(idx);
        if (idxs != null) Collections.addAll(indexes, idxs.value());
    }

    public static EntityMetadata of(Class<?> type) {
        return CACHE.computeIfAbsent(type, EntityMetadata::new);
    }

    public String getTableName() {
        return tableName;
    }

    public Field getIdField() {
        return idField;
    }

    public Map<String, Field> getColumns() {
        return columns;
    }

    public Map<String, ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }

    public List<Index> getIndexes() {
        return indexes;
    }
}
