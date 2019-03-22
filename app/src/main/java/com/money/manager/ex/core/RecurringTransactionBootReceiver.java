/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.money.manager.ex.notifications.RecurringTransactionReceiver;
import com.money.manager.ex.settings.BehaviourSettings;

import java.util.Calendar;

import timber.log.Timber;

/**
 * This class handles BOOT_RECEIVED event.
 */
public class RecurringTransactionBootReceiver
    extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // Start heartbeat for Recurring Transaction check.
            setAlarm(context);
        } catch (Exception e) {
            Timber.e(e, "Setting the alarm for recurring transactions check");
        }
    }

    private void setAlarm(Context context) {
        BehaviourSettings settings = new BehaviourSettings(context);

        boolean notify = settings.getNotificationRecurringTransaction();
        if (!notify) return;

        // compose intent
        Intent receiverIntent = new Intent(context, RecurringTransactionReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // take hour to start
        String hour = settings.getNotificationTime();
        // take a calendar and current time
        Calendar calendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(System.currentTimeMillis());
        // set time preferences
        calendar.add(Calendar.DAY_OF_YEAR, currentCalendar.get(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, currentCalendar.get(Calendar.MILLISECOND));
        calendar.set(Calendar.DATE, currentCalendar.get(Calendar.DATE));
        calendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(hour.substring(3, 5)));
        // add one day if hour was passed
        if (calendar.getTimeInMillis() < currentCalendar.getTimeInMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // cancel old pending intent
        alarmManager.cancel(pendingIntent);
        // start alarm manager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
