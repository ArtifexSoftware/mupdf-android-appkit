package com.artifex.mupdf_test;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.ClassNotFoundException;
import java.lang.ExceptionInInitializerError;
import java.lang.LinkageError;
import java.lang.SecurityException;
import java.util.Arrays;
import java.util.Comparator;

import com.artifex.solib.ConfigOptions;
import com.artifex.solib.ArDkLib;
import com.artifex.solib.SOSecureFS;
import com.artifex.sonui.editor.Utilities;

public class ChooseDocActivity extends Activity
{
    private static final int PERMISSION_STORAGE = 1;

    private final String  mDebugTag = "ChooseDocActivity";

    private ChooseExampleActivity.ExamplesEnum mExample;
    private ListView                           mListView;
    private ListView                           mConfigListView;
    private static boolean[]                   mConfigStates = null;
    private Activity                           mActivity;
    private ChooseDocAdapter                   mAdapter;
    static private File                        mDirectory;
    static private File                        mStartingDirectory;
    private File                               mParent;
    private File[]                             mDirs;
    private File[]                             mFiles;
    private Handler                            mHandler;
    private Runnable                           mUpdateFiles;
    private SOSecureFS                         mSecureFs;
    private String                             mSecurePath;
    private String                             mSecurePrefix;

    public class ConfigAdapter extends ArrayAdapter<String>
    {
        public ConfigAdapter(Context  context,
                             int      layoutResourceId,
                             String[] data)
        {
            super(context, layoutResourceId, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //  pass null here to insure that each checkbox is a separate view.
            View v = super.getView(position, null, parent);

            // Initialise the item state from state array.
            ((CheckBox)v).setChecked(mConfigStates[position]);
            v.setTag(position);

            // redaction is NOT currently available.
            if (position == 10)
            {
                v.setEnabled(false);
            }

            v.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    // Keep the state array up to date.
                    mConfigStates[(int)v.getTag()] =
                        ((CheckBox)v).isChecked();
                 }
            });

            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Check permissions
        boolean askPermission = false;

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED)
        {
            askPermission = true;
        }

        if (askPermission)
        {
            // Not immediately granted, so ask.
            // We'll return in onRequestPermissionsResult()
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_STORAGE);
            return;
        }

        // Already granted, proceed.
        gotPermission();
    }

    @Override
    public void onRequestPermissionsResult(int    requestCode,
                                           String permissions[],
                                           int[]  grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_STORAGE:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Permission was granted.
                    gotPermission();
                }
                else
                {
                    // Permission denied.
                    finish();
                }
                return;
            }
        }
    }

    //  this function extracts an asset with the given name,
    //  and copies it to a file with the same name, at the path given.
    //  the path is assumed to already exist.

    private static String extractAssetToPath(Context context, String assetName, String path)
    {
        File dest = new File(path, assetName);
        try {
            InputStream inputStream = context.getAssets().open(assetName);
            try {
                FileOutputStream outputStream = new FileOutputStream(dest);
                try {
                    byte[] buf = new byte[1024*16];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return dest.getAbsolutePath();
    }

    //  this function copies all the asset files to a directory clled "samples",
    //  in the secure location.  It assumes that the secure location already exists.

    private void copyAssets()
    {
        //  are we secure?  Do nothing if we're not.
        SOSecureFS secureFS = ArDkLib.getSecureFS();
        if (secureFS==null)
            return;

        //  make the destination directory.
        String path = secureFS.getSecurePath() + File.separator + "samples";
        new File(path).mkdirs();

        //  copy each asset
        try {
            String[] assets = this.getAssets().list("");
            for (String asset: assets) {

                //  these don't really exist, but we get them in the list anyway.
                if (asset.equals("images")
                        || asset.equals("sounds")
                        || asset.equals("webkit")
                        || asset.equals("databases")
                        || asset.equals("kioskmode"))
                    continue;

                //  extract  one
                extractAssetToPath(this, asset, path);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gotPermission()
    {
        setContentView(R.layout.choose_doc);

        //  copy sample files
        copyAssets();

        mActivity = this;

        Intent intent = getIntent();
        mExample =
            (ChooseExampleActivity.ExamplesEnum)intent.getExtras().
                                                getSerializable("EXAMPLE_ID");

        // Set the list view title for the selected example application.
        final TextView tv = (TextView) findViewById(R.id.fileListViewTitle);

        if (mExample ==
            ChooseExampleActivity.ExamplesEnum.DOCUMENT_EXPORT_TO_PNG)
        {
            tv.setText(R.string.png_export_example);
        }
        else
        {
            tv.setText(R.string.doc_editor_example);

            // Display the configuration icon
            tv.setCompoundDrawablesWithIntrinsicBounds(
                               0,                                      // left
                               0,                                      // top
                               android.R.drawable.ic_menu_preferences, // right
                               0);                                     // bottom

            // Set the handler to catch configuration icon press.
            tv.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP)
                    {
                        if(event.getRawX() >=
                           tv.getRight() - tv.getTotalPaddingRight())
                        {
                            // Toggle between file and configuration views.
                            if (mListView.isShown())
                            {
                                mListView.setVisibility(View.GONE);
                                mConfigListView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                mListView.setVisibility(View.VISIBLE);
                                mConfigListView.setVisibility(View.GONE);
                            }

                            return true;
                        }
                    }

                    return true;
                }
            });

            // Inflate the configuration view.
            mConfigListView = (ListView) findViewById(R.id.configListView);

            ConfigAdapter arrayAdapter = new ConfigAdapter(
                    this,
                    R.layout.config_entry,
                    getResources().getStringArray(R.array.config_array));


            mConfigListView.setAdapter(arrayAdapter);

            if (mConfigStates == null)
            {
                mConfigStates = new boolean[arrayAdapter.getCount()];

                // Enable all options by default.
                Arrays.fill(mConfigStates, true);

                mConfigStates[10] = false;      //  redactions off by default
                mConfigStates[12] = false;      //  invert content in dark mode off by default
            }
        }

        mHandler = new Handler();

        String storageState = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(storageState) &&
            !Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))
        {
            showMessage(getResources().getString(R.string.no_media_warning),
                        getResources().getString(R.string.no_media_hint),
                        getResources().getString(R.string.dismiss));

            return;
        }

        String errorBase =
            "ChooseDocActivity experienced unexpected exception [%s]";

        try
        {
            // Search for a registered SecureFS class.
            mSecureFs = ArDkLib.getSecureFS();
            if (mSecureFs==null)
                throw new ClassNotFoundException();

            mSecurePath = mSecureFs.getSecurePath();
            mSecurePrefix = mSecureFs.getSecurePrefix();
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
            Log.i(mDebugTag, "SecureFS implementation unavailable");
        }

        if (mSecureFs != null)
        {
            /* Enumerate the root secure folder. */
            mDirectory = new File(mSecurePath);
        }
        else
        {
            /* Enumerate the downloads directory. */
            mDirectory = Environment.getExternalStoragePublicDirectory(
                                              Environment.DIRECTORY_DOWNLOADS);
        }

        mStartingDirectory = mDirectory;  //  remember where we started

        // Create the list...
        mListView = (ListView)findViewById(R.id.fileListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id)
            {
                onListItemClick(mListView, view, position, id);
            }
        });

        // Create a list adapter...
        mAdapter = new ChooseDocAdapter(getLayoutInflater(), this);
        mListView.setAdapter(mAdapter);

        // ...that is updated dynamically when files are scanned
        mUpdateFiles = new Runnable()
        {
            public void run()
            {
                Resources res  = getResources();
                String    appName = res.getString(R.string.app_name);
                String    version = res.getString(R.string.version);
                String    title   = res.getString(
                                            R.string.picker_title_App_Ver_Dir);

                setTitle(String.format(title, appName, version, mDirectory));

                mParent = mDirectory.getParentFile();

                mDirs = mDirectory.listFiles(new FileFilter()
                {
                    public boolean accept(File file)
                    {
                        return file.isDirectory();
                    }
                });

                if (mDirs == null)
                {
                    mDirs = new File[0];
                }

                mFiles = mDirectory.listFiles(new FileFilter()
                {
                    public boolean accept(File file)
                    {
                        if (file.isDirectory())
                            return false;

                        String fname = file.getName().toLowerCase();

                        //  show all file types supported by the SDK
                        if (Utilities.isDocTypeSupported(fname))
                            return true;

                        return false;
                    }
                });

                if (mFiles == null)
                {
                    mFiles = new File[0];
                }

                Arrays.sort(mFiles, new Comparator<File>()
                {
                    public int compare(File arg0, File arg1)
                    {
                        return arg0.getName().compareToIgnoreCase(
                                                               arg1.getName());
                    }
                });

                Arrays.sort(mDirs, new Comparator<File>()
                {
                    public int compare(File arg0, File arg1)
                    {
                        return arg0.getName().compareToIgnoreCase(
                                                               arg1.getName());
                    }
                });

                mAdapter.clear();

                //  add a button for going up one level
                if (mParent != null)
                {
                    if (!mDirectory.getAbsolutePath().
                            equals(mStartingDirectory.
                            getAbsolutePath()))
                    {
                        mAdapter.add(new ChooseDocItem(
                                           ChooseDocItem.Type.PARENT,
                                           getString(R.string.parent_directory),
                                           mParent.getAbsolutePath()));
                    }
                }

                for (File f : mDirs)
                {
                    mAdapter.add(new ChooseDocItem(ChooseDocItem.Type.DIR,
                                                   f.getName(),
                                                   f.getAbsolutePath()));
                }

                for (File f : mFiles)
                {
                    if (mSecureFs != null)
                    {
                        /*
                         * Replace the actual file path with a pseudo path
                         * from the nominal secure root.
                         *
                         * The SecureFS implementation will map this back to
                         * a valid fie path.
                         */
                        String path = f.getAbsolutePath().
                                        replace(mSecurePath, mSecurePrefix);

                        mAdapter.add(new ChooseDocItem(ChooseDocItem.Type.DOC,
                                                       f.getName(),
                                                       path));
                    }
                    else
                    {
                        mAdapter.add(new ChooseDocItem(ChooseDocItem.Type.DOC,
                                                       f.getName(),
                                                       f.getAbsolutePath()));
                    }
                }

                for (int i=0; i<mListView.getCount(); i++)
                {
                    // Get the current item view
                    View v = mListView.getChildAt(i);

                    // Uncheck the item
                    mListView.setItemChecked(i, false);

                    // Reset the item background colour
                    if (v != null)
                    {
                        v.setBackgroundColor(getResources().getColor(
                                                        R.color.WhiteSmoke));
                    }

                    ChooseDocItem item =
                        (ChooseDocItem)mListView.getAdapter().getItem(i);

                }
            }
        };

        //  Start initial file scan...
        mHandler.post(mUpdateFiles);

        // ...and observe the directory and scan files upon changes.
        FileObserver observer = new FileObserver(mDirectory.getPath(),
                                                 (FileObserver.CREATE |
                                                  FileObserver.DELETE))
        {
            public void onEvent(int event, String path)
            {
                mHandler.post(mUpdateFiles);
            }
        };
        observer.startWatching();
    }

    private void onListItemClick(ListView l, View v, int position, long id)
    {
        ChooseDocItem item = (ChooseDocItem) v.getTag();
        File f             = new File(item.path);

        if (item.type == ChooseDocItem.Type.PARENT ||
            item.type == ChooseDocItem.Type.DIR)
        {
            mDirectory = f;
            mHandler.post(mUpdateFiles);
            return;
        }

        //  User selected a file, process it.
        if (mExample ==
            ChooseExampleActivity.ExamplesEnum.DOCUMENT_EXPORT_TO_PNG)
        {
            new ExportFileAsPng().run(this,
                                      item.path,
                                      mSecureFs != null,
                                      mSecurePath,
                                      mSecurePrefix);
        }
        else
        {
            Uri uri = Uri.fromFile(f);

            Intent intent=null;
            if (mExample == ChooseExampleActivity.ExamplesEnum.DOCUMENT_EDITOR)
                intent = new Intent(this, AppNUIActivity.class);
            else if (mExample == ChooseExampleActivity.ExamplesEnum.DOCUMENT_EDITOR_CUSTOM_UI)
                intent = new Intent(this, DocEditorActivity.class);
            else if (mExample == ChooseExampleActivity.ExamplesEnum.DOCUMENT_EDITOR_CUSTOM_UI_KOTLIN)
                intent = new Intent(this, DocEditorActivityK.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);

            /*
             * This enables 'Save' in the UI for the document.
             *
             * Setting to true will require a "SaveAs" operation
             * to save the document.
             */
            intent.putExtra("IS_TEMPLATE", false);

            // Setup the document viewing configuration options
            ConfigOptions appCfgOpts = ArDkLib.getAppConfigOptions();

            /*
             * Disable the use of application shared preferences to store
             * document state.
             */
            appCfgOpts.setUsePersistentFileState(false);

            /*
             * Disable things that do not apply to this sample app
             */
            appCfgOpts.setOpenPdfInEnabled(false);
            appCfgOpts.setSaveAsPdfEnabled(false);
            appCfgOpts.setImageInsertEnabled(false);
            appCfgOpts.setPhotoInsertEnabled(false);
            appCfgOpts.setDocAuthEntryEnabled(false);
            appCfgOpts.setTrackChangesFeatureEnabled(false);

            for (int i=0; i<Array.getLength(mConfigStates); i++)
            {
                // Case values be kept in sync with 'config_array' resource.
                switch (i)
                {
                    // Editing Enable
                    case 0:
                    {
                        appCfgOpts.setEditingEnabled(mConfigStates[i]);
                        break;
                    }

                    // SaveAs Enable
                    case 1:
                    {
                        appCfgOpts.setSaveAsEnabled(mConfigStates[i]);
                        break;
                    }

                    // Open In Enable
                    case 2:
                    {
                        appCfgOpts.setOpenInEnabled(mConfigStates[i]);
                        break;
                    }

                    // Share Enable
                    case 3:
                    {
                        appCfgOpts.setShareEnabled(mConfigStates[i]);
                        break;
                    }

                    // External Clipboard In (Paste) Enable
                    case 4:
                    {
                        appCfgOpts.setExtClipboardInEnabled(
                                                        mConfigStates[i]);
                        break;
                    }

                    // External Clipboard Out (Cut/Copy) Enable
                    case 5:
                    {
                        appCfgOpts.setExtClipboardOutEnabled(
                                                        mConfigStates[i]);
                        break;
                    }

                    // Printing Enable
                    case 6:
                    {
                        appCfgOpts.setPrintingEnabled(mConfigStates[i]);
                        break;
                    }

                    // Launch External Url Enable
                    case 7:
                    {
                        appCfgOpts.setLaunchUrlEnabled(mConfigStates[i]);
                        break;
                    }

                    // Entry of Form Filling
                    case 8:
                    {
                        appCfgOpts.setFormFillingEnabled(mConfigStates[i]);
                        break;
                    }

                    // Entry of Form Signing
                    case 9:
                    {
                        appCfgOpts.setFormSigningFeatureEnabled(mConfigStates[i]);
                        break;
                    }

                    // Entry of Redactions
                    case 10:
                    {
                        appCfgOpts.setRedactionsEnabled(mConfigStates[i]);
                        break;
                    }

                    // Entry of Full screen
                    case 11:
                    {
                        appCfgOpts.setFullscreenEnabled(mConfigStates[i]);
                        break;
                    }

                    // Entry invert content in dark mode
                    case 12:
                    {
                        appCfgOpts.setInvertContentInDarkModeEnabled(mConfigStates[i]);
                        break;
                    }

                    default:
                    {
                        Log.e(mDebugTag, "Unknown Configuration Item Index [" +
                                         String.valueOf(i) +
                                         "]");
                        break;
                    }
                }
            }

            //  These two ConfigOption values are not represented this app's config states UI.
            //  You can use them to override the initial values for the thickness and color
            //  when drawing ink annotations.
            //
            //  The color format is ARGB.  Be sure to set the alpha value.
            //  for example, solid blue is 0xff0000ff.
            //  If not specified, we use 0xffff0000 (solid red)
            //
            //  For thickness, use a non-zero float value, in points.
            //  If not specified, we use 4.5.

//            appCfgOpts.setDefaultPdfInkAnnotationDefaultLineColor(0xff0000ff);
//            appCfgOpts.setDefaultPdfInkAnnotationDefaultLineThickness(10.0f);

            startActivity(intent);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // do another file scan to pick up changes to files since we were away
        if (mHandler != null)
        {
            mHandler.post(mUpdateFiles);
        }
    }


    private void showMessage(final String title,
                             final String body,
                             final String okLabel)
    {
        final Activity activity = this;

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                new AlertDialog.Builder(activity, R.style.AlertDialogTheme)
                        .setTitle(title)
                        .setMessage(body)
                        .setCancelable(false)
                        .setPositiveButton(okLabel,
                                           new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }
}
