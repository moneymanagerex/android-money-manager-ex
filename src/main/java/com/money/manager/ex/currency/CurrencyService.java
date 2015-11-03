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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Currency;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * This class implements all the methods of utility for the management of currencies.
 */
public class CurrencyService {

    private static Integer mBaseCurrencyId = null;
    // hash map of all currencies
    private static Map<Integer, Currency> mCurrencies;
    /**
     * a fast lookup for symbol -> id. i.e. EUR->2.
     */
    private static HashMap<String, Integer> mCurrencyCodes;

    public static void destroy() {
        mCurrencies = null;
        mCurrencyCodes = null;
        mBaseCurrencyId = null;
    }

    public CurrencyService(Context context) {
        mContext = context.getApplicationContext();
    }

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    /**
     * @param currencyId of the currency to be get
     * @return an instance of class TableCurrencyFormats. Null if fail
     */
    public Currency getCurrency(Integer currencyId) {
        if (currencyId == null || currencyId == Constants.NOT_SET) return null;

        // check if the currency is cached.
        Currency result =  getCurrenciesStore().get(currencyId);

        // if not cached, try to load it.
        if (result == null) {
            CurrencyRepository repository = new CurrencyRepository(mContext);
            result = repository.loadCurrency(currencyId);

            // cache
            if (result != null && !getCurrenciesStore().containsKey(currencyId)) {
                cacheCurrency(result);
            }
        }

        return result;
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
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.showMessage(mContext.getString(R.string.base_currency_not_set));
            return null;
        }
        return currency.getCode();
    }

    public String getSymbolFor(int id) {
        Currency currency = getCurrency(id);
        return currency.getCode();
    }

    public Integer getIdForCode(String code) {
        Integer result = getCurrencyCodes().get(code);

        if (result == null) {
            CurrencyRepository repo = new CurrencyRepository(getContext());
            Currency currency = repo.loadCurrency(code);
            cacheCurrency(currency);
        }

        result = getCurrencyCodes().get(code);

        return result;
    }

