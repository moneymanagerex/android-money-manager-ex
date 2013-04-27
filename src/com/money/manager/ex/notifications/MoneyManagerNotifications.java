/*******************************************************************************
 * Copyright (C) 2013 The Android Money Manager Ex Project
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
 ******************************************************************************/
package com.money.manager.ex.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.money.manager.ex.R;
import com.money.manager.ex.RepeatingTransactionListActivity;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryBillDeposits;

public class MoneyManagerNotifications {
	private static final String LOGCAT = MoneyManagerNotifications.class.getSimpleName();
	private static final int SMALLICON = R.drawable.ic_stat_notification;
	private Context context;
	
	public MoneyManagerNotifications(Context context) {
		super();
		this.context = context;
	}
	
	public void notifyRepeatingTransaction() {
		// select data
		QueryBillDeposits billDeposits = new QueryBillDeposits(context);
		MoneyManagerOpenHelper databaseHelper = new MoneyManagerOpenHelper(context);
		if (databaseHelper != null) {
			Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(billDeposits.getSource() + " AND " + QueryBillDeposits.DAYSLEFT + "<=0", null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);			
					// create pendig intent
					Intent intent = new Intent(context, RepeatingTransactionListActivity.class);			
					PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);			
					// create notification
					Notification notification = null;
					try {
						notification = new NotificationCompat.Builder(context).setAutoCancel(true).setContentIntent(pendingIntent)
								.setContentTitle(context.getString(R.string.application_name))
								.setContentText(context.getString(R.string.notification_repeating_transaction_expired))
								.setSubText(context.getString(R.string.notification_click_to_check_repeating_transaction)).setSmallIcon(SMALLICON).build();
						// notify 
						if (notification != null) {
							notification.tickerText = context.getString(R.string.notification_repeating_transaction_expired);
							notification.vibrate = new long[] {0,100,200,300};
							notification.defaults |= Notification.DEFAULT_VIBRATE;
							notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
							//notification.icon = android.R.drawable.stat_sys_warning;
							notificationManager.notify(0, notification);
						}
					} catch (Exception e) {
						Log.e(LOGCAT, e.getMessage());
					}
				}
				// close cursor
				cursor.close();
			}
			// close database helper
			databaseHelper.close();
		}
	}
}
