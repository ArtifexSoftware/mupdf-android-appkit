package com.artifex.mupdf_test;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;

import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.artifex.solib.ArDkLib;
import com.artifex.solib.FileUtils;
import com.artifex.solib.SODocSaveListener;

import com.artifex.sonui.editor.DocumentListener;
import com.artifex.sonui.editor.DocumentView;
import com.artifex.sonui.editor.Utilities;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class DocEditorActivity extends AppCompatActivity {
    private Uri mUri;
    private DocumentView mDocumentView = null;
    private DocEditorActivity mActivity;
    private float mOriginalScale = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.setupApplicationSpecifics(this);

        super.onCreate(savedInstanceState);

        //  This example activity can either use the SDK's built-in UI (default), or a developer can
        //  supply their own (custom). This activity has examples of both.

        //  choose one:
//        useDefaultUI();  //  use the SDK's built-in UI
        useCustomUI();  //  use a UI provided by the developer
    }

    private void useDefaultUI() {

        //  set up UI
        setContentView(com.artifex.sonui.editor.R.layout.sodk_editor_doc_view_activity);

        //  find the DocumentView component
        mDocumentView = findViewById(com.artifex.sonui.editor.R.id.doc_view);

        mDocumentView.setDocConfigOptions(ArDkLib.getAppConfigOptions());
        mDocumentView.setDocDataLeakHandler(Utilities.getDataLeakHandlers());

        //  set an optional listener for document events
        mDocumentView.setDocumentListener(new DocumentListener() {
            @Override
            public void onPageLoaded(int pagesLoaded) {
                //  called when another page is loaded from the document.
            }

            @Override
            public void onDocCompleted() {
                //  called when the document is done loading.
            }

            @Override
            public void onPasswordRequired() {
                //  called when a password is required.
            }

            @Override
            public void onViewChanged(float scale, int scrollX, int scrollY, Rect selectionRect) {
                //  called when the scale, scroll, or selection in the document changes.
            }
        });

        //  set a listener for when the document view is closed.
        //  typically you'll use it to close your activity.
        mDocumentView.setOnDoneListener(new DocumentView.OnDoneListener() {
            @Override
            public void done() {
                DocEditorActivity.super.finish();
            }
        });

        //  get the URI for the document
        mUri = getIntent().getData();

        //  open it, specifying showUI = true;
        mDocumentView.start(mUri, 0, true);
    }

    private void useCustomUI() {
        mActivity = this;

        //  get the document view
        setContentView(R.layout.activity_layout);
        mDocumentView = findViewById(R.id.doc_view);

        mDocumentView.setDocConfigOptions(ArDkLib.getAppConfigOptions());
        mDocumentView.setDocDataLeakHandler(Utilities.getDataLeakHandlers());

        //  set a listener for document events
        mDocumentView.setDocumentListener(new DocumentListener() {
            @Override
            public void onPageLoaded(int pagesLoaded) {
                //  called when another page is loaded from the document.
                if (mOriginalScale==-1)
                    mOriginalScale = mDocumentView.getScaleFactor();
                Log.d("DocumentListener", "onPageLoaded pages= " + mDocumentView.getPageCount());
                updateUI();
            }

            @Override
            public void onDocCompleted() {
                //  called when the document is done loading.
                Log.d("DocumentListener", "onDocCompleted pages= " + mDocumentView.getPageCount());
                updateUI();
            }

            @Override
            public void onPasswordRequired() {
                //  called when a password is required.
                Log.d("DocumentListener", "onPasswordRequired");
                handlePassword();
            }

            @Override
            public void onViewChanged(float scale, int scrollX, int scrollY, Rect selectionRect) {
                //  called when the scale, scroll, or selection in the document changes.
                Log.d("DocumentListener", "onViewportChanged");
            }
        });

        //  set a listener for when the DocumentView is closed.
        //  typically you'll want to close your activity.
        mDocumentView.setOnDoneListener(new DocumentView.OnDoneListener()
        {
            @Override
            public void done()
            {
                DocEditorActivity.super.finish();
            }
        });

        //  get the URI for the document to open
        mUri = getIntent().getData();

        //  open it, specifying showUI = false;
        mDocumentView.start(mUri, 0, false);

        //  ... and set up our own UI
        setupUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mDocumentView != null)
            mDocumentView.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupUI() {

        //  This function does the work of binding buttons in the UI
        //  to the API of DocumentView.

        findViewById(R.id.toggle_pages).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    if (mDocumentView.isPageListVisible())
                        mDocumentView.hidePageList();
                    else
                        mDocumentView.showPageList();
                }
            }
        });

        findViewById(R.id.toggle_draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    if (mDocumentView.isDrawModeOn())
                        mDocumentView.setDrawModeOff();
                    else
                        mDocumentView.setDrawModeOn();
                }
            }
        });

        findViewById(R.id.delete_selection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null)
                    mDocumentView.deleteSelection();
            }
        });

        findViewById(R.id.line_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  this is a sample dialog for choosing a color.
                final Context context = DocEditorActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Line Color");
                String[] colorNames = {"red", "green", "blue"};
                final int colors[] = {0xffff0000, 0xff00ff00, 0xff0000ff};
                builder.setItems(colorNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //   color chosen, so set it
                        if (mDocumentView != null)
                            mDocumentView.setLineColor(colors[which]);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        findViewById(R.id.line_thickness).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  this is a sample dialog for choosing a line thickness
                final Context context = DocEditorActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Line Thickness");
                String[] sizeNames = {"small", "medium", "large"};
                final float sizes[] = {1, 8, 24};
                builder.setItems(sizeNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  value chose, now set it.
                        if (mDocumentView != null)
                            mDocumentView.setLineThickness(sizes[which]);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        findViewById(R.id.full_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  hide this activity's UI
                findViewById(R.id.ui_layout).setVisibility(View.GONE);
                //  clear the search text
                EditText editText = findViewById(R.id.search_text);
                editText.getText().clear();
                //  put DocumentView in full screen mode
                if (mDocumentView != null) {
                    mDocumentView.enterFullScreen(new Runnable() {
                        @Override
                        public void run() {
                            //  restore our UI
                            findViewById(R.id.ui_layout).setVisibility(View.VISIBLE);
                            updateUI();
                        }
                    });
                }
            }
        });

        findViewById(R.id.highlight_selection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null)
                    mDocumentView.highlightSelection();
            }
        });

        //  set up a runnable to handle UI updates triggers by DocumentView
        if (mDocumentView != null) {
            mDocumentView.setOnUpdateUI(new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            });
        }

        final EditText searchText = findViewById(R.id.search_text);
        Button searchNextButton = findViewById(R.id.search_next);
        Button searchPrevButton = findViewById(R.id.search_previous);

        //  search
        if (mDocumentView.hasSearch()) {
            searchNextButton.setText("Find ->");
            searchNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mDocumentView != null) {
                        String text = searchText.getText().toString();
                        mDocumentView.searchForward(text);
                    }
                }
            });

            searchPrevButton.setText("<- Find");
            searchPrevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mDocumentView != null) {
                        String text = searchText.getText().toString();
                        mDocumentView.searchBackward(text);
                    }
                }
            });
        }
        else {
            searchText.setEnabled(false);
            searchNextButton.setEnabled(false);
            searchPrevButton.setEnabled(false);
        }

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.save();
                    updateUI();
                }
            }
        });

        findViewById(R.id.save_as).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {

                    //  get a new path.  The customer would provide a way to specify a new
                    //  name and location.
                    File f = new File(mUri.getPath());
                    String newPath = f.getParentFile().getPath() + "/testFile.pdf";

                    //  check for collision, the SDK does not.
                    if (FileUtils.fileExists(newPath)) {
                        showMessage("The file " + newPath + " already exists");
                    }
                    else
                    {
                        //  save it
                        mDocumentView.saveTo(newPath, new SODocSaveListener() {
                            @Override
                            public void onComplete(int result, int err) {
                                if (result == SODocSave_Succeeded) {
                                    //  success
                                    showMessage("The file was saved.");
                                } else {
                                    //  error
                                    showMessage("There was an error saving the file.");
                                }
                                updateUI();
                            }
                        });
                    }
                }
            }
        });

        findViewById(R.id.print).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.print();
                }
            }
        });

        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    //  save a copy of the document
                    final File file = new File(getFilesDir().toString() + File.separator + new File(mUri.getPath()).getName());
                    mDocumentView.saveTo(file.getPath(), new SODocSaveListener() {
                        @Override
                        public void onComplete(int result, int err) {
                            //  share it
                            showMessage("Sharing is application-specific, and should be implemented by the developer.\n\nSee DocEditorActivity.setupUI");
                            file.delete();
                        }
                    });
                }
            }
        });

        findViewById(R.id.toggle_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    boolean noteMode = mDocumentView.isNoteModeOn();
                    if (noteMode)
                        mDocumentView.setNoteModeOff();
                    else
                        mDocumentView.setNoteModeOn();
                }

            }
        });

        findViewById(R.id.author).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null)
                    mDocumentView.author();
            }
        });

        findViewById(R.id.first_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null)
                    mDocumentView.firstPage();
            }
        });

        findViewById(R.id.last_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null)
                    mDocumentView.lastPage();
            }
        });

        Button historyNextButton = findViewById(R.id.history_next);
        historyNextButton.setText("History ->");
        historyNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.historyNext();
                }
            }
        });

        Button historyPrevButton = findViewById(R.id.history_previous);
        historyPrevButton.setText("<- History");
        historyPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.historyPrevious();
                }
            }
        });

        findViewById(R.id.table_of_contents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.tableOfContents();
                }
            }
        });

        findViewById(R.id.table_of_contents2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableOfContents2();
            }
        });

        findViewById(R.id.redact_mark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.redactMarkText();
                }
            }
        });

        findViewById(R.id.redact_mark_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.redactMarkArea();
                }
            }
        });

        findViewById(R.id.redact_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.redactRemove();
                }
            }
        });

        findViewById(R.id.redact_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    mDocumentView.redactApply();
                }
            }
        });

        final EditText pageNumberText = findViewById(R.id.page_number);
        findViewById(R.id.goto_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocumentView != null) {
                    try {
                        int pageNum = Integer.parseInt(pageNumberText.getText().toString());
                        pageNumberText.getText().clear();
                        mDocumentView.goToPage(pageNum-1);
                    }
                    catch (Exception e) {
                        showMessage("Please enter a valid integer");
                    }
                }
            }
        });

        if (mDocumentView !=null) {
            mDocumentView.setPageChangeListener(new DocumentView.ChangePageListener() {
                @Override
                public void onPage(int pageNumber) {
                    int page = pageNumber+1;
                    int count = mDocumentView.getPageCount();
                    String text = String.format("page %d of %d", page, count);
                    TextView view = findViewById(R.id.page_display);
                    view.setText(text);
                    view.measure(0, 0);
                    view.requestLayout();
                }
            });
        }

        //  establish a message handler
        final DocEditorActivity thisActivity = this;
        Utilities.setMessageHandler(new Utilities.MessageHandler() {
            @Override
            public void showMessage(String title, String body, String okLabel, final Runnable whenDone) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(thisActivity);
                dialog.setTitle(title);
                dialog.setMessage(body);
                dialog.setCancelable(false);
                dialog.setPositiveButton(okLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (whenDone!=null)
                            whenDone.run();
                    }
                });
                dialog.create().show();
            }

            @Override
            public void yesNoMessage(String title, String body, String yesButtonLabel, String noButtonLabel, final Runnable yesRunnable, final Runnable noRunnable) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(thisActivity);
                dialog.setTitle(title);
                dialog.setMessage(body);
                dialog.setCancelable(false);
                dialog.setPositiveButton(yesButtonLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (yesRunnable!=null)
                            yesRunnable.run();
                    }
                });
                dialog.setNegativeButton(noButtonLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (noRunnable!=null)
                            noRunnable.run();
                    }
                });
                dialog.create().show();
            }
        });

        findViewById(R.id.scale_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scaleBy(0.20f);
            }
        });

        findViewById(R.id.scale_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scaleBy(-0.20f);
            }
        });

        findViewById(R.id.scroll_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollBy(-100);
            }
        });

        findViewById(R.id.scroll_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollBy(100);
            }
        });

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDocumentView.setScaleAndScroll(mOriginalScale, 0, 0);
            }
        });

        //  Tabs
        findViewById(R.id.file_tab_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTabButton(R.id.file_tab_button, R.id.file_tab);
            }
        });

        //  Tabs
        findViewById(R.id.pages_tab_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTabButton(R.id.pages_tab_button, R.id.pages_tab);
            }
        });

        //  Tabs
        findViewById(R.id.redact_tab_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTabButton(R.id.redact_tab_button, R.id.redact_tab);
            }
        });

        //  Tabs
        findViewById(R.id.annotate_tab_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTabButton(R.id.annotate_tab_button, R.id.annotate_tab);
            }
        });

        //  Tabs
        findViewById(R.id.other_tab_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTabButton(R.id.other_tab_button, R.id.other_tab);
            }
        });

        findViewById(R.id.scale_tab_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTabButton(R.id.scale_tab_button, R.id.scale_tab);
            }
        });

        onClickTabButton(R.id.file_tab_button, R.id.file_tab);
    }

    private void scrollBy(int amount)
    {
        //  collect the scale and position values
        float scale = mDocumentView.getScaleFactor();
        int sx = mDocumentView.getScrollPositionX();
        int sy = mDocumentView.getScrollPositionY();
        //  add the amount
        sy += amount;
        //  set the new values
        mDocumentView.setScaleAndScroll(scale, sx, sy);
    }

    private void scaleBy(float increment)
    {
        //  collect the scale and position values
        float scale = mDocumentView.getScaleFactor();
        int sx = mDocumentView.getScrollPositionX();
        int sy = mDocumentView.getScrollPositionY();

        //  new scale factor
        float newScale = scale + increment;

        //  Applications may want to adjust scroll values
        //  in an attempt to maintain focus on a particular point.
        int newSx = sx;
        int newSy = sy;
        mDocumentView.setScaleAndScroll(newScale, newSx, newSy);
    }

    private void onClickTabButton(int buttonId, int tabId)
    {
        //  a tab button ws clicked.

        //  show the given tab
        findViewById(R.id.file_tab).setVisibility(View.GONE);
        findViewById(R.id.pages_tab).setVisibility(View.GONE);
        findViewById(R.id.redact_tab).setVisibility(View.GONE);
        findViewById(R.id.annotate_tab).setVisibility(View.GONE);
        findViewById(R.id.other_tab).setVisibility(View.GONE);
        findViewById(R.id.scale_tab).setVisibility(View.GONE);
        findViewById(tabId).setVisibility(View.VISIBLE);

        findViewById(R.id.file_tab_button).setSelected(false);
        findViewById(R.id.pages_tab_button).setSelected(false);
        findViewById(R.id.redact_tab_button).setSelected(false);
        findViewById(R.id.annotate_tab_button).setSelected(false);
        findViewById(R.id.other_tab_button).setSelected(false);
        findViewById(R.id.scale_tab_button).setSelected(false);
        findViewById(buttonId).setSelected(true);
    }

    private void setToggleButtonText(Button button, Boolean val, String text1, String text2) {
        if (val)
            button.setText(text1);
        else
            button.setText(text2);
        button.measure(0, 0);
        button.requestLayout();
    }

    private void updateUI() {

        //  this is called every time the UI should be updated.
        //  triggered internally by the selection changing (mostly).

        if (mDocumentView != null) {
            //  pages button
            Button pagesButton = findViewById(R.id.toggle_pages);
            boolean visible = mDocumentView.isPageListVisible();
            setToggleButtonText(pagesButton, visible, "Hide Pages", "Show Pages");

            //  delete selection button
            findViewById(R.id.delete_selection).setEnabled(mDocumentView.canDeleteSelection());

            //  draw on/off button
            boolean inkDrawMode = mDocumentView.isDrawModeOn();
            Button drawButton = findViewById(R.id.toggle_draw);
            setToggleButtonText(drawButton, inkDrawMode, "Draw Off", "Draw On");

            //  line color and thickness buttons
            findViewById(R.id.line_color).setEnabled(inkDrawMode);
            findViewById(R.id.line_thickness).setEnabled(inkDrawMode);

            //  highlight button
            findViewById(R.id.highlight_selection).setEnabled(mDocumentView.isAlterableTextSelection());

            //  save button
            findViewById(R.id.save).setEnabled(mDocumentView.isDocumentModified());

            //  note on/off button
            boolean noteMode = mDocumentView.isNoteModeOn();
            Button noteButton = findViewById(R.id.toggle_note);
            setToggleButtonText(noteButton, noteMode, "Note Off", "Note On");

            //  history buttons
            findViewById(R.id.history_next).setEnabled(mDocumentView.hasNextHistory());
            findViewById(R.id.history_previous).setEnabled(mDocumentView.hasPreviousHistory());

            //  TOC button
            findViewById(R.id.table_of_contents).setEnabled(mDocumentView.isTOCEnabled());
            findViewById(R.id.table_of_contents2).setEnabled(mDocumentView.isTOCEnabled());

            //  page number - see DocumentView.ChangePageListener, above

            //  redaction
            findViewById(R.id.redact_mark).setEnabled(mDocumentView.canMarkTextRedaction());
            findViewById(R.id.redact_remove).setEnabled(mDocumentView.canRemoveRedaction());
            findViewById(R.id.redact_apply).setEnabled(mDocumentView.canApplyRedactions());

            Button markAreaButton = findViewById(R.id.redact_mark_area);
            boolean isMarking = mDocumentView.redactIsMarkingArea();
            setToggleButtonText(markAreaButton, isMarking, "Marking...", "Mark Area");
        }
    }

    private void handlePassword() {
        //  this function presents a dialog to collect the password, and then
        //  calls into DocumentView to provide it.
        //  if the password is wrong, we'll come here again.
        Context context = mActivity;

        AlertDialog.Builder dBuilder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        dBuilder.setTitle("Password:");
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        dBuilder.setView(input);

        // Set up the buttons
        dBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  provide the password.
                        mDocumentView.providePassword(input.getText().toString());
                    }
                });

        dBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel the operation.
                        dialog.cancel();
                        mActivity.finish();
                    }
                });

        dBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // Cancel the operation.
                dialog.cancel();
                mActivity.finish();
            }
        });

        dBuilder.show();
    }

    public class TOCEntry {
        public int handle;
        public int parentHandle;
        public String label;
        private String url;
        private int page;
        private float x;
        private float y;

        TOCEntry(int handle, int parentHandle, int page, String label, String url, float x, float y) {
            this.handle = handle;
            this.parentHandle = parentHandle;
            this.page = page;
            this.label = label;
            this.url = url;
            this.x = x;
            this.y = y;
        }
    }

    private void tableOfContents2() {
        // use our own UI

        //  get the TOC entries
        final ArrayList<TOCEntry> entries = new ArrayList<>();
        mDocumentView.enumeratePdfToc(new DocumentView.EnumeratePdfTocListener() {
            @Override
            public void nextTocEntry(int handle, int parentHandle, int page, String label, String url, float x, float y) {
                TOCEntry entry = new TOCEntry(handle, parentHandle, page, label, url, x, y);
                entries.add(entry);
            }
        });

        //  make corresponding array of strings
        String[] labels = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++)
            labels[i] = entries.get(i).label;

        //  show them in a simple dialog
        final Context context = DocEditorActivity.this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Table Of Contents");
        builder.setItems(labels, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                TOCEntry entry = entries.get(which);
                if (entry.page >= 0) {
                    RectF box = new RectF(entry.x, entry.y, entry.x + 1, entry.y + 1);
                    mDocumentView.gotoInternalLocation(entry.page, box);
                } else if (entry.url != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.url));
                    startActivity(browserIntent);
                } else {
                    //  there's a problem
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showMessage(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }
}
