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

package com.money.manager.ex.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

import com.money.manager.ex.settings.PreferenceConstants;

public class RecurringTransactionReceiver
	extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        // If the notifications are disabled in preferences, do not trigger the alarm.
        boolean notify = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(PreferenceConstants.PREF_REPEATING_TRANSACTION_NOTIFICATIONS), true);
        if (!notify) return;

		Intent myIntent = new Intent(context, RecurringTransactionIntentService.class);
        RecurringTransactionIntentService.enqueueWork(context, myIntent);
    }

}
