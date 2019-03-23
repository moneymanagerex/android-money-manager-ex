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

import android.content.Context;
import android.content.Intent;
import androidx.core.app.JobIntentService;

/**
 * Background service that triggers notifications about recurring transactions.
 *
 * Updated to JobIntentService as per
 * https://android.jlelse.eu/keep-those-background-services-working-when-targeting-android-oreo-sdk-26-cbf6cc2bdb7f
 */
public class RecurringTransactionIntentService
	extends JobIntentService {

    public static int JOB_ID = 1001;

//	public RecurringTransactionIntentService() {
//		super("com.money.manager.ex.notifications.RecurringTransactionIntentService");
//	}

	@Override
	protected void onHandleWork(Intent intent) {
		// start repeating transaction
		RecurringTransactionNotifications notifications = new RecurringTransactionNotifications(getApplicationContext());
		notifications.notifyRepeatingTransaction();
	}

	public static void enqueueWork(Context context, Intent intent) {
	    enqueueWork(context, RecurringTransactionIntentService.class, JOB_ID, intent);
    }
}
