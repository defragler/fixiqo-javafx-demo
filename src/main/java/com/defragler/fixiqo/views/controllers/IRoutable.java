package com.defragler.fixiqo.views.controllers;

/**
 * Interface for a page controller that can receive typed parameters when navigated to.
 *
 * @param <T> the type of parameter the page can accept
 */
public interface IRoutable<T> {

    /**
     * Called when the page is navigated to.
     *
     * @param parameter the parameter passed during navigation
     */
    void onNavigate(T parameter);
}
