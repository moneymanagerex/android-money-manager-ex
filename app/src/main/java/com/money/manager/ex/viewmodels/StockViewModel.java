package com.money.manager.ex.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.investment.SecurityPriceModel;
import com.money.manager.ex.investment.yahoofinance.StockPriceRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// StockViewModel.java
public class StockViewModel extends AndroidViewModel {
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final AccountRepository accountRepository;
    private final MutableLiveData<List<Stock>> stocks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Account> account = new MutableLiveData<>();
    private final MutableLiveData<SecurityPriceModel> latestDownloadedPrice = new MutableLiveData<>();

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

    public void loadStocks(long accountId) {
        isLoading.postValue(true);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.execute(() -> {
            List<Stock> result = stockRepository.loadByAccount(accountId);
            stocks.postValue(result);
        });

        executor.execute(() -> {
            Account acc = accountRepository.load(accountId);
            account.postValue(acc);
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
}