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

package com.money.manager.ex.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.money.manager.ex.settings.PreferenceConstants;

public class RepeatingTransactionReceiver
        extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        // If the notifications are disabled in settings, do not trigger the alarm.
        boolean notify = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(PreferenceConstants.PREF_REPEATING_TRANSACTION_NOTIFICATIONS), true);
        if (!notify) return;

		Intent service = new Intent(context, RepeatingTransactionService.class);
		context.startService(service);
	}

}
