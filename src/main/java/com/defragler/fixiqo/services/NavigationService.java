package com.defragler.fixiqo.services;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.views.controllers.*;

import java.io.*;
import java.util.*;
import javafx.util.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.layout.*;

/**
 * Professional NavigationService implementation for applications with generics.
 * 
 * <p>Supports:
 * <ul>
 *     <li>Parameterized navigation via {@link IRoutable}</li>
 *     <li>Back/forward history stacks</li>
 *     <li>Controller caching and page reloads</li>
 *     <li>Dependency injection in controllers via {@link Callback}</li>
 * </ul></p>
 *
 * <p>Controllers must implement {@link IRoutable} to receive navigation parameters.</p>
 */
public class NavigationService implements INavigationService {

    private final StackPane container;
    private final Callback<Class<?>, Object> controllerFactory;

    // Cache for FXML pages and their controllers
    private final Map<String, Node> pageCache = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();

    // Navigation history stacks
    private final Stack<HistoryEntry> backHistory = new Stack<>();
    private final Stack<HistoryEntry> forwardHistory = new Stack<>();

    private HistoryEntry currentEntry = null;

    /**
     * Constructs a NavigationService with a container and controller factory.
     *
     * @param container root StackPane for displaying pages
     * @param controllerFactory factory for creating controllers with DI support
     */
    public NavigationService(StackPane container, Callback<Class<?>, Object> controllerFactory) {
        if (container == null) throw new ServiceException(ExceptionLevel.ERROR,"Container cannot be null");
        if (controllerFactory == null) throw new ServiceException(ExceptionLevel.ERROR,"ControllerFactory cannot be null");

        this.container = container;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public <T> void navigateTo(String fxmlPath, T parameter) {
        if (fxmlPath == null || fxmlPath.isEmpty()) return;

        try {
            Node page;
            Object controller;

            // Load page from cache or from FXML
            if (pageCache.containsKey(fxmlPath)) {
                page = pageCache.get(fxmlPath);
                controller = controllerCache.get(fxmlPath);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                loader.setControllerFactory(controllerFactory);
                page = loader.load();
                controller = loader.getController();

                pageCache.put(fxmlPath, page);
                controllerCache.put(fxmlPath, controller);
            }

            // Pass typed parameter if controller implements IRoutable<T>
            if (controller instanceof IRoutable<?> routable) {
                // Unsafe cast handled generically
                @SuppressWarnings("unchecked")
                IRoutable<T> typedRoutable = (IRoutable<T>) routable;
                typedRoutable.onNavigate(parameter);
            }

            // Update history
            if (currentEntry != null) {
                backHistory.push(currentEntry);
                forwardHistory.clear();
            }

            container.getChildren().setAll(page);
            currentEntry = new HistoryEntry(fxmlPath, parameter);

        } catch (IOException ex) {
            throw new ServiceException(ExceptionLevel.ERROR,"Failed to load FXML: " + fxmlPath, ex);
        }
    }

    /**
     * Reloads the currently displayed page, reloading FXML and creating a new controller instance.
     *
     * @throws ServiceException if there is no current page
     */
    public void reloadCurrentPage() {
        if (currentEntry == null) {
            throw new ServiceException(ExceptionLevel.ERROR,"No current page to reload.");
        }

        // Remove from cache
        pageCache.remove(currentEntry.fxmlPath);
        controllerCache.remove(currentEntry.fxmlPath);

        // Navigate again to reload
        navigateTo(currentEntry.fxmlPath, currentEntry.parameter);
    }

    @Override
    public void goBack() {
        if (!canGoBack()) throw new ServiceException(ExceptionLevel.ERROR,"No previous page in history.");

        forwardHistory.push(currentEntry);
        currentEntry = backHistory.pop();

        Node page = pageCache.get(currentEntry.fxmlPath);
        Object controller = controllerCache.get(currentEntry.fxmlPath);

        if (controller instanceof IRoutable<?> routable) {
            @SuppressWarnings("unchecked")
            IRoutable<Object> typedRoutable = (IRoutable<Object>) routable;
            typedRoutable.onNavigate(currentEntry.parameter);
        }

            container.getChildren().setAll(page);
    }

    @Override
    public void goForward() {
        if (!canGoForward()) throw new ServiceException(ExceptionLevel.ERROR,"No forward page in history.");

        backHistory.push(currentEntry);
        currentEntry = forwardHistory.pop();

        Node page = pageCache.get(currentEntry.fxmlPath);
        Object controller = controllerCache.get(currentEntry.fxmlPath);

        if (controller instanceof IRoutable<?> routable) {
            @SuppressWarnings("unchecked")
            IRoutable<Object> typedRoutable = (IRoutable<Object>) routable;
            typedRoutable.onNavigate(currentEntry.parameter);

        }

        container.getChildren().setAll(page);
    }

    @Override
    public boolean canGoBack() {
        return !backHistory.isEmpty();
    }

    @Override
    public boolean canGoForward() {
        return !forwardHistory.isEmpty();
    }

    @Override
    public Node getCurrentPage() {
        if (currentEntry == null) return null;
        return pageCache.get(currentEntry.fxmlPath);
    }

    @Override
    public void clear() {
        backHistory.clear();
        forwardHistory.clear();
        pageCache.clear();
        controllerCache.clear();
        currentEntry = null;
        container.getChildren().clear();
    }

    /**
     * Internal class to store page history entries with typed parameters.
     */
    private static class HistoryEntry {
        final String fxmlPath;
        final Object parameter;

        HistoryEntry(String fxmlPath, Object parameter) {
            this.fxmlPath = fxmlPath;
            this.parameter = parameter;
        }
    }
}
