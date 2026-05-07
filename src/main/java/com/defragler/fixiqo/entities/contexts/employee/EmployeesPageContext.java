package com.defragler.fixiqo.entities.contexts.employee;

import com.defragler.fixiqo.entities.*;
import java.util.function.*;

public class EmployeesPageContext {

    private final Runnable onAdd;
    private final Consumer<User> onEdit;
    private final Consumer<User> onDelete;

    public EmployeesPageContext(
          Runnable onAdd,
          Consumer<User> onEdit,
          Consumer<User> onDelete
    ) {
        this.onAdd = onAdd;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    public Runnable getOnAdd() {
        return onAdd;
    }

    public Consumer<User> getOnEdit() {
        return onEdit;
    }

    public Consumer<User> getOnDelete() {
        return onDelete;
    }
}