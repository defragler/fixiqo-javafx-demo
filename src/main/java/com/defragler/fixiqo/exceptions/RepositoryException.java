package com.defragler.fixiqo.exceptions;

import static com.defragler.fixiqo.utilities.ExceptionCatcher.*;

import com.defragler.fixiqo.exceptions.enums.*;

public class RepositoryException extends ApplicationException {
    public RepositoryException(ExceptionLevel level, String message) {
        super(LayerType.REPOSITORY, level, resolveCallerClass(), message);
    }

    public RepositoryException(ExceptionLevel level, String message, Throwable cause) {
        super(LayerType.REPOSITORY, level, resolveCallerClass(), message, cause);
    }
}
