/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.currency.CurrencyService;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class AccountBillsDashExtension extends DashClockExtension {

    @Override
    protected void onUpdateData(int arg0) {
        try {
            Context context = getApplicationContext();
            MoneyManagerApplication app = new MoneyManagerApplication();
            CurrencyService currencyService = new CurrencyService(context);

            QueryAccountBills accountBills = new QueryAccountBills(context);
            String selection = accountBills.getFilterAccountSelection();
            // create a cursor
            Cursor cursor = context.getContentResolver().query(accountBills.getUri(), null, selection, null, QueryAccountBills.ACCOUNTNAME);
            // body extensions
            String body = "";

            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String accountname = cursor.getString(cursor.getColumnIndex(QueryAccountBills.ACCOUNTNAME));
                    int currencyId = cursor.getInt(cursor.getColumnIndex(QueryAccountBills.CURRENCYID));
                    double summaryAccount = cursor.getDouble(cursor.getColumnIndex(QueryAccountBills.TOTAL));
                    String value = currencyService.getCurrencyFormatted(
                            currencyId, MoneyFactory.fromDouble(summaryAccount));
                    if (!TextUtils.isEmpty(body)) body += "\r\n";
                    // add account and summary
                    body += accountname + ": " + value;
                    // move to next row
                    cursor.moveToNext();
                }

                cursor.close();
            }

            // show data
            publishUpdate(new ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_stat_notification)
                .status(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(app.getSummaryAccounts(context))))
                .expandedTitle(app.getUserName())
                .expandedBody(body)
                .clickIntent(new Intent(this, MainActivity.class)));
        } catch (Exception e) {
            Timber.e(e, "updating accounts/bills widget");
        }
    }
}
