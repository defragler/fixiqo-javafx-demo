package com.defragler.fixiqo.exceptions;

import static com.defragler.fixiqo.utilities.ExceptionCatcher.*;

import com.defragler.fixiqo.exceptions.enums.*;

public class ServiceException extends ApplicationException {
    public ServiceException(ExceptionLevel level, String message) {
        super(LayerType.SERVICE, level, resolveCallerClass(), message);
    }

    public ServiceException(ExceptionLevel level, String message, Throwable cause) {
        super(LayerType.SERVICE, level, resolveCallerClass(), message, cause);
    }
}
