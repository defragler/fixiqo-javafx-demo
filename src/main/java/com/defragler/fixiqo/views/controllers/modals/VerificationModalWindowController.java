package com.defragler.fixiqo.views.controllers.modals;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.contexts.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.views.controllers.*;

import javafx.util.*;
import java.util.function.*;
import javafx.animation.*;
import javafx.fxml.*;
import javafx.scene.control.*;

public class VerificationModalWindowController extends ControllerBase implements IModal {
    @FXML
    private TextField codeField;
    @FXML
    private Hyperlink resendLink;

    private IVerificationService verificationService;
    private IRegistrationService registrationService;

    private VerificationContext contextData;
    private Consumer<User> onSuccess;

    @Override
    protected void onInit() {
        verificationService = context.get(IVerificationService.class);
        registrationService = context.get(IRegistrationService.class);
    }

    @Override
    public void onOpen(Object parameter) {
        Object[] data = (Object[]) parameter;
        this.contextData = (VerificationContext) data[0];
        this.onSuccess   = (Consumer<User>) data[1];

        startResendCooldown(60);
    }
    
    @FXML
    private void confirm() {
        String code = codeField.getText();
        boolean valid = verificationService.verifyCode(contextData.getEmail(), code, VerificationType.EMAIL);

        if (!valid) {
            System.out.println("Invalid code");
            return;
        }

        try {
            var currentUser = registrationService.register(
                  contextData.getUsername(),
                  contextData.getPassword(),
                  contextData.getEmail(),
                  1,
                  null
            );

            System.out.println("Registration success");

            if (onSuccess != null) {
                onSuccess.accept(currentUser);
            }

        } catch (ControllerException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void resendCode() {
        boolean sent = verificationService.resendCode(
              contextData.getEmail(),
              VerificationType.EMAIL
        );

        if (!sent) {
            System.out.println("Cooldown not finished yet");
            return;
        }

        startResendCooldown(60);
    }

    private void startResendCooldown(int seconds) {
        resendLink.setDisable(true);

        Timeline timeline = new Timeline();

        for (int i = seconds; i >= 0; i--) {
            int timeLeft = i;

            KeyFrame keyFrame = new KeyFrame(
                  Duration.seconds(seconds - i),
                  event -> resendLink.setText("Resend in " + timeLeft + "s")
            );

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setOnFinished(event -> {
            resendLink.setDisable(false);
            resendLink.setText("Resend code");
        });

        timeline.play();
    }
}
