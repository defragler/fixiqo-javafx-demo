package com.defragler.fixiqo.services;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.io.*;
import java.util.*;
import javafx.beans.property.*;

/**
 * Implementation of a localization service that supports dynamic locale switching,
 * binding for JavaFX UI elements, and XML-based resource bundles.
 */
public class LocalizationService implements ILocalizationService {

    private final Map<String, StringProperty> keyBindings = new HashMap<>();
    private final Map<Locale, Properties> localeProperties = new HashMap<>();
    private Locale currentLocale;

    /**
     * Creates a LocalizationService with a default locale.
     *
     * @param defaultLocale the initial locale
     * @param locales       map of supported locales to XML resource files
     * @throws IOException  if any resource file cannot be loaded
     */
    public LocalizationService(Locale defaultLocale, Map<Locale, File> locales) throws IOException {
        for (Map.Entry<Locale, File> entry : locales.entrySet()) {
            Properties props = new Properties();
            try (InputStream is = new FileInputStream(entry.getValue())) {
                props.loadFromXML(is);
            }
            localeProperties.put(entry.getKey(), props);
        }
        setLocale(defaultLocale);
    }

    /**
     * Returns a StringProperty that updates automatically when the locale changes.
     * Can be bound directly to JavaFX UI elements.
     *
     * @param key the localization key
     * @return StringProperty representing the localized value
     */
    @Override
    public StringProperty getStringProperty(String key) {
        return keyBindings.computeIfAbsent(key, k -> new SimpleStringProperty(get(k)));
    }

    /**
     * Returns the localized text for the given key in the current locale.
     *
     * @param key the localization key
     * @return localized text
     */
    @Override
    public String get(String key) {
        Properties props = localeProperties.get(currentLocale);
        if (props == null) return key;
        return props.getProperty(key, key);
    }

    /**
     * Sets the current locale and updates all bound StringProperties automatically.
     *
     * @param locale the new locale
     */
    @Override
    public void setLocale(Locale locale) {
        if (!localeProperties.containsKey(locale)) {
            throw new ServiceException(ExceptionLevel.ERROR,"Locale not supported: " + locale);
        }
        currentLocale = locale;

        for (Map.Entry<String, StringProperty> entry : keyBindings.entrySet()) {
            entry.getValue().set(get(entry.getKey()));
        }
    }

    /**
     * Returns the currently active locale.
     *
     * @return current Locale
     */
    @Override
    public Locale getLocale() {
        return currentLocale;
    }

    /**
     * Updates the underlying XML resource file for a given locale and key.
     * This can be used to dynamically add missing keys or modify values.
     *
     * @param locale the locale to update
     * @param key    the key to update
     * @param value  the new localized value
     * @throws IOException if the resource file cannot be saved
     */
    public void updateLocaleFile(Locale locale, String key, String value) throws IOException {
        Properties props = localeProperties.get(locale);
        if (props == null) throw new ServiceException(ExceptionLevel.ERROR,"Locale not supported: " + locale);

        props.setProperty(key, value);

        // Save back to XML file (assuming original path is known)
        File file = null;
        for (Map.Entry<Locale, File> entry : originalFiles.entrySet()) {
            if (entry.getKey().equals(locale)) {
                file = entry.getValue();
                break;
            }
        }
        if (file == null) throw new ServiceException(ExceptionLevel.ERROR,"Original file not found for locale: " + locale);

        try (OutputStream os = new FileOutputStream(file)) {
            props.storeToXML(os, "Updated localization key: " + key);
        }
    }

    // Optional: keep original files mapping for updateLocaleFile
    private final Map<Locale, File> originalFiles = new HashMap<>();

    public void registerOriginalFile(Locale locale, File file) {
        originalFiles.put(locale, file);
    }
}
