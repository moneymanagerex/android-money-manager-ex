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

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class InputAmountDialog
        extends DialogFragment {

    private static final String KEY_ID_VIEW = "InputAmountDialog:Id";
    private static final String KEY_AMOUNT = "InputAmountDialog:Amount";
    private static final String KEY_CURRENCY_ID = "InputAmountDialog:CurrencyId";
    private static final String KEY_EXPRESSION = "InputAmountDialog:Expression";

    public static InputAmountDialog getInstance(int id, Double amount) {
        return getInstance(id, amount, null);
    }

    public static InputAmountDialog getInstance(int id, Double amount, Integer currencyId) {
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putDouble("amount", amount);

        InputAmountDialog dialog = new InputAmountDialog();
        dialog.setArguments(args);
        dialog.mDisplayCurrencyId = currencyId;

        return dialog;
    }

    public boolean roundToCurrencyDecimals = true;

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

    private int mIdView;
    private BigDecimal mAmount;
    private Integer mDisplayCurrencyId;
    private Integer mDefaultColor;
    private TextView txtMain, txtTop;
    private IInputAmountDialogListener mListener;
    private CurrencyService mCurrencyService;
    /**
     * used to restore expression from saved instance state.
     */
    private String mExpression;
    private boolean mStartedTyping = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() instanceof IInputAmountDialogListener) {
            mListener = (IInputAmountDialogListener) getActivity();
        }
        if (getTargetFragment() instanceof IInputAmountDialogListener) {
            mListener = (IInputAmountDialogListener) getTargetFragment();
        }

        if (mListener == null) {
            throw new IllegalStateException("Need IInputAmountDialogListener. Implement in Activity" +
                    " or assign a TargetFragment which implements the interface.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrencyService = new CurrencyService(getContext());
        // Use the default currency if none sent.
        if (mDisplayCurrencyId == null) {
            mDisplayCurrencyId = mCurrencyService.getBaseCurrencyId();
        }

        if (savedInstanceState != null) {
            restoreSavedInstanceState(savedInstanceState);
//            return;
        } else {
            mIdView = getArguments().getInt("id");
            // Display the existing amount, if any has been passed into the dialog.
            NumericHelper numericHelper = new NumericHelper(getContext());
            TableCurrencyFormats currency = mCurrencyService.getCurrency(mDisplayCurrencyId);
            if (currency != null) {
                // no currency and no base currency set.
                int decimals = numericHelper.getNumberOfDecimals(currency.getScale());
                mAmount = this.roundToCurrencyDecimals
                        ? BigDecimal.valueOf(MathUtils.Round(getArguments().getDouble("amount"), decimals))
                        : BigDecimal.valueOf(getArguments().getDouble("amount"));
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        View view = inflater.inflate(R.layout.input_amount_dialog, null);

        // set the decimal separator to the currency's separator
        setDecimalSeparator(view);

        // Numbers and Operators.
        OnClickListener numberClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the default 0 value to avoid leading zero "01" numbers.
                // Reset prior value/text if nothing was entered (and there is no prior value?).
                String existingValue = txtMain.getText().toString();
                if (!mStartedTyping) {
                    // && mAmount.compareTo(BigDecimal.ZERO) == 0 ?
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
        if (!StringUtils.isEmpty(mExpression)) {
            txtMain.setText(mExpression);
        } else {
            showAmountInEntryField();
        }

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
                    BigDecimal result = getAmount();

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

        return builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFormattedAmount();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putDouble(KEY_AMOUNT, mAmount.doubleValue());
        if (mDisplayCurrencyId != null) savedInstanceState.putInt(KEY_CURRENCY_ID, mDisplayCurrencyId);
        savedInstanceState.putInt(KEY_ID_VIEW, mIdView);

        mExpression = txtMain.getText().toString();
        savedInstanceState.putString(KEY_EXPRESSION, mExpression);
    }

    /**
     * Displays the expression result in the top text box. This is a formatted number in the
     * given currency.
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
        NumericHelper helper = new NumericHelper(getContext());

        exp = helper.cleanUpNumberString(exp);

        if (exp.length() > 0) {
            try {
                Expression e = new ExpressionBuilder(exp).build();
                mAmount = BigDecimal.valueOf(e.evaluate());
            } catch (IllegalArgumentException ex) {
                // Just display the last valid value.
                refreshFormattedAmount();
                // Use the warning colour.
                txtTop.setTextColor(getResources().getColor(R.color.material_amber_800));

                return false;
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
                handler.handle(e, "evaluating expression");
            }
        } else {
            mAmount = BigDecimal.valueOf(0);
        }

        refreshFormattedAmount();
        txtTop.setTextColor(mDefaultColor);
        return true;
    }

    /**
     * Get amount formatted in the formatting currency.
     * @return String Amount formatted in the given currency.
     */
    public String getFormattedAmount() {
        double amount = mAmount.doubleValue();

        String result;

        if (mDisplayCurrencyId == null) {
            result = mCurrencyService.getBaseCurrencyFormatted(amount);
        } else {
            result = mCurrencyService.getCurrencyFormatted(mDisplayCurrencyId, amount);
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
            mAmount = BigDecimal.valueOf(savedInstanceState.getDouble(KEY_AMOUNT));
        }
        if (savedInstanceState.containsKey(KEY_CURRENCY_ID))
            mDisplayCurrencyId = savedInstanceState.getInt(KEY_CURRENCY_ID);
        if (savedInstanceState.containsKey(KEY_ID_VIEW))
            mIdView = savedInstanceState.getInt(KEY_ID_VIEW);
        if (savedInstanceState.containsKey(KEY_EXPRESSION)) {
            mExpression = savedInstanceState.getString(KEY_EXPRESSION);
        }
    }

    /**
     * Set the decimal separator to the base currency's separator.
     * @param view current view
     */
    private void setDecimalSeparator(View view) {
        Button separatorButton = (Button) view.findViewById(R.id.buttonKeyNumDecimal);

        NumericHelper helper = new NumericHelper(getContext());
        String separator = helper.getDecimalSeparatorForCurrency(mCurrencyService.getBaseCurrencyId());

        separatorButton.setText(separator);
    }

//    private int getCurrencyIdInUse() {
//        return mDisplayCurrencyId == null
//                ? mCurrencyService.getBaseCurrencyId()
//                : mDisplayCurrencyId;
//    }

    private BigDecimal getAmount() {
        double result;

        // to round or not?
        if (InputAmountDialog.this.roundToCurrencyDecimals) {
            NumericHelper numericHelper = new NumericHelper(getContext());
            int decimals = numericHelper.getNumberOfDecimals(
                    mCurrencyService.getCurrency(mDisplayCurrencyId).getScale());
            result = numericHelper.roundNumber(mAmount.doubleValue(), decimals);
        } else {
            result = mAmount.doubleValue();
        }
        return BigDecimal.valueOf(result);
    }

    private String getAmountInUserLocale() {
        NumericHelper helper = new NumericHelper(getContext());

        // Output currency. Used for scale/precision (number of decimal places).
        TableCurrencyFormats currency = mCurrencyService.getBaseCurrency();
        if (currency == null) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.showMessage(getString(R.string.base_currency_not_set));
            return "";
        }

        String result;
        if(this.roundToCurrencyDecimals) {
            result = helper.getNumberFormatted(mAmount.doubleValue(), currency);
        } else {
            // get number of decimals from the current value.
            result = mAmount.toString();
        }

        return result;
    }

    private void showAmountInEntryField() {
        // Get the calculated amount in default locale and display in the main box.
        String amount = getAmountInUserLocale();
        txtMain.setText(amount);
    }

}
