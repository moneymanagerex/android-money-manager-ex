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
package com.money.manager.ex.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;

public class ButtonAddTransactionWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		for (int i = 0; i < appWidgetIds.length; ++i) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_button_add_transaction);
			// register on click in icon launch application
			Intent intent = new Intent(context, CheckingTransactionEditActivity.class);
			intent.setAction(Intent.ACTION_INSERT);
			intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "ButtonAddTransactionWidgetProvoder.java");
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonNewOperation, pendingIntent);
		    
			// update widget
			appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
