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

package com.money.manager.ex.notifications;

import android.app.IntentService;
import android.content.Intent;

/**
 * Background service that triggers notifications about recurring transactions.
 */
public class RecurringTransactionIntentService
	extends IntentService {

	public RecurringTransactionIntentService() {
		super("com.money.manager.ex.notifications.RecurringTransactionIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// start repeating transaction
		RepeatingTransactionNotifications notifications = new RepeatingTransactionNotifications(getApplicationContext());
		notifications.notifyRepeatingTransaction();
	}
}
