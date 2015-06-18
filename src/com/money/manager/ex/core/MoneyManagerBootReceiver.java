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

package com.money.manager.ex.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.money.manager.ex.notifications.RepeatingTransactionReceiver;
import com.money.manager.ex.settings.PreferenceConstants;

import java.util.Calendar;

public class MoneyManagerBootReceiver
        extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            boolean notify = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(PreferenceConstants.PREF_REPEATING_TRANSACTION_NOTIFICATIONS), true);
            if (!notify) return;

            // compose intent
            Intent i = new Intent(context, RepeatingTransactionReceiver.class);
            PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            // take hour to start
            String hour = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(context.getString(PreferenceConstants.PREF_REPEATING_TRANSACTION_CHECK), "08:00");
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
            alarmManager.cancel(pending);
            // start alarm manager
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pending);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "Error checking for due recurring transactions");
//            Log.e(MoneyManagerBootReceiver.class.getSimpleName(), e.getMessage());
        }
    }

}
