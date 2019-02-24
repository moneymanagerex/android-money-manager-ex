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
package com.money.manager.ex.currency;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Spinner;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.domainmodel.Currency;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;

/**
 * Edit currency
 */
public class CurrencyEditActivity
        extends MmxBaseFragmentActivity {

    // key intent
    public static final String KEY_CURRENCY_ID = "CurrencyEditActivity:CurrencyId";
    // update-instance key
    private static final String KEY_CURRENCY_NAME = "CurrencyEditActivity:CurrencyName";
    private static final String KEY_CURRENCY_SYMBOL = "CurrencyEditActivity:CurrencySymbol";
    private static final String KEY_UNIT_NAME = "CurrencyEditActivity:UnitName";
    private static final String KEY_CENTS_NAME = "CurrencyEditActivity:CentsName";
    private static final String KEY_PREFIX_SYMBOL = "CurrencyEditActivity:PrefixSymbol";
    private static final String KEY_SUFFIX_SYMBOL = "CurrencyEditActivity:SuffixSymbol";
    private static final String KEY_DECIMAL_CHAR = "CurrencyEditActivity:DecimalChar";
    private static final String KEY_GROUP_CHAR = "CurrencyEditActivity:GroupChar";
    private static final String KEY_SCALE = "CurrencyEditActivity:Scale";
    private static final String KEY_CONVERSION_TO_BASE = "CurrencyEditActivity:ConversionToBaseRate";
    private static final String KEY_ACTION = "CurrencyEditActivity:Action";

    private Integer mCurrencyId;
    // type of action
    private String mIntentAction = "";
    CurrencyEditViewHolder holder;

    @Override
    public boolean onActionCancelClick() {
        finish();
        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        if (save()) {
            finish();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.currency_edit_activity);
        showStandardToolbarActions();


        this.holder = CurrencyEditViewHolder.initialize(this);

        // update instance
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
        // manage intent
        if (getIntent() != null) {
            if (savedInstanceState == null) {
                mCurrencyId = getIntent().getIntExtra(KEY_CURRENCY_ID, -1);
                if (getIntent().getAction() != null && Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                    mCurrencyId = getIntent().getIntExtra(KEY_CURRENCY_ID, -1);
                    // load existing data
                    loadData(mCurrencyId);
                }
            }
            mIntentAction = getIntent().getAction();
        }

        // check default values for scale and base conversion rate
        if (Intent.ACTION_INSERT.equalsIgnoreCase(mIntentAction)) {
            if (TextUtils.isEmpty(holder.edtScale.getText()))
                holder.edtScale.setText("100");
            if (TextUtils.isEmpty(holder.edtConversion.getText()))
                holder.edtConversion.setText("1");
            // set default separator
            DecimalFormatSymbols symbols = ((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols();
            if (TextUtils.isEmpty(holder.edtDecimal.getText()))
                holder.edtDecimal.setText(Character.toString(symbols.getDecimalSeparator()));
            if (TextUtils.isEmpty(holder.edtGroup.getText()))
                holder.edtGroup.setText(Character.toString(symbols.getGroupingSeparator()));
            // set default symbols
            if (TextUtils.isEmpty(holder.edtPrefix.getText()))
                holder.edtPrefix.setText(symbols.getCurrencySymbol());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CURRENCY_ID, mCurrencyId);
        outState.putString(KEY_CURRENCY_NAME, holder.edtCurrencyName.getText().toString());
        if (holder.spinCurrencySymbol.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
            outState.putString(KEY_CURRENCY_SYMBOL, getResources()
                .getStringArray(R.array.currencies_code)[holder.spinCurrencySymbol.getSelectedItemPosition()]);
        }
        outState.putString(KEY_UNIT_NAME, holder.edtCurrencyName.getText().toString());
        outState.putString(KEY_CENTS_NAME, holder.edtCentsName.getText().toString());
        outState.putString(KEY_PREFIX_SYMBOL, holder.edtPrefix.getText().toString());
        outState.putString(KEY_SUFFIX_SYMBOL, holder.edtSuffix.getText().toString());
        outState.putString(KEY_DECIMAL_CHAR, holder.edtDecimal.getText().toString());
        outState.putString(KEY_GROUP_CHAR, holder.edtGroup.getText().toString());
        outState.putString(KEY_SCALE, holder.edtScale.getText().toString());
        outState.putString(KEY_CONVERSION_TO_BASE, holder.edtConversion.getText().toString());
    }

    private boolean loadData(int currencyId) {
        CurrencyRepository repo = new CurrencyRepository(this);
        Cursor cursor = getContentResolver().query(repo.getUri(),
                repo.getAllColumns(),
                Currency.CURRENCYID + "=?",
                new String[]{Integer.toString(currencyId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }
        // populate values
        holder.edtCurrencyName.setText(cursor.getString(cursor.getColumnIndex(Currency.CURRENCYNAME)));
        holder.spinCurrencySymbol.setSelection(Arrays.asList(getResources().getStringArray(R.array.currencies_code))
            .indexOf(cursor.getString(cursor.getColumnIndex(Currency.CURRENCY_SYMBOL))), true);
        holder.edtUnitName.setText(cursor.getString(cursor.getColumnIndex(Currency.UNIT_NAME)));
        holder.edtCentsName.setText(cursor.getString(cursor.getColumnIndex(Currency.CENT_NAME)));
        holder.edtPrefix.setText(cursor.getString(cursor.getColumnIndex(Currency.PFX_SYMBOL)));
        holder.edtSuffix.setText(cursor.getString(cursor.getColumnIndex(Currency.SFX_SYMBOL)));
        holder.edtDecimal.setText(cursor.getString(cursor.getColumnIndex(Currency.DECIMAL_POINT)));
        holder.edtGroup.setText(cursor.getString(cursor.getColumnIndex(Currency.GROUP_SEPARATOR)));
        holder.edtScale.setText(cursor.getString(cursor.getColumnIndex(Currency.SCALE)));
        holder.edtConversion.setText(cursor.getString(cursor.getColumnIndex(Currency.BASECONVRATE)));

        cursor.close();

        return true;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        mCurrencyId = savedInstanceState.getInt(KEY_CURRENCY_ID);

        holder.edtCurrencyName.setText(savedInstanceState.getString(KEY_CURRENCY_NAME));
        holder.spinCurrencySymbol.setSelection(Arrays.asList(getResources().getStringArray(R.array.currencies_code))
            .indexOf(savedInstanceState.getString(KEY_CURRENCY_SYMBOL)), true);
        holder.edtUnitName.setText(savedInstanceState.getString(KEY_UNIT_NAME));
        holder.edtCentsName.setText(savedInstanceState.getString(KEY_CENTS_NAME));
        holder.edtPrefix.setText(savedInstanceState.getString(KEY_PREFIX_SYMBOL));
        holder.edtSuffix.setText(savedInstanceState.getString(KEY_SUFFIX_SYMBOL));
        holder.edtDecimal.setText(savedInstanceState.getString(KEY_DECIMAL_CHAR));
        holder.edtGroup.setText(savedInstanceState.getString(KEY_GROUP_CHAR));
        holder.edtScale.setText(savedInstanceState.getString(KEY_SCALE));
        holder.edtConversion.setText(savedInstanceState.getString(KEY_CONVERSION_TO_BASE));

        // action
        mIntentAction = savedInstanceState.getString(KEY_ACTION);
    }

    /**
     * This method is used to validate the data before saving
     *
     * @return true if data is valid
     */
    private boolean validateData() {
        if (TextUtils.isEmpty(holder.edtCurrencyName.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.currency_name_empty, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * This method update data into database
     * @return true if data is update into database
     */
    private boolean save() {
        if (!validateData()) return false;

        Currency currency = new Currency();

        currency.setName(holder.edtCurrencyName.getText().toString().trim());

        if (holder.spinCurrencySymbol.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
            String code = getResources()
                .getStringArray(R.array.currencies_code)[holder.spinCurrencySymbol.getSelectedItemPosition()];
            currency.setCode(code);
        }

        currency.setUnitName(holder.edtUnitName.getText().toString().trim());

        currency.setCentName(holder.edtCentsName.getText().toString().trim());
        currency.setPfxSymbol(holder.edtPrefix.getText().toString().trim());
        currency.setSfxSymbol(holder.edtSuffix.getText().toString().trim());
        currency.setDecimalPoint(holder.edtDecimal.getText().toString().trim());
        currency.setGroupSeparator(holder.edtGroup.getText().toString().trim());

        int scale = Integer.parseInt(holder.edtScale.getText().toString().trim());
        currency.contentValues.put(Currency.SCALE, scale);

        BigDecimal rate = new BigDecimal(holder.edtConversion.getText().toString().trim());
        currency.contentValues.put(Currency.BASECONVRATE, rate.doubleValue());
//        currency.setConversionRate();

        CurrencyRepository repo = new CurrencyRepository(getApplicationContext());

        // update data
        boolean success;
        switch (mIntentAction) {
            case Intent.ACTION_INSERT:
                success = repo.insert(currency);
                break;

            // todo: use ACTION_EDIT explicitly.
            default:
                // Add Id value only when updating.
                if (mCurrencyId != Constants.NOT_SET) {
                    currency.setCurrencyid(mCurrencyId);
                }

                success = repo.update(currency);
                break;
        }

        return success;
    }
}
