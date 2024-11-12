/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.Currency;

import net.objecthunter.exp4j.ExpressionBuilder;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;

import dagger.Lazy;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Activity for the full-screen numeric input.
 * Additional functionality includes currency conversion.
 * The result is returned in onActivityResult in the calling activity.
 */

public class CalculatorActivity extends MmxBaseFragmentActivity {

    public static final String EXTRA_CURRENCY_ID = "CurrencyId";
    public static final String EXTRA_AMOUNT = "Amount";
    public static final String EXTRA_ROUND_TO_CURRENCY = "RoundToCurrencyDecimals";
    public static final String RESULT_AMOUNT = "AmountEntered";

    boolean roundToCurrencyDecimals;
    Money mAmount;
    Long mCurrencyId;
    String mExpression;

    // Views
    private ImageButton deleteButton;
    private TextView txtMain;
    private TextView txtTop;

    private final int[] idButtonKeyNum = {
            R.id.buttonKeyNum0, R.id.buttonKeyNum1, R.id.buttonKeyNum2, R.id.buttonKeyNum3,
            R.id.buttonKeyNum4, R.id.buttonKeyNum5, R.id.buttonKeyNum6, R.id.buttonKeyNum7,
            R.id.buttonKeyNum8, R.id.buttonKeyNum9,
            R.id.buttonKeyNumDecimal,
    };
    private final int[] idOperatorKeys = {
            R.id.buttonKeyAdd, R.id.buttonKeyDiv,
            R.id.buttonKeyLess, R.id.buttonKeyMultiplication,
            R.id.buttonKeyLeftParenthesis, R.id.buttonKeyRightParenthesis
    };

    @Inject CurrencyService mCurrencyService;
    @Inject Lazy<FormatUtilities> formatUtilitiesLazy;

    private Integer mDefaultColor;
    private boolean mStartedTyping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        // Inject dependencies
        MmexApplication.getApp().iocComponent.inject(this);

        // Initialize views
        deleteButton = findViewById(R.id.deleteButton);
        txtMain = findViewById(R.id.textViewMain);
        txtTop = findViewById(R.id.textViewTop);

        setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.enter_amount);

        if (savedInstanceState == null) {
            extractArguments();
        }

        initializeControls();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayFormattedAmount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuHelper(this, menu).addSaveToolbarIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == MenuHelper.save) {
            returnResult();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayFormattedAmount() {
        String result = getFormattedAmount();
        txtTop.setText(result);
    }

    public boolean evalExpression() {
        String exp = txtMain.getText().toString();
        NumericHelper helper = new NumericHelper(this);

        exp = helper.cleanUpNumberString(exp);

        if (exp.length() > 0) {
            try {
                Double result = new ExpressionBuilder(exp).build().evaluate();

                int precision = getPrecision();
                mAmount = MoneyFactory.fromString(Double.toString(result)).truncate(precision);
            } catch (IllegalArgumentException ex) {
                displayFormattedAmount();
                txtTop.setTextColor(getResources().getColor(R.color.material_amber_800));
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

    public String getFormattedAmount() {
        String result = null;
        FormatUtilities format = formatUtilitiesLazy.get();

        if (mCurrencyId == null) {
            result = format.formatWithLocale(mAmount);
        } else if (!roundToCurrencyDecimals) {
            result = format.formatNumberIgnoreDecimalCount(mAmount, mCurrencyId);
        } else {
            result = mCurrencyService.getCurrencyFormatted(mCurrencyId, mAmount);
        }

        return result;
    }

    private int getPrecision() {
        if (!this.roundToCurrencyDecimals || this.mCurrencyId == null) return Constants.DEFAULT_PRECISION;

        Currency currency = this.mCurrencyService.getCurrency(mCurrencyId);
        if (currency == null) return Constants.DEFAULT_PRECISION;

        NumericHelper helper = new NumericHelper(this);
        return helper.getNumberOfDecimals((int)currency.getScale());
    }

    private String deleteLastCharacterFrom(String number) {
        if (number.length() <= 0) return number;
        number = number.substring(0, number.length() - 1);
        if (TextUtils.isEmpty(number)) {
            number = "0";
        }
        return number;
    }

    private void extractArguments() {
        Intent intent = getIntent();
        if (intent == null) return;

        mCurrencyId = intent.getLongExtra(EXTRA_CURRENCY_ID, Constants.NOT_SET);
        roundToCurrencyDecimals = intent.getBooleanExtra(EXTRA_ROUND_TO_CURRENCY, true);

        String value = intent.getStringExtra(EXTRA_AMOUNT);
        if (!TextUtils.isEmpty(value)) {
            NumericHelper numericHelper = new NumericHelper(this);
            Currency currency = mCurrencyService.getCurrency(mCurrencyId);

            Money amount = MoneyFactory.fromString(value);

            if (currency != null && this.roundToCurrencyDecimals) {
                mAmount = numericHelper.truncateToCurrency(amount, currency);
            } else {
                mAmount = amount;
            }
        }
    }

    private void initializeControls() {
        setDecimalSeparator();

        View.OnClickListener numberClickListener = v -> {
            String existingValue = txtMain.getText().toString();
            if (!mStartedTyping) {
                existingValue = "";
                mStartedTyping = true;
            }
            txtMain.setText(existingValue.concat(((Button) v).getText().toString()));
            evalExpression();
        };
        for (int id : idButtonKeyNum) {
            Button button = findViewById(id);
            button.setOnClickListener(numberClickListener);
        }

        View.OnClickListener operatorClickListener = v -> {
            String existingValue = txtMain.getText().toString();
            mStartedTyping = true;
            txtMain.setText(existingValue.concat(((Button) v).getText().toString()));
            evalExpression();
        };
        for (int id : idOperatorKeys) {
            Button button = findViewById(id);
            button.setOnClickListener(operatorClickListener);
        }

        Button clearButton = findViewById(R.id.buttonKeyClear);
        clearButton.setOnClickListener(v -> {
            mStartedTyping = true;
            txtMain.setText("");
            evalExpression();
        });

        deleteButton.setOnClickListener( v -> {
            mStartedTyping = true;

            String currentNumber = txtMain.getText().toString();
            currentNumber = deleteLastCharacterFrom(currentNumber);
            txtMain.setText(currentNumber);

            evalExpression();
        });

        findViewById(R.id.buttonKeyEqual).setOnClickListener(v -> returnResult());

        UIHelper uiHelper = new UIHelper(this);
        deleteButton.setImageDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_backspace)
                .sizeDp(40)
                .color(uiHelper.getColor(R.color.md_primary)));

        mDefaultColor = txtTop.getCurrentTextColor();

        if (!TextUtils.isEmpty(mExpression)) {
            txtMain.setText(mExpression);
        } else {
            showAmountInEntryField();
        }

        evalExpression();
    }

    private void setDecimalSeparator() {
        Button decimalSeparatorButton = findViewById(R.id.buttonKeyNumDecimal);
        String separator = this.formatUtilitiesLazy.get().getDecimalSeparatorForAppLocale();
        decimalSeparatorButton.setText(separator);
    }

    private String getFormattedAmountForEditing(Money amount) {
        if (amount == null) return "0";

        String result;
        Currency displayCurrency = mCurrencyService.getCurrency(mCurrencyId);

        if (displayCurrency != null) {
            if (roundToCurrencyDecimals) {
                result = formatUtilitiesLazy.get().format(amount, displayCurrency.getScale(),
                        formatUtilitiesLazy.get().getDecimalSeparatorForAppLocale(),
                        formatUtilitiesLazy.get().getGroupingSeparatorForAppLocale());
            } else {
                result = formatUtilitiesLazy.get().formatNumber(amount, Constants.DEFAULT_PRECISION,
                        displayCurrency.getDecimalSeparator(), displayCurrency.getGroupSeparator(),
                        null, null);
            }
        } else {
            result = formatUtilitiesLazy.get().format(amount, Constants.PRICE_FORMAT);
        }

        return result;
    }

    private void returnResult() {
        evalExpression();
        showAmountInEntryField();

        Intent result = new Intent();
        result.putExtra(RESULT_AMOUNT, mAmount.toString());
        setResult(AppCompatActivity.RESULT_OK, result);
        finish();
    }

    private void showAmountInEntryField() {
        String amount = getFormattedAmountForEditing(mAmount);
        txtMain.setText(amount);
    }
}
