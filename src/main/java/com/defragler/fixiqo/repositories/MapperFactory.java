package com.defragler.fixiqo.repositories;

import com.defragler.fixiqo.repositories.sqlite.*;

import java.util.*;
import java.util.concurrent.*;

public class MapperFactory {
    private static final Map<Class<?>, SqliteMapper<?>> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> SqliteMapper<T> getMapper(Class<T> type) {

        return (SqliteMapper<T>) CACHE.computeIfAbsent(
              type,
              ReflectionMapper::new
        );
    }
}
