package com.defragler.fixiqo.views.controllers;

/**
 * Interface for modal controllers that want to receive navigation parameters.
 */
public interface IModal {

    /**
     * Called when modal is opened.
     *
     * @param parameter optional parameter
     */
    void onOpen(Object parameter);
}
