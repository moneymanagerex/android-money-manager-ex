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
package com.money.manager.ex.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.businessobjects.InfoService;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.currency.CurrenciesActivity;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCurrencyFormats;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

/**
 *
 */
public class GeneralSettingsFragment
        extends PreferenceFragment {

    public static final int REQUEST_PICK_CURRENCY = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppSettings settings = new AppSettings(getActivity());

        addPreferencesFromResource(R.xml.general_settings);
        PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Application Locale

        final ListPreference lstLocaleApp = (ListPreference) findPreference(getString(R.string.pref_locale));
        if (lstLocaleApp != null) {
            String summary = settings.getGeneralSettings().getApplicationLocale();
            setSummaryListPreference(lstLocaleApp, summary, R.array.application_locale_values, R.array.application_locale_entries);
            lstLocaleApp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setSummaryListPreference(preference, String.valueOf(newValue), R.array.application_locale_values, R.array.application_locale_entries);
                    MainActivity.setRestartActivity(true);
                    return true;
                }
            });
        }

        // Username

        final Preference pUserName = findPreference(getString(PreferenceConstants.PREF_USER_NAME));
        if (pUserName != null) {
            pUserName.setSummary(MoneyManagerApplication.getInstanceApp().getUserName());
            pUserName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    MoneyManagerApplication.getInstanceApp().setUserName((String) newValue, true);
                    pUserName.setSummary(MoneyManagerApplication.getInstanceApp().getUserName());
                    return true;
                }
            });
        }

        final InfoService infoService = new InfoService(getActivity());

        // list date format
        final ListPreference lstDateFormat = (ListPreference) findPreference(getString(PreferenceConstants.PREF_DATE_FORMAT));
        if (lstDateFormat != null) {
            lstDateFormat.setEntries(getResources().getStringArray(R.array.date_format));
            lstDateFormat.setEntryValues(getResources().getStringArray(R.array.date_format_mask));
            //set summary
            String value = infoService.getInfoValue(Constants.INFOTABLE_DATEFORMAT);
            lstDateFormat.setSummary(getDateFormatFromMask(value));
            lstDateFormat.setValue(value);
            //on change
            lstDateFormat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (infoService.setInfoValue(Constants.INFOTABLE_DATEFORMAT, (String) newValue)) {
                        lstDateFormat.setSummary(getDateFormatFromMask((String) newValue));
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        // list preference base currency
        initBaseCurrency();

        // default status
        final ListPreference lstDefaultStatus = (ListPreference) findPreference(getString(PreferenceConstants.PREF_DEFAULT_STATUS));
        if (lstDefaultStatus != null) {
            setSummaryListPreference(lstDefaultStatus, lstDefaultStatus.getValue(), R.array.status_values, R.array.status_items);
            lstDefaultStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setSummaryListPreference(lstDefaultStatus, newValue.toString(), R.array.status_values, R.array.status_items);
                    return true;
                }
            });
        }

        //default payee
        final ListPreference lstDefaultPayee = (ListPreference) findPreference(getString(PreferenceConstants.PREF_DEFAULT_PAYEE));
        if (lstDefaultPayee != null) {
            setSummaryListPreference(lstDefaultPayee, lstDefaultPayee.getValue(), R.array.new_transaction_dialog_values, R.array.new_transaction_dialog_items);
            lstDefaultPayee.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setSummaryListPreference(lstDefaultPayee, newValue.toString(), R.array.new_transaction_dialog_values, R.array.new_transaction_dialog_items);
                    return true;
                }
            });
        }

        // financial day and month
        final Preference pFinancialDay = findPreference(getString(PreferenceConstants.PREF_FINANCIAL_YEAR_STARTDATE));
        if (pFinancialDay != null) {
            pFinancialDay.setSummary(infoService.getInfoValue(Constants.INFOTABLE_FINANCIAL_YEAR_START_DAY));
            if (pFinancialDay.getSummary() != null) {
                pFinancialDay.setDefaultValue(pFinancialDay.getSummary().toString());
            }
            pFinancialDay.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int day;
                    try {
                        day = Integer.parseInt((String) newValue);
                        if (!(day >= 1 && day <= 31)) {
                            return false;
                        }
                        if (infoService.setInfoValue(Constants.INFOTABLE_FINANCIAL_YEAR_START_DAY, Integer.toString(day))) {
                            pFinancialDay.setSummary(Integer.toString(day));
                        }
                        return true;
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
                        handler.handle(e, "changing the start day of the financial year");
                    }
                    return false;
                }
            });
        }

        final Core core = new Core(getActivity().getApplicationContext());

        final ListPreference lstFinancialMonth = (ListPreference) findPreference(getString(PreferenceConstants.PREF_FINANCIAL_YEAR_STARTMONTH));
        if (lstFinancialMonth != null) {
            lstFinancialMonth.setEntries(core.getListMonths());
            lstFinancialMonth.setEntryValues(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"});
            lstFinancialMonth.setDefaultValue("0");
            // get current month
            try {
                String currentMonth = infoService.getInfoValue(Constants.INFOTABLE_FINANCIAL_YEAR_START_MONTH);
                if ((!TextUtils.isEmpty(currentMonth)) && NumberUtils.isNumber(currentMonth)) {
                    int month = Integer.parseInt(currentMonth) - 1;
                    if (month > -1 && month < lstFinancialMonth.getEntries().length) {
                        lstFinancialMonth.setSummary(lstFinancialMonth.getEntries()[month]);
                        lstFinancialMonth.setValue(Integer.toString(month));
                    }
                }
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
                handler.handle(e, "showing the month of the financial year");
            }
            lstFinancialMonth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int value = Integer.parseInt(newValue.toString());
                        if (value > -1 && value < lstFinancialMonth.getEntries().length) {
                            if (infoService.setInfoValue(Constants.INFOTABLE_FINANCIAL_YEAR_START_MONTH, Integer.toString(value + 1))) {
                                lstFinancialMonth.setSummary(lstFinancialMonth.getEntries()[value]);
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
                        handler.handle(e, "changing the month of the financial year");
                        return false;
                    }
                    return true;
                }
            });
        }

        initDefaultAccount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_CURRENCY:
                // Returning from the currency picker screen.
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    int currencyId = data.getIntExtra(CurrenciesActivity.INTENT_RESULT_CURRENCYID, -1);
                    // set preference
