package com.artifex.mupdf_test;

/**
 * This file contains an example implementation of the SOClipboardHandler
 * interface using the system clipboard.
 *
 * If the class is not found the SOLib  will directly access the system
 * clipboard.
 */

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import java.lang.IllegalStateException;

import com.artifex.solib.SOClipboardHandler;

public class ClipboardHandler implements SOClipboardHandler
{
    private static final String mDebugTag    = "ClipboardHandler";
    private static boolean      mEnableDebug = false;

    private Activity           mActivity;       // The current activity.
    private OnClipboardChanged mChangeListener; // Client clipboard change
                                                // listener.
    private ClipboardManager   mClipboard;      // System clipboard.

    // System clipboard change listener.
    private ClipboardManager.OnPrimaryClipChangedListener
                                                 mPrimaryChangeListener;

    //////////////////////////////////////////////////////////////////////////
    // Utility Methods.
    //////////////////////////////////////////////////////////////////////////

    /**
     * Install the registered system clipboard change listener.
     *
     * @throws IllegalStateException
     */
    private void installChangeListener()
        throws IllegalStateException
    {
        // Listener hasn't been registered yet.
        if (mChangeListener == null)
        {
            throw new IllegalStateException();
        }

        // Create a system clipboard change listener.
        if (mPrimaryChangeListener == null)
        {
            mPrimaryChangeListener =
                    new ClipboardManager.OnPrimaryClipChangedListener() {
                        @Override
                        public void onPrimaryClipChanged() {
                            // Call the SOLib registered listener.
                            if (mChangeListener != null) {
                                mChangeListener.clipboardChanged();
                            }
                        }
                    };

            // Install the system clipboard listener
            mClipboard.addPrimaryClipChangedListener(mPrimaryChangeListener);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // Methods Required By Interface.
    //////////////////////////////////////////////////////////////////////////

    /**
     * This method registers the clipboard change listener.<br><br>
     *
     * @param onClipboardChanged The listener object
     */
    @Override
    public void registerListener(OnClipboardChanged onClipboardChanged)
    {
        // Remove any previous listener.
        if (mChangeListener != null && mClipboard != null)
        {
            mClipboard.removePrimaryClipChangedListener(mPrimaryChangeListener);
        }

        /*
         * Register the new listener.It will be installed when
         * initClipboardHandler() is called.
         */
        mChangeListener = onClipboardChanged;
    }

    /**
     * This method passes a string, cut or copied from the document, to be
     * stored in the clipboard.
     *
     * @param text The text to be stored in the clipboard.
     */
    @Override
    public void putPlainTextToClipboard(String text)
    {
        if (mEnableDebug)
        {
            Log.d(mDebugTag, "putPlainTextToClipboard: '" + text + "'");
        }

        if (text != null)
        {
            ClipData clip;
            clip = ClipData.newPlainText("text", text);
            mClipboard.setPrimaryClip(clip);
        }
    }

    /**
     * This method returns the contents of the clipboard.
     *
     * @return The text read from the clipboard.
     */
    @Override
    public String getPlainTextFromClipoard()
    {
        String text = "";

        if (clipboardHasPlaintext())
        {
            ClipData      clip = mClipboard.getPrimaryClip();
            ClipData.Item item = clip.getItemAt(0);

            text = item.coerceToText(mActivity).toString();
            text = text;

            if (mEnableDebug)
            {
                Log.d(mDebugTag, "getPlainTextFromClipoard: '" + text + "'");
            }
        }

        return text;
    }

    /**
     * This method ascertains whether the clipboard has any data.
     *
     * @return True if it has. False otherwise.
     */
    @Override
    public boolean clipboardHasPlaintext()
    {
        return mClipboard.hasPrimaryClip();
    }

    /**
     * Initialise the class, installing the example system clipboard listener
     * if available.<br><br>
     *
     * @param activity      The current activity.
     *
     * @throws IllegalStateException
     */
    public void initClipboardHandler(Activity activity)
        throws IllegalStateException
    {
        mActivity = activity;

        // Get the system clipboard.
        mClipboard =
            (ClipboardManager)mActivity.getSystemService(
                                                     Context.CLIPBOARD_SERVICE);

        try
        {
            // Install any registered clipboard listener.
            installChangeListener();
        }
        catch (IllegalStateException e)
        {
            throw e;
        }
    }
}
