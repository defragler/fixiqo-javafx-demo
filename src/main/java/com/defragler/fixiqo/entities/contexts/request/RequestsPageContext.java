package com.defragler.fixiqo.entities.contexts.request;

import com.defragler.fixiqo.entities.*;
import java.util.function.*;

public class RequestsPageContext {

    private final Runnable onAdd;
    private final Consumer<Request> onEdit;
    private final Consumer<Request> onDelete;

    public RequestsPageContext(
          Runnable onAdd,
          Consumer<Request> onEdit,
          Consumer<Request> onDelete
    ) {
        this.onAdd = onAdd;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    public Runnable getOnAdd() { return onAdd; }
    public Consumer<Request> getOnEdit() { return onEdit; }
    public Consumer<Request> getOnDelete() { return onDelete; }
}
