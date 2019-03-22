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

import android.content.Context;
import android.database.Cursor;
//import net.sqlcipher.database.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.CurrencyRepositorySql;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.investment.prices.IExchangeRateUpdater;
import com.money.manager.ex.investment.prices.ISecurityPriceUpdater;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.servicelayer.ServiceBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * This class implements all the methods of utility for the management of currencies.
 */
public class CurrencyService
    extends ServiceBase {

    @Inject
    public CurrencyService(Context context) {
        super(context);

        mCurrencyCodes = new HashMap<>();
        mCurrencies = new SparseArray<>();

        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Inject CurrencyRepositorySql mRepository;

    private Integer mBaseCurrencyId = null;
    // hash map of all currencies
    private SparseArray<Currency> mCurrencies;
    /**
     * a fast lookup for symbol -> id. i.e. EUR->2.
     */
    private HashMap<String, Integer> mCurrencyCodes;

    /**
     * @param currencyId of the currency to be get
     * @return a Currency. Null if fail
     */
    public Currency getCurrency(Integer currencyId) {
        if (currencyId == null || currencyId == Constants.NOT_SET) return null;

        if (mCurrencies.indexOfKey(currencyId) >= 0) {
            return mCurrencies.get(currencyId);
        }

        CurrencyRepository repository = getRepository();
        Currency currency = repository.loadCurrency(currencyId);

        mCurrencies.put(currencyId, currency);

        return currency;
    }

    public Currency getCurrency(String currencyCode) {
        int id = getIdForCode(currencyCode);
        return getCurrency(id);
    }

    public String getBaseCurrencyCode() {
        // get base currency
        int baseCurrencyId = this.getBaseCurrencyId();

        Currency currency = this.getCurrency(baseCurrencyId);
        if (currency == null) {
//            new UIHelper(getContext()).showToast(R.string.base_currency_not_set);
            Timber.w(getContext().getString(R.string.base_currency_not_set));
            return null;
        }
        return currency.getCode();
    }

    public String getSymbolFor(int id) {
        Currency currency = getCurrency(id);
        return currency.getCode();
    }

    public Integer getIdForCode(String code) {
        if (mCurrencyCodes.containsKey(code)) {
            return mCurrencyCodes.get(code);
        }

        CurrencyRepository repo = getRepository();
        Currency currency = repo.loadCurrency(code);

        mCurrencyCodes.put(code, currency.getCurrencyId());

        Integer result = currency.getCurrencyId();

        return result;
    }

    public List<Currency> getUsedCurrencies() {
        AccountService service = new AccountService(getContext());

        List<Account> accounts = service.getAccountList();
        if (accounts == null) return null;

        List<Currency> currencies = new ArrayList<>();

        for (Account account : accounts) {
            Currency currency = getCurrency(account.getCurrencyId());
            if (!currencies.contains(currency)) {
                currencies.add(currency);
            }
        }

        // order by name?
        Collections.sort(currencies, new Comparator<Currency>() {
            @Override
            public int compare(Currency lhs, Currency rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        return currencies;
    }

    public List<Currency> getUnusedCurrencies() {
        List<Currency> usedCurrencies = getUsedCurrencies();
        String usedList = "";
        for (Currency currency : usedCurrencies) {
            usedList += currency.getCurrencyId();
            usedList += ", ";
        }
        // old trick. Now remove the last separator.
        usedList = usedList.substring(0, usedList.length() - 2);

        Select query = new Select(getRepository().getAllColumns())
            .where(Currency.CURRENCYID + " NOT IN (" + usedList + ")")
            .orderBy(Currency.CURRENCYNAME);

        return getRepository().query(Currency.class, query);
    }

    public Money doCurrencyExchange(Integer toCurrencyId, Money amount, Integer fromCurrencyId) {
        if (toCurrencyId == null || fromCurrencyId == null) return amount;
        if (toCurrencyId == Constants.NOT_SET || fromCurrencyId == Constants.NOT_SET) return amount;

        // e same currencies
        if (toCurrencyId.equals(fromCurrencyId)) return amount;

        Currency fromCurrencyFormats = getCurrency(fromCurrencyId);
        Currency toCurrencyFormats = getCurrency(toCurrencyId);
        // check if exists from and to currencies
        if (fromCurrencyFormats == null || toCurrencyFormats == null) {
            String message = fromCurrencyFormats == null
                    ? "currency " + fromCurrencyId + " not loaded."
                    : "";
            message += toCurrencyFormats == null
                    ? " currency " + toCurrencyId + " not loaded."
                    : "";
            throw new RuntimeException(message);
        }

        // exchange
        double toConversionRate = toCurrencyFormats.getBaseConversionRate();
        double fromConversionRate = fromCurrencyFormats.getBaseConversionRate();

//        double result = (amount * fromConversionRate) / toConversionRate;
        Money result = amount.multiply(fromConversionRate).divide(toConversionRate, Constants.DEFAULT_PRECISION);

        return result;
    }

    /**
     * Loads id of base currency.
     *
     * @return Id of base currency
     */
    public int getBaseCurrencyId() {
        if (mBaseCurrencyId != null) return mBaseCurrencyId;

        int result;

        Integer baseCurrencyId = loadBaseCurrencyId();

        if (baseCurrencyId != null) {
            result = baseCurrencyId;
        } else {
            // No base currency set yet. Try to get it from the system.
            java.util.Currency systemCurrency = this.getSystemDefaultCurrency();
            if (systemCurrency == null) {
                // could not get base currency from the system. Use Euro?
                //Currency euro = repo.loadCurrency("EUR");
                //result = euro.getCurrencyId();
                Log.w("CurrencyService", "system default currency is null!");
                result = 2;
            } else {
                CurrencyRepository repo = getRepository();
                Currency defaultCurrency = repo.loadCurrency(systemCurrency.getCurrencyCode());

                if (defaultCurrency != null) {
                    result = defaultCurrency.getCurrencyId();
                } else {
                    // currency not found.
                    Log.w("CurrencyService", "currency " + systemCurrency.getCurrencyCode() +
                            "not found!");
                    result = 2;
                }
            }
        }
        mBaseCurrencyId = result;

        return result;
    }

    public void setBaseCurrencyId(int baseCurrencyId) {
        mBaseCurrencyId = baseCurrencyId;

        InfoService service = new InfoService(getContext());
        boolean saved = service.setInfoValue(InfoKeys.BASECURRENCYID, Integer.toString(baseCurrencyId));
        if (!saved) {
            new UIHelper(getContext()).showToast(R.string.error_saving_default_currency);
        }
    }

    public Currency getBaseCurrency() {
        int baseCurrencyId = getBaseCurrencyId();
        return getCurrency(baseCurrencyId);
    }

    /**
     * Formats the given value, in base currency, as a string for display.
     * @param value to format
     * @return formatted value
     */
    public String getBaseCurrencyFormatted(Money value) {
        int baseCurrencyId = getBaseCurrencyId();
        return this.getCurrencyFormatted(baseCurrencyId, value);
    }

    /**
     * Format the given value with the currency preferences.
     *
     * @param currencyId of the currency to be formatted
     * @param value      value to format
     * @return formatted value
     */
    public String getCurrencyFormatted(Integer currencyId, Money value) {
        String result;

        // check if value is null
        if (value == null) value = MoneyFactory.fromDouble(0);

        // find currency id
        if (currencyId != null) {
            Currency currency = getCurrency(currencyId);
//            NumericHelper helper = new NumericHelper(mContext);

            if (currency == null) {
                // no currency
                return value.toString();
                // we can not simply cut off the decimals!
//                result = String.format("%.2f", value);
            } else {
                // formatted value
                FormatUtilities formats = new FormatUtilities(getContext());
                result = formats.format(value, currency);
            }
        } else {
            result = String.valueOf(value);
        }
        return result;
    }

    private CurrencyRepository oldRepository;
    public CurrencyRepository getRepository() {
        if (oldRepository == null) {
            oldRepository = new CurrencyRepository(getContext());
        }
        return oldRepository;
    }

    /**
     * Import all currencies from Android System
     */
    public boolean importCurrenciesFromSystemLocales() {
        Locale[] locales = Locale.getAvailableLocales();
        // get map codes and symbols
        HashMap<String, String> symbols = getCurrenciesCodeAndSymbol();
        java.util.Currency localeCurrency;
        Currency newCurrency;

        for (Locale locale : locales) {
            try {
                String country = locale.getCountry();   // ISO code
                // String displayCountry = locale.getDisplayCountry(); // full country name
                if (TextUtils.isEmpty(country)) continue;

                localeCurrency = java.util.Currency.getInstance(locale);

                // check if already exists currency symbol
                if (mRepository.exists(localeCurrency.getCurrencyCode())) continue;

                // No currency. Create a new one.

                newCurrency = new Currency();

                // Name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    newCurrency.setName(localeCurrency.getDisplayName());
                } else {
                    newCurrency.setName(localeCurrency.getSymbol());
                }

                // Symbol
                newCurrency.setCode(localeCurrency.getCurrencyCode());

                if (symbols != null && symbols.containsKey(localeCurrency.getCurrencyCode())) {
                    newCurrency.setPfxSymbol(symbols.get(localeCurrency.getCurrencyCode()));
                }

                newCurrency.setDecimalPoint(".");
                newCurrency.setGroupSeparator(",");

                int scale = (int) Math.pow((double)10, (double) localeCurrency.getDefaultFractionDigits());
                newCurrency.setScale(scale);

                newCurrency.setConversionRate(1.0);

                getContext().getContentResolver().insert(getRepository().getUri(), newCurrency.contentValues);
                //todo mRepository.insert(newCurrency.contentValues);
            } catch (Exception e) {
                Timber.e(e, "importing currencies from locale %s", locale.getDisplayName());
            }
        }

        return true;
    }

    /**
     * Checks if the currency is used in any of the accounts. Useful before deletion.
     *
     * @param currencyId Id of the currency to check.
     * @return A boolean indicating if the currency is in use.
     */
    public boolean isCurrencyUsed(int currencyId) {
        AccountRepository accountRepository = new AccountRepository(getContext());
        return accountRepository.anyAccountsUsingCurrency(currencyId);
    }

    /**
     * Loads id of base currency
     *
     * @return Id base currency
     */
    protected Integer loadBaseCurrencyId() {
        InfoService infoService = new InfoService(getContext());
        String currencyString = infoService.getInfoValue(InfoKeys.BASECURRENCYID);
        Integer currencyId = null;

        if (!TextUtils.isEmpty(currencyString)) {
            currencyId = Integer.parseInt(currencyString);
        }

        return currencyId;
    }

    public java.util.Currency getSystemDefaultCurrency() {
        java.util.Currency currency = null;

        Locale defaultLocale = MmexApplication.getApp().getAppLocale();

        try {
            if (defaultLocale == null) {
                defaultLocale = Locale.getDefault();
            }
            // Check if there is a country.
            if (!TextUtils.isEmpty(defaultLocale.getCountry() )) {
                currency = java.util.Currency.getInstance(defaultLocale);
            }
            // Otherwise no country info in the locale. Just use the default.

        } catch (Exception ex) {
            if (!(ex instanceof IllegalArgumentException)) {
                String message = "getting default system currency";
                if (defaultLocale != null) {
                    message += " for " + defaultLocale.getCountry();
                }
                Timber.e(ex, message);
            }
            // else, just ignore Currency parsing exception and use the pre-set currency below.
            // http://docs.oracle.com/javase/7/docs/api/java/util/Currency.html#getInstance(java.util.Locale)
            // IllegalArgumentException - if the country of the given locale is not a supported ISO 3166 country code.
        }

        // can't get the default currency?
        if (currency == null) {
            currency = java.util.Currency.getInstance(Locale.GERMANY);
        }

        return currency;
    }

    /**
     * Fetches a currency by symbol.
     * Ran on database creation during OpenHelper instantiation.
     * todo Test if any advanced helpers can be used at that stage?
     * @param db    Database instance.
     * @param currencySymbol Currency symbol to load.
     * @return Id of the currency with the given symbol.
     */
    public int loadCurrencyIdFromSymbolRaw(SQLiteDatabase db, String currencySymbol) {
        int result = Constants.NOT_SET;

        // Cannot use any other db source here as this happens on database creation!

        Cursor cursor = db.query(CurrencyRepositorySql.TABLE_NAME,
                new String[]{ Currency.CURRENCYID },
                Currency.CURRENCY_SYMBOL + "=?",
                new String[]{ currencySymbol },
                null, null, null);

        if (cursor == null) return result;

        // set BaseCurrencyId
        if (cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(Currency.CURRENCYID));
        }
        cursor.close();

        return result;
    }

    public boolean saveExchangeRate(String symbol, Money rate) {
        CurrencyRepository repo = getRepository();

        Currency currency = repo.loadCurrency(symbol);
        int currencyId = currency.getCurrencyId();

        // update value on database
        int updateResult = repo.saveExchangeRate(currencyId, rate);

        return updateResult > 0;
    }

    public void updateExchangeRate(int currencyId) {
        List<Currency> currencies = new ArrayList<>();
        currencies.add(getCurrency(currencyId));

        updateExchangeRates(currencies);
    }

    public void updateExchangeRates(List<Currency> currencies){
        if (currencies == null || currencies.size() <= 0) return;

        String symbol;
        String baseCurrencySymbol = getBaseCurrencyCode();
        ArrayList<String> currencySymbols = new ArrayList<>();

        for (Currency currency : currencies) {
            symbol = currency.getCode();
            if (symbol == null) continue;
            if (symbol.equals(baseCurrencySymbol)) continue;

            // todo: move this into yahoo currency exchange rates provider (new file).
//            currencySymbols.add(symbol + baseCurrencySymbol + "=X");
            currencySymbols.add(symbol);
        }

        IExchangeRateUpdater updater = ExchangeRateUpdaterFactory.getUpdaterInstance(getContext());
        updater.downloadPrices(baseCurrencySymbol, currencySymbols);
        // result received via event
    }

    // Private

    /**
     * @return a hash map of currency code / currency symbol
     */
    private HashMap<String, String> getCurrenciesCodeAndSymbol() {
        HashMap<String, String> map = new HashMap<>();
        // compose map
        String[] codes = getContext().getResources().getStringArray(R.array.currencies_code);
        String[] symbols = getContext().getResources().getStringArray(R.array.currencies_symbol);

        for (int i = 0; i < codes.length; i++) {
            map.put(codes[i], symbols[i]);
        }

        return map;
    }

}
