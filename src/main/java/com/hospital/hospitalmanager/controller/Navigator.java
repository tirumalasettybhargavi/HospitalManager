package com.hospital.hospitalmanager.controller;

/** Lets a page ask the main window to switch pages or open the search dialog. */
public interface Navigator {
    void navigate(Page page);

    void openSearch();
}
