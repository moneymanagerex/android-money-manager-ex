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

package com.money.manager.ex.database;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.TransactionStatuses;

import androidx.core.content.ContextCompat;

/**
 * Helper for transaction status - related issues, like colors, etc.
 * Created by Alessandro Lazzari on 08/09/2014.
 */
public class TransactionStatus {
    /**
     * @param status char of status
     * @return decode status char
     */
    public static String getStatusAsString(Context ctx, String status) {
//        if (TextUtils.isEmpty(status)) {
//            return ctx.getResources().getString(R.string.status_none);
//        } else if (Constants.TRANSACTION_STATUS_RECONCILED.equalsIgnoreCase(status)) {
//            return ctx.getResources().getString(R.string.status_reconciled);
//        } else if (Constants.TRANSACTION_STATUS_VOID.equalsIgnoreCase(status)) {
//            return ctx.getResources().getString(R.string.status_void);
//        } else if (Constants.TRANSACTION_STATUS_FOLLOWUP.equalsIgnoreCase(status)) {
//            return ctx.getResources().getString(R.string.status_follow_up);
//        } else if (Constants.TRANSACTION_STATUS_DUPLICATE.equalsIgnoreCase(status)) {
//            return ctx.getResources().getString(R.string.status_duplicate);
//        }
//        return "";

        String result;
        TransactionStatuses transactionStatus = TransactionStatuses.get(status);
        if (transactionStatus == null) return null;

        switch (transactionStatus) {
            case NONE:
                result = ctx.getResources().getString(R.string.status_none);
                break;
            case RECONCILED:
                result = ctx.getResources().getString(R.string.status_reconciled);
                break;
            case VOID:
                result = ctx.getResources().getString(R.string.status_void);
                break;
            case FOLLOWUP:
                result = ctx.getResources().getString(R.string.status_follow_up);
                break;
            case DUPLICATE:
                result = ctx.getResources().getString(R.string.status_duplicate);
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

    public static int getBackgroundColorFromStatus(Context ctx, String status) {
//        if (Constants.TRANSACTION_STATUS_RECONCILED.equalsIgnoreCase(status)) {
//            return ctx.getResources().getColor(R.color.material_green_500);
//        } else if (Constants.TRANSACTION_STATUS_VOID.equalsIgnoreCase(status)) {
//            return ctx.getResources().getColor(R.color.material_red_500);
//        } else if (Constants.TRANSACTION_STATUS_FOLLOWUP.equalsIgnoreCase(status)) {
//            return ctx.getResources().getColor(R.color.material_orange_500);
//        } else if (Constants.TRANSACTION_STATUS_DUPLICATE.equalsIgnoreCase(status)) {
//            return ctx.getResources().getColor(R.color.material_indigo_500);
//        } else {
//            return ctx.getResources().getColor(R.color.material_grey_500);
//        }

        int result;
        TransactionStatuses txStatus = TransactionStatuses.get(status);
        if (txStatus == null) {
            txStatus = TransactionStatuses.NONE;
        }

        switch (txStatus) {
//            case NONE:
//                result = ctx.getResources().getString(R.string.status_none);
//                break;
            case RECONCILED:
                result = ContextCompat.getColor(ctx, R.color.material_green_500);
                break;
            case VOID:
                result = ContextCompat.getColor(ctx, R.color.material_red_500);
                break;
            case FOLLOWUP:
                result = ContextCompat.getColor(ctx, R.color.material_orange_500);
                break;
            case DUPLICATE:
                result = ContextCompat.getColor(ctx, R.color.material_indigo_500);
                break;
            default:
                result = ContextCompat.getColor(ctx, R.color.material_grey_500);
                break;
        }
        return result;
    }
}


