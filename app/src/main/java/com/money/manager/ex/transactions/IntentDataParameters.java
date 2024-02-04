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
package com.money.manager.ex.transactions;

import android.content.Context;
import android.net.Uri;

import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.servicelayer.PayeeService;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Parameters for creating new transaction from an external intent.
 */
public class IntentDataParameters {

    // Keys for uri parameters to be passed as data in intent.
    public static final String PARAM_TRANSACTION_TYPE = "transactionType";
    // account name!
    public static final String PARAM_ACCOUNT = "account";
    public static final String PARAM_ACCOUNT_TO = "accountTo";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_AMOUNT_TO = "amountTo";
    public static final String PARAM_PAYEE = "payee";
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_SUBCATEGORY = "subcategory";
    public static final String PARAM_NOTES = "notes";
    public static final String PARAM_SILENT_MODE = "silent";

    public TransactionTypes transactionType;
    public int accountId;
    public int accountToId;
    public String accountName;
    public int payeeId;
    public String payeeName;
    public Money amount;
    public Money amountTo;
    public int categoryId;
    public String categoryName;
    public int subcategoryId;
    public String subcategoryName;
    public String notes;
    public boolean isSilentMode;

    public static IntentDataParameters parseData(final Context context, final Uri data) {
        final IntentDataParameters parameters = new IntentDataParameters();

        // transaction type
        final String transactionTypeName = data.getQueryParameter(PARAM_TRANSACTION_TYPE);
        final TransactionTypes type = TransactionTypes.valueOf(transactionTypeName);
        if (null != type) parameters.transactionType = type;

        // account
        final String accountName = data.getQueryParameter(PARAM_ACCOUNT);
        if (null != accountName) {
            final AccountRepository account = new AccountRepository(context);
            final int accountId = account.loadIdByName(accountName);
            parameters.accountId = accountId;
        }
        final String accountToName = data.getQueryParameter(PARAM_ACCOUNT_TO);
        if (null != accountToName) {
            final AccountRepository accountTo = new AccountRepository(context);
            final int accountToId = accountTo.loadIdByName(accountToName);
            parameters.accountToId = accountToId;
        }

        parameters.payeeName = data.getQueryParameter(PARAM_PAYEE);
        if (null != parameters.payeeName) {
            final PayeeService payee = new PayeeService(context);
            final int payeeId = payee.loadIdByName(parameters.payeeName);
            parameters.payeeId = payeeId;
        }

        final String amount = data.getQueryParameter(PARAM_AMOUNT);
        if (null != amount && !amount.isEmpty()) {
            parameters.amount = MoneyFactory.fromString(amount);
        }
        final String amountTo = data.getQueryParameter(PARAM_AMOUNT_TO);
        if (null != amountTo && !amountTo.isEmpty()) {
            parameters.amountTo = MoneyFactory.fromString(amountTo);
        }

        parameters.categoryName = data.getQueryParameter(PARAM_CATEGORY);
        if (null != parameters.categoryName) {
            final CategoryService category = new CategoryService(context);
            final int categoryId = category.loadIdByName(parameters.categoryName);
            parameters.categoryId = categoryId;
        }

        parameters.subcategoryName = data.getQueryParameter(PARAM_SUBCATEGORY);
        if (null != parameters.subcategoryName) {
            final CategoryService category = new CategoryService(context);
            final int subcategoryId = category.loadIdByName(parameters.subcategoryName, parameters.categoryId);
            parameters.subcategoryId = subcategoryId;
        }

        parameters.notes = data.getQueryParameter(PARAM_NOTES);
        parameters.isSilentMode = Boolean.parseBoolean(data.getQueryParameter(PARAM_SILENT_MODE));

        return parameters;
    }

    public Uri toUri() {
        final StringBuilder builder = new StringBuilder("content://parameters?");
        // content://parameters?account=account_name&transactionType=transaction_type
        // &amount=amount&payee=payee_name&category=category_name
        boolean firstParamAdded = false;

        if (null != accountName) {
//        if (accountId > 0) {
            builder.append(PARAM_ACCOUNT);
            builder.append("=");
            builder.append(accountName);

            firstParamAdded = true;
        }

        if (null != transactionType) {
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

        if (null != payeeName) {
            builder.append("&");
            builder.append(PARAM_PAYEE);
            builder.append("=");
            builder.append(payeeName);
        }

        if (null != categoryName) {
            builder.append("&");
            builder.append(PARAM_CATEGORY);
            builder.append("=");
            builder.append(categoryName);
        }

        final String uriString = builder.toString();
        final Uri uri = Uri.parse(uriString);

        return uri;
    }
}
