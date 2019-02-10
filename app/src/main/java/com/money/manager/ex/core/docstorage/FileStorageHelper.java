package com.money.manager.ex.core.docstorage;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.utils.MmxDate;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

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
            this.downloadDatabase(docUri, metadata.localPath);
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }

        // Store the local snapshot timestamp, the time when the file was downloaded.
        MmxDate localSnapshot = getLocalFileModifiedDate(metadata);
        metadata.localSnapshotTimestamp = localSnapshot.toIsoString();

        // store the metadata.
        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(getContext());
        dbUtils.useDatabase(metadata);

        return metadata;
    }

    /**
     * Pushes the local file to the document provider.
     * @param metadata Database file metadata.
     */
    public void pushDatabase(DatabaseMetadata metadata) {
        // handle remote changes
        Uri remoteUri = Uri.parse(metadata.remotePath);
        DocFileMetadata remote = getFileMetadata(remoteUri);

        // Check if the remote file was modified since fetched.
        MmxDate remoteSnapshot = MmxDate.fromIso8601(metadata.remoteLastChangedDate);
        Date remoteModified = remote.lastModified.toDate();
        Date remoteFetched = remoteSnapshot.toDate();

        if (remoteModified.after(remoteFetched)) {
            Timber.w("The remote file was modified in the meantime");
            return;
        }

        // Check if the local file was modified.
        MmxDate localLastModifiedMmxDate = getLocalFileModifiedDate(metadata);
        Date localLastModified = localLastModifiedMmxDate.toDate();
        Date localDownloaded = MmxDate.fromIso8601(metadata.localSnapshotTimestamp).toDate();

        if (!localLastModified.after(localDownloaded)) {
            Timber.i("File not modified");
            return;
        }

        // upload local file
        uploadDatabase(metadata);

        // update the remote snapshot info.
        remote = getFileMetadata(remoteUri);
        Date remoteLastChangedDate = remote.lastModified.toDate();

        if(remoteLastChangedDate.before(localLastModified)) {
            // The metadata has not been updated yet!
            // todo: solve this problem.
            Timber.w("The remote file last modified on " +
                    remote.lastModified.toIsoString());
        }

        metadata.remoteLastChangedDate = remote.lastModified.toIsoString();
        metadata.localSnapshotTimestamp = localLastModifiedMmxDate.toIsoString();

        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(getContext());
        dbUtils.useDatabase(metadata);
    }

    /*
        Private area
     */

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
     * Just pushes the given local file to the document provider, using a temporary name.
     */
    private void uploadDatabase(DatabaseMetadata metadata) {
        ContentResolver resolver = getContext().getContentResolver();
        Uri remote = Uri.parse(metadata.remotePath);

        ParcelFileDescriptor pfd = null;
        try {
            pfd = resolver.openFileDescriptor(remote, "w");

            FileOutputStream fileOutputStream =
                new FileOutputStream(pfd.getFileDescriptor());

            // local file
            File localFile = new File(metadata.localPath);
            Files.copy(localFile, fileOutputStream);

            fileOutputStream.close();
            pfd.close();

            Timber.i("Database stored successfully.");
        } catch (FileNotFoundException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    /**
     * Shows how to delete the remote file. This was supposed to be used if a temp file is
     * uploaded. However, it is easy to overwrite the original file.
     * @param metadata The file info
     */
    private void deleteRemoteFile(DatabaseMetadata metadata) {
        ContentResolver resolver = getContext().getContentResolver();
        Uri remote = Uri.parse(metadata.remotePath);
        try {
            DocumentsContract.deleteDocument(resolver, remote);
        } catch (FileNotFoundException e) {
            Timber.e(e);
        }
    }

    /**
     * Creates a local copy of the database from document storage.
     * @param uri Remote Uri
     * @throws IOException boom
     */
    private void downloadDatabase(Uri uri, String localPath) throws IOException {
        ContentResolver resolver = getContext().getContentResolver();

        //ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
        //FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        //ContentProviderClient providerClient = resolver.acquireContentProviderClient(uri);
        //ParcelFileDescriptor descriptor = providerClient.openFile(uri, "r");

        // Prepare output
        FileOutputStream outputStream = new FileOutputStream(localPath);

        // Copy contents
        InputStream is = null;
        try {
            //Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            is = resolver.openInputStream(uri);
            long bytesCopied = ByteStreams.copy(is, outputStream);
            Timber.d("copied %d bytes", bytesCopied);
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

    /**
     * Shows a file picker. The results from the picker will be sent to the host activity.
     * This is a custom picker that works with local files only.
     * Uses SELECT_FILE request code.
     */
    private void showSelectLocalFileDialog() {
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
     * Reads the date/time when the local database file was last changed.
     * @return The date/time of the last change
     */
    private MmxDate getLocalFileModifiedDate(DatabaseMetadata metadata) {
        File localFile = new File(metadata.localPath);
        long localFileTimestamp = localFile.lastModified();
        MmxDate localSnapshot = new MmxDate(localFileTimestamp);
        return localSnapshot;
    }
}