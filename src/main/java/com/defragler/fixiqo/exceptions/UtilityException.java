package com.defragler.fixiqo.exceptions;

import static com.defragler.fixiqo.utilities.ExceptionCatcher.*;

import com.defragler.fixiqo.exceptions.enums.*;

public class UtilityException extends ApplicationException {
    public UtilityException(ExceptionLevel level, String message) {
        super(LayerType.UTILITY, level, resolveCallerClass(), message);
    }

    public UtilityException(ExceptionLevel level, String message, Throwable cause) {
        super(LayerType.UTILITY, level, resolveCallerClass(), message, cause);
    }
}
