package com.defragler.fixiqo.views.controllers;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

import java.lang.reflect.*;
import javafx.util.*;

public class ControllerFactory implements Callback<Class<?>, Object> {

    private final ApplicationContext context;

    public ControllerFactory(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object call(Class<?> type) {
        try {
            Object controller = type.getDeclaredConstructor().newInstance();

            if (controller instanceof IInjectable injectable) {
                injectable.inject(context);
            }

            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object value = context.get(field.getType());
                    field.setAccessible(true);
                    field.set(controller, value);
                }
            }

            return controller;
        } catch (Exception e) {
            throw new ControllerException(ExceptionLevel.ERROR,"Failed to create controller: " + type.getName(), e);
        }
    }
}
