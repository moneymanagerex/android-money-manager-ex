/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.Constants;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.sync.SyncBroadcastReceiver;
import com.money.manager.ex.sync.SyncManager;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import timber.log.Timber;

/**
 * Schedules the periodic alarm/heartbeat that triggers synchronization.
 * Run from the settings when the synchronization interval changes, and on BOOT_COMPLETED.
 */
public class SyncSchedulerBroadcastReceiver
    extends BroadcastReceiver {

    public static final String ACTION_START = "com.money.manager.ex.intent.action.START_SYNC_SERVICE";
    public static final String ACTION_STOP = "com.money.manager.ex.intent.action.STOP_SYNC_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = "";
        //Log actions
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            action = intent.getAction();

            Timber.d("Action request: %s", action);
        }

        Intent syncIntent = new Intent(context, SyncBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, syncIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        // PendingIntent.FLAG_CANCEL_CURRENT, FLAG_UPDATE_CURRENT

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel existing heartbeat.
        if (action.equals(ACTION_STOP)) {
            Timber.d("Stopping synchronisation.");
            alarmManager.cancel(pendingIntent);
            return;
        }

        // by default, the action is ACTION_START. This is assumed on device boot.
        startHeartbeat(context, alarmManager, pendingIntent);
    }

    private void startHeartbeat(Context context, AlarmManager alarmManager, PendingIntent pendingIntent) {
        SyncManager sync = new SyncManager(context);
        if (!sync.isSyncEnabled()) return;

        // get frequency in minutes.
        SyncPreferences preferences = new SyncPreferences(context);
        int minutes = preferences.getSyncInterval();
        // If the period is 0, do not schedule sync.
        if (minutes <= 0) return;

        DateTime now = DateTime.now();

        Timber.d("Scheduling synchronisation at: %s, repeat every %s minutes", now.toString(), minutes);

        int secondsInMinute = 60;

        // Schedule alarm for synchronization. Run immediately the first time.
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                now.toDateTime().getMillis(),
                minutes * secondsInMinute * 1000,
                pendingIntent);
    }
}
