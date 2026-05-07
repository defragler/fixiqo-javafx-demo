package com.defragler.fixiqo.views.controllers;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

import java.util.*;
import java.util.function.*;
import java.lang.reflect.*;

public class ApplicationContext {
    private final Map<Class<?>, Supplier<?>> providers = new HashMap<>();
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private User currentUser;

    /** Register singleton (created once) */
    public <T> void registerSingleton(Class<T> type, Supplier<T> provider) {
        providers.put(type, () -> {

            if (singletons.containsKey(type)) {
                return singletons.get(type);
            }

            singletons.put(type, null);
            T instance = provider.get();
            singletons.put(type, instance);

            return instance;
        });
    }

    /** Register transient (new instance each time) */
    public <T> void registerTransient(Class<T> type, Supplier<T> provider) {
        providers.put(type, provider);
    }

    /** Resolve dependency */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        Supplier<?> provider = providers.get(type);
        if (provider == null) {
            return (T) createInstance(type);
        }
        return (T) provider.get();
    }

    private Object createInstance(Class<?> type) {
        if (type.isInterface()) {
            throw new ControllerException(ExceptionLevel.ERROR,"Cannot instantiate interface: " + type.getName());
        }

        try {
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            Object[] parameters = new Object[constructor.getParameterCount()];
            Class<?>[] parameterTypes = constructor.getParameterTypes();

            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = get(parameterTypes[i]);
            }

            constructor.setAccessible(true);
            Object instance = constructor.newInstance(parameters);

            for (var field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    field.set(instance, get(field.getType()));
                }
            }

            return instance;
        } catch (Exception e) {
            throw new ControllerException(ExceptionLevel.ERROR,"Failed to auto-instantiate " + type.getName(), e);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
