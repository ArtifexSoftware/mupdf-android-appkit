package com.artifex.mupdf_test.pdf_sign;

import android.app.Activity;
import android.content.Intent;

import com.artifex.mupdf.fitz.FitzInputStream;
import com.artifex.sonui.editor.NUICertificate;
import com.artifex.sonui.editor.NUICertificateStore;
import com.artifex.sonui.editor.NUIPKCS7Verifier;


import java.util.HashMap;

public class SampleVerifier extends NUIPKCS7Verifier {
    private Activity mActivity;
    protected int mResult;  // a PKCS7VerifierXXX value
    protected NUIPKCS7Verifier.NUIPKCS7VerifierListener mListener;

    private static int mSampleResultCode = PKCS7VerifierOK;

    public SampleVerifier( Activity ctx )
    {
        mActivity = ctx;
        mResult = PKCS7VerifierUnknown;
    }


    public int checkDigest(FitzInputStream stream, byte[] signature)
    {
        // At this point in the code "signature" contains the PKCS7 signature from the
        // document that we're trying to verify. "signature" is a DER encoded PKCS7
        // structure. "_dataToBeVerified" contains the document content data that "signature"
        // is to be verified against.
        //
        // The following logic needs to be implemented here by SmartOffice SDK users wishing
        // to integrate digital signature verifcation with their chosen security infrastructure APIs:
        //
        //   1) Decode "signature" by converting it from DER format to a PKCS7 structure
        //
        //   2) Test that "_dataToBeVerified" is consistent with "signature" i.e. that the
        //      document content used to compute "signature" is identical to "_dataToBeVerified".
        //
        //   3) If the test 2) fails return "PKCS7VerifierDigestFailure" to the caller
        //      to indicate that the document content has been changed after the document
        //      was signed.
        //
        //   4) Extract the signer certificate that was used to sign "signature" form the PKCS7
        //      version of "signature".  NUICertificate class will handle generation of the
        //      designatedName and extension details if available
        //
        //   5) Test that the signer certificate obtained at 4) is trusted by the chosen
        //      security infrastructure APIs.
        //
        //   6) If the test 5) is successful return "PKCS7VerifierOK" to the
        //      caller. If this test fails, depending on the reason for the failure,
        //      one of the following values should be returned to the caller to indicate
        //      why the signature verification failed:
        //
        //    PKCS7VerifierOK
        //    PKCS7VerifierNoSignature
        //    PKCS7VerifierNoCertificate
        //    PKCS7VerifierDigestFailure
        //    PKCS7VerifierSelfSigned
        //    PKCS7VerifierSelfSignedInChain
        //    PKCS7VerifierNotTrusted
        //    PKCS7VerifierUnknown

        // cycle through the allowable return codes on each successive call to verify()
        mResult = mSampleResultCode;
        mSampleResultCode++;
        if (mSampleResultCode > PKCS7VerifierNotTrusted)
            mSampleResultCode = PKCS7VerifierUnknown;

        // For the purpose of this sample application, we'll just invent a certificate,
        // and cycle through the various return values for this method
        // each time it's called. This code should be replaced by an implementation of the logic
        // steps 1) .. 6) as described above

        HashMap<String, String> details;

        NUICertificate certificate = SampleCertificateStore.getSampleSigningCertificate();
        // Get the designated name from the certificate.
        if (certificate != null)
            details = certificate.designatedName();
        else
            details = NUICertificate.defaultDetails();

        // Get the X509v3 extensions from the cerificate and append.
        HashMap<String, String> mV3ExtensionsDetails;

        if (certificate != null)
        {
            mV3ExtensionsDetails = certificate.v3Extensions();

            if (mV3ExtensionsDetails != null)
            {
                details.putAll(mV3ExtensionsDetails);
            }
        }

        // Get the validity from the certificate and append.
        HashMap<String, String> validityDetails;

        if (certificate != null)
        {
            validityDetails = certificate.validity();

            if (validityDetails != null)
            {
                details.putAll(validityDetails);
            }
        }
        if (mListener != null)
            mListener.onVerifyResult(details, mResult );

        presentResults(details, mResult);

        return mResult;
    }

    public int checkCertificate(byte[] signature)
    {
        // Use your chosen security model to check the validity of the certificate,
        // check revocation status or any other checks required by your PKI setup.

        return PKCS7VerifierOK;
    }

    public void presentResults(HashMap<String, String> details, int result )
    {
        // kick off a certificate viewer activity
        Intent intent= new Intent(mActivity, SampleCertificateViewer.class);
        intent.putExtra("certificateDetails", details);
        intent.putExtra("verifyResult", result);
        intent.putExtra("updatedSinceSigning", (result == PKCS7VerifierDigestFailure) ? 1 : 0);
        mActivity.startActivity(intent);
    }

    @Override
    public void certificateUpdated()
    {
        // the certificate has been updated, so update the designated name if required
        // In most scenarios this method would not be required, but some certificate stores provide
        // tokens for certificates which can later be used to retrieve certificate information.
        // In that scenario you should call this method and update the distinguished name
    }

    // Start the verification process
    public void doVerify(final NUIPKCS7Verifier.NUIPKCS7VerifierListener listener, int signatureValidity ) {
        mListener = listener;
        mSignatureValidity = signatureValidity;

        listener.onInitComplete();
    }

}
