package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.contexts.*;
import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.utilities.*;

import com.fasterxml.jackson.core.type.*;
import com.jthemedetecor.*;

import javafx.application.*;
import java.util.*;

import atlantafx.base.theme.*;

/**
 * Default implementation of {@link IThemeService}.
 *
 * <p>Handles theme switching, persistence, and OS theme synchronization.</p>
 */
public class ThemeService implements IThemeService {
    private final ISessionService sessionService;
    
    private ThemeMode currentMode = ThemeMode.AUTO;
    private final OsThemeDetector detector = OsThemeDetector.getDetector();
    private boolean listenerRegistered = false;

    public ThemeService(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void setTheme(ThemeMode mode) {
        this.currentMode = mode;

        sessionService.setTheme(mode);

        applyCurrentTheme();
        handleAutoModeListener();
    }

    @Override
    public ThemeMode getCurrentTheme() {
        return currentMode;
    }

    @Override
    public void applyCurrentTheme() {
        Platform.runLater(() -> {
            switch (currentMode) {
                case LIGHT -> applyLight();
                case AUTO -> applyAuto();
                case DARK -> applyDark();
            }
        });
    }

    @Override
    public void loadTheme() {
        currentMode = sessionService.getTheme();
        handleAutoModeListener();
    }

    /* ---------------- PRIVATE ---------------- */
    private void applyLight() {
        Application.setUserAgentStylesheet(
              new CupertinoLight().getUserAgentStylesheet()
        );
    }

    private void applyDark() {
        Application.setUserAgentStylesheet(
              new CupertinoDark().getUserAgentStylesheet()
        );
    }

    private void applyAuto() {
        if (currentMode != ThemeMode.AUTO) return;
        boolean isDark = detector.isDark();

        Application.setUserAgentStylesheet(
              isDark
                    ? new CupertinoDark().getUserAgentStylesheet()
                    : new CupertinoLight().getUserAgentStylesheet()
        );
    }

    /**
     * Registers or unregisters OS theme listener depending on mode.
     */
    private void handleAutoModeListener() {
        if (currentMode == ThemeMode.AUTO && !listenerRegistered) {
            detector.registerListener(isDark ->
                  Platform.runLater(() -> {
                      if (currentMode == ThemeMode.AUTO) {
                          applyAuto();
                      }
                  })
            );
            listenerRegistered = true;
        }
    }
}