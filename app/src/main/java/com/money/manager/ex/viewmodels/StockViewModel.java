package com.money.manager.ex.viewmodels;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.investment.SecurityPriceModel;
import com.money.manager.ex.investment.yahoofinance.StockPriceRepository;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import android.text.TextUtils;

import java.util.LinkedHashSet;

// StockViewModel.java
public class StockViewModel extends AndroidViewModel {
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final AccountRepository accountRepository;
    private final MutableLiveData<List<Stock>> stocks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Account> account = new MutableLiveData<>();
    private final MutableLiveData<Money> accountBalance = new MutableLiveData<>();
    private final MutableLiveData<SecurityPriceModel> latestDownloadedPrice = new MutableLiveData<>();
    private final MutableLiveData<int[]> allDownloadedPricesResult = new MutableLiveData<>();

    public StockViewModel(@NonNull Application application, StockRepository repository) {
        super(application);
        this.stockRepository = repository;
        this.stockPriceRepository = new StockPriceRepository(application);
        this.accountRepository = new AccountRepository(application);
    }

    public LiveData<List<Stock>> getStocks() { return stocks; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<SecurityPriceModel> getLatestDownloadedPrice() { return latestDownloadedPrice; }
    public LiveData<Account> getAccount() {return account; }
    public LiveData<Money> getAccountBalance() { return accountBalance; }
    public LiveData<int[]> getAllDownloadedPricesResult() { return allDownloadedPricesResult; }

    public void clearDownloadEvents() {
        latestDownloadedPrice.setValue(null);
        allDownloadedPricesResult.setValue(null);
    }

    public void loadStocks(long accountId) {
        isLoading.postValue(true);
        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.execute(() -> {
            List<Stock> result = stockRepository.loadByAccount(accountId);
            stocks.postValue(result);
        });

        executor.execute(() -> {
            Account acc = accountRepository.load(accountId);
            account.postValue(acc);
        });

        executor.execute(() -> {
            QueryAccountBills query = new QueryAccountBills(getApplication());
            Select select = new Select().where(QueryAccountBills.ACCOUNTID + "=?", Long.toString(accountId));
            Cursor c = getApplication().getContentResolver().query(
                    query.getUri(), null,
                    select.selection, select.selectionArgs, null);
            Money balance = MoneyFactory.fromDouble(0);
            if (c != null) {
                if (c.moveToFirst()) {
                    balance = MoneyFactory.fromString(
                            Double.toString(c.getDouble(c.getColumnIndexOrThrow(QueryAccountBills.TOTAL))));
                }
                c.close();
            }
            accountBalance.postValue(balance);
        });

        executor.execute(() -> isLoading.postValue(false));

        executor.shutdown();
    }

    public void downloadStockPrice(String symbol) {
        isLoading.postValue(true);
        stockPriceRepository.downloadPrice(symbol).observeForever(priceModel -> {
            if (priceModel != null) {
                latestDownloadedPrice.postValue(priceModel);
            }
            isLoading.postValue(false);
        });
    }

    public void downloadAllStockPrices(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            allDownloadedPricesResult.postValue(new int[]{0, 0});
            return;
        }

        Set<String> symbols = new LinkedHashSet<>();
        for (Stock stock : stocks) {
            if (stock == null || TextUtils.isEmpty(stock.getSymbol())) continue;
            symbols.add(stock.getSymbol().trim());
        }

        if (symbols.isEmpty()) {
            allDownloadedPricesResult.postValue(new int[]{0, 0});
            return;
        }

        isLoading.postValue(true);

        final int total = symbols.size();
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger successful = new AtomicInteger(0);

        for (String symbol : symbols) {
            LiveData<SecurityPriceModel> source = stockPriceRepository.downloadPrice(symbol);
            Observer<SecurityPriceModel> observer = new Observer<>() {
                @Override
                public void onChanged(SecurityPriceModel priceModel) {
                    if (priceModel != null) {
                        successful.incrementAndGet();
                        latestDownloadedPrice.postValue(priceModel);
                    }

                    source.removeObserver(this);

                    if (completed.incrementAndGet() == total) {
                        isLoading.postValue(false);
                        allDownloadedPricesResult.postValue(new int[]{successful.get(), total});
                    }
                }
            };
            source.observeForever(observer);
        }
    }
}