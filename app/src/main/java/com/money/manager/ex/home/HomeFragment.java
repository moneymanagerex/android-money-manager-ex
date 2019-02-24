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
package com.money.manager.ex.home;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;
import com.melnykov.fab.FloatingActionButton;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountEditActivity;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.DatabaseMigrator14To20;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.InfoRepositorySql;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.home.events.AccountsTotalLoadedEvent;
import com.money.manager.ex.home.events.RequestAccountFragmentEvent;
import com.money.manager.ex.home.events.RequestPortfolioFragmentEvent;
import com.money.manager.ex.home.events.RequestWatchlistFragmentEvent;
import com.money.manager.ex.home.events.UsernameLoadedEvent;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.settings.SettingsActivity;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.transactions.IntentDataParameters;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.view.RobotoTextView;
import com.money.manager.ex.viewmodels.IncomeVsExpenseReportEntity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import dagger.Lazy;
import icepick.Icepick;
import icepick.State;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * The starting fragment that contains the accounts groups with accounts and their balances.
 */
public class HomeFragment
    extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

//    private static final int LOADER_USER_NAME = 1;
    private static final int LOADER_ACCOUNT_BILLS = 2;
    private static final int LOADER_INCOME_EXPENSES = 4;

    private static final String TAG_BALANCE_ACCOUNT = "HomeFragment:BalanceAccount";
    private static final int REQUEST_BALANCE_ACCOUNT = 1;

    @Inject Lazy<InfoRepositorySql> infoRepositorySqlLazy;

    private CurrencyService mCurrencyService;
    private boolean mHideReconciled;

    // This is the collapsible list of account groups with accounts.
    private ExpandableListView mExpandableListView;
    private ViewGroup linearHome, linearFooter, linearWelcome;
    private TextView txtTotalAccounts, txtFooterSummary, txtFooterSummaryReconciled;
    private ProgressBar prgAccountBills;
    private FloatingActionButton mFloatingActionButton;

    /**
     * List of account types in lowercase (i.e. checking, investment, ...)
     */
    private List<String> mAccountTypes = new ArrayList<>();
    private HashMap<String, List<QueryAccountBills>> mAccountsByType = new HashMap<>();
    private HashMap<String, QueryAccountBills> mTotalsByType = new HashMap<>();

    private Money mGrandTotal = MoneyFactory.fromDouble(0);
    private Money mGrandReconciled = MoneyFactory.fromDouble(0);

    @State int accountBalancedId = Constants.NOT_SET;
    private QueryAccountBills accountBeingBalanced = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MmexApplication.getApp().iocComponent.inject(this);

        mCurrencyService = new CurrencyService(getActivity().getApplicationContext());

        refreshSettings();

        // The fragment is using a custom option in the actionbar menu.
        setHasOptionsMenu(true);

        // restore number input binaryDialog reference, if any
        if (savedInstanceState != null) {
//            this.accountBalancedId = savedInstanceState.getInt(TAG_BALANCE_ACCOUNT);
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) return null;

        // inflate layout
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        linearHome = (FrameLayout) view.findViewById(R.id.linearLayoutHome);
        txtTotalAccounts = (TextView) view.findViewById(R.id.textViewTotalAccounts);

        createWelcomeView(view);

        setUpAccountsList(view);

        prgAccountBills = (ProgressBar) view.findViewById(R.id.progressAccountBills);

        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CheckingTransactionEditActivity.class);
                intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "HomeFragment.java");
                intent.setAction(Intent.ACTION_INSERT);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFloatingActionButton.attachToListView(mExpandableListView);
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    // Loader event handlers

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        MmxCursorLoader result;
        Select query;

        switch (id) {
            case LOADER_ACCOUNT_BILLS:
                setListViewAccountBillsVisible(false);

                LookAndFeelSettings settings = new AppSettings(getContext()).getLookAndFeelSettings();
                // compose whereClause
                String where = "";
                // check if show only open accounts
                if (settings.getViewOpenAccounts()) {
                    where = "LOWER(" + QueryAccountBills.STATUS + ")='open'";
                }
                // check if show fav accounts
                if (settings.getViewFavouriteAccounts()) {
                    where = "LOWER(" + QueryAccountBills.FAVORITEACCT + ")='true'";
                }

                QueryAccountBills queryAccountBills = new QueryAccountBills(getActivity());
                query = new Select(queryAccountBills.getAllColumns())
                    .where(where)
                    .orderBy(QueryAccountBills.ACCOUNTTYPE + ", upper(" + QueryAccountBills.ACCOUNTNAME + ")");

                result = new MmxCursorLoader(getActivity(), queryAccountBills.getUri(), query);
                break;

            case LOADER_INCOME_EXPENSES:
                // todo: Get custom period. pref_income_expense_footer_period
//                String period = new AppSettings(getContext()).getBehaviourSettings().getIncomeExpensePeriod();
//                String transactionsFilter = generator.getWhereClauseForPeriod(period);
//                report.filterTransactionsSource(transactionsFilter);

                String whereStatement =
                    IncomeVsExpenseReportEntity.Month + "="
                    + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                        " AND " +
                        IncomeVsExpenseReportEntity.YEAR + "=" +
                            Integer.toString(Calendar.getInstance().get(Calendar.YEAR));

                QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity());
                query = new Select(report.getAllColumns())
                        .where(whereStatement);
                result = new MmxCursorLoader(getActivity(), report.getUri(), query);
                break;

            default:
                result = null;
        }
        return  result;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ACCOUNT_BILLS:
                txtTotalAccounts.setText(mCurrencyService.getBaseCurrencyFormatted(MoneyFactory.fromString("0")));
                setListViewAccountBillsVisible(false);
                mAccountsByType.clear();
                mTotalsByType.clear();
                mAccountTypes.clear();
                break;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ACCOUNT_BILLS:
                try {
                    renderAccountsList(data);
                } catch (Exception e) {
                    Timber.e(e, "rendering account list");
                }

                // set total for accounts in the main Drawer.
                EventBus.getDefault().post(new AccountsTotalLoadedEvent(txtTotalAccounts.getText().toString()));
                break;

            case LOADER_INCOME_EXPENSES:
                double income = 0, expenses = 0;
                if (data != null) {
                    while (data.moveToNext()) {
                        expenses = data.getDouble(data.getColumnIndex(IncomeVsExpenseReportEntity.Expenses));
                        income = data.getDouble(data.getColumnIndex(IncomeVsExpenseReportEntity.Income));
                    }
                }
                TextView txtIncome = (TextView) getActivity().findViewById(R.id.textViewIncome);
                TextView txtExpenses = (TextView) getActivity().findViewById(R.id.textViewExpenses);
                TextView txtDifference = (TextView) getActivity().findViewById(R.id.textViewDifference);
                // set value
                if (txtIncome != null)
                    txtIncome.setText(mCurrencyService.getCurrencyFormatted(mCurrencyService.getBaseCurrencyId(),
                            MoneyFactory.fromDouble(income)));
                if (txtExpenses != null)
                    txtExpenses.setText(mCurrencyService.getCurrencyFormatted(mCurrencyService.getBaseCurrencyId(),
                            MoneyFactory.fromDouble(Math.abs(expenses))));
                if (txtDifference != null)
                    txtDifference.setText(mCurrencyService.getCurrencyFormatted(mCurrencyService.getBaseCurrencyId(),
                            MoneyFactory.fromDouble(income - Math.abs(expenses))));
                // manage progressbar
                final ProgressBar barIncome = (ProgressBar) getActivity().findViewById(R.id.progressBarIncome);
                final ProgressBar barExpenses = (ProgressBar) getActivity().findViewById(R.id.progressBarExpenses);

                if (barIncome != null && barExpenses != null) {
                    barIncome.setMax((int) (Math.abs(income) + Math.abs(expenses)));
                    barExpenses.setMax((int) (Math.abs(income) + Math.abs(expenses)));

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        ObjectAnimator animationIncome = ObjectAnimator.ofInt(barIncome, "progress", (int) Math.abs(income));
                        animationIncome.setDuration(1000); // 0.5 second
                        animationIncome.setInterpolator(new DecelerateInterpolator());
                        animationIncome.start();

                        ObjectAnimator animationExpenses = ObjectAnimator.ofInt(barExpenses, "progress", (int) Math.abs(expenses));
                        animationExpenses.setDuration(1000); // 0.5 second
                        animationExpenses.setInterpolator(new DecelerateInterpolator());
                        animationExpenses.start();
                    } else {
                        barIncome.setProgress((int) Math.abs(income));
                        barExpenses.setProgress((int) Math.abs(expenses));
                    }
                }
                break;
        }

        // Close the cursor.
        MmxDatabaseUtils.closeCursor(data);
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        switch (item.getItemId()) {
            case R.id.menu_search:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                result = true;
                break;
        }

        if (result) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // End menu.

    @Override
    public void onResume() {
        super.onResume();

        refreshSettings();

        // Toolbar
        FragmentActivity parent = getActivity();
        if (parent instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();

            // show title
            activity.getSupportActionBar().setDisplayShowTitleEnabled(true);

            // Show db name in toolbar.
            String dbPath = new AppSettings(activity).getDatabaseSettings().getDatabasePath();
            //String dbFileName = FilenameUtils.getBaseName(dbPath);
            String dbFileName = Files.getNameWithoutExtension(dbPath);
            activity.getSupportActionBar().setSubtitle(dbFileName);
        }

        // reload data.
        startLoaders();
    }

    // Context menu

    /**
     * Context menu for account entries.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (!(v instanceof ExpandableListView)) return;

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        // ignore long-press on group items.
        if (type != ExpandableListView.PACKED_POSITION_TYPE_CHILD) return;

        // get adapter.
        HomeAccountsExpandableAdapter accountsAdapter = (HomeAccountsExpandableAdapter) mExpandableListView.getExpandableListAdapter();
        Object childItem = accountsAdapter.getChild(groupPosition, childPosition);
        QueryAccountBills account = (QueryAccountBills) childItem;

//        menu.setHeaderIcon(android.R.drawable.ic_menu_manage);
        menu.setHeaderTitle(account.getAccountName());
        String[] menuItems = getResources().getStringArray(R.array.context_menu_account_dashboard);
        for(String menuItem : menuItems) {
            menu.add(menuItem);
        }

        // balance account should work only for transaction accounts.
        AccountService service = new AccountService(getActivity());
        List<String> accountTypes = service.getTransactionAccountTypeNames();
        String accountType = account.getAccountType();
        if (accountTypes.contains(accountType)) {
            menu.add(R.string.balance_account);
        }

        // Investment menu items.
        if (accountType.equals(AccountTypes.INVESTMENT.toString())) {
            menu.add(Menu.NONE, ContextMenuIds.Portfolio.getId(), 0, getString(R.string.portfolio));
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        boolean result = false;

        // get account id
        QueryAccountBills account = getSelectedAccount(item);
        if (account == null) return false;

        int accountId = account.getAccountId();

        // get the action
        String menuItemTitle = item.getTitle().toString();
        
        if (menuItemTitle.equalsIgnoreCase(getString(R.string.edit))) {
            Intent intent = new Intent(getActivity(), AccountEditActivity.class);
            intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, accountId);
            intent.setAction(Intent.ACTION_EDIT);
            startActivity(intent);

            result = true;
        }
        if (menuItemTitle.equalsIgnoreCase(getString(R.string.balance_account))) {
            startBalanceAccount(account);
        }
        if (menuItemTitle.equalsIgnoreCase(getString(R.string.portfolio))) {
            EventBus.getDefault().post(new RequestPortfolioFragmentEvent(accountId));
        }

        return result;
    }

    // Other

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        outState.putInt(TAG_BALANCE_ACCOUNT, this.accountBalancedId);
        Icepick.saveInstanceState(this, outState);
    }

    // Events

    @Subscribe
    public void onEvent(AmountEnteredEvent event) {
        QueryAccountBills account = this.getAccountBeingBalanced();
        Money currentBalance = MoneyFactory.fromDouble(account.getTotal());

        // calculate the diff.
        Money newBalance = event.amount;
        if (newBalance.compareTo(currentBalance) == 0) return;

        Money difference;
        TransactionTypes transactionType;

        if (newBalance.compareTo(currentBalance) > 0) {
            // new balance > current balance
            difference = newBalance.subtract(currentBalance);
            transactionType = TransactionTypes.Deposit;
        } else {
            // new balance < current balance
            difference = currentBalance.subtract(newBalance);
            transactionType = TransactionTypes.Withdrawal;
        }

        // open a new transaction screen to create a transaction to balance to the entered amount.
        Intent intent = new Intent(getContext(), CheckingTransactionEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "HomeFragment.java");

        // add balance and transaction type and payee
        IntentDataParameters params = new IntentDataParameters();
        params.accountName = account.getAccountName();
        params.transactionType = transactionType;
        params.payeeName = getContext().getString(R.string.balance_adjustment);
        params.amount = difference;
        params.categoryName = getContext().getString(R.string.cash);
        intent.setData(params.toUri());

        getContext().startActivity(intent);
    }

    // Public

    public void startLoaders() {
        LoaderManager loaderManager = getLoaderManager();
//        loaderManager.restartLoader(LOADER_USER_NAME, null, this);
        loadUsername();
        loaderManager.restartLoader(LOADER_ACCOUNT_BILLS, null, this);
        loaderManager.restartLoader(LOADER_INCOME_EXPENSES, null, this);
    }

    public void startBalanceAccount(QueryAccountBills account) {
        setAccountBeingBalanced(account);

        // get the amount via input binaryDialog.
        int currencyId = account.getCurrencyId();

        AmountInputDialog dialog = AmountInputDialog.getInstance(REQUEST_BALANCE_ACCOUNT,
                MoneyFactory.fromString("0"), currencyId, true);
        dialog.setTargetFragment(this, REQUEST_BALANCE_ACCOUNT);
        dialog.show(getActivity().getSupportFragmentManager(), TAG_BALANCE_ACCOUNT);

        // the task continues in onFinishedInputAmountDialog
    }

    // Private custom methods.

    private void addFooterToExpandableListView(double curTotal, double curReconciled) {
        // manage footer list view
        if (linearFooter == null) {
            linearFooter = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.item_account_bills, null);
            // text view into layout
            txtFooterSummary = (TextView) linearFooter.findViewById(R.id.textViewItemAccountTotal);
            txtFooterSummaryReconciled = (TextView) linearFooter.findViewById(R.id.textViewItemAccountTotalReconciled);
            if(mHideReconciled) {
                txtFooterSummaryReconciled.setVisibility(View.GONE);
            }
            // set text
            TextView txtTextSummary = (TextView) linearFooter.findViewById(R.id.textViewItemAccountName);
            txtTextSummary.setText(R.string.summary);
            // invisible image
            ImageView imgSummary = (ImageView) linearFooter.findViewById(R.id.imageViewAccountType);
            imgSummary.setVisibility(View.INVISIBLE);
            // set color textview
            txtTextSummary.setTextColor(Color.GRAY);
            txtFooterSummary.setTextColor(Color.GRAY);
            if(!mHideReconciled) {
                txtFooterSummaryReconciled.setTextColor(Color.GRAY);
            }
        }
        // remove footer
        mExpandableListView.removeFooterView(linearFooter);
        // set text
        txtTotalAccounts.setText(mCurrencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(curTotal)));
        txtFooterSummary.setText(txtTotalAccounts.getText());
        if(!mHideReconciled) {
            txtFooterSummaryReconciled.setText(mCurrencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(curReconciled)));
        }
        // add footer
        mExpandableListView.addFooterView(linearFooter, null, false);
    }

    private void createWelcomeView(View view) {
        linearWelcome = (ViewGroup) view.findViewById(R.id.linearLayoutWelcome);

        // basic preferences
        Button buttonSettings = (Button) view.findViewById(R.id.buttonSettings);
        if (buttonSettings != null) {
            buttonSettings.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }
            });
        }

        // Show current database
        TextView currentDatabaseTextView = view.findViewById(R.id.currentDatabaseTextView);
        if (currentDatabaseTextView != null) {
            String path = new DatabaseManager(getContext()).getDatabasePath();
            currentDatabaseTextView.setText(path);
        }

        // add account button
        Button btnAddAccount = view.findViewById(R.id.buttonAddAccount);
        if (btnAddAccount != null) {
            btnAddAccount.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AccountEditActivity.class);
                    intent.setAction(Intent.ACTION_INSERT);
                    startActivity(intent);
                }
            });
        }

        // Database migration v1.4 -> v2.0 location.
        setUpMigrationButton(view);
    }

    private QueryAccountBills getAccountBeingBalanced() {
        if (this.accountBeingBalanced == null) {
            AccountRepository repository = new AccountRepository(getContext());
            this.accountBeingBalanced = repository.loadAccountBills(this.accountBalancedId);
        }
        return this.accountBeingBalanced;
    }

    /**
     * Load username on the background thread, set the username and then fire an event.
     * This should be moved to the MainActivity, which actually handles the event and updates the UI.
     */
    private void loadUsername() {
        ((MmxBaseFragmentActivity) getActivity()).compositeSubscription.add(
        Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                InfoService infoService = new InfoService(getActivity());
                return infoService.getInfoValue(InfoKeys.USERNAME);
            }
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleSubscriber<String>() {
                @Override
                public void onSuccess(String value) {
                    MmexApplication.getApp().setUserName(value);
                    EventBus.getDefault().post(new UsernameLoadedEvent());
                }

                @Override
                public void onError(Throwable error) {
                    Timber.e(error, "error loading username");
                }
            })
        );
    }

    private void setAccountBeingBalanced(QueryAccountBills account) {
        this.accountBeingBalanced = account;
        accountBalancedId = account.getAccountId();
    }

    /**
     * @param visible if visible set true show the listview; false show progressbar
     */
    private void setListViewAccountBillsVisible(boolean visible) {
        if (visible) {
            mExpandableListView.setVisibility(View.VISIBLE);
            prgAccountBills.setVisibility(View.GONE);
        } else {
            mExpandableListView.setVisibility(View.GONE);
            prgAccountBills.setVisibility(View.VISIBLE);
        }
    }

    private void setUpAccountsList(View view) {
        mExpandableListView = getExpandableListView(view);

        // Handle clicking on an account.
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                QueryAccountBills selectedAccount = mAccountsByType.get(mAccountTypes.get(groupPosition))
                        .get(childPosition);
                if (selectedAccount == null) return false;

                int accountId = selectedAccount.getAccountId();
                String accountType = mAccountTypes.get(groupPosition);

                Object event;

                if (accountType.equalsIgnoreCase(AccountTypes.INVESTMENT.toString())) {
                    event = new RequestWatchlistFragmentEvent(accountId);
                } else {
                    event = new RequestAccountFragmentEvent(accountId);
                }
                EventBus.getDefault().post(event);
                return true;
            }
        });

        // store preferences when groups are collapsed/expanded
        mExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                // update collapsed group setting
                final boolean groupVisible = false;
                // update each group visibility into its own preferences.
                AppSettings settings = new AppSettings(getActivity());
                String key = getSettingsKeyFromGroupPosition(groupPosition);
                // store value.
                settings.set(key, groupVisible);
            }
        });
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                // update expanded group setting
                final boolean groupVisible = true;
                // update each group visibility into its own preferences.
                AppSettings settings = new AppSettings(getActivity());
                String key = getSettingsKeyFromGroupPosition(groupPosition);
                // store value.
                settings.set(key, groupVisible);
            }
        });

        registerForContextMenu(mExpandableListView);
    }

    private String getSettingsKeyFromGroupPosition(int groupPosition) {
        // get group name from position
        String accountType = mAccountTypes.get(groupPosition);
        String key = getActivity().getString(PreferenceConstants.PREF_DASHBOARD_GROUP_VISIBLE);
        key += "-" + accountType;

        return key;
    }

    private void setVisibilityOfAccountGroups() {
        // set visibility of the account groups.
        AppSettings settings = new AppSettings(getContext());
        // Expand groups based on their visibility preferences.
        for (int i = 0; i < mAccountTypes.size(); i++) {
            // Check saved visibility preferences. Some groups might be collapsed.
            String key = getSettingsKeyFromGroupPosition(i);
            Boolean expanded = settings.get(key, true);

            if(expanded) {
                mExpandableListView.expandGroup(i);
            }
        }
    }

    private void setUpMigrationButton(View view) {
        // check if there is a database at the old location.
        final DatabaseMigrator14To20 migrator = new DatabaseMigrator14To20(getActivity());
        boolean legacyDataExists = migrator.legacyDataExists();

        // hide option if there is no old database.
        if (!legacyDataExists) return;

        // otherwise show the options
        LinearLayout panel = (LinearLayout) view.findViewById(R.id.panelMigration);
        panel.setVisibility(View.VISIBLE);

        // e events, etc.

        Button migrateDatabaseButton = (Button) view.findViewById(R.id.buttonMigrateDatabase);
        if (migrateDatabaseButton != null) {
            if (!migrator.legacyDataExists()) {

                // add handler
                OnClickListener migrateClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean migrationSuccess = migrator.migrateLegacyDatabase();
                        if (migrationSuccess) {
                            Toast.makeText(getActivity(), R.string.database_migrate_14_to_20_success,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), R.string.database_migrate_14_to_20_failure,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                };
                migrateDatabaseButton.setOnClickListener(migrateClickListener);
            } else {
                // hide migration notification.
                RobotoTextView textMigrate = (RobotoTextView) view.findViewById(R.id.textMigrateDatabase);
                textMigrate.setVisibility(View.GONE);

                migrateDatabaseButton.setVisibility(View.GONE);
            }

        }
    }

    private QueryAccountBills getSelectedAccount(android.view.MenuItem item){
        ExpandableListView.ExpandableListContextMenuInfo info = null;
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        // clicking any context item in child fragments will also come here. We need only
        // the context menu items from the Home fragment.
        if (menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo) {
            info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        }
        if (info == null) return null;

        int groupPos, childPos;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type != ExpandableListView.PACKED_POSITION_TYPE_CHILD) return null;

        // Get the account.

        groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);

        HomeAccountsExpandableAdapter accountsAdapter = (HomeAccountsExpandableAdapter) mExpandableListView.getExpandableListAdapter();
        QueryAccountBills account = null;
        try {
            account = (QueryAccountBills) accountsAdapter.getChild(groupPos, childPos);
        } catch (Exception e) {
            Timber.e(e, "getting the selected account id");
        }

        return account;
    }

    private void renderAccountsList(Cursor cursor) {
        linearHome.setVisibility(cursor != null && cursor.getCount() > 0 ? View.VISIBLE : View.GONE);
        linearWelcome.setVisibility(linearHome.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

        mAccountsByType.clear();
        mTotalsByType.clear();
        mAccountTypes.clear();
        mGrandTotal = MoneyFactory.fromDouble(0);
        mGrandReconciled = MoneyFactory.fromDouble(0);

        // display individual accounts with balances
        if (cursor != null) {
            showAccountTotals(cursor);
        }

        // write accounts total
        addFooterToExpandableListView(mGrandTotal.toDouble(), mGrandReconciled.toDouble());

        // create adapter
        HomeAccountsExpandableAdapter expandableAdapter = new HomeAccountsExpandableAdapter(getActivity(),
                mAccountTypes, mAccountsByType, mTotalsByType, mHideReconciled);
        // set adapter and shown
        mExpandableListView.setAdapter(expandableAdapter);

        setVisibilityOfAccountGroups();
        setListViewAccountBillsVisible(true);
    }

    private void showAccountTotals(Cursor cursor) {
        while (cursor.moveToNext()) {
            QueryAccountBills accountTransaction = new QueryAccountBills(getActivity());
            accountTransaction.setValueFromCursor(cursor);

            double total = accountTransaction.getTotalBaseConvRate();
            mGrandTotal = mGrandTotal.add(MoneyFactory.fromDouble(total));
            double totalReconciled = accountTransaction.getReconciledBaseConvRate();
            mGrandReconciled = mGrandReconciled.add(MoneyFactory.fromDouble(totalReconciled));

            String accountType = accountTransaction.getAccountType().toLowerCase();
            QueryAccountBills totalForType;
            if (mAccountTypes.indexOf(accountType) == -1) {
                // add to the list of account types
                mAccountTypes.add(accountType);

                totalForType = new QueryAccountBills(getActivity());
                totalForType.setAccountType(accountType);

                // set group title
                if (AccountTypes.CASH.name().equalsIgnoreCase(accountType)) {
                    totalForType.setAccountName(getString(R.string.cash_accounts));
                } else if (AccountTypes.CHECKING.toString().equalsIgnoreCase(accountType)) {
                    totalForType.setAccountName(getString(R.string.bank_accounts));
                } else if (AccountTypes.TERM.toString().equalsIgnoreCase(accountType)) {
                    totalForType.setAccountName(getString(R.string.term_accounts));
                } else if (AccountTypes.CREDIT_CARD.toString().equalsIgnoreCase(accountType)) {
                    totalForType.setAccountName(getString(R.string.credit_card_accounts));
                } else if (AccountTypes.INVESTMENT.toString().equalsIgnoreCase(accountType)) {
                    totalForType.setAccountName(getString(R.string.investment_accounts));
                } else if (AccountTypes.LOAN.toString().equalsIgnoreCase(accountType)) {
                    totalForType.setAccountName(getString(R.string.loan_account));
                } else if (AccountTypes.SHARES.toString().equalsIgnoreCase(accountType)) {
                    totalForType.setAccountName(getString(R.string.shares_accounts));
                }
                mTotalsByType.put(accountType, totalForType);
            }

            totalForType = mTotalsByType.get(accountType);
            double reconciledBaseConversionRate = totalForType.getReconciledBaseConvRate() +
                    accountTransaction.getReconciledBaseConvRate();
            totalForType.setReconciledBaseConvRate(reconciledBaseConversionRate);
            double totalBaseConversionRate = totalForType.getTotalBaseConvRate() +
                    accountTransaction.getTotalBaseConvRate();
            totalForType.setTotalBaseConvRate(totalBaseConversionRate);

            List<QueryAccountBills> listOfAccountsOfType = mAccountsByType.get(accountType);
            if (listOfAccountsOfType == null) {
                listOfAccountsOfType = new ArrayList<>();
                mAccountsByType.put(accountType, listOfAccountsOfType);
            }
            if (!listOfAccountsOfType.contains(accountTransaction)) {
                listOfAccountsOfType.add(accountTransaction);
            }
        }
    }

    private ExpandableListView getExpandableListView(View view) {
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.listViewAccountBills);
        return mExpandableListView;
    }

    private void refreshSettings() {
        AppSettings settings = new AppSettings(getActivity());
        mHideReconciled = settings.getLookAndFeelSettings().getHideReconciledAmounts();

        if (txtFooterSummaryReconciled != null) {
            txtFooterSummaryReconciled.setVisibility(mHideReconciled ? View.GONE : View.VISIBLE);
        }
    }
}
