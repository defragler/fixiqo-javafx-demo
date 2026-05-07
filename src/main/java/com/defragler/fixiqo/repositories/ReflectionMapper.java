package com.defragler.fixiqo.repositories;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class ReflectionMapper<T> implements SqliteMapper<T> {

    private final Class<T> type;
    private final EntityMetadata metadata;

    public ReflectionMapper(Class<T> type) {

        this.type = type;
        this.metadata = EntityMetadata.of(type);
    }

    @Override
    public T fromResultSet(ResultSet rs) throws SQLException {
        try {

            T instance = type.getDeclaredConstructor().newInstance();

            for (Map.Entry<String, Field> entry : metadata.getColumns().entrySet()) {

                Field field = entry.getValue();
                String column = entry.getKey();

                Object value;

                if (field.getType() == byte[].class) {
                    value = rs.getBytes(column);
                } else {
                    value = rs.getObject(column);
                }

                value = convertValue(value, field.getType());

                field.set(instance, value);
            }

            return instance;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException(ExceptionLevel.ERROR,"Mapping failed", e);
        }
    }

    @Override
    public Map<String, Object> toColumnMap(T entity) {

        try {

            Map<String, Object> map = new LinkedHashMap<>();

            for (Map.Entry<String, Field> entry : metadata.getColumns().entrySet()) {

                Object value = entry.getValue().get(entity);

                map.put(entry.getKey(), value);
            }

            return map;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {

        // Null fallback
        if (value == null) {
            if (targetType.isPrimitive()) {
                if (targetType == int.class) return 0;
                if (targetType == long.class) return 0L;
                if (targetType == double.class) return 0.0;
                if (targetType == boolean.class) return false;
            }
            return null;
        }
        
        // Boolean (SQLite: INTEGER → Boolean)
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Integer intValue) {
                return intValue != 0;
            }
            if (value instanceof Long longValue) {
                return longValue != 0;
            }
        }

        // Integer
        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number num) {
                return num.intValue();
            }
        }

        // Long
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number num) {
                return num.longValue();
            }
        }

        // Double
        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number num) {
                return num.doubleValue();
            }
        }

        // Enum
        if (targetType.isEnum()) {
            if (value instanceof Number num) {
                int id = num.intValue();

                for (Object constant : targetType.getEnumConstants()) {
                    try {
                        Method method = targetType.getMethod("getId");
                        int enumId = (int) method.invoke(constant);

                        if (enumId == id) {
                            return constant;
                        }
                    } catch (Exception ignored) {}
                }
            }

            return Enum.valueOf((Class<Enum>) targetType, value.toString());
        }

        // Fallback
        if (!targetType.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(
                  "Cannot map value of type " + value.getClass() + " to " + targetType
            );
        }
        return value;
    }
}
