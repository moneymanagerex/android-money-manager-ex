///*
// * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
// *
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package org.moneymanagerex.android.tests;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.money.manager.ex.BuildConfig;
//import com.money.manager.ex.currency.CurrencyRepository;
//import com.money.manager.ex.currency.MmexExchangeRateProvider;
//import com.money.manager.ex.domainmodel.Currency;
//
//import org.javamoney.moneta.FastMoney;
//import org.javamoney.moneta.Money;
//import org.javamoney.moneta.RoundedMoney;
//import org.javamoney.moneta.function.MonetaryFunctions;
//import org.javamoney.moneta.function.MonetaryUtil;
//import org.javamoney.moneta.spi.DefaultNumberValue;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.moneymanagerex.android.testhelpers.UnitTestHelper;
//import org.robolectric.RobolectricGradleTestRunner;
//import org.robolectric.annotation.Config;
//
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//import javax.money.CurrencyUnit;
//import javax.money.Monetary;
//import javax.money.MonetaryAmount;
//import javax.money.MonetaryOperator;
//import javax.money.MonetaryRounding;
//import javax.money.RoundingQueryBuilder;
//import javax.money.convert.CurrencyConversion;
//import javax.money.convert.ExchangeRate;
//import javax.money.convert.ExchangeRateProvider;
//import javax.money.format.MonetaryAmountFormat;
//import javax.money.format.MonetaryFormats;
//
//import info.javaperformance.money.MoneyFactory;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Testing the new JavaMoney library and types.
// */
//@RunWith(RobolectricGradleTestRunner.class)
//@Config(constants = BuildConfig.class)
//public class JavaMoneyTests {
//
//    @Before
//    public void setUp() {
//        UnitTestHelper.setupContentProvider();
//    }
//
//    @After
//    public void tearDown() {
//        UnitTestHelper.teardownDatabase();
//    }
//
//    @Test
//    public void createInstances() {
//        // currency
//        CurrencyUnit euro = Monetary.getCurrency("EUR");
//        assertThat(euro.getCurrencyCode()).isEqualTo("EUR");
//
//        CurrencyUnit aussie = Monetary.getCurrency("AUD");
//
//        //MonetaryAmount amount = 20;
//
//        Money money = Money.of(150, euro);
//        assertThat(money.getCurrency().getCurrencyCode()).isEqualTo("EUR");
//
////        Money division = money.divide(2.54);
////        Log.d("test", division.toString());
//
//        FastMoney fast = FastMoney.of(250, euro);
//        assertThat(fast.getCurrency().getCurrencyCode()).isEqualTo("EUR");
//        assertThat(fast.getNumber().toString()).isEqualTo("250.00000");
//        assertThat(fast.getPrecision()).isEqualTo(2);
//        assertThat(fast.getScale()).isEqualTo(5);
//
//        // maximum size for fast money
//        // FastMoney maxFast = FastMoney.MAX_VALUE;
//
//        //MonetaryAmount unknown = new FastMoneyAmountBuilder().setNumber(30).create();
//
//        Log.d("test", "the end, debug manually");
//    }
//
//    @Test
//    public void otherOperations() {
//        CurrencyUnit euro = Monetary.getCurrency("EUR");
//
//        MonetaryAmount amount = FastMoney.of(150, euro);
//        FastMoney money = FastMoney.of(150, "EUR");
//        assertThat(amount).isEqualTo(money);
//
//        FastMoney third = money.divide(3);
//        assertThat(third).isEqualTo(FastMoney.of(50, euro));
//
//        MonetaryOperator tenPercent = MonetaryUtil.percent(10);
//        FastMoney tenPercentValue = money.with(tenPercent);
//        assertThat(tenPercentValue).isEqualTo(FastMoney.of(15, "EUR"));
//
//        // excellent comparison functions:
//        //money.isZero()
//
//        //MonetaryFunctions
//
//        Log.d("test", "the end, debug manually");
//    }
//
//    @Test
//    public void conversion() {
//        // Given
//
//        Context context = UnitTestHelper.getContext();
//        prepareCurrencies();
//
//        // When
//
//        CurrencyUnit euro = Monetary.getCurrency("EUR");
//        MonetaryAmount euros = FastMoney.of(100, euro);
//
//        ExchangeRateProvider provider = new MmexExchangeRateProvider(context);
//
//        CurrencyConversion audConversion = provider.getCurrencyConversion("AUD");
//        ExchangeRate rate = audConversion.getExchangeRate(euros);
//
//        assertThat(rate.getFactor().compareTo(DefaultNumberValue.of(2.0))).isEqualTo(0);
//
//        ExchangeRate audeur = provider.getExchangeRate("EUR", "AUD");
//        assertThat(audeur.getFactor().compareTo(DefaultNumberValue.of(2.0))).isEqualTo(0);
//
//        // requires network connection
////        CurrencyConversion conversion = MonetaryConversions.getConversion("EUR");
////        CurrencyConversion audConversion = MonetaryConversions.getConversion("AUD");
//        MonetaryAmount aussies = euros.with(audConversion);
//        assertThat(aussies).isEqualTo(FastMoney.of(200.0, "AUD"));
//
//        // check MonetaryConversions.getConversion("EUR", "AUD");
//
//        Log.d("test", "the end, debug manually");
//    }
//
////    @Test
//    public void collections() {
//        List<MonetaryAmount> amounts = new ArrayList<>();
//        amounts.add(Money.of(2, "EUR"));
//        amounts.add(Money.of(42, "USD"));
//        amounts.add(Money.of(7, "USD"));
//        amounts.add(Money.of(13.37, "JPY"));
//        amounts.add(Money.of(18, "USD"));
////        amounts.stream().
//
//        // money.isBetween
//
//        //amounts.sum
//    }
//
//    @Test
//    public void conversionOverBaseCurrency() {
//        // Given
//
//        Context context = UnitTestHelper.getContext();
//        prepareCurrencies();
//
//        // When
//
//        MonetaryAmount dollars = FastMoney.of(100, "USD");
//
//        ExchangeRateProvider provider = new MmexExchangeRateProvider(context);
//        CurrencyConversion audConversion = provider.getCurrencyConversion("AUD");
//
//        MonetaryAmount aussies = dollars.with(audConversion);
//
//        // Then
//
//        assertThat(aussies).isEqualTo(FastMoney.of(133.33, "AUD"));
//    }
//
//    private void prepareCurrencies() {
//        Context context = UnitTestHelper.getContext();
//        CurrencyRepository repo = new CurrencyRepository(context);
//
//        // set AUD rate to 2.0
//        Currency aud = repo.loadCurrency("AUD");
//        aud.setConversionRate(2.0);
//        boolean saved = repo.update(aud);
//        assertThat(saved).isTrue();
//
//        // Set USD to 1.5
//        Currency usd = repo.loadCurrency("USD");
//        usd.setConversionRate(1.5);
//        saved = repo.update(usd);
//        assertThat(saved).isTrue();
//
//    }
//
//    @Test
//    public void valueConversion() {
//        // Given
//
//        MonetaryAmount money = FastMoney.of(235.243, "EUR");
//
//        // When
//
//        String text = money.toString();
//        MonetaryAmount newAmount = FastMoney.parse(text);
//
////        double x = money.getNumber().doubleValueExact();  -> 235.243
////        double y = money.getNumber().doubleValue();       -> 235.24300
//
//        // Then
//
//        assertThat(text).isEqualTo("EUR 235.24300");
//        assertThat(newAmount).isEqualTo(FastMoney.of(235.243, "EUR"));
//    }
//
//    @Test
//    public void formatting() {
//        // Given
//
//        MonetaryAmount amount = FastMoney.of(3162.24523, "EUR");
//        MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(Locale.GERMANY);
//
//        // When
//
//        String actual = format.format(amount);
//
//        // Then
//
//        assertThat(actual).isEqualTo("3.162,25 EUR");
//    }
//
//    @Test
//    public void extremeValues() {
//        // todo: These values have XXX as currency. Use this wheen needing numbers only.
//
//        CurrencyUnit xxx = Monetary.getCurrency("XXX");
//
//        MonetaryAmount minimum = FastMoney.MIN_VALUE;
////        assertThat(minimum).isEqualTo(FastMoney.of(-92233720368547.75808, xxx)); <- overflow
//        assertThat(minimum.toString()).isEqualTo("XXX -92233720368547.75808");
//
//        MonetaryAmount maximum = FastMoney.MAX_VALUE;
//        assertThat(maximum.toString()).isEqualTo("XXX 92233720368547.75807");
//
//        MonetaryAmount zero = FastMoney.of(0, xxx);
//        assertThat(zero.isZero()).isTrue();
//
//        MonetaryAmount random = FastMoney.of(358.46, xxx);
//        MonetaryAmount copy  = FastMoney.from(random);
//        assertThat(copy).isEqualTo(random);
//
//        // change currency
//        MonetaryAmount euros = FastMoney.of(random.getNumber(), "EUR");
//        assertThat(euros.getNumber().doubleValueExact()).isEqualTo(random.getNumber().doubleValueExact());
//        assertThat(euros.getCurrency()).isNotEqualTo(random.getCurrency());
//
//        // comparison
//        assertThat(random.isLessThan(maximum)).isTrue();
//        // can't compare different currencies
//        // assertThat(euros.isLessThan(maximum)).isTrue();
//    }
//
//    @Test
//    public void rounding() {
//        CurrencyUnit euro = Monetary.getCurrency("EUR");
//
//        // Rounding to currency defaults.
//
//        MonetaryAmount amount = FastMoney.of(150.545, euro);
//        MonetaryRounding rounding = Monetary.getRounding(euro);
//        MonetaryAmount rounded = amount.divide(4.5).with(rounding);
//        assertThat(rounded.toString()).isEqualTo("EUR 33.33");
//
//        // Custom. Arithmetic.
//        MonetaryRounding custom = Monetary.getRounding(RoundingQueryBuilder.of()
//            .setScale(2).set(RoundingMode.HALF_UP).build());
//        assertThat(amount.with(custom).toString()).isEqualTo("EUR 150.54");
//
//        // Custom. Cash.
////        MonetaryRounding cash = Monetary.getRounding(RoundingQueryBuilder.of()
////            .set(RoundingType.CASH));
//
//        // Default
//        MonetaryRounding defaultRounding = Monetary.getDefaultRounding();
//        assertThat(amount.with(defaultRounding).toString()).isEqualTo("EUR 150.55");
//    }
//
//    @Test
//    public void conversionToFromMoney() {
//        // Given
//        String currencyCode = "EUR";
//        CurrencyUnit euro = Monetary.getCurrency(currencyCode);
//        MonetaryAmount moneta = FastMoney.of(125.14, currencyCode);
//        info.javaperformance.money.Money money = MoneyFactory.fromString("625.384");
////        MonetaryRounding defaultRounding = Monetary.getDefaultRounding();
//
//        // to money, use string?
//        info.javaperformance.money.Money destination = MoneyFactory.fromString(moneta.getNumber().toString());
//        assertThat(destination.toString()).isEqualTo("125.14");
//
//        // Conversion from money. Round to currency settings.
//        MonetaryRounding currencyRounding = Monetary.getRounding(euro);
//        MonetaryAmount createdMoneta = FastMoney.of(money.toDouble(), currencyCode)
//            .with(currencyRounding);
//        assertThat(createdMoneta.getNumber().doubleValueExact()).isEqualTo(625.38);
//    }
//}
