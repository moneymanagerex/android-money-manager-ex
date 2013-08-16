package com.money.manager.ex.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryAccountBills;

public class AccountBillsDashExtension extends DashClockExtension {

	@Override
	protected void onUpdateData(int arg0) {
		try {
			Context context = getApplicationContext();
			MoneyManagerApplication app = new MoneyManagerApplication();
			// load base currency
			app.loadBaseCurrencyId(context);
			app.loadHashMapCurrency(context);
			
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
					float summaryAccount = cursor.getFloat(cursor.getColumnIndex(QueryAccountBills.TOTAL));
					String value = app.getCurrencyFormatted(currencyId, summaryAccount);
					if (!TextUtils.isEmpty(body)) body += "\r\n";
					// add account and summary
					body += accountname + ": " + value;
					// move to next row
					cursor.moveToNext();
				}
			}
			// close data
			cursor.close();
			// show data
			publishUpdate(new ExtensionData()
						  .visible(true)
						  .icon(R.drawable.ic_stat_notification)
						  .status(app.getBaseCurrencyFormatted(app.getSummaryAccounts(context)))
						  .expandedTitle(app.getUserName())
						  .expandedBody(body)
						  .clickIntent(new Intent(this, MainActivity.class)));
		} catch (Exception e) {
			Log.e(AccountBillsDashExtension.class.getSimpleName(), e.getMessage());
		}
	}
}
