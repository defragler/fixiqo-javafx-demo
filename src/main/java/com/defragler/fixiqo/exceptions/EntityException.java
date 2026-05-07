package com.defragler.fixiqo.exceptions;

import static com.defragler.fixiqo.utilities.ExceptionCatcher.*;

import com.defragler.fixiqo.exceptions.enums.*;

public class EntityException extends ApplicationException {
    public EntityException(ExceptionLevel level, String message) {
        super(LayerType.ENTITY, level, resolveCallerClass(), message);
    }

    public EntityException(ExceptionLevel level, String message, Throwable cause) {
        super(LayerType.ENTITY, level, resolveCallerClass(), message, cause);
    }
}
