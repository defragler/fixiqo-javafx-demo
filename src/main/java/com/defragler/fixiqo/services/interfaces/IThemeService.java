package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.services.enums.*;

/**
 * Service responsible for managing application theme.
 *
 * <p>Provides functionality for:
 * <ul>
 *     <li>Switching between theme modes (light / dark / auto)</li>
 *     <li>Persisting user theme preference</li>
 *     <li>Applying theme to JavaFX application</li>
 *     <li>Reacting to OS theme changes in AUTO mode</li>
 * </ul>
 *
 * <p>Implementations must ensure that theme changes are applied
 * immediately and consistently across the application.</p>
 */
public interface IThemeService {

    /**
     * Sets current theme mode.
     *
     * <p>This method:
     * <ul>
     *     <li>Applies the theme immediately</li>
     *     <li>Persists the selection</li>
     *     <li>Registers/unregisters OS listeners if needed</li>
     * </ul></p>
     *
     * @param mode selected theme mode
     */
    void setTheme(ThemeMode mode);

    /**
     * Returns currently selected theme mode.
     *
     * @return current theme mode
     */
    ThemeMode getCurrentTheme();

    /**
     * Applies the current theme without changing it.
     *
     * <p>Useful on application startup.</p>
     */
    void applyCurrentTheme();

    /**
     * Loads persisted theme from storage.
     *
     * <p>If no data exists, defaults to {@link ThemeMode#AUTO}.</p>
     */
    void loadTheme();
}