package com.artifex.mupdf_test.pdf_sign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.artifex.sonui.editor.NUICertificatePicker;

public class SampleCertificatePicker extends NUICertificatePicker
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // instantiate a new SampleCertificateStore instance and populate our Picker dialog
        // with the certificates found in the store.
        populate(new SampleCertificateStore());
    }

    public static void show (final Activity activity,
                             final SampleCertificatePicker.NUICertificatePickerListener listener)
    {
        mListener = listener;

        Intent intent= new Intent(activity, SampleCertificatePicker.class);
        activity.startActivity(intent);
    }
}
