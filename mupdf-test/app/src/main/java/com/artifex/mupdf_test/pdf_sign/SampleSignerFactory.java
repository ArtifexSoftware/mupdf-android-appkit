package com.artifex.mupdf_test.pdf_sign;

import android.app.Activity;

import com.artifex.sonui.editor.NUIPKCS7Signer;
import com.artifex.sonui.editor.NUIPKCS7Verifier;
import com.artifex.sonui.editor.Utilities;

public class SampleSignerFactory implements Utilities.SigningFactoryListener {
    private static SampleSignerFactory mInstance;

    // Static Methods ---------------------------------------------------------
    public static SampleSignerFactory getInstance() {
        if (mInstance == null) {
            synchronized (SampleSignerFactory.class) {
                mInstance = new SampleSignerFactory();
            }
        }
        return mInstance;
    }


    public NUIPKCS7Signer getSigner(Activity context )
    {
        SampleSigner signer = new SampleSigner( context );
        return signer;
    }

    public NUIPKCS7Verifier getVerifier(Activity context )
    {
        SampleVerifier verifier = new SampleVerifier( context );
        return verifier;
    }

}
