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
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.WhereClauseGenerator;
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
        TableAccountList account = loadAccount(context, accountId);

//        CharSequence widgetText = SingleAccountWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.single_account_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // display the account name
//        String accountName = getAccountName(context, accountId);
        String accountName = account.getAccountName();
        views.setTextViewText(R.id.accountNameTextView, accountName);

        // get account balance (for this account?)
        String balance = getFormattedAccountBalance(context, account);
        views.setTextViewText(R.id.balanceTextView, balance);

        // handle + click -> open the new transaction screen for this account.
        // todo: pass the account id?
        initializeNewTransactionButton(context, views);

        // todo: handle logo click -> open the app.

        // todo: click account name -> refresh the balance.

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void initializeNewTransactionButton(Context context, RemoteViews views) {
        Intent intent = new Intent(context, EditTransactionActivity.class);
        intent.setAction(Constants.INTENT_ACTION_INSERT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.newTransactionButton, pendingIntent);
    }

    static String getFormattedAccountBalance(Context context, TableAccountList account) {
        WhereClauseGenerator generator = new WhereClauseGenerator(context);
        generator.addSelection(QueryAccountBills.ACCOUNTID, "=", account.getAccountId());
        String selection =  generator.getSelectionStatements();
        String[] args = generator.getSelectionArguments();

        AccountService service = new AccountService(context);
        double total = service.loadBalance(selection, args);

        // format the amount
        CurrencyService currencyService = new CurrencyService(context);
        String summary = currencyService.getCurrencyFormatted(account.getCurrencyId(), total);

        return summary;
    }

    static String getAccountName(Context context, int accountId) {
        AccountRepository repository = new AccountRepository(context);
        String name = repository.loadName(accountId);
        if (StringUtils.isEmpty(name)) {
            name = "n/a";
        }
        return name;
    }

    static TableAccountList loadAccount(Context context, int accountId) {
        AccountRepository repository = new AccountRepository(context);
        return repository.load(accountId);
    }
}

