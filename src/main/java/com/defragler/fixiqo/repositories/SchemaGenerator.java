package com.defragler.fixiqo.repositories;

import com.defragler.fixiqo.annotaions.*;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;

public class SchemaGenerator {
    public static String generateCreateTable(Class<?> entityClass) {

        EntityMetadata meta = EntityMetadata.of(entityClass);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ")
              .append(meta.getTableName())
              .append(" (\n");

        List<String> columnDefs = new ArrayList<>();

        for (Map.Entry<String, Field> entry : meta.getColumns().entrySet()) {

            String columnName = entry.getKey();
            Field field = entry.getValue();

            StringBuilder colDef = new StringBuilder();
            colDef.append(columnName).append(" ");

            colDef.append(resolveSqlType(field.getType()));

            if (field.equals(meta.getIdField())) {
                colDef.append(" PRIMARY KEY");

                Id id = field.getAnnotation(Id.class);

                if (id != null && id.autoIncrement()) {
                    colDef.append(" AUTOINCREMENT");
                }
            }

            Column column = field.getAnnotation(Column.class);

            if (column != null) {

                if (!column.nullable()) {
                    colDef.append(" NOT NULL");
                }

                if (column.unique()) {
                    colDef.append(" UNIQUE");
                }

                if (!column.defaultValue().isEmpty()) {
                    colDef.append(" DEFAULT ").append(column.defaultValue());
                }
            }

            columnDefs.add(colDef.toString());
        }

        for (Map.Entry<String, ForeignKey> fkEntry : meta.getForeignKeys().entrySet()) {

            String column = fkEntry.getKey();
            ForeignKey fk = fkEntry.getValue();

            EntityMetadata targetMeta = EntityMetadata.of(fk.target());

            StringBuilder fkSql = new StringBuilder();
            fkSql.append("FOREIGN KEY(")
                  .append(column)
                  .append(") REFERENCES ")
                  .append(targetMeta.getTableName())
                  .append("(")
                  .append(fk.column())
                  .append(")");

            if (fk.onDeleteCascade()) {
                fkSql.append(" ON DELETE CASCADE");
            }

            columnDefs.add(fkSql.toString());
        }

        sql.append(String.join(",\n", columnDefs));
        sql.append("\n);");

        return sql.toString();
    }

    public static List<String> generateIndexes(Class<?> entityClass) {

        List<String> sqlList = new ArrayList<>();
        EntityMetadata meta = EntityMetadata.of(entityClass);

        // --- INDEXES ---
        Indexes indexes = entityClass.getAnnotation(Indexes.class);
        if (indexes != null) {
            for (Index index : indexes.value()) {

                String indexName = index.name().isEmpty()
                      ? "idx_" + meta.getTableName() + "_" + String.join("_", index.columns())
                      : index.name();

                String sql = "CREATE " +
                      (index.unique() ? "UNIQUE " : "") +
                      "INDEX IF NOT EXISTS " +
                      indexName +
                      " ON " +
                      meta.getTableName() +
                      "(" + String.join(", ", index.columns()) + ")";

                sqlList.add(sql);
            }
        }

        // --- UNIQUE CONSTRAINTS ---
        UniqueConstraints uniqueConstraints = entityClass.getAnnotation(UniqueConstraints.class);

        if (uniqueConstraints != null) {
            for (UniqueConstraint uc : uniqueConstraints.value()) {

                String name = "uc_" + meta.getTableName() + "_" + String.join("_", uc.columns());

                String sql = "CREATE UNIQUE INDEX IF NOT EXISTS " +
                      name +
                      " ON " +
                      meta.getTableName() +
                      "(" + String.join(", ", uc.columns()) + ")";

                sqlList.add(sql);
            }
        }

        return sqlList;
    }

    private static String resolveSqlType(Class<?> type) {

        if (type == String.class) return "TEXT";
        if (type == int.class || type == Integer.class) return "INTEGER";
        if (type == long.class || type == Long.class) return "INTEGER";
        if (type == boolean.class || type == Boolean.class) return "INTEGER";
        if (type == double.class || type == Double.class) return "REAL";
        if (type == LocalDateTime.class) return "INTEGER";

        return "TEXT";
    }
}
