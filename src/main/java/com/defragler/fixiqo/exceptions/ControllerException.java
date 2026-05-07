package com.defragler.fixiqo.exceptions;

import static com.defragler.fixiqo.utilities.ExceptionCatcher.*;

import com.defragler.fixiqo.exceptions.enums.*;

public class ControllerException extends ApplicationException {
    public ControllerException(ExceptionLevel level, String message) {
        super(LayerType.CONTROLLER, level, resolveCallerClass(), message);
    }

    public ControllerException(ExceptionLevel level, String message, Throwable cause) {
        super(LayerType.CONTROLLER, level, resolveCallerClass(), message, cause);
    }
}
