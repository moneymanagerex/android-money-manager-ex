/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Background service that triggers notifications about recurring transactions.
 *
 * Updated to work manager as per
 * https://developer.android.com/reference/androidx/work/WorkManager
 */
public class ScheduledTransactionWorker
		extends Worker {

	public ScheduledTransactionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		RecurringTransactionNotifications notifications = new RecurringTransactionNotifications(getApplicationContext());
		notifications.notifyRepeatingTransaction();
		return Result.success();
	}

	public static void enqueueWork(Context context) {
		WorkRequest recurringTransactionWorkRequest = new OneTimeWorkRequest.Builder(ScheduledTransactionWorker.class)
				.build();

		WorkManager.getInstance(context).enqueue(recurringTransactionWorkRequest);
	}
}
