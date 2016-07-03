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

package com.money.manager.ex.dropbox;

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

/**
 * Schedules the periodic alarm/heartbeat that triggers synchronization.
 * Run from the settings, when the synchronization interval changes and on BOOT_COMPLETED.
 */
public class SyncSchedulerBroadcastReceiver
    extends BroadcastReceiver {

    // action intents
    public static final String ACTION_START = "com.money.manager.ex.intent.action.START_SYNC_SERVICE";
    public static final String ACTION_STOP = "com.money.manager.ex.intent.action.STOP_SYNC_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = "";
        //Log actions
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            action = intent.getAction();
            if (BuildConfig.DEBUG) Log.d(this.getClass().getSimpleName(), "Action request: " + action);
        }

        Intent syncIntent = new Intent(context, SyncBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, syncIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel existing heartbeat.
        if (action.equals(ACTION_STOP)) {
            alarmManager.cancel(pendingIntent);
            return;
        }

        // by default, the action is ACTION_START. This is assumed on device boot.
        startHeartbeat(context, alarmManager, pendingIntent);
    }

    private void startHeartbeat(Context context, AlarmManager alarmManager, PendingIntent pendingIntent) {
        SyncManager sync = new SyncManager(context);
        if (!sync.isActive()) return;

        // get frequency in minutes.
        SyncPreferences preferences = new SyncPreferences(context);
        int minute = preferences.getSyncInterval();
        if (minute <= 0) return;

        DateTime now = DateTime.now();

        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getSimpleName(),
                    "Start at: " + now.toString(Constants.ISO_DATE_FORMAT)
                            + " and repeats every: " + minute + " minutes");
        }

        // Schedule alarm for synchronization
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.toDateTime().getMillis(),
                minute * 60 * 1000, pendingIntent);
    }
}
