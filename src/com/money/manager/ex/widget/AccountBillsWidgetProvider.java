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
package com.money.manager.ex.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyUtils;

public class AccountBillsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        MoneyManagerApplication app = new MoneyManagerApplication();
        CurrencyUtils currencyUtils = new CurrencyUtils(context);

        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_account_bills);
            remoteViews.setTextViewText(R.id.textViewUserName, app.loadUserNameFromDatabase(context));
            remoteViews.setTextViewText(R.id.textViewTotalAccounts, context.getString(R.string.summary) + ": "
                    + currencyUtils.getBaseCurrencyFormatted(app.getSummaryAccounts(context)));
            // register on click in icon launch application
            Intent intentApplication = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentApplication, 0);
            remoteViews.setOnClickPendingIntent(R.id.imageButtonLogoWidget, pendingIntent);

            Intent intentRefresh = new Intent(context, AccountBillsWidgetProvider.class);
            intentRefresh.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingRefresh = PendingIntent.getBroadcast(context, 0, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.imageButtonRefresh, pendingRefresh);

            //service
            Intent svcIntent = new Intent(context, AccountBillsWidgetService.class);

            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            remoteViews.setRemoteAdapter(R.id.listViewAccountBills, svcIntent);

            // update widget
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.listViewAccountBills);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
