/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import androidx.core.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.recurring.transactions.RecurringTransactionListActivity;
import com.money.manager.ex.utils.NotificationUtils;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class RecurringTransactionNotifications {

    public static String CHANNEL_ID = "RecurringTransaction_NotificationChannel";
    private static final int ID_NOTIFICATION = 0x000A;

    public RecurringTransactionNotifications(Context context) {
        super();
        mContext = context.getApplicationContext();
    }

    private Context mContext;

    public void notifyRepeatingTransaction() {
        try {
            notifyRepeatingTransaction_Internal();
        } catch (Exception ex) {
            Timber.e(ex, "showing notification about recurring transactions");
        }
    }

    private void notifyRepeatingTransaction_Internal() {
        QueryBillDeposits billDeposits = new QueryBillDeposits(mContext);

        /*
          In this query, the 0 days diff parameter HAS to be set in the query. Adding it in
          the parameters will not work (for whatever reason).
        */

        Cursor cursor = mContext.getContentResolver().query(billDeposits.getUri(),
                null,
                QueryBillDeposits.DAYSLEFT + "<=0",
                null,
                QueryBillDeposits.NEXTOCCURRENCEDATE);
        if (cursor == null) return;

        if (cursor.getCount() > 0) {
            SyncNotificationModel model = getNotificationContent(cursor);
            showNotification(model);
        }
        cursor.close();
    }

    private void showNotification(SyncNotificationModel model) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(model.inboxLine);

        NotificationManager notificationManager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // create pending intent
        Intent intent = new Intent(getContext(), RecurringTransactionListActivity.class);
        // set launch from notification // check pin code
        intent.putExtra(RecurringTransactionListActivity.INTENT_EXTRA_LAUNCH_NOTIFICATION, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

        // todo: Actions
//        Intent skipIntent = new Intent(intent);
//        //skipIntent.setAction(Intent.)
//        PendingIntent skipPending = PendingIntent.getActivity(getContext(), 0, skipIntent, 0);
//        Intent enterIntent = new Intent(getContext(), RecurringTransactionEditActivity.class);
//        PendingIntent enterPending = PendingIntent.getActivity(getContext(), 0, enterIntent, 0);

        // create notification
        try {
            NotificationUtils.createNotificationChannel(getContext(), CHANNEL_ID);

            Notification notification = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(mContext.getString(R.string.application_name))
                    .setContentText(mContext.getString(R.string.notification_repeating_transaction_expired))
                    .setSubText(mContext.getString(R.string.notification_click_to_check_repeating_transaction))
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setTicker(mContext.getString(R.string.notification_repeating_transaction_expired))
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                    .setNumber(model.number)
                    .setStyle(inboxStyle)
                    .setColor(mContext.getResources().getColor(R.color.md_primary))
//                    .addAction(R.drawable.ic_action_content_clear_dark, getContext().getString(R.string.skip), skipPending)
//                    .addAction(R.drawable.ic_action_done_dark, getContext().getString(R.string.enter), enterPending)
                    .build();

            // notify
            notificationManager.cancel(ID_NOTIFICATION);
            notificationManager.notify(ID_NOTIFICATION, notification);
        } catch (Exception e) {
            Timber.e(e, "showing notification for recurring transaction");
        }
    }

    private SyncNotificationModel getNotificationContent(Cursor cursor) {
        SyncNotificationModel result = new SyncNotificationModel();

        result.number = cursor.getCount();

        CurrencyService currencyService = new CurrencyService(mContext);

        while (cursor.moveToNext()) {
            String payeeName = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.PAYEENAME));
            // check if payee name is null, then put toAccountName
            if (TextUtils.isEmpty(payeeName))
                payeeName = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.TOACCOUNTNAME));
            // compose text
            String line = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.USERNEXTOCCURRENCEDATE)) +
                    " " + payeeName +
                    ": <b>" + currencyService.getCurrencyFormatted(cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.CURRENCYID)),
                    MoneyFactory.fromDouble(cursor.getDouble(cursor.getColumnIndex(QueryBillDeposits.AMOUNT)))) + "</b>";

            result.inboxLine = Html.fromHtml("<small>" + line + "</small>").toString();
        }

        return result;
    }

    private Context getContext() {
        return mContext;
    }
}
