package com.money.manager.ex.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Build;
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
	
	@SuppressWarnings("deprecation")
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
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							notification = new NotificationCompat.Builder(context)
										   .setAutoCancel(true)
										   .setContentIntent(pendingIntent)
										   .setContentTitle(context.getString(R.string.application_name))
										   .setContentText(context.getString(R.string.notification_repeating_transaction_expired))
										   .setSubText(context.getString(R.string.notification_click_to_check_repeating_transaction))
										   .setSmallIcon(SMALLICON)
										   .build();
										   
						} else {
							notification = new Notification();
							notification.setLatestEventInfo(context, context.getString(R.string.application_name), context.getString(R.string.notification_repeating_transaction_expired), pendingIntent);
							notification.icon = SMALLICON;
						}
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
