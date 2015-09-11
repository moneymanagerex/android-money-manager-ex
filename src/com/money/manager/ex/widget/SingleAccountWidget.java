package com.money.manager.ex.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.transactions.EditTransactionActivity;

import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SingleAccountWidgetConfigureActivity SingleAccountWidgetConfigureActivity}
 */
public class SingleAccountWidget
        extends AppWidgetProvider {

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
        AppSettings settings = new AppSettings(context);
        String defaultAccountId = settings.getGeneralSettings().getDefaultAccountId();
        if (StringUtils.isEmpty(defaultAccountId)) return;

        int accountId = Integer.parseInt(defaultAccountId);
        Account account = loadAccount(context, accountId);

//        CharSequence widgetText = SingleAccountWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.single_account_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // display the account name
//        String accountName = getAccountName(context, accountId);
        String accountName = account.getName();
        views.setTextViewText(R.id.accountNameTextView, accountName);

        // get account balance (for this account?)
        String balance = getFormattedAccountBalance(context, account);
        views.setTextViewText(R.id.balanceTextView, balance);

        // handle + click -> open the new transaction screen for this account.
        // todo: pass the account id?
        initializeNewTransactionButton(context, views);

        // handle logo click -> open the app.
        initializeAppButton(context, views);

        // click account name -> refresh the balance.
        initializeContentPanel(context, views, appWidgetId);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void initializeNewTransactionButton(Context context, RemoteViews views) {
        Intent intent = new Intent(context, EditTransactionActivity.class);
        intent.setAction(Constants.INTENT_ACTION_INSERT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.newTransactionButton, pendingIntent);
    }

    static String getFormattedAccountBalance(Context context, Account account) {
//        WhereClauseGenerator generator = new WhereClauseGenerator(context);
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(QueryAccountBills.ACCOUNTID, "=", account.getId());
        String selection =  where.getWhere();
//        String[] args = generator.getSelectionArguments();

        AccountService service = new AccountService(context);
        double total = service.loadBalance(selection);

        // format the amount
        CurrencyService currencyService = new CurrencyService(context);
        String summary = currencyService.getCurrencyFormatted(account.getCurrencyId(), total);

        return summary;
    }

//    static String getAccountName(Context context, int accountId) {
//        AccountRepository repository = new AccountRepository(context);
//        String name = repository.loadName(accountId);
//        if (StringUtils.isEmpty(name)) {
//            name = "n/a";
//        }
//        return name;
//    }

    static Account loadAccount(Context context, int accountId) {
        AccountRepository repository = new AccountRepository(context);
        return repository.load(accountId);
    }

    static void initializeAppButton(Context context, RemoteViews views) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Get the layout for the App Widget and attach an on-click listener to the button
//        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.appwidget_provider_layout);
        views.setOnClickPendingIntent(R.id.appLogoImage, pendingIntent);
    }

    static void initializeContentPanel(Context context, RemoteViews views, int appWidgetId) {
        // refresh the balance on tap.
        Intent intent = new Intent(context, SingleAccountWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.contentPanel, pendingIntent);
    }
}

