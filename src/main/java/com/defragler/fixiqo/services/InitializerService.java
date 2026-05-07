package com.defragler.fixiqo.services;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.sql.*;
import java.util.*;
import java.lang.reflect.*;

import org.reflections.*;

public class InitializerService implements Initializer {

    private final IDatabaseService databaseService;
    private static final String BASE_PACKAGE = "com.defragler.fixiqo.entities";

    public InitializerService(IDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void initialize() {
        Reflections reflections = new Reflections(BASE_PACKAGE);
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Table.class);

        if (entities.isEmpty()) {
            System.out.println("No entities found for schema generation");
            return;
        }

        for (Class<?> entity : entities) {

            // Creating Table
            String createTableSql = SchemaGenerator.generateCreateTable(entity);
            runSql(createTableSql, "Create table " + entity.getSimpleName());

            // Creating Indexes
            for (String indexSql : SchemaGenerator.generateIndexes(entity)) {
                runSql(indexSql, "Create index for " + entity.getSimpleName());
            }

            // Generating Reference Table Data
            ReferenceTable refAnno = entity.getAnnotation(ReferenceTable.class);
            if (refAnno != null) {
                Class<? extends Enum<?>> enumClass = refAnno.values();
                Enum<?>[] enumConstants = enumClass.getEnumConstants();

                try {
                    Method getIdMethod = enumClass.getMethod("getId");

                    for (Enum<?> constant : enumConstants) {
                        Object id = getIdMethod.invoke(constant);
                        String code = constant.name();

                        String insertSql = String.format(
                              "INSERT OR IGNORE INTO %s (id, code) VALUES (%d, '%s')",
                              EntityMetadata.of(entity).getTableName(),
                              id,
                              code
                        );

                        runSql(insertSql, "Insert enum " + constant.name());
                    }

                } catch (NoSuchMethodException e) {
                    throw new ServiceException(ExceptionLevel.ERROR,
                          "Enum class " + enumClass.getName() + " must have a getId() method", e
                    );
                } catch (Exception e) {
                    throw new ServiceException(ExceptionLevel.ERROR,"Failed to populate reference table " + entity.getSimpleName(), e);
                }
            }
        }

        System.out.println("Schema initialized for " + entities.size() + " entities");
    }

    /**
     * Helper method to execute a SQL statement inside a transaction.
     */
    private void runSql(String sql, String description) {
        databaseService.runInTransaction(conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (Exception e) {
                throw new ServiceException(ExceptionLevel.ERROR,
                      "Failed to execute SQL for " + description, e
                );
            }
        });
    }
}
