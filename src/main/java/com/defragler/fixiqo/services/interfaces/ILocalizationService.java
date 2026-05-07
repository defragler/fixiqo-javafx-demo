package com.defragler.fixiqo.services.interfaces;

import java.util.*;
import javafx.beans.property.*;

/**
 * Interface for localization service in JavaFX applications.
 * Provides dynamic language switching and binding support for FXML elements.
 */
public interface ILocalizationService {
    /**
     * Returns a StringProperty for the given localization key.
     * The property automatically updates when the language changes.
     *
     * @param key the localization key
     * @return a StringProperty that can be bound in FXML
     */
    StringProperty getStringProperty(String key);

    /**
     * Returns the localized text for the given key.
     *
     * @param key the localization key
     * @return localized text
     */
    String get(String key);

    /**
     * Sets the current locale of the application.
     * All bound properties are automatically updated.
     *
     * @param locale the new locale
     */
    void setLocale(Locale locale);

    /**
     * Returns the current locale of the application.
     *
     * @return current locale
     */
    Locale getLocale();
}
