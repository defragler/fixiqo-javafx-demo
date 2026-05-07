package com.defragler.fixiqo.entities.contexts.request;

import com.defragler.fixiqo.entities.*;
import java.util.function.*;

public class RequestModalContext {

    private final Request request;
    private final boolean editMode;
    private final Consumer<Request> onSave;
    private final Runnable onClose;

    public RequestModalContext(
          Request request,
          boolean editMode,
          Consumer<Request> onSave,
          Runnable onClose
    ) {
        this.request = request;
        this.editMode = editMode;
        this.onSave = onSave;
        this.onClose = onClose;
    }

    public Request getRequest() { return request; }
    public boolean isEditMode() { return editMode; }
    public Consumer<Request> getOnSave() { return onSave; }
    public Runnable getOnClose() { return onClose; }
}
