/*
 * Copyright (C) 2012-2025 The Android Money Manager Ex Project Team
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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;

import java.util.Date;

import timber.log.Timber;

/**
 * Validation, saving and notification shared by the SMS transaction processors.
 */
class SmsTransactionHelper {

    static final String PASS = "PASS";
    static final String TRANS_SOURCE = "SmsReceiverTransactions.java";

    private SmsTransactionHelper() {
    }

    static String validate(Context context, ITransactionEntity txn) {

        if (txn.getAccountId().equals(Constants.NOT_SET)) {
            return context.getString(R.string.error_fromaccount_not_selected);
        }

        // Amount is required and must be positive. Sign is determined by transaction type.
        if (txn.getAmount().toDouble() <= 0) {
            return context.getString(R.string.error_amount_must_be_positive);
        }

        if (txn.getTransactionType().equals(TransactionTypes.Transfer)) {

            if (txn.getToAccountId().equals(Constants.NOT_SET)) {
                return context.getString(R.string.error_toaccount_not_selected);
            }

            if (txn.getToAccountId().equals(txn.getAccountId())) {
                return context.getString(R.string.error_transfer_to_same_account);
            }

            // Amount To is required and has to be positive.
            if (txn.getToAmount().toDouble() <= 0) {
                return context.getString(R.string.error_amount_must_be_positive);
            }
        } else { // payee required for automatic transactions.
            if (!txn.hasPayee()) {
                return context.getString(R.string.error_payee_not_selected);
            }
        }

        // Category is required if tx is not a split or transfer.
        if (!txn.hasCategory()) {
            return context.getString(R.string.error_category_not_selected);
        }

        return PASS;
    }

    /**
     * Inserts or updates the transaction. On insert the new id is set on the entity.
     */
    static String save(Context context, AccountTransaction txn) {

        AccountTransactionRepository repo = new AccountTransactionRepository(context);

        if (!txn.hasId()) { // insert
            repo.insert(txn);

            if (!txn.hasId()) { //Insert new transaction failed!
                return context.getString(R.string.db_checking_insert_failed);
            }
        } else { // update
            if (!repo.update(txn)) { //Update transaction failed!
                return context.getString(R.string.db_checking_update_failed);
            }
        }
        return PASS;
    }

    /**
     * Intent that opens the transaction edit screen pre-filled with the transaction.
     */
    static Intent buildEditIntent(Context context, ITransactionEntity txn, String payeeName) {
        Intent intent = new Intent(context, CheckingTransactionEditActivity.class);

        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, TRANS_SOURCE);
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, txn.getId());
        intent.putExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, String.valueOf(txn.getAccountId()));
        intent.putExtra(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID, String.valueOf(txn.getToAccountId()));
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_CODE,
                txn.getTransactionType() == null ? null : txn.getTransactionType().name());
        intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_ID, String.valueOf(txn.getPayeeId()));
        intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_NAME, payeeName);
        intent.putExtra(EditTransactionActivityConstants.KEY_CATEGORY_ID, String.valueOf(txn.getCategoryId()));
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_AMOUNT, String.valueOf(txn.getAmount()));
        intent.putExtra(EditTransactionActivityConstants.KEY_NOTES, txn.getNotes());
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_DATE, txn.getDate());
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_NUMBER, txn.getTransactionNumber());

        return intent;
    }

    /**
     * Note: Check the new NotificationUtils for creation of notification channel and the code that
     * utilizes it.
     */
    static void showNotification(Context context, Intent intent, String notificationText,
                                 String msgSender, String txnStatus, String errorMsg) {
        showCustomNotification(context, intent,
                context.getString(R.string.notification_process_sms_transaction_status) + ": " + txnStatus + errorMsg,
                context.getString(R.string.notification_click_to_edit_transaction),
                msgSender + " : " + notificationText,
                txnStatus);
    }

    static void showCustomNotification(Context context, Intent intent, String title, String subText,
                                       String text, String txnStatus) {

        try {

            String GROUP_KEY_AMMEX = "com.android.example.MoneyManagerEx";
            int ID_NOTIFICATION = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, ID_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            // Create the NotificationChannel
            NotificationChannel nChannel = new NotificationChannel(SmsReceiverTransactions.CHANNEL_ID, "AMMEXSMS", NotificationManager.IMPORTANCE_DEFAULT);
            nChannel.setDescription(context.getString(R.string.notification_process_sms_channel_description));

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(nChannel);

            Notification notification = new NotificationCompat.Builder(context, SmsReceiverTransactions.CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setSubText(subText)
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                    .setGroup(GROUP_KEY_AMMEX)
                    .build();

            // Change the notification color based on the status
            switch (txnStatus) {
                case "Auto Failed":
                    notification.color = context.getResources().getColor(R.color.md_red);
                    break;  //optional
                case "Already Exists":
                    notification.color = context.getResources().getColor(R.color.md_indigo);
                    break;  //optional
                default:
                    notification.color = context.getResources().getColor(R.color.md_primary);
            }

            // notify
            notificationManager.notify(ID_NOTIFICATION, notification);

        } catch (Exception e) {
            Timber.e(e, "showing notification for sms transaction");
        }
    }
}
