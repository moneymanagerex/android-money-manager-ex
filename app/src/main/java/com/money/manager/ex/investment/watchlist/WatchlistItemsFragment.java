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
package com.money.manager.ex.investment.watchlist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.StockFields;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.investment.EditPriceDialog;
import com.money.manager.ex.investment.InvestmentTransactionEditActivity;
import com.money.manager.ex.investment.PriceEditActivity;
import com.money.manager.ex.investment.StocksCursorAdapter;
import com.money.manager.ex.investment.events.PriceUpdateRequestEvent;
import com.money.manager.ex.utils.MmxDate;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import info.javaperformance.money.Money;
import timber.log.Timber;

/**
 * The list of securities.
 */
public class WatchlistItemsFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int ID_LOADER = 1;
    public static final String KEY_ACCOUNT_ID = "WatchlistItemsFragment:AccountId";

    /**
     * Create a new instance of the fragment with accountId params
     * @return new instance AllDataListFragment
     */
    public static WatchlistItemsFragment newInstance() {
        WatchlistItemsFragment fragment = new WatchlistItemsFragment();
        return fragment;
    }

    // non-static

    public Integer accountId;

    private Account mAccount;
    private boolean mAutoStarLoader = true;
    private View mListHeader = null;
    private StockRepository mStockRepository;
    private StockHistoryRepository mStockHistoryRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setHasOptionsMenu(true);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ACCOUNT_ID)) {
            // get data from saved instance state
            this.accountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
        } else {
            this.accountId = getArguments().getInt(KEY_ACCOUNT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_watchlist_item_list, container, false);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set fragment
        setEmptyText(getString(R.string.no_stock_data));
        setListShown(false);

        Context context = getActivity();
        mStockRepository =  new StockRepository(context);

        // create adapter
        StocksCursorAdapter adapter = new StocksCursorAdapter(context, null);

        // e list item click.
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ignore the header row.
                if (getListView().getHeaderViewsCount() > 0 && position == 0) return;

                if (getListAdapter() != null && getListAdapter() instanceof StocksCursorAdapter) {
                    getActivity().openContextMenu(view);
                }
            }
        });

        // if header is not null add to list view
        if (getListAdapter() == null) {
            if (mListHeader != null) {
                getListView().addHeaderView(mListHeader);
            } else {
                getListView().removeHeaderView(mListHeader);
            }
        }

        // set adapter
        setListAdapter(adapter);

        // register context menu
        registerForContextMenu(getListView());

        // start loader
        if (isAutoStarLoader()) {
            reloadData();
        }

        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RequestCodes.PRICE:
                if (resultCode != AppCompatActivity.RESULT_OK) return;

                reloadData();
                break;
        }
    }

    // context menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // ignore the header row if the headers are shown.
        if (hasHeaderRow() && info.position == 0) return;

        Cursor cursor = ((StocksCursorAdapter) getListAdapter()).getCursor();

        int cursorPosition = hasHeaderRow() ? info.position - 1 : info.position;
        cursor.moveToPosition(cursorPosition);

        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(StockFields.SYMBOL)));

        MenuHelper menuHelper = new MenuHelper(getActivity(), menu);
        menuHelper.addToContextMenu(ContextMenuIds.DownloadPrice);
        menuHelper.addToContextMenu(ContextMenuIds.EditPrice);
        menuHelper.addToContextMenu(ContextMenuIds.DELETE);
    }

    /**
     * Context menu click handler. Update individual price.
     * @param item selected context menu item.
     * @return indicator whether the action is handled or not.
     */
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        if (!(menuInfo instanceof AdapterView.AdapterContextMenuInfo)) return false;

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) ;

        Cursor cursor = ((StocksCursorAdapter) getListAdapter()).getCursor();
