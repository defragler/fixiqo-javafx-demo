package com.defragler.fixiqo.views.controllers.windows;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.contexts.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.*;
import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.views.controllers.*;
import com.defragler.fixiqo.views.windows.*;

import java.io.*;
import java.util.function.*;
import javafx.util.*;
import javafx.fxml.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.*;

public class RegistrationWindowController extends ControllerBase {
    @FXML
    private StackPane root;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button signUpButton;
    
    private IModalService modalService;
    private IValidationService validationService;
    private IVerificationService verificationService;

    private PauseTransition errorTimer;
    private FadeTransition fadeIn;
    private FadeTransition fadeOut;

    @Override
    protected void onInit() {
        if (root == null) {
            throw new ControllerException(ExceptionLevel.WARNING,"root was not injected from FXML");
        }
        
        modalService = new ModalService(root, new ControllerFactory(context));
        verificationService = context.get(IVerificationService.class);
        validationService = context.get(IValidationService.class);

        fieldsValidation();
        errorAnimations();
        hideErrorImmediate();
    }

    @FXML
    private void openLoginWindow() {
        Stage stage = (Stage) root.getScene().getWindow();

        try {
            new LoginWindow(context).start(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openVerificationModal() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();

            validationService.validateLogin(username);
            validationService.validatePassword(password);
            validationService.validateEmail(email);

            verificationService.sendCode(email, VerificationType.EMAIL);

            VerificationContext contextData =
                  new VerificationContext(username, password, email);

            modalService.show(
                  "/fxml/modals/verification-modal-window.fxml",
                  new Object[]{
                        contextData,
                        (Consumer<User>) user -> {
                            context.setCurrentUser(user);
                            modalService.closeAll();

                            Stage stage = (Stage) root.getScene().getWindow();
                            try {
                                new MainWindow(context).start(stage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                  }
            );

        } catch (ServiceException e) {
            showError(e.getUiMessage());
        }
    }

    @FXML
    private void openInformationModal() {
        modalService.show("/fxml/modals/information-modal-window.fxml");
    }

    @FXML
    private void openSettingsModal() {
        modalService.show("/fxml/modals/settings-modal-window.fxml");
    }

    private void fieldsValidation() {
        signUpButton.disableProperty().bind(
              usernameField.textProperty().isEmpty()
                    .or(passwordField.textProperty().isEmpty())
                    .or(emailField.textProperty().isEmpty())
        );

        usernameField.textProperty().addListener((obs, o, n) -> hideError());
        passwordField.textProperty().addListener((obs, o, n) -> hideError());
        emailField.textProperty().addListener((obs, o, n) -> hideError());
    }

    // HELPERS
    private void showError(String message) {
        errorLabel.setText(message);

        if (!errorLabel.isVisible()) {
            errorLabel.setVisible(true);
            fadeIn.playFromStart();
        }

        VBox usernameBlock = (VBox) usernameField.getParent();
        VBox passwordBlock = (VBox) passwordField.getParent();
        VBox emailBlock = (VBox) emailField.getParent();
        playShake(usernameBlock);
        playShake(passwordBlock);
        playShake(emailBlock);

        if (errorTimer != null) {
            errorTimer.stop();
        }

        errorTimer = new PauseTransition(Duration.seconds(5));
        errorTimer.setOnFinished(e -> hideError());
        errorTimer.play();
    }

    private void hideError() {
        if (!errorLabel.isVisible()) return;

        fadeOut.playFromStart();

        if (errorTimer != null) {
            errorTimer.stop();
        }
    }

    private void hideErrorImmediate() {
        errorLabel.setVisible(false);
        errorLabel.setOpacity(0);
    }

    private void errorAnimations() {
        fadeIn = new FadeTransition(Duration.millis(200), errorLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeOut = new FadeTransition(Duration.millis(200), errorLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> errorLabel.setVisible(false));
    }

    private void playShake(Node node) {
        Timeline timeline = new Timeline(
              new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), 0)),

              new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), -10)),
              new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), 10)),
              new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), -10)),
              new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 10)),
              new KeyFrame(Duration.millis(250), new KeyValue(node.translateXProperty(), -5)),
              new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), 5)),

              new KeyFrame(Duration.millis(350), new KeyValue(node.translateXProperty(), 0))
        );

        timeline.play();
    }
}
