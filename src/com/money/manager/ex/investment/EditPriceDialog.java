/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.investment;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.StockHistoryRepository;
import com.money.manager.ex.businessobjects.StockRepository;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.utils.DateUtils;
import com.money.manager.ex.view.RobotoTextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Edit price dialog for manual entry/modification of the latest stock price.
 * Created by Alen on 19/07/2015.
 * Ref:
 * http://developer.android.com/guide/topics/ui/dialogs.html
 * http://www.vogella.com/tutorials/AndroidDialogs/article.html
 */
public class EditPriceDialog
    extends DialogFragment
    implements IInputAmountDialogListener {

    public static final int REQUEST_AMOUNT = 1;
    public static final String TAG_AMOUNT_INPUT = "EditPriceDialog:AmountInput";

    private static final String KEY_ACCOUNT = "EditPriceDialog:Account";
    private static final String KEY_SYMBOL = "EditPriceDialog:Symbol";
    private static final String KEY_PRICE = "EditPriceDialog:Price";
    private static final String KEY_DATE = "EditPriceDialog:Date";

//    public EditPriceDialog(Context context) {
////        super(context);
//
//        mContext = context;
//    }

    private Context mContext;

    private RobotoTextView mAmountTextView;
    private RobotoTextView mDateTextView;

    private int mAccountId;
    private String mSymbol;
    private double mCurrentPrice;
    private String mPriceDate;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        InputAmountDialog inputAmountDialog = (InputAmountDialog) getFragmentManager()
//                .findFragmentByTag(TAG_AMOUNT_INPUT);
//        if (inputAmountDialog != null) {
////            Log.d("test", "input amount dialog found");
//            inputAmountDialog.show();
//        }

    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getContext();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        // Create dialog.

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);

        View viewDialog = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_stock_price, null);
        builder.setView(viewDialog);

        // date picker

        mDateTextView = (RobotoTextView) viewDialog.findViewById(R.id.dateTextView);
        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) mDateTextView.getTag());
                DatePickerDialog dialog = DatePickerDialog.newInstance(mDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
                dialog.setCloseOnSingleTapDay(true);
                dialog.show(((FragmentActivity)mContext).getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }

            public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                    try {
                        String enteredDate = Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth);
                        Date date = DateUtils.getDateFromString(getContext(), enteredDate, Constants.PATTERN_DB_DATE);
                        mDateTextView.setTag(date);
                        mDateTextView.setText(new SimpleDateFormat("EEEE dd MMMM yyyy",
                                mContext.getResources().getConfiguration().locale)
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
//        if (latestPriceValues != null) {
//            String latestPriceDate = latestPriceValues.getAsString(StockHistory.DATE);
//            try {
//                latestDate = new SimpleDateFormat(Constants.PATTERN_DB_DATE)
//                        .parse(latestPriceDate);
//            } catch (ParseException pex) {
//                latestDate = Calendar.getInstance().getTime();
//            }
//        } else {
//            latestDate = Calendar.getInstance().getTime();
//        }
        if (StringUtils.isEmpty(mPriceDate)) {
            mPriceDate = DateUtils.getStringFromDate(getContext(), new Date(), Constants.PATTERN_DB_DATE);
        }
        latestDate = DateUtils.getDateFromString(getContext(), mPriceDate, Constants.PATTERN_DB_DATE);
        mDateTextView.setTag(latestDate);
        formatExtendedDate(mDateTextView);

        // price

        mAmountTextView = (RobotoTextView) viewDialog.findViewById(R.id.amountTextView);
        AccountRepository accountRepository = new AccountRepository(mContext);
        TableAccountList account = accountRepository.load(mAccountId);

        final int currencyId = account.getCurrencyId();

        View.OnClickListener onClickAmount = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double amount = (Double) v.getTag();
                InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(), amount, currencyId);
                dialog.setTargetFragment(EditPriceDialog.this, REQUEST_AMOUNT);
                dialog.roundToCurrencyDecimals = false;
                dialog.show(getFragmentManager(), TAG_AMOUNT_INPUT);
                // getChildFragmentManager() ?
            }
        };
        mAmountTextView.setOnClickListener(onClickAmount);

        // get the current record price
        double latestPrice;