//        long packedPosition = hasHeaderRow() ? info.packedPosition - 1 : info.packedPosition;
        int cursorPosition = hasHeaderRow() ? info.position - 1 : info.position;
        cursor.moveToPosition(cursorPosition);

        Stock stock = Stock.from(cursor);
        String symbol = stock.getSymbol();

        boolean result = false;
        ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());

        switch (menuId) {
            case DownloadPrice:
                // Update price
                EventBus.getDefault().post(new PriceUpdateRequestEvent(symbol));
                result = true;
                break;

            case EditPrice:
                // Edit price
                int accountId = stock.getHeldAt();
                Money currentPrice = stock.getCurrentPrice();

                Intent intent = IntentFactory.getPriceEditIntent(getActivity());
                intent.putExtra(EditPriceDialog.ARG_ACCOUNT, accountId);
                intent.putExtra(EditPriceDialog.ARG_SYMBOL, symbol);
                intent.putExtra(EditPriceDialog.ARG_PRICE, currentPrice.toString());
                getAccount();
                intent.putExtra(PriceEditActivity.ARG_CURRENCY_ID, mAccount.getCurrencyId());
                String dateString = new MmxDate().toIsoDateString();
                intent.putExtra(EditPriceDialog.ARG_DATE, dateString);
                startActivityForResult(intent, RequestCodes.PRICE);

//                EditPriceDialog dialog = new EditPriceDialog();
//                Bundle args = new Bundle();
//                args.putInt(EditPriceDialog.ARG_ACCOUNT, accountId);
//                args.putString(EditPriceDialog.ARG_SYMBOL, symbol);
//                args.putString(EditPriceDialog.ARG_PRICE, currentPrice.toString());
//                String dateString = new MmxDate().toIsoDateString();
//                args.putString(EditPriceDialog.ARG_DATE, dateString);
//                dialog.setArguments(args);
//                dialog.show(getChildFragmentManager(), "input-amount");
                break;

            case DELETE:
                showDeleteConfirmationDialog(stock.getId());
                break;
        }

        return result;
    }

    /**
     * This is just to test:
     * http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
     */
    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (Exception e) {
            // NoSuchFieldException | IllegalAccessException
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        MmxCursorLoader result;

        //animation
        setListShown(false);

        switch (id) {
            case ID_LOADER:
                // compose selection and sort
                String selection = "";
                if (args != null && args.containsKey(AllDataListFragment.KEY_ARGUMENTS_WHERE)) {
                    ArrayList<String> whereClause = args.getStringArrayList(AllDataListFragment.KEY_ARGUMENTS_WHERE);
                    if (whereClause != null) {
                        for (int i = 0; i < whereClause.size(); i++) {
                            selection += (!TextUtils.isEmpty(selection) ? " AND " : "") + whereClause.get(i);
                        }
                    }
                }

                // set sort
                String sort = "";
                if (args != null && args.containsKey(AllDataListFragment.KEY_ARGUMENTS_SORT)) {
                    sort = args.getString(AllDataListFragment.KEY_ARGUMENTS_SORT);
                }

                Select query = new Select(mStockRepository.getAllColumns())
                        .where(selection)
                        .orderBy(sort);

                result = new MmxCursorLoader(getActivity(), mStockRepository.getUri(), query);
                break;
            default:
                result = null;
        }
        return result;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // reset the cursor reference to reduce memory leaks
        ((CursorAdapter) getListAdapter()).changeCursor(null);
//        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER:
                // send the data to the view adapter.
                StocksCursorAdapter adapter = (StocksCursorAdapter) getListAdapter();
                adapter.changeCursor(data);

                if (isResumed()) {
                    setListShown(true);

                    if (getFloatingActionButton() != null) {
                        getFloatingActionButton().show(true);
                    }
                } else {
                    setListShownNoAnimation(true);
                }
                // update the header
                displayHeaderData();
        }
    }

    @Override
    public void onFloatingActionButtonClicked() {
        openEditInvestmentActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putInt(KEY_ACCOUNT_ID, this.accountId);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            MmxBaseFragmentActivity activity = (MmxBaseFragmentActivity) getActivity();
            if (activity != null) {
                ActionBar actionBar = activity.getSupportActionBar();
                if(actionBar != null) {
                    View customView = actionBar.getCustomView();
                    if (customView != null) {
                        actionBar.setCustomView(null);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e, "stopping watchlist items fragment");
        }
    }

    @Override
    public String getSubTitle() {
        return null;
    }

    public boolean isAutoStarLoader() {
        return mAutoStarLoader;
    }

    /**
     * Start loader into fragment
     */
    public void reloadData() {
        // reset the account so that it gets loaded when referenced the next time.
        mAccount = null;

        Bundle arguments = prepareArgsForChildFragment();
        // mLoaderArgs
        getLoaderManager().restartLoader(ID_LOADER, arguments, this);
    }

    /**
     * @param mAutoStarLoader the mAutoStarLoader to set
     */
    public void setAutoStarLoader(boolean mAutoStarLoader) {
        this.mAutoStarLoader = mAutoStarLoader;
    }

    public void setListHeader(View mHeaderList) {
        this.mListHeader = mHeaderList;
    }

    public StockHistoryRepository getStockHistoryRepository() {
        if (mStockHistoryRepository == null) {
            mStockHistoryRepository = new StockHistoryRepository(getActivity());
        }
        return mStockHistoryRepository;
    }

    // Private

    private Account getAccount() {
        if (this.accountId == Constants.NOT_SET) return null;
        if (this.mAccount != null) return mAccount;

        AccountRepository repo = new AccountRepository(getActivity());
        mAccount = repo.load(this.accountId);

        return mAccount;
    }

    private void displayHeaderData() {
        TextView label = (TextView) getView().findViewById(R.id.cashBalanceLabel);
        TextView textView = (TextView) getView().findViewById(R.id.cashBalanceTextView);
        if (label == null || textView == null) return;

        // Clear if no account id, i.e. all accounts displayed.
        if (this.accountId == Constants.NOT_SET) {
            label.setText("");
            textView.setText("");
            return;
        }

        label.setText(getString(R.string.cash));

        getAccount();
        if (mAccount == null) return;

        FormatUtilities formatter = new FormatUtilities(getActivity());
        textView.setText(formatter.format(
            mAccount.getInitialBalance(), mAccount.getCurrencyId()));
    }

    private boolean hasHeaderRow() {
        return getListView().getHeaderViewsCount() > 0;
    }

    private Bundle prepareArgsForChildFragment() {
        ArrayList<String> selection = new ArrayList<>();

        if (this.accountId != Constants.NOT_SET) {
            selection.add(StockFields.HELDAT + "=" + Integer.toString(this.accountId));
        }

        Bundle args = new Bundle();
        args.putStringArrayList(AllDataListFragment.KEY_ARGUMENTS_WHERE, selection);
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT, StockFields.SYMBOL + " ASC");

        return args;
    }

    private void openEditInvestmentActivity() {
        Intent intent = new Intent(getActivity(), InvestmentTransactionEditActivity.class);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_ACCOUNT_ID, this.accountId);
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog(final int id) {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getActivity())
                .title(R.string.delete_transaction)
                .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle))
                .content(R.string.confirmDelete)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        StockRepository repo = new StockRepository(getActivity());
                        if (!repo.delete(id)) {
                            new UIHelper(getActivity()).showToast(R.string.db_delete_failed);
                        }

                        // restart loader
                        reloadData();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // close binaryDialog
                        dialog.cancel();
                    }
                })
                .show();
    }

}
