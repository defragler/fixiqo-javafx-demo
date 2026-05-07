package com.defragler.fixiqo.entities.contexts.client;

import com.defragler.fixiqo.entities.*;
import java.util.function.*;

public class ClientsPageContext {

    private final Runnable onAdd;
    private final Consumer<Client> onEdit;
    private final Consumer<Client> onDelete;

    public ClientsPageContext(
          Runnable onAdd,
          Consumer<Client> onEdit,
          Consumer<Client> onDelete
    ) {
        this.onAdd = onAdd;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    public Runnable getOnAdd() {
        return onAdd;
    }

    public Consumer<Client> getOnEdit() {
        return onEdit;
    }

    public Consumer<Client> getOnDelete() {
        return onDelete;
    }
}