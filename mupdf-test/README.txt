MuPDF Library Test Application (Android)
==============================================

This Gradle project contains a sample application to demonstrate how to
utilise the MuPDF library to:

  1. Convert a document into a series of PNG images. Each image containing
     a single page.
  2. Launch an editing session for a document.

Building The Project
--------------------

  Android Studio
  --------------

  1. Import the Gradle project into Android Studio.
     It will build automatically.

  2. The project comes bundled with javadoc documentation for the
     MuPDF library API's.

     Android Studio does not pick this up by default.

     Execute the following actions to configure Android Studio:

       1. Select Project view
       2. Right Click on "External Libraries -> solib-" and select
          "Library Properties"
       3. Hit '+' to attach an item.
          In the displayed folder structure select 'jars/classes.jar'

          Android Studio should correctly identify the addition of a
          JavaDocs entry.

  Command Line
  ------------

  Execute the following:
    ANDROID_HOME=<SDK Location> ./gradlew assembleDebug

The Sample Application
----------------------

  General Information
  -------------------

  Upon execution of the sample application a view is displayed allowing
  the required example to be selected.

  Once the required example has been selected a file picker will be displayed.
  By default his will enumerate the device download folder.

    See ChooseDocActivity.java::onCreate()::mDirectory to modify this.

  Once the file selection is complete the example will be run. In all but the
  document editing example the conversion will take place. Upon completion
  a dialogue will be displayed with the location of the output.

  In the document editing example the MuPDF document editor will
  be launched.

  Launching Examples
  ------------------

  In general ChooseDocActivity.java::onListItemClick() launches the selected
  example with the selected file.

  PNG Export
  ----------

  ExportFileAsPng.java details how to export a document as a series of
  PNG files.

  The process here is very similar to the PDF case. The run() method
  loads the library and opens the document.

  The onDocComplete() handler launches a background task to iterate over
  each document page, obtaining a bitmap then compressing it to PNG format.

  The compression is very processor intensive a must be done off the UI
  thread.

  Full Document Editor
  --------------------

  The editor, NUIActivity, is completely self contained and is launched via
  an intent directly from ChooseDocActivity.java::onListItemClick().

Importing to an application
---------------------------

 - The project app/libs folder contains the following Android archives.
   - mupdf.aar           The core library
   - editor.aar          The document editor activity
   - wheel.aar           UI artifacts required by the document editor.
   - sodk_resources.aar  resources used by editor.aar

   The libraries should be copied into the application app/libs folder.

   Additionally the required archives should be added as application
   dependencies in app/build.gradle as follows:

   dependencies {
       implementation(name:'sodk_resources', ext:'aar')
       implementation(name:'editor', ext:'aar')
       implementation(name:'wheel', ext:'aar')
       implementation(name:'mupdf', ext:'aar')
   }

  - Namespaces
    - The mupdf library uses the 'com.artifex.mupdf.fitz.' namespace.
    - The editor launch requires the 'com.artifex.sonui.editor.NUIActivity'
      namespace.
