package com.artifex.solib;

import android.app.Activity;

/**
 * This interface specifies the basis for implementing a class to handle
 * clipboard actions for the document editor.
 */

public interface SOClipboardHandler
{
    /**
     * This interface defines a listener that should be called by the
     * application when the contents of it's clipboard change.
     */
    public interface OnClipboardChanged
    {
        public void clipboardChanged();
    }

    /**
     * This method registers the clipboard change listener.<br><br>
     *
     * @param onClipboardChanged The SOClipboardHandler object implementing
     *                           the listener.
     */
    public void registerListener(OnClipboardChanged onClipboardChanged);

    /**
     * This method passes a string, cut or copied from the document, to be
     * stored in the clipboard.
     *
     * @param text The text to be stored in the clipboard.
     */
    public void putPlainTextToClipboard(String text);

    /**
     * This method returns the contents of the clipboard.
     *
     * @return The text stored in the clipboard.
     */
    public String getPlainTextFromClipoard();

    /**
     * This method ascertains whether the clipboard has any data.
     *
     * @return True if it has. False otherwise.
     */
    public boolean clipboardHasPlaintext();

    /**
     * This method initializes the clipboard handler
     **
     * @param activity the context
     */
    public void initClipboardHandler(Activity activity);
}
