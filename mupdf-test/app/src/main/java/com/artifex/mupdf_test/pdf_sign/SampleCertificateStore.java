package com.artifex.mupdf_test.pdf_sign;

import com.artifex.sonui.editor.NUICertificate;
import com.artifex.sonui.editor.NUICertificateStore;

public class SampleCertificateStore extends NUICertificateStore {

    private NUICertificate[] mCerts = new NUICertificate[3];

    // Certificate Store constructor
    public SampleCertificateStore()
    {
        initialise();
    }

    // initialise
    public void initialise()
    {
        NUICertificate signingCert = new NUICertificate();
        signingCert.alias = "green";
        signingCert.issuer = "BigBrother";
        signingCert.serial = "0011223344";
        signingCert.isValid = false;
        signingCert.subject = "CN=Test Cert Green, OU=Shipping Dept, O=Green Corp, L=Buenos Aires, C=Argentina";
        signingCert.subjectAlt = "email:info@green.com";
        signingCert.usage = "Digital Signature, Non Repudiation";
        signingCert.notAfter = "1672506436";
        mCerts[0] = signingCert;

        signingCert = new NUICertificate();
        signingCert.alias = "orange";
        signingCert.issuer = "BigBrother";
        signingCert.serial = "1122334455";
        signingCert.isValid = true;
        signingCert.subject = "CN=Test Cert Orange, OU=HR Dept, O=Orange Corp, L=Nagasaki, C=Japan";
        signingCert.subjectAlt = "email:info@orange.com";
        signingCert.usage = "Digital Signature, Non Repudiation";
        signingCert.notAfter = "1672506436";
        mCerts[1] = signingCert;

        signingCert = new NUICertificate();
        signingCert.alias = "blue";
        signingCert.issuer = "BigBrother";
        signingCert.serial = "2233445566";
        signingCert.isValid = true;
        signingCert.subject = "CN=Test Cert Blue, OU=Manufacturing Dept, O=Blue Corp, L=Mombasa, C=Kenya";
        signingCert.subjectAlt = "email:info@blue.com";
        signingCert.usage = "Digital Signature";
        signingCert.notAfter = "1672506436";
        mCerts[2] = signingCert;
    }

    // Retrieve list of certificates
    public NUICertificate[] getAllCertificates()
    {
        // Read all X509 certificates from your certificate store.
        // For the purpose of this sample application, we'll populate "mCerts"
        // with some static test data. This code should be replaced with code to extract
        // details of all available certificates capable of signing documents if you
        // wish to integrate digital signing with your chosen security infrastructure APIs

        return mCerts;
    }

    // Retrieve list of signing certificates
    public NUICertificate[] getSigningCertificates()
    {
        // Read all X509 signing certificates from your certificate store.
        // For the purpose of this sample application, we'll populate "mCerts"
        // with some static test data. This code should be replaced with code to extract
        // details of all available certificates capable of signing documents if you
        // wish to integrate digital signing with your chosen security infrastructure APIs
        // You may want to filter based on the keyUsage field for Digital Signature
        // and/or Non Repudiation based on your PKI requirements.

        return mCerts;
    }

    // Retrieve a sample X509 signing certificate
    // (Note: this method is for demonstration purposes only)
    public static NUICertificate getSampleSigningCertificate()
    {
        NUICertificate signingCert = new NUICertificate();
        signingCert.alias = "red";
        signingCert.issuer = "BigRed";
        signingCert.serial = "6666666666";
        signingCert.isValid = true;
        signingCert.subject = "CN=Test Cert Red, OU=Repo Dept, O=Red Corp, L=Classified, C=Redacted";
        signingCert.subjectAlt = "email:info@red.com";
        signingCert.usage = "Digital Signature";
        signingCert.notAfter = "1672506436";

        return signingCert;
    }

    // Retrieve list of auxiliary certificates
    public NUICertificate[] getAuxCertificates( NUICertificate cert )
    {
        NUICertificate auxCert = new NUICertificate();
        // Read auxiliary certificates for the supplied certificate from
        // your certificate store.
        // We create a dummy certificate list here with one dummy certificate.
        NUICertificate[] certs = new NUICertificate[1];
        certs[0] = cert;

        return certs;
    }

    // Free up resources
    @Override
    public void finalize()
    {
    }

}