//                    AppSettings settings = new AppSettings(getActivity());
//                    settings.getGeneralSettings().setBaseCurrency(currencyId);
                    CurrencyService utils = new CurrencyService(getActivity());
                    utils.saveBaseCurrencyId(currencyId);
                    // refresh the displayed value.
                    showCurrentDefaultCurrency();
                }
                break;
        }
    }

    private void showCurrentDefaultCurrency() {
        Preference baseCurrency = findPreference(getString(PreferenceConstants.PREF_BASE_CURRENCY));
        if (baseCurrency == null) return;

        CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());
        Integer currencyId = currencyService.getBaseCurrencyId();

        TableCurrencyFormats tableCurrency = currencyService.getCurrency(currencyId);
        if (tableCurrency != null) {
            baseCurrency.setSummary(tableCurrency.getCurrencyName());
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
                Intent intent = new Intent(getActivity(), CurrenciesActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_CURRENCY);

                return true;
            }
        };

        baseCurrency.setOnPreferenceClickListener(clickListener);
    }

    public void setSummaryListPreference(Preference preference, String value, int idArrayValues, int idArrayItems) {
        final String[] values = getResources().getStringArray(idArrayValues);
        final String[] items = getResources().getStringArray(idArrayItems);
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) {
                preference.setSummary(items[i]);
            }
        }
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
        ListPreference preference = (ListPreference) findPreference(getString(PreferenceConstants.PREF_DEFAULT_ACCOUNT));
        if (preference == null) return;

        // show default summary
//        preference.setSummary(getString(R.string.default_account_summary));

        AccountService accountService = new AccountService(getActivity().getApplicationContext());
        List<Account> accounts = accountService.getAccountList(false, false);
        // the list is already sorted by name.

        final String[] entries = new String[accounts.size() + 1];
        String[] entryValues = new String[accounts.size() + 1];
        // Add the null value so that the setting can be disabled.
        entries[0] = getString(R.string.none);
        entryValues[0] = "-1";
        // list of currency
        for (int i = 1; i < accounts.size() + 1; i++) {
            entries[i] = accounts.get(i-1).getName();
            entryValues[i] = ((Integer) accounts.get(i-1).getId()).toString();
        }
        // set value
        preference.setEntries(entries);
        preference.setEntryValues(entryValues);

        final AccountRepository repository = new AccountRepository(getActivity());

        // set account name as the value here
        String defaultAccount = preference.getValue();
        String accountName = entries[0]; // none
        if (!TextUtils.isEmpty(defaultAccount) && !defaultAccount.equalsIgnoreCase("-1")) {
            accountName = repository.loadName(Integer.parseInt(defaultAccount));
        }
        preference.setSummary(accountName);

        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String accountName = entries[0];
                int accountId = Integer.parseInt(newValue.toString());
                if (accountId != -1) {
                    accountName = repository.loadName(accountId);
                }
                preference.setSummary(accountName);

                return true;
            }
        });
    }
}
