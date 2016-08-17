/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.settings;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.currency.list.CurrencyListActivity;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.servicelayer.InfoService;

import org.apache.commons.lang3.math.NumberUtils;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerDatabaseFragment
    extends PreferenceFragment {

    public static final int REQUEST_PICK_CURRENCY = 1;

    public PerDatabaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_per_database);

        initializeControls();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_CURRENCY:
                // Returning from the currency picker screen.
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    int currencyId = data.getIntExtra(CurrencyListActivity.INTENT_RESULT_CURRENCYID, -1);
                    // set preference
                    CurrencyService utils = new CurrencyService(getActivity());
                    utils.setBaseCurrencyId(currencyId);
                    // refresh the displayed value.
                    showCurrentDefaultCurrency();

                    // notify the user to update exchange rates!
                    showCurrencyChangeNotification();
                }
                break;
        }
    }

    private void initializeControls() {
        final InfoService infoService = new InfoService(getActivity());

        // Date format

        final ListPreference lstDateFormat = (ListPreference) findPreference(getString(R.string.pref_date_format));
        if (lstDateFormat != null) {
            lstDateFormat.setEntries(getResources().getStringArray(R.array.date_format));
            lstDateFormat.setEntryValues(getResources().getStringArray(R.array.date_format_mask));
            //set summary
            String value = infoService.getInfoValue(InfoKeys.DATEFORMAT);
            lstDateFormat.setSummary(getDateFormatFromMask(value));
            lstDateFormat.setValue(value);
            //on change
            lstDateFormat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (infoService.setInfoValue(InfoKeys.DATEFORMAT, (String) newValue)) {
                        lstDateFormat.setSummary(getDateFormatFromMask((String) newValue));
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        // Base Currency

        initBaseCurrency();

        // financial year, day and month

        final Preference pFinancialDay = findPreference(getString(PreferenceConstants.PREF_FINANCIAL_YEAR_STARTDATE));
        if (pFinancialDay != null) {
            pFinancialDay.setSummary(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_DAY));
            if (pFinancialDay.getSummary() != null) {
                pFinancialDay.setDefaultValue(pFinancialDay.getSummary().toString());
            }
            pFinancialDay.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int day;
                    try {
                        day = Integer.parseInt((String) newValue);
                    } catch (NumberFormatException e) {
                        UIHelper.showToast(getActivity(), R.string.error_parsing_value);
                        return false;
                    }

                    try {
                        if (day < 1 || day > 31) {
                            return false;
                        }
                        if (infoService.setInfoValue(InfoKeys.FINANCIAL_YEAR_START_DAY, Integer.toString(day))) {
                            pFinancialDay.setSummary(Integer.toString(day));
                        }
                        return true;
                    } catch (Exception e) {
                        Timber.e(e, "changing the start day of the financial year");
                    }
                    return false;
                }
            });
        }

        final Core core = new Core(getActivity().getApplicationContext());

        // Financial year/month

        final ListPreference lstFinancialMonth = (ListPreference) findPreference(getString(PreferenceConstants.PREF_FINANCIAL_YEAR_STARTMONTH));
        if (lstFinancialMonth != null) {
            lstFinancialMonth.setEntries(core.getListMonths());
            lstFinancialMonth.setEntryValues(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"});
            lstFinancialMonth.setDefaultValue("0");
            // get current month
            try {
                String currentMonth = infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_MONTH);
                if ((!TextUtils.isEmpty(currentMonth)) && NumberUtils.isNumber(currentMonth)) {
                    int month = Integer.parseInt(currentMonth) - 1;
                    if (month > -1 && month < lstFinancialMonth.getEntries().length) {
                        lstFinancialMonth.setSummary(lstFinancialMonth.getEntries()[month]);
                        lstFinancialMonth.setValue(Integer.toString(month));
                    }
                }
            } catch (Exception e) {
                Timber.e(e, "showing the month of the financial year");
            }
            lstFinancialMonth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int value = Integer.parseInt(newValue.toString());
                        if (value > -1 && value < lstFinancialMonth.getEntries().length) {
                            if (infoService.setInfoValue(InfoKeys.FINANCIAL_YEAR_START_MONTH, Integer.toString(value + 1))) {
                                lstFinancialMonth.setSummary(lstFinancialMonth.getEntries()[value]);
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        Timber.e(e, "changing the month of the financial year");
                        return false;
                    }
                    return true;
                }
            });
        }

    }

    /**
     * select the base currency in a currency picker instead of a list.
     */
    private void initBaseCurrency() {
        Preference baseCurrency = findPreference(getString(PreferenceConstants.PREF_BASE_CURRENCY));
        if (baseCurrency == null) return;

        // Display the current default currency as the summary.
        showCurrentDefaultCurrency();

        // After the currency is selected in the Currencies screen, the change is handled
        // in onActivityResult

        Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // show the currencies activity
                Intent intent = new Intent(getActivity(), CurrencyListActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_CURRENCY);

                return true;
            }
        };

        baseCurrency.setOnPreferenceClickListener(clickListener);
    }

    private String getDateFormatFromMask(String mask) {
        if (!TextUtils.isEmpty(mask)) {
            for (int i = 0; i < getResources().getStringArray(R.array.date_format_mask).length; i++) {
                if (mask.equals(getResources().getStringArray(R.array.date_format_mask)[i])) {
                    return getResources().getStringArray(R.array.date_format)[i];
                }
            }
        }
        return null;
    }

    private void showCurrentDefaultCurrency() {
        Preference baseCurrency = findPreference(getString(PreferenceConstants.PREF_BASE_CURRENCY));
        if (baseCurrency == null) return;

        CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());
        Integer currencyId = currencyService.getBaseCurrencyId();

        Currency tableCurrency = currencyService.getCurrency(currencyId);
        if (tableCurrency != null) {
            baseCurrency.setSummary(tableCurrency.getName());
        }
    }

    private void showCurrencyChangeNotification() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.base_currency_changed)
                .content(R.string.base_currency_change_notification)
                .positiveText(android.R.string.ok)
                .neutralText(R.string.open_currencies)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        Intent intent = new Intent(getActivity(), CurrencyListActivity.class);
                        intent.setAction(Intent.ACTION_EDIT);
                        startActivity(intent);
                    }
                })
                .show();
    }

}
