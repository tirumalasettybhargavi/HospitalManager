package com.hospital.hospitalmanager.controller;

import javafx.scene.Parent;

/** Every page has a view to show and a refresh method that reloads its data. */
public interface PageController {
    Parent getView();

    default void refresh() { }
}
