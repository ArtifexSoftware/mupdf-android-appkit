package com.artifex.mupdf_test;

import android.os.Bundle;

import com.artifex.sonui.editor.NUIActivity;
import com.artifex.sonui.editor.ViewingState;

public class AppNUIActivity extends NUIActivity
{
    //  optional viewing state
    ViewingState mViewingState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        MainActivity.setupApplicationSpecifics(this);

        //  OPTIONAL
        //  Use ViewingState to tell NUIActivity what the initial viewing state should be,
        //  page, scroll values, and scale factor.
        //  An app can retrieve the values from its own storage, create an instance,
        //  and provide it by calling setViewingState().
        //  see also onPause, below.

//        mViewingState = new ViewingState();
//        mViewingState.pageNumber = ??;
//        mViewingState.scale = ??;
//        mViewingState.scrollX = ??;
//        mViewingState.scrollY = ??;
//        mViewingState.pageListVisible = ??;
//        setViewingState(mViewingState);

        super.onCreate(savedInstanceState);
    }
}
