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
package com.money.manager.ex.businessobjects.qif;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.SplitCategoriesRepository;
import com.money.manager.ex.database.TableSplitTransactions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * A .qif file record. Represents a single transaction or account header record.
 * References:
 * QIF format
 * http://en.wikipedia.org/wiki/Quicken_Interchange_Format
 * Date formats inside qif file
 * http://www.unicode.org/reports/tr35/tr35-dates.html#Date_Format_Patterns
 */
public class QifRecord {
    public QifRecord(Context context) {
        mContext = context;
    }

    private Context mContext;

    /**
     * Returns a string representing one QIF record.
     * @return
     */
    public String parse(Cursor cursor) throws ParseException {
        StringBuilder builder = new StringBuilder();

        // Date
        String date = parseDate(cursor);
        builder.append("D");
        builder.append(date);
        builder.append(System.lineSeparator());

        // Amount
        String amount = parseAmount(cursor);
//        builder.append("U");
//        builder.append(this.Amount);
//        builder.append(System.lineSeparator());
        builder.append("T");
        builder.append(amount);
        builder.append(System.lineSeparator());

        // Cleared status
        String cleared = parseCleared(cursor);
        if (!TextUtils.isEmpty(cleared)) {
            builder.append("C");
            builder.append(cleared);
        }

        // Payee
        String payee = cursor.getString(cursor.getColumnIndex(QueryAllData.Payee));
        if (!TextUtils.isEmpty(payee)) {
            builder.append("P");
            builder.append(payee);
            builder.append(System.lineSeparator());
        }

        // Categories / Transfers
        String category;
        String transactionTypeName = getTransactionType(cursor);
        TransactionTypes transactionType = TransactionTypes.valueOf(transactionTypeName);
        if (transactionType.equals(TransactionTypes.Transfer)) {
            // Category is the destination account name.
            category = cursor.getString(cursor.getColumnIndex(QueryAllData.FromAccountName));
            // in square brackets
            category = "[%]".replace("%", category);
        } else {
            // Category
            category = parseCategory(cursor);
        }
        if (category != null) {
            builder.append("L");
            builder.append(category);
            builder.append(System.lineSeparator());
        }

        // Split Categories
        int splitCategory = cursor.getInt(cursor.getColumnIndex(QueryAllData.Splitted));
        if (splitCategory == 1) {
            String splits = getSplitCategories(cursor);
            builder.append(splits);
        }

        // Memo
        String memo = parseMemo(cursor);
        if (!TextUtils.isEmpty(memo)) {
            builder.append("M");
            builder.append(memo);
            builder.append(System.lineSeparator());
        }

        builder.append("^");
        builder.append(System.lineSeparator());

        return builder.toString();
    }

    public int getAccountId(Cursor cursor) {
        int accountId = cursor.getInt(cursor.getColumnIndex(QueryAllData.ACCOUNTID));
        return accountId;
    }

    public String getSplitCategories(Cursor cursor) {
        StringBuilder builder = new StringBuilder();

        // retrieve splits
        SplitCategoriesRepository repo = new SplitCategoriesRepository(mContext);
        int transactionId = getTransactionId(cursor);
        ArrayList<TableSplitTransactions> splits = repo.loadSplitCategoriesFor(transactionId);
        if (splits == null) return Constants.EMPTY_STRING;

        String transactionType = getTransactionType(cursor);

        for(TableSplitTransactions split : splits) {
            String splitRecord = getSplitCategory(split, transactionType);
            builder.append(splitRecord);
        }

        return builder.toString();
    }

    private String getSplitCategory(TableSplitTransactions split, String transactionType) {
        StringBuilder builder = new StringBuilder();
        Core core = new Core(mContext);

        // S = category in split
        // $ = amount in split
        // E = memo in split

        // category
        String category = core.getCategSubName(split.getCategId(), split.getSubCategId());
        builder.append("S");
        builder.append(category);
        builder.append(System.lineSeparator());

        // amount
        double amount = split.getSplitTransAmount();
        // handle sign
        if (TransactionTypes.valueOf(transactionType).equals(TransactionTypes.Withdrawal)) {
            amount = amount * (-1);
        }
        if (TransactionTypes.valueOf(transactionType).equals(TransactionTypes.Deposit)) {
            // leave positive?
        }
        builder.append("$");
        builder.append(amount);
        builder.append(System.lineSeparator());

        // memo - currently we don't have a field for it.
//        String memo = split.get

        return builder.toString();
    }

    private int getTransactionId(Cursor cursor){
        return cursor.getInt(cursor.getColumnIndex(QueryAllData.ID));
    }

    private String parseCleared(Cursor cursor) {
        // todo: handle other statuses?
        // Cleared: * or c
        // Reconciled: X or R
        return cursor.getString(cursor.getColumnIndex(QueryAllData.Status));
    }

    private String parseDate(Cursor cursor) throws ParseException {
        String dateValue = cursor.getString(cursor.getColumnIndex(QueryAllData.Date));
        SimpleDateFormat savedFormat = new SimpleDateFormat(Constants.PATTERN_DB_DATE, Locale.US);
        java.util.Date date = savedFormat.parse(dateValue);
        // todo: get Quicken date format from settings.
        SimpleDateFormat qifFormat = new SimpleDateFormat("MM/dd''yy", Locale.US);
        return qifFormat.format(date);
    }

    private String parseAmount(Cursor cursor) {
        Double amountDouble = cursor.getDouble(cursor.getColumnIndex(QueryAllData.Amount));

        String amount = Double.toString(amountDouble);
        return amount;
    }

    private String parseCategory(Cursor cursor) {
        String category = cursor.getString(cursor.getColumnIndex(QueryAllData.Category));
        String subCategory = cursor.getString(cursor.getColumnIndex(QueryAllData.Subcategory));

        if (!TextUtils.isEmpty(subCategory)) {
            return category + ":" + subCategory;
        } else {
            return category;
        }
    }

    private String parseMemo(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(QueryAllData.Notes));
    }

    private String getTransactionType(Cursor cursor) {
        String type = cursor.getString(cursor.getColumnIndex(QueryAllData.TransactionType));
        return type;
    }
}
