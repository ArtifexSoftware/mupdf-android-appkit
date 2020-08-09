package com.artifex.solib;

import android.graphics.Bitmap;
import android.graphics.Rect;
import java.lang.IllegalArgumentException;

public class ArDkBitmap implements Comparable<ArDkBitmap>
{
    public ArDkBitmap() {}

    /**
     * Pixel formats supported for output by SmartOffice.
     *
     * Note that the naming scheme is not consistent with Bitmap.Config.
     */
    public enum Type {
        A8,
        RGB555,
        RGB565,
        RGBA8888
    }

    protected void allocateBitmap(int w, int h, ArDkBitmap.Type type)
    {
        serialBase++;
        serial = serialBase;
        Bitmap.Config config;
        switch (type) {
            default:
            case RGB555:
            case RGB565:   config = Bitmap.Config.RGB_565;   break;
            case A8:       config = Bitmap.Config.ALPHA_8;   break;
            case RGBA8888: config = Bitmap.Config.ARGB_8888; break;
        }
        this.bitmap = Bitmap.createBitmap(w, h, config);
        this.rect   = new Rect(0, 0, w, h);
    }

    /**
     * Create an ArDkBitmap that gives access to a subarea of another ArDkBitmap (sharing the same
     * underlying native Bitmap).
     *
     * @param s     ArDkBitmap that we want to represent a subrectangle of.
     * @param x0    bound of sub rectangle.
     * @param y0    bound of sub rectangle.
     * @param x1    bound of sub rectangle.
     * @param y1    bound of sub rectangle.
     */
    public ArDkBitmap(ArDkBitmap s, int x0, int y0, int x1, int y1)
    {
        this.serial = s.serial;
        this.bitmap  = s.bitmap;
        this.rect = new Rect(x0, y0, x1, y1);
    }

    public ArDkBitmap(Bitmap bitmap)
    {
        //  create an ArDkBitmap from an Android Bitmap
        serialBase++;
        serial = serialBase;
        this.bitmap = bitmap;
        this.rect   = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     * Get the Android bitmap that underlies this ArDkBitmap.
     *
     * @return the android bitmap that underlies this ArDkBitmap.
     */
    public Bitmap getBitmap()
    {
        return bitmap;
    }

    /**
     * Get the subrectangle of the underlying Android Bitmap that corresponds to this ArDkBitmap.
     *
     * @return subrectangle of the underlying Android Bitmap that corresponds to this ArDkBitmap.
     */
    public Rect getRect()
    {
        return rect;
    }

    /**
     * Read the current width of this bitmap.
     *
     * @note If we ever move to an implementation using adjustAspectForSize, then this value
     * may change. Best to code for this case initially.
     *
     * @return The width of the ArDkBitmap.
     */
    public int getWidth()
    {
        return this.rect.right - this.rect.left;
    }

    /**
     * Read the current height of this bitmap.
     *
     * @note If we ever move to an implementation using adjustAspectForSize, then this value
     * may change. Best to code for this case initially.
     *
     * @return The height of the ArDkBitmap.
     */
    public int getHeight()
    {
        return this.rect.bottom - this.rect.top;
    }

    /**
     * Used to order ArDkBitmap objects in ordered Collections
     */
    public int compareTo (ArDkBitmap o)
    {
        int size = (bitmap == null)? 0:bitmap.getByteCount();
        int compareSize = (o.bitmap== null)? 0:o.bitmap.getByteCount();
        if (size > compareSize)
            return 1;
        if (size < compareSize)
            return -1;
        if (serial > o.serial)
            return 1;
        if (serial < o.serial)
            return -1;
        return 0;
    }

    /** Recycle the bitmap within the ArDkBitmap
     *  Intended to be called just before the ArDkBitmap reference is set to null
     */
    public void dispose()
    {
        if (!bitmap.isRecycled())
            bitmap.recycle();
    }

    /**
     *  return the serial number
     */
    public int getSerial()
    {
        return serial;
    }

    /* Private implementation and internal data. */
    protected static int serialBase = 0;
    protected int      serial;
    protected Bitmap   bitmap;
    protected Rect     rect;

}
