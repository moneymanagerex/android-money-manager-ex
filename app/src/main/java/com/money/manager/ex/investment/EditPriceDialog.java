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
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.utils.DateUtils;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconDrawable;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;
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

    private static final String KEY_ACCOUNT = "EditPriceDialog:Account";
    private static final String KEY_SYMBOL = "EditPriceDialog:Symbol";
    private static final String KEY_PRICE = "EditPriceDialog:Price";
    private static final String KEY_DATE = "EditPriceDialog:Date";

    private Context mContext;

    private RobotoTextView mAmountTextView;
    private RobotoTextView mDateTextView;

    private int mAccountId;
    private String mSymbol;
    private Money mCurrentPrice;
    private String mPriceDate;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

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
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getContext();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        // Create dialog.

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getContext())
            .setTitle(mSymbol)
            .setIcon(FontIconDrawable.inflate(mContext, R.xml.ic_euro));

        View viewDialog = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_stock_price, null);
        builder.setView(viewDialog);

        // date picker

        mDateTextView = (RobotoTextView) viewDialog.findViewById(R.id.dateTextView);
        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) mDateTextView.getTag());

                CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
                    .setOnDateSetListener(listener)
                    .setPreselectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .setThemeDark();
                datePicker.show(((FragmentActivity)mContext).getSupportFragmentManager(), datePicker.getClass().getSimpleName());
            }

            CalendarDatePickerDialogFragment.OnDateSetListener listener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    try {
                        String enteredDate = Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth);
                        Date date = DateUtils.getDateFromString(enteredDate, Constants.ISO_DATE_FORMAT);
                        mDateTextView.setTag(date);
                        mDateTextView.setText(new SimpleDateFormat(Constants.LONG_DATE_MEDIUM_DAY_PATTERN,
                            MoneyManagerApplication.getInstanceApp().getAppLocale())
                                .format((Date) mDateTextView.getTag()));

                        mPriceDate = enteredDate;
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(mContext, this);
                        handler.handle(e, "setting the date");
                    }
                }
            };
        };
        mDateTextView.setOnClickListener(dateClickListener);

        Date latestDate;
        if (StringUtils.isEmpty(mPriceDate)) {
            mPriceDate = DateUtils.getStringFromDate(getContext(), new Date(), Constants.ISO_DATE_FORMAT);
        }
        latestDate = DateUtils.getDateFromString(mPriceDate, Constants.ISO_DATE_FORMAT);
        mDateTextView.setTag(latestDate);
        formatExtendedDate(mDateTextView);

        // price

        mAmountTextView = (RobotoTextView) viewDialog.findViewById(R.id.amountTextView);
        AccountRepository accountRepository = new AccountRepository(mContext);
        Account account = accountRepository.load(mAccountId);

        final int currencyId = account.getCurrencyId();

        View.OnClickListener onClickAmount = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Money amount = MoneyFactory.fromString(v.getTag().toString());

                AmountInputDialog dialog = AmountInputDialog.getInstance("ignore", amount, currencyId, false);
                dialog.show(getFragmentManager(), TAG_AMOUNT_INPUT);
                // getChildFragmentManager() ?
            }
        };
        mAmountTextView.setOnClickListener(onClickAmount);

        // get the current record price
        showCurrentPrice(mCurrentPrice);

        // actions
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //save price
                Money amount = MoneyFactory.fromString(mAmountTextView.getTag().toString());
                DateTime date = new DateTime((Date) mDateTextView.getTag());

                StockRepository repo = new StockRepository(mContext);
                repo.updateCurrentPrice(mSymbol, amount);

                StockHistoryRepository historyRepository = new StockHistoryRepository(mContext);
                boolean result = historyRepository.addStockHistoryRecord(mSymbol, amount, date);
                if (!result) {
                    Toast.makeText(mContext, mContext.getString(R.string.error_update_currency_exchange_rate),
                            Toast.LENGTH_SHORT).show();
                }

                DropboxHelper.notifyDataChanged();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(KEY_ACCOUNT, mAccountId);
        savedInstanceState.putString(KEY_SYMBOL, mSymbol);
        savedInstanceState.putString(KEY_PRICE, mCurrentPrice.toString());
        savedInstanceState.putString(KEY_DATE, mPriceDate);
    }

    // Events

    public void onEvent(AmountEnteredEvent event) {
        // set the amount on the dialog.
        showCurrentPrice(event.amount);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        this.mAccountId = savedInstanceState.getInt(KEY_ACCOUNT);
        this.mSymbol = savedInstanceState.getString(KEY_SYMBOL);
        this.mCurrentPrice = MoneyFactory.fromString(savedInstanceState.getString(KEY_PRICE));
        this.mPriceDate = savedInstanceState.getString(KEY_DATE);
    }

    public void setParameters(int accountId, final String symbol, Money currentPrice) {
        mAccountId = accountId;
        mSymbol = symbol;
        mCurrentPrice = currentPrice;
    }

    private void showCurrentPrice(Money currentPrice) {
        mAmountTextView.setText(currentPrice.toString());
        mAmountTextView.setTag(currentPrice.toString());

        this.mCurrentPrice = currentPrice;
    }

    public void formatExtendedDate(TextView dateTextView) {
        try {
            dateTextView.setText(new SimpleDateFormat(Constants.LONG_DATE_MEDIUM_DAY_PATTERN,
                MoneyManagerApplication.getInstanceApp().getAppLocale())
                    .format((Date) dateTextView.getTag()));
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "formatting date");
        }
    }
}
