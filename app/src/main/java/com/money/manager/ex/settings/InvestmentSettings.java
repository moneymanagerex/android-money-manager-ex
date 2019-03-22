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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.investment.ExchangeRateProviders;
import com.money.manager.ex.investment.QuoteProviders;
import com.money.manager.ex.servicelayer.InfoService;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Investment preferences / preferences.
 */
public class InvestmentSettings
    extends SettingsBase {

    public InvestmentSettings(Context context) {
        super(context);

    }

    @Override
    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /**
     * Threshold percentage at which the difference in actual asset allocation vs set asset
     * allocation will be painted. Green, if the current allocation is higher than the set allocation,
     * and red if it is smaller for the set percentage of the original value.
     * I.e. 20 represents 20% difference compared to the set asset allocation value.
     * @return A number that represents the percentage value.
     */
    public Money getAssetAllocationDifferenceThreshold() {
        InfoService service = new InfoService(getContext());
        String value = service.getInfoValue(InfoKeys.ASSET_ALLOCATION_DIFF_THRESHOLD);

        if (TextUtils.isEmpty(value)) {
            value = Integer.toString(Constants.NOT_SET); // "-1";
        }
        return MoneyFactory.fromString(value);
    }

    public void setAssetAllocationDifferenceThreshold(Money value) {
        InfoService service = new InfoService(getContext());
        service.setInfoValue(InfoKeys.ASSET_ALLOCATION_DIFF_THRESHOLD, value.toString());
    }

    public QuoteProviders getQuoteProvider() {
        QuoteProviders defaultValue = QuoteProviders.YahooYql;

        InfoService service = new InfoService(getContext());
        String value = service.getInfoValue(InfoKeys.QUOTE_PROVIDER);
        if (value == null) {
            return defaultValue;
        }

        QuoteProviders provider = QuoteProviders.valueOf(value);
        // default value returned if none set.
        return provider != null ? provider : defaultValue;
    }

    public void setQuoteProvider(QuoteProviders value) {
        InfoService service = new InfoService(getContext());
        service.setInfoValue(InfoKeys.QUOTE_PROVIDER, value.name());
    }

    public ExchangeRateProviders getExchangeRateProvider() {
        ExchangeRateProviders defaultValue = ExchangeRateProviders.Fixer;

        InfoService service = new InfoService(getContext());
        String value = service.getInfoValue(InfoKeys.EXCHANGE_RATE_PROVIDER);
        if (value == null) {
            return defaultValue;
        }

        ExchangeRateProviders provider = null;
        try {
            provider = ExchangeRateProviders.valueOf(value);
        } catch (Exception e) {
            Timber.e(e);
        }

        // default value returned if none set.
        return provider != null ? provider : defaultValue;
    }

    public void setExchangeRateProvider(ExchangeRateProviders value) {
        InfoService service = new InfoService(getContext());
        service.setInfoValue(InfoKeys.EXCHANGE_RATE_PROVIDER, value.name());
    }

}
