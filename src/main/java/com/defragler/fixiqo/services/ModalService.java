package com.defragler.fixiqo.services;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.views.controllers.*;

import java.io.*;
import java.util.*;
import javafx.util.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.animation.*;

/**
 * Default implementation of {@link IModalService}.
 *
 * <p>Responsible for rendering modal overlays above the main application UI.</p>
 *
 * <p><strong>Designed for Dependency Injection (DI):</strong>
 * <ul>
 *     <li>Requires a root {@link StackPane} as rendering container</li>
 *     <li>Uses a {@link Callback} controller factory to enable DI in modal controllers</li>
 *     <li>Does not instantiate controllers manually</li>
 * </ul></p>
 *
 * <p>Each modal consists of:
 * <ul>
 *     <li>Backdrop (semi-transparent overlay)</li>
 *     <li>Content (loaded from FXML)</li>
 * </ul>
 * </p>
 *
 * <p>Supports stacking multiple modals and animated transitions.</p>
 */
public class ModalService implements IModalService {

    private final StackPane root;
    private final Callback<Class<?>, Object> controllerFactory;
    private final Deque<Node> modalStack = new ArrayDeque<>();

    /**
     * Constructs ModalService with DI-compatible dependencies.
     *
     * @param root root {@link StackPane} used as overlay container
     * @param controllerFactory factory for creating controllers (supports DI)
     */
    public ModalService(StackPane root, Callback<Class<?>, Object> controllerFactory) {

        if (root == null) {
            throw new ServiceException(ExceptionLevel.ERROR,"Root StackPane cannot be null");
        }
        if (controllerFactory == null) {
            throw new ServiceException(ExceptionLevel.ERROR,"ControllerFactory cannot be null");
        }

        this.root = root;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show(String fxmlPath) {
        show(fxmlPath, null);
    }

    @Override
    public void show(String fxmlPath, Object parameter) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(controllerFactory);
            Node content = loader.load();

            Object controller = loader.getController();
            if (controller instanceof IModal modalController) {
                modalController.onOpen(parameter);
            }

            Node overlay = createOverlay(content);

            modalStack.push(overlay);
            root.getChildren().add(overlay);

            playShowAnimation(overlay);

        } catch (IOException e) {
            throw new ServiceException(ExceptionLevel.ERROR,"Failed to load modal: " + fxmlPath, e);
        }
    }

    @Override
    public void show(String fxmlPath, Object parameter, Runnable onClose) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(controllerFactory);
            Node content = loader.load();

            Object controller = loader.getController();
            if (controller instanceof IModal modalController) {
                modalController.onOpen(parameter);
            }

            Node overlay = createOverlay(content);

            modalStack.push(overlay);
            root.getChildren().add(overlay);

            playShowAnimation(overlay);

            overlay.setUserData(onClose);

        } catch (IOException e) {
            throw new ServiceException(ExceptionLevel.ERROR,"Failed to load modal: " + fxmlPath, e);
        }
    }

    @Override
    public void closeTop() {
        if (modalStack.isEmpty()) return;

        Node modal = modalStack.pop();
        Runnable onClose = (Runnable) modal.getUserData();

        playHideAnimation(modal, () -> {
            root.getChildren().remove(modal);

            if (onClose != null) {
                onClose.run();
            }
        });
    }

    @Override
    public void close(Node modal) {
        if (modal == null) return;

        if (modalStack.remove(modal)) {
            playHideAnimation(modal, () -> root.getChildren().remove(modal));
        }
    }

    @Override
    public void closeAll() {
        while (!modalStack.isEmpty()) {
            Node modal = modalStack.pop();
            root.getChildren().remove(modal);
        }
    }

    /**
     * Creates a modal overlay node consisting of a backdrop and modal content.
     *
     * @param content modal content loaded from FXML
     * @return overlay node containing backdrop + content
     */
    private Node createOverlay(Node content) {
        StackPane overlay = new StackPane();

        Pane backdrop = new Pane();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        backdrop.setOnMouseClicked(e -> closeTop());

        backdrop.setOpacity(0);
        content.setOpacity(0);
        content.setScaleX(0.9);
        content.setScaleY(0.9);

        overlay.getChildren().addAll(backdrop, content);

        return overlay;
    }

    /**
     * Plays show animation for modal overlay.
     *
     * @param modal modal node to animate
     */
    private void playShowAnimation(Node modal) {
        if (!(modal instanceof Parent parent) || parent.getChildrenUnmodifiable().size() < 2) return;

        Node backdrop = parent.getChildrenUnmodifiable().get(0);
        Node content  = parent.getChildrenUnmodifiable().get(1);
        
        // Background Animation
        FadeTransition fadeBackdrop = new FadeTransition(Duration.millis(150), backdrop);
        fadeBackdrop.setFromValue(0);
        fadeBackdrop.setToValue(1);
        fadeBackdrop.play();
        
        // Content Animation
        FadeTransition fadeContent = new FadeTransition(Duration.millis(200), content);
        fadeContent.setFromValue(0);
        fadeContent.setToValue(1);

        ScaleTransition scaleContent = new ScaleTransition(Duration.millis(200), content);
        scaleContent.setFromX(0.9);
        scaleContent.setFromY(0.9);
        scaleContent.setToX(1);
        scaleContent.setToY(1);

        ParallelTransition modalTransition = new ParallelTransition(fadeContent, scaleContent);

        PauseTransition delay = new PauseTransition(Duration.millis(50));
        SequentialTransition seq = new SequentialTransition(delay, modalTransition);
        seq.play();
    }

    /**
     * Plays hide animation for modal overlay.
     *
     * @param modal modal node to animate
     * @param onFinished callback executed after animation completes
     */
    private void playHideAnimation(Node modal, Runnable onFinished) {
        if (!(modal instanceof Parent parent) || parent.getChildrenUnmodifiable().size() < 2) {
            onFinished.run();
            return;
        }

        Node backdrop = parent.getChildrenUnmodifiable().get(0);
        Node content  = parent.getChildrenUnmodifiable().get(1);

        // Background Animation
        FadeTransition fadeBackdrop = new FadeTransition(Duration.millis(150), backdrop);
        fadeBackdrop.setFromValue(backdrop.getOpacity());
        fadeBackdrop.setToValue(0);

        // Content Animation
        FadeTransition fadeContent = new FadeTransition(Duration.millis(150), content);
        fadeContent.setFromValue(content.getOpacity());
        fadeContent.setToValue(0);

        ScaleTransition scaleContent = new ScaleTransition(Duration.millis(150), content);
        scaleContent.setFromX(content.getScaleX());
        scaleContent.setFromY(content.getScaleY());
        scaleContent.setToX(0.9);
        scaleContent.setToY(0.9);

        ParallelTransition modalTransition = new ParallelTransition(fadeContent, scaleContent);

        ParallelTransition all = new ParallelTransition(fadeBackdrop, modalTransition);
        all.setOnFinished(e -> onFinished.run());
        all.play();
    }
}