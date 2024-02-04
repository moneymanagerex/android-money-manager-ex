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
package com.money.manager.ex.investment.watchlist;

import android.app.Activity;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

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

import java.util.ArrayList;

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
    public Integer accountId;

    // non-static
    private Account mAccount;
    private boolean mAutoStarLoader = true;
    private View mListHeader;
    private StockRepository mStockRepository;
    private StockHistoryRepository mStockHistoryRepository;

    /**
     * Create a new instance of the fragment with accountId params
     *
     * @return new instance AllDataListFragment
     */
    public static WatchlistItemsFragment newInstance() {
        final WatchlistItemsFragment fragment = new WatchlistItemsFragment();
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setHasOptionsMenu(true);

        if (null != savedInstanceState && savedInstanceState.containsKey(KEY_ACCOUNT_ID)) {
            // get data from saved instance state
            accountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
        } else {
            accountId = getArguments().getInt(KEY_ACCOUNT_ID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        final View layout = inflater.inflate(R.layout.fragment_watchlist_item_list, container, false);

        return layout;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set fragment
        setEmptyText(getString(R.string.no_stock_data));
        setListShown(false);

        final Context context = getActivity();
        mStockRepository = new StockRepository(context);

        // create adapter
        final StocksCursorAdapter adapter = new StocksCursorAdapter(context, null);

        // e list item click.
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                // Ignore the header row.
                if (0 < getListView().getHeaderViewsCount() && 0 == position) return;

                if (null != getListAdapter() && getListAdapter() instanceof StocksCursorAdapter) {
                    getActivity().openContextMenu(view);
                }
            }
        });

        // if header is not null add to list view
        if (null == getListAdapter()) {
            if (null != mListHeader) {
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
        if (mAutoStarLoader) {
            reloadData();
        }

        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RequestCodes.PRICE == requestCode) {
            if (Activity.RESULT_OK != resultCode) return;

            reloadData();
        }
    }

    // context menu

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo menuInfo) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // ignore the header row if the headers are shown.
        if (hasHeaderRow() && 0 == info.position) return;

        final Cursor cursor = ((StocksCursorAdapter) getListAdapter()).getCursor();

        final int cursorPosition = hasHeaderRow() ? info.position - 1 : info.position;
        cursor.moveToPosition(cursorPosition);

        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(StockFields.SYMBOL)));

        final MenuHelper menuHelper = new MenuHelper(getActivity(), menu);
        menuHelper.addToContextMenu(ContextMenuIds.DownloadPrice);
        menuHelper.addToContextMenu(ContextMenuIds.EditPrice);
        menuHelper.addToContextMenu(ContextMenuIds.DELETE);
    }

    /**
     * Context menu click handler. Update individual price.
     *
     * @param item selected context menu item.
     * @return indicator whether the action is handled or not.
     */
    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        final ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        if (!(menuInfo instanceof AdapterView.AdapterContextMenuInfo)) return false;

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) ;

        final Cursor cursor = ((StocksCursorAdapter) getListAdapter()).getCursor();
