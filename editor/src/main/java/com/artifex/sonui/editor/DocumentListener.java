package com.artifex.sonui.editor;

//  this interface handles notifications for interesting document events.
public interface DocumentListener {

    //  called when another page is loaded from the document.
    void onPageLoaded(int pagesLoaded);

    //  called when the document is done loading.
    void onDocCompleted();

    //  called when a password is required.
    void onPasswordRequired();
}
