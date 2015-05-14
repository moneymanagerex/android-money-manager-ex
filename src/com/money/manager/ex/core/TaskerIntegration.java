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
package com.money.manager.ex.core;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.money.manager.ex.businessobjects.Payee;

/**
 * Contains code for parsing the Intent parameters for New Transaction (Tasker integration).
 */
public class TaskerIntegration {
    /*
    content://com.zvasvari.anmoneyp.AnMoney/books/<BookName>/transactions?account=<AccountName>
    &transfer_account=<TransferAccountName>&number=<TransactionNumber>&amount=<Amount>
    &payee=<PayeeName>&category=<CategoryName>&class1=<Class1Name>&class2=<Class2Name>
    &notes=<NotesName>&status=<Status>
     */

    public TaskerIntegration(Context context) {
        mContext = context;
        this.mLogcat = this.getClass().getSimpleName();
    }

    private Context mContext;

    // Keys for extra parameters in the Intent.
    public static final String PARAM_ACCOUNT = "account";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_PAYEE = "payee";

    public int PayeeId = -1;
    public double TotalAmount = 0;

    private String mLogcat;

    public void parseIntent(Intent intent) {
        // Get any parameters, if sent, when intent was raised. This is used when called
        // from Tasker or any external caller.

        // account
        //this.mAccountId =

        //this.mAmount = intent.getDoubleExtra(PARAM_AMOUNT, 0);
        this.TotalAmount = intent.getDoubleExtra(PARAM_AMOUNT, 0);

        // payee
        String payeeName = intent.getStringExtra(PARAM_PAYEE);
        Payee payee = new Payee(mContext);
        this.PayeeId = payee.loadIdByName(payeeName);

        Uri data = intent.getData();
        Log.d(mLogcat, data.toString());

    }
}
