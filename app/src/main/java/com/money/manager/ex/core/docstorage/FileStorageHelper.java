package com.money.manager.ex.core.docstorage;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import com.google.common.io.ByteStreams;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.utils.MmxDate;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public Context getContext() {
        return _host;
    }

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

        //MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(host);
        DatabaseManager dbManager = new DatabaseManager(getContext());
        String dbDirectory = dbManager.getDefaultDatabaseDirectory();
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
     * Open the selected database file from Storage Access Framework.
     * @param activityResultData the intent received in onActivityResult after the file
     *                           is selected in the picker.
     */
    public DatabaseMetadata selectDatabase(Intent activityResultData) {
        Uri docUri = getDatabaseUriFromProvider(activityResultData);
        DocFileMetadata fileMetadata = getFileMetadata(docUri);
        DatabaseMetadata metadata = getMetadata(fileMetadata);

        // If there is an existing file with the same name?
        // Delete previous local file.
        File prevFile = new File(metadata.localPath);
        boolean deleted = prevFile.delete();

        // copy the contents into a local database file.
        try {
            this.cacheDatabase(docUri, metadata.localPath);
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }

        // store the metadata.
        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(this._host);
        dbUtils.useDatabase(metadata);

        return metadata;
    }

    /**
     * Retrieves the database file from a document Uri.
     */
    private Uri getDatabaseUriFromProvider(Intent activityResultData) {
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

    private DatabaseMetadata getMetadata(DocFileMetadata fileMetadata) {
        DatabaseMetadata metadata = new DatabaseMetadata();
        metadata.remotePath = fileMetadata.Uri;
        metadata.remoteLastChangedDate = fileMetadata.lastModified.toIsoString();

        // Local file will always be the same.
        //String dataDir = new ContextWrapper(this._host).getDataDir("xy");
        //File dbPath = new ContextWrapper(this._host).getDatabasePath("xy");
        String localPath = new DatabaseManager(_host).getDefaultDatabaseDirectory();
        //Paths.get(localPath, fileMetadata.Name);
        metadata.localPath = localPath + File.separator + fileMetadata.Name;

        return metadata;
    }

    private DocFileMetadata getFileMetadata(Uri uri) {
        AppCompatActivity host = _host;

        DocFileMetadata result = new DocFileMetadata();
        result.Uri = uri.toString();

        Cursor cursor = host.getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            // columns: document_id, mime_type, _display_name, last_modified, flags, _size.

            String displayName = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            result.Name = displayName;

            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
//            String size = null;
//            if (!cursor.isNull(sizeIndex)) {
//                // Technically the column stores an int, but cursor.getString()
//                // will do the conversion automatically.
//                size = cursor.getString(sizeIndex);
//            } else {
//                size = "Unknown";
//            }
            result.Size = cursor.getInt(sizeIndex);


            int modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            //String lastModified = null;
            long lastModifiedTicks = -1;
            // get the last modified date
            if (!cursor.isNull(modifiedIndex)) {
                lastModifiedTicks = cursor.getLong(modifiedIndex);
            }
            // timestamp
            MmxDate lastModifiedDate = new MmxDate(lastModifiedTicks);
            //String dateString = lastModifiedDate.toIsoDateTimeString();
            result.lastModified = lastModifiedDate;

            //Timber.i("check the values");
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            cursor.close();
        }

        // check the values
        return result;
    }

    /**
     * Creates a local copy of the database from document storage.
     * @param uri Remote Uri
     * @throws IOException boom
     */
    private void cacheDatabase(Uri uri, String localPath) throws IOException {
        ContentResolver resolver = _host.getContentResolver();

        //ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
        //FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        //ContentProviderClient providerClient = resolver.acquireContentProviderClient(uri);
        //ParcelFileDescriptor descriptor = providerClient.openFile(uri, "r");

        // Prepare output
        FileOutputStream outputStream = new FileOutputStream(localPath);
        //Path outputPath = new File(localPath).toPath();
        //Path outputPath = FileSystems.getDefault().getPath(localPath);
        //Path outputPath = Paths.get(localPath);

        // Copy contents
        InputStream is = null;
        try {
            //Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            is = resolver.openInputStream(uri);
            //outputStream.write();
            //Files.copy(is, localPath);
            //IOUtils.copy(is, outputStream);
            long bytesCopied = ByteStreams.copy(is, outputStream);
        } catch (Exception e) {
           Timber.e(e);
        } finally {
            // Cleanup
            is.close();
            outputStream.close();
            //parcelFileDescriptor.close();
            //providerClient.close();
        }

    }
}