//        long packedPosition = hasHeaderRow() ? info.packedPosition - 1 : info.packedPosition;
        final int cursorPosition = hasHeaderRow() ? info.position - 1 : info.position;
        cursor.moveToPosition(cursorPosition);

        final Stock stock = Stock.from(cursor);
        final String symbol = stock.getSymbol();

        boolean result = false;
        final ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());

        switch (menuId) {
            case DownloadPrice:
                // Update price
                EventBus.getDefault().post(new PriceUpdateRequestEvent(symbol));
                result = true;
                break;

            case EditPrice:
                // Edit price
                final int accountId = stock.getHeldAt();
                final Money currentPrice = stock.getCurrentPrice();

                final Intent intent = IntentFactory.getPriceEditIntent(getActivity());
                intent.putExtra(EditPriceDialog.ARG_ACCOUNT, accountId);
                intent.putExtra(EditPriceDialog.ARG_SYMBOL, symbol);
                intent.putExtra(EditPriceDialog.ARG_PRICE, currentPrice.toString());
                getAccount();
                intent.putExtra(PriceEditActivity.ARG_CURRENCY_ID, mAccount.getCurrencyId());
                final String dateString = new MmxDate().toIsoDateString();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Loader

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final MmxCursorLoader result;

        //animation
        setListShown(false);

        if (ID_LOADER == id) {// compose selection and sort
            String selection = "";
            if (null != args && args.containsKey(AllDataListFragment.KEY_ARGUMENTS_WHERE)) {
                final ArrayList<String> whereClause = args.getStringArrayList(AllDataListFragment.KEY_ARGUMENTS_WHERE);
                if (null != whereClause) {
                    for (int i = 0; i < whereClause.size(); i++) {
                        selection += (!TextUtils.isEmpty(selection) ? " AND " : "") + whereClause.get(i);
                    }
                }
            }

            // set sort
            String sort = "";
            if (null != args && args.containsKey(AllDataListFragment.KEY_ARGUMENTS_SORT)) {
                sort = args.getString(AllDataListFragment.KEY_ARGUMENTS_SORT);
            }

            final Select query = new Select(mStockRepository.getAllColumns())
                    .where(selection)
                    .orderBy(sort);

            result = new MmxCursorLoader(getActivity(), mStockRepository.getUri(), query);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        // reset the cursor reference to reduce memory leaks
        ((CursorAdapter) getListAdapter()).changeCursor(null);
//        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        if (ID_LOADER == loader.getId()) {// send the data to the view adapter.
            final StocksCursorAdapter adapter = (StocksCursorAdapter) getListAdapter();
            adapter.changeCursor(data);

            if (isResumed()) {
                setListShown(true);

                if (null != getFloatingActionButton()) {
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
    public void onSaveInstanceState(final Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putInt(KEY_ACCOUNT_ID, accountId);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            final MmxBaseFragmentActivity activity = (MmxBaseFragmentActivity) getActivity();
            if (null != activity) {
                final ActionBar actionBar = activity.getSupportActionBar();
                if (null != actionBar) {
                    final View customView = actionBar.getCustomView();
                    if (null != customView) {
                        actionBar.setCustomView(null);
                    }
                }
            }
        } catch (final Exception e) {
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
     * @param mAutoStarLoader the mAutoStarLoader to set
     */
    public void setAutoStarLoader(final boolean mAutoStarLoader) {
        this.mAutoStarLoader = mAutoStarLoader;
    }

    /**
     * Start loader into fragment
     */
    public void reloadData() {
        // reset the account so that it gets loaded when referenced the next time.
        mAccount = null;

        final Bundle arguments = prepareArgsForChildFragment();
        // mLoaderArgs
        getLoaderManager().restartLoader(ID_LOADER, arguments, this);
    }

    public void setListHeader(final View mHeaderList) {
        mListHeader = mHeaderList;
    }

    public StockHistoryRepository getStockHistoryRepository() {
        if (null == mStockHistoryRepository) {
            mStockHistoryRepository = new StockHistoryRepository(getActivity());
        }
        return mStockHistoryRepository;
    }

    // Private

    private Account getAccount() {
        if (Constants.NOT_SET == this.accountId) return null;
        if (null != this.mAccount) return mAccount;

        final AccountRepository repo = new AccountRepository(getActivity());
        mAccount = repo.load(accountId);

        return mAccount;
    }

    private void displayHeaderData() {
        final TextView label = getView().findViewById(R.id.cashBalanceLabel);
        final TextView textView = getView().findViewById(R.id.cashBalanceTextView);
        if (null == label || null == textView) return;

        // Clear if no account id, i.e. all accounts displayed.
        if (Constants.NOT_SET == this.accountId) {
            label.setText("");
            textView.setText("");
            return;
        }

        label.setText(getString(R.string.cash));

        getAccount();
        if (null == mAccount) return;

        final FormatUtilities formatter = new FormatUtilities(getActivity());
        textView.setText(formatter.format(
                mAccount.getInitialBalance(), mAccount.getCurrencyId()));
    }

    private boolean hasHeaderRow() {
        return 0 < getListView().getHeaderViewsCount();
    }

    private Bundle prepareArgsForChildFragment() {
        final ArrayList<String> selection = new ArrayList<>();

        if (Constants.NOT_SET != this.accountId) {
            selection.add(StockFields.HELDAT + "=" + accountId);
        }

        final Bundle args = new Bundle();
        args.putStringArrayList(AllDataListFragment.KEY_ARGUMENTS_WHERE, selection);
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT, StockFields.SYMBOL + " ASC");

        return args;
    }

    private void openEditInvestmentActivity() {
        final Intent intent = new Intent(getActivity(), InvestmentTransactionEditActivity.class);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_ACCOUNT_ID, accountId);
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog(final int id) {
        final UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getActivity())
                .title(R.string.delete_transaction)
                .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle_o))
                .content(R.string.confirmDelete)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        final StockRepository repo = new StockRepository(getActivity());
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
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        // close binaryDialog
                        dialog.cancel();
                    }
                })
                .show();
    }

}
