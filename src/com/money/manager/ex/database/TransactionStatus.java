package com.money.manager.ex.database;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;

/**
 * Created by Alessandro Lazzari on 08/09/2014.
 */
public class TransactionStatus {
    /**
     * @param status char of status
     * @return decode status char
     */
    public static String getStatusAsString(Context ctx, String status) {
        if (TextUtils.isEmpty(status)) {
            return ctx.getResources().getString(R.string.status_none);
        } else if (Constants.TRANSACTION_STATUS_RECONCILED.equalsIgnoreCase(status)) {
            return ctx.getResources().getString(R.string.status_reconciled);
        } else if (Constants.TRANSACTION_STATUS_VOID.equalsIgnoreCase(status)) {
            return ctx.getResources().getString(R.string.status_void);
        } else if (Constants.TRANSACTION_STATUS_FOLLOWUP.equalsIgnoreCase(status)) {
            return ctx.getResources().getString(R.string.status_follow_up);
        } else if (Constants.TRANSACTION_STATUS_DUPLICATE.equalsIgnoreCase(status)) {
            return ctx.getResources().getString(R.string.status_duplicate);
        }
        return "";
    }
}


