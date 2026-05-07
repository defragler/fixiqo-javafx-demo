package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.exceptions.*;

import javafx.scene.*;

/**
 * Interface defining a professional navigation service for JavaFX applications.
 * <p>
 * Supports typed parameterized navigation, history management (back/forward), 
 * and reloading of pages.
 */
public interface INavigationService {

    /**
     * Navigates to a new page specified by the FXML path, passing an optional typed parameter.
     *
     * @param fxmlPath  the path to the FXML file representing the page
     * @param parameter optional typed parameter for the target page
     * @param <T>       type of the parameter
     * @throws ServiceException if the page cannot be loaded or navigated
     */
    <T> void navigateTo(String fxmlPath, T parameter);

    /**
     * Reloads the currently displayed page, creating a new controller instance.
     *
     * @throws ServiceException if there is no current page
     */
    void reloadCurrentPage();

    /**
     * Navigates back to the previous page in history.
     *
     * @throws ServiceException if there is no previous page in history
     */
    void goBack();

    /**
     * Navigates forward to the next page in history.
     *
     * @throws ServiceException if there is no forward page in history
     */
    void goForward();

    /**
     * Checks whether navigating back is possible.
     *
     * @return true if back navigation is possible, false otherwise
     */
    boolean canGoBack();

    /**
     * Checks whether navigating forward is possible.
     *
     * @return true if forward navigation is possible, false otherwise
     */
    boolean canGoForward();

    /**
     * Returns the currently displayed page node.
     *
     * @return current page node or null if no page is loaded
     */
    Node getCurrentPage();

    /**
     * Clears all navigation history and cached pages/controllers.
     */
    void clear();
}
