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
package com.money.manager.ex.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.bundlers.MoneyBundler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.Currency;
import com.shamanland.fonticon.FontIconView;

import net.objecthunter.exp4j.ExpressionBuilder;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import icepick.Icepick;
import icepick.State;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class AmountInputDialog
    extends DialogFragment {

    private static final String KEY_REQUEST_ID = "AmountInputDialog:Id";
    private static final String KEY_AMOUNT = "AmountInputDialog:Amount";
    private static final String KEY_CURRENCY_ID = "AmountInputDialog:CurrencyId";
    private static final String KEY_EXPRESSION = "AmountInputDialog:Expression";
    private static final String ARG_ROUNDING = "AmountInputDialog:Rounding";

    public static AmountInputDialog getInstance(int requestId, Money amount) {
        String requestIdString = Integer.toString(requestId);
        return getInstance(requestIdString, amount, null, false);
    }

    public static AmountInputDialog getInstance(String requestId, Money amount) {
        return getInstance(requestId, amount, null, false);
    }

    public static AmountInputDialog getInstance(int requestId, Money amount, int currencyId) {
        String requestIdString = Integer.toString(requestId);
        return getInstance(requestIdString, amount, currencyId, true);
    }

    public static AmountInputDialog getInstance(String requestId, Money amount, int currencyId) {
        return getInstance(requestId, amount, currencyId, true);
    }

    public static AmountInputDialog getInstance(int requestId, Money amount, Integer currencyId,
                                                boolean roundToCurrencyDecimals) {
        String requestIdString = Integer.toString(requestId);
        return getInstance(requestIdString, amount, currencyId, roundToCurrencyDecimals);
    }

    public static AmountInputDialog getInstance(String requestId, Money amount, Integer currencyId,
                                                boolean roundToCurrencyDecimals) {
        Bundle args = new Bundle();
        args.putString(KEY_REQUEST_ID, requestId);
        String amountString = amount == null ? "0" : amount.toString();
        args.putString(KEY_AMOUNT, amountString);

        if (currencyId == null) currencyId = Constants.NOT_SET;
        args.putInt(KEY_CURRENCY_ID, currencyId);
        args.putBoolean(ARG_ROUNDING, roundToCurrencyDecimals);

        AmountInputDialog dialog = new AmountInputDialog();
        dialog.setArguments(args);

        return dialog;
    }


    /**
     * By default, round the number to the currency Scale. Set in the factory method.
     */
    public boolean roundToCurrencyDecimals;

    private int[] idButtonKeyNum = {
            R.id.buttonKeyNum0, R.id.buttonKeyNum1, R.id.buttonKeyNum2, R.id.buttonKeyNum3,
            R.id.buttonKeyNum4, R.id.buttonKeyNum5, R.id.buttonKeyNum6, R.id.buttonKeyNum7,
            R.id.buttonKeyNum8, R.id.buttonKeyNum9,
            R.id.buttonKeyNumDecimal,
    };
    private int[] idOperatorKeys = {
            R.id.buttonKeyAdd, R.id.buttonKeyDiv,
            R.id.buttonKeyLess, R.id.buttonKeyMultiplication,
            R.id.buttonKeyLeftParenthesis, R.id.buttonKeyRightParenthesis
    };

    private String mRequestId;
    @State(MoneyBundler.class) Money mAmount;
    private Integer mCurrencyId;
    private Integer mDefaultColor;
    private TextView txtMain, txtTop;
    private CurrencyService mCurrencyService;
    /**
     * used to restore expression from saved instance state.
     */
    private String mExpression;
    /**
     * Indicates that the user has already started typing. We should not replace the existing number
     * with the typed value but append the typed value to the existing number.
     */
    private boolean mStartedTyping = false;
    private FormatUtilities formatUtilities;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrencyService = new CurrencyService(getContext());
        this.formatUtilities = new FormatUtilities(getActivity());

        // get arguments
        restoreArguments();

        if (savedInstanceState != null) {
            restoreSavedInstanceState(savedInstanceState);
        } else {
            initializeNewDialog();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        View view = inflater.inflate(R.layout.input_amount_dialog, null);

        // set the decimal separator according to the locale
        setDecimalSeparator(view);

        // Numbers and Operators.
        OnClickListener numberClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset prior value/text (in some cases only).
                String existingValue = txtMain.getText().toString();
                if (!mStartedTyping) {
                    existingValue = "";
                    mStartedTyping = true;
                }

                txtMain.setText(existingValue.concat(((Button) v).getText().toString()));
                evalExpression();
            }
        };
        for (int id : idButtonKeyNum) {
            Button button = (Button) view.findViewById(id);
            button.setOnClickListener(numberClickListener);
        }

        OnClickListener operatorClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingValue = txtMain.getText().toString();
                mStartedTyping = true;

                txtMain.setText(existingValue.concat(((Button) v).getText().toString()));
                evalExpression();
            }
        };
        for (int id : idOperatorKeys) {
            Button button = (Button) view.findViewById(id);
            button.setOnClickListener(operatorClickListener);
        }

        // Clear button. 'C'
        Button clearButton = (Button) view.findViewById(R.id.buttonKeyClear);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartedTyping = true;
                txtMain.setText("");
                evalExpression();
            }
        });

        // Equals button '='
        Button buttonKeyEquals = (Button) view.findViewById(R.id.buttonKeyEqual);
        buttonKeyEquals.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is called only to reset the warning colour in the top box, if any.
                evalExpression();
                showAmountInEntryField();
            }
        });

        // Delete button '<='
        FontIconView deleteButton = (FontIconView) view.findViewById(R.id.deleteButton);
        if (deleteButton != null) {
            deleteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mStartedTyping = true;

                    String currentNumber = txtMain.getText().toString();
                    currentNumber = deleteLastCharacterFrom(currentNumber);
                    txtMain.setText(currentNumber);

                    evalExpression();
                }
            });
        }

        // Amounts
        txtTop = (TextView) view.findViewById(R.id.textViewTop);
        mDefaultColor = txtTop.getCurrentTextColor();

        txtMain = (TextView) view.findViewById(R.id.textViewMain);
        if (!TextUtils.isEmpty(mExpression)) {
            txtMain.setText(mExpression);
        } else {
            showAmountInEntryField();
        }

        // evaluate the expression initially, in case there is an existing amount passed to the binaryDialog.
        evalExpression();

        // Dialog

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
            .customView(view, false)
            .cancelable(false)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    if (!evalExpression()) return;

                    EventBus.getDefault().post(new AmountEnteredEvent(mRequestId, getAmount()));

                    dialog.dismiss();
                }
            })
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            })
            .autoDismiss(false)
            .negativeText(android.R.string.cancel)
            .positiveText(android.R.string.ok);

        return builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayFormattedAmount();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (mCurrencyId != null) savedInstanceState.putInt(KEY_CURRENCY_ID, mCurrencyId);
        savedInstanceState.putString(KEY_REQUEST_ID, mRequestId);

        mExpression = txtMain.getText().toString();
        savedInstanceState.putString(KEY_EXPRESSION, mExpression);

        Icepick.saveInstanceState(this, savedInstanceState);
    }

    // methods

    /**
     * Displays the expression result in the top text box. This is a formatted number in the
     * given currency.
     */
    public void displayFormattedAmount() {
        String result = getFormattedAmount();

        txtTop.setText(result);
    }

    /**
     * Evaluate the entered expression and recalculate the resulting amount.
     * @return Boolean indicating whether the operation was successful or not.
     */
    public boolean evalExpression() {
        String exp = txtMain.getText().toString();
        NumericHelper helper = new NumericHelper(getContext());

        exp = helper.cleanUpNumberString(exp);

        if (exp.length() > 0) {
            try {
                Double result = new ExpressionBuilder(exp).build().evaluate();

                int precision = getPrecision();
                mAmount = MoneyFactory.fromString(Double.toString(result))
                        .truncate(precision);
            } catch (IllegalArgumentException ex) {
                // Just display the last valid value.
                displayFormattedAmount();
                // Use the warning colour.
                txtTop.setTextColor(ContextCompat.getColor(getContext(), R.color.material_amber_800));

                return false;
            } catch (Exception e) {
                Timber.e(e, "evaluating expression");
            }
        } else {
            mAmount = MoneyFactory.fromString("0");
        }

        displayFormattedAmount();
        txtTop.setTextColor(mDefaultColor);
        return true;
    }

    /**
     * Get amount formatted in the formatting currency.
     * @return String Amount formatted in the given currency.
     */
    public String getFormattedAmount() {
        String result = null;
        FormatUtilities format = new FormatUtilities(getActivity());

        // No currency. Use locale preferences.
        if (mCurrencyId == null) {
            result = format.formatWithLocale(mAmount);
        }

        // Use currency preferences but ignore the decimals.
        if (!getRoundToCurrencyDecimals()) {
            // ignore the currency preferences but show the symbol.
            result = format.formatNumberIgnoreDecimalCount(mAmount, mCurrencyId);
        }

        // default format, use currency preferences.
        if (result == null) {
            result = mCurrencyService.getCurrencyFormatted(mCurrencyId, mAmount);
        }

        return result;
    }

    // private

    private int getPrecision() {
        // if using a currency and currency precision is required, use that.
        if (!this.roundToCurrencyDecimals || this.mCurrencyId == null) return Constants.DEFAULT_PRECISION;

        Currency currency = this.mCurrencyService.getCurrency(mCurrencyId);
        if (currency == null) return Constants.DEFAULT_PRECISION;

        // get precision from the currency
        NumericHelper helper = new NumericHelper(getActivity());
        return helper.getNumberOfDecimals(currency.getScale());
    }

    private String deleteLastCharacterFrom(String number) {
        // check length
        if (number.length() <= 0) return number;

        // first cut-off the last digit
        number = number.substring(0, number.length() - 1);

        // Should we check if the next character is the decimal separator. (?)

        // Handle deleting the last number - set the remaining value to 0.
        if (TextUtils.isEmpty(number)) {
            number = "0";
        }

        return number;
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(KEY_REQUEST_ID)) {
            mRequestId = savedInstanceState.getString(KEY_REQUEST_ID);
        }
