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
import android.widget.RemoteViews;

import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;

public class ButtonAddTransactionWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		for (int i = 0; i < appWidgetIds.length; ++i) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_button_add_transaction);
			// register on click in icon launch application
			Intent intent = new Intent(context, CheckingAccountActivity.class);
			intent.setAction(Constants.INTENT_ACTION_INSERT);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonNewOperation, pendingIntent);
		    
			// update widget
			appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
