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
package org.moneymanagerex.android.tests;

import android.content.Context;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.currency.CurrencyRepository;
import com.money.manager.ex.currency.MmexExchangeRateProvider;
import com.money.manager.ex.domainmodel.Currency;

import org.javamoney.moneta.FastMoney;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.RoundedMoney;
import org.javamoney.moneta.function.MonetaryFunctions;
import org.javamoney.moneta.function.MonetaryUtil;
import org.javamoney.moneta.internal.convert.ECBCurrentRateProvider;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryAmountFactory;
import javax.money.MonetaryOperator;
import javax.money.MonetaryRounding;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import javax.money.spi.MonetaryRoundingsSingletonSpi;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing the new JavaMoney library and types.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class JavaMoneyTests {

    @Before
    public void setUp() {
        UnitTestHelper.initializeContentProvider();
    }

    @After
    public void tearDown() {
        UnitTestHelper.resetDatabase();
    }

    @Test
    public void createInstances() {
        // currency
        CurrencyUnit euro = Monetary.getCurrency("EUR");
        assertThat(euro.getCurrencyCode()).isEqualTo("EUR");

        CurrencyUnit aussie = Monetary.getCurrency("AUD");

        //MonetaryAmount amount = 20;

        Money money = Money.of(150, euro);
        assertThat(money.getCurrency().getCurrencyCode()).isEqualTo("EUR");

//        Money division = money.divide(2.54);
//        Log.d("test", division.toString());

        FastMoney fast = FastMoney.of(250, euro);
        assertThat(fast.getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(fast.getNumber().toString()).isEqualTo("250.00000");
        assertThat(fast.getPrecision()).isEqualTo(2);
        assertThat(fast.getScale()).isEqualTo(5);

        // maximum size for fast money
        // FastMoney maxFast = FastMoney.MAX_VALUE;

        // Conversion

//        ExchangeRateProvider provider = MonetaryConversions.getExchangeRateProvider("ECB");
//        ExchangeRateProvider provider = new
//        ExchangeRate rate = provider.getExchangeRate("USD", "EUR");
//        CurrencyConversion euraud = provider.getCurrencyConversion(aussie);
//        FastMoney aud = fast.with(euraud);

        // MonetaryConversions.

        Log.d("test", "the end, debug manually");
    }

    @Test
    public void otherOperations() {
        CurrencyUnit euro = Monetary.getCurrency("EUR");

        MonetaryAmount amount = FastMoney.of(150, euro);
        FastMoney money = FastMoney.of(150, "EUR");
        assertThat(amount).isEqualTo(money);

        FastMoney third = money.divide(3);
        assertThat(third).isEqualTo(FastMoney.of(50, euro));

        MonetaryOperator tenPercent = MonetaryUtil.percent(10);
        FastMoney tenPercentValue = money.with(tenPercent);
        assertThat(tenPercentValue).isEqualTo(FastMoney.of(15, "EUR"));

        // excellent comparison functions:
        //money.isZero()

        //MonetaryFunctions

        // Rounding!

        MonetaryRounding rounding = Monetary.getRounding(euro);
        MonetaryAmount rounded = amount.divide(4.5).with(rounding);

        Log.d("test", "the end, debug manually");
    }

    @Test
    public void conversion() {
        // Given

        Context context = UnitTestHelper.getContext();
        prepareCurrencies();

        // When

        CurrencyUnit euro = Monetary.getCurrency("EUR");
        MonetaryAmount euros = FastMoney.of(100, euro);

        ExchangeRateProvider provider = new MmexExchangeRateProvider(context);

        CurrencyConversion audConversion = provider.getCurrencyConversion("AUD");
        ExchangeRate rate = audConversion.getExchangeRate(euros);

        assertThat(rate.getFactor().compareTo(DefaultNumberValue.of(2.0))).isEqualTo(0);

        ExchangeRate audeur = provider.getExchangeRate("EUR", "AUD");
        assertThat(audeur.getFactor().compareTo(DefaultNumberValue.of(2.0))).isEqualTo(0);

        // requires network connection
//        CurrencyConversion conversion = MonetaryConversions.getConversion("EUR");
//        CurrencyConversion audConversion = MonetaryConversions.getConversion("AUD");
        MonetaryAmount aussies = euros.with(audConversion);
        assertThat(aussies).isEqualTo(FastMoney.of(200.0, "AUD"));

        Log.d("test", "the end, debug manually");
    }

//    @Test
    public void collections() {
        List<MonetaryAmount> amounts = new ArrayList<>();
        amounts.add(Money.of(2, "EUR"));
        amounts.add(Money.of(42, "USD"));
        amounts.add(Money.of(7, "USD"));
        amounts.add(Money.of(13.37, "JPY"));
        amounts.add(Money.of(18, "USD"));
//        amounts.stream().

        // money.isBetween

        //amounts.sum
    }

    @Test
    public void conversionOverBaseCurrency() {
        // Given

        Context context = UnitTestHelper.getContext();
        prepareCurrencies();

        // When

        MonetaryAmount dollars = FastMoney.of(100, "USD");

        ExchangeRateProvider provider = new MmexExchangeRateProvider(context);
        CurrencyConversion audConversion = provider.getCurrencyConversion("AUD");

        MonetaryAmount aussies = dollars.with(audConversion);

        // Then

        assertThat(aussies).isEqualTo(FastMoney.of(133.33, "AUD"));
    }

    private void prepareCurrencies() {
        Context context = UnitTestHelper.getContext();
        CurrencyRepository repo = new CurrencyRepository(context);

        // set AUD rate to 2.0
        Currency aud = repo.loadCurrency("AUD");
        aud.setConversionRate(2.0);
        boolean saved = repo.update(aud);
        assertThat(saved).isTrue();

        // Set USD to 1.5
        Currency usd = repo.loadCurrency("USD");
        usd.setConversionRate(1.5);
        saved = repo.update(usd);
        assertThat(saved).isTrue();

    }
}
