package com.artifex.mupdf_test;

import android.app.Application;

import com.artifex.mupdf_test.pdf_sign.SampleSignerFactory;
import com.artifex.solib.ConfigOptions;
import com.artifex.sonui.editor.NUIDefaultSignerFactory;
import com.artifex.sonui.editor.Utilities;
import com.artifex.solib.ArDkLib;

/*
 * Application class for the mupdf_test
 *
 */

public class TestApp extends Application
{
    private static ConfigOptions  mAppCfgOptions = null;

    public static void setupDefaultConfig()
    {
        // Register configuration options for the application.
        if (mAppCfgOptions == null)
        {
            mAppCfgOptions = new ConfigOptions();
            ArDkLib.setAppConfigOptions(mAppCfgOptions);
        }
    }

    private void enableFeatures()
    {
        //  set up default config settings
        setupDefaultConfig();
    }

    @Override
    public void onCreate()
    {
        enableFeatures();

        // Register digital signature listeners (if required)
        // SmartOffice SODK provides a default implementation of PKCS7 signing
        // for PDF documents using naive Android certificate store and BouncyCastle
        // crypto APIs.
        //
        // You can enable this default implementation by changing the initialisation
        // of mSignerFactory (the digital signature factory) below to:
        //    final NUIDefaultSignerFactory mSignerFactory =  NUIDefaultSignerFactory.getInstance();
        //
        // If you would like to provide your own certificate store and signing management,
        // you should implmeent and register your own versions of the "SampleSignerFactory" and
        // associated classes in the pdf_sign directory of the app.
        //
        // Modify the code below to register the SampleSignerFactory, which provides a dummy
        // implementation of the signer, verifier and Certificate management classes
        // in the pdf_sign directory of the sample app. You should replace them with your own
        // certificate management, PKCS7 signing and verifying implementation.
        //
        //    final SampleSignerFactory mSignerFactory = SampleSignerFactory.getInstance();

        final NUIDefaultSignerFactory mSignerFactory = NUIDefaultSignerFactory.getInstance();

        if ( mSignerFactory != null )
            Utilities.setSigningFactoryListener( mSignerFactory );

        super.onCreate();
    }
}
