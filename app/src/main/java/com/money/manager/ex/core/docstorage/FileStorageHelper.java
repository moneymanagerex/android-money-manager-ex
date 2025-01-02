package com.money.manager.ex.core.docstorage;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.file.Files;
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
import java.io.OutputStream;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

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
        DocFileMetadata fileMetadata = DocFileMetadata.fromUri(_host, docUri);
        DatabaseMetadata metadata = getDatabaseMetadata(fileMetadata);

        pullDatabase(metadata);

        return metadata;
    }

    public DatabaseMetadata createDatabase(Intent activityResultData) {
        Uri docUri = getDatabaseUriFromProvider(activityResultData);
        DocFileMetadata fileMetadata = DocFileMetadata.fromUri(_host, docUri);
        DatabaseMetadata metadata = getDatabaseMetadata(fileMetadata);

        pullDatabase(metadata);

        return metadata;
    }

    /*
        Private area
     */
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

        metadata.remoteLastChangedDate = metadata.getRemoteFileModifiedDate(_host).toIsoString();
        // Store the local snapshot timestamp, the time when the file was downloaded.
        metadata.localSnapshotTimestamp = metadata.getLocalFileModifiedDate().toIsoString();

        // store the metadata.
        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(getContext());

        // issue #1359
        try {
            dbUtils.useDatabase(metadata);
        } catch (Exception e) {
            Timber.e(e);
            try {
                Toast.makeText(getContext(), "Unable to open DB. Not a .mmb file.", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
            return;
        }
        MmexApplication.getAmplitude().track("synchronize", new HashMap<String, String>() {{
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
        MmxDate localLastModifiedMmxDate = metadata.getLocalFileModifiedDate();
        Date localLastModified = localLastModifiedMmxDate.toDate();

        Uri remoteUri = Uri.parse(metadata.remotePath);
        DocFileMetadata remote = DocFileMetadata.fromUri(_host, remoteUri);

        if (remote.lastModified.toDate().before(localLastModified)) {
            // The metadata has not been updated yet!
            // Solve this problem by polling until new value fetched. (doh!)
            pollNewRemoteTimestamp(metadata);
        } else {
            metadata.remoteLastChangedDate = remote.lastModified.toIsoString();
        }
        metadata.localSnapshotTimestamp = localLastModifiedMmxDate.toIsoString();

        saveMetadata(metadata);

        MmexApplication.getAmplitude().track("synchronize", new HashMap<String, String>() {{
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
        if (activityResultData == null || activityResultData.getData() == null) {
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

    private DatabaseMetadata getDatabaseMetadata(DocFileMetadata docFileMetadata) {
        DatabaseMetadata metadata = new DatabaseMetadata();
        metadata.remotePath = docFileMetadata.Uri;
        metadata.remoteLastChangedDate = docFileMetadata.lastModified.toIsoString();

        // Local file will always be the same.
        String localPath = new DatabaseManager(_host).getDefaultDatabaseDirectory();
        metadata.localPath = localPath + File.separator + docFileMetadata.Name;

        return metadata;
    }

    /**
     * Just pushes the given local file to the document provider, using a temporary name.
     */
    private void uploadDatabase(DatabaseMetadata metadata) {
        ContentResolver resolver = getContext().getContentResolver();
        Uri remoteUri = Uri.parse(metadata.remotePath);

        File localFile = new File(metadata.localPath);

        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(remoteUri, "w")) {
            if (pfd == null) {
                throw new FileNotFoundException("Failed to obtain ParcelFileDescriptor for URI: " + remoteUri);
            }

            // Use Files.copy() for direct file-to-stream copy
            try (OutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
                Files.copy(localFile.toPath(), outputStream);
                Timber.d("Database stored successfully to %s", remoteUri);
            }
        } catch (FileNotFoundException e) {
            Timber.e(e, "File not found during upload: %s", metadata.localPath);
        } catch (IOException e) {
            Timber.e(e, "IO error during upload: %s", metadata.localPath);
        }
    }

    /**
     * Creates a local copy of the database from document storage.
     * @param uri Remote Uri
     * @throws IOException boom
     */
    private void downloadDatabase(Uri uri, String localPath) throws Exception {
        ContentResolver resolver = getContext().getContentResolver();

        // Temporary database file creation
        File tempDatabaseFile = File.createTempFile("database", ".db", getContext().getFilesDir());

        // Use CompletableFuture for async operations
        CompletableFuture<Void> downloadTask = CompletableFuture.runAsync(() -> {
            try (InputStream is = resolver.openInputStream(uri);
                 OutputStream os = Files.newOutputStream(tempDatabaseFile.toPath())) {
                if (is == null) {
                    throw new IOException("InputStream is null for URI: " + uri);
                }
                long bytesCopied = is.transferTo(os); // Stream API to copy bytes
                Timber.i("Copied %d bytes", bytesCopied);
            } catch (Exception e) {
                Timber.e(e, "Error downloading database");
                throw new RuntimeException(e); // Wrap exception for CompletableFuture
            }
        });

        // Wait for the async task to complete
        downloadTask.get(); // Propagates exceptions if any

        // Replace the local database with the downloaded version
        File localDatabaseFile = new File(localPath);
        Timber.d("Moving temp file %s to %s", tempDatabaseFile.toPath(), localDatabaseFile.toPath());
        Files.move(tempDatabaseFile.toPath(), localDatabaseFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
                DocFileMetadata remote = DocFileMetadata.fromUri(_host, uri);
                Date storedLastChange = MmxDate.fromIso8601(metadata.remoteLastChangedDate).toDate();

                if (remote.lastModified.toDate().equals(storedLastChange)) {
                    // repeat
                    Timber.d("fetching the actual remote metadata...");
                    handler.postDelayed(this, milliseconds); // Optional, to repeat the task.
                } else {
                    // got an update. store the latest metadata.
                    metadata.remoteLastChangedDate = remote.lastModified.toIsoString();
                    saveMetadata(metadata);
                    Timber.d("The remote file updated at %s", remote.lastModified.toIsoString());
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
