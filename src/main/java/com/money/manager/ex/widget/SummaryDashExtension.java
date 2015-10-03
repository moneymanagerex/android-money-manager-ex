/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;

import info.javaperformance.money.MoneyFactory;

public class SummaryDashExtension extends DashClockExtension {

    @Override
    protected void onUpdateData(int arg0) {
        try {
            Context context = getApplicationContext();
            MoneyManagerApplication app = new MoneyManagerApplication();
            CurrencyService currencyService = new CurrencyService(context);

            // summary formatted
            String summary = currencyService.getBaseCurrencyFormatted(
                    MoneyFactory.fromDouble(app.getSummaryAccounts(context)));

            publishUpdate(new ExtensionData()
                    .visible(true)
                    .icon(R.drawable.ic_stat_notification)
                    .status(summary)
                    .expandedTitle(context.getString(R.string.summary) + ": " + summary)
                    .expandedBody(app.getUserName())
                    .clickIntent(new Intent(this, MainActivity.class)));
        } catch (Exception e) {
            Log.e(SummaryDashExtension.class.getSimpleName(), e.getMessage());
        }
    }
}
