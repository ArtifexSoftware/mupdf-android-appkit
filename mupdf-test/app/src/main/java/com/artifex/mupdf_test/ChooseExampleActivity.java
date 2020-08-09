package com.artifex.mupdf_test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseExampleActivity extends Activity
{
    public enum ExamplesEnum
    {
        DOCUMENT_EXPORT_TO_PNG,
        DOCUMENT_EDITOR,
        DOCUMENT_EDITOR_CUSTOM_UI,
        DOCUMENT_EDITOR_CUSTOM_UI_KOTLIN
    }

    private ListView mListView;

    private void onListItemClick(ListView l, View v, int position, long id)
    {
        ExamplesEnum example;

        // These values must be kept in sync with the 'examples_array' resource.
        switch ((int)id)
        {
            case 0:
            {
                example = ExamplesEnum.DOCUMENT_EXPORT_TO_PNG;
                break;
            }
            case 1:
            {
                example = ExamplesEnum.DOCUMENT_EDITOR;
                break;
            }
            case 2:
            {
                example = ExamplesEnum.DOCUMENT_EDITOR_CUSTOM_UI;
                break;
            }
            case 3:
            {
                example = ExamplesEnum.DOCUMENT_EDITOR_CUSTOM_UI_KOTLIN;
                break;
            }
            default:
            {
                return;
            }
        }

        Intent intent = new Intent(this, ChooseDocActivity.class);
        intent.putExtra("EXAMPLE_ID", example);

        startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.choose_example);

        // Set the list view title
        TextView tv = (TextView) findViewById(R.id.examplesListViewTitle);
        tv.setText(R.string.examples_titles);

        mListView = (ListView) findViewById(R.id.examplesListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id)
            {
                onListItemClick(mListView, view, position, id);
            }
        });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.example_entry,
                getResources().getStringArray(R.array.examples_array));

        mListView.setAdapter(arrayAdapter);
    }
}
