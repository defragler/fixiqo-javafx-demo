package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.services.enums.ThemeMode;
import javafx.stage.*;

/**
 * Service responsible for managing user session lifecycle.
 *
 * <p>Provides functionality for:
 * <ul>
 *     <li>Persisting session to storage</li>
 *     <li>Restoring session on application startup</li>
 *     <li>Handling logout flow</li>
 * </ul>
 *
 * <p>Session data is stored in JSON format.</p>
 */
public interface ISessionService {

    /**
     * Persists current user session.
     */
    void saveSession();

    /**
     * Attempts to restore session from storage.
     *
     * @return {@code true} if session was restored successfully, {@code false} otherwise
     */
    boolean tryAutoLogin();

    /**
     * Clears persisted session data.
     */
    void clearSession();

    /**
     * Performs logout and redirects to login window.
     *
     * @param stage current application stage
     */
    void logout(Stage stage);

    /**
     * Checks whether a persisted session exists.
     *
     * @return {@code true} if session file exists and is not empty
     */
    boolean hasSession();

    ThemeMode getTheme();

    void setTheme(ThemeMode mode);
}