package com.money.manager.ex.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.transactions.EditTransactionActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SingleAccountWidgetConfigureActivity SingleAccountWidgetConfigureActivity}
 */
public class SingleAccountWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            SingleAccountWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // todo: allow selecting the account from a list.
        // todo: load the configured account id
//        CharSequence widgetText = SingleAccountWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.single_account_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // todo: display the account name
        views.setTextViewText(R.id.accountNameTextView, "Account Name");

        // todo: get account balance
        String balance = getFormattedAccountBalance(context, 1);
        views.setTextViewText(R.id.balanceTextView, balance);

        // handle + click -> open the new transaction screen for this account.
        // todo: pass the account id?
        initializeNewTransactionButton(context, views);

        // todo: handle logo click -> open the app

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void initializeNewTransactionButton(Context context, RemoteViews views) {
        Intent intent = new Intent(context, EditTransactionActivity.class);
        intent.setAction(Constants.INTENT_ACTION_INSERT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.newTransactionButton, pendingIntent);
    }

    static String getFormattedAccountBalance(Context context, int accountId) {
        String selection = QueryAccountBills.ACCOUNTID + "=?";
        String[] args = new String[] { Integer.toString(accountId) };

        QueryAccountBills accountBills = new QueryAccountBills(context);
        Cursor cursor = context.getContentResolver().query(accountBills.getUri(),
                null,
                selection, args,
                null);
        if (cursor == null) return "";

        double total = 0;
        // calculate summary
        while (cursor.moveToNext()) {
            total = total + cursor.getDouble(cursor.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
        }
        cursor.close();

        // format the amount
        CurrencyService currencyService = new CurrencyService(context);
        String summary = currencyService.getBaseCurrencyFormatted(total);

        return summary;
    }
}

