package com.money.manager.ex.core.docstorage;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.file.Files;

import com.google.common.io.ByteStreams;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.database.Password;
import com.money.manager.ex.database.PasswordActivity;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.passcode.PasscodeActivity;
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
            // set default file name as your_data_<creationDateAndTime>.mmb
            String creationDateAndTime = new MmxDate().toString("yyyyMMdd_HHmmss");
            intent.putExtra(Intent.EXTRA_TITLE, "your_data_" + creationDateAndTime + ".mmb");
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
        DatabaseMetadata metadata = DatabaseMetadata.fromDocFileMetadata(_host, fileMetadata);

        if (!pullDatabase(metadata)) return null;

        return metadata;
    }

    public DatabaseMetadata createDatabase(Intent activityResultData) {
        Uri docUri = getDatabaseUriFromProvider(activityResultData);
        DocFileMetadata fileMetadata = DocFileMetadata.fromUri(_host, docUri);

        // During creation if user select a file that already exists file name will be in form
        // fileMetadata.Name = "<existing file name with extention> (1)"
        // this cause subsequent issue becouse filename has no .mmb or emb extension
        // see https://issuetracker.google.com/issues/37136466
        if (!(fileMetadata.Name.endsWith(".mmb") || fileMetadata.Name.endsWith(".emb"))) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(_host);
            alertDialog.setTitle("Invalid file name")
                    .setMessage("Please select unique filename or use Open")
                    .setPositiveButton(android.R.string.ok, null);
            alertDialog.show();
            return null;
        }

        DatabaseMetadata metadata = DatabaseMetadata.fromDocFileMetadata(_host, fileMetadata);

        if(!pullDatabase(metadata)) return null;

        return metadata;
    }

    /*
        Private area
     */
    /**
     * Copies the remote database locally and updates the metadata.
     * @param metadata Database file metadata.
     */
    public boolean pullDatabase(DatabaseMetadata metadata) {
        // copy the contents into a local database file.
        Uri uri = Uri.parse(metadata.remotePath);
        try {
            this.downloadDatabase(uri, metadata.localPath);
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }

        metadata.remoteLastChangedDate = metadata.getRemoteFileModifiedDate(_host).toIsoString();
        // Store the local snapshot timestamp, the time when the file was downloaded.
        metadata.localSnapshotTimestamp = metadata.getLocalFileModifiedDate().toIsoString();

        // store the metadata.
        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(getContext());

        // check password
        if (MmxDatabaseUtils.isEncryptedDatabase(metadata.localPath)) {
            // password is requested
            String pwd = MmexApplication.getApp().getPassword();
            if (TextUtils.isEmpty(pwd)) {
                Intent pwdIntent = new Intent(_host, PasswordActivity.class);
                pwdIntent.putExtra(MainActivity.EXTRA_DATABASE_PATH, metadata.localPath);
                _host.startActivity(pwdIntent);

/*
                // Use the new utility class to ask for the password.
                Password.ask(
                        _host,
                        new Password.PasswordCallback() {
                            @Override
                            public void onPasswordEntered(@NonNull String password) {
                                // The user has entered a valid password.
                                // Save it
                                MmexApplication.getApp().setPassword(password);
                                // open the main activity
                                Intent intent = IntentFactory.getMainActivityNew(_host);
                                _host.startActivity(intent);
                            }

                            @Override
                            public void onPasswordCancelled() {
                                // The user has cancelled the operation.
                                MmexApplication.getApp().setPassword(null);
                            }
                        }
                );
 */
                return false;
            }
        }

        // issue #1359
        try {
            dbUtils.useDatabase(metadata);
        } catch (Exception e) {
            Timber.e(e);
            try {
                Toast.makeText(getContext(), "Unable to open DB. Not a .mmb file.", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
            return false;
        }
        MmexApplication.getAmplitude().track("synchronize", new HashMap<String, String>() {{
            put("authority", uri.getAuthority());
            put("result", "pullDatabase");
        }});
        return true;
    }

    /**
     * Downloads the remote file into a temporary file.
     *
     * @param metadata Database file metadata.
     */
    public void pullDatabaseToTmpFile(DatabaseMetadata metadata) {
        // copy the contents into a local database file.
        Uri uri = Uri.parse(metadata.remotePath);
        try {
            // fix localTmpPtah null after first run
            this.downloadDatabase(uri, metadata.getTmpFilePath());
        } catch (Exception e) {
            Timber.e(e);
            return;
        }
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

    /**
     * Just pushes the given local file to the document provider, using a temporary name.
     */
    private void uploadDatabase(DatabaseMetadata metadata) {
        ContentResolver resolver = getContext().getContentResolver();
        MmxDatabaseUtils utils = new MmxDatabaseUtils(getContext());
        MmxDate lastSyncDateBackup = utils.getLastSyncDate(); // in case the sync fails
        utils.setLastSyncDate(new MmxDate(new Date()));
        boolean successfullySynced = false;
        Uri remoteUri = Uri.parse(metadata.remotePath);

        File localFile = new File(metadata.localPath);

        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(remoteUri, "w")) {
            if (pfd == null) {
                throw new FileNotFoundException("Failed to obtain ParcelFileDescriptor for URI: " + remoteUri);
            }

            // Use Files.copy() for direct file-to-stream copy
            try (OutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
                long bytesCopied = Files.copy(localFile.toPath(), outputStream);
                // Notify resolver to ensure synchronization
                resolver.notifyChange(remoteUri, null);
                Timber.d("Database stored %d bytes to %s",bytesCopied, remoteUri);
                successfullySynced = true;
            }
        } catch (FileNotFoundException e) {
            Timber.e(e, "File not found during upload: %s, URI: %s", metadata.localPath, remoteUri);
        } catch (IOException e) {
            Timber.e(e, "IO error during upload: %s", metadata.localPath);
        } catch (Exception e) {
            Timber.e(e, "Error during upload: %s", remoteUri);
        } finally {
            if (!successfullySynced) {
                // reset lastSync Date
                utils.setLastSyncDate(lastSyncDateBackup);
            }
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
                long bytesCopied = ByteStreams.copy(is, os);
                Timber.d("Copied %d bytes", bytesCopied);
            } catch (Exception e) {
                Timber.e(e, "Error downloading database");
                throw new RuntimeException(e); // Wrap exception for CompletableFuture
            }
        });

        // Wait for the async task to complete, byt try to handle timeout
        try {
            downloadTask.get(); // Propagates exceptions if any
        } catch ( Exception e ) {
            Timber.e(e, "Error downloading database");
            throw new RuntimeException(e); // propagate
        }

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
                DocFileMetadata remote;
                try {
                     remote = DocFileMetadata.fromUri(_host, uri);
                } catch (Exception e) {
                    Timber.e(e, "Error fetching remote metadata");
                    return;
                }
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
