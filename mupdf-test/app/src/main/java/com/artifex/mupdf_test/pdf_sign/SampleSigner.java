package com.artifex.mupdf_test.pdf_sign;

import android.app.Activity;
import android.util.Log;

import com.artifex.mupdf.fitz.FitzInputStream;
import com.artifex.mupdf.fitz.PKCS7DesignatedName;
import com.artifex.sonui.editor.NUICertificatePicker;
import com.artifex.sonui.editor.NUIPKCS7Signer;

public class SampleSigner extends NUIPKCS7Signer {

    private Activity mActivity;
    protected String mSerial;
    protected PKCS7DesignatedName mDesignatedName;

    public SampleSigner( Activity ctx )
    {
        mActivity = ctx;
    }

    // Get the signers designated name
    @Override
    public PKCS7DesignatedName name()
    {
        return mDesignatedName;
    }

    @Override
    public byte[] sign( FitzInputStream strm )
    {
        // sign the data supplied in 'strm' and return the digest
        // This method is called a the end of the signing process.
        // "strm" is a specialisation of InputStream containing the document
        // data to be signed.
        // A DER encoded PKCS7 signature should be returned to the caller

        // The following logic needs to be implemented here by SmartOffice SDK users:
        //
        //   1) Compute a PKCS7 digital signature of the data in "strm" using an X509 version
        //      of the selected certificate and it's corresponding private key as inputs to
        //      this computation
        //
        //   2) Return to the caller a DER encoded version of the PKCS7 digital signature
        //      computed in step 1)

        // For the purpose of this sample application, we'll just return a dummy digest.
        // This code should be replaced by your implementation of the logic steps 1) .. 2) as described
        // using your chosen security infrastructure APIs.

        String digestString = "This is some random string for the digest";

        return digestString.getBytes();
    }

    @Override
    public int maxDigest()
    {
        // Inform the PDF signer of the maximum space to allocate for the digest
        return 7 * 1024;
    }

    // Start the signing process. this method will be called by the SODK when the user has clicked
    // on a PDF signature form field which has not yet been signed.
    // You must provide a method of selecting a suitable X509 certificate using a
    // NUICertificatePicker-derived class, in this case SampleCertificatePicker
    public void doSign(final NUIPKCS7SignerListener listener )
    {
        if (mSerial == null) {
            SampleCertificatePicker.show(mActivity, new NUICertificatePicker.NUICertificatePickerListener() {
                @Override
                public void onOK(final String serial, PKCS7DesignatedName designatedName) {
                    // we have selected a certificate, now sign the content
                    Log.d("PDFSIGN", "Selected Certificate Serial: [" + serial + "]");

                    mSerial = serial;
                    mDesignatedName = designatedName;

                    listener.onSignatureReady();
                }

                @Override
                public void onCancel() {
                    //  user cancelled the chooser
                    listener.onCancel();
                }
            });
        }
        else
        {
            // we already selected a certificate - use that.
            listener.onSignatureReady();
        }

        return;
    }
}
