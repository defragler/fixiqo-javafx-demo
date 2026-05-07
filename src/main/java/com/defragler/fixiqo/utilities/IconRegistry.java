package com.defragler.fixiqo.utilities;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;

/**
 * Centralized registry for SVG icons with advanced features:
 * <ul>
 *     <li>Type-safe access via {@link Icon}</li>
 *     <li>Lazy loading of SVG paths from resources</li>
 *     <li>Cache support (icon + size + fill + stroke)</li>
 *     <li>CSS integration via style classes</li>
 *     <li>Customizable size, fill, stroke</li>
 *     <li>Thread-safe for multithreaded UI applications</li>
 * </ul>
 *
 * <p>
 * All icons are immutable after creation. Use {@link IconBuilder} for fluent configuration.
 * </p>
 */
public final class IconRegistry {

    /**
     * Enum representing all available icons.
     * The enum name should match the SVG filename (without extension) in /icons/.
     */
    public enum Icon {
        HOME,
        CLIENTS,
        REQUESTS,
        BROWSER,
        INFORMATION,
        SETTINGS,
        TEMP
    }

    /** Path cache: Icon -> SVG content (lazy loaded from resources) */
    private static final Map<Icon, String> ICON_PATHS = new EnumMap<>(Icon.class);

    /** Instance cache: icon + size + fill + stroke -> SVGPath */
    private static final Map<CacheKey, SVGPath> CACHE = new ConcurrentHashMap<>();

    // Prevent instantiation
    private IconRegistry() {
        throw new UtilityException(ExceptionLevel.ERROR,"Utility class cannot be instantiated");
    }

    /**
     * Returns a preconfigured builder for the specified icon.
     *
     * @param icon icon enum
     * @return IconBuilder instance
     */
    public static IconBuilder builder(Icon icon) {
        return new IconBuilder(icon);
    }

    /**
     * Fluent builder for SVG icons.
     * Supports size, fill, stroke, and CSS class.
     */
    public static class IconBuilder {
        private final Icon icon;
        private Paint fill;
        private Paint stroke;
        private double size = -1;
        private String styleClass;

        private IconBuilder(Icon icon) {
            this.icon = icon;
        }

        /** Sets the fill color */
        public IconBuilder fill(Paint fill) {
            this.fill = fill;
            return this;
        }

        /** Sets the stroke color */
        public IconBuilder stroke(Paint stroke) {
            this.stroke = stroke;
            return this;
        }

        /** Sets uniform size (scale) */
        public IconBuilder size(double size) {
            this.size = size;
            return this;
        }

        /** Adds CSS style class */
        public IconBuilder styleClass(String styleClass) {
            this.styleClass = styleClass;
            return this;
        }

        /** Builds the SVGPath instance */
        public SVGPath build() {
            CacheKey key = new CacheKey(icon, fill, stroke, size, styleClass);
            return CACHE.computeIfAbsent(key, IconRegistry::createIcon);
        }
    }

    /**
     * Internal factory for creating SVGPath instances.
     * Lazy loads SVG content from /icons/{icon}.svg if needed.
     */
    private static SVGPath createIcon(CacheKey key) {
        String svgContent = ICON_PATHS.computeIfAbsent(key.icon, IconRegistry::loadSvgContent);

        SVGPath svg = new SVGPath();
        svg.setContent(svgContent);

        if (key.fill != null) svg.setFill(key.fill);
        if (key.stroke != null) svg.setStroke(key.stroke);
        if (key.size > 0) {
            double scale = key.size / 24.0;
            svg.setScaleX(scale);
            svg.setScaleY(scale);
        }
        if (key.styleClass != null && !key.styleClass.isBlank()) {
            svg.getStyleClass().add(key.styleClass);
        }
        return svg;
    }

    /**
     * Loads SVG content from /icons/{icon}.svg resource file.
     *
     * @param icon icon enum
     * @return SVG path content as String
     */
    private static String loadSvgContent(Icon icon) {
        String resourcePath = "/icons/" + icon.name().toLowerCase() + ".svg";
        try (InputStream is = IconRegistry.class.getResourceAsStream(resourcePath)) {
            if (is == null) throw new UtilityException(ExceptionLevel.WARNING,"SVG file not found: " + resourcePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to load SVG: " + resourcePath, e);
        }
    }

    /**
     * Clears the icon cache.
     * Useful when switching themes dynamically.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Key for caching SVGPath instances.
     */
    private record CacheKey(Icon icon, Paint fill, Paint stroke, double size, String styleClass) {
        @Override
        public int hashCode() {
            return Objects.hash(icon, fill, stroke, size, styleClass);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof CacheKey other)) return false;
            return icon == other.icon &&
                  Objects.equals(fill, other.fill) &&
                  Objects.equals(stroke, other.stroke) &&
                  size == other.size &&
                  Objects.equals(styleClass, other.styleClass);
        }
    }
}
