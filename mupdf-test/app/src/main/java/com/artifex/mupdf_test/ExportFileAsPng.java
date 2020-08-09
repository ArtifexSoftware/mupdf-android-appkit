/**
 * Load a document in SOLib exporting each page as a PNG file.
 *
 * An AsyncTask is utilised to sequentially extract document pages as bitmaps
 * and convert them to PNG files.
 *
 * Page bitmap extraction happens asynchronously with completion being
 * notified via a callback run on the main thread.
 *
 * To avoid blocking the main thread, while waiting for page bitmap extraction
 * to complete, iteration over the document pages is done in the AsyncTask.
 *
 * Copyright (C) Artifex, 2017. All Rights Reserved.
 *
 * @author Artifex
 */

package com.artifex.mupdf_test;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Message;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.artifex.solib.ArDkBitmap;
import com.artifex.solib.ArDkDoc;
import com.artifex.solib.ArDkPage;
import com.artifex.solib.ArDkUtils;
import com.artifex.solib.ConfigOptions;
import com.artifex.solib.SODocLoadListener;
import com.artifex.solib.ArDkLib;
import com.artifex.solib.SOPageListener;
import com.artifex.solib.SORenderListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

/**
 * This class provides the ability to generate PNG representation
 * of each page of a given document.
 */
class ExportFileAsPng
{
    private String      mPath;         // Path to the source file.
    private ArDkDoc     mDoc;          // Object representing a document.
    private ArDkPage    mPage;         // Object representing a page.
    private ArDkBitmap  mBitmap;       // Rendered bitmap.
    private boolean     mCompleted;    // Set on document loading complete.
    private boolean     mCancelled;    // Set on document loading cancelled.
    private ProgressBar mProgressbar ; // Export progress bar.
    private Activity    mActivity;     // Current activity.
    private boolean     mUseSecureFS;  // True if SecureFS is active.
    private String      mSecurePath;   // Path to root of secure container.
    private String      mSecurePrefix; // Secure prefix tag.

    private final String mDebugTag = "ExportFileAsPng";

    /**
     * This method implements the generation of PNG representations of each
     * page of the given document.
     *
     * @param activity The current activity.
     * @param path     The file to be processed.
     *
     */
    public void run(final Activity activity,
                    final String   path,
                    final boolean  useSecureFS,
                    final String   securePath,
                    final String   securePrefix)
    {
        mActivity     = activity;    // Current activity.
        mPath         = path;        // Source document path.
        mUseSecureFS  = useSecureFS;
        mSecurePath   = securePath;
        mSecurePrefix = securePrefix;
        mCompleted    = false;       // Document load state.
        mCancelled    = false;

        // use the registered configuration options of the application as
        // the document configuration options.
        ConfigOptions docCfg = ArDkLib.getAppConfigOptions();

        /*
         *  Load the library.
         *  It's safe to call this multiple times.
         */
        ArDkLib lib = ArDkUtils.getLibraryForPath(activity, path);

        // Load the document.
        mDoc = lib.openDocument(path, new SODocLoadListener()
        {
            @Override
            public void onPageLoad(int pageNum)
            {
                /*
                 * This could be useful if you want to display
                 * progress as the document is loading.
                 */
            }

            @Override
            public void onDocComplete()
            {
                // onDocComplete is called twice, we only need the first one.
                if (!mCompleted && !mCancelled)
                {
                    /* Show the progress bar. */
                    mProgressbar =
                        (ProgressBar)activity.findViewById(R.id.progressBar2);
                    mProgressbar.setScaleX(3.0f);
                    mProgressbar.setScaleY(3.0f);
                    mProgressbar.setProgress(0);
                    mProgressbar.setVisibility(View.VISIBLE);

                    /*
                     * In an AsyncTask extract page bitmaps sequentially and
                     * compress them to PNG format.
                     */
                    GeneratePngsTask pngTask = new GeneratePngsTask();
                    pngTask.execute((Void)null);
                }

                mCompleted = true;
            }

            @Override
            public void onError(final int error, final int errorNum)
            {
                // Called when the document load fails.
                if (error == ArDkLib.SmartOfficeDocErrorType_PasswordRequest)
                {
                    // Display password entry dialog.
                    showPasswordDialog();
                }
            }

            @Override
            public void onSelectionChanged(final int startPage,
                                           final int endPage)
            {
                // Called when the selection changes
            }

            @Override
            public void onLayoutCompleted()
            {
                // Called when a core layout is done
            }
        }, activity, docCfg);
    }

