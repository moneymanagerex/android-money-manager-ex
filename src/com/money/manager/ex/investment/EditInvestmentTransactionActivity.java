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

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.model.Stock;
import com.money.manager.ex.utils.DateUtils;
import com.money.manager.ex.view.RobotoTextView;
import com.money.manager.ex.view.RobotoTextViewFontIcon;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditInvestmentTransactionActivity
        extends BaseFragmentActivity
        implements IInputAmountDialogListener {

    public static final String EXTRA_ACCOUNT_ID = "EditInvestmentTransactionActivity:AccountId";
    public static final String DATEPICKER_TAG = "datepicker";
    public static final int ID_NUM_SHARES = 1;
    public static final int ID_PURCHASE_PRICE = 2;
    public static final int ID_COMMISSION = 3;
    public static final int ID_CURRENT_PRICE = 4;

    private Stock mStock;
    private boolean mDirty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_investment_transaction);

        // this handles OK/Cancel button clicks in the toolbar.
        setToolbarStandardAction(getToolbar());

        // todo: receive the account id (and read currency)

        // todo: change this initialization after adding editing feature.
        mStock = Stock.getInstance();

        initializeForm();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_edit_investment_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onActionCancelClick();
    }

    @Override
    public boolean onActionCancelClick() {
//        return mCommonFunctions.onActionCancelClick();
        setResult(Activity.RESULT_CANCELED);
        finish();

        return true;
    }

    private void initializeForm() {
        final DateUtils dateUtils = new DateUtils();

        // Purchase Date

        final RobotoTextViewFontIcon dateView = (RobotoTextViewFontIcon) this.findViewById(R.id.textViewDate);
        dateUtils.formatExtendedDate(getApplicationContext(), dateView, mStock.getPurchaseDate());
        dateView.setOnClickListener(new View.OnClickListener() {
            public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                    setDirty(true);

                    try {
                        Date date = new SimpleDateFormat(Constants.PATTERN_DB_DATE, getResources().getConfiguration().locale)
                                .parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
                        mStock.setPurchaseDate(date);
                        dateUtils.formatExtendedDate(getApplicationContext(), dateView, date);
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(getApplicationContext(), EditInvestmentTransactionActivity.this);
                        handler.handle(e, "setting the date");
                    }
                }
            };

            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mStock.getPurchaseDate());
                DatePickerDialog dialog = DatePickerDialog.newInstance(mDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                        false);
                dialog.setCloseOnSingleTapDay(true);
                dialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

        initNumberOfShares();
        initPurchasePriceControls();
        initCommission();
        initCurrentPrice();
        showValue();
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    /**
     * Raised after the amount has been entered in the number input dialog.
     *
     * @param id     Id to identify the caller.
     * @param amount Amount entered
     */
    @Override
    public void onFinishedInputAmountDialog(int id, BigDecimal amount) {
        switch (id) {
            case ID_NUM_SHARES:
                mStock.setNumberOfShares(amount);
                showNumberOfShares();
                showValue();
                break;
            case ID_PURCHASE_PRICE:
                mStock.setPurchasePrice(amount);
                showPurchasePrice();
                if (mStock.getCurrentPrice().compareTo(BigDecimal.ZERO) == 0) {
                    mStock.setCurrentPrice(amount);
                    showCurrentPrice();
                }
                break;
            case ID_COMMISSION:
                mStock.setCommission(amount);
                showCommission();
                break;
            case ID_CURRENT_PRICE:
                mStock.setCurrentPrice(amount);
                showCurrentPrice();
                showValue();
                break;
        }
    }

    private void initCommission() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo: use currency
                InputAmountDialog dialog = InputAmountDialog.getInstance(ID_COMMISSION,
                        mStock.getCommission().doubleValue());
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextView purchasePriceView = (RobotoTextView) this.findViewById(R.id.commissionView);
        purchasePriceView.setOnClickListener(onAmountClick);
        // todo: format the number of shares based on selected locale.
        showCommission();
    }

    private void initCurrentPrice() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo: use currency
                InputAmountDialog dialog = InputAmountDialog.getInstance(ID_CURRENT_PRICE,
                        mStock.getCurrentPrice().doubleValue());
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextView purchasePriceView = (RobotoTextView) this.findViewById(R.id.currentPriceView);
        purchasePriceView.setOnClickListener(onAmountClick);
        // todo: format the number of shares based on selected locale.
        showCurrentPrice();
    }

    private void initNumberOfShares() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo: use currency
                InputAmountDialog dialog = InputAmountDialog.getInstance(ID_NUM_SHARES,
                        mStock.getNumberOfShares().doubleValue());
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextViewFontIcon numSharesView = (RobotoTextViewFontIcon) this.findViewById(R.id.numSharesView);
        numSharesView.setOnClickListener(onAmountClick);
        // todo: format the number of shares based on selected locale.
        showNumberOfShares();
    }

    private void initPurchasePriceControls() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo: use currency
                InputAmountDialog dialog = InputAmountDialog.getInstance(ID_PURCHASE_PRICE,
                        mStock.getPurchasePrice().doubleValue());
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.purchasePriceView);
        view.setOnClickListener(onAmountClick);
        // todo: format the number of shares based on selected locale.
        showPurchasePrice();
    }

    private void showCommission() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.commissionView);
        view.setText(mStock.getCommission().toString());
    }

    private void showCurrentPrice() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.currentPriceView);
        view.setText(mStock.getCurrentPrice().toString());
    }

    private void showNumberOfShares() {
        RobotoTextViewFontIcon view = (RobotoTextViewFontIcon) this.findViewById(R.id.numSharesView);
        view.setText(mStock.getNumberOfShares().toString());
    }

    private void showPurchasePrice() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.purchasePriceView);
        view.setText(mStock.getPurchasePrice().toString());
    }

    private void showValue() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.valueView);
        view.setText(mStock.getValue().toString());
    }
}
