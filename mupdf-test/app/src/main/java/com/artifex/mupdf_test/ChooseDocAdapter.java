package com.artifex.mupdf_test;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

import com.artifex.solib.FileUtils;
import com.artifex.sonui.editor.Utilities;

class ChooseDocAdapter extends BaseAdapter
{
    private final LinkedList<ChooseDocItem> mItems;
    private final LayoutInflater mInflater;

    public ChooseDocAdapter(LayoutInflater inflater, Activity context)
    {
        mInflater = inflater;
        mItems    = new LinkedList<>();
    }

    public void clear()
    {
        mItems.clear();
    }

    public void add(ChooseDocItem item)
    {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public int getCount()
    {
        return mItems.size();
    }

    public Object getItem(int i)
    {
        return mItems.get(i);
    }

    public long getItemId(int arg0)
    {
        return 0;
    }

    private int iconForType(ChooseDocItem.Type type, String docName)
    {
        switch (type)
        {
            case PARENT:
                return R.drawable.ic_explorer_up;

            case DIR:
                return R.drawable.ic_explorer_fldr;

            case DOC:
                return Utilities.iconForDocExt(FileUtils.getExtension(docName));

            default:
                return 0;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v;
        if (convertView == null)
        {
            v = mInflater.inflate(R.layout.picker_entry, null);
        }
        else
        {
            v = convertView;
        }

        ChooseDocItem item = mItems.get(position);

        ((TextView) v.findViewById(R.id.name)).setText(item.name);
        ((ImageView) v.findViewById(R.id.icon)).
            setImageResource(iconForType(item.type, item.name));

        v.setTag(item);

        return v;
    }
}
