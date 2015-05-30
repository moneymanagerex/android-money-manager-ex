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
package com.money.manager.ex.checkingaccount;

import android.net.Uri;

import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.core.TransactionTypes;

import java.net.URL;

/**
 * Parameters for creating new transaction from an external intent.
 */
public class IntentDataParameters {

    public TransactionTypes transactionType;
    public int accountId;
    public int payeeId;
    public String payeeName;
    public double amount;
    public int categoryId;
    public String categoryName;

    public Uri toUri() {
        StringBuilder builder = new StringBuilder("content://parameters?");
        // content://parameters?account=account_name&transactionType=transaction_type
        // &amount=amount&payee=payee_name&category=category_name
        boolean firstParamAdded = false;

        if (accountId > 0) {
            builder.append(DataParser.PARAM_ACCOUNT);
            builder.append("=");
            builder.append(accountId);

            firstParamAdded = true;
        }

        if (transactionType != null) {
            if (firstParamAdded) {
                builder.append("&");
            }
            builder.append(DataParser.PARAM_TRANSACTION_TYPE);
            builder.append("=");
            builder.append(transactionType);

            firstParamAdded = true;
        }

        if (firstParamAdded) {
            builder.append("&");
        }
        builder.append(DataParser.PARAM_AMOUNT);
        builder.append("=");
        builder.append(amount);

        if (payeeName != null) {
            builder.append("&");
            builder.append(DataParser.PARAM_PAYEE);
            builder.append("=");
            builder.append(payeeName);
        }

        if (categoryName != null) {
            builder.append("&");
            builder.append(DataParser.PARAM_CATEGORY);
            builder.append("=");
            builder.append(categoryName);
        }

        String uriString = builder.toString();
        Uri uri = Uri.parse(uriString);

        return uri;
    }
}
