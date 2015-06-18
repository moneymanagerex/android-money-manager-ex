/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.money.manager.ex.dropbox;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.PreferenceConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DropboxHelper {
    // info dropbox
    public static final String ROOT = "/";
    private static final String LOGCAT = DropboxHelper.class.getSimpleName();
    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    private static final int DROPBOX_FILE_LIMIT = 1000;

    // info to access sharedpref
    // singleton
    private static DropboxHelper mHelper;
    private static Context mContext;
    // flag status upload immediate
    private static boolean mDelayedUploadImmediate = false;
    // flag temp disable auto upload
    private static boolean mDisableAutoUpload = false;
    // Dropbox API
    DropboxAPI<AndroidAuthSession> mDropboxApi;

    private DropboxHelper(Context context) {
        super();

        mContext = context;

        AndroidAuthSession session = buildSession();
        mDropboxApi = new DropboxAPI<>(session);
    }

    /**
     * Get a singleton of dropbox. if object don't exists it does create
     *
     * @param context Executing context
     * @return A dropbox helper instance.
     */
    public static DropboxHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new DropboxHelper(context);
        }
        return mHelper;
    }

    public static DropboxHelper getInstance() throws Exception {
        if (mHelper == null) throw new Exception("Dropbox Helper not yet instantiated");

        return mHelper;
    }

    /**
     * called whenever the database has changed and should be resynchronized.
     */
    public static void notifyDataChanged() {
        if (mHelper == null) return;
        if (!mHelper.isLinked()) return;
        if (isAutoUploadDisabled()) return;

        // save the last modified date
        File database = new File(MoneyManagerApplication.getDatabasePath(mContext));
        mHelper.setDateLastModified(database.getName(), Calendar.getInstance().getTime());
        //check if upload as immediate
        if (mHelper.isActiveAutoUpload() && !mDelayedUploadImmediate) {
            final Runnable runnableDropboxUpload = new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG)
                        Log.d(LOGCAT, "Start postDelayed Runnanble to upload database");
                    mHelper.sendBroadcastStartService(DropboxServiceIntent.INTENT_ACTION_UPLOAD);
                    mDelayedUploadImmediate = false;
                }
            };
            if (BuildConfig.DEBUG) Log.d(LOGCAT, "Launch Handler postDelayed");
            Handler handler = new Handler();
            handler.postDelayed(runnableDropboxUpload, 30 * 1000);
            mDelayedUploadImmediate = true;
        }
    }

    public static boolean isAutoUploadDisabled() {
        return mDisableAutoUpload;
    }

    public static void setAutoUploadDisabled(boolean mDisableAutoUpload) {
        DropboxHelper.mDisableAutoUpload = mDisableAutoUpload;
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeysToken() {
        SharedPreferences prefs = mContext.getSharedPreferences(PreferenceConstants.PREF_DROPBOX_ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(PreferenceConstants.PREF_DROPBOX_ACCESS_KEY_NAME, null);
        String secret = prefs.getString(PreferenceConstants.PREF_DROPBOX_ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Clear token from local store
     */
    private void clearKeysToken() {
        SharedPreferences prefs = mContext.getSharedPreferences(PreferenceConstants.PREF_DROPBOX_ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private void storeKeysToken(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = mContext.getSharedPreferences(PreferenceConstants.PREF_DROPBOX_ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(PreferenceConstants.PREF_DROPBOX_ACCESS_KEY_NAME, key);
        edit.putString(PreferenceConstants.PREF_DROPBOX_ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    /**
     * Create a session for access to Dropbox service
     *
     * @return AndroidAuthSession
     */
    private AndroidAuthSession buildSession() {
        String secret = "";
        try {
            secret = SimpleCrypto.decrypt(MoneyManagerApplication.KEY, "A313D7447960230A802C9A55EDFE281E");
        } catch (Exception e) {
            Log.e(LOGCAT, log(e.getMessage(), "buildSession Exception"));
        }
        AppKeyPair appKeyPair = new AppKeyPair(MoneyManagerApplication.DROPBOX_APP_KEY, secret);
        AndroidAuthSession session;

        String[] stored = getKeysToken();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, MoneyManagerApplication.DROPBOX_ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, MoneyManagerApplication.DROPBOX_ACCESS_TYPE);
        }

        return session;
    }

    /**
     * Complete the authentication process on the service dropbox
     */
    public void completeAuthenticationDropbox() {
        AndroidAuthSession session = mDropboxApi.getSession();
        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // complete authentication
                session.finishAuthentication();
                // save login credentials
                TokenPair tokens = session.getAccessTokenPair();
                storeKeysToken(tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                Toast.makeText(mContext, "Couldn't authenticate with Dropbox:" + e.getMessage(), Toast.LENGTH_LONG).show();
                if (BuildConfig.DEBUG) Log.d(LOGCAT, "Error authenticating", e);
            }
        }
    }

    /**
     * Returns true if you are connected to Dropbox
     */
    public boolean isLinked() {
        return mDropboxApi.getSession().isLinked();
    }

    /**
     * Login in dropbox service
     */
    public void logIn() {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Login dropbox service");
        // Start the remote authentication
        mDropboxApi.getSession().startAuthentication(mContext);
    }

    /**
     * Logout from dropbox service
     */
    public void logOut() {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Logout from dropbox account");
        // remove info to access
        mDropboxApi.getSession().unlink();
        clearKeysToken();
    }

    /**
     * Get last modified datetime of dropbox file
     *
     * @param file
     * @return
     * @throws ParseException
     */
    public Date getDateLastModified(String file) throws ParseException {
        SharedPreferences prefs = mContext.getSharedPreferences(PreferenceConstants.PREF_DROPBOX_ACCOUNT_PREFS_NAME, 0);
        String stringDate = prefs.getString(file.toUpperCase(), null);
        if (TextUtils.isEmpty(stringDate)) return null;
        return new SimpleDateFormat(DATE_FORMAT).parse(stringDate);
    }

    /**
     * Save modified datetime dropbox file
     *
     * @param file
     * @param date
     */
    public void setDateLastModified(String file, Date date) {
        if (BuildConfig.DEBUG)
            Log.d(LOGCAT, "Set Dropbox file: " + file + " last modification date " + new SimpleDateFormat().format(date));

        SharedPreferences prefs = mContext.getSharedPreferences(PreferenceConstants.PREF_DROPBOX_ACCOUNT_PREFS_NAME, 0);
        if (!prefs.edit().putString(file.toUpperCase(), new SimpleDateFormat(DATE_FORMAT).format(date)).commit()) {
            Log.e(LOGCAT, "Dropbox: commit last modified date failed!");
        }
    }

    /**
     * get the file path Dropbox linked to the application
     *
     * @return
     */
    public String getLinkedRemoteFile() {
        return mContext.getSharedPreferences(PreferenceConstants.PREF_DROPBOX_ACCOUNT_PREFS_NAME, 0)
                .getString(PreferenceConstants.PREF_DROPBOX_REMOTE_FILE, null);
    }

    /**
     * set the file path Dropbox linked to the application
     *
     * @param fileDropbox
     */
    public void setLinkedRemoteFile(String fileDropbox) {
        SharedPreferences prefs = mContext.getSharedPreferences(PreferenceConstants.PREF_DROPBOX_ACCOUNT_PREFS_NAME, 0);
        prefs.edit().putString(mContext.getString(PreferenceConstants.PREF_DROPBOX_LINKED_FILE), fileDropbox)
                .putString(PreferenceConstants.PREF_DROPBOX_REMOTE_FILE, fileDropbox)
                .apply();
    }

    /**
     * get the status of immediate loading
     */
    public boolean isActiveAutoUpload() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean result = false;

        if (prefs != null) {
            result = prefs.getBoolean(mContext.getString(PreferenceConstants.PREF_DROPBOX_UPLOAD_IMMEDIATE), true);
        }

        return result;
    }

    /**
     * Send a broadcast intent for start service scheduled
     */
    public void sendBroadcastStartServiceScheduled(String action) {
        Intent intent = new Intent(mContext, DropboxReceiver.class);
        intent.setAction(action);
        mContext.sendBroadcast(intent);
    }

    /**
     * Send a broadcast intent for start service shceduled
     */
    public void sendBroadcastStartService(String action) {
        //create intent to launch sync
        Intent service = new Intent(mContext, DropboxServiceIntent.class);
        service.setAction(action);
        service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, MoneyManagerApplication.getDatabasePath(mContext));
        service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, this.getLinkedRemoteFile());
        //start service
        mContext.startService(service);
    }

    /**
     * Get a last modified date of entry
     *
     * @param entry
     * @return
     */
    public Date getLastModifiedEntry(Entry entry) {
        return RESTUtility.parseDate(entry.modified);
    }

    /**
     * Get the builder of a notification for download
     *
     * @return
     */
    public NotificationCompat.Builder getNotificationBuilderDownload() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.application_name_dropbox))
                .setAutoCancel(false)
                .setDefaults(Notification.FLAG_FOREGROUND_SERVICE)
                .setContentText(mContext.getString(R.string.dropbox_downloadProgress))
                        //.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(mContext.getResources().getColor(R.color.md_primary));
        // only for previous version!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            notification.setContentIntent(pendingIntent);
        }
        return notification;
    }

    /**
     * Get notification builder for download complete
     *
     * @param pendingIntent
     * @return
     */
    public NotificationCompat.Builder getNotificationBuilderDownloadComplete(PendingIntent pendingIntent) {
        // compose notification big view
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(mContext.getString(R.string.application_name_dropbox));
        inboxStyle.addLine(mContext.getString(R.string.dropbox_file_ready_for_use));
        inboxStyle.addLine(mContext.getString(R.string.dropbox_open_database_downloaded));
        // compose builder
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                .addAction(R.drawable.ic_action_folder_open_dark, mContext.getString(R.string.open_database), pendingIntent)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(mContext.getString(R.string.application_name_dropbox))
                .setContentText(mContext.getString(R.string.dropbox_open_database_downloaded))
                        ////.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setTicker(mContext.getString(R.string.dropbox_file_ready_for_use))
                .setStyle(inboxStyle)
                .setColor(mContext.getResources().getColor(R.color.md_primary));

        return notification;
    }

    /**
     * Get the builder of a notification for upload
     *
     * @return
     */
    public NotificationCompat.Builder getNotificationBuilderUpload() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.application_name_dropbox))
                .setContentInfo(mContext.getString(R.string.upload_file_to_dropbox_complete))
                .setAutoCancel(false)
                .setContentText(mContext.getString(R.string.dropbox_uploadProgress))
                        //.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(mContext.getResources().getColor(R.color.md_primary));
        ;
        // only for previous version!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            notification.setContentIntent(pendingIntent);
        }
        return notification;
    }

    /**
     * Get notification builder for upload complete
     *
     * @param pendingIntent
     * @return
     */
    public NotificationCompat.Builder getNotificationBuilderUploadComplete(PendingIntent pendingIntent) {
        // compose notification big view
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(mContext.getString(R.string.application_name_dropbox));
        inboxStyle.addLine(mContext.getString(R.string.upload_file_to_dropbox_complete));
        // compose builder
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                //.addAction(R.drawable.ic_action_folder_open_dark, mContext.getString(R.string.open_database), pendingIntent)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(mContext.getString(R.string.application_name_dropbox))
                .setContentText(mContext.getString(R.string.upload_file_to_dropbox_complete))
                        //.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setStyle(inboxStyle)
                .setTicker(mContext.getString(R.string.upload_file_to_dropbox_complete))
                .setColor(mContext.getResources().getColor(R.color.md_primary));
        ;

        return notification;
    }

    /**
     * Get a notification builder with progress bar
     *
     * @param notification existing builder
     * @param totalBytes   total bytes to transfer
     * @param bytes        bytes transfer
     * @return
     */
    public NotificationCompat.Builder getNotificationBuilderProgress(NotificationCompat.Builder notification, int totalBytes, int bytes) {
        notification.setProgress(totalBytes, bytes, false);
        notification.setContentInfo(String.format("%1dKB/%2dKB", bytes / 1024, totalBytes / 1024));

        return notification;
    }

    /**
     * Get a single entry from dropbox
     *
     * @param entry path dropbox entry
     * @return
     */
    public Entry getEntry(String entry) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            return mDropboxApi.metadata(entry, DROPBOX_FILE_LIMIT, null, false, null);
        } catch (DropboxException e) {
            if (BuildConfig.DEBUG) Log.e(LOGCAT, log(e.getMessage(), "getEntry failed!"));
            return null;
        }
    }

    /**
     * Reads the contents of the folder passed as a parameter
     */
    public void getEntries(final OnGetEntries onGetEntries) {
        getEntries(ROOT, onGetEntries);
    }

    /**
     * Reads the contents of the folder passed as a parameter
     */
    public void getEntries(String folder, final OnGetEntries callbacks) {
        AsyncTask<String, Long, List<Entry>> asyncTask = new AsyncTask<String, Long, List<Entry>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (callbacks != null) callbacks.onStarting();
            }

            @Override
            protected List<Entry> doInBackground(String... params) {
                try {
                    Entry folder = mDropboxApi.metadata(params[0], DROPBOX_FILE_LIMIT, null, true, null);
                    if (!folder.isDir) return null;
                    return folder.contents;
                } catch (DropboxException e) {
                    Log.e(LOGCAT, log(e.getMessage(), "getEntries exxception"));
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<Entry> result) {
                super.onPostExecute(result);
                if (callbacks != null) callbacks.onFinished(result);
            }

        };
        asyncTask.execute(folder);
    }

    public boolean download(final Entry dropboxFile, final File localFile, final ProgressListener progressListener) {
        try {
            FileOutputStream fos = new FileOutputStream(localFile);
            mDropboxApi.getFile(dropboxFile.path, null, fos, progressListener);
        } catch (Exception e) {
            Log.e(LOGCAT, log(e.getMessage(), "download exception"));
            return false;
        }
        setDateLastModified(dropboxFile.fileName(), RESTUtility.parseDate(dropboxFile.modified));
        return true;
    }

    public boolean upload(final String dropboxFile, final File localFile, final ProgressListener progressListener) {
        try {
            FileInputStream fis = new FileInputStream(localFile);
            UploadRequest uploadRequest = mDropboxApi.putFileOverwriteRequest(dropboxFile, fis, localFile.length(), progressListener);
            if (uploadRequest != null) {
                Entry entry = uploadRequest.upload();
                if (entry != null) {
                    setDateLastModified(entry.fileName(), RESTUtility.parseDate(entry.modified));
                    // link file if not linked
                    if (TextUtils.isEmpty(getLinkedRemoteFile())) {
                        setLinkedRemoteFile(entry.path);
                        //Toast.makeText(mContext, mContext.getString(R.string.dropbox_linkedFile) + ": " + entry.path, Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOGCAT, log(e.getMessage(), "Upload exception"));
            return false;
        }
        return true;
    }

    /**
     * Download file from dropbox to local storage
     *
     * @param dropboxFile
     * @param localFile
     */
    public void downloadFileAsync(final Entry dropboxFile, final File localFile,
                                  final OnDownloadUploadEntry onDownloadUpload, final ProgressListener progressListener) {
        AsyncTask<Void, Long, Boolean> asyncTask = new AsyncTask<Void, Long, Boolean>() {
            private FileOutputStream mFileOutputStream;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // callback
                if (onDownloadUpload != null) onDownloadUpload.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    mFileOutputStream = new FileOutputStream(localFile);
                    mDropboxApi.getFile(dropboxFile.path, null, mFileOutputStream, progressListener);
                } catch (Exception e) {
                    Log.e(LOGCAT, log(e.getMessage(), "downloadFileASync exception"));
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                //save date last modified
                if (result) {
                    setDateLastModified(dropboxFile.fileName(), RESTUtility.parseDate(dropboxFile.modified));
                }
                if (onDownloadUpload != null)
                    onDownloadUpload.onPostExecute(result);
            }
        };
        asyncTask.execute();
    }

    public void uploadFileAsync(final String dropboxFile, final File localFile, final OnDownloadUploadEntry onDownloadUpload, final ProgressListener progressListener) {
        AsyncTask<Void, Long, Boolean> asyncTask = new AsyncTask<Void, Long, Boolean>() {
            private FileInputStream mFileInputStream;
            private Entry mEntryDropboxFile;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // callback
                if (onDownloadUpload != null) onDownloadUpload.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    mFileInputStream = new FileInputStream(localFile);
                    UploadRequest uploadRequest = mDropboxApi.putFileOverwriteRequest(dropboxFile, mFileInputStream, localFile.length(), progressListener);
                    if (uploadRequest != null)
                        mEntryDropboxFile = uploadRequest.upload();
                } catch (Exception e) {
                    Log.e(LOGCAT, log(e.getMessage(), "uploadFileASync exception"));
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                //save date last modified
                if (result && mEntryDropboxFile != null) {
                    setDateLastModified(mEntryDropboxFile.fileName(), RESTUtility.parseDate(mEntryDropboxFile.modified));
                }
                if (onDownloadUpload != null)
                    onDownloadUpload.onPostExecute(result);
            }
        };
        asyncTask.execute();
    }

    private String log(String message, String messageIfNull) {
        return !TextUtils.isEmpty(message) ? message : messageIfNull;
    }

    /**
     * This function returns if the file is synchronized or not
     * @return int
     */
    public int checkIfFileIsSync() {
        if (isLinked()) {
            String localPath = MoneyManagerApplication.getDatabasePath(mContext.getApplicationContext());
            String remotePath = getLinkedRemoteFile();
            // check if file is correct
            if (TextUtils.isEmpty(localPath) || TextUtils.isEmpty(remotePath))
                return DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
            // check if remoteFile path is contain into localFile
            if (!localPath.toLowerCase().contains(remotePath.toLowerCase()))
                return DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
            // get File and Entry
            File localFile = new File(localPath);
            DropboxAPI.Entry remoteFile = getEntry(remotePath);
            // date lastModificed
            Date localLastModified = null;
            Date remoteLastModified = null;
            try {
                localLastModified = getDateLastModified(remoteFile.fileName());
                if (localLastModified == null)
                    localLastModified = new Date(localFile.lastModified());
                remoteLastModified = getLastModifiedEntry(remoteFile);
            } catch (Exception e) {
                String errorMessage = e.getMessage() == null
                        ? "Error in retrieving the last modified date in checkIfFileIsSync."
                        : e.getMessage();

                Log.e(LOGCAT, errorMessage);
                return DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
            }
            if (remoteLastModified.after(localLastModified)) {
                return DropboxServiceIntent.INTENT_EXTRA_MESSENGER_DOWNLOAD;
            } else if (remoteLastModified.before(localLastModified)) {
                return DropboxServiceIntent.INTENT_EXTRA_MESSENGER_UPLOAD;
            } else {
                return DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
            }
        }
        return DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
    }

    // interface for callbacks when call
    public interface OnGetEntries {
        public void onStarting();

        public void onFinished(List<Entry> result);
    }

    public interface OnDownloadUploadEntry {
        public void onPreExecute();

        public void onPostExecute(boolean result);
    }
}
