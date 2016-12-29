package com.money.manager.ex.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Helper for calculator (numeric input).
 */

public final class Calculator {

    /*
        Static / Factory methods.
     */

    public static Calculator forActivity(Activity activity) {
        Calculator calculator = new Calculator(activity);

        return calculator;
    }

    public static Calculator forFragment(Fragment fragment) {
        Calculator calculator = new Calculator(fragment.getActivity());
        calculator.fragment = fragment;

        return calculator;
    }

    /**
     * Extracts the entered amount in onActivityResult.
     * @return Amount entered
     */
    public static Money getAmountFromResult(Intent data) {
        if (data == null) return null;

        String stringExtra = data.getStringExtra(CalculatorActivity.RESULT_AMOUNT);
        if (TextUtils.isEmpty(stringExtra)) return null;

        return MoneyFactory.fromString(stringExtra);
    }

    /*
        Instance
     */

    private Calculator(Activity activity) {
        this.activity = activity;
        this.intent = new Intent(this.activity, CalculatorActivity.class);
    }

    private Activity activity;
    private Fragment fragment;
    private Intent intent;

    public void show(int requestCode) {
        if (fragment == null) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            fragment.startActivityForResult(intent, requestCode);
        }
    }

    public Calculator currency(int currencyId) {
        intent.putExtra(CalculatorActivity.EXTRA_CURRENCY_ID, currencyId);
        return this;
    }

    public Calculator amount(Money amount) {
        intent.putExtra(CalculatorActivity.EXTRA_AMOUNT, amount.toString());
        return this;
    }

    public Calculator roundToCurrency(boolean value) {
        intent.putExtra(CalculatorActivity.EXTRA_ROUND_TO_CURRENCY, value);
        return this;
    }
}
