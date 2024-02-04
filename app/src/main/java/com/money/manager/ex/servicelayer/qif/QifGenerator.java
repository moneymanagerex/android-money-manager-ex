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
import android.database.Cursor;

import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.viewmodels.AccountTransactionDisplay;

import java.text.ParseException;

/**
 * Generator of Qif file contents.
 * Parses the transactions and creates qif output.
 * References:
 * http://www.respmech.com/mym2qifw/qif_new.htm
 */
public class QifGenerator implements IQifGenerator {
    private final Context mContext;

    public QifGenerator(final Context context) {
        mContext = context;
    }

    public String createFromAdapter(final AllDataAdapter adapter)
            throws ParseException {
        final StringBuilder builder = new StringBuilder();

        final Cursor cursor = adapter.getCursor();
        final int originalCursorPosition = cursor.getPosition();
        cursor.moveToFirst();

        int previousAccountId = 0;
        final QifHeader header = new QifHeader(mContext);
        final QifRecord record = new QifRecord(mContext);
        final AccountTransactionDisplay transaction = new AccountTransactionDisplay();

        while (!cursor.isAfterLast()) {
            // get data from cursor.
            transaction.loadFromCursor(cursor);

            final int accountId;
            if (TransactionTypes.Transfer == transaction.getTransactionType()) {
                accountId = transaction.getToAccountId();
            } else {
                accountId = transaction.getAccountId();
            }
            if (accountId != previousAccountId) {
                previousAccountId = accountId;
                // add header record
                final String headerRecord = header.parse(cursor);
                builder.append(headerRecord);
            }

            // add transaction record
            final String row = record.parse(transaction);

            builder.append(row);
            cursor.moveToNext();
        }
        // No need to close the cursor here because it is used in the parent fragment.
//        cursor.close();
        cursor.moveToPosition(originalCursorPosition);

        return builder.toString();
    }

}
