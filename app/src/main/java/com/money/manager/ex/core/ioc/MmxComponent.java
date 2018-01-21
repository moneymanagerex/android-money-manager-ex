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

package com.money.manager.ex.core.ioc;

import com.money.manager.ex.MmxContentProvider;
import com.money.manager.ex.budget.BudgetAdapter;
import com.money.manager.ex.common.CalculatorActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.currency.list.CurrencyListFragment;
import com.money.manager.ex.datalayer.StockHistoryRepositorySql;
import com.money.manager.ex.datalayer.StockRepositorySql;
import com.money.manager.ex.home.HomeFragment;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.home.SelectDatabaseActivity;
import com.money.manager.ex.investment.EditPriceDialog;
import com.money.manager.ex.investment.prices.ISecurityPriceUpdater;
import com.money.manager.ex.investment.InvestmentTransactionEditActivity;
import com.money.manager.ex.investment.PriceEditActivity;
import com.money.manager.ex.investment.PriceEditModel;
import com.money.manager.ex.investment.morningstar.MorningstarPriceUpdater;
import com.money.manager.ex.recurring.transactions.RecurringTransactionEditActivity;
import com.money.manager.ex.recurring.transactions.RecurringTransactionListFragment;
import com.money.manager.ex.reports.BaseReportFragment;
import com.money.manager.ex.search.SearchParametersFragment;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.DatabaseSettingsFragment;
import com.money.manager.ex.settings.SettingsActivity;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.sync.SyncPreferenceFragment;
import com.money.manager.ex.sync.SyncService;
import com.money.manager.ex.sync.SyncServiceMessageHandler;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Main Dagger 2 Component. Represents the link between the Modules and Injections.
 * Component consumes functionality.
 */
@Singleton
@Component(modules = MmxModule.class)
public interface MmxComponent {
    // Example on how to expose a provision method.
//    MmxOpenHelper getHelper();

    // Activities
    void inject(CalculatorActivity activity);
    void inject(CheckingTransactionEditActivity activity);
    void inject(InvestmentTransactionEditActivity activity);
    void inject(MainActivity activity);
    void inject(PriceEditActivity activity);
    void inject(RecurringTransactionEditActivity activity);
    void inject(SelectDatabaseActivity activity);
    void inject(SettingsActivity activity);

    // Fragments
    void inject(BaseReportFragment fragment);
    void inject(CurrencyListFragment fragment);
    void inject(DatabaseSettingsFragment fragment);
    void inject(HomeFragment fragment);
    void inject(RecurringTransactionListFragment fragment);
    void inject(SearchParametersFragment fragment);
    void inject(SyncPreferenceFragment fragment);

    // Dialogs
    void inject(EditPriceDialog dialog);

    // Models
    void inject(PriceEditModel model);

    // Custom objects
    void inject(ISecurityPriceUpdater updater);
    void inject(MorningstarPriceUpdater updater);
    void inject(AppSettings settings);
    void inject(Core core);
    void inject(MmxContentProvider provider);
    void inject(MmxDatabaseUtils utils);
    void inject(SyncManager sync);
    void inject(SyncServiceMessageHandler handler);
    void inject(Passcode object);
    void inject(EditTransactionCommonFunctions object);

    // Helpers
    void inject(UIHelper helper);
    void inject(FormatUtilities utils);

    // Business Services
    void inject(CurrencyService service);
    void inject(InfoService service);

    // Intent Services
    void inject(SyncService service);

    // Repositories
    void inject(StockRepositorySql repository);
    void inject(StockHistoryRepositorySql repository);

    // Adapters
    void inject(BudgetAdapter adapter);
}
