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

import android.content.Context;
import android.net.Uri;

import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.businessobjects.CategoryService;
import com.money.manager.ex.businessobjects.PayeeService;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.AccountRepository;

import java.net.URL;

/**
 * Parameters for creating new transaction from an external intent.
 */
public class IntentDataParameters {

    // Keys for uri parameters to be passed as data in intent.
    public static final String PARAM_TRANSACTION_TYPE = "transactionType";
    // account name!
    public static final String PARAM_ACCOUNT = "account";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_PAYEE = "payee";
    public static final String PARAM_CATEGORY = "category";


    public TransactionTypes transactionType;
    public int accountId;
    public String accountName;
    public int payeeId;
    public String payeeName;
    public double amount;
    public int categoryId;
    public String categoryName;

    public static IntentDataParameters parseData(Context context, Uri data) {
        IntentDataParameters parameters = new IntentDataParameters();

        // transaction type
        String transactionTypeName = data.getQueryParameter(PARAM_TRANSACTION_TYPE);
        TransactionTypes type = TransactionTypes.valueOf(transactionTypeName);
        if (type != null) parameters.transactionType = type;

        // account
        String accountName = data.getQueryParameter(PARAM_ACCOUNT);
        if (accountName != null) {
            AccountRepository account = new AccountRepository(context);
            int accountId = account.loadIdByName(accountName);
            parameters.accountId = accountId;
        }

        parameters.payeeName = data.getQueryParameter(PARAM_PAYEE);
        if (parameters.payeeName != null) {
            PayeeService payee = new PayeeService(context);
            int payeeId = payee.loadIdByName(parameters.payeeName);
            parameters.payeeId = payeeId;
        }

        String amount = data.getQueryParameter(PARAM_AMOUNT);
        parameters.amount = Double.parseDouble(amount);

        parameters.categoryName = data.getQueryParameter(PARAM_CATEGORY);
        if (parameters.categoryName != null) {
            CategoryService category = new CategoryService(context);
            int categoryId = category.loadIdByName(parameters.categoryName);
            parameters.categoryId = categoryId;
        }

        return parameters;
    }

    public Uri toUri() {
        StringBuilder builder = new StringBuilder("content://parameters?");
        // content://parameters?account=account_name&transactionType=transaction_type
        // &amount=amount&payee=payee_name&category=category_name
        boolean firstParamAdded = false;

        if (accountName != null) {
//        if (accountId > 0) {
            builder.append(PARAM_ACCOUNT);
            builder.append("=");
            builder.append(accountName);

            firstParamAdded = true;
        }

        if (transactionType != null) {
            if (firstParamAdded) {
                builder.append("&");
            }
            builder.append(PARAM_TRANSACTION_TYPE);
            builder.append("=");
            builder.append(transactionType);

            firstParamAdded = true;
        }

        if (firstParamAdded) {
            builder.append("&");
        }
        builder.append(PARAM_AMOUNT);
        builder.append("=");
        builder.append(amount);

        if (payeeName != null) {
            builder.append("&");
            builder.append(PARAM_PAYEE);
            builder.append("=");
            builder.append(payeeName);
        }

        if (categoryName != null) {
            builder.append("&");
            builder.append(PARAM_CATEGORY);
            builder.append("=");
            builder.append(categoryName);
        }

        String uriString = builder.toString();
        Uri uri = Uri.parse(uriString);

        return uri;
    }
}
