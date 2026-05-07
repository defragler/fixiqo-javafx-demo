package com.defragler.fixiqo.entities.contexts;

import com.defragler.fixiqo.entities.*;
import java.util.function.*;

public class AccountContext {

    private final User user;
    private final Consumer<User> onSave;
    private final Runnable onClose;

    public AccountContext(
          User user,
          Consumer<User> onSave,
          Runnable onClose
    ) {
        this.user = user;
        this.onSave = onSave;
        this.onClose = onClose;
    }

    public User getUser() { return user; }
    public Consumer<User> getOnSave() { return onSave; }
    public Runnable getOnClose() { return onClose; }
}