/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.sync;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.settings.SyncPreferences;

import timber.log.Timber;

/**
 * Receiver that is triggered by the alarm to run synchronization.
 * Triggered by the timer/heartbeat. Set up in SyncSchedulerBroadcastReceiver.
 */
public class SyncBroadcastReceiver
	extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        Timber.d("receiving a sync intent");

		SyncManager sync = new SyncManager(context);
        if (!sync.canSync()) return;

		// check sync interval.
		if (new SyncPreferences(context).getSyncInterval() == 0) return;

		// Trigger synchronization

		Intent myIntent = new Intent(context, SyncService.class);
		myIntent.setAction(SyncConstants.INTENT_ACTION_SYNC);

		myIntent.putExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE,
				new DatabaseManager(context).getDatabasePath());
		myIntent.putExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE, sync.getRemotePath());

		SyncService.enqueueWork(context, myIntent);
	}
}
