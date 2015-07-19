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

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.StockHistory;
import com.money.manager.ex.businessobjects.StockHistoryRepository;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyUtils;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.view.RobotoTextView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Edit price dialog for manual entry/modification of the latest stock price.
 * Created by Alen on 19/07/2015.
 */
public class EditPriceDialog
    implements IInputAmountDialogListener {

    public EditPriceDialog(Context context) {
        mContext = context;
    }

    private Context mContext;
    private int mAccountId;
    private RobotoTextView mAmountTextView;
    private RobotoTextView mDateTextView;

    public void show(int accountId, final String symbol) {
        mAccountId = accountId;

        // get the current record date
        StockHistoryRepository historyRepository = new StockHistoryRepository(mContext.getApplicationContext());
        ContentValues latestPriceValues = historyRepository.getLatestPriceFor(symbol);
        // symbol, date, value
        String latestPriceDate = latestPriceValues.getAsString(StockHistory.DATE);

        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(mContext);

        View viewDialog = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_stock_price, null);
        alertDialog.setView(viewDialog);

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
                        Date date = new SimpleDateFormat("yyyy-MM-dd", mContext.getResources().getConfiguration().locale)
                                .parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
                        mDateTextView.setTag(date);
                        mDateTextView.setText(new SimpleDateFormat("EEEE dd MMMM yyyy", mContext.getResources().getConfiguration().locale)
                                .format((Date) mDateTextView.getTag()));
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(mContext, this);
                        handler.handle(e, "setting the date");
                    }
                }
            };
        };
        mDateTextView.setOnClickListener(dateClickListener);
        mDateTextView.setText(latestPriceDate);
        mDateTextView.setTag(Calendar.getInstance().getTime());;

        // price

        mAmountTextView = (RobotoTextView) viewDialog.findViewById(R.id.amountTextView);
        AccountRepository accountRepository = new AccountRepository(mContext);
        TableAccountList account = accountRepository.load(accountId);
        final int currencyId = account.getCurrencyId();

        View.OnClickListener onClickAmount = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double amount = (Double) v.getTag();
                InputAmountDialog dialog = InputAmountDialog.getInstance(EditPriceDialog.this,
                        v.getId(), amount, currencyId);
                dialog.RoundToCurrencyDecimals = false;
                dialog.show(((FragmentActivity)mContext).getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        mAmountTextView.setOnClickListener(onClickAmount);

        // get the current record price
        String currentPriceString = latestPriceValues.getAsString(StockHistory.VALUE);
        double currentPrice = Double.parseDouble(currentPriceString);
        showCurrentPrice(currentPrice, accountId);

        // actions
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //save price
                double amount = (Double) mAmountTextView.getTag();
                Date date = (Date) mDateTextView.getTag();
                StockHistoryRepository historyRepository = new StockHistoryRepository(mContext);
                boolean result = historyRepository.addStockHistoryRecord(symbol, BigDecimal.valueOf(amount), date);
                if (!result) {
                    Toast.makeText(mContext, mContext.getString(R.string.error_update_currency_exchange_rate),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        alertDialog.create().show();
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        // set the amount on the dialog.
        showCurrentPrice(amount, mAccountId);
    }

    private void showCurrentPrice(double currentPrice, int accountId) {
        EditTransactionCommonFunctions commonFunctions = new EditTransactionCommonFunctions(mContext);
        commonFunctions.formatAmount(mAmountTextView, currentPrice, accountId);

        AccountRepository accountRepository = new AccountRepository(mContext);
        TableAccountList account = accountRepository.load(accountId);
        int currencyId = account.getCurrencyId();
        CurrencyUtils currencyUtils = new CurrencyUtils(mContext.getApplicationContext());
        TableCurrencyFormats currency = currencyUtils.getCurrency(currencyId);
        String currencySymbol = currency.getSfxSymbol();

        mAmountTextView.setText(currencySymbol + " " + Double.toString(currentPrice));
        mAmountTextView.setTag(currentPrice);
    }
}
