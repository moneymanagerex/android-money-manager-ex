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
package com.money.manager.ex.investment.morningstar;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.StockHistoryRepositorySql;
import com.money.manager.ex.datalayer.StockRepositorySql;
import com.money.manager.ex.investment.prices.ISecurityPriceUpdater;
import com.money.manager.ex.investment.prices.PriceUpdaterBase;
import com.money.manager.ex.investment.events.AllPricesDownloadedEvent;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite.BriteDatabase;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Quote provider: Morningstar
 */
public class MorningstarPriceUpdater
    extends PriceUpdaterBase
    implements ISecurityPriceUpdater {

    @Inject
    public MorningstarPriceUpdater(Context context) {
        super(context);

        MmexApplication.getApp().iocComponent.inject(this);
    }

    /**
     * Tracks the number of records to update. Used to close progress binaryDialog when all done.
     */
    private int mCounter;
    private int mTotalRecords;
    private CompositeSubscription compositeSubscription;
    @Inject Lazy<StockRepositorySql> stockRepository;
    @Inject Lazy<StockHistoryRepositorySql> stockHistoryRepository;
    private SymbolConverter symbolConverter;
    private IMorningstarService service;

    @Override
    public void downloadPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) return;
        mTotalRecords = symbols.size();
        if (mTotalRecords == 0) return;

        showProgressDialog(mTotalRecords);

        service = getMorningstarService();
        compositeSubscription = new CompositeSubscription();
        symbolConverter = new SymbolConverter();

//        processSequentially(symbols);
        processInParallel(symbols);
    }

//    private void processSequentially(List<String> symbols) {
//        compositeSubscription.add(Observable.from(symbols)
//                .subscribeOn(Schedulers.io())
//                .map(new Func1<String, String>() {
//                    @Override
//                    public String call(String s) {
//                        // get a Morningstar symbol
//                        return symbolConverter.convert(s);
//                    }
//                })
//                .observeOn(Schedulers.io())     // Observe the network call on IO thread!
//                .flatMap(new Func1<String, Observable<Pair<String, String>>>() {
//                    @Override
//                    public Observable<Pair<String, String>> call(final String s) {
//                        // download the price
//                        return service.getPrice(s)
//                                .doOnError(new Action1<Throwable>() {
//                                    @Override
//                                    public void call(Throwable throwable) {
//                                        mCounter++;
//                                        setProgress(mCounter);
//
//                                        // report to the UI
//                                        Timber.e(throwable, "fetching %s", s);
//                                    }
//                                })
//                                .onErrorResumeNext(Observable.<String>empty())
//                                .map(new Func1<String, Pair<String, String>>() {
//                                    @Override
//                                    public Pair<String, String> call(String price) {
//                                        return Pair.of(s, price);
//                                    }
//                                });
//                    }
//                })
//                .map(new Func1<Pair<String, String>, PriceDownloadedEvent>() {
//                    @Override
//                    public PriceDownloadedEvent call(Pair<String, String> s) {
//                        // parse the price
//                        return parse(s.getLeft(), s.getRight());
//                    }
//                })
//                .doOnNext(new Action1<PriceDownloadedEvent>() {
//                    @Override
//                    public void call(PriceDownloadedEvent priceDownloadedEvent) {
//                        // update to database
//                        savePrice(priceDownloadedEvent);
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<PriceDownloadedEvent>() {
//                    @Override
//                    public void onCompleted() {
//                        closeProgressDialog();
//
//                        new UIHelper(getContext()).showToast(R.string.download_complete);
//
//                        compositeSubscription.unsubscribe();
//
//                        // send event to reload the data.
//                        EventBus.getDefault().post(new AllPricesDownloadedEvent());
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        closeProgressDialog();
//
//                        Timber.e(e, "downloading prices");
//                    }
//
//                    @Override
//                    public void onNext(PriceDownloadedEvent event) {
//                        mCounter++;
//                        setProgress(mCounter);
//
//                        // todo: update price in the UI?
//                        // todo: remove the progress bar in that case.
//                    }
//                })
//        );
//    }

    private void processInParallel(List<String> symbols) {
        for(int i = 0; i < symbols.size(); i++) {
            final String symbol = symbols.get(i);
            final String morningstarSymbol = symbolConverter.convert(symbol);

            compositeSubscription.add(
                    service.getPrice(morningstarSymbol)
                        .subscribeOn(Schedulers.io())
                            .doOnNext(new Action1<String>() {
                                @Override
                                public void call(String s) {
                                    PriceDownloadedEvent event = parse(morningstarSymbol, s);
                                    savePrice(event);
                                }
                            })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {
                                finishIfAllDone();
                            }

                            @Override
                            public void onError(Throwable e) {
                                mCounter++;
                                setProgress(mCounter);

                                Timber.e(e, "error downloading price %s", symbol);

                                finishIfAllDone();
                            }

                            @Override
                            public void onNext(String s) {
                                mCounter++;
                                setProgress(mCounter);
                            }
                        })
            );
        }
        // unsubscribe if the user navigates away while downloading prices?
    }

    /**
     * Parse Morningstar response into price information.
     * @param symbol Morningstar symbol
     * @param html Result
     * @return An object containing price details
     */
    private PriceDownloadedEvent parse(String symbol, String html) {
        Document doc = Jsoup.parse(html);

        // symbol
        String yahooSymbol = symbolConverter.getYahooSymbol(symbol);

        // price
        String priceString = doc.body().getElementById("last-price-value").text();
        if (TextUtils.isEmpty(priceString)) {
            throw new RuntimeException("No price available for " + symbol);
        }
        Money price = MoneyFactory.fromString(priceString);
        // currency
        String currency = doc.body().getElementById("curency").text();
        if (currency.equals("GBX")) {
            price = price.divide(100, MoneyFactory.MAX_ALLOWED_PRECISION);
        }

        // date
        String dateString = doc.body().getElementById("asOfDate").text();
        String dateFormat = "MM/dd/yyyy HH:mm:ss";
//        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);
        // the time zone is EST
//        DateTime date = formatter.withZone(DateTimeZone.forID("America/New_York"))
//                .parseDateTime(dateString)
//                .withZone(DateTimeZone.forID("Europe/Vienna"));
        // convert time zone
        MmxDate dateTime = new MmxDate(dateString, dateFormat)
                .setTimeZone("America/New_York")
                .inTimeZone("Europe/Vienna");

        // todo: should this be converted to the exchange time?

        return new PriceDownloadedEvent(yahooSymbol, price, dateTime.toDate());
    }

    private synchronized void finishIfAllDone() {
        if (mCounter != mTotalRecords) return;

        compositeSubscription.unsubscribe();

        closeProgressDialog();

        // Notify user that all the prices have been downloaded.
        new UIHelper(getContext()).showToast(R.string.download_complete);

        // fire an event so that the data can be reloaded.
        EventBus.getDefault().post(new AllPricesDownloadedEvent());
    }

    private void savePrice(PriceDownloadedEvent event) {
        BriteDatabase.Transaction tx = stockRepository.get().database.newTransaction();

        // update the current price of the stock.
        stockRepository.get().updateCurrentPrice(event.symbol, event.price);

        // update price history record.
        stockHistoryRepository.get().addStockHistoryRecord(event.symbol,
                event.price, event.date);

        tx.markSuccessful();
        tx.end();
    }

    private IMorningstarService getMorningstarService() {
        String BASE_URL = "http://quotes.morningstar.com";

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        return retrofit.create(IMorningstarService.class);
    }
}
