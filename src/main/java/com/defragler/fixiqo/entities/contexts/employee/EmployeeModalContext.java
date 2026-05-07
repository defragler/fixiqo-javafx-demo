package com.defragler.fixiqo.entities.contexts.employee;

import com.defragler.fixiqo.entities.*;
import java.util.function.*;

public class EmployeeModalContext {

    private final User user;
    private final boolean editMode;
    private final Consumer<User> onSave;
    private final Runnable onClose;

    public EmployeeModalContext(User user, boolean editMode, Consumer<User> onSave, Runnable onClose) {
        this.user = user;
        this.editMode = editMode;
        this.onSave = onSave;
        this.onClose = onClose;
    }

    public User getUser() { return user; }
    public boolean isEditMode() { return editMode; }
    public Consumer<User> getOnSave() { return onSave; }
    public Runnable getOnClose() { return onClose; }
}