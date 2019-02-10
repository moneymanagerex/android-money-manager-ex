package com.money.manager.ex.core;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.utils.MmxDate;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.FileDescriptor;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

/**
 * Functions to assist with selecting database file.
 */
public class FileStorageHelper {
    public FileStorageHelper(AppCompatActivity host) {
        _host = host;
    }

    private AppCompatActivity _host;

    /**
     * Opens a file dialog using the Storage Access Framework.
     * Uses RequestCodes.SELECT_DOCUMENT as a request code.
     * The result needs to be handled in onActivityResult.
     */
    public void showSelectFileInStorage() {
        // show the file picker
        int requestCode = RequestCodes.SELECT_DOCUMENT;
        AppCompatActivity host = _host;

        try {
            // ACTION_GET_CONTENT in older versions of Android.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // intent.setType("text/plain");
            //intent.setType("application/x-sqlite3");
            intent.setType("*/*");
            host.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Timber.e(e, "No storage providers found.");

            showSelectLocalFileDialog();
        }

    }

    /**
     * Shows a file picker. The results from the picker will be sent to the host activity.
     * This is a custom picker that works with local files only.
     * Uses SELECT_FILE request code.
     */
    public void showSelectLocalFileDialog() {
        int requestCode = RequestCodes.SELECT_FILE;
        AppCompatActivity host = _host;

        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(host);
        String dbDirectory = dbUtils.getDefaultDatabaseDirectory();
        // Environment.getDefaultDatabaseDirectory().getPath()

        // This always works
        Intent i = new Intent(host, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, dbDirectory);
        // Environment.getExternalStorageDirectory().getPath()

        host.startActivityForResult(i, requestCode);
    }

    /**
     * Retrieves the database file from a document Uri.
     */
    public Uri getDatabaseFromProvider(Intent activityResultData) {
        Uri uri = null;
        if (activityResultData == null) {
            return null;
        }

        uri = activityResultData.getData();
        //Timber.i("blah", "Uri: " + uri.toString());

        // Take persistable URI permission.
        _host.getContentResolver().takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        return uri;
    }

    public void getFileMetadata(Uri uri) {
        AppCompatActivity host = _host;
        Cursor cursor = host.getContentResolver()
                .query(uri, null, null, null, null, null);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }
            // columns: document_id, mime_type, _display_name, last_modified, flags, _size.

            String displayName = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            String size = null;
            if (!cursor.isNull(sizeIndex)) {
                // Technically the column stores an int, but cursor.getString()
                // will do the conversion automatically.
                size = cursor.getString(sizeIndex);
            } else {
                size = "Unknown";
            }

            int modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            //String lastModified = null;
            long lastModifiedTicks = -1;
            // get the last modified date
            if (!cursor.isNull(modifiedIndex)) {
                lastModifiedTicks = cursor.getLong(modifiedIndex);
            }
            // timestamp
            MmxDate lastModifiedDate = new MmxDate(lastModifiedTicks);
            String dateString = lastModifiedDate.toIsoDateTimeString();

            Timber.i("check the values");
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            cursor.close();
        }

        // check the values
    }

    public void readDocument(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = _host.getContentResolver()
                .openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        //Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
    }
}
