/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.home;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxManager;
import com.money.manager.ex.dropbox.DropboxServiceIntent;
import com.shamanland.fonticon.FontIconDrawable;

/**
 * Check for updates to the database on Dropbox. Ran on start of the main activity.
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
        try {
            if (DropboxServiceIntent.INTENT_EXTRA_MESSENGER_DOWNLOAD.equals(ret)) {
//            showNotificationSnackbar();
                showNotificationDialog();
            }
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "showing dropbox update notification dialog");
        }
    }

//    private void showNotificationSnackbar() {
//        // The context has to implement the callbacks interface!
//        final MainActivity mainActivity = (MainActivity) context;
//
//        Snackbar.with(context.getApplicationContext()) // context
//            .text(context.getString(R.string.dropbox_database_can_be_updted))
//            .actionLabel(context.getString(R.string.sync))
//            .actionColor(context.getResources().getColor(R.color.md_primary))
//            .actionListener(new ActionClickListener() {
//                @Override
//                public void onActionClicked(Snackbar snackbar) {
//                    DropboxManager dropbox = new DropboxManager(context, mDropboxHelper, mainActivity);
//                    dropbox.synchronizeDropbox();
//                }
//            })
//            .duration(5 * 1000)
//            .show(mainActivity);
//    }

    private void showNotificationDialog() {
        // The context has to implement the callbacks interface!
        // todo: code smell. Try to avoid conversion to main activity here! <- use EventBus
        final MainActivity mainActivity = (MainActivity) mContext;

        new AlertDialogWrapper.Builder(mContext)
            // setting alert dialog
            .setIcon(FontIconDrawable.inflate(mContext, R.xml.ic_alert))
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