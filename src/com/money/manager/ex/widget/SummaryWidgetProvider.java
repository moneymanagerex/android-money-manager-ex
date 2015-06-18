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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.utils.CurrencyUtils;

public class SummaryWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        MoneyManagerApplication app = new MoneyManagerApplication();
        CurrencyUtils currencyUtils = new CurrencyUtils(context);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, SummaryWidgetProvider.class));

        for (int i = 0; i < allWidgetIds.length; ++i) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_summary);
            remoteViews.setTextViewText(R.id.textViewUserName, app.loadUserNameFromDatabase(context));
            remoteViews.setTextViewText(R.id.textViewTotalAccounts, context.getString(R.string.summary) + ": "
                    + currencyUtils.getBaseCurrencyFormatted(app.getSummaryAccounts(context)));
            // register on click in icon launch application
            Intent intentApplication = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentApplication, 0);
            remoteViews.setOnClickPendingIntent(R.id.imageButtonLogoWidget, pendingIntent);

            Intent intentRefresh = new Intent(context, SummaryWidgetProvider.class);
            intentRefresh.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingRefresh = PendingIntent.getBroadcast(context, 0, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.imageButtonRefresh, pendingRefresh);

            // update widget
            try {
                appWidgetManager.updateAppWidget(allWidgetIds[i], remoteViews);
            } catch (Exception e) {
                Log.e(SummaryWidgetProvider.class.getSimpleName(), e.getMessage());
            }
        }
    }
}
