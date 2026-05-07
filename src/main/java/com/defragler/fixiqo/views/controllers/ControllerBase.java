package com.defragler.fixiqo.views.controllers;

import javafx.fxml.*;

public class ControllerBase implements IInjectable {

    protected ApplicationContext context;

    @Override
    public final void inject(ApplicationContext context) {
        this.context = context;
        onInject();
    }

    /** Optional: called after FXML initialize() */
    protected void onInit() { }

    /** Optional: called right after inject() */
    protected void onInject() { }

    /** JavaFX lifecycle */
    @FXML
    private void initialize() {
        onInit();
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }
}
