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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.recurring.transactions.RepeatingTransactionListActivity;
import com.money.manager.ex.utils.CurrencyUtils;

public class RepeatingTransactionNotifications {
    private static final String LOGCAT = RepeatingTransactionNotifications.class.getSimpleName();
    private static final int ID_NOTIFICATION = 0x000A;
    private Context mContext;

    public RepeatingTransactionNotifications(Context context) {
        super();
        mContext = context;
    }

    public void notifyRepeatingTransaction() {
        try {
            notifyRepeatingTransaction_Internal();
        } catch (Exception ex) {
            String error = "Error showing notification about recurring transactions";
            Log.e(LOGCAT, error + ": " + ex.getLocalizedMessage());
            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyRepeatingTransaction_Internal() {
        // create application
        CurrencyUtils currencyUtils = new CurrencyUtils(mContext);

        // select data
        QueryBillDeposits billDeposits = new QueryBillDeposits(mContext);

        MoneyManagerOpenHelper databaseHelper = MoneyManagerOpenHelper.getInstance(mContext);
        if (databaseHelper == null) return;
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        if(db == null) return;

        Cursor cursor = db.rawQuery(billDeposits.getSource() + " AND " +
                        QueryBillDeposits.DAYSLEFT + "<=0 ORDER BY " + QueryBillDeposits.NEXTOCCURRENCEDATE,
                null);
        if (cursor == null) return;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            while (!cursor.isAfterLast()) {
                String payeeName = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.PAYEENAME));
                // check if payee name is null, then put toAccountName
                if (TextUtils.isEmpty(payeeName))
                    payeeName = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.TOACCOUNTNAME));
                // compose text
                String line = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.USERNEXTOCCURRENCEDATE)) +
                        " " + payeeName +
                        ": <b>" + currencyUtils.getCurrencyFormatted(cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.CURRENCYID)), cursor.getDouble(cursor.getColumnIndex(QueryBillDeposits.AMOUNT))) + "</b>";
                // add line
                inboxStyle.addLine(Html.fromHtml("<small>" + line + "</small>"));
                // move to next row
                cursor.moveToNext();
            }

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            // create pending intent
            Intent intent = new Intent(mContext, RepeatingTransactionListActivity.class);
            // set launch from notification // check pin code
            intent.putExtra(RepeatingTransactionListActivity.INTENT_EXTRA_LAUNCH_NOTIFICATION, true);

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            // create notification
            Notification notification = null;
            try {
                notification = new NotificationCompat.Builder(mContext)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setContentTitle(mContext.getString(R.string.application_name))
                        .setContentText(mContext.getString(R.string.notification_repeating_transaction_expired))
                        .setSubText(mContext.getString(R.string.notification_click_to_check_repeating_transaction))
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setTicker(mContext.getString(R.string.notification_repeating_transaction_expired))
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                        .setNumber(cursor.getCount())
                        .setStyle(inboxStyle)
                        .setColor(mContext.getResources().getColor(R.color.md_primary))
                        .build();
                // notify
                notificationManager.cancel(ID_NOTIFICATION);
                notificationManager.notify(ID_NOTIFICATION, notification);
            } catch (Exception e) {
                Log.e(LOGCAT, e.getMessage());
            }
        }
        cursor.close();
    }
}
