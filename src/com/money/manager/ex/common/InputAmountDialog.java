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
package com.money.manager.ex.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.utils.MathUtils;
import com.shamanland.fonticon.FontIconView;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class InputAmountDialog
        extends DialogFragment {

    private static final String KEY_ID_VIEW = "InputAmountDialog:Id";
    private static final String KEY_AMOUNT = "InputAmountDialog:Amount";
    private static final String KEY_CURRENCY_ID = "InputAmountDialog:CurrencyId";
    private static final String KEY_EXPRESSION = "InputAmountDialog:Expression";

    public static InputAmountDialog getInstance(IInputAmountDialogListener listener, int id, Double amount) {
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putDouble("amount", amount);

        InputAmountDialog fragment = new InputAmountDialog();
        fragment.setArguments(args);
        fragment.mListener = listener;

        return fragment;
    }

    public static InputAmountDialog getInstance(IInputAmountDialogListener listener, int id,
                                                Double amount, Integer currencyId) {
        InputAmountDialog dialog = getInstance(listener, id, amount);

        dialog.mCurrencyId = currencyId;
        dialog.mListener = listener;
        return dialog;
    }

    public boolean RoundToCurrencyDecimals = true;

    // arrays id keynum button
    private int[] idButtonKeyNum = {
            R.id.buttonKeyNum0, R.id.buttonKeyNum1, R.id.buttonKeyNum2, R.id.buttonKeyNum3,
            R.id.buttonKeyNum4, R.id.buttonKeyNum5, R.id.buttonKeyNum6, R.id.buttonKeyNum7,
            R.id.buttonKeyNum8,
            R.id.buttonKeyNum9, R.id.buttonKeyNumDecimal, R.id.buttonKeyAdd, R.id.buttonKeyDiv,
            R.id.buttonKeyLess, R.id.buttonKeyMultiplication, R.id.buttonKeyLeftParenthesis,
            R.id.buttonKeyRightParenthesis};

    private int mIdView;
    private double mAmount;
    private String mExpression = null;
    private Integer mCurrencyId, mDefaultColor;
    private TextView txtMain, txtTop;
    private IInputAmountDialogListener mListener;
    private CurrencyService mCurrencyService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            restoreSavedInstanceState(savedInstanceState);
            return;
        }

        mIdView = getArguments().getInt("id");

        mCurrencyService = new CurrencyService(getActivity());
        if (mCurrencyId == null) {
            mCurrencyId = mCurrencyService.getBaseCurrencyId();
        }

        // Display the existing amount, if any has been passed into the dialog.
        NumericHelper numericHelper = new NumericHelper();
        int decimals = numericHelper.getNumberOfDecimals(
                mCurrencyService.getCurrency(mCurrencyId).getScale());
        Double amount = this.RoundToCurrencyDecimals
                ? MathUtils.Round(getArguments().getDouble("amount"), decimals)
                : getArguments().getDouble("amount");

        if (amount != 0) {
            mExpression = Double.toString(amount);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        View view = inflater.inflate(R.layout.input_amount_dialog, null);

        // set the decimal separator to the currency's separator
        setDecimalSeparator(view);

        // Numbers and Operators.
        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the default 0 value to avoid leading zero "01" numbers.
                String existingValue = txtMain.getText().toString();
                if (existingValue.equals("0")) {
                    existingValue = "";
                }

                txtMain.setText(existingValue
                        .concat(((Button) v).getText().toString()));
                evalExpression();
            }
        };
        // reference button click listener
        for (int id : idButtonKeyNum) {
            Button button = (Button) view.findViewById(id);
            button.setOnClickListener(clickListener);
        }

        // Clear button. 'C'
        Button clearButton = (Button) view.findViewById(R.id.buttonKeyClear);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
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
                // Get the calculated amount in default locale and display in the main box.
                String amount = getAmountInUserLocale();
                txtMain.setText(amount);
            }
        });

        // Delete button '<='
        FontIconView deleteButton = (FontIconView) view.findViewById(R.id.deleteButton);
        if (deleteButton != null) {
            deleteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String currentNumber = txtMain.getText().toString();
                    currentNumber = deleteLastDigitFrom(currentNumber);
                    txtMain.setText(currentNumber);

                    evalExpression();
                }
            });
        }

        // Amounts
        txtTop = (TextView) view.findViewById(R.id.textViewTop);
        mDefaultColor = txtTop.getCurrentTextColor();

        txtMain = (TextView) view.findViewById(R.id.textViewMain);
        txtMain.setText(mExpression);

        // evaluate the expression initially, in case there is an existing amount passed to the dialog.
        evalExpression();

        // Dialog

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(view, false);
        builder.cancelable(false);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                if (!evalExpression()) return;

                if (mListener != null) {
                    double result = getAmount();

                    mListener.onFinishedInputAmountDialog(mIdView, result);

                    dialog.dismiss();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                dialog.dismiss();
            }
        });
        builder.autoDismiss(false);
        builder.negativeText(android.R.string.cancel);
        builder.positiveText(android.R.string.ok);

        Dialog dialog = builder.show();

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFormattedAmount();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putDouble(KEY_AMOUNT, mAmount);
        if (mCurrencyId != null) savedInstanceState.putInt(KEY_CURRENCY_ID, mCurrencyId);
        savedInstanceState.putInt(KEY_ID_VIEW, mIdView);
        savedInstanceState.putString(KEY_EXPRESSION, txtMain.getText().toString());
    }

    /**
     * Displays the expression result in the top text box.
     */
    public void refreshFormattedAmount() {
        String result = getFormattedAmount();

        txtTop.setText(result);
    }

    /**
     * Evaluate the entered expression and recalculate the resulting amount.
     * @return Boolean indicating whether the operation was successful or not.
     */
    public boolean evalExpression() {
        String exp = txtMain.getText().toString();
        // replace any blanks
        exp = exp.replace(" ", "");

        if (exp.length() > 0) {
            try {
                Expression e = new ExpressionBuilder(exp).build();
                mAmount = e.evaluate();

                refreshFormattedAmount();
                txtTop.setTextColor(mDefaultColor);
                return true;
            } catch (IllegalArgumentException ex) {
//                txtTop.setText(R.string.invalid_expression);
//                txtTop.setTextColor(getResources().getColor(R.color.material_red_700));
                // Just display the last valid value.
                refreshFormattedAmount();
                // Use the warning colour.
                txtTop.setTextColor(getResources().getColor(R.color.material_amber_800));
                return false;
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
                handler.handle(e, "evaluating expression");
            }
        }
        return true;
    }

    /**
     * Get amount formatted in the given currency (or default).
     * @return String Amount formatted in the given or default currency.
     */
    public String getFormattedAmount() {
        double amount = mAmount;

        String result;

        if (mCurrencyId == null) {
            result = mCurrencyService.getBaseCurrencyFormatted(amount);
        } else {
            result = mCurrencyService.getCurrencyFormatted(mCurrencyId, amount);
        }

        return result;
    }

    private String deleteLastDigitFrom(String number) {
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
        if (savedInstanceState.containsKey(KEY_AMOUNT)) {
            mAmount = savedInstanceState.getDouble(KEY_AMOUNT);
        }
        if (savedInstanceState.containsKey(KEY_CURRENCY_ID))
            mCurrencyId = savedInstanceState.getInt(KEY_CURRENCY_ID);
        if (savedInstanceState.containsKey(KEY_ID_VIEW))
            mIdView = savedInstanceState.getInt(KEY_ID_VIEW);
        if (savedInstanceState.containsKey(KEY_EXPRESSION))
            mExpression = savedInstanceState.getString(KEY_EXPRESSION);
    }

    /**
     * Set the decimal separator to the current locale's separator.
     * It can not be the default currency's because we can not figure out in reverse
     * which locale this is. Example is €.
     * @param view
     */
    private void setDecimalSeparator(View view) {
        Locale locale = getResources().getConfiguration().locale;
        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
        char decimalSeparator = currencyFormatter.getDecimalFormatSymbols().getDecimalSeparator();
        String separator = Character.toString(decimalSeparator);

        Button separatorButton = (Button) view.findViewById(R.id.buttonKeyNumDecimal);

        separatorButton.setText(separator);
    }

    private double getAmount() {
        double result;

        // to round or not?
        if (InputAmountDialog.this.RoundToCurrencyDecimals) {
            NumericHelper numericHelper = new NumericHelper();
            int decimals = numericHelper.getNumberOfDecimals(
                    mCurrencyService.getCurrency(mCurrencyId).getScale());
            result = numericHelper.roundNumber(mAmount, decimals);
        } else {
            result = mAmount;
        }
        return result;
    }

    private String getAmountInUserLocale() {
        // User's locale. Used for decimal point and grouping separator.
        Locale locale = getResources().getConfiguration().locale;
        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
        String decimalPoint = Character.toString(currencyFormatter.getDecimalFormatSymbols().getDecimalSeparator());
        String groupSeparator = Character.toString(currencyFormatter.getDecimalFormatSymbols().getGroupingSeparator());

        // Output currency. Used for scale/precision (number of decimal places).
        TableCurrencyFormats currency = mCurrencyService.getCurrency(mCurrencyId);

        NumericHelper helper = new NumericHelper();
        String result = helper.getNumberFormatted(mAmount, currency.getScale(),
            decimalPoint, groupSeparator);
        return result;
    }
}
