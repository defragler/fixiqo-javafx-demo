package com.defragler.fixiqo.exceptions;

import com.defragler.fixiqo.exceptions.enums.*;

public abstract class ApplicationException extends RuntimeException {
    private final LayerType layerType;
    private final ExceptionLevel level;
    private final String source;

    public ApplicationException(
          LayerType layerType,
          ExceptionLevel level,
          String source,
          String message
    ) {
        super(message);
        this.layerType = layerType;
        this.level = level;
        this.source = source;
    }

    public ApplicationException(
          LayerType layerType,
          ExceptionLevel level,
          String source,
          String message,
          Throwable cause
    ) {
        super(message, cause);
        this.layerType = layerType;
        this.level = level;
        this.source = source;
    }

    public LayerType getLayerType() {
        return layerType;
    }

    public ExceptionLevel getLevel() {
        return level;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String getMessage() {
        return "[" + level + "] - [" + layerType + ":" + source + "]: " + super.getMessage() + ".";
    }

    public String getUiMessage() {
        return super.getMessage();
    }
}
