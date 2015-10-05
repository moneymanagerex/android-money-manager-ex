/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.domainmodel.Currency;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;

/**
 *
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
public class CurrencyEditActivity
        extends BaseFragmentActivity {

    // key intent
    public static final String KEY_CURRENCY_ID = "CurrencyEditActivity:CurrencyId";
    private static final String LOGCAT = CurrencyEditActivity.class.getSimpleName();
    // save-instance key
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

    private TableCurrencyFormats mCurrency = new TableCurrencyFormats();
    private Integer mCurrencyId;
    // type of action
    private String mIntentAction = "";

    private EditText edtCurrencyName, edtUnitName, edtCentsName, edtPrefix, edtSuffix,
            edtDecimal, edtGroup, edtScale, edtConversion;
    private Spinner spinCurrencySymbol;

    @Override
    public boolean onActionCancelClick() {
        finish();
//        return super.onActionCancelClick();
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
        setToolbarStandardAction(getToolbar());

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
        if (Intent.ACTION_INSERT.equalsIgnoreCase(mIntentAction)) {
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
                Currency.CURRENCYID + "=?",
                new String[]{Integer.toString(currencyId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }
        // populate values
        edtCurrencyName.setText(cursor.getString(cursor.getColumnIndex(Currency.CURRENCYNAME)));
        spinCurrencySymbol.setSelection(Arrays.asList(getResources().getStringArray(R.array.currencies_code))
            .indexOf(cursor.getString(cursor.getColumnIndex(Currency.CURRENCY_SYMBOL))), true);
        edtUnitName.setText(cursor.getString(cursor.getColumnIndex(Currency.UNIT_NAME)));
        edtCentsName.setText(cursor.getString(cursor.getColumnIndex(Currency.CENT_NAME)));
        edtPrefix.setText(cursor.getString(cursor.getColumnIndex(Currency.PFX_SYMBOL)));
        edtSuffix.setText(cursor.getString(cursor.getColumnIndex(Currency.SFX_SYMBOL)));
        edtDecimal.setText(cursor.getString(cursor.getColumnIndex(Currency.DECIMAL_POINT)));
        edtGroup.setText(cursor.getString(cursor.getColumnIndex(Currency.GROUP_SEPARATOR)));
        edtScale.setText(cursor.getString(cursor.getColumnIndex(Currency.SCALE)));
        edtConversion.setText(cursor.getString(cursor.getColumnIndex(Currency.BASECONVRATE)));

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
    private boolean save() {
        if (!validateData()) {
            return false;
        }

        Currency currency = new Currency();
        currency.setCurrencyid(mCurrencyId);

        currency.setName(edtCurrencyName.getText().toString().trim());

        if (spinCurrencySymbol.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
            String code = getResources().getStringArray(R.array.currencies_code)[spinCurrencySymbol.getSelectedItemPosition()];
            currency.setCode(code);
        }

        currency.setUnitName(edtUnitName.getText().toString().trim());

        currency.contentValues.put(Currency.CENT_NAME, edtCentsName.getText().toString().trim());
        currency.contentValues.put(Currency.PFX_SYMBOL, edtPrefix.getText().toString().trim());
        currency.contentValues.put(Currency.SFX_SYMBOL, edtSuffix.getText().toString().trim());
        currency.contentValues.put(Currency.DECIMAL_POINT, edtDecimal.getText().toString().trim());
        currency.contentValues.put(Currency.GROUP_SEPARATOR, edtGroup.getText().toString().trim());
        currency.contentValues.put(Currency.SCALE, edtScale.getText().toString().trim());
        currency.contentValues.put(Currency.BASECONVRATE, edtConversion.getText().toString().trim());
//        currency.setConversionRate();

        CurrencyRepository repo = new CurrencyRepository(getApplicationContext());

        // update data
        boolean success = false;
        if (Intent.ACTION_INSERT.equals(mIntentAction)) {
            success = repo.insert(currency);

//            if (!success) {
//                Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
//                Log.w(LOGCAT, "Inserting new currency failed!");
//                return false;
//            }
        } else {
            success = repo.update(currency);

//            if (!success) {
//                Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
//                Log.w(LOGCAT, "Update of currency with id:" + Integer.toString(mCurrencyId) + " failed!");
//                return false;
//            }
        }
        //reload currency
        CurrencyService currencyService = new CurrencyService(getApplicationContext());
        currencyService.reInit();

        return success;
    }
}
