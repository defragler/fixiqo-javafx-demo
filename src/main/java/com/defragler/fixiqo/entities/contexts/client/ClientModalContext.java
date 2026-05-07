package com.defragler.fixiqo.entities.contexts.client;

import com.defragler.fixiqo.entities.*;
import java.util.function.*;

public class ClientModalContext {

    private final Client client;
    private final boolean editMode;
    private final Consumer<Client> onSave;
    private final Runnable onClose;

    public ClientModalContext(Client client, boolean editMode, Consumer<Client> onSave, Runnable onClose) {
        this.client = client;
        this.editMode = editMode;
        this.onSave = onSave;
        this.onClose = onClose;
    }

    public Client getClient() { return client; }
    public boolean isEditMode() { return editMode; }
    public Consumer<Client> getOnSave() { return onSave; }
    public Runnable getOnClose() { return onClose; }
}