    /**
     * This method displays a password entry dialog.
     *
     * The retrieved password is passed to the library to allow the
     * document to be processed.
     */
    private void showPasswordDialog()
    {
        AlertDialog.Builder dBuilder =
            new AlertDialog.Builder(mActivity, R.style.AlertDialogTheme);
        dBuilder.setTitle(
            mActivity.getResources().getString(R.string.password));

        // Set up the input
        final EditText input = new EditText(mActivity);

        input.setInputType(InputType.TYPE_CLASS_TEXT |
                           InputType.TYPE_TEXT_VARIATION_PASSWORD);
        dBuilder.setView(input);

        // Set up the buttons
        dBuilder.setPositiveButton(
            mActivity.getResources().getString(android.R.string.ok),
            new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Pass the password to the library.
                mDoc.providePassword(input.getText().toString());
            }
        });

        dBuilder.setNegativeButton(
            mActivity.getResources().getString(android.R.string.cancel),
            new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Cancel the operation.
                mCancelled = true;
                dialog.cancel();
                cleanup();
            }
        });

        dBuilder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                // Cancel the operation.
                mCancelled = true;
                dialog.cancel();
                cleanup();
            }
        });

        dBuilder.show();
    }

    /**
     * This class implements an AsyncTask to iterate over all pages in
     * a document.
     *
     * Each page is extracted to a bitmap object which is compressed to a
     * PNG.
     */
    private class GeneratePngsTask extends AsyncTask<Void, Void, Boolean>
    {
        private int              mCptNumPages;
        private String           mCptPagePath   = mPath;
        private volatile Boolean mCptCompressOK = true;
        private final Semaphore  mCptSemaphore  = new Semaphore(1);

        @Override
        protected void onPreExecute()
        {
            // Note the number of pages in the document.
            mCptNumPages = mDoc.getNumPages();

            /*
             * Set the progress bar maximum to the number of pages in the
             * document.
             */
            mProgressbar.setMax(mCptNumPages);
        }

        @Override
        protected Boolean doInBackground(Void ... args)
        {
            /*
             * This value represents a zoom factor controlling the calculation
             * of bitmap size required to represent a page.
             *
             * A value of 1.0 results in bitmap dimensions large enough to
             * house the page rendered at 90 dpi, 2.0 results in bitmap
             * dimensions large enough to house the page rendered at 180 dpi
             * and so on.
             *
             * The display dimensions, in pixels, can be used to obtain
             * the zoom factor to match the native display size like so:
             *     mPage.zoomToFitRect(width, height)
             *
             * A new, larger, image can be generated on demand, one use case
             * being the user zooming.
             *
             * Rather than saving that image to disk, mBitmap.getBitmap()
             * can be used to get the underlying "Bitmap" which can be display
             * on the screen.
             */
            final float WANT_ZOOM = 4.0f; // Render pages at 360 dpi

            final int MAX_WIDTH   = 5000; // The maximum width of the bitmap
            final int MAX_HEIGHT  = 5000; // The maximum height of the bitmap

            Boolean     returnVal = true;
            int         pageNum;

            // Export each page sequentially.
            for (pageNum = 0; pageNum < mCptNumPages; pageNum++)
            {
                float zoom =  WANT_ZOOM;

                // Load the required page.
                mPage = mDoc.getPage(pageNum, new SOPageListener()
                {
                    @Override
                    public void update(RectF rectF)
                    {
                        /*
                         * this is used for updating sections of the page.
                         * not important for this application.
                         */
                    }
                });

                // Obtain the page dimensions at the desired zoom
                Point dimensions = mPage.sizeAtZoom(zoom);

                // Limit bitmap size
                if (dimensions.x > MAX_WIDTH || dimensions.y > MAX_HEIGHT)
                {
                    // recalculate bitmap size with an optimum zoom.
                    PointF fitZoom = mPage.zoomToFitRect(MAX_WIDTH, MAX_HEIGHT);
                    zoom = (fitZoom.x < fitZoom.y)? fitZoom.x : fitZoom.y;
                    dimensions = mPage.sizeAtZoom(zoom);
                }

                try
                {
                    // Allocate a bitmap of the same size
                    mBitmap =  ArDkUtils.createBitmapForPath(mPath,
                                                           dimensions.x,
                                                           dimensions.y);
                }
                catch (OutOfMemoryError e)
                {
                    Log.e(mDebugTag, "Cannot create bitmap for page " + String.valueOf(pageNum+1));

                    return false;
                }

                /*
                 * Acquire a semaphore to prevent the next iteration of the
                 * page loop until the current bitmap extraction is complete.
                 */
                try
                {
                    mCptSemaphore.acquire();
                }
                catch (InterruptedException e)
                {
                    Log.e(mDebugTag, "Cannot acquire mutex before rendering " +
                                     "page");

                    return false;
                }

                // Render the page to a bitmap.
                mPage.renderAtZoom(zoom,
                                   new PointF(0,0),
                                   mBitmap,
                                   new SORenderListener() {
                    @Override
                    public void progress(int error)
                    {
                        if (error == 0)
                        {
                            // Success.
                            mCptCompressOK = true;
                        }
                        else
                        {
                            // Error
                            Log.e(mDebugTag, "Error rendering to bitmap [" +
                                             String.valueOf(error) + "]");

                            mCptCompressOK = false;
                        }

                        mCptSemaphore.release();
                    }
                }, false);

                // Block until the current page bitmap has been rendered.
                try
                {
                    mCptSemaphore.acquire();
                    mCptSemaphore.release();
                }
                catch (InterruptedException e)
                {
                    Log.e(mDebugTag,
                          "Cannot acquire semaphore waiting on page rendering");
                }

                if (mCptCompressOK)
                {
                    // Generate a PNG from the page bitmap.
                    int thisPage = pageNum + 1;

                    /*
                     * Generate the path to the PNG file to be generated,
                     * in the same directory as the source bitmap.
                     */
                    mCptPagePath =  mPath.substring(0, mPath.lastIndexOf('.'));
                    mCptPagePath += "_page_" + thisPage + "_zoom_" + zoom;
                    mCptPagePath += ".png";

                    // Replace the psuedo path with a real path for png
                    // generation.
                    if (mUseSecureFS)
                    {
                        mCptPagePath =
                            mCptPagePath.replace(mSecurePrefix, mSecurePath);
                    }

                    Bitmap bm   = mBitmap.getBitmap();
                    File   file = new File(mCptPagePath);

                    try
                    {
                        OutputStream os =
                            new BufferedOutputStream(new FileOutputStream(file));
                        bm.compress(Bitmap.CompressFormat.PNG, 0, os);

                        os.flush();
                        os.close();

                        // Update the progress bar.
                        mProgressbar.setProgress(pageNum+1);
                    }
                    catch (IOException e)
                    {
                        Log.e(mDebugTag, "Error generating png for '" +
                                          mCptPagePath + "'");

                        returnVal = false;
                    }
                }

                // Free any resources allocated to the exported page.
                if (mPage != null)
                {
                    mPage.releasePage();
                    mPage.destroyPage();
                }

                if (mBitmap != null)
                {
                    mBitmap.getBitmap().recycle();
                }

                // Exit the loop on error.
                if (! returnVal)
                {
                    break;
                }
            }

            return returnVal;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            String text;

            // Dismiss the progress bar.
            mProgressbar.setVisibility(View.GONE);

            // All done, show a message
            AlertDialog alertDialog =
                new AlertDialog.Builder(mActivity, R.style.AlertDialogTheme)
                               .create();

            if (result)
            {
                String realPath;

                // Success
                alertDialog.setTitle(R.string.png_export_title_success);

                // Add message to dialogue.
                String folder =
                    mPath.substring(0, mPath.lastIndexOf('/') + 1);

                if (mUseSecureFS)
                {
                    // Map a SecureFS pseudo path to a real path for display.
                    realPath = folder.replace(mSecurePrefix, mSecurePath);
                }
                else
                {
                    realPath = folder;
                }

                text = mActivity.getResources().
                           getString(R.string.export_folder_message);
                text += "\n" + realPath;

                alertDialog.setMessage(text);
            }
            else
            {
                // Failure
                alertDialog.setTitle(R.string.png_export_title_fail);
            }

            // Add dismiss button.
            text = mActivity.getResources().getString(android.R.string.ok);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                                 text,
                                 (Message)null);

            alertDialog.show();

            cleanup();
        }
    }

    @Override
    /**
     * This method cleans up on activity finalize.
     */
    protected void finalize() throws Throwable
    {
        cleanup();
        super.finalize();
    }

    /**
     * This method frees any remaining allocated SOLib resources
     */
    private void cleanup()
    {
        //  clean up
        if (mPage != null)
        {
            mPage.releasePage();
            mPage.destroyPage();
        }

        if (mDoc != null)
        {
            mDoc.destroyDoc();
        }

        if (mBitmap != null)
        {
            mBitmap.getBitmap().recycle();
        }
    }
}
