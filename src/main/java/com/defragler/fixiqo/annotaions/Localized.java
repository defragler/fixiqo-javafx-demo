package com.defragler.fixiqo.annotaions;

import java.lang.annotation.*;

/**
 * Marks a JavaFX UI element for automatic localization binding.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Localized {
    /**
     * The localization key for this UI element.
     */
    String value();
}
