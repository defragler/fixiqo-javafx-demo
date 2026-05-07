package com.defragler.fixiqo.services.interfaces;

import javafx.scene.*;

/**
 * Service for displaying modal overlays above the main application UI.
 *
 * <p>Modals are rendered inside a root StackPane as overlay nodes,
 * blocking interaction with underlying content.</p>
 *
 * <p>Supports:
 * <ul>
 *     <li>Showing modal windows from FXML</li>
 *     <li>Passing parameters to controllers</li>
 *     <li>Closing specific or top-most modals</li>
 *     <li>Stacked modal navigation</li>
 * </ul>
 * </p>
 */
public interface IModalService {

    /**
     * Shows a modal window.
     *
     * @param fxmlPath path to FXML file (e.g. "/fxml/modals/code-modal.fxml")
     */
    void show(String fxmlPath);

    /**
     * Shows a modal window with a parameter passed to controller.
     *
     * @param fxmlPath path to FXML file
     * @param parameter parameter passed to controller (may be null)
     */
    void show(String fxmlPath, Object parameter);

    void show(String fxmlPath, Object parameter, Runnable onClose);

    /**
     * Closes the top-most modal window.
     */
    void closeTop();

    /**
     * Closes a specific modal node.
     *
     * @param modal modal node to remove
     */
    void close(Node modal);

    /**
     * Closes all currently opened modals.
     */
    void closeAll();
}