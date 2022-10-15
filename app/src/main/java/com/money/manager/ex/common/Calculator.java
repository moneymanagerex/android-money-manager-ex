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

package com.money.manager.ex.common;

import android.content.Intent;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Helper for calculator (numeric input).
 */

public final class Calculator {

    /*
        Static / Factory methods.
     */

    public static Calculator forActivity(FragmentActivity activity) {
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

    private Calculator(FragmentActivity activity) {
        this.activity = activity;
        this.intent = new Intent(this.activity, CalculatorActivity.class);
    }

    private FragmentActivity activity;
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
