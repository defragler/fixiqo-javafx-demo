package com.defragler.fixiqo.views.windows;

import com.defragler.fixiqo.views.controllers.*;

import java.io.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;

public class MainWindow {
    private final ApplicationContext context;

    public MainWindow(ApplicationContext context) {
        this.context = context;
    }
    
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/fxml/windows/main-window.fxml")
        );

        loader.setControllerFactory(new ControllerFactory(context));

        Scene scene = new Scene(loader.load());
        // TODO: Centralize style loading in base class.
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("Main Window - Fixiqo");
        stage.show();
    }
}
