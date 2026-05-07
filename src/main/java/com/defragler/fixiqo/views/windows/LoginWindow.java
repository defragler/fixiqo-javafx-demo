package com.defragler.fixiqo.views.windows;

import com.defragler.fixiqo.views.controllers.*;

import java.io.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;

public class LoginWindow {
    private final ApplicationContext context;

    public LoginWindow(ApplicationContext context) {
        this.context = context;
    }
    
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/fxml/windows/login-window.fxml")
        );

        loader.setControllerFactory(new ControllerFactory(context));

        Scene scene = new Scene(loader.load());
        // TODO: Centralize style loading in base class.
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.setTitle("Login Window - Fixiqo");
        stage.show();
    }
}
