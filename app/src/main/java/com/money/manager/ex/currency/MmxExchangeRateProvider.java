///*
// * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
//package com.money.manager.ex.currency;
//
//import android.content.Context;
//
//import com.android.annotations.NonNull;
//import com.money.manager.ex.domainmodel.Currency;
//
//import org.javamoney.moneta.ExchangeRateBuilder;
//import org.javamoney.moneta.spi.AbstractRateProvider;
//import org.javamoney.moneta.spi.DefaultNumberValue;
//
//import java.math.MathContext;
//import java.util.Objects;
//
//import javax.money.CurrencyUnit;
//import javax.money.Monetary;
//import javax.money.MonetaryException;
//import javax.money.MonetaryOperator;
//import javax.money.NumberValue;
//import javax.money.convert.ConversionContext;
//import javax.money.convert.ConversionContextBuilder;
//import javax.money.convert.ConversionQuery;
//import javax.money.convert.ConversionQueryBuilder;
//import javax.money.convert.CurrencyConversion;
//import javax.money.convert.CurrencyConversionException;
//import javax.money.convert.ExchangeRate;
//import javax.money.convert.ExchangeRateProvider;
//import javax.money.convert.ProviderContext;
//import javax.money.convert.ProviderContextBuilder;
//import javax.money.convert.RateType;
//
///**
// * Exchange Rate Provider for use with JavaMoney.
// */
//public class MmxExchangeRateProvider
//    extends AbstractRateProvider
//    implements ExchangeRateProvider {
//
//    /**
//     * The {@link ConversionContext} of this provider.
//     */
//    private static final ProviderContext CONTEXT =
//            ProviderContextBuilder.of("MMX", RateType.ANY)
//                    .set("providerDescription", "MoneyManagerEx for Android")
//                    .build();
//
//    public MmxExchangeRateProvider(Context context) {
//        super(CONTEXT);
//        // ProviderContext context
//
//        // load base currency code.
//        this.BASE_CURRENCY_CODE = loadBaseCurrencyCode(context);
//        this.context = context;
//    }
//
//    private String BASE_CURRENCY_CODE;
//    private Context context;
//
//    /**
//     * Access the {@link ConversionContext} for this ExchangeRateProvider. Each instance of
//     * ExchangeRateProvider provides conversion data for exact one
//     * {@link ConversionContext} .
//     *
//     * @return the exchange rate type, never {@code null}.
//     */
//    @Override
//    public ProviderContext getContext() {
//        return super.getContext();
//    }
//
//    /**
//     * Access a {@link ExchangeRate} using the given currencies. The
//     * {@link ExchangeRate} may be, depending on the data provider, eal-time or
//     * deferred. This method should return the rate that is <i>currently</i>
//     * valid.
//     *
//     * @param conversionQuery the required {@link ConversionQuery}, not {@code null}
//     * @return the matching {@link ExchangeRate}.
//     * @throws CurrencyConversionException If no such rate is available.
//     * @throws MonetaryException           if one of the currency codes passed is not valid.
//     * @see ConversionQueryBuilder
//     */
//    @Override
//    public ExchangeRate getExchangeRate(@NonNull ConversionQuery conversionQuery) {
//        ExchangeRateBuilder builder = getBuilder(conversionQuery);
//
//        ExchangeRate sourceRate = createExchangeRate(conversionQuery.getBaseCurrency());
//        ExchangeRate target = createExchangeRate(conversionQuery.getCurrency());
//
//        return createExchangeRate(conversionQuery, builder, sourceRate, target);
//    }
//
//    /**
//     * Access a {@link CurrencyConversion} that can be applied as a
//     * {@link MonetaryOperator} to an amount.
//     *
//     * @param conversionQuery the required {@link ConversionQuery}, not {@code null}
//     * @return a new instance of a corresponding {@link CurrencyConversion},
//     * never {@code null}.
//     * @throws MonetaryException if one of the currency codes passed is not valid.
//     * @see ConversionQueryBuilder
//     */
//    @Override
//    public CurrencyConversion getCurrencyConversion(ConversionQuery conversionQuery) {
//        return super.getCurrencyConversion(conversionQuery);
//    }
//
//    /**
//     * Checks if an {@link ExchangeRate} between two {@link CurrencyUnit} is
//     * available from this provider. This method should check, if a given rate
//     * is <i>currently</i> defined.
//     *
//     * @param conversionQuery the required {@link ConversionQuery}, not {@code null}
//     * @return {@code true}, if such an {@link ExchangeRate} is currently
//     * defined.
//     */
//    @Override
//    public boolean isAvailable(ConversionQuery conversionQuery) {
//        return super.isAvailable(conversionQuery);
//    }
//
//    /**
//     * Access a {@link ExchangeRate} using the given currencies. The
//     * {@link ExchangeRate} may be, depending on the data provider, eal-time or
//     * deferred. This method should return the rate that is <i>currently</i>
//     * valid.
//     *
//     * @param base base {@link CurrencyUnit}, not {@code null}
//     * @param term term {@link CurrencyUnit}, not {@code null}
//     * @throws CurrencyConversionException If no such rate is available.
//     */
//    @Override
//    public ExchangeRate getExchangeRate(CurrencyUnit base, CurrencyUnit term) {
//        return super.getExchangeRate(base, term);
//    }
//
//    /**
//     * Access a {@link CurrencyConversion} that can be applied as a
//     * {@link MonetaryOperator} to an amount.
//     *
//     * @param term term {@link CurrencyUnit}, not {@code null}
//     * @return a new instance of a corresponding {@link CurrencyConversion},
//     * never {@code null}.
//     */
//    @Override
//    public CurrencyConversion getCurrencyConversion(CurrencyUnit term) {
//        // this one is called from base class.
//
//        ConversionQueryBuilder builder = ConversionQueryBuilder.of();
//        builder.setBaseCurrency(BASE_CURRENCY_CODE);
//        builder.setTermCurrency(term);
//        builder.setRateTypes(RateType.ANY);
//
//        ConversionQuery query = builder.build();
//        return getCurrencyConversion(query);
//
////        return null;
//    }
//
//    /**
//     * Checks if an {@link ExchangeRate} between two {@link CurrencyUnit} is
//     * available from this provider. This method should check, if a given rate
//     * is <i>currently</i> defined.
//     *
//     * @param base the base {@link CurrencyUnit}
//     * @param term the term {@link CurrencyUnit}
//     * @return {@code true}, if such an {@link ExchangeRate} is currently
//     * defined.
//     */
//    @Override
//    public boolean isAvailable(CurrencyUnit base, CurrencyUnit term) {
//        return super.isAvailable(base, term);
//    }
//
//    /**
//     * Checks if an {@link ExchangeRate} between two {@link CurrencyUnit} is
//     * available from this provider. This method should check, if a given rate
//     * is <i>currently</i> defined.
//     *
//     * @param baseCode the base currency code
//     * @param termCode the terminal/target currency code
//     * @return {@code true}, if such an {@link ExchangeRate} is currently
//     * defined.
//     * @throws MonetaryException if one of the currency codes passed is not valid.
//     */
//    @Override
//    public boolean isAvailable(String baseCode, String termCode) {
//        return super.isAvailable(baseCode, termCode);
//    }
//
//    /**
//     * Access a {@link ExchangeRate} using the given currencies. The
//     * {@link ExchangeRate} may be, depending on the data provider, eal-time or
//     * deferred. This method should return the rate that is <i>currently</i>
//     * valid.
//     *
//     * @param baseCode base currency code, not {@code null}
//     * @param termCode term/target currency code, not {@code null}
//     * @return the matching {@link ExchangeRate}.
//     * @throws CurrencyConversionException If no such rate is available.
//     * @throws MonetaryException           if one of the currency codes passed is not valid.
//     */
//    @Override
//    public ExchangeRate getExchangeRate(String baseCode, String termCode) {
//        return super.getExchangeRate(baseCode, termCode);
//        // throw new CurrencyConversionException()
//    }
//
//    /**
//     * The method reverses the {@link ExchangeRate} to a rate mapping from term
//     * to base {@link CurrencyUnit}. Hereby the factor must <b>not</b> be
//     * recalculated as {@code 1/oldFactor}, since typically reverse rates are
//     * not symmetric in most cases.
//     *
//     * @param rate The exchange rate
//     * @return the matching reversed {@link ExchangeRate}, or {@code null}, if
//     * the rate cannot be reversed.
//     */
//    @Override
//    public ExchangeRate getReversed(ExchangeRate rate) {
//        return super.getReversed(rate);
//    }
//
//    /**
//     * Access a {@link CurrencyConversion} that can be applied as a
//     * {@link MonetaryOperator} to an amount.
//     *
//     * @param termCode terminal/target currency code, not {@code null}
//     * @return a new instance of a corresponding {@link CurrencyConversion},
//     * never {@code null}.
//     * @throws MonetaryException if one of the currency codes passed is not valid.
//     */
//    @Override
//    public CurrencyConversion getCurrencyConversion(String termCode) {
//        return super.getCurrencyConversion(termCode);
//    }
//
//    private ExchangeRateBuilder getBuilder(ConversionQuery query) {
//        ConversionContext conversionContext = ConversionContextBuilder
//                .create(getContext(), RateType.ANY)
//                .build();
//        ExchangeRateBuilder builder = new ExchangeRateBuilder(conversionContext);
//
//        builder.setBase(query.getBaseCurrency());
//        builder.setTerm(query.getCurrency());
//
//        return builder;
//    }
//
//    private ExchangeRate createExchangeRate(CurrencyUnit destination) {
//        // Required elements are: base, term, factor, & context.
//
//        ExchangeRateBuilder builder = new ExchangeRateBuilder(ConversionContext.ANY_CONVERSION);
//        builder.setBase(Monetary.getCurrency(BASE_CURRENCY_CODE));
//        builder.setTerm(destination);
//
//        CurrencyRepository repo = new CurrencyRepository(this.context);
//        Currency destinationCurrencyEntity = repo.loadCurrency(destination.getCurrencyCode());
//
//        Double baseConversionRate = destinationCurrencyEntity.getBaseConversionRate();
//        NumberValue value = DefaultNumberValue.of(baseConversionRate);
//        builder.setFactor(value);
//
//        return builder.build();
//    }
//
//    private ExchangeRate createExchangeRate(ConversionQuery query, ExchangeRateBuilder builder,
//                                            ExchangeRate sourceRate, ExchangeRate target) {
//
//        if (areBothBaseCurrencies(query)) {
//            builder.setFactor(DefaultNumberValue.ONE);
//            return builder.build();
//        } else if (BASE_CURRENCY_CODE.equals(query.getCurrency().getCurrencyCode())) {
//            if (sourceRate == null) {
//                return null;
//            }
//            return reverse(sourceRate);
//        } else if (BASE_CURRENCY_CODE.equals(query.getBaseCurrency().getCurrencyCode())) {
//            return target;
//        } else {
//            // Get Conversion base as derived rate: base -> EUR -> term
//            ExchangeRate rate1 = getExchangeRate(
//                    query.toBuilder().setTermCurrency(Monetary.getCurrency(BASE_CURRENCY_CODE)).build());
//            ExchangeRate rate2 = getExchangeRate(
//                    query.toBuilder().setBaseCurrency(Monetary.getCurrency(BASE_CURRENCY_CODE))
//                            .setTermCurrency(query.getCurrency()).build());
//            if (rate1!=null && rate2!=null) {
//                builder.setFactor(multiply(rate1.getFactor(), rate2.getFactor()));
//                builder.setRateChain(rate1, rate2);
//                return builder.build();
//            }
//            throw new CurrencyConversionException(query.getBaseCurrency(),
//                    query.getCurrency(), sourceRate.getContext());
//        }
//    }
//
//    private boolean areBothBaseCurrencies(ConversionQuery query) {
//        return BASE_CURRENCY_CODE.equals(query.getBaseCurrency().getCurrencyCode()) &&
//                BASE_CURRENCY_CODE.equals(query.getCurrency().getCurrencyCode());
//    }
//
//    private ExchangeRate reverse(ExchangeRate rate) {
//        if (rate==null) {
//            throw new IllegalArgumentException("Rate null is not reversible.");
//        }
//        return new ExchangeRateBuilder(rate).setRate(rate).setBase(rate.getCurrency()).setTerm(rate.getBaseCurrency())
//                .setFactor(divide(DefaultNumberValue.ONE, rate.getFactor(), MathContext.DECIMAL64)).build();
//    }
//
//    private String loadBaseCurrencyCode(Context context) {
//        CurrencyService service = new CurrencyService(context);
//        return service.getBaseCurrencyCode();
//    }
//}
