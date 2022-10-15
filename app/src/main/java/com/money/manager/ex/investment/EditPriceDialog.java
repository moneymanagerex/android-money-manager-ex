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
package com.money.manager.ex.investment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.core.bundlers.PriceDownloadedEventBundler;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.utils.AlertDialogWrapper;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import dagger.Lazy;
import icepick.Icepick;
import icepick.State;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Edit price binaryDialog for manual entry/modification of the latest stock price.
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

    @Inject Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;

    @State int mAccountId;
    @State String mUserDateFormat;
    @State(PriceDownloadedEventBundler.class) PriceDownloadedEvent mPrice;
    @State int mCurrencyId = Constants.NOT_SET;

    private EditPriceViewHolder viewHolder;
    private Lazy<FormatUtilities> formatUtilitiesLazy;

    public EditPriceDialog() {
        super();
        MmexApplication.getApp().iocComponent.inject(this);

        formatUtilitiesLazy = new Lazy<FormatUtilities>() {
            @Override
            public FormatUtilities get() {
                return new FormatUtilities(getContext());
            }
        };
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        } else {
            createNewEntity();
        }

        // Create dialog.

        UIHelper ui = new UIHelper(getContext());

        AlertDialogWrapper builder = new AlertDialogWrapper(getContext())
                .setTitle(mPrice.symbol)
                .setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_euro_symbol));

        View viewDialog = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_stock_price, null);
        builder.setView(viewDialog);

        viewHolder = new EditPriceViewHolder();
        viewHolder.bind(viewDialog);
        initializeControls(viewHolder);

        // actions
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //update price
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
        super.onSaveInstanceState(savedInstanceState);

        Icepick.saveInstanceState(this, savedInstanceState);
    }

    @Subscribe
    public void onEvent(AmountEnteredEvent event) {
        mPrice.price = event.amount;
        showCurrentPrice();
    }

    /*
        Private
     */

    private void createNewEntity() {
        mAccountId = getArguments().getInt(ARG_ACCOUNT);
        String symbol = getArguments().getString(ARG_SYMBOL);
        Money price = MoneyFactory.fromString(getArguments().getString(ARG_PRICE));
        String dateString = getArguments().getString(ARG_DATE);
        Date date = new MmxDate(dateString).toDate();
        mPrice = new PriceDownloadedEvent(symbol, price, date);
    }

    private String getUserDateFormat() {
        if (TextUtils.isEmpty(mUserDateFormat)) {
            mUserDateFormat = dateTimeUtilsLazy.get().getUserDatePattern(getContext());
        }
        return mUserDateFormat;
    }

    private void initializeControls(final EditPriceViewHolder viewHolder) {
        // date picker

        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MmxDate priceDate = new MmxDate(mPrice.date);

                CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
                        .setFirstDayOfWeek(dateTimeUtilsLazy.get().getFirstDayOfWeek())
                        .setOnDateSetListener(listener)
                        .setPreselectedDate(priceDate.getYear(), priceDate.getMonthOfYear(), priceDate.getDayOfMonth());
                if (new UIHelper(getActivity()).isUsingDarkTheme()) {
                    datePicker.setThemeDark();
                }
                datePicker.show(((FragmentActivity) getContext()).getSupportFragmentManager(), datePicker.getClass().getSimpleName());
            }

            CalendarDatePickerDialogFragment.OnDateSetListener listener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    mPrice.date = new MmxDate(year, monthOfYear, dayOfMonth).toDate();
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
                mPrice.date = new MmxDate(mPrice.date).minusDays(1).toDate();
                showDate();
            }
        });
        viewHolder.nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrice.date = new MmxDate(mPrice.date).plusDays(1).toDate();
                showDate();
            }
        });

        // price

        AccountRepository accountRepository = new AccountRepository(getContext());
        Account account = accountRepository.load(mAccountId);
        if (mCurrencyId == Constants.NOT_SET) {
            mCurrencyId = account.getCurrencyId();
        }

        View.OnClickListener onClickAmount = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance("ignore", mPrice.price, mCurrencyId, false);
                dialog.show(getFragmentManager(), TAG_AMOUNT_INPUT);
//                Intent intent = IntentFactory.getIntentForNumericInput(getActivity(), mPrice.price, mCurrencyId, false);
//                getActivity().startActivityForResult(intent, RequestCodes.AMOUNT);
            }
        };
        viewHolder.amountTextView.setOnClickListener(onClickAmount);
    }

    private void showCurrentPrice() {
        String priceFormatted = formatUtilitiesLazy.get().format(mPrice.price, Constants.PRICE_FORMAT);
        viewHolder.amountTextView.setText(priceFormatted);
    }

    private void showDate() {
        viewHolder.dateTextView.setText(new MmxDateTimeUtils().format(mPrice.date, getUserDateFormat()));
    }
}
