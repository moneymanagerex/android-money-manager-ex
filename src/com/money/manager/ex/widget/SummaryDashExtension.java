package com.money.manager.ex.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;

public class SummaryDashExtension extends DashClockExtension {

	@Override
	protected void onUpdateData(int arg0) {
		try {
			Context context = getApplicationContext();
			MoneyManagerApplication app = new MoneyManagerApplication();

			// summary formatted
			String summary = app.getBaseCurrencyFormatted(app.getSummaryAccounts(context));
		
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
