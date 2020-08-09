package com.artifex.mupdf_test;

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.artifex.solib.ArDkLib;
import com.artifex.solib.SODocSaveListener
import com.artifex.sonui.editor.DocumentListener
import com.artifex.sonui.editor.DocumentView
import com.artifex.sonui.editor.Utilities
import java.io.File

class DocEditorActivityK : AppCompatActivity() {
    
    private var mUri: Uri? = null
    private var mDocumentView: DocumentView? = null
    private lateinit var mActivity: DocEditorActivityK
    private var mOriginalScale = -1f

    override fun onCreate(savedInstanceState: Bundle?) {

        MainActivity.setupApplicationSpecifics(this)
        super.onCreate(savedInstanceState)

        //  This example activity can either use the SDK's built-in UI (default), or a developer can
        //  supply their own (custom). This activity has examples of both.

        //  choose one:
//        useDefaultUI();  //  use the SDK's built-in UI
        useCustomUI() //  use a UI provided by the developer
    }

    private fun useDefaultUI() {

        //  set up UI
        setContentView(com.artifex.sonui.editor.R.layout.sodk_editor_doc_view_activity)

        //  find the SO component
        mDocumentView = findViewById<View>(com.artifex.sonui.editor.R.id.doc_view) as DocumentView

        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            dv.setDocConfigOptions(SODKLib.getAppConfigOptions())

            //  set an optional listener for document events
            dv.setDocumentListener(object : DocumentListener {
                override fun onPageLoaded(pagesLoaded: Int) {}
                override fun onDocCompleted() {}
                override fun onPasswordRequired() {}
            })

            //  set a listener for when the document view is closed.
            //  typically you'll use it to close your activity.
            dv.setOnDoneListener { super@DocEditorActivityK.finish() }

            /*
             * get the URI for the document
             *
             * an exception will be thrown if 'data' is null.
             */
            mUri = intent.data
             
            mUri?.let { uri ->
                //  open it, specifying showUI = true;
                dv.start(uri, 0, true)
            }
        }
    }

    private fun useCustomUI() {

        mActivity = this

        //  get the DocumentView
        setContentView(R.layout.activity_layout)
        mDocumentView = findViewById(R.id.doc_view)

        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            dv.setDocConfigOptions(SODKLib.getAppConfigOptions())

            //  set a listener for document events
            dv.setDocumentListener(object : DocumentListener {
                override fun onPageLoaded(pagesLoaded: Int) {
                    // Work on a constant copy of mDocumentView
                    val documentView = mDocumentView

                    if (mOriginalScale == -1f)
                        mOriginalScale = documentView?.getScaleFactor()!!

                    documentView?.let { dv1 ->
                        Log.d("DocumentListener",
                              "onPageLoaded pages= " + dv1.getPageCount())
                        updateUI()
                    }
                }

                override fun onDocCompleted() {
                    // Work on a constant copy of mDocumentView
                    val documentView = mDocumentView

                    documentView?.let { dv1 ->
                        Log.d("DocumentListener",
                              "onDocCompleted pages= " + dv1.getPageCount())
                        updateUI()
                    }
                }

                override fun onPasswordRequired() {
                    Log.d("DocumentListener", "onPasswordRequired")
                    handlePassword()
                }
            })

            //  set a listener for when the document view is closed.
            //  typically you'll want to close your activity.
            dv.setOnDoneListener { super@DocEditorActivityK.finish() }

            /*
             * get the URI for the document
             *
             * an exception will be thrown if 'data' is null.
             */
            mUri = intent.data

            mUri?.let { uri ->
                //  open it, specifying showUI = false;
                dv.start(uri, 0, false)

                //  ... and set up our own UI
                setupUI()
            }
        }
    }

    public override fun onPause() {
        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            //  called when pausing is complete
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            dv.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            dv.onDestroy()
        }
    }

    override fun onBackPressed() {
        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            dv.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            dv.onActivityResult(requestCode, resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            dv.onConfigurationChange(newConfig)
        }
    }

    private fun setupUI() {
        //  This function does the work of binding buttons in the UI
        //  to functions in DocumentView.

        findViewById<View>(R.id.toggle_pages).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                if (dv.isPageListVisible) {
                    dv.hidePageList()
                } else {
                    dv.showPageList()
                }
            }
        }

        findViewById<View>(R.id.toggle_draw).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                if (dv.isDrawModeOn) {
                    dv.setDrawModeOff()
                } else {
                    dv.setDrawModeOn()
                }
            }
        }

        findViewById<View>(R.id.delete_selection).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.deleteSelection()
            }
        }

        findViewById<View>(R.id.line_color).setOnClickListener {
            //  this is a sample dialog for choosing a color.
            val context: Context = this@DocEditorActivityK
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Line Color")
            val colorNames = arrayOf("red", "green", "blue")
            val colors = intArrayOf(-0x10000, -0xff0100, -0xffff01)
            builder.setItems(colorNames) { dialog, which ->
                // Work on a constant copy of mDocumentView
                val documentView = mDocumentView

                documentView?.let { dv -> 
                    //   color chosen, so set it
                    dv.setLineColor(colors[which])
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        findViewById<View>(R.id.line_thickness).setOnClickListener {
            //  this is a sample dialog for choosing a line thickness
            val context: Context = this@DocEditorActivityK
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Line Thickness")
            val sizeNames = arrayOf("small", "medium", "large")
            val sizes = floatArrayOf(1f, 8f, 24f)
            builder.setItems(sizeNames) { dialog, which ->
                // Work on a constant copy of mDocumentView
                val documentView = mDocumentView

                documentView?.let { dv -> 
                    //  value chose, now set it.
                    dv.setLineThickness(sizes[which])
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        findViewById<View>(R.id.full_screen).setOnClickListener {
            //  hide this activity's UI
            findViewById<View>(R.id.ui_layout).visibility = View.GONE
            //  clear the search text
            val editText = findViewById<EditText>(R.id.search_text)
            editText.text.clear()
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                //  put DocumentView in full screen mode
                dv.enterFullScreen {
                    //  restore our UI
                    findViewById<View>(R.id.ui_layout).visibility = View.VISIBLE
                    updateUI()
                }
            }
        }

        findViewById<View>(R.id.highlight_selection).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.highlightSelection()
            }
        }

        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        //  set up a runnable to handle UI updates triggers by DocumentView
        documentView?.let { dv -> 
            dv.setOnUpdateUI { updateUI() }
        }

        val editText = findViewById<EditText>(R.id.search_text)
        val searchNextButton = findViewById<Button>(R.id.search_next)
        searchNextButton.text = "Find ->"
        searchNextButton.setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                val text = editText.text.toString()
                dv.searchForward(text)
            }
        }

        val searchPrevButton = findViewById<Button>(R.id.search_previous)
        searchPrevButton.text = "<- Find"
        searchPrevButton.setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                val text = editText.text.toString()
                dv.searchBackward(text)
            }
        }

        findViewById<View>(R.id.save).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.save()
                updateUI()
            }
        }

        findViewById<View>(R.id.save_as).setOnClickListener(View.OnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv ->
                //  get a new path.  The customer would provide a way to specify a new
                mUri?.let { uri ->
                    //  name and location.
                    val path = uri.path
                    val f = File(uri.path)
                    val newPath = f.parentFile.path + "/testFile.pdf"

                    //  check for collision, the SDK does not.
                    if (FileUtils.fileExists(newPath)) {
                        showMessage("The file $newPath already exists")
                    } else {
                        //  save it
                        dv.saveTo(newPath) { result, err ->
                            if (result == SODocSaveListener.SODocSave_Succeeded) {
                                //  success
                                showMessage("The file was saved.")
                            } else {
                                //  error
                                showMessage("There was an error saving the file.")
                            }
                            updateUI()
                        }
                    }
                }
            }
        })

        findViewById<View>(R.id.print).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.print()
            }
        }

        findViewById<View>(R.id.share).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                mUri?.let { uri ->
                    //  save a copy of the document
                    val file = File(filesDir.toString() + File.separator + File(uri.path).name)
                    dv.saveTo(file.path) { result, err ->
                        //  share it
                        showMessage("Sharing is application-specific, and should be implemented by the developer.\n\nSee DocEditorActivity.setupUI")
                        file.delete()
                    }
                }
            }
        }

        findViewById<View>(R.id.toggle_note).setOnClickListener {
            if (mDocumentView != null) {
            }
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                val noteMode = dv.isNoteModeOn
                if (noteMode) {
                    dv.setNoteModeOff()
                } else {
                    dv.setNoteModeOn()
                }
            }
        }

        findViewById<View>(R.id.author).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.author()
            }
        }

        findViewById<View>(R.id.first_page).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.firstPage()
            }
        }

        findViewById<View>(R.id.last_page).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.lastPage()
            }
        }

        val historyNextButton = findViewById<Button>(R.id.history_next)
        historyNextButton.text = "History ->"
        historyNextButton.setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.historyNext()
            }
        }

        val historyPrevButton = findViewById<Button>(R.id.history_previous)
        historyPrevButton.text = "<- History"
        historyPrevButton.setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.historyPrevious()
            }
        }

        findViewById<View>(R.id.table_of_contents).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.tableOfContents()
            }
        }

        findViewById<View>(R.id.table_of_contents2).setOnClickListener { tableOfContents2() }

        findViewById<View>(R.id.redact_mark).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.redactMarkText();
            }
        }

        findViewById<View>(R.id.redact_mark_area).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.redactMarkArea()
            }
        }

        findViewById<View>(R.id.redact_remove).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.redactRemove()
            }
        }

        findViewById<View>(R.id.redact_apply).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                dv.redactApply()
            }
        }

        val pageNumberText = findViewById<EditText>(R.id.page_number)
        findViewById<View>(R.id.goto_page).setOnClickListener {
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                try {
                    val pageNum = pageNumberText.text.toString().toInt()
                    pageNumberText.text.clear()
                    dv.goToPage(pageNum - 1)
                } catch (e: Exception) {
                    showMessage("Please enter a valid integer")
                }
            }
        }

        documentView?.let { dv -> 
            dv.setPageChangeListener { pageNumber ->
                val page = pageNumber + 1
                val count = dv.pageCount
                val text = String.format("page %d of %d", page, count)
                val view = findViewById<TextView>(R.id.page_display)
                view.text = text
                view.measure(0, 0)
                view.requestLayout()
            }
        }

        //  TODO: pipe this thru DocumentView
        val thisActivity: DocEditorActivityK = this
        Utilities.setMessageHandler(object : Utilities.MessageHandler {
            override fun showMessage(title: String, body: String, okLabel: String, whenDone: Runnable) {
                val dialog = AlertDialog.Builder(thisActivity)
                dialog.setTitle(title)
                dialog.setMessage(body)
                dialog.setCancelable(false)
                dialog.setPositiveButton(okLabel) { dialog, which ->
                    dialog.dismiss()
                    whenDone?.run()
                }
                dialog.create().show()
            }

            override fun yesNoMessage(title: String, body: String, yesButtonLabel: String, noButtonLabel: String, yesRunnable: Runnable, noRunnable: Runnable) {
                val dialog = AlertDialog.Builder(thisActivity)
                dialog.setTitle(title)
                dialog.setMessage(body)
                dialog.setCancelable(false)
                dialog.setPositiveButton(yesButtonLabel) { dialog, which ->
                    dialog.dismiss()
                    yesRunnable?.run()
                }
                dialog.setNegativeButton(noButtonLabel) { dialog, which ->
                    dialog.dismiss()
                    noRunnable?.run()
                }
                dialog.create().show()
            }
        })

        findViewById<View>(R.id.scale_up).setOnClickListener { scaleBy(0.20f) }

        findViewById<View>(R.id.scale_down).setOnClickListener { scaleBy(-0.20f) }

        findViewById<View>(R.id.scroll_up).setOnClickListener { scrollBy(-100) }

        findViewById<View>(R.id.scroll_down).setOnClickListener { scrollBy(100) }

        findViewById<View>(R.id.reset).setOnClickListener { mDocumentView!!.setScaleAndScroll(mOriginalScale, 0, 0) }

        //  Tabs
        findViewById<View>(R.id.file_tab_button).setOnClickListener { onClickTabButton(R.id.file_tab_button, R.id.file_tab) }

        //  Tabs
        findViewById<View>(R.id.pages_tab_button).setOnClickListener { onClickTabButton(R.id.pages_tab_button, R.id.pages_tab) }

        //  Tabs
        findViewById<View>(R.id.redact_tab_button).setOnClickListener { onClickTabButton(R.id.redact_tab_button, R.id.redact_tab) }

        //  Tabs
        findViewById<View>(R.id.annotate_tab_button).setOnClickListener { onClickTabButton(R.id.annotate_tab_button, R.id.annotate_tab) }

        //  Tabs
        findViewById<View>(R.id.other_tab_button).setOnClickListener { onClickTabButton(R.id.other_tab_button, R.id.other_tab) }

        findViewById<View>(R.id.scale_tab_button).setOnClickListener { onClickTabButton(R.id.scale_tab_button, R.id.scale_tab) }

        onClickTabButton(R.id.file_tab_button, R.id.file_tab)
    }

    private fun scrollBy(amount: Int) {
        //  collect the scale and position values
        val scale = mDocumentView!!.scaleFactor
        val sx = mDocumentView!!.scrollPositionX
        var sy = mDocumentView!!.scrollPositionY
        //  add the amount
        sy += amount
        //  set the new values
        mDocumentView!!.setScaleAndScroll(scale, sx, sy)
    }

    private fun scaleBy(increment: Float) {
        //  collect the scale and position values
        val scale = mDocumentView!!.scaleFactor
        val sx = mDocumentView!!.scrollPositionX
        val sy = mDocumentView!!.scrollPositionY

        //  new scale factor
        val newScale = scale + increment

        //  Applications may want to adjust scroll values
        //  in an attempt to maintain focus on a particular point.
        mDocumentView!!.setScaleAndScroll(newScale, sx, sy)
    }

    private fun onClickTabButton(buttonId: Int, tabId: Int) {
        //  a tab button ws clicked.

        //  show the given tab
        findViewById<View>(R.id.file_tab).visibility = View.GONE
        findViewById<View>(R.id.pages_tab).visibility = View.GONE
        findViewById<View>(R.id.redact_tab).visibility = View.GONE
        findViewById<View>(R.id.annotate_tab).visibility = View.GONE
        findViewById<View>(R.id.other_tab).visibility = View.GONE
        findViewById<View>(R.id.scale_tab).visibility = View.GONE
        findViewById<View>(tabId).visibility = View.VISIBLE
        findViewById<View>(R.id.file_tab_button).isSelected = false
        findViewById<View>(R.id.pages_tab_button).isSelected = false
        findViewById<View>(R.id.redact_tab_button).isSelected = false
        findViewById<View>(R.id.annotate_tab_button).isSelected = false
        findViewById<View>(R.id.other_tab_button).isSelected = false
        findViewById<View>(R.id.scale_tab_button).isSelected = false
        findViewById<View>(buttonId).isSelected = true
    }

    private fun setToggleButtonText(button: Button, `val`: Boolean, text1: String, text2: String) {
        if (`val`) button.text = text1 else button.text = text2
        button.measure(0, 0)
        button.requestLayout()
    }

    private fun updateUI() {
        //  this is called every time the UI should be updated.
        //  triggered internally by the selection changing (mostly).

        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            //  pages button
            val pagesButton = findViewById<Button>(R.id.toggle_pages)
            val visible = dv.isPageListVisible
            setToggleButtonText(pagesButton, visible, "Hide Pages", "Show Pages")

            //  delete selection button
            findViewById<View>(R.id.delete_selection).isEnabled = dv.canDeleteSelection()

            //  draw on/off button
            val inkDrawMode = dv.isDrawModeOn
            val drawButton = findViewById<Button>(R.id.toggle_draw)
            setToggleButtonText(drawButton, inkDrawMode, "Draw Off", "Draw On")

            //  line color and thickness buttons
            findViewById<View>(R.id.line_color).isEnabled = inkDrawMode
            findViewById<View>(R.id.line_thickness).isEnabled = inkDrawMode

            //  highlight button
            findViewById<View>(R.id.highlight_selection).isEnabled = dv.isAlterableTextSelection

            //  save button
            findViewById<View>(R.id.save).isEnabled = dv.isDocumentModified

            //  note on/off button
            val noteMode = dv.isNoteModeOn
            val noteButton = findViewById<Button>(R.id.toggle_note)
            setToggleButtonText(noteButton, noteMode, "Note Off", "Note On")

            //  history buttons
            findViewById<View>(R.id.history_next).isEnabled = dv.hasNextHistory()
            findViewById<View>(R.id.history_previous).isEnabled = dv.hasPreviousHistory()

            //  TOC button
            findViewById<View>(R.id.table_of_contents).isEnabled = dv.isTOCEnabled
            findViewById<View>(R.id.table_of_contents2).isEnabled = dv.isTOCEnabled

            //  page number - see DocumentView.ChangePageListener, above

            //  redaction
            findViewById<View>(R.id.redact_mark).isEnabled = dv.canMarkTextRedaction()
            findViewById<View>(R.id.redact_remove).isEnabled = dv.canRemoveRedaction()
            findViewById<View>(R.id.redact_apply).isEnabled = dv.canApplyRedactions()

            val markAreaButton = findViewById<Button>(R.id.redact_mark_area)
            val isMarking = dv.redactIsMarkingArea()
            setToggleButtonText(markAreaButton, isMarking, "Marking...", "Mark Area")

        }
    }

    private fun handlePassword() {
        //  this function presents a dialog to collect the password, and then
        //  calls into DocumentView to provide it.
        //  if the password is wrong, we'll come here again.

        val context: Context? = mActivity
        val dBuilder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        dBuilder.setTitle("Password:")
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        dBuilder.setView(input)

        // Set up the buttons
        dBuilder.setPositiveButton("OK"
        ) { dialog, which ->
            // Work on a constant copy of mDocumentView
            val documentView = mDocumentView

            documentView?.let { dv -> 
                //  provide the password.
                dv.providePassword(input.text.toString())
            }
        }
        dBuilder.setNegativeButton("Cancel"
        ) { dialog, which ->
            // Cancel the operation.
            dialog.cancel()
            mActivity.finish()
        }
        dBuilder.setOnCancelListener { dialog ->
            // Cancel the operation.
            dialog.cancel()
            mActivity.finish()
        }
        dBuilder.show()
    }

    inner class TOCEntry internal constructor(var handle: Int, var parentHandle: Int, public val page: Int, var label: String, public val url: String?, public val x: Float, public val y: Float)

    private fun tableOfContents2() {
        // use our own UI

        // Work on a constant copy of mDocumentView
        val documentView = mDocumentView

        documentView?.let { dv -> 
            //  get the TOC entries
            val entries = ArrayList<TOCEntry>()
            dv.enumeratePdfToc { handle, parentHandle, page, label, url, x, y ->
                val entry = TOCEntry(handle, parentHandle, page, label, url, x, y)
                entries.add(entry)
            }

            //  make corresponding array of strings
            val labels = arrayOfNulls<String>(entries.size)
            for (i in entries.indices) labels[i] = entries[i].label

            //  show them in a simple dialog
            val context: Context = this@DocEditorActivityK
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Table Of Contents")
            builder.setItems(labels) { dialog, which ->
                val entry = entries[which]
                if (entry.page >= 0) {
                    // Work on a constant copy of mDocumentView
                    val documentView = mDocumentView

                    documentView?.let { dv -> 
                        val box = RectF(entry.x, entry.y, entry.x + 1, entry.y + 1)
                        dv.gotoInternalLocation(entry.page, box)
                    }
                } else if (entry.url != null) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(entry.url))
                    startActivity(browserIntent)
                } else {
                    //  there's a problem
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun showMessage(message: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(message)
                .setPositiveButton("Ok") { dialoginterface, i -> }.show()
    }
}

