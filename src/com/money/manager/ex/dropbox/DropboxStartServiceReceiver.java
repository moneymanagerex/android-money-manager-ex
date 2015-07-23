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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.settings.DropboxSettings;

/**
 * Receiver that gets the scheduled notification to run the synchronization task.
 */
public class DropboxStartServiceReceiver
		extends BroadcastReceiver {

    private String LOGCAT = this.getClass().getSimpleName();
    private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
        mContext = context;

        if (!shouldSynchronize()) {
            return;
        }


        DropboxHelper dropboxHelper = DropboxHelper.getInstance(context);
		//create intent to launch sync
		Intent service = new Intent(context, DropboxServiceIntent.class);
		service.setAction(DropboxServiceIntent.INTENT_ACTION_SYNC);
		service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, MoneyManagerApplication.getDatabasePath(context));
		service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, dropboxHelper.getLinkedRemoteFile());
		//start service
		context.startService(service);
	}

    private boolean shouldSynchronize() {
        // Check WiFi settings.
        // should we sync only on wifi?
        DropboxSettings settings = new DropboxSettings(mContext);
        if (BuildConfig.DEBUG) Log.i(LOGCAT, "Preferences set to sync on WiFi only.");
        if (settings.getShouldSyncOnWifi()) {
            // check if we are on WiFi connection.
            ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!mWifi.isConnected()) {
                Log.i(LOGCAT, "Not on WiFi connection. Not synchronizing.");
                return false;
            }
        }

        return true;
    }

}
