package com.artifex.mupdf_test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.artifex.solib.ArDkLib;
import com.artifex.solib.FileUtils;
import com.artifex.sonui.editor.Utilities;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //  create/register handlers
        setupApplicationSpecifics(this);

        //  just show the document chooser activity
        Intent intent = new Intent(this, ChooseExampleActivity.class);
        startActivity(intent);
        finish();
    }

    private static boolean isSetup = false;
    public static void setupApplicationSpecifics(Context ctx)
    {
        //  create/register handlers (but only once)
        if (!isSetup)
        {
            Utilities.setDataLeakHandlers(new DataLeakHandlers());
            Utilities.setPersistentStorage(new PersistentStorage());
            ArDkLib.setClipboardHandler(new ClipboardHandler());
            ArDkLib.setSecureFS(new SecureFS());
            FileUtils.init(ctx);

            isSetup = true;
        }
    }
}
