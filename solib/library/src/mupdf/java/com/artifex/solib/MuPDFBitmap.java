package com.artifex.solib;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class MuPDFBitmap extends ArDkBitmap
{
    public MuPDFBitmap(int w, int h)
    {
        serialBase++;
        serial = serialBase;
        this.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        this.rect   = new Rect(0, 0, w, h);
    }
}
