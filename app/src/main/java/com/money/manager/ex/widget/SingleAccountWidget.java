/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;

import info.javaperformance.money.Money;
import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SingleAccountWidgetConfigureActivity SingleAccountWidgetConfigureActivity}
 */
public class SingleAccountWidget
    extends AppWidgetProvider {

    // Static

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    // Dynamic

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

    @Override
    public void onAppWidgetOptionsChanged (Context context,
                                      AppWidgetManager appWidgetManager,
                                      int appWidgetId, Bundle newOptions) {
        // Here you can update your widget view
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
//        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
//        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        Timber.d("resized");

        // Obtain appropriate widget and update it.
        appWidgetManager.updateAppWidget(appWidgetId, getRemoteViews(context, minWidth, minHeight));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        this.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    private RemoteViews mRemoteViews;

    private RemoteViews getRemoteViews(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (mRemoteViews == null) {
            // this call is available only on API 16!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                int width, height;
                Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

                width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
                height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

                //AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
//                width = info.minResizeWidth;
//                height = info.minResizeHeight;
//                width = info.minWidth;
//                height = info.minHeight;

                mRemoteViews = getRemoteViews(context, width, height);
            } else {
                mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_single_account);
            }
        }
        return mRemoteViews;
    }

    /**
     * Determine appropriate view based on width provided.
     *
     * @param width current width
     * @param height current height
     * @return Remote views for the current widget.
     */
    private RemoteViews getRemoteViews(Context context, int width, int height) {
        // First find out rows and columns based on width provided.
        //int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(width);

        if (columns <= 2) {
            // Get 1 column widget remote view and return
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_single_account_1x1);
        } else {
            // Get appropriate remote view.
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_single_account);
        }
        return mRemoteViews;
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = getRemoteViews(context, appWidgetManager, appWidgetId);

        // todo: allow selecting the account from a list.

        // todo: load the configured account id
        AppSettings settings = new AppSettings(context);
        Integer defaultAccountId = settings.getGeneralSettings().getDefaultAccountId();
//        if (StringUtils.isNotEmpty(defaultAccountId)) {
        if (defaultAccountId != null) {
            String defaultAccountString = Integer.toString(defaultAccountId);
            displayAccountInfo(context, defaultAccountString, views);
        }

        // e + click -> open the new transaction screen for this account.
        // todo: pass the account id?
        initializeNewTransactionCommand(context, views);

        // e logo click -> open the app.
        initializeStartAppCommand(context, views);

        // click account name -> refresh the balance.
        initializeRefreshDataCommand(context, views, appWidgetId);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void displayAccountInfo(Context context, String defaultAccountId, RemoteViews views) {
        int accountId = Integer.parseInt(defaultAccountId);
        Account account = loadAccount(context, accountId);
        if (account == null) return;

//        CharSequence widgetText = SingleAccountWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // display the account name
//        String accountName = getAccountName(context, accountId);
        String accountName = account.getName();
        views.setTextViewText(R.id.accountNameTextView, accountName);

        // get account balance (for this account?)
        String balance = getFormattedAccountBalance(context, account);
        views.setTextViewText(R.id.balanceTextView, balance);
    }

    private void initializeNewTransactionCommand(Context context, RemoteViews views) {
        Intent intent = new Intent(context, CheckingTransactionEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "SingelAccountWidget.java");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.newTransactionPanel, pendingIntent);
        // for now, the button requires a separate setup. try to find a way to propagate click.
        views.setOnClickPendingIntent(R.id.newTransactionButton, pendingIntent);
    }

    private String getFormattedAccountBalance(Context context, Account account) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(QueryAccountBills.ACCOUNTID, "=", account.getId());
        String selection =  where.getWhere();

        AccountService service = new AccountService(context);
        Money total = service.loadBalance(selection);

        // format the amount
        CurrencyService currencyService = new CurrencyService(context);
        String summary = currencyService.getCurrencyFormatted(
                account.getCurrencyId(), total);

        return summary;
    }

    private Account loadAccount(Context context, int accountId) {
        AccountRepository repository = new AccountRepository(context);
        return repository.load(accountId);
    }

    private void initializeStartAppCommand(Context context, RemoteViews views) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Get the layout for the App Widget and attach an on-click listener to the button
//        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.appwidget_provider_layout);

        views.setOnClickPendingIntent(R.id.appLogoImage, pendingIntent);
    }

    private void initializeRefreshDataCommand(Context context, RemoteViews views, int appWidgetId) {
        // refresh the balance on tap.
        Intent intent = new Intent(context, SingleAccountWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.refreshDataPanel, pendingIntent);
    }
}

