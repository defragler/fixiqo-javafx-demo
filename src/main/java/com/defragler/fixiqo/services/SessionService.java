package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.contexts.*;
import com.defragler.fixiqo.services.enums.ThemeMode;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.utilities.*;
import com.defragler.fixiqo.views.controllers.*;
import com.defragler.fixiqo.views.windows.*;

import com.fasterxml.jackson.core.type.*;

import java.io.*;
import java.time.*;
import java.util.*;
import javafx.stage.*;

/**
 * Default implementation of {@link ISessionService}.
 *
 * <p>Handles session persistence using JSON storage and supports
 * automatic login restoration.</p>
 */
public class SessionService implements ISessionService {
    private final String SESSION_FILE;

    private final ApplicationContext context;
    private SessionContext currentSession;
    
    private final IUserService userService;
    private final JsonFileHandler jsonHandler;

    public SessionService(ApplicationContext context, IUserService userService) {
        this.context = context;
        this.userService = userService;
        this.SESSION_FILE = AppPaths.sessionPath().toString();
        this.jsonHandler = new JsonFileHandler(SESSION_FILE);
    }
    
    @Override
    public void saveSession() {
        var user = context.getCurrentUser();
        if (user == null) return;

        ThemeMode theme = currentSession != null
              ? currentSession.getTheme()
              : ThemeMode.AUTO;

        currentSession = new SessionContext(
              user.getId(),
              user.getUsername(),
              Instant.now().getEpochSecond(),
              theme
        );

        jsonHandler.writeAllAtomic(List.of(currentSession));
    }

    @Override
    public boolean tryAutoLogin() {
        if (!hasSession()) return false;

        List<SessionContext> sessions = jsonHandler.readAll(new TypeReference<>() {});
        if (sessions.isEmpty()) return false;

        SessionContext session = sessions.get(0);

        Optional<User> optionalUser = userService.getById(session.getUserId());
        if (optionalUser.isEmpty()) {
            clearSession();
            return false;
        }

        long now = Instant.now().getEpochSecond();
        long sessionAge = now - session.getCreatedAt();

        if (sessionAge > 60 * 60 * 24 * 7) { // 7 days
            clearSession();
            return false;
        }

        context.setCurrentUser(optionalUser.get());
        currentSession = session;
        return true;
    }

    @Override
    public void clearSession() {
        jsonHandler.writeAllAtomic(List.of());
    }

    @Override
    public void logout(Stage stage) {
        try {
            clearSession();
            context.setCurrentUser(null);

            new LoginWindow(context).start(stage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasSession() {
        return !jsonHandler.isEmpty();
    }

    @Override
    public ThemeMode getTheme() {
        if (currentSession == null) return ThemeMode.AUTO;
        return currentSession.getTheme();
    }

    @Override
    public void setTheme(ThemeMode mode) {
        if (currentSession == null) return;

        currentSession.setTheme(mode);
        jsonHandler.writeAllAtomic(List.of(currentSession));
    }
}