//        if (savedInstanceState.containsKey(KEY_AMOUNT)) {
//            mAmount = MoneyFactory.fromString(savedInstanceState.getString(KEY_AMOUNT));
//        }
        if (savedInstanceState.containsKey(KEY_CURRENCY_ID)) {
            mCurrencyId = savedInstanceState.getInt(KEY_CURRENCY_ID);
        }
        if (savedInstanceState.containsKey(KEY_EXPRESSION)) {
            mExpression = savedInstanceState.getString(KEY_EXPRESSION);
        }

        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    /**
     * Set the decimal separator to the base currency's separator.
     * @param view current view
     */
    private void setDecimalSeparator(View view) {
        Button separatorButton = (Button) view.findViewById(R.id.buttonKeyNumDecimal);

        String separator = this.formatUtilities.getDecimalSeparatorForAppLocale();

        separatorButton.setText(separator);
    }

    private boolean isCurrencySet() {
        return this.mCurrencyId != null && this.mCurrencyId != Constants.NOT_SET;
    }

    private Money getAmount() {
        Money result;

        // to round or not? Handle case when no base currency set.
        if (this.roundToCurrencyDecimals && isCurrencySet()) {
            NumericHelper numericHelper = new NumericHelper(getContext());
            Currency currency = mCurrencyService.getCurrency(mCurrencyId);
            result = numericHelper.truncateToCurrency(mAmount, currency);
        } else {
            result = mAmount;
        }
        return result;
    }

    private String getFormattedAmountForEditing(Money amount) {
        if (amount == null) return "0";

        String result;
        Currency displayCurrency = mCurrencyService.getCurrency(mCurrencyId);

        if (displayCurrency != null) {
            if(getRoundToCurrencyDecimals()) {
                // use decimals from the display currency.
                // but decimal and group separators from the base currency.
                result = formatUtilities.format(amount, displayCurrency.getScale(),
                    formatUtilities.getDecimalSeparatorForAppLocale(),
                    formatUtilities.getGroupingSeparatorForAppLocale());

            } else {
                // Use default precision and no currency markup.
                result = formatUtilities.formatNumber(amount, Constants.DEFAULT_PRECISION,
                    displayCurrency.getDecimalSeparator(), displayCurrency.getGroupSeparator(),
                    null, null);
            }
        } else {
            result = formatUtilities.formatWithLocale(amount);
        }

        return result;
    }

    private boolean getRoundToCurrencyDecimals() {
        return getArguments().getBoolean(ARG_ROUNDING);
    }

    private void initializeNewDialog() {
        // not in restored state. new binaryDialog

        // Display the existing amount, if any has been passed into the binaryDialog.
        NumericHelper numericHelper = new NumericHelper(getContext());
        Currency currency = mCurrencyService.getCurrency(mCurrencyId);

        Money amount = MoneyFactory.fromString(getArguments().getString(KEY_AMOUNT));
        if (currency != null && this.roundToCurrencyDecimals) {
            mAmount = numericHelper.truncateToCurrency(amount, currency);
        } else {
            // no currency and no base currency set.
            mAmount = amount;
        }
    }

    private void restoreArguments() {
        Bundle args = getArguments();

        this.mRequestId = args.getString(KEY_REQUEST_ID);
        this.mCurrencyId = args.getInt(KEY_CURRENCY_ID);
        this.roundToCurrencyDecimals = args.getBoolean(ARG_ROUNDING);
    }

    private void showAmountInEntryField() {
        // Get the calculated amount in default locale and display in the main box.
        String amount = getFormattedAmountForEditing(mAmount);
        txtMain.setText(amount);
    }

}