//    public Boolean reInit() {
//        destroy();
//
//        return loadAllCurrencies();
//    }

    public Map<Integer, Currency> getCurrenciesStore() {
        if (mCurrencies == null) mCurrencies = new HashMap<>();
        return mCurrencies;
    }

    public HashMap<String, Integer> getCurrencyCodes() {
        if (mCurrencyCodes == null) {
            mCurrencyCodes = new HashMap<>();
        }
        return mCurrencyCodes;
    }

    public List<Currency> getUsedCurrencies() {
        AccountService service = new AccountService(mContext);

        List<Account> accounts = service.getAccountList();
        if (accounts == null) return null;

        List<Currency> currencies = new ArrayList<>();

        for(Account account : accounts) {
            Currency currency = getCurrency(account.getCurrencyId());
            if (!currencies.contains(currency)) {
                currencies.add(currency);
            }
        }

        return currencies;
    }

    public Money doCurrencyExchange(Integer toCurrencyId, Money amount, Integer fromCurrencyId) {
        if (toCurrencyId == null || fromCurrencyId == null) return amount;

        // handle same currencies
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
     * Get id of base currency.
     * Lazy loaded, no need to initialize separately.
     *
     * @return Id of base currency
     */
    public int getBaseCurrencyId() {
        int result;

        // lazy loading the base currency id.
        if (mBaseCurrencyId == null) {
            Integer baseCurrencyId = loadBaseCurrencyId();
            if(baseCurrencyId != null) {
                setBaseCurrencyId(baseCurrencyId);
                result = baseCurrencyId;
            } else {
                // No base currency set yet. Try to get it from the system.
                java.util.Currency systemCurrency = this.getSystemDefaultCurrency();
                if (systemCurrency == null) {
                    // could not get base currency from the system. Use Euro.
                    //Currency euro = repo.loadCurrency("EUR");
                    //result = euro.getCurrencyId();
                    result = 2;
                } else {
                    CurrencyRepository repo = new CurrencyRepository(getContext());
                    Currency defaultCurrency = repo.loadCurrency(systemCurrency.getCurrencyCode());
                    result = defaultCurrency.getCurrencyId();
                }
            }
        } else {
            result = mBaseCurrencyId;
        }

        setBaseCurrencyId(result);
        return result;
    }

    public void setBaseCurrencyId(int baseCurrencyId) {
        mBaseCurrencyId = baseCurrencyId;
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
     * @param currencyId of the currency to be formatted
     * @param value      value to format
     * @return formatted value
     */
    public String getCurrencyFormatted(Integer currencyId, Money value) {
        String result;

        // check if value is null
        if (value == null) value = MoneyFactory.fromString("0");

        // find currency id
        if (currencyId != null) {
            Currency tableCurrency = getCurrency(currencyId);
//            NumericHelper helper = new NumericHelper(mContext);

            if (tableCurrency == null) {
                // no currency
                return value.toString();
                // we can not simply cut off the decimals!
//                result = String.format("%.2f", value);
            } else {
                // formatted value
                FormatUtilities formats = new FormatUtilities(getContext());
                result = formats.getValueFormatted(value, tableCurrency);
            }
        } else {
            result = String.valueOf(value);
        }
        return result;
    }

    public boolean importCurrenciesFromAvailableLocales() {
        Locale[] locales = Locale.getAvailableLocales();
        // get map codes and symbols
        HashMap<String, String> symbols = getCurrenciesCodeAndSymbol();
        java.util.Currency localeCurrency;
        Currency newCurrency;
        CurrencyRepository repo = new CurrencyRepository(getContext());
        Currency existingCurrency;

        for (Locale locale : locales) {
            try {
                localeCurrency = java.util.Currency.getInstance(locale);

                // check if already exists currency symbol
                existingCurrency = repo.loadCurrency(localeCurrency.getCurrencyCode());

                if (existingCurrency != null) continue;

                // No currency. Create a new one.

                newCurrency = new Currency();

                newCurrency.setName(localeCurrency.getDisplayName());
                newCurrency.setCode(localeCurrency.getCurrencyCode());

                if (symbols != null && symbols.containsKey(localeCurrency.getCurrencyCode())) {
                    newCurrency.setPfxSymbol(symbols.get(localeCurrency.getCurrencyCode()));
                }

                newCurrency.setDecimalPoint(".");
                newCurrency.setGroupSeparator(",");
                newCurrency.setScale(100);
                newCurrency.setConversionRate(1.0);

                repo.insert(newCurrency);
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(getContext(), this);
                handler.handle(e, "importing currencies from locale " + locale.getDisplayName());
            }
        }

        return true;
    }

    /**
     * Checks if the currency is used in any of the accounts. Useful before deletion.
     * @param currencyId Id of the currency to check.
     * @return A boolean indicating if the currency is in use.
     */
    public boolean isCurrencyUsed(int currencyId) {
        AccountRepository accountRepository = new AccountRepository(getContext());
        return accountRepository.anyAccountsUsingCurrency(currencyId);
    }

    /**
     * Update database with new Base Currency Id
     *
     * @param currencyId of the currency
     * @return true if update succeed, otherwise false
     */
    public Boolean saveBaseCurrencyId(Integer currencyId) {

        InfoService infoService = new InfoService(getContext());
        boolean success = infoService.setInfoValue(InfoService.BASECURRENCYID, Integer.toString(currencyId));

        // cache the new base currency
        if (success) {
            mBaseCurrencyId = currencyId;
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.error_saving_default_currency), Toast.LENGTH_SHORT)
                    .show();
        }

        return success;
    }

    /**
     * Get id of base currency
     *
     * @return Id base currency
     */
    protected Integer loadBaseCurrencyId() {
        InfoService infoService = new InfoService(mContext);
        String currencyString = infoService.getInfoValue(InfoService.BASECURRENCYID);
        Integer currencyId = null;

        if(!StringUtils.isEmpty(currencyString)) {
            currencyId = Integer.parseInt(currencyString);
        }

        return currencyId;
    }

    public java.util.Currency getSystemDefaultCurrency() {
        java.util.Currency currency = null;

        Locale defaultLocale = MoneyManagerApplication.getInstanceApp().getAppLocale();

        try {
            if (defaultLocale == null) {
                defaultLocale = Locale.getDefault();
            }
            currency = java.util.Currency.getInstance(defaultLocale);
        } catch (Exception ex) {
            String message = "getting default system currency";
            if (defaultLocale != null) {
                message += " for " + defaultLocale.getCountry();
            }
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, message);
        }
        return currency;
    }

    public int loadCurrencyIdFromSymbolRaw(SQLiteDatabase db, String currencySymbol) {
        int result = Constants.NOT_SET;
        TableCurrencyFormats currencyEntity = new TableCurrencyFormats();

        Cursor cursor = db.query(currencyEntity.getSource(),
            new String[] { Currency.CURRENCYID },
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

    /**
     *
     * @return a hash map of currency code / currency symbol
     */
    private HashMap<String, String> getCurrenciesCodeAndSymbol() {
        HashMap<String, String> map = new HashMap<>();
        // compose map
        String[] codes = mContext.getResources().getStringArray(R.array.currencies_code);
        String[] symbols = mContext.getResources().getStringArray(R.array.currencies_symbol);

        for (int i = 0; i < codes.length; i++) {
            map.put(codes[i], symbols[i]);
        }

        return map;
    }

    private void cacheCurrency(Currency currency) {
        // add currency
        getCurrenciesStore().put(currency.getCurrencyId(), currency);
        // add symbol
        getCurrencyCodes().put(currency.getCode(), currency.getCurrencyId());
    }
}
