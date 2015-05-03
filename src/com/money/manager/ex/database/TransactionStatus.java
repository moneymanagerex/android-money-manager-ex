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

    public static int getBackgroundColorFromStatus(Context ctx, String status) {
        if (Constants.TRANSACTION_STATUS_RECONCILED.equalsIgnoreCase(status)) {
            return ctx.getResources().getColor(R.color.material_green_500);
        } else if (Constants.TRANSACTION_STATUS_VOID.equalsIgnoreCase(status)) {
            return ctx.getResources().getColor(R.color.material_red_500);
        } else if (Constants.TRANSACTION_STATUS_FOLLOWUP.equalsIgnoreCase(status)) {
            return ctx.getResources().getColor(R.color.material_orange_500);
        } else if (Constants.TRANSACTION_STATUS_DUPLICATE.equalsIgnoreCase(status)) {
            return ctx.getResources().getColor(R.color.material_indigo_500);
        } else {
            return ctx.getResources().getColor(R.color.material_grey_500);
        }
    }
}


