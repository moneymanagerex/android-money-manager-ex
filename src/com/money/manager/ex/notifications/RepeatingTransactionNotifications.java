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

import com.money.manager.ex.R;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.recurring.transactions.RepeatingTransactionListActivity;
import com.money.manager.ex.utils.CurrencyUtils;

public class RepeatingTransactionNotifications {
    private static final String LOGCAT = RepeatingTransactionNotifications.class.getSimpleName();
    private static final int ID_NOTIFICATION = 0x000A;
    private Context context;

    public RepeatingTransactionNotifications(Context context) {
        super();
        this.context = context;
    }

    public void notifyRepeatingTransaction() {
        // create application
        CurrencyUtils currencyUtils = new CurrencyUtils(context);
        // init currencies
        if (!currencyUtils.isInit())
            currencyUtils.init();

        // select data
        QueryBillDeposits billDeposits = new QueryBillDeposits(context);
        MoneyManagerOpenHelper databaseHelper = MoneyManagerOpenHelper.getInstance(context);

        if (databaseHelper != null) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            if(db == null) return;

            Cursor cursor = db.rawQuery(billDeposits.getSource() + " AND " +
                    QueryBillDeposits.DAYSLEFT + "<=0 ORDER BY " + QueryBillDeposits.NEXTOCCURRENCEDATE, null);
            if (cursor != null) {
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

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    // create pendig intent
                    Intent intent = new Intent(context, RepeatingTransactionListActivity.class);
                    // set launch from notification // check pin code
                    intent.putExtra(RepeatingTransactionListActivity.INTENT_EXTRA_LAUNCH_NOTIFICATION, true);

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    // create notification
                    Notification notification = null;
                    try {
                        notification = new NotificationCompat.Builder(context)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent)
                                .setContentTitle(context.getString(R.string.application_name))
                                .setContentText(context.getString(R.string.notification_repeating_transaction_expired))
                                .setSubText(context.getString(R.string.notification_click_to_check_repeating_transaction))
                                .setSmallIcon(R.drawable.ic_stat_notification)
                                .setTicker(context.getString(R.string.notification_repeating_transaction_expired))
                                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                                .setNumber(cursor.getCount())
                                .setStyle(inboxStyle)
                                .setColor(context.getResources().getColor(R.color.md_primary))
                                .build();
                        // notify
                        notificationManager.cancel(ID_NOTIFICATION);
                        notificationManager.notify(ID_NOTIFICATION, notification);
                    } catch (Exception e) {
                        Log.e(LOGCAT, e.getMessage());
                    }
                }
                // close cursor
                cursor.close();
            } //

            // close database helper
            //databaseHelper.close();
            db.close();
        }
    }
}
