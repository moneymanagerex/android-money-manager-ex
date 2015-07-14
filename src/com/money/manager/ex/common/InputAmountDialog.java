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
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyUtils;
import com.money.manager.ex.utils.MathUtils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.apache.commons.lang3.math.NumberUtils;

public class InputAmountDialog
        extends DialogFragment {

    private static final String KEY_ID_VIEW = "InputAmountDialog:Id";
    private static final String KEY_AMOUNT = "InputAmountDialog:Amount";
    private static final String KEY_CURRENCY_ID = "InputAmountDialog:CurrencyId";
    private static final String KEY_EXPRESSION = "InputAmountDialog:Expression";

    // arrays id keynum button
    private int[] idButtonKeyNum = {
            R.id.buttonKeyNum0, R.id.buttonKeyNum1, R.id.buttonKeyNum2, R.id.buttonKeyNum3,
            R.id.buttonKeyNum4, R.id.buttonKeyNum5, R.id.buttonKeyNum6, R.id.buttonKeyNum7,
            R.id.buttonKeyNum8,
            R.id.buttonKeyNum9, R.id.buttonKeyNumDecimal, R.id.buttonKeyAdd, R.id.buttonKeyDiv,
            R.id.buttonKeyLess, R.id.buttonKeyMultiplication, R.id.buttonKeyLeftParenthesis,
            R.id.buttonKeyRightParenthesis};

    private int mIdView;
    private String mAmount = Constants.EMPTY_STRING;
    private String mExpression = null;
    private Integer mCurrencyId, mDefaultColor;
    private TextView txtMain, txtTop;
    private IInputAmountDialogListener mListener;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            restoreSavedInstanceState(savedInstanceState);
            return;
        }

        int id = getArguments().getInt("id");
        Double amount = MathUtils.Round(getArguments().getDouble("amount"), 2);
        mIdView = id;
        if (amount != 0) {
            int iAmount = (int) (amount * 100);
            if (Math.abs(amount - (iAmount / 100)) == 0) {
                mAmount = Integer.toString(iAmount / 100);
            } else {
                mAmount = Double.toString(amount);
            }
            mExpression = mAmount;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        View view = inflater.inflate(R.layout.input_amount_dialog, null);

        // create listener
        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTop.setText(txtTop.getText().toString()
                        .concat(((Button) v).getText().toString()));
//                evalExpression();
            }
        };
        // reference button click listener
        for (int id : idButtonKeyNum) {
            Button button = (Button) view.findViewById(id);
            button.setOnClickListener(clickListener);
        }

        // Clear button.
        Button clearButton = (Button) view.findViewById(R.id.buttonKeyClear);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                txtTop.setText("");
            }
        });

        // button equals
        Button buttonKeyEquals = (Button) view.findViewById(R.id.buttonKeyEqual);
        buttonKeyEquals.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                evalExpression();
            }
        });

        // image button delete
        ImageButton imgDelete = (ImageButton) view.findViewById(R.id.imageButtonCancel);
        imgDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String currentNumber = txtTop.getText().toString();
                if (currentNumber.length() > 0) {
                    currentNumber = deleteLastDigitFrom(currentNumber);
                    txtTop.setText(currentNumber);
                }
            }
        });

        // Amounts
        txtMain = (TextView) view.findViewById(R.id.textViewMain);
        mDefaultColor = txtMain.getCurrentTextColor();

        txtTop = (TextView) view.findViewById(R.id.textViewTop);
        if (!TextUtils.isEmpty(mExpression)) {
            txtTop.setText(mExpression);
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(view, false);
        builder.cancelable(false);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                if (!evalExpression()) return;

                if (TextUtils.isEmpty(mAmount)) {
                    mAmount = Double.toString(0);
                }
                // check if is double
                if (NumberUtils.isNumber(mAmount) && mListener != null) {
                    double result = MathUtils.Round(Double.parseDouble(mAmount), 2);
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
        refreshAmount();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(KEY_AMOUNT, mAmount);
        if (mCurrencyId != null) savedInstanceState.putInt(KEY_CURRENCY_ID, mCurrencyId);
        savedInstanceState.putInt(KEY_ID_VIEW, mIdView);
        savedInstanceState.putString(KEY_EXPRESSION, txtTop.getText().toString());
    }

    public void refreshAmount() {
        String amount = mAmount;

        // check if amount is not empty and is double
        if (TextUtils.isEmpty(amount)) {
            amount = Double.toString(0);
        }

        if (NumberUtils.isNumber(amount)) {
            double fAmount = Double.parseDouble(amount);

            CurrencyUtils currencyUtils = new CurrencyUtils(getActivity().getApplicationContext());

            if (mCurrencyId == null) {
                txtMain.setText(currencyUtils.getBaseCurrencyFormatted(fAmount));
            } else {
                txtMain.setText(currencyUtils.getCurrencyFormatted(mCurrencyId, fAmount));
            }
        }
    }

    public boolean evalExpression() {
        String exp = txtTop.getText().toString();
        if (exp.length() > 0) {
            try {
                Expression e = new ExpressionBuilder(exp).build();
                double result = e.evaluate();
                mAmount = Double.toString(result);
                refreshAmount();
                txtMain.setTextColor(mDefaultColor);
                return true;
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
                handler.handle(e, "evaluating expression");

                txtMain.setText(R.string.invalid_expression);
                txtMain.setTextColor(getResources().getColor(R.color.material_red_700));
                return false;
            }
        }
        return true;
    }

    private String deleteLastDigitFrom(String number) {
        // first cut-off the last digit
        number = number.substring(0, number.length() - 1);

        // Should we check if the next character is the decimal separator. (?)

        return number;
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(KEY_AMOUNT)) {
            mAmount = savedInstanceState.getString(KEY_AMOUNT);
        }
        if (savedInstanceState.containsKey(KEY_CURRENCY_ID))
            mCurrencyId = savedInstanceState.getInt(KEY_CURRENCY_ID);
        if (savedInstanceState.containsKey(KEY_ID_VIEW))
            mIdView = savedInstanceState.getInt(KEY_ID_VIEW);
        if (savedInstanceState.containsKey(KEY_EXPRESSION))
            mExpression = savedInstanceState.getString(KEY_EXPRESSION);
    }
}
