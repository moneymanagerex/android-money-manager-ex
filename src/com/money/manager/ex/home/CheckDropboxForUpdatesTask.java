package com.money.manager.ex.home;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.R;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxManager;
import com.money.manager.ex.dropbox.DropboxServiceIntent;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

/**
 * Check for updates to the database on Dropbox. Ran on start of the main activity.
 * Created by Alen Siljak on 09/07/2015.
 */
public class CheckDropboxForUpdatesTask
    extends AsyncTask<Void, Integer, Integer> {

    public CheckDropboxForUpdatesTask(Context context, DropboxHelper helper) {
        mContext = context;
        mDropboxHelper = helper;
    }

    private Context mContext;
    private DropboxHelper mDropboxHelper;

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            publishProgress(1);

            return mDropboxHelper.checkIfFileIsSync();
        } catch (Exception e) {
            throw new RuntimeException("Error in checkDropboxForUpdates", e);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        Toast.makeText(mContext, R.string.checking_dropbox_for_changes, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Integer ret) {
        if (DropboxServiceIntent.INTENT_EXTRA_MESSENGER_DOWNLOAD.equals(ret)) {
//            showNotificationSnackbar();
            showNotificationDialog();
        }
    }

    private void showNotificationSnackbar() {
        // The context has to implement the callbacks interface!
        final MainActivity mainActivity = (MainActivity) mContext;

        Snackbar.with(mContext.getApplicationContext()) // context
            .text(mContext.getString(R.string.dropbox_database_can_be_updted))
            .actionLabel(mContext.getString(R.string.sync))
            .actionColor(mContext.getResources().getColor(R.color.md_primary))
            .actionListener(new ActionClickListener() {
                @Override
                public void onActionClicked(Snackbar snackbar) {
                    DropboxManager dropbox = new DropboxManager(mContext, mDropboxHelper, mainActivity);
                    dropbox.synchronizeDropbox();
                }
            })
            .duration(5 * 1000)
            .show(mainActivity);
    }

    private void showNotificationDialog() {
        // The context has to implement the callbacks interface!
        final MainActivity mainActivity = (MainActivity) mContext;

        new AlertDialogWrapper.Builder(mContext)
                // setting alert dialog
                .setIcon(R.drawable.ic_action_warning_light)
                .setTitle(R.string.update_available)
                .setMessage(R.string.update_on_dropbox)
                .setNeutralButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DropboxManager dropbox = new DropboxManager(mContext, mDropboxHelper, mainActivity);
                        dropbox.synchronizeDropbox();
                        dialog.dismiss();
                    }
                })
                .show();
    }
}