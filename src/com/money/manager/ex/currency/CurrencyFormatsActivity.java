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
package com.money.manager.ex.currency;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.utils.CurrencyUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public class CurrencyFormatsActivity extends BaseFragmentActivity {
    // key intent
    public static final String KEY_CURRENCY_ID = "CurrencyFormatsActivity:CurrencyId";
    private static final String LOGCAT = CurrencyFormatsActivity.class.getSimpleName();
    // saveinstance key
    private static final String KEY_CURRENCY_NAME = "CurrencyFormatsActivity:CurrencyName";
    private static final String KEY_CURRENCY_SYMBOL = "CurrencyFormatsActivity:CurrencySymbol";
    private static final String KEY_UNIT_NAME = "CurrencyFormatsActivity:UnitName";
    private static final String KEY_CENTS_NAME = "CurrencyFormatsActivity:CentsName";
    private static final String KEY_PREFIX_SYMBOL = "CurrencyFormatsActivity:PrefixSymbol";
    private static final String KEY_SUFFIX_SYMBOL = "CurrencyFormatsActivity:SuffixSymbol";
    private static final String KEY_DECIMAL_CHAR = "CurrencyFormatsActivity:DecimalChar";
    private static final String KEY_GROUP_CHAR = "CurrencyFormatsActivity:GroupChar";
    private static final String KEY_SCALE = "CurrencyFormatsActivity:Scale";
    private static final String KEY_CONVERSION_TO_BASE = "CurrencyFormatsActivity:ConversionToBaseRate";
    private static final String KEY_ACTION = "CurrencyFormatsActivity:Action";
    // object of table
    private TableCurrencyFormats mCurrency = new TableCurrencyFormats();
    private Integer mCurrencyId;
    // type of action
    private String mIntentAction = "";
    // object into layout
    private EditText edtCurrencyName, edtUnitName, edtCentsName, edtPrefix, edtSuffix,
            edtDecimal, edtGroup, edtScale, edtConversion;
    private Spinner spinCurrencySymbol;

    @Override
    public boolean onActionCancelClick() {
        finish();
        return super.onActionCancelClick();
    }

    @Override
    public boolean onActionDoneClick() {
        if (updateData()) {
            finish();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currecyformats_activity);
        setToolbarStandardAction(getToolbar());
        // take object
        edtCurrencyName = (EditText) findViewById(R.id.editTextCurrencyName);
        spinCurrencySymbol = (Spinner) findViewById(R.id.spinCurrencySymbol);
        edtUnitName = (EditText) findViewById(R.id.editTextUnitName);
        edtCentsName = (EditText) findViewById(R.id.editTextCentsName);
        edtPrefix = (EditText) findViewById(R.id.editTextPrefixSymbol);
        edtSuffix = (EditText) findViewById(R.id.editTextSuffixSymbol);
        edtDecimal = (EditText) findViewById(R.id.editTextDecimalChar);
        edtGroup = (EditText) findViewById(R.id.editTextGroupChar);
        edtScale = (EditText) findViewById(R.id.editTextScale);
        edtConversion = (EditText) findViewById(R.id.editTextConversion);

        // save instance
        if (savedInstanceState != null) {
            mCurrencyId = savedInstanceState.getInt(KEY_CURRENCY_ID);
            edtCurrencyName.setText(savedInstanceState.getString(KEY_CURRENCY_NAME));
            spinCurrencySymbol.setSelection(Arrays.asList(getResources().getStringArray(R.array.currencies_code)).indexOf(savedInstanceState.getString(KEY_CURRENCY_SYMBOL)), true);
            edtUnitName.setText(savedInstanceState.getString(KEY_UNIT_NAME));
            edtCentsName.setText(savedInstanceState.getString(KEY_CENTS_NAME));
            edtPrefix.setText(savedInstanceState.getString(KEY_PREFIX_SYMBOL));
            edtSuffix.setText(savedInstanceState.getString(KEY_SUFFIX_SYMBOL));
            edtDecimal.setText(savedInstanceState.getString(KEY_DECIMAL_CHAR));
            edtGroup.setText(savedInstanceState.getString(KEY_GROUP_CHAR));
            edtScale.setText(savedInstanceState.getString(KEY_SCALE));
            edtConversion.setText(savedInstanceState.getString(KEY_CONVERSION_TO_BASE));
            // action
            mIntentAction = savedInstanceState.getString(KEY_ACTION);
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

        // check default values for scale and baseconvrate
        if (Constants.INTENT_ACTION_INSERT.equalsIgnoreCase(mIntentAction)) {
            if (TextUtils.isEmpty(edtScale.getText()))
                edtScale.setText("100");
            if (TextUtils.isEmpty(edtConversion.getText()))
                edtConversion.setText("1");
            // set default separator
            DecimalFormatSymbols symbols = ((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols();
            if (TextUtils.isEmpty(edtDecimal.getText()))
                edtDecimal.setText(Character.toString(symbols.getDecimalSeparator()));
            if (TextUtils.isEmpty(edtGroup.getText()))
                edtGroup.setText(Character.toString(symbols.getGroupingSeparator()));
            // set default symbols
            if (TextUtils.isEmpty(edtPrefix.getText()))
                edtPrefix.setText(symbols.getCurrencySymbol());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the state of object
        outState.putInt(KEY_CURRENCY_ID, mCurrencyId);
        outState.putString(KEY_CURRENCY_NAME, edtCurrencyName.getText().toString());
        if (spinCurrencySymbol.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
            outState.putString(KEY_CURRENCY_SYMBOL, getResources().getStringArray(R.array.currencies_code)[spinCurrencySymbol.getSelectedItemPosition()]);
        }
        outState.putString(KEY_UNIT_NAME, edtCurrencyName.getText().toString());
        outState.putString(KEY_CENTS_NAME, edtCentsName.getText().toString());
        outState.putString(KEY_PREFIX_SYMBOL, edtPrefix.getText().toString());
        outState.putString(KEY_SUFFIX_SYMBOL, edtSuffix.getText().toString());
        outState.putString(KEY_DECIMAL_CHAR, edtDecimal.getText().toString());
        outState.putString(KEY_GROUP_CHAR, edtGroup.getText().toString());
        outState.putString(KEY_SCALE, edtScale.getText().toString());
        outState.putString(KEY_CONVERSION_TO_BASE, edtConversion.getText().toString());
    }

    private boolean loadData(int currencyId) {
        Cursor cursor = getContentResolver().query(mCurrency.getUri(),
                mCurrency.getAllColumns(),
                TableCurrencyFormats.CURRENCYID + "=?",
                new String[]{Integer.toString(currencyId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }
        // populate values
        edtCurrencyName.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYNAME)));
        spinCurrencySymbol.setSelection(Arrays.asList(getResources().getStringArray(R.array.currencies_code)).indexOf(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCY_SYMBOL))), true);
        edtUnitName.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.UNIT_NAME)));
        edtCentsName.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CENT_NAME)));
        edtPrefix.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.PFX_SYMBOL)));
        edtSuffix.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.SFX_SYMBOL)));
        edtDecimal.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.DECIMAL_POINT)));
        edtGroup.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.GROUP_SEPARATOR)));
        edtScale.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.SCALE)));
        edtConversion.setText(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.BASECONVRATE)));

        cursor.close();

        return true;
    }

    /**
     * This method is used to validate the data before saving
     *
     * @return true if data is valid
     */
    private boolean validateData() {
        if (TextUtils.isEmpty(edtCurrencyName.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.currency_name_empty, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * This method save data into database
     *
     * @return true if data is update into database
     */
    private boolean updateData() {
        if (!validateData()) {
            return false;
        }

        // create contentvalues for update data
        ContentValues values = new ContentValues();
        // set values
        values.put(TableCurrencyFormats.CURRENCYNAME, edtCurrencyName.getText().toString());
        if (spinCurrencySymbol.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
            values.put(TableCurrencyFormats.CURRENCY_SYMBOL, getResources().getStringArray(R.array.currencies_code)[spinCurrencySymbol.getSelectedItemPosition()]);
        }
        values.put(TableCurrencyFormats.UNIT_NAME, edtCurrencyName.getText().toString());
        values.put(TableCurrencyFormats.CENT_NAME, edtCentsName.getText().toString());
        values.put(TableCurrencyFormats.PFX_SYMBOL, edtPrefix.getText().toString());
        values.put(TableCurrencyFormats.SFX_SYMBOL, edtSuffix.getText().toString());
        values.put(TableCurrencyFormats.DECIMAL_POINT, edtDecimal.getText().toString());
        values.put(TableCurrencyFormats.GROUP_SEPARATOR, edtGroup.getText().toString());
        values.put(TableCurrencyFormats.SCALE, edtScale.getText().toString());
        values.put(TableCurrencyFormats.BASECONVRATE, edtConversion.getText().toString());
        // update data
        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction)) {
            if (getContentResolver().insert(mCurrency.getUri(), values) == null) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                Log.w(LOGCAT, "Insert new currency failed!");
                return false;
            }
        } else {
            if (getContentResolver().update(mCurrency.getUri(), values, TableCurrencyFormats.CURRENCYID + "=?", new String[]{Integer.toString(mCurrencyId)}) <= 0) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(LOGCAT, "Update currency id = " + Integer.toString(mCurrencyId) + " failed!");
                return false;
            }
        }
        //reload currency
        CurrencyUtils currencyUtils = new CurrencyUtils(getApplicationContext());
        currencyUtils.reInit();

        return true;
    }
}
