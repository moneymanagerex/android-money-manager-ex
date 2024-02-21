/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.core.docstorage;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.utils.MmxDate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Functions to assist with selecting database file.
 */
public class FileStorageHelper {
    private final Context _host;

    public FileStorageHelper(Context host) {
        _host = host;
    }

    public Context getContext() {
        return _host;
    }

    /**
     * Opens a file dialog using the Storage Access Framework.
     * Uses RequestCodes.SELECT_DOCUMENT as a request code.
     * The result needs to be handled in onActivityResult.
     */
    public void showStorageFilePicker() {
        // show the file picker
        int requestCode = RequestCodes.SELECT_DOCUMENT;
        AppCompatActivity host = (AppCompatActivity) _host;
        try {
            // ACTION_GET_CONTENT in older versions of Android.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            host.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Timber.e(e, "No storage providers found.");

            showSelectLocalFileDialog();
        }

    }

    public void showCreateFilePicker() {
        // show the file picker
        int requestCode = RequestCodes.CREATE_DOCUMENT;
        AppCompatActivity host = (AppCompatActivity) _host;
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, "your_data.mmb");
            host.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Timber.e(e, "No storage providers found.");
        }
    }

    /**
     * Open the selected database file from Storage Access Framework.
     * @param activityResultData the intent received in onActivityResult after the file
     *                           is selected in the picker.
     */
    public DatabaseMetadata selectDatabase(Intent activityResultData) {
        Uri docUri = getDatabaseUriFromProvider(activityResultData);
        DocFileMetadata fileMetadata = getRemoteMetadata(docUri);
        DatabaseMetadata metadata = getMetadataForRemote(fileMetadata);

        pullDatabase(metadata);

        return metadata;
    }

    public DatabaseMetadata createDatabase(Intent activityResultData) {
        Uri docUri = getDatabaseUriFromProvider(activityResultData);
        DocFileMetadata fileMetadata = getRemoteMetadata(docUri);
        DatabaseMetadata metadata = getMetadataForRemote(fileMetadata);

        pullDatabase(metadata);

        return metadata;
    }
    /**
     * Synchronize local and remote database files.
     * @param metadata Database file metadata.
     */
    public String synchronize(DatabaseMetadata metadata) {
        // validation
        // Make sure we have a valid storage-access-framework url.
        if (!metadata.remotePath.startsWith("content://")) {
            Timber.w("Invalid remote Uri. Please re-open the database.");
            return "Invalid remote Uri";
        }

        // check if we have remote changes
        boolean remoteChanged = isRemoteFileChanged(metadata);

        // check if we have local changes
        boolean localChanged = isLocalFileChanged(metadata);

        // decide on the action
        if (remoteChanged && localChanged) {
            String message = "Conflict! Both files have been modified.";
            //throw new RuntimeException();
            Timber.e(message);
            return "Conflict";
        }
        if (remoteChanged) {
            // download
            pullDatabase(metadata);
            return "pullDatabase";
        }
        if (localChanged) {
            // upload
            pushDatabase(metadata);
            return "pushDatabase";
        }
        if (!remoteChanged && !localChanged) {
            Timber.i("Not synchronizing. Files have not been modified.");
        }
        return "no change";
    }

    /*
        Private area
     */

    public boolean isLocalFileChanged(DatabaseMetadata metadata) {
        MmxDate localLastModifiedMmxDate = getLocalFileModifiedDate(metadata);
        Date localLastModified = localLastModifiedMmxDate.toDate();
        // The timestamp when the local file was downloaded.
        Date localDownloaded = MmxDate.fromIso8601(metadata.localSnapshotTimestamp).toDate();

        boolean result = localLastModified.after(localDownloaded);
        return result;
    }

    public boolean isRemoteFileChanged(DatabaseMetadata metadata) {
        DocFileMetadata remote = getRemoteMetadata(metadata);

        // Check if the remote file was modified since fetched.
        // This is the modification timestamp of the remote file when it was last downloaded.
        Date remoteSnapshot = MmxDate.fromIso8601(metadata.remoteLastChangedDate).toDate();
        // This is current dateModified at the remote file.
        Date remoteModified = remote.lastModified.toDate();

        return remoteModified.after(remoteSnapshot);
    }

    /**
     * Copies the remote database locally and updates the metadata.
     * @param metadata Database file metadata.
     */
    public void pullDatabase(DatabaseMetadata metadata) {
        // copy the contents into a local database file.
        Uri uri = Uri.parse(metadata.remotePath);
        try {
            this.downloadDatabase(uri, metadata.localPath);
        } catch (Exception e) {
            Timber.e(e);
            return;
        }

        DocFileMetadata remote = getRemoteMetadata(uri);
        metadata.remoteLastChangedDate = remote.lastModified.toIsoString();

        // Store the local snapshot timestamp, the time when the file was downloaded.
        MmxDate localSnapshot = getLocalFileModifiedDate(metadata);
        metadata.localSnapshotTimestamp = localSnapshot.toIsoString();

        // store the metadata.
        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(getContext());

        // issue #1359
        try {
            dbUtils.useDatabase(metadata);
        } catch (Exception e) {
            Timber.e(e);
            Toast.makeText(getContext(),"Unable to open DB. Not a .mmb file.", Toast.LENGTH_SHORT).show();
            return;
        }
        MmexApplication.getAmplitude().track("synchronize", new HashMap() {{
                       put("authority", uri.getAuthority());
                        put("result", "pullDatabase");
                    }});
    }

    /**
     * Pushes the local file to the document provider and updates the metadata.
     * @param metadata Database file metadata.
     */
    public void pushDatabase(DatabaseMetadata metadata) {
        // upload local file
        uploadDatabase(metadata);

        // Update the modification timestamps, both local and remote.
        MmxDate localLastModifiedMmxDate = getLocalFileModifiedDate(metadata);
        Date localLastModified = localLastModifiedMmxDate.toDate();

        Uri remoteUri = Uri.parse(metadata.remotePath);
        DocFileMetadata remote = getRemoteMetadata(remoteUri);

        if (remote.lastModified.toDate().before(localLastModified)) {
            // The metadata has not been updated yet!
            // Solve this problem by polling until new value fetched. (doh!)
            pollNewRemoteTimestamp(metadata);
        } else {
            metadata.remoteLastChangedDate = remote.lastModified.toIsoString();
        }
        metadata.localSnapshotTimestamp = localLastModifiedMmxDate.toIsoString();

        saveMetadata(metadata);

        MmexApplication.getAmplitude().track("synchronize", new HashMap() {{
            put("authority", remoteUri.getAuthority());
            put("result", "pushDatabase");
        }});
    }

    /**
     * Push the latest file info to the database manager.
     */
    private void saveMetadata(DatabaseMetadata metadata) {
        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(getContext());
        dbUtils.useDatabase(metadata);
    }

    /**
     * Retrieves the database file from a document Uri.
     */
    private Uri getDatabaseUriFromProvider(Intent activityResultData) {
        if (activityResultData == null) {
            return null;
        }

        Uri uri = activityResultData.getData();
        //Timber.i("blah", "Uri: " + uri.toString());

        // Take persistable URI permission.
        _host.getContentResolver().takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        return uri;
    }

    private DatabaseMetadata getMetadataForRemote(DocFileMetadata fileMetadata) {
        DatabaseMetadata metadata = new DatabaseMetadata();
        metadata.remotePath = fileMetadata.Uri;
        metadata.remoteLastChangedDate = fileMetadata.lastModified.toIsoString();

        // Local file will always be the same.
        // TODO add cloud storage provider in the path?
        String localPath = new DatabaseManager(_host).getDefaultDatabaseDirectory();
        metadata.localPath = localPath + File.separator + fileMetadata.Name;

        return metadata;
    }

    private DocFileMetadata getRemoteMetadata(DatabaseMetadata metadata) {
        Uri remoteUri = Uri.parse(metadata.remotePath);
        return getRemoteMetadata(remoteUri);
    }

    private DocFileMetadata getRemoteMetadata(Uri uri) {
        Context host = _host;

        DocFileMetadata result = new DocFileMetadata();
        result.Uri = uri.toString();

        try (Cursor cursor = host.getContentResolver().query(uri, null, null, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            // columns: document_id, mime_type, _display_name, last_modified, flags, _size.
            // Use constant values for column names to avoid errors
            int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            int lastModifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);

            result.Name = cursor.getString(displayNameIndex);

            if (!cursor.isNull(sizeIndex)) {
                result.Size = cursor.getInt(sizeIndex);
            } else {
                result.Size = -1; // or set to a default value
            }

            if (!cursor.isNull(lastModifiedIndex)) {
                result.lastModified = new MmxDate(cursor.getLong(lastModifiedIndex));
            } else {
                result.lastModified = null; // or set to a default value
            }
        } catch (Exception e) {
            Timber.e(e);
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
    private void downloadDatabase(Uri uri, String localPath) throws Exception {
        ContentResolver resolver = getContext().getContentResolver();

        // Use try-with-resources to automatically close resources
        File tempDatabaseFile = File.createTempFile("database", ".db", getContext().getFilesDir());
        try (FileOutputStream outputStream = new FileOutputStream(tempDatabaseFile);
             InputStream is = resolver.openInputStream(uri)) {

            // Copy contents
            long bytesCopied = ByteStreams.copy(is, outputStream);
            Timber.i("copied %d bytes", bytesCopied);
        } catch (Exception e) {
            Timber.e(e);
            return;
        }

        // Replace local database with downloaded version
        File localDatabaseFile = new File(localPath);
        Timber.d("%s %s %s", tempDatabaseFile.toPath(), localDatabaseFile.toPath(), localPath);
        // StandardCopyOption.REPLACE_EXISTING ensures that the destination file is replaced if it exists
        Files.move(tempDatabaseFile, localDatabaseFile);
    }

    /**
     * Shows a file picker. The results from the picker will be sent to the host activity.
     * This is a custom picker that works with local files only.
     * Uses SELECT_FILE request code.
     */
    private void showSelectLocalFileDialog() {
        int requestCode = RequestCodes.SELECT_FILE;
        AppCompatActivity host = (AppCompatActivity) _host;

        //MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(host);
        DatabaseManager dbManager = new DatabaseManager(getContext());
        String dbDirectory = dbManager.getDefaultDatabaseDirectory();
        // Environment.getDefaultDatabaseDirectory().getPath()

        // This works if you defined the intent filter
         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         intent.setType("*/*");
        // Set these depending on your use case. These are the defaults.

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        // Environment.getExternalStorageDirectory().getPath()

        host.startActivityForResult(intent, requestCode);
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

    private void pollNewRemoteTimestamp(DatabaseMetadata metadata) {
        // poll every n seconds.
        long milliseconds = 2 * 1000;

        // Param is optional, to run task on UI thread.
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Fetch the remote metadata until it has reflected the upload.
                Uri uri = Uri.parse(metadata.remotePath);
                DocFileMetadata remote = getRemoteMetadata(uri);
                Date storedLastChange = MmxDate.fromIso8601(metadata.remoteLastChangedDate).toDate();

                if (remote.lastModified.toDate().equals(storedLastChange)) {
                    // repeat
                    Timber.d("fetching the actual remote metadata...");
                    handler.postDelayed(this, milliseconds); // Optional, to repeat the task.
                } else {
                    // got an update. store the latest metadata.
                    metadata.remoteLastChangedDate = remote.lastModified.toIsoString();
                    saveMetadata(metadata);
                    Timber.i("The remote file updated at " +
                            remote.lastModified.toIsoString());
                    // do not poll further.
                }
            }
        };
        // Trigger the first run.
        handler.postDelayed(runnable, milliseconds);

        // Stop a repeating task like this.
        //handler.removeCallbacks(runnable);
    }
}
