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

import com.artifex.solib.SOClipboardHandler;

public class ClipboardHandler implements SOClipboardHandler
{
    private static final String mDebugTag    = "ClipboardHandler";
    private static boolean      mEnableDebug = false;

    private Activity           mActivity;       // The current activity.
    private ClipboardManager   mClipboard;      // System clipboard.

    //////////////////////////////////////////////////////////////////////////
    // Methods Required By Interface.
    //////////////////////////////////////////////////////////////////////////

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
     */
    public void initClipboardHandler(Activity activity)
    {
        mActivity = activity;

        // Get the system clipboard.
        mClipboard =
            (ClipboardManager)mActivity.getSystemService(
                                                     Context.CLIPBOARD_SERVICE);
    }
}
