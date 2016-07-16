/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.investment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.os.ParcelableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.utils.MyDateTimeUtils;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconDrawable;

import org.antlr.v4.codegen.model.Sync;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Edit price dialog for manual entry/modification of the latest stock price.
 * Ref:
 * http://developer.android.com/guide/topics/ui/dialogs.html
 * http://www.vogella.com/tutorials/AndroidDialogs/article.html
 */
public class EditPriceDialog
    extends DialogFragment {

    public static final String TAG_AMOUNT_INPUT = "EditPriceDialog:AmountInput";

    public static final String ARG_ACCOUNT = "EditPriceDialog:Account";
    public static final String ARG_SYMBOL = "EditPriceDialog:Symbol";
    public static final String ARG_PRICE = "EditPriceDialog:Price";
    public static final String ARG_DATE = "EditPriceDialog:Date";

    private static final String KEY_ACCOUNT = "EditPriceDialog:Account";
    private static final String KEY_PRICE = "EditPriceDialog:Price";

    private EditPriceViewHolder viewHolder;
    private PriceDownloadedEvent mPrice;
    private int mAccountId;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            mAccountId = getArguments().getInt(ARG_ACCOUNT);
            String symbol = getArguments().getString(ARG_SYMBOL);
            Money price = MoneyFactory.fromString(getArguments().getString(ARG_PRICE));
            DateTime date = DateTime.parse(getArguments().getString(ARG_DATE));
            mPrice = new PriceDownloadedEvent(symbol, price, date);
        }

        // Create dialog.

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getContext())
                .setTitle(mPrice.symbol)
                .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_euro));

        View viewDialog = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_stock_price, null);
        builder.setView(viewDialog);

        viewHolder = new EditPriceViewHolder(viewDialog);
        initializeControls(viewHolder);

        // actions
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //save price
                StockRepository repo = new StockRepository(getContext());
                repo.updateCurrentPrice(mPrice.symbol, mPrice.price);

                StockHistoryRepository historyRepository = new StockHistoryRepository(getContext());
                boolean result = historyRepository.addStockHistoryRecord(mPrice);
                if (!result) {
                    Toast.makeText(getContext(), getContext().getString(R.string.error_update_currency_exchange_rate),
                            Toast.LENGTH_SHORT).show();
                }

                new SyncManager(getContext()).dataChanged();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        showDate();
        showCurrentPrice();

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(KEY_ACCOUNT, mAccountId);
        savedInstanceState.putParcelable(KEY_PRICE, Parcels.wrap(mPrice));
    }

    // Events

    @Subscribe
    public void onEvent(AmountEnteredEvent event) {
        mPrice.price = event.amount;
        showCurrentPrice();
    }

    /*
        Private
     */

    private void initializeControls(final EditPriceViewHolder viewHolder) {
        // date picker

        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(listener)
                        .setPreselectedDate(mPrice.date.getYear(), mPrice.date.getMonthOfYear() - 1, mPrice.date.getDayOfMonth())
                        .setThemeDark();
                datePicker.show(((FragmentActivity) getContext()).getSupportFragmentManager(), datePicker.getClass().getSimpleName());
            }

            CalendarDatePickerDialogFragment.OnDateSetListener listener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    mPrice.date = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                    showDate();
                }
            };
        };
        viewHolder.dateTextView.setOnClickListener(dateClickListener);

        showDate();

        // date prev/next
        viewHolder.previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrice.date = mPrice.date.minusDays(1);
                showDate();
            }
        });
        viewHolder.nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrice.date = mPrice.date.plusDays(1);
                showDate();
            }
        });

        // price

        AccountRepository accountRepository = new AccountRepository(getContext());
        Account account = accountRepository.load(mAccountId);

        final int currencyId = account.getCurrencyId();

        View.OnClickListener onClickAmount = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance("ignore", mPrice.price, currencyId, false);
                dialog.show(getFragmentManager(), TAG_AMOUNT_INPUT);
                // getChildFragmentManager() ?
            }
        };
        viewHolder.amountTextView.setOnClickListener(onClickAmount);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        this.mAccountId = savedInstanceState.getInt(KEY_ACCOUNT);
        mPrice = Parcels.unwrap(savedInstanceState.getParcelable(KEY_PRICE));
    }

    private void showCurrentPrice() {
        viewHolder.amountTextView.setText(mPrice.price.toString());
    }

    private void showDate() {
        viewHolder.dateTextView.setText(mPrice.date.toString(Constants.LONG_DATE_MEDIUM_DAY_PATTERN));
    }
}
