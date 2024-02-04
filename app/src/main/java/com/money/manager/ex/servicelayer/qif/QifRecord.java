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
package com.money.manager.ex.servicelayer.qif;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.datalayer.SplitCategoriesRepository;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.viewmodels.AccountTransactionDisplay;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import info.javaperformance.money.Money;

/**
 * A .qif file record. Represents a first transaction or account header record.
 * References:
 * QIF format
 * http://en.wikipedia.org/wiki/Quicken_Interchange_Format
 * Date formats inside qif file
 * http://www.unicode.org/reports/tr35/tr35-dates.html#Date_Format_Patterns
 */
public class QifRecord {
    private final Context mContext;

    public QifRecord(final Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Parses the data and generates a QIF record for transaction.
     *
     * @return A string representing one QIF record
     */
    public String parse(final AccountTransactionDisplay transaction) throws ParseException {
        final String lineSeparator = System.getProperty("line.separator");

        final StringBuilder builder = new StringBuilder();

        // Date
        final String date = parseDate(transaction);
        builder.append("D");
        builder.append(date);
        builder.append(lineSeparator);

        // Amount
        final String amount = parseAmount(transaction);
//        builder.append("U");
//        builder.append(this.Amount);
//        builder.append(System.lineSeparator());
        builder.append("T");
        builder.append(amount);
        builder.append(lineSeparator);

        // Cleared status
//        String status = parseCleared(transaction);
        final String status = transaction.getStatusCode();
        if (!TextUtils.isEmpty(status)) {
            // Cleared: * or c. We don't have Cleared in MMEX.
            // Reconciled: X or R
            builder.append("C");
            if ("R".equals(status)) {
                builder.append(status);
            }
        }

        // Payee
        final String payee = transaction.getPayee();
        if (!TextUtils.isEmpty(payee)) {
            builder.append("P");
            builder.append(payee);
            builder.append(lineSeparator);
        }

        // Categories / Transfers
        String category;
//        String transactionTypeName = getTransactionTypeName(transaction);
        final TransactionTypes transactionType = transaction.getTransactionType();
        if (transactionType == TransactionTypes.Transfer) {
            // Category is the destination account name.
            category = transaction.getAccountName();
            // in square brackets
            category = "[%]".replace("%", category);
        } else {
            // Category
            category = parseCategory(transaction);
        }
        if (null != category) {
            builder.append("L");
            builder.append(category);
            builder.append(lineSeparator);
        }

        // Split Categories
        final boolean splitCategory = transaction.getIsSplit();
        if (splitCategory) {
            final String splits = getSplitCategories(transaction);
            builder.append(splits);
        }

        // Memo
        final String memo = transaction.getNotes();
        if (!TextUtils.isEmpty(memo)) {
            builder.append("M");
            builder.append(memo);
            builder.append(lineSeparator);
        }

        builder.append("^");
        builder.append(lineSeparator);

        return builder.toString();
    }

    public String getSplitCategories(final AccountTransactionDisplay transaction) {
        final StringBuilder builder = new StringBuilder();

        // retrieve splits
        final SplitCategoriesRepository repo = new SplitCategoriesRepository(mContext);
        final int transactionId = transaction.getId();
        final ArrayList<ISplitTransaction> splits = repo.loadSplitCategoriesFor(transactionId);
        if (null == splits) return Constants.EMPTY_STRING;

        final String transactionType = transaction.getTransactionTypeName();

        for (final ISplitTransaction split : splits) {
            final String splitRecord = getSplitCategory(split, transactionType);
            builder.append(splitRecord);
        }

        return builder.toString();
    }

    private String getSplitCategory(final ISplitTransaction split, final String transactionType) {
        final String lineSeparator = System.getProperty("line.separator");

        final StringBuilder builder = new StringBuilder();

        // S = category in split
        // $ = amount in split
        // E = memo in split

        // category
        final CategoryService service = new CategoryService(mContext);
        final String category = service.getCategorySubcategoryName(split.getCategoryId());
        builder.append("S");
        builder.append(category);
        builder.append(lineSeparator);

        // amount
        Money amount = split.getAmount();
        // e sign
        if (TransactionTypes.valueOf(transactionType) == TransactionTypes.Withdrawal) {
            amount = amount.negate();
        }
        if (TransactionTypes.valueOf(transactionType) == TransactionTypes.Deposit) {
            // leave positive?
        }
        builder.append("$");
        builder.append(amount);
        builder.append(lineSeparator);

        // memo - currently we don't have a field for it.
//        String memo = split.get

        return builder.toString();
    }

    private String parseDate(final AccountTransactionDisplay transaction) throws ParseException {
        final Date date = transaction.getDate();

        // todo: get Quicken date format from preferences.
        final String qifDatePattern = "MM/dd''yy";
//        DateTimeFormatter qifFormat = DateTimeFormat.forPattern();
//        return qifFormat.print(date);

        final MmxDate dateTime = new MmxDate(date);
        return dateTime.toString(qifDatePattern);
    }

    private String parseAmount(final AccountTransactionDisplay transaction) {
        final String amount;
        if (transaction.getTransactionType() == TransactionTypes.Transfer) {
            amount = transaction.getToAmount().toString();
        } else {
            amount = transaction.getAmount().toString();
        }
        return amount;
    }

    private String parseCategory(final AccountTransactionDisplay transaction) {
        final String category = transaction.getCategory();
        final String subCategory = transaction.getSubcategory();

        if (!TextUtils.isEmpty(subCategory)) {
            return category + ":" + subCategory;
        } else {
            return category;
        }
    }
}
