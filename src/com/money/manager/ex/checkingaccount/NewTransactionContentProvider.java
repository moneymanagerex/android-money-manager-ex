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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.businessobjects.Payee;

/**
 * Content provider for creation of new transaction.
 * References:
 * http://tasker.dinglisch.net/userguide/en/intents.html
 * https://groups.google.com/forum/#!topic/anmoney-users/1bCicdk12Uo
 */
public class NewTransactionContentProvider
        extends ContentProvider {
    /*
    content://com.zvasvari.anmoneyp.AnMoney/books/<BookName>/transactions?account=<AccountName>
    &transfer_account=<TransferAccountName>&number=<TransactionNumber>&amount=<Amount>
    &payee=<PayeeName>&category=<CategoryName>&class1=<Class1Name>&class2=<Class2Name>
    &notes=<NotesName>&status=<Status>
     */

    public static String mLogcat;

    // Keys for extra parameters in the Intent.
    public static final String PARAM_ACCOUNT = "account";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_PAYEE = "payee";

    public static final String Authority = "com.money.manager.ex.NewTransaction";

    @Override
    public boolean onCreate() {
        this.mLogcat = this.getClass().getSimpleName();

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
//        return null;

        StringBuilder builder = new StringBuilder();
        builder.append(ContentResolver.CURSOR_ITEM_BASE_TYPE);
        builder.append("/");
        // authority
        builder.append("vnd.");
        builder.append(Authority);
        // item
        builder.append(".");
        builder.append("transaction");

        this.openForm(uri);

        return builder.toString();
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.d(mLogcat, uri.toString());
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    /**
     * Start the new transaction activity.
     * @param data
     */
    private void openForm(Uri data) {
        Context context = getContext();

        Intent intent = new Intent(context, CheckingAccountActivity.class);
        intent.setAction(Intent.ACTION_INSERT);

        Bundle parameters = new Bundle();

        String account = data.getQueryParameter(PARAM_ACCOUNT);
        parameters.putString(PARAM_ACCOUNT, account);

        // payee

        String payeeName = data.getQueryParameter(PARAM_PAYEE);
        Payee payee = new Payee(context);
        int payeeId = payee.loadIdByName(payeeName);
        parameters.putInt(PARAM_PAYEE, payeeId);

        String amount = data.getQueryParameter(PARAM_AMOUNT);
        parameters.putString(PARAM_AMOUNT, amount);

        context.startActivity(intent);
    }
}
