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

package com.money.manager.ex.settings;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.primitives.Ints;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.currency.list.CurrencyListActivity;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.servicelayer.InfoService;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import timber.log.Timber;

/**
 *
 */
public class PerDatabaseFragment
    extends PreferenceFragmentCompat {

    public static final int REQUEST_PICK_CURRENCY = 1;

    public PerDatabaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_per_database);

        initializeControls();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PICK_CURRENCY:
                // Returning from the currency picker screen.
                if ((resultCode == AppCompatActivity.RESULT_OK) && (data != null)) {
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

        // Username

        final Preference pUserName = findPreference(getString(R.string.pref_user_name));
        if (pUserName != null) {
            pUserName.setSummary(MmexApplication.getApp().getUserName());
            pUserName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    MmexApplication.getApp().setUserName((String) newValue, true);
                    pUserName.setSummary(MmexApplication.getApp().getUserName());
                    return false;
                }
            });
        }

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
                    }
                    // Do not update to preferences file.
                    return false;
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
                        new UIHelper(getActivity()).showToast(R.string.error_parsing_value);
                        return false;
                    }

                    try {
                        if (day < 1 || day > 31) {
                            return false;
                        }
                        if (infoService.setInfoValue(InfoKeys.FINANCIAL_YEAR_START_DAY, Integer.toString(day))) {
                            pFinancialDay.setSummary(Integer.toString(day));
                        }
//                        return true;
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

                Integer month = Ints.tryParse(currentMonth);
                if (month != null) {
                    //int month = Integer.parseInt(currentMonth) - 1;
                    month = month - 1;
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
                            }
                        }
                    } catch (Exception e) {
                        Timber.e(e, "changing the month of the financial year");
                    }
                    return false;
                }
            });
        }

        initDefaultAccount();
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

    private void initDefaultAccount() {
        ListPreference preference = (ListPreference) findPreference(getString(R.string.pref_default_account));
        if (preference == null) return;

        AccountService accountService = new AccountService(getActivity());
        List<Account> accounts = accountService.getAccountList(false, false);

        // the list is already sorted by name.

        final String[] entries = new String[accounts.size() + 1];
        String[] entryValues = new String[accounts.size() + 1];
        // Add the null value so that the setting can be disabled.
        entries[0] = getString(R.string.none);
        entryValues[0] = "-1";
        // list of currencies
        for (int i = 1; i < accounts.size() + 1; i++) {
            entries[i] = accounts.get(i-1).getName();
            entryValues[i] = accounts.get(i-1).getId().toString();
        }
        // set value
        preference.setEntries(entries);
        preference.setEntryValues(entryValues);

        final AccountRepository repository = new AccountRepository(getActivity());

        // set account name as the value here
        Integer defaultAccountId = new GeneralSettings(getActivity()).getDefaultAccountId();
        String accountName = entries[0]; // none
        if (defaultAccountId != null && defaultAccountId != Constants.NOT_SET) {
            accountName = repository.loadName(defaultAccountId);
        }
        preference.setSummary(accountName);

        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String accountName = entries[0];
                int accountId = Integer.parseInt(newValue.toString());
                if (accountId != Constants.NOT_SET) {
                    accountName = repository.loadName(accountId);
                }
                preference.setSummary(accountName);

                new GeneralSettings(getActivity()).setDefaultAccountId(accountId);

                return true;
            }
        });
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