//        if (latestPriceValues != null) {
//            String currentPriceString = latestPriceValues.getAsString(StockHistory.VALUE);
//            latestPrice = Double.parseDouble(currentPriceString);
//        } else {
//            latestPrice = mCurrentPrice;
//        }
        latestPrice = mCurrentPrice;
        showCurrentPrice(latestPrice, mAccountId);

        // actions
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //save price
                double amount = (Double) mAmountTextView.getTag();
                Date date = (Date) mDateTextView.getTag();

                StockRepository repo = new StockRepository(mContext);
                repo.updateCurrentPrice(mSymbol, BigDecimal.valueOf(amount));

                StockHistoryRepository historyRepository = new StockHistoryRepository(mContext);
                boolean result = historyRepository.addStockHistoryRecord(mSymbol, BigDecimal.valueOf(amount), date);
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

        builder.setTitle(mSymbol);

//        builder.create().show();
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(KEY_ACCOUNT, mAccountId);
        savedInstanceState.putString(KEY_SYMBOL, mSymbol);
        savedInstanceState.putDouble(KEY_PRICE, mCurrentPrice);
        savedInstanceState.putString(KEY_DATE, mPriceDate);
    }

    private ContentValues loadLatestValuesFor(String symbol) {
        // get the current record values
        StockHistoryRepository historyRepository = new StockHistoryRepository(mContext.getApplicationContext());
        ContentValues latestPriceValues = historyRepository.getLatestPriceFor(symbol);
        // symbol, date, value
        if (latestPriceValues == null) {
            // todo: No history available. Get the values from the stock record.
//            StockRepository repository = new StockRepository(mContext);
//            repository.findIdsBySymbol()
        }

        return latestPriceValues;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        this.mAccountId = savedInstanceState.getInt(KEY_ACCOUNT);
        this.mSymbol = savedInstanceState.getString(KEY_SYMBOL);
        this.mCurrentPrice = savedInstanceState.getDouble(KEY_PRICE);
        this.mPriceDate = savedInstanceState.getString(KEY_DATE);

//        InputAmountDialog inputAmountDialog = (InputAmountDialog) getFragmentManager()
//                .findFragmentByTag(TAG_AMOUNT_INPUT);
//        if (inputAmountDialog != null) {
//            Log.d("test", "input amount dialog found");
//        }

    }

    public void setParameters(int accountId, final String symbol, double currentPrice) {
        mAccountId = accountId;
        mSymbol = symbol;
        mCurrentPrice = currentPrice;

//        ContentValues values = this.loadLatestValuesFor(symbol);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        // set the amount on the dialog.
        showCurrentPrice(amount, mAccountId);
    }

    private void showCurrentPrice(double currentPrice, int accountId) {
        EditTransactionCommonFunctions commonFunctions = new EditTransactionCommonFunctions(mContext, null);
        commonFunctions.displayAmountFormatted(mAmountTextView, currentPrice, accountId);

        AccountRepository accountRepository = new AccountRepository(mContext);
        TableAccountList account = accountRepository.load(accountId);
        int currencyId = account.getCurrencyId();
        CurrencyService currencyService = new CurrencyService(mContext.getApplicationContext());
        TableCurrencyFormats currency = currencyService.getCurrency(currencyId);
        String currencySymbol = currency.getSfxSymbol();

        mAmountTextView.setText(currencySymbol + " " + Double.toString(currentPrice));
        mAmountTextView.setTag(currentPrice);

        this.mCurrentPrice = currentPrice;
    }

    public void formatExtendedDate(TextView dateTextView) {
        try {
            dateTextView.setText(new SimpleDateFormat("EEEE dd MMMM yyyy", mContext.getResources().getConfiguration().locale)
                    .format((Date) dateTextView.getTag()));
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "formatting date");
        }
    }
}
