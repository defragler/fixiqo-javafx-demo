package com.defragler.fixiqo.utilities;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.lang.reflect.*;
import javafx.scene.control.*;
import javafx.scene.text.*;

/**
 * Utility class to automatically bind annotated JavaFX fields to localization properties.
 * <p>
 * This class is final and cannot be extended.
 */
public final class LocalizationBinder {

    private final ILocalizationService localizationService;

    /**
     * Creates a new LocalizationBinder instance for a given LocalizationService.
     *
     * @param localizationService the service providing localized string properties
     */
    public LocalizationBinder(ILocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    /**
     * Automatically binds all fields annotated with @Localized in the given controller
     * to the corresponding StringProperty from LocalizationService.
     *
     * Supports Label, Button, TextField, PasswordField, Text, Hyperlink, etc.
     *
     * @param controller the controller instance to process
     */
    public void bind(Object controller) {
        for (Field field : controller.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Localized.class)) {
                field.setAccessible(true);
                Localized annotation = field.getAnnotation(Localized.class);
                String key = annotation.value();

                try {
                    Object node = field.get(controller);

                    if (node instanceof Labeled labeled) {
                        labeled.textProperty().bind(localizationService.getStringProperty(key));
                    } else if (node instanceof TextInputControl input) {
                        input.promptTextProperty().bind(localizationService.getStringProperty(key));
                    } else if (node instanceof Text text) {
                        text.textProperty().bind(localizationService.getStringProperty(key));
                    } else {
                        throw new UtilityException(ExceptionLevel.ERROR,"Unsupported node type for localization: " + node.getClass());
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
