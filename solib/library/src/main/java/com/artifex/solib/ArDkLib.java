package com.artifex.solib;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class ArDkLib
{
    protected static final String mDebugTag = "ArDkLib";

    //  possible document loading errors
    public static final int SmartOfficeDocErrorType_NoError = 0;
    public static final int SmartOfficeDocErrorType_UnsupportedDocumentType = 1;
    public static final int SmartOfficeDocErrorType_EmptyDocument = 2;
    public static final int SmartOfficeDocErrorType_UnableToLoadDocument = 4;
    public static final int SmartOfficeDocErrorType_UnsupportedEncryption = 5;
    public static final int SmartOfficeDocErrorType_Aborted = 6;
    public static final int SmartOfficeDocErrorType_OutOfMemory = 7;

    /** A password is required to open this document.
     *
     * The app should provide it using SmartOffice_providePassword
     * or if it doesn't want to proceed call SmartOfficeDoc_destroy or
     * SmartOfficeDoc_abortLoad.
     */
    public static final int SmartOfficeDocErrorType_PasswordRequest = 0x1000;

    //  subclasses will implement this
    public abstract ArDkDoc openDocument(String path, SODocLoadListener listener, Context context, ConfigOptions cfg);

    public interface EnumeratePdfTocListener {
        void nextTocEntry(int handle, int parentHandle, int page, String label, String url, float x, float y);
    }

    public static void enumeratePdfToc(final ArDkDoc doc, final EnumeratePdfTocListener listener)
    {
        ((MuPDFDoc)doc).enumerateToc(new MuPDFDoc.MuPDFEnumerateTocListener() {
            @Override
            public void nextTocEntry(int handle, int parentHandle, int page, String label, String url, float x, float y)
            {
                listener.nextTocEntry(handle, parentHandle, page, label, url, x, y);
            }
        });
    }

    private static SOSecureFS mSecureFS = null;
    public static void setSecureFS(SOSecureFS fs) {mSecureFS=fs;}
    public static SOSecureFS getSecureFS() {return mSecureFS;}

    private static ConfigOptions mAppConfigOptions = null;
    public static void setAppConfigOptions(ConfigOptions co) {mAppConfigOptions=co;}
    public static ConfigOptions getAppConfigOptions()
    {
        if (mAppConfigOptions == null)
        {
            throw new RuntimeException("No registered ConfigOptions found.");
        }
        return mAppConfigOptions;
    }

    public abstract ArDkBitmap createBitmap(int w, int h);

    /**
     * data and function for using the external keyboard
     */
    private static ClipboardManager myClipboard;
    private static ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener;
    private static int externalCount = 0;
    public static int getExternalCount() {return externalCount;}

    // Reference to the application clipboard handler. May be null.
    protected static SOClipboardHandler mClipboardHandler;

    public static void setClipboardHandler(SOClipboardHandler handler)
    {
        mClipboardHandler = handler;

        registerClipboardListener();
    }

    public static SOClipboardHandler getClipboardHandler()
    {
        return mClipboardHandler;
    }

    /*
     * Register a clipboard listener with the application clipboard handler
     * class.
     */
    protected static void registerClipboardListener()
    {
        String errorBase =
                "setClipboardHandler() experienced unexpected exception [%s]";

        try
        {
            if (mClipboardHandler==null)
                throw new ClassNotFoundException();

            // Register the clipboard change listener.
            mClipboardHandler.registerListener(
                    new SOClipboardHandler.OnClipboardChanged()
                    {
                        @Override
                        public  void clipboardChanged()
                        {
                            //  if it has text, update the count.
                            if(mClipboardHandler.clipboardHasPlaintext())
                            {
                                externalCount++;
                            }
                        }
                    });
        }
        catch (ExceptionInInitializerError e)
        {
            Log.e(mDebugTag, String.format(errorBase,
                    "ExceptionInInitializerError"));
        }
        catch (LinkageError e)
        {
            Log.e(mDebugTag, String.format(errorBase, "LinkageError"));
        }
        catch (SecurityException e)
        {
            Log.e(mDebugTag, String.format(errorBase,
                    "SecurityException"));
        }
        catch (ClassNotFoundException e)
        {
            Log.i(mDebugTag, "ClipboardHandler implementation unavailable");
        }
    }

    /**
     * called once when the library is created.
     */
    protected static void setupClipboard(Context context)
    {
        //  get a clipboard manager
        myClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        //  create a clipboard change listener
        mPrimaryChangeListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {

                //  the system clipboard has been changed.
                //  if it has text, update the count.
                if(clipboardHasText())
                    externalCount++;
            }
        };

        //  register the listener
        myClipboard.addPrimaryClipChangedListener(mPrimaryChangeListener);
    }

    /**
     * copy text to the system clipboard
     */
    public static void putTextToClipboard(String text)
    {
        if (text != null)
        {
            if (mClipboardHandler == null)
            {
                // Use the system clipboard.
                ClipData myClip;
                myClip = ClipData.newPlainText("text", text);
                myClipboard.setPrimaryClip(myClip);
            }
            else
            {
                // Use the clipboard handler supplied by the application.
                mClipboardHandler.putPlainTextToClipboard(text);
            }
        }
    }

    /**
     * find out if the system clipboard has text
     */
    public static boolean clipboardHasText()
    {
        if (mClipboardHandler == null)
        {
            // Use the system clipboard.
            return myClipboard.hasPrimaryClip();
        }
        else
        {
            // Use the clipboard handler supplied by the application.
            return mClipboardHandler.clipboardHasPlaintext();
        }
    }

    /**
     * get text from the system clipboard
     */
    public static String getClipboardText(Context context)
    {
        String text = "";

        if (mClipboardHandler == null)
        {
            // Use the system clipboard.
            if (myClipboard.hasPrimaryClip())
            {
                ClipData clip = myClipboard.getPrimaryClip();
                ClipData.Item item = clip.getItemAt(0);
                text = item.coerceToText(context).toString();
            }
        }
        else
        {
            // Use the clipboard handler supplied by the application.
            return mClipboardHandler.getPlainTextFromClipoard();
        }

        return text;
    }

    public void reclaimMemory()
    {
    }

    public static boolean isNightMode(Context context)
    {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
    }

    /**
     * Function to delay an action until it can be run on the UiThread
     *
     * @param action   Action to run on the UiThread
     */
    protected static final void runOnUiThread(Runnable action)
    {
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        if (mainHandler!=null)
            mainHandler.post(action);
    }
}
